package com.configvault.service;

import com.configvault.dto.ConfigRequest;
import com.configvault.dto.ConfigResponse;
import com.configvault.model.AuditAction;
import com.configvault.model.Config;
import com.configvault.model.ConfigVersion;
import com.configvault.model.Environment;
import com.configvault.repository.ConfigRepository;
import com.configvault.repository.ConfigVersionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigServiceTest {

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private ConfigVersionRepository configVersionRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ConfigService configService;

    private void setSecurityContext(String username, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateConfig_savesEntityAndLogsAudit() {
        setSecurityContext("devUser", "ROLE_DEVELOPER");

        ConfigRequest req = new ConfigRequest("AppConfig", Environment.DEV, "key=value", "Init");

        Config savedConfig = Config.builder().id(1L).name("AppConfig").environment(Environment.DEV).build();
        ConfigVersion savedVersion = ConfigVersion.builder().id(1L).versionNumber(1).content("key=value").build();

        when(configRepository.save(any(Config.class))).thenReturn(savedConfig);
        when(configVersionRepository.save(any(ConfigVersion.class))).thenReturn(savedVersion);

        ConfigResponse response = configService.createConfig(req);

        assertNotNull(response);
        assertEquals("AppConfig", response.getName());
        verify(configRepository).save(any(Config.class));
        verify(configVersionRepository).save(any(ConfigVersion.class));
        verify(auditLogService).log(eq(1L), eq(AuditAction.CREATE), eq("devUser"), contains("Created config"));
    }

    @Test
    void testUpdateConfig_savesVersionBeforeUpdating() {
        setSecurityContext("devUser", "ROLE_DEVELOPER");

        Config existingConfig = Config.builder().id(1L).name("AppConfig").environment(Environment.DEV).build();
        ConfigVersion prevVersion = ConfigVersion.builder().id(1L).versionNumber(1).content("old=value").build();

        ConfigRequest updateReq = new ConfigRequest("AppConfig", Environment.DEV, "new=value", "Update");

        when(configRepository.findById(1L)).thenReturn(Optional.of(existingConfig));
        when(configRepository.save(any(Config.class))).thenReturn(existingConfig);
        when(configVersionRepository.findByConfigIdOrderByVersionNumberDesc(1L)).thenReturn(List.of(prevVersion));
        
        ConfigVersion newSavedVersion = ConfigVersion.builder().id(2L).versionNumber(2).content("new=value").build();
        when(configVersionRepository.save(any(ConfigVersion.class))).thenReturn(newSavedVersion);

        ConfigResponse response = configService.updateConfig(1L, updateReq);

        assertEquals(2, response.getVersionNumber());
        assertEquals("new=value", response.getContent());
        verify(configVersionRepository).save(argThat(v -> v.getVersionNumber() == 2 && v.getContent().equals("new=value")));
        verify(auditLogService).log(eq(1L), eq(AuditAction.UPDATE), eq("devUser"), contains("New version: 2"));
    }

    @Test
    void testRollback_restoresContentAndLogsAudit() {
        setSecurityContext("adminUser", "ROLE_ADMIN");

        Config existingConfig = Config.builder().id(1L).name("AppConfig").environment(Environment.DEV).build();
        ConfigVersion v1 = ConfigVersion.builder().id(1L).versionNumber(1).content("old=value").build();
        ConfigVersion v2 = ConfigVersion.builder().id(2L).versionNumber(2).content("new=value").build();

        when(configRepository.findById(1L)).thenReturn(Optional.of(existingConfig));
        when(configVersionRepository.findByConfigIdAndVersionNumber(1L, 1)).thenReturn(Optional.of(v1));
        when(configVersionRepository.findByConfigIdOrderByVersionNumberDesc(1L)).thenReturn(List.of(v2, v1));
        
        ConfigVersion rolledBackVersion = ConfigVersion.builder().id(3L).versionNumber(3).content("old=value").build();
        when(configVersionRepository.save(any(ConfigVersion.class))).thenReturn(rolledBackVersion);

        ConfigResponse response = configService.rollback(1L, 1);

        assertEquals(3, response.getVersionNumber());
        assertEquals("old=value", response.getContent());
        verify(configVersionRepository).save(argThat(v -> v.getVersionNumber() == 3 && v.getContent().equals("old=value")));
        verify(auditLogService).log(eq(1L), eq(AuditAction.ROLLBACK), eq("adminUser"), contains("Rolled back to version 1"));
    }

    @Test
    void testPromote_blocksDeveloperFromPromotingToProd() {
        setSecurityContext("devUser", "ROLE_DEVELOPER");

        assertThrows(AccessDeniedException.class, () -> {
            configService.promote(1L, Environment.PROD);
        });
        
        verifyNoInteractions(configRepository);
        verifyNoInteractions(configVersionRepository);
        verifyNoInteractions(auditLogService);
    }
}

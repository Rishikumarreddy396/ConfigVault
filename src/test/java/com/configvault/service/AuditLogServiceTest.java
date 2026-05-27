package com.configvault.service;

import com.configvault.model.AuditAction;
import com.configvault.model.AuditLog;
import com.configvault.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testLog_persistsAuditEntry() {
        Long configId = 1L;
        AuditAction action = AuditAction.CREATE;
        String performedBy = "test_user";
        String details = "Test details";

        auditLogService.log(configId, action, performedBy, details);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals(configId, savedLog.getConfigId());
        assertEquals(action, savedLog.getAction());
        assertEquals(performedBy, savedLog.getPerformedBy());
        assertEquals(details, savedLog.getDetails());
    }
}

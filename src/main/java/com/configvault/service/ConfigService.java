package com.configvault.service;

import com.configvault.dto.ConfigRequest;
import com.configvault.dto.ConfigResponse;
import com.configvault.exception.ResourceNotFoundException;
import com.configvault.model.AuditAction;
import com.configvault.model.Config;
import com.configvault.model.ConfigVersion;
import com.configvault.repository.ConfigRepository;
import com.configvault.repository.ConfigVersionRepository;
import com.configvault.dto.ConfigVersionResponse;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;
    private final ConfigVersionRepository configVersionRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ConfigResponse createConfig(ConfigRequest request) {
        // Create Config
        Config config = Config.builder()
                .name(request.getName())
                .environment(request.getEnvironment())
                .createdBy("system") // Will be updated in COMMIT 7
                .build();
        config = configRepository.save(config);

        // Create first ConfigVersion
        ConfigVersion version = ConfigVersion.builder()
                .config(config)
                .content(request.getContent())
                .versionNumber(1)
                .changedBy("system") // Will be updated in COMMIT 7
                .commitMessage(request.getCommitMessage())
                .build();
        version = configVersionRepository.save(version);

        auditLogService.log(config.getId(), AuditAction.CREATE, "system", "Created config: " + config.getName());

        return mapToResponse(config, version);
    }

    @Transactional
    public ConfigResponse updateConfig(Long id, ConfigRequest request) {
        Config config = configRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with id: " + id));

        // Update Config metadata if necessary
        config.setName(request.getName());
        config.setEnvironment(request.getEnvironment());
        config = configRepository.save(config);

        // Fetch latest version number
        List<ConfigVersion> versions = configVersionRepository.findByConfigIdOrderByVersionNumberDesc(id);
        int nextVersionNumber = versions.isEmpty() ? 1 : versions.get(0).getVersionNumber() + 1;

        // Auto-save the new content as a new ConfigVersion row
        ConfigVersion newVersion = ConfigVersion.builder()
                .config(config)
                .content(request.getContent())
                .versionNumber(nextVersionNumber)
                .changedBy("system") // Will be updated in COMMIT 7
                .commitMessage(request.getCommitMessage())
                .build();
        newVersion = configVersionRepository.save(newVersion);

        auditLogService.log(config.getId(), AuditAction.UPDATE, "system", "Updated config content. New version: " + nextVersionNumber);

        return mapToResponse(config, newVersion);
    }

    @Transactional(readOnly = true)
    public ConfigResponse getConfigById(Long id) {
        Config config = configRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with id: " + id));

        List<ConfigVersion> versions = configVersionRepository.findByConfigIdOrderByVersionNumberDesc(id);
        ConfigVersion latestVersion = versions.isEmpty() ? new ConfigVersion() : versions.get(0);

        return mapToResponse(config, latestVersion);
    }

    @Transactional(readOnly = true)
    public List<ConfigResponse> getAllConfigs() {
        return configRepository.findAll().stream().map(config -> {
            List<ConfigVersion> versions = configVersionRepository.findByConfigIdOrderByVersionNumberDesc(config.getId());
            ConfigVersion latestVersion = versions.isEmpty() ? new ConfigVersion() : versions.get(0);
            return mapToResponse(config, latestVersion);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConfigVersionResponse> getHistory(Long configId) {
        if (!configRepository.existsById(configId)) {
            throw new ResourceNotFoundException("Config not found with id: " + configId);
        }
        return configVersionRepository.findByConfigIdOrderByVersionNumberDesc(configId).stream()
                .map(this::mapToVersionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getDiff(Long configId, Integer v1, Integer v2) {
        ConfigVersion version1 = configVersionRepository.findByConfigIdAndVersionNumber(configId, v1)
                .orElseThrow(() -> new ResourceNotFoundException("Version " + v1 + " not found for config id: " + configId));
        ConfigVersion version2 = configVersionRepository.findByConfigIdAndVersionNumber(configId, v2)
                .orElseThrow(() -> new ResourceNotFoundException("Version " + v2 + " not found for config id: " + configId));

        List<String> original = Arrays.asList(version1.getContent().split("\\r?\\n"));
        List<String> revised = Arrays.asList(version2.getContent().split("\\r?\\n"));

        Patch<String> patch = DiffUtils.diff(original, revised);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                "v" + v1, "v" + v2, original, patch, 3);
        
        return String.join("\n", unifiedDiff);
    }

    @Transactional
    public ConfigResponse rollback(Long configId, Integer versionNumber) {
        Config config = configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with id: " + configId));

        ConfigVersion targetVersion = configVersionRepository.findByConfigIdAndVersionNumber(configId, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Version " + versionNumber + " not found for config id: " + configId));

        List<ConfigVersion> versions = configVersionRepository.findByConfigIdOrderByVersionNumberDesc(configId);
        int nextVersionNumber = versions.isEmpty() ? 1 : versions.get(0).getVersionNumber() + 1;

        ConfigVersion newVersion = ConfigVersion.builder()
                .config(config)
                .content(targetVersion.getContent())
                .versionNumber(nextVersionNumber)
                .changedBy("system") // Will be updated in COMMIT 7
                .commitMessage("Rollback to version " + versionNumber)
                .build();
        newVersion = configVersionRepository.save(newVersion);

        auditLogService.log(config.getId(), AuditAction.ROLLBACK, "system", "Rolled back to version " + versionNumber);

        return mapToResponse(config, newVersion);
    }

    @Transactional
    public ConfigResponse promote(Long configId, com.configvault.model.Environment targetEnvironment) {
        Config originalConfig = configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with id: " + configId));

        List<ConfigVersion> versions = configVersionRepository.findByConfigIdOrderByVersionNumberDesc(configId);
        ConfigVersion latestVersion = versions.isEmpty() ? new ConfigVersion() : versions.get(0);

        Config promotedConfig = Config.builder()
                .name(originalConfig.getName())
                .environment(targetEnvironment)
                .createdBy("system") // Will be updated in COMMIT 7
                .build();
        promotedConfig = configRepository.save(promotedConfig);

        ConfigVersion newVersion = ConfigVersion.builder()
                .config(promotedConfig)
                .content(latestVersion.getContent())
                .versionNumber(1)
                .changedBy("system") // Will be updated in COMMIT 7
                .commitMessage("Promoted from " + originalConfig.getEnvironment() + " config id: " + configId)
                .build();
        newVersion = configVersionRepository.save(newVersion);

        auditLogService.log(configId, AuditAction.PROMOTE, "system", "Promoted to " + targetEnvironment + " as new config id " + promotedConfig.getId());
        auditLogService.log(promotedConfig.getId(), AuditAction.CREATE, "system", "Created via promotion from config id " + configId);

        return mapToResponse(promotedConfig, newVersion);
    }

    private ConfigResponse mapToResponse(Config config, ConfigVersion version) {
        return ConfigResponse.builder()
                .id(config.getId())
                .name(config.getName())
                .environment(config.getEnvironment())
                .content(version.getContent())
                .versionNumber(version.getVersionNumber())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .createdBy(config.getCreatedBy())
                .build();
    }

    private ConfigVersionResponse mapToVersionResponse(ConfigVersion version) {
        return ConfigVersionResponse.builder()
                .id(version.getId())
                .configId(version.getConfig().getId())
                .content(version.getContent())
                .versionNumber(version.getVersionNumber())
                .changedBy(version.getChangedBy())
                .changedAt(version.getChangedAt())
                .commitMessage(version.getCommitMessage())
                .build();
    }
}

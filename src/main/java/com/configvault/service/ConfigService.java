package com.configvault.service;

import com.configvault.dto.ConfigRequest;
import com.configvault.dto.ConfigResponse;
import com.configvault.exception.ResourceNotFoundException;
import com.configvault.model.Config;
import com.configvault.model.ConfigVersion;
import com.configvault.repository.ConfigRepository;
import com.configvault.repository.ConfigVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;
    private final ConfigVersionRepository configVersionRepository;

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
}

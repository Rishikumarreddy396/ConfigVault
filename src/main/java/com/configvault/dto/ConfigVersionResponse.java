package com.configvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigVersionResponse {
    private Long id;
    private Long configId;
    private String content;
    private Integer versionNumber;
    private String changedBy;
    private LocalDateTime changedAt;
    private String commitMessage;
}

package com.configvault.dto;

import com.configvault.model.Environment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigRequest {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotNull(message = "Environment is mandatory")
    private Environment environment;

    @NotBlank(message = "Content is mandatory")
    private String content;

    private String commitMessage;
}

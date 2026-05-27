package com.configvault.controller;

import com.configvault.dto.ConfigRequest;
import com.configvault.dto.ConfigResponse;
import com.configvault.service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
@Tag(name = "Configurations", description = "Endpoints for managing application configurations")
public class ConfigController {

    private final ConfigService configService;

    @PostMapping
    @Operation(summary = "Create a new configuration")
    @ApiResponse(responseCode = "201", description = "Configuration created successfully")
    public ResponseEntity<ConfigResponse> createConfig(@Valid @RequestBody ConfigRequest request) {
        ConfigResponse response = configService.createConfig(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all configurations")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all configurations")
    public ResponseEntity<List<ConfigResponse>> getAllConfigs() {
        List<ConfigResponse> responses = configService.getAllConfigs();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a configuration by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved configuration")
    @ApiResponse(responseCode = "404", description = "Configuration not found")
    public ResponseEntity<ConfigResponse> getConfigById(@PathVariable Long id) {
        ConfigResponse response = configService.getConfigById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing configuration")
    @ApiResponse(responseCode = "200", description = "Configuration updated successfully")
    @ApiResponse(responseCode = "404", description = "Configuration not found")
    public ResponseEntity<ConfigResponse> updateConfig(@PathVariable Long id, @Valid @RequestBody ConfigRequest request) {
        ConfigResponse response = configService.updateConfig(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get version history of a configuration")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved history")
    public ResponseEntity<List<com.configvault.dto.ConfigVersionResponse>> getHistory(@PathVariable Long id) {
        List<com.configvault.dto.ConfigVersionResponse> history = configService.getHistory(id);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @GetMapping("/{id}/diff")
    @Operation(summary = "Get unified diff between two versions of a configuration")
    @ApiResponse(responseCode = "200", description = "Successfully generated diff")
    @ApiResponse(responseCode = "404", description = "Version not found")
    public ResponseEntity<String> getDiff(@PathVariable Long id, @RequestParam Integer v1, @RequestParam Integer v2) {
        String diff = configService.getDiff(id, v1, v2);
        return new ResponseEntity<>(diff, HttpStatus.OK);
    }

    @PostMapping("/{id}/rollback/{version}")
    @Operation(summary = "Rollback a configuration to a specific version")
    @ApiResponse(responseCode = "200", description = "Successfully rolled back configuration")
    public ResponseEntity<ConfigResponse> rollback(@PathVariable Long id, @PathVariable Integer version) {
        ConfigResponse response = configService.rollback(id, version);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/promote")
    @Operation(summary = "Promote a configuration to another environment")
    @ApiResponse(responseCode = "201", description = "Successfully promoted configuration")
    public ResponseEntity<ConfigResponse> promote(@PathVariable Long id, @RequestParam com.configvault.model.Environment to) {
        ConfigResponse response = configService.promote(id, to);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

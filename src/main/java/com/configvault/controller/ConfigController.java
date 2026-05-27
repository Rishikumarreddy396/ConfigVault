package com.configvault.controller;

import com.configvault.dto.ConfigRequest;
import com.configvault.dto.ConfigResponse;
import com.configvault.service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @PostMapping
    public ResponseEntity<ConfigResponse> createConfig(@Valid @RequestBody ConfigRequest request) {
        ConfigResponse response = configService.createConfig(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ConfigResponse>> getAllConfigs() {
        List<ConfigResponse> responses = configService.getAllConfigs();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfigResponse> getConfigById(@PathVariable Long id) {
        ConfigResponse response = configService.getConfigById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConfigResponse> updateConfig(@PathVariable Long id, @Valid @RequestBody ConfigRequest request) {
        ConfigResponse response = configService.updateConfig(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

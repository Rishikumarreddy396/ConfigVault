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

    @GetMapping("/{id}/history")
    public ResponseEntity<List<com.configvault.dto.ConfigVersionResponse>> getHistory(@PathVariable Long id) {
        List<com.configvault.dto.ConfigVersionResponse> history = configService.getHistory(id);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @GetMapping("/{id}/diff")
    public ResponseEntity<String> getDiff(@PathVariable Long id, @RequestParam Integer v1, @RequestParam Integer v2) {
        String diff = configService.getDiff(id, v1, v2);
        return new ResponseEntity<>(diff, HttpStatus.OK);
    }

    @PostMapping("/{id}/rollback/{version}")
    public ResponseEntity<ConfigResponse> rollback(@PathVariable Long id, @PathVariable Integer version) {
        ConfigResponse response = configService.rollback(id, version);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/promote")
    public ResponseEntity<ConfigResponse> promote(@PathVariable Long id, @RequestParam com.configvault.model.Environment to) {
        ConfigResponse response = configService.promote(id, to);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

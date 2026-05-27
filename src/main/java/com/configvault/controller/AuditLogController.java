package com.configvault.controller;

import com.configvault.model.AuditLog;
import com.configvault.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Endpoints for viewing audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get all audit logs", description = "Returns all audit logs ordered by performedAt descending")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> auditLogs = auditLogService.getAllAuditLogs();
        return new ResponseEntity<>(auditLogs, HttpStatus.OK);
    }
}

package com.configvault.service;

import com.configvault.model.AuditAction;
import com.configvault.model.AuditLog;
import com.configvault.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(Long configId, AuditAction action, String performedBy, String details) {
        AuditLog auditLog = AuditLog.builder()
                .configId(configId)
                .action(action)
                .performedBy(performedBy)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByPerformedAtDesc();
    }
}

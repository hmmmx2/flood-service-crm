package com.fyp.floodmonitoring.service;

import com.fyp.floodmonitoring.entity.AdminAuditLog;
import com.fyp.floodmonitoring.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogRepository repository;

    public void record(UUID actorUserId, String action, String targetType, String targetId, String details) {
        repository.save(AdminAuditLog.builder()
                .actorUserId(actorUserId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build());
    }

    public Page<AdminAuditLog> list(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(safePage, safeSize));
    }
}

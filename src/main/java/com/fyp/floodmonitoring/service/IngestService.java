package com.fyp.floodmonitoring.service;

import com.fyp.floodmonitoring.dto.request.IngestRequest;
import com.fyp.floodmonitoring.dto.response.IngestResponse;
import com.fyp.floodmonitoring.entity.Event;
import com.fyp.floodmonitoring.entity.Node;
import com.fyp.floodmonitoring.repository.EventRepository;
import com.fyp.floodmonitoring.repository.NodeRepository;
import com.fyp.floodmonitoring.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Handles IoT sensor data ingestion.
 *
 * On each call to ingest():
 *   1. Looks up the node by nodeId — auto-creates a row on first sight of a new nodeId
 *   2. Detects if the flood level has changed
 *   3. Saves a new event row (always)
 *   4. Updates node.currentLevel + node.lastUpdated
 *   5. If level >= 2 and it's a level increase, fires push notifications asynchronously
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestService {

    private final NodeRepository          nodeRepository;
    private final EventRepository         eventRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional
    @CacheEvict(value = {"analytics", "dashboard"}, allEntries = true)
    public IngestResponse ingest(IngestRequest req) {
        Node node = nodeRepository.findByNodeId(req.nodeId())
                .orElseGet(() -> provisionNewNode(req));

        int previousLevel = node.getCurrentLevel() != null ? node.getCurrentLevel() : 0;
        int newLevel      = req.level();
        boolean levelRaised = newLevel > previousLevel;

        // Always record the event regardless of level change
        Event event = Event.builder()
                .nodeId(req.nodeId())
                .eventType(newLevel >= 2 ? "ALERT" : "UPDATE")
                .newLevel(newLevel)
                .createdAt(req.timestamp() != null ? req.timestamp() : Instant.now())
                .build();
        eventRepository.save(event);

        // Update node state
        node.setCurrentLevel(newLevel);
        node.setLastUpdated(Instant.now());
        nodeRepository.save(node);

        // Fire push notifications asynchronously if level rose to Warning or Critical
        boolean alertFired = false;
        if (levelRaised && newLevel >= 2) {
            String area = node.getArea() != null ? node.getArea() : "Kuching";
            pushNotificationService.notifyLevelChange(req.nodeId(), newLevel, area);
            alertFired = true;
            log.info("[Ingest] Alert fired: nodeId={} level={}->{}", req.nodeId(), previousLevel, newLevel);
        }

        log.debug("[Ingest] nodeId={} level={}->{} alertFired={}", req.nodeId(), previousLevel, newLevel, alertFired);
        return new IngestResponse(true, req.nodeId(), alertFired);
    }

    private Node provisionNewNode(IngestRequest req) {
        double lat = req.latitude() != null ? req.latitude() : GeoUtils.KUCHING_LAT;
        double lon = req.longitude() != null ? req.longitude() : GeoUtils.KUCHING_LON;
        Node n = Node.builder()
                .nodeId(req.nodeId())
                .name("Node " + req.nodeId())
                .latitude(lat)
                .longitude(lon)
                .currentLevel(0)
                .isDead(false)
                .area("Kuching")
                .location("")
                .state("Sarawak")
                .lastUpdated(Instant.now())
                .createdAt(Instant.now())
                .build();
        log.info("[Ingest] Auto-provisioned node nodeId={} lat={} lon={}", req.nodeId(), lat, lon);
        return nodeRepository.save(n);
    }
}

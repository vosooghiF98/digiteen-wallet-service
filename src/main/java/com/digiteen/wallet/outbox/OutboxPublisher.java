package com.digiteen.wallet.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "app.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public OutboxPublisher(OutboxEventRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           @Value("${app.kafka.topic}") String topic) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:250}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : repository.findPending(PageRequest.of(0, 100))) {
            try {
                kafkaTemplate.send(topic, event.getId().toString(), event.getPayload()).get(1500, TimeUnit.MILLISECONDS);
                event.markPublished();
                log.info("outbox event published eventId={} transactionId={} traceId={}",
                        event.getId(), event.getAggregateId(), event.getTraceId());
            } catch (Exception ex) {
                event.incrementAttempts();
                log.warn("outbox publish failed eventId={} attempts={} error={}",
                        event.getId(), event.getAttempts(), ex.getMessage());
            }
        }
    }
}

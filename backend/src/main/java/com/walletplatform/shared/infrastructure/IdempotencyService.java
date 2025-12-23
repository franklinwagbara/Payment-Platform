package com.walletplatform.shared.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Service for ensuring idempotent execution of operations.
 * Caches results using the provided idempotency key.
 */
@Service
public class IdempotencyService {

    private final IdempotencyRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Execute an operation idempotently.
     * If the key was already processed, return cached result.
     * Otherwise, execute the operation and cache the result.
     */
    @Transactional
    public <T> T executeIdempotent(String idempotencyKey, Class<T> resultType, Supplier<T> operation) {
        Optional<IdempotencyRecord> existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return deserialize(existing.get().getResultJson(), resultType);
        }

        T result = operation.get();

        String resultJson = serialize(result);
        IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, resultType.getName(), resultJson);
        repository.save(record);

        return result;
    }

    public boolean wasProcessed(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).isPresent();
    }

    private <T> String serialize(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize idempotency result", e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize idempotency result", e);
        }
    }
}

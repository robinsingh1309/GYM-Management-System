package com.example.fitmanager.component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;


@Component
public class ExerciseEmbeddingStore {

    // Fields

    private final Map<Long, float[]> embeddings = //
            new ConcurrentHashMap<>();

    // Methods
    // --------------------------------------------------

    public void put(final Long exerciseId, final float[] embedding) {
        embeddings.put(exerciseId, embedding);
    }

    public float[] get(final Long exerciseId) {
        return embeddings.get(exerciseId);
    }

    public boolean contains(final Long exerciseId) {
        return embeddings.containsKey(exerciseId);
    }

    public int size() {
        return embeddings.size();
    }

    public Map<Long, float[]> getAll() {
        return Collections.unmodifiableMap(embeddings);
    }

    public void clear() {
        embeddings.clear();
    }
}

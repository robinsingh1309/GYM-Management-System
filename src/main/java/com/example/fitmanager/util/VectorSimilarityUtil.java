package com.example.fitmanager.util;

import java.util.Objects;

public final class VectorSimilarityUtil {
    
    /**
     *      dot product
     *   ----------------------
     *   |vectorA| × |vectorB|
     */

    // Constructors
    // ----------------------------------------------------------------------
    
    private VectorSimilarityUtil() {
        //
    }

    
    // Methods
    // ----------------------------------------------------------------------
    
    public static double cosineSimilarity( //
            final float[] vectorA, final float[] vectorB) {

        if (Objects.isNull(vectorA) || Objects.isNull(vectorB)) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }

        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must have the same dimensions");
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {

            dotProduct += vectorA[i] * vectorB[i];

            magnitudeA += vectorA[i] * vectorA[i];

            magnitudeB += vectorB[i] * vectorB[i];
        }

        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitudeA * magnitudeB);
    }
}

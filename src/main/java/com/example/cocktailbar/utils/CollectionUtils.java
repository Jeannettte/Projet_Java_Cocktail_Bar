package com.example.cocktailbar.utils;

import java.util.List;
import java.util.Map;

/** Small generic helpers for collections. */
public final class CollectionUtils {

    private CollectionUtils() {
        // Utility class: no instances.
    }

    /**
     * Returns the {@code limit} entries with the highest counts, best first.
     *
     * <p>The key type must be {@link Comparable} so that ties are broken by key order:
     * the result is then always the same for the same input, which keeps reports and tests stable.
     */
    public static <K extends Comparable<K>> List<Map.Entry<K, Integer>> topN(Map<K, Integer> counts, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must not be negative: " + limit);
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<K, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.<K, Integer>comparingByKey()))
                .limit(limit)
                .toList();
    }
}

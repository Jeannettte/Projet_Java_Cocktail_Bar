package com.example.cocktailbar.utils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 The topN ranking: ties, upper bound greater than the list, bound of 0 or negative
 */



class CollectionUtilsTest {

    private static List<String> keysOf(List<Map.Entry<String, Integer>> entries) {
        return entries.stream().map(Map.Entry::getKey).toList();
    }

    @Test
    void entriesAreSortedByDescendingCount() {
        Map<String, Integer> counts = Map.of("a", 1, "b", 5, "c", 3);

        assertEquals(List.of("b", "c"), keysOf(CollectionUtils.topN(counts, 2)));
    }

    @Test
    void tiesAreBrokenByKeyOrderSoTheResultIsStable() {
        Map<String, Integer> counts = Map.of("b", 2, "a", 2, "c", 2);

        assertEquals(List.of("a", "b", "c"), keysOf(CollectionUtils.topN(counts, 3)));
    }

    @Test
    void limitLargerThanTheMapReturnsEverything() {
        assertEquals(2, CollectionUtils.topN(Map.of("a", 1, "b", 2), 10).size());
    }

    @Test
    void zeroLimitAndEmptyMapGiveAnEmptyList() {
        assertTrue(CollectionUtils.topN(Map.of("a", 1), 0).isEmpty());
        assertTrue(CollectionUtils.topN(Map.<String, Integer>of(), 3).isEmpty());
    }

    @Test
    void negativeLimitIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> CollectionUtils.topN(Map.of("a", 1), -1));
    }

    @Test
    void worksWithAnyComparableKeyType() {
        Map<Integer, Integer> counts = Map.of(10, 1, 20, 9);

        assertEquals(Integer.valueOf(20), CollectionUtils.topN(counts, 1).get(0).getKey());
    }
}

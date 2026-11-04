package com.example.cocktailbar.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerTest {

    // The limit is inclusive: the interesting cases are right below and right on 18.
    @ParameterizedTest
    @CsvSource({"0,false", "17,false", "18,true", "19,true", "120,true"})
    void isAdultFromEighteenYearsOld(int age, boolean expectedAdult) {
        assertEquals(expectedAdult, new Customer("Test", age).isAdult());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 121})
    void unrealisticAgeIsRejected(int age) {
        assertThrows(IllegalArgumentException.class, () -> new Customer("Test", age));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void blankNameIsRejected(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Customer(name, 30));
    }

    @Test
    void nullNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(null, 30));
    }

    @Test
    void nameIsTrimmed() {
        assertEquals("Alice", new Customer("  Alice ", 30).getName());
    }
}

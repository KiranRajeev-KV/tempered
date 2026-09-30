package com.auco.tempered.progression;

import java.util.Objects;

/** Small assertion vocabulary for the standalone GameTest runtime. */
final class BaselineAssertions {
    static void assertTrue(boolean condition, String... message) {
        if (!condition) throw new AssertionError(message.length == 0 ? "Expected true" : message[0]);
    }

    static void assertFalse(boolean condition) {
        assertTrue(!condition, "Expected false");
    }

    static void assertEquals(Object expected, Object actual, String... message) {
        assertTrue(Objects.equals(expected, actual),
                (message.length == 0 ? "Values differ" : message[0]) + ": expected " + expected + ", actual " + actual);
    }

    static void assertEquals(float expected, float actual, float tolerance) {
        assertTrue(Math.abs(expected - actual) <= tolerance, "Expected " + expected + ", actual " + actual);
    }

    static void assertNull(Object actual, String... message) {
        assertTrue(actual == null, message.length == 0 ? "Expected null, actual " + actual : message[0]);
    }

    static void assertNotNull(Object actual) {
        assertTrue(actual != null, "Expected a value");
    }

    static void assertSame(Object expected, Object actual) {
        assertTrue(expected == actual, "Expected the same instance");
    }

    private BaselineAssertions() {}
}

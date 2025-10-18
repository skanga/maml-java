package com.skanga;

import com.skanga.MAMLValue.*;
import java.util.*;

/**
 * Builder for constructing MAML values programmatically.
 * 
 * <p>Usage example:
 * <pre>{@code
 * MAMLValue value = MAMLBuilder.object()
 *     .put("name", "John Doe")
 *     .put("age", 30)
 *     .put("hobbies", MAMLBuilder.array()
 *         .add("reading")
 *         .add("coding")
 *         .build())
 *     .build();
 * }</pre>
 */
public class MAMLBuilder {

    /**
     * Creates a new object builder.
     */
    public static ObjectBuilder object() {
        return new ObjectBuilder();
    }

    /**
     * Creates a new array builder.
     */
    public static ArrayBuilder array() {
        return new ArrayBuilder();
    }

    /**
     * Builder for MAML objects.
     */
    public static class ObjectBuilder {
        private final Map<String, MAMLValue> map = new LinkedHashMap<>();

        /**
         * Adds a key-value pair to the object.
         */
        public ObjectBuilder put(String key, MAMLValue value) {
            map.put(key, value);
            return this;
        }

        /**
         * Adds a string value.
         */
        public ObjectBuilder put(String key, String value) {
            map.put(key, new MAMLString(value));
            return this;
        }

        /**
         * Adds an integer value.
         */
        public ObjectBuilder put(String key, long value) {
            map.put(key, new MAMLInteger(value));
            return this;
        }

        /**
         * Adds an integer value.
         */
        public ObjectBuilder put(String key, int value) {
            map.put(key, new MAMLInteger(value));
            return this;
        }

        /**
         * Adds a float value.
         */
        public ObjectBuilder put(String key, double value) {
            map.put(key, new MAMLFloat(value));
            return this;
        }

        /**
         * Adds a boolean value.
         */
        public ObjectBuilder put(String key, boolean value) {
            map.put(key, new MAMLBoolean(value));
            return this;
        }

        /**
         * Adds a null value.
         */
        public ObjectBuilder putNull(String key) {
            map.put(key, new MAMLNull());
            return this;
        }

        /**
         * Builds the MAML object.
         */
        public MAMLObject build() {
            return new MAMLObject(Collections.unmodifiableMap(new LinkedHashMap<>(map)));
        }
    }

    /**
     * Builder for MAML arrays.
     */
    public static class ArrayBuilder {
        private final List<MAMLValue> list = new ArrayList<>();

        /**
         * Adds a value to the array.
         */
        public ArrayBuilder add(MAMLValue value) {
            list.add(value);
            return this;
        }

        /**
         * Adds a string value.
         */
        public ArrayBuilder add(String value) {
            list.add(new MAMLString(value));
            return this;
        }

        /**
         * Adds an integer value.
         */
        public ArrayBuilder add(long value) {
            list.add(new MAMLInteger(value));
            return this;
        }

        /**
         * Adds an integer value.
         */
        public ArrayBuilder add(int value) {
            list.add(new MAMLInteger(value));
            return this;
        }

        /**
         * Adds a float value.
         */
        public ArrayBuilder add(double value) {
            list.add(new MAMLFloat(value));
            return this;
        }

        /**
         * Adds a boolean value.
         */
        public ArrayBuilder add(boolean value) {
            list.add(new MAMLBoolean(value));
            return this;
        }

        /**
         * Adds a null value.
         */
        public ArrayBuilder addNull() {
            list.add(new MAMLNull());
            return this;
        }

        /**
         * Builds the MAML array.
         */
        public MAMLArray build() {
            return new MAMLArray(Collections.unmodifiableList(new ArrayList<>(list)));
        }
    }
}
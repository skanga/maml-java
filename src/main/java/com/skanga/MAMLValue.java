package com.skanga;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a MAML value which can be an object, array, string, number, boolean, or null.
 */
public sealed interface MAMLValue permits 
    MAMLValue.MAMLObject, 
    MAMLValue.MAMLArray, 
    MAMLValue.MAMLString, 
    MAMLValue.MAMLInteger, 
    MAMLValue.MAMLFloat, 
    MAMLValue.MAMLBoolean, 
    MAMLValue.MAMLNull {

    /**
     * Returns the type of this MAML value.
     */
    ValueType getType();

    /**
     * Type enumeration for MAML values.
     */
    enum ValueType {
        OBJECT, ARRAY, STRING, INTEGER, FLOAT, BOOLEAN, NULL
    }

    // Object
    record MAMLObject(Map<String, MAMLValue> value) implements MAMLValue {
        public MAMLObject {
            Objects.requireNonNull(value, "Object value cannot be null");
        }

        @Override
        public ValueType getType() {
            return ValueType.OBJECT;
        }

        public MAMLValue get(String key) {
            return value.get(key);
        }

        public boolean containsKey(String key) {
            return value.containsKey(key);
        }

        public int size() {
            return value.size();
        }
    }

    // Array
    record MAMLArray(List<MAMLValue> value) implements MAMLValue {
        public MAMLArray {
            Objects.requireNonNull(value, "Array value cannot be null");
        }

        @Override
        public ValueType getType() {
            return ValueType.ARRAY;
        }

        public MAMLValue get(int index) {
            return value.get(index);
        }

        public int size() {
            return value.size();
        }
    }

    // String
    record MAMLString(String value) implements MAMLValue {
        public MAMLString {
            Objects.requireNonNull(value, "String value cannot be null");
        }

        @Override
        public ValueType getType() {
            return ValueType.STRING;
        }
    }

    // Integer
    record MAMLInteger(long value) implements MAMLValue {
        @Override
        public ValueType getType() {
            return ValueType.INTEGER;
        }
    }

    // Float
    record MAMLFloat(double value) implements MAMLValue {
        @Override
        public ValueType getType() {
            return ValueType.FLOAT;
        }
    }

    // Boolean
    record MAMLBoolean(boolean value) implements MAMLValue {
        @Override
        public ValueType getType() {
            return ValueType.BOOLEAN;
        }
    }

    // Null
    record MAMLNull() implements MAMLValue {
        @Override
        public ValueType getType() {
            return ValueType.NULL;
        }
    }
}
package com.skanga;

import com.skanga.MAMLValue.*;
import java.util.Map;

/**
 * Serializes MAMLValue objects back to MAML format.
 */
public class MAMLSerializer {
    private final boolean prettyPrint;
    private final String indent;
    private final int indentSize;

    /**
     * Creates a serializer with default settings (pretty print enabled, 2 spaces).
     */
    public MAMLSerializer() {
        this(true, 2);
    }

    /**
     * Creates a serializer with custom settings.
     *
     * @param prettyPrint whether to format output with indentation and newlines
     * @param indentSize number of spaces per indentation level
     */
    public MAMLSerializer(boolean prettyPrint, int indentSize) {
        this.prettyPrint = prettyPrint;
        this.indentSize = indentSize;
        this.indent = " ".repeat(indentSize);
    }

    /**
     * Serializes a MAMLValue to a MAML string.
     *
     * @param value the value to serialize
     * @return the MAML string representation
     */
    public String serialize(MAMLValue value) {
        StringBuilder sb = new StringBuilder();
        serializeValue(value, sb, 0);
        return sb.toString();
    }
/*
    private void serializeValue(MAMLValue value, StringBuilder sb, int depth) {
        switch (value) {
            case MAMLObject obj -> serializeObject(obj, sb, depth);
            case MAMLArray arr -> serializeArray(arr, sb, depth);
            case MAMLString str -> serializeString(str.value(), sb);
            case MAMLInteger i -> sb.append(i.value());
            case MAMLFloat f -> serializeFloat(f.value(), sb);
            case MAMLBoolean bool -> sb.append(bool.value() ? "true" : "false");
            case MAMLNull ignored -> sb.append("null");
        }
    }
*/

    private void serializeValue(MAMLValue value, StringBuilder sb, int depth) {
        if (value instanceof MAMLObject obj) {
            serializeObject(obj, sb, depth);
        } else if (value instanceof MAMLArray arr) {
            serializeArray(arr, sb, depth);
        } else if (value instanceof MAMLString str) {
            serializeString(str.value(), sb);
        } else if (value instanceof MAMLInteger i) {
            sb.append(i.value());
        } else if (value instanceof MAMLFloat f) {
            serializeFloat(f.value(), sb);
        } else if (value instanceof MAMLBoolean bool) {
            sb.append(bool.value() ? "true" : "false");
        } else if (value instanceof MAMLNull) {
            sb.append("null");
        }
    }

    private void serializeObject(MAMLObject obj, StringBuilder sb, int depth) {
        sb.append("{");
        
        if (obj.value().isEmpty()) {
            sb.append("}");
            return;
        }

        if (prettyPrint) {
            sb.append("\n");
        }

        boolean first = true;
        for (Map.Entry<String, MAMLValue> entry : obj.value().entrySet()) {
            if (!first) {
                if (prettyPrint) {
                    sb.append("\n");
                } else {
                    sb.append(",");
                }
            }
            first = false;

            if (prettyPrint) {
                sb.append(indent.repeat(depth + 1));
            }

            serializeKey(entry.getKey(), sb);
            sb.append(":");
            if (prettyPrint) {
                sb.append(" ");
            }
            serializeValue(entry.getValue(), sb, depth + 1);
        }

        if (prettyPrint) {
            sb.append("\n").append(indent.repeat(depth));
        }
        sb.append("}");
    }

    private void serializeArray(MAMLArray arr, StringBuilder sb, int depth) {
        sb.append("[");
        
        if (arr.value().isEmpty()) {
            sb.append("]");
            return;
        }

        if (prettyPrint) {
            sb.append("\n");
        }

        boolean first = true;
        for (MAMLValue item : arr.value()) {
            if (!first) {
                if (prettyPrint) {
                    sb.append("\n");
                } else {
                    sb.append(",");
                }
            }
            first = false;

            if (prettyPrint) {
                sb.append(indent.repeat(depth + 1));
            }
            serializeValue(item, sb, depth + 1);
        }

        if (prettyPrint) {
            sb.append("\n").append(indent.repeat(depth));
        }
        sb.append("]");
    }

    private void serializeKey(String key, StringBuilder sb) {
        // Use identifier format if possible
        if (isValidIdentifier(key)) {
            sb.append(key);
        } else {
            serializeString(key, sb);
        }
    }

    private void serializeString(String str, StringBuilder sb) {
        // Check if multiline string is better
        if (str.contains("\n") && !str.contains("\"\"\"") && str.length() > 20) {
            serializeMultilineString(str, sb);
        } else {
            serializeQuotedString(str, sb);
        }
    }

    private void serializeQuotedString(String str, StringBuilder sb) {
        sb.append('"');
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\t' -> sb.append("\\t");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                default -> {
                    if (c < 0x20 || c == 0x7F) {
                        sb.append(String.format("\\u{%X}", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private void serializeMultilineString(String str, StringBuilder sb) {
        sb.append("\"\"\"");
        if (!str.startsWith("\n")) {
            sb.append("\n");
        }
        sb.append(str);
        if (!str.endsWith("\n")) {
            // Close on same line to avoid extra newline
            sb.append("\"\"\"");
        } else {
            sb.append("\"\"\"");
        }
    }

    private void serializeFloat(double value, StringBuilder sb) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new MAMLException("Cannot serialize NaN or Infinity as MAML float");
        }
        
        String str = Double.toString(value);
        // Ensure it has a decimal point or exponent
        if (!str.contains(".") && !str.contains("e") && !str.contains("E")) {
            str += ".0";
        }
        sb.append(str);
    }

    private boolean isValidIdentifier(String str) {
        if (str.isEmpty()) {
            return false;
        }

        // Check if it's a keyword
        if (str.equals("true") || str.equals("false") || str.equals("null")) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!isIdentifierChar(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isIdentifierChar(char c) {
        return (c >= 'a' && c <= 'z') || 
               (c >= 'A' && c <= 'Z') || 
               (c >= '0' && c <= '9') || 
               c == '_' || 
               c == '-';
    }
}
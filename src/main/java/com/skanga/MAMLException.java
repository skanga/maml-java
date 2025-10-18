package com.skanga;

/**
 * Exception thrown when parsing MAML content fails.
 */
public class MAMLException extends RuntimeException {
    private final int line;
    private final int column;

    public MAMLException(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }

    public MAMLException(String message, int line, int column) {
        super(String.format("%s at line %d, column %d", message, line, column));
        this.line = line;
        this.column = column;
    }

    public MAMLException(String message, Throwable cause) {
        super(message, cause);
        this.line = -1;
        this.column = -1;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
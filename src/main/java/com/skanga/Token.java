package com.skanga;

/**
 * Represents a token produced by the lexer.
 */
record Token(TokenType type, String value, int line, int column) {
    
    enum TokenType {
        // Structural
        LEFT_BRACE,      // {
        RIGHT_BRACE,     // }
        LEFT_BRACKET,    // [
        RIGHT_BRACKET,   // ]
        COLON,           // :
        COMMA,           // ,
        
        // Literals
        STRING,
        MULTILINE_STRING,
        INTEGER,
        FLOAT,
        TRUE,
        FALSE,
        NULL,
        IDENTIFIER,
        
        // Special
        NEWLINE,
        EOF
    }

    @Override
    public String toString() {
        if (value != null && !value.isEmpty()) {
            return String.format("%s('%s') at %d:%d", type, value, line, column);
        }
        return String.format("%s at %d:%d", type, line, column);
    }
}
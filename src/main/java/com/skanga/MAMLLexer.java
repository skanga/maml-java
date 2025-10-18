package com.skanga;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexer for MAML - tokenizes MAML input into a stream of tokens.
 */
class MAMLLexer {
    private final String input;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    MAMLLexer(String input) {
        this.input = input;
    }

    List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;

            // Skip comments
            if (peek() == '#') {
                skipComment();
                continue;
            }

            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }

        tokens.add(new Token(Token.TokenType.EOF, "", line, column));
        return tokens;
    }

    private Token nextToken() {
        char c = peek();
        int startLine = line;
        int startColumn = column;

        return switch (c) {
            case '{' -> {
                advance();
                yield new Token(Token.TokenType.LEFT_BRACE, "{", startLine, startColumn);
            }
            case '}' -> {
                advance();
                yield new Token(Token.TokenType.RIGHT_BRACE, "}", startLine, startColumn);
            }
            case '[' -> {
                advance();
                yield new Token(Token.TokenType.LEFT_BRACKET, "[", startLine, startColumn);
            }
            case ']' -> {
                advance();
                yield new Token(Token.TokenType.RIGHT_BRACKET, "]", startLine, startColumn);
            }
            case ':' -> {
                advance();
                yield new Token(Token.TokenType.COLON, ":", startLine, startColumn);
            }
            case ',' -> {
                advance();
                yield new Token(Token.TokenType.COMMA, ",", startLine, startColumn);
            }
            case '\n' -> {
                advance();
                yield new Token(Token.TokenType.NEWLINE, "\n", startLine, startColumn);
            }
            case '\r' -> {
                advance();
                if (peek() == '\n') advance();
                yield new Token(Token.TokenType.NEWLINE, "\n", startLine, startColumn);
            }
            case '"' -> scanString();
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> scanNumber();
            default -> {
                if (isIdentifierStart(c)) {
                    yield scanIdentifierOrKeyword();
                }
                throw new MAMLException("Unexpected character: '" + c + "'", line, column);
            }
        };
    }

    private Token scanString() {
        int startLine = line;
        int startColumn = column;

        // Check for multiline string (""")
        if (peek() == '"' && peekNext() == '"' && peekAt(2) == '"') {
            return scanMultilineString();
        }

        advance(); // consume opening "
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && peek() != '"') {
            char c = peek();

            // Check for invalid control characters
            if (isControlChar(c) && c != '\t') {
                throw new MAMLException("Control characters not allowed in strings", line, column);
            }

            if (c == '\\') {
                advance();
                if (isAtEnd()) {
                    throw new MAMLException("Unterminated string", line, column);
                }
                char next = peek();
                if (next == 'u') {
                    advance(); // consume 'u'
                    sb.append(scanUnicodeEscape());
                } else {
                    sb.append(scanEscape());
                }
            } else {
                sb.append(c);
                advance();
            }
        }

        if (isAtEnd()) {
            throw new MAMLException("Unterminated string", line, column);
        }

        advance(); // consume closing "
        return new Token(Token.TokenType.STRING, sb.toString(), startLine, startColumn);
    }

    private Token scanMultilineString() {
        int startLine = line;
        int startColumn = column;

        // Consume opening """
        advance();
        advance();
        advance();

        // Skip immediate newline after opening delimiter
        if (peek() == '\n') {
            advance();
        } else if (peek() == '\r' && peekNext() == '\n') {
            advance();
            advance();
        }

        StringBuilder sb = new StringBuilder();
        int quoteCount = 0;

        while (!isAtEnd()) {
            char c = peek();

            if (c == '"') {
                quoteCount++;
                if (quoteCount == 3) {
                    // Check if there are more quotes following
                    if (peekNext() == '"') {
                        throw new MAMLException("Sequences of three or more quotes not permitted in multiline strings", line, column);
                    }
                    // Remove the two quotes we just added
                    sb.setLength(sb.length() - 2);
                    advance(); // consume third quote
                    break;
                }
                sb.append(c);
                advance();
            } else {
                quoteCount = 0;
                sb.append(c);
                advance();
            }
        }

        if (quoteCount != 3) {
            throw new MAMLException("Unterminated multiline string", line, column);
        }

        return new Token(Token.TokenType.MULTILINE_STRING, sb.toString(), startLine, startColumn);
    }

    private char scanEscape() {
        char c = peek();
        advance();

        return switch (c) {
            case 't' -> '\t';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case '"' -> '"';
            case '\\' -> '\\';
            default -> throw new MAMLException("Invalid escape sequence: \\" + c, line, column);
        };
    }

    private String scanUnicodeEscape() {
        if (peek() != '{') {
            throw new MAMLException("Unicode escape must start with \\u{", line, column);
        }
        advance(); // consume {

        StringBuilder hex = new StringBuilder();
        while (!isAtEnd() && peek() != '}') {
            char c = peek();
            if (!isHexDigit(c)) {
                throw new MAMLException("Invalid hex digit in unicode escape: " + c, line, column);
            }
            hex.append(c);
            advance();
        }

        if (isAtEnd() || peek() != '}') {
            throw new MAMLException("Unterminated unicode escape", line, column);
        }
        advance(); // consume }

        if (hex.length() < 1 || hex.length() > 6) {
            throw new MAMLException("Unicode escape must have 1-6 hex digits", line, column);
        }

        int codePoint = Integer.parseInt(hex.toString(), 16);

        // Check if valid Unicode scalar value
        if (codePoint > 0x10FFFF || (codePoint >= 0xD800 && codePoint <= 0xDFFF)) {
            throw new MAMLException("Invalid Unicode scalar value: " + codePoint, line, column);
        }

        // Convert code point to String (handles surrogate pairs correctly)
        return new String(Character.toChars(codePoint));
    }

    private Token scanNumber() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;

        // Handle negative sign
        if (peek() == '-') {
            sb.append(advance());
        }

        // Integer part
        if (peek() == '0') {
            sb.append(advance());
            // Leading zeros not allowed unless it's just "0"
            if (isDigit(peek())) {
                throw new MAMLException("Leading zeros not allowed", line, column);
            }
        } else {
            while (isDigit(peek())) {
                sb.append(advance());
            }
        }

        // Fractional part
        if (peek() == '.') {
            isFloat = true;
            sb.append(advance());

            if (!isDigit(peek())) {
                throw new MAMLException("Digit required after decimal point", line, column);
            }

            while (isDigit(peek())) {
                sb.append(advance());
            }
        }

        // Exponent part
        if (peek() == 'e' || peek() == 'E') {
            isFloat = true;
            sb.append(advance());

            if (peek() == '+' || peek() == '-') {
                sb.append(advance());
            }

            if (!isDigit(peek())) {
                throw new MAMLException("Digit required in exponent", line, column);
            }

            while (isDigit(peek())) {
                sb.append(advance());
            }
        }

        String numStr = sb.toString();
        Token.TokenType type = isFloat ? Token.TokenType.FLOAT : Token.TokenType.INTEGER;

        return new Token(type, numStr, startLine, startColumn);
    }

    private Token scanIdentifierOrKeyword() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (isIdentifierChar(peek())) {
            sb.append(advance());
        }

        String text = sb.toString();
        Token.TokenType type = switch (text) {
            case "true" -> Token.TokenType.TRUE;
            case "false" -> Token.TokenType.FALSE;
            case "null" -> Token.TokenType.NULL;
            default -> Token.TokenType.IDENTIFIER;
        };

        return new Token(type, text, startLine, startColumn);
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t') {
                advance();
            } else {
                break;
            }
        }
    }

    private void skipComment() {
        advance(); // consume #
        while (!isAtEnd() && peek() != '\n' && peek() != '\r') {
            char c = peek();
            // Check for invalid control characters in comments
            if (isControlChar(c) && c != '\t') {
                throw new MAMLException("Control characters not allowed in comments", line, column);
            }
            advance();
        }
    }

    private boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '-';
    }

    private boolean isIdentifierChar(char c) {
        return isIdentifierStart(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isControlChar(char c) {
        return (c >= 0x00 && c <= 0x08) || (c >= 0x0A && c <= 0x1F) || c == 0x7F;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return input.charAt(pos);
    }

    private char peekNext() {
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }

    private char peekAt(int offset) {
        if (pos + offset >= input.length()) return '\0';
        return input.charAt(pos + offset);
    }

    private char advance() {
        char c = input.charAt(pos++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private boolean isAtEnd() {
        return pos >= input.length();
    }
}
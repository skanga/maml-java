package com.skanga;

import com.skanga.MAMLValue.*;
import java.util.*;

/**
 * Parser for MAML - converts tokens into a MAMLValue tree.
 */
class MAMLParser {
    private final List<Token> tokens;
    private int current = 0;

    MAMLParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    MAMLValue parse() {
        skipWhitespaceAndComments();
        
        if (isAtEnd()) {
            throw new MAMLException("Empty MAML document");
        }
        
        MAMLValue value = parseValue();
        
        skipWhitespaceAndComments();
        
        if (!isAtEnd()) {
            Token token = peek();
            throw new MAMLException("Unexpected token after value: " + token.type(), token.line(), token.column());
        }
        
        return value;
    }

    private MAMLValue parseValue() {
        Token token = peek();
        
        return switch (token.type()) {
            case LEFT_BRACE -> parseObject();
            case LEFT_BRACKET -> parseArray();
            case STRING, MULTILINE_STRING -> {
                advance();
                yield new MAMLString(token.value());
            }
            case INTEGER -> {
                advance();
                try {
                    long value = Long.parseLong(token.value());
                    yield new MAMLInteger(value);
                } catch (NumberFormatException e) {
                    throw new MAMLException("Integer value out of range: " + token.value(), token.line(), token.column());
                }
            }
            case FLOAT -> {
                advance();
                try {
                    double value = Double.parseDouble(token.value());
                    yield new MAMLFloat(value);
                } catch (NumberFormatException e) {
                    throw new MAMLException("Invalid float value: " + token.value(), token.line(), token.column());
                }
            }
            case TRUE -> {
                advance();
                yield new MAMLBoolean(true);
            }
            case FALSE -> {
                advance();
                yield new MAMLBoolean(false);
            }
            case NULL -> {
                advance();
                yield new MAMLNull();
            }
            default -> throw new MAMLException("Expected value, got " + token.type(), token.line(), token.column());
        };
    }

    private MAMLObject parseObject() {
        Token start = consume(Token.TokenType.LEFT_BRACE, "Expected '{'");
        Map<String, MAMLValue> members = new LinkedHashMap<>();
        
        skipWhitespaceAndComments();
        
        while (!check(Token.TokenType.RIGHT_BRACE) && !isAtEnd()) {
            // Parse key
            String key = parseKey();
            
            // Check for duplicate keys
            if (members.containsKey(key)) {
                Token token = previous();
                throw new MAMLException("Duplicate key: " + key, token.line(), token.column());
            }
            
            skipWhitespaceAndComments();
            consume(Token.TokenType.COLON, "Expected ':' after key");
            skipWhitespaceAndComments();
            
            // Parse value
            MAMLValue value = parseValue();
            members.put(key, value);
            
            skipWhitespaceAndComments();
            
            // Check for separator (comma or newline)
            if (check(Token.TokenType.COMMA) || check(Token.TokenType.NEWLINE)) {
                advance();
                skipWhitespaceAndComments();
            }
            
            // Allow trailing comma
            if (check(Token.TokenType.RIGHT_BRACE)) {
                break;
            }
        }
        
        consume(Token.TokenType.RIGHT_BRACE, "Expected '}'");
        return new MAMLObject(Collections.unmodifiableMap(members));
    }

    private MAMLArray parseArray() {
        consume(Token.TokenType.LEFT_BRACKET, "Expected '['");
        List<MAMLValue> items = new ArrayList<>();
        
        skipWhitespaceAndComments();
        
        while (!check(Token.TokenType.RIGHT_BRACKET) && !isAtEnd()) {
            items.add(parseValue());
            
            skipWhitespaceAndComments();
            
            // Check for separator (comma or newline)
            if (check(Token.TokenType.COMMA) || check(Token.TokenType.NEWLINE)) {
                advance();
                skipWhitespaceAndComments();
            }
            
            // Allow trailing comma
            if (check(Token.TokenType.RIGHT_BRACKET)) {
                break;
            }
        }
        
        consume(Token.TokenType.RIGHT_BRACKET, "Expected ']'");
        return new MAMLArray(Collections.unmodifiableList(items));
    }

    private String parseKey() {
        Token token = peek();
        
        if (token.type() == Token.TokenType.STRING) {
            advance();
            return token.value();
        } else if (token.type() == Token.TokenType.IDENTIFIER) {
            advance();
            return token.value();
        } else {
            throw new MAMLException("Expected key (string or identifier)", token.line(), token.column());
        }
    }

    private void skipWhitespaceAndComments() {
        while (check(Token.TokenType.NEWLINE)) {
            advance();
        }
    }

    private boolean check(Token.TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == Token.TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(Token.TokenType type, String message) {
        if (check(type)) return advance();
        
        Token token = peek();
        throw new MAMLException(message + ", got " + token.type(), token.line(), token.column());
    }
}
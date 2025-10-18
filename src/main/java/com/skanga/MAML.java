package com.skanga;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Main entry point for parsing MAML documents.
 * 
 * <p>Usage example:
 * <pre>{@code
 * String maml = """
 *     {
 *         name: "John Doe"
 *         age: 30
 *         active: true
 *     }
 *     """;
 * 
 * MAMLValue value = MAML.parse(maml);
 * MAMLValue.MAMLObject obj = (MAMLValue.MAMLObject) value;
 * System.out.println(obj.get("name")); // MAMLString("John Doe")
 * }</pre>
 */
public class MAML {
    
    /**
     * Parses a MAML string into a MAMLValue.
     *
     * @param input the MAML string to parse
     * @return the parsed MAMLValue
     * @throws MAMLException if parsing fails
     */
    public static MAMLValue parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        
        // Validate UTF-8
        validateUtf8(input);
        
        MAMLLexer lexer = new MAMLLexer(input);
        List<Token> tokens = lexer.tokenize();
        
        MAMLParser parser = new MAMLParser(tokens);
        return parser.parse();
    }
    
    /**
     * Parses a MAML file into a MAMLValue.
     *
     * @param path the path to the MAML file
     * @return the parsed MAMLValue
     * @throws MAMLException if parsing fails
     * @throws IOException if file reading fails
     */
    public static MAMLValue parseFile(Path path) throws IOException {
        String content = Files.readString(path);
        return parse(content);
    }
    
    /**
     * Parses a MAML file into a MAMLValue.
     *
     * @param filename the filename of the MAML file
     * @return the parsed MAMLValue
     * @throws MAMLException if parsing fails
     * @throws IOException if file reading fails
     */
    public static MAMLValue parseFile(String filename) throws IOException {
        return parseFile(Path.of(filename));
    }
    
    private static void validateUtf8(String input) {
        // Java strings are UTF-16, but we need to ensure the content is valid UTF-8
        // Check for invalid surrogate pairs
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= input.length() || !Character.isLowSurrogate(input.charAt(i + 1))) {
                    throw new MAMLException("Invalid UTF-8: unpaired high surrogate at position " + i);
                }
                i++; // Skip the low surrogate
            } else if (Character.isLowSurrogate(c)) {
                throw new MAMLException("Invalid UTF-8: unpaired low surrogate at position " + i);
            }
        }
    }
}
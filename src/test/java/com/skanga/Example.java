package com.skanga;

import com.skanga.MAMLValue.*;

/**
 * Example application demonstrating MAML parser usage.
 */
public class Example {
    
    public static void main(String[] args) {
        // Example 1: Parse a simple MAML document
        System.out.println("=== Example 1: Basic Parsing ===");
        simpleParsingExample();
        
        System.out.println("\n=== Example 2: Complex Structures ===");
        complexStructureExample();
        
        System.out.println("\n=== Example 3: Builder API ===");
        builderExample();
        
        System.out.println("\n=== Example 4: Serialization ===");
        serializationExample();
        
        System.out.println("\n=== Example 5: Error Handling ===");
        errorHandlingExample();
    }
    
    private static void simpleParsingExample() {
        String maml = """
            {
                name: "John Doe"
                age: 30
                email: "john@example.com"
                active: true
            }
            """;
        
        MAMLObject user = (MAMLObject) MAML.parse(maml);
        
        String name = ((MAMLString) user.get("name")).value();
        long age = ((MAMLInteger) user.get("age")).value();
        String email = ((MAMLString) user.get("email")).value();
        boolean active = ((MAMLBoolean) user.get("active")).value();
        
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Email: " + email);
        System.out.println("Active: " + active);
    }
    
    private static void complexStructureExample() {
        String maml = """
            {
                app: {
                    name: "MyApp"
                    version: "1.0.0"
                }
                
                database: {
                    host: "localhost"
                    port: 5432
                    credentials: {
                        username: "admin"
                        password: "secret"
                    }
                }
                
                features: [
                    "authentication"
                    "logging"
                    "metrics"
                ]
                
                description: \"\"\"
                This is a multi-line
                description of the application.
                It can span multiple lines!
                \"\"\"
            }
            """;
        
        MAMLObject config = (MAMLObject) MAML.parse(maml);
        
        // Access nested object
        MAMLObject app = (MAMLObject) config.get("app");
        System.out.println("App Name: " + ((MAMLString) app.get("name")).value());
        System.out.println("Version: " + ((MAMLString) app.get("version")).value());
        
        // Access deeply nested value
        MAMLObject db = (MAMLObject) config.get("database");
        MAMLObject creds = (MAMLObject) db.get("credentials");
        System.out.println("DB Username: " + ((MAMLString) creds.get("username")).value());
        
        // Access array
        MAMLArray features = (MAMLArray) config.get("features");
        System.out.print("Features: ");
        for (int i = 0; i < features.size(); i++) {
            String feature = ((MAMLString) features.get(i)).value();
            System.out.print(feature + (i < features.size() - 1 ? ", " : ""));
        }
        System.out.println();
        
        // Access multiline string
        String description = ((MAMLString) config.get("description")).value();
        System.out.println("Description: " + description.strip());
    }
    
    private static void builderExample() {
        // Build a complex structure programmatically
        MAMLValue product = MAMLBuilder.object()
            .put("id", 12345)
            .put("name", "Laptop")
            .put("price", 999.99)
            .put("inStock", true)
            .put("specifications", MAMLBuilder.object()
                .put("cpu", "Intel i7")
                .put("ram", 16)
                .put("storage", 512)
                .build())
            .put("tags", MAMLBuilder.array()
                .add("electronics")
                .add("computers")
                .add("portable")
                .build())
            .put("reviews", MAMLBuilder.array()
                .add(MAMLBuilder.object()
                    .put("user", "Alice")
                    .put("rating", 5)
                    .put("comment", "Excellent product!")
                    .build())
                .add(MAMLBuilder.object()
                    .put("user", "Bob")
                    .put("rating", 4)
                    .put("comment", "Good value for money")
                    .build())
                .build())
            .putNull("discount")
            .build();
        
        MAMLObject obj = (MAMLObject) product;
        System.out.println("Product ID: " + ((MAMLInteger) obj.get("id")).value());
        System.out.println("Product Name: " + ((MAMLString) obj.get("name")).value());
        System.out.println("Price: $" + ((MAMLFloat) obj.get("price")).value());
        
        MAMLArray reviews = (MAMLArray) obj.get("reviews");
        System.out.println("Number of reviews: " + reviews.size());
    }
    
    private static void serializationExample() {
        // Create a value
        MAMLValue value = MAMLBuilder.object()
            .put("server", MAMLBuilder.object()
                .put("host", "0.0.0.0")
                .put("port", 8080)
                .put("ssl", false)
                .build())
            .put("logging", MAMLBuilder.object()
                .put("level", "info")
                .put("format", "json")
                .build())
            .build();
        
        // Serialize with pretty-print
        System.out.println("Pretty-printed:");
        MAMLSerializer pretty = new MAMLSerializer(true, 2);
        System.out.println(pretty.serialize(value));
        
        // Serialize compact
        System.out.println("\nCompact:");
        MAMLSerializer compact = new MAMLSerializer(false, 0);
        System.out.println(compact.serialize(value));
    }
    
    private static void errorHandlingExample() {
        String invalidMaml = """
            {
                name: "John"
                age: 01  # Leading zero not allowed
            }
            """;
        
        try {
            MAML.parse(invalidMaml);
        } catch (MAMLException e) {
            System.out.println("Error caught: " + e.getMessage());
            System.out.println("Line: " + e.getLine() + ", Column: " + e.getColumn());
        }
        
        // Try parsing invalid JSON-like syntax
        String jsonLike = """
            {
                "key": "value",  // This comment style not allowed
            }
            """;
        
        try {
            MAML.parse(jsonLike);
        } catch (MAMLException e) {
            System.out.println("\nError caught: " + e.getMessage());
        }
    }
}
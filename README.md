# MAML Parser for Java

A comprehensive Java 17+ library for parsing and serializing MAML (Minimal Abstract Markup Language) v0.1.

## Features

- Full MAML v0.1 specification compliance
- Complete lexer and parser implementation
- Strongly-typed AST using Java records and sealed interfaces
- Support for all MAML types: objects, arrays, strings, multiline strings, integers, floats, booleans, and null
- Unicode escape sequences (`\u{XXXXXX}`)
- String escapes (`\t`, `\n`, `\r`, `\"`, `\\`)
- Comments support
- Identifier and quoted keys
- Optional commas and trailing commas
- Comprehensive error messages with line/column information
- Fluent builder API for programmatic construction
- Serialization support (pretty-print and compact modes)
- Zero external dependencies (except JUnit for tests)

## Installation

### Maven (not implemented yet - contributions welcome)

```xml
<dependency>
    <groupId>com.skanga</groupId>
    <artifactId>MAML-parser</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Build from Source

```bash
git clone https://github.com/skanga/maml-parser
cd maml-parser
mvn clean install
```

## Usage

### Parsing MAML

```java
import com.skanga.*;
import com.skanga.MAMLValue.*;

// Parse from string
String maml = """
    {
        name: "John Doe"
        age: 30
        email: "john@example.com"
        active: true
        roles: ["admin", "user"]
    }
    """;

MAMLValue value = MAML.parse(maml);

// Access values
MAMLObject obj = (MAMLObject) value;
String name = ((MAMLString) obj.get("name")).value();
long age = ((MAMLInteger) obj.get("age")).value();
boolean active = ((MAMLBoolean) obj.get("active")).value();

// Parse from file
MAMLValue fileValue = MAML.parseFile("config.maml");
```

### Building MAML Programmatically

```java
import com.skanga.*;

MAMLValue config = MAMLBuilder.object()
    .put("database", MAMLBuilder.object()
        .put("host", "localhost")
        .put("port", 5432)
        .put("name", "mydb")
        .build())
    .put("features", MAMLBuilder.array()
        .add("auth")
        .add("logging")
        .add("metrics")
        .build())
    .put("debug", false)
    .build();
```

### Serializing MAML

```java
import com.skanga.*;

MAMLValue value = MAMLBuilder.object()
    .put("name", "Alice")
    .put("score", 95.5)
    .build();

// Pretty-print (default)
MAMLSerializer serializer = new MAMLSerializer();
String maml = serializer.serialize(value);
System.out.println(maml);
// Output:
// {
//   name: "Alice"
//   score: 95.5
// }

// Compact format
MAMLSerializer compact = new MAMLSerializer(false, 0);
String compactMaml = compact.serialize(value);
System.out.println(compactMaml);
// Output: {name:"Alice",score:95.5}
```

### Working with Values

```java
MAMLObject obj = (MAMLObject) MAML.parse(maml);

// Check if key exists
if (obj.containsKey("email")) {
    MAMLValue email = obj.get("email");
}

// Get array elements
MAMLArray arr = (MAMLArray) obj.get("items");
for (int i = 0; i < arr.size(); i++) {
    MAMLValue item = arr.get(i);
    // Process item
}

// Type checking
MAMLValue val = obj.get("someKey");
switch (val) {
    case MAMLString s -> System.out.println("String: " + s.value());
    case MAMLInteger i -> System.out.println("Integer: " + i.value());
    case MAMLFloat f -> System.out.println("Float: " + f.value());
    case MAMLBoolean b -> System.out.println("Boolean: " + b.value());
    case MAMLNull n -> System.out.println("Null");
    case MAMLObject o -> System.out.println("Object with " + o.size() + " keys");
    case MAMLArray a -> System.out.println("Array with " + a.size() + " elements");
}
```

## MAML Language Features

### Objects

```maml
{
    key: "value"
    "quoted key": "value"
    nested: {
        inner: "value"
    }
}
```

### Arrays

```maml
# Comma-separated
["red", "yellow", "green"]

# Newline-separated
[
    "red"
    "yellow"
    "green"
]

# Mixed types
[1, "two", true, null, { key: "value" }]
```

### Strings

```maml
{
    simple: "Hello, World!"
    escaped: "Line 1\nLine 2\tTabbed"
    unicode: "Emoji: \u{1F601}"
}
```

### Multiline Strings

```maml
{
    poem: """
    Roses are red,
    Violets are blue;
    """
    
    # No trailing newline
    text: """
    First line
    Second line"""
}
```

### Numbers

```maml
{
    integer: 42
    negative: -100
    float: 3.14159
    exponent: 1.5e10
    negativeExp: -2E-5
}
```

### Booleans and Null

```maml
{
    active: true
    deleted: false
    optional: null
}
```

### Comments

```maml
# This is a comment
{
    foo: "value" # Inline comment
    bar: "# This is not a comment"
}
```

## API Reference

### Core Classes

#### `MAML`
Main entry point for parsing MAML documents.

- `static MAMLValue parse(String input)` - Parse MAML from string
- `static MAMLValue parseFile(String filename)` - Parse MAML from file
- `static MAMLValue parseFile(Path path)` - Parse MAML from file path

#### `MAMLValue`
Sealed interface representing all MAML values. Implementations:

- `MAMLObject(Map<String, MAMLValue> value)` - MAML object
- `MAMLArray(List<MAMLValue> value)` - MAML array
- `MAMLString(String value)` - String value
- `MAMLInteger(long value)` - Integer value (64-bit signed)
- `MAMLFloat(double value)` - Float value (IEEE 754 binary64)
- `MAMLBoolean(boolean value)` - Boolean value
- `MAMLNull()` - Null value

#### `MAMLBuilder`
Fluent API for building MAML values.

- `static ObjectBuilder object()` - Create object builder
- `static ArrayBuilder array()` - Create array builder

#### `MAMLSerializer`
Serializes MAMLValue objects back to MAML format.

- `MAMLSerializer()` - Default constructor (pretty-print, 2 spaces)
- `MAMLSerializer(boolean prettyPrint, int indentSize)` - Custom settings
- `String serialize(MAMLValue value)` - Serialize to MAML string

#### `MAMLException`
Exception thrown when parsing fails. Contains line and column information.

- `String getMessage()` - Error message with location
- `int getLine()` - Line number where error occurred
- `int getColumn()` - Column number where error occurred

## Error Handling

The parser provides detailed error messages with line and column information:

```java
try {
    MAMLValue value = MAML.parse(invalidMaml);
} catch (MAMLException e) {
    System.err.println(e.getMessage());
    // Example: "Unexpected character: '@' at line 5, column 12"
    
    System.err.println("Line: " + e.getLine());
    System.err.println("Column: " + e.getColumn());
}
```

## Examples

### Configuration File

```maml
# Application configuration
{
    app: {
        name: "MyApp"
        version: "1.0.0"
        debug: false
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
        "caching"
    ]
    
    limits: {
        maxConnections: 100
        timeout: 30.5
        retries: 3
    }
}
```

### Parsing and Accessing

```java
MAMLObject config = (MAMLObject) MAML.parseFile("config.maml");

// Access nested values
MAMLObject app = (MAMLObject) config.get("app");
String appName = ((MAMLString) app.get("name")).value();

// Access database config
MAMLObject db = (MAMLObject) config.get("database");
long port = ((MAMLInteger) db.get("port")).value();

// Access array
MAMLArray features = (MAMLArray) config.get("features");
for (int i = 0; i < features.size(); i++) {
    String feature = ((MAMLString) features.get(i)).value();
    System.out.println("Feature: " + feature);
}
```

### Building Complex Structures

```java
MAMLValue user = MAMLBuilder.object()
    .put("id", 12345)
    .put("username", "alice")
    .put("profile", MAMLBuilder.object()
        .put("firstName", "Alice")
        .put("lastName", "Smith")
        .put("age", 28)
        .put("bio", """
            Software engineer with a passion
            for building great products.
            """)
        .build())
    .put("permissions", MAMLBuilder.array()
        .add("read")
        .add("write")
        .add("admin")
        .build())
    .put("verified", true)
    .put("lastLogin", MAMLBuilder.object()
        .put("timestamp", 1634567890)
        .put("ipAddress", "192.168.1.100")
        .build())
    .build();

// Serialize to MAML
MAMLSerializer serializer = new MAMLSerializer();
String maml = serializer.serialize(user);
System.out.println(maml);
```

## Specification Compliance

This library fully implements the [MAML v0.1 specification](https://maml.dev/spec/v0.1):

- Case-sensitive parsing
- UTF-8 validation
- Whitespace handling (space 0x20, tab 0x09)
- Newline support (LF 0x0A, CRLF 0x0D 0x0A)
- Comment syntax with `#`
- Control character restrictions
- All value types (object, array, string, multiline string, integer, float, boolean, null)
- Array separators (comma or newline, optional trailing comma)
- Object key/value pairs with `:` separator
- Duplicate key detection
- Identifier keys (A-Z, a-z, 0-9, _, -)
- Quoted string keys
- String escape sequences (`\t`, `\n`, `\r`, `\"`, `\\`)
- Unicode escape sequences (`\u{XXXXXX}`)
- Multiline string syntax (`"""`)
- Integer range validation (64-bit signed)
- Float IEEE 754 binary64 support
- Leading zero prohibition
- Boolean lowercase enforcement
- Null lowercase enforcement

## Testing

Run the comprehensive test suite:

```bash
mvn test
```

The test suite includes:
- Basic value type parsing
- Nested structures
- String escapes and Unicode
- Multiline strings
- Comments
- Error cases
- Builder API
- Serialization round-trips

## Project Structure

```
src/
├── main/java/dev/maml/
│   ├── MAML.java              # Main entry point
│   ├── MAMLValue.java         # Value type definitions
│   ├── MAMLException.java     # Exception class
│   ├── MAMLLexer.java         # Tokenizer
│   ├── MAMLParser.java        # Parser
│   ├── MAMLSerializer.java    # Serializer
│   ├── MAMLBuilder.java       # Builder API
│   └── Token.java             # Token definitions
└── test/java/dev/maml/
    └── MAMLTest.java          # Test suite
```

## Requirements

- Java 17 or higher
- Maven 3.6 or higher (for building)

## License

This project is released under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## Related Links

- [MAML Specification v0.1](https://maml.dev/spec/v0.1)
- [MAML Official Website](https://maml.dev)

## Changelog

### Version 0.1.0 (Initial Release)
- Full MAML v0.1 specification support
- Complete lexer and parser
- Builder API
- Serialization support
- Comprehensive test coverage

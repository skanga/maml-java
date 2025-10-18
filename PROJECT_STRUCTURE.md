# MAML Parser - Project Structure

Complete directory structure for the MAML parser library.

```
maml-parser/
├── pom.xml                                 # Maven project configuration
├── README.md                               # Documentation
├── .gitignore                              # Git ignore rules
├── LICENSE                                 # MIT License
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── skanga/
│   │   │           ├── MAML.java          # Main API entry point
│   │   │           ├── MAMLValue.java     # Value type definitions (sealed interface)
│   │   │           ├── MAMLException.java # Exception class
│   │   │           ├── MAMLLexer.java     # Lexical analyzer (tokenizer)
│   │   │           ├── MAMLParser.java    # Syntax parser
│   │   │           ├── MAMLSerializer.java # Serializer (MAML value → string)
│   │   │           ├── MAMLBuilder.java   # Fluent builder API
│   │   │           └── Token.java         # Token record
│   │   │
│   │   └── resources/
│   │       └── (empty - no resources needed)
│   │
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── skanga/
│       │           ├── MAMLTest.java      # Comprehensive test suite
│       │           └── examples/
│       │               └── Example.java   # Example usage
│       │
│       └── resources/
│           └── test-files/
│               ├── simple.maml            # Test fixture
│               ├── complex.maml           # Test fixture
│               └── invalid.maml           # Test fixture
│
└── docs/
    ├── API.md                             # API documentation
    ├── EXAMPLES.md                        # Usage examples
    └── SPECIFICATION.md                   # MAML spec reference
```

## File Descriptions

### Core Library Files

**MAML.java**
- Main entry point for the library
- Static methods: `parse(String)`, `parseFile(Path)`, `parseFile(String)`
- UTF-8 validation
- Public API surface

**MAMLValue.java**
- Sealed interface defining all MAML value types
- Records for each type: `MAMLObject`, `MAMLArray`, `MAMLString`, `MAMLInteger`, `MAMLFloat`, `MAMLBoolean`, `MAMLNull`
- Type-safe representation of MAML data
- Immutable data structures

**MAMLException.java**
- Custom exception for parsing errors
- Contains line and column information
- Clear error messages

**MAMLLexer.java**
- Tokenizes MAML input
- Handles: strings, multiline strings, numbers, identifiers, keywords, structural characters
- Escape sequence processing
- Unicode escape handling
- Comment skipping
- Error detection with position tracking

**MAMLParser.java**
- Converts token stream to MAMLValue tree
- Recursive descent parser
- Validates syntax rules
- Checks for duplicate keys
- Handles optional commas and trailing commas

**MAMLSerializer.java**
- Converts MAMLValue back to MAML string
- Pretty-print and compact modes
- Configurable indentation
- Smart key formatting (identifier vs quoted)
- Smart string formatting (regular vs multiline)

**MAMLBuilder.java**
- Fluent API for building MAML structures
- `ObjectBuilder` for objects
- `ArrayBuilder` for arrays
- Type-safe methods for all value types

**Token.java**
- Record representing a lexical token
- Contains: type, value, line, column
- Used internally by lexer and parser

### Test Files

**MAMLTest.java**
- Comprehensive test coverage
- Tests for all value types
- Tests for edge cases
- Tests for error conditions
- Tests for builder API
- Tests for serialization round-trips

**Example.java**
- Demonstrates library usage
- Multiple examples covering different features
- Can be run as a standalone application

## Building the Library

### From Command Line

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package JAR
mvn package

# Install to local Maven repository
mvn install

# Generate JavaDoc
mvn javadoc:javadoc
```

### From IDE

**IntelliJ IDEA:**
1. File → Open → Select `pom.xml`
2. Wait for Maven import
3. Run tests: Right-click on `MAMLTest.java` → Run
4. Run example: Right-click on `Example.java` → Run

**Eclipse:**
1. File → Import → Existing Maven Projects
2. Select project directory
3. Run tests: Right-click on project → Run As → JUnit Test
4. Run example: Right-click on `Example.java` → Run As → Java Application

**VS Code:**
1. Open project folder
2. Install "Java Extension Pack"
3. Maven will auto-import
4. Use Testing sidebar to run tests

## Package Structure

All classes are in the `com.skanga` package:

```
com.skanga
├── MAML                  (public)
├── MAMLValue             (public, sealed interface)
│   ├── MAMLObject        (public, record)
│   ├── MAMLArray         (public, record)
│   ├── MAMLString        (public, record)
│   ├── MAMLInteger       (public, record)
│   ├── MAMLFloat         (public, record)
│   ├── MAMLBoolean       (public, record)
│   └── MAMLNull          (public, record)
├── MAMLException         (public)
├── MAMLBuilder           (public)
│   ├── ObjectBuilder     (public, static)
│   └── ArrayBuilder      (public, static)
├── MAMLSerializer        (public)
├── MAMLLexer             (package-private)
├── MAMLParser            (package-private)
└── Token                 (package-private, record)
```

## Public API

Only these classes are part of the public API:
- `MAML`
- `MAMLValue` and all its subtypes
- `MAMLException`
- `MAMLBuilder` and its inner classes
- `MAMLSerializer`

Internal classes (`MAMLLexer`, `MAMLParser`, `Token`) are package-private.

## Dependencies

### Runtime
- **None** - Zero runtime dependencies

### Test
- JUnit Jupiter 5.10.0

### Build
- Maven Compiler Plugin 3.11.0
- Maven Surefire Plugin 3.1.2

## Java Version

- **Minimum**: Java 17
- **Target**: Java 17
- Uses modern Java features:
    - Records
    - Sealed interfaces
    - Pattern matching for switch
    - Text blocks

## Creating a Release

```bash
# Update version in pom.xml
mvn versions:set -DnewVersion=0.2.0

# Build and test
mvn clean verify

# Create tag
git tag v0.2.0
git push origin v0.2.0

# Deploy to Maven Central (requires setup)
mvn clean deploy
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes
4. Add tests
5. Ensure all tests pass: `mvn test`
6. Submit pull request

## Code Style

- Follow standard Java conventions
- Use 4 spaces for indentation
- Keep lines under 120 characters
- Add JavaDoc for public API
- Write descriptive variable names
- Include unit tests for new features
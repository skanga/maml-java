package com.skanga;

import com.skanga.MAMLValue.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for MAML parser.
 */
class MAMLTest {

    @Test
    void testSimpleObject() {
        String maml = """
            {
                name: "John Doe"
                age: 30
            }
            """;

        MAMLValue value = MAML.parse(maml);
        assertInstanceOf(MAMLObject.class, value);

        MAMLObject obj = (MAMLObject) value;
        assertEquals(2, obj.size());
        assertEquals("John Doe", ((MAMLString) obj.get("name")).value());
        assertEquals(30L, ((MAMLInteger) obj.get("age")).value());
    }

    @Test
    void testArray() {
        String maml = """
            ["red", "yellow", "green"]
            """;

        MAMLValue value = MAML.parse(maml);
        assertInstanceOf(MAMLArray.class, value);

        MAMLArray arr = (MAMLArray) value;
        assertEquals(3, arr.size());
        assertEquals("red", ((MAMLString) arr.get(0)).value());
        assertEquals("yellow", ((MAMLString) arr.get(1)).value());
        assertEquals("green", ((MAMLString) arr.get(2)).value());
    }

    @Test
    void testNestedStructures() {
        String maml = """
            {
                user: {
                    name: "Alice"
                    roles: ["admin", "user"]
                }
                active: true
            }
            """;

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        MAMLObject user = (MAMLObject) obj.get("user");
        assertEquals("Alice", ((MAMLString) user.get("name")).value());

        MAMLArray roles = (MAMLArray) user.get("roles");
        assertEquals(2, roles.size());

        assertTrue(((MAMLBoolean) obj.get("active")).value());
    }

    @Test
    void testNumbers() {
        String maml = """
            {
                int: 42
                negative: -100
                float: 3.14
                exponent: 1e06
                negExp: -2E-2
            }
            """;

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        assertEquals(42L, ((MAMLInteger) obj.get("int")).value());
        assertEquals(-100L, ((MAMLInteger) obj.get("negative")).value());
        assertEquals(3.14, ((MAMLFloat) obj.get("float")).value(), 0.001);
        assertEquals(1e06, ((MAMLFloat) obj.get("exponent")).value(), 0.001);
        assertEquals(-2E-2, ((MAMLFloat) obj.get("negExp")).value(), 0.001);
    }

    @Test
    void testBooleanAndNull() {
        String maml = """
            {
                active: true
                deleted: false
                value: null
            }
            """;

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        assertTrue(((MAMLBoolean) obj.get("active")).value());
        assertFalse(((MAMLBoolean) obj.get("deleted")).value());
        assertInstanceOf(MAMLNull.class, obj.get("value"));
    }

    @Test
    void testStringEscapes() {
        String maml = "{" +
                "tab: \"a\\tb\"" +
                "newline: \"a\\nb\"" +
                "quote: \"say \\\"hello\\\"\"" +
                "unicode: \"\\u{1F601}\"" +
                "}";

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        assertEquals("a\tb", ((MAMLString) obj.get("tab")).value());
        assertEquals("a\nb", ((MAMLString) obj.get("newline")).value());
        assertEquals("say \"hello\"", ((MAMLString) obj.get("quote")).value());
        assertEquals("😁", ((MAMLString) obj.get("unicode")).value());
    }

    @Test
    void testMultilineString() {
        String maml = "{\n" +
                "poem: \"\"\"\n" +
                "Roses are red,\n" +
                "Violets are blue;\n" +
                "\"\"\"\n" +
                "}";

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        String poem = ((MAMLString) obj.get("poem")).value();
        assertEquals("Roses are red,\nViolets are blue;\n", poem);
    }

    @Test
    void testMultilineStringNoTrailingNewline() {
        String maml = "{\n" +
                "text: \"\"\"\n" +
                "Line 1\n" +
                "Line 2\"\"\"\n" +
                "}";

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        String text = ((MAMLString) obj.get("text")).value();
        assertEquals("Line 1\nLine 2", text);
    }

    @Test
    void testComments() {
        String maml = """
            # Comment before object
            {
                foo: "value" # Inline comment
                bar: "test"
            }
            """;

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        assertEquals(2, obj.size());
        assertEquals("value", ((MAMLString) obj.get("foo")).value());
        assertEquals("test", ((MAMLString) obj.get("bar")).value());
    }

    @Test
    void testQuotedKeys() {
        String maml = """
            {
                "quoted key": "value"
                "123": "numeric"
                "with-dash": "test"
            }
            """;

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        assertEquals("value", ((MAMLString) obj.get("quoted key")).value());
        assertEquals("numeric", ((MAMLString) obj.get("123")).value());
        assertEquals("test", ((MAMLString) obj.get("with-dash")).value());
    }

    @Test
    void testTrailingCommas() {
        String maml = """
            {
                a: 1,
                b: 2,
            }
            """;

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        assertEquals(2, obj.size());
    }

    @Test
    void testArrayWithNewlines() {
        String maml = """
            [
                "first"
                "second"
                "third"
            ]
            """;

        MAMLArray arr = (MAMLArray) MAML.parse(maml);
        assertEquals(3, arr.size());
    }

    @Test
    void testDuplicateKeyError() {
        String maml = """
            {
                key: "first"
                key: "second"
            }
            """;

        assertThrows(MAMLException.class, () -> MAML.parse(maml));
    }

    @Test
    void testLeadingZeroError() {
        String maml = "{ value: 01 }";
        assertThrows(MAMLException.class, () -> MAML.parse(maml));
    }

    @Test
    void testUnterminatedString() {
        String maml = "{ value: \"unterminated }";
        assertThrows(MAMLException.class, () -> MAML.parse(maml));
    }

    @Test
    void testInvalidEscape() {
        String maml = "{ value: \"\\x\" }";
        assertThrows(MAMLException.class, () -> MAML.parse(maml));
    }

    @Test
    void testBuilder() {
        MAMLValue value = MAMLBuilder.object()
                .put("name", "Alice")
                .put("age", 25)
                .put("active", true)
                .put("hobbies", MAMLBuilder.array()
                        .add("reading")
                        .add("coding")
                        .build())
                .putNull("middleName")
                .build();

        assertInstanceOf(MAMLObject.class, value);
        MAMLObject obj = (MAMLObject) value;
        assertEquals("Alice", ((MAMLString) obj.get("name")).value());
        assertEquals(25L, ((MAMLInteger) obj.get("age")).value());
        assertTrue(((MAMLBoolean) obj.get("active")).value());
        assertInstanceOf(MAMLNull.class, obj.get("middleName"));
    }

    @Test
    void testSerializer() {
        MAMLValue value = MAMLBuilder.object()
                .put("name", "Bob")
                .put("score", 95.5)
                .put("passed", true)
                .build();

        MAMLSerializer serializer = new MAMLSerializer();
        String maml = serializer.serialize(value);

        // Parse it back and verify
        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        assertEquals("Bob", ((MAMLString) obj.get("name")).value());
        assertEquals(95.5, ((MAMLFloat) obj.get("score")).value(), 0.001);
        assertTrue(((MAMLBoolean) obj.get("passed")).value());
    }

    @Test
    void testSerializerCompact() {
        MAMLValue value = MAMLBuilder.array()
                .add(1)
                .add(2)
                .add(3)
                .build();

        MAMLSerializer serializer = new MAMLSerializer(false, 0);
        String maml = serializer.serialize(value);

        assertFalse(maml.contains("\n"));
        MAMLArray arr = (MAMLArray) MAML.parse(maml);
        assertEquals(3, arr.size());
    }

    @Test
    void testEmptyObjectAndArray() {
        String maml = """
            {
                obj: {}
                arr: []
            }
            """;

        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        MAMLObject emptyObj = (MAMLObject) obj.get("obj");
        MAMLArray emptyArr = (MAMLArray) obj.get("arr");

        assertEquals(0, emptyObj.size());
        assertEquals(0, emptyArr.size());
    }

    @Test
    void testMixedTypes() {
        String maml = """
            [
                "string"
                42
                3.14
                true
                false
                null
                { nested: "object" }
                [1, 2, 3]
            ]
            """;

        MAMLArray arr = (MAMLArray) MAML.parse(maml);
        assertEquals(8, arr.size());
        assertInstanceOf(MAMLString.class, arr.get(0));
        assertInstanceOf(MAMLInteger.class, arr.get(1));
        assertInstanceOf(MAMLFloat.class, arr.get(2));
        assertInstanceOf(MAMLBoolean.class, arr.get(3));
        assertInstanceOf(MAMLBoolean.class, arr.get(4));
        assertInstanceOf(MAMLNull.class, arr.get(5));
        assertInstanceOf(MAMLObject.class, arr.get(6));
        assertInstanceOf(MAMLArray.class, arr.get(7));
    }

    @Test
    void testFile() throws IOException, URISyntaxException {
        Path path = Paths.get(getClass().getResource("/test.maml").toURI());
        String maml = Files.readString(path);
        MAMLObject obj = (MAMLObject) MAML.parse(maml);
        assertEquals(5, obj.size());
        assertEquals("MAML", ((MAMLString) obj.get("project")).value());

        MAMLArray arr = (MAMLArray) obj.get("tags");
        assertEquals(2, arr.size());
        assertEquals("minimal", ((MAMLString) arr.get(0)).value());
        assertEquals("readable", ((MAMLString) arr.get(1)).value());

        MAMLObject object = (MAMLObject) obj.get("spec");
        assertEquals(2, object.size());
        assertEquals(1, ((MAMLInteger) object.get("version")).value());
        assertEquals("Anton Medvedev", ((MAMLString) object.get("author")).value());

        MAMLArray array = (MAMLArray) obj.get("examples");
        MAMLObject object1 = (MAMLObject) array.get(0);
        assertEquals(2, object1.size());
        assertEquals("JSON", ((MAMLString) object1.get("name")).value());
        assertEquals(2001, ((MAMLInteger) object1.get("born")).value());
        MAMLObject object2 = (MAMLObject) array.get(1);
        assertEquals("MAML", ((MAMLString) object2.get("name")).value());
        assertEquals(2025, ((MAMLInteger) object2.get("born")).value());

        assertEquals("This is a multiline strings.Keeps formatting as-is.", ((MAMLString) obj.get("notes")).value().replaceAll("[\\r\\n]", ""));
    }
}
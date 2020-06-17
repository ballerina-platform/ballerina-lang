/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observability.anaylze;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * JSON Canonicalizer.
 *
 * @since 2.0.0
 */
public class JsonCanonicalizer {

    StringBuilder buffer;

    public JsonCanonicalizer(String jsonData) throws IOException {
        buffer = new StringBuilder();
        serialize(new JsonDecoder(jsonData).root);
    }

    public JsonCanonicalizer(byte[] jsonData) throws IOException {
        this(new String(jsonData, "utf-8"));
    }

    public static String getEncodedString(String toString) throws IOException {
        JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(toString);
        return jsonCanonicalizer.getEncodedString();
    }

    private void escape(char c) {
        buffer.append('\\').append(c);
    }

    private void serializeString(String value) {
        buffer.append('"');
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\n':
                    escape('n');
                    break;

                case '\b':
                    escape('b');
                    break;

                case '\f':
                    escape('f');
                    break;

                case '\r':
                    escape('r');
                    break;

                case '\t':
                    escape('t');
                    break;

                case '"':
                case '\\':
                    escape(c);
                    break;

                default:
                    if (c < 0x20) {
                        escape('u');
                        for (int i = 0; i < 4; i++) {
                            int hex = c >>> 12;
                            buffer.append((char) (hex > 9 ? hex + 'a' - 10 : hex + '0'));
                            c <<= 4;
                        }
                        break;
                    }
                    buffer.append(c);
            }
        }
        buffer.append('"');
    }

    @SuppressWarnings("unchecked")
    void serialize(Object o) throws IOException {
        if (o instanceof TreeMap) {
            buffer.append('{');
            boolean next = false;
            for (Map.Entry<String, Object> keyValue : ((TreeMap<String, Object>) o).entrySet()) {
                if (next) {
                    buffer.append(',');
                }
                next = true;
                serializeString(keyValue.getKey());
                buffer.append(':');
                serialize(keyValue.getValue());
            }
            buffer.append('}');
        } else if (o instanceof Vector) {
            buffer.append('[');
            boolean next = false;
            for (Object value : ((Vector<Object>) o).toArray()) {
                if (next) {
                    buffer.append(',');
                }
                next = true;
                serialize(value);
            }
            buffer.append(']');
        } else if (o == null) {
            buffer.append("null");
        } else if (o instanceof String) {
            serializeString((String) o);
        } else if (o instanceof Boolean) {
            buffer.append((boolean) o);
        } else if (o instanceof Double) {
            buffer.append(NumberToJSON.serializeNumber((Double) o));
        } else {
            throw new InternalError("Unknown object: " + o);
        }
    }

    public String getEncodedString() {
        return buffer.toString();
    }

    public byte[] getEncodedUTF8() throws IOException {
        return getEncodedString().getBytes("utf-8");
    }
}

class JsonDecoder {

    static final char LEFT_CURLY_BRACKET = '{';
    static final char RIGHT_CURLY_BRACKET = '}';
    static final char DOUBLE_QUOTE = '"';
    static final char COLON_CHARACTER = ':';
    static final char LEFT_BRACKET = '[';
    static final char RIGHT_BRACKET = ']';
    static final char COMMA_CHARACTER = ',';
    static final char BACK_SLASH = '\\';

    static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false");
    static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?([eE][-+]?[0-9]+)?");

    int index;

    int maxLength;

    String jsonData;

    Object root;

    JsonDecoder(String jsonString) throws IOException {
        jsonData = jsonString;
        maxLength = jsonData.length();
        if (testNextNonWhiteSpaceChar() == LEFT_BRACKET) {
            scan();
            root = parseArray();
        } else {
            scanFor(LEFT_CURLY_BRACKET);
            root = parseObject();
        }
        while (index < maxLength) {
            if (!isWhiteSpace(jsonData.charAt(index++))) {
                throw new IOException("Improperly terminated JSON object");
            }
        }
    }

    Object parseElement() throws IOException {
        switch (scan()) {
            case LEFT_CURLY_BRACKET:
                return parseObject();

            case DOUBLE_QUOTE:
                return parseQuotedString();

            case LEFT_BRACKET:
                return parseArray();

            default:
                return parseSimpleType();
        }
    }

    Object parseObject() throws IOException {
        TreeMap<String, Object> dict = new TreeMap<String, Object>();
        boolean next = false;
        while (testNextNonWhiteSpaceChar() != RIGHT_CURLY_BRACKET) {
            if (next) {
                scanFor(COMMA_CHARACTER);
            }
            next = true;
            scanFor(DOUBLE_QUOTE);
            String name = parseQuotedString();
            scanFor(COLON_CHARACTER);
            if (dict.put(name, parseElement()) != null) {
                throw new IOException("Duplicate property: " + name);
            }
        }
        scan();
        return dict;
    }

    Object parseArray() throws IOException {
        Vector<Object> array = new Vector<Object>();
        boolean next = false;
        while (testNextNonWhiteSpaceChar() != RIGHT_BRACKET) {
            if (next) {
                scanFor(COMMA_CHARACTER);
            } else {
                next = true;
            }
            array.add(parseElement());
        }
        scan();
        return array;
    }

    Object parseSimpleType() throws IOException {
        index--;
        StringBuilder tempBuffer = new StringBuilder();
        char c;
        while ((c = testNextNonWhiteSpaceChar()) != COMMA_CHARACTER && c != RIGHT_BRACKET && c != RIGHT_CURLY_BRACKET) {
            if (isWhiteSpace(c = nextChar())) {
                break;
            }
            tempBuffer.append(c);
        }
        String token = tempBuffer.toString();
        if (token.length() == 0) {
            throw new IOException("Missing argument");
        }
        if (NUMBER_PATTERN.matcher(token).matches()) {
            return Double.valueOf(token);  // Syntax check...
        } else if (BOOLEAN_PATTERN.matcher(token).matches()) {
            return Boolean.valueOf(token);
        } else if (token.equals("null")) {
            return null;
        } else {
            throw new IOException("Unrecognized or malformed JSON token: " + token);
        }
    }

    String parseQuotedString() throws IOException {
        StringBuilder result = new StringBuilder();
        while (true) {
            char c = nextChar();
            if (c < ' ') {
                throw new IOException(c == '\n' ?
                        "Unterminated string literal" : "Unescaped control character: 0x" + Integer.toString(c, 16));
            }
            if (c == DOUBLE_QUOTE) {
                break;
            }
            if (c == BACK_SLASH) {
                switch (c = nextChar()) {
                    case '"':
                    case '\\':
                    case '/':
                        break;

                    case 'b':
                        c = '\b';
                        break;

                    case 'f':
                        c = '\f';
                        break;

                    case 'n':
                        c = '\n';
                        break;

                    case 'r':
                        c = '\r';
                        break;

                    case 't':
                        c = '\t';
                        break;

                    case 'u':
                        c = 0;
                        for (int i = 0; i < 4; i++) {
                            c = (char) ((c << 4) + getHexChar());
                        }
                        break;

                    default:
                        throw new IOException("Unsupported escape:" + c);
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    char getHexChar() throws IOException {
        char c = nextChar();
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return (char) (c - '0');

            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return (char) (c - 'a' + 10);

            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return (char) (c - 'A' + 10);
        }
        throw new IOException("Bad hex in \\u escape: " + c);
    }

    char testNextNonWhiteSpaceChar() throws IOException {
        int save = index;
        char c = scan();
        index = save;
        return c;
    }

    void scanFor(char expected) throws IOException {
        char c = scan();
        if (c != expected) {
            throw new IOException("Expected '" + expected + "' but got '" + c + "'");
        }
    }

    char nextChar() throws IOException {
        if (index < maxLength) {
            return jsonData.charAt(index++);
        }
        throw new IOException("Unexpected EOF reached");
    }

    boolean isWhiteSpace(char c) {
        return c == 0x20 || c == 0x0A || c == 0x0D || c == 0x09;
    }

    char scan() throws IOException {
        while (true) {
            char c = nextChar();
            if (isWhiteSpace(c)) {
                continue;
            }
            return c;
        }
    }
}

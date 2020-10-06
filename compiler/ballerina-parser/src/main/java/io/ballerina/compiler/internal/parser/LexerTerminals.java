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
package io.ballerina.compiler.internal.parser;

/**
 * Contains lexer terminal nodes. Includes keywords, syntaxes, and operators.
 *
 * @since 1.2.0
 */
public class LexerTerminals {

    // Keywords
    public static final String PUBLIC = "public";
    public static final String PRIVATE = "private";
    public static final String FUNCTION = "function";
    public static final String RETURN = "return";
    public static final String RETURNS = "returns";
    public static final String EXTERNAL = "external";
    public static final String TYPE = "type";
    public static final String RECORD = "record";
    public static final String OBJECT = "object";
    public static final String REMOTE = "remote";
    public static final String ABSTRACT = "abstract";
    public static final String CLIENT = "client";
    public static final String IF = "if";
    public static final String ELSE = "else";
    public static final String WHILE = "while";
    public static final String PANIC = "panic";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String CHECK = "check";
    public static final String FAIL = "fail";
    public static final String CHECKPANIC = "checkpanic";
    public static final String CONTINUE = "continue";
    public static final String BREAK = "break";
    public static final String IMPORT = "import";
    public static final String VERSION = "version";
    public static final String AS = "as";
    public static final String ON = "on";
    public static final String RESOURCE = "resource";
    public static final String LISTENER = "listener";
    public static final String CONST = "const";
    public static final String FINAL = "final";
    public static final String TYPEOF = "typeof";
    public static final String IS = "is";
    public static final String NULL = "null";
    public static final String LOCK = "lock";
    public static final String ANNOTATION = "annotation";
    public static final String SOURCE = "source";
    public static final String WORKER = "worker";
    public static final String PARAMETER = "parameter";
    public static final String FIELD = "field";
    public static final String ISOLATED = "isolated";
    public static final String XMLNS = "xmlns";
    public static final String FORK = "fork";
    public static final String TRAP = "trap";
    public static final String IN = "in";
    public static final String FOREACH = "foreach";
    public static final String TABLE = "table";
    public static final String KEY = "key";
    public static final String ERROR = "error";
    public static final String LET = "let";
    public static final String STREAM = "stream";
    public static final String NEW = "new";
    public static final String READONLY = "readonly";
    public static final String DISTINCT = "distinct";
    public static final String FROM = "from";
    public static final String WHERE = "where";
    public static final String SELECT = "select";
    public static final String START = "start";
    public static final String FLUSH = "flush";
    public static final String DEFAULT = "default";
    public static final String WAIT = "wait";
    public static final String DO = "do";
    public static final String TRANSACTION = "transaction";
    public static final String TRANSACTIONAL = "transactional";
    public static final String COMMIT = "commit";
    public static final String RETRY = "retry";
    public static final String ROLLBACK = "rollback";
    public static final String ENUM = "enum";
    public static final String BASE16 = "base16";
    public static final String BASE64 = "base64";
    public static final String MATCH = "match";
    public static final String CONFLICT = "conflict";
    public static final String LIMIT = "limit";
    public static final String JOIN = "join";
    public static final String OUTER = "outer";
    public static final String EQUALS = "equals";
    public static final String ORDER = "order";
    public static final String BY = "by";
    public static final String ASCENDING = "ascending";
    public static final String DESCENDING = "descending";
    public static final String CLASS = "class";

    // For BFM only
    public static final String VARIABLE = "variable";
    public static final String MODULE = "module";

    // Types
    public static final String INT = "int";
    public static final String FLOAT = "float";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String DECIMAL = "decimal";
    public static final String XML = "xml";
    public static final String JSON = "json";
    public static final String HANDLE = "handle";
    public static final String ANY = "any";
    public static final String ANYDATA = "anydata";
    public static final String SERVICE = "service";
    public static final String VAR = "var";
    public static final String NEVER = "never";
    public static final String MAP = "map";
    public static final String FUTURE = "future";
    public static final String TYPEDESC = "typedesc";
    public static final String BYTE = "byte";

    // Separators
    public static final char SEMICOLON = ';';
    public static final char COLON = ':';
    public static final char DOT = '.';
    public static final char COMMA = ',';
    public static final char OPEN_PARANTHESIS = '(';
    public static final char CLOSE_PARANTHESIS = ')';
    public static final char OPEN_BRACE = '{';
    public static final char CLOSE_BRACE = '}';
    public static final char OPEN_BRACKET = '[';
    public static final char CLOSE_BRACKET = ']';
    public static final char PIPE = '|';
    public static final char QUESTION_MARK = '?';
    public static final char DOUBLE_QUOTE = '"';
    public static final char SINGLE_QUOTE = '\'';
    public static final char HASH = '#';
    public static final char AT = '@';
    public static final char BACKTICK = '`';
    public static final char DOLLAR = '$';

    // Arithmetic operators
    public static final char EQUAL = '=';
    public static final char PLUS = '+';
    public static final char MINUS = '-';
    public static final char ASTERISK = '*';
    public static final char SLASH = '/';
    public static final char PERCENT = '%';
    public static final char GT = '>';
    public static final char LT = '<';
    public static final char BACKSLASH = '\\';
    public static final char EXCLAMATION_MARK = '!';
    public static final char BITWISE_AND = '&';
    public static final char BITWISE_XOR = '^';
    public static final char NEGATION = '~';

    // Other
    public static final char NEWLINE = '\n'; // equivalent to 0xA
    public static final char CARRIAGE_RETURN = '\r'; // equivalent to 0xD
    public static final char TAB = 0x9;
    public static final char SPACE = 0x20;
    public static final char FORM_FEED = 0xC;
}

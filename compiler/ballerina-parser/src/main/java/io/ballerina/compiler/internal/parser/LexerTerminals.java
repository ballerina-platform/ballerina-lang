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
    private static Configurations configurations = Configurations.getInstance();
    // Keywords
    public static final String PUBLIC = configurations.getProperty("public");
    public static final String PRIVATE = configurations.getProperty("private");
    public static final String FUNCTION = configurations.getProperty("function");
    public static final String RETURN = configurations.getProperty("return");
    public static final String RETURNS = configurations.getProperty("returns");
    public static final String EXTERNAL = configurations.getProperty("external");
    public static final String TYPE = configurations.getProperty("type");
    public static final String RECORD = configurations.getProperty("record");
    public static final String OBJECT = configurations.getProperty("object");
    public static final String REMOTE = configurations.getProperty("remote");
    public static final String ABSTRACT = configurations.getProperty("abstract");
    public static final String CLIENT = configurations.getProperty("client");
    public static final String IF = configurations.getProperty("if");
    public static final String ELSE = configurations.getProperty("else");
    public static final String WHILE = configurations.getProperty("while");
    public static final String PANIC = configurations.getProperty("panic");
    public static final String TRUE = configurations.getProperty("true");
    public static final String FALSE = configurations.getProperty("false");
    public static final String CHECK = configurations.getProperty("check");
    public static final String FAIL = configurations.getProperty("fail");
    public static final String CHECKPANIC = configurations.getProperty("checkpanic");
    public static final String CONTINUE = configurations.getProperty("continue");
    public static final String BREAK = configurations.getProperty("break");
    public static final String IMPORT = configurations.getProperty("import");
    public static final String VERSION = configurations.getProperty("version");
    public static final String AS = configurations.getProperty("as");
    public static final String ON = configurations.getProperty("on");
    public static final String RESOURCE = configurations.getProperty("resource");
    public static final String LISTENER = configurations.getProperty("listener");
    public static final String CONST = configurations.getProperty("const");
    public static final String FINAL = configurations.getProperty("final");
    public static final String TYPEOF = configurations.getProperty("typeof");
    public static final String IS = configurations.getProperty("is");
    public static final String NULL = configurations.getProperty("null");
    public static final String LOCK = configurations.getProperty("lock");
    public static final String ANNOTATION = configurations.getProperty("annotation");
    public static final String SOURCE = configurations.getProperty("source");
    public static final String WORKER = configurations.getProperty("worker");
    public static final String PARAMETER = configurations.getProperty("parameter");
    public static final String FIELD = configurations.getProperty("field");
    public static final String ISOLATED = configurations.getProperty("isolated");
    public static final String XMLNS = configurations.getProperty("xmlns");
    public static final String FORK = configurations.getProperty("fork");
    public static final String TRAP = configurations.getProperty("trap");
    public static final String IN = configurations.getProperty("in");
    public static final String FOREACH = configurations.getProperty("foreach");
    public static final String TABLE = configurations.getProperty("table");
    public static final String KEY = configurations.getProperty("key");
    public static final String ERROR = configurations.getProperty("error");
    public static final String LET = configurations.getProperty("let");
    public static final String STREAM = configurations.getProperty("stream");
    public static final String NEW = configurations.getProperty("new");
    public static final String READONLY = configurations.getProperty("readonly");
    public static final String DISTINCT = configurations.getProperty("distinct");
    public static final String FROM = configurations.getProperty("from");
    public static final String WHERE = configurations.getProperty("where");
    public static final String SELECT = configurations.getProperty("select");
    public static final String START = configurations.getProperty("start");
    public static final String FLUSH = configurations.getProperty("flush");
    public static final String DEFAULT = configurations.getProperty("default");
    public static final String WAIT = configurations.getProperty("wait");
    public static final String DO = configurations.getProperty("do");
    public static final String TRANSACTION = configurations.getProperty("transaction");
    public static final String TRANSACTIONAL = configurations.getProperty("transactional");
    public static final String COMMIT = configurations.getProperty("commit");
    public static final String RETRY = configurations.getProperty("retry");
    public static final String ROLLBACK = configurations.getProperty("rollback");
    public static final String ENUM = configurations.getProperty("enum");
    public static final String BASE16 = configurations.getProperty("base16");
    public static final String BASE64 = configurations.getProperty("base64");
    public static final String MATCH = configurations.getProperty("match");
    public static final String CONFLICT = configurations.getProperty("conflict");
    public static final String LIMIT = configurations.getProperty("limit");
    public static final String JOIN = configurations.getProperty("join");
    public static final String OUTER = configurations.getProperty("outer");
    public static final String EQUALS = configurations.getProperty("equals");
    public static final String ORDER = configurations.getProperty("order");
    public static final String BY = configurations.getProperty("by");
    public static final String ASCENDING = configurations.getProperty("ascending");
    public static final String DESCENDING = configurations.getProperty("descending");
    public static final String CLASS = configurations.getProperty("class");
    public static final String CONFIGURABLE = configurations.getProperty("configurable");

    // For BFM only
    public static final String VARIABLE = configurations.getProperty("variable");
    public static final String MODULE = configurations.getProperty("module");

    // Types
    public static final String INT = configurations.getProperty("int");
    public static final String FLOAT = configurations.getProperty("float");
    public static final String STRING = configurations.getProperty("string");
    public static final String BOOLEAN = configurations.getProperty("boolean");
    public static final String DECIMAL = configurations.getProperty("decimal");
    public static final String XML = configurations.getProperty("xml");
    public static final String JSON = configurations.getProperty("json");
    public static final String HANDLE = configurations.getProperty("handle");
    public static final String ANY = configurations.getProperty("any");
    public static final String ANYDATA = configurations.getProperty("anydata");
    public static final String SERVICE = configurations.getProperty("service");
    public static final String VAR = configurations.getProperty("var");
    public static final String NEVER = configurations.getProperty("never");
    public static final String MAP = configurations.getProperty("map");
    public static final String FUTURE = configurations.getProperty("future");
    public static final String TYPEDESC = configurations.getProperty("typedesc");
    public static final String BYTE = configurations.getProperty("byte");

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

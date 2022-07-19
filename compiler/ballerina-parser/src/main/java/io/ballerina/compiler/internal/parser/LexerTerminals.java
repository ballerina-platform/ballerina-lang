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
    static String language = "en";

    // Keywords
    public static final String PUBLIC = Configurations.getInstance().getProperty("public");
    public static final String PRIVATE = Configurations.getInstance().getProperty("private");
    public static final String FUNCTION = Configurations.getInstance().getProperty("function");
    public static final String RETURN = Configurations.getInstance().getProperty("return");
    public static final String RETURNS = Configurations.getInstance().getProperty("returns");
    public static final String EXTERNAL = Configurations.getInstance().getProperty("external");
    public static final String TYPE = Configurations.getInstance().getProperty("type");
    public static final String RECORD = Configurations.getInstance().getProperty("record");
    public static final String OBJECT = Configurations.getInstance().getProperty("object");
    public static final String REMOTE = Configurations.getInstance().getProperty("remote");
    public static final String ABSTRACT = Configurations.getInstance().getProperty("abstract");
    public static final String CLIENT = Configurations.getInstance().getProperty("client");
    public static final String IF = Configurations.getInstance().getProperty("if");
    public static final String ELSE = Configurations.getInstance().getProperty("else");
    public static final String WHILE = Configurations.getInstance().getProperty("while");
    public static final String PANIC = Configurations.getInstance().getProperty("panic");
    public static final String TRUE = Configurations.getInstance().getProperty("true");
    public static final String FALSE = Configurations.getInstance().getProperty("false");
    public static final String CHECK = Configurations.getInstance().getProperty("check");
    public static final String FAIL = Configurations.getInstance().getProperty("fail");
    public static final String CHECKPANIC = Configurations.getInstance().getProperty("checkpanic");
    public static final String CONTINUE = Configurations.getInstance().getProperty("continue");
    public static final String BREAK = Configurations.getInstance().getProperty("break");
    public static final String IMPORT = Configurations.getInstance().getProperty("import");
    public static final String VERSION = Configurations.getInstance().getProperty("version");
    public static final String AS = Configurations.getInstance().getProperty("as");
    public static final String ON = Configurations.getInstance().getProperty("on");
    public static final String RESOURCE = Configurations.getInstance().getProperty("resource");
    public static final String LISTENER = Configurations.getInstance().getProperty("listener");
    public static final String CONST = Configurations.getInstance().getProperty("const");
    public static final String FINAL = Configurations.getInstance().getProperty("final");
    public static final String TYPEOF = Configurations.getInstance().getProperty("typeof");
    public static final String IS = Configurations.getInstance().getProperty("is");
    public static final String NULL = Configurations.getInstance().getProperty("null");
    public static final String LOCK = Configurations.getInstance().getProperty("lock");
    public static final String ANNOTATION = Configurations.getInstance().getProperty("annotation");
    public static final String SOURCE = Configurations.getInstance().getProperty("source");
    public static final String WORKER = Configurations.getInstance().getProperty("worker");
    public static final String PARAMETER = Configurations.getInstance().getProperty("parameter");
    public static final String FIELD = Configurations.getInstance().getProperty("field");
    public static final String ISOLATED = Configurations.getInstance().getProperty("isolated");
    public static final String XMLNS = Configurations.getInstance().getProperty("xmlns");
    public static final String FORK = Configurations.getInstance().getProperty("fork");
    public static final String TRAP = Configurations.getInstance().getProperty("trap");
    public static final String IN = Configurations.getInstance().getProperty("in");
    public static final String FOREACH = Configurations.getInstance().getProperty("foreach");
    public static final String TABLE = Configurations.getInstance().getProperty("table");
    public static final String KEY = Configurations.getInstance().getProperty("key");
    public static final String ERROR = Configurations.getInstance().getProperty("error");
    public static final String LET = Configurations.getInstance().getProperty("let");
    public static final String STREAM = Configurations.getInstance().getProperty("stream");
    public static final String NEW = Configurations.getInstance().getProperty("new");
    public static final String READONLY = Configurations.getInstance().getProperty("readonly");
    public static final String DISTINCT = Configurations.getInstance().getProperty("distinct");
    public static final String FROM = Configurations.getInstance().getProperty("from");
    public static final String WHERE = Configurations.getInstance().getProperty("where");
    public static final String SELECT = Configurations.getInstance().getProperty("select");
    public static final String START = Configurations.getInstance().getProperty("start");
    public static final String FLUSH = Configurations.getInstance().getProperty("flush");
    public static final String DEFAULT = Configurations.getInstance().getProperty("default");
    public static final String WAIT = Configurations.getInstance().getProperty("wait");
    public static final String DO = Configurations.getInstance().getProperty("do");
    public static final String TRANSACTION = Configurations.getInstance().getProperty("transaction");
    public static final String TRANSACTIONAL = Configurations.getInstance().getProperty("transactional");
    public static final String COMMIT = Configurations.getInstance().getProperty("commit");
    public static final String RETRY = Configurations.getInstance().getProperty("retry");
    public static final String ROLLBACK = Configurations.getInstance().getProperty("rollback");
    public static final String ENUM = Configurations.getInstance().getProperty("enum");
    public static final String BASE16 = Configurations.getInstance().getProperty("base16");
    public static final String BASE64 = Configurations.getInstance().getProperty("base64");
    public static final String MATCH = Configurations.getInstance().getProperty("match");
    public static final String CONFLICT = Configurations.getInstance().getProperty("conflict");
    public static final String LIMIT = Configurations.getInstance().getProperty("limit");
    public static final String JOIN = Configurations.getInstance().getProperty("join");
    public static final String OUTER = Configurations.getInstance().getProperty("outer");
    public static final String EQUALS = Configurations.getInstance().getProperty("equals");
    public static final String ORDER = Configurations.getInstance().getProperty("order");
    public static final String BY = Configurations.getInstance().getProperty("by");
    public static final String ASCENDING = Configurations.getInstance().getProperty("ascending");
    public static final String DESCENDING = Configurations.getInstance().getProperty("descending");
    public static final String CLASS = Configurations.getInstance().getProperty("class");
    public static final String CONFIGURABLE = Configurations.getInstance().getProperty("configurable");

    // For BFM only
    public static final String VARIABLE = Configurations.getInstance().getProperty("variable");
    public static final String MODULE = Configurations.getInstance().getProperty("module");

    // Types
    public static final String INT = Configurations.getInstance().getProperty("int");
    public static final String FLOAT = Configurations.getInstance().getProperty("float");
    public static final String STRING = Configurations.getInstance().getProperty("string");
    public static final String BOOLEAN = Configurations.getInstance().getProperty("boolean");
    public static final String DECIMAL = Configurations.getInstance().getProperty("decimal");
    public static final String XML = Configurations.getInstance().getProperty("xml");
    public static final String JSON = Configurations.getInstance().getProperty("json");
    public static final String HANDLE = Configurations.getInstance().getProperty("handle");
    public static final String ANY = Configurations.getInstance().getProperty("any");
    public static final String ANYDATA = Configurations.getInstance().getProperty("anydata");
    public static final String SERVICE = Configurations.getInstance().getProperty("service");
    public static final String VAR = Configurations.getInstance().getProperty("var");
    public static final String NEVER = Configurations.getInstance().getProperty("never");
    public static final String MAP = Configurations.getInstance().getProperty("map");
    public static final String FUTURE = Configurations.getInstance().getProperty("future");
    public static final String TYPEDESC = Configurations.getInstance().getProperty("typedesc");
    public static final String BYTE = Configurations.getInstance().getProperty("byte");

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

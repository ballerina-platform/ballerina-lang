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
    public static final String PUBLIC = Configurations.getInstance(language).getProperty("public");
    public static final String PRIVATE = Configurations.getInstance(language).getProperty("private");
    public static final String FUNCTION = Configurations.getInstance(language).getProperty("function");
    public static final String RETURN = Configurations.getInstance(language).getProperty("return");
    public static final String RETURNS = Configurations.getInstance(language).getProperty("returns");
    public static final String EXTERNAL = Configurations.getInstance(language).getProperty("external");
    public static final String TYPE = Configurations.getInstance(language).getProperty("type");
    public static final String RECORD = Configurations.getInstance(language).getProperty("record");
    public static final String OBJECT = Configurations.getInstance(language).getProperty("object");
    public static final String REMOTE = Configurations.getInstance(language).getProperty("remote");
    public static final String ABSTRACT = Configurations.getInstance(language).getProperty("abstract");
    public static final String CLIENT = Configurations.getInstance(language).getProperty("client");
    public static final String IF = Configurations.getInstance(language).getProperty("if");
    public static final String ELSE = Configurations.getInstance(language).getProperty("else");
    public static final String WHILE = Configurations.getInstance(language).getProperty("while");
    public static final String PANIC = Configurations.getInstance(language).getProperty("panic");
    public static final String TRUE = Configurations.getInstance(language).getProperty("true");
    public static final String FALSE = Configurations.getInstance(language).getProperty("false");
    public static final String CHECK = Configurations.getInstance(language).getProperty("check");
    public static final String FAIL = Configurations.getInstance(language).getProperty("fail");
    public static final String CHECKPANIC = Configurations.getInstance(language).getProperty("checkpanic");
    public static final String CONTINUE = Configurations.getInstance(language).getProperty("continue");
    public static final String BREAK = Configurations.getInstance(language).getProperty("break");
    public static final String IMPORT = Configurations.getInstance(language).getProperty("import");
    public static final String VERSION = Configurations.getInstance(language).getProperty("version");
    public static final String AS = Configurations.getInstance(language).getProperty("as");
    public static final String ON = Configurations.getInstance(language).getProperty("on");
    public static final String RESOURCE = Configurations.getInstance(language).getProperty("resource");
    public static final String LISTENER = Configurations.getInstance(language).getProperty("listener");
    public static final String CONST = Configurations.getInstance(language).getProperty("const");
    public static final String FINAL = Configurations.getInstance(language).getProperty("final");
    public static final String TYPEOF = Configurations.getInstance(language).getProperty("typeof");
    public static final String IS = Configurations.getInstance(language).getProperty("is");
    public static final String NULL = Configurations.getInstance(language).getProperty("null");
    public static final String LOCK = Configurations.getInstance(language).getProperty("lock");
    public static final String ANNOTATION = Configurations.getInstance(language).getProperty("annotation");
    public static final String SOURCE = Configurations.getInstance(language).getProperty("source");
    public static final String WORKER = Configurations.getInstance(language).getProperty("worker");
    public static final String PARAMETER = Configurations.getInstance(language).getProperty("parameter");
    public static final String FIELD = Configurations.getInstance(language).getProperty("field");
    public static final String ISOLATED = Configurations.getInstance(language).getProperty("isolated");
    public static final String XMLNS = Configurations.getInstance(language).getProperty("xmlns");
    public static final String FORK = Configurations.getInstance(language).getProperty("fork");
    public static final String TRAP = Configurations.getInstance(language).getProperty("trap");
    public static final String IN = Configurations.getInstance(language).getProperty("in");
    public static final String FOREACH = Configurations.getInstance(language).getProperty("foreach");
    public static final String TABLE = Configurations.getInstance(language).getProperty("table");
    public static final String KEY = Configurations.getInstance(language).getProperty("key");
    public static final String ERROR = Configurations.getInstance(language).getProperty("error");
    public static final String LET = Configurations.getInstance(language).getProperty("let");
    public static final String STREAM = Configurations.getInstance(language).getProperty("stream");
    public static final String NEW = Configurations.getInstance(language).getProperty("new");
    public static final String READONLY = Configurations.getInstance(language).getProperty("readonly");
    public static final String DISTINCT = Configurations.getInstance(language).getProperty("distinct");
    public static final String FROM = Configurations.getInstance(language).getProperty("from");
    public static final String WHERE = Configurations.getInstance(language).getProperty("where");
    public static final String SELECT = Configurations.getInstance(language).getProperty("select");
    public static final String START = Configurations.getInstance(language).getProperty("start");
    public static final String FLUSH = Configurations.getInstance(language).getProperty("flush");
    public static final String DEFAULT = Configurations.getInstance(language).getProperty("default");
    public static final String WAIT = Configurations.getInstance(language).getProperty("wait");
    public static final String DO = Configurations.getInstance(language).getProperty("do");
    public static final String TRANSACTION = Configurations.getInstance(language).getProperty("transaction");
    public static final String TRANSACTIONAL = Configurations.getInstance(language).getProperty("transactional");
    public static final String COMMIT = Configurations.getInstance(language).getProperty("commit");
    public static final String RETRY = Configurations.getInstance(language).getProperty("retry");
    public static final String ROLLBACK = Configurations.getInstance(language).getProperty("rollback");
    public static final String ENUM = Configurations.getInstance(language).getProperty("enum");
    public static final String BASE16 = Configurations.getInstance(language).getProperty("base16");
    public static final String BASE64 = Configurations.getInstance(language).getProperty("base64");
    public static final String MATCH = Configurations.getInstance(language).getProperty("match");
    public static final String CONFLICT = Configurations.getInstance(language).getProperty("conflict");
    public static final String LIMIT = Configurations.getInstance(language).getProperty("limit");
    public static final String JOIN = Configurations.getInstance(language).getProperty("join");
    public static final String OUTER = Configurations.getInstance(language).getProperty("outer");
    public static final String EQUALS = Configurations.getInstance(language).getProperty("equals");
    public static final String ORDER = Configurations.getInstance(language).getProperty("order");
    public static final String BY = Configurations.getInstance(language).getProperty("by");
    public static final String ASCENDING = Configurations.getInstance(language).getProperty("ascending");
    public static final String DESCENDING = Configurations.getInstance(language).getProperty("descending");
    public static final String CLASS = Configurations.getInstance(language).getProperty("class");
    public static final String CONFIGURABLE = Configurations.getInstance(language).getProperty("configurable");

    // For BFM only
    public static final String VARIABLE = Configurations.getInstance(language).getProperty("variable");
    public static final String MODULE = Configurations.getInstance(language).getProperty("module");

    // Types
    public static final String INT = Configurations.getInstance(language).getProperty("int");
    public static final String FLOAT = Configurations.getInstance(language).getProperty("float");
    public static final String STRING = Configurations.getInstance(language).getProperty("string");
    public static final String BOOLEAN = Configurations.getInstance(language).getProperty("boolean");
    public static final String DECIMAL = Configurations.getInstance(language).getProperty("decimal");
    public static final String XML = Configurations.getInstance(language).getProperty("xml");
    public static final String JSON = Configurations.getInstance(language).getProperty("json");
    public static final String HANDLE = Configurations.getInstance(language).getProperty("handle");
    public static final String ANY = Configurations.getInstance(language).getProperty("any");
    public static final String ANYDATA = Configurations.getInstance(language).getProperty("anydata");
    public static final String SERVICE = Configurations.getInstance(language).getProperty("service");
    public static final String VAR = Configurations.getInstance(language).getProperty("var");
    public static final String NEVER = Configurations.getInstance(language).getProperty("never");
    public static final String MAP = Configurations.getInstance(language).getProperty("map");
    public static final String FUTURE = Configurations.getInstance(language).getProperty("future");
    public static final String TYPEDESC = Configurations.getInstance(language).getProperty("typedesc");
    public static final String BYTE = Configurations.getInstance(language).getProperty("byte");

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

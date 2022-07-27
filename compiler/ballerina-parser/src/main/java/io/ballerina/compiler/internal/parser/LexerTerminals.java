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
    private static ParserConfigurations parserConfigurations = ParserConfigurations.getInstance();
    // Keywords
    public static final String PUBLIC = parserConfigurations.getProperty("public");
    public static final String PRIVATE = parserConfigurations.getProperty("private");
    public static final String FUNCTION = parserConfigurations.getProperty("function");
    public static final String RETURN = parserConfigurations.getProperty("return");
    public static final String RETURNS = parserConfigurations.getProperty("returns");
    public static final String EXTERNAL = parserConfigurations.getProperty("external");
    public static final String TYPE = parserConfigurations.getProperty("type");
    public static final String RECORD = parserConfigurations.getProperty("record");
    public static final String OBJECT = parserConfigurations.getProperty("object");
    public static final String REMOTE = parserConfigurations.getProperty("remote");
    public static final String ABSTRACT = parserConfigurations.getProperty("abstract");
    public static final String CLIENT = parserConfigurations.getProperty("client");
    public static final String IF = parserConfigurations.getProperty("if");
    public static final String ELSE = parserConfigurations.getProperty("else");
    public static final String WHILE = parserConfigurations.getProperty("while");
    public static final String PANIC = parserConfigurations.getProperty("panic");
    public static final String TRUE = parserConfigurations.getProperty("true");
    public static final String FALSE = parserConfigurations.getProperty("false");
    public static final String CHECK = parserConfigurations.getProperty("check");
    public static final String FAIL = parserConfigurations.getProperty("fail");
    public static final String CHECKPANIC = parserConfigurations.getProperty("checkpanic");
    public static final String CONTINUE = parserConfigurations.getProperty("continue");
    public static final String BREAK = parserConfigurations.getProperty("break");
    public static final String IMPORT = parserConfigurations.getProperty("import");
    public static final String VERSION = parserConfigurations.getProperty("version");
    public static final String AS = parserConfigurations.getProperty("as");
    public static final String ON = parserConfigurations.getProperty("on");
    public static final String RESOURCE = parserConfigurations.getProperty("resource");
    public static final String LISTENER = parserConfigurations.getProperty("listener");
    public static final String CONST = parserConfigurations.getProperty("const");
    public static final String FINAL = parserConfigurations.getProperty("final");
    public static final String TYPEOF = parserConfigurations.getProperty("typeof");
    public static final String IS = parserConfigurations.getProperty("is");
    public static final String NOT_IS = parserConfigurations.getProperty("!is");
    public static final String NULL = parserConfigurations.getProperty("null");
    public static final String LOCK = parserConfigurations.getProperty("lock");
    public static final String ANNOTATION = parserConfigurations.getProperty("annotation");
    public static final String SOURCE = parserConfigurations.getProperty("source");
    public static final String WORKER = parserConfigurations.getProperty("worker");
    public static final String PARAMETER = parserConfigurations.getProperty("parameter");
    public static final String FIELD = parserConfigurations.getProperty("field");
    public static final String ISOLATED = parserConfigurations.getProperty("isolated");
    public static final String XMLNS = parserConfigurations.getProperty("xmlns");
    public static final String FORK = parserConfigurations.getProperty("fork");
    public static final String TRAP = parserConfigurations.getProperty("trap");
    public static final String IN = parserConfigurations.getProperty("in");
    public static final String FOREACH = parserConfigurations.getProperty("foreach");
    public static final String TABLE = parserConfigurations.getProperty("table");
    public static final String KEY = parserConfigurations.getProperty("key");
    public static final String ERROR = parserConfigurations.getProperty("error");
    public static final String LET = parserConfigurations.getProperty("let");
    public static final String STREAM = parserConfigurations.getProperty("stream");
    public static final String NEW = parserConfigurations.getProperty("new");
    public static final String READONLY = parserConfigurations.getProperty("readonly");
    public static final String DISTINCT = parserConfigurations.getProperty("distinct");
    public static final String FROM = parserConfigurations.getProperty("from");
    public static final String WHERE = parserConfigurations.getProperty("where");
    public static final String SELECT = parserConfigurations.getProperty("select");
    public static final String START = parserConfigurations.getProperty("start");
    public static final String FLUSH = parserConfigurations.getProperty("flush");
    public static final String DEFAULT = parserConfigurations.getProperty("default");
    public static final String WAIT = parserConfigurations.getProperty("wait");
    public static final String DO = parserConfigurations.getProperty("do");
    public static final String TRANSACTION = parserConfigurations.getProperty("transaction");
    public static final String TRANSACTIONAL = parserConfigurations.getProperty("transactional");
    public static final String COMMIT = parserConfigurations.getProperty("commit");
    public static final String RETRY = parserConfigurations.getProperty("retry");
    public static final String ROLLBACK = parserConfigurations.getProperty("rollback");
    public static final String ENUM = parserConfigurations.getProperty("enum");
    public static final String BASE16 = parserConfigurations.getProperty("base16");
    public static final String BASE64 = parserConfigurations.getProperty("base64");
    public static final String MATCH = parserConfigurations.getProperty("match");
    public static final String CONFLICT = parserConfigurations.getProperty("conflict");
    public static final String LIMIT = parserConfigurations.getProperty("limit");
    public static final String JOIN = parserConfigurations.getProperty("join");
    public static final String OUTER = parserConfigurations.getProperty("outer");
    public static final String EQUALS = parserConfigurations.getProperty("equals");
    public static final String ORDER = parserConfigurations.getProperty("order");
    public static final String BY = parserConfigurations.getProperty("by");
    public static final String ASCENDING = parserConfigurations.getProperty("ascending");
    public static final String DESCENDING = parserConfigurations.getProperty("descending");
    public static final String CLASS = parserConfigurations.getProperty("class");
    public static final String CONFIGURABLE = parserConfigurations.getProperty("configurable");

    // For BFM only
    public static final String VARIABLE = parserConfigurations.getProperty("variable");
    public static final String MODULE = parserConfigurations.getProperty("module");

    // Types
    public static final String INT = parserConfigurations.getProperty("int");
    public static final String FLOAT = parserConfigurations.getProperty("float");
    public static final String STRING = parserConfigurations.getProperty("string");
    public static final String BOOLEAN = parserConfigurations.getProperty("boolean");
    public static final String DECIMAL = parserConfigurations.getProperty("decimal");
    public static final String XML = parserConfigurations.getProperty("xml");
    public static final String JSON = parserConfigurations.getProperty("json");
    public static final String HANDLE = parserConfigurations.getProperty("handle");
    public static final String ANY = parserConfigurations.getProperty("any");
    public static final String ANYDATA = parserConfigurations.getProperty("anydata");
    public static final String SERVICE = parserConfigurations.getProperty("service");
    public static final String VAR = parserConfigurations.getProperty("var");
    public static final String NEVER = parserConfigurations.getProperty("never");
    public static final String MAP = parserConfigurations.getProperty("map");
    public static final String FUTURE = parserConfigurations.getProperty("future");
    public static final String TYPEDESC = parserConfigurations.getProperty("typedesc");
    public static final String BYTE = parserConfigurations.getProperty("byte");

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

/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.compiler.format;

/**
 * Constants values to be used when comparing tokens in formatting.
 */
public class Tokens {
    // Operators and Separators.
    public static final String OPENING_BRACE = "{";
    public static final String CLOSING_BRACE = "}";
    public static final String SEMICOLON = ";";
    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String LESS_THAN = "<";
    public static final String GREATER_THAN = ">";
    public static final String OPENING_PARENTHESES = "(";
    public static final String CLOSING_PARENTHESES = ")";
    public static final String OPENING_BRACKET = "[";
    public static final String CLOSING_BRACKET = "]";
    public static final String QUESTION_MARK = "?";
    public static final String EQUAL_GT = "=>";
    public static final String ELLIPSIS = "...";
    public static final String EQUAL = "=";
    public static final String LEFT_ARROW = "<-";
    public static final String RIGHT_ARROW = "->";
    public static final String SEAL_OPENING = "{|";
    public static final String SEAL_CLOSING = "|}";
    public static final String DOT = ".";
    public static final String ELVIS = "?:";
    public static final String STAR = "*";
    public static final String NOT = "!";
    public static final String BIT_COMPLEMENT = "~";
    public static final String SUB = "-";
    public static final String ADD = "+";
    public static final String DIV = "/";
    public static final String AT = "@";

    // Keywords.
    public static final String VAR = "var";
    public static final String CHECK_PANIC = "checkpanic";
    public static final String START = "start";
    public static final String CHECK = "check";
    public static final String EXTERNAL = "external";
    public static final String RETURNS = "returns";
    public static final String FUNCTION = "function";
    public static final String PUBLIC = "public";
    public static final String FINAL = "final";
    public static final String IS = "is";
    public static final String PRIVATE = "private";
    public static final String CONST = "const";
    public static final String CLIENT = "client";
    public static final String LISTENER = "listener";
    public static final String ABSTRACT = "abstract";
    public static final String CHANNEL = "channel";
    public static final String OBJECT = "object";
    public static final String WAIT = "wait";
    public static final String WHERE = "where";
    public static final String WINDOW = "window";
    public static final String ANNOTATION = "annotation";
    public static final String ELSE = "else";
    public static final String RETURN = "return";
    public static final String ERROR = "error";
    public static final String FOREACH = "foreach";
    public static final String IN = "in";
    public static final String FOREVER = "forever";
    public static final String REMOTE = "remote";
    public static final String RESOURCE = "resource";
    public static final String WORKER = "worker";
    public static final String GROUP = "group";
    public static final String BY = "by";
    public static final String HAVING = "having";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String FULL = "full";
    public static final String OUTER = "outer";
    public static final String INNER = "inner";
    public static final String JOIN = "join";
    public static final String ON = "on";
    public static final String UNIDIRECTIONAL = "unidirectional";
    public static final String LIMIT = "limit";
    public static final String LOCK = "lock";
    public static final String MATCH = "match";
    public static final String ORDER = "order";
    public static final String OUTPUT = "output";
    public static final String PANIC = "panic";
    public static final String RECORD = "record";
    public static final String SELECT = "select";
    public static final String SERVICE = "service";
    public static final String AS = "as";
    public static final String FROM = "from";
    public static final String TABLE = "table";
    public static final String TRANSACTION = "transaction";
    public static final String ONRETRY = "onretry";
    public static final String WITH = "with";
    public static final String RETRIES = "retries";
    public static final String ONABORT = "onabort";
    public static final String ONCOMMIT = "oncommit";
    public static final String TRAP = "trap";
    public static final String TYPEOF = "typeof";
    public static final String UNTAINT = "untaint";
    public static final String NEW = "new";
    public static final String FLUSH = "flush";
    public static final String FORK = "fork";
    public static final String IMPORT = "import";
    public static final String VERSION = "version";
    public static final String TYPEDESC = "typedesc";
    public static final String XMLNS = "xmlns";
    public static final String XML_LITERAL_START = "xml `";
    public static final String COMMITTED = "committed";
    public static final String ABORTED = "aborted";
}

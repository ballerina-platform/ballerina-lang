/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Keyword {
    PUBLIC(LexerTerminals.PUBLIC),
    PRIVATE(LexerTerminals.PRIVATE),
    REMOTE(LexerTerminals.REMOTE),
    ABSTRACT(LexerTerminals.ABSTRACT),
    CLIENT(LexerTerminals.CLIENT),
    IMPORT(LexerTerminals.IMPORT),
    FUNCTION(LexerTerminals.FUNCTION),
    CONST(LexerTerminals.CONST),
    LISTENER(LexerTerminals.LISTENER),
    SERVICE(LexerTerminals.SERVICE),
    XMLNS(LexerTerminals.XMLNS),
    ANNOTATION(LexerTerminals.ANNOTATION),
    TYPE(LexerTerminals.TYPE),
    RECORD(LexerTerminals.RECORD),
    OBJECT(LexerTerminals.OBJECT),
    VERSION(LexerTerminals.VERSION),
    AS(LexerTerminals.AS),
    ON(LexerTerminals.ON),
    RESOURCE(LexerTerminals.RESOURCE),
    FINAL(LexerTerminals.FINAL),
    SOURCE(LexerTerminals.SOURCE),
    WORKER(LexerTerminals.WORKER),
    PARAMETER(LexerTerminals.PARAMETER),
    FIELD(LexerTerminals.FIELD),
    ISOLATED(LexerTerminals.ISOLATED),

    RETURNS(LexerTerminals.RETURNS),
    RETURN(LexerTerminals.RETURN),
    EXTERNAL(LexerTerminals.EXTERNAL),
    TRUE(LexerTerminals.TRUE),
    FALSE(LexerTerminals.FALSE),
    IF(LexerTerminals.IF),
    ELSE(LexerTerminals.ELSE),
    WHILE(LexerTerminals.WHILE),
    CHECK(LexerTerminals.CHECK),
    CHECKPANIC(LexerTerminals.CHECKPANIC),
    PANIC(LexerTerminals.PANIC),
    CONTINUE(LexerTerminals.CONTINUE),
    BREAK(LexerTerminals.BREAK),
    TYPEOF(LexerTerminals.TYPEOF),
    IS(LexerTerminals.IS),
    NULL(LexerTerminals.NULL),
    LOCK(LexerTerminals.LOCK),
    FORK(LexerTerminals.FORK),
    TRAP(LexerTerminals.TRAP),
    IN(LexerTerminals.IN),
    FOREACH(LexerTerminals.FOREACH),
    TABLE(LexerTerminals.TABLE),
    KEY(LexerTerminals.KEY),
    LET(LexerTerminals.LET),
    NEW(LexerTerminals.NEW),
    FROM(LexerTerminals.FROM),
    WHERE(LexerTerminals.WHERE),
    SELECT(LexerTerminals.SELECT),
    START(LexerTerminals.START),
    FLUSH(LexerTerminals.FLUSH),
    CONFIGURABLE(LexerTerminals.CONFIGURABLE),
    WAIT(LexerTerminals.WAIT),
    DO(LexerTerminals.DO),
    TRANSACTION(LexerTerminals.TRANSACTION),
    TRANSACTIONAL(LexerTerminals.TRANSACTIONAL),
    COMMIT(LexerTerminals.COMMIT),
    ROLLBACK(LexerTerminals.ROLLBACK),
    RETRY(LexerTerminals.RETRY),
    ENUM(LexerTerminals.ENUM),
    BASE16(LexerTerminals.BASE16),
    BASE64(LexerTerminals.BASE64),
    MATCH(LexerTerminals.MATCH),
    CONFLICT(LexerTerminals.CONFLICT),
    LIMIT(LexerTerminals.LIMIT),
    JOIN(LexerTerminals.JOIN),
    OUTER(LexerTerminals.OUTER),
    EQUALS(LexerTerminals.EQUALS),
    CLASS(LexerTerminals.CLASS),
    ORDER(LexerTerminals.ORDER),
    BY(LexerTerminals.BY),
    ASCENDING(LexerTerminals.ASCENDING),
    DESCENDING(LexerTerminals.DESCENDING),

    // Type keywords
    INT(LexerTerminals.INT),
    BYTE(LexerTerminals.BYTE),
    FLOAT(LexerTerminals.FLOAT),
    DECIMAL(LexerTerminals.DECIMAL),
    STRING(LexerTerminals.STRING),
    BOOLEAN(LexerTerminals.BOOLEAN),
    XML(LexerTerminals.XML),
    JSON(LexerTerminals.JSON),
    HANDLE(LexerTerminals.HANDLE),
    ANY(LexerTerminals.ANY),
    ANYDATA(LexerTerminals.ANYDATA),
    NEVER(LexerTerminals.NEVER),
    VAR(LexerTerminals.VAR),
    MAP(LexerTerminals.MAP),
    FUTURE(LexerTerminals.FUTURE),
    TYPEDESC(LexerTerminals.TYPEDESC),
    ERROR(LexerTerminals.ERROR),
    STREAM(LexerTerminals.STREAM),
    READONLY(LexerTerminals.READONLY),
    DISTINCT(LexerTerminals.DISTINCT),
    FAIL(LexerTerminals.FAIL),
    MODULE(LexerTerminals.MODULE),
    VARIABLE(LexerTerminals.VARIABLE),
    DEFAULT("");

    private String keyword;

    private static final Map<String, Keyword> keywordsMap = Stream.of(Keyword.values()).collect(Collectors
            .toMap(Keyword::toString, Function.identity()));

    Keyword(String keyword) {
        this.keyword = keyword;
    }

    public String toString() {
        return keyword;
    }

    public static Keyword getValue(String value) {
        return keywordsMap.getOrDefault(value, DEFAULT);
    }
}

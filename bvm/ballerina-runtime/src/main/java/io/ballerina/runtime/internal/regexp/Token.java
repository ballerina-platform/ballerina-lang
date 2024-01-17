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
package io.ballerina.runtime.internal.regexp;

/**
 * Represents a terminal node in the regular expression tree.
 *
 * @since 2201.3.0
 */
public class Token {
    public final TokenKind kind;
    protected final String value;

    Token(TokenKind kind) {
        this.kind = kind;
        this.value = kind.toString();
    }

    Token(TokenKind kind, String value) {
        this.kind = kind;
        this.value = value;
    }
}

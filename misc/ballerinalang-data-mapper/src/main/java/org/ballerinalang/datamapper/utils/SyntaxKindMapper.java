/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.datamapper.utils;

import io.ballerina.compiler.syntax.tree.SyntaxKind;

/**
 * Return TypeDescKind value for Syntax kind.
 */
public class SyntaxKindMapper {
    // To solve the rest Fields
    //TODO add other syntaxKinds
    public static String mapSyntaxKind(SyntaxKind kind) {
        switch (kind.name()) {
            case "NUMERIC_LITERAL":
                return SyntaxKind.INT_KEYWORD.stringValue();
            case "STRING_LITERAL":
                return SyntaxKind.STRING_KEYWORD.stringValue();
            default:
                return SyntaxKind.ANY_KEYWORD.name();
        }
    }
}

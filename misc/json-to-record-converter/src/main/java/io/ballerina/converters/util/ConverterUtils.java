/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.converters.util;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.Token;

import com.google.gson.JsonPrimitive;

import java.util.Optional;

/**
 * Util methods for JSON to record direct converter.
 *
 * @since 2.0.0
 */
public class ConverterUtils {

    public static String escapeIdentifier(String identifier) {
        if (identifier.matches("\\b[0-9]*\\b")) {
            return "'" + identifier;
        } else if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b")
                || SyntaxInfo.keywords().stream().anyMatch(identifier::equals)) {

            identifier = identifier.replaceAll(Constants.ESCAPE_PATTERN, "\\\\$1");
            if (identifier.endsWith("?")) {
                if (identifier.charAt(identifier.length() - 2) == '\\') {
                    StringBuilder stringBuilder = new StringBuilder(identifier);
                    stringBuilder.deleteCharAt(identifier.length() - 2);
                    identifier = stringBuilder.toString();
                }
                if (SyntaxInfo.keywords().stream().anyMatch(Optional.ofNullable(identifier)
                        .filter(sStr -> sStr.length() != 0)
                        .map(sStr -> sStr.substring(0, sStr.length() - 1))
                        .orElse(identifier)::equals)) {
                    identifier = "'" + identifier;
                } else {
                    return identifier;
                }
            } else {
                identifier = "'" + identifier;
            }
        }
        return identifier;
    }

    public static Token getPrimitiveTypeName(JsonPrimitive value) {
        if (value.isString()) {
            return AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);
        } else if (value.isBoolean()) {
            return AbstractNodeFactory.createToken(SyntaxKind.BOOLEAN_KEYWORD);
        } else if (value.isNumber()) {
            String strValue = value.getAsNumber().toString();
            if (strValue.contains(".")) {
                return AbstractNodeFactory.createToken(SyntaxKind.DECIMAL_KEYWORD);
            } else {
                return AbstractNodeFactory.createToken(SyntaxKind.INT_KEYWORD);
            }
        }
        return AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
    }
}

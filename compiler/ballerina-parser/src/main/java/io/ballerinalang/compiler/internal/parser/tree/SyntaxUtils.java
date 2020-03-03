/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.Token;

public class SyntaxUtils {

    private SyntaxUtils() {
    }

    public static SyntaxKind keywordKind(String value) {
        switch (value) {
            case "import":
                return SyntaxKind.IMPORT_KEYWORD;
            case "public":
                return SyntaxKind.PUBLIC_KEYWORD;
            case "function":
                return SyntaxKind.FUNCTION_KEYWORD;
            case "returns":
                return SyntaxKind.RETURNS_KEYWORD;
            case "return":
                return SyntaxKind.RETURN_KEYWORD;
            default:
                return SyntaxKind.NONE;
        }
    }


    public static boolean isToken(Node blNode) {
        // TODO find a syntaxKind based approach to check
        return blNode instanceof Token;
    }
}

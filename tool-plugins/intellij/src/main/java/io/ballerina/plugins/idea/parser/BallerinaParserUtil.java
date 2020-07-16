/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import io.ballerina.plugins.idea.psi.BallerinaTypes;

/**
 * Parser util class which will be used to handle custom rules.
 */
public class BallerinaParserUtil extends GeneratedParserUtilBase {

    public static boolean isGroupType(PsiBuilder builder, int level) {
        IElementType lookAhead = builder.lookAhead(1);
        if (lookAhead == BallerinaTypes.ADD || lookAhead == BallerinaTypes.SUB) {
            return false;
        }
        return false;
    }

    private static boolean isWhiteSpaceOrComment(IElementType rawLookup) {
        return rawLookup == TokenType.WHITE_SPACE || rawLookup == BallerinaTypes.LINE_COMMENT;
    }

    // Need to differentiate between nullable types and ternary expressions.
    public static boolean nullableTypePredicate(PsiBuilder builder, int level) {
        int steps = -1;
        IElementType prev1Element;

        do {
            prev1Element = builder.rawLookup(steps--);
            if (prev1Element == null || isWhiteSpaceOrComment(prev1Element)) {
                continue;
            }
            IElementType prev2Element;
            do {
                prev2Element = builder.rawLookup(steps--);
                if (prev2Element == null || isWhiteSpaceOrComment(prev2Element)) {
                    continue;
                }
                //Eg: x is string ? 1 : 2;
                if (prev2Element == BallerinaTypes.IS) {
                    return false;
                }
                IElementType prev3Element;
                do {
                    prev3Element = builder.rawLookup(steps--);
                    if (prev3Element == null || isWhiteSpaceOrComment(prev3Element)) {
                        continue;
                    }
                    // Eg: x is () ? 1 : 2;
                    if (prev3Element == BallerinaTypes.IS) {
                        return false;
                    }
                    IElementType prev4Element;
                    do {
                        prev4Element = builder.rawLookup(steps--);
                        if (prev4Element == null || isWhiteSpaceOrComment(prev4Element)) {
                            continue;
                        }
                        // Eg: x is http:Error ? 1 : 2;
                        if (prev4Element == BallerinaTypes.IS) {
                            return false;
                        }
                    } while ((prev4Element != null && isWhiteSpaceOrComment(prev4Element)));
                } while ((prev3Element != null && isWhiteSpaceOrComment(prev3Element)));
            } while ((prev2Element != null && isWhiteSpaceOrComment(prev2Element)));
        } while ((prev1Element != null && isWhiteSpaceOrComment(prev1Element)));
        return true;
    }
}

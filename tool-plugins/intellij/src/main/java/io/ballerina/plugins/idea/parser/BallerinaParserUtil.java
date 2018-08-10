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

import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import io.ballerina.plugins.idea.psi.BallerinaTypes;

/**
 * Parser util class which will be used to handle custom rules.
 */
public class BallerinaParserUtil extends GeneratedParserUtilBase {

    public static boolean isPackageExpected(PsiBuilder builder, int level) {
        // Todo - Refactor code to methods
        IElementType next1Element = builder.lookAhead(1);
        if (next1Element == null || (next1Element != BallerinaTypes.COLON
                && next1Element != BallerinaTypes.RIGHT_PARENTHESIS)) {
            return true;
        }
        IElementType next2Element = builder.lookAhead(2);
        if (next2Element == null || (next2Element != BallerinaTypes.IDENTIFIER
                && next2Element != BallerinaTypes.RIGHT_BRACE)) {
            return true;
        }
        IElementType next3Element = builder.lookAhead(3);
        // The next token can be one of the following tokens. Right brace is to check in record key literals.
        // Comma is used for record literals in function invocations - test(a,{b:c, d:e})
        // Left bracket is used to identify array elements - {name:"Ballerina", address:args[0]};
        //
        if (next3Element != null && (next3Element == BallerinaTypes.SEMICOLON
                || next3Element == BallerinaTypes.COLON || next3Element == BallerinaTypes.RIGHT_PARENTHESIS
                || next3Element == BallerinaTypes.RIGHT_BRACE || next3Element == BallerinaTypes.COMMA
                || next3Element == BallerinaTypes.LEFT_BRACKET || next3Element == BallerinaTypes.LEFT_PARENTHESIS
                || next3Element == BallerinaTypes.EQUAL || next3Element == BallerinaTypes.NOT_EQUAL
                || next3Element == BallerinaTypes.GT || next3Element == BallerinaTypes.LT
                || next3Element == BallerinaTypes.GT_EQUAL || next3Element == BallerinaTypes.LT_EQUAL
                || next3Element == BallerinaTypes.DOT || next3Element == BallerinaTypes.ADD
                || next3Element == BallerinaTypes.SUB || next3Element == BallerinaTypes.DIV
                || next3Element == BallerinaTypes.MUL
        )) {
            // We need to look behind few steps to identify the last token. If this token is not "?" only we
            // identify that the package is required.
            int steps = -1;
            IElementType rawLookup;
            do {
                rawLookup = builder.rawLookup(steps--);
                if (isWhiteSpaceOrComment(rawLookup)) {
                    continue;
                }
                // Left brace is to check in record key literals. Comma and other token is checked for situations like -
                // {name:"Child", parent:parent}
                if (rawLookup == BallerinaTypes.QUESTION_MARK || rawLookup == BallerinaTypes.LEFT_BRACE
                        || rawLookup == BallerinaTypes.LINE_COMMENT || (rawLookup == BallerinaTypes.COMMA
                        && (next3Element == BallerinaTypes.RIGHT_BRACE || next3Element == BallerinaTypes.COMMA
                        || next3Element == BallerinaTypes.DOT)
                )) {
                    // Note - Another raw lookup is added for situations like below. Second record key value
                    // pair does not get identified correctly otherwise.
                    // {sqlType:sql:Type.INTEGER, value:xmlDataArray};
                    IElementType rawLookup2;
                    do {
                        rawLookup2 = builder.rawLookup(--steps);
                        if (isWhiteSpaceOrComment(rawLookup2)) {
                            continue;
                        }
                        // Identifier example - endpoint ServiceEndpoint backendEP {port:getBackendPort()};
                        // Quoted literal string example - {address:{"city":"Colombo", "country":"SriLanka"}, info:info}

                        // Decimal literal example - string a = x == 1 ? s : i;
                        if (rawLookup2 != BallerinaTypes.ASSIGN /*&& rawLookup2 != BallerinaTypes.COLON*/
                                && rawLookup2 != BallerinaTypes.DOT && rawLookup2 != BallerinaTypes.IDENTIFIER
                                && rawLookup2 != BallerinaTypes.RIGHT_BRACE
                                && rawLookup2 != BallerinaTypes.QUOTED_STRING_LITERAL
                                && rawLookup2 != BallerinaTypes.DECIMAL_INTEGER_LITERAL
                                && rawLookup2 != BallerinaTypes.BOOLEAN_LITERAL
                                // Example for below condition - {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                // datasourceProperties:propertiesMap}
                                && !(rawLookup == BallerinaTypes.COMMA && rawLookup2 == BallerinaTypes.COLON)
                                // Example for below condition - worker w {a:b();}
                                && !(rawLookup == BallerinaTypes.LEFT_BRACE && rawLookup2 == BallerinaTypes.COMMA)
                                /*|| (rawLookup == BallerinaTypes.LEFT_BRACE && rawLookup2 == BallerinaTypes
                                .IDENTIFIER)*/
                                // @Args{value : stringvalue}
                                && !(rawLookup == BallerinaTypes.LEFT_BRACE && rawLookup2 == BallerinaTypes.AT)
                                && !(rawLookup == BallerinaTypes.LEFT_BRACE && rawLookup2 == BallerinaTypes.COLON)
                                // Connection conn = new({initialContextFactory:config.initialContextFactory});
                                && !(rawLookup == BallerinaTypes.LEFT_BRACE && rawLookup2 == BallerinaTypes.NEW)
                                // {message:"Notification failed for topic [" + topic + "]",  cause:httpConnectorError }
                                && !(rawLookup == BallerinaTypes.COMMA && rawLookup2 == BallerinaTypes.ADD)
                                && !(rawLookup == BallerinaTypes.QUESTION_MARK
                                && rawLookup2 == BallerinaTypes.RIGHT_PARENTHESIS)
                                && !(rawLookup == BallerinaTypes.LEFT_BRACE && rawLookup2 == BallerinaTypes.BIND)
                                && !(rawLookup == BallerinaTypes.LINE_COMMENT &&
                                rawLookup2 == BallerinaTypes.LINE_COMMENT)
                                && !(rawLookup == BallerinaTypes.LINE_COMMENT &&
                                rawLookup2 == BallerinaTypes.LEFT_BRACE)
                                && !(rawLookup == BallerinaTypes.LINE_COMMENT && rawLookup2 == BallerinaTypes.COMMA)
                                && !(rawLookup == BallerinaTypes.LEFT_BRACE
                                && rawLookup2 == BallerinaTypes.RIGHT_BRACKET)
                                && !(rawLookup == BallerinaTypes.LEFT_BRACE && rawLookup2 == BallerinaTypes.RETURN)
                                ) {
                            return true;
                        } else {
                            LighterASTNode latestDoneMarker = builder.getLatestDoneMarker();
                            if (rawLookup == BallerinaTypes.COMMA && rawLookup2 == BallerinaTypes.COLON) {
                                if (latestDoneMarker != null) {
                                    IElementType tokenType = latestDoneMarker.getTokenType();
                                    if (tokenType == BallerinaTypes.SIMPLE_TYPE_NAME) {
                                        // sql:ConnectionProperties properties3 = {dataSourceClassName:"org.hsqldb.jdbc
                                        // .JDBCDataSource", datasourceProperties:propertiesMap};
                                        return true;
                                    } else if (tokenType == BallerinaTypes.VARIABLE_REFERENCE_EXPRESSION) {
                                        // return (variable:a, variable:b, variable:c, variable:d);
                                        return true;
                                    } else if (tokenType == BallerinaTypes.INVOCATION_ARG) {
                                        // return (variable:a, variable:b, variable:c, variable:d);
                                        return true;
                                    }
                                }
                            } else if (rawLookup == BallerinaTypes.COMMA && rawLookup2 == BallerinaTypes.DOT) {
                                // EmployeeSalary s = {id:e.id, salary:e.salary};
                                if (latestDoneMarker != null
                                        && latestDoneMarker.getTokenType() == BallerinaTypes.RECORD_KEY_VALUE) {
                                    return false;
                                }
                                // return (variable:^"person 1".^"first name", variable2:^"person 2".^"current age2");
                                return true;
                            } else if (rawLookup == BallerinaTypes.COMMA && rawLookup2 == BallerinaTypes.ADD) {
                                //
                                if (latestDoneMarker != null) {
                                    IElementType tokenType = latestDoneMarker.getTokenType();
                                    if (tokenType == BallerinaTypes.INVOCATION_ARG) {
                                        return true;
                                    } else if (tokenType == BallerinaTypes.RECORD_KEY_VALUE) {
                                        return false;
                                    }
                                }
                                // return (variable:^"person 1".^"first name", variable2:^"person 2".^"current age2");
                                return true;
                            } else if (rawLookup == BallerinaTypes.COMMA
                                    && rawLookup2 == BallerinaTypes.QUOTED_STRING_LITERAL) {
                                if (latestDoneMarker != null) {
                                    IElementType tokenType = latestDoneMarker.getTokenType();
                                    if (tokenType == BallerinaTypes.RECORD_KEY_VALUE) {
                                        return false;
                                    }
                                    return true;
                                }
                            } else if (rawLookup == BallerinaTypes.LEFT_BRACE
                                    && rawLookup2 == BallerinaTypes.IDENTIFIER) {
                                if (latestDoneMarker != null) {
                                    IElementType tokenType = latestDoneMarker.getTokenType();
                                    // io:println(jwtToken); as the first statement in a function.
                                    // runtime:sleepCurrentWorker(20); in the first statement as the second worker
                                    if (tokenType == BallerinaTypes.CALLABLE_UNIT_SIGNATURE
                                            || tokenType == BallerinaTypes.OBJECT_CALLABLE_UNIT_SIGNATURE
                                            || tokenType == BallerinaTypes.WORKER_DEFINITION
                                            || tokenType == BallerinaTypes.UNARY_EXPRESSION
                                            || tokenType == BallerinaTypes.VARIABLE_REFERENCE_EXPRESSION
                                            || tokenType == BallerinaTypes.SIMPLE_TYPE_NAME
                                            ) {
                                        return true;
                                    }
                                }
                            } else if (rawLookup == BallerinaTypes.LINE_COMMENT &&
                                    rawLookup2 == BallerinaTypes.LINE_COMMENT) {
                                if (next1Element == BallerinaTypes.COLON && next3Element == BallerinaTypes.COLON) {
                                    return false;
                                }
                                if (latestDoneMarker != null) {
                                    // Todo - Add more conditions?
                                    return true;
                                }
                            } else if (rawLookup == BallerinaTypes.LINE_COMMENT &&
                                    rawLookup2 == BallerinaTypes.RIGHT_BRACE) {
                                if (next1Element == BallerinaTypes.COLON && next2Element == BallerinaTypes.IDENTIFIER) {
                                    return true;
                                }
                            } else if (rawLookup == BallerinaTypes.LINE_COMMENT &&
                                    rawLookup2 == BallerinaTypes.LEFT_BRACE) {
                                if (next1Element == BallerinaTypes.COLON && next2Element == BallerinaTypes.IDENTIFIER) {
                                    return true;
                                }
                            } else if (rawLookup == BallerinaTypes.LINE_COMMENT && rawLookup2 == BallerinaTypes.COMMA) {
                                if (next1Element == BallerinaTypes.COLON && next2Element == BallerinaTypes.IDENTIFIER) {
                                    IElementType tokenType = latestDoneMarker.getTokenType();
                                    if (tokenType == BallerinaTypes.RECORD_KEY_VALUE) {
                                        return false;
                                    }
                                    return false;
                                }
                            }
                            return false;
                        }
                    } while (rawLookup2 != null && isWhiteSpaceOrComment(rawLookup2));
                } else {
                    LighterASTNode latestDoneMarker = builder.getLatestDoneMarker();
                    // EmployeeSalary s = {id:e.id, salary:e.salary};
                    if (latestDoneMarker != null
                            && latestDoneMarker.getTokenType() == BallerinaTypes.RECORD_KEY_VALUE) {
                        return false;
                    }
                    return true;
                }
            } while (rawLookup != null && isWhiteSpaceOrComment(rawLookup));
        }
        return true;
    }

    public static boolean isVarDef(PsiBuilder builder, int level) {
        IElementType lookAhead = builder.lookAhead(0);
        if (lookAhead == BallerinaTypes.LEFT_PARENTHESIS) {
            return false;
        }
        return false;
    }

    public static boolean isGroupType(PsiBuilder builder, int level) {
        IElementType lookAhead = builder.lookAhead(1);
        if (lookAhead == BallerinaTypes.ADD || lookAhead == BallerinaTypes.SUB) {
            return false;
        }
        return false;
    }

    public static boolean isNotARestParameter(PsiBuilder builder, int level) {
        IElementType lookAhead = builder.lookAhead(1);
        if (lookAhead == BallerinaTypes.ELLIPSIS) {
            return false;
        }
        if (lookAhead == BallerinaTypes.LEFT_BRACKET) {
            IElementType lookAhead3 = builder.lookAhead(3);
            if (lookAhead3 == BallerinaTypes.ELLIPSIS) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWhiteSpaceOrComment(IElementType rawLookup) {
        return rawLookup == TokenType.WHITE_SPACE || rawLookup == BallerinaTypes.COMMENT;
    }

    public static boolean isNotAResourceDefinition(PsiBuilder builder, int level) {
        if (builder.getTokenType() != BallerinaTypes.IDENTIFIER) {
            return false;
        }
        IElementType next1Element = builder.lookAhead(1);
        if (next1Element != null && next1Element.toString().equals("(")) {
            return true;
        }
        return false;
    }

    public static boolean isNotInStreams(PsiBuilder builder, int level) {
        IElementType next1Element = builder.lookAhead(2);
        if (next1Element != null && next1Element != BallerinaTypes.WHERE) {
            return true;
        }
        return false;
    }

    public static boolean restDescriptorPredicate(PsiBuilder builder, int level) {
        IElementType next1Element = builder.lookAhead(-1);
        return next1Element != TokenType.WHITE_SPACE;
    }

    public static boolean shiftExprPredicate(PsiBuilder builder, int level) {
        IElementType next1Element = builder.lookAhead(-1);
        return next1Element != TokenType.WHITE_SPACE;
    }
}

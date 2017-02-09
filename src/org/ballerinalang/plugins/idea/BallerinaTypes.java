/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea;

import org.antlr.jetbrains.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.jetbrains.adaptor.lexer.RuleIElementType;
import org.antlr.jetbrains.adaptor.lexer.TokenIElementType;
import org.ballerinalang.plugins.idea.grammar.BallerinaLexer;
import org.ballerinalang.plugins.idea.grammar.BallerinaParser;

import java.util.List;

public class BallerinaTypes {

    public static List<RuleIElementType> ruleIElementTypes =
            PSIElementTypeFactory.getRuleIElementTypes(BallerinaLanguage.INSTANCE);

    public static List<TokenIElementType> tokenIElementTypes =
            PSIElementTypeFactory.getTokenIElementTypes(BallerinaLanguage.INSTANCE);

    public static TokenIElementType COMMENT_STATEMENT = tokenIElementTypes.get(BallerinaLexer.LINE_COMMENT);

    public static RuleIElementType STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_statement);

    public static RuleIElementType FUNCTION_BODY = ruleIElementTypes.get(BallerinaParser.RULE_functionBody);
    public static RuleIElementType CONNECTOR_BODY = ruleIElementTypes.get(BallerinaParser.RULE_connectorBody);
    public static RuleIElementType SERVICE_BODY = ruleIElementTypes.get(BallerinaParser.RULE_serviceBody);
    public static RuleIElementType STRUCT_BODY = ruleIElementTypes.get(BallerinaParser.RULE_structDefinitionBody);

    public static RuleIElementType FUNCTION_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_functionDefinition);
    public static RuleIElementType CONNECTOR_DEFINITION =
            ruleIElementTypes.get(BallerinaParser.RULE_connectorDefinition);
    public static RuleIElementType SERVICE_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_serviceDefinition);
    public static RuleIElementType STRUCT_DEFINITION = ruleIElementTypes.get(BallerinaParser.RULE_structDefinition);

    public static RuleIElementType IF_ELSE_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_ifElseStatement);
    public static RuleIElementType ITERATE_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_iterateStatement);
    public static RuleIElementType WHILE_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_whileStatement);
    public static RuleIElementType JOIN_CLAUS = ruleIElementTypes.get(BallerinaParser.RULE_joinClause);
    public static RuleIElementType TIMEOUT_CLAUSE = ruleIElementTypes.get(BallerinaParser.RULE_timeoutClause);
    public static RuleIElementType TRY_CATCH_STATEMENT = ruleIElementTypes.get(BallerinaParser.RULE_tryCatchStatement);
    public static RuleIElementType TYPE_CONVERTER_BODY = ruleIElementTypes.get(BallerinaParser.RULE_typeConvertorBody);
    public static RuleIElementType WORKER_DECLARATION = ruleIElementTypes.get(BallerinaParser.RULE_workerDeclaration);


}

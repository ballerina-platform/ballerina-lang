/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Consists of the match statement resolving utilities.
 * 
 * @since 0.985.0
 */
public class MatchStatementResolverUtil {

    /**
     * Get the variable value destruture pattern.
     *
     * @param ctx               LS Service Operation context
     * @return {@link String}   Generated Pattern clause
     */
    public static String getVariableValueDestructurePattern(LSContext ctx) {
        boolean snippetSupport = ctx.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem()
                .getSnippetSupport();
        String valueHolder;
        String bodyPlaceHolder;
        if (snippetSupport) {
            valueHolder = "${1:value}";
            bodyPlaceHolder = "${2}";
        } else {
            valueHolder = "value";
            bodyPlaceHolder = " ";
        }
        return valueHolder + " => " + UtilSymbolKeys.OPEN_BRACE_KEY + bodyPlaceHolder + UtilSymbolKeys.CLOSE_BRACE_KEY;
    }

    /**
     * Get the tuple destructured pattern.
     * 
     * @param bTupleType        Tuple type
     * @param varNames          Variable names list currently added
     * @param context           LS Operation context
     * @return {@link String}   Generated pattern clause
     */
    public static String getTupleDestructured(BTupleType bTupleType, List<String> varNames, LSContext context) {
        List<BType> typeList = bTupleType.getTupleTypes();
        List<String> destructedVars = new ArrayList<>();
        for (BType bType : typeList) {
            String varName;
            if (bType instanceof BTupleType) {
                varName = getTupleDestructured((BTupleType) bType, varNames, context);
            } else {
                String typeName = CommonUtil.getBTypeName(bType, context);
                varName = String.valueOf(typeName.charAt(0));
                if (varNames.contains(varName)) {
                    varName = varName + (int) (Math.random() * 20 + 1);
                }
            }
            varNames.add(varName);
            destructedVars.add(varName);
        }

        return UtilSymbolKeys.OPEN_PARENTHESES_KEY
                + String.join(", ", destructedVars)
                + UtilSymbolKeys.CLOSE_PARENTHESES_KEY;
    }

    /**
     * Get the Structured fixed value match.
     *
     * @param bType             Structured type, either BRecordType or a BTupleType
     * @return {@link String}   Generate pattern clause
     */
    public static String getStructuredFixedValueMatch(BType bType) {
        StringBuilder fixedValPattern = new StringBuilder();
        if (bType instanceof BTupleType) {
            List<BType> tupleTypes = ((BTupleType) bType).getTupleTypes();
            List<String> defaultValues = tupleTypes.stream()
                    .map(MatchStatementResolverUtil::getStructuredFixedValueMatch)
                    .collect(Collectors.toList());
            fixedValPattern
                    .append(UtilSymbolKeys.OPEN_PARENTHESES_KEY)
                    .append(String.join(", ", defaultValues))
                    .append(UtilSymbolKeys.CLOSE_PARENTHESES_KEY);
        } else if (bType instanceof BRecordType) {
            List<BField> fields = ((BRecordType) bType).fields;
            List<String> defaultValues = fields.stream()
                    .map(field -> field.getName().getValue() + ":" + getStructuredFixedValueMatch(field.getType()))
                    .collect(Collectors.toList());
            fixedValPattern
                    .append(UtilSymbolKeys.OPEN_BRACE_KEY)
                    .append(String.join(", ", defaultValues))
                    .append(UtilSymbolKeys.CLOSE_BRACE_KEY);
        } else {
            fixedValPattern.append(CommonUtil.getDefaultValueForType(bType));
        }

        return fixedValPattern.toString();
    }

    /**
     * Generate the match pattern clause for the given value.
     *
     * @param matchValue        Match pattern value
     * @param ctx               LS Service operation context
     * @return {@link String}   Generated clause
     */
    public static String generateMatchPattern(String matchValue, LSContext ctx) {
        boolean snippetSupport = ctx.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem()
                .getSnippetSupport();
        String valueHolder;
        String bodyPlaceHolder;
        if (snippetSupport) {
            valueHolder = "${1:" + matchValue + "}";
            bodyPlaceHolder = "${2}";
        } else {
            valueHolder = matchValue;
            bodyPlaceHolder = " ";
        }
        return valueHolder + " => " + UtilSymbolKeys.OPEN_BRACE_KEY + bodyPlaceHolder + UtilSymbolKeys.CLOSE_BRACE_KEY;
    }
}

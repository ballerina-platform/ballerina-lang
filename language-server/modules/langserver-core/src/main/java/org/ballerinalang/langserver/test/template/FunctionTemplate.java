/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.test.template;

import org.ballerinalang.langserver.common.utils.CommonUtil.FunctionGenerator;
import org.ballerinalang.langserver.test.TestGeneratorException;
import org.ballerinalang.langserver.test.template.io.FileTemplate;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.util.StringJoiner;
import java.util.stream.IntStream;

import static org.ballerinalang.langserver.test.ValueSpaceGenerator.createTemplateArray;
import static org.ballerinalang.langserver.test.ValueSpaceGenerator.getValueSpaceByNode;

/**
 * To represent a function template.
 */
public class FunctionTemplate extends AbstractTestTemplate {
    private static final int VALUE_SPACE_LENGTH = 4;
    private final String testFunctionName;
    private final String functionInvocation;
    private final String functionInvocationField;
    private final String dataProviderReturnType;
    private final String dataProviderReturnValue;
    private final String testFunctionParams;
    private final boolean isVoidFunction;

    public FunctionTemplate(BLangFunction function) {
        StringJoiner paramsStr = new StringJoiner(", ");
        StringJoiner paramsInvokeStr = new StringJoiner(", ");
        StringJoiner paramsTypeStr = new StringJoiner(", ");
        String[][] valueSpace = new String[VALUE_SPACE_LENGTH][function.requiredParams.size() + 1];

        // Populate target function's parameters
        for (int i = 0; i < function.requiredParams.size(); i++) {
            BLangVariable variable = function.requiredParams.get(i);
            String paramType = FunctionGenerator.getFuncReturnSignature(variable.typeNode);
            String paramName = variable.name.value;
            paramsStr.add(paramType + " " + paramName);
            paramsInvokeStr.add(paramName);
            paramsTypeStr.add(paramType);

            String[] pValueSpace = getValueSpaceByNode(variable.typeNode, createTemplateArray(VALUE_SPACE_LENGTH));
            for (int j = 0; j < pValueSpace.length; j++) {
                // Need to apply transpose of `pValueSpace`
                // i.e. valueSpace = (pValueSpace)^T
                valueSpace[j][i] = pValueSpace[j];
            }
        }

        // Populate target function's return type
        String variableType = FunctionGenerator.getFuncReturnSignature(function.returnTypeNode);
        paramsStr.add(variableType + " expected");
        paramsTypeStr.add(variableType);
        String[] rtValSpace = getValueSpaceByNode(function.returnTypeNode, createTemplateArray(VALUE_SPACE_LENGTH));

        IntStream.range(0, rtValSpace.length).forEach(index -> {
            valueSpace[index][function.requiredParams.size()] = rtValSpace[index];
        });

        // Prepare data provider's return value
        StringJoiner vSpace = new StringJoiner("), (", "(", ")");
        IntStream.range(0, valueSpace.length).parallel().forEach(index -> {
            vSpace.add(String.join(", ", valueSpace[index]));
        });

        String functionName = function.name.value;
        this.testFunctionName = "test" + upperCaseFirstLetter(functionName);
        this.isVoidFunction = (function.returnTypeNode == null || function.returnTypeNode.type instanceof BNilType);
        this.functionInvocation = functionName + "(" + paramsInvokeStr.toString() + ")";
        this.functionInvocationField = variableType + " actual = " + functionInvocation + ";";
        this.testFunctionParams = paramsStr.toString();
        this.dataProviderReturnType = "(" + paramsTypeStr.toString() + ")[]";
        this.dataProviderReturnValue = "[" + vSpace + "]";
    }

    /**
     * Renders content into this file template.
     *
     * @param rootFileTemplate root {@link FileTemplate}
     * @throws TestGeneratorException when template population process fails
     */
    @Override
    public void render(FileTemplate rootFileTemplate) throws TestGeneratorException {
        String filename = (isVoidFunction) ? "voidFunction.bal" : "returnTypedFunction.bal";
        FileTemplate template = new FileTemplate(filename);
        template.put("testFunctionName", testFunctionName);
        template.put("actual", (isVoidFunction) ? functionInvocation + ";" : functionInvocationField);
        if (!isVoidFunction) {
            template.put("dataProviderReturnType", dataProviderReturnType);
            template.put("dataProviderReturnValue", dataProviderReturnValue);
            template.put("testFunctionParams", testFunctionParams);
        }

        //Append to root template
        rootFileTemplate.append(RootTemplate.PLACEHOLDER_ATTR_CONTENT, template.getRenderedContent());
    }
}

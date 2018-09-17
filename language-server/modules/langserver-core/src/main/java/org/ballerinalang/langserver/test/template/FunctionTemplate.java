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

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.langserver.test.TestGeneratorUtil.upperCaseFirstLetter;

/**
 * To represent a function template.
 */
public class FunctionTemplate implements BallerinaTestTemplate {
    private final String testFunctionName;
    private final String functionInvocation;
    private final String functionInvocationField;
    private final String expectedValueField;
    private final boolean isVoid;

    public FunctionTemplate(BLangFunction function) {
        String name = function.name.value;
        testFunctionName = "test" + upperCaseFirstLetter(name);

        // Generate function invocation string
        String variableType = FunctionGenerator.getFuncReturnSignature(function.returnTypeNode);
        List<String> params = new ArrayList<>();
        for (BLangVariable variable : function.requiredParams) {
            params.add(FunctionGenerator.getFuncReturnDefaultStatement(variable, "{%1}"));
        }
        String paramsStr = String.join(", ", params);
        this.isVoid = (function.returnTypeNode == null || function.returnTypeNode.type instanceof BNilType);
        this.functionInvocation = name + "(" + paramsStr + ")";
        this.functionInvocationField = variableType + " actual = " + functionInvocation + ";";
        String expectedValue = FunctionGenerator.getFuncReturnDefaultStatement(function.returnTypeNode, "{%1}");
        this.expectedValueField = variableType + " expected = " + expectedValue + ";";
    }

    /**
     * Renders content into this file template.
     *
     * @param rootFileTemplate root {@link FileTemplate}
     * @throws TestGeneratorException when template population process fails
     */
    @Override
    public void render(FileTemplate rootFileTemplate) throws TestGeneratorException {
        String filename = (isVoid) ? "voidFunction.bal" : "returnTypedFunction.bal";
        FileTemplate template = new FileTemplate(filename);
        template.put("testFunctionName", testFunctionName);
        template.put("actual", (isVoid) ? functionInvocation + ";" : functionInvocationField);
        template.put("expected", expectedValueField);

        //Append to root template
        rootFileTemplate.append(RootTemplate.PLACEHOLDER_ATTR_CONTENT, template.getRenderedContent());
    }
}

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
package org.ballerinalang.langserver.command.testgen.template.type;

import org.ballerinalang.langserver.command.testgen.TestGenerator.TestFunctionGenerator;
import org.ballerinalang.langserver.command.testgen.TestGeneratorException;
import org.ballerinalang.langserver.command.testgen.renderer.RendererOutput;
import org.ballerinalang.langserver.command.testgen.renderer.TemplateBasedRendererOutput;
import org.ballerinalang.langserver.command.testgen.template.AbstractTestTemplate;
import org.ballerinalang.langserver.command.testgen.template.PlaceHolder;
import org.ballerinalang.langserver.commons.LSContext;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.List;
import java.util.function.BiConsumer;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

/**
 * To represent a function template.
 *
 * @since 0.985.0
 */
public class FunctionTemplate extends AbstractTestTemplate {
    private final String testFunctionName;
    private final List<String> functionInvocations;
    private final String dataProviderBasedFunctionInvocation;
    private final String dataProviderReturnType;
    private final String dataProviderReturnValue;
    private final String testFunctionParams;
    private final boolean hasReturnType;
    private final boolean hasParams;

    public FunctionTemplate(BLangPackage builtTestFile, BLangFunction function,
                            BiConsumer<Integer, Integer> focusLineAcceptor, TestFunctionGenerator generator,
                            LSContext context) {
        super(builtTestFile, focusLineAcceptor, context);
        String functionName = function.name.value;
        this.testFunctionName = getSafeName("test" + upperCaseFirstLetter(functionName));
        this.hasReturnType = (function.returnTypeNode != null && !(function.returnTypeNode.type instanceof BNilType));
        this.hasParams = generator.getParamsCount() > 0;
        this.functionInvocations = generator.getTargetFuncInvocations();
        if (hasReturnType) {
            this.dataProviderBasedFunctionInvocation =
                    generator.getTargetFuncReturnType() + " actual = " + generator.getTargetFuncInvocation() + ";";
        } else {
            this.dataProviderBasedFunctionInvocation = generator.getTargetFuncInvocation() + ";";
        }
        this.testFunctionParams = generator.getTestFuncParams();
        this.dataProviderReturnType = generator.getDataProviderReturnType();
        this.dataProviderReturnValue = generator.getDataProviderReturnValue();
    }

    /**
     * Renders content into this file template.
     *
     * @param rendererOutput root {@link TemplateBasedRendererOutput}
     * @throws TestGeneratorException when template population process fails
     */
    @Override
    public void render(RendererOutput rendererOutput) throws TestGeneratorException {
        String filename = (hasReturnType) ? "returnTypedFunction.bal" :
                (hasParams) ? "voidFunction.bal" : "voidFunctionNoParams.bal";
        RendererOutput functionOutput = new TemplateBasedRendererOutput(filename);
        functionOutput.put(PlaceHolder.OTHER.get("testFunctionName"), testFunctionName);
        functionOutput.put(PlaceHolder.OTHER.get("dataProviderReturnType"), dataProviderReturnType);
        functionOutput.put(PlaceHolder.OTHER.get("dataProviderReturnValue"), dataProviderReturnValue);
        functionOutput.put(PlaceHolder.OTHER.get("testFunctionParams"), testFunctionParams);

        String functionInvocationLine = dataProviderBasedFunctionInvocation;
        if (!hasReturnType && !hasParams) {
            functionInvocationLine = "    " + functionInvocations.get(0) + ";";
        }
        functionOutput.put(PlaceHolder.OTHER.get("actual"), functionInvocationLine);

        //Append to root template
        rendererOutput.setFocusLineAcceptor(testFunctionName, focusLineAcceptor);
        rendererOutput.append(PlaceHolder.CONTENT, LINE_SEPARATOR + functionOutput.getRenderedContent());
    }
}

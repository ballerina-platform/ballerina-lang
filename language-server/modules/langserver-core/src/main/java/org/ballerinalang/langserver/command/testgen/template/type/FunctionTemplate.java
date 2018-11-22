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
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.List;
import java.util.StringJoiner;
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
    private final boolean isVoidFunction;

    public FunctionTemplate(BiConsumer<String, String> importsConsumer,
                            BLangPackage builtTestFile, BLangFunction function) {
        super(builtTestFile);
        String functionName = function.name.value;
        TestFunctionGenerator generator = new TestFunctionGenerator(importsConsumer, builtTestFile.packageID, function);
        this.testFunctionName = getSafeFunctionName("test" + upperCaseFirstLetter(functionName));
        this.isVoidFunction = (function.returnTypeNode == null || function.returnTypeNode.type instanceof BNilType);
        this.functionInvocations = generator.getTargetFuncInvocations();
        this.dataProviderBasedFunctionInvocation =
                generator.getTargetFuncReturnType() + " actual = " + generator.getTargetFuncInvocation() + ";";
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
        String filename = (isVoidFunction) ? "voidFunction.bal" : "returnTypedFunction.bal";
        RendererOutput functionOutput = new TemplateBasedRendererOutput(filename);
        functionOutput.put(PlaceHolder.OTHER.get("testFunctionName"), testFunctionName);
        String functionInvocationLine;
        if (isVoidFunction) {
            StringJoiner lines = new StringJoiner(LINE_SEPARATOR);
            functionInvocations.forEach(
                    invocation -> lines.add("    " + invocation + ";")
            );
            functionInvocationLine = lines.toString();
        } else {
            functionInvocationLine = dataProviderBasedFunctionInvocation;
            functionOutput.put(PlaceHolder.OTHER.get("dataProviderReturnType"), dataProviderReturnType);
            functionOutput.put(PlaceHolder.OTHER.get("dataProviderReturnValue"), dataProviderReturnValue);
            functionOutput.put(PlaceHolder.OTHER.get("testFunctionParams"), testFunctionParams);
        }
        functionOutput.put(PlaceHolder.OTHER.get("actual"), functionInvocationLine);

        //Append to root template
        rendererOutput.append(PlaceHolder.CONTENT, functionOutput.getRenderedContent());
    }
}

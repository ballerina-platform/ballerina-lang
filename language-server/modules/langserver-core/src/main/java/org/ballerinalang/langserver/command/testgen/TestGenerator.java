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
package org.ballerinalang.langserver.command.testgen;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.command.testgen.renderer.BLangPkgBasedRendererOutput;
import org.ballerinalang.langserver.command.testgen.renderer.RendererOutput;
import org.ballerinalang.langserver.command.testgen.renderer.TemplateBasedRendererOutput;
import org.ballerinalang.langserver.command.testgen.template.RootTemplate;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.ballerinalang.langserver.command.testgen.ValueSpaceGenerator.createTemplateArray;
import static org.ballerinalang.langserver.command.testgen.ValueSpaceGenerator.getValueSpaceByNode;
import static org.ballerinalang.langserver.command.testgen.ValueSpaceGenerator.getValueSpaceByType;
import static org.ballerinalang.langserver.common.utils.CommonUtil.FunctionGenerator.generateTypeDefinition;

/**
 * This class is responsible for generating tests for a given source file.
 *
 * @since 0.985.0
 */
public class TestGenerator {

    private TestGenerator() {
    }

    /**
     * Creates a test file for a given BLangPackage in source file path.
     *
     * @param documentManager document manager
     * @param bLangNodePair   A pair of {@link BLangNode} and fallback node
     * @param builtSourceFile built {@link BLangPackage} source file
     * @param pkgRelativePath package relative path
     * @param testFile        test file
     * @return generated test file path
     * @throws TestGeneratorException when test case generation fails
     */
    public static List<TextEdit> generate(WorkspaceDocumentManager documentManager,
                                          Pair<BLangNode, Object> bLangNodePair, BLangPackage builtSourceFile,
                                          String pkgRelativePath, File testFile) throws TestGeneratorException {
        RootTemplate rootTemplate = getRootTemplateForBLangNode(pkgRelativePath, bLangNodePair, builtSourceFile);
        RendererOutput rendererOutput = getRendererOutput(documentManager, testFile);
        rootTemplate.render(rendererOutput);
        return rendererOutput.getRenderedTextEdits();
    }

    private static RendererOutput getRendererOutput(WorkspaceDocumentManager documentManager, File testFile)
            throws TestGeneratorException {
        // If exists, read the test file content
        String testContent = "";
        if (testFile.exists()) {
            try {
                // Reading through document manager since amendments are handled as text-edits
                testContent = documentManager.getFileContent(testFile.toPath());
            } catch (WorkspaceDocumentException e) {
                throw new TestGeneratorException("Error occurred while compiling file path:" + testFile.toString(), e);
            }
        }

        // Create file template
        RendererOutput fileTemplate;
        if (testContent.isEmpty()) {
            fileTemplate = new TemplateBasedRendererOutput("rootTest.bal");
        } else {
            BallerinaFile ballerinaFile;
            try {
                ballerinaFile = LSCompiler.compileContent(testContent, CompilerPhase.COMPILER_PLUGIN);
            } catch (LSCompilerException e) {
                throw new TestGeneratorException("Could not compile the test content", e);
            }
            Optional<BLangPackage> optBLangPackage = ballerinaFile.getBLangPackage();
            if (optBLangPackage.isPresent()) {
                fileTemplate = new BLangPkgBasedRendererOutput(optBLangPackage.get());
            } else {
                String msg = "Appending failed! unknown error occurred while appending to:" + testFile.toString();
                throw new TestGeneratorException(msg);
            }
        }
        return fileTemplate;
    }

    private static RootTemplate getRootTemplateForBLangNode(String fileName, Pair<BLangNode, Object> result,
                                                            BLangPackage builtTestFile) throws TestGeneratorException {
        BLangNode bLangNode = result.getLeft();
        Object fallBackNode = result.getRight();
        boolean fallback = false;

        if (bLangNode instanceof BLangFunction) {
            // A function
            return RootTemplate.fromFunction((BLangFunction) bLangNode, builtTestFile);

        } else if (bLangNode instanceof BLangService || (fallback = fallBackNode instanceof BLangService)) {
            // A Service
            BLangService service = (!fallback) ? ((BLangService) bLangNode) : (BLangService) fallBackNode;
            String owner = (service.serviceTypeStruct.type != null && service.serviceTypeStruct.type.tsymbol != null) ?
                    service.serviceTypeStruct.type.tsymbol.owner.name.value :
                    null;
            if ("http".equals(owner)) {
                switch (service.serviceTypeStruct.typeName.value) {
                    case "Service": {
                        return RootTemplate.fromHttpService(service, builtTestFile);
                    }
                    case "WebSocketService": {
                        return RootTemplate.fromHttpWSService(service, builtTestFile);
                    }
                    case "WebSocketClientService":
                        return RootTemplate.fromHttpClientWSService(service, builtTestFile);
                    default:
                        break;
                }
            } else if ("websub".equals(owner)) {
                throw new TestGeneratorException("WebSub services are not supported!");
            }
            throw new TestGeneratorException(service.serviceTypeStruct.toString() + " is not supported!");
        }
        // Whole file
        return new RootTemplate(fileName, builtTestFile);
    }

    /**
     * This class provides functionalities for generating a test function for a given target function.
     */
    public static class TestFunctionGenerator {
        public static final int VALUE_SPACE_LENGTH = 4;
        private String[][] valueSpace;
        private String[] typeSpace;
        private String[] namesSpace;
        private String functionName;
        private String returnType;

        public TestFunctionGenerator(BiConsumer<String, String> importsConsumer, PackageID currentPkgId,
                                     BLangFunction function) {
            List<BLangSimpleVariable> params = function.requiredParams;
            List<BLangType> paramTypes = params.stream().map(variable -> variable.typeNode).collect(
                    Collectors.toList());
            List<String> paramNames = new ArrayList<>();
            params.forEach(variable -> paramNames.add(variable.name.value));
            init(importsConsumer, currentPkgId, function.name.value, paramNames, paramTypes, function.returnTypeNode);
        }

        public TestFunctionGenerator(BiConsumer<String, String> importsConsumer, PackageID currentPkgId,
                                     BLangFunctionTypeNode type) {
            List<BLangVariable> params = type.params;
            List<BLangType> paramTypes = params.stream().map(variable -> variable.typeNode).collect(
                    Collectors.toList());
            List<String> paramNames = new ArrayList<>();
            params.forEach(variable -> {
                if (variable instanceof BLangSimpleVariable) {
                    paramNames.add(((BLangSimpleVariable) variable).name.value);
                } else {
                    paramNames.add(null);
                }
            });
            init(importsConsumer, currentPkgId, "", paramNames, paramTypes, type.returnTypeNode);
        }

        public TestFunctionGenerator(BiConsumer<String, String> importsConsumer, PackageID currentPkgId,
                                     BInvokableType invokableType) {
            this.functionName = "";
            List<BType> params = invokableType.paramTypes;
            BType returnBType = invokableType.retType;
            this.valueSpace = new String[VALUE_SPACE_LENGTH][params.size() + 1];
            this.typeSpace = new String[params.size() + 1];
            this.namesSpace = new String[params.size() + 1];

            // Populate target function's parameters
            Set<String> lookupSet = new HashSet<>();
            for (int i = 0; i < params.size(); i++) {
                String paramType = generateTypeDefinition(importsConsumer, currentPkgId,
                                                          params.get(i));
                String paramName = CommonUtil.generateName(1, lookupSet);
                lookupSet.add(paramName);

                this.typeSpace[i] = paramType;
                this.namesSpace[i] = paramName;

                String[] pValueSpace = getValueSpaceByType(importsConsumer, currentPkgId, params.get(i),
                                                           createTemplateArray(VALUE_SPACE_LENGTH));

                for (int j = 0; j < pValueSpace.length; j++) {
                    // Need to apply transpose of `pValueSpace`
                    // i.e. valueSpace = (pValueSpace)^T
                    this.valueSpace[j][i] = pValueSpace[j];
                }
            }

            // Populate target function's return type
            this.returnType = generateTypeDefinition(importsConsumer, currentPkgId, returnBType);
            String[] rtValSpace = getValueSpaceByType(importsConsumer, currentPkgId, returnBType,
                                                      createTemplateArray(VALUE_SPACE_LENGTH));

            this.typeSpace[params.size()] = returnType;
            this.namesSpace[params.size()] = "expected";

            IntStream.range(0, rtValSpace.length).forEach(index -> {
                valueSpace[index][params.size()] = rtValSpace[index];
            });
        }

        private void init(BiConsumer<String, String> importsConsumer, PackageID currentPkgId,
                          String functionName,
                          List<String> paramNames, List<BLangType> paramTypes, BLangType returnTypeNode) {
            this.functionName = functionName;
            this.valueSpace = new String[VALUE_SPACE_LENGTH][paramNames.size() + 1];
            this.typeSpace = new String[paramNames.size() + 1];
            this.namesSpace = new String[paramNames.size() + 1];

            // Populate target function's parameters
            Set<String> lookupSet = new HashSet<>();
            for (int i = 0; i < paramNames.size(); i++) {
                String paramType = generateTypeDefinition(importsConsumer, currentPkgId,
                                                          paramTypes.get(i));
                String paramName = paramNames.get(i);
                if (paramName == null) {
                    // If null, generate a param name
                    paramName = CommonUtil.generateName(1, lookupSet);
                }
                this.typeSpace[i] = paramType;
                this.namesSpace[i] = paramName;

                String[] pValueSpace = getValueSpaceByNode(importsConsumer, currentPkgId, paramTypes.get(i),
                                                           createTemplateArray(VALUE_SPACE_LENGTH));
                for (int j = 0; j < pValueSpace.length; j++) {
                    // Need to apply transpose of `pValueSpace`
                    // i.e. valueSpace = (pValueSpace)^T
                    this.valueSpace[j][i] = pValueSpace[j];
                }
                lookupSet.add(paramName);
            }

            // Populate target function's return type
            this.returnType = generateTypeDefinition(importsConsumer, currentPkgId,
                                                     returnTypeNode);
            String[] rtValSpace = getValueSpaceByNode(importsConsumer, currentPkgId, returnTypeNode,
                                                      createTemplateArray(VALUE_SPACE_LENGTH));

            this.typeSpace[paramNames.size()] = returnType;
            this.namesSpace[paramNames.size()] = "expected";

            IntStream.range(0, rtValSpace.length).forEach(index -> {
                valueSpace[index][paramNames.size()] = rtValSpace[index];
            });
        }

        /**
         * Returns parameters of the test function separated by comma.
         * <p>
         * eg. int x, int y
         * </p>
         *
         * @return string of type and name pairs
         */
        public String getTestFuncParams() {
            StringJoiner paramsStr = new StringJoiner(", ");
            IntStream.range(0, this.namesSpace.length).forEach(index -> {
                String type = this.typeSpace[index];
                String name = this.namesSpace[index];
                paramsStr.add(type + " " + name);
            });
            return paramsStr.toString();
        }

        /**
         * Returns a list of target function invocations.
         * <p>
         * eg. ["foo(5, 20)","foo(100, 30)"]
         * </p>
         *
         * @return function invocation
         */
        public List<String> getTargetFuncInvocations() {
            List<String> invocations = new ArrayList<>();
            IntStream.range(0, this.valueSpace.length - 1).forEach(
                    j -> {
                        StringJoiner paramsInvokeStr = new StringJoiner(", ");
                        IntStream.range(0, this.namesSpace.length - 1).forEach(
                                i -> paramsInvokeStr.add(this.valueSpace[j][i])
                        );
                        invocations.add(this.functionName + "(" + paramsInvokeStr.toString() + ")");
                    }
            );
            return invocations;
        }

        /**
         * Returns target function invocation string.
         * <p>
         * eg. foo(x, y)
         * </p>
         *
         * @return function invocation
         */
        public String getTargetFuncInvocation() {
            StringJoiner paramsInvokeStr = new StringJoiner(", ");
            IntStream.range(0, this.namesSpace.length - 1).forEach(i -> paramsInvokeStr.add(this.namesSpace[i]));
            return this.functionName + "(" + paramsInvokeStr.toString() + ")";
        }

        /**
         * Returns string of type of params of the data provider function.
         * <p>
         * eg. (int, float)[]
         * </p>
         *
         * @return return type
         */
        public String getDataProviderReturnType() {
            StringJoiner paramsTypeStr = new StringJoiner(", ");
            IntStream.range(0, this.typeSpace.length).forEach(i -> paramsTypeStr.add(this.typeSpace[i]));
            return "(" + paramsTypeStr.toString() + ")[]";
        }

        /**
         * Returns return type of the target function.
         * <p>
         * eg. (int, int)
         * </p>
         *
         * @return return type of the function
         */
        public String getTargetFuncReturnType() {
            return this.returnType;
        }

        /**
         * Returns return value of the data provider function.
         * <p>
         * eg. [(1, 0.5),(-1, 2.5)]
         * </p>
         *
         * @return return value of the data provider
         */
        public String getDataProviderReturnValue() {
            // Prepare data provider's return value
            StringJoiner vSpace = new StringJoiner("), (", "(", ")");
            IntStream.range(0, valueSpace.length).forEach(index -> {
                vSpace.add(String.join(", ", valueSpace[index]));
            });
            return "[" + vSpace.toString() + "]";
        }

        /**
         * Returns the value space of the test function (including expected return value).
         *
         * @return value space
         */
        public String[][] getValueSpace() {
            return valueSpace.clone();
        }

        /**
         * Returns the types space of the test function (including expected return type).
         *
         * @return type space
         */
        public String[] getTypeSpace() {
            return typeSpace.clone();
        }

        /**
         * Returns the names space of the test function (including expected return param name).
         *
         * @return names space
         */
        public String[] getNamesSpace() {
            return namesSpace.clone();
        }
    }
}

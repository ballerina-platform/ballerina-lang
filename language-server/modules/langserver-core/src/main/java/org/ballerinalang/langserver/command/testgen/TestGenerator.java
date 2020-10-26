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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.command.testgen.renderer.BLangPkgBasedRendererOutput;
import org.ballerinalang.langserver.command.testgen.renderer.RendererOutput;
import org.ballerinalang.langserver.command.testgen.renderer.TemplateBasedRendererOutput;
import org.ballerinalang.langserver.command.testgen.template.RootTemplate;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

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
     * @param documentManager   document manager
     * @param bLangNode         A pair of {@link BLangNode} and fallback node
     * @param focusLineAcceptor focus line acceptor
     * @param builtSourceFile   built {@link BLangPackage} source file
     * @param pkgRelativePath   package relative path
     * @param testFile          test file
     * @param context  {@link LSContext}
     * @return generated test file path
     * @throws TestGeneratorException when test case generation fails
     */
    public static List<TextEdit> generate(WorkspaceDocumentManager documentManager,
                                          BLangNode bLangNode,
                                          BiConsumer<Integer, Integer> focusLineAcceptor,
                                          BLangPackage builtSourceFile, String pkgRelativePath,
                                          File testFile, LSContext context) throws TestGeneratorException {
        RootTemplate template = getRootTemplate(pkgRelativePath, bLangNode, builtSourceFile, focusLineAcceptor,
                                                context);
        RendererOutput rendererOutput = getRendererOutput(documentManager, testFile, focusLineAcceptor);
        template.render(rendererOutput);
        return rendererOutput.getRenderedTextEdits();
    }

    private static RendererOutput getRendererOutput(WorkspaceDocumentManager documentManager, File testFile,
                                                    BiConsumer<Integer, Integer> focusLineAcceptor)
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

        // Create tests
        RendererOutput fileTemplate;
        if (testContent.isEmpty()) {
            // Create tests from file template
            fileTemplate = new TemplateBasedRendererOutput("rootTest.bal");
        } else {
            // Create tests from blang package
            BallerinaFile ballerinaFile;
            try {
                ballerinaFile = ExtendedLSCompiler.compileContent(testContent, CompilerPhase.COMPILER_PLUGIN);
            } catch (CompilationFailedException e) {
                throw new TestGeneratorException("Could not compile the test content", e);
            }
            Optional<BLangPackage> optBLangPackage = ballerinaFile.getBLangPackage();
            if (optBLangPackage.isPresent()) {
                fileTemplate = new BLangPkgBasedRendererOutput(optBLangPackage.get(), focusLineAcceptor);
            } else {
                String msg = "Appending failed! unknown error occurred while appending to:" + testFile.toString();
                throw new TestGeneratorException(msg);
            }
        }
        return fileTemplate;
    }

    private static RootTemplate getRootTemplate(String fileName, BLangNode bLangNode,
                                                BLangPackage builtTestFile,
                                                BiConsumer<Integer, Integer> focusLineAcceptor,
                                                LSContext context)
            throws TestGeneratorException {
        if (bLangNode == null) {
            throw new TestGeneratorException("Target test construct not found!");
        }

        if (bLangNode instanceof BLangFunction) {
            // A function
            return RootTemplate.fromFunction((BLangFunction) bLangNode, builtTestFile, focusLineAcceptor, context);

        } else if (bLangNode instanceof BLangService || hasServiceConstructor(bLangNode)) {
            // A Service
            BLangService service;
            if (bLangNode instanceof BLangService) {
                // is a service eg. service {};
                service = (BLangService) bLangNode;
            } else {
                // is a service variable eg. service a = service {};
                service = ((BLangServiceConstructorExpr) (((BLangSimpleVariable) bLangNode).expr)).serviceNode;
            }

            String owner = (service.listenerType != null) ? service.listenerType.tsymbol.owner.name.value : null;
            String serviceTypeName = (service.listenerType != null) ? service.listenerType.tsymbol.name.value : null;
            Optional<BLangTypeInit> optionalServiceInit = getServiceInit(builtTestFile, service);
            RootTemplate[] t = {null};
            // Has ServiceInit
            optionalServiceInit.ifPresent(init -> {
                if ("http".equals(owner)) {
                    switch (serviceTypeName) {
                        case "Listener":
                            t[0] = RootTemplate.fromHttpService(service, init, builtTestFile, focusLineAcceptor,
                                                                context);
                            break;
                        case "WebSocketListener":
                            t[0] = RootTemplate.fromHttpWSService(service, init, builtTestFile, focusLineAcceptor,
                                                                  context);
                            break;
                        default:
                            // do nothing
                    }
                }
            });
            // Return service
            if (t[0] == null) {
                if (hasServiceConstructor(bLangNode)) {
                    throw new TestGeneratorException("Services assigned to the variables are not supported!");
                }
                throw new TestGeneratorException(owner + ":" + serviceTypeName + " services are not supported!");
            }
            return t[0];
        }
        // Whole file
        return new RootTemplate(fileName, builtTestFile, focusLineAcceptor, context);
    }

    private static boolean hasServiceConstructor(BLangNode bLangNode) {
        return (bLangNode instanceof BLangSimpleVariable &&
                ((BLangSimpleVariable) bLangNode).expr instanceof BLangServiceConstructorExpr);
    }

    public static Optional<BLangTypeInit> getServiceInit(BLangPackage builtTestFile, BLangService service) {
        if (service.attachedExprs.isEmpty()) {
            return Optional.empty();
        }
        BLangExpression expr = service.attachedExprs.get(0);
        if (expr instanceof BLangTypeInit) {
            // If in-line listener
            return Optional.of((BLangTypeInit) expr);
        }
        String[] variableName = {""};
        if (expr instanceof BLangSimpleVarRef) {
            // variable ref listener
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) expr;
            variableName[0] = varRef.variableName.value;
        }

        for (TopLevelNode topLevelNode : builtTestFile.topLevelNodes) {
            if (topLevelNode instanceof BLangSimpleVariable) {
                BLangSimpleVariable var = (BLangSimpleVariable) topLevelNode;
                BLangExpression varExpr = var.expr;
                if (varExpr instanceof BLangTypeInit && variableName[0].equals(var.name.value)) {
                    return Optional.of((BLangTypeInit) varExpr);
                }
            }
        }
        return Optional.empty();
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
//        private String returnType;
        private int paramsCount;

        public TestFunctionGenerator(ImportsAcceptor importsAcceptor,
                                     BLangFunction function, LSContext context) {
            List<BLangSimpleVariable> params = function.requiredParams;
            List<BLangType> paramTypes = params.stream().map(variable -> variable.typeNode).collect(
                    Collectors.toList());
            List<String> paramNames = new ArrayList<>();
            params.forEach(variable -> paramNames.add(variable.name.value));
            init(importsAcceptor, function.name.value, paramNames, paramTypes, function.returnTypeNode,
                 context);
        }

        public TestFunctionGenerator(ImportsAcceptor importsAcceptor,
                                     BLangFunctionTypeNode type, LSContext context) {
            List<BLangVariable> params = type.params;
            this.paramsCount = type.params.size();
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
            init(importsAcceptor, "", paramNames, paramTypes, type.returnTypeNode, context);
        }

        public TestFunctionGenerator(ImportsAcceptor importsAcceptor,
                                     BInvokableType invokableType, LSContext context) {
            boolean hasReturnType = true;
            if (invokableType.retType == null || invokableType.retType instanceof BNilType) {
                // No return type
                hasReturnType = false;
            }
            this.functionName = "";
            List<BType> params = invokableType.paramTypes;
            this.paramsCount = params.size();
//            BType returnBType = invokableType.retType;
            this.valueSpace = new String[VALUE_SPACE_LENGTH][params.size() + 1];
            this.typeSpace = new String[params.size() + 1];
            this.namesSpace = new String[params.size() + 1];

            // Populate target function's parameters
            Set<String> lookupSet = new HashSet<>();
            for (int i = 0; i < params.size(); i++) {
//                String paramType = generateTypeDefinition(importsAcceptor,
//                                                          params.get(i), context);
                String paramType = "";
                String paramName = CommonUtil.generateName(1, lookupSet);
                lookupSet.add(paramName);

                this.typeSpace[i] = paramType;
                this.namesSpace[i] = paramName;
                //TODO: Fix this
                String[] pValueSpace = new String[0];
//                String[] pValueSpace = getValueSpaceByType(importsAcceptor,  params.get(i),
//                                                           createTemplateArray(VALUE_SPACE_LENGTH), context);

                for (int j = 0; j < pValueSpace.length; j++) {
                    // Need to apply transpose of `pValueSpace`
                    // i.e. valueSpace = (pValueSpace)^T
                    this.valueSpace[j][i] = pValueSpace[j];
                }
            }

            // Populate target function's return type
            //TODO: Fix this
//            if (hasReturnType) {
//                this.returnType = generateTypeDefinition(importsAcceptor,  returnBType, context);
//                String[] rtValSpace = getValueSpaceByType(importsAcceptor,  returnBType,
//                                                          createTemplateArray(VALUE_SPACE_LENGTH), context);

//                this.typeSpace[params.size()] = returnType;
//                this.namesSpace[params.size()] = "expected";

//                IntStream.range(0, rtValSpace.length).forEach(index -> {
//                    valueSpace[index][params.size()] = rtValSpace[index];
//                });
//            }
        }

        private void init(ImportsAcceptor importsAcceptor,
                          String functionName,
                          List<String> paramNames, List<BLangType> paramTypes, BLangType returnTypeNode,
                          LSContext context) {
            boolean hasReturnType = true;
            if (returnTypeNode instanceof BLangValueType &&
                    (TypeKind.NIL.equals(((BLangValueType) returnTypeNode).getTypeKind()))) {
                // No return type
                hasReturnType = false;
            }
            this.functionName = functionName;
            this.paramsCount = paramNames.size();
            this.valueSpace = new String[VALUE_SPACE_LENGTH][paramNames.size() + ((hasReturnType ? 1 : 0))];
            this.typeSpace = new String[paramNames.size() + ((hasReturnType ? 1 : 0))];
            this.namesSpace = new String[paramNames.size() + ((hasReturnType ? 1 : 0))];

            // Populate target function's parameters
            Set<String> lookupSet = new HashSet<>();
            for (int i = 0; i < paramNames.size(); i++) {
                String paramType = "";
//                String paramType = generateTypeDefinition(importsAcceptor,
//                                                          paramTypes.get(i), context);
                String paramName = paramNames.get(i);
                if (paramName == null) {
                    // If null, generate a param name
                    paramName = CommonUtil.generateName(1, lookupSet);
                }
                this.typeSpace[i] = paramType;
                this.namesSpace[i] = paramName;
                //TODO: Fix this
                String[] pValueSpace = new String[0];
//                String[] pValueSpace = getValueSpaceByNode(importsAcceptor,  paramTypes.get(i),
//                                                           createTemplateArray(VALUE_SPACE_LENGTH), context);
                for (int j = 0; j < pValueSpace.length; j++) {
                    // Need to apply transpose of `pValueSpace`
                    // i.e. valueSpace = (pValueSpace)^T
                    this.valueSpace[j][i] = pValueSpace[j];
                }
                lookupSet.add(paramName);
            }

            // Populate target function's return type
            if (hasReturnType) {

                //TODO: Fix this
//                this.returnType = generateTypeDefinition(importsAcceptor,
//                                                         returnTypeNode, context);
                String[] rtValSpace = new String[0];
//                String[] rtValSpace = getValueSpaceByNode(importsAcceptor,  returnTypeNode,
//                                                          createTemplateArray(VALUE_SPACE_LENGTH), context);

//                this.typeSpace[paramNames.size()] = returnType;
//                this.namesSpace[paramNames.size()] = "expected";

                IntStream.range(0, rtValSpace.length).forEach(index -> {
                    valueSpace[index][paramNames.size()] = rtValSpace[index];
                });
            }
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
            IntStream.range(0, this.paramsCount).forEach(i -> paramsInvokeStr.add(this.namesSpace[i]));
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
            //TODO: Fix this
            return "";
//            return this.returnType;
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


        /**
         * Returns params count of the function.
         *
         * @return params count
         */
        public int getParamsCount() {
            return paramsCount;
        }
    }
}

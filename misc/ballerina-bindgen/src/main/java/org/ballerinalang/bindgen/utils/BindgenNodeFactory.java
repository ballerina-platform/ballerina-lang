/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.utils;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastParamNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.model.BFunction;
import org.ballerinalang.bindgen.model.JConstructor;
import org.ballerinalang.bindgen.model.JField;
import org.ballerinalang.bindgen.model.JMethod;
import org.ballerinalang.bindgen.model.JParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.bindgen.utils.BindgenConstants.CLASS;
import static org.ballerinalang.bindgen.utils.BindgenConstants.HANDLE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.PARAM_TYPES;

/**
 * Class for generating the Ballerina syntax tree util nodes and tokens.
 *
 * @since 2.0.0
 */
class BindgenNodeFactory {

    /**
     * Create an import declaration name node while providing the organization name, optional prefix, and module names.
     *
     * @param orgNameValue - the organization name
     * @param prefixValue - an optional prefix value
     * @param moduleNames - list of module names with separators
     * @return the import declaration node created
     */
    static ImportDeclarationNode createImportDeclarationNode(String orgNameValue, String prefixValue,
                                                             List<String> moduleNames) {
        Token importKeyword = AbstractNodeFactory.createToken(SyntaxKind.IMPORT_KEYWORD, emptyML(), singleWSML());
        ImportOrgNameNode orgName = null;
        if (orgNameValue != null) {
            orgName = createImportOrgNameNode(orgNameValue);
        }
        SeparatedNodeList<IdentifierToken> moduleName = AbstractNodeFactory
                .createSeparatedNodeList(getTokenList(moduleNames));

        ImportPrefixNode prefix = null;
        if (prefixValue != null) {
            prefix = createImportPrefixNode(prefixValue);
        }
        Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createImportDeclarationNode(importKeyword, orgName, moduleName, prefix, semicolon);
    }

    /**
     * Create an import organization name node while providing the organization name.
     */
    private static ImportOrgNameNode createImportOrgNameNode(String orgNameValue) {
        Token orgName = AbstractNodeFactory.createIdentifierToken(orgNameValue);
        Token slashToken = AbstractNodeFactory.createToken(SyntaxKind.SLASH_TOKEN);

        return NodeFactory.createImportOrgNameNode(orgName, slashToken);
    }

    /**
     * Create an import prefix node while providing the prefix.
     */
    private static ImportPrefixNode createImportPrefixNode(String prefixValue) {
        Token asKeyword = AbstractNodeFactory.createToken(SyntaxKind.AS_KEYWORD, singleWSML(), singleWSML());
        Token prefix = AbstractNodeFactory.createIdentifierToken(prefixValue);

        return NodeFactory.createImportPrefixNode(asKeyword, prefix);
    }

    /**
     * Creates a type reference node using the string type provided.
     *
     * @param type - the type reference value as a string
     * @return the type reference node created
     */
    static TypeReferenceNode createTypeReferenceNode(String type) {
        Token asteriskToken = AbstractNodeFactory.createToken(SyntaxKind.ASTERISK_TOKEN);
        Node typeName = createSimpleNameReferenceNode(type);
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN, emptyML(), singleNLML());

        return NodeFactory.createTypeReferenceNode(asteriskToken, typeName, semicolonToken);
    }

    /**
     * Create a function definition node for a specific java method.
     *
     * @param bFunction - Java method for which the Ballerina function definition node is to be created.
     * @param isExternal - Specifies if the external function needs to be created instead of the mapping function.
     * @return the function definition node created
     */
    static FunctionDefinitionNode createFunctionDefinitionNode(BFunction bFunction, boolean isExternal)
            throws BindgenException {
        MetadataNode metadata = null;
        if (!isExternal) {
            metadata = createMetadataNode(createMarkdownDocumentationNode(
                    getFunctionMarkdownDocumentation(bFunction)), AbstractNodeFactory.createNodeList());
        }

        NodeList<Token> qualifierList;
        if (bFunction.getEnv().hasPublicFlag() && !isExternal) {
            Token accessModifier = AbstractNodeFactory.createToken(SyntaxKind.PUBLIC_KEYWORD, emptyML(), singleWSML());
            qualifierList = AbstractNodeFactory.createNodeList(accessModifier);
        } else {
            qualifierList = AbstractNodeFactory.createNodeList();
        }

        Token functionKeyword = AbstractNodeFactory.createToken(SyntaxKind.FUNCTION_KEYWORD, emptyML(), singleWSML());
        NodeList<Node> relativeResourcePath = AbstractNodeFactory.createNodeList();
        FunctionSignatureNode functionSignature = createFunctionSignatureNode(bFunction, isExternal);

        IdentifierToken functionName;
        FunctionBodyNode functionBody;
        if (isExternal) {
            functionName = AbstractNodeFactory.createIdentifierToken(bFunction.getExternalFunctionName());
            functionBody = createExternalFunctionBodyNode(bFunction);
        } else {
            functionName = AbstractNodeFactory.createIdentifierToken(bFunction.getFunctionName());
            functionBody = createFunctionBodyBlockNode(bFunction);
        }
        return NodeFactory.createFunctionDefinitionNode(null, metadata, qualifierList, functionKeyword,
                functionName, relativeResourcePath, functionSignature, functionBody);
    }

    private static NodeList<Node> getFunctionMarkdownDocumentation(BFunction bFunction) {
        List<Node> documentationLines = new LinkedList<>();
        documentationLines.addAll(getFunctionMarkdownDocumentationLine(bFunction));
        documentationLines.addAll(getFunctionMarkdownParameterDocumentationLine(bFunction));

        return AbstractNodeFactory.createNodeList(documentationLines);
    }

    private static List<Node> getFunctionMarkdownDocumentationLine(BFunction bFunction) {
        List<Node> documentationLines = new LinkedList<>();
        String className = bFunction.getDeclaringClass().getName();
        String documentationValue;
        if (bFunction.getKind() == BFunction.BFunctionKind.CONSTRUCTOR) {
            documentationValue = "The constructor function to generate an object of `" + className + "`.";
        } else if (bFunction.getKind() == BFunction.BFunctionKind.METHOD) {
            JMethod jMethod = (JMethod) bFunction;
            documentationValue = "The function that maps to the `" + jMethod.getJavaMethodName() + "` method of `"
                    + className + "`.";
        } else if (bFunction.getKind() == BFunction.BFunctionKind.FIELD_GET) {
            JField jField = (JField) bFunction;
            documentationValue = "The function that retrieves the value of the public field `"
                    + jField.getFieldName() + "`.";
        } else if (bFunction.getKind() == BFunction.BFunctionKind.FIELD_SET) {
            JField jField = (JField) bFunction;
            documentationValue = "The function to set the value of the public field `" + jField.getFieldName() + "`.";
        } else {
            return documentationLines;
        }
        documentationLines.add(createMarkdownDocumentationLineNode(documentationValue));
        if (!bFunction.getParameters().isEmpty() || bFunction.getReturnType() != null
                || bFunction.getErrorType() != null) {
            documentationLines.add(createMarkdownDocumentationLineNode(""));
        }
        return documentationLines;
    }

    private static List<Node> getFunctionMarkdownParameterDocumentationLine(BFunction bFunction) {
        List<Node> parameterDocumentationLines = new LinkedList<>();
        if (!bFunction.getParameters().isEmpty()) {
            for (JParameter jParameter : bFunction.getParameters()) {
                String paramDescription = documentationParamDescription(bFunction.getKind(), jParameter);
                if (paramDescription != null) {
                    parameterDocumentationLines.add(createMarkdownParameterDocumentationLineNode(
                            jParameter.getFieldName(), paramDescription));
                }
            }
        }
        String returnDescription = documentationReturnDescription(bFunction);
        if (returnDescription != null) {
            parameterDocumentationLines.add(createMarkdownParameterDocumentationLineNode(
                    "return", returnDescription));
        }
        return parameterDocumentationLines;
    }

    private static String documentationParamDescription(BFunction.BFunctionKind functionKind, JParameter jParameter) {
        String paramDescription = null;
        if (functionKind == BFunction.BFunctionKind.CONSTRUCTOR) {
            paramDescription = "The `" + jParameter.getShortTypeName() + "` value required to map with the " +
                    "Java constructor parameter.";
        } else if (functionKind == BFunction.BFunctionKind.METHOD) {
            paramDescription = "The `" + jParameter.getShortTypeName() + "` value required to map with the " +
                    "Java method parameter.";
        } else if (functionKind == BFunction.BFunctionKind.FIELD_SET) {
            paramDescription = "The `" + jParameter.getShortTypeName() + "` value that is to be set for the field.";
        }
        return paramDescription;
    }

    private static String documentationReturnDescription(BFunction bFunction) {
        String paramDescription = null;
        if (bFunction.getReturnType() == null && bFunction.getErrorType() == null) {
            return paramDescription;
        }
        if (bFunction.getKind() == BFunction.BFunctionKind.CONSTRUCTOR) {
            if (bFunction.getErrorType() != null) {
                paramDescription = "The new `" + bFunction.getReturnType() + "` class or `"
                        + bFunction.getErrorType() + "` error generated.";
            } else {
                paramDescription = "The new `" + bFunction.getReturnType() + "` class generated.";
            }
        } else if (bFunction.getKind() == BFunction.BFunctionKind.METHOD) {
            if (bFunction.getReturnType() != null && bFunction.getErrorType() != null) {
                paramDescription = "The `" + bFunction.getReturnType() + "` or the `" + bFunction.getErrorType()
                        + "` value returning from the Java mapping.";
            } else if (bFunction.getReturnType() != null) {
                paramDescription = "The `" + bFunction.getReturnType() + "` value returning from the Java mapping.";
            } else {
                paramDescription = "The `" + bFunction.getErrorType() + "` value returning from the Java mapping.";
            }
        } else if (bFunction.getKind() == BFunction.BFunctionKind.FIELD_GET) {
            paramDescription = "The `" + bFunction.getReturnType() + "` value of the field.";
        }
        return paramDescription;
    }

    /*
     * Create a function signature node using the BFunction node provided.
     * */
    private static FunctionSignatureNode createFunctionSignatureNode(BFunction bFunction, boolean isExternal)
            throws BindgenException {
        Token openParenToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        Token closeParenToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);
        List<Node> parameterNodes = new LinkedList<>();
        if (isExternal && !bFunction.isStatic()) {
            parameterNodes.add(createRequiredParameterNode(HANDLE, "receiver"));
            if (!bFunction.getParameters().isEmpty()) {
                parameterNodes.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
            }
        }
        for (JParameter jParameter : bFunction.getParameters()) {
            if (jParameter.isArray() && isMultiDimensionalArray(jParameter.getParameterClass().getName())) {
                throw new BindgenException("multidimensional arrays are currently unsupported");
            }
            if (isExternal) {
                parameterNodes.add(createRequiredParameterNode(jParameter.getExternalType()
                        + " ", jParameter.getFieldName()));
            } else {
                parameterNodes.add(createRequiredParameterNode(jParameter.getShortTypeName()
                        + " ", jParameter.getFieldName()));
            }

            parameterNodes.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        if (parameterNodes.size() > 1) {
            parameterNodes.remove(parameterNodes.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameters = AbstractNodeFactory.createSeparatedNodeList(parameterNodes);

        ReturnTypeDescriptorNode returnTypeDescriptor;
        if (isExternal) {
            returnTypeDescriptor = getExternalFunctionSignatureReturnType(bFunction);
        } else {
            returnTypeDescriptor = getFunctionSignatureReturnType(bFunction);
        }

        return NodeFactory.createFunctionSignatureNode(openParenToken, parameters, closeParenToken,
                returnTypeDescriptor);
    }


    private static ReturnTypeDescriptorNode getFunctionSignatureReturnType(BFunction bFunction)
            throws BindgenException {
        String returnType = null;
        if (bFunction.getKind() == BFunction.BFunctionKind.METHOD) {
            returnType = ((JMethod) bFunction).getFunctionReturnType();
        } else if (bFunction.getKind() == BFunction.BFunctionKind.CONSTRUCTOR) {
            returnType = ((JConstructor) bFunction).getFunctionReturnType();
        } else if (bFunction.getKind() == BFunction.BFunctionKind.FIELD_GET) {
            returnType = ((JField) bFunction).getFunctionReturnType();
        }
        if (returnType == null || returnType.equals("")) {
            return null;
        } else if (isMultiDimensionalArray(returnType)) {
            throw new BindgenException("multidimensional arrays are currently unsupported");
        }
        return createReturnTypeDescriptorNode(createSimpleNameReferenceNode(returnType));
    }

    private static ReturnTypeDescriptorNode getExternalFunctionSignatureReturnType(BFunction bFunction) {
        if (bFunction.getKind() == BFunction.BFunctionKind.FIELD_SET) {
            return null;
        }
        if (bFunction.getExternalReturnType() != null && !bFunction.getThrowables().isEmpty()) {
            return createReturnTypeDescriptorNode(createSimpleNameReferenceNode(bFunction
                    .getExternalReturnType() + "|error"));
        } else if (bFunction.getExternalReturnType() != null) {
            return createReturnTypeDescriptorNode(createSimpleNameReferenceNode(bFunction.getExternalReturnType()));
        } else if (!bFunction.getThrowables().isEmpty()) {
            return createReturnTypeDescriptorNode(createSimpleNameReferenceNode("error?"));
        }
        return null;
    }

    private static boolean isMultiDimensionalArray(String className) {
        return className.codePoints().filter(ch -> ch == '[').count() > 1;
    }

    /**
     * Create an external function body node for a specific BFunction provided.
     */
    private static ExternalFunctionBodyNode createExternalFunctionBodyNode(BFunction bFunction) {
        Map<String, List<String>> fields = new LinkedHashMap<>();

        if (bFunction.getKind() == BFunction.BFunctionKind.METHOD) {
            JMethod jMethod = (JMethod) bFunction;
            fields.put(NAME, Collections.singletonList(jMethod.getJavaMethodName()));
            fields.put(CLASS, Collections.singletonList(jMethod.getDeclaringClass().getName()));
            fields.put(PARAM_TYPES, getParameterList(jMethod.getParameters()));

        } else if (bFunction.getKind() == BFunction.BFunctionKind.CONSTRUCTOR) {
            JConstructor jConstructor = (JConstructor) bFunction;
            fields.put(CLASS, Collections.singletonList(jConstructor.getDeclaringClass().getName()));
            fields.put(PARAM_TYPES, getParameterList(jConstructor.getParameters()));

        } else if (bFunction.getKind() == BFunction.BFunctionKind.FIELD_GET ||
                bFunction.getKind() == BFunction.BFunctionKind.FIELD_SET) {
            JField jField = (JField) bFunction;
            fields.put(NAME, Collections.singletonList(jField.getFieldName()));
            fields.put(CLASS, Collections.singletonList(jField.getDeclaringClass().getName()));
        }

        Token equalsToken = AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN);
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createNodeList(createAnnotationNode(
                bFunction.getKind().value(), fields));
        Token externalToken = AbstractNodeFactory.createToken(SyntaxKind.EXTERNAL_KEYWORD);
        Token semiColonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createExternalFunctionBodyNode(equalsToken, annotations, externalToken, semiColonToken);
    }

    private static FunctionBodyBlockNode createFunctionBodyBlockNode(BFunction bFunction) {
        Token openBraceToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);

        List<StatementNode> statementNodes;
        if (bFunction.getKind() == BFunction.BFunctionKind.METHOD) {
            JMethod jMethod = (JMethod) bFunction;
            statementNodes = getMethodFunctionStatements(jMethod);
        } else if (bFunction.getKind() == BFunction.BFunctionKind.CONSTRUCTOR) {
            JConstructor jConstructor = (JConstructor) bFunction;
            statementNodes = getConstructorFunctionStatements(jConstructor);
        } else {
            JField jField = (JField) bFunction;
            statementNodes = getFieldFunctionStatements(jField);
        }

        NodeList<StatementNode> statements = AbstractNodeFactory.createNodeList(statementNodes);
        Token closeBraceToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN, emptyML(), doubleNLML());

        return NodeFactory.createFunctionBodyBlockNode(openBraceToken, null, statements, closeBraceToken);
    }

    private static List<StatementNode> getConstructorFunctionStatements(JConstructor jConstructor) {
        List<StatementNode> statementNodes = new LinkedList<>();
        if (jConstructor.isHandleException()) {
            statementNodes.addAll(getConstructorWithException(jConstructor));
        } else {
            statementNodes.addAll(getConstructorWithoutException(jConstructor));
        }

        return statementNodes;
    }

    private static List<StatementNode> getConstructorWithoutException(JConstructor jConstructor) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement(HANDLE, jConstructor));
        statementNodes.add(createVariableDeclarationNode(
                createTypedBindingPatternNode(jConstructor.getReturnType(), "newObj"),
                createImplicitNewExpressionNode(Collections.singletonList("externalObj"))));
        statementNodes.add(createReturnStatementNode(createSimpleNameReferenceNode("newObj")));

        return statementNodes;
    }

    private static List<StatementNode> getConstructorWithException(JConstructor jConstructor) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement("handle|error", jConstructor));
        statementNodes.add(createIfElseStatementNode(
                createBracedExpressionNode(createSimpleNameReferenceNode("externalObj is error")),
                getCheckExceptionBlock(jConstructor.getExceptionName(), jConstructor.getExceptionConstName()),
                createElseBlockNode(createBlockStatementNode(AbstractNodeFactory.createNodeList(
                        createVariableDeclarationNode(
                                createTypedBindingPatternNode(jConstructor.getReturnType(), "newObj"),
                                createImplicitNewExpressionNode(Collections.singletonList("externalObj"))
                        ),
                        createReturnStatementNode(createSimpleNameReferenceNode("newObj")))))));

        return statementNodes;
    }

    private static List<StatementNode> getFieldFunctionStatements(JField jField) {
        List<StatementNode> statementNodes = new LinkedList<>();
        if (jField.getKind() == BFunction.BFunctionKind.FIELD_GET) {
            if (!jField.isArray()) {
                if (jField.isString()) {
                    // string, no arrays, no errors
                    statementNodes.addAll(getFieldGetStringStatement(jField));
                } else if (jField.isObject()) {
                    // object, no arrays, no errors
                    statementNodes.addAll(getFieldGetObjectStatements(jField));
                } else {
                    // primitive, no arrays, no errors
                    statementNodes.addAll(getFieldGetPrimitiveStatement(jField));
                }
            } else {
                if (jField.isStringArray()) {
                    // string, has simple error
                    statementNodes.addAll(getFieldGetStringArrayStatements(jField));
                } else if (jField.isObjectArray()) {
                    // object, has simple error
                    statementNodes.addAll(getFieldGetObjectArrayStatements(jField));
                } else {
                    // primitive, has simple error
                    statementNodes.addAll(getFieldGetPrimitiveArrayStatements(jField));
                }
            }
        } else {
            statementNodes.addAll(getFieldSetStatements(jField));
        }

        return statementNodes;
    }

    private static List<StatementNode> getFieldGetPrimitiveArrayStatements(JField jField) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement(HANDLE, jField));
        statementNodes.add(createReturnStatementNode(createTypeCastExpressionNode(jField.getReturnType(),
                createCheckExpressionNode(createFunctionCallExpressionNode("jarrays:fromHandle",
                        new LinkedList<>(Arrays.asList("externalObj", "\"" + jField.getFieldType() + "\"")))))));

        return statementNodes;
    }

    private static List<StatementNode> getFieldGetObjectArrayStatements(JField jField) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement(HANDLE, jField));
        statementNodes.add(createVariableDeclarationNode(
                createTypedBindingPatternNode(jField.getReturnShortName(), "newObj"),
                createListConstructorExpressionNode(new LinkedList<>())));
        statementNodes.add(createVariableDeclarationNode(
                createTypedBindingPatternNode("handle[]", "anyObj"),
                createTypeCastExpressionNode("handle[]",
                        createCheckExpressionNode(createFunctionCallExpressionNode("jarrays:fromHandle",
                                new LinkedList<>(Arrays.asList("externalObj", "\"handle\"")))))));
        statementNodes.add(createVariableDeclarationNode(
                createTypedBindingPatternNode("int", "count"),
                createMethodCallExpressionNode(createSimpleNameReferenceNode("anyObj"), "length", new LinkedList<>())));
        statementNodes.add(getObjectArrayPopulation(jField.getReturnShortName()));
        statementNodes.add(createReturnStatementNode(createSimpleNameReferenceNode("newObj")));
        return statementNodes;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static List<StatementNode> getFieldGetStringArrayStatements(JField jField) {
        return List.of(
            //   handle externalObj = <call external method>;
            getExternalFunctionCallStatement(HANDLE, jField),
            //   if java:isNull(externalObj) {
            //       return null;
            //   }
            createReturnIfHandleIsNullStatement("externalObj"),
            //   return <string[]> check jarrays:fromHandle(externalObj, "string");
            createReturnStatementNode(
                createTypeCastExpressionNode("string[]",
                createCheckExpressionNode(
                    createFunctionCallExpressionNode(
                        "jarrays:fromHandle",
                        new LinkedList<>(Arrays.asList("externalObj", "\"string\""))
                    )
                )
            ))
        );
    }

    private static List<StatementNode> getFieldGetPrimitiveStatement(JField jField) {
        List<StatementNode> statementNodes = new LinkedList<>();
        statementNodes.add(createReturnStatementNode(createFunctionCallExpressionNode(
                jField.getExternalFunctionName(), getParameterArgumentList(jField))));

        return statementNodes;
    }

    private static List<StatementNode> getFieldGetObjectStatements(JField jField) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement(HANDLE, jField));
        statementNodes.add(createVariableDeclarationNode(createTypedBindingPatternNode(
                jField.getReturnShortName(), "newObj"),
                createImplicitNewExpressionNode(Collections.singletonList("externalObj"))));
        statementNodes.add(createReturnStatementNode(createSimpleNameReferenceNode("newObj")));

        return statementNodes;
    }

    private static List<StatementNode> getFieldGetStringStatement(JField jField) {
        List<StatementNode> statementNodes = new LinkedList<>();
        FunctionCallExpressionNode innerFunctionCall = createFunctionCallExpressionNode(
                jField.getExternalFunctionName(), getParameterArgumentList(jField));
        statementNodes.add(createReturnStatementNode(createFunctionCallExpressionNode("java:toString",
                Collections.singletonList(innerFunctionCall.toSourceCode()))));

        return statementNodes;
    }

    private static List<StatementNode> getFieldSetStatements(JField jField) {
        List<StatementNode> statementNodes = new LinkedList<>();
        statementNodes.add(createExpressionStatementNode(
                createFunctionCallExpressionNode(jField.getExternalFunctionName(), getParameterArgumentList(jField))));

        return statementNodes;
    }

    private static List<StatementNode> getMethodFunctionStatements(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();
        if (!jMethod.getHasReturn()) {
            if (!jMethod.isHandleException()) {
                statementNodes.add(getNoReturnNoExceptionStatement(jMethod));
            } else {
                statementNodes.addAll(getOnlyExceptionReturnStatement(jMethod));
            }
        } else {
            if (!jMethod.isHandleException()) {
                if (!jMethod.isArrayReturn()) {
                    if (jMethod.isStringReturn()) {
                        // string, no arrays, no errors
                        statementNodes.add(getStringReturnStatement(jMethod));
                    } else if (jMethod.isObjectReturn()) {
                        // object, no arrays, no errors
                        statementNodes.addAll(getObjectReturnStatements(jMethod));
                    } else {
                        // primitive, no arrays, no errors
                        statementNodes.add(getPrimitiveReturnStatement(jMethod));
                    }
                } else {
                    if (jMethod.isStringArrayReturn()) {
                        // string, has simple error
                        statementNodes.addAll(getStringArrayReturnStatements(jMethod));
                    } else if (jMethod.isObjectReturn()) {
                        // object, has simple error
                        statementNodes.addAll(getObjectArrayReturnStatements(jMethod));
                    } else {
                        // primitive,  has simple error
                        statementNodes.addAll(getPrimitiveArrayReturnStatements(jMethod));
                    }
                }
            } else {
                if (!jMethod.isArrayReturn()) {
                    if (jMethod.isStringReturn()) {
                        // string, no arrays, has user defined errors
                        statementNodes.addAll(getStringReturnWithException(jMethod));
                    } else if (jMethod.isObjectReturn()) {
                        // object, no arrays, has user defined errors
                        statementNodes.addAll(getObjectReturnWithException(jMethod));
                    } else {
                        // primitive, no arrays, has user defined errors
                        statementNodes.addAll(getPrimitiveReturnWithException(jMethod));
                    }
                } else {
                    if (jMethod.isStringArrayReturn()) {
                        // string, have arrays, has simple error and user defined errors
                        statementNodes.addAll(getStringArrayWithException(jMethod));
                    } else if (jMethod.isObjectReturn()) {
                        // object, have arrays, has simple error and user defined errors
                        statementNodes.addAll(getObjectArrayWithException(jMethod));
                    } else {
                        // primitive, have arrays, has simple error and user defined errors
                        statementNodes.addAll(getPrimitiveArrayWithException(jMethod));
                    }
                }
            }
        }

        return statementNodes;
    }

    private static ReturnStatementNode getPrimitiveReturnStatement(JMethod jMethod) {
        FunctionCallExpressionNode functionCallExpression = createFunctionCallExpressionNode(
                jMethod.getExternalFunctionName(), getParameterArgumentList(jMethod));
        return createReturnStatementNode(functionCallExpression);
    }

    private static ReturnStatementNode getStringReturnStatement(JMethod jMethod) {
        FunctionCallExpressionNode innerFunctionCall = createFunctionCallExpressionNode(
                jMethod.getExternalFunctionName(), getParameterArgumentList(jMethod));
        PositionalArgumentNode positionalArgNode = createPositionalArgumentNode(innerFunctionCall.toSourceCode());
        FunctionCallExpressionNode outerFunctionCall = createFunctionCallExpressionNode("java:toString",
                Collections.singletonList(positionalArgNode.toSourceCode()));

        return createReturnStatementNode(outerFunctionCall);
    }

    private static ExpressionStatementNode getNoReturnNoExceptionStatement(JMethod jMethod) {
        FunctionCallExpressionNode functionCallExpression = createFunctionCallExpressionNode(
                jMethod.getExternalFunctionName(), getParameterArgumentList(jMethod));
        return createExpressionStatementNode(functionCallExpression);
    }

    private static List<StatementNode> getPrimitiveArrayWithException(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement("handle|error", jMethod));
        statementNodes.add(createIfElseStatementNode(
                createBracedExpressionNode(createSimpleNameReferenceNode("externalObj is error")),
                getCheckExceptionBlock(jMethod.getExceptionName(), jMethod.getExceptionConstName()),
                createElseBlockNode(createBlockStatementNode(AbstractNodeFactory.createNodeList(
                        createReturnStatementNode(createTypeCastExpressionNode(jMethod.getReturnType(),
                                createCheckExpressionNode(createFunctionCallExpressionNode("jarrays:fromHandle",
                                        new LinkedList<>(Arrays.asList("externalObj", "\""
                                                + jMethod.getReturnTypeJava() + "\""))))))
                )))));

        return statementNodes;
    }

    private static List<StatementNode> getObjectArrayWithException(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement("handle|error", jMethod));
        statementNodes.add(createIfElseStatementNode(
                createBracedExpressionNode(createSimpleNameReferenceNode("externalObj is error")),
                getCheckExceptionBlock(jMethod.getExceptionName(), jMethod.getExceptionConstName()),
                createElseBlockNode(createBlockStatementNode(AbstractNodeFactory.createNodeList(
                        createVariableDeclarationNode(
                                createTypedBindingPatternNode(jMethod.getReturnType(), "newObj"),
                                createListConstructorExpressionNode(new LinkedList<>())),
                        createVariableDeclarationNode(
                                createTypedBindingPatternNode("handle[]", "anyObj"),
                                createTypeCastExpressionNode("handle[]",
                                        createCheckExpressionNode(createFunctionCallExpressionNode("jarrays:fromHandle",
                                                new LinkedList<>(Arrays.asList("externalObj", "\"handle\"")))))),
                        createVariableDeclarationNode(
                                createTypedBindingPatternNode("int", "count"),
                                createMethodCallExpressionNode(createSimpleNameReferenceNode("anyObj"),
                                        "length", new LinkedList<>())),
                        getObjectArrayPopulation(jMethod.getReturnComponentType()))))));
        statementNodes.add(createReturnStatementNode(createSimpleNameReferenceNode("newObj")));

        return statementNodes;
    }

    private static List<StatementNode> getStringArrayWithException(JMethod jMethod) {
        return List.of(
                //   handle externalObj = <call external method>;
                getExternalFunctionCallStatement("handle|error", jMethod),
                //   if java:isNull(externalObj) {
                //       return null;
                //   }
                createReturnIfHandleIsNullStatement("externalObj"),
                //  if (externalObj is error) {
                //      InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj,
                //           message = externalObj.message());
                //      return e;
                //  } else {
                //      return <string[]>check jarrays:fromHandle(externalObj, "string");
                //  }
                createIfElseStatementNode(
                    createBracedExpressionNode(
                        createSimpleNameReferenceNode("externalObj is error")
                    ),
                    getCheckExceptionBlock(jMethod.getExceptionName(), jMethod.getExceptionConstName()),
                    createElseBlockNode(
                        createBlockStatementNode(
                            AbstractNodeFactory.createNodeList(
                                createReturnStatementNode(
                                    createTypeCastExpressionNode(
                                        "string[]",
                                        createCheckExpressionNode(
                                            createFunctionCallExpressionNode(
                                                    "jarrays:fromHandle",
                                                    new LinkedList<>(Arrays.asList("externalObj", "\"string\""))
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            );
    }

    private static List<StatementNode> getPrimitiveReturnWithException(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement(jMethod.getReturnType() + "|error", jMethod));
        statementNodes.add(createIfElseStatementNode(
                createBracedExpressionNode(createSimpleNameReferenceNode("externalObj is error")),
                getCheckExceptionBlock(jMethod.getExceptionName(), jMethod.getExceptionConstName()),
                createElseBlockNode(createBlockStatementNode(AbstractNodeFactory.createNodeList(
                        createReturnStatementNode(createSimpleNameReferenceNode("externalObj")))))));

        return statementNodes;
    }

    private static List<StatementNode> getObjectReturnWithException(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement("handle|error", jMethod));
        statementNodes.add(createIfElseStatementNode(
                createBracedExpressionNode(createSimpleNameReferenceNode("externalObj is error")),
                getCheckExceptionBlock(jMethod.getExceptionName(), jMethod.getExceptionConstName()),
                createElseBlockNode(createBlockStatementNode(AbstractNodeFactory.createNodeList(
                        createVariableDeclarationNode(
                                createTypedBindingPatternNode(jMethod.getReturnType(), "newObj"),
                                createImplicitNewExpressionNode(Collections.singletonList("externalObj"))
                        ),
                        createReturnStatementNode(createSimpleNameReferenceNode("newObj")))))));

        return statementNodes;
    }

    private static List<StatementNode> getStringReturnWithException(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement("handle|error", jMethod));
        statementNodes.add(createIfElseStatementNode(
                createBracedExpressionNode(createSimpleNameReferenceNode("externalObj is error")),
                getCheckExceptionBlock(jMethod.getExceptionName(), jMethod.getExceptionConstName()),
                createElseBlockNode(createBlockStatementNode(AbstractNodeFactory.createNodeList(
                        createReturnStatementNode(createFunctionCallExpressionNode(
                                "java:toString", Collections.singletonList("externalObj"))))))));

        return statementNodes;
    }

    private static List<StatementNode> getObjectReturnStatements(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement(HANDLE, jMethod));
        statementNodes.add(createVariableDeclarationNode(createTypedBindingPatternNode(
                jMethod.getReturnType(), "newObj"),
                createImplicitNewExpressionNode(Collections.singletonList("externalObj"))));
        statementNodes.add(createReturnStatementNode(createSimpleNameReferenceNode("newObj")));

        return statementNodes;
    }

    private static List<StatementNode> getObjectArrayReturnStatements(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement(HANDLE, jMethod));
        statementNodes.add(createVariableDeclarationNode(
                createTypedBindingPatternNode(jMethod.getReturnType(), "newObj"),
                createListConstructorExpressionNode(new LinkedList<>())));
        statementNodes.add(createVariableDeclarationNode(
                createTypedBindingPatternNode("handle[]", "anyObj"),
                createTypeCastExpressionNode("handle[]",
                        createCheckExpressionNode(createFunctionCallExpressionNode("jarrays:fromHandle",
                        new LinkedList<>(Arrays.asList("externalObj", "\"handle\"")))))));
        statementNodes.add(createVariableDeclarationNode(
                createTypedBindingPatternNode("int", "count"),
                createMethodCallExpressionNode(createSimpleNameReferenceNode("anyObj"), "length", new LinkedList<>())));
        statementNodes.add(getObjectArrayPopulation(jMethod.getReturnComponentType()));
        statementNodes.add(createReturnStatementNode(createSimpleNameReferenceNode("newObj")));

        return statementNodes;
    }


    private static final Token OPEN_PAREN_TOKEN = NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
    private static final Token CLOSED_PAREN_TOKEN = NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);
    private static final Token SEMICOLON_TOKEN = NodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
    private static final Token COLON_TOKEN = NodeFactory.createToken(SyntaxKind.COLON_TOKEN);
    private static final MinutiaeList EMPTY_WHITE_SPACE = NodeFactory.createEmptyMinutiaeList();
    private static final MinutiaeList SINGLE_SPACE_WHITE_SPACE = NodeFactory.createMinutiaeList(
        NodeFactory.createWhitespaceMinutiae(" ")
    );
    private static final ExpressionNode NULL_TOKEN_EXPRESSION = NodeFactory.createSimpleNameReferenceNode(
        NodeFactory.createToken(SyntaxKind.NULL_KEYWORD)
    );

    private static NameReferenceNode createNameReferenceNode(String namespace, String name) {
        if (name == null || "".equals(name.trim())) {
            throw new IllegalArgumentException("name must not be null, blank, or empty");
        }
        if (namespace == null) {
            return NodeFactory.createSimpleNameReferenceNode(
                NodeFactory.createIdentifierToken(name)
            );
        } else {
            if (namespace.isBlank()) {
                throw new IllegalArgumentException("namespace must not be blank or empty");
            }
            return NodeFactory.createQualifiedNameReferenceNode(
                NodeFactory.createIdentifierToken(namespace),
                COLON_TOKEN,
                NodeFactory.createIdentifierToken(name)
            );
        }
    }

    private static NameReferenceNode createNameReferenceNode(String name) {
        return createNameReferenceNode(null /* no namespace */, name);
    }

    /**
     * Creates a {@link StatementNode} for the following ballerina block.
     * <code>
     *     if java:isNull(<handleName>) {
     *         return null;
     *     }
     * </code>
     * @param handleName the variable name of the handle
     * @return the ballerina block as statement node
     */
    private static StatementNode createReturnIfHandleIsNullStatement(final String handleName) {

        // builds the code fragment 'java:isNull(<handleName>)'
        final FunctionCallExpressionNode checkIsNullExpression = NodeFactory.createFunctionCallExpressionNode(
            createNameReferenceNode("java", "isNull"),
            OPEN_PAREN_TOKEN,
            NodeFactory.createSeparatedNodeList(
                NodeFactory.createPositionalArgumentNode(
                    createNameReferenceNode(handleName)
                )
            ),
            CLOSED_PAREN_TOKEN
        );

        // builds the code fragment 'return null;'
        final ReturnStatementNode returnStatement = NodeFactory.createReturnStatementNode(
            NodeFactory.createToken(
                SyntaxKind.RETURN_KEYWORD,
                EMPTY_WHITE_SPACE,       // no leading whitespace
                SINGLE_SPACE_WHITE_SPACE // one blank as trailing whitespace
            ),
            NULL_TOKEN_EXPRESSION,
            SEMICOLON_TOKEN
        );

        // builds the code fragment
        //   if java:isNull(<handleName>) {
        //       return null;
        //   }
        return NodeFactory.createIfElseStatementNode(
            NodeFactory.createToken(
                SyntaxKind.IF_KEYWORD,
                EMPTY_WHITE_SPACE,       // no leading whitespace
                SINGLE_SPACE_WHITE_SPACE // one blank as trailing whitespace
            ),
            // the expression
            checkIsNullExpression,
            // the if block
            createBlockStatementNode(
                NodeFactory.createNodeList(returnStatement)
            ),
            null // no else block
        );
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static List<StatementNode> getStringArrayReturnStatements(JMethod jMethod) {
        return List.of(
            //   handle externalObj = <call external method>;
            getExternalFunctionCallStatement(HANDLE, jMethod),
            //   if java:isNull(externalObj) {
            //       return null;
            //   }
            createReturnIfHandleIsNullStatement("externalObj"),
            //   return <string[]> check jarrays:fromHandle(externalObj, "string");
            createReturnStatementNode(createTypeCastExpressionNode("string[]",
                createCheckExpressionNode(
                    createFunctionCallExpressionNode(
                        "jarrays:fromHandle",
                        new LinkedList<>(Arrays.asList("externalObj", "\"string\""))
                    )
                )
            ))
        );
    }

    private static List<StatementNode> getPrimitiveArrayReturnStatements(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement(HANDLE, jMethod));
        statementNodes.add(createReturnStatementNode(createTypeCastExpressionNode(jMethod.getReturnType(),
                createCheckExpressionNode(createFunctionCallExpressionNode("jarrays:fromHandle",
                        new LinkedList<>(Arrays.asList("externalObj", "\"" + jMethod.getReturnTypeJava() + "\"")))))));

        return statementNodes;
    }

    private static List<StatementNode> getOnlyExceptionReturnStatement(JMethod jMethod) {
        List<StatementNode> statementNodes = new LinkedList<>();

        statementNodes.add(getExternalFunctionCallStatement("error|()", jMethod));
        statementNodes.add(createIfElseStatementNode(
                createBracedExpressionNode(createSimpleNameReferenceNode("externalObj is error")),
                getCheckExceptionBlock(jMethod.getExceptionName(), jMethod.getExceptionConstName()), null));

        return statementNodes;
    }

    private static ForEachStatementNode getObjectArrayPopulation(String returnShortName) {
        return createForEachStatementNode(
                createTypedBindingPatternNode("int", "i"),
                createBinaryExpressionNode("0", AbstractNodeFactory.createToken(SyntaxKind.ELLIPSIS_TOKEN),
                        "count - 1"),
                createBlockStatementNode(AbstractNodeFactory.createNodeList(
                        createVariableDeclarationNode(
                                createTypedBindingPatternNode(returnShortName, "element"),
                                createImplicitNewExpressionNode(Collections.singletonList("anyObj[i]"))),
                        createAssignmentStatementNode(
                                createSimpleNameReferenceNode("newObj[i]"),
                                createSimpleNameReferenceNode("element"))
                )));
    }

    private static BlockStatementNode getCheckExceptionBlock(String exceptionName, String exceptionConstName) {
        return createBlockStatementNode(AbstractNodeFactory.createNodeList(
                createVariableDeclarationNode(
                        createTypedBindingPatternNode(exceptionName, "e"),
                        createErrorConstructorExpressionNode(
                                createSimpleNameReferenceNode(exceptionName),
                                new LinkedList<>(Arrays.asList(exceptionConstName, "externalObj",
                                        "message = externalObj.message()")))
                ),
                createReturnStatementNode(createSimpleNameReferenceNode("e")))
        );
    }

    private static StatementNode getExternalFunctionCallStatement(String returnType, BFunction bFunction) {
        FunctionCallExpressionNode innerFunctionCall = createFunctionCallExpressionNode(
                bFunction.getExternalFunctionName(), getParameterArgumentList(bFunction));
        return createVariableDeclarationNode(createTypedBindingPatternNode(returnType,
                "externalObj"), innerFunctionCall);
    }

    private static List<String> getParameterArgumentList(BFunction bFunction) {
        List<String> argValues = new LinkedList<>();
        if (!bFunction.isStatic()) {
            argValues.add("self.jObj");
        }
        for (JParameter jParameter : bFunction.getParameters()) {
            if (jParameter.getIsString()) {
                argValues.add("java:fromString(" + jParameter.getFieldName() + ")");
            } else if (jParameter.isArray()) {
                argValues.add("check jarrays:toHandle(" + jParameter.getFieldName() + ", \"" +
                        jParameter.getComponentType() + "\")");
            } else if (jParameter.getIsObj()) {
                argValues.add(jParameter.getFieldName() + ".jObj");
            } else {
                argValues.add(jParameter.getFieldName());
            }
        }
        return argValues;
    }

    /**
     * Creates a markdown documentation line node using the parameter name and the documentation elements provided.
     */
    private static MarkdownParameterDocumentationLineNode createMarkdownParameterDocumentationLineNode(
            String paramName, String content) {
        Token hashToken = AbstractNodeFactory.createToken(SyntaxKind.HASH_TOKEN, emptyML(), emptyML());
        Token plusToken = AbstractNodeFactory.createToken(SyntaxKind.PLUS_TOKEN);
        Token parameterName = AbstractNodeFactory.createLiteralValueToken(SyntaxKind.PARAMETER_NAME, paramName,
                singleWSML(), singleWSML());
        LiteralValueToken documentElements = AbstractNodeFactory.createLiteralValueToken(
                SyntaxKind.DOCUMENTATION_DESCRIPTION, content, emptyML(), singleNLML());
        Token minusToken = AbstractNodeFactory.createToken(SyntaxKind.MINUS_TOKEN);

        return NodeFactory.createMarkdownParameterDocumentationLineNode(null, hashToken, plusToken,
                parameterName, minusToken, AbstractNodeFactory.createNodeList(documentElements));
    }

    /**
     * Creates a markdown documentation line node using the documentation elements provided.
     */
    private static MarkdownDocumentationLineNode createMarkdownDocumentationLineNode(String content) {
        Token hashToken = AbstractNodeFactory.createToken(SyntaxKind.HASH_TOKEN, emptyML(), singleWSML());
        LiteralValueToken documentElements = AbstractNodeFactory.createLiteralValueToken(
                SyntaxKind.DOCUMENTATION_DESCRIPTION, content, emptyML(), singleNLML());

        return NodeFactory.createMarkdownDocumentationLineNode(null, hashToken,
                AbstractNodeFactory.createNodeList(documentElements));
    }

    /**
     * Creates a markdown documentation node using the documentation string node list provided.
     */
    private static MarkdownDocumentationNode createMarkdownDocumentationNode(NodeList<Node> documentationString) {
        return NodeFactory.createMarkdownDocumentationNode(documentationString);
    }

    /**
     * Creates a metadata node using the documentation string and annotations provided.
     */
    private static MetadataNode createMetadataNode(Node documentationString, NodeList<AnnotationNode> annotations) {
        return NodeFactory.createMetadataNode(documentationString, annotations);
    }

    /**
     * Creates a error constructor expression node using the details provided.
     */
    private static ErrorConstructorExpressionNode createErrorConstructorExpressionNode(TypeDescriptorNode typeReference,
                                                                                       List<String> argList) {
        Token errorKeyword = AbstractNodeFactory.createToken(SyntaxKind.ERROR_KEYWORD, emptyML(), singleWSML());
        Token openParenToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        List<Node> argumentNodes = new LinkedList<>();
        for (String arg : argList) {
            argumentNodes.add(createPositionalArgumentNode(arg));
            argumentNodes.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        if (argumentNodes.size() > 1) {
            argumentNodes.remove(argumentNodes.size() - 1);
        }
        SeparatedNodeList<FunctionArgumentNode> arguments = AbstractNodeFactory.createSeparatedNodeList(argumentNodes);
        Token closeParenToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        return NodeFactory.createErrorConstructorExpressionNode(errorKeyword, typeReference,
                openParenToken, arguments, closeParenToken);
    }

    /**
     * Creates a assignment statement node using the details provided.
     */
    private static AssignmentStatementNode createAssignmentStatementNode(Node varRef, ExpressionNode expression) {
        Token equalsToken = AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN, singleWSML(), singleWSML());
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createAssignmentStatementNode(varRef, equalsToken, expression, semicolonToken);
    }

    /**
     * Creates a binary expression node using the details provided.
     */
    private static BinaryExpressionNode createBinaryExpressionNode(String lhs, Token operator, String rhs) {
        Node lhsExpr = createSimpleNameReferenceNode(lhs);
        Node rhsExpr = createSimpleNameReferenceNode(rhs);

        return NodeFactory.createBinaryExpressionNode(null, lhsExpr, operator, rhsExpr);
    }

    /**
     * Creates a foreach statement node using the details provided.
     */
    private static ForEachStatementNode createForEachStatementNode(TypedBindingPatternNode typedBindingPattern,
                                                                   Node actionOrExpressionNode,
                                                                   BlockStatementNode blockStatement) {
        Token forEachKeyword = AbstractNodeFactory.createToken(SyntaxKind.FOREACH_KEYWORD, emptyML(), singleWSML());
        Token inKeyword = AbstractNodeFactory.createToken(SyntaxKind.IN_KEYWORD, singleWSML(), singleWSML());

        return NodeFactory.createForEachStatementNode(forEachKeyword, typedBindingPattern, inKeyword,
                actionOrExpressionNode, blockStatement, null);
    }

    /**
     * Creates a return statement node using the expression, method name and argument list node provided.
     */
    private static MethodCallExpressionNode createMethodCallExpressionNode(ExpressionNode expression,
                                                                           String methodNameValue,
                                                                           List<String> argList) {
        Token dotToken = AbstractNodeFactory.createToken(SyntaxKind.DOT_TOKEN);
        NameReferenceNode methodName = createSimpleNameReferenceNode(methodNameValue);
        Token openParenToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        List<Node> argListNode = new LinkedList<>();
        for (String arg : argList) {
            argListNode.add(createPositionalArgumentNode(arg));
            argListNode.add(createPositionalArgumentNode(","));
        }
        if (argListNode.size() > 1) {
            argListNode.remove(argListNode.size() - 1);
        }
        SeparatedNodeList<FunctionArgumentNode> arguments = AbstractNodeFactory.createSeparatedNodeList(argListNode);
        Token closeParenToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        return NodeFactory.createMethodCallExpressionNode(expression, dotToken, methodName, openParenToken,
                arguments, closeParenToken);
    }

    /**
     * Creates a return statement node using the expression node provided.
     */
    private static ReturnStatementNode createReturnStatementNode(ExpressionNode expression) {
        Token returnKeyword = AbstractNodeFactory.createToken(SyntaxKind.RETURN_KEYWORD, emptyML(), singleWSML());
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createReturnStatementNode(returnKeyword, expression, semicolonToken);
    }

    /**
     * Creates a variable declaration node using the typed binding pattern and the expression node provided.
     */
    private static VariableDeclarationNode createVariableDeclarationNode(TypedBindingPatternNode typedBindingPattern,
                                                                         ExpressionNode initializer) {
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createNodeList();
        Token equalsToken = AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN);
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createVariableDeclarationNode(annotations, null, typedBindingPattern,
                equalsToken, initializer, semicolonToken);
    }

    /**
     * Creates a type cast expression node using the string type and the expression node provided.
     */
    private static TypeCastExpressionNode createTypeCastExpressionNode(String type, ExpressionNode expression) {
        Token ltToken = AbstractNodeFactory.createToken(SyntaxKind.LT_TOKEN);
        TypeCastParamNode typeCastParam = createTypeCastParamNode(type);
        Token gtToken = AbstractNodeFactory.createToken(SyntaxKind.GT_TOKEN);

        return NodeFactory.createTypeCastExpressionNode(ltToken, typeCastParam, gtToken, expression);
    }

    /**
     * Creates a type cast param node using the string type provided.
     */
    private static TypeCastParamNode createTypeCastParamNode(String type) {
        NodeList<AnnotationNode> annotationNodes = AbstractNodeFactory.createNodeList();
        Node typeNode = createSimpleNameReferenceNode(type);

        return NodeFactory.createTypeCastParamNode(annotationNodes, typeNode);
    }

    /**
     * Creates an implicit new expression node using the string argument list provided.
     */
    private static ImplicitNewExpressionNode createImplicitNewExpressionNode(List<String> argList) {
        Token newToken = AbstractNodeFactory.createToken(SyntaxKind.NEW_KEYWORD);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(argList);

        return NodeFactory.createImplicitNewExpressionNode(newToken, parenthesizedArgList);
    }

    /**
     * Creates a check expression node using the expression node provided.
     */
    private static CheckExpressionNode createCheckExpressionNode(ExpressionNode expressionNode) {
        Token checkKeyword = AbstractNodeFactory.createToken(SyntaxKind.CHECK_KEYWORD, emptyML(), singleWSML());

        return NodeFactory.createCheckExpressionNode(null, checkKeyword, expressionNode);
    }

    /**
     * Creates an else block node using the else body statement node provided.
     */
    private static ElseBlockNode createElseBlockNode(StatementNode elseBody) {
        Token elseKeyword = AbstractNodeFactory.createToken(SyntaxKind.ELSE_KEYWORD, emptyML(), singleWSML());

        return NodeFactory.createElseBlockNode(elseKeyword, elseBody);
    }

    /**
     * Creates an if else statement node using the condition, if body and the else body provided.
     */
    private static IfElseStatementNode createIfElseStatementNode(ExpressionNode condition, BlockStatementNode ifBody,
                                                                 Node elseBody) {
        Token ifKeyword = AbstractNodeFactory.createToken(SyntaxKind.IF_KEYWORD, emptyML(), singleWSML());
        return NodeFactory.createIfElseStatementNode(ifKeyword, condition, ifBody, elseBody);
    }

    /**
     * Creates a block statement node using the statement nodes provided.
     */
    private static BlockStatementNode createBlockStatementNode(NodeList<StatementNode> statementNodes) {
        Token openBraceToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
        Token closeBraceToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);

        return NodeFactory.createBlockStatementNode(openBraceToken, statementNodes, closeBraceToken);
    }

    /**
     * Creates a braced expression node using the expression node provided.
     */
    private static BracedExpressionNode createBracedExpressionNode(ExpressionNode expressionNode) {
        Token openParenToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        Token closeParenToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        return NodeFactory.createBracedExpressionNode(null, openParenToken, expressionNode, closeParenToken);
    }

    /**
     * Creates a parenthesized argument list using the argument list provided.
     */
    private static ParenthesizedArgList createParenthesizedArgList(List<String> argList) {
        Token openParenToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        List<Node> argumentNodeList = new LinkedList<>();
        for (String arg : argList) {
            argumentNodeList.add(createPositionalArgumentNode(arg));
            argumentNodeList.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        if (argumentNodeList.size() > 1) {
            argumentNodeList.remove(argumentNodeList.size() - 1);
        }
        SeparatedNodeList<FunctionArgumentNode> arguments = AbstractNodeFactory
                .createSeparatedNodeList(argumentNodeList);
        Token closeParenToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        return NodeFactory.createParenthesizedArgList(openParenToken, arguments, closeParenToken);
    }

    /**
     * Creates a function call expression node using the function name and the argument list provided.
     */
    private static FunctionCallExpressionNode createFunctionCallExpressionNode(String functionNameValue,
                                                                               List<String> argValues) {
        Token openParenToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        NameReferenceNode functionName = createSimpleNameReferenceNode(functionNameValue);
        SeparatedNodeList<FunctionArgumentNode> argumentNodeList;
        List<Node> argList = new LinkedList<>();
        for (String arg : argValues) {
            argList.add(createPositionalArgumentNode(arg));
            argList.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        if (argList.size() > 1) {
            argList.remove(argList.size() - 1);
        }
        argumentNodeList = AbstractNodeFactory.createSeparatedNodeList(argList);
        Token closeParenToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        return NodeFactory.createFunctionCallExpressionNode(functionName, openParenToken,
                argumentNodeList, closeParenToken);
    }

    /**
     * Creates a positional argument node using the string name provided.
     */
    private static PositionalArgumentNode createPositionalArgumentNode(String name) {
        ExpressionNode expression = createSimpleNameReferenceNode(name);

        return NodeFactory.createPositionalArgumentNode(expression);
    }

    /**
     * Creates a simple name reference node using the string name provided.
     */
    private static SimpleNameReferenceNode createSimpleNameReferenceNode(String name) {
        IdentifierToken nameReference = AbstractNodeFactory.createIdentifierToken(name);

        return NodeFactory.createSimpleNameReferenceNode(nameReference);
    }

    /**
     * Creates a typed binding pattern node using the string type and the string variable name provided.
     */
    private static TypedBindingPatternNode createTypedBindingPatternNode(String type, String variableName) {
        TypeDescriptorNode typeDescriptor = createSimpleNameReferenceNode(type);
        BindingPatternNode bindingPattern = createCaptureBindingPatternNode(variableName);

        return NodeFactory.createTypedBindingPatternNode(typeDescriptor, bindingPattern);
    }

    /**
     * Creates a capture binding pattern node using the string identifier provided.
     */
    private static CaptureBindingPatternNode createCaptureBindingPatternNode(String identifier) {
        Token variableName = AbstractNodeFactory.createIdentifierToken(identifier, singleWSML(), singleWSML());

        return NodeFactory.createCaptureBindingPatternNode(variableName);
    }

    /**
     * Creates an annotation node using the string annotation name and the field map provided.
     */
    private static AnnotationNode createAnnotationNode(String annotation, Map<String, List<String>> fields) {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);
        SimpleNameReferenceNode nameReference = createSimpleNameReferenceNode(annotation);
        MappingConstructorExpressionNode mappingConstructor = createMappingConstructorExpressionNode(fields);

        return NodeFactory.createAnnotationNode(atToken, nameReference, mappingConstructor);
    }

    /**
     * Creates a mapping constructor expression node using the field map provided.
     */
    private static MappingConstructorExpressionNode createMappingConstructorExpressionNode(
            Map<String, List<String>> fields) {
        Token openBraceToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN, emptyML(), singleNLML());
        Token closeBraceToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        List<Node> mappingFields = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : fields.entrySet()) {
            List<String> fieldValues = entry.getValue();
            mappingFields.add(createSpecificFieldNode(entry.getKey(), fieldValues));
            mappingFields.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        if (mappingFields.size() > 1) {
            mappingFields.remove(mappingFields.size() - 1);
        }
        SeparatedNodeList<MappingFieldNode> fieldsNodeList = AbstractNodeFactory.createSeparatedNodeList(mappingFields);

        return NodeFactory.createMappingConstructorExpressionNode(openBraceToken, fieldsNodeList, closeBraceToken);
    }

    /**
     * Creates a specific field node using the field name and the list of string values provided.
     */
    private static SpecificFieldNode createSpecificFieldNode(String name, List<String> value) {
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(name);
        Token colonToken = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        ExpressionNode expressionNode = null;
        if (value.size() > 1 || name.equals("paramTypes")) {
            expressionNode = createListConstructorExpressionNode(value);
        } else if (!value.isEmpty()) {
            expressionNode = createBasicLiteralNode(value.get(0));
        }

        return NodeFactory.createSpecificFieldNode(null, fieldName, colonToken, expressionNode);
    }

    /**
     * Creates a list constructor expression node using the list of strings provided.
     */
    private static ListConstructorExpressionNode createListConstructorExpressionNode(List<String> list) {
        Token openBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        List<Node> listElements = new LinkedList<>();
        for (String element : list) {
            listElements.add(createBasicLiteralNode(element));
            listElements.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        if (listElements.size() > 1) {
            listElements.remove(listElements.size() - 1);
        }
        SeparatedNodeList<Node> expressions = AbstractNodeFactory.createSeparatedNodeList(listElements);

        return NodeFactory.createListConstructorExpressionNode(openBracketToken, expressions, closeBracketToken);
    }

    /**
     * Creates an expression statement node using the expression node provided.
     */
    private static ExpressionStatementNode createExpressionStatementNode(ExpressionNode expression) {
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createExpressionStatementNode(null, expression, semicolonToken);
    }

    /**
     * Creates a basic literal node using the string value provided.
     */
    private static BasicLiteralNode createBasicLiteralNode(String value) {
        Token valueToken = AbstractNodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                "\"" + value + "\"", emptyML(), emptyML());
        return NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, valueToken);
    }

    /**
     * Creates a required parameter node using the string type and the string parameter provided.
     */
    private static RequiredParameterNode createRequiredParameterNode(String type, String param) {
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createNodeList();
        Node typeName = createSimpleNameReferenceNode(type);
        IdentifierToken paramName = AbstractNodeFactory.createIdentifierToken(param, singleWSML(), emptyML());

        return NodeFactory.createRequiredParameterNode(annotations, typeName, paramName);
    }

    /**
     * Creates a return type descriptor node from the provided type descriptor node.
     */
    private static ReturnTypeDescriptorNode createReturnTypeDescriptorNode(
            TypeDescriptorNode returnTypeDescriptorNode) {
        Token returnsKeyword = AbstractNodeFactory.createToken(SyntaxKind.RETURNS_KEYWORD, emptyML(), singleWSML());
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createNodeList();

        return NodeFactory.createReturnTypeDescriptorNode(returnsKeyword, annotations, returnTypeDescriptorNode);
    }

    /**
     * Retrieve a single whitespace minutiae list.
     */
    private static MinutiaeList singleWSML() {
        return emptyML().add(AbstractNodeFactory.createWhitespaceMinutiae(" "));
    }

    /**
     * Retrieve a single new line minutiae list.
     */
    private static MinutiaeList singleNLML() {
        String newLine = System.getProperty("line.separator");
        return emptyML().add(AbstractNodeFactory.createEndOfLineMinutiae(newLine));
    }

    /**
     * Retrieve two new line minutiaes as a minutiae list.
     */
    private static MinutiaeList doubleNLML() {
        String newLine = System.getProperty("line.separator") + System.getProperty("line.separator");
        return emptyML().add(AbstractNodeFactory.createEndOfLineMinutiae(newLine));
    }

    /**
    * Retrieve an empty minutiae list.
    */
    private static MinutiaeList emptyML() {
        return AbstractNodeFactory.createEmptyMinutiaeList();
    }

    /**
     * Converts a string list into a collection of tokens.
     */
    private static Collection<Node> getTokenList(List<String> stringList) {
        List<Node> tokenList = new LinkedList<>();
        for (String value : stringList) {
            tokenList.add(AbstractNodeFactory.createIdentifierToken(value));
        }
        return tokenList;
    }

    /**
     * Returns a list of quoted parameters separated by commas once the JParameter list is provided.
     */
    private static List<String> getParameterList(List<JParameter> jParameterList) {
        List<String> paramList = new ArrayList<>();
        for (JParameter jparameter : jParameterList) {
            paramList.add(jparameter.getType());
        }
        return paramList;
    }
}

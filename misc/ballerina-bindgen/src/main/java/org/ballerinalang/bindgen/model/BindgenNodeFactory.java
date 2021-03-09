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
package org.ballerinalang.bindgen.model;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.bindgen.utils.BindgenConstants.CLASS;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JAVA;
import static org.ballerinalang.bindgen.utils.BindgenConstants.NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.PARAM_TYPES;

/**
 * Class for generating the Ballerina syntax tree util nodes and tokens.
 *
 * @since 2.0.0
 */
public class BindgenNodeFactory {

    /**
     * Create an import declaration name node while providing the organization name, optional prefix, and module names.
     *
     * @param orgNameValue - the organization name
     * @param prefixValue - an optional prefix value
     * @param moduleNames - list of module names with separators
     */
    public static ImportDeclarationNode createImportDeclarationNode(String orgNameValue, String prefixValue,
                                                                    List<String> moduleNames) {
        Token importKeyword = AbstractNodeFactory.createToken(SyntaxKind.IMPORT_KEYWORD, emptyML(), singleWSML());
        ImportOrgNameNode orgName = createImportOrgNameNode(orgNameValue);
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
     *
     * @param orgNameValue - the organization name
     */
    private static ImportOrgNameNode createImportOrgNameNode(String orgNameValue) {
        Token orgName = AbstractNodeFactory.createIdentifierToken(orgNameValue);
        Token slashToken = AbstractNodeFactory.createToken(SyntaxKind.SLASH_TOKEN);

        return NodeFactory.createImportOrgNameNode(orgName, slashToken);
    }

    /**
     * Create an import prefix node while providing the prefix.
     *
     * @param prefixValue - the prefix value
     */
    private static ImportPrefixNode createImportPrefixNode(String prefixValue) {
        Token asKeyword = AbstractNodeFactory.createToken(SyntaxKind.AS_KEYWORD, singleWSML(), singleWSML());
        Token prefix = AbstractNodeFactory.createIdentifierToken(prefixValue);

        return NodeFactory.createImportPrefixNode(asKeyword, prefix);
    }

    /**
     * Create a function definition node for a specific java method.
     *
     * @param bFunction - Java method for which the Ballerina function definition node is to be created.
     * @param isExternal - Specifies if the external function needs to be created instead of the mapping function.
     */
    public static FunctionDefinitionNode createFunctionDefinitionNode(BFunction bFunction,
                                                                      boolean isExternal) {
        MetadataNode metadata = null; // TODO: implement the markdown documentation

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

    /*
     * Create a function signature node while providing the function name.
     * */
    private static FunctionSignatureNode createFunctionSignatureNode(BFunction bFunction,
                                                                     boolean isExternal) {
        Token openParenToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        Token closeParenToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);
        List<Node> parameterNodes = new LinkedList<>();
        if (isExternal) {
            parameterNodes.add(createRequiredParameterNode("handle", "receiver", false));
        }
        for (JParameter jParameter : bFunction.getParameters()) {
            parameterNodes.add(createRequiredParameterNode(jParameter.getShortTypeName(), jParameter.getFieldName(),
                    jParameter.isArray()));
            parameterNodes.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        if (parameterNodes.size() > 1) {
            parameterNodes.remove(parameterNodes.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameters = AbstractNodeFactory.createSeparatedNodeList(parameterNodes);
        ReturnTypeDescriptorNode returnTypeDescriptor = null;
        String returnType = isExternal ? bFunction.getExternalType() : bFunction.getReturnType();
        if (returnType != null || bFunction.isHandleException()) {
            returnTypeDescriptor = createReturnTypeDescriptorNode(returnType, bFunction.isArrayReturn(),
                    (!bFunction.isStatic() && isExternal));
        }

        return NodeFactory.createFunctionSignatureNode(openParenToken, parameters, closeParenToken,
                returnTypeDescriptor);
    }

    /**
     * Create an external function body node for a specific `JFunction` provided.
     *
     * @param bFunction - Java method for which the Ballerina external function is to be created.
     */
    private static ExternalFunctionBodyNode createExternalFunctionBodyNode(BFunction bFunction) {
        Map<String, List<String>> fields = new LinkedHashMap<>();

        if (bFunction.getKind() == BFunction.BFunctionKind.METHOD) {
            JMethod jMethod = (JMethod) bFunction;
            fields.put(NAME, Collections.singletonList(jMethod.getMethodName()));
            fields.put(CLASS, Collections.singletonList(jMethod.getDeclaringClass().getName()));
            fields.put(PARAM_TYPES, getParameterList(jMethod.getParameters()));

        } else if (bFunction.getKind() == BFunction.BFunctionKind.CONSTRUCTOR) {
            JConstructor jConstructor = (JConstructor) bFunction;
            fields.put(CLASS, Collections.singletonList(jConstructor.getDeclaringClass().getName()));
            fields.put(PARAM_TYPES, getParameterList(jConstructor.getParameters()));

        } else if (bFunction.getKind() == BFunction.BFunctionKind.FIELD_GET ||
                bFunction.getKind() == BFunction.BFunctionKind.FIELD_SET) {
            JField jField = (JField) bFunction;
            fields.put(CLASS, Collections.singletonList(jField.getDeclaringClass().getName()));
            fields.put(PARAM_TYPES, getParameterList(Collections.singletonList(jField.getFieldObj())));
        }

        Token equalsToken = AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN);
        NodeList<AnnotationNode> annotations = AbstractNodeFactory
                .createNodeList(createAnnotationNode(bFunction.getKind(), fields));
        Token externalToken = AbstractNodeFactory.createToken(SyntaxKind.EXTERNAL_KEYWORD);
        Token semiColonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createExternalFunctionBodyNode(equalsToken, annotations, externalToken, semiColonToken);
    }

    private static List<String> statementTypes(ReturnTypes returnType) {
        List<String> statementTypesList = new LinkedList<>();
        switch (returnType) {
            case OPTIONAL_ERROR:
                break;
            case ERROR:
                break;
            case PRIMITIVE:
                break;
            case OBJECT:
                statementTypesList.add("getHandleObj");
                statementTypesList.add("createNewObj");
                statementTypesList.add("objReturn");
                break;
            case PRIMITIVE_ERROR:
                break;
            case OBJECT_ERROR:
                break;
        }
        return statementTypesList;
    }

    private enum ReturnTypes {
        OPTIONAL_ERROR,
        ERROR,
        PRIMITIVE,
        OBJECT,
        PRIMITIVE_ERROR,
        OBJECT_ERROR
    }

    private static FunctionBodyBlockNode createFunctionBodyBlockNode(BFunction bFunction) {
        Token openBraceToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
        List<StatementNode> statementNodes = new LinkedList<>();
        if (!bFunction.isStatic()) {

        }
        for (String entry : statementTypes) {
            if (entry.equals("getHandleObj") || entry.equals("createNewObj")) {
                statementNodes.add(createVariableDeclarationNode(entry, bFunction));
            } else if (entry.equals("objReturn")) {
                statementNodes.add(createReturnStatementNode("objReturn"));
            }
        }
        NodeList<StatementNode> statements = AbstractNodeFactory.createNodeList(statementNodes);
        Token closeBraceToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);

        return NodeFactory.createFunctionBodyBlockNode(openBraceToken, null, statements, closeBraceToken);
    }

    private static ReturnStatementNode createReturnStatementNode(String type) {
        Token returnKeyword = AbstractNodeFactory.createToken(SyntaxKind.RETURN_KEYWORD, emptyML(),
                singleWSML());
        ExpressionNode expression = null; // TODO: remove this null return
        if (type.equals("objReturn")) {
            expression = createSimpleNameReferenceNode("obj");
        }
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createReturnStatementNode(returnKeyword, expression, semicolonToken);
    }

    private static VariableDeclarationNode createVariableDeclarationNode(String type, JMethod jMethod) {
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createNodeList();
        TypedBindingPatternNode typedBindingPattern;
        Token equalsToken = AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN);
        ExpressionNode initializer;
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        if (type.equals("getHandleObj")) {
            typedBindingPattern = createTypedBindingPatternNode("handle", true, "externalObj");
            initializer = createFunctionCallExpressionNode(jMethod.getExternalFunctionName(),
                    jMethod.getExternalFunctionCallArguments());

            return NodeFactory.createVariableDeclarationNode(annotations, null, typedBindingPattern,
                    equalsToken, initializer, semicolonToken);
        } else if (type.equals("createNewObj")) {
            typedBindingPattern = createTypedBindingPatternNode(jMethod.getReturnType(), false, "obj");
            initializer = createImplicitNewExpressionNode(new LinkedList<>(Collections.singletonList("externalObj")));

            return NodeFactory.createVariableDeclarationNode(annotations, null, typedBindingPattern,
                    equalsToken, initializer, semicolonToken);
        }
        // TODO: handle this without returning a null value
        return null;
    }

    private static ImplicitNewExpressionNode createImplicitNewExpressionNode(List<String> argList) {
        Token newToken = AbstractNodeFactory.createToken(SyntaxKind.NEW_KEYWORD);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(argList);

        return NodeFactory.createImplicitNewExpressionNode(newToken, parenthesizedArgList);
    }

//    private static CheckExpressionNode createCheckExpressionNode(String expressionType) throws BindgenException {
//        Token checkKeyword = AbstractNodeFactory.createToken(SyntaxKind.CHECK_KEYWORD);
//        ExpressionNode expressionNode = null;
//        if (expressionType.equals("jarrays_to_handle")) {
//            // TODO
//        } else if (expressionType.equals("jarrays_to_handle")) {
//            // TODO
//        } else {
//            throw new BindgenException("error: unable to create the check expression node");
//        }
//
//        return NodeFactory.createCheckExpressionNode(null, checkKeyword, expressionNode);
//    }

//    public static IfElseStatementNode createIfElseStatementNode(String expression,
//                                                                String type) throws BindgenException {
//        Token ifKeyword = AbstractNodeFactory.createToken(SyntaxKind.IF_KEYWORD);
//        ExpressionNode condition = createBracedExpressionNode("typetest", expression, type);
//        BlockStatementNode ifBody =
//        Node elseBody
//    }
//
//    public static BlockStatementNode createBlockStatementNode() {
//
//    }

//    public static BracedExpressionNode createBracedExpressionNode(String expressionType, String expression,
//                                                                  String type) throws BindgenException {
//        Token openParenToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
//        ExpressionNode expressionNode;
//        if (expressionType.equals("typetest")) {
//            expressionNode = createTypeTestExpressionNode("simpleName", expression, type);
//        } else {
//            throw new BindgenException("error: unhandled expression type kind found `" + type + "`");
//        }
//        Token closeParenToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);
//
//        return NodeFactory.createBracedExpressionNode(null, openParenToken, expressionNode, closeParenToken);
//    }

//    public static TypeTestExpressionNode createTypeTestExpressionNode(String expressionType, String expression,
//                                                                      String type) throws BindgenException {
//        ExpressionNode expressionNode = null;
//        if (expressionType.equals("simpleName")) {
//            expressionNode = createSimpleNameReferenceNode(expression);
//        } else {
//            throw new BindgenException("error: unhandled expression kind found `" + expressionType + "`");
//        }
//        Token isKeyword = AbstractNodeFactory.createToken(SyntaxKind.IS_KEYWORD, singleWSML(),
//                singleWSML());
//        Node typeDescriptorNode;
//        if (type.equals("error")) {
//            typeDescriptorNode = NodeFactory.createErrorTypeDescriptorNode(AbstractNodeFactory
//                    .createToken(SyntaxKind.ERROR_KEYWORD), null);
//        } else {
//            throw new BindgenException("error: unhandled type descriptor kind found `" + type + "`");
//        }
//
//        return NodeFactory.createTypeTestExpressionNode(expressionNode, isKeyword, typeDescriptorNode);
//    }

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

    private static FunctionCallExpressionNode createFunctionCallExpressionNode(String functionName,
                                                                               List<String> arguments) {
        NameReferenceNode functionNameToken = createSimpleNameReferenceNode(functionName);
        Token openParenToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        List<Node> argList = new LinkedList<>();
        for (String arg : arguments) {
            argList.add(createPositionalArgumentNode(arg));
            argList.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        if (argList.size() > 1) {
            argList.remove(argList.size() - 1);
        }
        SeparatedNodeList<FunctionArgumentNode> argumentNodeList = AbstractNodeFactory.createSeparatedNodeList(argList);
        Token closeParenToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        return NodeFactory.createFunctionCallExpressionNode(functionNameToken, openParenToken,
                argumentNodeList, closeParenToken);
    }

    private static PositionalArgumentNode createPositionalArgumentNode(String name) {
        ExpressionNode expression = createSimpleNameReferenceNode(name);

        return NodeFactory.createPositionalArgumentNode(expression);
    }

    private static SimpleNameReferenceNode createSimpleNameReferenceNode(String name) {
        IdentifierToken nameReference = AbstractNodeFactory.createIdentifierToken(name);

        return NodeFactory.createSimpleNameReferenceNode(nameReference);
    }

    private static TypedBindingPatternNode createTypedBindingPatternNode(String type, boolean isBuiltInType,
                                                                         String variableName) {
        TypeDescriptorNode typeDescriptor;
        if (isBuiltInType) {
            typeDescriptor = createBuiltinSimpleNameReferenceNode(type);
        } else {
            typeDescriptor = createSimpleNameReferenceNode(type);
        }
        BindingPatternNode bindingPattern = createCaptureBindingPatternNode(variableName);

        return NodeFactory.createTypedBindingPatternNode(typeDescriptor, bindingPattern);
    }

    private static CaptureBindingPatternNode createCaptureBindingPatternNode(String identifier) {
        Token variableName = AbstractNodeFactory.createIdentifierToken(identifier);

        return NodeFactory.createCaptureBindingPatternNode(variableName);
    }

    private static List<String> getParameterList(List<JParameter> jParameterList) {
        List<String> paramList = new ArrayList<>();
        for (JParameter jparameter : jParameterList) {
            paramList.add(jparameter.getType());
        }
        return paramList;
    }

    private static AnnotationNode createAnnotationNode(BFunction.BFunctionKind functionKind, Map<String, List<String>> fields) {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);
        QualifiedNameReferenceNode nameReference = createQualifiedNameReferenceNode(JAVA, functionKind.value());
        MappingConstructorExpressionNode mappingConstructor = createMappingConstructorExpressionNode(fields);

        return NodeFactory.createAnnotationNode(atToken, nameReference, mappingConstructor);
    }

    private static MappingConstructorExpressionNode createMappingConstructorExpressionNode(
            Map<String, List<String>> fields) {
        Token openBraceToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
        Token closeBraceToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        List<Node> mappingFields = new LinkedList<>();
        for (Map.Entry<String, List<String>> entry : fields.entrySet()) {
            List<String> fieldValues = entry.getValue();
            mappingFields.add(createSpecificFieldNode(entry.getKey(), fieldValues));
            mappingFields.add(AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }
        mappingFields.remove(mappingFields.size() - 1);
        SeparatedNodeList<MappingFieldNode> fieldsNodeList = AbstractNodeFactory.createSeparatedNodeList(mappingFields);

        return NodeFactory.createMappingConstructorExpressionNode(openBraceToken, fieldsNodeList, closeBraceToken);
    }

    private static SpecificFieldNode createSpecificFieldNode(String name, List<String> value) {
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(name);
        Token colonToken = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        ExpressionNode expressionNode;
        if (name.equals("paramTypes")) {
            expressionNode = createListConstructorExpressionNode(value);
        } else if (!value.isEmpty()) {
            expressionNode = createBasicLiteralNode(value.get(0));
        } else {
            expressionNode = createBasicLiteralNode("");
        }

        return NodeFactory.createSpecificFieldNode(null, fieldName, colonToken, expressionNode);
    }

    private static ListConstructorExpressionNode createListConstructorExpressionNode(List<String> list) {
        Token openBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        List<Node> listElements = new LinkedList<>();
        for (String element : list) {
            listElements.add(AbstractNodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, element,
                    emptyML(), emptyML()));
        }
        SeparatedNodeList<Node> expressions = AbstractNodeFactory.createSeparatedNodeList(listElements);

        return NodeFactory.createListConstructorExpressionNode(openBracketToken, expressions, closeBracketToken);
    }

    private static OptionalTypeDescriptorNode createOptionalTypeDescriptorNode(TypeDescriptorNode type) {
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);

        return NodeFactory.createOptionalTypeDescriptorNode(type, questionMarkToken);
    }

//    private static FieldAccessExpressionNode createFieldAccessExpressionNode(String expression, String fieldName) {
//        SimpleNameReferenceNode expressionNode = NodeFactory.createSimpleNameReferenceNode(AbstractNodeFactory
//                .createIdentifierToken(expression));
//        Token dotToken = AbstractNodeFactory.createToken(SyntaxKind.DOT_TOKEN);
//        SimpleNameReferenceNode fieldNameNode = NodeFactory.createSimpleNameReferenceNode(AbstractNodeFactory
//                .createIdentifierToken(fieldName));
//
//        return NodeFactory.createFieldAccessExpressionNode(expressionNode, dotToken, fieldNameNode);
//    }

    private static ErrorTypeDescriptorNode createErrorTypeDescriptorNode() {
        Token errorKeyword = AbstractNodeFactory.createToken(SyntaxKind.ERROR_KEYWORD);

        return NodeFactory.createErrorTypeDescriptorNode(errorKeyword, null);
    }

    private static BasicLiteralNode createBasicLiteralNode(String value) {
        Token valueToken = AbstractNodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                "\"" + value + "\"", emptyML(), emptyML());
        return NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, valueToken);
    }

    private static QualifiedNameReferenceNode createQualifiedNameReferenceNode(String prefix, String identifier) {
        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken(prefix);
        Token colonToken = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifierToken = AbstractNodeFactory.createIdentifierToken(identifier);

        return NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colonToken, identifierToken);
    }

    private static RequiredParameterNode createRequiredParameterNode(String type, String param, boolean isArray) {
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createNodeList();
        Node typeName;
        if (isArray) {
            typeName = createArrayTypeDescriptorNode(type);
        } else {
            typeName = createBuiltinSimpleNameReferenceNode(type);
        }
        IdentifierToken paramName = AbstractNodeFactory.createIdentifierToken(param);

        return NodeFactory.createRequiredParameterNode(annotations, typeName, paramName);
    }

    private static ArrayTypeDescriptorNode createArrayTypeDescriptorNode(String type) {
        BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(type);
        Token openBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);

        return NodeFactory.createArrayTypeDescriptorNode(typeName, openBracketToken, null, closeBracketToken);
    }

    private static BuiltinSimpleNameReferenceNode createBuiltinSimpleNameReferenceNode(String kind) {
        SyntaxKind type = getBuiltInTypeDesc(kind);
        SyntaxKind tokenKind = getBuiltInTokenKind(kind);
        return NodeFactory.createBuiltinSimpleNameReferenceNode(type, AbstractNodeFactory.createToken(tokenKind,
                emptyML(), singleWSML()));
    }

    private static ReturnTypeDescriptorNode createReturnTypeDescriptorNode(String type, boolean isArray,
                                                                           boolean returnsError) {
        Token returnsKeyword = AbstractNodeFactory.createToken(SyntaxKind.RETURNS_KEYWORD, emptyML(),
                singleWSML());
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createNodeList();
        TypeDescriptorNode returnType;
        if (type != null) {
            if (isArray) {
                returnType = createArrayTypeDescriptorNode(type);
            } else {
                returnType = createBuiltinSimpleNameReferenceNode(type);
            }
            if (returnsError) {
                createUnionTypeDescriptorNode(returnType, createErrorTypeDescriptorNode());
            }
        } else {
            returnType = createOptionalTypeDescriptorNode(createErrorTypeDescriptorNode());
        }

        return NodeFactory.createReturnTypeDescriptorNode(returnsKeyword, annotations, returnType);
    }

    private static UnionTypeDescriptorNode createUnionTypeDescriptorNode(TypeDescriptorNode firstType,
                                                                         TypeDescriptorNode secondType) {
        Token pipeToken = AbstractNodeFactory.createToken(SyntaxKind.PIPE_TOKEN);

        return NodeFactory.createUnionTypeDescriptorNode(firstType, pipeToken, secondType);
    }

    /*
     * Retrieve a single whitespace minutiae list.
     * */
    private static MinutiaeList singleWSML() {
        return emptyML().add(AbstractNodeFactory.createWhitespaceMinutiae(" "));
    }

    /*
    * Retrieve an empty minutiae list.
    * */
    private static MinutiaeList emptyML() {
        return AbstractNodeFactory.createEmptyMinutiaeList();
    }

    private static Collection<Node> getTokenList(List<String> stringList) {
        List<Node> tokenList = new LinkedList<>();
        for (String value : stringList) {
            tokenList.add(AbstractNodeFactory.createIdentifierToken(value));
        }
        return tokenList;
    }

    private static SyntaxKind getBuiltInTypeDesc(String type) {
        switch (type) {
            case "handle":
                return SyntaxKind.HANDLE_TYPE_DESC;
            case "int":
                return SyntaxKind.INT_TYPE_DESC;
            case "float":
                return SyntaxKind.FLOAT_TYPE_DESC;
            case "boolean":
                return SyntaxKind.BOOLEAN_TYPE_DESC;
            case "byte":
                return SyntaxKind.BYTE_TYPE_DESC;
            case "error":
                return SyntaxKind.ERROR_TYPE_DESC;
            default:
                return SyntaxKind.HANDLE_TYPE_DESC;
        }
    }

    private static SyntaxKind getBuiltInTokenKind(String type) {
        switch (type) {
            case "handle":
                return SyntaxKind.HANDLE_KEYWORD;
            case "int":
                return SyntaxKind.INT_KEYWORD;
            case "float":
                return SyntaxKind.FLOAT_KEYWORD;
            case "boolean":
                return SyntaxKind.BOOLEAN_KEYWORD;
            case "byte":
                return SyntaxKind.BYTE_KEYWORD;
            case "error":
                return SyntaxKind.ERROR_KEYWORD;
            default:
                return SyntaxKind.HANDLE_KEYWORD;
        }
    }
}

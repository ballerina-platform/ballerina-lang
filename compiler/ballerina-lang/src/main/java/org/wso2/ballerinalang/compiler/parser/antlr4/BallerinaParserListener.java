/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Generated from BallerinaParser.g4 by ANTLR 4.5.3
package org.wso2.ballerinalang.compiler.parser.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BallerinaParser}.
 */
public interface BallerinaParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(BallerinaParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(BallerinaParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#packageName}.
	 * @param ctx the parse tree
	 */
	void enterPackageName(BallerinaParser.PackageNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#packageName}.
	 * @param ctx the parse tree
	 */
	void exitPackageName(BallerinaParser.PackageNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#version}.
	 * @param ctx the parse tree
	 */
	void enterVersion(BallerinaParser.VersionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#version}.
	 * @param ctx the parse tree
	 */
	void exitVersion(BallerinaParser.VersionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterImportDeclaration(BallerinaParser.ImportDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#orgName}.
	 * @param ctx the parse tree
	 */
	void enterOrgName(BallerinaParser.OrgNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#orgName}.
	 * @param ctx the parse tree
	 */
	void exitOrgName(BallerinaParser.OrgNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#definition}.
	 * @param ctx the parse tree
	 */
	void enterDefinition(BallerinaParser.DefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#definition}.
	 * @param ctx the parse tree
	 */
	void exitDefinition(BallerinaParser.DefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#serviceDefinition}.
	 * @param ctx the parse tree
	 */
	void enterServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#serviceDefinition}.
	 * @param ctx the parse tree
	 */
	void exitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#serviceEndpointAttachments}.
	 * @param ctx the parse tree
	 */
	void enterServiceEndpointAttachments(BallerinaParser.ServiceEndpointAttachmentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#serviceEndpointAttachments}.
	 * @param ctx the parse tree
	 */
	void exitServiceEndpointAttachments(BallerinaParser.ServiceEndpointAttachmentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#serviceBody}.
	 * @param ctx the parse tree
	 */
	void enterServiceBody(BallerinaParser.ServiceBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#serviceBody}.
	 * @param ctx the parse tree
	 */
	void exitServiceBody(BallerinaParser.ServiceBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#resourceDefinition}.
	 * @param ctx the parse tree
	 */
	void enterResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#resourceDefinition}.
	 * @param ctx the parse tree
	 */
	void exitResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#resourceParameterList}.
	 * @param ctx the parse tree
	 */
	void enterResourceParameterList(BallerinaParser.ResourceParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#resourceParameterList}.
	 * @param ctx the parse tree
	 */
	void exitResourceParameterList(BallerinaParser.ResourceParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#callableUnitBody}.
	 * @param ctx the parse tree
	 */
	void enterCallableUnitBody(BallerinaParser.CallableUnitBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#callableUnitBody}.
	 * @param ctx the parse tree
	 */
	void exitCallableUnitBody(BallerinaParser.CallableUnitBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#lambdaFunction}.
	 * @param ctx the parse tree
	 */
	void enterLambdaFunction(BallerinaParser.LambdaFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#lambdaFunction}.
	 * @param ctx the parse tree
	 */
	void exitLambdaFunction(BallerinaParser.LambdaFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#callableUnitSignature}.
	 * @param ctx the parse tree
	 */
	void enterCallableUnitSignature(BallerinaParser.CallableUnitSignatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#callableUnitSignature}.
	 * @param ctx the parse tree
	 */
	void exitCallableUnitSignature(BallerinaParser.CallableUnitSignatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeDefinition}.
	 * @param ctx the parse tree
	 */
	void enterTypeDefinition(BallerinaParser.TypeDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeDefinition}.
	 * @param ctx the parse tree
	 */
	void exitTypeDefinition(BallerinaParser.TypeDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectBody}.
	 * @param ctx the parse tree
	 */
	void enterObjectBody(BallerinaParser.ObjectBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectBody}.
	 * @param ctx the parse tree
	 */
	void exitObjectBody(BallerinaParser.ObjectBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#publicObjectFields}.
	 * @param ctx the parse tree
	 */
	void enterPublicObjectFields(BallerinaParser.PublicObjectFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#publicObjectFields}.
	 * @param ctx the parse tree
	 */
	void exitPublicObjectFields(BallerinaParser.PublicObjectFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#privateObjectFields}.
	 * @param ctx the parse tree
	 */
	void enterPrivateObjectFields(BallerinaParser.PrivateObjectFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#privateObjectFields}.
	 * @param ctx the parse tree
	 */
	void exitPrivateObjectFields(BallerinaParser.PrivateObjectFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectInitializer}.
	 * @param ctx the parse tree
	 */
	void enterObjectInitializer(BallerinaParser.ObjectInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectInitializer}.
	 * @param ctx the parse tree
	 */
	void exitObjectInitializer(BallerinaParser.ObjectInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectInitializerParameterList}.
	 * @param ctx the parse tree
	 */
	void enterObjectInitializerParameterList(BallerinaParser.ObjectInitializerParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectInitializerParameterList}.
	 * @param ctx the parse tree
	 */
	void exitObjectInitializerParameterList(BallerinaParser.ObjectInitializerParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectFunctions}.
	 * @param ctx the parse tree
	 */
	void enterObjectFunctions(BallerinaParser.ObjectFunctionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectFunctions}.
	 * @param ctx the parse tree
	 */
	void exitObjectFunctions(BallerinaParser.ObjectFunctionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#fieldDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFieldDefinition(BallerinaParser.FieldDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#fieldDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFieldDefinition(BallerinaParser.FieldDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectParameterList}.
	 * @param ctx the parse tree
	 */
	void enterObjectParameterList(BallerinaParser.ObjectParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectParameterList}.
	 * @param ctx the parse tree
	 */
	void exitObjectParameterList(BallerinaParser.ObjectParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectParameter}.
	 * @param ctx the parse tree
	 */
	void enterObjectParameter(BallerinaParser.ObjectParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectParameter}.
	 * @param ctx the parse tree
	 */
	void exitObjectParameter(BallerinaParser.ObjectParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectDefaultableParameter}.
	 * @param ctx the parse tree
	 */
	void enterObjectDefaultableParameter(BallerinaParser.ObjectDefaultableParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectDefaultableParameter}.
	 * @param ctx the parse tree
	 */
	void exitObjectDefaultableParameter(BallerinaParser.ObjectDefaultableParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterObjectFunctionDefinition(BallerinaParser.ObjectFunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitObjectFunctionDefinition(BallerinaParser.ObjectFunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectCallableUnitSignature}.
	 * @param ctx the parse tree
	 */
	void enterObjectCallableUnitSignature(BallerinaParser.ObjectCallableUnitSignatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectCallableUnitSignature}.
	 * @param ctx the parse tree
	 */
	void exitObjectCallableUnitSignature(BallerinaParser.ObjectCallableUnitSignatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#annotationDefinition}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#annotationDefinition}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#globalVariableDefinition}.
	 * @param ctx the parse tree
	 */
	void enterGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#globalVariableDefinition}.
	 * @param ctx the parse tree
	 */
	void exitGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 */
	void enterAttachmentPoint(BallerinaParser.AttachmentPointContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 */
	void exitAttachmentPoint(BallerinaParser.AttachmentPointContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#workerDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#workerDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#workerDefinition}.
	 * @param ctx the parse tree
	 */
	void enterWorkerDefinition(BallerinaParser.WorkerDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#workerDefinition}.
	 * @param ctx the parse tree
	 */
	void exitWorkerDefinition(BallerinaParser.WorkerDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#globalEndpointDefinition}.
	 * @param ctx the parse tree
	 */
	void enterGlobalEndpointDefinition(BallerinaParser.GlobalEndpointDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#globalEndpointDefinition}.
	 * @param ctx the parse tree
	 */
	void exitGlobalEndpointDefinition(BallerinaParser.GlobalEndpointDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#endpointDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterEndpointDeclaration(BallerinaParser.EndpointDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#endpointDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitEndpointDeclaration(BallerinaParser.EndpointDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#endpointType}.
	 * @param ctx the parse tree
	 */
	void enterEndpointType(BallerinaParser.EndpointTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#endpointType}.
	 * @param ctx the parse tree
	 */
	void exitEndpointType(BallerinaParser.EndpointTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#endpointInitlization}.
	 * @param ctx the parse tree
	 */
	void enterEndpointInitlization(BallerinaParser.EndpointInitlizationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#endpointInitlization}.
	 * @param ctx the parse tree
	 */
	void exitEndpointInitlization(BallerinaParser.EndpointInitlizationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#finiteType}.
	 * @param ctx the parse tree
	 */
	void enterFiniteType(BallerinaParser.FiniteTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#finiteType}.
	 * @param ctx the parse tree
	 */
	void exitFiniteType(BallerinaParser.FiniteTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#finiteTypeUnit}.
	 * @param ctx the parse tree
	 */
	void enterFiniteTypeUnit(BallerinaParser.FiniteTypeUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#finiteTypeUnit}.
	 * @param ctx the parse tree
	 */
	void exitFiniteTypeUnit(BallerinaParser.FiniteTypeUnitContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tupleTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTupleTypeNameLabel(BallerinaParser.TupleTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tupleTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTupleTypeNameLabel(BallerinaParser.TupleTypeNameLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code recordTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterRecordTypeNameLabel(BallerinaParser.RecordTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code recordTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitRecordTypeNameLabel(BallerinaParser.RecordTypeNameLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unionTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterUnionTypeNameLabel(BallerinaParser.UnionTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unionTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitUnionTypeNameLabel(BallerinaParser.UnionTypeNameLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterSimpleTypeNameLabel(BallerinaParser.SimpleTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitSimpleTypeNameLabel(BallerinaParser.SimpleTypeNameLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullableTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterNullableTypeNameLabel(BallerinaParser.NullableTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullableTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitNullableTypeNameLabel(BallerinaParser.NullableTypeNameLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterArrayTypeNameLabel(BallerinaParser.ArrayTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitArrayTypeNameLabel(BallerinaParser.ArrayTypeNameLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code objectTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterObjectTypeNameLabel(BallerinaParser.ObjectTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code objectTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitObjectTypeNameLabel(BallerinaParser.ObjectTypeNameLabelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code groupTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterGroupTypeNameLabel(BallerinaParser.GroupTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code groupTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitGroupTypeNameLabel(BallerinaParser.GroupTypeNameLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#fieldDefinitionList}.
	 * @param ctx the parse tree
	 */
	void enterFieldDefinitionList(BallerinaParser.FieldDefinitionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#fieldDefinitionList}.
	 * @param ctx the parse tree
	 */
	void exitFieldDefinitionList(BallerinaParser.FieldDefinitionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void enterSimpleTypeName(BallerinaParser.SimpleTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void exitSimpleTypeName(BallerinaParser.SimpleTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#builtInTypeName}.
	 * @param ctx the parse tree
	 */
	void enterBuiltInTypeName(BallerinaParser.BuiltInTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#builtInTypeName}.
	 * @param ctx the parse tree
	 */
	void exitBuiltInTypeName(BallerinaParser.BuiltInTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#referenceTypeName}.
	 * @param ctx the parse tree
	 */
	void enterReferenceTypeName(BallerinaParser.ReferenceTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#referenceTypeName}.
	 * @param ctx the parse tree
	 */
	void exitReferenceTypeName(BallerinaParser.ReferenceTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#userDefineTypeName}.
	 * @param ctx the parse tree
	 */
	void enterUserDefineTypeName(BallerinaParser.UserDefineTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#userDefineTypeName}.
	 * @param ctx the parse tree
	 */
	void exitUserDefineTypeName(BallerinaParser.UserDefineTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#valueTypeName}.
	 * @param ctx the parse tree
	 */
	void enterValueTypeName(BallerinaParser.ValueTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#valueTypeName}.
	 * @param ctx the parse tree
	 */
	void exitValueTypeName(BallerinaParser.ValueTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#builtInReferenceTypeName}.
	 * @param ctx the parse tree
	 */
	void enterBuiltInReferenceTypeName(BallerinaParser.BuiltInReferenceTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#builtInReferenceTypeName}.
	 * @param ctx the parse tree
	 */
	void exitBuiltInReferenceTypeName(BallerinaParser.BuiltInReferenceTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionTypeName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionTypeName(BallerinaParser.FunctionTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionTypeName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionTypeName(BallerinaParser.FunctionTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlNamespaceName}.
	 * @param ctx the parse tree
	 */
	void enterXmlNamespaceName(BallerinaParser.XmlNamespaceNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlNamespaceName}.
	 * @param ctx the parse tree
	 */
	void exitXmlNamespaceName(BallerinaParser.XmlNamespaceNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlLocalName}.
	 * @param ctx the parse tree
	 */
	void enterXmlLocalName(BallerinaParser.XmlLocalNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlLocalName}.
	 * @param ctx the parse tree
	 */
	void exitXmlLocalName(BallerinaParser.XmlLocalNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#annotationAttachment}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationAttachment(BallerinaParser.AnnotationAttachmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#annotationAttachment}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationAttachment(BallerinaParser.AnnotationAttachmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(BallerinaParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(BallerinaParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#variableDefinitionStatement}.
	 * @param ctx the parse tree
	 */
	void enterVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#variableDefinitionStatement}.
	 * @param ctx the parse tree
	 */
	void exitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#recordLiteral}.
	 * @param ctx the parse tree
	 */
	void enterRecordLiteral(BallerinaParser.RecordLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#recordLiteral}.
	 * @param ctx the parse tree
	 */
	void exitRecordLiteral(BallerinaParser.RecordLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#recordKeyValue}.
	 * @param ctx the parse tree
	 */
	void enterRecordKeyValue(BallerinaParser.RecordKeyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#recordKeyValue}.
	 * @param ctx the parse tree
	 */
	void exitRecordKeyValue(BallerinaParser.RecordKeyValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#recordKey}.
	 * @param ctx the parse tree
	 */
	void enterRecordKey(BallerinaParser.RecordKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#recordKey}.
	 * @param ctx the parse tree
	 */
	void exitRecordKey(BallerinaParser.RecordKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tableLiteral}.
	 * @param ctx the parse tree
	 */
	void enterTableLiteral(BallerinaParser.TableLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tableLiteral}.
	 * @param ctx the parse tree
	 */
	void exitTableLiteral(BallerinaParser.TableLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tableInitialization}.
	 * @param ctx the parse tree
	 */
	void enterTableInitialization(BallerinaParser.TableInitializationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tableInitialization}.
	 * @param ctx the parse tree
	 */
	void exitTableInitialization(BallerinaParser.TableInitializationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#arrayLiteral}.
	 * @param ctx the parse tree
	 */
	void enterArrayLiteral(BallerinaParser.ArrayLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#arrayLiteral}.
	 * @param ctx the parse tree
	 */
	void exitArrayLiteral(BallerinaParser.ArrayLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeInitExpr}.
	 * @param ctx the parse tree
	 */
	void enterTypeInitExpr(BallerinaParser.TypeInitExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeInitExpr}.
	 * @param ctx the parse tree
	 */
	void exitTypeInitExpr(BallerinaParser.TypeInitExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tupleDestructuringStatement}.
	 * @param ctx the parse tree
	 */
	void enterTupleDestructuringStatement(BallerinaParser.TupleDestructuringStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tupleDestructuringStatement}.
	 * @param ctx the parse tree
	 */
	void exitTupleDestructuringStatement(BallerinaParser.TupleDestructuringStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#compoundAssignmentStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundAssignmentStatement(BallerinaParser.CompoundAssignmentStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#compoundAssignmentStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundAssignmentStatement(BallerinaParser.CompoundAssignmentStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#compoundOperator}.
	 * @param ctx the parse tree
	 */
	void enterCompoundOperator(BallerinaParser.CompoundOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#compoundOperator}.
	 * @param ctx the parse tree
	 */
	void exitCompoundOperator(BallerinaParser.CompoundOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#postIncrementStatement}.
	 * @param ctx the parse tree
	 */
	void enterPostIncrementStatement(BallerinaParser.PostIncrementStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#postIncrementStatement}.
	 * @param ctx the parse tree
	 */
	void exitPostIncrementStatement(BallerinaParser.PostIncrementStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#postArithmeticOperator}.
	 * @param ctx the parse tree
	 */
	void enterPostArithmeticOperator(BallerinaParser.PostArithmeticOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#postArithmeticOperator}.
	 * @param ctx the parse tree
	 */
	void exitPostArithmeticOperator(BallerinaParser.PostArithmeticOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#variableReferenceList}.
	 * @param ctx the parse tree
	 */
	void enterVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#variableReferenceList}.
	 * @param ctx the parse tree
	 */
	void exitVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#ifElseStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfElseStatement(BallerinaParser.IfElseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#ifElseStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfElseStatement(BallerinaParser.IfElseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#ifClause}.
	 * @param ctx the parse tree
	 */
	void enterIfClause(BallerinaParser.IfClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#ifClause}.
	 * @param ctx the parse tree
	 */
	void exitIfClause(BallerinaParser.IfClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#elseIfClause}.
	 * @param ctx the parse tree
	 */
	void enterElseIfClause(BallerinaParser.ElseIfClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#elseIfClause}.
	 * @param ctx the parse tree
	 */
	void exitElseIfClause(BallerinaParser.ElseIfClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void enterElseClause(BallerinaParser.ElseClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void exitElseClause(BallerinaParser.ElseClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#matchStatement}.
	 * @param ctx the parse tree
	 */
	void enterMatchStatement(BallerinaParser.MatchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#matchStatement}.
	 * @param ctx the parse tree
	 */
	void exitMatchStatement(BallerinaParser.MatchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#matchPatternClause}.
	 * @param ctx the parse tree
	 */
	void enterMatchPatternClause(BallerinaParser.MatchPatternClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#matchPatternClause}.
	 * @param ctx the parse tree
	 */
	void exitMatchPatternClause(BallerinaParser.MatchPatternClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#foreachStatement}.
	 * @param ctx the parse tree
	 */
	void enterForeachStatement(BallerinaParser.ForeachStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#foreachStatement}.
	 * @param ctx the parse tree
	 */
	void exitForeachStatement(BallerinaParser.ForeachStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#intRangeExpression}.
	 * @param ctx the parse tree
	 */
	void enterIntRangeExpression(BallerinaParser.IntRangeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#intRangeExpression}.
	 * @param ctx the parse tree
	 */
	void exitIntRangeExpression(BallerinaParser.IntRangeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(BallerinaParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(BallerinaParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#nextStatement}.
	 * @param ctx the parse tree
	 */
	void enterNextStatement(BallerinaParser.NextStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#nextStatement}.
	 * @param ctx the parse tree
	 */
	void exitNextStatement(BallerinaParser.NextStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#breakStatement}.
	 * @param ctx the parse tree
	 */
	void enterBreakStatement(BallerinaParser.BreakStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#breakStatement}.
	 * @param ctx the parse tree
	 */
	void exitBreakStatement(BallerinaParser.BreakStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#forkJoinStatement}.
	 * @param ctx the parse tree
	 */
	void enterForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#forkJoinStatement}.
	 * @param ctx the parse tree
	 */
	void exitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#joinClause}.
	 * @param ctx the parse tree
	 */
	void enterJoinClause(BallerinaParser.JoinClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#joinClause}.
	 * @param ctx the parse tree
	 */
	void exitJoinClause(BallerinaParser.JoinClauseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code anyJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void enterAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code anyJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void exitAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code allJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void enterAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code allJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void exitAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#timeoutClause}.
	 * @param ctx the parse tree
	 */
	void enterTimeoutClause(BallerinaParser.TimeoutClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#timeoutClause}.
	 * @param ctx the parse tree
	 */
	void exitTimeoutClause(BallerinaParser.TimeoutClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void enterTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void exitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#catchClauses}.
	 * @param ctx the parse tree
	 */
	void enterCatchClauses(BallerinaParser.CatchClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#catchClauses}.
	 * @param ctx the parse tree
	 */
	void exitCatchClauses(BallerinaParser.CatchClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void enterCatchClause(BallerinaParser.CatchClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void exitCatchClause(BallerinaParser.CatchClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#finallyClause}.
	 * @param ctx the parse tree
	 */
	void enterFinallyClause(BallerinaParser.FinallyClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#finallyClause}.
	 * @param ctx the parse tree
	 */
	void exitFinallyClause(BallerinaParser.FinallyClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#throwStatement}.
	 * @param ctx the parse tree
	 */
	void enterThrowStatement(BallerinaParser.ThrowStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#throwStatement}.
	 * @param ctx the parse tree
	 */
	void exitThrowStatement(BallerinaParser.ThrowStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(BallerinaParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(BallerinaParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#workerInteractionStatement}.
	 * @param ctx the parse tree
	 */
	void enterWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#workerInteractionStatement}.
	 * @param ctx the parse tree
	 */
	void exitWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code invokeWorker}
	 * labeled alternative in {@link BallerinaParser#triggerWorker}.
	 * @param ctx the parse tree
	 */
	void enterInvokeWorker(BallerinaParser.InvokeWorkerContext ctx);
	/**
	 * Exit a parse tree produced by the {@code invokeWorker}
	 * labeled alternative in {@link BallerinaParser#triggerWorker}.
	 * @param ctx the parse tree
	 */
	void exitInvokeWorker(BallerinaParser.InvokeWorkerContext ctx);
	/**
	 * Enter a parse tree produced by the {@code invokeFork}
	 * labeled alternative in {@link BallerinaParser#triggerWorker}.
	 * @param ctx the parse tree
	 */
	void enterInvokeFork(BallerinaParser.InvokeForkContext ctx);
	/**
	 * Exit a parse tree produced by the {@code invokeFork}
	 * labeled alternative in {@link BallerinaParser#triggerWorker}.
	 * @param ctx the parse tree
	 */
	void exitInvokeFork(BallerinaParser.InvokeForkContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#workerReply}.
	 * @param ctx the parse tree
	 */
	void enterWorkerReply(BallerinaParser.WorkerReplyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#workerReply}.
	 * @param ctx the parse tree
	 */
	void exitWorkerReply(BallerinaParser.WorkerReplyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code xmlAttribVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttribVariableReference(BallerinaParser.XmlAttribVariableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code xmlAttribVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttribVariableReference(BallerinaParser.XmlAttribVariableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterSimpleVariableReference(BallerinaParser.SimpleVariableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitSimpleVariableReference(BallerinaParser.SimpleVariableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code awaitExpressionReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterAwaitExpressionReference(BallerinaParser.AwaitExpressionReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code awaitExpressionReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitAwaitExpressionReference(BallerinaParser.AwaitExpressionReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code invocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterInvocationReference(BallerinaParser.InvocationReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code invocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitInvocationReference(BallerinaParser.InvocationReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterFunctionInvocationReference(BallerinaParser.FunctionInvocationReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitFunctionInvocationReference(BallerinaParser.FunctionInvocationReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code fieldVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterFieldVariableReference(BallerinaParser.FieldVariableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fieldVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitFieldVariableReference(BallerinaParser.FieldVariableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mapArrayVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterMapArrayVariableReference(BallerinaParser.MapArrayVariableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mapArrayVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitMapArrayVariableReference(BallerinaParser.MapArrayVariableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(BallerinaParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(BallerinaParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#index}.
	 * @param ctx the parse tree
	 */
	void enterIndex(BallerinaParser.IndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#index}.
	 * @param ctx the parse tree
	 */
	void exitIndex(BallerinaParser.IndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlAttrib}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttrib(BallerinaParser.XmlAttribContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlAttrib}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttrib(BallerinaParser.XmlAttribContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionInvocation}.
	 * @param ctx the parse tree
	 */
	void enterFunctionInvocation(BallerinaParser.FunctionInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionInvocation}.
	 * @param ctx the parse tree
	 */
	void exitFunctionInvocation(BallerinaParser.FunctionInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#invocation}.
	 * @param ctx the parse tree
	 */
	void enterInvocation(BallerinaParser.InvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#invocation}.
	 * @param ctx the parse tree
	 */
	void exitInvocation(BallerinaParser.InvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#invocationArgList}.
	 * @param ctx the parse tree
	 */
	void enterInvocationArgList(BallerinaParser.InvocationArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#invocationArgList}.
	 * @param ctx the parse tree
	 */
	void exitInvocationArgList(BallerinaParser.InvocationArgListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#invocationArg}.
	 * @param ctx the parse tree
	 */
	void enterInvocationArg(BallerinaParser.InvocationArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#invocationArg}.
	 * @param ctx the parse tree
	 */
	void exitInvocationArg(BallerinaParser.InvocationArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#actionInvocation}.
	 * @param ctx the parse tree
	 */
	void enterActionInvocation(BallerinaParser.ActionInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#actionInvocation}.
	 * @param ctx the parse tree
	 */
	void exitActionInvocation(BallerinaParser.ActionInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(BallerinaParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(BallerinaParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#expressionStmt}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStmt(BallerinaParser.ExpressionStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#expressionStmt}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStmt(BallerinaParser.ExpressionStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#transactionStatement}.
	 * @param ctx the parse tree
	 */
	void enterTransactionStatement(BallerinaParser.TransactionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#transactionStatement}.
	 * @param ctx the parse tree
	 */
	void exitTransactionStatement(BallerinaParser.TransactionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#transactionClause}.
	 * @param ctx the parse tree
	 */
	void enterTransactionClause(BallerinaParser.TransactionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#transactionClause}.
	 * @param ctx the parse tree
	 */
	void exitTransactionClause(BallerinaParser.TransactionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#transactionPropertyInitStatement}.
	 * @param ctx the parse tree
	 */
	void enterTransactionPropertyInitStatement(BallerinaParser.TransactionPropertyInitStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#transactionPropertyInitStatement}.
	 * @param ctx the parse tree
	 */
	void exitTransactionPropertyInitStatement(BallerinaParser.TransactionPropertyInitStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#transactionPropertyInitStatementList}.
	 * @param ctx the parse tree
	 */
	void enterTransactionPropertyInitStatementList(BallerinaParser.TransactionPropertyInitStatementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#transactionPropertyInitStatementList}.
	 * @param ctx the parse tree
	 */
	void exitTransactionPropertyInitStatementList(BallerinaParser.TransactionPropertyInitStatementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#lockStatement}.
	 * @param ctx the parse tree
	 */
	void enterLockStatement(BallerinaParser.LockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#lockStatement}.
	 * @param ctx the parse tree
	 */
	void exitLockStatement(BallerinaParser.LockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#onretryClause}.
	 * @param ctx the parse tree
	 */
	void enterOnretryClause(BallerinaParser.OnretryClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#onretryClause}.
	 * @param ctx the parse tree
	 */
	void exitOnretryClause(BallerinaParser.OnretryClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#abortStatement}.
	 * @param ctx the parse tree
	 */
	void enterAbortStatement(BallerinaParser.AbortStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#abortStatement}.
	 * @param ctx the parse tree
	 */
	void exitAbortStatement(BallerinaParser.AbortStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#failStatement}.
	 * @param ctx the parse tree
	 */
	void enterFailStatement(BallerinaParser.FailStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#failStatement}.
	 * @param ctx the parse tree
	 */
	void exitFailStatement(BallerinaParser.FailStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#retriesStatement}.
	 * @param ctx the parse tree
	 */
	void enterRetriesStatement(BallerinaParser.RetriesStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#retriesStatement}.
	 * @param ctx the parse tree
	 */
	void exitRetriesStatement(BallerinaParser.RetriesStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#oncommitStatement}.
	 * @param ctx the parse tree
	 */
	void enterOncommitStatement(BallerinaParser.OncommitStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#oncommitStatement}.
	 * @param ctx the parse tree
	 */
	void exitOncommitStatement(BallerinaParser.OncommitStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#onabortStatement}.
	 * @param ctx the parse tree
	 */
	void enterOnabortStatement(BallerinaParser.OnabortStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#onabortStatement}.
	 * @param ctx the parse tree
	 */
	void exitOnabortStatement(BallerinaParser.OnabortStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#namespaceDeclarationStatement}.
	 * @param ctx the parse tree
	 */
	void enterNamespaceDeclarationStatement(BallerinaParser.NamespaceDeclarationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#namespaceDeclarationStatement}.
	 * @param ctx the parse tree
	 */
	void exitNamespaceDeclarationStatement(BallerinaParser.NamespaceDeclarationStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#namespaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterNamespaceDeclaration(BallerinaParser.NamespaceDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#namespaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitNamespaceDeclaration(BallerinaParser.NamespaceDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryOrExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryOrExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code xmlLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterXmlLiteralExpression(BallerinaParser.XmlLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code xmlLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitXmlLiteralExpression(BallerinaParser.XmlLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSimpleLiteralExpression(BallerinaParser.SimpleLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSimpleLiteralExpression(BallerinaParser.SimpleLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringTemplateLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStringTemplateLiteralExpression(BallerinaParser.StringTemplateLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringTemplateLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStringTemplateLiteralExpression(BallerinaParser.StringTemplateLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeAccessExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTypeAccessExpression(BallerinaParser.TypeAccessExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeAccessExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTypeAccessExpression(BallerinaParser.TypeAccessExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryAndExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryAndExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryAddSubExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryAddSubExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeConversionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTypeConversionExpression(BallerinaParser.TypeConversionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeConversionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTypeConversionExpression(BallerinaParser.TypeConversionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code checkedExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCheckedExpression(BallerinaParser.CheckedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code checkedExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCheckedExpression(BallerinaParser.CheckedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(BallerinaParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bracedOrTupleExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBracedOrTupleExpression(BallerinaParser.BracedOrTupleExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bracedOrTupleExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBracedOrTupleExpression(BallerinaParser.BracedOrTupleExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryDivMulModExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryDivMulModExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code valueTypeTypeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterValueTypeTypeExpression(BallerinaParser.ValueTypeTypeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code valueTypeTypeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitValueTypeTypeExpression(BallerinaParser.ValueTypeTypeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTableLiteralExpression(BallerinaParser.TableLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTableLiteralExpression(BallerinaParser.TableLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code lambdaFunctionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLambdaFunctionExpression(BallerinaParser.LambdaFunctionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lambdaFunctionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLambdaFunctionExpression(BallerinaParser.LambdaFunctionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code awaitExprExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAwaitExprExpression(BallerinaParser.AwaitExprExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code awaitExprExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAwaitExprExpression(BallerinaParser.AwaitExprExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code recordLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterRecordLiteralExpression(BallerinaParser.RecordLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code recordLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitRecordLiteralExpression(BallerinaParser.RecordLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterArrayLiteralExpression(BallerinaParser.ArrayLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitArrayLiteralExpression(BallerinaParser.ArrayLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variableReferenceExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variableReferenceExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code matchExprExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMatchExprExpression(BallerinaParser.MatchExprExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code matchExprExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMatchExprExpression(BallerinaParser.MatchExprExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryCompareExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryCompareExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code builtInReferenceTypeTypeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBuiltInReferenceTypeTypeExpression(BallerinaParser.BuiltInReferenceTypeTypeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code builtInReferenceTypeTypeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBuiltInReferenceTypeTypeExpression(BallerinaParser.BuiltInReferenceTypeTypeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code elvisExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterElvisExpression(BallerinaParser.ElvisExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code elvisExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitElvisExpression(BallerinaParser.ElvisExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableQueryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTableQueryExpression(BallerinaParser.TableQueryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableQueryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTableQueryExpression(BallerinaParser.TableQueryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ternaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTernaryExpression(BallerinaParser.TernaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ternaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTernaryExpression(BallerinaParser.TernaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTypeInitExpression(BallerinaParser.TypeInitExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTypeInitExpression(BallerinaParser.TypeInitExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryPowExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryPowExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code awaitExpr}
	 * labeled alternative in {@link BallerinaParser#awaitExpression}.
	 * @param ctx the parse tree
	 */
	void enterAwaitExpr(BallerinaParser.AwaitExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code awaitExpr}
	 * labeled alternative in {@link BallerinaParser#awaitExpression}.
	 * @param ctx the parse tree
	 */
	void exitAwaitExpr(BallerinaParser.AwaitExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#matchExpression}.
	 * @param ctx the parse tree
	 */
	void enterMatchExpression(BallerinaParser.MatchExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#matchExpression}.
	 * @param ctx the parse tree
	 */
	void exitMatchExpression(BallerinaParser.MatchExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#matchExpressionPatternClause}.
	 * @param ctx the parse tree
	 */
	void enterMatchExpressionPatternClause(BallerinaParser.MatchExpressionPatternClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#matchExpressionPatternClause}.
	 * @param ctx the parse tree
	 */
	void exitMatchExpressionPatternClause(BallerinaParser.MatchExpressionPatternClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#nameReference}.
	 * @param ctx the parse tree
	 */
	void enterNameReference(BallerinaParser.NameReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#nameReference}.
	 * @param ctx the parse tree
	 */
	void exitNameReference(BallerinaParser.NameReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#returnParameter}.
	 * @param ctx the parse tree
	 */
	void enterReturnParameter(BallerinaParser.ReturnParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#returnParameter}.
	 * @param ctx the parse tree
	 */
	void exitReturnParameter(BallerinaParser.ReturnParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#lambdaReturnParameter}.
	 * @param ctx the parse tree
	 */
	void enterLambdaReturnParameter(BallerinaParser.LambdaReturnParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#lambdaReturnParameter}.
	 * @param ctx the parse tree
	 */
	void exitLambdaReturnParameter(BallerinaParser.LambdaReturnParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#parameterTypeNameList}.
	 * @param ctx the parse tree
	 */
	void enterParameterTypeNameList(BallerinaParser.ParameterTypeNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#parameterTypeNameList}.
	 * @param ctx the parse tree
	 */
	void exitParameterTypeNameList(BallerinaParser.ParameterTypeNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#parameterTypeName}.
	 * @param ctx the parse tree
	 */
	void enterParameterTypeName(BallerinaParser.ParameterTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#parameterTypeName}.
	 * @param ctx the parse tree
	 */
	void exitParameterTypeName(BallerinaParser.ParameterTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(BallerinaParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(BallerinaParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleParameter}
	 * labeled alternative in {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterSimpleParameter(BallerinaParser.SimpleParameterContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleParameter}
	 * labeled alternative in {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitSimpleParameter(BallerinaParser.SimpleParameterContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tupleParameter}
	 * labeled alternative in {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterTupleParameter(BallerinaParser.TupleParameterContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tupleParameter}
	 * labeled alternative in {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitTupleParameter(BallerinaParser.TupleParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#defaultableParameter}.
	 * @param ctx the parse tree
	 */
	void enterDefaultableParameter(BallerinaParser.DefaultableParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#defaultableParameter}.
	 * @param ctx the parse tree
	 */
	void exitDefaultableParameter(BallerinaParser.DefaultableParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#restParameter}.
	 * @param ctx the parse tree
	 */
	void enterRestParameter(BallerinaParser.RestParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#restParameter}.
	 * @param ctx the parse tree
	 */
	void exitRestParameter(BallerinaParser.RestParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameterList(BallerinaParser.FormalParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameterList(BallerinaParser.FormalParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#simpleLiteral}.
	 * @param ctx the parse tree
	 */
	void enterSimpleLiteral(BallerinaParser.SimpleLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#simpleLiteral}.
	 * @param ctx the parse tree
	 */
	void exitSimpleLiteral(BallerinaParser.SimpleLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(BallerinaParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(BallerinaParser.IntegerLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#emptyTupleLiteral}.
	 * @param ctx the parse tree
	 */
	void enterEmptyTupleLiteral(BallerinaParser.EmptyTupleLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#emptyTupleLiteral}.
	 * @param ctx the parse tree
	 */
	void exitEmptyTupleLiteral(BallerinaParser.EmptyTupleLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#namedArgs}.
	 * @param ctx the parse tree
	 */
	void enterNamedArgs(BallerinaParser.NamedArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#namedArgs}.
	 * @param ctx the parse tree
	 */
	void exitNamedArgs(BallerinaParser.NamedArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#restArgs}.
	 * @param ctx the parse tree
	 */
	void enterRestArgs(BallerinaParser.RestArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#restArgs}.
	 * @param ctx the parse tree
	 */
	void exitRestArgs(BallerinaParser.RestArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlLiteral}.
	 * @param ctx the parse tree
	 */
	void enterXmlLiteral(BallerinaParser.XmlLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlLiteral}.
	 * @param ctx the parse tree
	 */
	void exitXmlLiteral(BallerinaParser.XmlLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlItem}.
	 * @param ctx the parse tree
	 */
	void enterXmlItem(BallerinaParser.XmlItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlItem}.
	 * @param ctx the parse tree
	 */
	void exitXmlItem(BallerinaParser.XmlItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#content}.
	 * @param ctx the parse tree
	 */
	void enterContent(BallerinaParser.ContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#content}.
	 * @param ctx the parse tree
	 */
	void exitContent(BallerinaParser.ContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(BallerinaParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(BallerinaParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(BallerinaParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(BallerinaParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#startTag}.
	 * @param ctx the parse tree
	 */
	void enterStartTag(BallerinaParser.StartTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#startTag}.
	 * @param ctx the parse tree
	 */
	void exitStartTag(BallerinaParser.StartTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#closeTag}.
	 * @param ctx the parse tree
	 */
	void enterCloseTag(BallerinaParser.CloseTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#closeTag}.
	 * @param ctx the parse tree
	 */
	void exitCloseTag(BallerinaParser.CloseTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#emptyTag}.
	 * @param ctx the parse tree
	 */
	void enterEmptyTag(BallerinaParser.EmptyTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#emptyTag}.
	 * @param ctx the parse tree
	 */
	void exitEmptyTag(BallerinaParser.EmptyTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#procIns}.
	 * @param ctx the parse tree
	 */
	void enterProcIns(BallerinaParser.ProcInsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#procIns}.
	 * @param ctx the parse tree
	 */
	void exitProcIns(BallerinaParser.ProcInsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(BallerinaParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(BallerinaParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#text}.
	 * @param ctx the parse tree
	 */
	void enterText(BallerinaParser.TextContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#text}.
	 * @param ctx the parse tree
	 */
	void exitText(BallerinaParser.TextContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlQuotedString}.
	 * @param ctx the parse tree
	 */
	void enterXmlQuotedString(BallerinaParser.XmlQuotedStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlQuotedString}.
	 * @param ctx the parse tree
	 */
	void exitXmlQuotedString(BallerinaParser.XmlQuotedStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlSingleQuotedString}.
	 * @param ctx the parse tree
	 */
	void enterXmlSingleQuotedString(BallerinaParser.XmlSingleQuotedStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlSingleQuotedString}.
	 * @param ctx the parse tree
	 */
	void exitXmlSingleQuotedString(BallerinaParser.XmlSingleQuotedStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlDoubleQuotedString}.
	 * @param ctx the parse tree
	 */
	void enterXmlDoubleQuotedString(BallerinaParser.XmlDoubleQuotedStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlDoubleQuotedString}.
	 * @param ctx the parse tree
	 */
	void exitXmlDoubleQuotedString(BallerinaParser.XmlDoubleQuotedStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlQualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterXmlQualifiedName(BallerinaParser.XmlQualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlQualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitXmlQualifiedName(BallerinaParser.XmlQualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#stringTemplateLiteral}.
	 * @param ctx the parse tree
	 */
	void enterStringTemplateLiteral(BallerinaParser.StringTemplateLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#stringTemplateLiteral}.
	 * @param ctx the parse tree
	 */
	void exitStringTemplateLiteral(BallerinaParser.StringTemplateLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#stringTemplateContent}.
	 * @param ctx the parse tree
	 */
	void enterStringTemplateContent(BallerinaParser.StringTemplateContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#stringTemplateContent}.
	 * @param ctx the parse tree
	 */
	void exitStringTemplateContent(BallerinaParser.StringTemplateContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#anyIdentifierName}.
	 * @param ctx the parse tree
	 */
	void enterAnyIdentifierName(BallerinaParser.AnyIdentifierNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#anyIdentifierName}.
	 * @param ctx the parse tree
	 */
	void exitAnyIdentifierName(BallerinaParser.AnyIdentifierNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#reservedWord}.
	 * @param ctx the parse tree
	 */
	void enterReservedWord(BallerinaParser.ReservedWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#reservedWord}.
	 * @param ctx the parse tree
	 */
	void exitReservedWord(BallerinaParser.ReservedWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tableQuery}.
	 * @param ctx the parse tree
	 */
	void enterTableQuery(BallerinaParser.TableQueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tableQuery}.
	 * @param ctx the parse tree
	 */
	void exitTableQuery(BallerinaParser.TableQueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#aggregationQuery}.
	 * @param ctx the parse tree
	 */
	void enterAggregationQuery(BallerinaParser.AggregationQueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#aggregationQuery}.
	 * @param ctx the parse tree
	 */
	void exitAggregationQuery(BallerinaParser.AggregationQueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#foreverStatement}.
	 * @param ctx the parse tree
	 */
	void enterForeverStatement(BallerinaParser.ForeverStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#foreverStatement}.
	 * @param ctx the parse tree
	 */
	void exitForeverStatement(BallerinaParser.ForeverStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#doneStatement}.
	 * @param ctx the parse tree
	 */
	void enterDoneStatement(BallerinaParser.DoneStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#doneStatement}.
	 * @param ctx the parse tree
	 */
	void exitDoneStatement(BallerinaParser.DoneStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#streamingQueryStatement}.
	 * @param ctx the parse tree
	 */
	void enterStreamingQueryStatement(BallerinaParser.StreamingQueryStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#streamingQueryStatement}.
	 * @param ctx the parse tree
	 */
	void exitStreamingQueryStatement(BallerinaParser.StreamingQueryStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#patternClause}.
	 * @param ctx the parse tree
	 */
	void enterPatternClause(BallerinaParser.PatternClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#patternClause}.
	 * @param ctx the parse tree
	 */
	void exitPatternClause(BallerinaParser.PatternClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withinClause}.
	 * @param ctx the parse tree
	 */
	void enterWithinClause(BallerinaParser.WithinClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withinClause}.
	 * @param ctx the parse tree
	 */
	void exitWithinClause(BallerinaParser.WithinClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderByClause(BallerinaParser.OrderByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderByClause(BallerinaParser.OrderByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(BallerinaParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(BallerinaParser.SelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#selectExpressionList}.
	 * @param ctx the parse tree
	 */
	void enterSelectExpressionList(BallerinaParser.SelectExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#selectExpressionList}.
	 * @param ctx the parse tree
	 */
	void exitSelectExpressionList(BallerinaParser.SelectExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#selectExpression}.
	 * @param ctx the parse tree
	 */
	void enterSelectExpression(BallerinaParser.SelectExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#selectExpression}.
	 * @param ctx the parse tree
	 */
	void exitSelectExpression(BallerinaParser.SelectExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupByClause(BallerinaParser.GroupByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupByClause(BallerinaParser.GroupByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void enterHavingClause(BallerinaParser.HavingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void exitHavingClause(BallerinaParser.HavingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#streamingAction}.
	 * @param ctx the parse tree
	 */
	void enterStreamingAction(BallerinaParser.StreamingActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#streamingAction}.
	 * @param ctx the parse tree
	 */
	void exitStreamingAction(BallerinaParser.StreamingActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#setClause}.
	 * @param ctx the parse tree
	 */
	void enterSetClause(BallerinaParser.SetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#setClause}.
	 * @param ctx the parse tree
	 */
	void exitSetClause(BallerinaParser.SetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#setAssignmentClause}.
	 * @param ctx the parse tree
	 */
	void enterSetAssignmentClause(BallerinaParser.SetAssignmentClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#setAssignmentClause}.
	 * @param ctx the parse tree
	 */
	void exitSetAssignmentClause(BallerinaParser.SetAssignmentClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#streamingInput}.
	 * @param ctx the parse tree
	 */
	void enterStreamingInput(BallerinaParser.StreamingInputContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#streamingInput}.
	 * @param ctx the parse tree
	 */
	void exitStreamingInput(BallerinaParser.StreamingInputContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#joinStreamingInput}.
	 * @param ctx the parse tree
	 */
	void enterJoinStreamingInput(BallerinaParser.JoinStreamingInputContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#joinStreamingInput}.
	 * @param ctx the parse tree
	 */
	void exitJoinStreamingInput(BallerinaParser.JoinStreamingInputContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#outputRateLimit}.
	 * @param ctx the parse tree
	 */
	void enterOutputRateLimit(BallerinaParser.OutputRateLimitContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#outputRateLimit}.
	 * @param ctx the parse tree
	 */
	void exitOutputRateLimit(BallerinaParser.OutputRateLimitContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#patternStreamingInput}.
	 * @param ctx the parse tree
	 */
	void enterPatternStreamingInput(BallerinaParser.PatternStreamingInputContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#patternStreamingInput}.
	 * @param ctx the parse tree
	 */
	void exitPatternStreamingInput(BallerinaParser.PatternStreamingInputContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#patternStreamingEdgeInput}.
	 * @param ctx the parse tree
	 */
	void enterPatternStreamingEdgeInput(BallerinaParser.PatternStreamingEdgeInputContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#patternStreamingEdgeInput}.
	 * @param ctx the parse tree
	 */
	void exitPatternStreamingEdgeInput(BallerinaParser.PatternStreamingEdgeInputContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(BallerinaParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(BallerinaParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionClause}.
	 * @param ctx the parse tree
	 */
	void enterFunctionClause(BallerinaParser.FunctionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionClause}.
	 * @param ctx the parse tree
	 */
	void exitFunctionClause(BallerinaParser.FunctionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void enterWindowClause(BallerinaParser.WindowClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void exitWindowClause(BallerinaParser.WindowClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#outputEventType}.
	 * @param ctx the parse tree
	 */
	void enterOutputEventType(BallerinaParser.OutputEventTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#outputEventType}.
	 * @param ctx the parse tree
	 */
	void exitOutputEventType(BallerinaParser.OutputEventTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterJoinType(BallerinaParser.JoinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitJoinType(BallerinaParser.JoinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#timeScale}.
	 * @param ctx the parse tree
	 */
	void enterTimeScale(BallerinaParser.TimeScaleContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#timeScale}.
	 * @param ctx the parse tree
	 */
	void exitTimeScale(BallerinaParser.TimeScaleContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#deprecatedAttachment}.
	 * @param ctx the parse tree
	 */
	void enterDeprecatedAttachment(BallerinaParser.DeprecatedAttachmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#deprecatedAttachment}.
	 * @param ctx the parse tree
	 */
	void exitDeprecatedAttachment(BallerinaParser.DeprecatedAttachmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#deprecatedText}.
	 * @param ctx the parse tree
	 */
	void enterDeprecatedText(BallerinaParser.DeprecatedTextContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#deprecatedText}.
	 * @param ctx the parse tree
	 */
	void exitDeprecatedText(BallerinaParser.DeprecatedTextContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#deprecatedTemplateInlineCode}.
	 * @param ctx the parse tree
	 */
	void enterDeprecatedTemplateInlineCode(BallerinaParser.DeprecatedTemplateInlineCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#deprecatedTemplateInlineCode}.
	 * @param ctx the parse tree
	 */
	void exitDeprecatedTemplateInlineCode(BallerinaParser.DeprecatedTemplateInlineCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#singleBackTickDeprecatedInlineCode}.
	 * @param ctx the parse tree
	 */
	void enterSingleBackTickDeprecatedInlineCode(BallerinaParser.SingleBackTickDeprecatedInlineCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#singleBackTickDeprecatedInlineCode}.
	 * @param ctx the parse tree
	 */
	void exitSingleBackTickDeprecatedInlineCode(BallerinaParser.SingleBackTickDeprecatedInlineCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#doubleBackTickDeprecatedInlineCode}.
	 * @param ctx the parse tree
	 */
	void enterDoubleBackTickDeprecatedInlineCode(BallerinaParser.DoubleBackTickDeprecatedInlineCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#doubleBackTickDeprecatedInlineCode}.
	 * @param ctx the parse tree
	 */
	void exitDoubleBackTickDeprecatedInlineCode(BallerinaParser.DoubleBackTickDeprecatedInlineCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tripleBackTickDeprecatedInlineCode}.
	 * @param ctx the parse tree
	 */
	void enterTripleBackTickDeprecatedInlineCode(BallerinaParser.TripleBackTickDeprecatedInlineCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tripleBackTickDeprecatedInlineCode}.
	 * @param ctx the parse tree
	 */
	void exitTripleBackTickDeprecatedInlineCode(BallerinaParser.TripleBackTickDeprecatedInlineCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationAttachment}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationAttachment(BallerinaParser.DocumentationAttachmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationAttachment}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationAttachment(BallerinaParser.DocumentationAttachmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationTemplateContent}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationTemplateContent(BallerinaParser.DocumentationTemplateContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationTemplateContent}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationTemplateContent(BallerinaParser.DocumentationTemplateContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationTemplateAttributeDescription}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationTemplateAttributeDescription(BallerinaParser.DocumentationTemplateAttributeDescriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationTemplateAttributeDescription}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationTemplateAttributeDescription(BallerinaParser.DocumentationTemplateAttributeDescriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#docText}.
	 * @param ctx the parse tree
	 */
	void enterDocText(BallerinaParser.DocTextContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#docText}.
	 * @param ctx the parse tree
	 */
	void exitDocText(BallerinaParser.DocTextContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationTemplateInlineCode}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationTemplateInlineCode(BallerinaParser.DocumentationTemplateInlineCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationTemplateInlineCode}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationTemplateInlineCode(BallerinaParser.DocumentationTemplateInlineCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#singleBackTickDocInlineCode}.
	 * @param ctx the parse tree
	 */
	void enterSingleBackTickDocInlineCode(BallerinaParser.SingleBackTickDocInlineCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#singleBackTickDocInlineCode}.
	 * @param ctx the parse tree
	 */
	void exitSingleBackTickDocInlineCode(BallerinaParser.SingleBackTickDocInlineCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#doubleBackTickDocInlineCode}.
	 * @param ctx the parse tree
	 */
	void enterDoubleBackTickDocInlineCode(BallerinaParser.DoubleBackTickDocInlineCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#doubleBackTickDocInlineCode}.
	 * @param ctx the parse tree
	 */
	void exitDoubleBackTickDocInlineCode(BallerinaParser.DoubleBackTickDocInlineCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tripleBackTickDocInlineCode}.
	 * @param ctx the parse tree
	 */
	void enterTripleBackTickDocInlineCode(BallerinaParser.TripleBackTickDocInlineCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tripleBackTickDocInlineCode}.
	 * @param ctx the parse tree
	 */
	void exitTripleBackTickDocInlineCode(BallerinaParser.TripleBackTickDocInlineCodeContext ctx);
}
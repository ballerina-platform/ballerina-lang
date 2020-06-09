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
	 * Enter a parse tree produced by {@link BallerinaParser#versionPattern}.
	 * @param ctx the parse tree
	 */
	void enterVersionPattern(BallerinaParser.VersionPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#versionPattern}.
	 * @param ctx the parse tree
	 */
	void exitVersionPattern(BallerinaParser.VersionPatternContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#blockFunctionBody}.
	 * @param ctx the parse tree
	 */
	void enterBlockFunctionBody(BallerinaParser.BlockFunctionBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#blockFunctionBody}.
	 * @param ctx the parse tree
	 */
	void exitBlockFunctionBody(BallerinaParser.BlockFunctionBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(BallerinaParser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(BallerinaParser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#externalFunctionBody}.
	 * @param ctx the parse tree
	 */
	void enterExternalFunctionBody(BallerinaParser.ExternalFunctionBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#externalFunctionBody}.
	 * @param ctx the parse tree
	 */
	void exitExternalFunctionBody(BallerinaParser.ExternalFunctionBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#exprFunctionBody}.
	 * @param ctx the parse tree
	 */
	void enterExprFunctionBody(BallerinaParser.ExprFunctionBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#exprFunctionBody}.
	 * @param ctx the parse tree
	 */
	void exitExprFunctionBody(BallerinaParser.ExprFunctionBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionDefinitionBody}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinitionBody(BallerinaParser.FunctionDefinitionBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionDefinitionBody}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinitionBody(BallerinaParser.FunctionDefinitionBodyContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#anonymousFunctionExpr}.
	 * @param ctx the parse tree
	 */
	void enterAnonymousFunctionExpr(BallerinaParser.AnonymousFunctionExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#anonymousFunctionExpr}.
	 * @param ctx the parse tree
	 */
	void exitAnonymousFunctionExpr(BallerinaParser.AnonymousFunctionExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#explicitAnonymousFunctionExpr}.
	 * @param ctx the parse tree
	 */
	void enterExplicitAnonymousFunctionExpr(BallerinaParser.ExplicitAnonymousFunctionExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#explicitAnonymousFunctionExpr}.
	 * @param ctx the parse tree
	 */
	void exitExplicitAnonymousFunctionExpr(BallerinaParser.ExplicitAnonymousFunctionExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#inferAnonymousFunctionExpr}.
	 * @param ctx the parse tree
	 */
	void enterInferAnonymousFunctionExpr(BallerinaParser.InferAnonymousFunctionExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#inferAnonymousFunctionExpr}.
	 * @param ctx the parse tree
	 */
	void exitInferAnonymousFunctionExpr(BallerinaParser.InferAnonymousFunctionExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#inferParamList}.
	 * @param ctx the parse tree
	 */
	void enterInferParamList(BallerinaParser.InferParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#inferParamList}.
	 * @param ctx the parse tree
	 */
	void exitInferParamList(BallerinaParser.InferParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#inferParam}.
	 * @param ctx the parse tree
	 */
	void enterInferParam(BallerinaParser.InferParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#inferParam}.
	 * @param ctx the parse tree
	 */
	void exitInferParam(BallerinaParser.InferParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionSignature}.
	 * @param ctx the parse tree
	 */
	void enterFunctionSignature(BallerinaParser.FunctionSignatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionSignature}.
	 * @param ctx the parse tree
	 */
	void exitFunctionSignature(BallerinaParser.FunctionSignatureContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#typeReference}.
	 * @param ctx the parse tree
	 */
	void enterTypeReference(BallerinaParser.TypeReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeReference}.
	 * @param ctx the parse tree
	 */
	void exitTypeReference(BallerinaParser.TypeReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectFieldDefinition}.
	 * @param ctx the parse tree
	 */
	void enterObjectFieldDefinition(BallerinaParser.ObjectFieldDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectFieldDefinition}.
	 * @param ctx the parse tree
	 */
	void exitObjectFieldDefinition(BallerinaParser.ObjectFieldDefinitionContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#recordRestFieldDefinition}.
	 * @param ctx the parse tree
	 */
	void enterRecordRestFieldDefinition(BallerinaParser.RecordRestFieldDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#recordRestFieldDefinition}.
	 * @param ctx the parse tree
	 */
	void exitRecordRestFieldDefinition(BallerinaParser.RecordRestFieldDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#sealedLiteral}.
	 * @param ctx the parse tree
	 */
	void enterSealedLiteral(BallerinaParser.SealedLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#sealedLiteral}.
	 * @param ctx the parse tree
	 */
	void exitSealedLiteral(BallerinaParser.SealedLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#restDescriptorPredicate}.
	 * @param ctx the parse tree
	 */
	void enterRestDescriptorPredicate(BallerinaParser.RestDescriptorPredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#restDescriptorPredicate}.
	 * @param ctx the parse tree
	 */
	void exitRestDescriptorPredicate(BallerinaParser.RestDescriptorPredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#objectMethod}.
	 * @param ctx the parse tree
	 */
	void enterObjectMethod(BallerinaParser.ObjectMethodContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#objectMethod}.
	 * @param ctx the parse tree
	 */
	void exitObjectMethod(BallerinaParser.ObjectMethodContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMethodDeclaration(BallerinaParser.MethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMethodDeclaration(BallerinaParser.MethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#methodDefinition}.
	 * @param ctx the parse tree
	 */
	void enterMethodDefinition(BallerinaParser.MethodDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#methodDefinition}.
	 * @param ctx the parse tree
	 */
	void exitMethodDefinition(BallerinaParser.MethodDefinitionContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#constantDefinition}.
	 * @param ctx the parse tree
	 */
	void enterConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#constantDefinition}.
	 * @param ctx the parse tree
	 */
	void exitConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#enumDefinition}.
	 * @param ctx the parse tree
	 */
	void enterEnumDefinition(BallerinaParser.EnumDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#enumDefinition}.
	 * @param ctx the parse tree
	 */
	void exitEnumDefinition(BallerinaParser.EnumDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#enumMember}.
	 * @param ctx the parse tree
	 */
	void enterEnumMember(BallerinaParser.EnumMemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#enumMember}.
	 * @param ctx the parse tree
	 */
	void exitEnumMember(BallerinaParser.EnumMemberContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#dualAttachPoint}.
	 * @param ctx the parse tree
	 */
	void enterDualAttachPoint(BallerinaParser.DualAttachPointContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#dualAttachPoint}.
	 * @param ctx the parse tree
	 */
	void exitDualAttachPoint(BallerinaParser.DualAttachPointContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#dualAttachPointIdent}.
	 * @param ctx the parse tree
	 */
	void enterDualAttachPointIdent(BallerinaParser.DualAttachPointIdentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#dualAttachPointIdent}.
	 * @param ctx the parse tree
	 */
	void exitDualAttachPointIdent(BallerinaParser.DualAttachPointIdentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#sourceOnlyAttachPoint}.
	 * @param ctx the parse tree
	 */
	void enterSourceOnlyAttachPoint(BallerinaParser.SourceOnlyAttachPointContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#sourceOnlyAttachPoint}.
	 * @param ctx the parse tree
	 */
	void exitSourceOnlyAttachPoint(BallerinaParser.SourceOnlyAttachPointContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#sourceOnlyAttachPointIdent}.
	 * @param ctx the parse tree
	 */
	void enterSourceOnlyAttachPointIdent(BallerinaParser.SourceOnlyAttachPointIdentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#sourceOnlyAttachPointIdent}.
	 * @param ctx the parse tree
	 */
	void exitSourceOnlyAttachPointIdent(BallerinaParser.SourceOnlyAttachPointIdentContext ctx);
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
	 * Enter a parse tree produced by the {@code exclusiveRecordTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterExclusiveRecordTypeNameLabel(BallerinaParser.ExclusiveRecordTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exclusiveRecordTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitExclusiveRecordTypeNameLabel(BallerinaParser.ExclusiveRecordTypeNameLabelContext ctx);
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
	 * Enter a parse tree produced by the {@code tableTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTableTypeNameLabel(BallerinaParser.TableTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTableTypeNameLabel(BallerinaParser.TableTypeNameLabelContext ctx);
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
	 * Enter a parse tree produced by the {@code intersectionTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterIntersectionTypeNameLabel(BallerinaParser.IntersectionTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intersectionTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitIntersectionTypeNameLabel(BallerinaParser.IntersectionTypeNameLabelContext ctx);
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
	 * Enter a parse tree produced by the {@code inclusiveRecordTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveRecordTypeNameLabel(BallerinaParser.InclusiveRecordTypeNameLabelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code inclusiveRecordTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveRecordTypeNameLabel(BallerinaParser.InclusiveRecordTypeNameLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#inclusiveRecordTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveRecordTypeDescriptor(BallerinaParser.InclusiveRecordTypeDescriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#inclusiveRecordTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveRecordTypeDescriptor(BallerinaParser.InclusiveRecordTypeDescriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tupleTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void enterTupleTypeDescriptor(BallerinaParser.TupleTypeDescriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tupleTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void exitTupleTypeDescriptor(BallerinaParser.TupleTypeDescriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tupleRestDescriptor}.
	 * @param ctx the parse tree
	 */
	void enterTupleRestDescriptor(BallerinaParser.TupleRestDescriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tupleRestDescriptor}.
	 * @param ctx the parse tree
	 */
	void exitTupleRestDescriptor(BallerinaParser.TupleRestDescriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#exclusiveRecordTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void enterExclusiveRecordTypeDescriptor(BallerinaParser.ExclusiveRecordTypeDescriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#exclusiveRecordTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void exitExclusiveRecordTypeDescriptor(BallerinaParser.ExclusiveRecordTypeDescriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#fieldDescriptor}.
	 * @param ctx the parse tree
	 */
	void enterFieldDescriptor(BallerinaParser.FieldDescriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#fieldDescriptor}.
	 * @param ctx the parse tree
	 */
	void exitFieldDescriptor(BallerinaParser.FieldDescriptorContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#streamTypeName}.
	 * @param ctx the parse tree
	 */
	void enterStreamTypeName(BallerinaParser.StreamTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#streamTypeName}.
	 * @param ctx the parse tree
	 */
	void exitStreamTypeName(BallerinaParser.StreamTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tableConstructorExpr}.
	 * @param ctx the parse tree
	 */
	void enterTableConstructorExpr(BallerinaParser.TableConstructorExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tableConstructorExpr}.
	 * @param ctx the parse tree
	 */
	void exitTableConstructorExpr(BallerinaParser.TableConstructorExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tableRowList}.
	 * @param ctx the parse tree
	 */
	void enterTableRowList(BallerinaParser.TableRowListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tableRowList}.
	 * @param ctx the parse tree
	 */
	void exitTableRowList(BallerinaParser.TableRowListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tableTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void enterTableTypeDescriptor(BallerinaParser.TableTypeDescriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tableTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void exitTableTypeDescriptor(BallerinaParser.TableTypeDescriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tableKeyConstraint}.
	 * @param ctx the parse tree
	 */
	void enterTableKeyConstraint(BallerinaParser.TableKeyConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tableKeyConstraint}.
	 * @param ctx the parse tree
	 */
	void exitTableKeyConstraint(BallerinaParser.TableKeyConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tableKeySpecifier}.
	 * @param ctx the parse tree
	 */
	void enterTableKeySpecifier(BallerinaParser.TableKeySpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tableKeySpecifier}.
	 * @param ctx the parse tree
	 */
	void exitTableKeySpecifier(BallerinaParser.TableKeySpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tableKeyTypeConstraint}.
	 * @param ctx the parse tree
	 */
	void enterTableKeyTypeConstraint(BallerinaParser.TableKeyTypeConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tableKeyTypeConstraint}.
	 * @param ctx the parse tree
	 */
	void exitTableKeyTypeConstraint(BallerinaParser.TableKeyTypeConstraintContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#errorTypeName}.
	 * @param ctx the parse tree
	 */
	void enterErrorTypeName(BallerinaParser.ErrorTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorTypeName}.
	 * @param ctx the parse tree
	 */
	void exitErrorTypeName(BallerinaParser.ErrorTypeNameContext ctx);
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
	 * Enter a parse tree produced by the {@code staticMatchRecordLiteral}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStaticMatchRecordLiteral(BallerinaParser.StaticMatchRecordLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code staticMatchRecordLiteral}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStaticMatchRecordLiteral(BallerinaParser.StaticMatchRecordLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code staticMatchListLiteral}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStaticMatchListLiteral(BallerinaParser.StaticMatchListLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code staticMatchListLiteral}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStaticMatchListLiteral(BallerinaParser.StaticMatchListLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code staticMatchIdentifierLiteral}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStaticMatchIdentifierLiteral(BallerinaParser.StaticMatchIdentifierLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code staticMatchIdentifierLiteral}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStaticMatchIdentifierLiteral(BallerinaParser.StaticMatchIdentifierLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code staticMatchOrExpression}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStaticMatchOrExpression(BallerinaParser.StaticMatchOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code staticMatchOrExpression}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStaticMatchOrExpression(BallerinaParser.StaticMatchOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code staticMatchSimpleLiteral}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStaticMatchSimpleLiteral(BallerinaParser.StaticMatchSimpleLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code staticMatchSimpleLiteral}
	 * labeled alternative in {@link BallerinaParser#staticMatchLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStaticMatchSimpleLiteral(BallerinaParser.StaticMatchSimpleLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#recordField}.
	 * @param ctx the parse tree
	 */
	void enterRecordField(BallerinaParser.RecordFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#recordField}.
	 * @param ctx the parse tree
	 */
	void exitRecordField(BallerinaParser.RecordFieldContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#listConstructorExpr}.
	 * @param ctx the parse tree
	 */
	void enterListConstructorExpr(BallerinaParser.ListConstructorExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#listConstructorExpr}.
	 * @param ctx the parse tree
	 */
	void exitListConstructorExpr(BallerinaParser.ListConstructorExprContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#listDestructuringStatement}.
	 * @param ctx the parse tree
	 */
	void enterListDestructuringStatement(BallerinaParser.ListDestructuringStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#listDestructuringStatement}.
	 * @param ctx the parse tree
	 */
	void exitListDestructuringStatement(BallerinaParser.ListDestructuringStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#recordDestructuringStatement}.
	 * @param ctx the parse tree
	 */
	void enterRecordDestructuringStatement(BallerinaParser.RecordDestructuringStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#recordDestructuringStatement}.
	 * @param ctx the parse tree
	 */
	void exitRecordDestructuringStatement(BallerinaParser.RecordDestructuringStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorDestructuringStatement}.
	 * @param ctx the parse tree
	 */
	void enterErrorDestructuringStatement(BallerinaParser.ErrorDestructuringStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorDestructuringStatement}.
	 * @param ctx the parse tree
	 */
	void exitErrorDestructuringStatement(BallerinaParser.ErrorDestructuringStatementContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#bindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterBindingPattern(BallerinaParser.BindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#bindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitBindingPattern(BallerinaParser.BindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#structuredBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterStructuredBindingPattern(BallerinaParser.StructuredBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#structuredBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitStructuredBindingPattern(BallerinaParser.StructuredBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterErrorBindingPattern(BallerinaParser.ErrorBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitErrorBindingPattern(BallerinaParser.ErrorBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorBindingPatternParamaters}.
	 * @param ctx the parse tree
	 */
	void enterErrorBindingPatternParamaters(BallerinaParser.ErrorBindingPatternParamatersContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorBindingPatternParamaters}.
	 * @param ctx the parse tree
	 */
	void exitErrorBindingPatternParamaters(BallerinaParser.ErrorBindingPatternParamatersContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorMatchPattern}.
	 * @param ctx the parse tree
	 */
	void enterErrorMatchPattern(BallerinaParser.ErrorMatchPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorMatchPattern}.
	 * @param ctx the parse tree
	 */
	void exitErrorMatchPattern(BallerinaParser.ErrorMatchPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorArgListMatchPattern}.
	 * @param ctx the parse tree
	 */
	void enterErrorArgListMatchPattern(BallerinaParser.ErrorArgListMatchPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorArgListMatchPattern}.
	 * @param ctx the parse tree
	 */
	void exitErrorArgListMatchPattern(BallerinaParser.ErrorArgListMatchPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorFieldMatchPatterns}.
	 * @param ctx the parse tree
	 */
	void enterErrorFieldMatchPatterns(BallerinaParser.ErrorFieldMatchPatternsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorFieldMatchPatterns}.
	 * @param ctx the parse tree
	 */
	void exitErrorFieldMatchPatterns(BallerinaParser.ErrorFieldMatchPatternsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorRestBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterErrorRestBindingPattern(BallerinaParser.ErrorRestBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorRestBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitErrorRestBindingPattern(BallerinaParser.ErrorRestBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#restMatchPattern}.
	 * @param ctx the parse tree
	 */
	void enterRestMatchPattern(BallerinaParser.RestMatchPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#restMatchPattern}.
	 * @param ctx the parse tree
	 */
	void exitRestMatchPattern(BallerinaParser.RestMatchPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#simpleMatchPattern}.
	 * @param ctx the parse tree
	 */
	void enterSimpleMatchPattern(BallerinaParser.SimpleMatchPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#simpleMatchPattern}.
	 * @param ctx the parse tree
	 */
	void exitSimpleMatchPattern(BallerinaParser.SimpleMatchPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorDetailBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterErrorDetailBindingPattern(BallerinaParser.ErrorDetailBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorDetailBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitErrorDetailBindingPattern(BallerinaParser.ErrorDetailBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#listBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterListBindingPattern(BallerinaParser.ListBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#listBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitListBindingPattern(BallerinaParser.ListBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#recordBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterRecordBindingPattern(BallerinaParser.RecordBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#recordBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitRecordBindingPattern(BallerinaParser.RecordBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#entryBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterEntryBindingPattern(BallerinaParser.EntryBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#entryBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitEntryBindingPattern(BallerinaParser.EntryBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#fieldBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterFieldBindingPattern(BallerinaParser.FieldBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#fieldBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitFieldBindingPattern(BallerinaParser.FieldBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#restBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterRestBindingPattern(BallerinaParser.RestBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#restBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitRestBindingPattern(BallerinaParser.RestBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#bindingRefPattern}.
	 * @param ctx the parse tree
	 */
	void enterBindingRefPattern(BallerinaParser.BindingRefPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#bindingRefPattern}.
	 * @param ctx the parse tree
	 */
	void exitBindingRefPattern(BallerinaParser.BindingRefPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#structuredRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterStructuredRefBindingPattern(BallerinaParser.StructuredRefBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#structuredRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitStructuredRefBindingPattern(BallerinaParser.StructuredRefBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#listRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterListRefBindingPattern(BallerinaParser.ListRefBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#listRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitListRefBindingPattern(BallerinaParser.ListRefBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#listRefRestPattern}.
	 * @param ctx the parse tree
	 */
	void enterListRefRestPattern(BallerinaParser.ListRefRestPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#listRefRestPattern}.
	 * @param ctx the parse tree
	 */
	void exitListRefRestPattern(BallerinaParser.ListRefRestPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#recordRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterRecordRefBindingPattern(BallerinaParser.RecordRefBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#recordRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitRecordRefBindingPattern(BallerinaParser.RecordRefBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterErrorRefBindingPattern(BallerinaParser.ErrorRefBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitErrorRefBindingPattern(BallerinaParser.ErrorRefBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorRefArgsPattern}.
	 * @param ctx the parse tree
	 */
	void enterErrorRefArgsPattern(BallerinaParser.ErrorRefArgsPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorRefArgsPattern}.
	 * @param ctx the parse tree
	 */
	void exitErrorRefArgsPattern(BallerinaParser.ErrorRefArgsPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorNamedArgRefPattern}.
	 * @param ctx the parse tree
	 */
	void enterErrorNamedArgRefPattern(BallerinaParser.ErrorNamedArgRefPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorNamedArgRefPattern}.
	 * @param ctx the parse tree
	 */
	void exitErrorNamedArgRefPattern(BallerinaParser.ErrorNamedArgRefPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#errorRefRestPattern}.
	 * @param ctx the parse tree
	 */
	void enterErrorRefRestPattern(BallerinaParser.ErrorRefRestPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#errorRefRestPattern}.
	 * @param ctx the parse tree
	 */
	void exitErrorRefRestPattern(BallerinaParser.ErrorRefRestPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#entryRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterEntryRefBindingPattern(BallerinaParser.EntryRefBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#entryRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitEntryRefBindingPattern(BallerinaParser.EntryRefBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#fieldRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterFieldRefBindingPattern(BallerinaParser.FieldRefBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#fieldRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitFieldRefBindingPattern(BallerinaParser.FieldRefBindingPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#restRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void enterRestRefBindingPattern(BallerinaParser.RestRefBindingPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#restRefBindingPattern}.
	 * @param ctx the parse tree
	 */
	void exitRestRefBindingPattern(BallerinaParser.RestRefBindingPatternContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#continueStatement}.
	 * @param ctx the parse tree
	 */
	void enterContinueStatement(BallerinaParser.ContinueStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#continueStatement}.
	 * @param ctx the parse tree
	 */
	void exitContinueStatement(BallerinaParser.ContinueStatementContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#panicStatement}.
	 * @param ctx the parse tree
	 */
	void enterPanicStatement(BallerinaParser.PanicStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#panicStatement}.
	 * @param ctx the parse tree
	 */
	void exitPanicStatement(BallerinaParser.PanicStatementContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#workerSendAsyncStatement}.
	 * @param ctx the parse tree
	 */
	void enterWorkerSendAsyncStatement(BallerinaParser.WorkerSendAsyncStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#workerSendAsyncStatement}.
	 * @param ctx the parse tree
	 */
	void exitWorkerSendAsyncStatement(BallerinaParser.WorkerSendAsyncStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#peerWorker}.
	 * @param ctx the parse tree
	 */
	void enterPeerWorker(BallerinaParser.PeerWorkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#peerWorker}.
	 * @param ctx the parse tree
	 */
	void exitPeerWorker(BallerinaParser.PeerWorkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#workerName}.
	 * @param ctx the parse tree
	 */
	void enterWorkerName(BallerinaParser.WorkerNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#workerName}.
	 * @param ctx the parse tree
	 */
	void exitWorkerName(BallerinaParser.WorkerNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#flushWorker}.
	 * @param ctx the parse tree
	 */
	void enterFlushWorker(BallerinaParser.FlushWorkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#flushWorker}.
	 * @param ctx the parse tree
	 */
	void exitFlushWorker(BallerinaParser.FlushWorkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#waitForCollection}.
	 * @param ctx the parse tree
	 */
	void enterWaitForCollection(BallerinaParser.WaitForCollectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#waitForCollection}.
	 * @param ctx the parse tree
	 */
	void exitWaitForCollection(BallerinaParser.WaitForCollectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#waitKeyValue}.
	 * @param ctx the parse tree
	 */
	void enterWaitKeyValue(BallerinaParser.WaitKeyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#waitKeyValue}.
	 * @param ctx the parse tree
	 */
	void exitWaitKeyValue(BallerinaParser.WaitKeyValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code groupMapArrayVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterGroupMapArrayVariableReference(BallerinaParser.GroupMapArrayVariableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code groupMapArrayVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitGroupMapArrayVariableReference(BallerinaParser.GroupMapArrayVariableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code xmlStepExpressionReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterXmlStepExpressionReference(BallerinaParser.XmlStepExpressionReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code xmlStepExpressionReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitXmlStepExpressionReference(BallerinaParser.XmlStepExpressionReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code groupInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterGroupInvocationReference(BallerinaParser.GroupInvocationReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code groupInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitGroupInvocationReference(BallerinaParser.GroupInvocationReferenceContext ctx);
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
	 * Enter a parse tree produced by the {@code xmlElementFilterReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterXmlElementFilterReference(BallerinaParser.XmlElementFilterReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code xmlElementFilterReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitXmlElementFilterReference(BallerinaParser.XmlElementFilterReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code groupFieldVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterGroupFieldVariableReference(BallerinaParser.GroupFieldVariableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code groupFieldVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitGroupFieldVariableReference(BallerinaParser.GroupFieldVariableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeDescExprInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterTypeDescExprInvocationReference(BallerinaParser.TypeDescExprInvocationReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeDescExprInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitTypeDescExprInvocationReference(BallerinaParser.TypeDescExprInvocationReferenceContext ctx);
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
	 * Enter a parse tree produced by the {@code groupStringFunctionInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterGroupStringFunctionInvocationReference(BallerinaParser.GroupStringFunctionInvocationReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code groupStringFunctionInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitGroupStringFunctionInvocationReference(BallerinaParser.GroupStringFunctionInvocationReferenceContext ctx);
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
	 * Enter a parse tree produced by the {@code annotAccessExpression}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterAnnotAccessExpression(BallerinaParser.AnnotAccessExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code annotAccessExpression}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitAnnotAccessExpression(BallerinaParser.AnnotAccessExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code stringFunctionInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterStringFunctionInvocationReference(BallerinaParser.StringFunctionInvocationReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringFunctionInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitStringFunctionInvocationReference(BallerinaParser.StringFunctionInvocationReferenceContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#xmlElementFilter}.
	 * @param ctx the parse tree
	 */
	void enterXmlElementFilter(BallerinaParser.XmlElementFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlElementFilter}.
	 * @param ctx the parse tree
	 */
	void exitXmlElementFilter(BallerinaParser.XmlElementFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlStepExpression}.
	 * @param ctx the parse tree
	 */
	void enterXmlStepExpression(BallerinaParser.XmlStepExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlStepExpression}.
	 * @param ctx the parse tree
	 */
	void exitXmlStepExpression(BallerinaParser.XmlStepExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlElementNames}.
	 * @param ctx the parse tree
	 */
	void enterXmlElementNames(BallerinaParser.XmlElementNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlElementNames}.
	 * @param ctx the parse tree
	 */
	void exitXmlElementNames(BallerinaParser.XmlElementNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#xmlElementAccessFilter}.
	 * @param ctx the parse tree
	 */
	void enterXmlElementAccessFilter(BallerinaParser.XmlElementAccessFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#xmlElementAccessFilter}.
	 * @param ctx the parse tree
	 */
	void exitXmlElementAccessFilter(BallerinaParser.XmlElementAccessFilterContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#multiKeyIndex}.
	 * @param ctx the parse tree
	 */
	void enterMultiKeyIndex(BallerinaParser.MultiKeyIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#multiKeyIndex}.
	 * @param ctx the parse tree
	 */
	void exitMultiKeyIndex(BallerinaParser.MultiKeyIndexContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#committedAbortedClauses}.
	 * @param ctx the parse tree
	 */
	void enterCommittedAbortedClauses(BallerinaParser.CommittedAbortedClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#committedAbortedClauses}.
	 * @param ctx the parse tree
	 */
	void exitCommittedAbortedClauses(BallerinaParser.CommittedAbortedClausesContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#committedClause}.
	 * @param ctx the parse tree
	 */
	void enterCommittedClause(BallerinaParser.CommittedClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#committedClause}.
	 * @param ctx the parse tree
	 */
	void exitCommittedClause(BallerinaParser.CommittedClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#abortedClause}.
	 * @param ctx the parse tree
	 */
	void enterAbortedClause(BallerinaParser.AbortedClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#abortedClause}.
	 * @param ctx the parse tree
	 */
	void exitAbortedClause(BallerinaParser.AbortedClauseContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#retryStatement}.
	 * @param ctx the parse tree
	 */
	void enterRetryStatement(BallerinaParser.RetryStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#retryStatement}.
	 * @param ctx the parse tree
	 */
	void exitRetryStatement(BallerinaParser.RetryStatementContext ctx);
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
	 * Enter a parse tree produced by the {@code tableConstructorExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTableConstructorExpression(BallerinaParser.TableConstructorExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableConstructorExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTableConstructorExpression(BallerinaParser.TableConstructorExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code flushWorkerExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFlushWorkerExpression(BallerinaParser.FlushWorkerExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code flushWorkerExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFlushWorkerExpression(BallerinaParser.FlushWorkerExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code serviceConstructorExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterServiceConstructorExpression(BallerinaParser.ServiceConstructorExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code serviceConstructorExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitServiceConstructorExpression(BallerinaParser.ServiceConstructorExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code explicitAnonymousFunctionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExplicitAnonymousFunctionExpression(BallerinaParser.ExplicitAnonymousFunctionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code explicitAnonymousFunctionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExplicitAnonymousFunctionExpression(BallerinaParser.ExplicitAnonymousFunctionExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code inferAnonymousFunctionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterInferAnonymousFunctionExpression(BallerinaParser.InferAnonymousFunctionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code inferAnonymousFunctionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitInferAnonymousFunctionExpression(BallerinaParser.InferAnonymousFunctionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code workerReceiveExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterWorkerReceiveExpression(BallerinaParser.WorkerReceiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code workerReceiveExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitWorkerReceiveExpression(BallerinaParser.WorkerReceiveExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code groupExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterGroupExpression(BallerinaParser.GroupExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code groupExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitGroupExpression(BallerinaParser.GroupExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bitwiseShiftExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBitwiseShiftExpression(BallerinaParser.BitwiseShiftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bitwiseShiftExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBitwiseShiftExpression(BallerinaParser.BitwiseShiftExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code workerSendSyncExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterWorkerSendSyncExpression(BallerinaParser.WorkerSendSyncExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code workerSendSyncExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitWorkerSendSyncExpression(BallerinaParser.WorkerSendSyncExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code letExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLetExpression(BallerinaParser.LetExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code letExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLetExpression(BallerinaParser.LetExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code checkPanickedExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCheckPanickedExpression(BallerinaParser.CheckPanickedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code checkPanickedExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCheckPanickedExpression(BallerinaParser.CheckPanickedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bitwiseExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBitwiseExpression(BallerinaParser.BitwiseExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bitwiseExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBitwiseExpression(BallerinaParser.BitwiseExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code typeTestExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTypeTestExpression(BallerinaParser.TypeTestExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeTestExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTypeTestExpression(BallerinaParser.TypeTestExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code waitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterWaitExpression(BallerinaParser.WaitExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code waitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitWaitExpression(BallerinaParser.WaitExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code trapExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTrapExpression(BallerinaParser.TrapExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code trapExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTrapExpression(BallerinaParser.TrapExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryEqualsExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryEqualsExpression(BallerinaParser.BinaryEqualsExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryEqualsExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryEqualsExpression(BallerinaParser.BinaryEqualsExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code queryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterQueryExpression(BallerinaParser.QueryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code queryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitQueryExpression(BallerinaParser.QueryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryRefEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryRefEqualExpression(BallerinaParser.BinaryRefEqualExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryRefEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryRefEqualExpression(BallerinaParser.BinaryRefEqualExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code listConstructorExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterListConstructorExpression(BallerinaParser.ListConstructorExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code listConstructorExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitListConstructorExpression(BallerinaParser.ListConstructorExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code actionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterActionInvocationExpression(BallerinaParser.ActionInvocationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code actionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitActionInvocationExpression(BallerinaParser.ActionInvocationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code queryActionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterQueryActionExpression(BallerinaParser.QueryActionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code queryActionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitQueryActionExpression(BallerinaParser.QueryActionExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code integerRangeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIntegerRangeExpression(BallerinaParser.IntegerRangeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code integerRangeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIntegerRangeExpression(BallerinaParser.IntegerRangeExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code constSimpleLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstSimpleLiteralExpression(BallerinaParser.ConstSimpleLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constSimpleLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstSimpleLiteralExpression(BallerinaParser.ConstSimpleLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constGroupExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstGroupExpression(BallerinaParser.ConstGroupExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constGroupExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstGroupExpression(BallerinaParser.ConstGroupExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constDivMulModExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstDivMulModExpression(BallerinaParser.ConstDivMulModExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constDivMulModExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstDivMulModExpression(BallerinaParser.ConstDivMulModExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constRecordLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstRecordLiteralExpression(BallerinaParser.ConstRecordLiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constRecordLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstRecordLiteralExpression(BallerinaParser.ConstRecordLiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constAddSubExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstAddSubExpression(BallerinaParser.ConstAddSubExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constAddSubExpression}
	 * labeled alternative in {@link BallerinaParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstAddSubExpression(BallerinaParser.ConstAddSubExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#letExpr}.
	 * @param ctx the parse tree
	 */
	void enterLetExpr(BallerinaParser.LetExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#letExpr}.
	 * @param ctx the parse tree
	 */
	void exitLetExpr(BallerinaParser.LetExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#letVarDecl}.
	 * @param ctx the parse tree
	 */
	void enterLetVarDecl(BallerinaParser.LetVarDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#letVarDecl}.
	 * @param ctx the parse tree
	 */
	void exitLetVarDecl(BallerinaParser.LetVarDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeDescExpr}.
	 * @param ctx the parse tree
	 */
	void enterTypeDescExpr(BallerinaParser.TypeDescExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeDescExpr}.
	 * @param ctx the parse tree
	 */
	void exitTypeDescExpr(BallerinaParser.TypeDescExprContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#serviceConstructorExpr}.
	 * @param ctx the parse tree
	 */
	void enterServiceConstructorExpr(BallerinaParser.ServiceConstructorExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#serviceConstructorExpr}.
	 * @param ctx the parse tree
	 */
	void exitServiceConstructorExpr(BallerinaParser.ServiceConstructorExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#trapExpr}.
	 * @param ctx the parse tree
	 */
	void enterTrapExpr(BallerinaParser.TrapExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#trapExpr}.
	 * @param ctx the parse tree
	 */
	void exitTrapExpr(BallerinaParser.TrapExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void enterShiftExpression(BallerinaParser.ShiftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void exitShiftExpression(BallerinaParser.ShiftExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#shiftExprPredicate}.
	 * @param ctx the parse tree
	 */
	void enterShiftExprPredicate(BallerinaParser.ShiftExprPredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#shiftExprPredicate}.
	 * @param ctx the parse tree
	 */
	void exitShiftExprPredicate(BallerinaParser.ShiftExprPredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void enterLimitClause(BallerinaParser.LimitClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void exitLimitClause(BallerinaParser.LimitClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#onConflictClause}.
	 * @param ctx the parse tree
	 */
	void enterOnConflictClause(BallerinaParser.OnConflictClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#onConflictClause}.
	 * @param ctx the parse tree
	 */
	void exitOnConflictClause(BallerinaParser.OnConflictClauseContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#onClause}.
	 * @param ctx the parse tree
	 */
	void enterOnClause(BallerinaParser.OnClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#onClause}.
	 * @param ctx the parse tree
	 */
	void exitOnClause(BallerinaParser.OnClauseContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#letClause}.
	 * @param ctx the parse tree
	 */
	void enterLetClause(BallerinaParser.LetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#letClause}.
	 * @param ctx the parse tree
	 */
	void exitLetClause(BallerinaParser.LetClauseContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(BallerinaParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(BallerinaParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#doClause}.
	 * @param ctx the parse tree
	 */
	void enterDoClause(BallerinaParser.DoClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#doClause}.
	 * @param ctx the parse tree
	 */
	void exitDoClause(BallerinaParser.DoClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#queryPipeline}.
	 * @param ctx the parse tree
	 */
	void enterQueryPipeline(BallerinaParser.QueryPipelineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#queryPipeline}.
	 * @param ctx the parse tree
	 */
	void exitQueryPipeline(BallerinaParser.QueryPipelineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#queryConstructType}.
	 * @param ctx the parse tree
	 */
	void enterQueryConstructType(BallerinaParser.QueryConstructTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#queryConstructType}.
	 * @param ctx the parse tree
	 */
	void exitQueryConstructType(BallerinaParser.QueryConstructTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#queryExpr}.
	 * @param ctx the parse tree
	 */
	void enterQueryExpr(BallerinaParser.QueryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#queryExpr}.
	 * @param ctx the parse tree
	 */
	void exitQueryExpr(BallerinaParser.QueryExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#queryAction}.
	 * @param ctx the parse tree
	 */
	void enterQueryAction(BallerinaParser.QueryActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#queryAction}.
	 * @param ctx the parse tree
	 */
	void exitQueryAction(BallerinaParser.QueryActionContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#functionNameReference}.
	 * @param ctx the parse tree
	 */
	void enterFunctionNameReference(BallerinaParser.FunctionNameReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionNameReference}.
	 * @param ctx the parse tree
	 */
	void exitFunctionNameReference(BallerinaParser.FunctionNameReferenceContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(BallerinaParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(BallerinaParser.ParameterContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#restParameterTypeName}.
	 * @param ctx the parse tree
	 */
	void enterRestParameterTypeName(BallerinaParser.RestParameterTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#restParameterTypeName}.
	 * @param ctx the parse tree
	 */
	void exitRestParameterTypeName(BallerinaParser.RestParameterTypeNameContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#floatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFloatingPointLiteral(BallerinaParser.FloatingPointLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#floatingPointLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFloatingPointLiteral(BallerinaParser.FloatingPointLiteralContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#nilLiteral}.
	 * @param ctx the parse tree
	 */
	void enterNilLiteral(BallerinaParser.NilLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#nilLiteral}.
	 * @param ctx the parse tree
	 */
	void exitNilLiteral(BallerinaParser.NilLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#blobLiteral}.
	 * @param ctx the parse tree
	 */
	void enterBlobLiteral(BallerinaParser.BlobLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#blobLiteral}.
	 * @param ctx the parse tree
	 */
	void exitBlobLiteral(BallerinaParser.BlobLiteralContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#documentationString}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationString(BallerinaParser.DocumentationStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationString}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationString(BallerinaParser.DocumentationStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationLine}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationLine(BallerinaParser.DocumentationLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationLine}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationLine(BallerinaParser.DocumentationLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#parameterDocumentationLine}.
	 * @param ctx the parse tree
	 */
	void enterParameterDocumentationLine(BallerinaParser.ParameterDocumentationLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#parameterDocumentationLine}.
	 * @param ctx the parse tree
	 */
	void exitParameterDocumentationLine(BallerinaParser.ParameterDocumentationLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#returnParameterDocumentationLine}.
	 * @param ctx the parse tree
	 */
	void enterReturnParameterDocumentationLine(BallerinaParser.ReturnParameterDocumentationLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#returnParameterDocumentationLine}.
	 * @param ctx the parse tree
	 */
	void exitReturnParameterDocumentationLine(BallerinaParser.ReturnParameterDocumentationLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#deprecatedAnnotationDocumentationLine}.
	 * @param ctx the parse tree
	 */
	void enterDeprecatedAnnotationDocumentationLine(BallerinaParser.DeprecatedAnnotationDocumentationLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#deprecatedAnnotationDocumentationLine}.
	 * @param ctx the parse tree
	 */
	void exitDeprecatedAnnotationDocumentationLine(BallerinaParser.DeprecatedAnnotationDocumentationLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#deprecatedParametersDocumentationLine}.
	 * @param ctx the parse tree
	 */
	void enterDeprecatedParametersDocumentationLine(BallerinaParser.DeprecatedParametersDocumentationLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#deprecatedParametersDocumentationLine}.
	 * @param ctx the parse tree
	 */
	void exitDeprecatedParametersDocumentationLine(BallerinaParser.DeprecatedParametersDocumentationLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationContent}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationContent(BallerinaParser.DocumentationContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationContent}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationContent(BallerinaParser.DocumentationContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#parameterDescriptionLine}.
	 * @param ctx the parse tree
	 */
	void enterParameterDescriptionLine(BallerinaParser.ParameterDescriptionLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#parameterDescriptionLine}.
	 * @param ctx the parse tree
	 */
	void exitParameterDescriptionLine(BallerinaParser.ParameterDescriptionLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#returnParameterDescriptionLine}.
	 * @param ctx the parse tree
	 */
	void enterReturnParameterDescriptionLine(BallerinaParser.ReturnParameterDescriptionLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#returnParameterDescriptionLine}.
	 * @param ctx the parse tree
	 */
	void exitReturnParameterDescriptionLine(BallerinaParser.ReturnParameterDescriptionLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#deprecateAnnotationDescriptionLine}.
	 * @param ctx the parse tree
	 */
	void enterDeprecateAnnotationDescriptionLine(BallerinaParser.DeprecateAnnotationDescriptionLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#deprecateAnnotationDescriptionLine}.
	 * @param ctx the parse tree
	 */
	void exitDeprecateAnnotationDescriptionLine(BallerinaParser.DeprecateAnnotationDescriptionLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationText}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationText(BallerinaParser.DocumentationTextContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationText}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationText(BallerinaParser.DocumentationTextContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationReference}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationReference(BallerinaParser.DocumentationReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationReference}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationReference(BallerinaParser.DocumentationReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#referenceType}.
	 * @param ctx the parse tree
	 */
	void enterReferenceType(BallerinaParser.ReferenceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#referenceType}.
	 * @param ctx the parse tree
	 */
	void exitReferenceType(BallerinaParser.ReferenceTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#parameterDocumentation}.
	 * @param ctx the parse tree
	 */
	void enterParameterDocumentation(BallerinaParser.ParameterDocumentationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#parameterDocumentation}.
	 * @param ctx the parse tree
	 */
	void exitParameterDocumentation(BallerinaParser.ParameterDocumentationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#returnParameterDocumentation}.
	 * @param ctx the parse tree
	 */
	void enterReturnParameterDocumentation(BallerinaParser.ReturnParameterDocumentationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#returnParameterDocumentation}.
	 * @param ctx the parse tree
	 */
	void exitReturnParameterDocumentation(BallerinaParser.ReturnParameterDocumentationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#deprecatedAnnotationDocumentation}.
	 * @param ctx the parse tree
	 */
	void enterDeprecatedAnnotationDocumentation(BallerinaParser.DeprecatedAnnotationDocumentationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#deprecatedAnnotationDocumentation}.
	 * @param ctx the parse tree
	 */
	void exitDeprecatedAnnotationDocumentation(BallerinaParser.DeprecatedAnnotationDocumentationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#deprecatedParametersDocumentation}.
	 * @param ctx the parse tree
	 */
	void enterDeprecatedParametersDocumentation(BallerinaParser.DeprecatedParametersDocumentationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#deprecatedParametersDocumentation}.
	 * @param ctx the parse tree
	 */
	void exitDeprecatedParametersDocumentation(BallerinaParser.DeprecatedParametersDocumentationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#docParameterName}.
	 * @param ctx the parse tree
	 */
	void enterDocParameterName(BallerinaParser.DocParameterNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#docParameterName}.
	 * @param ctx the parse tree
	 */
	void exitDocParameterName(BallerinaParser.DocParameterNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#singleBacktickedBlock}.
	 * @param ctx the parse tree
	 */
	void enterSingleBacktickedBlock(BallerinaParser.SingleBacktickedBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#singleBacktickedBlock}.
	 * @param ctx the parse tree
	 */
	void exitSingleBacktickedBlock(BallerinaParser.SingleBacktickedBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#singleBacktickedContent}.
	 * @param ctx the parse tree
	 */
	void enterSingleBacktickedContent(BallerinaParser.SingleBacktickedContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#singleBacktickedContent}.
	 * @param ctx the parse tree
	 */
	void exitSingleBacktickedContent(BallerinaParser.SingleBacktickedContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#doubleBacktickedBlock}.
	 * @param ctx the parse tree
	 */
	void enterDoubleBacktickedBlock(BallerinaParser.DoubleBacktickedBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#doubleBacktickedBlock}.
	 * @param ctx the parse tree
	 */
	void exitDoubleBacktickedBlock(BallerinaParser.DoubleBacktickedBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#doubleBacktickedContent}.
	 * @param ctx the parse tree
	 */
	void enterDoubleBacktickedContent(BallerinaParser.DoubleBacktickedContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#doubleBacktickedContent}.
	 * @param ctx the parse tree
	 */
	void exitDoubleBacktickedContent(BallerinaParser.DoubleBacktickedContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tripleBacktickedBlock}.
	 * @param ctx the parse tree
	 */
	void enterTripleBacktickedBlock(BallerinaParser.TripleBacktickedBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tripleBacktickedBlock}.
	 * @param ctx the parse tree
	 */
	void exitTripleBacktickedBlock(BallerinaParser.TripleBacktickedBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tripleBacktickedContent}.
	 * @param ctx the parse tree
	 */
	void enterTripleBacktickedContent(BallerinaParser.TripleBacktickedContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tripleBacktickedContent}.
	 * @param ctx the parse tree
	 */
	void exitTripleBacktickedContent(BallerinaParser.TripleBacktickedContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationTextContent}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationTextContent(BallerinaParser.DocumentationTextContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationTextContent}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationTextContent(BallerinaParser.DocumentationTextContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationFullyqualifiedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationFullyqualifiedIdentifier(BallerinaParser.DocumentationFullyqualifiedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationFullyqualifiedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationFullyqualifiedIdentifier(BallerinaParser.DocumentationFullyqualifiedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationFullyqualifiedFunctionIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationFullyqualifiedFunctionIdentifier(BallerinaParser.DocumentationFullyqualifiedFunctionIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationFullyqualifiedFunctionIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationFullyqualifiedFunctionIdentifier(BallerinaParser.DocumentationFullyqualifiedFunctionIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationIdentifierQualifier}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationIdentifierQualifier(BallerinaParser.DocumentationIdentifierQualifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationIdentifierQualifier}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationIdentifierQualifier(BallerinaParser.DocumentationIdentifierQualifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationIdentifierTypename}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationIdentifierTypename(BallerinaParser.DocumentationIdentifierTypenameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationIdentifierTypename}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationIdentifierTypename(BallerinaParser.DocumentationIdentifierTypenameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#documentationIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterDocumentationIdentifier(BallerinaParser.DocumentationIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#documentationIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitDocumentationIdentifier(BallerinaParser.DocumentationIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#braket}.
	 * @param ctx the parse tree
	 */
	void enterBraket(BallerinaParser.BraketContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#braket}.
	 * @param ctx the parse tree
	 */
	void exitBraket(BallerinaParser.BraketContext ctx);
}
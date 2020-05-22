/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser;

import io.ballerinalang.compiler.syntax.tree.AnnotationNode;
import io.ballerinalang.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.BasicLiteralNode;
import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BlockStatementNode;
import io.ballerinalang.compiler.syntax.tree.BracedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BreakStatementNode;
import io.ballerinalang.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.CheckExpressionNode;
import io.ballerinalang.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ContinueStatementNode;
import io.ballerinalang.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerinalang.compiler.syntax.tree.ElseBlockNode;
import io.ballerinalang.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ErrorTypeParamsNode;
import io.ballerinalang.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerinalang.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.IfElseStatementNode;
import io.ballerinalang.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerinalang.compiler.syntax.tree.ImportPrefixNode;
import io.ballerinalang.compiler.syntax.tree.ImportVersionNode;
import io.ballerinalang.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.InterpolationNode;
import io.ballerinalang.compiler.syntax.tree.KeySpecifierNode;
import io.ballerinalang.compiler.syntax.tree.KeyTypeConstraintNode;
import io.ballerinalang.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MappingFieldNode;
import io.ballerinalang.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.NamedArgumentNode;
import io.ballerinalang.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerinalang.compiler.syntax.tree.NewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.NodeTransformer;
import io.ballerinalang.compiler.syntax.tree.ObjectFieldNode;
import io.ballerinalang.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.PanicStatementNode;
import io.ballerinalang.compiler.syntax.tree.ParameterNode;
import io.ballerinalang.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerinalang.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerinalang.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RequiredParameterNode;
import io.ballerinalang.compiler.syntax.tree.RestArgumentNode;
import io.ballerinalang.compiler.syntax.tree.RestDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RestParameterNode;
import io.ballerinalang.compiler.syntax.tree.ReturnStatementNode;
import io.ballerinalang.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SeparatedNodeList;
import io.ballerinalang.compiler.syntax.tree.ServiceBodyNode;
import io.ballerinalang.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SpecificFieldNode;
import io.ballerinalang.compiler.syntax.tree.SpreadFieldNode;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TemplateMemberNode;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.TrapExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.TypeParameterNode;
import io.ballerinalang.compiler.syntax.tree.TypeReferenceNode;
import io.ballerinalang.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerinalang.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.WhileStatementNode;
import io.ballerinalang.compiler.syntax.tree.XMLComment;
import io.ballerinalang.compiler.syntax.tree.XMLElementNode;
import io.ballerinalang.compiler.syntax.tree.XMLEndTagNode;
import io.ballerinalang.compiler.syntax.tree.XMLNameNode;
import io.ballerinalang.compiler.syntax.tree.XMLProcessingInstruction;
import io.ballerinalang.compiler.syntax.tree.XMLQualifiedNameNode;
import io.ballerinalang.compiler.syntax.tree.XMLSimpleNameNode;
import io.ballerinalang.compiler.syntax.tree.XMLStartTagNode;
import io.ballerinalang.compiler.syntax.tree.XMLTextNode;
import io.ballerinalang.compiler.text.LinePosition;
import io.ballerinalang.compiler.text.LineRange;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordSpreadOperatorField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Constants;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.ballerinalang.compiler.util.Constants.OPEN_SEALED_ARRAY_INDICATOR;
import static org.wso2.ballerinalang.compiler.util.Constants.UNSEALED_ARRAY_INDICATOR;
import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

/**
 * Generates a {@code BLandCompilationUnit} from the given {@code ModulePart}.
 *
 * @since 1.3.0
 */
public class BLangNodeTransformer extends NodeTransformer<BLangNode> {
    private static final String IDENTIFIER_LITERAL_PREFIX = "'";
    private BLangDiagnosticLogHelper dlog;
    private SymbolTable symTable;
    private BDiagnosticSource diagnosticSource;

    private static final Pattern UNICODE_PATTERN = Pattern.compile(Constants.UNICODE_REGEX);
    private BLangAnonymousModelHelper anonymousModelHelper;
    /* To keep track of additional top-level nodes produced from multi-BLangNode resultant transformations */
    private Stack<TopLevelNode> additionalTopLevelNodes = new Stack<>();
    /* To keep track of additional statements produced from multi-BLangNode resultant transformations */
    private Stack<BLangStatement> additionalStatements = new Stack<>();

    public BLangNodeTransformer(CompilerContext context, BDiagnosticSource diagnosticSource) {
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.diagnosticSource = diagnosticSource;
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
    }

    public List<org.ballerinalang.model.tree.Node> accept(Node node) {
        BLangNode bLangNode = node.apply(this);
        List<org.ballerinalang.model.tree.Node> nodes = new ArrayList<>();
        // if not already consumed, add left-over top-level nodes
        while (!additionalTopLevelNodes.empty()) {
            nodes.add(additionalTopLevelNodes.pop());
        }
        // if not already consumed, add left-over statements
        while (!additionalStatements.empty()) {
            nodes.add(additionalStatements.pop());
        }
        nodes.add(bLangNode);
        return nodes;
    }

    @Override
    public BLangNode transform(IdentifierToken identifierToken) {
        return this.createIdentifier(getPosition(identifierToken), identifierToken.text());
    }

    private DiagnosticPos getPosition(Node node) {
        if (node == null) {
            return null;
        }
        LineRange lineRange = node.lineRange();
        LinePosition startPos = lineRange.startLine();
        LinePosition endPos = lineRange.endLine();
        return new DiagnosticPos(diagnosticSource, startPos.line() + 1, endPos.line() + 1,
                startPos.offset() + 1, endPos.offset() + 1);
    }

    @Override
    public BLangNode transform(ModulePartNode modulePart) {
        BLangCompilationUnit compilationUnit = (BLangCompilationUnit) TreeBuilder.createCompilationUnit();
        compilationUnit.name = diagnosticSource.cUnitName;
        DiagnosticPos pos = getPosition(modulePart);

        // Generate import declarations
        for (ImportDeclarationNode importDecl : modulePart.imports()) {
            BLangImportPackage bLangImport = (BLangImportPackage) importDecl.apply(this);
            bLangImport.compUnit = this.createIdentifier(pos, compilationUnit.getName());
            compilationUnit.addTopLevelNode(bLangImport);
        }

        // Generate other module-level declarations
        for (ModuleMemberDeclarationNode member : modulePart.members()) {
            compilationUnit.addTopLevelNode((TopLevelNode) member.apply(this));
        }

        // Consume additional top-level nodes generated
        while (!this.additionalTopLevelNodes.empty()) {
            compilationUnit.addTopLevelNode(this.additionalTopLevelNodes.pop());
        }

        compilationUnit.pos = pos;
        return compilationUnit;
    }

    @Override
    public BLangNode transform(ModuleVariableDeclarationNode modVarDeclrNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(modVarDeclrNode.variableName(),
                modVarDeclrNode.typeName(), modVarDeclrNode.initializer(),
                modVarDeclrNode.finalKeyword().isPresent(), false, null);
        simpleVar.pos = getPosition(modVarDeclrNode);
        return simpleVar;
    }

    @Override
    public BLangNode transform(ImportDeclarationNode importDeclaration) {
        Node orgNameNode = importDeclaration.orgName().orElse(null);
        Node versionNode = importDeclaration.version().orElse(null);
        Node prefixNode = importDeclaration.prefix().orElse(null);

        String orgName = null;
        if (orgNameNode != null) {
            ImportOrgNameNode importOrgName = (ImportOrgNameNode) orgNameNode;
            orgName = importOrgName.orgName().text();
        }

        String version = null;
        if (versionNode != null) {
            version = ((ImportVersionNode) versionNode).versionNumber().toString();
        }

        String prefix = null;
        if (prefixNode != null) {
            prefix = ((ImportPrefixNode) prefixNode).prefix().toString();
        }

        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        NodeList<IdentifierToken> names = importDeclaration.moduleName();
        names.forEach(name -> pkgNameComps.add(this.createIdentifier(getPosition(name), name.text(), null)));

        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = getPosition(importDeclaration);
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.orgName = this.createIdentifier(getPosition(orgNameNode), orgName);
        importDcl.version = this.createIdentifier(getPosition(versionNode), version);
        importDcl.alias = (prefix != null && !prefix.isEmpty()) ? this.createIdentifier(getPosition(prefixNode), prefix,
                null) :
                pkgNameComps.get(pkgNameComps.size() - 1);

        return importDcl;
    }

    public BLangNode transform(ConstantDeclarationNode constantDeclarationNode) {
        BLangConstant constantNode = (BLangConstant) TreeBuilder.createConstantNode();
        DiagnosticPos pos = getPosition(constantDeclarationNode);
        DiagnosticPos identifierPos = getPosition(constantDeclarationNode.variableName());
        constantNode.name = createIdentifier(pos, constantDeclarationNode.variableName().text());
        constantNode.expr = createExpression(constantDeclarationNode.initializer());
        if (constantDeclarationNode.typeDescriptor() != null) {
            constantNode.typeNode = createTypeNode(constantDeclarationNode.typeDescriptor());
        }

        // TODO: Add annotationAttachments
        // TODO: Add markdownDocumentations
        constantNode.flagSet.add(Flag.CONSTANT);
        if (constantDeclarationNode.visibilityQualifier() != null &&
                constantDeclarationNode.visibilityQualifier().kind() == SyntaxKind.PUBLIC_KEYWORD) {
            constantNode.flagSet.add(Flag.PUBLIC);
        }

        // Check whether the value is a literal. If it is not a literal, it is an invalid case. So we don't need to
        // consider it.
        NodeKind nodeKind = constantNode.expr.getKind();
        if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL) {
            // Note - If the RHS is a literal, we need to create an anonymous type definition which can later be used
            // in type definitions.

            // Create a new literal.
            BLangLiteral literal = nodeKind == NodeKind.LITERAL ?
                    (BLangLiteral) TreeBuilder.createLiteralExpression() :
                    (BLangLiteral) TreeBuilder.createNumericLiteralExpression();
            literal.setValue(((BLangLiteral) constantNode.expr).value);
            literal.type = constantNode.expr.type;
            literal.isConstant = true;

            // Create a new finite type node.
            BLangFiniteTypeNode finiteTypeNode = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
            finiteTypeNode.valueSpace.add(literal);

            // Create a new anonymous type definition.
            BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
            String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
            IdentifierNode anonTypeGenName = createIdentifier(identifierPos, genName);
            typeDef.setName(anonTypeGenName);
            typeDef.flagSet.add(Flag.PUBLIC);
            typeDef.flagSet.add(Flag.ANONYMOUS);
            typeDef.typeNode = finiteTypeNode;
            typeDef.pos = pos;

            // We add this type definition to the `associatedTypeDefinition` field of the constant node. Then when we
            // visit the constant node, we visit this type definition as well. By doing this, we don't need to change
            // any of the type def visiting logic in symbol enter.
            constantNode.associatedTypeDefinition = typeDef;
        }
        return constantNode;
    }

    public BLangNode transform(TypeDefinitionNode typeDefNode) {
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        BLangIdentifier identifierNode = this.createIdentifier(getPosition(typeDefNode.typeName()),
                typeDefNode.typeName().text());
        typeDef.setName(identifierNode);

        typeDef.typeNode = createTypeNode(typeDefNode.typeDescriptor());

        typeDefNode.visibilityQualifier().ifPresent(visibilityQual -> {
            if (visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                typeDef.flagSet.add(Flag.PUBLIC);
            }
        });
        typeDef.pos = getPosition(typeDefNode);
        return typeDef;
    }

    @Override
    public BLangNode transform(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        BLangType rhsTypeNode = createTypeNode(unionTypeDescriptorNode.rightTypeDesc());
        BLangType lhsTypeNode = createTypeNode(unionTypeDescriptorNode.leftTypeDesc());

        return addUnionType(lhsTypeNode, rhsTypeNode, getPosition(unionTypeDescriptorNode));
    }

    @Override
    public BLangNode transform(ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
        BLangType typeNode = createTypeNode(parenthesisedTypeDescriptorNode.typedesc());
        typeNode.grouped = true;
        return typeNode;
    }

    @Override
    public BLangNode transform(TypeParameterNode typeParameterNode) {
        return createTypeNode(typeParameterNode.typeNode());
    }

    @Override
    public BLangNode transform(TupleTypeDescriptorNode tupleTypeDescriptorNode) {
        BLangTupleTypeNode tupleTypeNode = (BLangTupleTypeNode) TreeBuilder.createTupleTypeNode();
        SeparatedNodeList<Node> types = tupleTypeDescriptorNode.memberTypeDesc();
        for (int i = 0; i < types.size(); i++) {
            Node node = types.get(i);
            if (node.kind() == SyntaxKind.REST_TYPE) {
                RestDescriptorNode restDescriptor = (RestDescriptorNode) node;
                tupleTypeNode.restParamType = createTypeNode(restDescriptor.typeDescriptor());
            } else {
                tupleTypeNode.memberTypeNodes.add(createTypeNode(node));
            }
        }
        tupleTypeNode.pos = getPosition(tupleTypeDescriptorNode);

        return tupleTypeNode;
    }

    @Override
    public BLangNode transform(ErrorTypeDescriptorNode errorTypeDescriptorNode) {
        BLangErrorType errorType = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        // TODO : Add parameter
        if (errorTypeDescriptorNode.errorTypeParamsNode().isPresent()) {
            errorType.reasonType = createTypeNode(errorTypeDescriptorNode.errorTypeParamsNode().get());
        }
        return errorType;
    }

    @Override
    public BLangNode transform(ErrorTypeParamsNode errorTypeParamsNode) {
        return createTypeNode(errorTypeParamsNode.parameter());
    }

    @Override
    public BLangNode transform(ObjectTypeDescriptorNode objTypeDescNode) {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) TreeBuilder.createObjectTypeNode();

        for (Token qualifier : objTypeDescNode.objectTypeQualifiers()) {
            if (qualifier.kind() == SyntaxKind.ABSTRACT_KEYWORD) {
                objectTypeNode.flagSet.add(Flag.ABSTRACT);
            }

            if (qualifier.kind() == SyntaxKind.CLIENT_KEYWORD) {
                objectTypeNode.flagSet.add(Flag.CLIENT);
            }

            if (qualifier.kind() == SyntaxKind.SERVICE_KEYWORD) {
                objectTypeNode.flagSet.add(Flag.SERVICE);
            }
        }

        for (Node node : objTypeDescNode.members()) {
            // TODO: Check for fields other than SimpleVariableNode
            BLangNode bLangNode = node.apply(this);
            if (bLangNode.getKind() == NodeKind.FUNCTION) {
                BLangFunction bLangFunction = (BLangFunction) bLangNode;
                bLangFunction.attachedFunction = true;
                if (Names.USER_DEFINED_INIT_SUFFIX.value.equals(bLangFunction.name.value)) {
                    bLangFunction.objInitFunction = true;
                    // TODO: verify removing NULL check for objectTypeNode.initFunction has no side-effects
                    objectTypeNode.initFunction = bLangFunction;
                } else {
                    objectTypeNode.addFunction(bLangFunction);
                }
            } else if (bLangNode.getKind() == NodeKind.VARIABLE) {
                objectTypeNode.addField((BLangSimpleVariable) bLangNode);
            }
        }

        objectTypeNode.isAnonymous = false;
        objectTypeNode.pos = getPosition(objTypeDescNode);
        return objectTypeNode;
    }

    @Override
    public BLangNode transform(ObjectFieldNode objFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(objFieldNode.fieldName(), objFieldNode.typeName(),
                objFieldNode.expression(),
                false, false, objFieldNode.visibilityQualifier());
        simpleVar.pos = getPosition(objFieldNode);
        return simpleVar;
    }

    @Override
    public BLangNode transform(ServiceDeclarationNode serviceDeclrNode) {
        return createService(serviceDeclrNode, serviceDeclrNode.serviceName(), false);
    }

    private BLangNode createService(ServiceDeclarationNode serviceDeclrNode, IdentifierToken serviceNameNode,
                                    boolean isAnonServiceValue) {
        // Any Service can be represented in two major components.
        //  1) A anonymous type node (Object)
        //  2) Variable assignment with "serviceName".
        //      This is a global variable if the service is defined in module level.
        //      Otherwise (isAnonServiceValue = true) it is a local variable definition, which is written by user.
        BLangService bLService = (BLangService) TreeBuilder.createServiceNode();
        //TODO handle service.expression
        // TODO: Look for generify this into sepearte method for type as well
        bLService.isAnonymousServiceValue = isAnonServiceValue;

        DiagnosticPos pos = getPosition(serviceDeclrNode);
        String serviceName;
        DiagnosticPos identifierPos;
        if (isAnonServiceValue) {
            serviceName = this.anonymousModelHelper.getNextAnonymousServiceVarKey(diagnosticSource.pkgID);
            identifierPos = pos;
        } else {
            serviceName = serviceNameNode.text();
            identifierPos = getPosition(serviceNameNode);
        }
        String serviceTypeName = this.anonymousModelHelper.getNextAnonymousServiceTypeKey(diagnosticSource.pkgID,
                serviceName);
        BLangIdentifier serviceVar = createIdentifier(identifierPos, serviceName);
        serviceVar.pos = identifierPos;
        bLService.setName(serviceVar);
        if (!isAnonServiceValue) {
            // TODO: Enable this one CCE error fixed
//            for (io.ballerinalang.compiler.syntax.tree.ExpressionNode expr : serviceDeclrNode.expressions()) {
//                bLService.attachedExprs.add((BLangExpression) expr.apply(this));
//            }
        }
        // We add all service nodes to top level, only for future reference.
        this.additionalTopLevelNodes.add(bLService);

        // 1) Define type nodeDefinition for service type.
        BLangTypeDefinition bLTypeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        BLangIdentifier serviceTypeID = createIdentifier(identifierPos, serviceTypeName);
        serviceTypeID.pos = pos;
        bLTypeDef.setName(serviceTypeID);
        bLTypeDef.flagSet.add(Flag.SERVICE);
        bLTypeDef.typeNode = (BLangType) serviceDeclrNode.serviceBody().apply(this);
        bLTypeDef.pos = pos;
        bLService.serviceTypeDefinition = bLTypeDef;

        // 2) Create service constructor.
        final BLangServiceConstructorExpr serviceConstNode = (BLangServiceConstructorExpr) TreeBuilder
                .createServiceConstructorNode();
        serviceConstNode.serviceNode = bLService;
        serviceConstNode.pos = pos;

        // Crate Global variable for service.
        bLService.pos = pos;
        if (!isAnonServiceValue) {
            BLangSimpleVariable var = (BLangSimpleVariable) createBasicVarNodeWithoutType(identifierPos,
                    Collections.emptySet(),
                    serviceName, identifierPos,
                    serviceConstNode);
            var.flagSet.add(Flag.FINAL);
            var.flagSet.add(Flag.SERVICE);

            BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            bLUserDefinedType.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
            bLUserDefinedType.typeName = bLTypeDef.name;
            bLUserDefinedType.pos = pos;

            var.typeNode = bLUserDefinedType;
            bLService.variableNode = var;
            this.additionalTopLevelNodes.add(bLTypeDef);
            return var;
        } else {
            return bLTypeDef;
        }
    }

    @Override
    public BLangNode transform(ServiceBodyNode serviceBodyNode) {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) TreeBuilder.createObjectTypeNode();
        objectTypeNode.flagSet.add(Flag.SERVICE);
        for (Node resourceNode : serviceBodyNode.resources()) {
            BLangNode bLangNode = resourceNode.apply(this);
            if (bLangNode.getKind() == NodeKind.FUNCTION) {
                BLangFunction bLangFunction = (BLangFunction) bLangNode;
                bLangFunction.attachedFunction = true;
                objectTypeNode.addFunction(bLangFunction);
            }
        }
        objectTypeNode.isAnonymous = false;
        objectTypeNode.pos = getPosition(serviceBodyNode);
        return objectTypeNode;
    }

    @Override
    public BLangNode transform(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        boolean hasRestField = false;
        for (Node field : recordTypeDescriptorNode.fields()) {
            if (field.kind() == SyntaxKind.RECORD_FIELD || field.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE) {
                recordTypeNode.fields.add((BLangSimpleVariable) field.apply(this));
            } else if (field.kind() == SyntaxKind.RECORD_REST_TYPE) {
                recordTypeNode.restFieldType = (BLangValueType) field.apply(this);
                hasRestField = true;
            } else if (field.kind() == SyntaxKind.TYPE_REFERENCE) {
                recordTypeNode.addTypeReference((BLangType) field.apply(this));
            }
        }
        recordTypeNode.sealed = !hasRestField;
        recordTypeNode.pos = getPosition(recordTypeDescriptorNode);
        return recordTypeNode;
    }

    @Override
    public BLangNode transform(TypeReferenceNode typeReferenceNode) {
        return createTypeNode(typeReferenceNode.typeName());
    }

    @Override
    public BLangNode transform(RecordFieldNode recordFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(recordFieldNode.fieldName(), recordFieldNode.typeName());
        simpleVar.flagSet.add(Flag.PUBLIC);
        if (recordFieldNode.questionMarkToken().isPresent()) {
            simpleVar.flagSet.add(Flag.OPTIONAL);
        } else {
            simpleVar.flagSet.add(Flag.REQUIRED);
        }
        simpleVar.pos = getPosition(recordFieldNode);
        return simpleVar;
    }

    @Override
    public BLangNode transform(RecordFieldWithDefaultValueNode recordFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(recordFieldNode.fieldName(), recordFieldNode.typeName());
        simpleVar.flagSet.add(Flag.PUBLIC);
        if (isPresent(recordFieldNode.expression())) {
            simpleVar.setInitialExpression(createExpression(recordFieldNode.expression()));
        }
        simpleVar.pos = getPosition(recordFieldNode);
        return simpleVar;
    }

    @Override
    public BLangNode transform(RecordRestDescriptorNode recordFieldNode) {
        return createTypeNode(recordFieldNode.typeName());
    }

    @Override
    public BLangNode transform(FunctionDefinitionNode funcDefNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();

        // Set function name
        IdentifierToken funcName = funcDefNode.functionName();
        bLFunction.name = createIdentifier(getPosition(funcName), funcName.text());

        // Set the visibility qualifier
        funcDefNode.visibilityQualifier().ifPresent(visibilityQual -> {
            if (visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                bLFunction.flagSet.add(Flag.PUBLIC);
            } else if (visibilityQual.kind() == SyntaxKind.PRIVATE_KEYWORD) {
                bLFunction.flagSet.add(Flag.PRIVATE);
            } else if (visibilityQual.kind() == SyntaxKind.REMOTE_KEYWORD) {
                bLFunction.flagSet.add(Flag.REMOTE);
            } else if (visibilityQual.kind() == SyntaxKind.RESOURCE_KEYWORD) {
                bLFunction.flagSet.add(Flag.RESOURCE);
            }
        });

        // Set function signature
        populateFuncSignature(bLFunction, funcDefNode.functionSignature());

        // Set the function body
        if (funcDefNode.functionBody() == null) {
            bLFunction.body = null;
            bLFunction.flagSet.add(Flag.INTERFACE);
            bLFunction.interfaceFunction = true;
        } else {
            bLFunction.body = (BLangFunctionBody) funcDefNode.functionBody().apply(this);
            if (bLFunction.body.getKind() == NodeKind.EXTERN_FUNCTION_BODY) {
                bLFunction.flagSet.add(Flag.NATIVE);
            }
        }

//        attachAnnotations(function, annCount, false);
        bLFunction.pos = getPosition(funcDefNode);
        return bLFunction;
    }

    @Override
    public BLangNode transform(ExternalFunctionBodyNode externalFunctionBodyNode) {
        BLangExternalFunctionBody externFunctionBodyNode =
                (BLangExternalFunctionBody) TreeBuilder.createExternFunctionBodyNode();
        externFunctionBodyNode.annAttachments = applyAll(externalFunctionBodyNode.annotations());
        return externFunctionBodyNode;
    }

    @Override
    public BLangNode transform(ExplicitAnonymousFunctionExpressionNode anonFuncExprNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        DiagnosticPos pos = getPosition(anonFuncExprNode);

        // Set function name
        bLFunction.name = createIdentifier(pos,
                                           anonymousModelHelper.getNextAnonymousFunctionKey(diagnosticSource.pkgID));

        // Set function signature
        populateFuncSignature(bLFunction, anonFuncExprNode.functionSignature());

        // Set the function body
        bLFunction.body = (BLangFunctionBody) anonFuncExprNode.functionBody().apply(this);

//        attachAnnotations(function, annCount, false);
        bLFunction.pos = pos;

        bLFunction.addFlag(Flag.LAMBDA);
        bLFunction.addFlag(Flag.ANONYMOUS);
        this.additionalTopLevelNodes.add(bLFunction);

        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = bLFunction;
        lambdaExpr.pos = pos;
        return lambdaExpr;
    }

    @Override
    public BLangNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
        BLangBlockFunctionBody bLFuncBody = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        List<BLangStatement> statements = new ArrayList<>();
        for (StatementNode statement : functionBodyBlockNode.statements()) {
            // TODO: Remove this check once statements are non null guaranteed
            if (statement != null) {
                statements.add((BLangStatement) statement.apply(this));
            }
        }
        if (functionBodyBlockNode.namedWorkerDeclarator().isPresent()) {
            NamedWorkerDeclarator namedWorkerDeclarator = functionBodyBlockNode.namedWorkerDeclarator().get();
            for (StatementNode statement : namedWorkerDeclarator.workerInitStatements()) {
                statements.add((BLangStatement) statement.apply(this));
            }
            for (NamedWorkerDeclarationNode workerDeclarationNode : namedWorkerDeclarator.namedWorkerDeclarations()) {
                statements.add((BLangStatement) workerDeclarationNode.apply(this));
                // Consume resultant additional statements
                while (!this.additionalStatements.empty()) {
                    statements.add(additionalStatements.pop());
                }
            }
        }

        bLFuncBody.stmts = statements;
        bLFuncBody.pos = getPosition(functionBodyBlockNode);
        return bLFuncBody;
    }


    @Override
    public BLangNode transform(NamedWorkerDeclarationNode namedWorkerDeclNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        DiagnosticPos pos = getPosition(namedWorkerDeclNode);

        // Set function name
        bLFunction.name = createIdentifier(pos,
                                           anonymousModelHelper.getNextAnonymousFunctionKey(diagnosticSource.pkgID));

        // Set the function body
        BLangBlockStmt blockStmt = (BLangBlockStmt) namedWorkerDeclNode.workerBody().apply(this);
        BLangBlockFunctionBody bodyNode = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        bodyNode.stmts = blockStmt.stmts;
        bLFunction.body = bodyNode;

//        attachAnnotations(function, annCount, false);
        bLFunction.pos = pos;

        bLFunction.addFlag(Flag.LAMBDA);
        bLFunction.addFlag(Flag.ANONYMOUS);
        bLFunction.addFlag(Flag.WORKER);

        // change default worker name
        String workerName = namedWorkerDeclNode.workerName().text();
        bLFunction.defaultWorkerName.value = workerName;
        bLFunction.defaultWorkerName.pos = getPosition(namedWorkerDeclNode.workerName());

        NodeList<AnnotationNode> annotations = namedWorkerDeclNode.annotations();
        bLFunction.annAttachments = applyAll(annotations);

        // Set Return Type
        Optional<Node> retNode = namedWorkerDeclNode.returnTypeDesc();
        if (retNode.isPresent()) {
            ReturnTypeDescriptorNode returnType = (ReturnTypeDescriptorNode) retNode.get();
            bLFunction.setReturnTypeNode(createTypeNode(returnType.type()));
        } else {
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            bLValueType.pos = getPosition(namedWorkerDeclNode);
            bLValueType.typeKind = TypeKind.NIL;
            bLFunction.setReturnTypeNode(bLValueType);
        }

        this.additionalTopLevelNodes.add(bLFunction);

        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = bLFunction;
        lambdaExpr.pos = pos;

        String workerLambdaName = WORKER_LAMBDA_VAR_PREFIX + workerName;

        // Check if the worker is in a fork. If so add the lambda function to the worker list in fork, else ignore.
        BLangSimpleVariable var = new SimpleVarBuilder()
                .with(workerLambdaName)
                .setExpression(lambdaExpr)
                .isDeclaredWithVar()
                .isFinal()
                .build();

        BLangSimpleVariableDef lamdaWrkr = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        DiagnosticPos workerNamePos = getPosition(namedWorkerDeclNode.workerName());
        lamdaWrkr.pos = workerNamePos;
        lamdaWrkr.setVariable(var);
        lamdaWrkr.isWorker = true;
//        if (!this.forkJoinNodesStack.empty()) {
//            // TODO: Revisit the fork join worker declaration and decide whether move this to desugar.
//            lamdaWrkr.isInFork = true;
//            lamdaWrkr.var.flagSet.add(Flag.FORKED);
//            this.forkJoinNodesStack.peek().addWorkers(lamdaWrkr);
//        }

        BLangInvocation bLInvocation = (BLangInvocation) TreeBuilder.createActionInvocation();
        BLangIdentifier nameInd = this.createIdentifier(pos, workerLambdaName);
        BLangNameReference reference = new BLangNameReference(workerNamePos, null, TreeBuilder.createIdentifierNode(),
                                                              nameInd);
        bLInvocation.pkgAlias = (BLangIdentifier) reference.pkgAlias;
        bLInvocation.name = (BLangIdentifier) reference.name;
        bLInvocation.pos = workerNamePos;
        bLInvocation.flagSet = new HashSet<>();
        bLInvocation.annAttachments = bLFunction.annAttachments;

        if (bLInvocation.getKind() == NodeKind.INVOCATION) {
            bLInvocation.async = true;
//            attachAnnotations(invocation, numAnnotations, false);
        } else {
            dlog.error(pos, DiagnosticCode.START_REQUIRE_INVOCATION);
        }

        BLangSimpleVariable invoc = new SimpleVarBuilder()
                .with(workerName, getPosition(namedWorkerDeclNode.workerName()))
                .isDeclaredWithVar()
                .isWorkerVar()
                .setExpression(bLInvocation)
                .isFinal()
                .build();

        BLangSimpleVariableDef workerInvoc = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        workerInvoc.pos = workerNamePos;
        workerInvoc.setVariable(invoc);
        workerInvoc.isWorker = true;
        this.additionalStatements.push(workerInvoc);

        return lamdaWrkr;
    }

    private <A extends BLangNode, B extends Node> List<A> applyAll(NodeList<B> annotations) {
        ArrayList<A> annAttachments = new ArrayList<>();
        for (B annotation : annotations) {
            A blNode = (A) annotation.apply(this);
            annAttachments.add(blNode);
        }
        return annAttachments;
    }

    @Override
    public BLangNode transform(AnnotationNode annotation) {
        Node name = annotation.annotReference();
        BLangAnnotationAttachment bLAnnotationAttachment =
                (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        if (annotation.annotValue().isPresent()) {
            MappingConstructorExpressionNode map = annotation.annotValue().get();
            BLangExpression bLExpression = (BLangExpression) map.apply(this);
            bLAnnotationAttachment.setExpression(bLExpression);
        }
        BLangNameReference nameReference = createBLangNameReference(name);
        bLAnnotationAttachment.setAnnotationName(nameReference.name);
        bLAnnotationAttachment.setPackageAlias(nameReference.pkgAlias);
        bLAnnotationAttachment.pos = getPosition(annotation);
        return bLAnnotationAttachment;
    }

    // -----------------------------------------------Expressions-------------------------------------------------------
    @Override
    public BLangNode transform(CheckExpressionNode checkExpressionNode) {
        DiagnosticPos pos = getPosition(checkExpressionNode);
        BLangExpression expr = createExpression(checkExpressionNode.expression());
        if (checkExpressionNode.checkKeyword().kind() == SyntaxKind.CHECK_KEYWORD) {
            return createCheckExpr(pos, expr);
        }
        return createCheckPanickedExpr(pos, expr);
    }

    @Override
    public BLangNode transform(TypeTestExpressionNode typeTestExpressionNode) {
        BLangTypeTestExpr typeTestExpr = (BLangTypeTestExpr) TreeBuilder.createTypeTestExpressionNode();
        typeTestExpr.expr = createExpression(typeTestExpressionNode.expression());
        typeTestExpr.typeNode = createTypeNode(typeTestExpressionNode.typeDescriptor());
        typeTestExpr.pos = getPosition(typeTestExpressionNode);

        return typeTestExpr;
    }

    @Override
    public BLangNode transform(MappingConstructorExpressionNode mapConstruct) {
        BLangRecordLiteral bLiteralNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        for (MappingFieldNode field : mapConstruct.fields()) {
            if (field.kind() == SyntaxKind.SPREAD_FIELD) {
                SpreadFieldNode spreadFieldNode = (SpreadFieldNode) field;
                BLangRecordSpreadOperatorField bLRecordSpreadOpField =
                        (BLangRecordSpreadOperatorField) TreeBuilder.createRecordSpreadOperatorField();
                bLRecordSpreadOpField.expr = createExpression(spreadFieldNode.valueExpr());
                bLiteralNode.fields.add(bLRecordSpreadOpField);
            } else {
                SpecificFieldNode specificField = (SpecificFieldNode) field;
                BLangRecordKeyValueField bLRecordKeyValueField =
                        (BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
                bLRecordKeyValueField.valueExpr = createExpression(specificField.valueExpr());
                bLRecordKeyValueField.key = new BLangRecordLiteral.BLangRecordKey(
                        createExpression(specificField.fieldName()));
                bLRecordKeyValueField.key.computedKey = false;
                bLiteralNode.fields.add(bLRecordKeyValueField);
            }
        }
        bLiteralNode.pos = getPosition(mapConstruct);
        return bLiteralNode;
    }

    @Override
    public BLangNode transform(ListConstructorExpressionNode listConstructorExprNode) {
        List<BLangExpression> argExprList = new ArrayList<>();
        BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr)
                TreeBuilder.createListConstructorExpressionNode();
        for (Node expr : listConstructorExprNode.expressions()) {
            argExprList.add(createExpression(expr));
        }
        listConstructorExpr.exprs = argExprList;
        listConstructorExpr.pos = getPosition(listConstructorExprNode);
        return listConstructorExpr;
    }

    @Override
    public BLangNode transform(UnaryExpressionNode unaryExprNode) {
        DiagnosticPos pos = getPosition(unaryExprNode);
        OperatorKind operator = OperatorKind.valueFrom(unaryExprNode.unaryOperator().text());
        BLangExpression expr = createExpression(unaryExprNode.expression());
        return createBLangUnaryExpr(pos, operator, expr);
    }

    @Override
    public BLangNode transform(TypeofExpressionNode typeofExpressionNode) {
        DiagnosticPos pos = getPosition(typeofExpressionNode);
        OperatorKind operator = OperatorKind.valueFrom(typeofExpressionNode.typeofKeyword().text());
        BLangExpression expr = createExpression(typeofExpressionNode.expression());
        return createBLangUnaryExpr(pos, operator, expr);
    }

    @Override
    public BLangNode transform(BinaryExpressionNode binaryExprNode) {
        BLangBinaryExpr bLBinaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        bLBinaryExpr.pos = getPosition(binaryExprNode);
        bLBinaryExpr.lhsExpr = createExpression(binaryExprNode.lhsExpr());
        bLBinaryExpr.rhsExpr = createExpression(binaryExprNode.rhsExpr());
        bLBinaryExpr.opKind = OperatorKind.valueFrom(binaryExprNode.operator().text());
        return bLBinaryExpr;
    }

    @Override
    public BLangNode transform(FieldAccessExpressionNode fieldAccessExprNode) {
        BLangFieldBasedAccess bLFieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        Token fieldName = fieldAccessExprNode.fieldName();
        bLFieldBasedAccess.pos = getPosition(fieldAccessExprNode);
        BLangNameReference nameRef = createBLangNameReference(fieldName);
        bLFieldBasedAccess.field = createIdentifier(getPosition(fieldName), nameRef.name.getValue());
        bLFieldBasedAccess.field.pos = getPosition(fieldAccessExprNode);
        bLFieldBasedAccess.expr = createExpression(fieldAccessExprNode.expression());
        bLFieldBasedAccess.fieldKind = FieldKind.SINGLE;
        // TODO: Fix this when optional field access is available
        bLFieldBasedAccess.optionalFieldAccess = false;
        return bLFieldBasedAccess;
    }

    @Override
    public BLangNode transform(BracedExpressionNode brcExprOut) {
        return createExpression(brcExprOut.expression());
    }

    @Override
    public BLangNode transform(FunctionCallExpressionNode functionCallNode) {
        return createBLangInvocation(functionCallNode.functionName(), functionCallNode.arguments(),
                                     getPosition(functionCallNode));
    }

    public BLangNode transform(MethodCallExpressionNode methodCallExprNode) {
        BLangInvocation bLInvocation = createBLangInvocation(methodCallExprNode.methodName(),
                                                             methodCallExprNode.arguments(),
                                                             getPosition(methodCallExprNode));
        bLInvocation.expr = createExpression(methodCallExprNode.expression());
        return bLInvocation;
    }

    @Override
    public BLangNode transform(ImplicitNewExpressionNode implicitNewExprNode) {
        BLangTypeInit initNode = createTypeInit(implicitNewExprNode);
        BLangInvocation invocationNode = createInvocation(implicitNewExprNode, implicitNewExprNode.newKeyword());
        // Populate the argument expressions on initNode as well.
        initNode.argsExpr.addAll(invocationNode.argExprs);
        initNode.initInvocation = invocationNode;

        return initNode;
    }

    @Override
    public BLangNode transform(ExplicitNewExpressionNode explicitNewExprNode) {
        BLangTypeInit initNode = createTypeInit(explicitNewExprNode);
        BLangInvocation invocationNode = createInvocation(explicitNewExprNode, explicitNewExprNode.newKeyword());
        // Populate the argument expressions on initNode as well.
        initNode.argsExpr.addAll(invocationNode.argExprs);
        initNode.initInvocation = invocationNode;
        return initNode;
    }

    private BLangTypeInit createTypeInit(NewExpressionNode expression) {
        BLangTypeInit initNode = (BLangTypeInit) TreeBuilder.createInitNode();
        initNode.pos = getPosition(expression);
        if (expression.kind() == SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
            Node type = ((ExplicitNewExpressionNode) expression).typeDescriptor();
            initNode.userDefinedType = createTypeNode(type);
        }

        return initNode;
    }

    private BLangInvocation createInvocation(NewExpressionNode expression, Token newKeyword) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = getPosition(expression);

        populateArgsInvocation(expression, invocationNode);

        BLangNameReference nameReference = createBLangNameReference(newKeyword);
        invocationNode.name = (BLangIdentifier) nameReference.name;
        invocationNode.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;

        return invocationNode;
    }

    private void populateArgsInvocation(NewExpressionNode expression, BLangInvocation invocationNode) {
        Iterator<FunctionArgumentNode> argumentsIter = getArgumentNodesIterator(expression);
        if (argumentsIter != null) {
            while (argumentsIter.hasNext()) {
                BLangExpression argument = createExpression(argumentsIter.next());
                invocationNode.argExprs.add(argument);
            }
        }
    }

    private Iterator<FunctionArgumentNode> getArgumentNodesIterator(NewExpressionNode expression) {
        Iterator<FunctionArgumentNode> argumentsIter = null;

        if (expression.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION) {
            Optional<ParenthesizedArgList> argsList = ((ImplicitNewExpressionNode) expression).parenthesizedArgList();
            if (argsList.isPresent()) {
                ParenthesizedArgList argList = argsList.get();
                argumentsIter = argList.arguments().iterator();
            }
        } else {
            ParenthesizedArgList argList =
                    (ParenthesizedArgList) ((ExplicitNewExpressionNode) expression).parenthesizedArgList();
            argumentsIter = argList.arguments().iterator();
        }

        return argumentsIter;
    }

    @Override
    public BLangIndexBasedAccess transform(IndexedExpressionNode indexedExpressionNode) {
        BLangIndexBasedAccess indexBasedAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexBasedAccess.pos = getPosition(indexedExpressionNode);
        // TODO : Add BLangTableMultiKeyExpr for indexExpr if it is available
        indexBasedAccess.indexExpr = createExpression(indexedExpressionNode.keyExpression().get(0));
        indexBasedAccess.expr = createExpression(indexedExpressionNode.containerExpression());
        return indexBasedAccess;
    }

    @Override
    public BLangTypeConversionExpr transform(TypeCastExpressionNode typeCastExpressionNode) {
        BLangTypeConversionExpr typeConversionNode = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        // TODO : Attach annotations if available
        typeConversionNode.pos = getPosition(typeCastExpressionNode);
        if (typeCastExpressionNode.typeCastParam() != null) {
            typeConversionNode.typeNode = createTypeNode(typeCastExpressionNode.typeCastParam().type());
        }
        typeConversionNode.expr = createExpression(typeCastExpressionNode.expression());
        return typeConversionNode;
    }

    @Override
    public BLangNode transform(Token token) {
        SyntaxKind kind = token.kind();
        switch (kind) {
            case XML_TEXT_CONTENT:
            case TEMPLATE_STRING:
                return createSimpleLiteral(token);
            default:
                throw new RuntimeException("Syntax kind is not supported: " + kind);
        }
    }

    @Override
    public BLangNode transform(InterpolationNode interpolationNode) {
        return createExpression(interpolationNode.expression());
    }

    @Override
    public BLangNode transform(TemplateExpressionNode expressionNode) {
        SyntaxKind kind = expressionNode.kind();
        switch (kind) {
            case XML_TEMPLATE_EXPRESSION:
                return expressionNode.content().get(0).apply(this);
            case STRING_TEMPLATE_EXPRESSION:
                return createStringTemplateLiteral(expressionNode.content(), getPosition(expressionNode));
            default:
                throw new RuntimeException("Syntax kind is not supported: " + kind);
        }
    }

    @Override
    public BLangNode transform(TableConstructorExpressionNode tableConstructorExpressionNode) {
        BLangTableConstructorExpr tableConstructorExpr =
                (BLangTableConstructorExpr) TreeBuilder.createTableConstructorExpressionNode();
        tableConstructorExpr.pos = getPosition(tableConstructorExpressionNode);

        for (Node node : tableConstructorExpressionNode.mappingConstructors()) {
            tableConstructorExpr.addRecordLiteral((BLangRecordLiteral) node.apply(this));
        }
        if (tableConstructorExpressionNode.keySpecifier() != null) {
            tableConstructorExpr.tableKeySpecifier =
                    (BLangTableKeySpecifier) tableConstructorExpressionNode.keySpecifier().apply(this);
        }
        return tableConstructorExpr;
    }

    @Override
    public BLangNode transform(TrapExpressionNode trapExpressionNode) {
        BLangTrapExpr trapExpr = (BLangTrapExpr) TreeBuilder.createTrapExpressionNode();
        trapExpr.expr = createExpression(trapExpressionNode.expression());
        return trapExpr;
    }

    private BLangCheckedExpr createCheckExpr(DiagnosticPos pos, BLangExpression expr) {
        BLangCheckedExpr checkedExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();
        checkedExpr.pos = pos;
        checkedExpr.expr = expr;
        return checkedExpr;
    }

    private BLangCheckPanickedExpr createCheckPanickedExpr(DiagnosticPos pos, BLangExpression expr) {
        BLangCheckPanickedExpr checkPanickedExpr =
                (BLangCheckPanickedExpr) TreeBuilder.createCheckPanicExpressionNode();
        checkPanickedExpr.pos = pos;
        checkPanickedExpr.expr = expr;
        return checkPanickedExpr;
    }

    // -----------------------------------------------Statements--------------------------------------------------------
    @Override
    public BLangNode transform(ReturnStatementNode returnStmtNode) {
        BLangReturn bLReturn = (BLangReturn) TreeBuilder.createReturnNode();
        bLReturn.pos = getPosition(returnStmtNode);
        if (returnStmtNode.expression().isPresent()) {
            bLReturn.expr = createExpression(returnStmtNode.expression().get());
        } else {
            BLangLiteral nilLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            nilLiteral.pos = getPosition(returnStmtNode);
            nilLiteral.value = Names.NIL_VALUE;
            nilLiteral.type = symTable.nilType;
            bLReturn.expr = nilLiteral;
        }
        return bLReturn;
    }

    @Override
    public BLangNode transform(PanicStatementNode panicStmtNode) {
        BLangPanic bLPanic = (BLangPanic) TreeBuilder.createPanicNode();
        bLPanic.pos = getPosition(panicStmtNode);
        bLPanic.expr = createExpression(panicStmtNode.expression());
        return bLPanic;
    }

    @Override
    public BLangNode transform(ContinueStatementNode continueStmtNode) {
        BLangContinue bLContinue = (BLangContinue) TreeBuilder.createContinueNode();
        bLContinue.pos = getPosition(continueStmtNode);
        return bLContinue;
    }

    @Override
    public BLangNode transform(BreakStatementNode breakStmtNode) {
        BLangBreak bLBreak = (BLangBreak) TreeBuilder.createBreakNode();
        bLBreak.pos = getPosition(breakStmtNode);
        return bLBreak;
    }

    @Override
    public BLangNode transform(AssignmentStatementNode assignmentStmtNode) {
        BLangAssignment bLAssignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        BLangExpression lhsExpr = createExpression(assignmentStmtNode.varRef());
        validateLvexpr(lhsExpr, DiagnosticCode.INVALID_INVOCATION_LVALUE_ASSIGNMENT);
        bLAssignment.setExpression(createExpression(assignmentStmtNode.expression()));
        bLAssignment.pos = getPosition(assignmentStmtNode);
        bLAssignment.varRef = lhsExpr;
        return bLAssignment;
    }

    @Override
    public BLangNode transform(CompoundAssignmentStatementNode compoundAssignmentStmtNode) {
        BLangCompoundAssignment bLCompAssignment = (BLangCompoundAssignment) TreeBuilder.createCompoundAssignmentNode();
        bLCompAssignment.setExpression(createExpression(compoundAssignmentStmtNode.rhsExpression()));

        bLCompAssignment.setVariable(
                (BLangVariableReference) createExpression(compoundAssignmentStmtNode.lhsExpression())
        );
        bLCompAssignment.pos = getPosition(compoundAssignmentStmtNode);
        bLCompAssignment.opKind = OperatorKind.valueFrom(compoundAssignmentStmtNode.binaryOperator().text());
        return bLCompAssignment;
    }

    private void validateLvexpr(ExpressionNode lExprNode, DiagnosticCode errorCode) {
        if (lExprNode.getKind() == NodeKind.INVOCATION) {
            dlog.error(((BLangInvocation) lExprNode).pos, errorCode);
        }
        if (lExprNode.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                || lExprNode.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            validateLvexpr(((BLangAccessExpression) lExprNode).expr, errorCode);
        }
    }

    @Override
    public BLangNode transform(WhileStatementNode whileStmtNode) {
        BLangWhile bLWhile = (BLangWhile) TreeBuilder.createWhileNode();
        bLWhile.setCondition(createExpression(whileStmtNode.condition()));
        bLWhile.pos = getPosition(whileStmtNode);

        BLangBlockStmt bLBlockStmt = (BLangBlockStmt) whileStmtNode.whileBody().apply(this);
        bLBlockStmt.pos = getPosition(whileStmtNode.whileBody());
        bLWhile.setBody(bLBlockStmt);
        return bLWhile;
    }

    @Override
    public BLangNode transform(IfElseStatementNode ifElseStmtNode) {
        BLangIf bLIf = (BLangIf) TreeBuilder.createIfElseStatementNode();
        bLIf.pos = getPosition(ifElseStmtNode);
        bLIf.setCondition(createExpression(ifElseStmtNode.condition()));
        bLIf.setBody((BLangBlockStmt) ifElseStmtNode.ifBody().apply(this));

        ifElseStmtNode.elseBody().ifPresent(elseBody -> {
            ElseBlockNode elseNode = (ElseBlockNode) elseBody;
            bLIf.setElseStatement(
                    (org.ballerinalang.model.tree.statements.StatementNode) elseNode.elseBody().apply(this));
        });
        return bLIf;
    }

    @Override
    public BLangNode transform(BlockStatementNode blockStatement) {
        BLangBlockStmt bLBlockStmt = (BLangBlockStmt) TreeBuilder.createBlockNode();
        List<BLangStatement> statements = new ArrayList<>();
        for (StatementNode statement : blockStatement.statements()) {
            // TODO: Remove this check once statements are non null guaranteed
            if (statement != null) {
                statements.add((BLangStatement) statement.apply(this));
            }
        }
        bLBlockStmt.stmts = statements;
        return bLBlockStmt;
    }

    @Override
    public BLangNode transform(VariableDeclarationNode varDeclaration) {
        BLangSimpleVariableDef bLVarDef = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        bLVarDef.pos = getPosition(varDeclaration);

        BLangSimpleVariable simpleVar = new SimpleVarBuilder()
                .with(varDeclaration.variableName().text(), getPosition(varDeclaration.variableName()))
                .setTypeByNode(varDeclaration.typeName())
                .setExpressionByNode(varDeclaration.initializer().orElse(null))
                .setFinal(varDeclaration.finalKeyword().isPresent())
                .build();

        bLVarDef.setVariable(simpleVar);
        return bLVarDef;
    }

    @Override
    public BLangNode transform(ExpressionStatementNode expressionStatement) {
        BLangExpressionStmt bLExpressionStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        bLExpressionStmt.expr = (BLangExpression) expressionStatement.expression().apply(this);
        bLExpressionStmt.pos = getPosition(expressionStatement);
        return bLExpressionStmt;
    }

    // -------------------------------------------------Misc------------------------------------------------------------

    @Override
    public BLangNode transform(PositionalArgumentNode argumentNode) {
        return createExpression(argumentNode.expression());
    }

    @Override
    public BLangNode transform(NamedArgumentNode namedArgumentNode) {
        BLangNamedArgsExpression namedArg = (BLangNamedArgsExpression) TreeBuilder.createNamedArgNode();
        namedArg.pos = getPosition(namedArgumentNode);
        namedArg.name = this.createIdentifier(getPosition(namedArgumentNode.argumentName()),
                namedArgumentNode.argumentName().name().text());
        namedArg.expr = createExpression(namedArgumentNode.expression());
        return namedArg;
    }

    @Override
    public BLangNode transform(RestArgumentNode restArgumentNode) {
        return restArgumentNode.expression().apply(this);
    }

    @Override
    public BLangNode transform(RequiredParameterNode requiredParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(requiredParameter.paramName(),
                requiredParameter.typeName());

        Optional<Token> visibilityQual = requiredParameter.visibilityQualifier();
        //TODO: Check and Fix flags OPTIONAL, REQUIRED
        if (visibilityQual.isPresent() && visibilityQual.get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
            simpleVar.flagSet.add(Flag.PUBLIC);
        }

        simpleVar.pos = getPosition(requiredParameter);
        return simpleVar;
    }

    @Override
    public BLangNode transform(DefaultableParameterNode defaultableParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(defaultableParameter.paramName(),
                defaultableParameter.typeName());

        Optional<Token> visibilityQual = defaultableParameter.visibilityQualifier();
        //TODO: Check and Fix flags OPTIONAL, REQUIRED
        if (visibilityQual.isPresent() && visibilityQual.get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
            simpleVar.flagSet.add(Flag.PUBLIC);
        }

        simpleVar.setInitialExpression(createExpression(defaultableParameter.expression()));

        simpleVar.pos = getPosition(defaultableParameter);
        return simpleVar;
    }

    @Override
    public BLangNode transform(RestParameterNode restParameter) {
        BLangSimpleVariable bLSimpleVar = createSimpleVar(restParameter.paramName(), restParameter.typeName());

        BLangArrayType bLArrayType = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        bLArrayType.elemtype = bLSimpleVar.typeNode;
        bLArrayType.dimensions = 1;
        bLSimpleVar.typeNode = bLArrayType;
        bLArrayType.pos = getPosition(restParameter.typeName());

        bLSimpleVar.pos = getPosition(restParameter);
        return bLSimpleVar;
    }

    @Override
    public BLangNode transform(OptionalTypeDescriptorNode optTypeDescriptor) {
        BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nilTypeNode.pos = getPosition(optTypeDescriptor.questionMarkToken());
        nilTypeNode.typeKind = TypeKind.NIL;

        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.memberTypeNodes.add(createTypeNode(optTypeDescriptor.typeDescriptor()));
        unionTypeNode.memberTypeNodes.add(nilTypeNode);

        unionTypeNode.pos = getPosition(optTypeDescriptor);
        return unionTypeNode;
    }

    @Override
    public BLangNode transform(FunctionTypeDescriptorNode functionTypeDescriptorNode) {
        BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) TreeBuilder.createFunctionTypeNode();
        functionTypeNode.pos = getPosition(functionTypeDescriptorNode);
        functionTypeNode.returnsKeywordExists = true;

        FunctionSignatureNode funcSignature = functionTypeDescriptorNode.functionSignature();

        // Set Parameters
        for (ParameterNode child : funcSignature.parameters()) {
            SimpleVariableNode param = (SimpleVariableNode) child.apply(this);
            if (child instanceof RestParameterNode) {
                functionTypeNode.restParam = (BLangSimpleVariable) param;
            } else {
                functionTypeNode.params.add((BLangVariable) param);
            }
        }

        // Set Return Type
        Optional<ReturnTypeDescriptorNode> retNode = funcSignature.returnTypeDesc();
        if (retNode.isPresent()) {
            ReturnTypeDescriptorNode returnType = retNode.get();
            functionTypeNode.returnTypeNode = createTypeNode(returnType.type());
        } else {
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            bLValueType.pos = getPosition(funcSignature);
            bLValueType.typeKind = TypeKind.NIL;
            functionTypeNode.returnTypeNode = bLValueType;
        }

        functionTypeNode.flagSet.add(Flag.PUBLIC);
        return functionTypeNode;
    }

    @Override
    public BLangNode transform(ParameterizedTypeDescriptorNode parameterizedTypeDescNode) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        BLangValueType typeNode = (BLangValueType) createBuiltInTypeNode(parameterizedTypeDescNode.parameterizedType());
        refType.typeKind = typeNode.typeKind;
        refType.pos = typeNode.pos;

        BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
        constrainedType.type = refType;
        constrainedType.constraint = createTypeNode(parameterizedTypeDescNode.typeNode());
        constrainedType.pos = getPosition(parameterizedTypeDescNode);
        return constrainedType;
    }

    @Override
    public BLangNode transform(KeySpecifierNode keySpecifierNode) {
        BLangTableKeySpecifier tableKeySpecifierNode =
                (BLangTableKeySpecifier) TreeBuilder.createTableKeySpecifierNode();
        tableKeySpecifierNode.pos = getPosition(keySpecifierNode);

        for (Node field : keySpecifierNode.fieldNames()) {
            tableKeySpecifierNode.addFieldNameIdentifier(createIdentifier(getPosition(field), field.toString()));
        }
        return tableKeySpecifierNode;
    }

    @Override
    public BLangNode transform(KeyTypeConstraintNode keyTypeConstraintNode) {
        BLangTableKeyTypeConstraint tableKeyTypeConstraint = new BLangTableKeyTypeConstraint();
        tableKeyTypeConstraint.pos = getPosition(keyTypeConstraintNode);
        tableKeyTypeConstraint.keyType = createTypeNode(keyTypeConstraintNode.typeParameterNode());
        return tableKeyTypeConstraint;
    }

    @Override
    public BLangNode transform(TableTypeDescriptorNode tableTypeDescriptorNode) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(tableTypeDescriptorNode.tableKeywordToken().text());
        refType.pos = getPosition(tableTypeDescriptorNode);

        BLangTableTypeNode tableTypeNode = (BLangTableTypeNode) TreeBuilder.createTableTypeNode();
        tableTypeNode.pos = getPosition(tableTypeDescriptorNode);
        tableTypeNode.type = refType;
        tableTypeNode.constraint = createTypeNode(tableTypeDescriptorNode.rowTypeParameterNode());
        if (tableTypeDescriptorNode.keyConstraintNode() != null) {
            Node constraintNode = tableTypeDescriptorNode.keyConstraintNode();
            if (constraintNode.kind() == SyntaxKind.KEY_TYPE_CONSTRAINT) {
                tableTypeNode.tableKeyTypeConstraint =
                        (BLangTableKeyTypeConstraint) constraintNode.apply(this);
            } else if (constraintNode.kind() == SyntaxKind.KEY_SPECIFIER) {
                tableTypeNode.tableKeySpecifier = (BLangTableKeySpecifier) constraintNode.apply(this);
            }
        }
        return tableTypeNode;
    }

    @Override
    public BLangNode transform(SimpleNameReferenceNode simpleNameRefNode) {
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = getPosition(simpleNameRefNode);
        varRef.variableName = createIdentifier(getPosition(simpleNameRefNode.name()), simpleNameRefNode.name().text());
        varRef.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        return varRef;
    }

    @Override
    public BLangNode transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = getPosition(qualifiedNameReferenceNode);
        varRef.variableName = createIdentifier(getPosition(qualifiedNameReferenceNode.identifier()),
                qualifiedNameReferenceNode.identifier().text());
        varRef.pkgAlias = createIdentifier(getPosition(qualifiedNameReferenceNode.modulePrefix()),
                qualifiedNameReferenceNode.modulePrefix().text());
        return varRef;
    }

    @Override
    public BLangNode transform(XMLProcessingInstruction xmlProcessingInstruction) {
        BLangXMLProcInsLiteral xmlProcInsLiteral =
                (BLangXMLProcInsLiteral) TreeBuilder.createXMLProcessingIntsructionLiteralNode();
        for (Node dataNode: xmlProcessingInstruction.data()) {
            xmlProcInsLiteral.dataFragments.add(createExpression(dataNode));
        }
        xmlProcInsLiteral.target = (BLangLiteral) xmlProcessingInstruction.target().apply(this);
        return xmlProcInsLiteral;
    }

    @Override
    public BLangNode transform(XMLSimpleNameNode xmlSimpleNameNode) {
        BLangLiteral bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        bLiteral.pos = getPosition(xmlSimpleNameNode);
        bLiteral.type = symTable.getTypeFromTag(TypeTags.STRING);
        bLiteral.value = xmlSimpleNameNode.name().text();
        bLiteral.originalValue = xmlSimpleNameNode.name().text();
        return bLiteral;
    }

    @Override
    public BLangNode transform(XMLComment xmlComment) {
        BLangXMLCommentLiteral xmlCommentLiteral = (BLangXMLCommentLiteral) TreeBuilder.createXMLCommentLiteralNode();
        for (Node commentNode: xmlComment.content()) {
            xmlCommentLiteral.textFragments.add((BLangExpression) commentNode.apply(this));
        }
        xmlCommentLiteral.pos = getPosition(xmlComment);
        return xmlCommentLiteral;
    }

    @Override
    public BLangNode transform(XMLElementNode xmlElementNode) {
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) TreeBuilder.createXMLElementLiteralNode();
        xmlElement.startTagName = createExpression(xmlElementNode.startTag());
        xmlElement.endTagName = createExpression(xmlElementNode.endTag());

        for (Node node : xmlElementNode.content()) {
            if (node.kind() == SyntaxKind.XML_TEXT) {
                xmlElement.children.add(createSimpleLiteral(((XMLTextNode) node).content()));
                continue;
            }
            xmlElement.children.add(createExpression(node));
        }
        xmlElement.pos = getPosition(xmlElementNode);
        xmlElement.isRoot = true; // TODO : check this
        return xmlElement;
    }

    @Override
    public BLangNode transform(XMLStartTagNode startTagNode) {
        return createStartEndXMLTag(startTagNode.name(), getPosition(startTagNode));
    }

    @Override
    public BLangNode transform(XMLEndTagNode endTagNode) {
        return createStartEndXMLTag(endTagNode.name(), getPosition(endTagNode));
    }

    @Override
    public BLangNode transform(XMLTextNode xmlTextNode) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.textFragments.add(0, (BLangExpression) xmlTextNode.content().apply(this));
        xmlTextLiteral.pos = getPosition(xmlTextNode);
        return xmlTextLiteral;
    }

    @Override
    public BLangNode transform(ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
        int dimensions = 1;
        List<Integer> sizes = new ArrayList<>();
        while (true) {
            if (!arrayTypeDescriptorNode.arrayLength().isPresent()) {
                sizes.add(UNSEALED_ARRAY_INDICATOR);
            } else {
                Node keyExpr = arrayTypeDescriptorNode.arrayLength().get();
                if (keyExpr.kind() == SyntaxKind.DECIMAL_INTEGER_LITERAL) {
                    sizes.add(Integer.parseInt(keyExpr.toString()));
                } else if (keyExpr.kind() == SyntaxKind.ASTERISK_TOKEN) {
                    sizes.add(OPEN_SEALED_ARRAY_INDICATOR);
                } else {
                    // TODO : should handle the const-reference-expr
                }
            }

            if (arrayTypeDescriptorNode.memberTypeDesc().kind() != SyntaxKind.ARRAY_TYPE_DESC) {
                break;
            }

            arrayTypeDescriptorNode = (ArrayTypeDescriptorNode) arrayTypeDescriptorNode.memberTypeDesc();
            dimensions++;
        }

        BLangArrayType arrayTypeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        arrayTypeNode.pos = getPosition(arrayTypeDescriptorNode);
        arrayTypeNode.elemtype = createTypeNode(arrayTypeDescriptorNode.memberTypeDesc());
        arrayTypeNode.dimensions = dimensions;
        arrayTypeNode.sizes = sizes.stream().mapToInt(val -> val).toArray();
        return arrayTypeNode;
    }
    
    @Override
    protected BLangNode transformSyntaxNode(Node node) {
        // TODO: Remove this RuntimeException once all nodes covered
        throw new RuntimeException("Node not supported: " + node.getClass().getSimpleName());
    }

    // ------------------------------------------private methods--------------------------------------------------------
    
    private void populateFuncSignature(BLangFunction bLFunction, FunctionSignatureNode funcSignature) {
        // Set Parameters
        for (ParameterNode child : funcSignature.parameters()) {
            SimpleVariableNode param = (SimpleVariableNode) child.apply(this);
            if (child instanceof RestParameterNode) {
                bLFunction.setRestParameter(param);
            } else {
                bLFunction.addParameter(param);
            }
        }

        // Set Return Type
        Optional<ReturnTypeDescriptorNode> retNode = funcSignature.returnTypeDesc();
        if (retNode.isPresent()) {
            ReturnTypeDescriptorNode returnType = retNode.get();
            bLFunction.setReturnTypeNode(createTypeNode(returnType.type()));
        } else {
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            bLValueType.pos = getPosition(funcSignature);
            bLValueType.typeKind = TypeKind.NIL;
            bLFunction.setReturnTypeNode(bLValueType);
        }
    }

    private BLangUnaryExpr createBLangUnaryExpr(DiagnosticPos pos, OperatorKind operatorKind, BLangExpression expr) {
        BLangUnaryExpr bLUnaryExpr = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        bLUnaryExpr.pos = pos;
        bLUnaryExpr.operator = operatorKind;
        bLUnaryExpr.expr = expr;
        return bLUnaryExpr;
    }

    private BLangXMLQName createStartEndXMLTag(XMLNameNode xmlNameNode, DiagnosticPos pos) {
        BLangXMLQName xmlName = (BLangXMLQName) TreeBuilder.createXMLQNameNode();
        SyntaxKind kind = xmlNameNode.kind();
        xmlName.pos = pos;
        switch (kind) {
            case XML_QUALIFIED_NAME:
                xmlName.localname = createIdentifier(getPosition(((XMLQualifiedNameNode) xmlNameNode).name()),
                        ((XMLQualifiedNameNode) xmlNameNode).name().toString());
                xmlName.prefix = createIdentifier(getPosition(((XMLQualifiedNameNode) xmlNameNode).prefix()),
                        ((XMLQualifiedNameNode) xmlNameNode).prefix().toString());
                return xmlName;
            case XML_SIMPLE_NAME:
                xmlName.localname = createIdentifier(getPosition(((XMLSimpleNameNode) xmlNameNode).name()),
                        ((XMLSimpleNameNode) xmlNameNode).name().text());
                xmlName.prefix = createIdentifier(null, "");
                return xmlName;
            default:
                throw new RuntimeException("Syntax kind is not supported: " + kind);
        }
    }

    private BLangExpression createExpression(Node expression) {
        if (isSimpleLiteral(expression.kind())) {
            return createSimpleLiteral(expression);
        } else if (expression.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                   expression.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE ||
                   expression.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            // Variable References
            BLangNameReference nameReference = createBLangNameReference(expression);
            BLangSimpleVarRef bLVarRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
            bLVarRef.pos = getPosition(expression);
            bLVarRef.pkgAlias = this.createIdentifier((DiagnosticPos) nameReference.pkgAlias.getPosition(),
                                                      nameReference.pkgAlias.getValue());
            bLVarRef.variableName = this.createIdentifier((DiagnosticPos) nameReference.name.getPosition(),
                                                          nameReference.name.getValue());
            return bLVarRef;
        } else if (expression.kind() == SyntaxKind.BRACED_EXPRESSION) {
            BLangGroupExpr group = (BLangGroupExpr) TreeBuilder.createGroupExpressionNode();
            group.expression = (BLangExpression) expression.apply(this);
            return group;
        } else {
            return (BLangExpression) expression.apply(this);
        }
    }

    private BLangNode createStringTemplateLiteral(NodeList<TemplateMemberNode> memberNodes, DiagnosticPos pos) {
        BLangStringTemplateLiteral stringTemplateLiteral =
                (BLangStringTemplateLiteral) TreeBuilder.createStringTemplateLiteralNode();
        for (Node memberNode : memberNodes) {
            stringTemplateLiteral.exprs.add((BLangExpression) memberNode.apply(this));
        }
        stringTemplateLiteral.pos = pos;
        return stringTemplateLiteral;
    }

    private BLangSimpleVariable createSimpleVar(Optional<Token> name, Node type) {
        if (name.isPresent()) {
            Token nameToken = name.get();
            return createSimpleVar(nameToken, type, null, false, false, null);
        }

        return createSimpleVar(null, null, type, null, false, false, null);
    }

    private BLangSimpleVariable createSimpleVar(Token name, Node type) {
        return createSimpleVar(name, type, null, false, false, null);
    }

    private BLangSimpleVariable createSimpleVar(Token name, Node typeName, Node initializer, boolean isFinal,
                                                boolean isListenerVar,
                                                Token visibilityQualifier) {
        return createSimpleVar(name.text(), getPosition(name), typeName, initializer, isFinal, isListenerVar,
                               visibilityQualifier);
    }

    private BLangSimpleVariable createSimpleVar(String name, DiagnosticPos pos, Node typeName, Node initializer,
                                                boolean isFinal,
                                                boolean isListenerVar,
                                                Token visibilityQualifier) {
        BLangSimpleVariable bLSimpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        bLSimpleVar.setName(this.createIdentifier(pos, name));

        if (typeName == null || typeName.kind() == SyntaxKind.VAR_TYPE_DESC) {
            bLSimpleVar.isDeclaredWithVar = true;
        } else {
            bLSimpleVar.setTypeNode(createTypeNode(typeName));
        }

        if (visibilityQualifier != null) {
            if (visibilityQualifier.kind() == SyntaxKind.PRIVATE_KEYWORD) {
                bLSimpleVar.flagSet.add(Flag.PRIVATE);
            } else if (visibilityQualifier.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                bLSimpleVar.flagSet.add(Flag.PUBLIC);
            }
        }

        if (isFinal) {
            markVariableAsFinal(bLSimpleVar);
        }
        if (initializer != null) {
            bLSimpleVar.setInitialExpression(createExpression(initializer));
        }
        if (isListenerVar) {
            bLSimpleVar.flagSet.add(Flag.LISTENER);
            bLSimpleVar.flagSet.add(Flag.FINAL);
        }
        return bLSimpleVar;
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, String value) {
        return createIdentifier(pos, value, null);
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, String value, Set<Whitespace> ws) {
        BLangIdentifier bLIdentifer = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        if (value == null) {
            return bLIdentifer;
        }

        if (value.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            if (!escapeQuotedIdentifier(value).matches("^[0-9a-zA-Z.]*$")) {
                dlog.error(pos, DiagnosticCode.IDENTIFIER_LITERAL_ONLY_SUPPORTS_ALPHANUMERICS);
            }
            String unescapedValue = StringEscapeUtils.unescapeJava(value);
            bLIdentifer.setValue(unescapedValue.substring(1));
            bLIdentifer.originalValue = value;
            bLIdentifer.setLiteral(true);
        } else {
            bLIdentifer.setValue(value);
            bLIdentifer.setLiteral(false);
        }
        bLIdentifer.pos = pos;
        if (ws != null) {
            bLIdentifer.addWS(ws);
        }
        return bLIdentifer;
    }

    private BLangLiteral createSimpleLiteral(Node literal) {
        BLangLiteral bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        SyntaxKind type = literal.kind();
        int typeTag = -1;
        Object value = null;
        String originalValue = null;

        String textValue;
        if (literal instanceof BasicLiteralNode) {
            textValue = ((BasicLiteralNode) literal).literalToken().text();
        } else if (literal instanceof Token) {
            textValue = ((Token) literal).text();
        } else {
            textValue = "";
        }

        //TODO: Verify all types, only string type tested
        if (type == SyntaxKind.DECIMAL_INTEGER_LITERAL || type == SyntaxKind.HEX_INTEGER_LITERAL) {
            typeTag = TypeTags.INT;
            value = getIntegerLiteral(literal, textValue);
            originalValue = textValue;
            bLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else if (type == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL) {
            //TODO: Check effect of mapping negative(-) numbers as unary-expr
            typeTag = NumericLiteralSupport.isDecimalDiscriminated(textValue) ? TypeTags.DECIMAL : TypeTags.FLOAT;
            value = textValue;
            originalValue = textValue;
            bLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else if (type == SyntaxKind.HEX_FLOATING_POINT_LITERAL) {
            //TODO: Check effect of mapping negative(-) numbers as unary-expr
            typeTag = TypeTags.FLOAT;
            value = getHexNodeValue(textValue);
            originalValue = textValue;
            bLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else if (type == SyntaxKind.TRUE_KEYWORD || type == SyntaxKind.FALSE_KEYWORD) {
            typeTag = TypeTags.BOOLEAN;
            value = Boolean.parseBoolean(textValue);
            originalValue = textValue;
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.STRING_LITERAL || type == SyntaxKind.XML_TEXT_CONTENT ||
                type == SyntaxKind.TEMPLATE_STRING) {
            String text = textValue;
            if (type == SyntaxKind.STRING_LITERAL) {
                text = text.substring(1, text.length() - 1);
            }
            String originalText = text; // to log the errors
            Matcher matcher = UNICODE_PATTERN.matcher(text);
            int position = 0;
            while (matcher.find(position)) {
                String hexStringVal = matcher.group(1);
                int hexDecimalVal = Integer.parseInt(hexStringVal, 16);
                if ((hexDecimalVal >= Constants.MIN_UNICODE && hexDecimalVal <= Constants.MIDDLE_LIMIT_UNICODE)
                        || hexDecimalVal > Constants.MAX_UNICODE) {
                    String hexStringWithBraces = matcher.group(0);
                    int offset = originalText.indexOf(hexStringWithBraces) + 1;
                    DiagnosticPos pos = getPosition(literal);
                    dlog.error(new DiagnosticPos(this.diagnosticSource, pos.sLine, pos.eLine, pos.sCol + offset,
                                                 pos.sCol + offset + hexStringWithBraces.length()),
                               DiagnosticCode.INVALID_UNICODE, hexStringWithBraces);
                }
                text = matcher.replaceFirst("\\\\u" + fillWithZeros(hexStringVal));
                position = matcher.end() - 2;
                matcher = UNICODE_PATTERN.matcher(text);
            }
            text = StringEscapeUtils.unescapeJava(text);

            typeTag = TypeTags.STRING;
            value = text;
            originalValue = textValue;
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.NULL_KEYWORD) {
            typeTag = TypeTags.NIL;
            value = null;
            originalValue = "null";
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.NIL_LITERAL) {
            typeTag = TypeTags.NIL;
            value = null;
            originalValue = "()";
            bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.BINARY_EXPRESSION) { // Should be base16 and base64
            typeTag = TypeTags.BYTE_ARRAY;
            value = textValue;
            originalValue = textValue;

            // If numeric literal create a numeric literal expression; otherwise create a literal expression
            if (isNumericLiteral(type)) {
                bLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
            } else {
                bLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            }
        }

        bLiteral.pos = getPosition(literal);
        bLiteral.type = symTable.getTypeFromTag(typeTag);
        bLiteral.type.tag = typeTag;
        bLiteral.value = value;
        bLiteral.originalValue = originalValue;
        return bLiteral;
    }

    private BLangType createTypeNode(Node type) {
        if (type instanceof BuiltinSimpleNameReferenceNode || type.kind() == SyntaxKind.NIL_TYPE_DESC) {
            return createBuiltInTypeNode(type);
        } else if (type.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE || type.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            // Exclusive type
            BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            BLangNameReference nameReference = createBLangNameReference(type);
            bLUserDefinedType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
            bLUserDefinedType.typeName = (BLangIdentifier) nameReference.name;
            bLUserDefinedType.pos = getPosition(type);
            return bLUserDefinedType;
        } else if (type.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            // Map name reference as a type
            SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) type;
            return createTypeNode(nameReferenceNode.name());
        }

        return (BLangType) type.apply(this);
    }

    private BLangType createBuiltInTypeNode(Node type) {
        String typeText;
        if (type.kind() == SyntaxKind.NIL_TYPE_DESC) {
            typeText = "()";
        } else if (type instanceof BuiltinSimpleNameReferenceNode) {
            typeText = ((BuiltinSimpleNameReferenceNode) type).name().text();
        } else {
            typeText = ((Token) type).text(); // TODO: Remove this once map<string> returns Nodes for `map`
        }
        TypeKind typeKind = TreeUtils.stringToTypeKind(typeText.replaceAll("\\s+", ""));

        if (typeKind == TypeKind.JSON) { //TODO: add full list of types
            BLangBuiltInRefTypeNode bLValueType =
                    (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
            bLValueType.typeKind = typeKind;
            bLValueType.pos = getPosition(type);
            return bLValueType;
        } else {
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            bLValueType.typeKind = typeKind;
            bLValueType.pos = getPosition(type);
            return bLValueType;
        }
    }

    private VariableNode createBasicVarNodeWithoutType(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                                       DiagnosticPos identifierPos, ExpressionNode expr) {
        BLangSimpleVariable bLSimpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        bLSimpleVar.pos = pos;
        IdentifierNode name = this.createIdentifier(identifierPos, identifier, ws);
        ((BLangIdentifier) name).pos = identifierPos;
        bLSimpleVar.setName(name);
        bLSimpleVar.addWS(ws);
        if (expr != null) {
            bLSimpleVar.setInitialExpression(expr);
        }
        return bLSimpleVar;
    }

    private BLangInvocation createBLangInvocation(Node nameNode, NodeList<FunctionArgumentNode> arguments,
                                                  DiagnosticPos position) {
        BLangInvocation bLInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        BLangNameReference reference = createBLangNameReference(nameNode);
        bLInvocation.pkgAlias = (BLangIdentifier) reference.pkgAlias;
        bLInvocation.name = (BLangIdentifier) reference.name;

        List<BLangExpression> args = new ArrayList<>();
        arguments.iterator().forEachRemaining(arg -> args.add((BLangExpression) arg.apply(this)));
        bLInvocation.argExprs = args;
        bLInvocation.pos = position;
        return bLInvocation;
    }

    private BLangNameReference createBLangNameReference(Node node) {
        if (node.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            // qualified identifier
            QualifiedNameReferenceNode iNode = (QualifiedNameReferenceNode) node;
            Token modulePrefix = iNode.modulePrefix();
            IdentifierToken identifier = iNode.identifier();
            BLangIdentifier pkgAlias = this.createIdentifier(getPosition(modulePrefix), modulePrefix.text());
            BLangIdentifier name = this.createIdentifier(getPosition(identifier), identifier.text());
            return new BLangNameReference(getPosition(node), null, pkgAlias, name);
        } else if (node.kind() == SyntaxKind.IDENTIFIER_TOKEN || node.kind() == SyntaxKind.ERROR_KEYWORD) {
            // simple identifier
            Token token = (Token) node;
            BLangIdentifier pkgAlias = this.createIdentifier(null, "");
            BLangIdentifier name = this.createIdentifier(getPosition(token), token.text());
            return new BLangNameReference(getPosition(node), null, pkgAlias, name);
        } else if (node.kind() == SyntaxKind.NEW_KEYWORD) {
            Token iToken = (Token) node;
            BLangIdentifier pkgAlias = this.createIdentifier(getPosition(iToken), "");
            BLangIdentifier name = this.createIdentifier(getPosition(iToken), iToken.text());
            return new BLangNameReference(getPosition(node), null, pkgAlias, name);
        } else {
            // Map name reference as a name
            SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) node;
            // TODO: Fix when there's MISSING_TOKEN
            return createBLangNameReference(nameReferenceNode.name());
        }
    }

    private Object getIntegerLiteral(Node literal, String nodeValue) {
        SyntaxKind type = literal.kind();
        if (type == SyntaxKind.DECIMAL_INTEGER_LITERAL) {
            return parseLong(literal, nodeValue, nodeValue, 10, DiagnosticCode.INTEGER_TOO_SMALL,
                    DiagnosticCode.INTEGER_TOO_LARGE);
        } else if (type == SyntaxKind.HEX_INTEGER_LITERAL) {
            String processedNodeValue = nodeValue.toLowerCase().replace("0x", "");
            return parseLong(literal, nodeValue, processedNodeValue, 16,
                    DiagnosticCode.HEXADECIMAL_TOO_SMALL, DiagnosticCode.HEXADECIMAL_TOO_LARGE);
        }
        return null;
    }

    private Object parseLong(Node literal, String originalNodeValue,
                             String processedNodeValue, int radix,
                             DiagnosticCode code1, DiagnosticCode code2) {
        try {
            return Long.parseLong(processedNodeValue, radix);
        } catch (Exception e) {
            DiagnosticPos pos = getPosition(literal);
            if (originalNodeValue.startsWith("-")) {
                dlog.error(pos, code1, originalNodeValue);
            } else {
                dlog.error(pos, code2, originalNodeValue);
            }
        }
        return originalNodeValue;
    }

    private String getHexNodeValue(String value) {
        if (!(value.contains("p") || value.contains("P"))) {
            value = value + "p0";
        }
        return value;
    }

    private String fillWithZeros(String str) {
        while (str.length() < 4) {
            str = "0".concat(str);
        }
        return str;
    }

    private void markVariableAsFinal(BLangVariable variable) {
        // Set the final flag to the variable.
        variable.flagSet.add(Flag.FINAL);

        switch (variable.getKind()) {
            case TUPLE_VARIABLE:
                // If the variable is a tuple variable, we need to set the final flag to the all member variables.
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                tupleVariable.memberVariables.forEach(this::markVariableAsFinal);
                if (tupleVariable.restVariable != null) {
                    markVariableAsFinal(tupleVariable.restVariable);
                }
                break;
            case RECORD_VARIABLE:
                // If the variable is a record variable, we need to set the final flag to the all the variables in
                // the record.
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.variableList.stream()
                        .map(BLangRecordVariable.BLangRecordVariableKeyValue::getValue)
                        .forEach(this::markVariableAsFinal);
                if (recordVariable.restParam != null) {
                    markVariableAsFinal((BLangVariable) recordVariable.restParam);
                }
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                markVariableAsFinal(errorVariable.reason);
                errorVariable.detail.forEach(entry -> markVariableAsFinal(entry.valueBindingPattern));
                if (errorVariable.restDetail != null) {
                    markVariableAsFinal(errorVariable.restDetail);
                }
                break;
        }
    }

    private boolean isSimpleLiteral(SyntaxKind syntaxKind) {
        switch (syntaxKind) {
            case STRING_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NIL_LITERAL:
            case NULL_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    private boolean isNumericLiteral(SyntaxKind syntaxKind) {
        switch (syntaxKind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                return true;
            default:
                return false;
        }
    }

    // If this is a quoted identifier then unescape it and remove the quote prefix.
    // Else return original.
    private static String escapeQuotedIdentifier(String identifier) {
        if (identifier.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            identifier = StringEscapeUtils.unescapeJava(identifier).substring(1);
        }
        return identifier;
    }

    private boolean isPresent(Node node) {
        return node.kind() != SyntaxKind.NONE;
    }

    private BLangUnionTypeNode addUnionType(BLangType lhsTypeNode, BLangType rhsTypeNode, DiagnosticPos position) {
        BLangUnionTypeNode unionTypeNode;
        if (rhsTypeNode.getKind() == NodeKind.UNION_TYPE_NODE) {
            unionTypeNode = (BLangUnionTypeNode) rhsTypeNode;
            unionTypeNode.memberTypeNodes.add(0, lhsTypeNode);
        } else if (lhsTypeNode.getKind() == NodeKind.UNION_TYPE_NODE) {
            unionTypeNode = (BLangUnionTypeNode) lhsTypeNode;
            unionTypeNode.memberTypeNodes.add(rhsTypeNode);
        } else {
            unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
            unionTypeNode.memberTypeNodes.add(lhsTypeNode);
            unionTypeNode.memberTypeNodes.add(rhsTypeNode);
        }

        unionTypeNode.pos = position;
        return unionTypeNode;
    }

    private class SimpleVarBuilder {
        private BLangIdentifier name;
        private BLangType type;
        private boolean isDeclaredWithVar;
        private Set<Flag> flags = new HashSet<>();
        private boolean isFinal;
        private ExpressionNode expr;

        public BLangSimpleVariable build() {
            BLangSimpleVariable bLSimpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
            bLSimpleVar.setName(this.name);
            bLSimpleVar.setTypeNode(this.type);
            bLSimpleVar.isDeclaredWithVar = this.isDeclaredWithVar;
            bLSimpleVar.setTypeNode(this.type);
            bLSimpleVar.flagSet.addAll(this.flags);
            if (this.isFinal) {
                markVariableAsFinal(bLSimpleVar);
            }
            bLSimpleVar.setInitialExpression(this.expr);
            return bLSimpleVar;
        }

        public SimpleVarBuilder with(String name) {
            this.name = createIdentifier(null, name);
            return this;
        }

        public SimpleVarBuilder with(String name, DiagnosticPos identifierPos) {
            this.name = createIdentifier(identifierPos, name);
            return this;
        }

        public SimpleVarBuilder setTypeByNode(Node typeName) {
            if (typeName == null || typeName.kind() == SyntaxKind.VAR_TYPE_DESC) {
                this.isDeclaredWithVar = true;
            }
            this.type = createTypeNode(typeName);
            return this;
        }

        public SimpleVarBuilder setExpressionByNode(Node initExprNode) {
            this.expr = initExprNode != null ? createExpression(initExprNode) : null;
            return this;
        }

        public SimpleVarBuilder setExpression(ExpressionNode expression) {
            this.expr = expression;
            return this;
        }

        public SimpleVarBuilder isDeclaredWithVar() {
            this.isDeclaredWithVar = true;
            return this;
        }

        public SimpleVarBuilder isFinal() {
            this.isFinal = true;
            return this;
        }

        public SimpleVarBuilder isListenerVar() {
            this.flags.add(Flag.LISTENER);
            this.flags.add(Flag.FINAL);
            return this;
        }

        public SimpleVarBuilder setVisibility(Token visibilityQualifier) {
            if (visibilityQualifier != null) {
                if (visibilityQualifier.kind() == SyntaxKind.PRIVATE_KEYWORD) {
                    this.flags.add(Flag.PRIVATE);
                } else if (visibilityQualifier.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                    this.flags.add(Flag.PUBLIC);
                }
            }
            return this;
        }

        public SimpleVarBuilder setFinal(boolean present) {
            this.isFinal = present;
            return this;
        }

        public SimpleVarBuilder setOptional(boolean present) {
            if (present) {
                this.flags.add(Flag.PUBLIC);
            } else {
                this.flags.remove(Flag.PUBLIC);
            }
            return this;
        }

        public SimpleVarBuilder setRequired(boolean present) {
            if (present) {
                this.flags.add(Flag.REQUIRED);
            } else {
                this.flags.remove(Flag.REQUIRED);
            }
            return this;
        }

        public SimpleVarBuilder isPublic() {
            this.flags.add(Flag.PUBLIC);
            return this;
        }

        public SimpleVarBuilder isWorkerVar() {
            this.flags.add(Flag.WORKER);
            return this;
        }
    }
}

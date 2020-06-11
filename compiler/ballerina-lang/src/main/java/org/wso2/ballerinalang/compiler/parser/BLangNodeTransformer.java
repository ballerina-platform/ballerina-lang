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

import io.ballerinalang.compiler.syntax.tree.AnnotationAttachPointNode;
import io.ballerinalang.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.AnnotationNode;
import io.ballerinalang.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.AsyncSendActionNode;
import io.ballerinalang.compiler.syntax.tree.BasicLiteralNode;
import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.BlockStatementNode;
import io.ballerinalang.compiler.syntax.tree.BracedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BreakStatementNode;
import io.ballerinalang.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.CheckExpressionNode;
import io.ballerinalang.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ContinueStatementNode;
import io.ballerinalang.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerinalang.compiler.syntax.tree.DocumentationStringNode;
import io.ballerinalang.compiler.syntax.tree.ElseBlockNode;
import io.ballerinalang.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ErrorTypeParamsNode;
import io.ballerinalang.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionListItemNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerinalang.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ForEachStatementNode;
import io.ballerinalang.compiler.syntax.tree.ForkStatementNode;
import io.ballerinalang.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.IfElseStatementNode;
import io.ballerinalang.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ImplicitAnonymousFunctionParameters;
import io.ballerinalang.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerinalang.compiler.syntax.tree.ImportPrefixNode;
import io.ballerinalang.compiler.syntax.tree.ImportVersionNode;
import io.ballerinalang.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.InterpolationNode;
import io.ballerinalang.compiler.syntax.tree.KeySpecifierNode;
import io.ballerinalang.compiler.syntax.tree.KeyTypeConstraintNode;
import io.ballerinalang.compiler.syntax.tree.ListBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.LockStatementNode;
import io.ballerinalang.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MappingFieldNode;
import io.ballerinalang.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.NamedArgumentNode;
import io.ballerinalang.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerinalang.compiler.syntax.tree.NewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.NodeTransformer;
import io.ballerinalang.compiler.syntax.tree.ObjectFieldNode;
import io.ballerinalang.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.PanicStatementNode;
import io.ballerinalang.compiler.syntax.tree.ParameterNode;
import io.ballerinalang.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerinalang.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.ReceiveActionNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerinalang.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerinalang.compiler.syntax.tree.RequiredParameterNode;
import io.ballerinalang.compiler.syntax.tree.RestArgumentNode;
import io.ballerinalang.compiler.syntax.tree.RestBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.RestDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RestParameterNode;
import io.ballerinalang.compiler.syntax.tree.ReturnStatementNode;
import io.ballerinalang.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SeparatedNodeList;
import io.ballerinalang.compiler.syntax.tree.ServiceBodyNode;
import io.ballerinalang.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SingletonTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SpecificFieldNode;
import io.ballerinalang.compiler.syntax.tree.SpreadFieldNode;
import io.ballerinalang.compiler.syntax.tree.StartActionNode;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.SyncSendActionNode;
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
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypeParameterNode;
import io.ballerinalang.compiler.syntax.tree.TypeReferenceNode;
import io.ballerinalang.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerinalang.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerinalang.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.WaitActionNode;
import io.ballerinalang.compiler.syntax.tree.WhileStatementNode;
import io.ballerinalang.compiler.syntax.tree.WildcardBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.XMLAttributeNode;
import io.ballerinalang.compiler.syntax.tree.XMLAttributeValue;
import io.ballerinalang.compiler.syntax.tree.XMLComment;
import io.ballerinalang.compiler.syntax.tree.XMLElementNode;
import io.ballerinalang.compiler.syntax.tree.XMLEndTagNode;
import io.ballerinalang.compiler.syntax.tree.XMLNamespaceDeclarationNode;
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
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.DocumentationReferenceType;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
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
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordSpreadOperatorField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
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
import org.wso2.ballerinalang.compiler.util.QuoteType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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

    private BLangCompilationUnit currentCompilationUnit;
    private static final Pattern UNICODE_PATTERN = Pattern.compile(Constants.UNICODE_REGEX);
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;

    /* To keep track of additional statements produced from multi-BLangNode resultant transformations */
    private Stack<BLangStatement> additionalStatements = new Stack<>();
    /* To keep track if we are inside a block statment for the use of type definition creation */
    private boolean isInLocalContext = false;

    public BLangNodeTransformer(CompilerContext context, BDiagnosticSource diagnosticSource) {
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.diagnosticSource = diagnosticSource;
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
    }

    public List<org.ballerinalang.model.tree.Node> accept(Node node) {
        BLangNode bLangNode = node.apply(this);
        List<org.ballerinalang.model.tree.Node> nodes = new ArrayList<>();
        // if not already consumed, add left-over statements
        while (!additionalStatements.empty()) {
            nodes.add(additionalStatements.pop());
        }
        nodes.add(bLangNode);
        return nodes;
    }

    @Override
    public BLangNode transform(IdentifierToken identifierToken) {
        return this.createIdentifier(getPosition(identifierToken), identifierToken);
    }

    private DiagnosticPos getPosition(Node node) {
        if (node == null) {
            return null;
        }
        LineRange lineRange = node.lineRange();
        LinePosition startPos = lineRange.startLine();
        LinePosition endPos = lineRange.endLine();
        return new DiagnosticPos(diagnosticSource, startPos.line() + 1, endPos.line() + 1, startPos.offset() + 1,
                endPos.offset() + 1);
    }

    @Override
    public BLangNode transform(ModulePartNode modulePart) {
        BLangCompilationUnit compilationUnit = (BLangCompilationUnit) TreeBuilder.createCompilationUnit();
        this.currentCompilationUnit = compilationUnit;
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

        compilationUnit.pos = pos;
        this.currentCompilationUnit = null;
        return compilationUnit;
    }

    @Override
    public BLangNode transform(ModuleVariableDeclarationNode modVarDeclrNode) {
        TypedBindingPatternNode typedBindingPattern = modVarDeclrNode.typedBindingPattern();
        CaptureBindingPatternNode bindingPattern = (CaptureBindingPatternNode) typedBindingPattern.bindingPattern();
        BLangSimpleVariable simpleVar = createSimpleVar(bindingPattern.variableName(),
                    typedBindingPattern.typeDescriptor(), modVarDeclrNode.initializer(),
                    modVarDeclrNode.finalKeyword().isPresent(), false, null, modVarDeclrNode.metadata().annotations());
        simpleVar.pos = getPosition(modVarDeclrNode);
        simpleVar.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(modVarDeclrNode.metadata().documentationString());
        return simpleVar;
    }

    @Override
    public BLangNode transform(ImportDeclarationNode importDeclaration) {
        ImportOrgNameNode orgNameNode = importDeclaration.orgName().orElse(null);
        ImportVersionNode versionNode = importDeclaration.version().orElse(null);
        ImportPrefixNode prefixNode = importDeclaration.prefix().orElse(null);

        Token orgName = null;
        if (orgNameNode != null) {
            orgName = orgNameNode.orgName();
        }

        String version = null;
        if (versionNode != null) {
            if (versionNode.isMissing()) {
                version = missingNodesHelper.getNextMissingNodeName(diagnosticSource.pkgID);
            }

            // TODO: toString will contain trivia such as comments as well.
            version = versionNode.versionNumber().toString();
        }

        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        NodeList<IdentifierToken> names = importDeclaration.moduleName();
        names.forEach(name -> pkgNameComps.add(this.createIdentifier(getPosition(name), name.text(), null)));

        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = getPosition(importDeclaration);
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.orgName = this.createIdentifier(getPosition(orgNameNode), orgName);
        importDcl.version = this.createIdentifier(getPosition(versionNode), version);
        importDcl.alias = (prefixNode != null) ? this.createIdentifier(getPosition(prefixNode), prefixNode.prefix())
                : pkgNameComps.get(pkgNameComps.size() - 1);

        return importDcl;
    }

    @Override
    public BLangNode transform(MethodDeclarationNode methodDeclarationNode) {
        BLangFunction bLFunction = createFunctionNode(methodDeclarationNode.methodName(),
                methodDeclarationNode.visibilityQualifier(), methodDeclarationNode.methodSignature(), null);

        bLFunction.annAttachments = applyAll(methodDeclarationNode.metadata().annotations());
        bLFunction.pos = getPosition(methodDeclarationNode);
        return bLFunction;
    }

    public BLangNode transform(ConstantDeclarationNode constantDeclarationNode) {
        BLangConstant constantNode = (BLangConstant) TreeBuilder.createConstantNode();
        DiagnosticPos pos = getPosition(constantDeclarationNode);
        DiagnosticPos identifierPos = getPosition(constantDeclarationNode.variableName());
        constantNode.name = createIdentifier(pos, constantDeclarationNode.variableName());
        constantNode.expr = createExpression(constantDeclarationNode.initializer());
        if (constantDeclarationNode.typeDescriptor() != null) {
            constantNode.typeNode = createTypeNode(constantDeclarationNode.typeDescriptor());
        }

        constantNode.annAttachments = applyAll(constantDeclarationNode.metadata().annotations());
        constantNode.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(constantDeclarationNode.metadata().documentationString());

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
        BLangIdentifier identifierNode =
                this.createIdentifier(typeDefNode.typeName());
        typeDef.setName(identifierNode);
        typeDef.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(typeDefNode.metadata().documentationString());

        typeDef.typeNode = createTypeNode(typeDefNode.typeDescriptor());

        typeDefNode.visibilityQualifier().ifPresent(visibilityQual -> {
            if (visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                typeDef.flagSet.add(Flag.PUBLIC);
            }
        });
        typeDef.pos = getPosition(typeDefNode);
        typeDef.annAttachments = applyAll(typeDefNode.metadata().annotations());
        return typeDef;
    }

    @Override
    public BLangNode transform(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        List<TypeDescriptorNode> nodes = flattenUnionType(unionTypeDescriptorNode);

        List<TypeDescriptorNode> finiteTypeElements = new ArrayList<>();
        List<List<TypeDescriptorNode>> unionTypeElementsCollection = new ArrayList<>();
        for (TypeDescriptorNode type : nodes) {
            if (type.kind() == SyntaxKind.SINGLETON_TYPE_DESC) {
                finiteTypeElements.add(type);
                unionTypeElementsCollection.add(new ArrayList<>());
            } else {
                List<TypeDescriptorNode> lastOfOthers;
                if (unionTypeElementsCollection.isEmpty()) {
                    lastOfOthers = new ArrayList<>();
                    unionTypeElementsCollection.add(lastOfOthers);
                } else {
                    lastOfOthers = unionTypeElementsCollection.get(unionTypeElementsCollection.size() - 1);
                }

                lastOfOthers.add(type);
            }
        }

        List<TypeDescriptorNode> unionElements = new ArrayList<>();
        reverseFlatMap(unionTypeElementsCollection, unionElements);

        BLangFiniteTypeNode bLangFiniteTypeNode = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
        for (TypeDescriptorNode finiteTypeEl : finiteTypeElements) {
            SingletonTypeDescriptorNode singletonTypeNode = (SingletonTypeDescriptorNode) finiteTypeEl;
            BLangLiteral literal = createSimpleLiteral(singletonTypeNode.simpleContExprNode());
            bLangFiniteTypeNode.addValue(literal);
        }

        if (unionElements.isEmpty()) {
            return bLangFiniteTypeNode;
        }

        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        for (TypeDescriptorNode unionElement : unionElements) {
            unionTypeNode.memberTypeNodes.add(createTypeNode(unionElement));
        }

        if (!finiteTypeElements.isEmpty()) {
            unionTypeNode.memberTypeNodes.add(deSugarTypeAsUserDefType(bLangFiniteTypeNode));
        }
        return unionTypeNode;
    }

    private List<TypeDescriptorNode> flattenUnionType(UnionTypeDescriptorNode unionTypeDescriptorNode) {
        List<TypeDescriptorNode> list = new ArrayList<>();
        list.add(unionTypeDescriptorNode.leftTypeDesc());
        while (unionTypeDescriptorNode.rightTypeDesc().kind() == SyntaxKind.UNION_TYPE_DESC) {
            unionTypeDescriptorNode = (UnionTypeDescriptorNode) unionTypeDescriptorNode.rightTypeDesc();
            list.add(unionTypeDescriptorNode.leftTypeDesc());
        }
        list.add(unionTypeDescriptorNode.rightTypeDesc());
        return list;
    }

    private <T> void reverseFlatMap(List<List<T>> listOfLists, List<T> result) {
        for (int i = listOfLists.size() - 1; i >= 0; i--) {
            result.addAll(listOfLists.get(i));
        }
    }

    private BLangUserDefinedType deSugarTypeAsUserDefType(BLangType toIndirect) {
        DiagnosticPos pos = toIndirect.pos;
        BLangTypeDefinition bLTypeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();

        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(diagnosticSource.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName);
        bLTypeDef.setName(anonTypeGenName);
        bLTypeDef.flagSet.add(Flag.PUBLIC);
        bLTypeDef.flagSet.add(Flag.ANONYMOUS);

        bLTypeDef.typeNode = toIndirect;
        bLTypeDef.pos = pos;
        addToTop(bLTypeDef);

        return createUserDefinedType(pos, (BLangIdentifier) TreeBuilder.createIdentifierNode(), bLTypeDef.name);
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
        // commenting out as error reason type is no longer supported.
//        if (errorTypeDescriptorNode.errorTypeParamsNode().isPresent()) {
//            errorType.reasonType = createTypeNode(errorTypeDescriptorNode.errorTypeParamsNode().get());
//        }
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
                bLangFunction.flagSet.add(Flag.ATTACHED);
                if (Names.USER_DEFINED_INIT_SUFFIX.value.equals(bLangFunction.name.value)) {
                    bLangFunction.objInitFunction = true;
                    // TODO: verify removing NULL check for objectTypeNode.initFunction has no side-effects
                    objectTypeNode.initFunction = bLangFunction;
                } else {
                    objectTypeNode.addFunction(bLangFunction);
                }
            } else if (bLangNode.getKind() == NodeKind.VARIABLE) {
                objectTypeNode.addField((BLangSimpleVariable) bLangNode);
            } else if (bLangNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                objectTypeNode.addTypeReference((BLangType) bLangNode);
            }
        }

        objectTypeNode.pos = getPosition(objTypeDescNode);

        boolean isAnonymous = checkIfAnonymous(objTypeDescNode);
        objectTypeNode.isAnonymous = isAnonymous;

        if (!isAnonymous) {
            return objectTypeNode;
        }

        return deSugarTypeAsUserDefType(objectTypeNode);
    }

    @Override
    public BLangNode transform(ObjectFieldNode objFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(objFieldNode.fieldName(), objFieldNode.typeName(),
                                                        objFieldNode.expression(),
                                                        false, false, objFieldNode.visibilityQualifier().orElse(null),
                                                        objFieldNode.metadata().annotations());
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
            if (serviceNameNode.isMissing()) {
                serviceName = missingNodesHelper.getNextMissingNodeName(diagnosticSource.pkgID);
            } else {
                serviceName = serviceNameNode.text();
            }
            identifierPos = getPosition(serviceNameNode);
        }

        String serviceTypeName =
                this.anonymousModelHelper.getNextAnonymousServiceTypeKey(diagnosticSource.pkgID, serviceName);
        BLangIdentifier serviceVar = createIdentifier(identifierPos, serviceName);
        serviceVar.pos = identifierPos;
        bLService.setName(serviceVar);
        if (!isAnonServiceValue) {
            for (Node expr : serviceDeclrNode.expressions()) {
                bLService.attachedExprs.add(createExpression(expr));
            }
        }
        // We add all service nodes to top level, only for future reference.
        addToTop(bLService);

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
        bLService.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(serviceDeclrNode.metadata().documentationString());
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
            addToTop(bLTypeDef);
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
                bLangFunction.flagSet.add(Flag.ATTACHED);
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
        boolean isAnonymous = checkIfAnonymous(recordTypeDescriptorNode);

        for (Node field : recordTypeDescriptorNode.fields()) {
            if (field.kind() == SyntaxKind.RECORD_FIELD || field.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE) {
                recordTypeNode.fields.add((BLangSimpleVariable) field.apply(this));
            } else if (field.kind() == SyntaxKind.RECORD_REST_TYPE) {
                recordTypeNode.restFieldType = (BLangType) field.apply(this);
                hasRestField = true;
            } else if (field.kind() == SyntaxKind.TYPE_REFERENCE) {
                recordTypeNode.addTypeReference((BLangType) field.apply(this));
            }
        }
        boolean isOpen = recordTypeDescriptorNode.bodyStartDelimiter().kind() == SyntaxKind.OPEN_BRACE_TOKEN;
        recordTypeNode.sealed = !(hasRestField || isOpen);
        recordTypeNode.pos = getPosition(recordTypeDescriptorNode);
        recordTypeNode.isAnonymous = isAnonymous;
        recordTypeNode.isLocal = this.isInLocalContext;

        // If anonymous type, create a user defined type and return it.
        if (!isAnonymous || this.isInLocalContext) {
            return recordTypeNode;
        }

        return createAnonymousRecordType(recordTypeDescriptorNode, recordTypeNode);
    }

    @Override
    public BLangNode transform(SingletonTypeDescriptorNode singletonTypeDescriptorNode) {
        BLangFiniteTypeNode bLangFiniteTypeNode = new BLangFiniteTypeNode();
        BLangLiteral simpleLiteral = createSimpleLiteral(singletonTypeDescriptorNode.simpleContExprNode());
        bLangFiniteTypeNode.valueSpace.add(simpleLiteral);
        return bLangFiniteTypeNode;
    }

    @Override
    public BLangNode transform(BuiltinSimpleNameReferenceNode singletonTypeDescriptorNode) {
        return createTypeNode(singletonTypeDescriptorNode);
    }

    @Override
    public BLangNode transform(TypeReferenceNode typeReferenceNode) {
        return createTypeNode(typeReferenceNode.typeName());
    }

    @Override
    public BLangNode transform(RecordFieldNode recordFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(recordFieldNode.fieldName(), recordFieldNode.typeName(),
                                                        recordFieldNode.metadata().annotations());
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
        BLangSimpleVariable simpleVar = createSimpleVar(recordFieldNode.fieldName(), recordFieldNode.typeName(),
                                                        recordFieldNode.metadata().annotations());
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
        BLangFunction bLFunction = createFunctionNode(funcDefNode.functionName(), funcDefNode.visibilityQualifier(),
                funcDefNode.functionSignature(), funcDefNode.functionBody());

        bLFunction.annAttachments = applyAll(funcDefNode.metadata().annotations());
        bLFunction.pos = getPosition(funcDefNode);
        bLFunction.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(funcDefNode.metadata().documentationString());
        return bLFunction;
    }

    private BLangFunction createFunctionNode(IdentifierToken funcName, Optional<Token> visibilityQualifier,
            FunctionSignatureNode functionSignature, FunctionBodyNode functionBody) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();

        // Set function name
        bLFunction.name = createIdentifier(getPosition(funcName), funcName);

        // Set the visibility qualifier
        visibilityQualifier.ifPresent(visibilityQual -> {
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
        populateFuncSignature(bLFunction, functionSignature);

        // Set the function body
        if (functionBody == null) {
            bLFunction.body = null;
            bLFunction.flagSet.add(Flag.INTERFACE);
            bLFunction.interfaceFunction = true;
        } else {
            bLFunction.body = (BLangFunctionBody) functionBody.apply(this);
            if (bLFunction.body.getKind() == NodeKind.EXTERN_FUNCTION_BODY) {
                bLFunction.flagSet.add(Flag.NATIVE);
            }
        }
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
        addToTop(bLFunction);

        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = bLFunction;
        lambdaExpr.pos = pos;
        return lambdaExpr;
    }

    @Override
    public BLangNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
        BLangBlockFunctionBody bLFuncBody = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        this.isInLocalContext = true;
        List<BLangStatement> statements = generateBLangStatements(functionBodyBlockNode.statements());

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
        this.isInLocalContext = false;
        return bLFuncBody;
    }

    @Override
    public BLangNode transform(ForEachStatementNode forEachStatementNode) {
        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = getPosition(forEachStatementNode);

        TypedBindingPatternNode typedBindingPatternNode = forEachStatementNode.typedBindingPattern();
        boolean isDeclaredWithVar = typedBindingPatternNode.typeDescriptor().kind() == SyntaxKind.VAR_TYPE_DESC;
        VariableDefinitionNode variableDefinitionNode = createVarDefNode(typedBindingPatternNode, isDeclaredWithVar);
        foreach.setVariableDefinitionNode(variableDefinitionNode);
        foreach.isDeclaredWithVar = isDeclaredWithVar;

        BLangBlockStmt foreachBlock = (BLangBlockStmt) forEachStatementNode.blockStatement().apply(this);
        foreachBlock.pos = getPosition(forEachStatementNode.blockStatement());
        foreach.setBody(foreachBlock);
        foreach.setCollection(createExpression(forEachStatementNode.actionOrExpressionNode()));
        return foreach;
    }

    @Override
    public BLangNode transform(ForkStatementNode forkStatementNode) {
        BLangForkJoin forkJoin = (BLangForkJoin) TreeBuilder.createForkJoinNode();
        DiagnosticPos forkStmtPos = getPosition(forkStatementNode);
        forkJoin.pos = forkStmtPos;
        String nextAnonymousForkKey = anonymousModelHelper.getNextAnonymousForkKey(forkStmtPos.src.pkgID);
        for (BLangSimpleVariableDef worker : forkJoin.workers) {
            BLangFunction function = ((BLangLambdaFunction) worker.var.expr).function;
            function.flagSet.add(Flag.FORKED);
            function.anonForkName = nextAnonymousForkKey;
        }
        return forkJoin;
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

        addToTop(bLFunction);

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

    @Override
    public BLangNode transform(AnnotationDeclarationNode annotationDeclarationNode) {
        BLangAnnotation annotationDecl = (BLangAnnotation) TreeBuilder.createAnnotationNode();
        DiagnosticPos pos = getPosition(annotationDeclarationNode);
        annotationDecl.pos = pos;
        annotationDecl.name = createIdentifier(annotationDeclarationNode.annotationTag());

        if (annotationDeclarationNode.visibilityQualifier() != null) {
            annotationDecl.addFlag(Flag.PUBLIC);
        }

        if (annotationDeclarationNode.constKeyword() != null) {
            annotationDecl.addFlag(Flag.CONSTANT);
        }

        annotationDecl.annAttachments = applyAll(annotationDeclarationNode.metadata().annotations());

        annotationDecl.markdownDocumentationAttachment =
                createMarkdownDocumentationAttachment(annotationDeclarationNode.metadata().documentationString());

        Node typedesc = annotationDeclarationNode.typeDescriptor();
        if (typedesc != null) {
            annotationDecl.typeNode = createTypeNode(typedesc);
        }

        SeparatedNodeList<Node> paramList = annotationDeclarationNode.attachPoints();

        for (Node child : paramList) {
            AnnotationAttachPointNode attachPoint = (AnnotationAttachPointNode) child;
            boolean source = attachPoint.sourceKeyword() != null;
            AttachPoint bLAttachPoint;
            Token firstIndent =  attachPoint.firstIdent();

            switch (firstIndent.kind()) {
                case OBJECT_KEYWORD:
                    Token secondIndent = attachPoint.secondIdent();
                    switch (secondIndent.kind()) {
                        case FUNCTION_KEYWORD:
                            bLAttachPoint =
                                    AttachPoint.getAttachmentPoint(AttachPoint.Point.OBJECT_METHOD.getValue(), source);
                            break;
                        case FIELD_KEYWORD:
                            bLAttachPoint =
                                    AttachPoint.getAttachmentPoint(AttachPoint.Point.OBJECT_FIELD.getValue(), source);
                            break;
                        default:
                            bLAttachPoint =
                                    AttachPoint.getAttachmentPoint(AttachPoint.Point.OBJECT.getValue(), source);
                    }
                    break;
                case RESOURCE_KEYWORD:
                    bLAttachPoint = AttachPoint.getAttachmentPoint(AttachPoint.Point.RESOURCE.getValue(), source);
                    break;
                case RECORD_KEYWORD:
                    bLAttachPoint = AttachPoint.getAttachmentPoint(AttachPoint.Point.RECORD_FIELD.getValue(), source);
                    break;
                default:
                    bLAttachPoint = AttachPoint.getAttachmentPoint(firstIndent.text(), source);
            }
            annotationDecl.addAttachPoint(bLAttachPoint);
        }

        return annotationDecl;
    }

    // -----------------------------------------------Expressions-------------------------------------------------------
    @Override
    public BLangNode transform(ConditionalExpressionNode conditionalExpressionNode) {
        BLangTernaryExpr ternaryExpr = (BLangTernaryExpr) TreeBuilder.createTernaryExpressionNode();
        ternaryExpr.pos = getPosition(conditionalExpressionNode);
        ternaryExpr.elseExpr = createExpression(conditionalExpressionNode.endExpression());
        ternaryExpr.thenExpr = createExpression(conditionalExpressionNode.middleExpression());
        ternaryExpr.expr = createExpression(conditionalExpressionNode.lhsExpression());
        if (ternaryExpr.expr.getKind() == NodeKind.TERNARY_EXPR) {
            // Re-organizing ternary expression tree if there nested ternary expressions.
            BLangTernaryExpr root = (BLangTernaryExpr) ternaryExpr.expr;
            BLangTernaryExpr parent = root;
            while (parent.elseExpr.getKind() == NodeKind.TERNARY_EXPR) {
                parent = (BLangTernaryExpr) parent.elseExpr;
            }
            ternaryExpr.expr = parent.elseExpr;
            parent.elseExpr = ternaryExpr;
            ternaryExpr = root;
        }
        return ternaryExpr;
    }

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
                bLRecordKeyValueField.key =
                        new BLangRecordLiteral.BLangRecordKey(createExpression(specificField.fieldName()));
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
        SyntaxKind expressionKind = unaryExprNode.expression().kind();
        SyntaxKind operatorKind = unaryExprNode.unaryOperator().kind();
        if (expressionKind == SyntaxKind.DECIMAL_INTEGER_LITERAL ||
                expressionKind == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL) {
            BLangNumericLiteral numericLiteral = (BLangNumericLiteral) createSimpleLiteral(unaryExprNode.expression());
            if (operatorKind == SyntaxKind.MINUS_TOKEN) {
                if (numericLiteral.value instanceof String) {
                    numericLiteral.value = "-" + numericLiteral.value;
                } else {
                    numericLiteral.value = ((Long) numericLiteral.value) * -1;
                }

                if (expressionKind != SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL) {
                    numericLiteral.originalValue = "-" + numericLiteral.originalValue;
                }
            } else if (operatorKind == SyntaxKind.PLUS_TOKEN) {
                if (expressionKind != SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL) {
                    numericLiteral.originalValue = "+" + numericLiteral.originalValue;
                }
            }
            return numericLiteral;
        }
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
        BLangFieldBasedAccess bLFieldBasedAccess;
        Node fieldName = fieldAccessExprNode.fieldName();
        if (fieldName.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qualifiedFieldName = (QualifiedNameReferenceNode) fieldName;
            BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess accessWithPrefixNode =
                    (BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess)
                            TreeBuilder.createFieldBasedAccessWithPrefixNode();
            accessWithPrefixNode.nsPrefix = createIdentifier(qualifiedFieldName.modulePrefix());
            accessWithPrefixNode.field = createIdentifier(qualifiedFieldName.identifier());
            bLFieldBasedAccess = accessWithPrefixNode;
        } else {
            bLFieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
            bLFieldBasedAccess.field =
                    createIdentifier(((SimpleNameReferenceNode) fieldName).name());
        }
        bLFieldBasedAccess.pos = getPosition(fieldAccessExprNode);
        bLFieldBasedAccess.field.pos = getPosition(fieldAccessExprNode);
        bLFieldBasedAccess.expr = createExpression(fieldAccessExprNode.expression());
        bLFieldBasedAccess.fieldKind = FieldKind.SINGLE;
        bLFieldBasedAccess.optionalFieldAccess = false;
        return bLFieldBasedAccess;
    }

    @Override
    public BLangNode transform(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        BLangFieldBasedAccess bLFieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        Node fieldName = optionalFieldAccessExpressionNode.fieldName();
        bLFieldBasedAccess.pos = getPosition(optionalFieldAccessExpressionNode);
        bLFieldBasedAccess.field =
                createIdentifier(((SimpleNameReferenceNode) fieldName).name());
        bLFieldBasedAccess.field.pos = getPosition(optionalFieldAccessExpressionNode);
        bLFieldBasedAccess.expr = createExpression(optionalFieldAccessExpressionNode.expression());
        bLFieldBasedAccess.fieldKind = FieldKind.SINGLE;
        bLFieldBasedAccess.optionalFieldAccess = true;
        return bLFieldBasedAccess;
    }

    @Override
    public BLangNode transform(BracedExpressionNode brcExprOut) {
        return createExpression(brcExprOut.expression());
    }

    @Override
    public BLangNode transform(FunctionCallExpressionNode functionCallNode) {
        return createBLangInvocation(functionCallNode.functionName(), functionCallNode.arguments(),
                                     getPosition(functionCallNode), isFunctionCallAsync(functionCallNode));
    }

    public BLangNode transform(MethodCallExpressionNode methodCallExprNode) {
        BLangInvocation bLInvocation = createBLangInvocation(methodCallExprNode.methodName(),
                                                             methodCallExprNode.arguments(),
                                                             getPosition(methodCallExprNode), false);
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

    private boolean isFunctionCallAsync(FunctionCallExpressionNode functionCallExpressionNode) {
        return functionCallExpressionNode.parent().kind() == SyntaxKind.START_ACTION;
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
        typeConversionNode.annAttachments = applyAll(typeCastExpressionNode.typeCastParam().annotations());
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
        trapExpr.pos = getPosition(trapExpressionNode);
        return trapExpr;
    }

    @Override
    public BLangNode transform(ReceiveActionNode receiveActionNode) {
        BLangWorkerReceive workerReceiveExpr = (BLangWorkerReceive) TreeBuilder.createWorkerReceiveNode();
        workerReceiveExpr.setWorkerName(createIdentifier(receiveActionNode.receiveWorkers().name()));
        workerReceiveExpr.pos = getPosition(receiveActionNode);
        return workerReceiveExpr;
    }

    @Override
    public BLangNode transform(SyncSendActionNode syncSendActionNode) {
        BLangWorkerSyncSendExpr workerSendExpr = TreeBuilder.createWorkerSendSyncExprNode();
        workerSendExpr.setWorkerName(createIdentifier(
                syncSendActionNode.peerWorker().name()));
        workerSendExpr.expr = createExpression(syncSendActionNode.expression());
        workerSendExpr.pos = getPosition(syncSendActionNode);
        return workerSendExpr;
    }

    @Override
    public BLangNode transform(ImplicitAnonymousFunctionExpressionNode implicitAnonymousFunctionExpressionNode) {
        BLangArrowFunction arrowFunction = (BLangArrowFunction) TreeBuilder.createArrowFunctionNode();
        arrowFunction.pos = getPosition(implicitAnonymousFunctionExpressionNode);
        arrowFunction.functionName = TreeBuilder.createIdentifierNode();
        // TODO initialize other attributes
        // arrowFunction.funcType;
        // arrowFunction.function;

        // Set Parameters
        Node param = implicitAnonymousFunctionExpressionNode.params();
        if (param.kind() == SyntaxKind.INFER_PARAM_LIST) {

            ImplicitAnonymousFunctionParameters paramsNode = (ImplicitAnonymousFunctionParameters) param;
            SeparatedNodeList<SimpleNameReferenceNode> paramList = paramsNode.parameters();

            for (SimpleNameReferenceNode child : paramList) {
                BLangUserDefinedType userDefinedType = (BLangUserDefinedType) child.apply(this);
                BLangSimpleVariable parameter = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
                parameter.name = userDefinedType.typeName;
                arrowFunction.params.add(parameter);
            }

        } else {
            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) param.apply(this);
            BLangSimpleVariable parameter = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
            parameter.name = userDefinedType.typeName;
            arrowFunction.params.add(parameter);
        }
        arrowFunction.body = new BLangExprFunctionBody();
        arrowFunction.body.expr = createExpression(implicitAnonymousFunctionExpressionNode.expression());
        return arrowFunction;
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
    public BLangNode transform(ListenerDeclarationNode listenerDeclarationNode) {
        Token visibilityQualifier = null;
        if (listenerDeclarationNode.visibilityQualifier().isPresent()) {
            visibilityQualifier = listenerDeclarationNode.visibilityQualifier().get();
        }

        BLangSimpleVariable var = new SimpleVarBuilder()
                .with(listenerDeclarationNode.variableName().text())
                .setTypeByNode(listenerDeclarationNode.typeDescriptor())
                .setExpressionByNode(listenerDeclarationNode.initializer())
                .setVisibility(visibilityQualifier)
                .isListenerVar()
                .build();
        var.annAttachments = applyAll(listenerDeclarationNode.metadata().annotations());
        return var;
    }

    @Override
    public BLangNode transform(BreakStatementNode breakStmtNode) {
        BLangBreak bLBreak = (BLangBreak) TreeBuilder.createBreakNode();
        bLBreak.pos = getPosition(breakStmtNode);
        return bLBreak;
    }

    @Override
    public BLangNode transform(AssignmentStatementNode assignmentStmtNode) {
        SyntaxKind lhsKind = assignmentStmtNode.children().get(0).kind();
        switch (lhsKind) {
            case LIST_BINDING_PATTERN:
                return transformDestructingStatement(assignmentStmtNode);
//            case MAPPING_BINDING_PATTERN: // ignored for now
//                lhs = (MappingBindingPatternNode) lhs;
//                return transformDestructingStatement(assignmentStmtNode);
            default:
                break;
        }
        BLangAssignment bLAssignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        BLangExpression lhsExpr = createExpression(assignmentStmtNode.varRef());
        validateLvexpr(lhsExpr, DiagnosticCode.INVALID_INVOCATION_LVALUE_ASSIGNMENT);
        bLAssignment.setExpression(createExpression(assignmentStmtNode.expression()));
        bLAssignment.pos = getPosition(assignmentStmtNode);
        bLAssignment.varRef = lhsExpr;
        return bLAssignment;
    }

    public BLangNode transformDestructingStatement(
            AssignmentStatementNode assignmentStmtNode) {
        BLangTupleDestructure bLAssignmentDesc =
                (BLangTupleDestructure) TreeBuilder.createTupleDestructureStatementNode();
        bLAssignmentDesc.varRef = (BLangTupleVarRef)
                TreeBuilder.createTupleVariableReferenceNode();
        addListBindingPattern(bLAssignmentDesc.varRef,
                (ListBindingPatternNode) assignmentStmtNode.children().get(0));
        bLAssignmentDesc.setExpression(createExpression(assignmentStmtNode.expression()));
        return bLAssignmentDesc;
    }


    public void addListBindingPattern(BLangTupleVarRef bLangTupleVarRef,
                                      ListBindingPatternNode listBindingPatternNode) {
        Optional restBindingPatternNode =
                listBindingPatternNode.restBindingPattern();
        if (restBindingPatternNode.isPresent()) {
            bLangTupleVarRef.restParam =
                    createExpression(
                            ((RestBindingPatternNode) restBindingPatternNode.get()).
                                    children().get(1)
                    );
        }
        for (Node expr : listBindingPatternNode.bindingPatterns()) {
            switch (expr.kind()) {
                case LIST_BINDING_PATTERN:
                    BLangTupleVarRef res = (BLangTupleVarRef)
                            TreeBuilder.createTupleVariableReferenceNode();
                    addListBindingPattern(res, (ListBindingPatternNode) expr);
                    bLangTupleVarRef.expressions.add(res);
                    break;
                // mapping binding pattern
                case CAPTURE_BINDING_PATTERN:
                    bLangTupleVarRef.expressions.add(
                            createExpression(
                                    ((CaptureBindingPatternNode) expr).children().get(0))
                    );
                    break;
                case WILDCARD_BINDING_PATTERN:
                    bLangTupleVarRef.expressions.add(
                            createExpression(
                                    ((WildcardBindingPatternNode) expr).children().get(0))
                    );
                    break;
                default:
                    throw new RuntimeException("" +
                            "Syntax kind is not supported in listbindingpattern: " + expr.kind());
            }

        }
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
        this.isInLocalContext = true;
        bLBlockStmt.stmts = generateBLangStatements(blockStatement.statements());
        this.isInLocalContext = false;
        return bLBlockStmt;
    }

    @Override
    public BLangNode transform(LockStatementNode lockStatementNode) {
        BLangLock lockNode = (BLangLock) TreeBuilder.createLockNode();
        lockNode.pos = getPosition(lockStatementNode);
        BLangBlockStmt lockBlock = (BLangBlockStmt) lockStatementNode.blockStatement().apply(this);
        lockBlock.pos = getPosition(lockStatementNode.blockStatement());
        lockNode.setBody(lockBlock);
        return lockNode;
    }

    @Override
    public BLangNode transform(VariableDeclarationNode varDeclaration) {
        BLangSimpleVariableDef bLVarDef = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        bLVarDef.pos = getPosition(varDeclaration);

        TypedBindingPatternNode typedBindingPattern = varDeclaration.typedBindingPattern();
        DiagnosticPos pos = null;
        String text = null;

        switch (typedBindingPattern.bindingPattern().kind()) {
            case CAPTURE_BINDING_PATTERN:
                CaptureBindingPatternNode captureBindingPattern =
                        (CaptureBindingPatternNode) typedBindingPattern.bindingPattern();
                pos = getPosition(captureBindingPattern.variableName());
                text = captureBindingPattern.variableName().text();
                break;
            case WILDCARD_BINDING_PATTERN:
                WildcardBindingPatternNode wildcardBindingPatternNode =
                        (WildcardBindingPatternNode) typedBindingPattern.bindingPattern();
                pos = getPosition(wildcardBindingPatternNode.underscoreToken());
                text = wildcardBindingPatternNode.underscoreToken().text();
                break;
            default:
                throw new RuntimeException("Syntax kind is not a valid binding pattern " +
                        typedBindingPattern.bindingPattern().kind());
        }

        BLangSimpleVariable simpleVar = new SimpleVarBuilder()
                .with(text, pos)
                .setPos(bLVarDef.pos)
                .setTypeByNode(typedBindingPattern.typeDescriptor())
                .setExpressionByNode(varDeclaration.initializer().orElse(null))
                .setFinal(varDeclaration.finalKeyword().isPresent())
                .build();

        bLVarDef.setVariable(simpleVar);
        return bLVarDef;
    }

    @Override
    public BLangNode transform(ExpressionStatementNode expressionStatement) {
        SyntaxKind kind = expressionStatement.expression().kind();
        switch (kind) {
            case ASYNC_SEND_ACTION:
                return expressionStatement.expression().apply(this);
            default:
                BLangExpressionStmt bLExpressionStmt =
                        (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
                bLExpressionStmt.expr = (BLangExpression) expressionStatement.expression().apply(this);
                bLExpressionStmt.pos = getPosition(expressionStatement);
                return bLExpressionStmt;
        }
    }

    @Override
    public BLangNode transform(AsyncSendActionNode asyncSendActionNode) {
        BLangWorkerSend workerSendNode = (BLangWorkerSend) TreeBuilder.createWorkerSendNode();
        workerSendNode.setWorkerName(createIdentifier(getPosition(asyncSendActionNode.peerWorker()),
                asyncSendActionNode.peerWorker().name()));
        workerSendNode.expr = createExpression(asyncSendActionNode.expression());
        workerSendNode.pos = getPosition(asyncSendActionNode);
        return workerSendNode;
    }

    @Override
    public BLangNode transform(WaitActionNode waitActionNode) {
        BLangWaitExpr waitExpr = TreeBuilder.createWaitExpressionNode();
        waitExpr.pos = getPosition(waitActionNode);
        if (waitActionNode.waitFutureExpr().kind() != SyntaxKind.WAIT_FIELDS_LIST) {
            waitExpr.exprList = Collections.singletonList(createExpression(waitActionNode.waitFutureExpr()));
        }
        return waitExpr;
    }

    @Override
    public BLangNode transform(StartActionNode startActionNode) {
        BLangExpression expression = createExpression(startActionNode.expression());
        BLangInvocation.BLangActionInvocation invocation = (BLangInvocation.BLangActionInvocation) expression;
        invocation.async = true;
        invocation.annAttachments = applyAll(startActionNode.annotations());

        return expression;
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
        namedArg.name = this.createIdentifier(namedArgumentNode.argumentName().name());
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
                                                        requiredParameter.typeName(), requiredParameter.annotations());

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
                                                        defaultableParameter.typeName(),
                                                        defaultableParameter.annotations());

        Optional<Token> visibilityQual = defaultableParameter.visibilityQualifier();
        // TODO: Check and Fix flags OPTIONAL, REQUIRED
        if (visibilityQual.isPresent() && visibilityQual.get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
            simpleVar.flagSet.add(Flag.PUBLIC);
        }

        simpleVar.setInitialExpression(createExpression(defaultableParameter.expression()));

        simpleVar.pos = getPosition(defaultableParameter);
        return simpleVar;
    }

    @Override
    public BLangNode transform(RestParameterNode restParameter) {
        BLangSimpleVariable bLSimpleVar = createSimpleVar(restParameter.paramName(), restParameter.typeName(),
                                                          restParameter.annotations());

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
        unionTypeNode.nullable = true;

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
        BLangBuiltInRefTypeNode typeNode =
                (BLangBuiltInRefTypeNode) createBuiltInTypeNode(parameterizedTypeDescNode.parameterizedType());
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

        for (Token field : keySpecifierNode.fieldNames()) {
            tableKeySpecifierNode.addFieldNameIdentifier(createIdentifier(field));
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
                tableTypeNode.tableKeyTypeConstraint = (BLangTableKeyTypeConstraint) constraintNode.apply(this);
            } else if (constraintNode.kind() == SyntaxKind.KEY_SPECIFIER) {
                tableTypeNode.tableKeySpecifier = (BLangTableKeySpecifier) constraintNode.apply(this);
            }
        }
        return tableTypeNode;
    }

    @Override
    public BLangNode transform(SimpleNameReferenceNode simpleNameRefNode) {
        BLangUserDefinedType bLUserDefinedType = new BLangUserDefinedType();

        bLUserDefinedType.pos = getPosition(simpleNameRefNode);
        bLUserDefinedType.typeName =
                createIdentifier(simpleNameRefNode.name());
        bLUserDefinedType.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        return bLUserDefinedType;
    }

    @Override
    public BLangNode transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = getPosition(qualifiedNameReferenceNode);
        varRef.variableName = createIdentifier(qualifiedNameReferenceNode.identifier());
        varRef.pkgAlias = createIdentifier(qualifiedNameReferenceNode.modulePrefix());
        return varRef;
    }

    @Override
    public BLangNode transform(XMLProcessingInstruction xmlProcessingInstruction) {
        BLangXMLProcInsLiteral xmlProcInsLiteral =
                (BLangXMLProcInsLiteral) TreeBuilder.createXMLProcessingIntsructionLiteralNode();
        for (Node dataNode : xmlProcessingInstruction.data()) {
            xmlProcInsLiteral.dataFragments.add(createExpression(dataNode));
        }
        xmlProcInsLiteral.target = (BLangLiteral) xmlProcessingInstruction.target().apply(this);
        return xmlProcInsLiteral;
    }

    @Override
    public BLangNode transform(XMLComment xmlComment) {
        BLangXMLCommentLiteral xmlCommentLiteral = (BLangXMLCommentLiteral) TreeBuilder.createXMLCommentLiteralNode();
        for (Node commentNode : xmlComment.content()) {
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

        for (XMLAttributeNode attribute : xmlElementNode.startTag().attributes()) {
            xmlElement.attributes.add((BLangXMLAttribute) attribute.apply(this));
        }

        xmlElement.pos = getPosition(xmlElementNode);
        xmlElement.isRoot = true; // TODO : check this
        return xmlElement;
    }

    @Override
    public BLangNode transform(XMLAttributeNode xmlAttributeNode) {
        BLangXMLAttribute xmlAttribute = (BLangXMLAttribute) TreeBuilder.createXMLAttributeNode();
        xmlAttribute.value = (BLangXMLQuotedString) xmlAttributeNode.value().apply(this);
        xmlAttribute.name = createExpression(xmlAttributeNode.attributeName());
        xmlAttribute.pos = getPosition(xmlAttributeNode);
        return xmlAttribute;
    }

    @Override
    public BLangNode transform(XMLAttributeValue xmlAttributeValue) {
        BLangXMLQuotedString quotedString = (BLangXMLQuotedString) TreeBuilder.createXMLQuotedStringNode();
        quotedString.pos = getPosition(xmlAttributeValue);
        if (xmlAttributeValue.startQuote().kind() == SyntaxKind.SINGLE_QUOTE_TOKEN) {
            quotedString.quoteType = QuoteType.SINGLE_QUOTE;
        } else {
            quotedString.quoteType = QuoteType.DOUBLE_QUOTE;
        }

        for (Node value : xmlAttributeValue.value()) {
            quotedString.textFragments.add((BLangExpression) value.apply(this));
        }

        return quotedString;
    }

    @Override
    public BLangNode transform(XMLStartTagNode startTagNode) {
        return startTagNode.name().apply(this);
    }

    @Override
    public BLangNode transform(XMLEndTagNode endTagNode) {
        return endTagNode.name().apply(this);
    }

    @Override
    public BLangNode transform(XMLTextNode xmlTextNode) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.textFragments.add(0, (BLangExpression) xmlTextNode.content().apply(this));
        xmlTextLiteral.pos = getPosition(xmlTextNode);
        return xmlTextLiteral;
    }

    @Override
    public BLangNode transform(XMLNamespaceDeclarationNode xmlnsDeclNode) {
        BLangXMLNS xmlns = (BLangXMLNS) TreeBuilder.createXMLNSNode();
        BLangIdentifier prefixIdentifier = createIdentifier(xmlnsDeclNode.namespacePrefix());

        xmlns.namespaceURI = createSimpleLiteral(xmlnsDeclNode.namespaceuri());
        xmlns.prefix = prefixIdentifier;
        xmlns.pos = getPosition(xmlnsDeclNode);

        BLangXMLNSStatement xmlnsStmt = (BLangXMLNSStatement) TreeBuilder.createXMLNSDeclrStatementNode();
        xmlnsStmt.xmlnsDecl = xmlns;
        xmlnsStmt.pos = getPosition(xmlnsDeclNode);
        return xmlnsStmt;
    }

    @Override
    public BLangNode transform(ModuleXMLNamespaceDeclarationNode xmlnsDeclNode) {
        BLangXMLNS xmlns = (BLangXMLNS) TreeBuilder.createXMLNSNode();
        BLangIdentifier prefixIdentifier = createIdentifier(xmlnsDeclNode.namespacePrefix());

        xmlns.namespaceURI = createSimpleLiteral(xmlnsDeclNode.namespaceuri());
        xmlns.prefix = prefixIdentifier;
        xmlns.pos = getPosition(xmlnsDeclNode);

        return xmlns;
    }

    @Override
    public BLangNode transform(XMLQualifiedNameNode xmlQualifiedNameNode) {
        BLangXMLQName xmlName = (BLangXMLQName) TreeBuilder.createXMLQNameNode();

        xmlName.localname = createIdentifier(getPosition(xmlQualifiedNameNode.name()),
                xmlQualifiedNameNode.name().name());
        xmlName.prefix = createIdentifier(getPosition(xmlQualifiedNameNode.prefix()),
                xmlQualifiedNameNode.prefix().name());
        return xmlName;
    }

    @Override
    public BLangNode transform(XMLSimpleNameNode xmlSimpleNameNode) {
        BLangXMLQName xmlName = (BLangXMLQName) TreeBuilder.createXMLQNameNode();

        xmlName.localname = createIdentifier(xmlSimpleNameNode.name());
        xmlName.prefix = createIdentifier(null, "");
        return xmlName;
    }

    @Override
    public BLangNode transform(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        BLangInvocation.BLangActionInvocation bLangActionInvocation = (BLangInvocation.BLangActionInvocation)
                TreeBuilder.createActionInvocation();

        bLangActionInvocation.expr = createExpression(remoteMethodCallActionNode.expression());
        bLangActionInvocation.argExprs = applyAll(remoteMethodCallActionNode.arguments());

        BLangNameReference nameReference = createBLangNameReference(remoteMethodCallActionNode.methodName().name());
        bLangActionInvocation.name = (BLangIdentifier) nameReference.name;
        bLangActionInvocation.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        return bLangActionInvocation;
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
    private List<BLangStatement> generateBLangStatements(NodeList<StatementNode> statementNodes) {
        List<BLangStatement> statements = new ArrayList<>();
        for (StatementNode statement : statementNodes) {
            // TODO: Remove this check once statements are non null guaranteed
            if (statement != null) {
                if (statement.kind() == SyntaxKind.FORK_STATEMENT) {
                    generateForkStatements(statements, (ForkStatementNode) statement);
                    continue;
                }
                statements.add((BLangStatement) statement.apply(this));
            }
        }
        return statements;
    }

    private VariableDefinitionNode createVarDefNode(TypedBindingPatternNode typedBindingPatternNode,
                                                    boolean isDeclaredWithVar) {
        SyntaxKind kind = typedBindingPatternNode.bindingPattern().kind();
        switch (kind) {
            case CAPTURE_BINDING_PATTERN:
                CaptureBindingPatternNode captureBindingPatternNode =
                        (CaptureBindingPatternNode) typedBindingPatternNode.bindingPattern();
                BLangSimpleVariable variable = createVarFromCaptureBindingPattern(captureBindingPatternNode,
                        typedBindingPatternNode.typeDescriptor(), isDeclaredWithVar);
                return createSimpleVariableDef(variable, getPosition(typedBindingPatternNode));
            case LIST_BINDING_PATTERN:
                ListBindingPatternNode listBindingPatternNode =
                        (ListBindingPatternNode) typedBindingPatternNode.bindingPattern();

                BLangTupleVariable tupleVariable = createVarFromListBindingPattern(listBindingPatternNode,
                        isDeclaredWithVar);
                return createTupleVariableDef(tupleVariable, getPosition(listBindingPatternNode));
            default:
                throw new RuntimeException("Syntax kind is not supported: " +
                        typedBindingPatternNode.bindingPattern().kind());
        }
    }

    private BLangSimpleVariable createVarFromCaptureBindingPattern(CaptureBindingPatternNode captureBindingPatternNode,
                                                                   TypeDescriptorNode typeDesc,
                                                                   boolean isDeclaredWithVar) {
        BLangSimpleVariable simpleVariable = new SimpleVarBuilder()
                .with(captureBindingPatternNode.variableName().text())
                .build();
        simpleVariable.isDeclaredWithVar = isDeclaredWithVar;
        if (typeDesc != null) {
            simpleVariable.typeNode = createTypeNode(typeDesc);
        }
        return simpleVariable;
    }

    private BLangTupleVariable createVarFromListBindingPattern(ListBindingPatternNode listBindingPatternNode,
                                                               boolean isDeclaredWithVar) {
        BLangTupleVariable tupleVariable = (BLangTupleVariable) TreeBuilder.createTupleVariableNode();
        tupleVariable.pos = getPosition(listBindingPatternNode);
        for (BindingPatternNode node : listBindingPatternNode.bindingPatterns()) {
            if (node.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN) {
                CaptureBindingPatternNode captureBindingPatternNode = (CaptureBindingPatternNode) node;
                tupleVariable.memberVariables.add(createVarFromCaptureBindingPattern(captureBindingPatternNode, null,
                        false));
            }
        }
        tupleVariable.isDeclaredWithVar = isDeclaredWithVar;
        return tupleVariable;
    }

    private BLangTupleVariableDef createTupleVariableDef(BLangTupleVariable var, DiagnosticPos pos) {
        BLangTupleVariableDef varDefNode = (BLangTupleVariableDef) TreeBuilder.createTupleVariableDefinitionNode();
        varDefNode.pos = pos;
        varDefNode.setVariable(var);
        return varDefNode;
    }

    private BLangSimpleVariableDef createSimpleVariableDef(BLangSimpleVariable var, DiagnosticPos pos) {
        BLangSimpleVariableDef varDefNode = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        varDefNode.pos = pos;
        varDefNode.setVariable(var);
        return varDefNode;
    }

    private void generateForkStatements(List<BLangStatement> statements, ForkStatementNode forkStatementNode) {
        BLangForkJoin forkJoin = (BLangForkJoin) forkStatementNode.apply(this);

        for (NamedWorkerDeclarationNode workerDeclarationNode : forkStatementNode.namedWorkerDeclarations()) {
            BLangSimpleVariableDef workerDef = (BLangSimpleVariableDef) workerDeclarationNode.apply(this);
            workerDef.isWorker = true;
            workerDef.isInFork = true;
            ((BLangLambdaFunction) workerDef.var.expr).function.addFlag(Flag.FORKED);
            workerDef.var.flagSet.add(Flag.FORKED);
            statements.add(workerDef);
            while (!this.additionalStatements.empty()) {
                statements.add(additionalStatements.pop());
            }
            forkJoin.addWorkers(workerDef);
        }
        statements.add(forkJoin);
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
            bLFunction.returnTypeAnnAttachments = applyAll(returnType.annotations());
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
        } else if (expression.kind() == SyntaxKind.EXPRESSION_LIST_ITEM) {
            return createExpression(((ExpressionListItemNode) expression).expression());
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

    private BLangSimpleVariable createSimpleVar(Optional<Token> name, Node type, NodeList<AnnotationNode> annotations) {
        if (name.isPresent()) {
            Token nameToken = name.get();
            return createSimpleVar(nameToken, type, null, false, false, null, annotations);
        }

        return createSimpleVar(null, type, null, false, false, null, annotations);
    }

    private BLangSimpleVariable createSimpleVar(Token name, Node type, NodeList<AnnotationNode> annotations) {
        return createSimpleVar(name, type, null, false, false, null, annotations);
    }

    private BLangSimpleVariable createSimpleVar(Token name, Node typeName, Node initializer, boolean isFinal,
                                                boolean isListenerVar, Token visibilityQualifier,
                                                NodeList<AnnotationNode> annotations) {
        BLangSimpleVariable bLSimpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        bLSimpleVar.setName(this.createIdentifier(name));

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

        if (annotations != null) {
            bLSimpleVar.annAttachments = applyAll(annotations);
        }

        return bLSimpleVar;
    }

    private BLangIdentifier createIdentifier(Token token) {
        return createIdentifier(getPosition(token), token);
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, Token token) {
        if (token == null) {
            return createIdentifier(pos, null, null);
        }

        String identifierName;
        if (token.isMissing()) {
            identifierName = missingNodesHelper.getNextMissingNodeName(diagnosticSource.pkgID);
        } else {
            identifierName = token.text();
        }

        return createIdentifier(pos, identifierName);
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
        } else if (type.kind() == SyntaxKind.TYPE_DESC) {
            // kind can be type-desc only if its a missing token.
            BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            BLangIdentifier pkgAlias = this.createIdentifier(null, "");
            BLangIdentifier name = this.createIdentifier((Token) type);
            BLangNameReference nameReference = new BLangNameReference(getPosition(type), null, pkgAlias, name);
            bLUserDefinedType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
            bLUserDefinedType.typeName = (BLangIdentifier) nameReference.name;
            bLUserDefinedType.pos = getPosition(type);
            return bLUserDefinedType;
        }

        return (BLangType) type.apply(this);
    }

    private BLangType createBuiltInTypeNode(Node type) {
        String typeText;
        if (type.kind() == SyntaxKind.NIL_TYPE_DESC) {
            typeText = "()";
        } else if (type instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode simpleNameRef = (BuiltinSimpleNameReferenceNode) type;
            if (simpleNameRef.kind() == SyntaxKind.VAR_TYPE_DESC) {
                return null;
            }
            typeText = simpleNameRef.name().text();
        } else {
            typeText = ((Token) type).text(); // TODO: Remove this once map<string> returns Nodes for `map`
        }

        TypeKind typeKind = TreeUtils.stringToTypeKind(typeText.replaceAll("\\s+", ""));

        SyntaxKind kind = type.kind();
        switch (kind) {
            case BOOLEAN_TYPE_DESC:
            case INT_TYPE_DESC:
            case BYTE_TYPE_DESC:
            case FLOAT_TYPE_DESC:
            case DECIMAL_TYPE_DESC:
            case STRING_TYPE_DESC:
            case ANY_TYPE_DESC:
            case NIL_TYPE_DESC:
            case HANDLE_TYPE_DESC:
            case ANYDATA_TYPE_DESC:
                BLangValueType valueType = (BLangValueType) TreeBuilder.createValueTypeNode();
                valueType.typeKind = typeKind;
                valueType.pos = getPosition(type);
                return valueType;
            default:
                BLangBuiltInRefTypeNode builtInValueType =
                        (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
                builtInValueType.typeKind = typeKind;
                builtInValueType.pos = getPosition(type);
                return builtInValueType;
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
                                                  DiagnosticPos position, boolean isAsync) {
        BLangInvocation bLInvocation;
        if (isAsync) {
            bLInvocation = (BLangInvocation) TreeBuilder.createActionInvocation();
        } else {
            bLInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        }
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
            BLangIdentifier pkgAlias = this.createIdentifier(getPosition(modulePrefix), modulePrefix);
            BLangIdentifier name = this.createIdentifier(getPosition(identifier), identifier);
            return new BLangNameReference(getPosition(node), null, pkgAlias, name);
        } else if (node.kind() == SyntaxKind.IDENTIFIER_TOKEN || node.kind() == SyntaxKind.ERROR_KEYWORD) {
            // simple identifier
            Token token = (Token) node;
            BLangIdentifier pkgAlias = this.createIdentifier(null, "");
            BLangIdentifier name = this.createIdentifier(token);
            return new BLangNameReference(getPosition(node), null, pkgAlias, name);
        } else if (node.kind() == SyntaxKind.NEW_KEYWORD) {
            Token iToken = (Token) node;
            BLangIdentifier pkgAlias = this.createIdentifier(getPosition(iToken), "");
            BLangIdentifier name = this.createIdentifier(iToken);
            return new BLangNameReference(getPosition(node), null, pkgAlias, name);
        } else {
            // Map name reference as a name
            SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) node;
            return createBLangNameReference(nameReferenceNode.name());
        }
    }

    private BLangMarkdownDocumentation createMarkdownDocumentationAttachment(Optional<Node> documentationStringNode) {
        if (!documentationStringNode.isPresent()) {
            return null;
        }
        BLangMarkdownDocumentation doc = (BLangMarkdownDocumentation) TreeBuilder.createMarkdownDocumentationNode();

        LinkedList<BLangMarkdownDocumentationLine> documentationLines = new LinkedList<>();
        LinkedList<BLangMarkdownParameterDocumentation> parameters = new LinkedList<>();
        LinkedList<BLangMarkdownReferenceDocumentation> references = new LinkedList<>();

        NodeList<Token> tokens = ((DocumentationStringNode) documentationStringNode.get()).documentationLines();
        List<String> previousParamLines = null;
        StringBuilder threeTickContent = new StringBuilder();
        boolean needToAppendThreeTickContent = false;
        boolean inThreeTicks = false;
        for (Token token : tokens) {
            String text = token.text();
            text = text.substring(1);

            if (text.startsWith("\n#")) { // currently parse sends us empty doc lines prepended to non empty ones
                BLangMarkdownDocumentationLine docLine = (BLangMarkdownDocumentationLine)
                        TreeBuilder.createMarkdownDocumentationTextNode();
                docLine.text = "";

                if (needToAppendThreeTickContent) {
                    documentationLines.getLast().text += threeTickContent.toString();
                    threeTickContent.setLength(0);
                    needToAppendThreeTickContent = false;
                }
                documentationLines.add(docLine);

                text = text.substring(2);
            }

            boolean inThreeTicksPreviousLine = inThreeTicks;
            inThreeTicks = addReferences(text, references, inThreeTicks);

            if (inThreeTicksPreviousLine) {
                threeTickContent.append(token.leadingMinutiae())
                                .append(token.text());
                if (inThreeTicks) {
                    threeTickContent.append(token.trailingMinutiae());
                }
                needToAppendThreeTickContent = true;
                continue;
            }

            if (inThreeTicks) { // inThreeTicksPreviousLine == true && inThreeTicks == false means quote just started
                threeTickContent.setLength(0);
            }

            if (text.startsWith(" +")) { // '+' indicates a param
                text = text.substring(2);
                int nameSeparator = text.indexOf('-'); // '-' is the param separate
                String value = text.substring(0, nameSeparator).trim();

                if ("return".equals(value)) { // spacial param name to mark return
                    BLangMarkdownReturnParameterDocumentation returnParameter =
                            new BLangMarkdownReturnParameterDocumentation();
                    previousParamLines = returnParameter.returnParameterDocumentationLines;
                    doc.returnParameter = returnParameter;
                } else {
                    BLangMarkdownParameterDocumentation parameterDoc = new BLangMarkdownParameterDocumentation();
                    previousParamLines = parameterDoc.parameterDocumentationLines;
                    BLangIdentifier parameterName = new BLangIdentifier();
                    parameterDoc.parameterName = parameterName;
                    parameterName.value = value;
                    parameters.add(parameterDoc);
                }

                previousParamLines.add(trimLeftAtMostOne(text.substring(nameSeparator + 1)));

            } else if (text.startsWith(" # Deprecated")) {
                doc.deprecationDocumentation = new BLangMarkDownDeprecationDocumentation();
            } else if (previousParamLines != null) {
                previousParamLines.add(trimLeftAtMostOne(text));
            } else {
                if (needToAppendThreeTickContent) {
                    documentationLines.getLast().text += threeTickContent.toString();
                    threeTickContent.setLength(0);
                    needToAppendThreeTickContent = false;
                }

                BLangMarkdownDocumentationLine docLine =
                        (BLangMarkdownDocumentationLine) TreeBuilder.createMarkdownDocumentationTextNode();
                docLine.text = trimLeftAtMostOne(text);
                documentationLines.add(docLine);
                threeTickContent.append(token.trailingMinutiae());
            }
        }

        if (needToAppendThreeTickContent) {
            documentationLines.getLast().text += threeTickContent.toString();
        }

        doc.documentationLines = documentationLines;
        doc.parameters = parameters;
        doc.references = references;
        return doc;
    }

    private String trimLeftAtMostOne(String text) {
        int countToStrip = 0;
        if (Character.isWhitespace(text.charAt(0))) {
            countToStrip = 1;
        }
        return text.substring(countToStrip);
    }

    private boolean addReferences(String text, LinkedList<BLangMarkdownReferenceDocumentation> references,
                                  boolean startsInsideQuotes) {
//              _
//             / \ non-tick (in single quote)
//             v /
//            --4<----.                 _____
//      tick /         \non-tick       /     \ non-tick
//          v     tick  \       tick  v tick  \        tick
//     .-> 0-------------->1--------->2--------->3-----------------.
//    /   ^ \                        ^ \                           |
//    |   | |                        | |                           |
//    |   \_/non-tick (un-quoted)    \_/non-tick (in double quote) |
//    \____________________________________________________________/
//
// note this state machine is only design to distinguish single tick and double tick quoted string
// for simplicity are assuming three tick case is just a subset of double tick case

        int length = text.length();
        int state = startsInsideQuotes ? 2 : 0;
        int lastWhiteSpace = 0;
        int secondToLastWhiteSpace = 0;

        //                0  1  2  3  4
        int[][] table = {{0, 4, 2, 2, 4},  // non-tick
                         {1, 2, 3, 0, 0}}; // tick

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            int isTick = c == '`' ? 1 : 0;
            int newState = table[isTick][state];

            if (newState == 0 && c == ' ') {
                secondToLastWhiteSpace = lastWhiteSpace;
                lastWhiteSpace = i;
            }

            if (state == 4 && newState == 0) { // we are coming out of a single quoted string
                BLangMarkdownReferenceDocumentation ref = new BLangMarkdownReferenceDocumentation();
                ref.type = DocumentationReferenceType.BACKTICK_CONTENT;
                if (secondToLastWhiteSpace + 1 < lastWhiteSpace) {
                    ref.type = stringToRefType(text.substring(secondToLastWhiteSpace + 1, lastWhiteSpace));
                }
                references.add(ref);
            }
            state = newState;
        }
        return state == 2 || state == 3;
    }

    private DocumentationReferenceType stringToRefType(String refTypeName) {
        for (DocumentationReferenceType type : DocumentationReferenceType.values()) {
            if (type.getValue().equals(refTypeName)) {
                return type;
            }
        }
        return DocumentationReferenceType.BACKTICK_CONTENT;
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
                recordVariable.variableList.stream().map(BLangRecordVariable.BLangRecordVariableKeyValue::getValue)
                        .forEach(this::markVariableAsFinal);
                if (recordVariable.restParam != null) {
                    markVariableAsFinal((BLangVariable) recordVariable.restParam);
                }
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                markVariableAsFinal(errorVariable.message);
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

    private boolean checkIfAnonymous(Node node) {
        Node parent = node.parent();
        switch (parent.kind()) {
            case UNION_TYPE_DESC:
            case ARRAY_TYPE_DESC:
            case OPTIONAL_TYPE_DESC:
            case TYPE_TEST_EXPRESSION:
            case TUPLE_TYPE_DESC:
            case TYPED_BINDING_PATTERN:
            case RECORD_FIELD:
                return true;
            default:
                return false;
        }
    }

    private boolean ifInLocalContext(Node parent) {
        while (parent != null) {
            if (parent instanceof StatementNode) {
                return true;
            }
            parent = parent.parent();
        }
        return false;
    }

    private BLangType createAnonymousRecordType(RecordTypeDescriptorNode recordTypeDescriptorNode,
            BLangRecordTypeNode recordTypeNode) {
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        DiagnosticPos pos = getPosition(recordTypeDescriptorNode);
        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(this.diagnosticSource.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName, null);
        typeDef.setName(anonTypeGenName);
        typeDef.flagSet.add(Flag.PUBLIC);
        typeDef.flagSet.add(Flag.ANONYMOUS);

        typeDef.typeNode = recordTypeNode;
        typeDef.pos = pos;
        addToTop(typeDef);
        return createUserDefinedType(pos, (BLangIdentifier) TreeBuilder.createIdentifierNode(), typeDef.name);
    }

    private BLangUserDefinedType createUserDefinedType(DiagnosticPos pos,
            BLangIdentifier pkgAlias,
            BLangIdentifier name) {
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.pos = pos;
        userDefinedType.pkgAlias = pkgAlias;
        userDefinedType.typeName = name;
        return userDefinedType;
    }

    private class SimpleVarBuilder {
        private BLangIdentifier name;
        private BLangType type;
        private boolean isDeclaredWithVar;
        private Set<Flag> flags = new HashSet<>();
        private boolean isFinal;
        private ExpressionNode expr;
        private DiagnosticPos pos;

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
            bLSimpleVar.pos = pos;
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
            this.isDeclaredWithVar = typeName == null || typeName.kind() == SyntaxKind.VAR_TYPE_DESC;
            if (typeName == null) {
                return this;
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

        public SimpleVarBuilder setPos(DiagnosticPos pos) {
            this.pos = pos;
            return this;
        }
    }

    private void addToTop(TopLevelNode topLevelNode) {
        if (currentCompilationUnit != null) {
            currentCompilationUnit.addTopLevelNode(topLevelNode);
        }
    }
}

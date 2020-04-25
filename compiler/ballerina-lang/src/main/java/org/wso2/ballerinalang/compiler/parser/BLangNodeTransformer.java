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

import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BlockStatementNode;
import io.ballerinalang.compiler.syntax.tree.BracedExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BreakStatementNode;
import io.ballerinalang.compiler.syntax.tree.ContinueStatementNode;
import io.ballerinalang.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerinalang.compiler.syntax.tree.ElseBlockNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerinalang.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.IfElseStatementNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerinalang.compiler.syntax.tree.ImportPrefixNode;
import io.ballerinalang.compiler.syntax.tree.ImportVersionNode;
import io.ballerinalang.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.MappingFieldNode;
import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.NamedArgumentNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.NodeTransformer;
import io.ballerinalang.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.PanicStatementNode;
import io.ballerinalang.compiler.syntax.tree.ParameterNode;
import io.ballerinalang.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerinalang.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.RequiredParameterNode;
import io.ballerinalang.compiler.syntax.tree.RestArgumentNode;
import io.ballerinalang.compiler.syntax.tree.RestParameterNode;
import io.ballerinalang.compiler.syntax.tree.SpecificFieldNode;
import io.ballerinalang.compiler.syntax.tree.SpreadFieldNode;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.TypeReferenceNode;
import io.ballerinalang.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordSpreadOperatorField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Constants;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates a {@code BLandCompilationUnit} from the given {@code ModulePart}.
 *
 * @since 1.3.0
 */
public class BLangNodeTransformer extends NodeTransformer<BLangNode> {

    private static final String IDENTIFIER_LITERAL_PREFIX = "'";
    public static final String VAR = "var";

    // TODO This is a temporary solution,
    private DiagnosticPos emptyPos;

    private BLangDiagnosticLogHelper dlog;
    private SymbolTable symTable;
    private BDiagnosticSource diagnosticSource;

    private static final Pattern UNICODE_PATTERN = Pattern.compile(Constants.UNICODE_REGEX);
    private BLangAnonymousModelHelper anonymousModelHelper;
    private Stack<BLangNode> otherTopLevelNodes = new Stack<>();

    public BLangNodeTransformer(CompilerContext context, BDiagnosticSource diagnosticSource) {
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.diagnosticSource = diagnosticSource;
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.emptyPos = new DiagnosticPos(diagnosticSource, 1, 1, 1, 1);
    }

    public List<BLangNode> accept(Node node) {
        BLangNode bLangNode = node.apply(this);
        List<BLangNode> nodes = new ArrayList<>();
        while (!otherTopLevelNodes.empty()) {
            nodes.add(otherTopLevelNodes.pop());
        }
        nodes.add(bLangNode);
        return nodes;
    }

    @Override
    public BLangNode transform(IdentifierToken identifierToken) {
        return this.createIdentifier(emptyPos, identifierToken.text());
    }

    @Override
    public BLangNode transform(ModulePartNode modulePart) {
        BLangCompilationUnit compilationUnit = (BLangCompilationUnit) TreeBuilder.createCompilationUnit();
        compilationUnit.name = diagnosticSource.cUnitName;

        // Generate import declarations
        for (ImportDeclarationNode importDecl : modulePart.imports()) {
            BLangImportPackage bLangImport = (BLangImportPackage) importDecl.apply(this);
            bLangImport.compUnit = this.createIdentifier(emptyPos, compilationUnit.getName());
            compilationUnit.addTopLevelNode(bLangImport);
        }

        // Generate other module-level declarations
        for (ModuleMemberDeclarationNode member : modulePart.members()) {
            compilationUnit.addTopLevelNode((TopLevelNode) member.apply(this));
        }

        // Add other top-level nodes
        while (!otherTopLevelNodes.empty()) {
            compilationUnit.addTopLevelNode((TopLevelNode) otherTopLevelNodes.pop());
        }

        for (BLangNode topLevelNode : otherTopLevelNodes) {
            compilationUnit.addTopLevelNode((TopLevelNode) topLevelNode);
        }
        otherTopLevelNodes.clear();

        compilationUnit.pos = emptyPos;
        return compilationUnit;
    }

    @Override
    public BLangNode transform(ModuleVariableDeclarationNode modVarDeclrNode) {
        return createSimpleVar(modVarDeclrNode.variableName().text(),
                               modVarDeclrNode.typeName(), modVarDeclrNode.initializer(),
                               modVarDeclrNode.finalKeyword().isPresent(), false);
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
        names.forEach(name -> pkgNameComps.add(this.createIdentifier(emptyPos, name.text(), null)));

        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = emptyPos;
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.version = this.createIdentifier(emptyPos, version);
        importDcl.orgName = this.createIdentifier(emptyPos, orgName);
        importDcl.alias = (prefix != null && !prefix.isEmpty()) ? this.createIdentifier(emptyPos, prefix, null) :
                pkgNameComps.get(pkgNameComps.size() - 1);

        return importDcl;
    }

    public BLangNode transform(TypeDefinitionNode typeDefNode) {
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        BLangIdentifier identifierNode = this.createIdentifier(emptyPos, typeDefNode.typeName().text());
        typeDef.setName(identifierNode);

        BLangStructureTypeNode structTypeNode = (BLangStructureTypeNode) typeDefNode.typeDescriptor().apply(this);
        structTypeNode.isAnonymous = false;
        structTypeNode.isLocal = false;
        typeDef.typeNode = structTypeNode;

        typeDefNode.visibilityQualifier().ifPresent(visibilityQual -> {
            if (visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                typeDef.flagSet.add(Flag.PUBLIC);
            }
        });
        typeDef.pos = emptyPos;
        return typeDef;
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
        recordTypeNode.isFieldAnalyseRequired = true;
        recordTypeNode.sealed = !hasRestField;
        recordTypeNode.pos = emptyPos;
        return recordTypeNode;
    }

    @Override
    public BLangNode transform(TypeReferenceNode typeReferenceNode) {
        return createTypeNode(typeReferenceNode.typeName());
    }

    @Override
    public BLangNode transform(RecordFieldNode recordFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(recordFieldNode.fieldName().text(), recordFieldNode.typeName());
        simpleVar.flagSet.add(Flag.PUBLIC);
        if (recordFieldNode.questionMarkToken().isPresent()) {
            simpleVar.flagSet.add(Flag.OPTIONAL);
        } else {
            simpleVar.flagSet.add(Flag.REQUIRED);
        }
        return simpleVar;
    }

    @Override
    public BLangNode transform(RecordFieldWithDefaultValueNode recordFieldNode) {
        BLangSimpleVariable simpleVar = createSimpleVar(recordFieldNode.fieldName().text(), recordFieldNode.typeName());
        simpleVar.flagSet.add(Flag.PUBLIC);
        if (isPresent(recordFieldNode.expression())) {
            simpleVar.setInitialExpression(createExpression(recordFieldNode.expression()));
        }
        return simpleVar;
    }

    @Override
    public BLangNode transform(RecordRestDescriptorNode recordFieldNode) {
        return createTypeNode(recordFieldNode.typeName());
    }

    @Override
    public BLangNode transform(FunctionDefinitionNode funcDefNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        bLFunction.pos = emptyPos;

        // Set function name
        IdentifierToken funcName = funcDefNode.functionName();
        bLFunction.name = createIdentifier(emptyPos, funcName.text());

        // Set the visibility qualifier
        funcDefNode.visibilityQualifier().ifPresent(visibilityQual -> {
            if (visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                bLFunction.flagSet.add(Flag.PUBLIC);
            } else if (visibilityQual.kind() == SyntaxKind.PRIVATE_KEYWORD) {
                bLFunction.flagSet.add(Flag.PRIVATE);
            }
        });

        // TODO populate function signature
        getFuncSignature(bLFunction, funcDefNode);

        // Set the function body
        bLFunction.body = (BLangFunctionBody) funcDefNode.functionBody().apply(this);
        return bLFunction;
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
        bLFuncBody.stmts = statements;
        return bLFuncBody;
    }

    // -----------------------------------------------Statements--------------------------------------------------------
    @Override
    public BLangNode transform(PanicStatementNode panicStmtNode) {
        BLangPanic bLPanic = (BLangPanic) TreeBuilder.createPanicNode();
        bLPanic.pos = emptyPos;
        bLPanic.expr = createExpression(panicStmtNode.expression());
        return bLPanic;
    }

    @Override
    public BLangNode transform(ContinueStatementNode continueStmtNode) {
        BLangContinue bLContinue = (BLangContinue) TreeBuilder.createContinueNode();
        bLContinue.pos = emptyPos;
        return bLContinue;
    }

    @Override
    public BLangNode transform(BreakStatementNode breakStmtNode) {
        BLangBreak bLBreak = (BLangBreak) TreeBuilder.createBreakNode();
        bLBreak.pos = emptyPos;
        return bLBreak;
    }

    @Override
    public BLangNode transform(IfElseStatementNode ifElseStmtNode) {
        BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = emptyPos;
        ifNode.setCondition(createExpression(ifElseStmtNode.condition()));
        ifNode.setBody((BLangBlockStmt) ifElseStmtNode.ifBody().apply(this));

        ifElseStmtNode.elseBody().ifPresent(elseBody -> {
            ElseBlockNode elseNode = (ElseBlockNode) elseBody;
            ifNode.setElseStatement((org.ballerinalang.model.tree.statements.StatementNode) elseNode.elseBody().apply(this));
        });

        return ifNode;
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
        bLVarDef.pos = emptyPos;
        bLVarDef.setVariable(createSimpleVar(varDeclaration.variableName().text(), varDeclaration.typeName(),
                                             varDeclaration.initializer().orElse(null),
                                             varDeclaration.finalKeyword().isPresent(), false));
        return bLVarDef;
    }

    @Override
    public BLangNode transform(ExpressionStatementNode expressionStatement) {
        BLangExpressionStmt bLExpressionStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        bLExpressionStmt.expr = (BLangExpression) expressionStatement.expression().apply(this);
        bLExpressionStmt.pos = emptyPos;
        return bLExpressionStmt;
    }

    public BLangNode transform(FunctionCallExpressionNode functionCallNode) {
        BLangInvocation bLInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        Node nameNode = functionCallNode.functionName();
        BLangNameReference reference = getBLangNameReference(nameNode);
        bLInvocation.pkgAlias = (BLangIdentifier) reference.pkgAlias;
        bLInvocation.name = (BLangIdentifier) reference.name;

        List<BLangExpression> args = new ArrayList<>();
        functionCallNode.arguments().iterator().forEachRemaining(arg -> {
            args.add((BLangExpression) arg.apply(this));
        });
        bLInvocation.argExprs = args;
        bLInvocation.pos = emptyPos;

        return bLInvocation;
    }

    // -------------------------------------------------Misc------------------------------------------------------------

    @Override
    public BLangNode transform(PositionalArgumentNode argumentNode) {
        return createExpression(argumentNode.expression());
    }

    @Override
    public BLangNode transform(NamedArgumentNode namedArgumentNode) {
        return namedArgumentNode.expression().apply(this);
    }

    @Override
    public BLangNode transform(RestArgumentNode restArgumentNode) {
        return restArgumentNode.expression().apply(this);
    }

    @Override
    public BLangNode transform(RequiredParameterNode requiredParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(requiredParameter.paramName().text(),
                                                        requiredParameter.typeName());

        Optional<Token> visibilityQual = requiredParameter.visibilityQualifier();
        //TODO: Check and Fix flags OPTIONAL, REQUIRED
        if (visibilityQual.isPresent() && visibilityQual.get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
            simpleVar.flagSet.add(Flag.PUBLIC);
        }

        simpleVar.pos = emptyPos;
        return simpleVar;
    }

    @Override
    public BLangNode transform(DefaultableParameterNode defaultableParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(defaultableParameter.paramName().text(),
                                                        defaultableParameter.typeName());

        Optional<Token> visibilityQual = defaultableParameter.visibilityQualifier();
        //TODO: Check and Fix flags OPTIONAL, REQUIRED
        if (visibilityQual.isPresent() && visibilityQual.get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
            simpleVar.flagSet.add(Flag.PUBLIC);
        }

        simpleVar.setInitialExpression(createExpression(defaultableParameter.expression()));

        simpleVar.pos = emptyPos;
        return simpleVar;
    }

    @Override
    public BLangNode transform(RestParameterNode restParameter) {
        BLangSimpleVariable bLSimpleVar = createSimpleVar(restParameter.paramName().text(), restParameter.typeName());

        BLangArrayType bLArrayType = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        bLArrayType.elemtype = bLSimpleVar.typeNode;
        bLArrayType.dimensions = 1;
        bLSimpleVar.typeNode = bLArrayType;
        bLArrayType.pos = emptyPos;

        bLSimpleVar.pos = emptyPos;
        return bLSimpleVar;
    }

    @Override
    protected BLangNode transformSyntaxNode(Node node) {
        throw new RuntimeException("Node not supported: " + node);
    }

    // ------------------------------------------private methods--------------------------------------------------------
    private void getFuncSignature(BLangFunction bLFunction, FunctionDefinitionNode funcDefNode) {
        // Set Parameters
        for (ParameterNode child : funcDefNode.parameters()) {
            SimpleVariableNode param = (SimpleVariableNode) child.apply(this);
            if (child instanceof RestParameterNode) {
                bLFunction.setRestParameter(param);
            } else {
                bLFunction.addParameter(param);
            }
        }

        // Set Return Type
        Optional<Node> retNode = funcDefNode.returnTypeDesc();
        if (retNode.isPresent()) {
            VariableDeclarationNode returnType = (VariableDeclarationNode) retNode.get();
            bLFunction.setReturnTypeNode((BLangType) createTypeNode(returnType.typeName()));
        } else {
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            bLValueType.pos = emptyPos;
            bLValueType.typeKind = TypeKind.NIL;
            bLFunction.setReturnTypeNode(bLValueType);
        }
    }

    private BLangExpression createExpression(Node expression) {
        if (isSimpleLiteral(expression.kind())) {
            return createSimpleLiteral(expression);
        } else if (expression.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            // Variable Reference
            IdentifierToken identifier = (IdentifierToken) expression;
            BLangSimpleVarRef bLVarRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
            bLVarRef.pos = emptyPos;
            bLVarRef.pkgAlias = this.createIdentifier(emptyPos, "");
            bLVarRef.variableName = this.createIdentifier(emptyPos, identifier.text());
            return bLVarRef;
        } else if (expression.kind() == SyntaxKind.MAPPING_CONSTRUCTOR) {
            BLangRecordLiteral bLiteralNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
            MappingConstructorExpressionNode mapConstruct = (MappingConstructorExpressionNode) expression;
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
            bLiteralNode.pos = emptyPos;
            return bLiteralNode;
        } else if (expression.kind() == SyntaxKind.UNARY_EXPRESSION) {
            BLangUnaryExpr bLUnaryExpr = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
            UnaryExpressionNode unaryExpressionNode = (UnaryExpressionNode) expression;
            bLUnaryExpr.pos = emptyPos;
            bLUnaryExpr.expr = createExpression(unaryExpressionNode.expression());
            bLUnaryExpr.operator = OperatorKind.valueFrom(unaryExpressionNode.unaryOperator().text());
            return bLUnaryExpr;
        } else if (expression.kind() == SyntaxKind.BINARY_EXPRESSION) {
            BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
            BinaryExpressionNode bLBinaryExpr = (BinaryExpressionNode) expression;
            binaryExpr.pos = emptyPos;
            binaryExpr.lhsExpr = createExpression(bLBinaryExpr.lhsExpr());
            binaryExpr.rhsExpr = createExpression(bLBinaryExpr.rhsExpr());
            binaryExpr.opKind = OperatorKind.valueFrom(bLBinaryExpr.operator().text());
            return binaryExpr;
        } else if (expression.kind() == SyntaxKind.FIELD_ACCESS) {
            BLangFieldBasedAccess bLFieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
            FieldAccessExpressionNode fieldAccessExpressionNode = (FieldAccessExpressionNode) expression;
            Token fieldName = fieldAccessExpressionNode.fieldName();
            bLFieldBasedAccess.pos = emptyPos;
            BLangNameReference nameRef = getBLangNameReference(fieldName);
            bLFieldBasedAccess.field = createIdentifier(emptyPos, nameRef.name.getValue());
            bLFieldBasedAccess.field.pos = emptyPos;
            bLFieldBasedAccess.expr = createExpression(fieldAccessExpressionNode.expression());
            bLFieldBasedAccess.fieldKind = FieldKind.SINGLE;
            // TODO: Fix this when optional field access is available
            bLFieldBasedAccess.optionalFieldAccess = false;
            return bLFieldBasedAccess;
        } else if (expression.kind() == SyntaxKind.BRACED_EXPRESSION) {
            BracedExpressionNode brcExprOut = (BracedExpressionNode) expression;
            return createExpression(brcExprOut.expression());
        }
        //TODO: Remove this
        dlog.error(emptyPos, DiagnosticCode.UNDEFINED_SYMBOL, expression.kind());
        return null;
    }

    private BLangSimpleVariable createSimpleVar(String name, Node type) {
        return createSimpleVar(name, type, null, false, false);
    }

    private BLangSimpleVariable createSimpleVar(String name, Node typeName, Node initializer, boolean isFinal,
                                                boolean isListenerVar) {
        BLangSimpleVariable bLSimpleVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        bLSimpleVar.setName(this.createIdentifier(emptyPos, name));
        bLSimpleVar.name.pos = emptyPos;
        bLSimpleVar.pos = emptyPos;
        boolean isDeclaredWithVar = (VAR.equals(typeName.toString().trim()));
        if (isDeclaredWithVar) {
            bLSimpleVar.isDeclaredWithVar = true;
        } else {
            bLSimpleVar.setTypeNode(createTypeNode(typeName));
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
        String textValue = (literal instanceof Token) ? ((Token) literal).text() : "";

        //TODO: Verify all types, only string type tested
        if (type == SyntaxKind.DECIMAL_INTEGER_LITERAL || type == SyntaxKind.HEX_INTEGER_LITERAL) {
            typeTag = TypeTags.INT;
            value = getIntegerLiteral(type, textValue);
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
        } else if (type == SyntaxKind.STRING_LITERAL) {
            String text = textValue;
            Matcher matcher = UNICODE_PATTERN.matcher(text);
            int position = 0;
            while (matcher.find(position)) {
                String hexStringVal = matcher.group(1);
                int hexDecimalVal = Integer.parseInt(hexStringVal, 16);
                if ((hexDecimalVal >= Constants.MIN_UNICODE && hexDecimalVal <= Constants.MIDDLE_LIMIT_UNICODE)
                        || hexDecimalVal > Constants.MAX_UNICODE) {
                    String hexStringWithBraces = matcher.group(0);
                    dlog.error(emptyPos, DiagnosticCode.INVALID_UNICODE, hexStringWithBraces);
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

        bLiteral.pos = emptyPos;
        bLiteral.type = symTable.getTypeFromTag(typeTag);
        bLiteral.type.tag = typeTag;
        bLiteral.value = value;
        bLiteral.originalValue = originalValue;
        return bLiteral;
    }

    private BLangType createTypeNode(Node type) {
        if (type instanceof Token || type.kind() == SyntaxKind.NIL_TYPE) {
            // Default type
            BLangValueType bLValueType = (BLangValueType) TreeBuilder.createValueTypeNode();
            String typeText = (type.kind() != SyntaxKind.NIL_TYPE) ? ((Token) type).text() : "()";
            bLValueType.typeKind = TreeUtils.stringToTypeKind(typeText.replaceAll("\\s+", ""));
            bLValueType.pos = emptyPos;
            return bLValueType;
        } else if (type.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE || type.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            // Exclusive type
            BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            BLangNameReference nameReference = getBLangNameReference(type);
            bLUserDefinedType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
            bLUserDefinedType.typeName = (BLangIdentifier) nameReference.name;
            bLUserDefinedType.pos = emptyPos;
            return bLUserDefinedType;
        } else if (type.kind() == SyntaxKind.RECORD_TYPE_DESC) {
            // Inclusive type
            BLangTypeDefinition bLTypeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();

            // Generate a name for the anonymous object
            String genName = anonymousModelHelper.getNextAnonymousTypeKey(emptyPos.src.pkgID);
            IdentifierNode anonTypeGenName = createIdentifier(emptyPos, genName, null);
            bLTypeDef.setName(anonTypeGenName);
            bLTypeDef.flagSet.add(Flag.PUBLIC);
            bLTypeDef.flagSet.add(Flag.ANONYMOUS);

            bLTypeDef.typeNode = (BLangType) type.apply(this);
            bLTypeDef.pos = emptyPos;
            otherTopLevelNodes.push(bLTypeDef);

            // Create UserDefinedType
            BLangUserDefinedType bLUserDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            bLUserDefinedType.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
            bLUserDefinedType.typeName = bLTypeDef.name;
            bLUserDefinedType.pos = emptyPos;

            return bLUserDefinedType;
        } else if (type.kind() == SyntaxKind.OPTIONAL_TYPE) {
            OptionalTypeDescriptorNode optTypeDescriptor = (OptionalTypeDescriptorNode) type;

            BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            nilTypeNode.pos = emptyPos;
            nilTypeNode.typeKind = TypeKind.NIL;

            BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
            unionTypeNode.memberTypeNodes.add(createTypeNode(optTypeDescriptor.typeDescriptor()));
            unionTypeNode.memberTypeNodes.add(nilTypeNode);

            unionTypeNode.pos = emptyPos;
            return unionTypeNode;
        }
        return null;
    }

    private BLangNameReference getBLangNameReference(Node node) {
        if (node.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            // qualified identifier
            QualifiedNameReferenceNode identifierNode = (QualifiedNameReferenceNode) node;
            BLangIdentifier pkgAlias = this.createIdentifier(emptyPos, identifierNode.modulePrefix().text());
            BLangIdentifier name = this.createIdentifier(emptyPos, identifierNode.identifier().text());
            return new BLangNameReference(emptyPos, null, pkgAlias, name);
        } else {
            // simple identifier
            BLangIdentifier pkgAlias = this.createIdentifier(emptyPos, "");
            BLangIdentifier name = this.createIdentifier(emptyPos, ((Token) node).text());
            return new BLangNameReference(emptyPos, null, pkgAlias, name);
        }
    }

    private Object getIntegerLiteral(SyntaxKind type, String nodeValue) {
        if (type == SyntaxKind.DECIMAL_INTEGER_LITERAL) {
            return parseLong(nodeValue, nodeValue, 10, DiagnosticCode.INTEGER_TOO_SMALL,
                             DiagnosticCode.INTEGER_TOO_LARGE);
        } else if (type == SyntaxKind.HEX_INTEGER_LITERAL) {
            String processedNodeValue = nodeValue.toLowerCase().replace("0x", "");
            return parseLong(nodeValue, processedNodeValue, 16,
                             DiagnosticCode.HEXADECIMAL_TOO_SMALL, DiagnosticCode.HEXADECIMAL_TOO_LARGE);
        }
        return null;
    }

    private Object parseLong(String originalNodeValue, String processedNodeValue, int radix,
                             DiagnosticCode code1, DiagnosticCode code2) {
        try {
            return Long.parseLong(processedNodeValue, radix);
        } catch (Exception e) {
            DiagnosticPos pos = emptyPos;
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
}

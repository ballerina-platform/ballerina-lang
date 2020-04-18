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

import io.ballerinalang.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.BlockStatementNode;
import io.ballerinalang.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerinalang.compiler.syntax.tree.EmptyToken;
import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerinalang.compiler.syntax.tree.ImportPrefixNode;
import io.ballerinalang.compiler.syntax.tree.ImportVersionNode;
import io.ballerinalang.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.NamedArgumentNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.NodeTransformer;
import io.ballerinalang.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedIdentifierNode;
import io.ballerinalang.compiler.syntax.tree.RequiredParameterNode;
import io.ballerinalang.compiler.syntax.tree.RestArgumentNode;
import io.ballerinalang.compiler.syntax.tree.RestParameterNode;
import io.ballerinalang.compiler.syntax.tree.SpecificFieldNode;
import io.ballerinalang.compiler.syntax.tree.SpreadFieldNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
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
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Constants;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates a {@code BLandCompilationUnit} from the given {@code ModulePart}.
 *
 * @since 1.3.0
 */
public class BLangCompUnitGen extends NodeTransformer<BLangNode> {

    private static final String IDENTIFIER_LITERAL_PREFIX = "'";
    public static final String VAR = "var";

    // TODO This is a temporary solution,
    private DiagnosticPos emptyPos;

    private BLangDiagnosticLogHelper dlog;
    private SymbolTable symTable;
    private BDiagnosticSource diagnosticSource;

    private static final Pattern UNICODE_PATTERN = Pattern.compile(Constants.UNICODE_REGEX);

    public BLangCompilationUnit getCompilationUnit(ModulePartNode modulePart,
                                                   CompilerContext context,
                                                   BDiagnosticSource diagnosticSource) {
        this.emptyPos = new DiagnosticPos(diagnosticSource, 1, 1, 1, 1);
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.diagnosticSource = diagnosticSource;
        return (BLangCompilationUnit) modulePart.apply(this);
    }

    @Override
    public BLangIdentifier transform(IdentifierToken identifierToken) {
        return this.createIdentifier(emptyPos, identifierToken.text());
    }

    @Override
    public BLangCompilationUnit transform(ModulePartNode modulePart) {
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

        compilationUnit.pos = emptyPos;
        return compilationUnit;
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

    @Override
    public BLangFunction transform(FunctionDefinitionNode funcDefNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        bLFunction.pos = emptyPos;

        // Set function name
        IdentifierToken funcName = funcDefNode.functionName();
        bLFunction.name = createIdentifier(emptyPos, funcName.text());

        // Set the visibility qualifier
        Optional<Token> optionalVisibilityQual = funcDefNode.visibilityQualifier();
        optionalVisibilityQual.ifPresent(visibilityQual -> {
            if (visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                bLFunction.flagSet.add(Flag.PUBLIC);
            } else if (visibilityQual.kind() == SyntaxKind.PRIVATE_KEYWORD) {
                bLFunction.flagSet.add(Flag.PRIVATE);
            }
        });

        // TODO populate function signature
        getFuncSignature(bLFunction, funcDefNode);

        // Set the function body
        BLangBlockStmt bLangBlockStmt = (BLangBlockStmt) funcDefNode.functionBody().apply(this);
        BLangBlockFunctionBody blFuncBody = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        blFuncBody.stmts = bLangBlockStmt.stmts;
        bLFunction.body = blFuncBody;

        return bLFunction;
    }

    // -----------------------------------------------Statements--------------------------------------------------------

    @Override
    public BLangBlockStmt transform(BlockStatementNode blockStatement) {
        BLangBlockStmt blockNode = (BLangBlockStmt) TreeBuilder.createBlockNode();
        List<BLangStatement> statements = new ArrayList<>();
        blockStatement.statements().iterator().forEachRemaining(statement -> {
            // TODO: Remove this check once statements are non null guaranteed
            if (statement != null) {
                statements.add((BLangStatement) statement.apply(this));
            }
        });
        blockNode.stmts = statements;
        return blockNode;
    }

    @Override
    public BLangNode transform(AssignmentStatementNode assignmentStatement) {
        return null;
    }

    @Override
    public BLangSimpleVariableDef transform(VariableDeclarationNode varDeclaration) {
        BLangSimpleVariable var = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        BLangSimpleVariableDef varDefNode = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        var.pos = emptyPos;
        var.setName(this.createIdentifier(emptyPos, varDeclaration.variableName().text()));
        var.name.pos = emptyPos;

        if (varDeclaration.finalKeyword().isPresent()) {
            markVariableAsFinal(var);
        }

        boolean isDeclaredWithVar = (VAR.equals(varDeclaration.typeName().toString().trim()));
        if (isDeclaredWithVar) {
            var.isDeclaredWithVar = true;
        } else {
            var.setTypeNode(createTypeNode(varDeclaration.typeName()));
        }
        if (varDeclaration.initializer().isPresent()) {
            var.setInitialExpression(createExpression(varDeclaration.initializer().get()));
        }

        varDefNode.pos = emptyPos;
        varDefNode.setVariable(var);
        return varDefNode;
    }

    @Override
    public BLangExpressionStmt transform(ExpressionStatementNode expressionStatement) {
        BLangExpressionStmt expressionStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        expressionStmt.expr = (BLangExpression) expressionStatement.expression().apply(this);
        expressionStmt.pos = emptyPos;
        return expressionStmt;
    }

    public BLangInvocation transform(FunctionCallExpressionNode functionCallNode) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        Node nameNode = functionCallNode.functionName();
        BLangNameReference reference = getBLangNameReference(nameNode);
        invocationNode.pkgAlias = (BLangIdentifier) reference.pkgAlias;
        invocationNode.name = (BLangIdentifier) reference.name;

        List<BLangExpression> args = new ArrayList<>();
        functionCallNode.arguments().iterator().forEachRemaining(arg -> args.add((BLangExpression) arg.apply(this)));
        invocationNode.argExprs = args;
        invocationNode.pos = emptyPos;

        return invocationNode;
    }

    // -------------------------------------------------Misc------------------------------------------------------------

    @Override
    public BLangExpression transform(PositionalArgumentNode argumentNode) {
        return createExpression(argumentNode.expression());
    }

    @Override
    public BLangExpression transform(NamedArgumentNode namedArgumentNode) {
        return (BLangExpression) namedArgumentNode.expression().apply(this);
    }

    @Override
    public BLangExpression transform(RestArgumentNode restArgumentNode) {
        return (BLangExpression) restArgumentNode.expression().apply(this);
    }

    @Override
    public BLangSimpleVariable transform(RequiredParameterNode requiredParameter) {
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
    public BLangSimpleVariable transform(DefaultableParameterNode defaultableParameter) {
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
    public BLangSimpleVariable transform(RestParameterNode restParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(restParameter.paramName().text(), restParameter.typeName());

        BLangArrayType typeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        typeNode.elemtype = simpleVar.typeNode;
        typeNode.dimensions = 1;
        simpleVar.typeNode = typeNode;
        typeNode.pos = emptyPos;

        simpleVar.pos = emptyPos;
        return simpleVar;
    }

    @Override
    protected BLangNode transformSyntaxNode(Node node) {
        return null;
    }

    // ------------------------------------------private methods--------------------------------------------------------
    private void getFuncSignature(BLangFunction bLFunction, FunctionDefinitionNode funcDefNode) {
        // Set Parameters
        funcDefNode.parameters().iterator().forEachRemaining(child -> {
            SimpleVariableNode param = (SimpleVariableNode) child.apply(this);
            if (child instanceof RestParameterNode) {
                bLFunction.setRestParameter(param);
            } else {
                bLFunction.addParameter(param);
            }
        });

        // Set Return Type
        Optional<Node> retNode = funcDefNode.returnTypeDesc();
        if (retNode.isPresent()) {
            VariableDeclarationNode returnType = (VariableDeclarationNode) retNode.get();
            bLFunction.setReturnTypeNode(createTypeNode(returnType.typeName()));
        } else {
            bLFunction.setReturnTypeNode(createTypeNode(null));
        }
    }

    private BLangExpression createExpression(Node expression) {
        if (isSimpleLiteral(expression.kind())) {
            return createSimpleLiteral((Token) expression);
        } else if (expression.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            // Variable Reference
            IdentifierToken identifier = (IdentifierToken) expression;
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
            varRef.pos = emptyPos;
            varRef.pkgAlias = this.createIdentifier(emptyPos, "");
            varRef.variableName = this.createIdentifier(emptyPos, identifier.text());
            return varRef;
        } else if (expression.kind() == SyntaxKind.MAPPING_CONSTRUCTOR) {
            BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
            MappingConstructorExpressionNode mapConstruct = (MappingConstructorExpressionNode) expression;
            mapConstruct.fields().iterator().forEachRemaining(field -> {
                if (field.kind() == SyntaxKind.SPREAD_FIELD) {
                    SpreadFieldNode spreadFieldNode = (SpreadFieldNode) field;
                    BLangRecordSpreadOperatorField valueExpr =
                            (BLangRecordSpreadOperatorField) TreeBuilder.createRecordSpreadOperatorField();
                    valueExpr.expr = createExpression(spreadFieldNode.valueExpr());
                    literalNode.fields.add(valueExpr);
                } else {
                    SpecificFieldNode specificField = (SpecificFieldNode) field;
                    BLangRecordKeyValueField keyValue = (BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
                    keyValue.valueExpr = createExpression(specificField.valueExpr());
                    keyValue.key = new BLangRecordLiteral.BLangRecordKey(createExpression(specificField.fieldName()));
                    keyValue.key.computedKey = false;
                    literalNode.fields.add(keyValue);
                }
            });
            literalNode.pos = emptyPos;
            return literalNode;
        } else if (expression.kind() == SyntaxKind.UNARY_EXPRESSION) {
            UnaryExpressionNode unaryExpressionNode = (UnaryExpressionNode) expression;
            BLangUnaryExpr unaryExpression = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
            unaryExpression.pos = emptyPos;
            unaryExpression.expr = createExpression(unaryExpressionNode.expression());
            unaryExpression.operator = OperatorKind.valueFrom(unaryExpressionNode.unaryOperator().text());
            return unaryExpression;
        } else if (expression.kind() == SyntaxKind.BINARY_EXPRESSION) {
            BinaryExpressionNode binaryExpressionNode = (BinaryExpressionNode) expression;
            BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
            binaryExpr.pos = emptyPos;
            binaryExpr.lhsExpr = createExpression(binaryExpressionNode.lhsExpr());
            binaryExpr.rhsExpr = createExpression(binaryExpressionNode.lhsExpr());
            binaryExpr.opKind = OperatorKind.valueFrom(binaryExpressionNode.operator().text());
            return binaryExpr;
        }
        //TODO: Remove this
        dlog.error(emptyPos, DiagnosticCode.UNDEFINED_SYMBOL, expression.kind());
        return null;
    }

    private BLangSimpleVariable createSimpleVar(String name, Node type) {
        BLangSimpleVariable variableNode = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        variableNode.setName(this.createIdentifier(emptyPos, name));
        variableNode.setTypeNode(createTypeNode(type));
        return variableNode;
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, String value) {
        return createIdentifier(pos, value, null);
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, String value, Set<Whitespace> ws) {
        BLangIdentifier node = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        if (value == null) {
            return node;
        }

        if (value.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            if (!escapeQuotedIdentifier(value).matches("^[0-9a-zA-Z.]*$")) {
                dlog.error(pos, DiagnosticCode.IDENTIFIER_LITERAL_ONLY_SUPPORTS_ALPHANUMERICS);
            }
            String unescapedValue = StringEscapeUtils.unescapeJava(value);
            node.setValue(unescapedValue.substring(1));
            node.originalValue = value;
            node.setLiteral(true);
        } else {
            node.setValue(value);
            node.setLiteral(false);
        }
        node.pos = pos;
        if (ws != null) {
            node.addWS(ws);
        }
        return node;
    }

    private BLangLiteral createSimpleLiteral(Token node) {
        BLangLiteral litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        SyntaxKind type = node.kind();
        int typeTag = -1;
        Object value = null;
        String originalValue = null;

        //TODO: Verify all types, only string type tested

        if (type == SyntaxKind.DECIMAL_INTEGER_LITERAL || type == SyntaxKind.HEX_INTEGER_LITERAL) {
            typeTag = TypeTags.INT;
            value = getIntegerLiteral(type, node.text());
            originalValue = node.text();
            litExpr = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else if (type == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL) {
            //TODO: Fix 'op' parameter
            String nodeValue = getNodeValue(node.text(), null);
            typeTag = NumericLiteralSupport.isDecimalDiscriminated(nodeValue) ? TypeTags.DECIMAL : TypeTags.FLOAT;
            value = nodeValue;
            originalValue = nodeValue;
            litExpr = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else if (type == SyntaxKind.HEX_FLOATING_POINT_LITERAL) {
            //TODO: Fix 'op' parameter
            typeTag = TypeTags.FLOAT;
            value = getHexNodeValue(node.text(), null);
            originalValue = node.text();
            litExpr = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else if (type == SyntaxKind.TRUE_KEYWORD || type == SyntaxKind.FALSE_KEYWORD) {
            typeTag = TypeTags.BOOLEAN;
            value = Boolean.parseBoolean(node.text());
            originalValue = node.text();
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.STRING_LITERAL) {
            String text = node.text();
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
            originalValue = node.text();
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.NONE) {
            typeTag = TypeTags.NIL;
            value = null;
            originalValue = "null";
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.NIL_TYPE) {
            typeTag = TypeTags.NIL;
            value = null;
            originalValue = "()";
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.BINARY_EXPRESSION) { // Should be base16 and base64
            String nodeValue = getNodeValue(node.text(), null);
            typeTag = TypeTags.BYTE_ARRAY;
            value = nodeValue;
            originalValue = nodeValue;

            // If numeric literal create a numeric literal expression; otherwise create a literal expression
            if (isNumericLiteral(type)) {
                litExpr = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
            } else {
                litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
            }
        }

        litExpr.pos = emptyPos;
        litExpr.type = symTable.getTypeFromTag(typeTag);
        litExpr.type.tag = typeTag;
        litExpr.value = value;
        litExpr.originalValue = originalValue;
        return litExpr;
    }

    private BLangType createTypeNode(Node type) {
        if (type == null) {
            BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            TypeKind typeKind = TypeKind.NIL;
            typeNode.pos = emptyPos;
            typeNode.typeKind = typeKind;
            return typeNode;
        } else if (type.kind() == SyntaxKind.QUALIFIED_IDENTIFIER || type.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            BLangNameReference nameReference = getBLangNameReference(type);
            userDefinedType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
            userDefinedType.typeName = (BLangIdentifier) nameReference.name;
            userDefinedType.pos = emptyPos;
            return userDefinedType;
        } else if (type instanceof Token) {
            BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            String typeText = ((Token) type).text();
            TypeKind typeKind = (TreeUtils.stringToTypeKind(typeText.replaceAll("\\s+", "")));
            typeNode.pos = emptyPos;
            typeNode.typeKind = typeKind;
            return typeNode;
        }
        return null;
    }

    private BLangNameReference getBLangNameReference(Node node) {
        if (node.kind() == SyntaxKind.QUALIFIED_IDENTIFIER) {
            // qualified identifier
            QualifiedIdentifierNode identifierNode = (QualifiedIdentifierNode) node;
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

    private String getNodeValue(String value, String op) {
        if (op != null && "-".equals(op)) {
            value = "-" + value;
        }
        return value;
    }

    private String getHexNodeValue(String value, String op) {
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
            case NIL_TYPE:
            case NONE:
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

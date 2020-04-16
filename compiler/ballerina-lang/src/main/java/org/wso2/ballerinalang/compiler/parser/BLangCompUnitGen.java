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

import io.ballerinalang.compiler.internal.parser.tree.STIdentifierToken;
import io.ballerinalang.compiler.internal.parser.tree.STLiteralValueToken;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.internal.parser.tree.STTypeToken;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxTrivia;
import io.ballerinalang.compiler.syntax.tree.AssignmentStatement;
import io.ballerinalang.compiler.syntax.tree.BlockStatement;
import io.ballerinalang.compiler.syntax.tree.CallStatement;
import io.ballerinalang.compiler.syntax.tree.EmptyToken;
import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinition;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.ImportDeclaration;
import io.ballerinalang.compiler.syntax.tree.ImportOrgName;
import io.ballerinalang.compiler.syntax.tree.ImportPrefix;
import io.ballerinalang.compiler.syntax.tree.ImportVersion;
import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclaration;
import io.ballerinalang.compiler.syntax.tree.ModulePart;
import io.ballerinalang.compiler.syntax.tree.NamedArgument;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeTransformer;
import io.ballerinalang.compiler.syntax.tree.PositionalArgument;
import io.ballerinalang.compiler.syntax.tree.QualifiedIdentifier;
import io.ballerinalang.compiler.syntax.tree.RequiredParameter;
import io.ballerinalang.compiler.syntax.tree.RestArgument;
import io.ballerinalang.compiler.syntax.tree.RestParameter;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.VariableDeclaration;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Generates a {@code BLandCompilationUnit} from the given {@code ModulePart}.
 *
 * @since 1.3.0
 */
public class BLangCompUnitGen extends NodeTransformer<BLangNode> {

    private static final String IDENTIFIER_LITERAL_PREFIX = "'";

    // TODO This is a temporary solution,
    private DiagnosticPos emptyPos;

    private BLangDiagnosticLogHelper dlog;
    private SymbolTable symTable;
    private BDiagnosticSource diagnosticSource;

    private static final Pattern UNICODE_PATTERN = Pattern.compile(Constants.UNICODE_REGEX);

    public BLangCompilationUnit getCompilationUnit(ModulePart modulePart,
                                                   CompilerContext context,
                                                   BDiagnosticSource diagnosticSource) {
        this.emptyPos = new DiagnosticPos(diagnosticSource, 1, 1, 1, 1);
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.diagnosticSource = diagnosticSource;
        return (BLangCompilationUnit) modulePart.apply(this);
    }

    @Override
    public BLangNode transform(ModulePart modulePart) {
        BLangCompilationUnit compilationUnit = (BLangCompilationUnit) TreeBuilder.createCompilationUnit();
        compilationUnit.name = diagnosticSource.cUnitName;

        // Generate import declarations
        for (ImportDeclaration importDecl : modulePart.imports()) {
            BLangImportPackage bLangImport = (BLangImportPackage) importDecl.apply(this);
            bLangImport.compUnit = this.createIdentifier(emptyPos, compilationUnit.getName());
            compilationUnit.addTopLevelNode(bLangImport);
        }

        // Generate other module-level declarations
        for (ModuleMemberDeclaration member : modulePart.members()) {
            compilationUnit.addTopLevelNode((TopLevelNode) member.apply(this));
        }

        compilationUnit.pos = emptyPos;
        return compilationUnit;
    }

    @Override
    public BLangImportPackage transform(ImportDeclaration importDeclaration) {
        Node orgNameNode = importDeclaration.orgName();
        Node versionNode = importDeclaration.version();
        Node prefixNode = importDeclaration.prefix();

        String orgName = null;
        if (orgNameNode.kind() == SyntaxKind.IMPORT_ORG_NAME) {
            ImportOrgName importOrgName = (ImportOrgName) orgNameNode;
            orgName = importOrgName.orgName().text();
        }

        String version = null;
        if (versionNode.kind() == SyntaxKind.IMPORT_VERSION) {
            version = ((ImportVersion) versionNode).versionNumber().toString();
        }

        String prefix = null;
        if (prefixNode.kind() == SyntaxKind.IMPORT_PREFIX) {
            prefix = ((ImportPrefix) prefixNode).prefix().toString();
        }

        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        Node node = importDeclaration.moduleName();
        if (node.kind() == SyntaxKind.LIST) {
            STNodeList list = (STNodeList) node.internalNode();
            IntStream.range(0, list.bucketCount())
                    .forEach(i -> {
                        STNode stNode = list.childInBucket(i);
                        pkgNameComps.add(this.createIdentifier(emptyPos, getTokenText(stNode), null));
                    });
        }

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
    public BLangFunction transform(FunctionDefinition funcDefNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        bLFunction.pos = emptyPos;

        // Set function name
        IdentifierToken funcName = funcDefNode.functionName();
        bLFunction.name = createIdentifier(emptyPos, funcName.text());

        // Set the visibility qualifier
        Token visibilityQual = funcDefNode.visibilityQualifier();
        if (isPresent(visibilityQual)) {
            if (visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
                bLFunction.flagSet.add(Flag.PUBLIC);
            } else if (visibilityQual.kind() == SyntaxKind.PRIVATE_KEYWORD) {
                bLFunction.flagSet.add(Flag.PRIVATE);
            }
        }

        // TODO populate function signature
        getFuncSignature(bLFunction, funcDefNode);

        // Set the function body
        BLangBlockStmt bLangBlockStmt = (BLangBlockStmt) funcDefNode.functionBody().apply(this);
        BLangBlockFunctionBody blFuncBody = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        blFuncBody.stmts = bLangBlockStmt.stmts;
        bLFunction.body = blFuncBody;

        return bLFunction;
    }

    private void getFuncSignature(BLangFunction bLFunction, FunctionDefinition funcDefNode) {
        // Set Parameters
        funcDefNode.parameters().iterator().forEachRemaining(child -> {
            SimpleVariableNode param = (SimpleVariableNode) child.apply(this);
            if (child instanceof RestParameter) {
                bLFunction.setRestParameter(param);
            } else {
                bLFunction.addParameter(param);
            }
        });

        // Set Return Type
        Node retNode = funcDefNode.returnTypeDesc();
        if (retNode.kind() == SyntaxKind.RETURN_TYPE_DESCRIPTOR) {
            VariableDeclaration returnType = (VariableDeclaration) retNode;
            String typeText = getTokenText(returnType.typeName().internalNode());
            BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            typeNode.pos = emptyPos;
            typeNode.typeKind = (TreeUtils.stringToTypeKind(typeText.replaceAll("\\s+", "")));
            bLFunction.setReturnTypeNode(typeNode);
        } else {
            BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            nilTypeNode.pos = emptyPos;
            nilTypeNode.typeKind = TypeKind.NIL;
            bLFunction.setReturnTypeNode(nilTypeNode);
        }
    }

    // -----------------------------------------------Statements--------------------------------------------------------

    @Override
    public BLangBlockStmt transform(BlockStatement blockStatement) {
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
    public BLangNode transform(AssignmentStatement assignmentStatement) {
        return super.transform(assignmentStatement);
    }

    @Override
    public BLangExpressionStmt transform(CallStatement callStatement) {
        BLangExpressionStmt expressionStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        expressionStmt.expr = (BLangExpression) callStatement.expression().apply(this);
        expressionStmt.pos = emptyPos;
        return expressionStmt;
    }

    public BLangInvocation transform(FunctionCallExpressionNode functionCallNode) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        Node nameNode = functionCallNode.functionName();
        if (nameNode.kind() == SyntaxKind.QUALIFIED_IDENTIFIER) {
            QualifiedIdentifier name = (QualifiedIdentifier) nameNode;
            invocationNode.name = createIdentifier(emptyPos, name.identifier().text());
            //TODO: Find a proper way to get package alias
            invocationNode.pkgAlias = createIdentifier(emptyPos, name.modulePrefix().text());
        }
        List<BLangExpression> args = new ArrayList<>();
        functionCallNode.arguments().iterator().forEachRemaining(arg -> args.add((BLangExpression) arg.apply(this)));
        invocationNode.argExprs = args;
        invocationNode.pos = emptyPos;

        return invocationNode;
    }

    // -------------------------------------------------Misc------------------------------------------------------------

    @Override
    public BLangExpression transform(PositionalArgument argumentNode) {
        ExpressionNode expression = argumentNode.expression();
        if (isSimpleLiteral(expression)) {
            return createSimpleLiteral(expression);
        }
        return null;
    }

    @Override
    public BLangExpression transform(NamedArgument namedArgumentNode) {
        return (BLangExpression) namedArgumentNode.expression().apply(this);
    }

    @Override
    public BLangExpression transform(RestArgument restArgumentNode) {
        return (BLangExpression) restArgumentNode.expression().apply(this);
    }

    @Override
    public BLangSimpleVariable transform(RequiredParameter requiredParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(requiredParameter.paramName(), requiredParameter.type());

        Node visibilityQual = requiredParameter.visibilityQualifier();
        //TODO: Check and Fix flags OPTIONAL, REQUIRED
        if (isPresent(visibilityQual) && visibilityQual.kind() == SyntaxKind.PUBLIC_KEYWORD) {
            simpleVar.flagSet.add(Flag.PUBLIC);
        }

        simpleVar.pos = emptyPos;
        return simpleVar;
    }

    @Override
    public BLangSimpleVariable transform(RestParameter restParameter) {
        BLangSimpleVariable simpleVar = createSimpleVar(restParameter.paramName(), restParameter.type());

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

    private BLangSimpleVariable createSimpleVar(Token name, Node type) {
        BLangSimpleVariable variableNode = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();

        String nameText = getTokenText(name.internalNode());
        variableNode.setName(this.createIdentifier(emptyPos, nameText));

        String typeText = getTokenText(type.internalNode());
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = emptyPos;
        typeNode.typeKind = (TreeUtils.stringToTypeKind(typeText.replaceAll("\\s+", "")));
        variableNode.setTypeNode(typeNode);

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

    private BLangLiteral createSimpleLiteral(Node node) {
        BLangLiteral litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        SyntaxKind type = node.kind();
        int typeTag = -1;
        Object value = null;
        String originalValue = null;

        //TODO: Verify all types, only string type tested

        if (type == SyntaxKind.DECIMAL_INTEGER_LITERAL || type == SyntaxKind.HEX_INTEGER_LITERAL) {
            typeTag = TypeTags.INT;
            String nodeValue = getTokenText(node.internalNode());
            value = getIntegerLiteral(type, nodeValue);
            originalValue = nodeValue;
            litExpr = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else if (type == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL) {
            //TODO: Fix 'op' parameter
            String nodeValue = getNodeValue(node.internalNode(), null);
            typeTag = NumericLiteralSupport.isDecimalDiscriminated(nodeValue) ? TypeTags.DECIMAL : TypeTags.FLOAT;
            value = nodeValue;
            originalValue = nodeValue;
            litExpr = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else if (type == SyntaxKind.HEX_FLOATING_POINT_LITERAL) {
            String nodeValue = getTokenText(node.internalNode());
            //TODO: Fix 'op' parameter
            typeTag = TypeTags.FLOAT;
            value = getHexNodeValue(node.internalNode(), null);
            originalValue = nodeValue;
            litExpr = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else if (type == SyntaxKind.TRUE_KEYWORD || type == SyntaxKind.FALSE_KEYWORD) {
            typeTag = TypeTags.BOOLEAN;
            String nodeValue = getTokenText(node.internalNode());
            value = Boolean.parseBoolean(nodeValue);
            originalValue = nodeValue;
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.STRING_LITERAL) {
            String nodeValue = getTokenText(node.internalNode());
            String text = nodeValue;
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
            originalValue = nodeValue;
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.SIMPLE_TYPE) { // NULL
//            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.NIL, null, "null");
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.NONE) { // NIL
//            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.NIL, null, "()");
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        } else if (type == SyntaxKind.BINARY_EXPRESSION) { // BLOB
//            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.BYTE_ARRAY, ctx.blobLiteral().getText());
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        }

        litExpr.pos = emptyPos;
        litExpr.type = symTable.getTypeFromTag(typeTag);
        litExpr.type.tag = typeTag;
        litExpr.value = value;
        litExpr.originalValue = originalValue;
        return litExpr;
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

    private String getNodeValue(STNode node, String op) {
        String value = getTokenText(node);
        if (op != null && "-".equals(op)) {
            value = "-" + value;
        }
        return value;
    }

    private String getHexNodeValue(STNode node, String op) {
        String value = getNodeValue(node, op);
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

    private boolean isSimpleLiteral(Node expression) {
        switch (expression.kind()) {
            case STRING_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
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

    private boolean isPresent(Token token) {
        // TODO find a better way to check this condition
        return !(token instanceof EmptyToken);
    }

    private boolean isPresent(Node node) {
        return node.kind() != SyntaxKind.NONE;
    }

    private static String getTokenText(STNode stNode) {
        switch (stNode.kind) {
            case IDENTIFIER_TOKEN:
                return ((STIdentifierToken) stNode).text;
            case STRING_LITERAL:
                String val = ((STLiteralValueToken) stNode).text;
                int stringLen = val.length();
                int lastCharPosition = val.endsWith("\"") ? stringLen - 1 : stringLen;
                return val.substring(1, lastCharPosition);
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
                return ((STLiteralValueToken) stNode).text;
            case SIMPLE_TYPE:
                return ((STTypeToken) stNode).text;
            case WHITESPACE_TRIVIA:
            case END_OF_LINE_TRIVIA:
            case COMMENT:
            case INVALID:
                return ((SyntaxTrivia) stNode).text;
            case NONE:
                return "";
            default:
                return stNode.kind.toString();
        }
    }
}

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

import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.AssignmentStatement;
import io.ballerinalang.compiler.syntax.tree.BlockStatement;
import io.ballerinalang.compiler.syntax.tree.EmptyToken;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.Identifier;
import io.ballerinalang.compiler.syntax.tree.ImportDeclaration;
import io.ballerinalang.compiler.syntax.tree.LocalVariableDeclaration;
import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclaration;
import io.ballerinalang.compiler.syntax.tree.ModulePart;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.SyntaxNodeTransformer;
import io.ballerinalang.compiler.syntax.tree.Token;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.Set;

/**
 * Generates a {@code BLandCompilationUnit} from the given {@code ModulePart}.
 *
 * @since 1.3.0
 */
public class BLangCompUnitGen extends SyntaxNodeTransformer<BLangNode> {

    private static final String IDENTIFIER_LITERAL_PREFIX = "'";

    // TODO This is a temporary solution,
    private DiagnosticPos emptyPos;

    private BLangDiagnosticLogHelper dlog;

    public BLangCompilationUnit getCompilationUnit(ModulePart modulePart,
                                                   CompilerContext context,
                                                   BDiagnosticSource diagnosticSource) {
        this.emptyPos = new DiagnosticPos(diagnosticSource, 1, 1, 1, 1);
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        return (BLangCompilationUnit) modulePart.apply(this);
    }

    @Override
    public BLangNode transform(ModulePart modulePart) {
        BLangCompilationUnit compilationUnit = (BLangCompilationUnit) TreeBuilder.createCompilationUnit();
        // Generate import declarations
        for (ImportDeclaration importDecl : modulePart.imports()) {
            BLangImportPackage importPackage = (BLangImportPackage) importDecl.apply(this);
            compilationUnit.addTopLevelNode(importPackage);
        }
        // Generate other module-level declarations
        for (ModuleMemberDeclaration member : modulePart.members()) {
            TopLevelNode topLevelNode = (TopLevelNode) member.apply(this);
            compilationUnit.addTopLevelNode(topLevelNode);
        }
        return compilationUnit;
    }

    @Override
    public BLangNode transform(ImportDeclaration importDeclaration) {
        BLangImportPackage importPackage = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        // TODO
        return importPackage;
    }

    @Override
    public BLangNode transform(FunctionDefinitionNode funcDefNode) {
        BLangFunction bLFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        bLFunction.pos = emptyPos;
        // Set function name
        Identifier funcName = funcDefNode.functionName();
        bLFunction.name = createIdentifier(emptyPos, funcName.value());
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
        genFuncSignature(bLFunction, funcDefNode);

        // Set the function body
        BLangBlockStmt bLangBlockStmt = (BLangBlockStmt) funcDefNode.functionBody().apply(this);
        BLangBlockFunctionBody blFuncBody = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        blFuncBody.stmts = bLangBlockStmt.stmts;
        bLFunction.body = blFuncBody;
        return bLFunction;
    }

    // Statements

    @Override
    public BLangNode transform(BlockStatement blockStatement) {
        return super.transform(blockStatement);
    }

    @Override
    public BLangNode transform(LocalVariableDeclaration localVariableDeclaration) {
        return super.transform(localVariableDeclaration);
    }

    @Override
    public BLangNode transform(AssignmentStatement assignmentStatement) {
        return super.transform(assignmentStatement);
    }


    @Override
    protected BLangNode transformSyntaxNode(Node node) {
        return null;
    }

    // private methods

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

    private void genFuncSignature(InvokableNode invokableNode, FunctionDefinitionNode funcDefNode) {
        genFuncParams(invokableNode, funcDefNode);
        genFuncReturn(invokableNode, funcDefNode);
    }

    private void genFuncParams(InvokableNode invokableNode, FunctionDefinitionNode funcDefNode) {
        // TODO
    }

    private void genFuncReturn(InvokableNode invokableNode, FunctionDefinitionNode funcDefNode) {
        BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nilTypeNode.pos = emptyPos;
        nilTypeNode.typeKind = TypeKind.NIL;
        invokableNode.setReturnTypeNode(nilTypeNode);
    }
}

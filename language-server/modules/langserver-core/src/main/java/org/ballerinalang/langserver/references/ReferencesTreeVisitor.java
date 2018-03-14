/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.references;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.common.NodeVisitor;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tree visitor for finding the references of a statement.
 */
public class ReferencesTreeVisitor extends NodeVisitor {
    private boolean terminateVisitor = false;
    private TextDocumentServiceContext context;
    private List<Location> locations;


    public ReferencesTreeVisitor(TextDocumentServiceContext context) {
        this.context = context;
        this.locations = context.get(NodeContextKeys.REFERENCE_NODES_KEY);
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.topLevelNodes.isEmpty()) {
            terminateVisitor = true;
            acceptNode(null);
        } else {
            pkgNode.topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
        }
    }

    @Override
    public void visit(BLangFunction funcNode) {
        // Check for native functions
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol)) {
            return;
        }

        if (this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue()
                .equals(funcNode.symbol.pkgID.name.getValue()) && this.context.get(NodeContextKeys.NAME_OF_NODE_KEY)
                .equals(funcNode.name.getValue())) {
            addLocation(funcNode, funcNode.symbol.pkgID.nameComps, funcNode.symbol.pkgID.nameComps);
        }
        if (!funcNode.requiredParams.isEmpty()) {
            funcNode.requiredParams.forEach(this::acceptNode);
        }

        if (!funcNode.retParams.isEmpty()) {
            funcNode.retParams.forEach(this::acceptNode);
        }

        if (funcNode.body != null) {
            this.acceptNode(funcNode.body);
        }

        if (!funcNode.workers.isEmpty()) {
            funcNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        if (serviceNode.symbol.pkgID.name.getValue()
                .equals(this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue()) &&
                this.context.get(NodeContextKeys.NAME_OF_NODE_KEY).equals(serviceNode.name.getValue()) &&
                this.context.get(NodeContextKeys.NODE_OWNER_KEY).equals(serviceNode.symbol.owner.name.getValue())) {
            addLocation(serviceNode, serviceNode.symbol.pkgID.nameComps,
                    serviceNode.symbol.pkgID.nameComps);
        }

        if (!serviceNode.vars.isEmpty()) {
            serviceNode.vars.forEach(this::acceptNode);
        }

        if (!serviceNode.resources.isEmpty()) {
            serviceNode.resources.forEach(this::acceptNode);
        }

        if (serviceNode.initFunction != null) {
            this.acceptNode(serviceNode.initFunction);
        }
    }

    @Override
    public void visit(BLangResource resourceNode) {
        if (resourceNode.symbol.pkgID.name.getValue()
                .equals(this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue()) &&
                this.context.get(NodeContextKeys.NAME_OF_NODE_KEY).equals(resourceNode.name.getValue()) &&
                this.context.get(NodeContextKeys.NODE_OWNER_KEY).equals(resourceNode.symbol.owner.name.getValue())) {
            addLocation(resourceNode, resourceNode.symbol.pkgID.nameComps,
                    resourceNode.symbol.pkgID.nameComps);
        }

        if (!resourceNode.requiredParams.isEmpty()) {
            resourceNode.requiredParams.forEach(this::acceptNode);
        }

        if (!resourceNode.retParams.isEmpty()) {
            resourceNode.retParams.forEach(this::acceptNode);
        }

        if (resourceNode.body != null) {
            this.acceptNode(resourceNode.body);
        }
    }

    @Override
    public void visit(BLangConnector connectorNode) {
        if (connectorNode.symbol.pkgID.name.getValue()
                .equals(this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue()) &&
                this.context.get(NodeContextKeys.NAME_OF_NODE_KEY).equals(connectorNode.name.getValue()) &&
                this.context.get(NodeContextKeys.NODE_OWNER_KEY).equals(connectorNode.symbol.owner.name.getValue())) {
            addLocation(connectorNode, connectorNode.symbol.pkgID.nameComps,
                    connectorNode.symbol.pkgID.nameComps);
        }

        if (!connectorNode.params.isEmpty()) {
            connectorNode.params.forEach(this::acceptNode);
        }

        if (!connectorNode.varDefs.isEmpty()) {
            connectorNode.varDefs.forEach(this::acceptNode);
        }

        if (!connectorNode.actions.isEmpty()) {
            connectorNode.actions.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangAction actionNode) {
        if (actionNode.symbol.pkgID.name.getValue()
                .equals(this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue()) &&
                this.context.get(NodeContextKeys.NAME_OF_NODE_KEY).equals(actionNode.name.getValue())) {
            addLocation(actionNode, actionNode.symbol.pkgID.nameComps,
                    actionNode.symbol.pkgID.nameComps);
        }

        if (!actionNode.requiredParams.isEmpty()) {
            actionNode.requiredParams.forEach(this::acceptNode);
        }

        if (!actionNode.retParams.isEmpty()) {
            actionNode.retParams.forEach(this::acceptNode);
        }

        if (actionNode.body != null) {
            acceptNode(actionNode.body);
        }

        if (!actionNode.workers.isEmpty()) {
            actionNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangVariable varNode) {
        if ((this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY) != null &&
                this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY).equals(varNode.name.getValue())) &&
                varNode.symbol.owner.name.getValue().equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY)) &&
                varNode.symbol.owner.pkgID.getName().getValue()
                        .equals(this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY).name.getValue())) {

            addLocation(varNode, varNode.symbol.owner.pkgID.nameComps,
                    varNode.pos.getSource().pkgID.nameComps);
        }

        if (varNode.typeNode != null) {
            this.acceptNode(varNode.typeNode);
        }

        if (varNode.expr != null) {
            this.acceptNode(varNode.expr);
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        if (workerNode.body != null) {
            this.acceptNode(workerNode.body);
        }

        if (!workerNode.workers.isEmpty()) {
            workerNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        if (!blockNode.stmts.isEmpty()) {
            blockNode.stmts.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        if (varDefNode.getVariable() != null) {
            this.acceptNode(varDefNode.getVariable());
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        if (!assignNode.varRefs.isEmpty()) {
            assignNode.varRefs.forEach(this::acceptNode);
        }

        if (assignNode.expr != null) {
            this.acceptNode(assignNode.expr);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (ifNode.expr != null) {
            this.acceptNode(ifNode.expr);
        }

        if (ifNode.body != null) {
            this.acceptNode(ifNode.body);
        }

        if (ifNode.elseStmt != null) {
            this.acceptNode(ifNode.elseStmt);
        }
    }

    @Override
    public void visit(BLangForeach foreach) {
        if (!foreach.varRefs.isEmpty()) {
            foreach.varRefs.forEach(this::acceptNode);
        }

        if (foreach.body != null) {
            this.acceptNode(foreach.body);
        }
    }

    @Override
    public void visit(BLangWhile whileNode) {
        if (whileNode.expr != null) {
            this.acceptNode(whileNode.expr);
        }

        if (whileNode.body != null) {
            this.acceptNode(whileNode.body);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        if (transactionNode.transactionBody != null) {
            this.acceptNode(transactionNode.transactionBody);
        }

        if (transactionNode.failedBody != null) {
            this.acceptNode(transactionNode.failedBody);
        }
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        if (tryNode.tryBody != null) {
            this.acceptNode(tryNode.tryBody);
        }

        if (!tryNode.catchBlocks.isEmpty()) {
            tryNode.catchBlocks.forEach(this::acceptNode);
        }

        if (tryNode.finallyBody != null) {
            this.acceptNode(tryNode.finallyBody);
        }
    }

    @Override
    public void visit(BLangCatch catchNode) {
        if (catchNode.body != null) {
            this.acceptNode(catchNode.body);
        }
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        if (!forkJoin.getWorkers().isEmpty()) {
            forkJoin.getWorkers().forEach(this::acceptNode);
        }

        if (forkJoin.joinedBody != null) {
            this.acceptNode(forkJoin.joinedBody);
        }

        if (forkJoin.timeoutBody != null) {
            this.acceptNode(forkJoin.timeoutBody);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        CommonUtil.calculateEndColumnOfGivenName(varRefExpr.getPosition(), varRefExpr.variableName.value,
                varRefExpr.pkgAlias.value);

        if (this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY) != null && varRefExpr.variableName.getValue()
                .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {

            if (varRefExpr.symbol != null && varRefExpr.symbol.owner.name.getValue()
                    .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY)) && varRefExpr.symbol.pkgID.name.getValue()
                    .equals(this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue())) {

                addLocation(varRefExpr, varRefExpr.symbol.owner.pkgID.nameComps,
                        varRefExpr.pos.getSource().pkgID.nameComps);

            } else if (varRefExpr.type.tsymbol.owner.name.getValue()
                    .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY)) &&
                    varRefExpr.type.tsymbol.pkgID.name.getValue()
                            .equals(this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue())) {
                addLocation(varRefExpr, varRefExpr.type.tsymbol.owner.pkgID.nameComps,
                        varRefExpr.pos.getSource().pkgID.nameComps);
            }
        }
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        if (bLangLambdaFunction.function != null) {
            this.acceptNode(bLangLambdaFunction.function);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (this.context.get(NodeContextKeys.NAME_OF_NODE_KEY).equals(invocationExpr.name.getValue()) &&
                invocationExpr.symbol.owner.name.getValue()
                        .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))
                && invocationExpr.symbol.owner.pkgID.getName().getValue()
                .equals(this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY).name.getValue())) {
            addLocation(invocationExpr, invocationExpr.symbol.owner.pkgID.nameComps,
                    invocationExpr.pos.getSource().pkgID.nameComps);
        }

        if (invocationExpr.expr != null) {
            this.acceptNode(invocationExpr.expr);
        }

        if (!invocationExpr.argExprs.isEmpty()) {
            invocationExpr.argExprs.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        if (exprStmtNode.expr != null) {
            this.acceptNode(exprStmtNode.expr);
        }
    }

    @Override
    public void visit(BLangReturn returnNode) {
        if (!returnNode.exprs.isEmpty()) {
            returnNode.exprs.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (fieldAccessExpr.expr != null) {
            this.acceptNode(fieldAccessExpr.expr);
        }
    }

    @Override
    public void visit(BLangEnum enumNode) {
        if (enumNode.symbol.owner.name.getValue().equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY)) &&
                enumNode.symbol.owner.pkgID.name.getValue()
                        .equals(this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY).name.getValue()) &&
                enumNode.name.getValue().equals(this.context.get(NodeContextKeys.NAME_OF_NODE_KEY))) {
            // Fixing the issue with enum end positions by
            // replacing end line and end column with start line and column values
            enumNode.getPosition().eLine = enumNode.getPosition().sLine;
            enumNode.getPosition().eCol = enumNode.getPosition().sCol;
            addLocation(enumNode, enumNode.symbol.owner.pkgID.nameComps,
                    enumNode.pos.getSource().pkgID.nameComps);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        userDefinedType.getPosition().sCol += (this.context.get(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY)
                instanceof BLangEndpointTypeNode ? "endpoint<".length() : 0);
        CommonUtil.calculateEndColumnOfGivenName(userDefinedType.getPosition(), userDefinedType.typeName.value,
                userDefinedType.pkgAlias.value);
        if (userDefinedType.typeName.getValue().equals(this.context.get(NodeContextKeys.NAME_OF_NODE_KEY)) &&
                userDefinedType.type.tsymbol.owner.name.getValue()
                        .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY)) &&
                userDefinedType.type.tsymbol.owner.pkgID.name.getValue()
                        .equals(this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY).name.getValue())) {
            addLocation(userDefinedType, userDefinedType.type.tsymbol.owner.pkgID.nameComps,
                    userDefinedType.pos.getSource().pkgID.nameComps);
        }
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (binaryExpr.lhsExpr != null) {
            acceptNode(binaryExpr.lhsExpr);
        }

        if (binaryExpr.rhsExpr != null) {
            acceptNode(binaryExpr.rhsExpr);
        }
    }

    @Override
    public void visit(BLangStruct structNode) {
        if (structNode.symbol.owner.name.getValue().equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY)) &&
                structNode.symbol.owner.pkgID.name.getValue()
                        .equals(this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY).name.getValue()) &&
                this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue()
                        .equals(structNode.symbol.pkgID.name.getValue()) &&
                this.context.get(NodeContextKeys.NAME_OF_NODE_KEY).equals(structNode.name.getValue())) {
            addLocation(structNode, structNode.symbol.owner.pkgID.nameComps,
                    structNode.pos.getSource().pkgID.nameComps);
        }
        if (!structNode.fields.isEmpty()) {
            structNode.fields.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTransformer transformerNode) {
        if (transformerNode.symbol.owner.name.getValue().equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY)) &&
                transformerNode.symbol.owner.pkgID.name.getValue()
                        .equals(this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY).name.getValue()) &&
                this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue()
                        .equals(transformerNode.symbol.pkgID.name.getValue()) &&
                this.context.get(NodeContextKeys.NAME_OF_NODE_KEY).equals(transformerNode.name.getValue())) {
            addLocation(transformerNode, transformerNode.symbol.owner.pkgID.nameComps,
                    transformerNode.pos.getSource().pkgID.nameComps);
        }
        if (transformerNode.source != null) {
            acceptNode(transformerNode.source);
        }

        if (!transformerNode.requiredParams.isEmpty()) {
            transformerNode.requiredParams.forEach(this::acceptNode);
        }

        if (!transformerNode.retParams.isEmpty()) {
            transformerNode.retParams.forEach(this::acceptNode);
        }

        if (transformerNode.body != null) {
            acceptNode(transformerNode.body);
        }

        if (!transformerNode.workers.isEmpty()) {
            transformerNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTypeCastExpr castExpr) {
        if (castExpr.typeNode != null) {
            this.acceptNode(castExpr.typeNode);
        }

        if (castExpr.expr != null) {
            this.acceptNode(castExpr.expr);
        }
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        if (conversionExpr.expr != null) {
            acceptNode(conversionExpr.expr);
        }

        if (conversionExpr.typeNode != null) {
            acceptNode(conversionExpr.typeNode);
        }

        if (conversionExpr.transformerInvocation != null) {
            acceptNode(conversionExpr.transformerInvocation);
        }
    }

    /**
     * Accept node to visit.
     *
     * @param node node to be accepted to visit.
     */
    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor) {
            return;
        }
        node.accept(this);
    }

    /**
     * Get the physical source location of the given package.
     *
     * @param bLangNode          ballerina language node references are requested for
     * @param ownerPackageName   list of name compositions of the node's package name
     * @param currentPackageName list of name compositions of the current package
     * @return location of the package of the given node
     */
    private Location getLocation(BLangNode bLangNode, List<Name> ownerPackageName, List<Name> currentPackageName) {
        Location l = new Location();
        Range r = new Range();
        TextDocumentPositionParams position = this.context.get(DocumentServiceKeys.POSITION_KEY);
        Path parentPath = CommonUtil.getPath(position.getTextDocument().getUri()).getParent();
        if (parentPath != null) {
            String fileName = bLangNode.getPosition().getSource().getCompilationUnitName();
            Path filePath = Paths.get(CommonUtil
                    .getPackageURI(currentPackageName, parentPath.toString(), ownerPackageName), fileName);
            l.setUri(filePath.toUri().toString());

            // Subtract 1 to convert the token lines and char positions to zero based indexing
            r.setStart(new Position(bLangNode.getPosition().getStartLine() - 1,
                    bLangNode.getPosition().getStartColumn() - 1));
            r.setEnd(new Position(bLangNode.getPosition().getEndLine() - 1,
                    bLangNode.getPosition().getEndColumn() - 1));
            l.setRange(r);
        }

        return l;
    }

    /**
     * Add location to locations list.
     *
     * @param node            node to calculate the location of
     * @param ownerPkgComps   package of the owner as a list of package paths
     * @param currentPkgComps package of the current node as a list of package paths
     */
    private void addLocation(BLangNode node, List<Name> ownerPkgComps, List<Name> currentPkgComps) {
        this.locations.add(getLocation(node, ownerPkgComps, currentPkgComps));
    }
}

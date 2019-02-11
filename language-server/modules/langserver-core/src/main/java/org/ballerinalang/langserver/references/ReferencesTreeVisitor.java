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

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tree visitor for finding the references of a statement.
 */
public class ReferencesTreeVisitor extends LSNodeVisitor {
    private boolean terminateVisitor = false;
    private LSServiceOperationContext context;
    private List<Location> locations;


    public ReferencesTreeVisitor(LSServiceOperationContext context) {
        this.context = context;
        this.locations = context.get(NodeContextKeys.REFERENCE_RESULTS_KEY);
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes.stream()
                .filter(topLevelNode -> !(topLevelNode instanceof BLangFunction
                        && ((BLangFunction) topLevelNode).flagSet.contains(Flag.LAMBDA))
                        && !(topLevelNode instanceof BLangSimpleVariable
                        && ((BLangSimpleVariable) topLevelNode).flagSet.contains(Flag.SERVICE)))
                .collect(Collectors.toList());
        if (pkgNode.containsTestablePkg()) {
            topLevelNodes.addAll(pkgNode.getTestablePkg().topLevelNodes);
        }
        if (topLevelNodes.isEmpty()) {
            terminateVisitor = true;
            acceptNode(null);
        } else {
            topLevelNodes.stream()
                    .filter(CommonUtil.checkInvalidTypesDefs())
                    .forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
        }
    }

    @Override
    public void visit(BLangFunction funcNode) {
        // Check for native functions
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol) || !CommonUtil.isValidInvokableSymbol(funcSymbol)) {
            return;
        }

        if (isReferenced(funcNode.name.getValue(), funcNode.symbol.owner, funcNode.symbol.pkgID,
                         funcNode.symbol.owner.pkgID)) {
            addLocation(funcNode, funcNode.symbol.pkgID.name.getValue(), funcNode.symbol.pkgID.name.getValue());
        }

        if (funcNode.receiver != null &&
                !funcNode.receiver.getName().getValue().equals("self")) {
            this.acceptNode(funcNode.receiver);
        }

        if (funcNode.requiredParams != null) {
            funcNode.requiredParams.forEach(this::acceptNode);
        }

        if (funcNode.returnTypeNode != null && !(funcNode.returnTypeNode.type instanceof BNilType)) {
            this.acceptNode(funcNode.returnTypeNode);
        }

        if (funcNode.endpoints != null) {
            funcNode.endpoints.forEach(this::acceptNode);
        }

        if (funcNode.body != null) {
            this.acceptNode(funcNode.body);
        }

        if (funcNode.workers != null) {
            funcNode.workers.forEach(this::acceptNode);
        }

        if (funcNode.defaultableParams != null) {
            funcNode.defaultableParams.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        BSymbol servSymbol = serviceNode.symbol;
        if (isReferenced(serviceNode.name.getValue(), servSymbol.owner, servSymbol.pkgID, servSymbol.owner.pkgID)) {
            addLocation(serviceNode.pos, servSymbol.pkgID.name.getValue(), servSymbol.pkgID.name.getValue());
        }

        List<BLangNode> serviceContent = new ArrayList<>();
        BLangObjectTypeNode serviceType = (BLangObjectTypeNode) serviceNode.serviceTypeDefinition.typeNode;
        List<BLangFunction> serviceFunctions = ((BLangObjectTypeNode) serviceNode.serviceTypeDefinition.typeNode)
                .getFunctions();
        List<BLangSimpleVariable> serviceFields = serviceType.getFields().stream()
                .map(simpleVar -> (BLangSimpleVariable) simpleVar)
                .collect(Collectors.toList());
        serviceContent.addAll(serviceFunctions);
        serviceContent.addAll(serviceFields);
        serviceContent.sort(new CommonUtil.BLangNodeComparator());
        serviceContent.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangResource resourceNode) {
        if (isReferenced(resourceNode.name.getValue(), resourceNode.symbol.owner, resourceNode.symbol.pkgID,
                         resourceNode.symbol.owner.pkgID)) {
            CommonUtil.replacePosition(resourceNode.getPosition(), HoverUtil.getIdentifierPosition(resourceNode));
            addLocation(resourceNode.pos, resourceNode.symbol.pkgID.name.getValue(),
                        resourceNode.symbol.pkgID.name.getValue());
        }

        if (resourceNode.requiredParams != null) {
            resourceNode.requiredParams.forEach(this::acceptNode);
        }

        if (resourceNode.body != null) {
            this.acceptNode(resourceNode.body);
        }
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        DiagnosticPos pos = CommonUtil.calculateEndColumnOfGivenName(varNode.getPosition(),
                                                                     varNode.symbol.name.getValue(), "");
        DiagnosticPos identifierPos = HoverUtil.getIdentifierPosition(varNode, pos);
        BVarSymbol varSymbol = varNode.symbol;
        if (!(varNode.type instanceof BUnionType) && isReferenced(varNode.name.getValue(), varSymbol.owner,
                                                                  varSymbol.pkgID, varSymbol.owner.pkgID)) {
            addLocation(identifierPos, varSymbol.owner.pkgID.name.getValue(),
                        varNode.pos.getSource().pkgID.name.getValue());
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

        if (workerNode.workers != null) {
            workerNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        if (blockNode.stmts != null) {
            blockNode.stmts.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        if (varDefNode.getVariable() != null) {
            this.acceptNode(varDefNode.getVariable());
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        if (assignNode.varRef != null) {
            this.acceptNode(assignNode.varRef);
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
        this.acceptNode(foreach.collection);

        acceptNode((BLangNode) foreach.variableDefinitionNode);

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

        if (transactionNode.onRetryBody != null) {
            this.acceptNode(transactionNode.onRetryBody);
        }
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        if (forkJoin.getWorkers() != null) {
            forkJoin.getWorkers().forEach(this::acceptNode);
        }
        // todo need to remove this block
//        if (forkJoin.joinedBody != null) {
//            this.acceptNode(forkJoin.joinedBody);
//        }
//
//        if (forkJoin.timeoutBody != null) {
//            this.acceptNode(forkJoin.timeoutBody);
//        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        DiagnosticPos pos = CommonUtil.calculateEndColumnOfGivenName(varRefExpr.getPosition(),
                                                                     varRefExpr.variableName.value,
                                                                     varRefExpr.pkgAlias.value);
        if (varRefExpr.symbol != null && isReferenced(varRefExpr.variableName.getValue(),
                                                      varRefExpr.symbol.owner,
                                                      varRefExpr.symbol.pkgID,
                                                      varRefExpr.symbol.owner.pkgID)) {
            addLocation(pos, varRefExpr.symbol.owner.pkgID.name.getValue(),
                        varRefExpr.pos.getSource().pkgID.name.getValue());

        } else if (varRefExpr.type.tsymbol != null && isReferenced(varRefExpr.variableName.getValue(),
                                                                   varRefExpr.type.tsymbol.owner,
                                                                   varRefExpr.type.tsymbol.pkgID,
                                                                   varRefExpr.type.tsymbol.owner.pkgID)) {
            addLocation(pos, varRefExpr.type.tsymbol.owner.pkgID.name.getValue(),
                        varRefExpr.pos.getSource().pkgID.name.getValue());
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
        BSymbol inExSymbol = invocationExpr.symbol;
        if (isReferencedRegardlessPkgs(invocationExpr.name.getValue(), inExSymbol.owner)) {
            addLocation(invocationExpr.pos, inExSymbol.owner.pkgID.name.getValue(),
                        invocationExpr.pos.getSource().pkgID.name.getValue());
        }

        if (invocationExpr.expr != null) {
            this.acceptNode(invocationExpr.expr);
        }

        if (invocationExpr.argExprs != null) {
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
        if (returnNode.expr != null) {
            this.acceptNode(returnNode.expr);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldBasedAccess) {
        BSymbol fbaSymbol = fieldBasedAccess.symbol;
        if (isReferenced(fieldBasedAccess.field.getValue(), fbaSymbol.owner, fbaSymbol.pkgID, fbaSymbol.owner.pkgID)) {
            addLocation(fieldBasedAccess.pos, fbaSymbol.owner.pkgID.name.getValue(),
                        fieldBasedAccess.pos.getSource().pkgID.name.getValue());
        }
        if (fieldBasedAccess.expr != null) {
            this.acceptNode(fieldBasedAccess.expr);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        if (userDefinedType.getPosition() != null) {
            DiagnosticPos pos = CommonUtil.calculateEndColumnOfGivenName(userDefinedType.getPosition(),
                                                                                   userDefinedType.typeName.value,
                                                                                   userDefinedType.pkgAlias.value);
            BType udType = userDefinedType.type;
            if (udType.tsymbol != null && isReferencedRegardlessPkgs(userDefinedType.typeName.getValue(),
                                                                                   udType.tsymbol.owner)) {
                addLocation(pos, udType.tsymbol.owner.pkgID.name.getValue(),
                            userDefinedType.pos.getSource().pkgID.name.getValue());
            } else if (udType instanceof BUnionType) {
                try {
                    BUnionType bUnionType = (BUnionType) udType;
                    for (BType type : bUnionType.memberTypes) {
                        if (type.tsymbol != null && isReferencedRegardlessPkgs(type.tsymbol.name.getValue(),
                                                                               type.tsymbol.owner)) {
                            addLocation(pos, type.tsymbol.owner.pkgID.name.getValue(),
                                        userDefinedType.pos.getSource().pkgID.name.getValue());
                            break;
                        }
                    }
                } catch (ClassCastException e) {
                    // Ignores
                }
            }
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
    public void visit(BLangTypeConversionExpr conversionExpr) {
        if (conversionExpr.expr != null) {
            acceptNode(conversionExpr.expr);
        }

        if (conversionExpr.typeNode != null) {
            acceptNode(conversionExpr.typeNode);
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        if (ternaryExpr.expr != null) {
            this.acceptNode(ternaryExpr.expr);
        }

        if (ternaryExpr.thenExpr != null) {
            this.acceptNode(ternaryExpr.thenExpr);
        }

        if (ternaryExpr.elseExpr != null) {
            this.acceptNode(ternaryExpr.elseExpr);
        }
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        if (unionTypeNode.memberTypeNodes != null) {
            unionTypeNode.memberTypeNodes.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        if (tupleTypeNode.memberTypeNodes != null) {
            tupleTypeNode.memberTypeNodes.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        if (bracedOrTupleExpr.expressions != null) {
            bracedOrTupleExpr.expressions.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        if (stmt.varRef.expressions != null) {
            stmt.varRef.expressions.forEach(this::acceptNode);
        }

        if (stmt.expr != null) {
            this.acceptNode(stmt.expr);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        BSymbol objectSymbol = objectTypeNode.symbol;
        if (isReferenced(objectSymbol.name.getValue(), objectSymbol.owner, objectSymbol.pkgID,
                         objectSymbol.owner.pkgID)) {
            addLocation(objectTypeNode.pos, objectSymbol.owner.pkgID.name.getValue(),
                        objectTypeNode.pos.getSource().pkgID.name.getValue());
        }

        if (objectTypeNode.fields != null) {
            objectTypeNode.fields.forEach(this::acceptNode);
        }

        if (objectTypeNode.functions != null) {
            objectTypeNode.functions.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        if (matchNode.expr != null) {
            this.acceptNode(matchNode.expr);
        }

        if (matchNode.patternClauses != null) {
            matchNode.patternClauses.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause patternClauseNode) {
        /*ignore*/
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        if (typeInitExpr.userDefinedType != null) {
            acceptNode(typeInitExpr.userDefinedType);
        }

        if (typeInitExpr.argsExpr != null) {
            typeInitExpr.argsExpr.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        if (recordTypeNode.fields != null) {
            recordTypeNode.fields.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        if (recordLiteral.keyValuePairs != null) {
            recordLiteral.keyValuePairs.forEach((bLangRecordKeyValue -> {
                if (bLangRecordKeyValue.valueExpr != null) {
                    this.acceptNode(bLangRecordKeyValue.valueExpr);
                }
            }));
        }

        if (recordLiteral.impConversionExpr != null) {
            this.acceptNode(recordLiteral.impConversionExpr);
        }
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        if (bLangMatchExpression.impConversionExpr != null) {
            this.acceptNode(bLangMatchExpression.impConversionExpr);
        }

        if (bLangMatchExpression.expr != null) {
            this.acceptNode(bLangMatchExpression.expr);
        }

        if (bLangMatchExpression.patternClauses != null) {
            bLangMatchExpression.patternClauses.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        if (bLangMatchExprPatternClause.variable != null) {
            this.acceptNode(bLangMatchExprPatternClause.variable);
        }

        if (bLangMatchExprPatternClause.expr != null) {
            this.acceptNode(bLangMatchExprPatternClause.expr);
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BTypeSymbol typeSymbol = typeDefinition.symbol;
        if (isReferenced(typeDefinition.name.value, typeSymbol.owner, typeSymbol.pkgID, typeSymbol.owner.pkgID)) {
            addLocation(typeDefinition.pos, typeSymbol.owner.pkgID.name.getValue(),
                        typeDefinition.pos.getSource().pkgID.name.getValue());
        }

        if (typeDefinition.typeNode != null) {
            this.acceptNode(typeDefinition.typeNode);
        }

//        if (typeDefinition.valueSpace != null) {
//            typeDefinition.valueSpace.forEach(this::acceptNode);
//        }

    }

    @Override
    public void visit(BLangConstant constant) {
        BConstantSymbol constSymbol = constant.symbol;
        if (isReferenced(constant.name.value, constSymbol.owner, constSymbol.pkgID, constSymbol.owner.pkgID)) {
            addLocation(constant.pos, constSymbol.owner.pkgID.name.getValue(),
                        constant.pos.getSource().pkgID.name.getValue());
        }

        if (constant.typeNode != null) {
            this.acceptNode(constant.typeNode);
        }
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        if (checkedExpr.expr != null) {
            this.acceptNode(checkedExpr.expr);
        }
    }

    @Override
    public void visit(BLangIndexBasedAccess indexBasedAccess) {
        if (indexBasedAccess.expr != null) {
            this.acceptNode(indexBasedAccess.expr);
        }
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (unaryExpr.expr != null) {
            this.acceptNode(unaryExpr.expr);
        }
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        this.acceptNode(compoundAssignNode.varRef);
        if (compoundAssignNode.expr != null) {
            this.acceptNode(compoundAssignNode.expr);
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
        DiagnosticPos position = node.getPosition();
        if (position != null) {
            node.pos = new DiagnosticPos(position.src, position.sLine, position.eLine, position.sCol, position.eCol);
        }
        node.accept(this);
    }

    /**
     * Get the physical source location of the given package.
     *
     * @param pos                diagnostic position
     * @param ownerPackageName   list of name compositions of the node's package name
     * @param currentPackageName list of name compositions of the current package
     * @return location of the package of the given node
     */
    private Location getLocation(DiagnosticPos pos, String ownerPackageName, String currentPackageName) {
        Location l = new Location();
        Range r = new Range();
        TextDocumentPositionParams position = this.context.get(DocumentServiceKeys.POSITION_KEY);
        String parentPath = new LSDocument(position.getTextDocument().getUri()).getSourceRoot();
        if (parentPath != null) {
            String fileName = pos.getSource().getCompilationUnitName();
            Path filePath = Paths.get(CommonUtil.getPackageURI(currentPackageName, parentPath, ownerPackageName),
                                      fileName);
            l.setUri(filePath.toUri().toString());

            // Subtract 1 to convert the token lines and char positions to zero based indexing
            r.setStart(new Position(pos.getStartLine() - 1, pos.getStartColumn() - 1));
            r.setEnd(new Position(pos.getEndLine() - 1, pos.getEndColumn() - 1));
            l.setRange(r);
        }

        return l;
    }

    /**
     * Add location to locations list.
     *
     * @param pos       node position
     * @param ownerPkg   package of the owner
     * @param currentPkg package of the current node as a list of package paths
     */
    private void addLocation(DiagnosticPos pos, String ownerPkg, String currentPkg) {
        this.locations.add(getLocation(pos, ownerPkg, currentPkg));
    }

    /**
     * Add function node location to locations list.
     *
     * @param node       function node to calculate the location of
     * @param ownerPkg   package of the owner
     * @param currentPkg package of the current node as a list of package paths
     */
    private void addLocation(BLangFunction node, String ownerPkg, String currentPkg) {
        DiagnosticPos oldPos = node.getPosition();
        DiagnosticPos position = new DiagnosticPos(oldPos.src, oldPos.sLine, oldPos.eLine, oldPos.sCol, oldPos.eCol);

        Set<Whitespace> wsSet = node.getWS();
        if (wsSet != null && wsSet.size() > 4 && !node.annAttachments.isEmpty()) {
            Whitespace[] wsArray = new Whitespace[wsSet.size()];
            wsSet.toArray(wsArray);
            Arrays.sort(wsArray);
            int lastAnnotationEndline = CommonUtil.getLastItem(node.annAttachments).pos.eLine;
            position.sLine = lastAnnotationEndline +
                    wsArray[0].getWs().split(CommonUtil.LINE_SEPARATOR_SPLIT).length - 1;
        }

        this.locations.add(getLocation(position, ownerPkg, currentPkg));
    }

    /**
     * Returns true if the node is referenced.
     *
     * @param name name of the node
     * @param owner owner of the node
     * @return true if the node is referenced
     */
    private boolean isReferencedRegardlessPkgs(String name, BSymbol owner) {
        return isReferenced(name, owner, null, null);
    }

    /**
     * Returns true if the node is referenced.
     *
     * @param name name of the node
     * @param owner owner of the node
     * @param pkgID package id of the node
     * @param ownerPkgId package id of the owner of the node
     * @return true if the node is referenced
     */
    private boolean isReferenced(String name, BSymbol owner, PackageID pkgID, PackageID ownerPkgId) {
        if (this.context.get(NodeContextKeys.NAME_OF_NODE_KEY) == null ||
                this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY) == null ||
                this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY) == null) {
            return false;
        }
        boolean isSameVar = this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY) != null &&
                this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY).equals(name);
        boolean isSameNode = this.context.get(NodeContextKeys.NAME_OF_NODE_KEY).equals(name);
        boolean isSameOwner = (owner == null || this.context.get(NodeContextKeys.NODE_OWNER_KEY).equals(
                owner.name.getValue()));
        boolean isSameOwnerPkg = ownerPkgId == null ||
                this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY).name.getValue().
                        equals(ownerPkgId.name.getValue());
        boolean isSamePkg = pkgID == null ||
                this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue().equals(pkgID.name.getValue());
        List<PackageID> packageIds = this.context.get(NodeContextKeys.REFERENCE_PKG_IDS_KEY);
        if (!isSamePkg && (isSameNode || isSameVar) && isSameOwner && isSameOwnerPkg && packageIds != null) {
            return packageIds.stream().anyMatch(pkgID::equals);
        }
        return isSamePkg && (isSameNode || isSameVar) && isSameOwner && isSameOwnerPkg;
    }
}

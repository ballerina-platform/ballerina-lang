package io.ballerina.compiler.api.impl;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SymbolFilter extends BaseVisitor {

    private List<BSymbol> outOfScopeSymbols = new ArrayList<>();
    private LinePosition linePosition = null;

    public List<BSymbol> getOutOfScopeSymbols(BLangCompilationUnit compilationUnit, LinePosition linePosition) {
        this.linePosition = linePosition;
        this.outOfScopeSymbols = new ArrayList<>();
        for (TopLevelNode node : compilationUnit.topLevelNodes) {
            if (!PositionUtil.withinRightInclusive(linePosition, node.getPosition())) {
                continue;
            }

            ((BLangNode) node).accept(this);
        }

        return Collections.unmodifiableList(this.outOfScopeSymbols);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        lookupNode(funcNode.body);
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        lookupNodes(blockFuncBody.stmts);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        lookupNode(varDefNode.var);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        setEnclosingSymbol(varNode.symbol, varNode.getPosition());
        lookupNode(varNode.expr);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        lookupNodes(queryExpr.queryClauseList);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        lookupNodes(classDefinition.fields);
        lookupNodes(classDefinition.referencedFields);
        lookupNode(classDefinition.initFunction);
        lookupNodes(classDefinition.functions);
        lookupNodes(classDefinition.typeRefs);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        lookupNodes(recordTypeNode.fields);
        lookupNodes(recordTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        lookupNodes(objectTypeNode.fields);
        lookupNodes(objectTypeNode.functions);
        lookupNodes(objectTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangService serviceNode) {
        lookupNode(serviceNode.serviceClass);
        lookupNodes(serviceNode.attachedExprs);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable letVarDeclaration : letClause.letVarDeclarations) {
            lookupNode((BLangNode) letVarDeclaration.definitionNode);
        }
    }

    @Override
    public void visit(BLangConstant constant) {
        setEnclosingSymbol(constant.symbol, constant.getPosition());
    }

    @Override
    public void visit(BLangLetExpression letExpr) {

    }


    @Override
    public void visit(BLangAssignment assignNode) {
        if (PositionUtil.withinRightInclusive(linePosition, assignNode.getPosition())) {
            lookupNode(assignNode.varRef);
        }
    }

    private void setEnclosingSymbol(BSymbol symbol, Location pos) {
        if (PositionUtil.withinRightInclusive(linePosition, pos)) {
            outOfScopeSymbols.add(symbol);
        }
    }

    private void lookupNodes(List<? extends BLangNode> nodes) {
        for (BLangNode node : nodes) {
            lookupNode(node);
        }
    }

    private void lookupNode(BLangNode node) {
        if (node == null || node.getPosition() == null) {
            return;
        }

        if (PositionUtil.withinRightInclusive(linePosition, node.getPosition())) {
            node.accept(this);
        }
    }


}

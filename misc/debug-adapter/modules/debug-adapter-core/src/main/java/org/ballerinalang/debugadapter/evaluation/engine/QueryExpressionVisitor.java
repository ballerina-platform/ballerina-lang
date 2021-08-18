package org.ballerinalang.debugadapter.evaluation.engine;

import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.JoinClauseNode;
import io.ballerina.compiler.syntax.tree.LetClauseNode;
import io.ballerina.compiler.syntax.tree.LimitClauseNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.OrderByClauseNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.WhereClauseNode;

import java.util.ArrayList;
import java.util.List;

public class QueryExpressionVisitor extends NodeVisitor {

    private boolean isWithinLetClause = false;
    private final QueryExpressionNode queryExpressionNode;
    private final List<String> internalVariables = new ArrayList<>();
    private final List<String> letVariables = new ArrayList<>();
    private final List<String> capturedVariables = new ArrayList<>();

    public QueryExpressionVisitor(QueryExpressionNode node) {
        this.queryExpressionNode = node;
    }

    public List<String> getCapturedVariables() {
        queryExpressionNode.accept(this);
        return capturedVariables;
    }

    @Override
    public void visit(FromClauseNode fromClauseNode) {
        BindingPatternNode bindingPattern = fromClauseNode.typedBindingPattern().bindingPattern();
        internalVariables.addAll(extractVariablesFromBindingPattern(bindingPattern));
        fromClauseNode.expression().accept(this);
    }

    @Override
    public void visit(WhereClauseNode whereClauseNode) {
        whereClauseNode.expression().accept(this);
    }

    @Override
    public void visit(JoinClauseNode joinClauseNode) {
        joinClauseNode.expression().accept(this);
    }

    @Override
    public void visit(LetClauseNode letClauseNode) {
        letClauseNode.letVarDeclarations().forEach(declarationNode -> {
            BindingPatternNode bindingPattern = declarationNode.typedBindingPattern().bindingPattern();
            letVariables.addAll(extractVariablesFromBindingPattern(bindingPattern));
        });

        isWithinLetClause = true;
        letClauseNode.letVarDeclarations().forEach(declarationNode -> declarationNode.expression().accept(this));
        isWithinLetClause = false;
    }

    @Override
    public void visit(LimitClauseNode limitClauseNode) {
        limitClauseNode.expression().accept(this);
    }

    @Override
    public void visit(OrderByClauseNode orderByClauseNode) {
    }

    @Override
    public void visit(SelectClauseNode selectClauseNode) {
        selectClauseNode.expression().accept(this);
    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        String variableRef = simpleNameReferenceNode.name().text().trim();
        if (isWithinLetClause && !internalVariables.contains(variableRef) && !letVariables.contains(variableRef)) {
            if (!internalVariables.contains(variableRef) && !letVariables.contains(variableRef)) {
                capturedVariables.add(variableRef);
            }
        } else if (!internalVariables.contains(variableRef)) {
            capturedVariables.add(variableRef);
        }
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        super.visitSyntaxNode(node);
    }

    private List<String> extractVariablesFromBindingPattern(BindingPatternNode bindingPattern) {
        List<String> capturedVariableNames = new ArrayList<>();
        if (bindingPattern instanceof CaptureBindingPatternNode) {
            capturedVariableNames.add(((CaptureBindingPatternNode) bindingPattern).variableName().text().trim());
        }
        return capturedVariableNames;
    }
}

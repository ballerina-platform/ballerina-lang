package io.ballerina.shell.invoker.classload;

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.TreeModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeRewriter extends TreeModifier {

    private final HashMap<String, String> symbolTypes;
    private final Map<String, String> variableReplaceMap;

    // Keep track of required global vars to recall
    private final List<String> globalVars = new ArrayList<>();

    // Necessary recall call nodes
    private final List<Node> additionalNodes = new ArrayList<>();

    // Global variable reference - contains list of startOffsets related to global variables
    private final Map<String, List<Integer>> globalVarRefs;

    public NodeRewriter(Map<String, List<Integer>> refs , HashMap<String, String> symbolTypes) {
        this.symbolTypes = symbolTypes;
        this.globalVarRefs = refs;
        this.variableReplaceMap = new HashMap<>();
    }

    public Node apply(Node node) {
        return node.apply(this);
    }

    @Override
    public ModulePartNode transform(ModulePartNode modulePartNode) {
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        for (int i= 0; i < members.size(); i++) {
            if (members.get(i) instanceof FunctionDefinitionNode) {
                // TODO create a function list to filter by function names
                if (((FunctionDefinitionNode) members.get(i)).functionName().toString().equals("__stmts")) {
                    members = members.set(i,(ModuleMemberDeclarationNode) members.get(i).apply(this));
                } else continue;
            } else {
                additionalNodes.add(members.get(i));
                members = members.set(i,(ModuleMemberDeclarationNode) members.get(i).apply(this));
            }
        }
        return modulePartNode.modify().withMembers(members).apply();
    }

    @Override
    public ModuleVariableDeclarationNode transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        // handle initializer which is optional
        if (moduleVariableDeclarationNode.initializer().isPresent()) {
            ExpressionNode initializer = (ExpressionNode) moduleVariableDeclarationNode.initializer().get().apply(this);
            return moduleVariableDeclarationNode.modify().withInitializer(initializer).apply();
        }
        return moduleVariableDeclarationNode.modify().apply();
    }

    @Override
    public FunctionDefinitionNode transform(FunctionDefinitionNode functionDefinitionNode) {
        FunctionBodyNode functionBody = (FunctionBodyNode) functionDefinitionNode.functionBody().apply(this);
        return functionDefinitionNode.modify().withFunctionBody(functionBody).apply();
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {
        //TODO handle workers
        globalVars.clear();
        NodeList<StatementNode> replacementList = NodeFactory.createEmptyNodeList();
        NodeList<StatementNode> statementsList = functionBodyBlockNode.statements();
        for (int i = 0; i < statementsList.size(); i++) {
            StatementNode replacementNode = (StatementNode) statementsList.get(i).apply(this);
            for (String variable : globalVars) {
                replacementList = replacementList.add(NodeParser.parseStatements(variable + " = " + "<" +
                        symbolTypes.get("'" +variable) + "> " +
                        String.format("__recall_any(\"%s\")",variable) + ";").get(0));
            }
            replacementList = replacementList.add(replacementNode);
            globalVars.clear();
        }
        return functionBodyBlockNode.modify().withStatements(replacementList).apply();
    }

    @Override
    public DoStatementNode transform(DoStatementNode doStatementNode) {
        //TODO handle on fail clause
        BlockStatementNode blockStatementNode = doStatementNode.blockStatement();
        BlockStatementNode newBlockStatementNode = (BlockStatementNode) blockStatementNode.apply(this);
        return doStatementNode.modify().withBlockStatement(newBlockStatementNode).apply();
    }

    @Override
    public BlockStatementNode transform(BlockStatementNode blockStatementNode) {
        //TODO handle as functionBodyNode
        NodeList<StatementNode> statementsList = blockStatementNode.statements();
        globalVars.clear();
        for (int i = 0; i < statementsList.size(); i++) {
            statementsList = statementsList.set(i,(StatementNode) statementsList.get(i).apply(this));
            globalVars.size();
        }

        return blockStatementNode.modify().withStatements(statementsList).apply();
    }

    @Override
    public AssignmentStatementNode transform(AssignmentStatementNode assignmentStatementNode) {
        String varRef = assignmentStatementNode.varRef().toString().trim();
        // handle __last__ as a separate case
        if (varRef.equals("__last__")){
            ExpressionNode expression = (ExpressionNode) assignmentStatementNode.expression().apply(this);
            return assignmentStatementNode.modify().withExpression(expression).apply();
        }
        ExpressionNode expression = (ExpressionNode) assignmentStatementNode.expression().apply(this);
        ExpressionNode newFunctionCallExpressionNode = NodeParser.parseExpression(
                String.format(" __memorize(\"%s\", %s)",
                        varRef, expression.toString()));
        Node newVarRef = NodeParser.parseExpression("_");
        return assignmentStatementNode.modify().withVarRef(newVarRef).
                withExpression(newFunctionCallExpressionNode).apply();
    }

    @Override
    public BinaryExpressionNode transform(BinaryExpressionNode binaryExpressionNode) {
        Node nodeLhs = binaryExpressionNode.lhsExpr().apply(this);
        Node nodeRhs = binaryExpressionNode.rhsExpr().apply(this);
        return binaryExpressionNode.modify().withLhsExpr(nodeLhs).withRhsExpr(nodeRhs).apply();
    }

    @Override
    public BasicLiteralNode transform(BasicLiteralNode basicLiteralNode) {
        return basicLiteralNode.modify().apply();
    }

    @Override
    public BracedExpressionNode transform(BracedExpressionNode bracedExpressionNode) {
        ExpressionNode expressionNode = (ExpressionNode) bracedExpressionNode.expression().apply(this);
        return bracedExpressionNode.modify().withExpression(expressionNode).apply();
    }

    @Override
    public FunctionCallExpressionNode transform(FunctionCallExpressionNode functionCallExpressionNode) {
        return functionCallExpressionNode.modify().apply();
    }

    @Override
    public SimpleNameReferenceNode transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        String nameReference = simpleNameReferenceNode.name().toString().trim();
        if (globalVarRefs.containsKey(nameReference)) {
            if (globalVarRefs.get(nameReference).contains(simpleNameReferenceNode.position())){
                globalVars.add(nameReference);
            }
        } else if (globalVarRefs.containsKey("'" + nameReference)) {
            // check this if statement
            if (globalVarRefs.get("'" + nameReference).contains(simpleNameReferenceNode.position())) {
                //TODO quoted is not included
                globalVars.add(nameReference);
            }
        }
        return simpleNameReferenceNode.modify().apply();
    }

    @Override
    public CompoundAssignmentStatementNode transform(CompoundAssignmentStatementNode compoundAssignmentStatementNode) {
        String lhs = compoundAssignmentStatementNode.lhsExpression().toString().trim();
        String rhs = compoundAssignmentStatementNode.rhsExpression().toString().trim();
        String binaryOperator = compoundAssignmentStatementNode.binaryOperator().toString();
        StatementNode node = NodeParser.parseStatements(
                String.format(" _ = __memorize(\"%s\", %s %s %s);", lhs, lhs, binaryOperator, rhs)).get(0);
        return compoundAssignmentStatementNode.modify().apply();
    }

    @Override
    public ReturnStatementNode transform(ReturnStatementNode returnStatementNode) {
        String type;
        if (symbolTypes.get(returnStatementNode.expression().get().toString()) != null) {
            type = "<" + symbolTypes.get(returnStatementNode.expression().get().toString()) + ">";
        } else {
            type = "<" + symbolTypes.get("'" + returnStatementNode.expression().get()) + ">";
        }

        ExpressionNode expressionNode = NodeParser.parseExpression(type + " __recall_any(\"" +
                returnStatementNode.expression().get() + "\")");
        return returnStatementNode.modify().withExpression(expressionNode).apply();
    }

    @Override
    public ExplicitAnonymousFunctionExpressionNode transform
            (ExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        FunctionBodyNode functionBodyNode =
                (FunctionBodyNode) explicitAnonymousFunctionExpressionNode.functionBody().apply(this);
        return explicitAnonymousFunctionExpressionNode.modify().withFunctionBody(functionBodyNode).apply();
    }
}

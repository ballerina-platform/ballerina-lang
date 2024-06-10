/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.snippet.factory;

import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BreakStatementNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ContinueStatementNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FailStatementNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.ForkStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.LocalTypeDefinitionStatementNode;
import io.ballerina.compiler.syntax.tree.LockStatementNode;
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.PanicStatementNode;
import io.ballerina.compiler.syntax.tree.RetryStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.RollbackStatementNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TransactionStatementNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.compiler.syntax.tree.XMLNamespaceDeclarationNode;
import io.ballerina.shell.exceptions.SnippetException;
import io.ballerina.shell.parser.ParserConstants;
import io.ballerina.shell.snippet.SnippetSubKind;
import io.ballerina.shell.snippet.types.ExpressionSnippet;
import io.ballerina.shell.snippet.types.ImportDeclarationSnippet;
import io.ballerina.shell.snippet.types.ModuleMemberDeclarationSnippet;
import io.ballerina.shell.snippet.types.StatementSnippet;
import io.ballerina.shell.snippet.types.VariableDeclarationSnippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A factory that will create snippets from given nodes.
 *
 * @since 2.0.0
 */
public class BasicSnippetFactory extends SnippetFactory {
    // Create a caches of Syntax kind -> Variable types/Sub snippets that are known.
    // These will be used when identifying the variable type/snippet type.
    private int varFunctionCount = 0;
    protected static final Map<Class<?>, SnippetSubKind> MODULE_MEM_DCLNS = Map.ofEntries(
            Map.entry(FunctionDefinitionNode.class, SnippetSubKind.FUNCTION_DEFINITION),
            Map.entry(ListenerDeclarationNode.class, SnippetSubKind.LISTENER_DECLARATION),
            Map.entry(TypeDefinitionNode.class, SnippetSubKind.TYPE_DEFINITION),
            Map.entry(ServiceDeclarationNode.class, SnippetSubKind.SERVICE_DECLARATION), // Error
            Map.entry(ConstantDeclarationNode.class, SnippetSubKind.CONSTANT_DECLARATION),
            Map.entry(ModuleVariableDeclarationNode.class, SnippetSubKind.MODULE_VARIABLE_DECLARATION), // Ignored
            Map.entry(AnnotationDeclarationNode.class, SnippetSubKind.ANNOTATION_DECLARATION),
            Map.entry(ModuleXMLNamespaceDeclarationNode.class, SnippetSubKind.MODULE_XML_NAMESPACE_DECLARATION),
            Map.entry(EnumDeclarationNode.class, SnippetSubKind.ENUM_DECLARATION),
            Map.entry(ClassDefinitionNode.class, SnippetSubKind.CLASS_DEFINITION)
    );
    protected static final Map<Class<?>, SnippetSubKind> STATEMENTS = Map.ofEntries(
            Map.entry(AssignmentStatementNode.class, SnippetSubKind.ASSIGNMENT_STATEMENT),
            Map.entry(CompoundAssignmentStatementNode.class, SnippetSubKind.COMPOUND_ASSIGNMENT_STATEMENT),
            Map.entry(VariableDeclarationNode.class, SnippetSubKind.VARIABLE_DECLARATION_STATEMENT), // Ignore
            Map.entry(BlockStatementNode.class, SnippetSubKind.BLOCK_STATEMENT),
            Map.entry(BreakStatementNode.class, SnippetSubKind.BREAK_STATEMENT), // Error
            Map.entry(FailStatementNode.class, SnippetSubKind.FAIL_STATEMENT), // Error
            Map.entry(ExpressionStatementNode.class, SnippetSubKind.EXPRESSION_STATEMENT), // Ignore
            Map.entry(ContinueStatementNode.class, SnippetSubKind.CONTINUE_STATEMENT), // Error
            Map.entry(IfElseStatementNode.class, SnippetSubKind.IF_ELSE_STATEMENT),
            Map.entry(WhileStatementNode.class, SnippetSubKind.WHILE_STATEMENT),
            Map.entry(PanicStatementNode.class, SnippetSubKind.PANIC_STATEMENT),
            Map.entry(ReturnStatementNode.class, SnippetSubKind.RETURN_STATEMENT), // Error
            Map.entry(LocalTypeDefinitionStatementNode.class, SnippetSubKind.LOCAL_TYPE_DEFINITION_STATEMENT), // Ignore
            Map.entry(LockStatementNode.class, SnippetSubKind.LOCK_STATEMENT),
            Map.entry(ForkStatementNode.class, SnippetSubKind.FORK_STATEMENT),
            Map.entry(ForEachStatementNode.class, SnippetSubKind.FOR_EACH_STATEMENT),
            Map.entry(XMLNamespaceDeclarationNode.class, SnippetSubKind.XML_NAMESPACE_DECLARATION_STATEMENT), // Ignore
            Map.entry(TransactionStatementNode.class, SnippetSubKind.TRANSACTION_STATEMENT),
            Map.entry(RollbackStatementNode.class, SnippetSubKind.ROLLBACK_STATEMENT), // Error
            Map.entry(RetryStatementNode.class, SnippetSubKind.RETRY_STATEMENT),
            Map.entry(MatchStatementNode.class, SnippetSubKind.MATCH_STATEMENT),
            Map.entry(DoStatementNode.class, SnippetSubKind.DO_STATEMENT)
    );

    @Override
    public ImportDeclarationSnippet createImportSnippet(Node node) {
        if (node instanceof ImportDeclarationNode importDeclarationNode) {
            return new ImportDeclarationSnippet(importDeclarationNode);
        }
        return null;
    }

    @Override
    public List<VariableDeclarationSnippet> createVariableDeclarationSnippets(Node node) {
        ModuleVariableDeclarationNode dclnNode;
        ModuleVariableDeclarationNode newDclnNode = null;
        List<VariableDeclarationSnippet> snippets = new ArrayList<>();
        if (containsIsolated(node)) {
            addErrorDiagnostic("Isolation not allowed in the Ballerina shell");
            return null;
        }

        if (node instanceof ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
            dclnNode = moduleVariableDeclarationNode;
        } else if (node instanceof VariableDeclarationNode varNode) {
            VariableDeclarationNode newNode = null;
            NodeList<Token> qualifiers = NodeFactory.createEmptyNodeList();
            // Only final qualifier is transferred.
            // It is the only possible qualifier that can be transferred.
            if (varNode.finalKeyword().isPresent()) {
                qualifiers = NodeFactory.createNodeList(varNode.finalKeyword().get());
            }

            Optional<ExpressionNode> optVarInitNode = varNode.initializer();
            if (optVarInitNode.isEmpty()) {
                assert false : "This line is unreachable as the error is captured before.";
                addErrorDiagnostic("Variable declaration without an initializer is not allowed");
                return null;
            }

            ExpressionNode varInitNode = optVarInitNode.get();
            SyntaxKind nodeKind = varInitNode.kind();
            if (isSupportedAction(nodeKind)) {
                TypedBindingPatternNode typedBindingPatternNode = varNode.typedBindingPattern();
                TypeDescriptorNode typeDescriptorNode = typedBindingPatternNode.typeDescriptor();
                BindingPatternNode bindingPatternNode = typedBindingPatternNode.bindingPattern();

                // Check if the type descriptor of the variable is 'var' as it is not yet supported.
                if (typeDescriptorNode.kind() == SyntaxKind.VAR_TYPE_DESC) {
                    addErrorDiagnostic("'var' type is not yet supported for actions. Please specify the exact type.");
                    return null;
                }

                boolean isCheckAction = nodeKind == SyntaxKind.CHECK_ACTION;
                String initAction = varInitNode.toSourceCode();
                String functionTypeDesc = (isCheckAction ? "function() returns error|" :
                        "function() returns ") + typeDescriptorNode;
                String functionName = ParserConstants.WRAPPER_PREFIX + varFunctionCount;
                String functionVarDecl = String.format("%s %s = %s {%s %s = %s; return %s;};", functionTypeDesc,
                        functionName, functionTypeDesc, typeDescriptorNode, bindingPatternNode, initAction,
                        bindingPatternNode);
                varNode = (VariableDeclarationNode) NodeParser.parseStatement(functionVarDecl);
                newNode = (VariableDeclarationNode) NodeParser.parseStatement(
                        String.format("%s %s = %s %s();", typeDescriptorNode, bindingPatternNode,
                                (isCheckAction ? "check " : ""), functionName));
            }

            varFunctionCount += 1;
            dclnNode = NodeFactory.createModuleVariableDeclarationNode(
                    NodeFactory.createMetadataNode(null, varNode.annotations()), null,
                    qualifiers, varNode.typedBindingPattern(),
                    varNode.equalsToken().orElse(null), varNode.initializer().orElse(null),
                    varNode.semicolonToken()
            );
            if (newNode != null) {
                newDclnNode = NodeFactory.createModuleVariableDeclarationNode(
                        NodeFactory.createMetadataNode(null, newNode.annotations()), null,
                        qualifiers, newNode.typedBindingPattern(),
                        newNode.equalsToken().orElse(null), newNode.initializer().orElse(null),
                        newNode.semicolonToken()
                );
            }
        } else {
            return null;
        }

        if (dclnNode.initializer().isEmpty()) {
            addErrorDiagnostic("" +
                    "Variables without initializers are not permitted. " +
                    "Give an initial value for your variable.");
            return null;
        }

        snippets.add(new VariableDeclarationSnippet(dclnNode));
        if (newDclnNode != null) {
            snippets.add(new VariableDeclarationSnippet(newDclnNode));
        }

        return snippets;
    }

    @Override
    public ModuleMemberDeclarationSnippet createModuleMemberDeclarationSnippet(Node node)
            throws SnippetException {
        if (containsIsolated(node)) {
            return null;
        }

        if (node instanceof ModuleMemberDeclarationNode moduleMemberDeclarationNode) {
            assert MODULE_MEM_DCLNS.containsKey(node.getClass());
            SnippetSubKind subKind = MODULE_MEM_DCLNS.get(node.getClass());
            if (subKind.hasError()) {
                addErrorDiagnostic(subKind.getError());
                throw new SnippetException();
            } else if (subKind.isValid()) {
                return new ModuleMemberDeclarationSnippet(subKind, moduleMemberDeclarationNode);
            }
        }
        return null;
    }

    @Override
    public StatementSnippet createStatementSnippet(Node node) throws SnippetException {
        if (node instanceof StatementNode) {
            assert STATEMENTS.containsKey(node.getClass());
            SnippetSubKind subKind = STATEMENTS.get(node.getClass());
            if (subKind.hasError()) {
                addErrorDiagnostic(subKind.getError());
                throw new SnippetException();
            } else if (subKind.isValid()) {
                return new StatementSnippet(subKind, (StatementNode) node);
            }
        } else if (node instanceof NamedWorkerDeclarator) {
            addErrorDiagnostic("" +
                    "Named worker declarators cannot be used in the REPL as top level statement.\n" +
                    "Please use them in a suitable place such as in a function.");
        }
        return null;
    }

    @Override
    public ExpressionSnippet createExpressionSnippet(Node node) {
        if (node instanceof ExpressionNode expressionNode) {
            return new ExpressionSnippet(expressionNode);
        }
        return null;
    }

    /**
     * Check the input node contains isolated keyword.
     *
     * @param node input Node.
     * @return node contains isolated keyword or not.
     */
    private boolean containsIsolated(Node node) {
        if (node instanceof ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
            NodeList<Token> nodeList = moduleVariableDeclarationNode.qualifiers();
             return nodeList.stream().anyMatch(token -> token.kind() == SyntaxKind.ISOLATED_KEYWORD);
        } else if (node instanceof FunctionDefinitionNode functionDefinitionNode) {
            NodeList<Token> nodeList = functionDefinitionNode.qualifierList();
            return nodeList.stream().anyMatch(token -> token.kind() == SyntaxKind.ISOLATED_KEYWORD);
        }

        return false;
    }

    private boolean isSupportedAction(SyntaxKind nodeKind) {
        switch (nodeKind) {
            case REMOTE_METHOD_CALL_ACTION:
            case BRACED_ACTION:
            case CHECK_ACTION:
            case START_ACTION:
            case TRAP_ACTION:
            case FLUSH_ACTION:
            case ASYNC_SEND_ACTION:
            case SYNC_SEND_ACTION:
            case RECEIVE_ACTION:
            case WAIT_ACTION:
            case QUERY_ACTION:
            case COMMIT_ACTION:
            case CLIENT_RESOURCE_ACCESS_ACTION:
                return true;
            default:
                return false;
        }
    }
}

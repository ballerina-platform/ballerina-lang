/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.command.visitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FailStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.GroupingKeyVarDeclarationNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.PanicStatementNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;

import java.util.List;
import java.util.Optional;

/**
 * Finds the expected {@link TypeSymbol} of a provided {@link FunctionCallExpressionNode} node. This will try its
 * best to return a valid {@link TypeSymbol}. Else, it will try to find the {@link TypeDescKind} of the expected return
 * type.
 *
 * @since 2.0.0
 */
public class FunctionCallExpressionTypeFinder extends NodeVisitor {

    private final SemanticModel semanticModel;
    private FunctionCallExpressionNode functionCallExpr;
    private TypeSymbol returnTypeSymbol;
    private boolean resultFound = false;

    public FunctionCallExpressionTypeFinder(SemanticModel semanticModel,
                                            FunctionCallExpressionNode functionCallExpr) {
        this.semanticModel = semanticModel;
        this.functionCallExpr = functionCallExpr;
    }

    @Override
    public void visit(ObjectFieldNode objectFieldNode) {
        Symbol symbol = semanticModel.symbol(objectFieldNode).orElse(null);
        TypeSymbol typeDescriptor = SymbolUtil.getTypeDescriptor(symbol).orElse(null);
        checkAndSetTypeResult(typeDescriptor);
    }

    @Override
    public void visit(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        Symbol symbol = semanticModel.symbol(recordFieldWithDefaultValueNode).orElse(null);
        TypeSymbol typeDescriptor = SymbolUtil.getTypeDescriptor(symbol).orElse(null);
        checkAndSetTypeResult(typeDescriptor);
    }

    @Override
    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        visitVariableDeclaration(moduleVariableDeclarationNode, moduleVariableDeclarationNode.typedBindingPattern());
    }

    @Override
    public void visit(AssignmentStatementNode assignmentStatementNode) {
        Symbol symbol = semanticModel.symbol(assignmentStatementNode).orElse(null);
        TypeSymbol typeDescriptor = SymbolUtil.getTypeDescriptor(symbol).orElse(null);
        checkAndSetTypeResult(typeDescriptor);
        if (resultFound) {
            return;
        }

        assignmentStatementNode.varRef().accept(this);
        // We don't check the expression as it mostly is the original function call expression
    }
    
    @Override
    public void visit(CompoundAssignmentStatementNode compoundAssignmentNode) {
        TypeSymbol typeSymbol = semanticModel.typeOf(compoundAssignmentNode.lhsExpression()).orElse(null);
        checkAndSetTypeResult(typeSymbol);
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        visitVariableDeclaration(variableDeclarationNode, variableDeclarationNode.typedBindingPattern());
    }

    @Override
    public void visit(SpecificFieldNode specificFieldNode) {
        semanticModel.symbol(specificFieldNode)
                .map(symbol -> (RecordFieldSymbol) symbol)
                .ifPresent(recordFieldSymbol -> checkAndSetTypeResult(recordFieldSymbol.typeDescriptor()));
    }

    @Override
    public void visit(BinaryExpressionNode binaryExpressionNode) {
        TypeSymbol typeSymbol = semanticModel.typeOf(binaryExpressionNode.lhsExpr()).orElse(null);
        checkAndSetTypeResult(typeSymbol);
        if (resultFound) {
            return;
        }

        typeSymbol = semanticModel.typeOf(binaryExpressionNode.rhsExpr()).orElse(null);
        if (binaryExpressionNode.operator().kind().name().equals("ELVIS_TOKEN")) {
            Types types = semanticModel.types();
            TypeBuilder builder = types.builder();
            UnionTypeSymbol unionTypeSymbol =
                    builder.UNION_TYPE.withMemberTypes(typeSymbol, types.NIL).build();
            checkAndSetTypeResult(unionTypeSymbol);
            return;
        }
        checkAndSetTypeResult(typeSymbol);
    }

    @Override
    public void visit(LetExpressionNode letExpressionNode) {
        TypeSymbol typeSymbol = semanticModel.typeOf(letExpressionNode).orElse(null);
        checkAndSetTypeResult(typeSymbol);
        if (resultFound) {
            return;
        }

        letExpressionNode.parent().accept(this);
    }

    @Override
    public void visit(LetVariableDeclarationNode letVariableDeclarationNode) {
        Optional<Symbol> symbol1 = semanticModel.symbol(letVariableDeclarationNode);

        symbol1.map(symbol -> (VariableSymbol) symbol)
                .map(VariableSymbol::typeDescriptor)
                .ifPresent(this::checkAndSetTypeResult);
    }

    @Override
    public void visit(StartActionNode startActionNode) {
        startActionNode.parent().accept(this);
        if (resultFound && returnTypeSymbol.typeKind() == TypeDescKind.FUTURE) {
            FutureTypeSymbol futureTypeSymbol = (FutureTypeSymbol) returnTypeSymbol;
            TypeSymbol typeSymbol = futureTypeSymbol.typeParameter().orElse(null);
            checkAndSetTypeResult(typeSymbol);
        } else {
            TypeSymbol nilTypeSymbol = semanticModel.types().NIL;
            checkAndSetTypeResult(nilTypeSymbol);
        }
    }

    @Override
    public void visit(FunctionCallExpressionNode fnCallExprNode) {
        fnCallExprNode.functionName().accept(this);
        if (resultFound) {
            return;
        }

        TypeSymbol typeSymbol = semanticModel.typeOf(fnCallExprNode).orElse(null);
        checkAndSetTypeResult(typeSymbol);
        if (resultFound) {
            return;
        }

        fnCallExprNode.parent().accept(this);
    }

    @Override
    public void visit(MethodCallExpressionNode methodCallExpressionNode) {
        methodCallExpressionNode.methodName().accept(this);
        if (resultFound) {
            return;
        }
        TypeSymbol typeSymbol = semanticModel.typeOf(methodCallExpressionNode).orElse(null);
        checkAndSetTypeResult(typeSymbol);
    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        remoteMethodCallActionNode.methodName().accept(this);
        if (resultFound) {
            return;
        }
        TypeSymbol typeSymbol = semanticModel.typeOf(remoteMethodCallActionNode).orElse(null);
        checkAndSetTypeResult(typeSymbol);
    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        semanticModel.symbol(simpleNameReferenceNode)
                .flatMap(SymbolUtil::getTypeDescriptor)
                .ifPresent(this::checkAndSetTypeResult);
    }

    @Override
    public void visit(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        semanticModel.typeOf(errorConstructorExpressionNode)
                .map(CommonUtil::getRawType)
                .ifPresent(this::checkAndSetTypeResult);
    }

    @Override
    public void visit(PositionalArgumentNode positionalArgumentNode) {
        positionalArgumentNode.parent().accept(this);
        if (!resultFound) {
            return;
        }

        // This is the message parameter of an error constructor.
        if (returnTypeSymbol.typeKind() == TypeDescKind.ERROR) {
            checkAndSetTypeResult(semanticModel.types().STRING);
            return;
        }

        Optional<List<ParameterSymbol>> params = getParameterSymbols();
        if (params.isEmpty() || params.get().isEmpty()) {
            return;
        }
        SeparatedNodeList<FunctionArgumentNode> arguments;
        switch (positionalArgumentNode.parent().kind()) {
            case METHOD_CALL:
                MethodCallExpressionNode methodCallExpressionNode =
                        (MethodCallExpressionNode) positionalArgumentNode.parent();
                arguments = methodCallExpressionNode.arguments();
                break;
            case FUNCTION_CALL:
                FunctionCallExpressionNode functionCallExpressionNode =
                        (FunctionCallExpressionNode) positionalArgumentNode.parent();
                arguments = functionCallExpressionNode.arguments();
                break;
            case REMOTE_METHOD_CALL_ACTION:
                RemoteMethodCallActionNode remoteMethodCallActionNode =
                        (RemoteMethodCallActionNode) positionalArgumentNode.parent();
                arguments = remoteMethodCallActionNode.arguments();
                break;
            case PARENTHESIZED_ARG_LIST:
                ParenthesizedArgList parenthesizedArgList =
                        (ParenthesizedArgList) positionalArgumentNode.parent();
                arguments = parenthesizedArgList.arguments();
                break;
            default:
                return;
        }
        if (arguments != null) {
            int argIndex = -1;
            for (int i = 0; i < arguments.size(); i++) {
                if (arguments.get(i).equals(positionalArgumentNode)) {
                    argIndex = i;
                    break;
                }
            }
            if (argIndex < 0 || params.get().size() < argIndex + 1) {
                return;
            }
            ParameterSymbol parameterSymbol = params.get().get(argIndex);
            checkAndSetTypeResult(parameterSymbol.typeDescriptor());
        }
    }

    @Override
    public void visit(NamedArgumentNode namedArgumentNode) {
        namedArgumentNode.parent().accept(this);
        if (!resultFound) {
            return;
        }

        if (returnTypeSymbol.typeKind() == TypeDescKind.ERROR) {
            ErrorTypeSymbol errorTypeSymbol = (ErrorTypeSymbol) returnTypeSymbol;
            TypeSymbol detailType = CommonUtil.getRawType(errorTypeSymbol.detailTypeDescriptor());
            if (detailType.typeKind() != TypeDescKind.RECORD) {
                // Should be a map<> - member type is assumed to be anydata at this time
                checkAndSetTypeResult(semanticModel.types().ANYDATA);
                return;
            }

            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) detailType;
            RecordFieldSymbol fieldSymbol = recordTypeSymbol.fieldDescriptors()
                    .get(namedArgumentNode.argumentName().name().text());
            if (fieldSymbol == null) {
                resetResult();
                return;
            }

            checkAndSetTypeResult(fieldSymbol.typeDescriptor());
            return;
        }

        Optional<List<ParameterSymbol>> params = getParameterSymbols();
        if (params.isEmpty()) {
            return;
        }
        params.get().stream().filter(param -> param.getName().isPresent()
                        && param.getName().get().equals(namedArgumentNode.argumentName().name().text())).findFirst()
                .ifPresent(parameterSymbol -> this.checkAndSetTypeResult(parameterSymbol.typeDescriptor()));
    }

    /**
     * Returns the parameter symbols once the {@link #returnTypeSymbol} is detected as a function/method symbol.
     *
     * @return Optional parameter symbol list
     */
    private Optional<List<ParameterSymbol>> getParameterSymbols() {
        FunctionTypeSymbol functionTypeSymbol;
        if (returnTypeSymbol.typeKind() == TypeDescKind.FUNCTION) {
            functionTypeSymbol = (FunctionTypeSymbol) returnTypeSymbol;
        } else if (returnTypeSymbol.kind() == SymbolKind.CLASS) {
            Optional<MethodSymbol> methodSymbol = ((ClassSymbol) returnTypeSymbol).initMethod();
            if (methodSymbol.isEmpty()) {
                return Optional.empty();
            }
            functionTypeSymbol = methodSymbol.get().typeDescriptor();
        } else {
            return Optional.empty();
        }
        return functionTypeSymbol.params();
    }

    @Override
    public void visit(ParenthesizedArgList parenthesizedArgList) {
        parenthesizedArgList.parent().accept(this);
    }

    @Override
    public void visit(ExplicitNewExpressionNode explicitNewExpressionNode) {
        semanticModel.typeOf(explicitNewExpressionNode)
                .flatMap(typeSymbol -> Optional.of(CommonUtil.getRawType(typeSymbol)))
                .stream().findFirst().ifPresent(this::checkAndSetTypeResult);
    }

    @Override
    public void visit(ImplicitNewExpressionNode implicitNewExpressionNode) {
        semanticModel.typeOf(implicitNewExpressionNode)
                .flatMap(typeSymbol -> Optional.of(CommonUtil.getRawType(typeSymbol)))
                .stream().findFirst().ifPresent(this::checkAndSetTypeResult);
    }

    @Override
    public void visit(FunctionDefinitionNode node) {
        semanticModel.symbol(node)
                .flatMap(SymbolUtil::getTypeDescriptor)
                .ifPresent(this::checkAndSetTypeResult);
    }

    @Override
    public void visit(FunctionBodyBlockNode node) {
        node.parent().accept(this);
    }

    @Override
    public void visit(BlockStatementNode node) {
        node.parent().accept(this);
    }

    @Override
    public void visit(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        semanticModel.symbol(namedWorkerDeclarationNode)
                .ifPresent(value -> checkAndSetTypeResult(((WorkerSymbol) value).returnType()));
    }

    @Override
    public void visit(ReturnStatementNode returnStatementNode) {
        this.semanticModel.typeOf(returnStatementNode).ifPresent(this::checkAndSetTypeResult);
        if (resultFound) {
            return;
        }

        // Get function type symbol and get return type descriptor from it
        returnStatementNode.parent().accept(this);

        if (!resultFound) {
            resetResult();
            return;
        }
        if (returnTypeSymbol.typeKind() == TypeDescKind.FUNCTION) {
            FunctionTypeSymbol functionTypeSymbol = (FunctionTypeSymbol) returnTypeSymbol;
            functionTypeSymbol.returnTypeDescriptor().ifPresentOrElse(this::checkAndSetTypeResult, this::resetResult);
        }
    }

    @Override
    public void visit(UnaryExpressionNode unaryExpressionNode) {
        semanticModel.typeOf(unaryExpressionNode).ifPresent(this::checkAndSetTypeResult);

        if (!resultFound) {
            checkAndSetTypeResult(semanticModel.types().BOOLEAN);
        }
    }

    @Override
    public void visit(IfElseStatementNode node) {
        // Set function call type to boolean only if it's in the condition area
        if (PositionUtil.isWithinLineRange(functionCallExpr.lineRange(), node.condition().lineRange())) {
            checkAndSetTypeResult(semanticModel.types().BOOLEAN);
            return;
        }
        node.parent().accept(this);
    }

    @Override
    public void visit(FailStatementNode failStatementNode) {
        checkAndSetTypeResult(semanticModel.types().ERROR);
    }

    @Override
    public void visit(WhileStatementNode whileStatementNode) {
        if (PositionUtil.isWithinLineRange(functionCallExpr.lineRange(), whileStatementNode.condition().lineRange())) {
            checkAndSetTypeResult(semanticModel.types().BOOLEAN);
            return;
        }
        whileStatementNode.parent().accept(this);
    }

    @Override
    public void visit(ConditionalExpressionNode conditionalExpressionNode) {
        // Check if the function call expression is in LHS expression first
        if (!conditionalExpressionNode.lhsExpression().isMissing() &&
                PositionUtil.isWithinLineRange(this.functionCallExpr.lineRange(),
                        conditionalExpressionNode.lhsExpression().lineRange())) {
            checkAndSetTypeResult(this.semanticModel.types().BOOLEAN);
            return;
        }
        Optional<TypeSymbol> typeSymbol = semanticModel.typeOf(conditionalExpressionNode.middleExpression())
                .filter(type -> type.typeKind() != TypeDescKind.COMPILATION_ERROR)
                .or(() -> semanticModel.typeOf(conditionalExpressionNode.endExpression())
                        .filter(type -> type.typeKind() != TypeDescKind.COMPILATION_ERROR));

        if (typeSymbol.isPresent()) {
            checkAndSetTypeResult(typeSymbol.get());
        } else {
            conditionalExpressionNode.parent().accept(this);
        }
    }

    @Override
    public void visit(CheckExpressionNode checkExpressionNode) {
        if (checkExpressionNode.parent().kind() == SyntaxKind.CALL_STATEMENT) {
            checkAndSetTypeResult(semanticModel.types().ERROR);
        } else {
            checkExpressionNode.parent().accept(this);
        }
    }
    @Override
    public void visit(GroupingKeyVarDeclarationNode groupingKeyVarDeclarationNode) {
        Symbol symbol = semanticModel.symbol(groupingKeyVarDeclarationNode.typeDescriptor()).orElse(null);
        TypeSymbol typeDescriptor = SymbolUtil.getTypeDescriptor(symbol).orElse(null);
        checkAndSetTypeResult(typeDescriptor);
    }
    
    @Override
    public void visit(SelectClauseNode selectClauseNode) {
        selectClauseNode.parent().parent().accept(this);
        if (resultFound) {
            TypeDescKind kind = this.returnTypeSymbol.typeKind();
            if (kind == TypeDescKind.ARRAY) {
                checkAndSetTypeResult(((ArrayTypeSymbol) returnTypeSymbol).memberTypeDescriptor());
            } else if (kind == TypeDescKind.STREAM) {
                checkAndSetTypeResult(((StreamTypeSymbol) returnTypeSymbol).typeParameter());
            }
        }
    }
    
    @Override
    public void visit(ImplicitAnonymousFunctionExpressionNode expr) {
        Optional<TypeSymbol> typeSymbol = semanticModel.typeOf(expr);
        if (typeSymbol.isPresent()) {
            TypeSymbol ts = typeSymbol.get();
            if (ts.typeKind() == TypeDescKind.FUNCTION) {
                ((FunctionTypeSymbol) ts).returnTypeDescriptor().ifPresent(this::checkAndSetTypeResult);
            }
        }
    }

    @Override
    public void visit(ExplicitAnonymousFunctionExpressionNode explicitAnonymousFunctionExpressionNode) {
        semanticModel.typeOf(explicitAnonymousFunctionExpressionNode).ifPresent(this::checkAndSetTypeResult);
    }

    @Override
    public void visit(PanicStatementNode panicStatementNode) {
        checkAndSetTypeResult(semanticModel.types().ERROR);
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        // Do nothing
    }

    private void checkAndSetTypeResult(TypeSymbol typeSymbol) {
        if (typeSymbol == null) {
            return;
        }

        this.returnTypeSymbol = typeSymbol;
        if (typeSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR) {
            resultFound = true;
        }
    }

    private void resetResult() {
        this.returnTypeSymbol = null;
        this.resultFound = false;
    }

    /**
     * Get the type symbol of the return type of the function call expression provided to this instance.
     * Should be invoked after executing this visitor.
     *
     * @return Optional type symbol of the return type of function call expression
     */
    public Optional<TypeSymbol> getReturnTypeSymbol() {
        return Optional.ofNullable(returnTypeSymbol);
    }
    
    private void visitVariableDeclaration(Node variableDeclarationNode, TypedBindingPatternNode typedBindingNode) {
        Symbol symbol = semanticModel.symbol(variableDeclarationNode).orElse(null);
        Optional<TypeSymbol> typeDescriptor = SymbolUtil.getTypeDescriptor(symbol);
        TypeSymbol ts = null;
        if (typeDescriptor.isPresent()) {
            ts = typeDescriptor.get();
            if (ts.typeKind() == TypeDescKind.COMPILATION_ERROR && 
                    typedBindingNode.typeDescriptor().kind() == SyntaxKind.VAR_TYPE_DESC) {
                Types types = semanticModel.types();
                ts = types.builder().UNION_TYPE.withMemberTypes(types.ANY, types.ERROR).build();
            }
        }
        checkAndSetTypeResult(ts);        
    }
}

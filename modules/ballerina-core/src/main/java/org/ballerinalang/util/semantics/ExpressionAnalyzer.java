/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.semantics;

import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.GlobalVarLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeDef;
import org.ballerinalang.model.AnnotationDef;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.CompilationUnit;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.TypeMapper;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BacktickExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FieldAccessExpr;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.JSONArrayInitExpr;
import org.ballerinalang.model.expressions.JSONFieldAccessExpr;
import org.ballerinalang.model.expressions.JSONInitExpr;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.invokers.MainInvoker;
import org.ballerinalang.model.statements.AbortStmt;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.CommentStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TransactionRollbackStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.SemanticErrors;
import org.ballerinalang.util.exceptions.SemanticException;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * {@code ExpressionAnalyzer} analyze and arrange execution units in relevant order and detects loops if any.
 *
 * @since 0.88
 */
public class ExpressionAnalyzer implements NodeVisitor {
    private String currentPkg;
    private SymbolScope currentScope;
    private Map<SymbolName, CompilationUnitWrapper> symbolMap;
    private Stack<CompilationUnit> compilationUnitStack = new Stack<CompilationUnit>();
    private CompilationUnitWrapper currentCompilationUnit;

    public ExpressionAnalyzer(String currentPkg, SymbolScope currentScope) {
        this.currentPkg = currentPkg;
        this.currentScope = currentScope;
        this.symbolMap = new HashMap<>();
    }

    @Override
    public void visit(BLangProgram bLangProgram) {

    }

    @Override
    public void visit(BLangPackage bLangPackage) {

        for (ConstDef constDef : bLangPackage.getConsts()) {
            symbolMap.put(new SymbolName(constDef.getName(), constDef.getPackagePath()),
                    new CompilationUnitWrapper(constDef));
        }
        for (GlobalVariableDef globalVariableDef : bLangPackage.getGlobalVariables()) {
            symbolMap.put(new SymbolName(globalVariableDef.getName(), globalVariableDef.getPackagePath()),
                    new CompilationUnitWrapper(globalVariableDef));
        }
        for (Function function : bLangPackage.getFunctions()) {
            symbolMap.put(new SymbolName(function.getName(), function.getPackagePath()),
                    new CompilationUnitWrapper((BallerinaFunction) function));
        }

        for (StructDef structDef : bLangPackage.getStructDefs()) {
            symbolMap.put(new SymbolName(structDef.getName(), structDef.getPackagePath()),
                    new CompilationUnitWrapper(structDef));
        }

        //loop detection logic start
        for (ConstDef constDef : bLangPackage.getConsts()) {
            constDef.accept(this);
        }

        for (GlobalVariableDef globalVariableDef : bLangPackage.getGlobalVariables()) {
            globalVariableDef.accept(this);
        }

        for (Function function : bLangPackage.getFunctions()) {
            addCompilationUnit((BallerinaFunction) function);
        }

        for (Service service : bLangPackage.getServices()) {
            addCompilationUnit(service);
        }

        for (BallerinaConnectorDef connectorDef : bLangPackage.getConnectors()) {
            addCompilationUnit(connectorDef);
        }

        for (TypeMapper typeMapper : bLangPackage.getTypeMappers()) {
            addCompilationUnit((BTypeMapper) typeMapper);
        }

        for (StructDef structDef : bLangPackage.getStructDefs()) {
            addCompilationUnit(structDef);
        }

        for (AnnotationDef annotationDef : bLangPackage.getAnnotationDefs()) {
            addCompilationUnit(annotationDef);
        }

        // after stack is prepared, execute this
        bLangPackage.setCompilationUnits(compilationUnitStack.toArray(new CompilationUnit[0]));
    }

    @Override
    public void visit(BallerinaFile bFile) {

    }

    @Override
    public void visit(ImportPackage importPkg) {

    }

    @Override
    public void visit(ConstDef constant) {
        Expression rhsExp = constant.getRhsExpr();
        if (rhsExp instanceof BasicLiteral) {
            addCompilationUnit(constant);
        }
        //TODO later implement for other expression types
    }

    @Override
    public void visit(GlobalVariableDef globalVar) {
        SymbolName symbolName = new SymbolName(globalVar.getName(), globalVar.getPackagePath());
        CompilationUnitWrapper compilationUnitWrapper = symbolMap.get(symbolName);
        if (currentCompilationUnit != null) {
            currentCompilationUnit.setDependency(compilationUnitWrapper);
        }
        currentCompilationUnit = compilationUnitWrapper;
        if (compilationUnitWrapper.isOrdered()) {
            return;
        }
        compilationUnitWrapper.setOrderingStarted(true);
        VariableDefStmt variableDefStmt = globalVar.getVariableDefStmt();
        Expression rhsExp = variableDefStmt.getRExpr();
        if (rhsExp != null) {
            rhsExp.accept(this);
        }
        addCompilationUnit(globalVar);
        compilationUnitWrapper.setOrdered(true);
    }

    @Override
    public void visit(Service service) {
    }

    @Override
    public void visit(BallerinaConnectorDef connector) {
    }

    @Override
    public void visit(Resource resource) {

    }

    @Override
    public void visit(BallerinaFunction function) {
    }

    @Override
    public void visit(BTypeMapper typeMapper) {
    }

    @Override
    public void visit(BallerinaAction action) {

    }

    @Override
    public void visit(Worker worker) {

    }

    @Override
    public void visit(AnnotationAttachment annotation) {

    }

    @Override
    public void visit(ParameterDef parameterDef) {

    }

    @Override
    public void visit(VariableDef variableDef) {

    }

    @Override
    public void visit(StructDef structDef) {

    }

    @Override
    public void visit(AnnotationAttributeDef annotationAttributeDef) {

    }

    @Override
    public void visit(AnnotationDef annotationDef) {
    }

    @Override
    public void visit(VariableDefStmt varDefStmt) {
        varDefStmt.getLExpr().accept(this);
        varDefStmt.getRExpr().accept(this);
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        for (Expression expression : assignStmt.getLExprs()) {
            expression.accept(this);
        }
        assignStmt.getRExpr().accept(this);
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        for (Statement statement:blockStmt.getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        ifElseStmt.getCondition().accept(this);
        ifElseStmt.getThenBody().accept(this);
        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            elseIfBlock.getElseIfCondition().accept(this);
            for (Statement statement : elseIfBlock.getElseIfBody().getStatements()) {
                statement.accept(this);
            }
        }
        if (ifElseStmt.getElseBody() != null) {
            ifElseStmt.getElseBody().accept(this);
        }
    }

    @Override
    public void visit(ReplyStmt replyStmt) {

    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        for (Expression expression : returnStmt.getExprs()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        whileStmt.getCondition().accept(this);
        for (Statement statement : whileStmt.getBody().getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(BreakStmt breakStmt) {

    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {

    }

    @Override
    public void visit(ThrowStmt throwStmt) {

    }

    @Override
    public void visit(FunctionInvocationStmt functionInvocationStmt) {

    }

    @Override
    public void visit(ActionInvocationStmt actionInvocationStmt) {

    }

    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {

    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {

    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {

    }

    @Override
    public void visit(TransformStmt transformStmt) {

    }

    @Override
    public void visit(TransactionRollbackStmt transactionRollbackStmt) {

    }

    @Override
    public void visit(AbortStmt abortStmt) {

    }

    @Override
    public void visit(AddExpression addExpr) {
        addExpr.getLExpr().accept(this);
        addExpr.getRExpr().accept(this);
    }

    @Override
    public void visit(AndExpression andExpression) {

    }

    @Override
    public void visit(BasicLiteral basicLiteral) {

    }

    @Override
    public void visit(DivideExpr divideExpr) {

    }

    @Override
    public void visit(ModExpression modExpression) {

    }

    @Override
    public void visit(EqualExpression equalExpression) {

    }

    @Override
    public void visit(FunctionInvocationExpr functionInvocationExpr) {
        if (!currentPkg.equals(functionInvocationExpr.getPackagePath())) {
            return;
        }
        Expression[] exprs = functionInvocationExpr.getArgExprs();
        for (Expression expression : exprs) {
            expression.accept(this);
        }
        CompilationUnitWrapper compilationUnitWrapper = symbolMap.get(new SymbolName(functionInvocationExpr.getName(),
                functionInvocationExpr.getPackagePath()));
        Function function = (Function) compilationUnitWrapper.getCompilationUnit();
        for (Statement statement : function.getCallableUnitBody().getStatements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(ActionInvocationExpr actionInvocationExpr) {

    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpression) {
        greaterEqualExpression.getLExpr().accept(this);
        greaterEqualExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        greaterThanExpression.getLExpr().accept(this);
        greaterThanExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpression) {
        lessEqualExpression.getLExpr().accept(this);
        lessEqualExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(LessThanExpression lessThanExpression) {
        lessThanExpression.getLExpr().accept(this);
        lessThanExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(MultExpression multExpression) {
        multExpression.getLExpr().accept(this);
        multExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {

    }

    @Override
    public void visit(NotEqualExpression notEqualExpression) {
        notEqualExpression.getLExpr().accept(this);
        notEqualExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(OrExpression orExpression) {
        orExpression.getLExpr().accept(this);
        orExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(SubtractExpression subtractExpression) {
        subtractExpression.getLExpr().accept(this);
        subtractExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        unaryExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        typeCastExpression.getRExpr().accept(this);
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {

    }

    @Override
    public void visit(FieldAccessExpr structAttributeAccessExpr) {

    }

    @Override
    public void visit(JSONFieldAccessExpr jsonPathExpr) {

    }

    @Override
    public void visit(BacktickExpr backtickExpr) {

    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {

    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        for (Expression expression : refTypeInitExpr.getArgExprs()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {

    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        for (Expression expression : structInitExpr.getArgExprs()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {

    }

    @Override
    public void visit(JSONInitExpr jsonInitExpr) {

    }

    @Override
    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {

    }

    @Override
    public void visit(KeyValueExpr keyValueExpr) {
        keyValueExpr.getKeyExpr().accept(this);
        keyValueExpr.getValueExpr().accept(this);
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        if (!currentPkg.equals(variableRefExpr.getPkgPath())) {
            return;
        }
        SymbolName varRefSymbol = new SymbolName(variableRefExpr.getVarName(), variableRefExpr.getPkgPath());
        CompilationUnitWrapper refVarWrapper = symbolMap.get(varRefSymbol);
        if (refVarWrapper == null) {
            return;
        }
        if (refVarWrapper.isOrderingStarted() && !refVarWrapper.isOrdered()) {
            String errorMsg = BLangExceptionHelper.constructSemanticError(variableRefExpr.getNodeLocation(),
                    SemanticErrors.VARIABLE_REFERENCE_LOOP, varRefSymbol);
            String location = variableRefExpr.getNodeLocation().getFileName() + ":"
                    + variableRefExpr.getNodeLocation().getLineNumber() + ": ";
            errorMsg = errorMsg + "\n\t" + location;
            errorMsg = buildSemanticErrorMsg(refVarWrapper, errorMsg);
            throw new SemanticException(errorMsg);
        }
        refVarWrapper.getCompilationUnit().accept(this);
    }

    private String buildSemanticErrorMsg(CompilationUnitWrapper compilationUnitWrapper, String errorMsg) {
        if (compilationUnitWrapper != null) {
            NodeLocation nodeLocation = compilationUnitWrapper.getCompilationUnit().getNodeLocation();
            String location = nodeLocation.getFileName() + ":" + nodeLocation.getLineNumber() + ": ";
            errorMsg = errorMsg + "\n\t" + location;
            errorMsg = buildSemanticErrorMsg(compilationUnitWrapper.getDependency(), errorMsg);
        }
        return errorMsg;
    }


    @Override
    public void visit(NullLiteral nullLiteral) {

    }

    @Override
    public void visit(StackVarLocation stackVarLocation) {

    }

    @Override
    public void visit(ServiceVarLocation serviceVarLocation) {

    }

    @Override
    public void visit(GlobalVarLocation globalVarLocation) {

    }

    @Override
    public void visit(ConnectorVarLocation connectorVarLocation) {

    }

    @Override
    public void visit(ConstantLocation constantLocation) {

    }

    @Override
    public void visit(StructVarLocation structVarLocation) {

    }

    @Override
    public void visit(ResourceInvocationExpr resourceIExpr) {

    }

    @Override
    public void visit(MainInvoker mainInvoker) {

    }

    @Override
    public void visit(WorkerVarLocation workerVarLocation) {

    }

    private void addCompilationUnit(CompilationUnit compilationUnit) {
        compilationUnitStack.push(compilationUnit);
    }


    class CompilationUnitWrapper {

        private boolean isOrdered;
        private boolean isOrderingStarted;
        private CompilationUnitWrapper dependency;

        private CompilationUnit compilationUnit;

        public CompilationUnitWrapper(CompilationUnit compilationUnit) {
            this.compilationUnit = compilationUnit;
        }

        public void setOrdered(boolean ordered) {
            isOrdered = ordered;
        }

        public boolean isOrdered() {
            return isOrdered;
        }

        public boolean isOrderingStarted() {
            return isOrderingStarted;
        }

        public void setOrderingStarted(boolean orderingStarted) {
            isOrderingStarted = orderingStarted;
        }

        public CompilationUnit getCompilationUnit() {
            return compilationUnit;
        }

        public CompilationUnitWrapper getDependency() {
            return dependency;
        }

        public void setDependency(CompilationUnitWrapper dependency) {
            this.dependency = dependency;
        }
    }
}

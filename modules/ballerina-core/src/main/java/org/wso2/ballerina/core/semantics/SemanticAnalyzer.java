/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.semantics;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.exception.LinkerException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.LocalVarLocation;
import org.wso2.ballerina.core.interpreter.MemoryLocation;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.interpreter.SymTable;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BackquoteExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryArithmeticExpression;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.BinaryLogicalExpression;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.KeyValueExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.invokers.MainInvoker;
import org.wso2.ballerina.core.model.invokers.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.statements.ActionInvocationStmt;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.BArrayType;
import org.wso2.ballerina.core.model.types.BMapType;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.util.LangModelUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code SemanticAnalyzer} analyzes semantic properties of a Ballerina program
 *
 * @since 1.0.0
 */
public class SemanticAnalyzer implements NodeVisitor {
    private int stackFrameOffset = -1;
    private int staticMemAddrOffset = -1;
    private int connectorMemAddrOffset = -1;

    private SymTable symbolTable;

    private String currentPkg;

    // We need to keep a map of import packages.
    // This is useful when analyzing import functions, actions and types.
    private Map<String, ImportPackage> importPkgMap = new HashMap<>();

    public SemanticAnalyzer(BallerinaFile bFile, SymScope globalScope) {
        SymScope pkgScope = bFile.getPackageScope();
        pkgScope.setParent(globalScope);
        symbolTable = new SymTable(pkgScope);

        currentPkg = bFile.getPackageName();

        // TODO We can move this logic to the parser.
        Arrays.asList(bFile.getFunctions()).forEach(this::addFuncSymbol);
        bFile.getConnectorList().forEach(connector -> {
            addConnectorSymbol(connector);
            Arrays.asList(connector.getActions()).forEach(this::addActionSymbol);
        });
    }

    @Override
    public void visit(BallerinaFile bFile) {
        for (ImportPackage importPkg : bFile.getImportPackages()) {
            importPkg.accept(this);
        }

        // Analyze and allocate static memory locations for constants
        for (Const constant : bFile.getConstants()) {
            staticMemAddrOffset++;
            constant.accept(this);
        }

        for (BallerinaConnector connector : bFile.getConnectorList()) {
            connector.accept(this);
        }

        for (Service service : bFile.getServices()) {
            service.accept(this);
        }

        for (Function function : bFile.getFunctions()) {
            BallerinaFunction bFunction = (BallerinaFunction) function;
            bFunction.accept(this);
        }

        int setSizeOfStaticMem = staticMemAddrOffset + 1;
        bFile.setSizeOfStaticMem(setSizeOfStaticMem);
        staticMemAddrOffset = -1;
    }

    @Override
    public void visit(ImportPackage importPkg) {
        if (importPkgMap.containsKey(importPkg.getName())) {
            throw new RuntimeException("Duplicate import package declaration: " + importPkg.getPath());
        }

        importPkgMap.put(importPkg.getName(), importPkg);
    }

    @Override
    public void visit(Const constant) {
        SymbolName symName = constant.getName();

        Symbol symbol = symbolTable.lookup(symName);
        if (symbol != null && isSymbolInCurrentScope(symbol)) {
            throw new SemanticException("Duplicate constant name: " + symName.getName());
        }

        // Constants values must be basic literals
        if (!(constant.getValueExpr() instanceof BasicLiteral)) {
            throw new SemanticException("Invalid value in constant definition: constant name: " +
                    constant.getName().getName());
        }

        BasicLiteral basicLiteral = (BasicLiteral) constant.getValueExpr();
        constant.setValue(basicLiteral.getBValue());

        ConstantLocation location = new ConstantLocation(staticMemAddrOffset);
        BType type = constant.getType();
        symbol = new Symbol(type, SymScope.Name.PACKAGE, location);

        symbolTable.insert(symName, symbol);
    }

    @Override
    public void visit(Service service) {
        // Visit the contents within a service
        // Open a new symbol scope
        openScope(SymScope.Name.SERVICE);

        for (ConnectorDcl connectorDcl : service.getConnectorDcls()) {
            staticMemAddrOffset++;
            visit(connectorDcl);
        }

        for (VariableDcl variableDcl : service.getVariableDcls()) {
            staticMemAddrOffset++;
            visit(variableDcl);
        }

        // Visit the set of resources in a service
        for (Resource resource : service.getResources()) {
            resource.accept(this);
        }

        // Close the symbol scope
        closeScope();
    }

    @Override
    public void visit(BallerinaConnector connector) {
        // Then open the connector namespace
        openScope(SymScope.Name.CONNECTOR);

        for (Parameter parameter : connector.getParameters()) {
            connectorMemAddrOffset++;
            visit(parameter);
        }

        for (ConnectorDcl connectorDcl : connector.getConnectorDcls()) {
            connectorMemAddrOffset++;
            visit(connectorDcl);
        }

        for (VariableDcl variableDcl : connector.getVariableDcls()) {
            connectorMemAddrOffset++;
            visit(variableDcl);
        }

        for (BallerinaAction action : connector.getActions()) {
            action.accept(this);
        }

        int sizeOfConnectorMem = connectorMemAddrOffset + 1;
        connector.setSizeOfConnectorMem(sizeOfConnectorMem);

        // Close the symbol scope
        connectorMemAddrOffset = -1;
        closeScope();
    }

    @Override
    public void visit(Resource resource) {
        // Visit the contents within a resource
        // Open a new symbol scope
        openScope(SymScope.Name.RESOURCE);

        // TODO create a Symbol for this function( with parameter and return types)

        Parameter[] parameters = resource.getParameters();
        for (Parameter parameter : parameters) {
            stackFrameOffset++;
            visit(parameter);
        }

        VariableDcl[] variableDcls = resource.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            stackFrameOffset++;
            visit(variableDcl);
        }

        ConnectorDcl[] connectorDcls = resource.getConnectorDcls();
        for (ConnectorDcl connectorDcl : connectorDcls) {
            stackFrameOffset++;
            visit(connectorDcl);
        }

        BlockStmt blockStmt = resource.getResourceBody();
        blockStmt.accept(this);

        int sizeOfStackFrame = stackFrameOffset + 1;
        resource.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        stackFrameOffset = -1;
        closeScope();
    }

    @Override
    public void visit(BallerinaFunction bFunction) {
        // Open a new symbol scope
        openScope(SymScope.Name.FUNCTION);

        Parameter[] parameters = bFunction.getParameters();
        for (Parameter parameter : parameters) {
            stackFrameOffset++;
            visit(parameter);
        }

        VariableDcl[] variableDcls = bFunction.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            stackFrameOffset++;
            visit(variableDcl);
        }

        ConnectorDcl[] connectorDcls = bFunction.getConnectorDcls();
        for (ConnectorDcl connectorDcl : connectorDcls) {
            stackFrameOffset++;
            visit(connectorDcl);
        }

        BlockStmt blockStmt = bFunction.getFunctionBody();
        blockStmt.accept(this);

        // Here we need to calculate size of the BValue array which will be created in the stack frame
        // Values in the stack frame are stored in the following order.
        // -- Parameter values --
        // -- Local var values --
        // -- Temp values      --
        // -- Return values    --
        // These temp values are results of intermediate expression evaluations.
        int sizeOfStackFrame = stackFrameOffset + 1;
        bFunction.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        stackFrameOffset = -1;
        closeScope();
    }

    @Override
    public void visit(BallerinaAction action) {
        // Open a new symbol scope
        openScope(SymScope.Name.ACTION);

        Parameter[] parameters = action.getParameters();
        for (Parameter parameter : parameters) {
            stackFrameOffset++;
            visit(parameter);
        }

        VariableDcl[] variableDcls = action.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            stackFrameOffset++;
            visit(variableDcl);
        }

        ConnectorDcl[] connectorDcls = action.getConnectorDcls();
        for (ConnectorDcl connectorDcl : connectorDcls) {
            stackFrameOffset++;
            visit(connectorDcl);
        }

        BlockStmt blockStmt = action.getActionBody();
        blockStmt.accept(this);

        // Here we need to calculate size of the BValue array which will be created in the stack frame
        // Values in the stack frame are stored in the following order.
        // -- Parameter values --
        // -- Local var values --
        // -- Temp values      --
        // -- Return values    --
        // These temp values are results of intermediate expression evaluations.
        int sizeOfStackFrame = stackFrameOffset + 1;
        action.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        stackFrameOffset = -1;
        closeScope();
    }

    @Override
    public void visit(Worker worker) {

    }

    @Override
    public void visit(Annotation annotation) {

    }

    @Override
    public void visit(Parameter parameter) {
        SymbolName symName = parameter.getName();

        Symbol symbol = symbolTable.lookup(symName);
        if (symbol != null && isSymbolInCurrentScope(symbol)) {
            throw new SemanticException("Duplicate parameter name: " + symName.getName());
        }

        MemoryLocation location;
        if (isInScope(SymScope.Name.CONNECTOR)) {
            location = new ConnectorVarLocation(connectorMemAddrOffset);

        } else if (isInScope(SymScope.Name.FUNCTION) ||
                isInScope(SymScope.Name.RESOURCE) ||
                isInScope(SymScope.Name.ACTION)) {

            location = new LocalVarLocation(stackFrameOffset);
        } else {

            // This error should not be thrown
            throw new IllegalStateException("Parameter declaration is invalid");
        }

        BType type = parameter.getType();
        symbol = new Symbol(type, currentScopeName(), location);
        symbolTable.insert(symName, symbol);

    }

    @Override
    public void visit(VariableDcl variableDcl) {
        SymbolName symName = variableDcl.getName();
        Symbol symbol = symbolTable.lookup(symName);
        if (symbol != null && isSymbolInCurrentScope(symbol)) {
            throw new SemanticException("Duplicate variable declaration with name: " + symName.getName());
        }

        MemoryLocation location;
        if (isInScope(SymScope.Name.CONNECTOR)) {
            location = new ConnectorVarLocation(connectorMemAddrOffset);

        } else if (isInScope(SymScope.Name.SERVICE)) {
            location = new ServiceVarLocation(staticMemAddrOffset);

        } else if (isInScope(SymScope.Name.FUNCTION) ||
                isInScope(SymScope.Name.RESOURCE) ||
                isInScope(SymScope.Name.ACTION)) {

            location = new LocalVarLocation(stackFrameOffset);
        } else {

            // This error should not be thrown
            throw new IllegalStateException("Variable declaration is invalid");
        }

        BType type = variableDcl.getType();
        symbol = new Symbol(type, currentScopeName(), location);
        symbolTable.insert(symName, symbol);
    }

    @Override
    public void visit(ConnectorDcl connectorDcl) {
        SymbolName symbolName = connectorDcl.getVarName();

        Symbol symbol = symbolTable.lookup(symbolName);
        if (symbol != null && isSymbolInCurrentScope(symbol)) {
            throw new SemanticException("Duplicate connector declaration with name: " + symbolName.getName());
        }

        MemoryLocation location;
        if (isInScope(SymScope.Name.CONNECTOR)) {
            location = new ConnectorVarLocation(connectorMemAddrOffset);

        } else if (isInScope(SymScope.Name.SERVICE)) {
            location = new ServiceVarLocation(staticMemAddrOffset);

        } else if (isInScope(SymScope.Name.FUNCTION) ||
                isInScope(SymScope.Name.RESOURCE) ||
                isInScope(SymScope.Name.ACTION)) {

            location = new LocalVarLocation(stackFrameOffset);
        } else {

            // This error should not be thrown
            throw new IllegalStateException("Connector declaration is invalid");
        }

        symbol = new Symbol(BTypes.getType(connectorDcl.getConnectorName().getName()), currentScopeName(), location);
        symbolTable.insert(symbolName, symbol);

        // Setting the connector name with the package name
        SymbolName connectorName = connectorDcl.getConnectorName();
        String pkgPath = getPackagePath(connectorName);
        connectorName = LangModelUtils.getConnectorSymName(connectorName.getName(), pkgPath);
        connectorDcl.setConnectorName(connectorName);

        Symbol connectorSym = symbolTable.lookup(connectorName);
        if (connectorSym == null) {
            throw new SemanticException("Connector : " + connectorName + " not found");
        }
        connectorDcl.setConnector(connectorSym.getConnector());

        // Visit connector arguments
        for (Expression argExpr : connectorDcl.getArgExprs()) {
            argExpr.accept(this);
        }
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        Expression lExpr = assignStmt.getLExpr();

        if (lExpr instanceof ArrayMapAccessExpr) {
            ((ArrayMapAccessExpr) lExpr).setLHSExpr(true);
        }

        lExpr.accept(this);

        // Check whether someone is trying to change the values of a constant
        if (lExpr instanceof VariableRefExpr &&
                ((VariableRefExpr) lExpr).getLocation() instanceof ConstantLocation) {
            throw new SemanticException("Error: cannot assign a value to constant: " +
                    ((VariableRefExpr) lExpr).getSymbolName());
        }

        Expression rExpr = assignStmt.getRExpr();
        rExpr.accept(this);

        // Return types of the function or action invoked are only available during the linking phase
        // There type compatibility check is impossible during the semantic analysis phase.
        if (rExpr instanceof FunctionInvocationExpr || rExpr instanceof ActionInvocationExpr) {
            return;
        }

        // If the rExpr typ is not set, then check whether it is a BackquoteExpr
        if (rExpr.getType() == null && rExpr instanceof BackquoteExpr) {

            // In this case, type of the lExpr should be either xml or json
            if (lExpr.getType() != BTypes.JSON_TYPE && lExpr.getType() != BTypes.XML_TYPE) {
                throw new SemanticException("Error:() ballerina: incompatible types: string template " +
                        "cannot be converted to " + lExpr.getType() + ": required xml or json");
            }

            rExpr.setType(lExpr.getType());
            // TODO Visit the rExpr again after the setting the type.
            //rExpr.accept(this);

        }
        // TODO Remove the MAP related logic when type casting is implemented
        if ((lExpr.getType() != BTypes.MAP_TYPE) && (rExpr.getType() != BTypes.MAP_TYPE) &&
                (lExpr.getType() != rExpr.getType())) {
            throw new SemanticException("Error:() ballerina: incompatible types: " + rExpr.getType() +
                    " cannot be converted to " + lExpr.getType());
        }
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        for (Statement stmt : blockStmt.getStatements()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        Expression expr = ifElseStmt.getCondition();
        expr.accept(this);

        if (expr.getType() != BTypes.BOOLEAN_TYPE) {
            throw new SemanticException("Incompatible types: expected a boolean expression");
        }

        Statement thenBody = ifElseStmt.getThenBody();
        thenBody.accept(this);

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            elseIfCondition.accept(this);

            if (elseIfCondition.getType() != BTypes.BOOLEAN_TYPE) {
                throw new SemanticException("Incompatible types: expected a boolean expression");
            }

            Statement elseIfBody = elseIfBlock.getElseIfBody();
            elseIfBody.accept(this);
        }

        Statement elseBody = ifElseStmt.getElseBody();
        if (elseBody != null) {
            elseBody.accept(this);
        }
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        Expression expr = whileStmt.getCondition();
        expr.accept(this);

        if (expr.getType() != BTypes.BOOLEAN_TYPE) {
            throw new SemanticException("Incompatible types: expected a boolean expression");
        }

        BlockStmt blockStmt = whileStmt.getBody();
        if (blockStmt.getStatements().length == 0) {
            // This can be optimized later to skip the while statement
            throw new SemanticException("No statements in the while loop");
        }

        blockStmt.accept(this);
    }

    @Override
    public void visit(FunctionInvocationStmt functionInvocationStmt) {
        functionInvocationStmt.getFunctionInvocationExpr().accept(this);
    }

    @Override
    public void visit(ActionInvocationStmt actionInvocationStmt) {
        actionInvocationStmt.getActionInvocationExpr().accept(this);
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        replyStmt.getReplyExpr().accept(this);
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        Expression[] exprs = returnStmt.getExprs();

        for (Expression expr : exprs) {
            expr.accept(this);
        }
    }

    // Expressions

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        visitExpr(instanceCreationExpr);

        // TODO here the type shouldn't be a value type

//        Expression expr = instanceCreationExpr.getRExpr();
//        expr.accept(this);
    }

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        visitExpr(funcIExpr);

        Expression[] exprs = funcIExpr.getExprs();
        for (Expression expr : exprs) {
            expr.accept(this);
        }

        linkFunction(funcIExpr);

        // Can we do this bit in the linker
//        SymbolName symbolName = funcIExpr.getFunctionName();
//        String pkgPath = getPackagePath(symbolName);
//
//        BType[] paramTypes = new BType[exprs.length];
//        for (int i = 0; i < exprs.length; i++) {
//            paramTypes[i] = exprs[i].getType();
//        }
//
//        symbolName = LangModelUtils.getSymNameWithParams(symbolName.getName(), pkgPath, paramTypes);


//        funcIExpr.setFunctionName(symbolName);

//        bFile.addFuncInvocationExpr(funcIExpr);

        // TODO store the types of each func argument expression
        // Implement semantic analysis for function invocations

        // Identify the package of the function to be invoked.
    }

    // TODO Duplicate code. fix me
    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        visitExpr(actionIExpr);

        Expression[] exprs = actionIExpr.getExprs();
        for (Expression expr : exprs) {
            expr.accept(this);
        }

        linkAction(actionIExpr);

        // TODO Check whether first argument is of type Connector (with connector name). e.g. HttpConnector

//        // Can we do this bit in the linker
//        SymbolName symName = actionIExpr.getActionName();
//        if (symName.getConnectorName() == null) {
//            throw new SemanticException("Connector type is not associated with the action invocation");
//        }
//
//        String pkgPath = getPackagePath(symName);
//
//        BType[] paramTypes = new BType[exprs.length];
//        for (int i = 0; i < exprs.length; i++) {
//            paramTypes[i] = exprs[i].getType();
//        }
//
//        symName = LangModelUtils.getActionSymName(symName.getName(), symName.getConnectorName(),
//                pkgPath, paramTypes);
//        actionIExpr.setActionName(symName);
//
//        bFile.addActionIExpr(actionIExpr);
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
    }

    @Override
    public void visit(UnaryExpression unaryExpr) {

    }

    @Override
    public void visit(AddExpression addExpr) {
        BType arithmeticExprType = verifyBinaryArithmeticExprType(addExpr);

        // We need to find a better implementation than this.
        if (arithmeticExprType == BTypes.INT_TYPE) {
            addExpr.setEvalFunc(AddExpression.ADD_INT_FUNC);

        } else if (arithmeticExprType == BTypes.FLOAT_TYPE) {
            addExpr.setEvalFunc(AddExpression.ADD_FLOAT_FUNC);

        } else if (arithmeticExprType == BTypes.STRING_TYPE) {
            addExpr.setEvalFunc(AddExpression.ADD_STRING_FUNC);

        } else {
            throw new SemanticException("Add operation is not supported for type: " + arithmeticExprType);
        }
    }

    @Override
    public void visit(MultExpression multExpr) {
        BType binaryExprType = verifyBinaryArithmeticExprType(multExpr);

        if (binaryExprType == BTypes.INT_TYPE) {
            multExpr.setEvalFunc(MultExpression.MULT_INT_FUNC);

        } else if (binaryExprType == BTypes.FLOAT_TYPE) {
            multExpr.setEvalFunc(MultExpression.MULT_FLOAT_FUNC);

        } else {
            throw new SemanticException("Mult operation is not supported for type: " + binaryExprType);
        }
    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        BType binaryExprType = verifyBinaryArithmeticExprType(subtractExpr);

        if (binaryExprType == BTypes.INT_TYPE) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_INT_FUNC);

        } else if (binaryExprType == BTypes.FLOAT_TYPE) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_FLOAT_FUNC);

        } else {
            throw new SemanticException("Subtraction operation is not supported for type: " + binaryExprType);
        }
    }

    @Override
    public void visit(AndExpression andExpr) {
        visitBinaryLogicalExpr(andExpr);
        andExpr.setEvalFunc(AndExpression.AND_FUNC);
    }

    @Override
    public void visit(OrExpression orExpr) {
        visitBinaryLogicalExpr(orExpr);
        orExpr.setEvalFunc(OrExpression.OR_FUNC);
    }

    @Override
    public void visit(EqualExpression expr) {
        BType compareExprType = verifyBinaryCompareExprType(expr);

        if (compareExprType == BTypes.INT_TYPE) {
            expr.setEvalFunc(EqualExpression.EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.FLOAT_TYPE) {
            expr.setEvalFunc(EqualExpression.EQUAL_FLOAT_FUNC);

        } else if (compareExprType == BTypes.BOOLEAN_TYPE) {
            expr.setEvalFunc(EqualExpression.EQUAL_BOOLEAN_FUNC);

        } else if (compareExprType == BTypes.STRING_TYPE) {
            expr.setEvalFunc(EqualExpression.EQUAL_STRING_FUNC);

        } else {
            throw new SemanticException("Equals operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {
        BType compareExprType = verifyBinaryCompareExprType(notEqualExpr);

        if (compareExprType == BTypes.INT_TYPE) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.FLOAT_TYPE) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_FLOAT_FUNC);

        } else if (compareExprType == BTypes.BOOLEAN_TYPE) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_BOOLEAN_FUNC);

        } else if (compareExprType == BTypes.STRING_TYPE) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_STRING_FUNC);

        } else {
            throw new SemanticException("NotEqual operation is not supported for type: " + compareExprType);
        }
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {
        BType compareExprType = verifyBinaryCompareExprType(greaterEqualExpr);

        if (compareExprType == BTypes.INT_TYPE) {
            greaterEqualExpr.setEvalFunc(GreaterEqualExpression.GREATER_EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.FLOAT_TYPE) {
            greaterEqualExpr.setEvalFunc(GreaterEqualExpression.GREATER_EQUAL_FLOAT_FUNC);

        } else {
            throw new SemanticException("Greater than equal operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        BType compareExprType = verifyBinaryCompareExprType(greaterThanExpr);

        if (compareExprType == BTypes.INT_TYPE) {
            greaterThanExpr.setEvalFunc(GreaterThanExpression.GREATER_THAN_INT_FUNC);

        } else if (compareExprType == BTypes.FLOAT_TYPE) {
            greaterThanExpr.setEvalFunc(GreaterThanExpression.GREATER_THAN_FLOAT_FUNC);

        } else {
            throw new SemanticException("Greater than operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        BType compareExprType = verifyBinaryCompareExprType(lessEqualExpr);

        if (compareExprType == BTypes.INT_TYPE) {
            lessEqualExpr.setEvalFunc(LessEqualExpression.LESS_EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.FLOAT_TYPE) {
            lessEqualExpr.setEvalFunc(LessEqualExpression.LESS_EQUAL_FLOAT_FUNC);

        } else {
            throw new SemanticException("Less than equal operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        BType compareExprType = verifyBinaryCompareExprType(lessThanExpr);

        if (compareExprType == BTypes.INT_TYPE) {
            lessThanExpr.setEvalFunc(LessThanExpression.LESS_THAN_INT_FUNC);

        } else if (compareExprType == BTypes.FLOAT_TYPE) {
            lessThanExpr.setEvalFunc(LessThanExpression.LESS_THAN_FLOAT_FUNC);

        } else {
            throw new SemanticException("Less than operation is not supported for type: " + compareExprType);
        }
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        // Check whether this access expression is in left hand side of an assignment expression
        // If yes, skip assigning a stack frame offset
        if (!arrayMapAccessExpr.isLHSExpr()) {
            visitExpr(arrayMapAccessExpr);
        }

        // Here we assume that rExpr of array access expression is always a variable reference expression.
        // This according to the grammar
        VariableRefExpr arrayMapVarRefExpr = (VariableRefExpr) arrayMapAccessExpr.getRExpr();
        arrayMapVarRefExpr.accept(this);

        // Handle the array type
        if (arrayMapVarRefExpr.getType() instanceof BArrayType) {
            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            indexExpr.accept(this);
            if (indexExpr.getType() != BTypes.INT_TYPE) {
                throw new SemanticException("Array index should be of type int, not " + indexExpr.getType().toString() +
                        ". Array name: " + arrayMapVarRefExpr.getSymbolName().getName());
            }
            // Set type of the array access expression
            BType typeOfArray = ((BArrayType) arrayMapVarRefExpr.getType()).getElementType();
            arrayMapAccessExpr.setType(typeOfArray);
        } else if (arrayMapVarRefExpr.getType() instanceof BMapType) {
            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            indexExpr.accept(this);
            if (indexExpr.getType() != BTypes.STRING_TYPE) {
                throw new SemanticException("Map index should be of type string, not " +
                        indexExpr.getType().toString() +
                        ". Map name: " + arrayMapVarRefExpr.getSymbolName().getName());
            }
            // Set type of the map access expression
            BType typeOfMap = arrayMapVarRefExpr.getType();
            arrayMapAccessExpr.setType(typeOfMap);
        } else {
            throw new SemanticException("Attempt to index non-array, non-map variable: " +
                    arrayMapVarRefExpr.getSymbolName().getName());
        }
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        Expression[] argExprs = mapInitExpr.getArgExprs();

        if (argExprs.length == 0) {
            throw new SemanticException("Array initializer should have at least one argument");
        }

        argExprs[0].accept(this);
        BType typeOfMap = ((KeyValueExpression) argExprs[0]).getValueExpression().getType();

        for (int i = 1; i < argExprs.length; i++) {
            argExprs[i].accept(this);

            if (((KeyValueExpression) argExprs[i]).getValueExpression().getType() != typeOfMap) {
                throw new SemanticException("Incompatible types used in map initializer: " +
                        "All arguments must have the same type.");
            }
        }

        // Type of this expression is map and internal data type cannot be identifier from declaration
        mapInitExpr.setType(BTypes.MAP_TYPE);
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        Expression[] argExprs = arrayInitExpr.getArgExprs();

        if (argExprs.length == 0) {
            throw new SemanticException("Array initializer should have at least one argument");
        }

        argExprs[0].accept(this);
        BType typeOfArray = argExprs[0].getType();

        for (int i = 1; i < argExprs.length; i++) {
            argExprs[i].accept(this);

            if (argExprs[i].getType() != typeOfArray) {
                throw new SemanticException("Incompatible types used in array initializer: " +
                        "All arguments must have the same type.");
            }
        }

        arrayInitExpr.setType(BTypes.getArrayType(typeOfArray.toString()));
    }

    @Override
    public void visit(KeyValueExpression keyValueExpr) {

    }

    @Override
    public void visit(BackquoteExpr backquoteExpr) {
        // TODO If the type is not set then just return
        visitExpr(backquoteExpr);
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        SymbolName varName = variableRefExpr.getSymbolName();

        // Check whether this symName is declared
        Symbol symbol = getSymbol(varName);

        variableRefExpr.setType(symbol.getType());
//        variableRefExpr.setOffset(symbol.getOffset());

        variableRefExpr.setLocation(symbol.getLocation());
    }

    @Override
    public void visit(LocalVarLocation localVarLocation) {

    }

    @Override
    public void visit(ServiceVarLocation serviceVarLocation) {

    }

    @Override
    public void visit(ConnectorVarLocation connectorVarLocation) {

    }

    @Override
    public void visit(ConstantLocation constantLocation) {

    }

    public void visit(ResourceInvocationExpr resourceIExpr) {
    }

    public void visit(MainInvoker mainInvoker) {
    }


    // Private methods.

    private void openScope(SymScope.Name scopeName) {
        symbolTable.openScope(scopeName);
    }

    private void closeScope() {
        symbolTable.closeScope();
    }

    private boolean isInScope(SymScope.Name scopeName) {
        return symbolTable.getCurrentScope().getScopeName() == scopeName;
    }

    private SymScope.Name currentScopeName() {
        return symbolTable.getCurrentScope().getScopeName();
    }

    private boolean isSymbolInCurrentScope(Symbol symbol) {
        return symbol.getScopeName() == currentScopeName();
    }

    private void visitBinaryExpr(BinaryExpression expr) {
        visitExpr(expr);

        expr.getRExpr().accept(this);
        expr.getLExpr().accept(this);
    }

    private void visitExpr(Expression expr) {
        stackFrameOffset++;
        expr.setOffset(stackFrameOffset);
    }

    private void addFuncSymbol(Function function) {
        SymbolName symbolName = LangModelUtils.getSymNameWithParams(function.getName(), function.getParameters());
        function.setSymbolName(symbolName);

        BType[] paramTypes = LangModelUtils.getTypesOfParams(function.getParameters());
        Symbol symbol = new Symbol(function, paramTypes, function.getReturnTypes());

        if (symbolTable.lookup(symbolName) != null) {
            throw new SemanticException("Duplicate function definition: " + symbolName);
        }

        symbolTable.insert(symbolName, symbol);
    }

    private void addActionSymbol(BallerinaAction action) {
        SymbolName actionSymbolName = action.getSymbolName();
        BType[] paramTypes = LangModelUtils.getTypesOfParams(action.getParameters());

        SymbolName symbolName =
                LangModelUtils.getActionSymName(actionSymbolName.getName(),
                        actionSymbolName.getConnectorName(),
                        actionSymbolName.getPkgName(), paramTypes);
        Symbol symbol = new Symbol(action, paramTypes, action.getReturnTypes());

        if (symbolTable.lookup(symbolName) != null) {
            throw new SemanticException("Duplicate action definition: " + symbolName);
        }

        symbolTable.insert(symbolName, symbol);
    }

    private void addConnectorSymbol(BallerinaConnector connector) {
        BType[] paramTypes = LangModelUtils.getTypesOfParams(connector.getParameters());
        Symbol symbol = new Symbol(connector, paramTypes);

        SymbolName symbolName = new SymbolName(connector.getPackageQualifiedName());
        if (symbolTable.lookup(symbolName) != null) {
            throw new SemanticException("Duplicate connector definition: " + symbolName);
        }

        symbolTable.insert(new SymbolName(connector.getPackageQualifiedName()), symbol);
    }

    private BType verifyBinaryArithmeticExprType(BinaryArithmeticExpression binaryArithmeticExpr) {
        BType type = verifyBinaryExprType(binaryArithmeticExpr);
        binaryArithmeticExpr.setType(type);
        return type;
    }

    private BType verifyBinaryCompareExprType(BinaryExpression binaryExpression) {
        BType type = verifyBinaryExprType(binaryExpression);
        binaryExpression.setType(BTypes.BOOLEAN_TYPE);
        return type;
    }

    private BType verifyBinaryExprType(BinaryExpression binaryExpr) {
        visitBinaryExpr(binaryExpr);

        Expression rExpr = binaryExpr.getRExpr();
        Expression lExpr = binaryExpr.getLExpr();

        if (lExpr.getType() != rExpr.getType()) {
            throw new BallerinaException(
                    "Incompatible types in binary expression: " + lExpr.getType() + " vs "
                            + rExpr.getType());
        }

        return lExpr.getType();
    }

    private void visitBinaryLogicalExpr(BinaryLogicalExpression expr) {
        visitBinaryExpr(expr);

        Expression rExpr = expr.getRExpr();
        Expression lExpr = expr.getLExpr();

        if (lExpr.getType() == BTypes.BOOLEAN_TYPE && rExpr.getType() == BTypes.BOOLEAN_TYPE) {
            expr.setType(BTypes.BOOLEAN_TYPE);
        } else {
            throw new BallerinaException("Incompatible types used for '&&' operator");
        }
    }

    private String getPackagePath(SymbolName symbolName) {
        // Extract the package name from the function name.
        // Function name should be in one of the following formats
        //      1)  sayHello                        ->  No package name. must be a function in the same package.
        //      2)  hello:sayHello                  ->  Function is defined in the 'hello' package.  User must have
        //                                              added import declaration. 'import wso2.connector.hello'.
        //      3)  wso2.connector.hello:sayHello   ->  Function is defined in the wso2.connector.hello package.

        // First check whether there is a packaged name attached to the function.
        String pkgPath = null;
        String pkgName = symbolName.getPkgName();

        if (pkgName != null) {
            // A package name is specified. Check whether it is already listed as an imported package.
            ImportPackage importPkg = importPkgMap.get(pkgName);

            if (importPkg != null) {
                // Found the imported package of the pkgName.
                // Retrieve the package path
                pkgPath = importPkg.getPath();

            } else {
                // Package name is not listed in the imported packages.
                // User may have used the fully qualified package path.
                // If this package is not available, linker will throw an error.
                pkgPath = pkgName;
            }
        }

        return pkgPath;
    }

    private Symbol getSymbol(SymbolName symName) {
        // Check whether this symName is declared
        Symbol symbol = symbolTable.lookup(symName);

        if (symbol == null) {
            throw new SemanticException("Undeclared variable: " + symName.getName());
        }

        return symbol;
    }

    private void linkFunction(FunctionInvocationExpr funcIExpr) {

        SymbolName funcName = funcIExpr.getFunctionName();
        String pkgPath = getPackagePath(funcName);
        funcName.setPkgName(pkgPath);


        Expression[] exprs = funcIExpr.getExprs();
        BType[] paramTypes = new BType[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        SymbolName symbolName = LangModelUtils.getSymNameWithParams(funcName.getName(), pkgPath, paramTypes);
        Symbol symbol = symbolTable.lookup(symbolName);
        if (symbol == null) {
            throw new LinkerException("Undefined function: " + funcIExpr.getFunctionName().getName());
        }

        // Package name null means the function is defined in the same bal file. 
        // Hence set the package name of the bal file as the function's package name.
        // TODO: Do this in a better way
        if (funcName.getPkgName() == null) {
            String fullPackageName = getPackagePath(new SymbolName(funcName.getName(), currentPkg));
            funcName.setPkgName(fullPackageName);
        }

        // Link
        Function function = symbol.getFunction();
        funcIExpr.setFunction(function);

        // TODO improve this once multiple return types are supported
        funcIExpr.setType((function.getReturnTypes().length != 0) ? function.getReturnTypes()[0] : null);
    }

    private void linkAction(ActionInvocationExpr actionIExpr) {
        // Can we do this bit in the linker
        SymbolName actionName = actionIExpr.getActionName();
        if (actionName.getConnectorName() == null) {
            throw new SemanticException("Connector type is not associated with the action invocation");
        }

        String pkgPath = getPackagePath(actionName);

        // Set the fully qualified package name
        actionName.setPkgName(pkgPath);

        Expression[] exprs = actionIExpr.getExprs();
        BType[] paramTypes = new BType[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        SymbolName symName = LangModelUtils.getActionSymName(actionName.getName(), actionName.getConnectorName(),
                pkgPath, paramTypes);

        Symbol symbol = symbolTable.lookup(symName);
        if (symbol == null) {
            throw new LinkerException("Undefined action: " +
                    actionIExpr.getActionName().getName());
        }

        // Package name null means the action is defined in the same bal file. 
        // Hence set the package name of the bal file as the action's package name.
        // TODO: Do this in a better way
        if (actionName.getPkgName() == null) {
            String fullPackageName = getPackagePath(new SymbolName(actionName.getName(), currentPkg));
            actionName.setPkgName(fullPackageName);
        }

        // Link
        Action action = symbol.getAction();
        actionIExpr.setAction(action);

        // TODO improve this once multiple return types are supported
        actionIExpr.setType((action.getReturnTypes().length != 0) ? action.getReturnTypes()[0] : null);
    }
}

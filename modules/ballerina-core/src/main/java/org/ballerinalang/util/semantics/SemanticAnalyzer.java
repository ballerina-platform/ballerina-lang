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
package org.ballerinalang.util.semantics;

import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.MemoryLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.model.Action;
import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.CallableUnit;
import org.ballerinalang.model.CompilationUnit;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.Operator;
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
import org.ballerinalang.model.expressions.BinaryArithmeticExpression;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.BinaryLogicalExpression;
import org.ballerinalang.model.expressions.CallableUnitInvocationExpr;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.MapStructInitKeyValueExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ReferenceExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructFieldAccessExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.invokers.MainInvoker;
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
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BConnectorType;
import org.ballerinalang.model.types.BExceptionType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BMessageType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BXMLType;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.types.TypeEdge;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.model.types.TypeVertex;
import org.ballerinalang.model.util.LangModelUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.natives.connectors.AbstractNativeConnector;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.LinkerException;
import org.ballerinalang.util.exceptions.SemanticErrors;
import org.ballerinalang.util.exceptions.SemanticException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code SemanticAnalyzer} analyzes semantic properties of a Ballerina program.
 *
 * @since 0.8.0
 */
public class SemanticAnalyzer implements NodeVisitor {
    private int stackFrameOffset = -1;
    private int staticMemAddrOffset = -1;
    private int connectorMemAddrOffset = -1;
    private int structMemAddrOffset = -1;
    private int workerMemAddrOffset = -1;
    private String currentPkg;
    private TypeLattice packageTypeLattice;
    private CallableUnit currentCallableUnit = null;
    private CallableUnit parentCallableUnit = null;
    // following pattern matches ${anyString} or ${anyString[int]} or ${anyString["anyString"]}
    private static final String patternString = "\\$\\{((\\w+)(\\[(\\d+|\\\"(\\w+)\\\")\\])?)\\}";
    private static final Pattern compiledPattern = Pattern.compile(patternString);

    private int whileStmtCount = 0;
    private SymbolScope currentScope;

    public SemanticAnalyzer(BLangProgram programScope) {
        currentScope = programScope;
    }

    @Override
    public void visit(BLangProgram bLangProgram) {
        BLangPackage mainPkg = bLangProgram.getMainPackage();
        if (bLangProgram.getProgramCategory() == BLangProgram.Category.MAIN_PROGRAM) {
            mainPkg.accept(this);

        } else if (bLangProgram.getProgramCategory() == BLangProgram.Category.SERVICE_PROGRAM) {
            BLangPackage[] servicePackages = bLangProgram.getServicePackages();
            for (BLangPackage servicePkg : servicePackages) {
                servicePkg.accept(this);
            }
        } else {
            BLangPackage[] libraryPackages = bLangProgram.getLibraryPackages();
            for (BLangPackage libraryPkg : libraryPackages) {
                libraryPkg.accept(this);
            }
        }

        int setSizeOfStaticMem = staticMemAddrOffset + 1;
        bLangProgram.setSizeOfStaticMem(setSizeOfStaticMem);
        staticMemAddrOffset = -1;
    }

    @Override
    public void visit(BLangPackage bLangPackage) {
        for (BLangPackage dependentPkg : bLangPackage.getDependentPackages()) {
            if (dependentPkg.isSymbolsDefined()) {
                continue;
            }

            dependentPkg.accept(this);
        }

        currentScope = bLangPackage;
        currentPkg = bLangPackage.getPackagePath();
        packageTypeLattice = bLangPackage.getTypeLattice();

        defineConstants(bLangPackage.getConsts());
        defineStructs(bLangPackage.getStructDefs());
        defineConnectors(bLangPackage.getConnectors());
        resolveStructFieldTypes(bLangPackage.getStructDefs());
        defineFunctions(bLangPackage.getFunctions());
        defineTypeMappers(bLangPackage.getTypeMappers());
        defineServices(bLangPackage.getServices());

        for (CompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
            compilationUnit.accept(this);
        }

        bLangPackage.setSymbolsDefined(true);
    }

    @Override
    public void visit(BallerinaFile bFile) {
    }

    @Override
    public void visit(ImportPackage importPkg) {
    }

    @Override
    public void visit(ConstDef constDef) {
        SimpleTypeName typeName = constDef.getTypeName();
        BType bType = BTypes.resolveType(typeName, currentScope, constDef.getNodeLocation());
        constDef.setType(bType);
        if (!BTypes.isValueType(bType)) {
            BLangExceptionHelper.throwSemanticError(constDef, SemanticErrors.INVALID_TYPE, typeName);
        }

        // Set memory location
        ConstantLocation memLocation = new ConstantLocation(++staticMemAddrOffset);
        constDef.setMemoryLocation(memLocation);

        // TODO Figure out how to evaluate constant values properly
        // TODO This should be done properly in the RuntimeEnvironment
        BasicLiteral basicLiteral = (BasicLiteral) constDef.getRhsExpr();
        constDef.setValue(basicLiteral.getBValue());
    }

    @Override
    public void visit(Service service) {
        // Visit the contents within a service
        // Open a new symbol scope
        openScope(service);

        for (VariableDefStmt variableDefStmt : service.getVariableDefStmts()) {
            variableDefStmt.accept(this);
        }

        // Visit the set of resources in a service
        for (Resource resource : service.getResources()) {
            resource.accept(this);
        }

        // Close the symbol scope
        closeScope();
    }

    @Override
    public void visit(BallerinaConnectorDef connector) {
        // Open the connector namespace
        openScope(connector);

        for (ParameterDef parameterDef : connector.getParameterDefs()) {
            parameterDef.setMemoryLocation(new ConnectorVarLocation(++connectorMemAddrOffset));
            parameterDef.accept(this);
        }

        for (VariableDefStmt variableDefStmt : connector.getVariableDefStmts()) {
            variableDefStmt.accept(this);
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
        openScope(resource);
        currentCallableUnit = resource;

        // TODO Check whether the reply statement is missing. Ignore if the function does not return anything.
        //checkForMissingReplyStmt(resource);

        for (ParameterDef parameterDef : resource.getParameterDefs()) {
            parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            parameterDef.accept(this);
        }

        for (Worker worker : resource.getWorkers()) {
            visit(worker);
            addWorkerSymbol(worker);
        }

        BlockStmt blockStmt = resource.getResourceBody();
        blockStmt.accept(this);

        int sizeOfStackFrame = stackFrameOffset + 1;
        resource.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        stackFrameOffset = -1;
        currentCallableUnit = null;
        closeScope();
    }

    @Override
    public void visit(BallerinaFunction function) {
        // Open a new symbol scope
        openScope(function);
        currentCallableUnit = function;

        // Check whether the return statement is missing. Ignore if the function does not return anything.
        // TODO Define proper error message codes
        //checkForMissingReturnStmt(function, "missing return statement at end of function");

        for (ParameterDef parameterDef : function.getParameterDefs()) {
            parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            parameterDef.accept(this);
        }

        for (ParameterDef parameterDef : function.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            }

            parameterDef.accept(this);
        }

        if (!function.isNative()) {
            for (Worker worker : function.getWorkers()) {
                worker.accept(this);
                addWorkerSymbol(worker);
            }

            BlockStmt blockStmt = function.getCallableUnitBody();
            blockStmt.accept(this);
        }
        // Here we need to calculate size of the BValue arrays which will be created in the stack frame
        // Values in the stack frame are stored in the following order.
        // -- Parameter values --
        // -- Local var values --
        // -- Temp values      --
        // -- Return values    --
        // These temp values are results of intermediate expression evaluations.
        int sizeOfStackFrame = stackFrameOffset + 1;
        function.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        stackFrameOffset = -1;
        currentCallableUnit = null;
        closeScope();
    }

    @Override
    public void visit(BTypeMapper typeMapper) {
        // Open a new symbol scope
        openScope(typeMapper);
        currentCallableUnit = typeMapper;

        // Check whether the return statement is missing. Ignore if the function does not return anything.
        // TODO Define proper error message codes
        //checkForMissingReturnStmt(function, "missing return statement at end of function");

        for (ParameterDef parameterDef : typeMapper.getParameterDefs()) {
            parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            parameterDef.accept(this);
        }

//        for (VariableDef variableDef : typeMapper.getVariableDefs()) {
//            stackFrameOffset++;
//            visit(variableDef);
//        }

        for (ParameterDef parameterDef : typeMapper.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            }

            parameterDef.accept(this);
        }

        if (!typeMapper.isNative()) {
            BlockStmt blockStmt = typeMapper.getCallableUnitBody();
            currentScope = blockStmt;
            blockStmt.accept(this);
            currentScope = blockStmt.getEnclosingScope();
        }

        // Here we need to calculate size of the BValue arrays which will be created in the stack frame
        // Values in the stack frame are stored in the following order.
        // -- Parameter values --
        // -- Local var values --
        // -- Temp values      --
        // -- Return values    --
        // These temp values are results of intermediate expression evaluations.
        int sizeOfStackFrame = stackFrameOffset + 1;
        typeMapper.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        stackFrameOffset = -1;
        currentCallableUnit = null;
        closeScope();
    }

    @Override
    public void visit(BallerinaAction action) {
        // Open a new symbol scope
        openScope(action);
        currentCallableUnit = action;

        for (ParameterDef parameterDef : action.getParameterDefs()) {
            parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            parameterDef.accept(this);
        }

        // First parameter should be of type connector in which these actions are defined.
        ParameterDef firstParamDef = action.getParameterDefs()[0];
        if (firstParamDef.getType() != action.getConnectorDef()) {
            BLangExceptionHelper.throwSemanticError(action, SemanticErrors.INCOMPATIBLE_TYPES,
                    action.getConnectorDef(), firstParamDef.getType());
        }

        for (ParameterDef parameterDef : action.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            }

            parameterDef.accept(this);
        }

        if (!action.isNative()) {
            for (Worker worker : action.getWorkers()) {
                worker.accept(this);
                addWorkerSymbol(worker);
            }

            BlockStmt blockStmt = action.getCallableUnitBody();
            blockStmt.accept(this);
        }

        // Here we need to calculate size of the BValue arrays which will be created in the stack frame
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
        currentCallableUnit = null;
        closeScope();
    }

    @Override
    public void visit(Worker worker) {
        // Open a new symbol scope. This is done manually to avoid falling back to package scope
        SymbolScope parentScope = currentScope;
        currentScope = worker;
        parentCallableUnit = currentCallableUnit;
        currentCallableUnit = worker;

        // Check whether the return statement is missing. Ignore if the function does not return anything.
        // TODO Define proper error message codes
        //checkForMissingReturnStmt(function, "missing return statement at end of function");

        for (ParameterDef parameterDef : worker.getParameterDefs()) {
            parameterDef.setMemoryLocation(new WorkerVarLocation(++workerMemAddrOffset));
            parameterDef.accept(this);
        }

        for (ParameterDef parameterDef : worker.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                parameterDef.setMemoryLocation(new WorkerVarLocation(++workerMemAddrOffset));
            }

            parameterDef.accept(this);
        }

        BlockStmt blockStmt = worker.getCallableUnitBody();
        blockStmt.accept(this);

        // Here we need to calculate size of the BValue arrays which will be created in the stack frame
        // Values in the stack frame are stored in the following order.
        // -- Parameter values --
        // -- Local var values --
        // -- Temp values      --
        // -- Return values    --
        // These temp values are results of intermediate expression evaluations.
        int sizeOfStackFrame = workerMemAddrOffset + 1;
        worker.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        workerMemAddrOffset = -1;
        currentCallableUnit = parentCallableUnit;
        // Close symbol scope. This is done manually to avoid falling back to package scope
        currentScope = parentScope;
    }

    private void addWorkerSymbol(Worker worker) {
        SymbolName symbolName = worker.getSymbolName();
        BLangSymbol varSymbol = currentScope.resolve(symbolName);
        if (varSymbol != null && varSymbol.getSymbolScope().getScopeName() == currentScope.getScopeName()) {
            BLangExceptionHelper.throwSemanticError(worker,
                    SemanticErrors.REDECLARED_SYMBOL, worker.getName());
        }
        currentScope.define(symbolName, worker);
    }

    @Override
    public void visit(StructDef structDef) {
        for (VariableDef field : structDef.getFields()) {
            MemoryLocation location = new StructVarLocation(++structMemAddrOffset);
            field.setMemoryLocation(location);
        }

        structDef.setStructMemorySize(structMemAddrOffset + 1);
        structMemAddrOffset = -1;
    }

    @Override
    public void visit(Annotation annotation) {

    }

    @Override
    public void visit(ParameterDef paramDef) {
        BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
        paramDef.setType(bType);
    }

    @Override
    public void visit(VariableDef varDef) {
    }


    // Visit statements

    @Override
    public void visit(VariableDefStmt varDefStmt) {
        // Resolves the type of the variable
        VariableDef varDef = varDefStmt.getVariableDef();
        BType varBType = BTypes.resolveType(varDef.getTypeName(), currentScope, varDef.getNodeLocation());
        varDef.setType(varBType);

        // Check whether this variable is already defined, if not define it.
        SymbolName symbolName = new SymbolName(varDef.getName());
        BLangSymbol varSymbol = currentScope.resolve(symbolName);
        if (varSymbol != null && varSymbol.getSymbolScope().getScopeName() == currentScope.getScopeName()) {
            BLangExceptionHelper.throwSemanticError(varDef, SemanticErrors.REDECLARED_SYMBOL, varDef.getName());
        }
        currentScope.define(symbolName, varDef);

        // Set memory location
        setMemoryLocation(varDef);

        Expression rExpr = varDefStmt.getRExpr();
        if (rExpr == null) {
            return;
        }

        if (rExpr instanceof RefTypeInitExpr) {
            RefTypeInitExpr refTypeInitExpr = (RefTypeInitExpr) rExpr;

            if (varBType instanceof BMapType) {
                refTypeInitExpr = new MapInitExpr(refTypeInitExpr.getNodeLocation(), refTypeInitExpr.getArgExprs());
                varDefStmt.setRExpr(refTypeInitExpr);
            } else if (varBType instanceof StructDef) {
                refTypeInitExpr = new StructInitExpr(refTypeInitExpr.getNodeLocation(), refTypeInitExpr.getArgExprs());
                varDefStmt.setRExpr(refTypeInitExpr);
            }

            refTypeInitExpr.setInheritedType(varBType);
            refTypeInitExpr.accept(this);
            return;
        }


        if (rExpr instanceof FunctionInvocationExpr || rExpr instanceof ActionInvocationExpr) {
            rExpr.accept(this);

            CallableUnitInvocationExpr invocationExpr = (CallableUnitInvocationExpr) rExpr;
            BType[] returnTypes = invocationExpr.getTypes();
            if (returnTypes.length != 1) {
                BLangExceptionHelper.throwSemanticError(varDefStmt, SemanticErrors.ASSIGNMENT_COUNT_MISMATCH, "1",
                        returnTypes.length);
            } else if ((varBType != BTypes.typeMap) && (returnTypes[0] != BTypes.typeMap) &&
                    (!varBType.equals(returnTypes[0]))) {

                TypeCastExpression newExpr = checkWideningPossible(varBType, rExpr);
                if (newExpr != null) {
                    newExpr.accept(this);
                    varDefStmt.setRExpr(newExpr);
                } else {
                    BLangExceptionHelper.throwSemanticError(rExpr, SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT,
                            returnTypes[0], varBType);
                }
            }

            return;
        }

        visitSingleValueExpr(rExpr);
        BType rType = rExpr.getType();
        if (rExpr instanceof TypeCastExpression && rType == null) {
            rType = BTypes.resolveType(((TypeCastExpression) rExpr).getTypeName(), currentScope, null);
        }

        // TODO Remove the MAP related logic when type casting is implemented
        if ((varBType != BTypes.typeMap) && (rType != BTypes.typeMap) &&
                (!varBType.equals(rType))) {

            TypeCastExpression newExpr = checkWideningPossible(varBType, rExpr);
            if (newExpr != null) {
                newExpr.accept(this);
                varDefStmt.setRExpr(newExpr);
            } else {
                BLangExceptionHelper.throwSemanticError(varDefStmt, SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT,
                        rExpr.getType(), varBType);
            }
        }
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        Expression[] lExprs = assignStmt.getLExprs();
        visitLExprsOfAssignment(assignStmt, lExprs);

        Expression rExpr = assignStmt.getRExpr();
        if (rExpr instanceof FunctionInvocationExpr || rExpr instanceof ActionInvocationExpr) {
            rExpr.accept(this);
            checkForMultiAssignmentErrors(assignStmt, lExprs, (CallableUnitInvocationExpr) rExpr);
            return;
        }

        // Now we know that this is a single value assignment statement.
        Expression lExpr = assignStmt.getLExprs()[0];
        BType lExprType = lExpr.getType();

        if (rExpr instanceof RefTypeInitExpr) {
            RefTypeInitExpr refTypeInitExpr = (RefTypeInitExpr) rExpr;

            if (lExprType instanceof BMapType) {
                refTypeInitExpr = new MapInitExpr(refTypeInitExpr.getNodeLocation(), refTypeInitExpr.getArgExprs());
                assignStmt.setRExpr(refTypeInitExpr);
            } else if (lExprType instanceof StructDef) {
                refTypeInitExpr = new StructInitExpr(refTypeInitExpr.getNodeLocation(), refTypeInitExpr.getArgExprs());
                assignStmt.setRExpr(refTypeInitExpr);
            }

            refTypeInitExpr.setInheritedType(lExprType);
            refTypeInitExpr.accept(this);
            return;
        }

        visitSingleValueExpr(rExpr);
        BType rType = rExpr.getType();
        if (rExpr instanceof TypeCastExpression && rType == null) {
            rType = BTypes.resolveType(((TypeCastExpression) rExpr).getTypeName(), currentScope, null);
        }

        // TODO Remove the MAP related logic when type casting is implemented
        if ((lExprType != BTypes.typeMap) && (rType != BTypes.typeMap) &&
                (!lExprType.equals(rType))) {

            TypeCastExpression newExpr = checkWideningPossible(lExpr.getType(), rExpr);
            if (newExpr != null) {
                newExpr.accept(this);
                assignStmt.setRhsExpr(newExpr);
            } else {
                BLangExceptionHelper.throwSemanticError(lExpr, SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT,
                        rExpr.getType(), lExpr.getType());
            }
        }
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        openScope(blockStmt);

        for (int stmtIndex = 0; stmtIndex < blockStmt.getStatements().length; stmtIndex++) {
            Statement stmt = blockStmt.getStatements()[stmtIndex];
            if (stmt instanceof BreakStmt && whileStmtCount < 1) {
                BLangExceptionHelper.throwSemanticError(stmt,
                        SemanticErrors.BREAK_STMT_NOT_ALLOWED_HERE);
            }
            if (stmt instanceof ReturnStmt || stmt instanceof ReplyStmt || stmt instanceof BreakStmt
                    || stmt instanceof ThrowStmt) {
                checkUnreachableStmt(blockStmt.getStatements(), ++stmtIndex);
            }
            stmt.accept(this);
        }

        closeScope();
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        Expression expr = ifElseStmt.getCondition();
        visitSingleValueExpr(expr);

        if (expr.getType() != BTypes.typeBoolean) {
            BLangExceptionHelper
                    .throwSemanticError(ifElseStmt, SemanticErrors.INCOMPATIBLE_TYPES_BOOLEAN_EXPECTED, expr.getType());
        }

        Statement thenBody = ifElseStmt.getThenBody();
        thenBody.accept(this);

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            visitSingleValueExpr(elseIfCondition);

            if (elseIfCondition.getType() != BTypes.typeBoolean) {
                BLangExceptionHelper.throwSemanticError(ifElseStmt, SemanticErrors.INCOMPATIBLE_TYPES_BOOLEAN_EXPECTED,
                        elseIfCondition.getType());
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
        whileStmtCount++;
        Expression expr = whileStmt.getCondition();
        visitSingleValueExpr(expr);

        if (expr.getType() != BTypes.typeBoolean) {
            BLangExceptionHelper
                    .throwSemanticError(whileStmt, SemanticErrors.INCOMPATIBLE_TYPES_BOOLEAN_EXPECTED, expr.getType());
        }

        BlockStmt blockStmt = whileStmt.getBody();
        if (blockStmt.getStatements().length == 0) {
            // This can be optimized later to skip the while statement
            BLangExceptionHelper.throwSemanticError(blockStmt, SemanticErrors.NO_STATEMENTS_WHILE_LOOP);
        }

        blockStmt.accept(this);
        whileStmtCount--;
    }

    @Override
    public void visit(BreakStmt breakStmt) {

    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        tryCatchStmt.getTryBlock().accept(this);
        tryCatchStmt.getCatchBlock().getParameterDef().setMemoryLocation(new StackVarLocation(++stackFrameOffset));
        tryCatchStmt.getCatchBlock().getParameterDef().accept(this);
        tryCatchStmt.getCatchBlock().getCatchBlockStmt().accept(this);
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        throwStmt.getExpr().accept(this);
        if (throwStmt.getExpr() instanceof VariableRefExpr) {
            if (throwStmt.getExpr().getType() instanceof BExceptionType) {
                return;
            }
        } else {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) throwStmt.getExpr();
            if (!funcIExpr.isMultiReturnExpr() && funcIExpr.getTypes().length > 0
                    && funcIExpr.getTypes()[0] instanceof BExceptionType) {
                return;
            }
        }
        throw new SemanticException(throwStmt.getNodeLocation().getFileName() + ":" +
                throwStmt.getNodeLocation().getLineNumber() +
                ": only a variable reference of type 'exception' is allowed in throw statement");
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
    public void visit(WorkerInvocationStmt workerInvocationStmt) {
        VariableRefExpr variableRefExpr = workerInvocationStmt.getInMsg();
        variableRefExpr.accept(this);

        linkWorker(workerInvocationStmt);

        //Find the return types of this function invocation expression.
        ParameterDef[] returnParams = workerInvocationStmt.getCallableUnit().getReturnParameters();
        BType[] returnTypes = new BType[returnParams.length];
        for (int i = 0; i < returnParams.length; i++) {
            returnTypes[i] = returnParams[i].getType();
        }
        workerInvocationStmt.setTypes(returnTypes);
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        String workerName = workerReplyStmt.getWorkerName();
        SymbolName workerSymbol = new SymbolName(workerName);
        VariableRefExpr variableRefExpr = workerReplyStmt.getReceiveExpr();
        variableRefExpr.accept(this);
        
        BLangSymbol worker = currentScope.resolve(workerSymbol);
        if (!(worker instanceof Worker)) {
            BLangExceptionHelper.throwSemanticError(variableRefExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, 
                    workerSymbol);
        }
        
        workerReplyStmt.setWorker((Worker) worker);
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        openScope(forkJoinStmt);
        // Visit incoming message
        VariableRefExpr messageReference = forkJoinStmt.getMessageReference();
        messageReference.accept(this);

        if (!messageReference.getType().equals(BTypes.typeMessage)) {
            throw new SemanticException("Incompatible types: expected a message in " +
                    messageReference.getNodeLocation().getFileName() + ":" +
                    messageReference.getNodeLocation().getLineNumber());
        }

        // Visit workers
        for (Worker worker: forkJoinStmt.getWorkers()) {
            worker.accept(this);
        }

        closeScope();

        // Visit join condition
        ForkJoinStmt.Join join = forkJoinStmt.getJoin();
        openScope(join);
        ParameterDef parameter = join.getJoinResult();
        parameter.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
        parameter.accept(this);
        join.define(parameter.getSymbolName(), parameter);

        if (!(parameter.getType() instanceof BArrayType &&
                (((BArrayType) parameter.getType()).getElementType() == BTypes.typeMessage))) {
            throw new SemanticException("Incompatible types: expected a message[] in " +
                    parameter.getNodeLocation().getFileName() + ":" + parameter.getNodeLocation().getLineNumber());
        }

        // Visit join body
        Statement joinBody = join.getJoinBlock();
        joinBody.accept(this);
        closeScope();

        // Visit timeout condition
        ForkJoinStmt.Timeout timeout = forkJoinStmt.getTimeout();
        openScope(timeout);
        Expression timeoutExpr = timeout.getTimeoutExpression();
        timeoutExpr.accept(this);

        ParameterDef timeoutParam = timeout.getTimeoutResult();
        timeoutParam.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
        timeoutParam.accept(this);
        timeout.define(timeoutParam.getSymbolName(), timeoutParam);

        if (!(timeoutParam.getType() instanceof BArrayType &&
                (((BArrayType) timeoutParam.getType()).getElementType() == BTypes.typeMessage))) {
            throw new SemanticException("Incompatible types: expected a message[] in " +
                    timeoutParam.getNodeLocation().getFileName() + ":" +
                    timeoutParam.getNodeLocation().getLineNumber());
        }

        // Visit timeout body
        Statement timeoutBody = timeout.getTimeoutBlock();
        timeoutBody.accept(this);
        closeScope();

    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        if (currentCallableUnit instanceof Function) {
            BLangExceptionHelper.throwSemanticError(currentCallableUnit,
                    SemanticErrors.REPLY_STATEMENT_CANNOT_USED_IN_FUNCTION);
        } else if (currentCallableUnit instanceof Action) {
            BLangExceptionHelper.throwSemanticError(currentCallableUnit,
                    SemanticErrors.REPLY_STATEMENT_CANNOT_USED_IN_ACTION);
        }

        if (replyStmt.getReplyExpr() instanceof ActionInvocationExpr) {
            BLangExceptionHelper.throwSemanticError(currentCallableUnit,
                    SemanticErrors.ACTION_INVOCATION_NOT_ALLOWED_IN_REPLY);
        }
        
        Expression replyExpr = replyStmt.getReplyExpr();
        visitSingleValueExpr(replyExpr);
        
        // reply statement supports only message type
        if (replyExpr.getType() != BTypes.typeMessage) {
            BLangExceptionHelper.throwSemanticError(replyExpr, SemanticErrors.INCOMPATIBLE_TYPES, 
                    BTypes.typeMessage, replyExpr.getType());
        }
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        if (currentCallableUnit instanceof Resource) {
            BLangExceptionHelper.throwSemanticError(returnStmt, SemanticErrors.RETURN_CANNOT_USED_IN_RESOURCE);
        }

        // Expressions that this return statement contains.
        Expression[] returnArgExprs = returnStmt.getExprs();

        // Return parameters of the current function or actions
        ParameterDef[] returnParamsOfCU = currentCallableUnit.getReturnParameters();

        if (returnArgExprs.length == 0 && returnParamsOfCU.length == 0) {
            // Return stmt has no expressions and function/action does not return anything. Just return.
            return;
        }

        // Return stmt has no expressions, but function/action has returns. Check whether they are named returns
        if (returnArgExprs.length == 0 && returnParamsOfCU[0].getName() != null) {
            // This function/action has named return parameters.
            Expression[] returnExprs = new Expression[returnParamsOfCU.length];
            for (int i = 0; i < returnParamsOfCU.length; i++) {
                VariableRefExpr variableRefExpr = new VariableRefExpr(returnStmt.getNodeLocation(),
                        returnParamsOfCU[i].getSymbolName());
                visit(variableRefExpr);
                returnExprs[i] = variableRefExpr;
            }
            returnStmt.setExprs(returnExprs);
            return;

        } else if (returnArgExprs.length == 0) {
            // This function/action does not contain named return parameters.
            // Therefore this is a semantic error.
            BLangExceptionHelper.throwSemanticError(returnStmt, SemanticErrors.NOT_ENOUGH_ARGUMENTS_TO_RETURN);
        }

        BType[] typesOfReturnExprs = new BType[returnArgExprs.length];
        for (int i = 0; i < returnArgExprs.length; i++) {
            Expression returnArgExpr = returnArgExprs[i];
            returnArgExpr.accept(this);
            typesOfReturnExprs[i] = returnArgExpr.getType();
        }

        // Now check whether this return contains a function invocation expression which returns multiple values
        if (returnArgExprs.length == 1 && returnArgExprs[0] instanceof FunctionInvocationExpr) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) returnArgExprs[0];
            // Return types of the function invocations expression
            BType[] funcIExprReturnTypes = funcIExpr.getTypes();
            if (funcIExprReturnTypes.length > returnParamsOfCU.length) {
                BLangExceptionHelper.throwSemanticError(returnStmt, SemanticErrors.TOO_MANY_ARGUMENTS_TO_RETURN);

            } else if (funcIExprReturnTypes.length < returnParamsOfCU.length) {
                BLangExceptionHelper.throwSemanticError(returnStmt, SemanticErrors.NOT_ENOUGH_ARGUMENTS_TO_RETURN);

            }

            for (int i = 0; i < returnParamsOfCU.length; i++) {
                if (!funcIExprReturnTypes[i].equals(returnParamsOfCU[i].getType())) {
                    BLangExceptionHelper.throwSemanticError(returnStmt,
                            SemanticErrors.CANNOT_USE_TYPE_IN_RETURN_STATEMENT, funcIExprReturnTypes[i],
                            returnParamsOfCU[i].getType());
                }
            }

            return;
        }

        if (typesOfReturnExprs.length > returnParamsOfCU.length) {
            BLangExceptionHelper.throwSemanticError(returnStmt, SemanticErrors.TOO_MANY_ARGUMENTS_TO_RETURN);

        } else if (typesOfReturnExprs.length < returnParamsOfCU.length) {
            BLangExceptionHelper.throwSemanticError(returnStmt, SemanticErrors.NOT_ENOUGH_ARGUMENTS_TO_RETURN);

        } else {
            // Now we know that lengths for both arrays are equal.
            // Let's check their types
            for (int i = 0; i < returnParamsOfCU.length; i++) {
                // Check for ActionInvocationExprs in return arguments
                if (returnArgExprs[i] instanceof ActionInvocationExpr) {
                    BLangExceptionHelper.throwSemanticError(returnStmt,
                            SemanticErrors.ACTION_INVOCATION_NOT_ALLOWED_IN_RETURN);
                }

                // Except for the first argument in return statement, fheck for FunctionInvocationExprs which return
                // multiple values.
                if (returnArgExprs[i] instanceof FunctionInvocationExpr) {
                    FunctionInvocationExpr funcIExpr = ((FunctionInvocationExpr) returnArgExprs[i]);
                    if (funcIExpr.getTypes().length > 1) {
                        BLangExceptionHelper.throwSemanticError(returnStmt,
                                SemanticErrors.MULTIPLE_VALUE_IN_SINGLE_VALUE_CONTEXT,
                                funcIExpr.getCallableUnit().getName());
                    }
                }

                if (!typesOfReturnExprs[i].equals(returnParamsOfCU[i].getType())) {
                    BLangExceptionHelper.throwSemanticError(returnStmt,
                            SemanticErrors.CANNOT_USE_TYPE_IN_RETURN_STATEMENT, typesOfReturnExprs[i],
                            returnParamsOfCU[i].getType());
                }
            }
        }
    }


    // Expressions

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        visitSingleValueExpr(instanceCreationExpr);

        if (BTypes.isValueType(instanceCreationExpr.getType())) {
            BLangExceptionHelper.throwSemanticError(instanceCreationExpr,
                    SemanticErrors.CANNOT_USE_CREATE_FOR_VALUE_TYPES, instanceCreationExpr.getType());
        }
        // TODO here the type shouldn't be a value type
//        Expression expr = instanceCreationExpr.getRExpr();
//        expr.accept(this);

    }

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        Expression[] exprs = funcIExpr.getArgExprs();
        for (Expression expr : exprs) {
            visitSingleValueExpr(expr);
        }

        linkFunction(funcIExpr);

        //Find the return types of this function invocation expression.
        BType[] returnParamTypes = funcIExpr.getCallableUnit().getReturnParamTypes();
        funcIExpr.setTypes(returnParamTypes);
    }

    // TODO Duplicate code. fix me
    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        Expression[] exprs = actionIExpr.getArgExprs();
        for (Expression expr : exprs) {
            visitSingleValueExpr(expr);
        }

        linkAction(actionIExpr);

        //Find the return types of this function invocation expression.
        BType[] returnParamTypes = actionIExpr.getCallableUnit().getReturnParamTypes();
        actionIExpr.setTypes(returnParamTypes);
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
        BType bType = BTypes.resolveType(basicLiteral.getTypeName(), currentScope, basicLiteral.getNodeLocation());
        basicLiteral.setType(bType);
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        BType arithmeticExprType = verifyBinaryArithmeticExprType(divideExpr);

        if (arithmeticExprType == BTypes.typeInt) {
            divideExpr.setEvalFunc(DivideExpr.DIV_INT_FUNC);

        } else if (arithmeticExprType == BTypes.typeFloat) {
            divideExpr.setEvalFunc(DivideExpr.DIV_FLOAT_FUNC);

        } else if (arithmeticExprType == BTypes.typeDouble) {
            divideExpr.setEvalFunc(DivideExpr.DIV_DOUBLE_FUNC);

        } else if (arithmeticExprType == BTypes.typeLong) {
            divideExpr.setEvalFunc(DivideExpr.DIV_LONG_FUNC);

        } else {
            throwInvalidBinaryOpError(divideExpr);
        }
    }

    @Override
    public void visit(ModExpression modExpression) {
        BType arithmeticExprType = verifyBinaryArithmeticExprType(modExpression);

        if (arithmeticExprType == BTypes.typeInt) {
            modExpression.setEvalFunc(ModExpression.MOD_INT_FUNC);

        } else if (arithmeticExprType == BTypes.typeFloat) {
            modExpression.setEvalFunc(ModExpression.MOD_FLOAT_FUNC);

        } else if (arithmeticExprType == BTypes.typeDouble) {
            modExpression.setEvalFunc(ModExpression.MOD_DOUBLE_FUNC);

        } else if (arithmeticExprType == BTypes.typeLong) {
            modExpression.setEvalFunc(ModExpression.MOD_LONG_FUNC);

        } else {
            throwInvalidBinaryOpError(modExpression);
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpr) {
        visitSingleValueExpr(unaryExpr.getRExpr());
        unaryExpr.setType(unaryExpr.getRExpr().getType());

        if (Operator.SUB.equals(unaryExpr.getOperator())) {
            if (unaryExpr.getType() == BTypes.typeInt) {
                unaryExpr.setEvalFunc(UnaryExpression.NEGATIVE_INT_FUNC);
            } else if (unaryExpr.getType() == BTypes.typeDouble) {
                unaryExpr.setEvalFunc(UnaryExpression.NEGATIVE_DOUBLE_FUNC);
            } else if (unaryExpr.getType() == BTypes.typeLong) {
                unaryExpr.setEvalFunc(UnaryExpression.NEGATIVE_LONG_FUNC);
            } else if (unaryExpr.getType() == BTypes.typeFloat) {
                unaryExpr.setEvalFunc(UnaryExpression.NEGATIVE_FLOAT_FUNC);
            } else {
                throwInvalidUnaryOpError(unaryExpr);
            }
        } else if (Operator.ADD.equals(unaryExpr.getOperator())) {
            if (unaryExpr.getType() == BTypes.typeInt) {
                unaryExpr.setEvalFunc(UnaryExpression.POSITIVE_INT_FUNC);
            } else if (unaryExpr.getType() == BTypes.typeDouble) {
                unaryExpr.setEvalFunc(UnaryExpression.POSITIVE_DOUBLE_FUNC);
            } else if (unaryExpr.getType() == BTypes.typeLong) {
                unaryExpr.setEvalFunc(UnaryExpression.POSITIVE_LONG_FUNC);
            } else if (unaryExpr.getType() == BTypes.typeFloat) {
                unaryExpr.setEvalFunc(UnaryExpression.POSITIVE_FLOAT_FUNC);
            } else {
                throwInvalidUnaryOpError(unaryExpr);
            }

        } else if (Operator.NOT.equals(unaryExpr.getOperator())) {
            if (unaryExpr.getType() == BTypes.typeBoolean) {
                unaryExpr.setEvalFunc(UnaryExpression.NOT_BOOLEAN_FUNC);
            } else {
                throwInvalidUnaryOpError(unaryExpr);
            }

        } else {
            BLangExceptionHelper.throwSemanticError(unaryExpr, SemanticErrors.UNKNOWN_OPERATOR_IN_UNARY,
                    unaryExpr.getOperator());
        }
    }

    @Override
    public void visit(AddExpression addExpr) {
        BType arithmeticExprType = verifyBinaryArithmeticExprType(addExpr);

        if (arithmeticExprType == BTypes.typeInt) {
            addExpr.setEvalFunc(AddExpression.ADD_INT_FUNC);

        } else if (arithmeticExprType == BTypes.typeFloat) {
            addExpr.setEvalFunc(AddExpression.ADD_FLOAT_FUNC);

        } else if (arithmeticExprType == BTypes.typeLong) {
            addExpr.setEvalFunc(AddExpression.ADD_LONG_FUNC);

        } else if (arithmeticExprType == BTypes.typeDouble) {
            addExpr.setEvalFunc(AddExpression.ADD_DOUBLE_FUNC);

        } else if (arithmeticExprType == BTypes.typeString) {
            addExpr.setEvalFunc(AddExpression.ADD_STRING_FUNC);

        } else {
            throwInvalidBinaryOpError(addExpr);
        }
    }

    @Override
    public void visit(MultExpression multExpr) {
        BType binaryExprType = verifyBinaryArithmeticExprType(multExpr);

        if (binaryExprType == BTypes.typeInt) {
            multExpr.setEvalFunc(MultExpression.MULT_INT_FUNC);

        } else if (binaryExprType == BTypes.typeFloat) {
            multExpr.setEvalFunc(MultExpression.MULT_FLOAT_FUNC);

        } else if (binaryExprType == BTypes.typeDouble) {
            multExpr.setEvalFunc(MultExpression.MULT_DOUBLE_FUNC);

        } else if (binaryExprType == BTypes.typeLong) {
            multExpr.setEvalFunc(MultExpression.MULT_LONG_FUNC);

        } else {
            throwInvalidBinaryOpError(multExpr);
        }
    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        BType binaryExprType = verifyBinaryArithmeticExprType(subtractExpr);

        if (binaryExprType == BTypes.typeInt) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_INT_FUNC);

        } else if (binaryExprType == BTypes.typeFloat) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_FLOAT_FUNC);

        } else if (binaryExprType == BTypes.typeDouble) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_DOUBLE_FUNC);

        } else if (binaryExprType == BTypes.typeLong) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_LONG_FUNC);

        } else {
            throwInvalidBinaryOpError(subtractExpr);
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
    public void visit(EqualExpression equalExpr) {
        BType compareExprType = verifyBinaryCompareExprType(equalExpr);

        if (compareExprType == BTypes.typeInt) {
            equalExpr.setEvalFunc(EqualExpression.EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.typeDouble) {
            equalExpr.setEvalFunc(EqualExpression.EQUAL_DOUBLE_FUNC);

        } else if (compareExprType == BTypes.typeFloat) {
            equalExpr.setEvalFunc(EqualExpression.EQUAL_FLOAT_FUNC);

        } else if (compareExprType == BTypes.typeBoolean) {
            equalExpr.setEvalFunc(EqualExpression.EQUAL_BOOLEAN_FUNC);

        } else if (compareExprType == BTypes.typeString) {
            equalExpr.setEvalFunc(EqualExpression.EQUAL_STRING_FUNC);

        } else {
            throwInvalidBinaryOpError(equalExpr);
        }
    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {
        BType compareExprType = verifyBinaryCompareExprType(notEqualExpr);

        if (compareExprType == BTypes.typeInt) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.typeFloat) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_FLOAT_FUNC);

        } else if (compareExprType == BTypes.typeBoolean) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_BOOLEAN_FUNC);

        } else if (compareExprType == BTypes.typeString) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_STRING_FUNC);

        } else {
            throwInvalidBinaryOpError(notEqualExpr);
        }
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {
        BType compareExprType = verifyBinaryCompareExprType(greaterEqualExpr);

        if (compareExprType == BTypes.typeInt) {
            greaterEqualExpr.setEvalFunc(GreaterEqualExpression.GREATER_EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.typeFloat) {
            greaterEqualExpr.setEvalFunc(GreaterEqualExpression.GREATER_EQUAL_FLOAT_FUNC);

        } else {
            throwInvalidBinaryOpError(greaterEqualExpr);
        }
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        BType compareExprType = verifyBinaryCompareExprType(greaterThanExpr);

        if (compareExprType == BTypes.typeInt) {
            greaterThanExpr.setEvalFunc(GreaterThanExpression.GREATER_THAN_INT_FUNC);

        } else if (compareExprType == BTypes.typeFloat) {
            greaterThanExpr.setEvalFunc(GreaterThanExpression.GREATER_THAN_FLOAT_FUNC);

        } else {
            throwInvalidBinaryOpError(greaterThanExpr);
        }
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        BType compareExprType = verifyBinaryCompareExprType(lessEqualExpr);

        if (compareExprType == BTypes.typeInt) {
            lessEqualExpr.setEvalFunc(LessEqualExpression.LESS_EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.typeFloat) {
            lessEqualExpr.setEvalFunc(LessEqualExpression.LESS_EQUAL_FLOAT_FUNC);

        } else {
            throwInvalidBinaryOpError(lessEqualExpr);
        }
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        BType compareExprType = verifyBinaryCompareExprType(lessThanExpr);

        if (compareExprType == BTypes.typeInt) {
            lessThanExpr.setEvalFunc(LessThanExpression.LESS_THAN_INT_FUNC);

        } else if (compareExprType == BTypes.typeFloat) {
            lessThanExpr.setEvalFunc(LessThanExpression.LESS_THAN_FLOAT_FUNC);

        } else {
            throwInvalidBinaryOpError(lessThanExpr);
        }
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        // Here we assume that rExpr of arrays access expression is always a variable reference expression.
        // This according to the grammar
        VariableRefExpr arrayMapVarRefExpr = (VariableRefExpr) arrayMapAccessExpr.getRExpr();
        arrayMapVarRefExpr.accept(this);

        handleArrayType(arrayMapAccessExpr);
    }

    @Override
    public void visit(StructFieldAccessExpr structFieldAccessExpr) {
        visitStructField(structFieldAccessExpr, currentScope);
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        BType inheritedType = refTypeInitExpr.getInheritedType();
        if (BTypes.isValueType(inheritedType) || inheritedType instanceof BArrayType ||
                inheritedType instanceof BXMLType || inheritedType instanceof BConnectorType) {
            BLangExceptionHelper.throwSemanticError(refTypeInitExpr, SemanticErrors.REF_TYPE_INTI_NOT_ALLOWED_HERE);
        }

        Expression[] argExprs = refTypeInitExpr.getArgExprs();
        if (argExprs.length == 0) {
            refTypeInitExpr.setType(inheritedType);

        } else if (inheritedType instanceof BJSONType || inheritedType instanceof BMessageType ||
                inheritedType instanceof BExceptionType) {
            // If there are arguments, then only Structs and Map types are supported.
            BLangExceptionHelper.throwSemanticError(refTypeInitExpr, SemanticErrors.STRUCT_MAP_INIT_NOT_ALLOWED);
        }
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        BType inheritedType = connectorInitExpr.getInheritedType();
        if (!(inheritedType instanceof BallerinaConnectorDef) && !(inheritedType instanceof AbstractNativeConnector)) {
            BLangExceptionHelper.throwSemanticError(connectorInitExpr, SemanticErrors.CONNECTOR_INIT_NOT_ALLOWED);
        }
        connectorInitExpr.setType(inheritedType);

        for (Expression argExpr : connectorInitExpr.getArgExprs()) {
            visitSingleValueExpr(argExpr);
        }

        if (inheritedType instanceof AbstractNativeConnector) {
            AbstractNativeConnector nativeConnector = (AbstractNativeConnector) inheritedType;
            for (int i = 0; i < nativeConnector.getArgumentTypeNames().length; i++) {
                SimpleTypeName simpleTypeName = nativeConnector.getArgumentTypeNames()[i];
                BType argType = BTypes.resolveType(simpleTypeName, currentScope, connectorInitExpr.getNodeLocation());
                if (argType != connectorInitExpr.getArgExprs()[i].getType()) {
                    BLangExceptionHelper.throwSemanticError(connectorInitExpr, SemanticErrors.INCOMPATIBLE_TYPES,
                            argType, connectorInitExpr.getArgExprs()[i].getType());
                }

            }
            return;
        }

        Expression[] argExprs = connectorInitExpr.getArgExprs();
        ParameterDef[] parameterDefs = ((BallerinaConnectorDef) inheritedType).getParameterDefs();
        for (int i = 0; i < argExprs.length; i++) {
            SimpleTypeName simpleTypeName = parameterDefs[i].getTypeName();
            BType paramType = BTypes.resolveType(simpleTypeName, currentScope, connectorInitExpr.getNodeLocation());
            parameterDefs[i].setType(paramType);

            Expression argExpr = argExprs[i];
            if (parameterDefs[i].getType() != argExpr.getType()) {
                BLangExceptionHelper.throwSemanticError(connectorInitExpr, SemanticErrors.INCOMPATIBLE_TYPES,
                        parameterDefs[i].getType(), argExpr.getType());
            }
        }
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        BType inheritedType = arrayInitExpr.getInheritedType();
        if (!(inheritedType instanceof BArrayType)) {
            BLangExceptionHelper.throwSemanticError(arrayInitExpr, SemanticErrors.ARRAY_INIT_NOT_ALLOWED_HERE);
        }

        arrayInitExpr.setType(inheritedType);
        Expression[] argExprs = arrayInitExpr.getArgExprs();
        if (argExprs.length == 0) {
            return;
        }

        BType expectedElementType = ((BArrayType) inheritedType).getElementType();
        for (int i = 0; i < argExprs.length; i++) {
            visitSingleValueExpr(argExprs[i]);

            // Types are defined only once, hence the following object equal should work.
            if (argExprs[i].getType() != expectedElementType) {
                TypeCastExpression typeCastExpr = checkWideningPossible(expectedElementType, argExprs[i]);
                if (typeCastExpr == null) {
                    BLangExceptionHelper.throwSemanticError(arrayInitExpr,
                            SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT,
                            argExprs[i].getType(), expectedElementType);
                }
                argExprs[i] = typeCastExpr;
            }
        }
    }

    /**
     * Visit and analyze ballerina Struct initializing expression.
     */
    @Override
    public void visit(StructInitExpr structInitExpr) {
        BType inheritedType = structInitExpr.getInheritedType();
        structInitExpr.setType(inheritedType);
        Expression[] argExprs = structInitExpr.getArgExprs();
        if (argExprs.length == 0) {
            return;
        }

        StructDef structDef = (StructDef) inheritedType;
        for (Expression argExpr : argExprs) {
            MapStructInitKeyValueExpr keyValueExpr = (MapStructInitKeyValueExpr) argExpr;
            Expression keyExpr = keyValueExpr.getKeyExpr();
            if (!(keyExpr instanceof VariableRefExpr)) {
                BLangExceptionHelper.throwSemanticError(keyExpr, SemanticErrors.INVALID_FIELD_NAME_STRUCT_INIT);
            }

            VariableRefExpr varRefExpr = (VariableRefExpr) keyExpr;
            
            BLangSymbol varDefSymbol = structDef.resolveMembers(varRefExpr.getSymbolName());
            
            if (varDefSymbol == null) {
                BLangExceptionHelper.throwSemanticError(keyExpr, SemanticErrors.UNKNOWN_FIELD_IN_STRUCT,
                        varRefExpr.getVarName(), structDef.getName());
            }
            
            if (!(varDefSymbol instanceof VariableDef)) {
                BLangExceptionHelper.throwSemanticError(varRefExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, 
                        varDefSymbol.getSymbolName());
            }
            
            VariableDef varDef = (VariableDef) varDefSymbol;
            
            varRefExpr.setVariableDef(varDef);
            Expression valueExpr = keyValueExpr.getValueExpr();
            visitSingleValueExpr(valueExpr);

            if (!valueExpr.getType().equals(varDef.getType())) {
                BLangExceptionHelper.throwSemanticError(keyExpr, SemanticErrors.INCOMPATIBLE_TYPES,
                        varDef.getType(), valueExpr.getType());
            }
        }
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        mapInitExpr.setType(mapInitExpr.getInheritedType());
        Expression[] argExprs = mapInitExpr.getArgExprs();
        if (argExprs.length == 0) {
            return;
        }

        for (Expression argExpr : argExprs) {
            MapStructInitKeyValueExpr keyValueExpr = (MapStructInitKeyValueExpr) argExpr;
            Expression keyExpr = keyValueExpr.getKeyExpr();
            visitSingleValueExpr(keyExpr);

            if (keyExpr.getType() != BTypes.typeString) {
                BLangExceptionHelper.throwSemanticError(mapInitExpr,
                        SemanticErrors.INVALID_TYPE_IN_MAP_INDEX_EXPECTED_STRING, keyExpr.getType());
            }

            visitSingleValueExpr(keyValueExpr.getValueExpr());
        }
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        // In this case, type of the backtickExpr should be either xml or json
        BType inheritedType = backtickExpr.getInheritedType();
        if (inheritedType != BTypes.typeJSON && inheritedType != BTypes.typeXML) {
            BLangExceptionHelper.throwSemanticError(backtickExpr, SemanticErrors.INCOMPATIBLE_TYPES_EXPECTED_JSON_XML);
        }
        backtickExpr.setType(inheritedType);

        // Analyze the string and create relevant tokens
        // First check the literals
        String[] literals = backtickExpr.getTemplateStr().split(patternString);
        List<Expression> argExprList = new ArrayList<>();

        // Split will always have at least one matching literal
        int i = 0;
        if (literals.length > i) {
            BasicLiteral basicLiteral = new BasicLiteral(backtickExpr.getNodeLocation(),
                    new SimpleTypeName(TypeConstants.STRING_TNAME), new BString(literals[i]));
            visit(basicLiteral);
            argExprList.add(basicLiteral);
            i++;
        }

        // Then get the variable references
        // ${var} --> group0: ${var}, group1: var, group2: var
        // ${arr[10]} --> group0: ${arr[10]}, group1: arr[10], group2: arr, group3: [10], group4: 10
        // ${myMap["key"]} --> group0: ${myMap["key"]}, group1: myMap["key"],
        //                                          group2: myMap, group3: ["key"], group4: "key", group5: key
        Matcher m = compiledPattern.matcher(backtickExpr.getTemplateStr());

        while (m.find()) {
            if (m.group(3) != null) {
                BasicLiteral indexExpr;
                if (m.group(5) != null) {
                    indexExpr = new BasicLiteral(backtickExpr.getNodeLocation(),
                            new SimpleTypeName(TypeConstants.STRING_TNAME), new BString(m.group(5)));
                    indexExpr.setType(BTypes.typeString);
                } else {
                    indexExpr = new BasicLiteral(backtickExpr.getNodeLocation(),
                            new SimpleTypeName(TypeConstants.INT_TNAME), new BInteger(Integer.parseInt(m.group(4))));
                    indexExpr.setType(BTypes.typeInt);
                }

                SymbolName mapOrArrName = new SymbolName(m.group(2));

                ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder =
                        new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();

                VariableRefExpr arrayMapVarRefExpr = new VariableRefExpr(backtickExpr.getNodeLocation(), mapOrArrName);
                visit(arrayMapVarRefExpr);

                builder.setArrayMapVarRefExpr(arrayMapVarRefExpr);
                builder.setVarName(mapOrArrName);
                builder.setIndexExpr(indexExpr);
                ArrayMapAccessExpr arrayMapAccessExpr = builder.build();
                visit(arrayMapAccessExpr);
                argExprList.add(arrayMapAccessExpr);
            } else {
                VariableRefExpr variableRefExpr = new VariableRefExpr(backtickExpr.getNodeLocation(),
                        new SymbolName(m.group(1)));
                visit(variableRefExpr);
                argExprList.add(variableRefExpr);
            }
            if (literals.length > i) {
                BasicLiteral basicLiteral = new BasicLiteral(backtickExpr.getNodeLocation(),
                        new SimpleTypeName(TypeConstants.STRING_TNAME), new BString(literals[i]));
                visit(basicLiteral);
                argExprList.add(basicLiteral);
                i++;
            }
        }

        backtickExpr.setArgsExprs(argExprList.toArray(new Expression[argExprList.size()]));
    }

    @Override
    public void visit(MapStructInitKeyValueExpr keyValueExpr) {

    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        SymbolName symbolName = variableRefExpr.getSymbolName();

        // Check whether this symName is declared
        BLangSymbol varDefSymbol = currentScope.resolve(symbolName);
        
        if (varDefSymbol == null) {
            BLangExceptionHelper.throwSemanticError(variableRefExpr, SemanticErrors.UNDEFINED_SYMBOL, symbolName);
        }
        
        if (!(varDefSymbol instanceof VariableDef)) {
            BLangExceptionHelper.throwSemanticError(variableRefExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, 
                    symbolName);
        }
        
        variableRefExpr.setVariableDef((VariableDef) varDefSymbol);
    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        // Evaluate the expression and set the type
        visitSingleValueExpr(typeCastExpression.getRExpr());
        BType sourceType = typeCastExpression.getRExpr().getType();
        BType targetType = typeCastExpression.getTargetType();
        if (targetType == null) {
            targetType = BTypes.resolveType(typeCastExpression.getTypeName(), currentScope, null);
        }
        // Check whether this is a native conversion
        if (BTypes.isValueType(sourceType) &&
                BTypes.isValueType(targetType)) {
            TypeEdge newEdge = null;
            newEdge = TypeLattice.getExplicitCastLattice().getEdgeFromTypes(sourceType, targetType, null);
            typeCastExpression.setEvalFunc(newEdge.getTypeMapperFunction());
        } else {
            linkTypeMapper(typeCastExpression, sourceType, targetType);
        }
    }

    @Override
    public void visit(StackVarLocation stackVarLocation) {

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

    @Override
    public void visit(StructVarLocation structVarLocation) {
    }

    @Override
    public void visit(WorkerVarLocation workerVarLocation) {

    }

    public void visit(ResourceInvocationExpr resourceIExpr) {
    }

    public void visit(MainInvoker mainInvoker) {
    }


    // Private methods.

    private void openScope(SymbolScope symbolScope) {
        currentScope = symbolScope;
    }

    private void closeScope() {
        currentScope = currentScope.getEnclosingScope();
    }

    private void handleArrayType(ArrayMapAccessExpr arrayMapAccessExpr) {
        VariableRefExpr arrayMapVarRefExpr = (VariableRefExpr) arrayMapAccessExpr.getRExpr();

        // Handle the arrays type
        if (arrayMapVarRefExpr.getType() instanceof BArrayType) {

            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            visitSingleValueExpr(indexExpr);
            if (indexExpr.getType() != BTypes.typeInt) {
                BLangExceptionHelper.throwSemanticError(arrayMapAccessExpr, SemanticErrors.NON_INTEGER_ARRAY_INDEX,
                        indexExpr.getType());
            }

            // Set type of the arrays access expression
            BType typeOfArray = ((BArrayType) arrayMapVarRefExpr.getType()).getElementType();
            arrayMapAccessExpr.setType(typeOfArray);

        } else if (arrayMapVarRefExpr.getType() instanceof BMapType) {

            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            visitSingleValueExpr(indexExpr);
            if (indexExpr.getType() != BTypes.typeString) {
                BLangExceptionHelper.throwSemanticError(arrayMapAccessExpr, SemanticErrors.NON_STRING_MAP_INDEX,
                        indexExpr.getType());
            }

            // Set type of the map access expression
            BType typeOfMap = arrayMapVarRefExpr.getType();
            arrayMapAccessExpr.setType(typeOfMap);

        } else {
            BLangExceptionHelper.throwSemanticError(arrayMapAccessExpr,
                    SemanticErrors.INVALID_OPERATION_NOT_SUPPORT_INDEXING, arrayMapVarRefExpr.getType());
        }
    }

    private void visitBinaryExpr(BinaryExpression expr) {
        visitSingleValueExpr(expr.getLExpr());
        visitSingleValueExpr(expr.getRExpr());
    }

    private void visitSingleValueExpr(Expression expr) {
        expr.accept(this);
        if (expr.isMultiReturnExpr()) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) expr;
            String nameWithPkgName = (funcIExpr.getPackageName() != null) ? funcIExpr.getPackageName()
                    + ":" + funcIExpr.getName() : funcIExpr.getName();
            BLangExceptionHelper.throwSemanticError(expr, SemanticErrors.MULTIPLE_VALUE_IN_SINGLE_VALUE_CONTEXT,
                    nameWithPkgName);
        }
    }

    private BType verifyBinaryArithmeticExprType(BinaryArithmeticExpression binaryArithmeticExpr) {
        BType type = verifyBinaryExprType(binaryArithmeticExpr);
        binaryArithmeticExpr.setType(type);
        return type;
    }

    private BType verifyBinaryCompareExprType(BinaryExpression binaryExpression) {
        BType type = verifyBinaryExprType(binaryExpression);
        binaryExpression.setType(BTypes.typeBoolean);
        return type;
    }

    private BType verifyBinaryExprType(BinaryExpression binaryExpr) {
        visitBinaryExpr(binaryExpr);

        Expression rExpr = binaryExpr.getRExpr();
        Expression lExpr = binaryExpr.getLExpr();

        BType rType = rExpr.getType();
        if (rExpr instanceof TypeCastExpression && rType == null) {
            rType = BTypes.resolveType(((TypeCastExpression) rExpr).getTypeName(), currentScope, null);
        }

        BType lType = lExpr.getType();
        if (lExpr instanceof TypeCastExpression && lType == null) {
            lType = BTypes.resolveType(((TypeCastExpression) lExpr).getTypeName(), currentScope, null);
        }

        if (!(rType.equals(lType))) {
            TypeCastExpression newExpr;
            TypeEdge newEdge;

            if (((rType.equals(BTypes.typeString) || lType.equals(BTypes.typeString))
                    && binaryExpr.getOperator().equals(Operator.ADD)) || (!(rType.equals(BTypes.typeString)) &&
                    !(lType.equals(BTypes.typeString)))) {
                newEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(rType, lType, null);
                if (newEdge != null) { // Implicit cast from right to left
                    newExpr = new TypeCastExpression(rExpr.getNodeLocation(), rExpr, lType);
                    newExpr.setEvalFunc(newEdge.getTypeMapperFunction());
                    newExpr.accept(this);
                    binaryExpr.setRExpr(newExpr);
                    return lType;
                } else {
                    newEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(lType, rType, null);
                    if (newEdge != null) { // Implicit cast from left to right
                        newExpr = new TypeCastExpression(lExpr.getNodeLocation(), lExpr, rType);
                        newExpr.setEvalFunc(newEdge.getTypeMapperFunction());
                        newExpr.accept(this);
                        binaryExpr.setLExpr(newExpr);
                        return rType;
                    }
                }
            }
            throwInvalidBinaryOpError(binaryExpr);
        }
        return rType;
    }

    private void visitBinaryLogicalExpr(BinaryLogicalExpression expr) {
        visitBinaryExpr(expr);

        Expression rExpr = expr.getRExpr();
        Expression lExpr = expr.getLExpr();

        if (lExpr.getType() == BTypes.typeBoolean && rExpr.getType() == BTypes.typeBoolean) {
            expr.setType(BTypes.typeBoolean);
        } else {
            throwInvalidBinaryOpError(expr);
        }
    }

    private String getVarNameFromExpression(Expression expr) {
        if (expr instanceof ArrayMapAccessExpr) {
            return ((ArrayMapAccessExpr) expr).getSymbolName().getName();
        } else if (expr instanceof StructFieldAccessExpr) {
            return getVarNameFromExpression(((StructFieldAccessExpr) expr).getVarRef());
        } else {
            return ((VariableRefExpr) expr).getSymbolName().getName();
        }
    }

    private void checkForConstAssignment(AssignStmt assignStmt, Expression lExpr) {
        if (lExpr instanceof VariableRefExpr &&
                ((VariableRefExpr) lExpr).getMemoryLocation() instanceof ConstantLocation) {
            BLangExceptionHelper.throwSemanticError(assignStmt, SemanticErrors.CANNOT_ASSIGN_VALUE_CONSTANT,
                    ((VariableRefExpr) lExpr).getSymbolName());
        }
    }

    private void checkForMultiAssignmentErrors(AssignStmt assignStmt, Expression[] lExprs,
                                               CallableUnitInvocationExpr rExpr) {
        BType[] returnTypes = rExpr.getTypes();
        if (lExprs.length != returnTypes.length) {
            BLangExceptionHelper.throwSemanticError(assignStmt,
                    SemanticErrors.ASSIGNMENT_COUNT_MISMATCH, lExprs.length, returnTypes.length);
        }

        //cannot assign string to b (type int) in multiple assignment

        for (int i = 0; i < lExprs.length; i++) {
            Expression lExpr = lExprs[i];
            BType returnType = returnTypes[i];
            if (!lExpr.getType().equals(returnType)) {
                String varName = getVarNameFromExpression(lExpr);
                BLangExceptionHelper.throwSemanticError(assignStmt,
                        SemanticErrors.CANNOT_ASSIGN_IN_MULTIPLE_ASSIGNMENT, returnType, varName, lExpr.getType());
            }
        }
    }

    private void visitLExprsOfAssignment(AssignStmt assignStmt, Expression[] lExprs) {
        // This set data structure is used to check for repeated variable names in the assignment statement
        Set<String> varNameSet = new HashSet<>();

        for (Expression lExpr : lExprs) {
            String varName = getVarNameFromExpression(lExpr);
            if (!varNameSet.add(varName)) {
                BLangExceptionHelper.throwSemanticError(assignStmt,
                        SemanticErrors.VAR_IS_REPEATED_ON_LEFT_SIDE_ASSIGNMENT, varName);
            }

            // First mark all left side ArrayMapAccessExpr. This is to skip some processing which is applicable only
            // for right side expressions.
            if (lExpr instanceof ArrayMapAccessExpr) {
                ((ArrayMapAccessExpr) lExpr).setLHSExpr(true);
            } else if (lExpr instanceof StructFieldAccessExpr) {
                ((StructFieldAccessExpr) lExpr).setLHSExpr(true);
            }

            lExpr.accept(this);

            // Check whether someone is trying to change the values of a constant
            checkForConstAssignment(assignStmt, lExpr);
        }
    }

    private void linkFunction(FunctionInvocationExpr funcIExpr) {
        String pkgPath = funcIExpr.getPackagePath();

        Expression[] exprs = funcIExpr.getArgExprs();
        BType[] paramTypes = new BType[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        SymbolName symbolName = LangModelUtils.getSymNameWithParams(funcIExpr.getName(), pkgPath, paramTypes);
        BLangSymbol functionSymbol = currentScope.resolve(symbolName);
        if (functionSymbol == null) {
            String funcName = (funcIExpr.getPackageName() != null) ? funcIExpr.getPackageName() + ":" +
                    funcIExpr.getName() : funcIExpr.getName();
            BLangExceptionHelper.throwSemanticError(funcIExpr, SemanticErrors.UNDEFINED_FUNCTION, funcName);
        }

        Function function;
        if (functionSymbol instanceof NativeUnitProxy) {
            NativeUnit nativeUnit = ((NativeUnitProxy) functionSymbol).load();
            // Loading return parameter types of this native function
            SimpleTypeName[] returnParamTypeNames = nativeUnit.getReturnParamTypeNames();
            BType[] returnTypes = new BType[returnParamTypeNames.length];
            for (int i = 0; i < returnParamTypeNames.length; i++) {
                SimpleTypeName typeName = returnParamTypeNames[i];
                BType bType = BTypes.resolveType(typeName, currentScope, funcIExpr.getNodeLocation());
                returnTypes[i] = bType;
            }
            
            if (!(nativeUnit instanceof Function)) {
                BLangExceptionHelper.throwSemanticError(funcIExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, 
                        symbolName);
            }
            function = (Function) nativeUnit;
            function.setReturnParamTypes(returnTypes);

        } else {
            if (!(functionSymbol instanceof Function)) {
                BLangExceptionHelper.throwSemanticError(funcIExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, 
                        symbolName);
            }
            function = (Function) functionSymbol;
        }

        // Link the function with the function invocation expression
        funcIExpr.setCallableUnit(function);
    }

    private void linkAction(ActionInvocationExpr actionIExpr) {
        String pkgPath = actionIExpr.getPackagePath();
        String connectorName = actionIExpr.getConnectorName();

        // First look for the connectors
        SymbolName connectorSymbolName = new SymbolName(connectorName, pkgPath);
        BLangSymbol connectorSymbol = currentScope.resolve(connectorSymbolName);
        if (connectorSymbol == null) {
            String connectorWithPkgName = (actionIExpr.getPackageName() != null) ? actionIExpr.getPackageName() +
                    ":" + actionIExpr.getConnectorName() : actionIExpr.getConnectorName();
            BLangExceptionHelper.throwSemanticError(actionIExpr, SemanticErrors.UNDEFINED_CONNECTOR,
                    connectorWithPkgName);
        }

        Expression[] exprs = actionIExpr.getArgExprs();
        BType[] paramTypes = new BType[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        // When getting the action symbol name, Package name for the action is set to null, since the action is 
        // registered under connector, and connecter contains the package
        SymbolName symbolName = LangModelUtils.getActionSymName(actionIExpr.getName(), actionIExpr.getConnectorName(),
                null, paramTypes);

        // Now check whether there is a matching action
        BLangSymbol actionSymbol = null;
        if (connectorSymbol instanceof NativeUnitProxy) {
            AbstractNativeConnector connector = (AbstractNativeConnector) ((NativeUnitProxy) connectorSymbol).load();
            actionSymbol = connector.resolveMembers(symbolName);
        } else if (connectorSymbol instanceof BallerinaConnectorDef) {
            actionSymbol = ((BallerinaConnectorDef) connectorSymbol).resolveMembers(symbolName);
        } else {
            BLangExceptionHelper.throwSemanticError(actionIExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, 
                    connectorSymbolName);
        }

        if (actionSymbol == null) {
            String actionWithConnector = actionIExpr.getConnectorName() + "." + actionIExpr.getName();
            String actionName = (actionIExpr.getPackageName() != null) ? actionIExpr.getPackageName() + ":" +
                    actionWithConnector : actionWithConnector;
            BLangExceptionHelper.throwSemanticError(actionIExpr, SemanticErrors.UNDEFINED_ACTION, actionName);
        }

        // Load native action
        Action action = null;
        if (actionSymbol instanceof NativeUnitProxy) {
            // Loading return parameter types of this native function
            NativeUnit nativeUnit = ((NativeUnitProxy) actionSymbol).load();
            SimpleTypeName[] returnParamTypeNames = nativeUnit.getReturnParamTypeNames();
            BType[] returnTypes = new BType[returnParamTypeNames.length];
            for (int i = 0; i < returnParamTypeNames.length; i++) {
                SimpleTypeName typeName = returnParamTypeNames[i];
                BType bType = BTypes.resolveType(typeName, currentScope, actionIExpr.getNodeLocation());
                returnTypes[i] = bType;
            }
            
            if (!(nativeUnit instanceof Action)) {
                BLangExceptionHelper.throwSemanticError(actionIExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, 
                        symbolName);
            }
            action = (Action) nativeUnit;
            action.setReturnParamTypes(returnTypes);

        } else if (actionSymbol instanceof Action) {
            action = (Action) actionSymbol;
        } else {
            BLangExceptionHelper.throwSemanticError(actionIExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, 
                    symbolName);
        }

        // Link the action with the action invocation expression
        actionIExpr.setCallableUnit(action);
    }

    private void linkWorker(WorkerInvocationStmt workerInvocationStmt) {

        String workerName = workerInvocationStmt.getCallableUnitName();
        SymbolName workerSymbolName = new SymbolName(workerName);
        Worker worker = (Worker) currentScope.resolve(workerSymbolName);
        if (worker == null) {
            throw new LinkerException(workerInvocationStmt.getNodeLocation().getFileName() + ":" +
                    workerInvocationStmt.getNodeLocation().getLineNumber() +
                    ": undefined worker '" + workerInvocationStmt.getCallableUnitName() + "'");
        }
        workerInvocationStmt.setCallableUnit(worker);
    }

    private void throwInvalidBinaryOpError(BinaryExpression binaryExpr) {
        BType lExprType = binaryExpr.getLExpr().getType();
        BType rExprType = binaryExpr.getRExpr().getType();

        if (lExprType == rExprType) {
            BLangExceptionHelper.throwSemanticError(binaryExpr,
                    SemanticErrors.INVALID_OPERATION_OPERATOR_NOT_DEFINED, binaryExpr.getOperator(), lExprType);
        } else {
            BLangExceptionHelper.throwSemanticError(binaryExpr,
                    SemanticErrors.INVALID_OPERATION_INCOMPATIBLE_TYPES, lExprType, rExprType);
        }
    }

    private void throwInvalidUnaryOpError(UnaryExpression unaryExpr) {
        BType rExprType = unaryExpr.getRExpr().getType();
        BLangExceptionHelper.throwSemanticError(unaryExpr,
                SemanticErrors.INVALID_OPERATION_OPERATOR_NOT_DEFINED, unaryExpr.getOperator(), rExprType);
    }

    private void visitStructField(StructFieldAccessExpr structFieldAccessExpr, SymbolScope currentScope) {
        ReferenceExpr varRefExpr = structFieldAccessExpr.getVarRef();
        SymbolName symbolName = varRefExpr.getSymbolName();
        BLangSymbol fieldSymbol = currentScope.resolve(symbolName);

        if (fieldSymbol == null) {
            if (currentScope instanceof StructDef) {
                BLangExceptionHelper.throwSemanticError(structFieldAccessExpr, SemanticErrors.UNKNOWN_FIELD_IN_STRUCT,
                        symbolName.getName(), ((StructDef) currentScope).getName());
            } else {
                BLangExceptionHelper.throwSemanticError(structFieldAccessExpr, SemanticErrors.STRUCT_NOT_FOUND,
                        symbolName.getName());
            }
        }

        // Set expression type
        if (!(fieldSymbol instanceof VariableDef)) {
            BLangExceptionHelper.throwSemanticError(varRefExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND,
                    symbolName);
        }
        VariableDef varDef = (VariableDef) fieldSymbol;
        BType exprType;
        

        /* Get the actual var representation of this field, and semantically analyze. This will check for semantic
         * errors of arrays/map accesses, used in this struct field.
         * eg: in dpt.employee[2].name , below will check for semantics of 'employee[2]',
         * treating them as individual arrays/map variables.
         */
        if (varRefExpr instanceof ArrayMapAccessExpr) {
            Expression rExpr = ((ArrayMapAccessExpr) varRefExpr).getRExpr();
            ((VariableRefExpr) rExpr).setVariableDef(varDef);

            exprType = varDef.getType();
            if (exprType instanceof BArrayType) {
                exprType = ((BArrayType) varDef.getType()).getElementType();
            }
            handleArrayType((ArrayMapAccessExpr) varRefExpr);
        } else {
            ((VariableRefExpr) varRefExpr).setVariableDef(varDef);
            exprType = (varDef).getType();
        }

        // Go to the referenced field of this struct
        StructFieldAccessExpr fieldExpr = structFieldAccessExpr.getFieldExpr();
        if (fieldExpr != null) {
            if (!(exprType instanceof StructDef)) {
                BLangExceptionHelper.throwSemanticError(structFieldAccessExpr, SemanticErrors.MUST_BE_STRUCT_TYPE,
                        symbolName.getName());
            }
            visitStructField(fieldExpr, ((StructDef) exprType).getSymbolScope());
        }
    }

    private void linkTypeMapper(TypeCastExpression typeCastExpression, BType sourceType, BType targetType) {
        TypeEdge newEdge = null;
        TypeMapper typeMapper;
        // First check on this package
        newEdge = packageTypeLattice.getEdgeFromTypes(sourceType, targetType, currentPkg);
        if (newEdge != null) {
            typeMapper = newEdge.getTypeMapper();
            if (typeMapper != null) {
                typeCastExpression.setCallableUnit(typeMapper);
            }
        } else {
            newEdge = TypeLattice.getExplicitCastLattice().getEdgeFromTypes(sourceType, targetType, currentPkg);
            if (newEdge != null) {
                typeMapper = newEdge.getTypeMapper();
                if (typeMapper != null) {
                    typeCastExpression.setCallableUnit(typeMapper);
                }
            } else {
                newEdge = TypeLattice.getExplicitCastLattice().getEdgeFromTypes(sourceType, targetType, null);
                if (newEdge != null) {
                    typeMapper = newEdge.getTypeMapper();
                    if (typeMapper != null) {
                        typeCastExpression.setCallableUnit(typeMapper);
                    }
                } else {
                    String pkgPath = typeCastExpression.getPackagePath();

                    Expression[] exprs = typeCastExpression.getArgExprs();
                    BType[] paramTypes = new BType[exprs.length];
                    for (int i = 0; i < exprs.length; i++) {
                        paramTypes[i] = exprs[i].getType();
                    }

                    SymbolName symbolName = LangModelUtils.getTypeMapperSymName(pkgPath,
                            sourceType, targetType);
                    BLangSymbol typeMapperSymbol = currentScope.resolve(symbolName);
                    if (typeMapperSymbol == null) {
                        BLangExceptionHelper.throwSemanticError(typeCastExpression,
                                SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CAST, sourceType, targetType);
                    }

                    if (typeMapperSymbol instanceof NativeUnitProxy) {
                        // TODO We need to find a way to load input parameter types

                        // Loading return parameter types of this native function
                        NativeUnit nativeUnit = ((NativeUnitProxy) typeMapperSymbol).load();
                        SimpleTypeName[] returnParamTypeNames = nativeUnit.getReturnParamTypeNames();
                        BType[] returnTypes = new BType[returnParamTypeNames.length];
                        for (int i = 0; i < returnParamTypeNames.length; i++) {
                            SimpleTypeName typeName = returnParamTypeNames[i];
                            BType bType = BTypes.resolveType(typeName, currentScope,
                                    typeCastExpression.getNodeLocation());
                            returnTypes[i] = bType;
                        }
                        
                        if (!(nativeUnit instanceof TypeMapper)) {
                            BLangExceptionHelper.throwSemanticError(typeCastExpression, 
                                    SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, symbolName);
                        }
                        typeMapper = (TypeMapper) nativeUnit;
                        typeMapper.setReturnParamTypes(returnTypes);

                    } else {
                        if (!(typeMapperSymbol instanceof TypeMapper)) {
                            BLangExceptionHelper.throwSemanticError(typeCastExpression, 
                                    SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND, symbolName);
                        }
                        typeMapper = (TypeMapper) typeMapperSymbol;
                    }

                    if (typeMapper != null) {
                        typeMapper.setParameterTypes(paramTypes);
                        // Link the function with the function invocation expression
                        typeCastExpression.setCallableUnit(typeMapper);
                    } else {
                        BLangExceptionHelper.throwSemanticError(typeCastExpression,
                                SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CAST, sourceType, targetType);
                    }
                }
            }
        }
    }

    private TypeCastExpression checkWideningPossible(BType lhsType, Expression rhsExpr) {
        BType rhsType = rhsExpr.getType();
        if (rhsType == null && rhsExpr instanceof TypeCastExpression) {
            rhsType = BTypes.resolveType(((TypeCastExpression) rhsExpr).getTypeName(), currentScope, null);
        }
        TypeCastExpression newExpr = null;
        TypeEdge newEdge;

        newEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(rhsType, lhsType, null);
        if (newEdge != null) {
            newExpr = new TypeCastExpression(rhsExpr.getNodeLocation(), rhsExpr, lhsType);
            newExpr.setEvalFunc(newEdge.getTypeMapperFunction());
        }
        return newExpr;
    }

    private void setMemoryLocation(VariableDef variableDef) {
        if (currentScope.getScopeName() == SymbolScope.ScopeName.LOCAL) {
            if (currentScope.getEnclosingScope().getScopeName() == SymbolScope.ScopeName.WORKER) {
                variableDef.setMemoryLocation(new WorkerVarLocation(++workerMemAddrOffset));
            } else {
                variableDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            }
        } else if (currentScope.getScopeName() == SymbolScope.ScopeName.SERVICE) {
            variableDef.setMemoryLocation(new ServiceVarLocation(++staticMemAddrOffset));
        } else if (currentScope.getScopeName() == SymbolScope.ScopeName.CONNECTOR) {
            variableDef.setMemoryLocation(new ConnectorVarLocation(++connectorMemAddrOffset));
        }
    }

    private void defineConstants(ConstDef[] constDefs) {
        for (ConstDef constDef : constDefs) {
            SymbolName symbolName = new SymbolName(constDef.getName());
            // Check whether this constant is already defined.
            if (currentScope.resolve(symbolName) != null) {
                BLangExceptionHelper.throwSemanticError(constDef, SemanticErrors.REDECLARED_SYMBOL, constDef.getName());
            }
            // Define the variableRef symbol in the current scope
            currentScope.define(symbolName, constDef);
        }
    }

    private void defineFunctions(Function[] functions) {
        for (Function function : functions) {
            // Resolve input parameters
            ParameterDef[] paramDefArray = function.getParameterDefs();
            BType[] paramTypes = new BType[paramDefArray.length];
            for (int i = 0; i < paramDefArray.length; i++) {
                ParameterDef paramDef = paramDefArray[i];
                BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
                paramDef.setType(bType);
                paramTypes[i] = bType;
            }

            function.setParameterTypes(paramTypes);
            SymbolName symbolName = LangModelUtils.getSymNameWithParams(function.getName(),
                    function.getPackagePath(), paramTypes);
            function.setSymbolName(symbolName);

            BLangSymbol functionSymbol = currentScope.resolve(symbolName);

            if (function.isNative() && functionSymbol == null) {
                BLangExceptionHelper.throwSemanticError(function,
                        SemanticErrors.UNDEFINED_FUNCTION, function.getName());
            }

            if (!function.isNative()) {
                if (functionSymbol != null) {
                    BLangExceptionHelper.throwSemanticError(function,
                            SemanticErrors.REDECLARED_SYMBOL, function.getName());
                }
                currentScope.define(symbolName, function);
            }

            // Resolve return parameters
            ParameterDef[] returnParameters = function.getReturnParameters();
            BType[] returnTypes = new BType[returnParameters.length];
            for (int i = 0; i < returnParameters.length; i++) {
                ParameterDef paramDef = returnParameters[i];
                BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
                paramDef.setType(bType);
                returnTypes[i] = bType;
            }
            function.setReturnParamTypes(returnTypes);
        }
    }

    private void defineTypeMappers(TypeMapper[] typeMappers) {
        for (TypeMapper typeMapper : typeMappers) {
            NodeLocation location = typeMapper.getNodeLocation();

            // Resolve input parameters
            SimpleTypeName sourceType = typeMapper.getParameterDefs()[0].getTypeName();
            BType sourceBType = BTypes.resolveType(sourceType, currentScope, location);
            typeMapper.setParameterTypes(new BType[] { sourceBType });

            // Resolve return parameters
            SimpleTypeName targetType = typeMapper.getReturnParameters()[0].getTypeName();
            BType targetBType = BTypes.resolveType(targetType, currentScope, location);

            TypeVertex sourceV = new TypeVertex(sourceBType);
            TypeVertex targetV = new TypeVertex(targetBType);
            typeMapper.setReturnParamTypes(new BType[] { targetBType });

            packageTypeLattice.addVertex(sourceV, true);
            packageTypeLattice.addVertex(targetV, true);
            packageTypeLattice.addEdge(sourceV, targetV, typeMapper, typeMapper.getPackagePath());

            SymbolName symbolName = LangModelUtils
                    .getTypeMapperSymName(typeMapper.getPackagePath(), sourceBType, targetBType);
            typeMapper.setSymbolName(symbolName);

            BLangSymbol typConvertorSymbol = currentScope.resolve(symbolName);

            if (typeMapper.isNative() && typConvertorSymbol == null) {
                BLangExceptionHelper
                        .throwSemanticError(typeMapper, SemanticErrors.UNDEFINED_TYPE_MAPPER, typeMapper.getName());
            }

            if (!typeMapper.isNative()) {
                if (typConvertorSymbol != null) {
                    BLangExceptionHelper
                            .throwSemanticError(typeMapper, SemanticErrors.REDECLARED_SYMBOL, typeMapper.getName());
                }
                currentScope.define(symbolName, typeMapper);
            }
        }
    }

    private void defineConnectors(BallerinaConnectorDef[] connectorDefArray) {
        for (BallerinaConnectorDef connectorDef : connectorDefArray) {
            String connectorName = connectorDef.getName();

            // Define ConnectorDef Symbol in the package scope..
            SymbolName connectorSymbolName = new SymbolName(connectorName);

            BLangSymbol connectorSymbol = currentScope.resolve(connectorSymbolName);
            if (connectorDef.isNative() && connectorSymbol == null) {
             BLangExceptionHelper.throwSemanticError(connectorDef,
                     SemanticErrors.UNDEFINED_CONNECTOR, connectorDef.getName());
            }

            if (!connectorDef.isNative()) {
                if (connectorSymbol != null) {
                    BLangExceptionHelper.throwSemanticError(connectorDef,
                            SemanticErrors.REDECLARED_SYMBOL, connectorName);
                }
                currentScope.define(connectorSymbolName, connectorDef);
            }

            // Create the '<init>' function and inject it to the connector;
            BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(
                    connectorDef.getNodeLocation(), connectorDef);
            for (VariableDefStmt variableDefStmt : connectorDef.getVariableDefStmts()) {
                blockStmtBuilder.addStmt(variableDefStmt);
            }

            BallerinaFunction.BallerinaFunctionBuilder functionBuilder =
                    new BallerinaFunction.BallerinaFunctionBuilder(connectorDef);
            functionBuilder.setNodeLocation(connectorDef.getNodeLocation());
            functionBuilder.setName(connectorName + ".<init>");
            functionBuilder.setPkgPath(connectorDef.getPackagePath());
            functionBuilder.setBody(blockStmtBuilder.build());
            connectorDef.setInitFunction(functionBuilder.buildFunction());
        }

        for (BallerinaConnectorDef connectorDef : connectorDefArray) {
            // Define actions
            openScope(connectorDef);

            for (BallerinaAction bAction : connectorDef.getActions()) {
                bAction.setConnectorDef(connectorDef);
                defineAction(bAction, connectorDef);
            }

            closeScope();
        }
    }

    private void defineAction(BallerinaAction action, BallerinaConnectorDef connectorDef) {
        ParameterDef[] paramDefArray = action.getParameterDefs();
        BType[] paramTypes = new BType[paramDefArray.length];
        for (int i = 0; i < paramDefArray.length; i++) {
            ParameterDef paramDef = paramDefArray[i];
            BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
            paramDef.setType(bType);
            paramTypes[i] = bType;
        }

        action.setParameterTypes(paramTypes);
        SymbolName symbolName = LangModelUtils.getActionSymName(action.getName(), connectorDef.getName(),
                action.getPackagePath(), paramTypes);
        action.setSymbolName(symbolName);

        BLangSymbol actionSymbol = currentScope.resolve(symbolName);

        if (action.isNative()) {
            AbstractNativeConnector connector = (AbstractNativeConnector) BTypes
                    .resolveType(new SimpleTypeName(connectorDef.getName()),
                            currentScope, connectorDef.getNodeLocation());
            actionSymbol = connector.resolve(symbolName);
            if (actionSymbol == null) {
                BLangExceptionHelper.throwSemanticError(connectorDef,
                        SemanticErrors.UNDEFINED_ACTION_IN_CONNECTOR, action.getName(), connectorDef.getName());
            }
        } else {
            if (actionSymbol != null) {
                BLangExceptionHelper.throwSemanticError(action, SemanticErrors.REDECLARED_SYMBOL, action.getName());
            }
            currentScope.define(symbolName, action);
        }

        // Resolve return parameters
        ParameterDef[] returnParameters = action.getReturnParameters();
        BType[] returnTypes = new BType[returnParameters.length];
        for (int i = 0; i < returnParameters.length; i++) {
            ParameterDef paramDef = returnParameters[i];
            BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
            paramDef.setType(bType);
            returnTypes[i] = bType;
        }
        action.setReturnParamTypes(returnTypes);
    }

    private void defineServices(Service[] services) {
        for (Service service : services) {

            // Define Service Symbol in the package scope..
            if (currentScope.resolve(service.getSymbolName()) != null) {
                BLangExceptionHelper.throwSemanticError(service, SemanticErrors.REDECLARED_SYMBOL, service.getName());
            }
            currentScope.define(service.getSymbolName(), service);

            // Create the '<init>' function and inject it to the connector;
            BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(
                    service.getNodeLocation(), service);
            for (VariableDefStmt variableDefStmt : service.getVariableDefStmts()) {
                blockStmtBuilder.addStmt(variableDefStmt);
            }

            BallerinaFunction.BallerinaFunctionBuilder functionBuilder =
                    new BallerinaFunction.BallerinaFunctionBuilder(service);
            functionBuilder.setNodeLocation(service.getNodeLocation());
            functionBuilder.setName(service.getName() + ".<init>");
            functionBuilder.setPkgPath(service.getPackagePath());
            functionBuilder.setBody(blockStmtBuilder.build());
            service.setInitFunction(functionBuilder.buildFunction());

            // Define resources
            openScope(service);

            for (Resource resource : service.getResources()) {
                defineResource(resource, service);
            }

            closeScope();
        }
    }

    private void defineResource(Resource resource, Service service) {
        ParameterDef[] paramDefArray = resource.getParameterDefs();
        BType[] paramTypes = new BType[paramDefArray.length];
        for (int i = 0; i < paramDefArray.length; i++) {
            ParameterDef paramDef = paramDefArray[i];
            BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
            paramDef.setType(bType);
            paramTypes[i] = bType;
        }

        resource.setParameterTypes(paramTypes);
        SymbolName symbolName = LangModelUtils.getActionSymName(resource.getName(), service.getName(),
                resource.getPackagePath(), paramTypes);
        resource.setSymbolName(symbolName);

        if (currentScope.resolve(symbolName) != null) {
            BLangExceptionHelper.throwSemanticError(resource, SemanticErrors.REDECLARED_SYMBOL, resource.getName());
        }
        currentScope.define(symbolName, resource);
    }

    private void defineStructs(StructDef[] structDefs) {
        for (StructDef structDef : structDefs) {
            SymbolName symbolName = new SymbolName(structDef.getName());

            // Check whether this constant is already defined.
            if (currentScope.resolve(symbolName) != null) {
                BLangExceptionHelper.throwSemanticError(structDef,
                        SemanticErrors.REDECLARED_SYMBOL, structDef.getName());
            }

            currentScope.define(symbolName, structDef);
        }
    }

    private void resolveStructFieldTypes(StructDef[] structDefs) {
        for (StructDef structDef : structDefs) {
            for (VariableDef variableDef : structDef.getFields()) {
                BType fieldType = BTypes.resolveType(variableDef.getTypeName(), currentScope,
                        variableDef.getNodeLocation());
                variableDef.setType(fieldType);
            }
        }
    }

    private void checkUnreachableStmt(Statement[] stmts, int stmtIndex) {
        if (stmts.length > stmtIndex) {
            //skip comment statement.
            if (stmts[stmtIndex] instanceof CommentStmt) {
                checkUnreachableStmt(stmts, ++stmtIndex);
            } else {
                BLangExceptionHelper.throwSemanticError(stmts[stmtIndex], SemanticErrors.UNREACHABLE_STATEMENT);
            }
        }
    }
}

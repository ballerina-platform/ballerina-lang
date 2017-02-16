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

import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.MemoryLocation;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.StructVarLocation;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.BTypeConvertor;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnectorDef;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.CallableUnit;
import org.wso2.ballerina.core.model.CompilationUnit;
import org.wso2.ballerina.core.model.ConstDef;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NativeUnit;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Operator;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.StructDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.TypeConvertor;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BacktickExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryArithmeticExpression;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.BinaryLogicalExpression;
import org.wso2.ballerina.core.model.expressions.CallableUnitInvocationExpr;
import org.wso2.ballerina.core.model.expressions.ConnectorInitExpr;
import org.wso2.ballerina.core.model.expressions.DivideExpr;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.MapStructInitKeyValueExpr;
import org.wso2.ballerina.core.model.expressions.ModExpression;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.RefTypeInitExpr;
import org.wso2.ballerina.core.model.expressions.ReferenceExpr;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.StructFieldAccessExpr;
import org.wso2.ballerina.core.model.expressions.StructInitExpr;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.TypeCastExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.invokers.MainInvoker;
import org.wso2.ballerina.core.model.statements.ActionInvocationStmt;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.VariableDefStmt;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.types.BArrayType;
import org.wso2.ballerina.core.model.types.BConnectorType;
import org.wso2.ballerina.core.model.types.BJSONType;
import org.wso2.ballerina.core.model.types.BMapType;
import org.wso2.ballerina.core.model.types.BMessageType;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.types.BXMLType;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.model.types.TypeConstants;
import org.wso2.ballerina.core.model.types.TypeEdge;
import org.wso2.ballerina.core.model.types.TypeLattice;
import org.wso2.ballerina.core.model.util.LangModelUtils;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.nativeimpl.NativeUnitProxy;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.ballerina.core.model.util.LangModelUtils.getNodeLocationStr;

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
    private String currentPkg;
    private TypeLattice packageTypeLattice;
    private CallableUnit currentCallableUnit = null;

    // following pattern matches ${anyString} or ${anyString[int]} or ${anyString["anyString"]}
    private static final String patternString = "\\$\\{((\\w+)(\\[(\\d+|\\\"(\\w+)\\\")\\])?)\\}";
    private static final Pattern compiledPattern = Pattern.compile(patternString);

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
        defineTypeConvertors(packageTypeLattice);
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
            throw new SemanticException(getNodeLocationStr(constDef.getNodeLocation()) +
                    "invalid type '" + typeName + "'");
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
            BlockStmt blockStmt = function.getCallableUnitBody();
            blockStmt.accept(this);
        }

        // Here we need to calculate size of the BValue array which will be created in the stack frame
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
    public void visit(BTypeConvertor typeConvertor) {
        // Open a new symbol scope
        openScope(typeConvertor);
        currentCallableUnit = typeConvertor;

        // Check whether the return statement is missing. Ignore if the function does not return anything.
        // TODO Define proper error message codes
        //checkForMissingReturnStmt(function, "missing return statement at end of function");

        for (ParameterDef parameterDef : typeConvertor.getParameterDefs()) {
            parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            parameterDef.accept(this);
        }

//        for (VariableDef variableDef : typeConvertor.getVariableDefs()) {
//            stackFrameOffset++;
//            visit(variableDef);
//        }

        for (ParameterDef parameterDef : typeConvertor.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            }

            parameterDef.accept(this);
        }

        if (!typeConvertor.isNative()) {
            BlockStmt blockStmt = typeConvertor.getCallableUnitBody();
            currentScope = blockStmt;
            blockStmt.accept(this);
            currentScope = blockStmt.getEnclosingScope();
        }

        // Here we need to calculate size of the BValue array which will be created in the stack frame
        // Values in the stack frame are stored in the following order.
        // -- Parameter values --
        // -- Local var values --
        // -- Temp values      --
        // -- Return values    --
        // These temp values are results of intermediate expression evaluations.
        int sizeOfStackFrame = stackFrameOffset + 1;
        typeConvertor.setStackFrameSize(sizeOfStackFrame);

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
            throw new SemanticException(getNodeLocationStr(action.getNodeLocation()) +
                    "incompatible types: expected '" + action.getConnectorDef() +
                    "', found '" + firstParamDef.getType() + "'");
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
            BlockStmt blockStmt = action.getCallableUnitBody();
            blockStmt.accept(this);
        }

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
        currentCallableUnit = null;
        closeScope();
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
    public void visit(Worker worker) {
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
            String errMsg = getNodeLocationStr(varDefStmt.getNodeLocation()) +
                    "redeclared symbol '" + varDef.getName() + "'";
            throw new SemanticException(errMsg);
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
                throw new SemanticException(varDefStmt.getNodeLocation().getFileName() + ":"
                        + varDefStmt.getNodeLocation().getLineNumber() + ": assignment count mismatch: " +
                        "1 = " + returnTypes.length);

            } else if ((varBType != BTypes.typeMap) && (returnTypes[0] != BTypes.typeMap) &&
                    (!varBType.equals(returnTypes[0]))) {

                TypeCastExpression newExpr = checkWideningPossible(varBType, rExpr);
                if (newExpr != null) {
                    newExpr.accept(this);
                    varDefStmt.setRExpr(newExpr);
                } else {
                    throw new SemanticException(rExpr.getNodeLocation().getFileName() + ":"
                            + rExpr.getNodeLocation().getLineNumber() + ": incompatible types: " + returnTypes[0] +
                            " cannot be converted to " + varBType);
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
                throw new SemanticException(getNodeLocationStr(varDefStmt.getNodeLocation()) +
                        "incompatible types: '" + rExpr.getType() +
                        "' cannot be converted to '" + varBType + "'");
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
                throw new SemanticException(lExpr.getNodeLocation().getFileName() + ":"
                        + lExpr.getNodeLocation().getLineNumber() + ": incompatible types: '" + rExpr.getType() +
                        "' cannot be converted to '" + lExpr.getType() + "'");
            }
        }
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        openScope(blockStmt);

        for (int i = 0; i < blockStmt.getStatements().length; i++) {
            Statement stmt = blockStmt.getStatements()[i];
            if (stmt instanceof ReturnStmt) {
                int stmtLocation = i + 1;
                if (blockStmt.getStatements().length > stmtLocation) {
                    throw new SemanticException(
                            LangModelUtils.getNodeLocationStr(blockStmt.getStatements()[stmtLocation].getNodeLocation())
                                    + "unreachable statement");
                }
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
            throw new SemanticException(getNodeLocationStr(ifElseStmt.getNodeLocation()) +
                    "incompatible type: 'boolean' expected, found '" + expr.getType() + "'");
        }

        Statement thenBody = ifElseStmt.getThenBody();
        thenBody.accept(this);

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            visitSingleValueExpr(elseIfCondition);

            if (elseIfCondition.getType() != BTypes.typeBoolean) {
                throw new SemanticException(getNodeLocationStr(ifElseStmt.getNodeLocation()) +
                        "incompatible type: 'boolean' expected, found '" + elseIfCondition.getType() + "'");
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
        visitSingleValueExpr(expr);

        if (expr.getType() != BTypes.typeBoolean) {
            throw new SemanticException(getNodeLocationStr(whileStmt.getNodeLocation()) +
                    "incompatible type: 'boolean' expected, found '" + expr.getType() + "'");
        }

        BlockStmt blockStmt = whileStmt.getBody();
        if (blockStmt.getStatements().length == 0) {
            // This can be optimized later to skip the while statement
            throw new SemanticException("No statements in the while loop in " +
                    blockStmt.getNodeLocation().getFileName() + ":" + blockStmt.getNodeLocation().getLineNumber());
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
        if (currentCallableUnit instanceof Function) {
            throw new SemanticException(currentCallableUnit.getNodeLocation().getFileName() + ":" +
                    currentCallableUnit.getNodeLocation().getLineNumber() +
                    ": reply statement cannot be used in a function definition");

        } else if (currentCallableUnit instanceof Action) {
            throw new SemanticException(currentCallableUnit.getNodeLocation().getFileName() + ":" +
                    currentCallableUnit.getNodeLocation().getLineNumber() +
                    ": reply statement cannot be used in a action definition");
        }

        if (replyStmt.getReplyExpr() instanceof ActionInvocationExpr) {
            throw new SemanticException(currentCallableUnit.getNodeLocation().getFileName() + ":" +
                    currentCallableUnit.getNodeLocation().getLineNumber() +
                    ": action invocation is not allowed in a reply statement");
        }

        visitSingleValueExpr(replyStmt.getReplyExpr());
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        if (currentCallableUnit instanceof Resource) {
            throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                    returnStmt.getNodeLocation().getLineNumber() +
                    ": return statement cannot be used in a resource definition");
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
            throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                    returnStmt.getNodeLocation().getLineNumber() +
                    ": not enough arguments to return");
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
                throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                        returnStmt.getNodeLocation().getLineNumber() +
                        ": too many arguments to return");

            } else if (funcIExprReturnTypes.length < returnParamsOfCU.length) {
                throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                        returnStmt.getNodeLocation().getLineNumber() +
                        ": not enough arguments to return");

            }

            for (int i = 0; i < returnParamsOfCU.length; i++) {
                if (!funcIExprReturnTypes[i].equals(returnParamsOfCU[i].getType())) {
                    throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                            returnStmt.getNodeLocation().getLineNumber() +
                            ": cannot use " + funcIExprReturnTypes[i] + " as type " +
                            returnParamsOfCU[i].getType() + " in return statement");
                }
            }

            return;
        }

        if (typesOfReturnExprs.length > returnParamsOfCU.length) {
            throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                    returnStmt.getNodeLocation().getLineNumber() +
                    ": too many arguments to return");

        } else if (typesOfReturnExprs.length < returnParamsOfCU.length) {
            throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                    returnStmt.getNodeLocation().getLineNumber() +
                    ": not enough arguments to return");

        } else {
            // Now we know that lengths for both arrays are equal.
            // Let's check their types
            for (int i = 0; i < returnParamsOfCU.length; i++) {
                // Check for ActionInvocationExprs in return arguments
                if (returnArgExprs[i] instanceof ActionInvocationExpr) {
                    throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                            returnStmt.getNodeLocation().getLineNumber() +
                            ": action invocation is not allowed in a return statement");
                }

                // Except for the first argument in return statement, fheck for FunctionInvocationExprs which return
                // multiple values.
                if (returnArgExprs[i] instanceof FunctionInvocationExpr) {
                    FunctionInvocationExpr funcIExpr = ((FunctionInvocationExpr) returnArgExprs[i]);
                    if (funcIExpr.getTypes().length > 1) {
                        throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                                returnStmt.getNodeLocation().getLineNumber() +
                                ": multiple-value " + funcIExpr.getCallableUnit().getName() +
                                "() in single-value context");
                    }
                }

                if (!typesOfReturnExprs[i].equals(returnParamsOfCU[i].getType())) {
                    throw new SemanticException(returnStmt.getNodeLocation().getFileName() + ":" +
                            returnStmt.getNodeLocation().getLineNumber() +
                            ": cannot use " + typesOfReturnExprs[i] + " as type " +
                            returnParamsOfCU[i].getType() + " in return statement");
                }
            }
        }
    }


    // Expressions

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        visitSingleValueExpr(instanceCreationExpr);

        if (BTypes.isValueType(instanceCreationExpr.getType())) {
            throw new SemanticException("Error: cannot use 'new' for value types: " + instanceCreationExpr.getType() +
                    " in " + instanceCreationExpr.getNodeLocation().getFileName() + ":" +
                    instanceCreationExpr.getNodeLocation().getLineNumber());
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
            throw new SemanticException(getNodeLocationStr(unaryExpr.getNodeLocation()) +
                    "unknown operator '" + unaryExpr.getOperator() + "' in unary expression");
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
        // Here we assume that rExpr of array access expression is always a variable reference expression.
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
            throw new SemanticException(getNodeLocationStr(refTypeInitExpr.getNodeLocation()) +
                    "reference type initializer is not allowed here");
        }

        Expression[] argExprs = refTypeInitExpr.getArgExprs();
        if (argExprs.length == 0) {
            refTypeInitExpr.setType(inheritedType);

        } else if (inheritedType instanceof BJSONType || inheritedType instanceof BMessageType) {
            // If there are arguments, then only Structs and Map types are supported.
            throw new SemanticException(getNodeLocationStr(refTypeInitExpr.getNodeLocation()) +
                    "struct/map initializer is not allowed here");
        }
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        BType inheritedType = connectorInitExpr.getInheritedType();
        if (!(inheritedType instanceof BallerinaConnectorDef) && !(inheritedType instanceof AbstractNativeConnector)) {
            throw new SemanticException(getNodeLocationStr(connectorInitExpr.getNodeLocation()) +
                    "connector initializer is not allowed here");
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
                    throw new SemanticException(getNodeLocationStr(connectorInitExpr.getNodeLocation()) +
                            "incompatible types: expected '" + argType +
                            "', found '" + connectorInitExpr.getArgExprs()[i].getType() + "'");
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
                throw new SemanticException(getNodeLocationStr(connectorInitExpr.getNodeLocation()) +
                        "incompatible types: expected '" + parameterDefs[i].getType() +
                        "', found '" + argExpr.getType() + "'");
            }
        }
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        BType inheritedType = arrayInitExpr.getInheritedType();
        if (!(inheritedType instanceof BArrayType)) {
            throw new SemanticException(getNodeLocationStr(arrayInitExpr.getNodeLocation()) +
                    "array initializer is not allowed here");
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
                    throw new SemanticException(getNodeLocationStr(arrayInitExpr.getNodeLocation()) +
                            "incompatible types: '" + argExprs[i].getType() +
                            "' cannot be converted to '" + expectedElementType + "'");
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
                throw new SemanticException(getNodeLocationStr(keyExpr.getNodeLocation()) +
                        "invalid field name in struct initializer");
            }

            VariableRefExpr varRefExpr = (VariableRefExpr) keyExpr;
            VariableDef varDef = (VariableDef) structDef.resolveMembers(varRefExpr.getSymbolName());
            if (varDef == null) {
                throw new SemanticException(getNodeLocationStr(keyExpr.getNodeLocation()) +
                        "unknown field '" + varRefExpr.getVarName() + "' in struct '" + structDef.getName() + "'");
            }
            varRefExpr.setVariableDef(varDef);
            Expression valueExpr = keyValueExpr.getValueExpr();
            visitSingleValueExpr(valueExpr);

            if (!valueExpr.getType().equals(varDef.getType())) {
                throw new SemanticException(getNodeLocationStr(keyExpr.getNodeLocation()) + "incompatible type: '" +
                        varDef.getType() + "' expected, found '" + valueExpr.getType() + "'");
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
                throw new SemanticException(getNodeLocationStr(mapInitExpr.getNodeLocation()) +
                        "invalid type '" + keyExpr.getType() + "' in map index: expected 'string'");
            }

            visitSingleValueExpr(keyValueExpr.getValueExpr());
        }
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        // In this case, type of the backtickExpr should be either xml or json
        BType inheritedType = backtickExpr.getInheritedType();
        if (inheritedType != BTypes.typeJSON && inheritedType != BTypes.typeXML) {
            throw new SemanticException(getNodeLocationStr(backtickExpr.getNodeLocation()) +
                    "incompatible types: expected json or xml");
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
        VariableDef variableDef = (VariableDef) currentScope.resolve(symbolName);
        if (variableDef == null) {
            throw new SemanticException(getNodeLocationStr(variableRefExpr.getNodeLocation()) +
                    ": undefined symbol '" + symbolName + "'");
        }

        variableRefExpr.setVariableDef(variableDef);
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
            typeCastExpression.setEvalFunc(newEdge.getTypeConvertorFunction());
        } else {
            linkTypeConverter(typeCastExpression, sourceType, targetType);
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

        // Handle the array type
        if (arrayMapVarRefExpr.getType() instanceof BArrayType) {

            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            visitSingleValueExpr(indexExpr);
            if (indexExpr.getType() != BTypes.typeInt) {
                throw new SemanticException(getNodeLocationStr(arrayMapAccessExpr.getNodeLocation()) +
                        "non-integer array index type '" + indexExpr.getType() + "'");
            }

            // Set type of the array access expression
            BType typeOfArray = ((BArrayType) arrayMapVarRefExpr.getType()).getElementType();
            arrayMapAccessExpr.setType(typeOfArray);

        } else if (arrayMapVarRefExpr.getType() instanceof BMapType) {

            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            visitSingleValueExpr(indexExpr);
            if (indexExpr.getType() != BTypes.typeString) {
                throw new SemanticException(getNodeLocationStr(arrayMapAccessExpr.getNodeLocation()) +
                        "non-string map index type '" + indexExpr.getType() + "'");
            }

            // Set type of the map access expression
            BType typeOfMap = arrayMapVarRefExpr.getType();
            arrayMapAccessExpr.setType(typeOfMap);

        } else {
            throw new SemanticException(getNodeLocationStr(arrayMapAccessExpr.getNodeLocation()) +
                    "invalid operation: type '" + arrayMapVarRefExpr.getType() + "' does not support indexing");
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
            throw new SemanticException(getNodeLocationStr(expr.getNodeLocation()) +
                    ": multiple-value '" + nameWithPkgName + "()' in single-value context");
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
                    newExpr.setEvalFunc(newEdge.getTypeConvertorFunction());
                    newExpr.accept(this);
                    binaryExpr.setRExpr(newExpr);
                    return lType;
                } else {
                    newEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(lType, rType, null);
                    if (newEdge != null) { // Implicit cast from left to right
                        newExpr = new TypeCastExpression(lExpr.getNodeLocation(), lExpr, rType);
                        newExpr.setEvalFunc(newEdge.getTypeConvertorFunction());
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
            throw new SemanticException(assignStmt.getNodeLocation().getFileName() + ":"
                    + assignStmt.getNodeLocation().getLineNumber() + ": cannot assign a value to constant '" +
                    ((VariableRefExpr) lExpr).getSymbolName() + "'");
        }
    }

    private void checkForMultiAssignmentErrors(AssignStmt assignStmt, Expression[] lExprs,
                                               CallableUnitInvocationExpr rExpr) {
        BType[] returnTypes = rExpr.getTypes();
        if (lExprs.length != returnTypes.length) {
            throw new SemanticException(assignStmt.getNodeLocation().getFileName() + ":"
                    + assignStmt.getNodeLocation().getLineNumber() + ": assignment count mismatch: " +
                    lExprs.length + " = " + returnTypes.length);
        }

        //cannot assign string to b (type int) in multiple assignment

        for (int i = 0; i < lExprs.length; i++) {
            Expression lExpr = lExprs[i];
            BType returnType = returnTypes[i];
            if (!lExpr.getType().equals(returnType)) {
                String varName = getVarNameFromExpression(lExpr);
                throw new SemanticException(assignStmt.getNodeLocation().getFileName() + ":"
                        + assignStmt.getNodeLocation().getLineNumber() + ": cannot assign " + returnType + " to '" +
                        varName + "' (type " + lExpr.getType() + ") in multiple assignment");
            }
        }
    }

    private void visitLExprsOfAssignment(AssignStmt assignStmt, Expression[] lExprs) {
        // This set data structure is used to check for repeated variable names in the assignment statement
        Set<String> varNameSet = new HashSet<>();

        for (Expression lExpr : lExprs) {
            String varName = getVarNameFromExpression(lExpr);
            if (!varNameSet.add(varName)) {
                throw new SemanticException(assignStmt.getNodeLocation().getFileName() + ":"
                        + assignStmt.getNodeLocation().getLineNumber() + ": '" + varName + "' is repeated " +
                        "on the left side of assignment");
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
            throw new SemanticException(funcIExpr.getNodeLocation().getFileName() + ":" +
                    funcIExpr.getNodeLocation().getLineNumber() +
                    ": undefined function '" + funcName + "'");
        }

        Function function;
        if (functionSymbol instanceof NativeUnitProxy) {
            function = (Function) ((NativeUnitProxy) functionSymbol).load();

            // Loading return parameter types of this native function
            NativeUnit nativeUnit = (NativeUnit) function;
            SimpleTypeName[] returnParamTypeNames = nativeUnit.getReturnParamTypeNames();
            BType[] returnTypes = new BType[returnParamTypeNames.length];
            for (int i = 0; i < returnParamTypeNames.length; i++) {
                SimpleTypeName typeName = returnParamTypeNames[i];
                BType bType = BTypes.resolveType(typeName, currentScope, funcIExpr.getNodeLocation());
                returnTypes[i] = bType;
            }
            function.setReturnParamTypes(returnTypes);

        } else {
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
            throw new SemanticException(getNodeLocationStr(actionIExpr.getNodeLocation()) + "" +
                    "undefined connector '" + connectorWithPkgName + "'");
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
        BLangSymbol actionSymbol;
        if (connectorSymbol instanceof NativeUnitProxy) {
            AbstractNativeConnector connector = (AbstractNativeConnector) ((NativeUnitProxy) connectorSymbol).load();
            actionSymbol = connector.resolveMembers(symbolName);
        } else {
            actionSymbol = ((BallerinaConnectorDef) connectorSymbol).resolveMembers(symbolName);
        }

        if (actionSymbol == null) {
            String actionWithConnector = actionIExpr.getConnectorName() + "." + actionIExpr.getName();
            String actionName = (actionIExpr.getPackageName() != null) ? actionIExpr.getPackageName() + ":" +
                    actionWithConnector : actionWithConnector;
            throw new SemanticException(actionIExpr.getNodeLocation().getFileName() + ":" +
                    actionIExpr.getNodeLocation().getLineNumber() +
                    ": undefined action '" + actionName + "'");
        }

        // Load native action
        Action action;
        if (actionSymbol instanceof NativeUnitProxy) {
            action = (Action) ((NativeUnitProxy) actionSymbol).load();

            // Loading return parameter types of this native function
            NativeUnit nativeUnit = (NativeUnit) action;
            SimpleTypeName[] returnParamTypeNames = nativeUnit.getReturnParamTypeNames();
            BType[] returnTypes = new BType[returnParamTypeNames.length];
            for (int i = 0; i < returnParamTypeNames.length; i++) {
                SimpleTypeName typeName = returnParamTypeNames[i];
                BType bType = BTypes.resolveType(typeName, currentScope, actionIExpr.getNodeLocation());
                returnTypes[i] = bType;
            }
            action.setReturnParamTypes(returnTypes);

        } else {
            action = (Action) actionSymbol;
        }

        // Link the action with the action invocation expression
        actionIExpr.setCallableUnit(action);
    }

    private void throwInvalidBinaryOpError(BinaryExpression binaryExpr) {
        String locationStr = getNodeLocationStr(binaryExpr.getNodeLocation());
        BType lExprType = binaryExpr.getLExpr().getType();
        BType rExprType = binaryExpr.getRExpr().getType();

        if (lExprType == rExprType) {
            throw new SemanticException(locationStr + "invalid operation: operator " + binaryExpr.getOperator() +
                    " not defined on '" + lExprType + "'");
        } else {
            throw new SemanticException(locationStr + "invalid operation: incompatible types '" + lExprType +
                    "' and '" + rExprType + "'");
        }
    }

    private void throwInvalidUnaryOpError(UnaryExpression unaryExpr) {
        String locationStr = getNodeLocationStr(unaryExpr.getNodeLocation());
        BType rExprType = unaryExpr.getRExpr().getType();

        throw new SemanticException(locationStr + "invalid operation: operator " + unaryExpr.getOperator() +
                " not defined on '" + rExprType + "'");
    }

    private void visitStructField(StructFieldAccessExpr structFieldAccessExpr, SymbolScope currentScope) {
        ReferenceExpr varRefExpr = structFieldAccessExpr.getVarRef();
        SymbolName symbolName = varRefExpr.getSymbolName();
        BLangSymbol fieldSymbol = currentScope.resolve(symbolName);

        if (fieldSymbol == null) {
            if (currentScope instanceof StructDef) {
                throw new SemanticException(getNodeLocationStr(structFieldAccessExpr.getNodeLocation()) + "field '" +
                        symbolName.getName() + "' not found in struct '" + ((StructDef) currentScope).getName() + "'.");
            } else {
                throw new SemanticException(getNodeLocationStr(structFieldAccessExpr.getNodeLocation()) + "struct '" +
                        symbolName.getName() + "' not found.");
            }
        }

        // Set expression type
        VariableDef varDef = (VariableDef) fieldSymbol;
        BType exprType;
        

        /* Get the actual var representation of this field, and semantically analyze. This will check for semantic
         * errors of array/map accesses, used in this struct field.
         * eg: in dpt.employee[2].name , below will check for semantics of 'employee[2]',
         * treating them as individual array/map variables.
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
                throw new SemanticException(getNodeLocationStr(structFieldAccessExpr.getNodeLocation()) + "'" +
                        symbolName.getName() + "' must be of struct type");
            }
            visitStructField(fieldExpr, ((StructDef) exprType).getSymbolScope());
        }
    }

    private void linkTypeConverter(TypeCastExpression typeCastExpression, BType sourceType, BType targetType) {
        TypeEdge newEdge = null;
        TypeConvertor typeConvertor;
        // First check on this package
        newEdge = packageTypeLattice.getEdgeFromTypes(sourceType, targetType, currentPkg);
        if (newEdge != null) {
            typeConvertor = newEdge.getTypeConvertor();
            if (typeConvertor != null) {
                typeCastExpression.setCallableUnit(typeConvertor);
            }
        } else {
            newEdge = TypeLattice.getExplicitCastLattice().getEdgeFromTypes(sourceType, targetType, currentPkg);
            if (newEdge != null) {
                typeConvertor = newEdge.getTypeConvertor();
                if (typeConvertor != null) {
                    typeCastExpression.setCallableUnit(typeConvertor);
                }
            } else {
                newEdge = TypeLattice.getExplicitCastLattice().getEdgeFromTypes(sourceType, targetType, null);
                if (newEdge != null) {
                    typeConvertor = newEdge.getTypeConvertor();
                    if (typeConvertor != null) {
                        typeCastExpression.setCallableUnit(typeConvertor);
                    }
                } else {
                    String pkgPath = typeCastExpression.getPackagePath();

                    Expression[] exprs = typeCastExpression.getArgExprs();
                    BType[] paramTypes = new BType[exprs.length];
                    for (int i = 0; i < exprs.length; i++) {
                        paramTypes[i] = exprs[i].getType();
                    }

                    SymbolName symbolName = LangModelUtils.getTypeConverterSymNameWithoutPackage
                            (sourceType, targetType);
                    BLangSymbol typeConvertorSymbol = currentScope.resolve(symbolName);
                    if (typeConvertorSymbol == null) {
                        String funcName = (typeCastExpression.getPackageName() != null) ?
                                typeCastExpression.getPackageName() + ":" +
                                        typeCastExpression.getName() : typeCastExpression.getName();
                        throw new SemanticException(getNodeLocationStr(typeCastExpression.getNodeLocation()) +
                                "'" + sourceType + "' cannot be cast to '" + targetType + "'");
                    }

                    if (typeConvertorSymbol instanceof NativeUnitProxy) {
                        typeConvertor = (TypeConvertor) ((NativeUnitProxy) typeConvertorSymbol).load();
                        // TODO We need to find a way to load input parameter types

                        // Loading return parameter types of this native function
                        NativeUnit nativeUnit = (NativeUnit) typeConvertor;
                        SimpleTypeName[] returnParamTypeNames = nativeUnit.getReturnParamTypeNames();
                        BType[] returnTypes = new BType[returnParamTypeNames.length];
                        for (int i = 0; i < returnParamTypeNames.length; i++) {
                            SimpleTypeName typeName = returnParamTypeNames[i];
                            BType bType = BTypes.resolveType(typeName, currentScope,
                                    typeCastExpression.getNodeLocation());
                            returnTypes[i] = bType;
                        }
                        typeConvertor.setReturnParamTypes(returnTypes);

                    } else {
                        typeConvertor = (TypeConvertor) typeConvertorSymbol;
                    }

                    if (typeConvertor != null) {
                        typeConvertor.setParameterTypes(paramTypes);
                        // Link the function with the function invocation expression
                        typeCastExpression.setCallableUnit(typeConvertor);
                    } else {
                        throw new SemanticException(getNodeLocationStr(typeCastExpression.getNodeLocation()) +
                                "'" + sourceType + "' cannot be cast to '" + targetType + "'");
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
            newExpr.setEvalFunc(newEdge.getTypeConvertorFunction());
        }
        return newExpr;
    }

    private void setMemoryLocation(VariableDef variableDef) {
        if (currentScope.getScopeName() == SymbolScope.ScopeName.LOCAL) {
            variableDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
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
                throw new SemanticException(getNodeLocationStr(constDef.getNodeLocation()) +
                        "redeclared symbol '" + constDef.getName() + "'");
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
                throw new SemanticException(getNodeLocationStr(function.getNodeLocation()) + "undefined function '" +
                        function.getName() + "'");
            }

            if (!function.isNative()) {
                if (functionSymbol != null) {
                    throw new SemanticException(getNodeLocationStr(function.getNodeLocation()) + "redeclared symbol '" +
                            function.getName() + "'");
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

    private void defineTypeConvertors(TypeLattice typeLattice) {
        for (TypeEdge typeEdge : typeLattice.getEdges()) {
            TypeConvertor typeConvertor = typeEdge.getTypeConvertor();
            // Resolve input parameters
            ParameterDef[] paramDefArray = typeConvertor.getParameterDefs();
            BType[] paramTypes = new BType[paramDefArray.length];
            for (int i = 0; i < paramDefArray.length; i++) {
                ParameterDef paramDef = paramDefArray[i];
                BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
                paramDef.setType(bType);
                paramTypes[i] = bType;
            }

            typeConvertor.setParameterTypes(paramTypes);
            SymbolName symbolName = LangModelUtils.getSymNameWithParams(typeConvertor.getName(),
                    typeConvertor.getPackagePath(), paramTypes);
            typeConvertor.setSymbolName(symbolName);

            BLangSymbol typConvertorSymbol = currentScope.resolve(symbolName);

            if (typeConvertor.isNative() && typConvertorSymbol == null) {
                throw new SemanticException(getNodeLocationStr(typeConvertor.getNodeLocation()) + "undefined type " +
                        "convertor '" + typeConvertor.getName() + "'");
            }

            if (!typeConvertor.isNative()) {
                if (typConvertorSymbol != null) {
                    throw new SemanticException(typeConvertor.getNodeLocation().getFileName() + ":" +
                            typeConvertor.getNodeLocation().getLineNumber() + ": redeclared symbol '" +
                            typeConvertor.getName() + "'");
                }
                currentScope.define(symbolName, typeConvertor);
            }

            // Resolve return parameters
            ParameterDef[] returnParameters = typeConvertor.getReturnParameters();
            BType[] returnTypes = new BType[returnParameters.length];
            for (int i = 0; i < returnParameters.length; i++) {
                ParameterDef paramDef = returnParameters[i];
                BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
                paramDef.setType(bType);
                returnTypes[i] = bType;
            }
            typeConvertor.setReturnParamTypes(returnTypes);
        }
    }

    private void defineConnectors(BallerinaConnectorDef[] connectorDefArray) {
        for (BallerinaConnectorDef connectorDef : connectorDefArray) {
            String connectorName = connectorDef.getName();

            // Define ConnectorDef Symbol in the package scope..
            SymbolName connectorSymbolName = new SymbolName(connectorName);

            BLangSymbol connectorSymbol = currentScope.resolve(connectorSymbolName);
            if (connectorDef.isNative() && connectorSymbol == null) {
                throw new SemanticException(getNodeLocationStr(connectorDef.getNodeLocation()) +
                        "undefined connector '" + connectorDef.getName() + "'");
            }

            if (!connectorDef.isNative()) {
                if (connectorSymbol != null) {
                    throw new SemanticException(getNodeLocationStr(connectorDef.getNodeLocation()) +
                            "redeclared symbol '" + connectorDef.getName() + "'");
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
                throw new SemanticException(getNodeLocationStr(connectorDef.getNodeLocation()) + "undefined action '" +
                        action.getName() + "' in connector '" + connectorDef.getName() + "'");
            }
        } else {
            if (actionSymbol != null) {
                throw new SemanticException(
                        getNodeLocationStr(action.getNodeLocation()) + "redeclared symbol '" + action.getName() + "'");
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
                throw new SemanticException(getNodeLocationStr(service.getNodeLocation()) +
                        "redeclared symbol '" + service.getName() + "'");
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
            throw new SemanticException(getNodeLocationStr(resource.getNodeLocation()) +
                    "redeclared symbol '" + resource.getName() + "'");
        }
        currentScope.define(symbolName, resource);
    }

    private void defineStructs(StructDef[] structDefs) {
        for (StructDef structDef : structDefs) {
            SymbolName symbolName = new SymbolName(structDef.getName());

            // Check whether this constant is already defined.
            if (currentScope.resolve(symbolName) != null) {
                throw new SemanticException(getNodeLocationStr(structDef.getNodeLocation()) +
                        "redeclared symbol '" + structDef.getName() + "'");
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
}

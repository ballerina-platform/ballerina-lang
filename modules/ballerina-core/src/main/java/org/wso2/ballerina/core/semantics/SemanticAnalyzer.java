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

import org.wso2.ballerina.core.exception.LinkerException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.LocalVarLocation;
import org.wso2.ballerina.core.interpreter.MemoryLocation;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.StructVarLocation;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.interpreter.SymTable;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BTypeConvertor;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.BallerinaStruct;
import org.wso2.ballerina.core.model.CallableUnit;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Operator;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.StructDcl;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
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
import org.wso2.ballerina.core.model.expressions.DivideExpr;
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
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.BArrayType;
import org.wso2.ballerina.core.model.types.BMapType;
import org.wso2.ballerina.core.model.types.BStructType;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.types.TypeEdge;
import org.wso2.ballerina.core.model.types.TypeLattice;
import org.wso2.ballerina.core.model.util.LangModelUtils;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private SymTable symbolTable;
    private String currentPkg;
    private TypeLattice packageTypeLattice;
    private CallableUnit currentCallableUnit = null;
    // following pattern matches ${anyString} or ${anyString[int]} or ${anyString["anyString"]}
    private static final String patternString = "\\$\\{((\\w+)(\\[(\\d+|\\\"(\\w+)\\\")\\])?)\\}";
    private static final Pattern compiledPattern = Pattern.compile(patternString);

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
        
        // Add struct symbols to symbol table
        for (BallerinaStruct struct : bFile.getStructs()) {
            addStructSymbol(struct);
        }
        
        bFile.getConnectorList().forEach(connector -> {
            addConnectorSymbol(connector);
            Arrays.asList(connector.getActions()).forEach(this::addActionSymbol);
        });

        packageTypeLattice = bFile.getTypeLattice();
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

        for (BallerinaStruct struct: bFile.getStructs()) {
            struct.accept(this);
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

        for (TypeEdge typeEdge : bFile.getTypeLattice().getEdges()) {
            BTypeConvertor typeConvertor = (BTypeConvertor) typeEdge.getTypeConvertor();
            typeConvertor.accept(this);
        }

        int setSizeOfStaticMem = staticMemAddrOffset + 1;
        bFile.setSizeOfStaticMem(setSizeOfStaticMem);
        staticMemAddrOffset = -1;
    }

    @Override
    public void visit(ImportPackage importPkg) {
        if (importPkgMap.containsKey(importPkg.getName())) {
            throw new RuntimeException("Duplicate import package declaration: " + importPkg.getPath() + " in " +
                    importPkg.getLocation().getFileName() + ":" + importPkg.getLocation().getLine());
        }

        importPkgMap.put(importPkg.getName(), importPkg);
    }

    @Override
    public void visit(Const constant) {
        SymbolName symName = constant.getName();

        Symbol symbol = symbolTable.lookup(symName);
        if (symbol != null && isSymbolInCurrentScope(symbol)) {
            throw new SemanticException("Duplicate constant name: " + symName.getName() + " in " +
                    constant.getLocation().getFileName() + ":" + constant.getLocation().getLine());
        }

        BasicLiteral basicLiteral = (BasicLiteral) constant.getValueExpr();
        constant.setValue(basicLiteral.getBValue());

        ConstantLocation location = new ConstantLocation(staticMemAddrOffset);
        BType type = constant.getType();
        
        // constants can be only of value types.
        if (!BTypes.isValueType(type)) {
            throw new SemanticException(getLocationStr(constant.getLocation()) + "constant cannot be of type '" 
                    + type.toString() + "'.");
        }
        validateType(type, constant.getLocation());
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
        currentCallableUnit = resource;

        // Check whether the return statement is missing. Ignore if the function does not return anything.
        //checkForMissingReplyStmt(resource);

        for (Parameter parameter : resource.getParameters()) {
            stackFrameOffset++;
            visit(parameter);
        }
        
        for (Parameter parameter : resource.getReturnParameters()) {
            validateType(parameter.getType(), parameter.getLocation());
        }

        for (ConnectorDcl connectorDcl : resource.getConnectorDcls()) {
            stackFrameOffset++;
            visit(connectorDcl);
        }

        for (VariableDcl variableDcl : resource.getVariableDcls()) {
            stackFrameOffset++;
            visit(variableDcl);
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
        openScope(SymScope.Name.FUNCTION);
        currentCallableUnit = function;

        // Check whether the return statement is missing. Ignore if the function does not return anything.
        // TODO Define proper error message codes
        //checkForMissingReturnStmt(function, "missing return statement at end of function");

        for (Parameter parameter : function.getParameters()) {
            stackFrameOffset++;
            visit(parameter);
        }
        
        for (ConnectorDcl connectorDcl : function.getConnectorDcls()) {
            stackFrameOffset++;
            visit(connectorDcl);
        }

        for (VariableDcl variableDcl : function.getVariableDcls()) {
            stackFrameOffset++;
            visit(variableDcl);
        }
        
        for (Parameter parameter : function.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameter.getName() == null) {
                break;
            }

            stackFrameOffset++;
            visit(parameter);
        }

        BlockStmt blockStmt = function.getCallableUnitBody();
        blockStmt.accept(this);

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
        openScope(SymScope.Name.TYPECONVERTOR);
        currentCallableUnit = typeConvertor;

        // Check whether the return statement is missing. Ignore if the function does not return anything.
        // TODO Define proper error message codes
        //checkForMissingReturnStmt(function, "missing return statement at end of function");

        for (Parameter parameter : typeConvertor.getParameters()) {
            stackFrameOffset++;
            visit(parameter);
        }

        for (VariableDcl variableDcl : typeConvertor.getVariableDcls()) {
            stackFrameOffset++;
            visit(variableDcl);
        }

        for (Parameter parameter : typeConvertor.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameter.getName() == null) {
                break;
            }

            stackFrameOffset++;
            visit(parameter);
        }

        BlockStmt blockStmt = typeConvertor.getCallableUnitBody();
        blockStmt.accept(this);

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
        openScope(SymScope.Name.ACTION);
        currentCallableUnit = action;

        // Check whether the return statement is missing. Ignore if the function does not return anything.
        // TODO Define proper error message codes
        //checkForMissingReturnStmt(action, "missing return statement at end of action");

        for (Parameter parameter : action.getParameters()) {
            stackFrameOffset++;
            visit(parameter);
        }
        
        for (ConnectorDcl connectorDcl : action.getConnectorDcls()) {
            stackFrameOffset++;
            visit(connectorDcl);
        }

        for (VariableDcl variableDcl : action.getVariableDcls()) {
            stackFrameOffset++;
            visit(variableDcl);
        }
        
        for (Parameter parameter : action.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameter.getName() == null) {
                break;
            }

            stackFrameOffset++;
            visit(parameter);
        }

        BlockStmt blockStmt = action.getCallableUnitBody();
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
        currentCallableUnit = null;
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
            throw new SemanticException(parameter.getLocation().getFileName() + ":" +
                    parameter.getLocation().getLine() + ": duplicate parameter '" + symName.getName() + "'");
        }

        MemoryLocation location;
        if (isInScope(SymScope.Name.CONNECTOR)) {
            location = new ConnectorVarLocation(connectorMemAddrOffset);
        } else if (isInScope(SymScope.Name.FUNCTION) ||
                isInScope(SymScope.Name.RESOURCE) ||
                isInScope(SymScope.Name.ACTION) ||
                isInScope(SymScope.Name.TYPECONVERTOR)) {

            location = new LocalVarLocation(stackFrameOffset);
        } else {
            // This error should not be thrown
            throw new IllegalStateException("Parameter declaration is invalid");
        }

        BType type = parameter.getType();
        validateType(type, parameter.getLocation());
        symbol = new Symbol(type, currentScopeName(), location);
        symbolTable.insert(symName, symbol);
    }

    @Override
    public void visit(VariableDcl variableDcl) {
        BType type = variableDcl.getType();
        validateType(type, variableDcl.getLocation());
        
        SymbolName symName = variableDcl.getName();
        Symbol symbol = symbolTable.lookup(symName);
        if (symbol != null && isSymbolInCurrentScope(symbol)) {
            throw new SemanticException(getLocationStr(variableDcl.getLocation()) + "Duplicate variable '" + 
                    symName.getName() + "'.");
        }

        MemoryLocation location;
        if (isInScope(SymScope.Name.CONNECTOR)) {
            location = new ConnectorVarLocation(connectorMemAddrOffset);
        } else if (isInScope(SymScope.Name.SERVICE)) {
            location = new ServiceVarLocation(staticMemAddrOffset);
        } else if (isInScope(SymScope.Name.FUNCTION) ||
                isInScope(SymScope.Name.RESOURCE) ||
                isInScope(SymScope.Name.ACTION) ||
                isInScope(SymScope.Name.TYPECONVERTOR)) {
            location = new LocalVarLocation(stackFrameOffset);
        } else if (isInScope(SymScope.Name.PACKAGE)) {
            location = new StructVarLocation(structMemAddrOffset);
        } else {
            // This error should not be thrown
            throw new IllegalStateException("Variable declaration is invalid");
        }

        symbol = new Symbol(type, currentScopeName(), location);
        symbolTable.insert(symName, symbol);
    }
    
    /**
     * Check whether the Type is a valid one.
     * A valid type can be either a primitive type, or a user defined type.
     * 
     * @param type   type name
     * @param location source location
     */
    private void validateType(BType type, Position location) {
        if (type instanceof BArrayType) {
            type = ((BArrayType) type).getElementType();
        }
        if (type instanceof BStructType) {
            // If the type of the variable is a user defined type, then check whether the
            // type is defined.
            Symbol structSymbol = symbolTable.lookup(new SymbolName(type.toString()));
            if (structSymbol == null) {
                throw new SemanticException(getLocationStr(location) + "type '" + type + "' is undefined.");
            }
        }
    }

    @Override
    public void visit(ConnectorDcl connectorDcl) {
        SymbolName symbolName = connectorDcl.getVarName();

        Symbol symbol = symbolTable.lookup(symbolName);
        if (symbol != null && isSymbolInCurrentScope(symbol)) {
            throw new SemanticException("Duplicate connector declaration with name: " + symbolName.getName() + " in " +
                    connectorDcl.getLocation().getFileName() + ":" + connectorDcl.getLocation().getLine());
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
            throw new SemanticException("Connector : " + connectorName + " not found in " +
                    connectorDcl.getLocation().getFileName() + ":" + connectorDcl.getLocation().getLine());
        }
        connectorDcl.setConnector(connectorSym.getConnector());

        // Visit connector arguments
        for (Expression argExpr : connectorDcl.getArgExprs()) {
            argExpr.accept(this);
        }
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        Expression[] lExprs = assignStmt.getLExprs();
        visitLExprsOfAssignment(assignStmt, lExprs);

        Expression rExpr = assignStmt.getRExpr();
        rExpr.accept(this);

        // Return types of the function or action invoked are only available during the linking phase
        // There type compatibility check is impossible during the semantic analysis phase.
        if (rExpr instanceof FunctionInvocationExpr || rExpr instanceof ActionInvocationExpr) {
            checkForMultiAssignmentErrors(assignStmt, lExprs, (CallableUnitInvocationExpr) rExpr);
            return;
        }

        // Now we know that this is a single value assignment statement.
        Expression lExpr = assignStmt.getLExprs()[0];

        // If the rExpr typ is not set, then check whether it is a BacktickExpr
        if (rExpr.getType() == null && rExpr instanceof BacktickExpr) {

            // In this case, type of the lExpr should be either xml or json
            if (!(lExpr.getType().equals(BTypes.JSON_TYPE)) && !(lExpr.getType().equals(BTypes.XML_TYPE))) {
                throw new SemanticException(getLocationStr(lExpr.getLocation()) + "incompatible types: expected json" +
                    " or xml on the left side of assignment");
            }

            rExpr.setType(lExpr.getType());
        }

        // TODO Remove the MAP related logic when type casting is implemented
        if (!(lExpr.getType().equals(BTypes.MAP_TYPE)) && !(rExpr.getType().equals(BTypes.MAP_TYPE)) &&
                (!lExpr.getType().equals(rExpr.getType()))) {
            if (rExpr instanceof ArrayInitExpr && lExpr.getType() instanceof BArrayType) {
                Expression[] args = ((ArrayInitExpr) rExpr).getArgExprs();
                Expression[] results = new Expression[args.length];
                int count = 0;
                for (Expression expression : args) {
                    TypeCastExpression newExpr = checkWideningPossible(
                            ((BArrayType) lExpr.getType()).getElementType(), expression, null);
                    if (newExpr != null) {
                        newExpr.accept(this);
                        results[count++] = newExpr;
                    } else {
                        throw new SemanticException(lExpr.getLocation().getFileName() + ":"
                                + lExpr.getLocation().getLine() + ": incompatible types: " + rExpr.getType() +
                                " cannot be converted to " + lExpr.getType());
                    }
                }
                ArrayInitExpr.ArrayInitExprBuilder builder = new ArrayInitExpr.ArrayInitExprBuilder();
                builder.setArgList(Arrays.asList(results));
                ArrayInitExpr newArrayInitExpr = builder.build();
                newArrayInitExpr.accept(this);
                assignStmt.setRhsExpr(newArrayInitExpr);

            } else {
                TypeCastExpression newExpr = checkWideningPossible(lExpr.getType(), rExpr, null);
                if (newExpr != null) {
                    newExpr.accept(this);
                    assignStmt.setRhsExpr(newExpr);
                } else {
                    throw new SemanticException(lExpr.getLocation().getFileName() + ":"
                            + lExpr.getLocation().getLine() + ": incompatible types: " + rExpr.getType() +
                            " cannot be converted to " + lExpr.getType());
                }
            }
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

        if (!expr.getType().equals(BTypes.BOOLEAN_TYPE)) {
            throw new SemanticException("Incompatible types: expected a boolean expression in " +
                    expr.getLocation().getFileName() + ":" + expr.getLocation().getLine());
        }

        Statement thenBody = ifElseStmt.getThenBody();
        thenBody.accept(this);

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            elseIfCondition.accept(this);

            if (!elseIfCondition.getType().equals(BTypes.BOOLEAN_TYPE)) {
                throw new SemanticException("Incompatible types: expected a boolean expression in " +
                        elseIfCondition.getLocation().getFileName() + ":" + elseIfCondition.getLocation().getLine());
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

        if (!expr.getType().equals(BTypes.BOOLEAN_TYPE)) {
            throw new SemanticException("Incompatible types: expected a boolean expression in " +
                    whileStmt.getLocation().getFileName() + ":" + whileStmt.getLocation().getLine());
        }

        BlockStmt blockStmt = whileStmt.getBody();
        if (blockStmt.getStatements().length == 0) {
            // This can be optimized later to skip the while statement
            throw new SemanticException("No statements in the while loop in " +
                    blockStmt.getLocation().getFileName() + ":" + blockStmt.getLocation().getLine());
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
            throw new SemanticException(currentCallableUnit.getLocation().getFileName() + ":" +
                    currentCallableUnit.getLocation().getLine() +
                    ": reply statement cannot be used in a function definition");

        } else if (currentCallableUnit instanceof Action) {
            throw new SemanticException(currentCallableUnit.getLocation().getFileName() + ":" +
                    currentCallableUnit.getLocation().getLine() +
                    ": reply statement cannot be used in a action definition");
        }

        if (replyStmt.getReplyExpr() instanceof ActionInvocationExpr) {
            throw new SemanticException(currentCallableUnit.getLocation().getFileName() + ":" +
                    currentCallableUnit.getLocation().getLine() +
                    ": action invocation is not allowed in a reply statement");
        }

        replyStmt.getReplyExpr().accept(this);
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        if (currentCallableUnit instanceof Resource) {
            throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                    returnStmt.getLocation().getLine() +
                    ": return statement cannot be used in a resource definition");
        }

        // Expressions that this return statement contains.
        Expression[] returnArgExprs = returnStmt.getExprs();

        // Return parameters of the current function or actions
        Parameter[] returnParamsOfCU = currentCallableUnit.getReturnParameters();

        if (returnArgExprs.length == 0 && returnParamsOfCU.length == 0) {
            // Return stmt has no expressions and function/action does not return anything. Just return.
            return;
        }

        // Return stmt has no expressions, but function/action has returns. Check whether they are named returns
        if (returnArgExprs.length == 0 && returnParamsOfCU[0].getName() != null) {
            // This function/action has named return parameters.
            Expression[] returnExprs = new Expression[returnParamsOfCU.length];
            for (int i = 0; i < returnParamsOfCU.length; i++) {
                VariableRefExpr variableRefExpr = new VariableRefExpr(returnParamsOfCU[i].getName());
                visit(variableRefExpr);
                returnExprs[i] = variableRefExpr;
            }
            returnStmt.setExprs(returnExprs);
            return;

        } else if (returnArgExprs.length == 0) {
            // This function/action does not contain named return parameters.
            // Therefore this is a semantic error.
            throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                    returnStmt.getLocation().getLine() +
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
                throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                        returnStmt.getLocation().getLine() +
                        ": too many arguments to return");

            } else if (funcIExprReturnTypes.length < returnParamsOfCU.length) {
                throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                        returnStmt.getLocation().getLine() +
                        ": not enough arguments to return");

            }

            for (int i = 0; i < returnParamsOfCU.length; i++) {
                if (!funcIExprReturnTypes[i].equals(returnParamsOfCU[i].getType())) {
                    throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                            returnStmt.getLocation().getLine() +
                            ": cannot use " + funcIExprReturnTypes[i] + " as type " +
                            returnParamsOfCU[i].getType() + " in return statement");
                }
            }

            return;
        }

        if (typesOfReturnExprs.length > returnParamsOfCU.length) {
            throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                    returnStmt.getLocation().getLine() +
                    ": too many arguments to return");

        } else if (typesOfReturnExprs.length < returnParamsOfCU.length) {
            throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                    returnStmt.getLocation().getLine() +
                    ": not enough arguments to return");

        } else {
            // Now we know that lengths for both arrays are equal.
            // Let's check their types
            for (int i = 0; i < returnParamsOfCU.length; i++) {
                // Check for ActionInvocationExprs in return arguments
                if (returnArgExprs[i] instanceof ActionInvocationExpr) {
                    throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                            returnStmt.getLocation().getLine() +
                            ": action invocation is not allowed in a return statement");
                }

                // Except for the first argument in return statement, fheck for FunctionInvocationExprs which return
                // multiple values.
                if (returnArgExprs[i] instanceof FunctionInvocationExpr) {
                    FunctionInvocationExpr funcIExpr = ((FunctionInvocationExpr) returnArgExprs[i]);
                    if (funcIExpr.getTypes().length > 1) {
                        throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                                returnStmt.getLocation().getLine() +
                                ": multiple-value " + funcIExpr.getCallableUnit().getFunctionName() +
                                "() in single-value context");
                    }
                }

                if (!typesOfReturnExprs[i].equals(returnParamsOfCU[i].getType())) {
                    throw new SemanticException(returnStmt.getLocation().getFileName() + ":" +
                            returnStmt.getLocation().getLine() +
                            ": cannot use " + typesOfReturnExprs[i] + " as type " +
                            returnParamsOfCU[i].getType() + " in return statement");
                }
            }
        }
    }

    // Expressions

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        visitExpr(instanceCreationExpr);

        if (BTypes.isValueType(instanceCreationExpr.getType())) {
            throw new SemanticException("Error: cannot use 'new' for value types: " + instanceCreationExpr.getType() +
                    " in " + instanceCreationExpr.getLocation().getFileName() + ":" +
                    instanceCreationExpr.getLocation().getLine());
        }
        // TODO here the type shouldn't be a value type
//        Expression expr = instanceCreationExpr.getRExpr();
//        expr.accept(this);

    }

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        Expression[] exprs = funcIExpr.getArgExprs();
        for (Expression expr : exprs) {
            expr.accept(this);
        }

        linkFunction(funcIExpr);

        //Find the return types of this function invocation expression.
        Parameter[] returnParams = funcIExpr.getCallableUnit().getReturnParameters();
        BType[] returnTypes = new BType[returnParams.length];
        for (int i = 0; i < returnParams.length; i++) {
            returnTypes[i] = returnParams[i].getType();
        }
        funcIExpr.setTypes(returnTypes);
    }

    // TODO Duplicate code. fix me
    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        Expression[] exprs = actionIExpr.getArgExprs();
        for (Expression expr : exprs) {
            expr.accept(this);
        }

        linkAction(actionIExpr);

        //Find the return types of this function invocation expression.
        Parameter[] returnParams = actionIExpr.getCallableUnit().getReturnParameters();
        BType[] returnTypes = new BType[returnParams.length];
        for (int i = 0; i < returnParams.length; i++) {
            returnTypes[i] = returnParams[i].getType();
        }
        actionIExpr.setTypes(returnTypes);
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        BType arithmeticExprType = verifyBinaryArithmeticExprType(divideExpr);

        if (arithmeticExprType.equals(BTypes.INT_TYPE)) {
            divideExpr.setEvalFunc(DivideExpr.DIV_INT_FUNC);

        } else if (arithmeticExprType.equals(BTypes.FLOAT_TYPE)) {
            divideExpr.setEvalFunc(DivideExpr.DIV_FLOAT_FUNC);

        } else if (arithmeticExprType.equals(BTypes.DOUBLE_TYPE)) {
            divideExpr.setEvalFunc(DivideExpr.DIV_DOUBLE_FUNC);

        } else if (arithmeticExprType.equals(BTypes.LONG_TYPE)) {
            divideExpr.setEvalFunc(DivideExpr.DIV_LONG_FUNC);

        } else {
            throw new SemanticException("Divide operation is not supported for type: " + arithmeticExprType + " in " +
                    divideExpr.getLocation().getFileName() + ":" + divideExpr.getLocation().getLine());
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpr) {
        unaryExpr.getRExpr().accept(this);
        unaryExpr.setType(unaryExpr.getRExpr().getType());

        if (Operator.SUB.equals(unaryExpr.getOperator())) {
            if (unaryExpr.getType().equals(BTypes.INT_TYPE)) {
                unaryExpr.setEvalFunc(UnaryExpression.NEGATIVE_INT_FUNC);
            } else if (unaryExpr.getType().equals(BTypes.DOUBLE_TYPE)) {
                unaryExpr.setEvalFunc(UnaryExpression.NEGATIVE_DOUBLE_FUNC);
            } else if (unaryExpr.getType().equals(BTypes.LONG_TYPE)) {
                unaryExpr.setEvalFunc(UnaryExpression.NEGATIVE_LONG_FUNC);
            } else if (unaryExpr.getType().equals(BTypes.FLOAT_TYPE)) {
                unaryExpr.setEvalFunc(UnaryExpression.NEGATIVE_FLOAT_FUNC);
            } else {
                throw new SemanticException("Incompatible type in unary expression: " + unaryExpr.getType() + " in " +
                        unaryExpr.getLocation().getFileName() + ":" + unaryExpr.getLocation().getLine());
            }
        } else if (Operator.ADD.equals(unaryExpr.getOperator())) {
            if (unaryExpr.getType().equals(BTypes.INT_TYPE)) {
                unaryExpr.setEvalFunc(UnaryExpression.POSITIVE_INT_FUNC);
            } else if (unaryExpr.getType().equals(BTypes.DOUBLE_TYPE)) {
                unaryExpr.setEvalFunc(UnaryExpression.POSITIVE_DOUBLE_FUNC);
            } else if (unaryExpr.getType().equals(BTypes.LONG_TYPE)) {
                unaryExpr.setEvalFunc(UnaryExpression.POSITIVE_LONG_FUNC);
            } else if (unaryExpr.getType().equals(BTypes.FLOAT_TYPE)) {
                unaryExpr.setEvalFunc(UnaryExpression.POSITIVE_FLOAT_FUNC);
            } else {
                throw new SemanticException("Incompatible type in unary expression: " + unaryExpr.getType() + " in " +
                        unaryExpr.getLocation().getFileName() + ":" + unaryExpr.getLocation().getLine());
            }

        } else if (Operator.NOT.equals(unaryExpr.getOperator())) {
            if (unaryExpr.getType().equals(BTypes.BOOLEAN_TYPE)) {
                unaryExpr.setEvalFunc(UnaryExpression.NOT_BOOLEAN_FUNC);
            } else {
                throw new SemanticException("Incompatible type in unary expression: " + unaryExpr.getType() + " in " +
                        unaryExpr.getLocation().getFileName() + ":" + unaryExpr.getLocation().getLine()
                        + " 'Not' operation only supports boolean");
            }

        } else {
            throw new SemanticException("Incompatible operation for unary expression " +
                    unaryExpr.getOperator().name() + " in " + unaryExpr.getLocation().getFileName() + ":" +
                    unaryExpr.getLocation().getLine());
        }

    }

    @Override
    public void visit(AddExpression addExpr) {
        BType arithmeticExprType = verifyBinaryArithmeticExprType(addExpr);

        if (arithmeticExprType.equals(BTypes.INT_TYPE)) {
            addExpr.setEvalFunc(AddExpression.ADD_INT_FUNC);

        } else if (arithmeticExprType.equals(BTypes.FLOAT_TYPE)) {
            addExpr.setEvalFunc(AddExpression.ADD_FLOAT_FUNC);

        } else if (arithmeticExprType.equals(BTypes.LONG_TYPE)) {
            addExpr.setEvalFunc(AddExpression.ADD_LONG_FUNC);

        } else if (arithmeticExprType.equals(BTypes.DOUBLE_TYPE)) {
            addExpr.setEvalFunc(AddExpression.ADD_DOUBLE_FUNC);

        } else if (arithmeticExprType.equals(BTypes.STRING_TYPE)) {
            addExpr.setEvalFunc(AddExpression.ADD_STRING_FUNC);

        } else {
            throw new SemanticException("Add operation is not supported for type: " + arithmeticExprType + " in " +
                    addExpr.getLocation().getFileName() + ":" + addExpr.getLocation().getLine());
        }
    }

    @Override
    public void visit(MultExpression multExpr) {
        BType binaryExprType = verifyBinaryArithmeticExprType(multExpr);

        if (binaryExprType.equals(BTypes.INT_TYPE)) {
            multExpr.setEvalFunc(MultExpression.MULT_INT_FUNC);

        } else if (binaryExprType.equals(BTypes.FLOAT_TYPE)) {
            multExpr.setEvalFunc(MultExpression.MULT_FLOAT_FUNC);

        } else if (binaryExprType.equals(BTypes.DOUBLE_TYPE)) {
            multExpr.setEvalFunc(MultExpression.MULT_DOUBLE_FUNC);

        } else if (binaryExprType.equals(BTypes.LONG_TYPE)) {
            multExpr.setEvalFunc(MultExpression.MULT_LONG_FUNC);

        } else {
            throw new SemanticException("Multiply operation is not supported for type: " + binaryExprType + " in " +
                    multExpr.getLocation().getFileName() + ":" + multExpr.getLocation().getLine());
        }
    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        BType binaryExprType = verifyBinaryArithmeticExprType(subtractExpr);

        if (binaryExprType.equals(BTypes.INT_TYPE)) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_INT_FUNC);

        } else if (binaryExprType.equals(BTypes.FLOAT_TYPE)) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_FLOAT_FUNC);

        } else if (binaryExprType.equals(BTypes.DOUBLE_TYPE)) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_DOUBLE_FUNC);

        } else if (binaryExprType.equals(BTypes.LONG_TYPE)) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_LONG_FUNC);

        } else {
            throw new SemanticException("Subtract operation is not supported for type: " + binaryExprType + " in " +
                    subtractExpr.getLocation().getFileName() + ":" + subtractExpr.getLocation().getLine());
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

        if (compareExprType.equals(BTypes.INT_TYPE)) {
            expr.setEvalFunc(EqualExpression.EQUAL_INT_FUNC);

        } else if (compareExprType.equals(BTypes.FLOAT_TYPE)) {
            expr.setEvalFunc(EqualExpression.EQUAL_FLOAT_FUNC);

        } else if (compareExprType.equals(BTypes.BOOLEAN_TYPE)) {
            expr.setEvalFunc(EqualExpression.EQUAL_BOOLEAN_FUNC);

        } else if (compareExprType.equals(BTypes.STRING_TYPE)) {
            expr.setEvalFunc(EqualExpression.EQUAL_STRING_FUNC);

        } else {
            throw new SemanticException("Equals operation is not supported for type: " + compareExprType + " in " +
                    expr.getLocation().getFileName() + ":" + expr.getLocation().getLine());
        }
    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {
        BType compareExprType = verifyBinaryCompareExprType(notEqualExpr);

        if (compareExprType.equals(BTypes.INT_TYPE)) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_INT_FUNC);

        } else if (compareExprType.equals(BTypes.FLOAT_TYPE)) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_FLOAT_FUNC);

        } else if (compareExprType.equals(BTypes.BOOLEAN_TYPE)) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_BOOLEAN_FUNC);

        } else if (compareExprType.equals(BTypes.STRING_TYPE)) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_STRING_FUNC);

        } else {
            throw new SemanticException("NotEqual operation is not supported for type: " + compareExprType + " in " +
                    notEqualExpr.getLocation().getFileName() + ":" + notEqualExpr.getLocation().getLine());
        }
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {
        BType compareExprType = verifyBinaryCompareExprType(greaterEqualExpr);

        if (compareExprType.equals(BTypes.INT_TYPE)) {
            greaterEqualExpr.setEvalFunc(GreaterEqualExpression.GREATER_EQUAL_INT_FUNC);

        } else if (compareExprType.equals(BTypes.FLOAT_TYPE)) {
            greaterEqualExpr.setEvalFunc(GreaterEqualExpression.GREATER_EQUAL_FLOAT_FUNC);

        } else {
            throw new SemanticException("Greater than equal operation is not supported for type: " + compareExprType +
                    " in " + greaterEqualExpr.getLocation().getFileName() + ":" +
                    greaterEqualExpr.getLocation().getLine());
        }
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        BType compareExprType = verifyBinaryCompareExprType(greaterThanExpr);

        if (compareExprType.equals(BTypes.INT_TYPE)) {
            greaterThanExpr.setEvalFunc(GreaterThanExpression.GREATER_THAN_INT_FUNC);

        } else if (compareExprType.equals(BTypes.FLOAT_TYPE)) {
            greaterThanExpr.setEvalFunc(GreaterThanExpression.GREATER_THAN_FLOAT_FUNC);

        } else {
            throw new SemanticException("Greater than operation is not supported for type: " + compareExprType +
                    " in " + greaterThanExpr.getLocation().getFileName() + ":" +
                    greaterThanExpr.getLocation().getLine());
        }
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        BType compareExprType = verifyBinaryCompareExprType(lessEqualExpr);

        if (compareExprType.equals(BTypes.INT_TYPE)) {
            lessEqualExpr.setEvalFunc(LessEqualExpression.LESS_EQUAL_INT_FUNC);

        } else if (compareExprType.equals(BTypes.FLOAT_TYPE)) {
            lessEqualExpr.setEvalFunc(LessEqualExpression.LESS_EQUAL_FLOAT_FUNC);

        } else {
            throw new SemanticException("Less than equal operation is not supported for type: " + compareExprType +
                    " in " + lessEqualExpr.getLocation().getFileName() + ":" + lessEqualExpr.getLocation().getLine());
        }
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        BType compareExprType = verifyBinaryCompareExprType(lessThanExpr);

        if (compareExprType.equals(BTypes.INT_TYPE)) {
            lessThanExpr.setEvalFunc(LessThanExpression.LESS_THAN_INT_FUNC);

        } else if (compareExprType.equals(BTypes.FLOAT_TYPE)) {
            lessThanExpr.setEvalFunc(LessThanExpression.LESS_THAN_FLOAT_FUNC);

        } else {
            throw new SemanticException("Less than operation is not supported for type: " + compareExprType + " in "
                    + lessThanExpr.getLocation().getFileName() + ":" + lessThanExpr.getLocation().getLine());
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
        
        handleArrayType(arrayMapAccessExpr);

    }
    
    private void handleArrayType(ArrayMapAccessExpr arrayMapAccessExpr) {
        VariableRefExpr arrayMapVarRefExpr = (VariableRefExpr) arrayMapAccessExpr.getRExpr();
        // Handle the array type
        if (arrayMapVarRefExpr.getType() instanceof BArrayType) {
            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            indexExpr.accept(this);
            if (!indexExpr.getType().equals(BTypes.INT_TYPE)) {
                throw new SemanticException("Array index should be of type int, not " + indexExpr.getType().toString() +
                        ". Array name: " + arrayMapVarRefExpr.getSymbolName().getName() + " in "
                        + indexExpr.getLocation().getFileName() + ":" + indexExpr.getLocation().getLine());
            }
            // Set type of the array access expression
            BType typeOfArray = ((BArrayType) arrayMapVarRefExpr.getType()).getElementType();
            arrayMapAccessExpr.setType(typeOfArray);
        } else if (arrayMapVarRefExpr.getType() instanceof BMapType) {
            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            indexExpr.accept(this);
            if (!indexExpr.getType().equals(BTypes.STRING_TYPE)) {
                throw new SemanticException("Map index should be of type string, not " + indexExpr.getType().toString()
                        + ". Map name: " + arrayMapVarRefExpr.getSymbolName().getName() + " in "
                        + indexExpr.getLocation().getFileName() + ":" + indexExpr.getLocation().getLine());
            }
            // Set type of the map access expression
            BType typeOfMap = arrayMapVarRefExpr.getType();
            arrayMapAccessExpr.setType(typeOfMap);
        } else {
            throw new SemanticException("Attempt to index non-array, non-map variable: " +
                    arrayMapVarRefExpr.getSymbolName().getName() + " in " +
                    arrayMapVarRefExpr.getLocation().getFileName() + ":" + arrayMapVarRefExpr.getLocation().getLine());
        }
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        Expression[] argExprs = mapInitExpr.getArgExprs();

        if (argExprs.length == 0) {
            throw new SemanticException("Map initializer should have at least one argument" + " in "
                    + mapInitExpr.getLocation().getFileName() + ":" + mapInitExpr.getLocation().getLine());
        }

        for (int i = 0; i < argExprs.length; i++) {
            argExprs[i].accept(this);
        }

        // Type of this expression is map and internal data type cannot be identifier from declaration
        mapInitExpr.setType(BTypes.MAP_TYPE);
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        Expression[] argExprs = arrayInitExpr.getArgExprs();

        if (argExprs.length == 0) {
            throw new SemanticException("Array initializer should have at least one argument" + " in "
                    + arrayInitExpr.getLocation().getFileName() + ":" + arrayInitExpr.getLocation().getLine());
        }

        argExprs[0].accept(this);
        BType typeOfArray = argExprs[0].getType();

        for (int i = 1; i < argExprs.length; i++) {
            argExprs[i].accept(this);

            if (argExprs[i].getType() != typeOfArray) {
                throw new SemanticException("Incompatible types used in array initializer. All arguments must have " +
                        "the same type." + " in " + arrayInitExpr.getLocation().getFileName() + ":" +
                        arrayInitExpr.getLocation().getLine());
            }
        }

        arrayInitExpr.setType(BTypes.getArrayType(typeOfArray.toString()));
    }

    @Override
    public void visit(KeyValueExpression keyValueExpr) {

    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        // Analyze the string and create relevant tokens
        // First check the literals
        String[] literals = backtickExpr.getTemplateStr().split(patternString);
        // Split will always have at least one matching literal
        int i = 0;
        if (literals.length > i) {
            BasicLiteral basicLiteral = new BasicLiteral(new BString(literals[i]));
            visit(basicLiteral);
            backtickExpr.addExpression(basicLiteral);
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
                    indexExpr = new BasicLiteral(new BString(m.group(5)));
                    indexExpr.setType(BTypes.STRING_TYPE);
                } else {
                    indexExpr = new BasicLiteral(new BInteger(Integer.parseInt(m.group(4))));
                    indexExpr.setType(BTypes.INT_TYPE);
                }
                indexExpr.setLocation(backtickExpr.getLocation());
                SymbolName mapOrArrName = new SymbolName(m.group(2));

                ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder =
                        new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();

                VariableRefExpr arrayMapVarRefExpr = new VariableRefExpr(mapOrArrName);
                visit(arrayMapVarRefExpr);

                builder.setArrayMapVarRefExpr(arrayMapVarRefExpr);
                builder.setVarName(mapOrArrName);
                builder.setIndexExpr(indexExpr);
                ArrayMapAccessExpr arrayMapAccessExpr = builder.build();
                visit(arrayMapAccessExpr);
                backtickExpr.addExpression(arrayMapAccessExpr);
            } else {
                VariableRefExpr variableRefExpr = new VariableRefExpr(new SymbolName(m.group(1)));
                variableRefExpr.setLocation(backtickExpr.getLocation());
                visit(variableRefExpr);
                backtickExpr.addExpression(variableRefExpr);
            }
            if (literals.length > i) {
                BasicLiteral basicLiteral = new BasicLiteral(new BString(literals[i]));
                visit(basicLiteral);
                backtickExpr.addExpression(basicLiteral);
                i++;
            }
        }
        // TODO If the type is not set then just return
        visitExpr(backtickExpr);
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        SymbolName varName = variableRefExpr.getSymbolName();

        // Check whether this symName is declared
        Symbol symbol = getSymbol(varName, variableRefExpr.getLocation());

        variableRefExpr.setType(symbol.getType());
//        variableRefExpr.setOffset(symbol.getOffset());

        variableRefExpr.setMemoryLocation(symbol.getLocation());
    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        // Evaluate the expression and set the type
        typeCastExpression.getSourceExpression().accept(this);
        BType sourceType = typeCastExpression.getSourceExpression().getType();
        BType targetType = typeCastExpression.getTargetType();
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

    @Override
    public void visit(StructVarLocation structVarLocation) {
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

        if (symbolTable.lookup(symbolName) != null) {
            throw new SemanticException(function.getLocation().getFileName() + ":" + function.getLocation().getLine() +
                    ": duplicate function '" + function.getFunctionName() + "'");
        }

        Symbol symbol = new Symbol(function);
        symbolTable.insert(symbolName, symbol);
    }

    private void addActionSymbol(BallerinaAction action) {
        SymbolName actionSymbolName = action.getSymbolName();
        BType[] paramTypes = LangModelUtils.getTypesOfParams(action.getParameters());

        SymbolName symbolName =
                LangModelUtils.getActionSymName(actionSymbolName.getName(),
                        actionSymbolName.getConnectorName(),
                        actionSymbolName.getPkgName(), paramTypes);
        Symbol symbol = new Symbol(action);

        if (symbolTable.lookup(symbolName) != null) {
            throw new SemanticException("Duplicate action definition: " + symbolName + " in "
                    + action.getLocation().getFileName() + ":" + action.getLocation().getLine());
        }

        symbolTable.insert(symbolName, symbol);
    }

    private void addConnectorSymbol(BallerinaConnector connector) {
        Symbol symbol = new Symbol(connector);

        SymbolName symbolName = new SymbolName(connector.getPackageQualifiedName());
        if (symbolTable.lookup(symbolName) != null) {
            throw new SemanticException("Duplicate connector definition: " + symbolName + " in "
                    + connector.getLocation().getFileName() + ":" + connector.getLocation().getLine());
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

        if (!(lExpr.getType().equals(rExpr.getType()))) {
            TypeCastExpression newExpr = checkWideningPossible(lExpr.getType(), rExpr, binaryExpr.getOperator());
            if (newExpr != null) {
                newExpr.accept(this);
                binaryExpr.setRExpr(newExpr);
            } else {
                throw new SemanticException(binaryExpr.getLocation().getFileName() + ":" +
                        binaryExpr.getLocation().getLine() +
                        ": incompatible types in binary expression: " + lExpr.getType() + " vs " + rExpr.getType());
            }
        }

        return lExpr.getType();
    }

    private void visitBinaryLogicalExpr(BinaryLogicalExpression expr) {
        visitBinaryExpr(expr);

        Expression rExpr = expr.getRExpr();
        Expression lExpr = expr.getLExpr();

        if (lExpr.getType().equals(BTypes.BOOLEAN_TYPE) && rExpr.getType().equals(BTypes.BOOLEAN_TYPE)) {
            expr.setType(BTypes.BOOLEAN_TYPE);
        } else {
            throw new SemanticException("Incompatible types used for '&&' operator" + " in " +
                    expr.getLocation().getFileName() + ":" + expr.getLocation().getLine());
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

    private Symbol getSymbol(SymbolName symName, Position sourceLocation) {
        // Check whether this symName is declared
        Symbol symbol = symbolTable.lookup(symName);

        if (symbol == null) {
            throw new SemanticException(sourceLocation.getFileName() + ":" + sourceLocation.getLine() +
                    ": undeclared variable '" + symName.getName() + "'");
        }

        return symbol;
    }

    private String getVarNameFromExpression(Expression expr) {
        if (expr instanceof ArrayMapAccessExpr) {
            return ((ArrayMapAccessExpr) expr).getSymbolName().getName();
        } else if (expr instanceof StructFieldAccessExpr) {
            return ((StructFieldAccessExpr) expr).getSymbolName().getName();
        } else {
            return ((VariableRefExpr) expr).getSymbolName().getName();
        }
    }

    private void checkForConstAssignment(AssignStmt assignStmt, Expression lExpr) {
        if (lExpr instanceof VariableRefExpr &&
                ((VariableRefExpr) lExpr).getMemoryLocation() instanceof ConstantLocation) {
            throw new SemanticException(assignStmt.getLocation().getFileName() + ":"
                    + assignStmt.getLocation().getLine() + ": cannot assign a value to constant '" +
                    ((VariableRefExpr) lExpr).getSymbolName() + "'");
        }
    }

    private void checkForMultiAssignmentErrors(AssignStmt assignStmt, Expression[] lExprs,
                                               CallableUnitInvocationExpr rExpr) {
        BType[] returnTypes = rExpr.getTypes();
        if (lExprs.length != returnTypes.length) {
            throw new SemanticException(assignStmt.getLocation().getFileName() + ":"
                    + assignStmt.getLocation().getLine() + ": assignment count mismatch: " +
                    lExprs.length + " = " + returnTypes.length);
        }

        //cannot assign string to b (type int) in multiple assignment

        for (int i = 0; i < lExprs.length; i++) {
            Expression lExpr = lExprs[i];
            BType returnType = returnTypes[i];
            if (!lExpr.getType().equals(returnType)) {
                String varName = getVarNameFromExpression(lExpr);
                throw new SemanticException(assignStmt.getLocation().getFileName() + ":"
                        + assignStmt.getLocation().getLine() + ": cannot assign " + returnType + " to '" +
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
                throw new SemanticException(assignStmt.getLocation().getFileName() + ":"
                        + assignStmt.getLocation().getLine() + ": '" + varName + "' is repeated " +
                        "on the left side of assignment");
            }

            // First mark all left side ArrayMapAccessExpr. This is to skip some processing which is applicable only
            // for right side expressions.
            if (lExpr instanceof ArrayMapAccessExpr) {
                ((ArrayMapAccessExpr) lExpr).setLHSExpr(true);
            } else  if (lExpr instanceof StructFieldAccessExpr) {
                ((StructFieldAccessExpr) lExpr).setLHSExpr(true);
            }

            lExpr.accept(this);

            // Check whether someone is trying to change the values of a constant
            checkForConstAssignment(assignStmt, lExpr);
        }
    }

//    private void checkForMissingReturnStmt(CallableUnit callableUnit, String errorMsg) {
//        Statement[] stmt = callableUnit.getCallableUnitBody().getStatements();
//        int lastStmtIndex = stmt.length - 1;
//        Statement lastStmt = stmt[lastStmtIndex];
//        if (callableUnit.getReturnParameters().length > 0 &&
//                !(lastStmt instanceof ReturnStmt)) {
//            throw new SemanticException(currentCallableUnit.getLocation().getFileName() + ":" +
//                    currentCallableUnit.getLocation().getLine() +
//                    ": " + errorMsg);
//        }
//    }
//
//    private void checkForMissingReplyStmt(Resource resource) {
//        Statement[] stmt = resource.getCallableUnitBody().getStatements();
//        int lastStmtIndex = stmt.length - 1;
//        Statement lastStmt = stmt[lastStmtIndex];
//        if (resource.getReturnParameters().length > 0 &&
//                !(lastStmt instanceof ReplyStmt)) {
//            throw new SemanticException(currentCallableUnit.getLocation().getFileName() + ":" +
//                    currentCallableUnit.getLocation().getLine() +
//                    ": missing reply statement at end of resource");
//        }
//    }

    private void linkFunction(FunctionInvocationExpr funcIExpr) {

        SymbolName funcName = funcIExpr.getCallableUnitName();
        String pkgPath = getPackagePath(funcName);
        funcName.setPkgName(pkgPath);


        Expression[] exprs = funcIExpr.getArgExprs();
        BType[] paramTypes = new BType[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        SymbolName symbolName = LangModelUtils.getSymNameWithParams(funcName.getName(), pkgPath, paramTypes);
        Symbol symbol = symbolTable.lookup(symbolName);
        if (symbol == null) {
            throw new LinkerException(funcIExpr.getLocation().getFileName() + ":" + funcIExpr.getLocation().getLine() +
                    ": undefined function '" + funcIExpr.getCallableUnitName().getName() + "'");
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
        funcIExpr.setCallableUnit(function);

        // TODO improve this once multiple return types are supported
        funcIExpr.setType((function.getReturnParameters().length != 0) ?
                function.getReturnParameters()[0].getType() : null);
    }

    private void linkAction(ActionInvocationExpr actionIExpr) {
        // Can we do this bit in the linker
        SymbolName actionName = actionIExpr.getCallableUnitName();
        if (actionName.getConnectorName() == null) {
            throw new SemanticException("Connector type is not associated with the action invocation in "
                    + actionIExpr.getLocation().getFileName() + ":" + actionIExpr.getLocation().getLine());
        }

        String pkgPath = getPackagePath(actionName);

        // Set the fully qualified package name
        actionName.setPkgName(pkgPath);

        Expression[] exprs = actionIExpr.getArgExprs();
        BType[] paramTypes = new BType[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        SymbolName symName = LangModelUtils.getActionSymName(actionName.getName(), actionName.getConnectorName(),
                pkgPath, paramTypes);

        Symbol symbol = symbolTable.lookup(symName);
        if (symbol == null) {
            throw new LinkerException("Undefined action: " + actionIExpr.getCallableUnitName().getName() + " in "
                    + actionIExpr.getLocation().getFileName() + ":" + actionIExpr.getLocation().getLine());
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
        actionIExpr.setCallableUnit(action);

        // TODO improve this once multiple return types are supported
        actionIExpr.setType((action.getReturnParameters().length != 0) ?
                action.getReturnParameters()[0].getType() : null);
    }

    private String getLocationStr(Position location) {
        return location.getFileName() + ":" +  location.getLine() + ": ";
    }
    
    /*
     * Struct related methods
     */
    
    /**
     * Visit and semantically analyze a ballerina Struct definition.
     */
    @Override
    public void visit(BallerinaStruct ballerinaStruct) {
        String structName = ballerinaStruct.getName();
        String structStructPackage = ballerinaStruct.getPackageName();
        
        for (VariableDcl field : ballerinaStruct.getFields()) {
            structMemAddrOffset++;
            BType type = field.getType();
            validateType(type, field.getLocation());
            
            SymbolName fieldSym = LangModelUtils.getStructFieldSymName(field.getName().getName(), 
                structName, structStructPackage);
            Symbol symbol = symbolTable.lookup(fieldSym);
            if (symbol != null && isSymbolInCurrentScope(symbol)) {
                throw new SemanticException(getLocationStr(field.getLocation()) + "duplicate field '" + 
                        fieldSym.getName() + "'.");
            }
            MemoryLocation location = new StructVarLocation(structMemAddrOffset);
            symbol = new Symbol(type, currentScopeName(), location);
            symbolTable.insert(fieldSym, symbol);
        }

        ballerinaStruct.setStructMemorySize(structMemAddrOffset + 1);
        structMemAddrOffset = -1;
    }
    
    /**
     * Add the struct to the symbol table.
     * 
     * @param struct    Ballerina struct
     */
    private void addStructSymbol(BallerinaStruct struct) {
        if (symbolTable.lookup(struct.getSymbolName()) != null) {
            throw new SemanticException(getLocationStr(struct.getLocation()) + "duplicate struct '" + struct.getName()
                + "'.");
        }
        Symbol symbol = new Symbol(struct);
        symbolTable.insert(struct.getSymbolName(), symbol);
    }

    /**
     * Visit and analyze allerina Struct declaration expression.
     */
    @Override
    public void visit(StructDcl structDcl) {
        // No need to validate struct instance variable names. It will happen in var declaration phase.

        // Setting the struct name with the package name
        SymbolName structName = structDcl.getStructName();
        String pkgPath = getPackagePath(structName);
        structName = LangModelUtils.getConnectorSymName(structName.getName(), pkgPath);
        structDcl.setStructName(structName);

        Symbol structSymbol = symbolTable.lookup(structName);
        if (structSymbol == null) {
            throw new SemanticException(getLocationStr(structDcl.getLocation()) + "struct '" + structName + 
                    "' not found.");
        }
        structDcl.setStruct(structSymbol.getStruct());
    }

    /**
     * Visit and analyze ballerina Struct initializing expression.
     */
    @Override
    public void visit(StructInitExpr structInitExpr) {
        // Struct type is not known at this stage
        structInitExpr.setType(BTypes.getType(structInitExpr.getStructDcl().getStructName().getName()));
        visit(structInitExpr.getStructDcl());
    }

    /**
     * visit and analyze ballerina struc-field-access-expressions.
     */
    @Override
    public void visit(StructFieldAccessExpr structFieldAccessExpr) {
        // Check whether this access expression is in left hand side of an assignment expression
        // If yes, skip assigning a stack frame offset
        if (!structFieldAccessExpr.isLHSExpr()) {
            visitExpr(structFieldAccessExpr);
        }

        Symbol fieldSymbol = getFieldSymbol(structFieldAccessExpr);
        
        // Set expression type
        BType exprType = fieldSymbol.getType();
        structFieldAccessExpr.setType(exprType);

        /* Get the actual var representation of this field, and semantically analyze. This will check for semantic
         * errors of array/map accesses, used in this struct field.
         * eg: in dpt.employee[2].name , below will check for semantics of 'employee[2]',
         * treating them as individual array/map variables.
         */
        if (structFieldAccessExpr.getVarRef() instanceof ArrayMapAccessExpr) {
            ArrayMapAccessExpr arrayMapAcsExpr = (ArrayMapAccessExpr) structFieldAccessExpr.getVarRef();
            arrayMapAcsExpr.getRExpr().setType(exprType);
            setMemoryLocation(structFieldAccessExpr, fieldSymbol.getLocation());

            // Here we only check for array/map type validation, as symbol validation is already done.
            handleArrayType((ArrayMapAccessExpr) structFieldAccessExpr.getVarRef());
        } else if (structFieldAccessExpr.getVarRef() instanceof VariableRefExpr) {
            VariableRefExpr varRefExpr = (VariableRefExpr) structFieldAccessExpr.getVarRef();
            varRefExpr.setType(exprType);
            setMemoryLocation(structFieldAccessExpr, fieldSymbol.getLocation());
        }
        
        // Go to the referenced field of this struct
        ReferenceExpr fieldExpr = structFieldAccessExpr.getFieldExpr();
        if (fieldExpr != null) {
            fieldExpr.accept(this);
        }
    }
    /**
     * Set the memory location for a expression.
     * 
     * @param expr          Expression to set the memory location
     * @param memLocation   Memory location
     */
    private void setMemoryLocation(Expression expr, MemoryLocation memLocation) {
        // If the expression is an array-map expression, then set the location to the variable-reference-expression 
        // of the array-map-access-expression.
        if (expr instanceof ArrayMapAccessExpr) {
            setMemoryLocation(((ArrayMapAccessExpr) expr).getRExpr(), memLocation);
            return;
        }

        // If the expression is a Struct field access expression, then set the memory location to the variable
        // referenced by the struct-field-access-expression
        if (expr instanceof StructFieldAccessExpr) {
            setMemoryLocation(((StructFieldAccessExpr) expr).getVarRef(), memLocation);
            return;
        }

        // Set the memory location to the variable reference expression
        ((VariableRefExpr) expr).setMemoryLocation(memLocation);
    }
    
    /**
     * Get the symbol of the parent struct to which this field belongs to.
     * 
     * @param expr  Field reference expression
     * @return      Symbol of the parent
     */
    private Symbol getFieldSymbol(StructFieldAccessExpr expr) {
        Symbol fieldSymbol;
        if (expr.getParent() == null) {
            fieldSymbol = symbolTable.lookup(new SymbolName(expr.getSymbolName().getName()));
            // Check for variable existence
            if (fieldSymbol == null) {
                throw new SemanticException(getLocationStr(expr.getLocation()) +
                    "undeclraed struct '" + expr.getSymbolName() + "'.");
            }
            return fieldSymbol;
        }
        // parent is always a StructAttributeAccessExpr
        StructFieldAccessExpr parent = expr.getParent();
        BType parentType = parent.getExpressionType();
        if (parentType instanceof BArrayType) {
            parentType = ((BArrayType) parentType).getElementType();
        }
        SymbolName structFieldSym = LangModelUtils.getStructFieldSymName(expr.getSymbolName().getName(), 
                parentType.toString(), currentPkg);
        
        fieldSymbol = symbolTable.lookup(structFieldSym);
        // Check for field existence
        if (fieldSymbol == null) {
            throw new SemanticException(getLocationStr(expr.getLocation()) +
                "undeclraed field '" + expr.getSymbolName() + "' for type '" + parentType + "'.");
        }
        return fieldSymbol;
    }

    private void linkTypeConverter(TypeCastExpression typeCastExpression, BType sourceType, BType targetType) {
        TypeEdge newEdge = null;
        // First check on this package
        newEdge = packageTypeLattice.getEdgeFromTypes(sourceType, targetType, currentPkg);
        if (newEdge != null) {
            typeCastExpression.setCallableUnit(newEdge.getTypeConvertor());
        } else {
            newEdge = TypeLattice.getExplicitCastLattice().getEdgeFromTypes(sourceType, targetType, currentPkg);
            if (newEdge != null) {
                typeCastExpression.setCallableUnit(newEdge.getTypeConvertor());
            } else {
                newEdge = TypeLattice.getExplicitCastLattice().getEdgeFromTypes(sourceType, targetType, null);
                if (newEdge != null) {
                    typeCastExpression.setCallableUnit(newEdge.getTypeConvertor());
                } else {
                    throw new LinkerException(typeCastExpression.getLocation().getFileName() + ":" +
                            typeCastExpression.getLocation().getLine() +
                            ": type converter cannot be found for '" + sourceType
                            + " to " + targetType + "'");
                }
            }
        }
    }

    private TypeCastExpression checkWideningPossible(BType lhsType, Expression rhsExpr, Operator op) {
        BType rhsType = rhsExpr.getType();
        TypeCastExpression newExpr = null;
        TypeEdge newEdge = null;

        if (((rhsType.equals(BTypes.STRING_TYPE) || lhsType.equals(BTypes.STRING_TYPE)) && op != null
                && op.equals(Operator.ADD)) || (!(rhsType.equals(BTypes.STRING_TYPE)) &&
                !(lhsType.equals(BTypes.STRING_TYPE)) && op != null) || op == null) {
            newEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(rhsType, lhsType, null);
        }

        if (newEdge != null) {
            newExpr = new TypeCastExpression(rhsExpr, lhsType);
            newExpr.setEvalFunc(newEdge.getTypeConvertorFunction());
            newExpr.setLocation(rhsExpr.getLocation());
        }
        return newExpr;
    }

}

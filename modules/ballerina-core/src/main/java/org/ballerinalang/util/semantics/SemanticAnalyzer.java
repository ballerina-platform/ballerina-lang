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
import org.ballerinalang.bre.GlobalVarLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.model.Action;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeDef;
import org.ballerinalang.model.AnnotationAttributeValue;
import org.ballerinalang.model.AnnotationDef;
import org.ballerinalang.model.AttachmentPoint;
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
import org.ballerinalang.model.ExecutableMultiReturnExpr;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.FunctionSymbolName;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.Identifier;
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
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.AbstractExpression;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayLengthExpression;
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
import org.ballerinalang.model.expressions.ReferenceExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
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
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.types.TypeEdge;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.LangModelUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.natives.typemappers.NativeCastMapper;
import org.ballerinalang.natives.typemappers.TypeMappingUtils;
import org.ballerinalang.runtime.worker.WorkerInteractionDataHolder;
import org.ballerinalang.util.codegen.InstructionCodes;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.LinkerException;
import org.ballerinalang.util.exceptions.SemanticErrors;
import org.ballerinalang.util.exceptions.SemanticException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
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
    private Stack<CallableUnit> parentCallableUnit = new Stack<>();
    private Stack<SymbolScope> parentScope = new Stack<>();
    // following pattern matches ${anyString} or ${anyString[int]} or ${anyString["anyString"]}
    private static final String patternString = "\\$\\{((\\w+)(\\[(\\d+|\\\"(\\w+)\\\")\\])?)\\}";
    private static final Pattern compiledPattern = Pattern.compile(patternString);
    private static final String ERRORS_PACKAGE = "ballerina.lang.errors";
    private static final String BALLERINA_CAST_ERROR = "CastError";
    private static final String BALLERINA_ERROR = "Error";

    private int whileStmtCount = 0;
    private int transactionStmtCount = 0;
    private SymbolScope currentScope;
    private SymbolScope nativeScope;

    private BlockStmt.BlockStmtBuilder pkgInitFuncStmtBuilder;

    public SemanticAnalyzer(BLangProgram programScope) {
        currentScope = programScope;
        this.nativeScope = programScope.getNativeScope();
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
        BLangPackage[] dependentPackages = bLangPackage.getDependentPackages();
        List<BallerinaFunction> initFunctionList = new ArrayList<>();
        for (BLangPackage dependentPkg : dependentPackages) {
            if (dependentPkg.isSymbolsDefined()) {
                continue;
            }

            dependentPkg.accept(this);
            initFunctionList.add(dependentPkg.getInitFunction());
        }

        currentScope = bLangPackage;
        currentPkg = bLangPackage.getPackagePath();
        if (packageTypeLattice != null) {
            TypeLattice currentLattice = bLangPackage.getTypeLattice();
            currentLattice.merge(packageTypeLattice, currentPkg);
            packageTypeLattice = currentLattice;
        } else {
            packageTypeLattice = bLangPackage.getTypeLattice();
        }

        // Create package.<init> function
        NodeLocation pkgLocation = bLangPackage.getNodeLocation();
        if (pkgLocation == null) {
            BallerinaFile[] ballerinaFiles = bLangPackage.getBallerinaFiles();

            // TODO filename becomes "" for built-in packages. FIX ME
            String filename = ballerinaFiles.length == 0 ? "" :
                    ballerinaFiles[0].getFileName();
            pkgLocation = new NodeLocation("", filename, 0);
        }

        BallerinaFunction.BallerinaFunctionBuilder functionBuilder =
                new BallerinaFunction.BallerinaFunctionBuilder(bLangPackage);
        functionBuilder.setNodeLocation(pkgLocation);
        functionBuilder.setIdentifier(new Identifier(bLangPackage.getPackagePath() + ".<init>"));
        functionBuilder.setPkgPath(bLangPackage.getPackagePath());
        pkgInitFuncStmtBuilder = new BlockStmt.BlockStmtBuilder(bLangPackage.getNodeLocation(),
                bLangPackage);

        // Invoke <init> methods of all the dependent packages
        addDependentPkgInitCalls(initFunctionList, pkgInitFuncStmtBuilder, pkgLocation);

        // Define package level constructs
        defineStructs(bLangPackage.getStructDefs());
        defineConnectors(bLangPackage.getConnectors());
        resolveStructFieldTypes(bLangPackage.getStructDefs());
        defineFunctions(bLangPackage.getFunctions());
        defineServices(bLangPackage.getServices());
        defineAnnotations(bLangPackage.getAnnotationDefs());

        for (CompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
            compilationUnit.accept(this);
        }

        resolveWorkerInteractions(bLangPackage.getWorkerInteractionDataHolders());
        // Complete the package init function
        ReturnStmt returnStmt = new ReturnStmt(pkgLocation, null, new Expression[0]);
        pkgInitFuncStmtBuilder.addStmt(returnStmt);
        functionBuilder.setBody(pkgInitFuncStmtBuilder.build());
        BallerinaFunction initFunction = functionBuilder.buildFunction();
        initFunction.setReturnParamTypes(new BType[0]);
        bLangPackage.setInitFunction(initFunction);

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

        // Check whether this constant is already defined.
        SymbolName symbolName = new SymbolName(constDef.getName(), currentPkg);
        if (currentScope.resolve(symbolName) != null) {
            BLangExceptionHelper.throwSemanticError(constDef,
                    SemanticErrors.REDECLARED_SYMBOL, constDef.getName());
        }

        // Define the constant in the package scope
        currentScope.define(symbolName, constDef);

        constDef.getRhsExpr().accept(this);

        for (AnnotationAttachment annotationAttachment : constDef.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.CONSTANT);
            annotationAttachment.accept(this);
        }

        // Set memory location
        ConstantLocation memLocation = new ConstantLocation(++staticMemAddrOffset);
        constDef.setMemoryLocation(memLocation);

        // Insert constant initialization stmt to the package init function
        VariableRefExpr varRefExpr = new VariableRefExpr(constDef.getNodeLocation(),
                constDef.getWhiteSpaceDescriptor(), constDef.getName());
        varRefExpr.setVariableDef(constDef);
        AssignStmt assignStmt = new AssignStmt(constDef.getNodeLocation(),
                new Expression[]{varRefExpr}, constDef.getRhsExpr());
        pkgInitFuncStmtBuilder.addStmt(assignStmt);
    }

    @Override
    public void visit(GlobalVariableDef globalVarDef) {
        VariableDefStmt variableDefStmt = globalVarDef.getVariableDefStmt();
        variableDefStmt.accept(this);

        if (variableDefStmt.getRExpr() != null) {
            // Create an assignment statement
            // Insert global variable initialization stmt to the package init function
            AssignStmt assignStmt = new AssignStmt(variableDefStmt.getNodeLocation(),
                    new Expression[]{variableDefStmt.getLExpr()}, variableDefStmt.getRExpr());
            pkgInitFuncStmtBuilder.addStmt(assignStmt);
        }
    }

    @Override
    public void visit(Service service) {
        // Visit the contents within a service
        // Open a new symbol scope
        openScope(service);

        for (AnnotationAttachment annotationAttachment : service.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.SERVICE);
            annotationAttachment.accept(this);
        }

        for (VariableDefStmt variableDefStmt : service.getVariableDefStmts()) {
            variableDefStmt.accept(this);
        }

        createServiceInitFunction(service);

        // Visit the set of resources in a service
        for (Resource resource : service.getResources()) {
            resource.accept(this);
        }

        // Close the symbol scope
        closeScope();
    }

    @Override
    public void visit(BallerinaConnectorDef connectorDef) {
        // Open the connector namespace
        openScope(connectorDef);

        for (AnnotationAttachment annotationAttachment : connectorDef.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.CONNECTOR);
            annotationAttachment.accept(this);
        }

        for (ParameterDef parameterDef : connectorDef.getParameterDefs()) {
            parameterDef.setMemoryLocation(new ConnectorVarLocation(++connectorMemAddrOffset));
            parameterDef.accept(this);
        }

        for (VariableDefStmt variableDefStmt : connectorDef.getVariableDefStmts()) {
            variableDefStmt.accept(this);
        }

        createConnectorInitFunction(connectorDef);

        for (BallerinaAction action : connectorDef.getActions()) {
            action.accept(this);
        }

        int sizeOfConnectorMem = connectorMemAddrOffset + 1;
        connectorDef.setSizeOfConnectorMem(sizeOfConnectorMem);

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

        for (AnnotationAttachment annotationAttachment : resource.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.RESOURCE);
            annotationAttachment.accept(this);
        }

        for (ParameterDef parameterDef : resource.getParameterDefs()) {
            parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            parameterDef.accept(this);
        }

        for (Worker worker : resource.getWorkers()) {
            addWorkerSymbol(worker);
            visit(worker);
        }

        BlockStmt blockStmt = resource.getResourceBody();
        blockStmt.accept(this);
        checkAndAddReplyStmt(blockStmt);

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

        for (AnnotationAttachment annotationAttachment : function.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.FUNCTION);
            annotationAttachment.accept(this);
        }

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
                addWorkerSymbol(worker);
                worker.accept(this);
            }

            BlockStmt blockStmt = function.getCallableUnitBody();
            blockStmt.accept(this);

            if (function.getReturnParameters().length > 0 && !blockStmt.isAlwaysReturns()) {
                BLangExceptionHelper.throwSemanticError(function, SemanticErrors.MISSING_RETURN_STATEMENT);
            }

            checkAndAddReturnStmt(function.getReturnParamTypes().length, blockStmt);
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

        for (AnnotationAttachment annotationAttachment : typeMapper.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.TYPEMAPPER);
            annotationAttachment.accept(this);
        }

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

        for (AnnotationAttachment annotationAttachment : action.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.ACTION);
            annotationAttachment.accept(this);
        }

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
                addWorkerSymbol(worker);
                worker.accept(this);
            }

            BlockStmt blockStmt = action.getCallableUnitBody();
            blockStmt.accept(this);

            if (action.getReturnParameters().length > 0 && !blockStmt.isAlwaysReturns()) {
                BLangExceptionHelper.throwSemanticError(action, SemanticErrors.MISSING_RETURN_STATEMENT);
            }

            checkAndAddReturnStmt(action.getReturnParameters().length, blockStmt);
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
        parentScope.push(currentScope);
        currentScope = worker;
        parentCallableUnit.push(currentCallableUnit);
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

        // Define the worker at symbol scope so that workers defined within this worker can invoke this
        // addWorkerSymbol(worker);

        for (Worker worker2 : worker.getWorkers()) {
            addWorkerSymbol(worker2);
            worker2.accept(this);
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
        currentCallableUnit = parentCallableUnit.pop();
        // Close symbol scope. This is done manually to avoid falling back to package scope
        currentScope = parentScope.pop();
    }

    private void addWorkerSymbol(Worker worker) {
        SymbolName symbolName = worker.getSymbolName();
        BLangSymbol varSymbol = currentScope.resolve(symbolName);
        if (varSymbol != null) {
            BLangExceptionHelper.throwSemanticError(worker,
                    SemanticErrors.REDECLARED_SYMBOL, worker.getName());
        }
        currentScope.define(symbolName, worker);
    }

    @Override
    public void visit(StructDef structDef) {
        for (AnnotationAttachment annotationAttachment : structDef.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.STRUCT);
            annotationAttachment.accept(this);
        }
    }

    @Override
    public void visit(AnnotationAttachment annotation) {
        AttachmentPoint attachedPoint = annotation.getAttachedPoint();
        SymbolName annotationSymName = new SymbolName(annotation.getName(), annotation.getPkgPath());
        BLangSymbol annotationSymbol = currentScope.resolve(annotationSymName);
        
        if (!(annotationSymbol instanceof AnnotationDef)) {
            BLangExceptionHelper.throwSemanticError(annotation, SemanticErrors.UNDEFINED_ANNOTATION, 
                    annotationSymName);
        }
        
        // Validate the attached point
        AnnotationDef annotationDef = (AnnotationDef) annotationSymbol;
        if (annotationDef.getAttachmentPoints() != null && annotationDef.getAttachmentPoints().length > 0) {
            Optional<String> matchingAttachmentPoint = Arrays.stream(annotationDef.getAttachmentPoints())
                    .filter(attachmentPoint -> attachmentPoint.equals(attachedPoint.getValue()))
                    .findAny();
            if (!matchingAttachmentPoint.isPresent()) {
                BLangExceptionHelper.throwSemanticError(annotation, SemanticErrors.ANNOTATION_NOT_ALLOWED,
                        annotationSymName, attachedPoint);
            }
        }
        
        // Validate the attributes and their types
        validateAttributes(annotation, annotationDef);
        
        // Populate default values for annotation attributes
        populateDefaultValues(annotation, annotationDef);
    }

    /**
     * Visit and validate attributes of an annotation attachment.
     * 
     * @param annotation Annotation attachment to validate attributes
     * @param annotationDef Definition of the annotation
     */
    private void validateAttributes(AnnotationAttachment annotation, AnnotationDef annotationDef) {
        annotation.getAttributeNameValuePairs().forEach((attributeName, attributeValue) -> {
            // Check attribute existence
            BLangSymbol attributeSymbol = annotationDef.resolveMembers(new SymbolName(attributeName));
            if (attributeSymbol == null || !(attributeSymbol instanceof AnnotationAttributeDef)) {
                BLangExceptionHelper.throwSemanticError(annotation, SemanticErrors.NO_SUCH_ATTRIBUTE, 
                    attributeName, annotation.getName());
            }
            
            // Check types
            AnnotationAttributeDef attributeDef = ((AnnotationAttributeDef) attributeSymbol);
            SimpleTypeName attributeType = attributeDef.getTypeName();
            SimpleTypeName valueType = attributeValue.getType();
            BLangSymbol valueTypeSymbol = currentScope.resolve(valueType.getSymbolName());
            BLangSymbol attributeTypeSymbol = annotationDef.resolve(new SymbolName(attributeType.getName(), 
                    attributeType.getPackagePath()));
            
            if (attributeType.isArrayType()) {
                if (!valueType.isArrayType()) {
                    BLangExceptionHelper.throwSemanticError(attributeValue, SemanticErrors.INCOMPATIBLE_TYPES, 
                        attributeTypeSymbol.getSymbolName() + TypeConstants.ARRAY_TNAME, 
                        valueTypeSymbol.getSymbolName());
                }
                
                AnnotationAttributeValue[] valuesArray = attributeValue.getValueArray();
                for (AnnotationAttributeValue value : valuesArray) {
                    valueTypeSymbol = currentScope.resolve(value.getType().getSymbolName());
                    if (attributeTypeSymbol != valueTypeSymbol) {
                        BLangExceptionHelper.throwSemanticError(attributeValue, SemanticErrors.INCOMPATIBLE_TYPES, 
                            attributeTypeSymbol.getSymbolName(), valueTypeSymbol.getSymbolName());
                    }
                    
                    // If the value of the attribute is another annotation, then recursively
                    // traverse to its attributes and validate
                    AnnotationAttachment childAnnotation = value.getAnnotationValue();
                    if (childAnnotation != null && valueTypeSymbol instanceof AnnotationDef) {
                        validateAttributes(childAnnotation, (AnnotationDef) valueTypeSymbol);
                    }
                }
            } else {
                if (valueType.isArrayType()) {
                    BLangExceptionHelper.throwSemanticError(attributeValue, 
                        SemanticErrors.INCOMPATIBLE_TYPES_ARRAY_FOUND, attributeTypeSymbol.getName());
                }

                if (attributeTypeSymbol != valueTypeSymbol) {
                    BLangExceptionHelper.throwSemanticError(attributeValue, SemanticErrors.INCOMPATIBLE_TYPES,
                            attributeTypeSymbol.getSymbolName(), valueTypeSymbol.getSymbolName());
                }
                
                // If the value of the attribute is another annotation, then recursively
                // traverse to its attributes and validate
                AnnotationAttachment childAnnotation = attributeValue.getAnnotationValue();
                if (childAnnotation != null && valueTypeSymbol instanceof AnnotationDef) {
                    validateAttributes(childAnnotation, (AnnotationDef) valueTypeSymbol);
                }
            }
        });
    }
    
    /**
     * Populate default values to the annotation attributes.
     * 
     * @param annotation Annotation attachment to populate default values
     * @param annotationDef Definition of the annotation corresponds to the provided annotation attachment
     */
    private void populateDefaultValues(AnnotationAttachment annotation, AnnotationDef annotationDef) {
        Map<String, AnnotationAttributeValue> attributeValPairs = annotation.getAttributeNameValuePairs();
        for (AnnotationAttributeDef attributeDef : annotationDef.getAttributeDefs()) {
            String attributeName = attributeDef.getName();
            
            // if the current attribute is not defined in the annotation attachment, populate it with default value
            if (!attributeValPairs.containsKey(attributeName)) {
                BasicLiteral defaultValue = attributeDef.getAttributeValue();
                if (defaultValue != null) {
                    annotation.addAttributeNameValuePair(attributeName,
                            new AnnotationAttributeValue(defaultValue.getBValue(),
                                    defaultValue.getTypeName(), null, null));
                }
                continue;
            }

            // If the annotation attachment contains the current attribute, and if the value is another 
            // annotationAttachment, then recursively populate its default values
            AnnotationAttributeValue attributeValue = attributeValPairs.get(attributeName);
            SimpleTypeName valueType = attributeValue.getType();
            if (valueType.isArrayType()) {
                AnnotationAttributeValue[] valuesArray = attributeValue.getValueArray();
                for (AnnotationAttributeValue value : valuesArray) {
                    AnnotationAttachment annotationTypeVal = value.getAnnotationValue();

                    // skip if the array element is not an annotation
                    if (annotationTypeVal == null) {
                        continue;
                    }

                    SimpleTypeName attributeType = attributeDef.getTypeName();
                    BLangSymbol attributeTypeSymbol = annotationDef.resolve(
                            new SymbolName(attributeType.getName(), attributeType.getPackagePath()));
                    if (attributeTypeSymbol instanceof AnnotationDef) {
                        populateDefaultValues(annotationTypeVal, (AnnotationDef) attributeTypeSymbol);
                    }
                }
            } else {
                AnnotationAttachment annotationTypeVal = attributeValue.getAnnotationValue();

                // skip if the value is not an annotation
                if (annotationTypeVal == null) {
                    continue;
                }

                BLangSymbol attributeTypeSymbol = annotationDef.resolve(attributeDef.getTypeName().getSymbolName());
                if (attributeTypeSymbol instanceof AnnotationDef) {
                    populateDefaultValues(annotationTypeVal, (AnnotationDef) attributeTypeSymbol);
                }
            }
        }
    }
    
    @Override
    public void visit(AnnotationAttributeDef annotationAttributeDef) {
        SimpleTypeName fieldType = annotationAttributeDef.getTypeName();
        BasicLiteral fieldVal = annotationAttributeDef.getAttributeValue();

        if (fieldVal != null) {
            fieldVal.accept(this);
            BType valueType = fieldVal.getType();

            if (!BTypes.isBuiltInTypeName(fieldType.getName())) {
                BLangExceptionHelper.throwSemanticError(annotationAttributeDef, SemanticErrors.INVALID_DEFAULT_VALUE);
            }

            BLangSymbol typeSymbol = currentScope.resolve(fieldType.getSymbolName());
            BType fieldBType = (BType) typeSymbol;
            if (!BTypes.isValueType(fieldBType)) {
                BLangExceptionHelper.throwSemanticError(annotationAttributeDef, SemanticErrors.INVALID_DEFAULT_VALUE);
            }

            if (fieldBType != valueType) {
                BLangExceptionHelper.throwSemanticError(annotationAttributeDef,
                    SemanticErrors.INVALID_OPERATION_INCOMPATIBLE_TYPES, fieldType, fieldVal.getTypeName());
            }
        } else {
            BLangSymbol typeSymbol;
            if (fieldType.isArrayType()) {
                typeSymbol = currentScope.resolve(new SymbolName(fieldType.getName(), fieldType.getPackagePath()));
            } else {
                typeSymbol = currentScope.resolve(fieldType.getSymbolName());
            }

            // Check whether the field type is a built in value type or an annotation.
            if (((typeSymbol instanceof BType) && !BTypes.isValueType((BType) typeSymbol)) || 
                    (!(typeSymbol instanceof BType) && !(typeSymbol instanceof AnnotationDef))) {
                BLangExceptionHelper.throwSemanticError(annotationAttributeDef, SemanticErrors.INVALID_ATTRIBUTE_TYPE,
                    fieldType);
            }

            if (!(typeSymbol instanceof BType)) {
                fieldType.setPkgPath(annotationAttributeDef.getPackagePath());
            }
        }
    }

    @Override
    public void visit(AnnotationDef annotationDef) {
        for (AnnotationAttributeDef fields : annotationDef.getAttributeDefs()) {
            fields.accept(this);
        }

        for (AnnotationAttachment annotationAttachment : annotationDef.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.ANNOTATION);
            annotationAttachment.accept(this);
        }
    }

    @Override
    public void visit(ParameterDef paramDef) {
        BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
        paramDef.setType(bType);

        if (paramDef.getAnnotations() == null) {
            return;
        }

        for (AnnotationAttachment annotationAttachment : paramDef.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.PARAMETER);
            annotationAttachment.accept(this);
        }
    }

    @Override
    public void visit(VariableDef varDef) {
    }


    // Visit statements

    @Override
    public void visit(VariableDefStmt varDefStmt) {
        // Resolves the type of the variable
        VariableDef varDef = varDefStmt.getVariableDef();
        BType lhsType = BTypes.resolveType(varDef.getTypeName(), currentScope, varDef.getNodeLocation());
        varDef.setType(lhsType);

        // Mark the this variable references as LHS expressions
        ((ReferenceExpr) varDefStmt.getLExpr()).setLHSExpr(true);

        // Check whether this variable is already defined, if not define it.
        SymbolName symbolName = new SymbolName(varDef.getName(), currentPkg);
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
            RefTypeInitExpr refTypeInitExpr = getNestedInitExpr(rExpr, lhsType);
            varDefStmt.setRExpr(refTypeInitExpr);
            refTypeInitExpr.accept(this);
            return;
        }

        BType rhsType;
        if (rExpr instanceof ExecutableMultiReturnExpr) {
            rExpr.accept(this);
            ExecutableMultiReturnExpr multiReturnExpr = (ExecutableMultiReturnExpr) rExpr;
            BType[] returnTypes = multiReturnExpr.getTypes();

            if (returnTypes.length != 1) {
                BLangExceptionHelper.throwSemanticError(varDefStmt, SemanticErrors.ASSIGNMENT_COUNT_MISMATCH,
                        "1", returnTypes.length);
            }

            rhsType = returnTypes[0];
        } else {
            visitSingleValueExpr(rExpr);
            rhsType = rExpr.getType();
        }

        // Check whether the right-hand type can be assigned to the left-hand type.
        AssignabilityResult result = performAssignabilityCheck(lhsType, rExpr);
        if (result.implicitCastExpr != null) {
            varDefStmt.setRExpr(result.implicitCastExpr);
        } else if (!result.assignable) {
            BLangExceptionHelper.throwSemanticError(varDefStmt, SemanticErrors.INCOMPATIBLE_ASSIGNMENT,
                            rhsType, lhsType);
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

        if (lExprs.length > 1 && (rExpr instanceof TypeCastExpression || rExpr instanceof TypeConversionExpr)) {
            ((AbstractExpression) rExpr).setMultiReturnAvailable(true);
            rExpr.accept(this);
            checkForMultiValuedCastingErrors(assignStmt, lExprs, (ExecutableMultiReturnExpr) rExpr);
            return;
        }

        // Now we know that this is a single value assignment statement.
        Expression lExpr = assignStmt.getLExprs()[0];
        BType lhsType = lExpr.getType();

        if (rExpr instanceof RefTypeInitExpr) {
            RefTypeInitExpr refTypeInitExpr = getNestedInitExpr(rExpr, lhsType);
            assignStmt.setRExpr(refTypeInitExpr);
            refTypeInitExpr.accept(this);
            return;
        }

        visitSingleValueExpr(rExpr);
        BType rhsType = rExpr.getType();

        // Check whether the right-hand type can be assigned to the left-hand type.
        AssignabilityResult result = performAssignabilityCheck(lhsType, rExpr);
        if (result.implicitCastExpr != null) {
            assignStmt.setRExpr(result.implicitCastExpr);
        } else if (!result.assignable) {
            BLangExceptionHelper.throwSemanticError(assignStmt, SemanticErrors.INCOMPATIBLE_ASSIGNMENT,
                    rhsType, lhsType);
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

            if (stmt instanceof AbortStmt && transactionStmtCount < 1) {
                BLangExceptionHelper.throwSemanticError(stmt,
                        SemanticErrors.ABORT_STMT_NOT_ALLOWED_HERE);
            }

            if (stmt instanceof BreakStmt || stmt instanceof ReplyStmt || stmt instanceof AbortStmt) {
                checkUnreachableStmt(blockStmt.getStatements(), stmtIndex + 1);
            }

            stmt.accept(this);

            if (stmt.isAlwaysReturns()) {
                checkUnreachableStmt(blockStmt.getStatements(), stmtIndex + 1);
                blockStmt.setAlwaysReturns(true);
            }
        }

        closeScope();
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        boolean stmtReturns = true;
        Expression expr = ifElseStmt.getCondition();
        visitSingleValueExpr(expr);

        if (expr.getType() != BTypes.typeBoolean) {
            BLangExceptionHelper
                    .throwSemanticError(ifElseStmt, SemanticErrors.INCOMPATIBLE_TYPES_BOOLEAN_EXPECTED, expr.getType());
        }

        Statement thenBody = ifElseStmt.getThenBody();
        thenBody.accept(this);

        stmtReturns &= thenBody.isAlwaysReturns();

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            visitSingleValueExpr(elseIfCondition);

            if (elseIfCondition.getType() != BTypes.typeBoolean) {
                BLangExceptionHelper.throwSemanticError(ifElseStmt, SemanticErrors.INCOMPATIBLE_TYPES_BOOLEAN_EXPECTED,
                        elseIfCondition.getType());
            }

            Statement elseIfBody = elseIfBlock.getElseIfBody();
            elseIfBody.accept(this);

            stmtReturns &= elseIfBody.isAlwaysReturns();
        }

        Statement elseBody = ifElseStmt.getElseBody();
        if (elseBody != null) {
            elseBody.accept(this);
            stmtReturns &= elseBody.isAlwaysReturns();
        } else {
            stmtReturns = false;
        }

        ifElseStmt.setAlwaysReturns(stmtReturns);
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

        BLangSymbol error = currentScope.resolve(new SymbolName(BALLERINA_ERROR, ERRORS_PACKAGE));
        Set<BType> definedTypes = new HashSet<>();
        if (tryCatchStmt.getCatchBlocks().length != 0) {
            // Assumption : To use CatchClause, ballerina.lang.errors should be resolved before.
            if (error == null || !(error instanceof StructDef)) {
                BLangExceptionHelper.throwSemanticError(tryCatchStmt,
                        SemanticErrors.CANNOT_RESOLVE_STRUCT, ERRORS_PACKAGE, BALLERINA_ERROR);
            }
        }
        for (TryCatchStmt.CatchBlock catchBlock : tryCatchStmt.getCatchBlocks()) {
            catchBlock.getParameterDef().setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            catchBlock.getParameterDef().accept(this);
            // Validation for error type.
            if (!error.equals(catchBlock.getParameterDef().getType()) &&
                    (!(catchBlock.getParameterDef().getType() instanceof StructDef) ||
                            TypeLattice.getExplicitCastLattice().getEdgeFromTypes(catchBlock.getParameterDef()
                                    .getType(), error, null) == null)) {
                throw new SemanticException(BLangExceptionHelper.constructSemanticError(
                        catchBlock.getCatchBlockStmt().getNodeLocation(),
                        SemanticErrors.ONLY_ERROR_TYPE_ALLOWED_HERE));
            }
            // Validation for duplicate catch.
            if (!definedTypes.add(catchBlock.getParameterDef().getType())) {
                throw new SemanticException(BLangExceptionHelper.constructSemanticError(
                        catchBlock.getCatchBlockStmt().getNodeLocation(),
                        SemanticErrors.DUPLICATED_ERROR_CATCH, catchBlock.getParameterDef().getTypeName()));
            }
            catchBlock.getCatchBlockStmt().accept(this);
        }

        if (tryCatchStmt.getFinallyBlock() != null) {
            tryCatchStmt.getFinallyBlock().getFinallyBlockStmt().accept(this);
        }
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        throwStmt.getExpr().accept(this);
        BType expressionType = null;
        if (throwStmt.getExpr() instanceof VariableRefExpr && throwStmt.getExpr().getType() instanceof StructDef) {
            expressionType = throwStmt.getExpr().getType();
        } else if (throwStmt.getExpr() instanceof FunctionInvocationExpr) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) throwStmt.getExpr();
            if (!funcIExpr.isMultiReturnExpr() && funcIExpr.getTypes().length == 1 && funcIExpr.getTypes()[0]
                    instanceof StructDef) {
                expressionType = funcIExpr.getTypes()[0];
            }
        }
        if (expressionType != null) {
            BLangSymbol error = currentScope.resolve(new SymbolName(BALLERINA_ERROR, ERRORS_PACKAGE));
            // TODO : Fix this.
            // Assumption : To use CatchClause, ballerina.lang.errors should be resolved before.
            if (error == null) {
                BLangExceptionHelper.throwSemanticError(throwStmt,
                        SemanticErrors.CANNOT_RESOLVE_STRUCT, ERRORS_PACKAGE, BALLERINA_ERROR);
            }
            if (error.equals(expressionType) || TypeLattice.getExplicitCastLattice().getEdgeFromTypes
                    (expressionType, error, null) != null) {
                throwStmt.setAlwaysReturns(true);
                return;
            }
        }
        throw new SemanticException(BLangExceptionHelper.constructSemanticError(
                throwStmt.getNodeLocation(), SemanticErrors.ONLY_ERROR_TYPE_ALLOWED_HERE));
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
        Expression[] expressions = workerInvocationStmt.getExpressionList();
        for (Expression expression : expressions) {
            expression.accept(this);
        }

        if (!workerInvocationStmt.getCallableUnitName().equals("default")) {
            linkWorker(workerInvocationStmt);

            //Find the return types of this function invocation expression.
            ParameterDef[] returnParams = workerInvocationStmt.getCallableUnit().getReturnParameters();
            BType[] returnTypes = new BType[returnParams.length];
            for (int i = 0; i < returnParams.length; i++) {
                returnTypes[i] = returnParams[i].getType();
            }
            workerInvocationStmt.setTypes(returnTypes);
        }
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        String workerName = workerReplyStmt.getWorkerName();
        SymbolName workerSymbol = new SymbolName(workerName);

        Expression[] expressions = workerReplyStmt.getExpressionList();
        for (Expression expression : expressions) {
            expression.accept(this);
        }

        if (!workerName.equals("default")) {
            BLangSymbol worker = currentScope.resolve(workerSymbol);
            if (!(worker instanceof Worker)) {
                BLangExceptionHelper.throwSemanticError(expressions[0], SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND,
                        workerSymbol);
            }

            workerReplyStmt.setWorker((Worker) worker);
        }
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        boolean stmtReturns = true;
        //open the fork join statement scope
        openScope(forkJoinStmt);

        // Visit workers
        for (Worker worker : forkJoinStmt.getWorkers()) {
            /* Here we are setting the current stack frame size of the parent component (function, resource, action)
            as the accessible stack frame size for fork-join internal workers. */
            worker.setAccessibleStackFrameSize(stackFrameOffset + 1);

            /* Actual worker memory segment starts after the current in-scope variables within the control stack.
            Hence, we are adding the stackFrameOffset + 1 to begin with */
            workerMemAddrOffset += stackFrameOffset + 1;

            worker.accept(this);
        }

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
        stmtReturns &= joinBody.isAlwaysReturns();
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
        stmtReturns &= timeoutBody.isAlwaysReturns();
        closeScope();

        forkJoinStmt.setAlwaysReturns(stmtReturns);

        //closing the fork join statement scope
        closeScope();

    }

    @Override
    public void visit(TransactionRollbackStmt transactionRollbackStmt) {
        transactionStmtCount++;
        transactionRollbackStmt.getTransactionBlock().accept(this);
        transactionRollbackStmt.getRollbackBlock().getRollbackBlockStmt().accept(this);
        transactionStmtCount--;
    }

    @Override
    public void visit(AbortStmt abortStmt) {

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
        if (replyExpr != null) {
            visitSingleValueExpr(replyExpr);
            // reply statement supports only message type
            if (replyExpr.getType() != BTypes.typeMessage) {
                BLangExceptionHelper.throwSemanticError(replyExpr, SemanticErrors.INCOMPATIBLE_TYPES,
                        BTypes.typeMessage, replyExpr.getType());
            }
        }
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        if (currentCallableUnit instanceof Resource) {
            BLangExceptionHelper.throwSemanticError(returnStmt, SemanticErrors.RETURN_CANNOT_USED_IN_RESOURCE);
        }

        if (transactionStmtCount > 0) {
            BLangExceptionHelper.throwSemanticError(returnStmt, SemanticErrors.RETURN_CANNOT_USED_IN_TRANSACTION);
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
                        returnStmt.getWhiteSpaceDescriptor(), returnParamsOfCU[i].getSymbolName());
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
                BType lhsType = returnParamsOfCU[i].getType();
                BType rhsType = funcIExprReturnTypes[i];

                // Check whether the right-hand type can be assigned to the left-hand type.
                if (isAssignableTo(lhsType, rhsType)) {
                    continue;
                }

                // TODO Check whether an implicit cast is possible
                // This requires a tree rewrite. Off the top of my head the results of function or action invocation
                // should be stored in temporary variables with matching types. Then these temporary variables can be
                // assigned to left-hand side expressions one by one.

                BLangExceptionHelper.throwSemanticError(returnStmt,
                        SemanticErrors.CANNOT_USE_TYPE_IN_RETURN_STATEMENT, lhsType, rhsType);
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
                // Except for the first argument in return statement, check for FunctionInvocationExprs which return
                // multiple values.
                if (returnArgExprs[i] instanceof FunctionInvocationExpr) {
                    FunctionInvocationExpr funcIExpr = ((FunctionInvocationExpr) returnArgExprs[i]);
                    if (funcIExpr.getTypes().length > 1) {
                        BLangExceptionHelper.throwSemanticError(returnStmt,
                                SemanticErrors.MULTIPLE_VALUE_IN_SINGLE_VALUE_CONTEXT,
                                funcIExpr.getCallableUnit().getName());
                    }
                }

                BType lhsType = returnParamsOfCU[i].getType();
                BType rhsType = typesOfReturnExprs[i];

                // Check type assignability
                AssignabilityResult result = performAssignabilityCheck(lhsType, returnArgExprs[i]);
                if (result.implicitCastExpr != null) {
                    returnArgExprs[i] = result.implicitCastExpr;
                } else if (!result.assignable) {
                    BLangExceptionHelper.throwSemanticError(returnStmt,
                            SemanticErrors.CANNOT_USE_TYPE_IN_RETURN_STATEMENT, lhsType, rhsType);
                }
            }
        }
    }

    @Override
    public void visit(TransformStmt transformStmt) {
        BlockStmt blockStmt = transformStmt.getBody();
        if (blockStmt.getStatements().length == 0) {
            BLangExceptionHelper.throwSemanticError(transformStmt, SemanticErrors.TRANSFORM_STATEMENT_NO_BODY);
        }
        blockStmt.accept(this);
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
            } else if (unaryExpr.getType() == BTypes.typeFloat) {
                unaryExpr.setEvalFunc(UnaryExpression.NEGATIVE_FLOAT_FUNC);
            } else {
                throwInvalidUnaryOpError(unaryExpr);
            }
        } else if (Operator.ADD.equals(unaryExpr.getOperator())) {
            if (unaryExpr.getType() == BTypes.typeInt) {
                unaryExpr.setEvalFunc(UnaryExpression.POSITIVE_INT_FUNC);
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
        BType compareExprType = verifyBinaryEqualityExprType(equalExpr);

        if (compareExprType == BTypes.typeInt) {
            equalExpr.setEvalFunc(EqualExpression.EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.typeFloat) {
            equalExpr.setEvalFunc(EqualExpression.EQUAL_FLOAT_FUNC);

        } else if (compareExprType == BTypes.typeBoolean) {
            equalExpr.setEvalFunc(EqualExpression.EQUAL_BOOLEAN_FUNC);

        } else if (compareExprType == BTypes.typeString) {
            equalExpr.setEvalFunc(EqualExpression.EQUAL_STRING_FUNC);

        } else if (compareExprType == BTypes.typeNull) {
            equalExpr.setRefTypeEvalFunc(EqualExpression.EQUAL_NULL_FUNC);

        } else {
            throwInvalidBinaryOpError(equalExpr);
        }
    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {
        BType compareExprType = verifyBinaryEqualityExprType(notEqualExpr);

        if (compareExprType == BTypes.typeInt) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_INT_FUNC);

        } else if (compareExprType == BTypes.typeFloat) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_FLOAT_FUNC);

        } else if (compareExprType == BTypes.typeBoolean) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_BOOLEAN_FUNC);

        } else if (compareExprType == BTypes.typeString) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_STRING_FUNC);

        } else if (compareExprType == BTypes.typeNull) {
            notEqualExpr.setRefTypeEvalFunc(NotEqualExpression.NOT_EQUAL_NULL_FUNC);

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
    public void visit(FieldAccessExpr fieldAccessExpr) {
        visitField(fieldAccessExpr, currentScope);
    }

    @Override
    public void visit(JSONFieldAccessExpr jsonFieldExpr) {

    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        visitMapJsonInitExpr(refTypeInitExpr);
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        visitMapJsonInitExpr(mapInitExpr);
    }

    @Override
    public void visit(JSONInitExpr jsonInitExpr) {
        visitMapJsonInitExpr(jsonInitExpr);
    }

    @Override
    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {
        BType inheritedType = jsonArrayInitExpr.getInheritedType();
        jsonArrayInitExpr.setType(inheritedType);

        Expression[] argExprs = jsonArrayInitExpr.getArgExprs();

        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            if (argExpr instanceof RefTypeInitExpr) {
                argExpr = getNestedInitExpr(argExpr, inheritedType);
                argExprs[i] = argExpr;
            }
            visitSingleValueExpr(argExpr);

            // check the type compatibility of the value.
            BType argExprType = argExpr.getType();
            if (BTypes.isValueType(argExprType)) {
                TypeCastExpression typeCastExpr = checkWideningPossible(BTypes.typeJSON, argExpr);
                if (typeCastExpr != null) {
                    argExprs[i] = typeCastExpr;
                } else {
                    BLangExceptionHelper.throwSemanticError(argExpr,
                            SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT, argExprType.getSymbolName(),
                            inheritedType.getSymbolName());
                }
                continue;
            }

            if (argExprType != BTypes.typeNull && isAssignableTo(BTypes.typeJSON, argExprType)) {
                continue;
            }

            TypeCastExpression typeCastExpr = checkWideningPossible(BTypes.typeJSON, argExpr);
            if (typeCastExpr == null) {
                BLangExceptionHelper.throwSemanticError(jsonArrayInitExpr,
                        SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT, argExpr.getType(), BTypes.typeJSON);
            }
            argExprs[i] = typeCastExpr;
        }
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        BType inheritedType = connectorInitExpr.getInheritedType();
        if (!(inheritedType instanceof BallerinaConnectorDef)) {
            BLangExceptionHelper.throwSemanticError(connectorInitExpr, SemanticErrors.CONNECTOR_INIT_NOT_ALLOWED);
        }
        connectorInitExpr.setType(inheritedType);

        for (Expression argExpr : connectorInitExpr.getArgExprs()) {
            visitSingleValueExpr(argExpr);
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
        if (!(arrayInitExpr.getInheritedType() instanceof BArrayType)) {
            BLangExceptionHelper.throwSemanticError(arrayInitExpr, SemanticErrors.ARRAY_INIT_NOT_ALLOWED_HERE);
        }

        visitArrayInitExpr(arrayInitExpr);
    }

    private void visitArrayInitExpr(ArrayInitExpr arrayInitExpr) {
        BType inheritedType = arrayInitExpr.getInheritedType();
        arrayInitExpr.setType(inheritedType);
        Expression[] argExprs = arrayInitExpr.getArgExprs();
        if (argExprs.length == 0) {
            return;
        }

        BType expectedElementType = ((BArrayType) inheritedType).getElementType();
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            if (argExpr instanceof RefTypeInitExpr) {
                ((RefTypeInitExpr) argExpr).setInheritedType(expectedElementType);
                argExpr = getNestedInitExpr(argExpr, expectedElementType);
                argExprs[i] = argExpr;
            }

            visitSingleValueExpr(argExpr);
            AssignabilityResult result = performAssignabilityCheck(expectedElementType, argExpr);
            if (result.implicitCastExpr != null) {
                argExprs[i] = result.implicitCastExpr;
            } else if (!result.assignable) {
                BLangExceptionHelper.throwSemanticError(argExpr, SemanticErrors.INCOMPATIBLE_ASSIGNMENT,
                        argExpr.getType(), expectedElementType);
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
            KeyValueExpr keyValueExpr = (KeyValueExpr) argExpr;
            Expression keyExpr = keyValueExpr.getKeyExpr();
            if (!(keyExpr instanceof VariableRefExpr)) {
                BLangExceptionHelper.throwSemanticError(keyExpr, SemanticErrors.INVALID_FIELD_NAME_STRUCT_INIT);
            }

            VariableRefExpr varRefExpr = (VariableRefExpr) keyExpr;
            //TODO fix properly package conflict
            BLangSymbol varDefSymbol = structDef.resolveMembers(new SymbolName(varRefExpr.getSymbolName().getName(),
                    structDef.getPackagePath()));

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

            BType structFieldType = varDef.getType();
            if (valueExpr instanceof RefTypeInitExpr) {
                valueExpr = getNestedInitExpr(valueExpr, structFieldType);
                keyValueExpr.setValueExpr(valueExpr);
            }

            valueExpr.accept(this);

            if (structFieldType == BTypes.typeAny) {
                AssignabilityResult result = performAssignabilityCheck(structFieldType, valueExpr);
                if (result.implicitCastExpr != null) {
                    valueExpr = result.implicitCastExpr;
                    keyValueExpr.setValueExpr(valueExpr);
                }
            }

            if (!TypeMappingUtils.isCompatible(structFieldType, valueExpr.getType())) {
                BLangExceptionHelper.throwSemanticError(keyExpr, SemanticErrors.INCOMPATIBLE_TYPES,
                        varDef.getType(), valueExpr.getType());
            }
        }
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        // In this case, type of the backtickExpr should be xml
        BType inheritedType = backtickExpr.getInheritedType();
        if (inheritedType != BTypes.typeXML) {
            BLangExceptionHelper.throwSemanticError(backtickExpr, SemanticErrors.INCOMPATIBLE_TYPES_EXPECTED_XML);
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
                    backtickExpr.getWhiteSpaceDescriptor(),
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
                    indexExpr = new BasicLiteral(backtickExpr.getNodeLocation(), backtickExpr.getWhiteSpaceDescriptor(),
                            new SimpleTypeName(TypeConstants.STRING_TNAME), new BString(m.group(5)));
                    indexExpr.setType(BTypes.typeString);
                } else {
                    indexExpr = new BasicLiteral(backtickExpr.getNodeLocation(), backtickExpr.getWhiteSpaceDescriptor(),
                            new SimpleTypeName(TypeConstants.INT_TNAME), new BInteger(Integer.parseInt(m.group(4))));
                    indexExpr.setType(BTypes.typeInt);
                }

                SymbolName mapOrArrName = new SymbolName(m.group(2), currentPkg);

                ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder =
                        new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();

                VariableRefExpr arrayMapVarRefExpr = new VariableRefExpr(backtickExpr.getNodeLocation(),
                        backtickExpr.getWhiteSpaceDescriptor(), mapOrArrName);
                visit(arrayMapVarRefExpr);

                builder.setArrayMapVarRefExpr(arrayMapVarRefExpr);
                builder.setSymbolName(mapOrArrName);
                Expression[] exprs = {indexExpr};
                builder.setIndexExprs(exprs);
                ArrayMapAccessExpr arrayMapAccessExpr = builder.buildWithSymbol();
                visit(arrayMapAccessExpr);
                argExprList.add(arrayMapAccessExpr);
            } else {
                VariableRefExpr variableRefExpr = new VariableRefExpr(backtickExpr.getNodeLocation(),
                        backtickExpr.getWhiteSpaceDescriptor(), new SymbolName(m.group(1), currentPkg));
                visit(variableRefExpr);
                argExprList.add(variableRefExpr);
            }
            if (literals.length > i) {
                BasicLiteral basicLiteral = new BasicLiteral(backtickExpr.getNodeLocation(),
                        backtickExpr.getWhiteSpaceDescriptor(),
                        new SimpleTypeName(TypeConstants.STRING_TNAME), new BString(literals[i]));
                visit(basicLiteral);
                argExprList.add(basicLiteral);
                i++;
            }
        }

        backtickExpr.setArgsExprs(argExprList.toArray(new Expression[argExprList.size()]));
    }

    @Override
    public void visit(KeyValueExpr keyValueExpr) {

    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        SymbolName symbolName = variableRefExpr.getSymbolName();

        // Check whether this symName is declared
        BLangSymbol varDefSymbol = currentScope.resolve(symbolName);

        if (varDefSymbol == null) {
            BLangExceptionHelper.throwSemanticError(variableRefExpr, SemanticErrors.UNDEFINED_SYMBOL,
                    symbolName);
        }

        if (!(varDefSymbol instanceof VariableDef)) {
            BLangExceptionHelper.throwSemanticError(variableRefExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND,
                    symbolName);
        }

        variableRefExpr.setVariableDef((VariableDef) varDefSymbol);
    }

    @Override
    public void visit(TypeCastExpression typeCastExpr) {
        // Evaluate the expression and set the type
        Expression rExpr = typeCastExpr.getRExpr();
        visitSingleValueExpr(rExpr);
        BType sourceType = rExpr.getType();
        BType targetType = typeCastExpr.getType();
        if (targetType == null) {
            targetType = BTypes.resolveType(typeCastExpr.getTypeName(), currentScope, typeCastExpr.getNodeLocation());
            typeCastExpr.setType(targetType);
        }

        // casting a null literal is not supported.
        if (rExpr instanceof NullLiteral) {
            BLangExceptionHelper.throwSemanticError(typeCastExpr, SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CAST,
                sourceType, targetType);
        }

        boolean isMultiReturn = typeCastExpr.isMultiReturnExpr();

        // Find the eval function from explicit casting lattice
        TypeEdge newEdge = TypeLattice.getExplicitCastLattice().getEdgeFromTypes(sourceType, targetType, null);
        if (newEdge != null) {
            typeCastExpr.setOpcode(newEdge.getOpcode());
            typeCastExpr.setEvalFunc(newEdge.getTypeMapperFunction());

            if (!isMultiReturn) {
                typeCastExpr.setTypes(new BType[] { targetType });
                return;
            }

        } else {
            // TODO Remove the this else if block once the old interpreter is removed
            if (sourceType instanceof StructDef && targetType instanceof StructDef) {
                typeCastExpr.setEvalFunc(NativeCastMapper.STRUCT_TO_STRUCT_UNSAFE_FUNC);
            }

            boolean isUnsafeCastPossible = false;
            if (isMultiReturn) {
                isUnsafeCastPossible = checkUnsafeCastPossible(sourceType, targetType);
            }

            if (isUnsafeCastPossible) {
                typeCastExpr.setOpcode(InstructionCodes.CHECKCAST);
            } else {
                // TODO: print a suggestion
                BLangExceptionHelper.throwSemanticError(typeCastExpr, SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CAST,
                        sourceType, targetType);
            }
        }

        // If this is a multi-value return conversion expression, set the return types. 
        BLangSymbol error = currentScope.resolve(new SymbolName(BALLERINA_CAST_ERROR, ERRORS_PACKAGE));
        if (error == null || !(error instanceof StructDef)) {
            BLangExceptionHelper.throwSemanticError(typeCastExpr,
                SemanticErrors.CANNOT_RESOLVE_STRUCT, ERRORS_PACKAGE, BALLERINA_CAST_ERROR);
        }
        typeCastExpr.setTypes(new BType[] { targetType, (BType) error });
    }


    @Override
    public void visit(TypeConversionExpr typeConversionExpr) {
        // Evaluate the expression and set the type
        Expression rExpr = typeConversionExpr.getRExpr();
        visitSingleValueExpr(rExpr);
        BType sourceType = rExpr.getType();
        BType targetType = typeConversionExpr.getType();
        if (targetType == null) {
            targetType = BTypes.resolveType(typeConversionExpr.getTypeName(), currentScope, null);
            typeConversionExpr.setType(targetType);
        }

        // casting a null literal is not supported.
        if (rExpr instanceof NullLiteral) {
            BLangExceptionHelper.throwSemanticError(typeConversionExpr,
                    SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT, sourceType, targetType);
        }

        boolean isMultiReturn = typeConversionExpr.isMultiReturnExpr();

        // Find the eval function from the conversion lattice
        TypeEdge newEdge = TypeLattice.getTransformLattice().getEdgeFromTypes(sourceType, targetType, null);
        if (newEdge != null) {
            typeConversionExpr.setOpcode(newEdge.getOpcode());
            typeConversionExpr.setEvalFunc(newEdge.getTypeMapperFunction());

            if (!isMultiReturn) {
                typeConversionExpr.setTypes(new BType[] { targetType });
                return;
            }

        } else {
            // TODO: print a suggestion
            BLangExceptionHelper.throwSemanticError(typeConversionExpr,
                    SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT, sourceType, targetType);
        }

        if (!isMultiReturn) {
            return;
        }

        // If this is a multi-value return conversion expression, set the return types. 
        BLangSymbol error = currentScope.resolve(new SymbolName(BALLERINA_CAST_ERROR, ERRORS_PACKAGE));
        if (error == null || !(error instanceof StructDef)) {
            BLangExceptionHelper.throwSemanticError(typeConversionExpr,
                SemanticErrors.CANNOT_RESOLVE_STRUCT, ERRORS_PACKAGE, BALLERINA_CAST_ERROR);
        }
        typeConversionExpr.setTypes(new BType[] { targetType, (BType) error });
    }

    @Override
    public void visit(NullLiteral nullLiteral) {
        nullLiteral.setType(BTypes.typeNull);
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
        ReferenceExpr arrayMapVarRefExpr = (ReferenceExpr) arrayMapAccessExpr.getRExpr();

        // Handle the arrays type
        if (arrayMapVarRefExpr.getType() instanceof BArrayType) {
            // Check the type of the index expression
            for (Expression indexExpr : arrayMapAccessExpr.getIndexExprs()) {
                visitSingleValueExpr(indexExpr);
                if (indexExpr.getType() != BTypes.typeInt) {
                    BLangExceptionHelper.throwSemanticError(arrayMapAccessExpr, SemanticErrors.NON_INTEGER_ARRAY_INDEX,
                            indexExpr.getType());
                }
            }
            // Set type of the arrays access expression
            BType expectedType =  arrayMapVarRefExpr.getType();
            for (int i = 0; i < arrayMapAccessExpr.getIndexExprs().length; i++) {
                expectedType = ((BArrayType) expectedType).getElementType();
            }
            arrayMapAccessExpr.setType(expectedType);

        } else if (arrayMapVarRefExpr.getType() instanceof BMapType) {
            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExprs()[0];
            visitSingleValueExpr(indexExpr);
            if (indexExpr.getType() != BTypes.typeString) {
                BLangExceptionHelper.throwSemanticError(arrayMapAccessExpr, SemanticErrors.NON_STRING_MAP_INDEX,
                        indexExpr.getType());
            }
            // Set type of the map access expression
            BMapType typeOfMap = (BMapType) arrayMapVarRefExpr.getType();
            arrayMapAccessExpr.setType(typeOfMap.getElementType());

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
        visitBinaryExpr(binaryArithmeticExpr);
        BType type = verifyBinaryExprType(binaryArithmeticExpr);
        binaryArithmeticExpr.setType(type);
        return type;
    }

    private BType verifyBinaryCompareExprType(BinaryExpression binaryExpression) {
        visitBinaryExpr(binaryExpression);
        BType type = verifyBinaryExprType(binaryExpression);
        binaryExpression.setType(BTypes.typeBoolean);
        return type;
    }

    private BType verifyBinaryEqualityExprType(BinaryExpression binaryExpression) {
        visitBinaryExpr(binaryExpression);
        BType rType = binaryExpression.getRExpr().getType();
        BType lType = binaryExpression.getLExpr().getType();
        BType type;

        if (rType == BTypes.typeNull) {
            if (BTypes.isValueType(lType)) {
                BLangExceptionHelper.throwSemanticError(binaryExpression, 
                    SemanticErrors.INVALID_OPERATION_INCOMPATIBLE_TYPES, lType, rType);
            }
            type = rType;
        } else if (lType == BTypes.typeNull) {
            if (BTypes.isValueType(rType)) {
                BLangExceptionHelper.throwSemanticError(binaryExpression, 
                    SemanticErrors.INVALID_OPERATION_INCOMPATIBLE_TYPES, lType, rType);
            }
            type = lType;
        } else {
            type = verifyBinaryExprType(binaryExpression);
        }

        binaryExpression.setType(BTypes.typeBoolean);
        return type;
    }

    private BType verifyBinaryExprType(BinaryExpression binaryExpr) {
        Expression rExpr = binaryExpr.getRExpr();
        Expression lExpr = binaryExpr.getLExpr();
        BType rType = rExpr.getType();
        BType lType = lExpr.getType();

        if (!(rType.equals(lType))) {
            TypeCastExpression newExpr;
            TypeEdge newEdge;

            if (((rType.equals(BTypes.typeString) || lType.equals(BTypes.typeString))
                    && binaryExpr.getOperator().equals(Operator.ADD)) || (!(rType.equals(BTypes.typeString)) &&
                    !(lType.equals(BTypes.typeString)))) {
                newEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(rType, lType, null);
                if (newEdge != null) { // Implicit cast from right to left
                    newExpr = new TypeCastExpression(rExpr.getNodeLocation(), rExpr.getWhiteSpaceDescriptor(),
                                                rExpr, lType);
                    newExpr.setOpcode(newEdge.getOpcode());
                    newExpr.setEvalFunc(newEdge.getTypeMapperFunction());
                    newExpr.accept(this);
                    binaryExpr.setRExpr(newExpr);
                    return lType;
                } else {
                    newEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(lType, rType, null);
                    if (newEdge != null) { // Implicit cast from left to right
                        newExpr = new TypeCastExpression(lExpr.getNodeLocation(), lExpr.getWhiteSpaceDescriptor(),
                                lExpr, rType);
                        newExpr.setOpcode(newEdge.getOpcode());
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
        } else if (expr instanceof FieldAccessExpr) {
            return getVarNameFromExpression(((FieldAccessExpr) expr).getVarRef());
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
            String varName = getVarNameFromExpression(lExpr);
            if ("_".equals(varName)) {
                continue;
            }

            BType lhsType = lExprs[i].getType();
            BType rhsType = returnTypes[i];

            // Check whether the right-hand type can be assigned to the left-hand type.
            if (isAssignableTo(lhsType, rhsType)) {
                continue;
            }

            // TODO Check whether an implicit cast is possible
            // This requires a tree rewrite. Off the top of my head the results of function or action invocation
            // should be stored in temporary variables with matching types. Then these temporary variables can be
            // assigned to left-hand side expressions one by one.

            BLangExceptionHelper.throwSemanticError(assignStmt,
                    SemanticErrors.CANNOT_ASSIGN_IN_MULTIPLE_ASSIGNMENT, rhsType, varName, lExpr.getType());
        }
    }

    private void checkForMultiValuedCastingErrors(AssignStmt assignStmt, Expression[] lExprs,
            ExecutableMultiReturnExpr rExpr) {
        BType[] returnTypes = rExpr.getTypes();
        if (lExprs.length != returnTypes.length) {
            BLangExceptionHelper.throwSemanticError(assignStmt, SemanticErrors.ASSIGNMENT_COUNT_MISMATCH, 
                    lExprs.length, returnTypes.length);
        }

        for (int i = 0; i < lExprs.length; i++) {
            Expression lExpr = lExprs[i];
            BType returnType = returnTypes[i];
            String varName = getVarNameFromExpression(lExpr);
            if ("_".equals(varName)) {
                continue;
            }
            if ((lExpr.getType() != BTypes.typeAny) && (!lExpr.getType().equals(returnType))) {
                BLangExceptionHelper.throwSemanticError(assignStmt, 
                    SemanticErrors.INCOMPATIBLE_TYPES_IN_MULTIPLE_ASSIGNMENT, varName, returnType, lExpr.getType());
            }
        }
    }

    private void visitLExprsOfAssignment(AssignStmt assignStmt, Expression[] lExprs) {
        // This set data structure is used to check for repeated variable names in the assignment statement
        Set<String> varNameSet = new HashSet<>();

        int ignoredCount = 0;
        for (Expression lExpr : lExprs) {
            String varName = getVarNameFromExpression(lExpr);
            if (varName.equals("_")) {
                ignoredCount++;
                continue;
            }
            if (!varNameSet.add(varName)) {
                BLangExceptionHelper.throwSemanticError(assignStmt,
                        SemanticErrors.VAR_IS_REPEATED_ON_LEFT_SIDE_ASSIGNMENT, varName);
            }

            // First mark all left side ArrayMapAccessExpr. This is to skip some processing which is applicable only
            // for right side expressions.
            ((ReferenceExpr) lExpr).setLHSExpr(true);
            if (lExpr instanceof ArrayMapAccessExpr) {
                ((ArrayMapAccessExpr) lExpr).setLHSExpr(true);
            } else if (lExpr instanceof FieldAccessExpr) {
                ((FieldAccessExpr) lExpr).setLHSExpr(true);
            }

            lExpr.accept(this);

            // Check whether someone is trying to change the values of a constant
            checkForConstAssignment(assignStmt, lExpr);
        }
        if (ignoredCount == lExprs.length) {
            throw new SemanticException(BLangExceptionHelper.constructSemanticError(
                    assignStmt.getNodeLocation(), SemanticErrors.IGNORED_ASSIGNMENT));
        }
    }

    private void linkFunction(FunctionInvocationExpr funcIExpr) {
        String pkgPath = funcIExpr.getPackagePath();

        Expression[] exprs = funcIExpr.getArgExprs();
        BType[] paramTypes = new BType[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        FunctionSymbolName symbolName = LangModelUtils.getFuncSymNameWithParams(funcIExpr.getName(),
                                                                                pkgPath, paramTypes);
        BLangSymbol functionSymbol = currentScope.resolve(symbolName);

        functionSymbol = matchAndUpdateFunctionArguments(funcIExpr, symbolName, functionSymbol);

        if (functionSymbol == null) {
            String funcName = (funcIExpr.getPackageName() != null) ? funcIExpr.getPackageName() + ":" +
                    funcIExpr.getName() : funcIExpr.getName();
            BLangExceptionHelper.throwSemanticError(funcIExpr, SemanticErrors.UNDEFINED_FUNCTION, funcName);
            return;
        }

        Function function;
        if (functionSymbol.isNative()) {
            functionSymbol = ((BallerinaFunction) functionSymbol).getNativeFunction();
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
                return;
            }
            function = (Function) functionSymbol;
        }

        // Link the function with the function invocation expression
        funcIExpr.setCallableUnit(function);
    }

    /**
     * Helper method to match the function with invocation (check whether parameters map, do cast if applicable).
     *
     * @param funcIExpr invocation expression
     * @param symbolName function symbol name
     * @param functionSymbol matching function
     * @return functionSymbol matching function
     */
    private BLangSymbol matchAndUpdateFunctionArguments(FunctionInvocationExpr funcIExpr,
                                                       FunctionSymbolName symbolName, BLangSymbol functionSymbol) {
        if (functionSymbol == null) {
            return null;
        }

        Expression[] argExprs = funcIExpr.getArgExprs();
        Expression[] updatedArgExprs = new Expression[argExprs.length];

        FunctionSymbolName funcSymName = (FunctionSymbolName) functionSymbol.getSymbolName();
        if (!funcSymName.isNameAndParamCountMatch(symbolName)) {
            return null;
        }

        boolean implicitCastPossible = true;

        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            updatedArgExprs[i] = argExpr;
            BType lhsType;
            if (functionSymbol instanceof NativeUnitProxy) {
                NativeUnit nativeUnit = ((NativeUnitProxy) functionSymbol).load();
                SimpleTypeName simpleTypeName = nativeUnit.getArgumentTypeNames()[i];
                lhsType = BTypes.resolveType(simpleTypeName, currentScope, funcIExpr.getNodeLocation());
            } else {
                lhsType = ((Function) functionSymbol).getParameterDefs()[i].getType();
            }

            AssignabilityResult result = performAssignabilityCheck(lhsType, argExpr);
            if (result.implicitCastExpr != null) {
                updatedArgExprs[i] = result.implicitCastExpr;
            } else if (!result.assignable) {
                // TODO do we need to throw an error here?
                implicitCastPossible = false;
                break;
            }
        }

        if (!implicitCastPossible) {
            return null;
        }

        for (int i = 0; i < updatedArgExprs.length; i++) {
            funcIExpr.getArgExprs()[i] = updatedArgExprs[i];
        }
        return functionSymbol;
    }

    /**
     * Helper method to generate error message for each ambiguous function.
     *
     * @param funcIExpr
     * @param functionSymbol
     * @return errorMsg
     */
    private static String generateErrorMessage(FunctionInvocationExpr funcIExpr, BLangSymbol functionSymbol,
                                               String packagePath) {
        Function function;
        //in future when native functions support implicit casting invocation, functionSymbol can be either
        //NativeUnitProxy or a Function.
        if (functionSymbol instanceof NativeUnitProxy) {
            NativeUnit nativeUnit = ((NativeUnitProxy) functionSymbol).load();

            if (!(nativeUnit instanceof Function)) {
                BLangExceptionHelper.throwSemanticError(funcIExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND,
                                                        functionSymbol.getName());
            }
            function = (Function) nativeUnit;
        } else {
            if (!(functionSymbol instanceof Function)) {
                BLangExceptionHelper.throwSemanticError(funcIExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND,
                                                        functionSymbol.getName());
            }
            function = (Function) functionSymbol;
        }
        //below getName should always return a valid String value, hence ArrayIndexOutOfBoundsException
        // or NullPointerException cannot happen here.
        String funcName = (function.getPackagePath() == null || function.getPackagePath().equals(".")) ?
                function.getName() : function.getPackagePath() + ":" + function.getName();

        StringBuilder sBuilder = new StringBuilder(funcName + "(");
        String prefix = "";
        for (ParameterDef parameterDef : function.getParameterDefs()) {
            sBuilder.append(prefix);
            prefix = ",";
            String pkgPath = parameterDef.getTypeName().getPackagePath();
            if (pkgPath != null) {
                sBuilder.append(pkgPath).append(":");
            }
            sBuilder.append(parameterDef.getTypeName().getName());
        }
        sBuilder.append(")");
        return sBuilder.toString();
    }

    /**
     * Get current package Scope.
     *
     * @param scope
     * @return scope
     */
    private SymbolScope getCurrentPackageScope(SymbolScope scope) {
        if (scope instanceof BLangPackage) {
            return scope;
        } else {
            return getCurrentPackageScope(scope.getEnclosingScope());
        }
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
            return;
        }

        Expression[] exprs = actionIExpr.getArgExprs();
        BType[] paramTypes = new BType[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        // When getting the action symbol name, Package name for the action is set to null, since the action is 
        // registered under connector, and connecter contains the package
        SymbolName actionSymbolName = LangModelUtils.getActionSymName(actionIExpr.getName(),
                actionIExpr.getPackagePath(), actionIExpr.getConnectorName(), paramTypes);

        // Now check whether there is a matching action
        BLangSymbol actionSymbol = null;
        if (connectorSymbol instanceof BallerinaConnectorDef) {
            actionSymbol = ((BallerinaConnectorDef) connectorSymbol).resolveMembers(actionSymbolName);
        } else {
            BLangExceptionHelper.throwSemanticError(actionIExpr, SemanticErrors.INCOMPATIBLE_TYPES_CONNECTOR_EXPECTED,
                    connectorSymbolName);
        }

        if ((actionSymbol instanceof BallerinaAction) && (actionSymbol.isNative())) {
            actionSymbol = ((BallerinaAction) actionSymbol).getNativeAction();
        }

        if (actionSymbol == null) {
            BLangExceptionHelper.throwSemanticError(actionIExpr, SemanticErrors.UNDEFINED_ACTION,
                    actionIExpr.getName(), connectorSymbol.getSymbolName());
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
                        actionSymbolName);
            }
            action = (Action) nativeUnit;
            action.setReturnParamTypes(returnTypes);

        } else if (actionSymbol instanceof Action) {
            action = (Action) actionSymbol;
        } else {
            BLangExceptionHelper.throwSemanticError(actionIExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND,
                    actionSymbolName);
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

    private void visitField(FieldAccessExpr fieldAccessExpr, SymbolScope enclosingScope) {
        ReferenceExpr varRefExpr = (ReferenceExpr) fieldAccessExpr.getVarRef();
        SymbolName symbolName = varRefExpr.getSymbolName();
        //BLangSymbol fieldSymbol = enclosingScope.resolve(symbolName);
        BLangSymbol fieldSymbol;
        //TODO resolve packge path conflict
        if (enclosingScope instanceof StructDef) {
            fieldSymbol = ((StructDef) enclosingScope).resolveMembers(new SymbolName(symbolName.getName(),
                    ((StructDef) enclosingScope).getPackagePath()));
        } else {
            fieldSymbol = enclosingScope.resolve(symbolName);
        }

        if (fieldSymbol == null) {
            if (enclosingScope instanceof StructDef) {
                BLangExceptionHelper.throwSemanticError(fieldAccessExpr, SemanticErrors.UNKNOWN_FIELD_IN_STRUCT,
                        symbolName.getName(), ((StructDef) enclosingScope).getName());
            } else {
                BLangExceptionHelper.throwSemanticError(fieldAccessExpr, SemanticErrors.UNDEFINED_SYMBOL,
                        symbolName.getName());
            }
        }

        // Set expression type
        if (!(fieldSymbol instanceof VariableDef)) {
            BLangExceptionHelper.throwSemanticError(varRefExpr, SemanticErrors.INCOMPATIBLE_TYPES_UNKNOWN_FOUND,
                    symbolName);
        }
        VariableDef varDef = (VariableDef) fieldSymbol;
        BType exprType = varDef.getType();

        /* Get the actual var representation of this field, and semantically analyze. This will check for semantic
         * errors of arrays/map accesses, used in this field.
         * eg: in dpt.employee[2].name , below will check for semantics of 'employee[2]',
         * treating them as individual arrays/map variables.
         */

        if (varRefExpr instanceof ArrayMapAccessExpr) {
            Expression rExpr = ((ArrayMapAccessExpr) varRefExpr).getRExpr();
            if (rExpr instanceof VariableRefExpr) {
                ((VariableRefExpr) rExpr).setVariableDef(varDef);
            }
            if (exprType instanceof BArrayType) {
                exprType = ((BArrayType) varDef.getType()).getElementType();
            }
            handleArrayType((ArrayMapAccessExpr) varRefExpr);
        } else {
            ((VariableRefExpr) varRefExpr).setVariableDef(varDef);
        }

        // Go to the child field
        FieldAccessExpr fieldExpr = fieldAccessExpr.getFieldExpr();
        if (fieldExpr == null) {
            return;
        }

        if (exprType instanceof StructDef) {
            visitStructAccessExpr(fieldExpr, exprType);
        } else if (exprType  instanceof BJSONType) {
            visitJSONAccessExpr(fieldAccessExpr, fieldExpr);
        } else if (exprType instanceof BMapType) {
            visitMapAccessExpr(fieldAccessExpr, varRefExpr, fieldExpr, enclosingScope);
        } else if (exprType instanceof BArrayType) {
            visitArrayAccessExpr(fieldAccessExpr, varRefExpr, fieldExpr, exprType, enclosingScope);
        } else {
            BLangExceptionHelper.throwSemanticError(fieldAccessExpr,
                    SemanticErrors.INVALID_OPERATION_NOT_SUPPORT_INDEXING, exprType);
        }
    }

    /**
     * Visit a struct and its fields and semantically validate the field expression.
     *
     * @param fieldExpr field expression to validate
     * @param exprType Struct definition
     */
    private void visitStructAccessExpr(FieldAccessExpr fieldExpr, BType exprType) {
        Expression fieldVar = fieldExpr.getVarRef();

        // Field of a struct is always a variable reference.
        if (fieldVar instanceof BasicLiteral) {
            String varName = ((BasicLiteral) fieldVar).getBValue().stringValue();
            VariableRefExpr varRef = new VariableRefExpr(fieldVar.getNodeLocation(), fieldVar.getWhiteSpaceDescriptor(),
                    varName);
            fieldExpr.setVarRef(varRef);
            fieldExpr.setIsStaticField(true);
        }

        if (!fieldExpr.isStaticField()) {
            BLangExceptionHelper.throwSemanticError(fieldVar, SemanticErrors.DYNAMIC_KEYS_NOT_SUPPORTED_FOR_STRUCT);
        }

        visitField(fieldExpr, ((StructDef) exprType));
    }

    /**
     * Visits a JSON access expression. Rewrites the tree by replacing the {@link FieldAccessExpr}
     * with a {@link JSONFieldAccessExpr}.
     *
     * @param parentExpr Current expression
     * @param fieldExpr Field access expression of the current expression
     */
    private void visitJSONAccessExpr(FieldAccessExpr parentExpr, FieldAccessExpr fieldExpr) {
        if (fieldExpr == null) {
            return;
        }

        FieldAccessExpr currentFieldExpr;
        FieldAccessExpr nextFieldExpr = fieldExpr.getFieldExpr();
        if (fieldExpr instanceof JSONFieldAccessExpr) {
            currentFieldExpr = fieldExpr;
        } else {
            Expression varRefExpr = fieldExpr.getVarRef();
            varRefExpr.accept(this);
            if (varRefExpr.getType() != BTypes.typeInt && varRefExpr.getType() != BTypes.typeString) {
                BLangExceptionHelper.throwSemanticError(varRefExpr,
                        SemanticErrors.INCOMPATIBLE_TYPES, "string or int", varRefExpr.getType());
            }

            currentFieldExpr = new JSONFieldAccessExpr(fieldExpr.getNodeLocation(), fieldExpr.getWhiteSpaceDescriptor(),
                    varRefExpr, nextFieldExpr);
        }
        parentExpr.setFieldExpr(currentFieldExpr);
        visitJSONAccessExpr(currentFieldExpr, nextFieldExpr);
    }

    /**
     * Visits a map access expression. Rewrites the tree by replacing the {@link FieldAccessExpr} with an
     * {@link ArrayMapAccessExpr}. Then revisits the rewritten branch, and check for semantic.
     *
     * @param parentExpr Current expression
     * @param varRefExpr VariableRefExpression of the current expression
     * @param fieldExpr Field access expression of the current expression
     * @param enclosingScope Enclosing scope
     */
    private void visitMapAccessExpr(FieldAccessExpr parentExpr, ReferenceExpr varRefExpr, FieldAccessExpr fieldExpr,
                                    SymbolScope enclosingScope) {
        Expression fieldVar = fieldExpr.getVarRef();

        // map access can only be at the end of a field access expression chain. Because maps are of any-type. Hence
        // cannot get a child field of any-type, without casting.
        // TODO: Improve this once type-bound maps are implemented
        if (fieldExpr.getFieldExpr() != null) {
            BLangExceptionHelper.throwSemanticError(fieldExpr, SemanticErrors.INDEXING_NOT_SUPPORTED_FOR_MAP_ELEMENT,
                    BTypes.typeAny);
        }

        Expression indexExpr[] = new Expression[]{fieldVar};

        ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder = new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();
        builder.setVarName(varRefExpr.getVarName());
        builder.setPkgName(varRefExpr.getPkgName());
        builder.setPkgPath(varRefExpr.getPkgPath());
        builder.setIndexExprs(indexExpr);
        builder.setArrayMapVarRefExpr(varRefExpr);
        builder.setNodeLocation(fieldExpr.getNodeLocation());
        ArrayMapAccessExpr accessExpr = builder.build();

        parentExpr.setFieldExpr(fieldExpr.getFieldExpr());
        parentExpr.setVarRef(accessExpr);
        accessExpr.setLHSExpr(parentExpr.isLHSExpr());
        visitField(parentExpr, enclosingScope);
    }

    /**
     * Visits an array access expression. Rewrites the tree by replacing the {@link FieldAccessExpr} with an
     * {@link ArrayMapAccessExpr}. Then revisits the rewritten branch, and check for semantic.
     *
     * @param parentExpr Current expression
     * @param varRefExpr VariableRefExpression of the current expression
     * @param fieldExpr Field access expression of the current expression
     * @param exprType Type to which the expression evaluates
     * @param enclosingScope Enclosing scope
     */
    private void visitArrayAccessExpr(FieldAccessExpr parentExpr, ReferenceExpr varRefExpr, FieldAccessExpr fieldExpr,
                                      BType exprType, SymbolScope enclosingScope) {

        if (fieldExpr.getVarRef() instanceof BasicLiteral) {
            String value = ((BasicLiteral) fieldExpr.getVarRef()).getBValue().stringValue();
            if (value.equals("length")) {

                if (parentExpr.isLHSExpr()) {
                    //cannot assign a value to array length
                    BLangExceptionHelper.throwSemanticError(fieldExpr, SemanticErrors.CANNOT_ASSIGN_VALUE_ARRAY_LENGTH);
                }

                if (fieldExpr.getFieldExpr() != null) {
                    BLangExceptionHelper.throwSemanticError(fieldExpr,
                            SemanticErrors.INVALID_OPERATION_NOT_SUPPORT_INDEXING, BTypes.typeInt);
                }

                ArrayLengthExpression arrayLengthExpr = new ArrayLengthExpression(
                        parentExpr.getNodeLocation(), null, varRefExpr);
                arrayLengthExpr.setType(BTypes.typeInt);
                FieldAccessExpr childFAExpr = new FieldAccessExpr(parentExpr.getNodeLocation(),
                        null, arrayLengthExpr, null);
                parentExpr.setFieldExpr(childFAExpr);
                return;
            }
        }

        int dimensions = ((BArrayType) exprType).getDimensions();
        List<Expression> indexExprs = new ArrayList<>();

        for (int i = 0; i < dimensions; i++) {
            if (fieldExpr == null) {
                break;
            }
            indexExprs.add(fieldExpr.getVarRef());
            fieldExpr = fieldExpr.getFieldExpr();
        }
        Collections.reverse(indexExprs);

        ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder = new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();
        builder.setVarName(varRefExpr.getVarName());
        builder.setPkgName(varRefExpr.getPkgName());
        builder.setPkgPath(varRefExpr.getPkgPath());
        builder.setIndexExprs(indexExprs.toArray(new Expression[0]));
        builder.setArrayMapVarRefExpr(varRefExpr);
        builder.setNodeLocation(parentExpr.getNodeLocation());

        ArrayMapAccessExpr accessExpr = builder.build();
        parentExpr.setFieldExpr(fieldExpr);
        parentExpr.setVarRef(accessExpr);
        accessExpr.setLHSExpr(parentExpr.isLHSExpr());
        visitField(parentExpr, enclosingScope);
    }

    private TypeCastExpression checkWideningPossible(BType lhsType, Expression rhsExpr) {
        TypeCastExpression typeCastExpr = null;
        BType rhsType = rhsExpr.getType();

        TypeEdge typeEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(rhsType, lhsType, null);
        if (typeEdge != null) {
            typeCastExpr = new TypeCastExpression(rhsExpr.getNodeLocation(),
                    rhsExpr.getWhiteSpaceDescriptor(), rhsExpr, lhsType);
            typeCastExpr.setOpcode(typeEdge.getOpcode());
            typeCastExpr.setEvalFunc(typeEdge.getTypeMapperFunction());
        }
        return typeCastExpr;
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
        } else if (currentScope.getScopeName() == SymbolScope.ScopeName.STRUCT) {
            variableDef.setMemoryLocation(new StructVarLocation(++structMemAddrOffset));
        } else if (currentScope.getScopeName() == SymbolScope.ScopeName.PACKAGE) {
            variableDef.setMemoryLocation(new GlobalVarLocation(++staticMemAddrOffset));
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
            FunctionSymbolName symbolName = LangModelUtils.getFuncSymNameWithParams(function.getName(),
                                                                            function.getPackagePath(), paramTypes);
            function.setSymbolName(symbolName);

            BLangSymbol functionSymbol = currentScope.resolve(symbolName);

            if (!function.isNative() && functionSymbol != null) {
                BLangExceptionHelper.throwSemanticError(function,
                        SemanticErrors.REDECLARED_SYMBOL, function.getName());
            }

            if (function.isNative() && functionSymbol == null) {
                functionSymbol = nativeScope.resolve(symbolName);
                if (functionSymbol == null) {
                    BLangExceptionHelper.throwSemanticError(function,
                            SemanticErrors.UNDEFINED_FUNCTION, function.getName());
                }
                if (function instanceof BallerinaFunction) {
                    ((BallerinaFunction) function).setNativeFunction((NativeUnitProxy) functionSymbol);
                }
            }

            currentScope.define(symbolName, function);

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

    private void defineConnectors(BallerinaConnectorDef[] connectorDefArray) {
        for (BallerinaConnectorDef connectorDef : connectorDefArray) {
            String connectorName = connectorDef.getName();

            // Define ConnectorDef Symbol in the package scope..
            SymbolName connectorSymbolName = new SymbolName(connectorName, connectorDef.getPackagePath());
            BLangSymbol connectorSymbol = currentScope.resolve(connectorSymbolName);
            if (connectorSymbol != null) {
                BLangExceptionHelper.throwSemanticError(connectorDef,
                        SemanticErrors.REDECLARED_SYMBOL, connectorName);
            }
            currentScope.define(connectorSymbolName, connectorDef);

            BLangSymbol actionSymbol;
            SymbolName name = new SymbolName("NativeAction." + connectorName
                    + ".<init>", connectorDef.getPackagePath());
            actionSymbol = nativeScope.resolve(name);
            if (actionSymbol != null) {
                if (actionSymbol instanceof NativeUnitProxy) {
                    NativeUnit nativeUnit = ((NativeUnitProxy) actionSymbol).load();
                    Action action = (Action) nativeUnit;
                    connectorDef.setInitAction(action);
                }
            }
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
        SymbolName symbolName = LangModelUtils.getActionSymName(action.getName(), action.getPackagePath(),
                connectorDef.getName(), paramTypes);
        action.setSymbolName(symbolName);

        BLangSymbol actionSymbol = currentScope.resolve(symbolName);
        if (actionSymbol != null) {
            BLangExceptionHelper.throwSemanticError(action, SemanticErrors.REDECLARED_SYMBOL, action.getName());
        }
        currentScope.define(symbolName, action);

        if (action.isNative()) {
            SymbolName nativeActionSymName = LangModelUtils.getNativeActionSymName(action.getName(),
                    connectorDef.getName(), action.getPackagePath(), paramTypes);
            BLangSymbol nativeAction = nativeScope.resolve(nativeActionSymName);

            if (nativeAction == null || !(nativeAction instanceof NativeUnitProxy)) {
                BLangExceptionHelper.throwSemanticError(connectorDef,
                        SemanticErrors.UNDEFINED_NATIVE_ACTION, action.getName(), connectorDef.getName());
                return;
            }

            action.setNativeAction((NativeUnitProxy) nativeAction);
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
        SymbolName symbolName = LangModelUtils.getActionSymName(resource.getName(),
                resource.getPackagePath(), service.getName(), paramTypes);
        resource.setSymbolName(symbolName);

        if (currentScope.resolve(symbolName) != null) {
            BLangExceptionHelper.throwSemanticError(resource, SemanticErrors.REDECLARED_SYMBOL, resource.getName());
        }
        currentScope.define(symbolName, resource);
    }

    private void defineStructs(StructDef[] structDefs) {
        for (StructDef structDef : structDefs) {

            SymbolName symbolName = new SymbolName(structDef.getName(), structDef.getPackagePath());
            // Check whether this constant is already defined.
            if (currentScope.resolve(symbolName) != null) {
                BLangExceptionHelper.throwSemanticError(structDef,
                        SemanticErrors.REDECLARED_SYMBOL, structDef.getName());
            }

            currentScope.define(symbolName, structDef);

            // Create the '<init>' function and inject it to the struct
            BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(
                structDef.getNodeLocation(), structDef);
            for (VariableDefStmt variableDefStmt : structDef.getFieldDefStmts()) {
                blockStmtBuilder.addStmt(variableDefStmt);
            }

            BallerinaFunction.BallerinaFunctionBuilder functionBuilder =
                    new BallerinaFunction.BallerinaFunctionBuilder(structDef);
            functionBuilder.setNodeLocation(structDef.getNodeLocation());
            functionBuilder.setIdentifier(new Identifier(structDef + ".<init>"));
            functionBuilder.setPkgPath(structDef.getPackagePath());
            functionBuilder.setBody(blockStmtBuilder.build());
            structDef.setInitFunction(functionBuilder.buildFunction());
        }

        // Define fields in each struct. This is done after defining all the structs,
        // since a field of a struct can be another struct.
        for (StructDef structDef : structDefs) {
            SymbolScope tmpScope = currentScope;
            currentScope = structDef;
            for (VariableDefStmt fieldDefStmt : structDef.getFieldDefStmts()) {
                fieldDefStmt.accept(this);
            }
            structDef.setStructMemorySize(structMemAddrOffset + 1);

            structMemAddrOffset = -1;
            currentScope = tmpScope;
        }

        // Add type mappers for each struct. This is done after defining all the fields of all the structs,
        // since fields of structs are compared when adding type mappers.
        for (StructDef structDef : structDefs) {
            TypeLattice.addStructEdges(structDef, currentScope);
        }
    }

    /**
     * Add the annotation definitions to the current scope.
     *
     * @param annotationDefs Annotations definitions list
     */
    private void defineAnnotations(AnnotationDef[] annotationDefs) {
        for (AnnotationDef annotationDef : annotationDefs) {
            SymbolName symbolName = new SymbolName(annotationDef.getName(), currentPkg);

            // Check whether this annotation is already defined.
            if (currentScope.resolve(symbolName) != null) {
                BLangExceptionHelper.throwSemanticError(annotationDef,
                        SemanticErrors.REDECLARED_SYMBOL, annotationDef.getSymbolName().getName());
            }

            currentScope.define(symbolName, annotationDef);
        }
    }

    /**
     * Create the '<init>' function and inject it to the connector.
     *
     * @param connectorDef connector model object
     */
    private void createConnectorInitFunction(BallerinaConnectorDef connectorDef) {
        NodeLocation location = connectorDef.getNodeLocation();
        BallerinaFunction.BallerinaFunctionBuilder functionBuilder =
                new BallerinaFunction.BallerinaFunctionBuilder(connectorDef);
        functionBuilder.setNodeLocation(location);
        functionBuilder.setIdentifier(new Identifier(connectorDef.getName() + ".<init>"));
        functionBuilder.setPkgPath(connectorDef.getPackagePath());

        ParameterDef paramDef = new ParameterDef(location, null, new Identifier("connector"),
                null, new SymbolName("connector"), functionBuilder.getCurrentScope());
        paramDef.setType(connectorDef);
        functionBuilder.addParameter(paramDef);

        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, connectorDef);

        for (VariableDefStmt variableDefStmt : connectorDef.getVariableDefStmts()) {
            AssignStmt assignStmt = new AssignStmt(variableDefStmt.getNodeLocation(),
                    new Expression[]{variableDefStmt.getLExpr()}, variableDefStmt.getRExpr());
            blockStmtBuilder.addStmt(assignStmt);
        }

        // Adding the return statement
        ReturnStmt returnStmt = new ReturnStmt(location, null, new Expression[0]);
        blockStmtBuilder.addStmt(returnStmt);
        functionBuilder.setBody(blockStmtBuilder.build());
        connectorDef.setInitFunction(functionBuilder.buildFunction());
    }

    /**
     * Create the '<init>' function and inject it to the service.
     *
     * @param service service model object
     */
    private void createServiceInitFunction(Service service) {
        NodeLocation location = service.getNodeLocation();
        BallerinaFunction.BallerinaFunctionBuilder functionBuilder =
                new BallerinaFunction.BallerinaFunctionBuilder(service);
        functionBuilder.setNodeLocation(location);
        functionBuilder.setIdentifier(new Identifier(service.getName() + ".<init>"));
        functionBuilder.setPkgPath(service.getPackagePath());

        BlockStmt.BlockStmtBuilder blockStmtBuilder = new BlockStmt.BlockStmtBuilder(location, service);
        for (VariableDefStmt variableDefStmt : service.getVariableDefStmts()) {
            AssignStmt assignStmt = new AssignStmt(variableDefStmt.getNodeLocation(),
                    new Expression[]{variableDefStmt.getLExpr()}, variableDefStmt.getRExpr());
            blockStmtBuilder.addStmt(assignStmt);
        }

        // Adding the return statement
        ReturnStmt returnStmt = new ReturnStmt(location, null, new Expression[0]);
        blockStmtBuilder.addStmt(returnStmt);
        functionBuilder.setBody(blockStmtBuilder.build());
        service.setInitFunction(functionBuilder.buildFunction());
    }

    private void resolveWorkerInteractions(WorkerInteractionDataHolder[] workerInteractionDataHolders) {
        for (WorkerInteractionDataHolder workerInteraction : workerInteractionDataHolders) {
            if (workerInteraction.getSourceWorker() == null || workerInteraction.getWorkerReplyStmt() == null) {
                // Invalid worker reply statement
                // TODO: Need to have a specific error message
                BLangExceptionHelper.throwSemanticError(workerInteraction.getWorkerInvocationStmt(),
                        SemanticErrors.WORKER_INTERACTION_NOT_VALID);
            }

            if (workerInteraction.getTargetWorker() == null || workerInteraction.getWorkerInvocationStmt() == null) {
                // Invalid worker invocation statement
                // TODO: Need to have a specific error message
                BLangExceptionHelper.throwSemanticError(workerInteraction.getWorkerReplyStmt(),
                        SemanticErrors.WORKER_INTERACTION_NOT_VALID);
            }

            if (workerInteraction.getSourceWorker() == workerInteraction.getTargetWorker()) {
                // Worker cannot invoke itself.
                // TODO: Need to have a specific error message
                    BLangExceptionHelper.throwSemanticError(workerInteraction.getWorkerReplyStmt(),
                            SemanticErrors.WORKER_INTERACTION_NOT_VALID);
            }

            if (workerInteraction.getWorkerReplyStmt() != null && workerInteraction.getWorkerInvocationStmt() != null) {
                // Check for number of variables send and received
                Expression[] invokeParams = workerInteraction.getWorkerInvocationStmt().getExpressionList();
                Expression[] receiveParams = workerInteraction.getWorkerReplyStmt().getExpressionList();
                if (invokeParams.length != receiveParams.length) {
                    // TODO: Need to have a specific error message
                    BLangExceptionHelper.throwSemanticError(workerInteraction.getWorkerReplyStmt(),
                            SemanticErrors.WORKER_INTERACTION_NOT_VALID);
                } else {
                    int i = 0;
                    for (Expression invokeParam : invokeParams) {
                        if (!(receiveParams[i++].getType().equals(invokeParam.getType()))) {
                            // TODO: Need to have a specific error message
                            BLangExceptionHelper.throwSemanticError(workerInteraction.getWorkerReplyStmt(),
                                    SemanticErrors.WORKER_INTERACTION_NOT_VALID);
                        }
                    }
                }
            }
        }
    }

    private void resolveStructFieldTypes(StructDef[] structDefs) {
        for (StructDef structDef : structDefs) {
            for (VariableDefStmt fieldDefStmt : structDef.getFieldDefStmts()) {
                VariableDef fieldDef = fieldDefStmt.getVariableDef();
                BType fieldType = BTypes.resolveType(fieldDef.getTypeName(), currentScope,
                        fieldDef.getNodeLocation());
                fieldDef.setType(fieldType);
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

    /**
     * Recursively visits a nested init expression. Reconstruct the init expression with the
     * specific init expression type, and replaces the generic {@link RefTypeInitExpr}.
     *
     * @param fieldType Type of the current field
     * @return reconstructed nested init expression
     */
    private RefTypeInitExpr getNestedInitExpr(Expression expr, BType fieldType) {
        RefTypeInitExpr refTypeInitExpr = (RefTypeInitExpr) expr;
        if (refTypeInitExpr instanceof ArrayInitExpr) {
            if (fieldType == BTypes.typeAny || fieldType == BTypes.typeMap) {
                fieldType = BTypes.resolveType(new SimpleTypeName(BTypes.typeAny.getName(),
                                true, 1), currentScope, expr.getNodeLocation());
            } else if (fieldType == BTypes.typeJSON) {
                refTypeInitExpr = new JSONArrayInitExpr(refTypeInitExpr.getNodeLocation(),
                        refTypeInitExpr.getWhiteSpaceDescriptor(), refTypeInitExpr.getArgExprs());
            }
        } else if (!(refTypeInitExpr instanceof BacktickExpr)) {
            // if the inherited type is any, then default this initializer to a map init expression
            if (fieldType == BTypes.typeAny) {
                fieldType = BTypes.typeMap;
            }
            if (fieldType == BTypes.typeMap) {
                refTypeInitExpr = new MapInitExpr(refTypeInitExpr.getNodeLocation(),
                        refTypeInitExpr.getWhiteSpaceDescriptor(), refTypeInitExpr.getArgExprs());
            } else if (fieldType == BTypes.typeJSON) {
                refTypeInitExpr = new JSONInitExpr(refTypeInitExpr.getNodeLocation(),
                        refTypeInitExpr.getWhiteSpaceDescriptor(), refTypeInitExpr.getArgExprs());
            } else if (fieldType instanceof StructDef) {
                refTypeInitExpr = new StructInitExpr(refTypeInitExpr.getNodeLocation(),
                        refTypeInitExpr.getWhiteSpaceDescriptor(), refTypeInitExpr.getArgExprs());
            }
        }
        refTypeInitExpr.setInheritedType(fieldType);

        return refTypeInitExpr;
    }

    /**
     * Visit and validate map/json initialize expression.
     *
     * @param initExpr Expression to visit.
     */
    private void visitMapJsonInitExpr(RefTypeInitExpr initExpr) {
        BType inheritedType = initExpr.getInheritedType();
        initExpr.setType(inheritedType);
        Expression[] argExprs = initExpr.getArgExprs();

        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            KeyValueExpr keyValueExpr = (KeyValueExpr) argExpr;
            Expression keyExpr = keyValueExpr.getKeyExpr();

            // In maps and json, key is always a string literal.
            if (keyExpr instanceof VariableRefExpr) {
                BString key = new BString(((VariableRefExpr) keyExpr).getVarName());
                keyExpr = new BasicLiteral(keyExpr.getNodeLocation(), keyExpr.getWhiteSpaceDescriptor(),
                        new SimpleTypeName(TypeConstants.STRING_TNAME),
                        key);
                keyValueExpr.setKeyExpr(keyExpr);
            }
            visitSingleValueExpr(keyExpr);

            Expression valueExpr = keyValueExpr.getValueExpr();
            if (valueExpr instanceof RefTypeInitExpr) {
                valueExpr = getNestedInitExpr(valueExpr, inheritedType);
                keyValueExpr.setValueExpr(valueExpr);
            }
            valueExpr.accept(this);
            BType valueExprType = valueExpr.getType();

            // Generate type cast expression if the rhs type is a value type
            if (inheritedType == BTypes.typeMap) {
                if (BTypes.isValueType(valueExprType)) {
                    TypeCastExpression newExpr = checkWideningPossible(BTypes.typeAny, valueExpr);
                    if (newExpr != null) {
                        keyValueExpr.setValueExpr(newExpr);
                    } else {
                        BLangExceptionHelper.throwSemanticError(keyValueExpr,
                                SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT, valueExprType.getSymbolName(),
                                inheritedType);
                    }
                }
                continue;
            }

            // for JSON init expr, check the type compatibility of the value.
            if (BTypes.isValueType(valueExprType)) {
                TypeCastExpression typeCastExpr = checkWideningPossible(BTypes.typeJSON, valueExpr);
                if (typeCastExpr != null) {
                    keyValueExpr.setValueExpr(typeCastExpr);
                } else {
                    BLangExceptionHelper.throwSemanticError(keyValueExpr,
                            SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT, valueExprType.getSymbolName(),
                            inheritedType.getSymbolName());
                }
                continue;
            }

            if (valueExprType != BTypes.typeNull && isAssignableTo(BTypes.typeJSON, valueExprType)) {
                continue;
            }

            TypeCastExpression typeCastExpr = checkWideningPossible(BTypes.typeJSON, valueExpr);
            if (typeCastExpr == null) {
                BLangExceptionHelper.throwSemanticError(initExpr, SemanticErrors.INCOMPATIBLE_TYPES_CANNOT_CONVERT,
                        valueExpr.getType(), BTypes.typeJSON);
            }
            keyValueExpr.setValueExpr(typeCastExpr);
        }

    }

    private void addDependentPkgInitCalls(List<BallerinaFunction> initFunctionList,
                                          BlockStmt.BlockStmtBuilder blockStmtBuilder, NodeLocation initFuncLocation) {
        for (BallerinaFunction initFunc : initFunctionList) {
            FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(initFuncLocation, null,
                    initFunc.getName(), null, initFunc.getPackagePath(), new Expression[]{});
            funcIExpr.setCallableUnit(initFunc);
            FunctionInvocationStmt funcIStmt = new FunctionInvocationStmt(initFuncLocation, funcIExpr);
            blockStmtBuilder.addStmt(funcIStmt);
        }
    }

    private boolean isAssignableTo(BType lhsType, BType rhsType) {
        if (lhsType == BTypes.typeAny) {
            return true;
        }

        if (rhsType == BTypes.typeNull && !BTypes.isValueType(lhsType)) {
            return true;
        }

        return lhsType == rhsType;
    }

    private boolean checkUnsafeCastPossible(BType sourceType, BType targetType) {

        // 1) If either source type or target type is of type 'any', then an unsafe cast possible;
        if (sourceType == BTypes.typeAny || targetType == BTypes.typeAny) {
            return true;
        }

        // 2) If both types are struct types, unsafe cast is possible
        if (sourceType instanceof StructDef && targetType instanceof StructDef) {
            return true;
        }

        // 3) If both types are not array types, unsafe cast is not possible now.
        if (targetType.getTag() == TypeTags.ARRAY_TAG && sourceType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType targetArrayType = (BArrayType) targetType;
            BArrayType sourceArrayType = (BArrayType) sourceType;

            if (sourceArrayType.getDimensions() < targetArrayType.getDimensions()) {
                return false;
            }

            return checkUnsafeCastPossible(sourceArrayType.getElementType(), targetArrayType.getElementType());
        }

        return false;
    }

    private AssignabilityResult performAssignabilityCheck(BType lhsType, Expression rhsExpr) {
        AssignabilityResult assignabilityResult = new AssignabilityResult();
        BType rhsType = rhsExpr.getType();
        if (lhsType == rhsType) {
            assignabilityResult.assignable = true;
            return assignabilityResult;
        }

        if (rhsType == BTypes.typeNull && !BTypes.isValueType(lhsType)) {
            assignabilityResult.assignable = true;
            return assignabilityResult;
        }

        // Now check whether an implicit cast is available;
        TypeCastExpression implicitCastExpr = checkWideningPossible(lhsType, rhsExpr);
        if (implicitCastExpr != null) {
            assignabilityResult.assignable = true;
            assignabilityResult.implicitCastExpr = implicitCastExpr;
            return assignabilityResult;
        }

        // Now check whether left-hand side type is 'any', then an implicit cast is possible;
        if (isImplicitiCastPossible(lhsType, rhsType)) {
            implicitCastExpr = new TypeCastExpression(rhsExpr.getNodeLocation(),
                    null, rhsExpr, lhsType);
            implicitCastExpr.setOpcode(InstructionCodes.NOP);

            // TODO Remove following line once the old interpreter is removed.
            implicitCastExpr.setEvalFunc(NativeCastMapper.STRUCT_TO_STRUCT_SAFE_FUNC);

            assignabilityResult.assignable = true;
            assignabilityResult.implicitCastExpr = implicitCastExpr;
            return assignabilityResult;
        }

        // Further check whether types are assignable recursively, specially array types

        return assignabilityResult;
    }

    private boolean isImplicitiCastPossible(BType lhsType, BType rhsType) {
        if (lhsType == BTypes.typeAny) {
            return true;
        }

        // 2) Check whether both types are array types
        if (lhsType.getTag() == TypeTags.ARRAY_TAG && rhsType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType lhrArrayType = (BArrayType) lhsType;
            BArrayType rhsArrayType = (BArrayType) rhsType;

            if (rhsArrayType.getDimensions() < lhrArrayType.getDimensions()) {
                return false;
            }

            return isImplicitiCastPossible(lhrArrayType.getElementType(), rhsArrayType.getElementType());
        }

        return false;
    }

    private void checkAndAddReturnStmt(int returnParamCount, BlockStmt blockStmt) {
        if (returnParamCount != 0) {
            return;
        }

        Statement[] statements = blockStmt.getStatements();
        int length = statements.length;
        Statement lastStatement = statements[length - 1];
        if (!(lastStatement instanceof ReturnStmt)) {
            NodeLocation location = lastStatement.getNodeLocation();
            ReturnStmt returnStmt = new ReturnStmt(
                    new NodeLocation(location.getFileName(), location.getLineNumber() + 1), null, new Expression[0]);
            statements = Arrays.copyOf(statements, length + 1);
            statements[length] = returnStmt;
            blockStmt.setStatements(statements);
        }
    }

    private void checkAndAddReplyStmt(BlockStmt blockStmt) {
        Statement[] statements = blockStmt.getStatements();
        int length = statements.length;
        Statement lastStatement = statements[length - 1];
        if (!(lastStatement instanceof ReplyStmt)) {
            NodeLocation location = lastStatement.getNodeLocation();
            ReplyStmt replyStmt = new ReplyStmt(
                    new NodeLocation(location.getFileName(), location.getLineNumber() + 1), null, null);
            statements = Arrays.copyOf(statements, length + 1);
            statements[length] = replyStmt;
            blockStmt.setStatements(statements);
        }
    }

    /**
     * This class holds the results of the type assignability check.
     *
     * @since 0.88
     */
    static class AssignabilityResult {
        boolean assignable;
        TypeCastExpression implicitCastExpr;
    }

    @Override
    public void visit(ArrayLengthExpression arrayLengthExpression) {
    }
}

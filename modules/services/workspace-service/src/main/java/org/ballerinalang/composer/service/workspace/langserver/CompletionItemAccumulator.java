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
package org.ballerinalang.composer.service.workspace.langserver;

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
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.Identifier;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NamespaceDeclaration;
import org.ballerinalang.model.Node;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
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
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.BinaryLogicalExpression;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.JSONArrayInitExpr;
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
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.XMLQNameExpr;
import org.ballerinalang.model.expressions.variablerefs.FieldBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.IndexBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.SimpleVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.VariableReferenceExpr;
import org.ballerinalang.model.expressions.variablerefs.XMLAttributesRefExpr;
import org.ballerinalang.model.statements.AbortStmt;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.CommentStmt;
import org.ballerinalang.model.statements.ContinueStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.NamespaceDeclarationStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TransactionStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.types.TypeEdge;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.model.util.LangModelUtils;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.runtime.worker.WorkerDataChannel;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangPrograms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * {@code SemanticAnalyzer} analyzes semantic properties of a Ballerina program.
 *
 * @since 0.8.0
 */
public class CompletionItemAccumulator implements NodeVisitor {
    private static final String ERRORS_PACKAGE = "ballerina.lang.errors";
    private static final String BALLERINA_CAST_ERROR = "TypeCastError";
    private static final String BALLERINA_CONVERSION_ERROR = "TypeConversionError";
    private static final String BALLERINA_ERROR = "Error";

    private int stackFrameOffset = -1;
    private int staticMemAddrOffset = -1;
    private int connectorMemAddrOffset = -1;
    private int structMemAddrOffset = -1;
    private int workerMemAddrOffset = -1;
    private String currentPkg;
    private CallableUnit currentCallableUnit = null;
    private Stack<CallableUnit> parentCallableUnit = new Stack<>();

    private Stack<SymbolScope> parentScope = new Stack<>();

    private SymbolScope currentScope;
    private SymbolScope nativeScope;

    private BlockStmt.BlockStmtBuilder pkgInitFuncStmtBuilder;

    private List completionItems;
    private org.ballerinalang.composer.service.workspace.langserver.dto.Position position;

    private SymbolScope closestScope;

    public CompletionItemAccumulator(List completionItems,
                                     org.ballerinalang.composer.service.workspace.langserver.dto.Position position) {
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        currentScope = globalScope;
        this.nativeScope = BLangPrograms.populateNativeScope();
        this.completionItems = completionItems;
        this.position = position;
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

        //resolveWorkerInteractions(bLangPackage.getWorkerInteractionDataHolders());
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
        for (CompilationUnit compilationUnit : bFile.getCompilationUnits()) {
            compilationUnit.accept(this);
        }
        getSymbolMap(closestScope, completionItems);
        getSymbolMap(this.nativeScope, completionItems);
    }

    @Override
    public void visit(ImportPackage importPkg) {
    }

    @Override
    public void visit(ConstDef constDef) {
        checkAndSetClosestScope(constDef);
        SimpleTypeName typeName = constDef.getTypeName();
        BType bType = BTypes.resolveType(typeName, currentScope, constDef.getNodeLocation());
        constDef.setType(bType);

        // Check whether this constant is already defined.
        SymbolName symbolName = new SymbolName(constDef.getName(), currentPkg);


        // Define the constant in the package scope
        currentScope.define(symbolName, constDef);

        constDef.getVariableDefStmt().accept(this);

        for (AnnotationAttachment annotationAttachment : constDef.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.CONSTANT);
            annotationAttachment.accept(this);
        }

        // Set memory location
        ConstantLocation memLocation = new ConstantLocation(++staticMemAddrOffset);
        constDef.setMemoryLocation(memLocation);

        // Insert constant initialization stmt to the package init function
        SimpleVarRefExpr varRefExpr = new SimpleVarRefExpr(constDef.getNodeLocation(),
                constDef.getWhiteSpaceDescriptor(), constDef.getName(), null, null);
        varRefExpr.setVariableDef(constDef);
        AssignStmt assignStmt = new AssignStmt(constDef.getNodeLocation(),
                new Expression[]{varRefExpr}, constDef.getVariableDefStmt().getRExpr());
        pkgInitFuncStmtBuilder.addStmt(assignStmt);

        addToCompletionItems(constDef);
    }

    @Override
    public void visit(GlobalVariableDef globalVarDef) {
        checkAndSetClosestScope(globalVarDef);
        VariableDefStmt variableDefStmt = globalVarDef.getVariableDefStmt();
        variableDefStmt.accept(this);

        if (variableDefStmt.getRExpr() != null) {
            // Create an assignment statement
            // Insert global variable initialization stmt to the package init function
            AssignStmt assignStmt = new AssignStmt(variableDefStmt.getNodeLocation(),
                    new Expression[]{variableDefStmt.getLExpr()}, variableDefStmt.getRExpr());
            if (pkgInitFuncStmtBuilder != null) {
                pkgInitFuncStmtBuilder.addStmt(assignStmt);
            }
        }

        addToCompletionItems(globalVarDef);
    }

    private void addToCompletionItems(BLangSymbol symbol) {
        SymbolInfo symbolInfo = new SymbolInfo(symbol.getName(), symbol);
        completionItems.add(symbolInfo);
    }

    @Override
    public void visit(Service service) {
        // Visit the contents within a service
        // Open a new symbol scope
        openScope(service);

        checkAndSetClosestScope(service);

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

        checkAndSetClosestScope(connectorDef);

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
        addToCompletionItems(connectorDef);
        closeScope();
    }

    @Override
    public void visit(Resource resource) {
        // Visit the contents within a resource
        // Open a new symbol scope
        openScope(resource);
        checkAndSetClosestScope(resource);

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


        int sizeOfStackFrame = stackFrameOffset + 1;
        resource.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        stackFrameOffset = -1;
        currentCallableUnit = null;
        closeScope();
    }

    private void resolveForkJoin(ForkJoinStmt forkJoinStmt) {
        Worker[] workers = forkJoinStmt.getWorkers();
        if (workers != null && workers.length > 0) {
            for (Worker worker : workers) {
                for (Statement statement : worker.getWorkerInteractionStatements()) {
                    if (statement instanceof WorkerInvocationStmt) {
                        String targetWorkerName = ((WorkerInvocationStmt) statement).getName();
                        if (targetWorkerName.equalsIgnoreCase("fork")) {
                            String sourceWorkerName = worker.getName();
                            WorkerDataChannel workerDataChannel = new WorkerDataChannel
                                    (sourceWorkerName, targetWorkerName);
                            ((WorkerInvocationStmt) statement).setWorkerDataChannel(workerDataChannel);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visit(BallerinaFunction function) {
        // Open a new symbol scope
        openScope(function);
        currentCallableUnit = function;

        checkAndSetClosestScope(function);
        //resolveWorkerInteractions(function.gerWorkerInteractions());

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


            //checkAndAddReturnStmt(function.getReturnParamTypes().length, blockStmt);
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

        addToCompletionItems(function);
        closeScope();
    }

    @Override
    public void visit(BTypeMapper typeMapper) {
        // Open a new symbol scope
        openScope(typeMapper);
        currentCallableUnit = typeMapper;

        checkAndSetClosestScope(typeMapper);

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

        addToCompletionItems(typeMapper);
        closeScope();
    }

    @Override
    public void visit(BallerinaAction action) {
        // Open a new symbol scope
        openScope(action);
        currentCallableUnit = action;

        checkAndSetClosestScope(action);

        for (AnnotationAttachment annotationAttachment : action.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.ACTION);
            annotationAttachment.accept(this);
        }

        for (ParameterDef parameterDef : action.getParameterDefs()) {
            parameterDef.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            parameterDef.accept(this);
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
        checkAndSetClosestScope(worker);

        //resolveWorkerInteractions(worker.gerWorkerInteractions());

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

        //resolveWorkerInteractions(worker);

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
        currentScope.define(symbolName, worker);
    }

    @Override
    public void visit(StructDef structDef) {
        checkAndSetClosestScope(structDef);
        for (AnnotationAttachment annotationAttachment : structDef.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.STRUCT);
            annotationAttachment.accept(this);
        }
        addToCompletionItems(structDef);
    }

    @Override
    public void visit(AnnotationAttachment annotation) {
        checkAndSetClosestScope(annotation);
        //AttachmentPoint attachedPoint = annotation.getAttachedPoint();
        //SymbolName annotationSymName = new SymbolName(annotation.getName(), annotation.getPkgPath());
        //BLangSymbol annotationSymbol = currentScope.resolve(annotationSymName);

        //getSymbolMap(currentScope, completionItems);

    }


    private void getSymbolMap(SymbolScope symbolScope, List symbols) {
        if (symbolScope != null) {
            symbolScope.getSymbolMap().forEach((k, v) -> {
                SymbolInfo symbolInfo = new SymbolInfo(k.getName(), v);
                symbols.add(symbolInfo);
            });
//            symbols.addAll(symbolScope.getSymbolMap());
            SymbolScope enclosingScope = symbolScope.getEnclosingScope();
            if (enclosingScope != null) {
                getSymbolMap(enclosingScope, symbols);
            } else {
                return;
            }
        }
    }

    @Override
    public void visit(AnnotationAttributeDef annotationAttributeDef) {
        checkAndSetClosestScope(annotationAttributeDef);
        BasicLiteral fieldVal = annotationAttributeDef.getAttributeValue();

        if (fieldVal != null) {
            fieldVal.accept(this);
        }
    }

    @Override
    public void visit(AnnotationDef annotationDef) {
        checkAndSetClosestScope(annotationDef);
        for (AnnotationAttributeDef fields : annotationDef.getAttributeDefs()) {
            fields.accept(this);
        }

        for (AnnotationAttachment annotationAttachment : annotationDef.getAnnotations()) {
            annotationAttachment.setAttachedPoint(AttachmentPoint.ANNOTATION);
            annotationAttachment.accept(this);
        }
        addToCompletionItems(annotationDef);
    }

    @Override
    public void visit(ParameterDef paramDef) {
        checkAndSetClosestScope(paramDef);

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
        checkAndSetClosestScope(varDefStmt);
        // Resolves the type of the variable
        VariableDef varDef = varDefStmt.getVariableDef();


        // Mark the this variable references as LHS expressions
        ((VariableReferenceExpr) varDefStmt.getLExpr()).setLHSExpr(true);

        // Check whether this variable is already defined, if not define it.
        SymbolName symbolName = new SymbolName(varDef.getName(), currentPkg);

        currentScope.define(symbolName, varDef);

        // Set memory location
        setMemoryLocation(varDef);

        Expression rExpr = varDefStmt.getRExpr();
        if (rExpr == null) {
            return;
        }


        if (rExpr instanceof ExecutableMultiReturnExpr) {
            rExpr.accept(this);

        } else {
            visitSingleValueExpr(rExpr);
        }
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        checkAndSetClosestScope(assignStmt);
        Expression[] lExprs = assignStmt.getLExprs();
        visitLExprsOfAssignment(assignStmt, lExprs);

        Expression rExpr = assignStmt.getRExpr();
        if (rExpr instanceof FunctionInvocationExpr || rExpr instanceof ActionInvocationExpr) {
            rExpr.accept(this);
            return;
        }

        if (lExprs.length > 1 && (rExpr instanceof TypeCastExpression || rExpr instanceof TypeConversionExpr)) {
            ((AbstractExpression) rExpr).setMultiReturnAvailable(true);
            rExpr.accept(this);
            return;
        }
    }

    private void checkAndSetClosestScope(Node node) {
        int startLineNumber = node.getNodeLocation().startLineNumber;
        int startColumn = node.getNodeLocation().startColumn;

        Position stop = new Position();
        getStopPosition(node, stop);

        if (position.getLine() > startLineNumber) {
            if (stop.lineNumber != -1 && stop.column != -1) {
                if (position.getLine() < stop.lineNumber) {
                    closestScope = currentScope;
                } else if (position.getLine() == stop.lineNumber) {
                    if (position.getCharacter() <= stop.column) {
                        closestScope = currentScope;
                    }
                }
            }
        } else if (position.getLine() == startLineNumber) {
            if (position.getCharacter() >= startColumn) {
                if (stop.lineNumber != -1 && stop.column != -1) {
                    if (position.getLine() < stop.lineNumber) {
                        closestScope = currentScope;
                    } else if (position.getLine() == stop.lineNumber) {
                        if (position.getCharacter() <= stop.column) {
                            closestScope = currentScope;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        openScope(blockStmt);

        checkAndSetClosestScope(blockStmt);

        for (int stmtIndex = 0; stmtIndex < blockStmt.getStatements().length; stmtIndex++) {
            Statement stmt = blockStmt.getStatements()[stmtIndex];

            stmt.accept(this);

            if (stmt.isAlwaysReturns()) {
                blockStmt.setAlwaysReturns(true);
            }
        }

        closeScope();
    }

    private static class Position {
        int lineNumber;
        int column;
    }

    private void getStopPosition(Node node, Position stopPosition) {
        NodeLocation location = node.getNodeLocation();
        if (location != null) {
            int stopLineNumber = location.stopLineNumber;
            int stopColumn = location.stopColumn;
            if (stopLineNumber == -1) {
                if (node instanceof SymbolScope) {
                    SymbolScope enclosingScope = ((SymbolScope) node).getEnclosingScope();
                    if (enclosingScope instanceof Node) {
                        Node parent = (Node) enclosingScope;
                        getStopPosition(parent, stopPosition);
                    }
                }
            } else {
                stopPosition.lineNumber = stopLineNumber;
                stopPosition.column = stopColumn;
            }
        }
        return;
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        checkAndSetClosestScope(ifElseStmt);
        boolean stmtReturns = true;
        Expression expr = ifElseStmt.getCondition();
        visitSingleValueExpr(expr);

        Statement thenBody = ifElseStmt.getThenBody();
        thenBody.accept(this);

        stmtReturns &= thenBody.isAlwaysReturns();

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            visitSingleValueExpr(elseIfCondition);

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
        checkAndSetClosestScope(whileStmt);
        Expression expr = whileStmt.getCondition();
        visitSingleValueExpr(expr);
        BlockStmt blockStmt = whileStmt.getBody();
        blockStmt.accept(this);
    }

    @Override
    public void visit(BreakStmt breakStmt) {

    }

    @Override
    public void visit(ContinueStmt continueStmt) {

    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        checkAndSetClosestScope(tryCatchStmt);
        tryCatchStmt.getTryBlock().accept(this);

        for (TryCatchStmt.CatchBlock catchBlock : tryCatchStmt.getCatchBlocks()) {
            catchBlock.getParameterDef().setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            catchBlock.getParameterDef().accept(this);
            catchBlock.getCatchBlockStmt().accept(this);
        }

        if (tryCatchStmt.getFinallyBlock() != null) {
            tryCatchStmt.getFinallyBlock().getFinallyBlockStmt().accept(this);
        }
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        checkAndSetClosestScope(throwStmt);
        throwStmt.getExpr().accept(this);

    }

    @Override
    public void visit(FunctionInvocationStmt functionInvocationStmt) {
        checkAndSetClosestScope(functionInvocationStmt);
        functionInvocationStmt.getFunctionInvocationExpr().accept(this);
    }

    @Override
    public void visit(ActionInvocationStmt actionInvocationStmt) {
        checkAndSetClosestScope(actionInvocationStmt);
        actionInvocationStmt.getActionInvocationExpr().accept(this);
    }

    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {
        checkAndSetClosestScope(workerInvocationStmt);
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        checkAndSetClosestScope(workerReplyStmt);
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        boolean stmtReturns = true;
        //open the fork join statement scope
        openScope(forkJoinStmt);

        checkAndSetClosestScope(forkJoinStmt);

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
        if (parameter != null) {
            parameter.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            parameter.accept(this);
            join.define(parameter.getSymbolName(), parameter);

            // TODO: Join statement's parameter type is null when the parameter name is map. For the moment, instead of
            // type check we check the type name
            if (!(parameter.getTypeName().getName().equals("map"))) {
                throw new SemanticException("Incompatible types: expected map in " +
                        parameter.getNodeLocation().getFileName() + ":" + parameter.getNodeLocation().
                        getLineNumber());
            }

        }

        // Visit join body
        Statement joinBody = join.getJoinBlock();
        if (joinBody != null) {
            joinBody.accept(this);
            stmtReturns &= joinBody.isAlwaysReturns();
        }
        closeScope();

        // Visit timeout condition
        ForkJoinStmt.Timeout timeout = forkJoinStmt.getTimeout();
        openScope(timeout);
        Expression timeoutExpr = timeout.getTimeoutExpression();
        if (timeoutExpr != null) {
            timeoutExpr.accept(this);
        }

        ParameterDef timeoutParam = timeout.getTimeoutResult();
        if (timeoutParam != null) {
            timeoutParam.setMemoryLocation(new StackVarLocation(++stackFrameOffset));
            timeoutParam.accept(this);
            timeout.define(timeoutParam.getSymbolName(), timeoutParam);

            if (!(parameter.getType() instanceof BMapType)) {
                throw new SemanticException("Incompatible types: expected map in " +
                        parameter.getNodeLocation().getFileName() + ":" + parameter.getNodeLocation().getLineNumber());
            }
        }

        // Visit timeout body
        Statement timeoutBody = timeout.getTimeoutBlock();
        if (timeoutBody != null) {
            timeoutBody.accept(this);
            stmtReturns &= timeoutBody.isAlwaysReturns();
        }

        resolveForkJoin(forkJoinStmt);
        closeScope();

        forkJoinStmt.setAlwaysReturns(stmtReturns);

        //closing the fork join statement scope
        closeScope();

    }

    @Override
    public void visit(TransactionStmt transactionStmt) {
        checkAndSetClosestScope(transactionStmt);

        transactionStmt.getTransactionBlock().accept(this);
        TransactionStmt.AbortedBlock abortedBlock = transactionStmt.getAbortedBlock();
        if (abortedBlock != null) {
            abortedBlock.getAbortedBlockStmt().accept(this);
        }
        TransactionStmt.CommittedBlock committedBlock = transactionStmt.getCommittedBlock();
        if (committedBlock != null) {
            committedBlock.getCommittedBlockStmt().accept(this);
        }
    }

    @Override
    public void visit(AbortStmt abortStmt) {

    }

    @Override
    public void visit(NamespaceDeclarationStmt namespaceDeclarationStmt) {
        checkAndSetClosestScope(namespaceDeclarationStmt);

        NamespaceDeclaration namespaceDeclaration = namespaceDeclarationStmt.getNamespaceDclr();

        SymbolName symbolName = new SymbolName(namespaceDeclaration.getName(), currentPkg);
        currentScope.define(symbolName, namespaceDeclaration);
    }

    @Override
    public void visit(NamespaceDeclaration namespaceDclr) {
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        checkAndSetClosestScope(replyStmt);

        Expression replyExpr = replyStmt.getReplyExpr();
        if (replyExpr != null) {
            visitSingleValueExpr(replyExpr);
        }
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        checkAndSetClosestScope(returnStmt);

        // Expressions that this return statement contains.
        Expression[] returnArgExprs = returnStmt.getExprs();

        for (int i = 0; i < returnArgExprs.length; i++) {
            Expression returnArgExpr = returnArgExprs[i];
            returnArgExpr.accept(this);
        }
    }

    @Override
    public void visit(TransformStmt transformStmt) {
        checkAndSetClosestScope(transformStmt);
        BlockStmt blockStmt = transformStmt.getBody();
        blockStmt.accept(this);
    }

    // Expressions

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        checkAndSetClosestScope(instanceCreationExpr);

        visitSingleValueExpr(instanceCreationExpr);

        // TODO here the type shouldn't be a value type
//        Expression expr = instanceCreationExpr.getRExpr();
//        expr.accept(this);

    }

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        checkAndSetClosestScope(funcIExpr);

        Expression[] exprs = funcIExpr.getArgExprs();
        for (Expression expr : exprs) {
            visitSingleValueExpr(expr);
        }
    }

    // TODO Duplicate code. fix me
    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        checkAndSetClosestScope(actionIExpr);
        Expression[] exprs = actionIExpr.getArgExprs();
        for (Expression expr : exprs) {
            visitSingleValueExpr(expr);
        }
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
        checkAndSetClosestScope(basicLiteral);
        BType bType = BTypes.resolveType(basicLiteral.getTypeName(), currentScope, basicLiteral.getNodeLocation());
        basicLiteral.setType(bType);
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        checkAndSetClosestScope(divideExpr);
    }

    @Override
    public void visit(ModExpression modExpr) {
        checkAndSetClosestScope(modExpr);
    }

    @Override
    public void visit(UnaryExpression unaryExpr) {
        checkAndSetClosestScope(unaryExpr);
        visitSingleValueExpr(unaryExpr.getRExpr());
        unaryExpr.setType(unaryExpr.getRExpr().getType());
    }

    @Override
    public void visit(AddExpression addExpr) {
        checkAndSetClosestScope(addExpr);
    }

    @Override
    public void visit(MultExpression multExpr) {
        checkAndSetClosestScope(multExpr);
    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        checkAndSetClosestScope(subtractExpr);
    }

    @Override
    public void visit(AndExpression andExpr) {
        checkAndSetClosestScope(andExpr);
        visitBinaryLogicalExpr(andExpr);
    }

    @Override
    public void visit(OrExpression orExpr) {
        checkAndSetClosestScope(orExpr);
        visitBinaryLogicalExpr(orExpr);
    }

    @Override
    public void visit(EqualExpression equalExpr) {
        checkAndSetClosestScope(equalExpr);
        verifyBinaryEqualityExprType(equalExpr);
    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {
        checkAndSetClosestScope(notEqualExpr);
        verifyBinaryEqualityExprType(notEqualExpr);
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {
        checkAndSetClosestScope(greaterEqualExpr);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        checkAndSetClosestScope(greaterThanExpr);
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        checkAndSetClosestScope(lessEqualExpr);
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        checkAndSetClosestScope(lessThanExpr);
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        checkAndSetClosestScope(refTypeInitExpr);
        visitMapJsonInitExpr(refTypeInitExpr);
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        checkAndSetClosestScope(mapInitExpr);
        visitMapJsonInitExpr(mapInitExpr);
    }

    @Override
    public void visit(JSONInitExpr jsonInitExpr) {
        checkAndSetClosestScope(jsonInitExpr);
        visitMapJsonInitExpr(jsonInitExpr);
    }

    @Override
    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {
        checkAndSetClosestScope(jsonArrayInitExpr);
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
                }
                continue;
            }

            if (argExprType != BTypes.typeNull && isAssignableTo(BTypes.typeJSON, argExprType)) {
                continue;
            }

            TypeCastExpression typeCastExpr = checkWideningPossible(BTypes.typeJSON, argExpr);
            argExprs[i] = typeCastExpr;
        }
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        checkAndSetClosestScope(connectorInitExpr);
        BType inheritedType = connectorInitExpr.getInheritedType();
        connectorInitExpr.setType(inheritedType);

        for (Expression argExpr : connectorInitExpr.getArgExprs()) {
            visitSingleValueExpr(argExpr);
        }

    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        checkAndSetClosestScope(arrayInitExpr);
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

            SimpleVarRefExpr varRefExpr = (SimpleVarRefExpr) keyExpr;
            //TODO fix properly package conflict
            BLangSymbol varDefSymbol = structDef.resolveMembers(new SymbolName(varRefExpr.getSymbolName().getName(),
                    structDef.getPackagePath()));

            VariableDef varDef = (VariableDef) varDefSymbol;
            varRefExpr.setVariableDef(varDef);

            BType structFieldType = varDef.getType();
            Expression valueExpr = keyValueExpr.getValueExpr();
            if (valueExpr instanceof RefTypeInitExpr) {
                valueExpr = getNestedInitExpr(valueExpr, structFieldType);
                keyValueExpr.setValueExpr(valueExpr);
            }

            valueExpr.accept(this);
        }
    }

    @Override
    public void visit(KeyValueExpr keyValueExpr) {

    }

    @Override
    public void visit(SimpleVarRefExpr simpleVarRefExpr) {

    }

    @Override
    public void visit(FieldBasedVarRefExpr fieldBasedVarRefExpr) {
        VariableReferenceExpr varRefExpr = fieldBasedVarRefExpr.getVarRefExpr();
        varRefExpr.accept(this);
    }

    @Override
    public void visit(IndexBasedVarRefExpr indexBasedVarRefExpr) {
        Expression indexExpr = indexBasedVarRefExpr.getIndexExpr();
        indexExpr.accept(this);

        VariableReferenceExpr varRefExpr = indexBasedVarRefExpr.getVarRefExpr();
        varRefExpr.accept(this);
    }

    @Override
    public void visit(XMLAttributesRefExpr xmlAttributesRefExpr) {
        Expression indexExpr = xmlAttributesRefExpr.getIndexExpr();
        indexExpr.accept(this);

        VariableReferenceExpr variableReferenceExpr = xmlAttributesRefExpr.getVarRefExpr();
        variableReferenceExpr.accept(this);
    }

    @Override
    public void visit(XMLQNameExpr xmlQNameRefExpr) {

    }

    @Override
    public void visit(TypeCastExpression typeCastExpr) {
        checkAndSetClosestScope(typeCastExpr);
        Expression rExpr = typeCastExpr.getRExpr();
        visitSingleValueExpr(rExpr);

    }


    @Override
    public void visit(TypeConversionExpr typeConversionExpr) {
        checkAndSetClosestScope(typeConversionExpr);
        Expression rExpr = typeConversionExpr.getRExpr();
        visitSingleValueExpr(rExpr);
    }

    @Override
    public void visit(NullLiteral nullLiteral) {
        checkAndSetClosestScope(nullLiteral);
        nullLiteral.setType(BTypes.typeNull);
    }


    // Private methods.

    private void openScope(SymbolScope symbolScope) {
        currentScope = symbolScope;
    }

    private void closeScope() {
        currentScope = currentScope.getEnclosingScope();
    }

    private void visitBinaryExpr(BinaryExpression expr) {
        visitSingleValueExpr(expr.getLExpr());
        visitSingleValueExpr(expr.getRExpr());
    }

    private void visitSingleValueExpr(Expression expr) {
        expr.accept(this);
    }

    private void verifyBinaryEqualityExprType(BinaryExpression binaryExpr) {
        visitBinaryExpr(binaryExpr);

        binaryExpr.setType(BTypes.typeBoolean);
    }

    private void visitBinaryLogicalExpr(BinaryLogicalExpression expr) {
        visitBinaryExpr(expr);

        Expression rExpr = expr.getRExpr();
        Expression lExpr = expr.getLExpr();

        if (lExpr.getType() == BTypes.typeBoolean && rExpr.getType() == BTypes.typeBoolean) {
            expr.setType(BTypes.typeBoolean);
        }
    }

    private void checkForConstAssignment(AssignStmt assignStmt, Expression lExpr) {
    }

    private void visitLExprsOfAssignment(AssignStmt assignStmt, Expression[] lExprs) {
        // Handle special case for assignment statement declared with var
        if (assignStmt.isDeclaredWithVar()) {
            // This set data structure is used to check for repeated variable names in the assignment statement
            for (Expression expr : lExprs) {

                SimpleVarRefExpr refExpr = (SimpleVarRefExpr) expr;
                String varName = refExpr.getVarName();

                Identifier identifier = new Identifier(varName);
                SymbolName symbolName = new SymbolName(identifier.getName());
                VariableDef variableDef = new VariableDef(refExpr.getNodeLocation(),
                        refExpr.getWhiteSpaceDescriptor(), identifier,
                        null, symbolName, currentScope);

                // Check whether this variable is already defined, if not define it.
                SymbolName varDefSymName = new SymbolName(variableDef.getName(), currentPkg);
                currentScope.define(varDefSymName, variableDef);
                // Set memory location
                setMemoryLocation(variableDef);
            }
        }

        int ignoredVarCount = 0;
        for (Expression lExpr : lExprs) {
            if (lExpr instanceof SimpleVarRefExpr && ((SimpleVarRefExpr) lExpr).getVarName().equals("_")) {
                ignoredVarCount++;
                continue;
            }

            // First mark all left side ArrayMapAccessExpr. This is to skip some processing which is applicable only
            // for right side expressions.
            ((VariableReferenceExpr) lExpr).setLHSExpr(true);
            lExpr.accept(this);

            // Check whether someone is trying to change the values of a constant
            checkForConstAssignment(assignStmt, lExpr);
        }
    }

    private TypeCastExpression checkWideningPossible(BType lhsType, Expression rhsExpr) {
        TypeCastExpression typeCastExpr = null;
        BType rhsType = rhsExpr.getType();

        TypeEdge typeEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(rhsType, lhsType, null);
        if (typeEdge != null) {
            typeCastExpr = new TypeCastExpression(rhsExpr.getNodeLocation(),
                    rhsExpr.getWhiteSpaceDescriptor(), rhsExpr, lhsType);
            typeCastExpr.setOpcode(typeEdge.getOpcode());
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
            currentScope.define(connectorSymbolName, connectorDef);
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
        SymbolName symbolName = new SymbolName("");
        action.setSymbolName(symbolName);

        currentScope.define(symbolName, action);

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
        SymbolName symbolName = new SymbolName("");
        resource.setSymbolName(symbolName);

        currentScope.define(symbolName, resource);
    }

    private void defineStructs(StructDef[] structDefs) {
        for (StructDef structDef : structDefs) {

            SymbolName symbolName = new SymbolName(structDef.getName(), structDef.getPackagePath());
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
        } else {
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
            if (keyExpr instanceof SimpleVarRefExpr) {
                BString key = new BString(((SimpleVarRefExpr) keyExpr).getVarName());
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

    private void checkAndAddReturnStmt(int returnParamCount, BlockStmt blockStmt) {
        if (returnParamCount != 0) {
            return;
        }

        Statement[] statements = blockStmt.getStatements();
        int length = statements.length;
        if (length > 0) {
            Statement lastStatement = statements[length - 1];
            if (!(lastStatement instanceof ReturnStmt)) {
                NodeLocation location = lastStatement.getNodeLocation();
                ReturnStmt returnStmt = new ReturnStmt(
                        new NodeLocation(location.getFileName(),
                                location.getLineNumber() + 1), null, new Expression[0]);
                statements = Arrays.copyOf(statements, length + 1);
                statements[length] = returnStmt;
                blockStmt.setStatements(statements);
            }
        } else {
            NodeLocation location = blockStmt.getNodeLocation();
            ReturnStmt returnStmt = new ReturnStmt(
                    new NodeLocation(location.getFileName(),
                            location.getLineNumber() + 1), null, new Expression[0]);
            statements = Arrays.copyOf(statements, length + 1);
            statements[length] = returnStmt;
            blockStmt.setStatements(statements);
        }
    }

    public SymbolScope getClosestScope() {
        return closestScope;
    }

    /**
     * This class holds the results of the type assignability check.
     *
     * @since 0.88
     */
    static class AssignabilityResult {
    }
}

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
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
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
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.Identifier;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.Node;
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
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
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
import org.ballerinalang.model.statements.TransactionStmt;
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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.runtime.worker.WorkerDataChannel;
import org.ballerinalang.util.codegen.InstructionCodes;
import org.ballerinalang.util.exceptions.LinkerException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangPrograms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Position position;

    private SymbolScope closestScope;

    public CompletionItemAccumulator(List completionItems, Position position) {
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
        checkAndSetClosestScope(globalVarDef);
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

        resolveWorkerInteractions(resource);

        int sizeOfStackFrame = stackFrameOffset + 1;
        resource.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        stackFrameOffset = -1;
        currentCallableUnit = null;
        closeScope();
    }

    private void buildWorkerInteractions(CallableUnit callableUnit, Worker[] workers, boolean isWorkerInWorker,
                                         boolean isForkJoinStmt) {
        // This map holds the worker data channels against the respective source and target workers
        Map<String, WorkerDataChannel> workerDataChannels = new HashMap<>();
        boolean statementCompleted = false;
        List<Statement> processedStatements = new ArrayList<>();

        if (callableUnit.getWorkerInteractionStatements() != null &&
                !callableUnit.getWorkerInteractionStatements().isEmpty()) {
            String sourceWorkerName;
            String targetWorkerName;
            for (Statement statement : callableUnit.getWorkerInteractionStatements()) {
                if (statement instanceof WorkerInvocationStmt) {
                    targetWorkerName = ((WorkerInvocationStmt) statement).getName();
                    if (targetWorkerName == "fork" && isForkJoinStmt) {
                        break;
                    }
                    if (callableUnit instanceof Worker) {
                        sourceWorkerName = callableUnit.getName();
                    } else {
                        sourceWorkerName = "default";
                    }
                    // Find a matching worker reply statment
                    for (Worker worker : workers) {
                        if (worker.getWorkerInteractionStatements().peek() instanceof WorkerReplyStmt) {
                            String complimentSourceWorkerName = ((WorkerReplyStmt) worker.
                                    getWorkerInteractionStatements().peek()).getWorkerName();
                            String complimentTargetWorkerName = worker.getName();
                            if (sourceWorkerName.equals(complimentSourceWorkerName)
                                    && targetWorkerName.equals(complimentTargetWorkerName)) {
                                // Statements are matching for their names. Check the parameters
                                // Check for number of variables send and received
                                Expression[] invokeParams = ((WorkerInvocationStmt) statement).getExpressionList();
                                Expression[] receiveParams = ((WorkerReplyStmt) worker.
                                        getWorkerInteractionStatements().peek()).getExpressionList();
                                if (invokeParams.length != receiveParams.length) {
                                    break;
                                } else {
                                    int i = 0;
                                    for (Expression invokeParam : invokeParams) {
                                        if (!(receiveParams[i++].getType().equals(invokeParam.getType()))) {
                                            break;
                                        }
                                    }
                                }
                                // Nothing wrong with the statements. Now create the data channel and pop the statement.
                                String interactionName = sourceWorkerName + "->" + targetWorkerName;
                                WorkerDataChannel workerDataChannel;
                                if (!workerDataChannels.containsKey(interactionName)) {
                                    workerDataChannel = new
                                            WorkerDataChannel(sourceWorkerName, targetWorkerName);
                                    workerDataChannels.put(interactionName, workerDataChannel);
                                } else {
                                    workerDataChannel = workerDataChannels.get(interactionName);
                                }

                                ((WorkerInvocationStmt) statement).setWorkerDataChannel(workerDataChannel);
                                ((WorkerReplyStmt) worker.getWorkerInteractionStatements().peek()).
                                        setWorkerDataChannel(workerDataChannel);
                                ((WorkerReplyStmt) worker.getWorkerInteractionStatements().peek()).
                                        setEnclosingCallableUnitName(callableUnit.getName());
                                ((WorkerInvocationStmt) statement).setEnclosingCallableUnitName(callableUnit.getName());
                                ((WorkerInvocationStmt) statement).setPackagePath(callableUnit.getPackagePath());
                                worker.getWorkerInteractionStatements().remove();
                                processedStatements.add(statement);
                                statementCompleted = true;
                                break;
                            }
                        }
                    }
                } else {
                    sourceWorkerName = ((WorkerReplyStmt) statement).getWorkerName();
                    if (callableUnit instanceof Worker) {
                        targetWorkerName = callableUnit.getName();
                    } else {
                        targetWorkerName = "default";
                    }
                    // Find a matching worker invocation statment
                    for (Worker worker : callableUnit.getWorkers()) {
                        if (worker.getWorkerInteractionStatements().peek() instanceof WorkerInvocationStmt) {
                            String complimentTargetWorkerName = ((WorkerInvocationStmt) worker.
                                    getWorkerInteractionStatements().peek()).getName();
                            String complimentSourceWorkerName = worker.getName();
                            if (sourceWorkerName.equals(complimentSourceWorkerName) &&
                                    targetWorkerName.equals(complimentTargetWorkerName)) {
                                // Statements are matching for their names. Check the parameters
                                // Check for number of variables send and received
                                Expression[] invokeParams = ((WorkerReplyStmt) statement).getExpressionList();
                                Expression[] receiveParams = ((WorkerInvocationStmt) worker.
                                        getWorkerInteractionStatements().peek()).getExpressionList();
                                if (invokeParams.length != receiveParams.length) {
                                    break;
                                } else {
                                    int i = 0;
                                    for (Expression invokeParam : invokeParams) {
                                        if (!(receiveParams[i++].getType().equals(invokeParam.getType()))) {
                                            break;
                                        }
                                    }
                                }
                                // Nothing wrong with the statements. Now create the data channel and pop the statement.
                                String interactionName = sourceWorkerName + "->" + targetWorkerName;
                                WorkerDataChannel workerDataChannel;
                                if (!workerDataChannels.containsKey(interactionName)) {
                                    workerDataChannel = new
                                            WorkerDataChannel(sourceWorkerName, targetWorkerName);
                                    workerDataChannels.put(interactionName, workerDataChannel);
                                } else {
                                    workerDataChannel = workerDataChannels.get(interactionName);
                                }

                                ((WorkerReplyStmt) statement).setWorkerDataChannel(workerDataChannel);
                                ((WorkerInvocationStmt) worker.getWorkerInteractionStatements().peek()).
                                        setWorkerDataChannel(workerDataChannel);
                                ((WorkerInvocationStmt) worker.getWorkerInteractionStatements().peek()).
                                        setEnclosingCallableUnitName(callableUnit.getName());
                                ((WorkerReplyStmt) statement).setEnclosingCallableUnitName(callableUnit.getName());
                                ((WorkerReplyStmt) statement).setPackagePath(callableUnit.getPackagePath());
                                worker.getWorkerInteractionStatements().remove();
                                processedStatements.add(statement);
                                statementCompleted = true;
                                break;
                            }
                        }
                    }
                }


            }
            callableUnit.getWorkerInteractionStatements().removeAll(processedStatements);
        }
    }

    private void resolveWorkerInteractions(CallableUnit callableUnit) {
        //CallableUnit callableUnit = function;
        boolean isWorkerInWorker = callableUnit instanceof Worker;
        boolean isForkJoinStmt = callableUnit instanceof ForkJoinStmt;
        Worker[] workers = callableUnit.getWorkers();
        if (workers.length > 0) {
            Worker[] tempWorkers = new Worker[workers.length];
            System.arraycopy(workers, 0, tempWorkers, 0, tempWorkers.length);
            int i = 0;
            do {
                buildWorkerInteractions(callableUnit, tempWorkers, isWorkerInWorker, isForkJoinStmt);
                callableUnit = workers[i];
                i++;
                System.arraycopy(workers, i, tempWorkers, 0, workers.length - i);
            } while (i < workers.length);
        }
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

        resolveWorkerInteractions(function);

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
        resolveWorkerInteractions(action);

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
        checkAndSetClosestScope(worker);
        // Open a new symbol scope. This is done manually to avoid falling back to package scope
        parentScope.push(currentScope);
        currentScope = worker;
        parentCallableUnit.push(currentCallableUnit);
        currentCallableUnit = worker;

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

    /**
     * Visit and validate attributes of an annotation attachment.
     *
     * @param annotation    Annotation attachment to validate attributes
     * @param annotationDef Definition of the annotation
     */
    private void validateAttributes(AnnotationAttachment annotation, AnnotationDef annotationDef) {
        annotation.getAttributeNameValuePairs().forEach((attributeName, attributeValue) -> {
            // Check attribute existence
            BLangSymbol attributeSymbol = annotationDef.resolveMembers(new SymbolName(attributeName));

            // Check types
            AnnotationAttributeDef attributeDef = ((AnnotationAttributeDef) attributeSymbol);
            SimpleTypeName attributeType = attributeDef.getTypeName();
            SimpleTypeName valueType = attributeValue.getType();
            BLangSymbol valueTypeSymbol = currentScope.resolve(valueType.getSymbolName());
            BLangSymbol attributeTypeSymbol = annotationDef.resolve(new SymbolName(attributeType.getName(),
                    attributeType.getPackagePath()));

            if (attributeType.isArrayType()) {

                AnnotationAttributeValue[] valuesArray = attributeValue.getValueArray();
                for (AnnotationAttributeValue value : valuesArray) {
                    valueTypeSymbol = currentScope.resolve(value.getType().getSymbolName());

                    // If the value of the attribute is another annotation, then recursively
                    // traverse to its attributes and validate
                    AnnotationAttachment childAnnotation = value.getAnnotationValue();
                    if (childAnnotation != null && valueTypeSymbol instanceof AnnotationDef) {
                        validateAttributes(childAnnotation, (AnnotationDef) valueTypeSymbol);
                    }
                }
            } else {
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
     * @param annotation    Annotation attachment to populate default values
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
        checkAndSetClosestScope(annotationAttributeDef);
        SimpleTypeName fieldType = annotationAttributeDef.getTypeName();
        BasicLiteral fieldVal = annotationAttributeDef.getAttributeValue();

        if (fieldVal != null) {
            fieldVal.accept(this);
        } else {
            BLangSymbol typeSymbol;
            if (fieldType.isArrayType()) {
                typeSymbol = currentScope.resolve(new SymbolName(fieldType.getName(), fieldType.getPackagePath()));
            } else {
                typeSymbol = currentScope.resolve(fieldType.getSymbolName());
            }

            if (!(typeSymbol instanceof BType)) {
                fieldType.setPkgPath(annotationAttributeDef.getPackagePath());
            }
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
    }

    @Override
    public void visit(ParameterDef paramDef) {
        checkAndSetClosestScope(paramDef);

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
        checkAndSetClosestScope(varDefStmt);
        // Resolves the type of the variable
        VariableDef varDef = varDefStmt.getVariableDef();
        BType lhsType = BTypes.resolveType(varDef.getTypeName(), currentScope, varDef.getNodeLocation());
        varDef.setType(lhsType);

        // Mark the this variable references as LHS expressions
        ((ReferenceExpr) varDefStmt.getLExpr()).setLHSExpr(true);

        // Check whether this variable is already defined, if not define it.
        SymbolName symbolName = new SymbolName(varDef.getName(), currentPkg);

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
            checkForMultiAssignmentErrors(assignStmt, lExprs, (CallableUnitInvocationExpr) rExpr);
            return;
        }

        if (lExprs.length > 1 && (rExpr instanceof TypeCastExpression || rExpr instanceof TypeConversionExpr)) {
            ((AbstractExpression) rExpr).setMultiReturnAvailable(true);
            rExpr.accept(this);
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

    }

    private void checkAndSetClosestScope(Node node) {
        org.ballerinalang.model.values.Position start = node.getNodeLocation().getStartPosition();
        org.ballerinalang.model.values.Position stop = new org.ballerinalang.model.values.Position();
        getStopPosition(node, stop);

        if (position.getLine() > start.getLineNumber()) {
            if (stop.getLineNumber() != -1 && stop.getColumn() != -1) {
                if (position.getLine() < stop.getLineNumber()) {
                    closestScope = currentScope;
                } else if (position.getLine() == stop.getLineNumber()) {
                    if (position.getCharacter() <= stop.getColumn()) {
                        closestScope = currentScope;
                    }
                }
            }
        } else if (position.getLine() == start.getLineNumber()) {
            if (position.getCharacter() >= start.getColumn()) {
                if (stop.getLineNumber() != -1 && stop.getColumn() != -1) {
                    if (position.getLine() < stop.getLineNumber()) {
                        closestScope = currentScope;
                    } else if (position.getLine() == stop.getLineNumber()) {
                        if (position.getCharacter() <= stop.getColumn()) {
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

    private void getStopPosition(Node node, org.ballerinalang.model.values.Position stopPosition) {
        NodeLocation location = node.getNodeLocation();
        if (location != null) {
            org.ballerinalang.model.values.Position stop = location.getStopPosition();
            if (stop.getLineNumber() == -1) {
                if (node instanceof SymbolScope) {
                    SymbolScope enclosingScope = ((SymbolScope) node).getEnclosingScope();
                    if (enclosingScope instanceof Node) {
                        Node parent = (Node) enclosingScope;
                        getStopPosition(parent, stopPosition);
                    }
                }
            } else {
                stopPosition.setLineNumber(stop.getLineNumber());
                stopPosition.setColumn(stop.getColumn());
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

            if (error.equals(expressionType) || TypeLattice.getExplicitCastLattice().getEdgeFromTypes
                    (expressionType, error, null) != null) {
                throwStmt.setAlwaysReturns(true);
                return;
            }
        }
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
        Expression[] expressions = workerInvocationStmt.getExpressionList();
        BType[] bTypes = new BType[expressions.length];
        int p = 0;
        for (Expression expression : expressions) {
            expression.accept(this);
            bTypes[p++] = expression.getType();
        }

        workerInvocationStmt.setTypes(bTypes);


        if (workerInvocationStmt.getCallableUnitName() != null &&
                !workerInvocationStmt.getCallableUnitName().equals("default") &&
                !workerInvocationStmt.getCallableUnitName().equals("fork")) {
            linkWorker(workerInvocationStmt);

            //Find the return types of this function invocation expression.
//            ParameterDef[] returnParams = workerInvocationStmt.getCallableUnit().getReturnParameters();
//            BType[] returnTypes = new BType[returnParams.length];
//            for (int i = 0; i < returnParams.length; i++) {
//                returnTypes[i] = returnParams[i].getType();
//            }
//            workerInvocationStmt.setTypes(returnTypes);
        }
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        checkAndSetClosestScope(workerReplyStmt);
        String workerName = workerReplyStmt.getWorkerName();
        SymbolName workerSymbol = new SymbolName(workerName);

        Expression[] expressions = workerReplyStmt.getExpressionList();
        BType[] bTypes = new BType[expressions.length];
        int p = 0;
        for (Expression expression : expressions) {
            expression.accept(this);
            bTypes[p++] = expression.getType();
        }

        workerReplyStmt.setTypes(bTypes);

        if (!workerName.equals("default")) {
            BLangSymbol worker = currentScope.resolve(workerSymbol);

            workerReplyStmt.setWorker((Worker) worker);
        }
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

            if (!(parameter.getType() instanceof BMapType)) {
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

        resolveWorkerInteractions(forkJoinStmt);
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

        }

        for (int i = 0; i < returnArgExprs.length; i++) {
            Expression returnArgExpr = returnArgExprs[i];
            returnArgExpr.accept(this);
        }

        // Now check whether this return contains a function invocation expression which returns multiple values
        if (returnArgExprs.length == 1 && returnArgExprs[0] instanceof FunctionInvocationExpr) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) returnArgExprs[0];
            // Return types of the function invocations expression
            BType[] funcIExprReturnTypes = funcIExpr.getTypes();

            for (int i = 0; i < returnParamsOfCU.length; i++) {
                BType lhsType = returnParamsOfCU[i].getType();
                BType rhsType = funcIExprReturnTypes[i];

                // Check whether the right-hand type can be assigned to the left-hand type.
                if (isAssignableTo(lhsType, rhsType)) {
                    continue;
                }
            }

            return;
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

        String pkgPath = actionIExpr.getPackagePath();
        String name = actionIExpr.getConnectorName();

        // First check action invocation happens on a variable def
        SymbolName symbolName = new SymbolName(name, pkgPath);
        BLangSymbol bLangSymbol = currentScope.resolve(symbolName);

        if (bLangSymbol instanceof VariableDef) {
            Expression[] exprs = new Expression[actionIExpr.getArgExprs().length + 1];
            VariableRefExpr variableRefExpr = new VariableRefExpr(actionIExpr.getNodeLocation(), null, symbolName);
            exprs[0] = variableRefExpr;
            for (int i = 0; i < actionIExpr.getArgExprs().length; i++) {
                exprs[i + 1] = actionIExpr.getArgExprs()[i];
            }
            actionIExpr.setArgExprs(exprs);
            VariableDef varDef = (VariableDef) bLangSymbol;
            actionIExpr.setConnectorName(varDef.getTypeName().getName());
            actionIExpr.setPackageName(varDef.getTypeName().getPackageName());
            actionIExpr.setPackagePath(varDef.getTypeName().getPackagePath());
        }

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
        checkAndSetClosestScope(basicLiteral);
        BType bType = BTypes.resolveType(basicLiteral.getTypeName(), currentScope, basicLiteral.getNodeLocation());
        basicLiteral.setType(bType);
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        checkAndSetClosestScope(divideExpr);
        BType binaryExprType = verifyBinaryArithmeticExprType(divideExpr);
        validateBinaryExprTypeForIntFloat(divideExpr, binaryExprType);
    }

    @Override
    public void visit(ModExpression modExpr) {
        checkAndSetClosestScope(modExpr);
        BType binaryExprType = verifyBinaryArithmeticExprType(modExpr);
        validateBinaryExprTypeForIntFloat(modExpr, binaryExprType);
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
        BType binaryExprType = verifyBinaryArithmeticExprType(multExpr);
        validateBinaryExprTypeForIntFloat(multExpr, binaryExprType);
    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        checkAndSetClosestScope(subtractExpr);
        BType binaryExprType = verifyBinaryArithmeticExprType(subtractExpr);
        validateBinaryExprTypeForIntFloat(subtractExpr, binaryExprType);
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
        BType compareExprType = verifyBinaryCompareExprType(greaterEqualExpr);
        validateBinaryExprTypeForIntFloat(greaterEqualExpr, compareExprType);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        checkAndSetClosestScope(greaterThanExpr);
        BType compareExprType = verifyBinaryCompareExprType(greaterThanExpr);
        validateBinaryExprTypeForIntFloat(greaterThanExpr, compareExprType);
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        checkAndSetClosestScope(lessEqualExpr);
        BType compareExprType = verifyBinaryCompareExprType(lessEqualExpr);
        validateBinaryExprTypeForIntFloat(lessEqualExpr, compareExprType);
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        checkAndSetClosestScope(lessThanExpr);
        BType compareExprType = verifyBinaryCompareExprType(lessThanExpr);
        validateBinaryExprTypeForIntFloat(lessThanExpr, compareExprType);
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        checkAndSetClosestScope(arrayMapAccessExpr);
        // Here we assume that rExpr of arrays access expression is always a variable reference expression.
        // This according to the grammar
        VariableRefExpr arrayMapVarRefExpr = (VariableRefExpr) arrayMapAccessExpr.getRExpr();
        arrayMapVarRefExpr.accept(this);

        handleArrayType(arrayMapAccessExpr);
    }

    @Override
    public void visit(FieldAccessExpr fieldAccessExpr) {
        checkAndSetClosestScope(fieldAccessExpr);
        visitField(fieldAccessExpr, currentScope);
    }

    @Override
    public void visit(JSONFieldAccessExpr jsonFieldExpr) {

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

        Expression[] argExprs = connectorInitExpr.getArgExprs();
        ParameterDef[] parameterDefs = ((BallerinaConnectorDef) inheritedType).getParameterDefs();
        for (int i = 0; i < argExprs.length; i++) {
            SimpleTypeName simpleTypeName = parameterDefs[i].getTypeName();
            BType paramType = BTypes.resolveType(simpleTypeName, currentScope, connectorInitExpr.getNodeLocation());
            parameterDefs[i].setType(paramType);
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
        checkAndSetClosestScope(structInitExpr);
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

            VariableRefExpr varRefExpr = (VariableRefExpr) keyExpr;
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
    public void visit(VariableRefExpr varRefExpr) {
        checkAndSetClosestScope(varRefExpr);
        SymbolName symbolName = varRefExpr.getSymbolName();

        // Check whether this symName is declared
        BLangSymbol varDefSymbol = currentScope.resolve(symbolName);
        varRefExpr.setVariableDef((VariableDef) varDefSymbol);
    }

    @Override
    public void visit(TypeCastExpression typeCastExpr) {
        checkAndSetClosestScope(typeCastExpr);
        // Evaluate the expression and set the type
        boolean isMultiReturn = typeCastExpr.isMultiReturnExpr();
        Expression rExpr = typeCastExpr.getRExpr();
        visitSingleValueExpr(rExpr);

        BType sourceType = rExpr.getType();
        BType targetType = typeCastExpr.getType();
        if (targetType == null) {
            targetType = BTypes.resolveType(typeCastExpr.getTypeName(), currentScope, typeCastExpr.getNodeLocation());
            typeCastExpr.setType(targetType);
        }

        // Find the eval function from explicit casting lattice
        TypeEdge newEdge = TypeLattice.getExplicitCastLattice().getEdgeFromTypes(sourceType, targetType, null);
        if (newEdge != null) {
            typeCastExpr.setOpcode(newEdge.getOpcode());

            // TODO 0.89 release
//            if (!newEdge.isSafe() && !isMultiReturn) {
//                BLangExceptionHelper.throwSemanticError(typeCastExpr, SemanticErrors.UNSAFE_CAST_ATTEMPT,
//                        sourceType, targetType);
//            }

            if (!isMultiReturn) {
                typeCastExpr.setTypes(new BType[]{targetType});
                return;
            }

        } else if (sourceType == targetType) {
            typeCastExpr.setOpcode(InstructionCodes.NOP);
            if (!isMultiReturn) {
                typeCastExpr.setTypes(new BType[]{targetType});
                return;
            }

        } else {
            boolean isUnsafeCastPossible = false;
            if (isMultiReturn) {
                isUnsafeCastPossible = checkUnsafeCastPossible(sourceType, targetType);
            }

            if (isUnsafeCastPossible) {
                typeCastExpr.setOpcode(InstructionCodes.CHECKCAST);
            }
        }

        // If this is a multi-value return conversion expression, set the return types.
        BLangSymbol error = currentScope.resolve(new SymbolName(BALLERINA_CAST_ERROR, ERRORS_PACKAGE));
        typeCastExpr.setTypes(new BType[]{targetType, (BType) error});
    }


    @Override
    public void visit(TypeConversionExpr typeConversionExpr) {
        checkAndSetClosestScope(typeConversionExpr);
        // Evaluate the expression and set the type
        boolean isMultiReturn = typeConversionExpr.isMultiReturnExpr();
        Expression rExpr = typeConversionExpr.getRExpr();
        visitSingleValueExpr(rExpr);

        BType sourceType = rExpr.getType();
        BType targetType = typeConversionExpr.getType();
        if (targetType == null) {
            targetType = BTypes.resolveType(typeConversionExpr.getTypeName(), currentScope, null);
            typeConversionExpr.setType(targetType);
        }

        // Find the eval function from the conversion lattice
        TypeEdge newEdge = TypeLattice.getTransformLattice().getEdgeFromTypes(sourceType, targetType, null);
        if (newEdge != null) {
            typeConversionExpr.setOpcode(newEdge.getOpcode());

            // TODO 0.89 release
//            if (!newEdge.isSafe() && !isMultiReturn) {
//                BLangExceptionHelper.throwSemanticError(typeCastExpr, SemanticErrors.UNSAFE_CAST_ATTEMPT,
//                        sourceType, targetType);
//            }

            if (!isMultiReturn) {
                typeConversionExpr.setTypes(new BType[]{targetType});
                return;
            }

        } else if (sourceType == targetType) {
            typeConversionExpr.setOpcode(InstructionCodes.NOP);
            if (!isMultiReturn) {
                typeConversionExpr.setTypes(new BType[]{targetType});
                return;
            }
        }

        // If this is a multi-value return conversion expression, set the return types.
        BLangSymbol error = currentScope.resolve(new SymbolName(BALLERINA_CONVERSION_ERROR, ERRORS_PACKAGE));
        typeConversionExpr.setTypes(new BType[]{targetType, (BType) error});
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

    private void handleArrayType(ArrayMapAccessExpr arrayMapAccessExpr) {
        ReferenceExpr arrayMapVarRefExpr = (ReferenceExpr) arrayMapAccessExpr.getRExpr();

        // Handle the arrays type
        if (arrayMapVarRefExpr.getType() instanceof BArrayType) {
            // Check the type of the index expression
            for (Expression indexExpr : arrayMapAccessExpr.getIndexExprs()) {
                visitSingleValueExpr(indexExpr);
            }
            // Set type of the arrays access expression
            BType expectedType = arrayMapVarRefExpr.getType();
            for (int i = 0; i < arrayMapAccessExpr.getIndexExprs().length; i++) {
                expectedType = ((BArrayType) expectedType).getElementType();
            }
            arrayMapAccessExpr.setType(expectedType);

        } else if (arrayMapVarRefExpr.getType() instanceof BMapType) {
            // Check the type of the index expression
            Expression indexExpr = arrayMapAccessExpr.getIndexExprs()[0];
            visitSingleValueExpr(indexExpr);
            // Set type of the map access expression
            BMapType typeOfMap = (BMapType) arrayMapVarRefExpr.getType();
            arrayMapAccessExpr.setType(typeOfMap.getElementType());

        }
    }

    private void visitBinaryExpr(BinaryExpression expr) {
        visitSingleValueExpr(expr.getLExpr());
        visitSingleValueExpr(expr.getRExpr());
    }

    private void visitSingleValueExpr(Expression expr) {
        expr.accept(this);
    }

    private void validateBinaryExprTypeForIntFloat(BinaryExpression binaryExpr, BType binaryExprType) {

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

    private void verifyBinaryEqualityExprType(BinaryExpression binaryExpr) {
        visitBinaryExpr(binaryExpr);

        binaryExpr.setType(BTypes.typeBoolean);
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
                    newExpr.accept(this);
                    binaryExpr.setRExpr(newExpr);
                    return lType;
                } else {
                    newEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(lType, rType, null);
                    if (newEdge != null) { // Implicit cast from left to right
                        newExpr = new TypeCastExpression(lExpr.getNodeLocation(), lExpr.getWhiteSpaceDescriptor(),
                                lExpr, rType);
                        newExpr.setOpcode(newEdge.getOpcode());
                        newExpr.accept(this);
                        binaryExpr.setLExpr(newExpr);
                        return rType;
                    }
                }
            }
        }
        return rType;
    }

    private void visitBinaryLogicalExpr(BinaryLogicalExpression expr) {
        visitBinaryExpr(expr);

        Expression rExpr = expr.getRExpr();
        Expression lExpr = expr.getLExpr();

        if (lExpr.getType() == BTypes.typeBoolean && rExpr.getType() == BTypes.typeBoolean) {
            expr.setType(BTypes.typeBoolean);
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
    }

    private void checkForMultiAssignmentErrors(AssignStmt assignStmt, Expression[] lExprs,
                                               CallableUnitInvocationExpr rExpr) {
        BType[] returnTypes = rExpr.getTypes();

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
        }
    }

    private void visitLExprsOfAssignment(AssignStmt assignStmt, Expression[] lExprs) {

        for (Expression lExpr : lExprs) {
            String varName = getVarNameFromExpression(lExpr);
            if (varName.equals("_")) {
                continue;
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
    }

    private void linkAction(ActionInvocationExpr actionIExpr) {
        String pkgPath = actionIExpr.getPackagePath();
        String connectorName = actionIExpr.getConnectorName();

        // First look for the connectors
        SymbolName connectorSymbolName = new SymbolName(connectorName, pkgPath);
        BLangSymbol connectorSymbol = currentScope.resolve(connectorSymbolName);


        //Expression[] exprs = actionIExpr.getArgExprs();
        //BType[] paramTypes = new BType[exprs.length];
        //for (int i = 0; i < exprs.length; i++) {
        //    paramTypes[i] = exprs[i].getType();
        //}

        // When getting the action symbol name, Package name for the action is set to null, since the action is
        // registered under connector, and connecter contains the package
        SymbolName actionSymbolName = new SymbolName("");

        // Now check whether there is a matching action
        BLangSymbol actionSymbol = null;
        if (connectorSymbol instanceof BallerinaConnectorDef) {
            actionSymbol = ((BallerinaConnectorDef) connectorSymbol).resolveMembers(actionSymbolName);
        }

        if ((actionSymbol instanceof BallerinaAction) && (actionSymbol.isNative())) {
            actionSymbol = ((BallerinaAction) actionSymbol).getNativeAction();
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

            action = (Action) nativeUnit;
            action.setReturnParamTypes(returnTypes);

        } else if (actionSymbol instanceof Action) {
            action = (Action) actionSymbol;
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

    private void visitField(FieldAccessExpr fieldAccessExpr, SymbolScope enclosingScope) {
        ReferenceExpr varRefExpr = (ReferenceExpr) fieldAccessExpr.getVarRef();

        BLangSymbol fieldSymbol;
        SymbolName symbolName = new SymbolName(varRefExpr.getVarName(), varRefExpr.getPkgPath());
        //TODO resolve package path conflict
        if (enclosingScope instanceof StructDef) {
            fieldSymbol = ((StructDef) enclosingScope).resolveMembers(new SymbolName(symbolName.getName(),
                    ((StructDef) enclosingScope).getPackagePath()));
        } else {
            fieldSymbol = enclosingScope.resolve(symbolName);
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
        } else if (exprType instanceof BJSONType) {
            visitJSONAccessExpr(fieldAccessExpr, fieldExpr);
        } else if (exprType instanceof BMapType) {
            visitMapAccessExpr(fieldAccessExpr, varRefExpr, fieldExpr, enclosingScope);
        } else if (exprType instanceof BArrayType) {
            visitArrayAccessExpr(fieldAccessExpr, varRefExpr, fieldExpr, exprType, enclosingScope);
        }
    }

    /**
     * Visit a struct and its fields and semantically validate the field expression.
     *
     * @param fieldExpr field expression to validate
     * @param exprType  Struct definition
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

        visitField(fieldExpr, ((StructDef) exprType));
    }

    /**
     * Visits a JSON access expression. Rewrites the tree by replacing the {@link FieldAccessExpr}
     * with a {@link JSONFieldAccessExpr}.
     *
     * @param parentExpr Current expression
     * @param fieldExpr  Field access expression of the current expression
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
     * @param parentExpr     Current expression
     * @param varRefExpr     VariableRefExpression of the current expression
     * @param fieldExpr      Field access expression of the current expression
     * @param enclosingScope Enclosing scope
     */
    private void visitMapAccessExpr(FieldAccessExpr parentExpr, ReferenceExpr varRefExpr, FieldAccessExpr fieldExpr,
                                    SymbolScope enclosingScope) {
        Expression fieldVar = fieldExpr.getVarRef();

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
     * @param parentExpr     Current expression
     * @param varRefExpr     VariableRefExpression of the current expression
     * @param fieldExpr      Field access expression of the current expression
     * @param exprType       Type to which the expression evaluates
     * @param enclosingScope Enclosing scope
     */
    private void visitArrayAccessExpr(FieldAccessExpr parentExpr, ReferenceExpr varRefExpr, FieldAccessExpr fieldExpr,
                                      BType exprType, SymbolScope enclosingScope) {

        if (fieldExpr.getVarRef() instanceof BasicLiteral) {
            String value = ((BasicLiteral) fieldExpr.getVarRef()).getBValue().stringValue();
            if (value.equals("length")) {

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

            if (function.isNative() && functionSymbol == null) {
                functionSymbol = nativeScope.resolve(symbolName);
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
            currentScope.define(connectorSymbolName, connectorDef);

            BLangSymbol actionSymbol;
            SymbolName name = new SymbolName("NativeAction." + connectorName
                    + ".<init>", connectorDef.getPackagePath());
            actionSymbol = nativeScope.resolve(name);
            if (actionSymbol != null) {
                if (actionSymbol instanceof NativeUnitProxy) {
                    AbstractNativeAction nativeUnit = (AbstractNativeAction) ((NativeUnitProxy) actionSymbol).load();
                    BallerinaAction.BallerinaActionBuilder ballerinaActionBuilder = new BallerinaAction
                            .BallerinaActionBuilder(connectorDef);
                    ballerinaActionBuilder.setIdentifier(nativeUnit.getIdentifier());
                    ballerinaActionBuilder.setPkgPath(nativeUnit.getPackagePath());
                    ballerinaActionBuilder.setNative(nativeUnit.isNative());
                    ballerinaActionBuilder.setSymbolName(nativeUnit.getSymbolName());
                    ParameterDef paramDef = new ParameterDef(connectorDef.getNodeLocation(), null,
                            new Identifier(nativeUnit.getArgumentNames()[0]),
                            nativeUnit.getArgumentTypeNames()[0],
                            new SymbolName(nativeUnit.getArgumentNames()[0], connectorDef.getPackagePath()),
                            ballerinaActionBuilder.getCurrentScope());
                    paramDef.setType(connectorDef);
                    ballerinaActionBuilder.addParameter(paramDef);
                    BallerinaAction ballerinaAction = ballerinaActionBuilder.buildAction();
                    ballerinaAction.setNativeAction((NativeUnitProxy) actionSymbol);
                    ballerinaAction.setConnectorDef(connectorDef);
                    BType bType = BTypes.resolveType(paramDef.getTypeName(), currentScope, paramDef.getNodeLocation());
                    ballerinaAction.setParameterTypes(new BType[]{bType});
                    connectorDef.setInitAction(ballerinaAction);
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
        SymbolName symbolName = new SymbolName("");
        action.setSymbolName(symbolName);

        currentScope.define(symbolName, action);

        if (action.isNative()) {
            SymbolName nativeActionSymName = new SymbolName("");
            BLangSymbol nativeAction = nativeScope.resolve(nativeActionSymName);

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
                    }
                }
                continue;
            }

            // for JSON init expr, check the type compatibility of the value.
            if (BTypes.isValueType(valueExprType)) {
                TypeCastExpression typeCastExpr = checkWideningPossible(BTypes.typeJSON, valueExpr);
                if (typeCastExpr != null) {
                    keyValueExpr.setValueExpr(typeCastExpr);
                }
                continue;
            }

            if (valueExprType != BTypes.typeNull && isAssignableTo(BTypes.typeJSON, valueExprType)) {
                continue;
            }

            TypeCastExpression typeCastExpr = checkWideningPossible(BTypes.typeJSON, valueExpr);
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
        if (targetType.getTag() == TypeTags.ARRAY_TAG || sourceType.getTag() == TypeTags.ARRAY_TAG) {
            return isUnsafeArrayCastPossible(sourceType, targetType);
        }

        return false;
    }

    private boolean isUnsafeArrayCastPossible(BType sourceType, BType targetType) {
        if (targetType.getTag() == TypeTags.ARRAY_TAG && sourceType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType sourceArrayType = (BArrayType) sourceType;
            BArrayType targetArrayType = (BArrayType) targetType;
            return isUnsafeArrayCastPossible(sourceArrayType.getElementType(), targetArrayType.getElementType());

        } else if (targetType.getTag() == TypeTags.ARRAY_TAG) {
            // If only the target type is an array type, then the source type must be of type 'any'
            return sourceType == BTypes.typeAny;

        } else if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
            // If only the source type is an array type, then the target type must be of type 'any'
            return targetType == BTypes.typeAny;
        }

        // Now both types are not array types
        if (sourceType == targetType) {
            return true;
        }

        // In this case, target type should be of type 'any' and the source type cannot be a value type
        if (targetType == BTypes.typeAny && !BTypes.isValueType(sourceType)) {
            return true;
        }

        return !BTypes.isValueType(targetType) && sourceType == BTypes.typeAny;
    }

    private boolean isImplicitArrayCastPossible(BType lhsType, BType rhsType) {
        if (lhsType.getTag() == TypeTags.ARRAY_TAG && rhsType.getTag() == TypeTags.ARRAY_TAG) {
            // Both types are array types
            BArrayType lhrArrayType = (BArrayType) lhsType;
            BArrayType rhsArrayType = (BArrayType) rhsType;
            return isImplicitArrayCastPossible(lhrArrayType.getElementType(), rhsArrayType.getElementType());

        } else if (rhsType.getTag() == TypeTags.ARRAY_TAG) {
            // Only the right-hand side is an array type
            // Then lhs type should 'any' type
            return lhsType == BTypes.typeAny;

        } else if (lhsType.getTag() == TypeTags.ARRAY_TAG) {
            // Only the left-hand side is an array type
            return false;
        }

        // Now both types are not array types
        if (lhsType == rhsType) {
            return true;
        }

        // In this case, lhs type should be of type 'any' and the rhs type cannot be a value type
        return lhsType.getTag() == BTypes.typeAny.getTag() && !BTypes.isValueType(rhsType);
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

    /**
     * This class holds the results of the type assignability check.
     *
     * @since 0.88
     */
    static class AssignabilityResult {
    }

    @Override
    public void visit(ArrayLengthExpression arrayLengthExpression) {
    }
}
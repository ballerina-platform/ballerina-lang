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
package org.ballerinalang.util.codegen;

import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.GlobalVarLocation;
import org.ballerinalang.bre.MemoryLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.model.Action;
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
import org.ballerinalang.model.CallableUnit;
import org.ballerinalang.model.CompilationUnit;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.ExecutableMultiReturnExpr;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.Identifier;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NamespaceDeclaration;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.Operator;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.SimpleVariableDef;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.BinaryArithmeticExpression;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.CallableUnitInvocationExpr;
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
import org.ballerinalang.model.expressions.LambdaExpression;
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
import org.ballerinalang.model.expressions.XMLCommentLiteral;
import org.ballerinalang.model.expressions.XMLElementLiteral;
import org.ballerinalang.model.expressions.XMLLiteral;
import org.ballerinalang.model.expressions.XMLPILiteral;
import org.ballerinalang.model.expressions.XMLQNameExpr;
import org.ballerinalang.model.expressions.XMLSequenceLiteral;
import org.ballerinalang.model.expressions.XMLTextLiteral;
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
import org.ballerinalang.model.statements.StatementKind;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TransactionStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BConnectorType;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BJSONConstraintType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeSignature;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.runtime.worker.WorkerDataChannel;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ErrorTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LineNumberTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParamAnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.VarTypeCountAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.ActionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.ConstantPool;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.ForkJoinCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.PackageRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.StructureRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.TypeRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;
import org.ballerinalang.util.codegen.cpentries.WorkerDataChannelRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.WrkrInteractionArgsCPEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.XMLConstants;

import static org.ballerinalang.util.BLangConstants.BLOB_OFFSET;
import static org.ballerinalang.util.BLangConstants.BOOL_OFFSET;
import static org.ballerinalang.util.BLangConstants.FLOAT_OFFSET;
import static org.ballerinalang.util.BLangConstants.INT_OFFSET;
import static org.ballerinalang.util.BLangConstants.REF_OFFSET;
import static org.ballerinalang.util.BLangConstants.STRING_OFFSET;

/**
 * Generates Ballerina bytecode instructions.
 *
 * @since 0.87
 */
public class CodeGenerator implements NodeVisitor {
    private int[] maxRegIndexes = {-1, -1, -1, -1, -1, -1};

    // This int array hold then current local variable index of following types
    // index 0 - int, 1 - float, 2 - string, 3 - boolean, 4 - reference(BValue)
    private int[] lvIndexes = {-1, -1, -1, -1, -1, -1};

    // This int array hold then current register index of following types
    // index 0 - int, 1 - float, 2 - string, 3 - boolean, 4 - reference(BValue)
    private int[] regIndexes = {-1, -1, -1, -1, -1, -1};

    // This int array hold then current register index of following types
    // index 0 - int, 1 - float, 2 - string, 3 - boolean, 4 - reference(BValue)
    private int[] fieldIndexes = {-1, -1, -1, -1, -1, -1};

    // Package level variable indexes. This includes package constants, package level variables and
    //  service level variables
    private int[] gvIndexes = {-1, -1, -1, -1, -1, -1};

    private ProgramFile programFile = new ProgramFile();
    private String currentPkgPath;
    private int currentPkgCPIndex = -1;
    private PackageInfo currentPkgInfo;
    private int baseConnectorIndex = -1;
    private int lastFilterConnectorIndex = -1;
    private ConnectorInfo baseConnectorInfo = null;

    private ServiceInfo currentServiceInfo;
    private WorkerInfo currentWorkerInfo;
    private LocalVariableAttributeInfo currentlLocalVarAttribInfo;
    private LineNumberTableAttributeInfo lineNumberTableAttributeInfo;
    private CallableUnitInfo currentCallableUnitInfo;
    private int workerChannelCount = 0;

    // Required variables to generate code for assignment statements
    private int rhsExprRegIndex = -1;
    private boolean varAssignment;
    private boolean arrayMapAssignment;
    private boolean structAssignment;

    private Stack<Instruction> breakInstructions = new Stack<>();
    private Stack<Instruction> continueInstructions = new Stack<>();
    private Stack<Instruction> abortInstructions = new Stack<>();

    public ProgramFile getProgramFile() {
        return programFile;
    }

    @Override
    public void visit(BLangProgram bLangProgram) {
        for (BLangPackage bLangPackage : bLangProgram.getPackages()) {
            bLangPackage.setSymbolsDefined(false);
        }

        BLangPackage entryPkg = bLangProgram.getEntryPackage();
        entryPkg.accept(this);

        String pkgName = entryPkg.getName();
        UTF8CPEntry mainPkgPathCPEntry = new UTF8CPEntry(pkgName);
        int pkgNameCPIndex = programFile.getCPEntryIndex(mainPkgPathCPEntry);
        PackageRefCPEntry packageRefCPEntry = new PackageRefCPEntry(pkgNameCPIndex, pkgName);
        int pkgCPIndex = programFile.addCPEntry(packageRefCPEntry);
        programFile.setEntryPkgCPIndex(pkgCPIndex);
        programFile.setEntryPkgName(pkgName);
        programFile.setEntryPackage(programFile.getPackageInfo(pkgName));

        // Add global variable indexes to the ProgramFile
        prepareIndexes(gvIndexes);

        // Create Global variable attribute info
        addVariableCountAttributeInfo(programFile, programFile, gvIndexes);
    }

    @Override
    public void visit(BLangPackage bLangPackage) {
        for (BLangPackage dependentPkg : bLangPackage.getDependentPackages()) {
            // TODO Validate the following logic
            if (dependentPkg.isSymbolsDefined()) {
                continue;
            }

            dependentPkg.accept(this);
        }

        currentPkgPath = bLangPackage.getPackagePath();
        UTF8CPEntry pkgPathCPEntry = new UTF8CPEntry(currentPkgPath);
        int pkgNameCPIndex = programFile.addCPEntry(pkgPathCPEntry);
        currentPkgInfo = new PackageInfo(pkgNameCPIndex, currentPkgPath);
        currentPkgInfo.setProgramFile(programFile);
        programFile.addPackageInfo(currentPkgPath, currentPkgInfo);

        // Insert the package reference to the constant pool of the Ballerina program
        PackageRefCPEntry packageRefCPEntry = new PackageRefCPEntry(pkgNameCPIndex, currentPkgPath);
        packageRefCPEntry.setPackageInfo(currentPkgInfo);
        programFile.addCPEntry(packageRefCPEntry);

        // Insert the package reference to current package's constant pool
        pkgNameCPIndex = currentPkgInfo.addCPEntry(pkgPathCPEntry);
        packageRefCPEntry = new PackageRefCPEntry(pkgNameCPIndex, currentPkgPath);
        packageRefCPEntry.setPackageInfo(currentPkgInfo);
        currentPkgCPIndex = currentPkgInfo.addCPEntry(packageRefCPEntry);

        // Create lineNumberTableAttributeInfo object to collect line number details.
        UTF8CPEntry lineNumberAttribUTF8CPEntry = new UTF8CPEntry(
                AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE.toString());
        int lineNumberAttribNameIndex = currentPkgInfo.addCPEntry(lineNumberAttribUTF8CPEntry);
        lineNumberTableAttributeInfo = new LineNumberTableAttributeInfo(lineNumberAttribNameIndex);

        visitConstants(bLangPackage.getConsts());
        visitGlobalVariables(bLangPackage.getGlobalVariables());
        createStructInfoEntries(bLangPackage.getStructDefs());
        createConnectorInfoEntries(bLangPackage.getConnectors());
        createServiceInfoEntries(bLangPackage.getServices());
        createFunctionInfoEntries(bLangPackage.getFunctions());

        // Create function info for the package function
        BallerinaFunction pkgInitFunction = bLangPackage.getInitFunction();
        createFunctionInfoEntries(new Function[]{pkgInitFunction});

        for (CompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
            compilationUnit.accept(this);
        }

        // Visit package init function
        pkgInitFunction.accept(this);
        currentPkgInfo.setInitFunctionInfo(currentPkgInfo.getFunctionInfo(pkgInitFunction.getName()));

        currentPkgInfo.addAttributeInfo(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE, lineNumberTableAttributeInfo);
        currentPkgInfo.complete();
        currentPkgCPIndex = -1;
        currentPkgPath = null;
    }

    private void visitConstants(ConstDef[] constDefs) {
        for (ConstDef constDef : constDefs) {
            BType varType = constDef.getType();
            int regIndex = getNextIndex(varType.getTag(), gvIndexes);
            GlobalVarLocation globalVarLocation = new GlobalVarLocation(regIndex);
            constDef.setMemoryLocation(globalVarLocation);

            UTF8CPEntry constNameUTF8Entry = new UTF8CPEntry(constDef.getName());
            int constNameCPIndex = currentPkgInfo.addCPEntry(constNameUTF8Entry);
            UTF8CPEntry typeSigUTF8Entry = new UTF8CPEntry(varType.getSig().toString());
            int typeSigCPIndex = currentPkgInfo.addCPEntry(typeSigUTF8Entry);
            PackageVarInfo packageVarInfo = new PackageVarInfo(constNameCPIndex, constDef.getName(),
                    typeSigCPIndex, typeSigUTF8Entry.getValue());
            packageVarInfo.setType(varType);
            currentPkgInfo.addConstantInfo(constDef.getName(), packageVarInfo);

            // TODO Populate annotation attribute
        }
    }

    private void visitGlobalVariables(GlobalVariableDef[] globalVariableDefs) {
        for (GlobalVariableDef varDef : globalVariableDefs) {
            BType varType = varDef.getType();
            int regIndex = getNextIndex(varType.getTag(), gvIndexes);
            GlobalVarLocation globalVarLocation = new GlobalVarLocation(regIndex);
            varDef.setMemoryLocation(globalVarLocation);

            UTF8CPEntry globalVarNameUTF8Entry = new UTF8CPEntry(varDef.getName());
            int globalVarNameCPIndex = currentPkgInfo.addCPEntry(globalVarNameUTF8Entry);
            UTF8CPEntry typeSigUTF8Entry = new UTF8CPEntry(varType.getSig().toString());
            int typeSigCPIndex = currentPkgInfo.addCPEntry(typeSigUTF8Entry);
            PackageVarInfo packageVarInfo = new PackageVarInfo(globalVarNameCPIndex, varDef.getName(),
                    typeSigCPIndex, typeSigUTF8Entry.getValue());
            packageVarInfo.setType(varType);
            currentPkgInfo.addPackageVarInfo(varDef.getName(), packageVarInfo);

            // TODO Populate annotation attribute
        }
    }

    private void createServiceInfoEntries(Service[] services) {
        for (Service service : services) {
            // Add Connector name as an UTFCPEntry to the constant pool
            UTF8CPEntry serviceNameCPEntry = new UTF8CPEntry(service.getName());
            int serviceNameCPIndex = currentPkgInfo.addCPEntry(serviceNameCPEntry);

            String protocolPkgPath = service.getProtocolPkgPath();
            UTF8CPEntry protocolPkgPathCPEntry = new UTF8CPEntry(protocolPkgPath);
            int protocolPkgPathCPIndex = currentPkgInfo.addCPEntry(protocolPkgPathCPEntry);

            ServiceInfo serviceInfo = new ServiceInfo(currentPkgCPIndex, currentPkgPath,
                    serviceNameCPIndex, service.getName(),
                    protocolPkgPathCPIndex, protocolPkgPath);
            currentPkgInfo.addServiceInfo(service.getName(), serviceInfo);

            List<LocalVariableInfo> localVarInfo = new ArrayList<>();

            // Assign field indexes for Connector variables
            for (VariableDefStmt varDefStmt : service.getVariableDefStmts()) {
                VariableDef varDef = varDefStmt.getVariableDef();
                BType fieldType = varDef.getType();

                int fieldIndex = getNextIndex(fieldType.getTag(), gvIndexes);
                GlobalVarLocation globalVarLocation = new GlobalVarLocation(fieldIndex);
                varDef.setMemoryLocation(globalVarLocation);

                localVarInfo.add(getLocalVarAttributeInfo(varDef));
            }

            // Add local variables
            UTF8CPEntry localVarAttribUTF8CPEntry = new UTF8CPEntry(
                    AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.toString());
            int localVarAttribNameIndex = currentPkgInfo.addCPEntry(localVarAttribUTF8CPEntry);
            LocalVariableAttributeInfo localVarAttribInfo = new LocalVariableAttributeInfo(localVarAttribNameIndex);
            localVarAttribInfo.setLocalVariables(localVarInfo);
            serviceInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttribInfo);


            // Create the init function info
            createFunctionInfoEntries(new Function[]{service.getInitFunction()});
            serviceInfo.setInitFunctionInfo(currentPkgInfo.getFunctionInfo(service.getInitFunction().getName()));

            // Create resource info entries for all resource
            createResourceInfoEntries(service.getResources(), serviceInfo);
        }
    }

    private void createResourceInfoEntries(Resource[] resources, ServiceInfo serviceInfo) {
        for (Resource resource : resources) {

            // Add resource name as an UTFCPEntry to the constant pool
            UTF8CPEntry resourceNameCPEntry = new UTF8CPEntry(resource.getName());
            int resourceNameCPIndex = currentPkgInfo.addCPEntry(resourceNameCPEntry);

            ResourceInfo resourceInfo = new ResourceInfo(currentPkgCPIndex, currentPkgPath,
                    resourceNameCPIndex, resource.getName());
            resourceInfo.setParamTypes(getParamTypes(resource.getParameterDefs()));
            resourceInfo.setRetParamTypes(new BType[0]);

            generateCallableUnitInfoDataChannelMap(resource, resourceInfo);

            setParameterNames(resource.getParameterDefs(), resourceInfo);
            resourceInfo.setPackageInfo(currentPkgInfo);
            addWorkerInfoEntries(resourceInfo, resource.getWorkers());

            setCallableUnitSignature(resourceInfo);
            serviceInfo.addResourceInfo(resource.getName(), resourceInfo);
            resourceInfo.setServiceInfo(serviceInfo);
        }
    }

    private void createStructInfoEntries(StructDef[] structDefs) {
        for (StructDef structDef : structDefs) {

            // Add Struct name as an UTFCPEntry to the constant pool
            UTF8CPEntry structNameCPEntry = new UTF8CPEntry(structDef.getName());
            int structNameCPIndex = currentPkgInfo.addCPEntry(structNameCPEntry);

            StructInfo structInfo = new StructInfo(currentPkgCPIndex, currentPkgPath,
                    structNameCPIndex, structDef.getName());
            currentPkgInfo.addStructInfo(structDef.getName(), structInfo);
            structInfo.setPackageInfo(currentPkgInfo);

            BStructType structType = new BStructType(structDef.getName(), structDef.getPackagePath());
            structInfo.setType(structType);

        }

        for (StructDef structDef : structDefs) {
            StructInfo structInfo = currentPkgInfo.getStructInfo(structDef.getName());

            VariableDefStmt[] fieldDefStmts = structDef.getFieldDefStmts();

            BStructType.StructField[] structFields = new BStructType.StructField[fieldDefStmts.length];
            for (int i = 0; i < fieldDefStmts.length; i++) {
                VariableDefStmt fieldDefStmt = fieldDefStmts[i];
                VariableDef fieldDef = fieldDefStmt.getVariableDef();

                // Get the VM type -
                BType fieldType = getVMTypeFromSig(fieldDef.getType().getSig());

                // Create StructFieldInfo Entry
                UTF8CPEntry fieldNameUTF8Entry = new UTF8CPEntry(fieldDef.getName());
                int fieldNameCPIndex = currentPkgInfo.addCPEntry(fieldNameUTF8Entry);
                String fieldTypeSig = fieldDef.getType().getSig().toString();
                UTF8CPEntry sigUTF8Entry = new UTF8CPEntry(fieldTypeSig);
                int sigCPIndex = currentPkgInfo.addCPEntry(sigUTF8Entry);
                StructFieldInfo structFieldInfo = new StructFieldInfo(fieldNameCPIndex, fieldDef.getName(),
                        sigCPIndex, fieldTypeSig);
                structFieldInfo.setFieldType(fieldType);
                structInfo.addFieldInfo(structFieldInfo);

                int fieldIndex = getNextIndex(fieldType.getTag(), fieldIndexes);
                StructVarLocation structVarLocation = new StructVarLocation(fieldIndex);
                fieldDef.setMemoryLocation(structVarLocation);

                // Add struct field to the BStructType
                BStructType.StructField structField = new BStructType.StructField(fieldType, fieldDef.getName());
                structFields[i] = structField;
            }

            // Create variable count attribute info
            int[] fieldCount = Arrays.copyOf(prepareIndexes(fieldIndexes), fieldIndexes.length);
            addVariableCountAttributeInfo(currentPkgInfo, structInfo, fieldCount);
            structInfo.getType().setFieldTypeCount(fieldCount);
            structInfo.getType().setStructFields(structFields);
            resetIndexes(fieldIndexes);
        }
    }

    private void createConnectorInfoEntries(BallerinaConnectorDef[] connectorDefs) {
        for (BallerinaConnectorDef connectorDef : connectorDefs) {

            // TODO Temporary solution to get both executors working
            int fieldTypeCount = 0;
            BType[] connectorFieldTypes = new BType[connectorDef.getParameterDefs().length +
                    connectorDef.getVariableDefStmts().length];

            // Assign field indexes for Connector parameters
            StringBuilder sigBuilder = new StringBuilder();
            for (ParameterDef parameterDef : connectorDef.getParameterDefs()) {
                BType fieldType = parameterDef.getType();
                connectorFieldTypes[fieldTypeCount++] = fieldType;

                sigBuilder.append(fieldType.getSig().toString());

                int fieldIndex = getNextIndex(fieldType.getTag(), fieldIndexes);
                ConnectorVarLocation connectorVarLocation = new ConnectorVarLocation(fieldIndex);
                parameterDef.setMemoryLocation(connectorVarLocation);
            }

            // Add Connector name as an UTFCPEntry to the constant pool
            UTF8CPEntry connectorNameCPEntry = new UTF8CPEntry(connectorDef.getName());
            int connectorNameCPIndex = currentPkgInfo.addCPEntry(connectorNameCPEntry);

            // Add Connector signature to the constant pool
            UTF8CPEntry sigUTF8CPEntry = new UTF8CPEntry(sigBuilder.toString());
            int sigCPIndex = currentPkgInfo.addCPEntry(sigUTF8CPEntry);
            ConnectorInfo connectorInfo = new ConnectorInfo(currentPkgCPIndex, currentPkgPath,
                    connectorNameCPIndex, connectorDef.getName(), sigCPIndex, sigUTF8CPEntry.getValue());
            currentPkgInfo.addConnectorInfo(connectorDef.getName(), connectorInfo);

            BConnectorType connectorType = new BConnectorType(connectorDef.getName(), connectorDef.getPackagePath());
            connectorInfo.setType(connectorType);


            // Assign field indexes for Connector variables
            List<LocalVariableInfo> localVarInfo = new ArrayList<>();
            for (VariableDefStmt varDefStmt : connectorDef.getVariableDefStmts()) {
                VariableDef varDef = varDefStmt.getVariableDef();
                BType fieldType = varDef.getType();
                connectorFieldTypes[fieldTypeCount++] = fieldType;

                int fieldIndex = getNextIndex(fieldType.getTag(), fieldIndexes);
                ConnectorVarLocation connectorVarLocation = new ConnectorVarLocation(fieldIndex);
                varDef.setMemoryLocation(connectorVarLocation);

                localVarInfo.add(getLocalVarAttributeInfo(varDef));
            }

            // Add local variables
            UTF8CPEntry localVarAttribUTF8CPEntry = new UTF8CPEntry(
                    AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.toString());
            int localVarAttribNameIndex = currentPkgInfo.addCPEntry(localVarAttribUTF8CPEntry);
            LocalVariableAttributeInfo localVarAttribInfo = new LocalVariableAttributeInfo(localVarAttribNameIndex);
            localVarAttribInfo.setLocalVariables(localVarInfo);
            connectorInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttribInfo);

            // Create variable count attribute info
            int[] fieldCount = Arrays.copyOf(prepareIndexes(fieldIndexes), fieldIndexes.length);
            addVariableCountAttributeInfo(currentPkgInfo, connectorInfo, fieldCount);
            connectorType.setFieldTypeCount(fieldCount);
            resetIndexes(fieldIndexes);

            // Create the init function info
            createFunctionInfoEntries(new Function[]{connectorDef.getInitFunction()});

            // Create function info entries for all actions
            createActionInfoEntries(connectorDef.getActions(), connectorInfo);

            // Create the init native action info
            if (connectorDef.getInitAction() != null) {
                createActionInfoEntries(new Action[]{connectorDef.getInitAction()}, connectorInfo);
            }
        }
    }

    private void createActionInfoEntries(Action[] actions, ConnectorInfo connectorInfo) {
        for (Action action : actions) {
            // Add action name as an UTFCPEntry to the constant pool
            UTF8CPEntry actionNameCPEntry = new UTF8CPEntry(action.getName());
            int actionNameCPIndex = currentPkgInfo.addCPEntry(actionNameCPEntry);

            ActionInfo actionInfo = new ActionInfo(currentPkgCPIndex, currentPkgPath,
                    actionNameCPIndex, action.getName(), connectorInfo);
            actionInfo.setParamTypes(getParamTypes(action.getParameterDefs()));
            actionInfo.setRetParamTypes(getParamTypes(action.getReturnParameters()));
            actionInfo.setPackageInfo(currentPkgInfo);
            actionInfo.setNative(action.isNative());

            if (action instanceof BallerinaAction) {
                addWorkerInfoEntries(actionInfo, action.getWorkers());
            }

            generateCallableUnitInfoDataChannelMap(action, actionInfo);

            setCallableUnitSignature(actionInfo);
            connectorInfo.addActionInfo(action.getName(), actionInfo);
        }
    }

    private void createFunctionInfoEntries(Function[] functions) {
        for (Function function : functions) {

            // Add function name as an UTFCPEntry to the constant pool
            UTF8CPEntry funcNameCPEntry = new UTF8CPEntry(function.getName());
            int funcNameCPIndex = currentPkgInfo.addCPEntry(funcNameCPEntry);

            FunctionInfo funcInfo = new FunctionInfo(currentPkgCPIndex, currentPkgPath,
                    funcNameCPIndex, function.getName());
            funcInfo.setParamTypes(getParamTypes(function.getParameterDefs()));
            funcInfo.setRetParamTypes(getParamTypes(function.getReturnParameters()));
            funcInfo.setPackageInfo(currentPkgInfo);
            funcInfo.setNative(function.isNative());

            if (function instanceof BallerinaFunction) {
                addWorkerInfoEntries(funcInfo, (function).getWorkers());
            }

            generateCallableUnitInfoDataChannelMap(function, funcInfo);

            setCallableUnitSignature(funcInfo);
            currentPkgInfo.addFunctionInfo(function.getName(), funcInfo);
        }
    }

    private void generateCallableUnitInfoDataChannelMap(CallableUnit callableUnit, CallableUnitInfo callableUnitInfo) {
        callableUnit.getWorkerDataChannelMap().forEach((k, v) -> {
            UTF8CPEntry sourceCPEntry = new UTF8CPEntry(v.getSource());
            int sourceCPIndex = currentPkgInfo.addCPEntry(sourceCPEntry);
            UTF8CPEntry targetCPEntry = new UTF8CPEntry(v.getTarget());
            int targetCPIndex = currentPkgInfo.addCPEntry(targetCPEntry);
            WorkerDataChannelInfo workerDataChannelInfo = new WorkerDataChannelInfo(sourceCPIndex,
                    v.getSource(), targetCPIndex, v.getTarget());
            if (callableUnitInfo.getWorkerDataChannelInfo(workerDataChannelInfo.getChannelName()) == null) {
                workerDataChannelInfo.setUniqueName(workerDataChannelInfo.getChannelName() + workerChannelCount);
                callableUnitInfo.addWorkerDataChannelInfo(workerDataChannelInfo);
                String uniqueName = workerDataChannelInfo.getUniqueName();
                UTF8CPEntry uniqueNameCPEntry = new UTF8CPEntry(uniqueName);
                int uniqueNameCPIndex = currentPkgInfo.addCPEntry(uniqueNameCPEntry);
                workerDataChannelInfo.setUniqueNameCPIndex(uniqueNameCPIndex);
                workerChannelCount++;
            }

        });
    }

    private void addWorkerInfoEntries(CallableUnitInfo callableUnitInfo, Worker[] workers) {
        // Create default worker
        UTF8CPEntry workerNameCPEntry = new UTF8CPEntry("default");
        int workerNameCPIndex = currentPkgInfo.addCPEntry(workerNameCPEntry);
        WorkerInfo defaultWorkerInfo = new WorkerInfo(workerNameCPIndex, "default");
        callableUnitInfo.setDefaultWorkerInfo(defaultWorkerInfo);

        // Create other workers if any
        for (Worker worker : workers) {
            workerNameCPEntry = new UTF8CPEntry(worker.getName());
            workerNameCPIndex = currentPkgInfo.addCPEntry(workerNameCPEntry);
            WorkerInfo workerInfo = new WorkerInfo(workerNameCPIndex, worker.getName());
            callableUnitInfo.addWorkerInfo(worker.getName(), workerInfo);
        }
    }

    @Override
    public void visit(BallerinaFile bFile) {

    }

    @Override
    public void visit(ImportPackage importPkg) {

    }

    @Override
    public void visit(ConstDef constant) {

    }

    @Override
    public void visit(GlobalVariableDef globalVar) {

    }

    @Override
    public void visit(Service service) {
        BallerinaFunction initFunction = service.getInitFunction();
        visit(initFunction);

        currentServiceInfo = currentPkgInfo.getServiceInfo(service.getName());
        AnnotationAttachment[] annotationAttachments = service.getAnnotations();
        if (annotationAttachments.length > 0) {
            AnnotationAttributeInfo annotationsAttribute = getAnnotationAttributeInfo(annotationAttachments);
            currentServiceInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, annotationsAttribute);
        }

        for (Resource resource : service.getResources()) {
            resource.accept(this);
        }
    }

    @Override
    public void visit(BallerinaConnectorDef connectorDef) {
        BallerinaFunction initFunction = connectorDef.getInitFunction();
        visit(initFunction);

        if (connectorDef.isFilterConnector()) {
            BallerinaConnectorDef filterConnectorDef = (BallerinaConnectorDef) connectorDef.getFilteredType();
            PackageInfo connectorPkgInfo = programFile.getPackageInfo(filterConnectorDef.getPackagePath());
            int pkgCPIndex = addPackageCPEntry(filterConnectorDef.getPackagePath());

            UTF8CPEntry nameUTF8CPEntry = new UTF8CPEntry(filterConnectorDef.getName());
            int nameIndex = currentPkgInfo.addCPEntry(nameUTF8CPEntry);

            StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex,
                    filterConnectorDef.getPackagePath(), nameIndex, filterConnectorDef.getName());
            ConnectorInfo connectorInfo = connectorPkgInfo.getConnectorInfo(filterConnectorDef.getName());
            connectorInfo.setFilterConnector(filterConnectorDef.isFilterConnector());
            structureRefCPEntry.setStructureTypeInfo(connectorInfo);
            currentPkgInfo.addCPEntry(structureRefCPEntry);
        }

        BallerinaAction initAction = connectorDef.getInitAction();
        if (initAction != null) {
            visit(initAction);
        }

        ConnectorInfo connectorInfo = currentPkgInfo.getConnectorInfo(connectorDef.getName());
        AnnotationAttachment[] annotationAttachments = connectorDef.getAnnotations();
        if (annotationAttachments.length > 0) {
            AnnotationAttributeInfo annotationsAttribute = getAnnotationAttributeInfo(annotationAttachments);
            connectorInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, annotationsAttribute);
        }

        for (BallerinaAction action : connectorDef.getActions()) {
            action.accept(this);
        }

    }

    @Override
    public void visit(Resource resource) {
        ResourceInfo resourceInfo = currentServiceInfo.getResourceInfo(resource.getName());
        currentCallableUnitInfo = resourceInfo;
        visitCallableUnit(resource, resourceInfo, resource.getWorkers());
    }

    @Override
    public void visit(BallerinaFunction function) {
        currentCallableUnitInfo = currentPkgInfo.getFunctionInfo(function.getName());
        visitCallableUnit(function, currentPkgInfo.getFunctionInfo(function.getName()), function.getWorkers());
    }

    @Override
    public void visit(BTypeMapper typeMapper) {

    }

    @Override
    public void visit(BallerinaAction action) {
        ConnectorInfo connectorInfo = currentPkgInfo.getConnectorInfo(action.getConnectorDef().getName());

        // Now find out the ActionInfo

        ActionInfo actionInfo = connectorInfo.getActionInfo(action.getName());
        currentCallableUnitInfo = actionInfo;
        visitCallableUnit(action, actionInfo, action.getWorkers());
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
    public void visit(SimpleVariableDef variableDef) {
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
        int opcode;
        int lvIndex;

        Expression rhsExpr = varDefStmt.getRExpr();
        if (rhsExpr != null) {
            rhsExpr.accept(this);
            rhsExprRegIndex = rhsExpr.getTempOffset();
        } else {
            // TODO get the default value;
        }

        MemoryLocation stackVarLocation;
        VariableDef variableDef = varDefStmt.getVariableDef();

        MemoryLocation memoryLocation = varDefStmt.getVariableDef().getMemoryLocation();
        if (memoryLocation instanceof StackVarLocation || memoryLocation instanceof WorkerVarLocation) {
            OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(variableDef.getType().getTag(),
                    InstructionCodes.ISTORE, lvIndexes);
            opcode = opcodeAndIndex.opcode;
            lvIndex = opcodeAndIndex.index;
            stackVarLocation = new StackVarLocation(lvIndex);
            variableDef.setMemoryLocation(stackVarLocation);
            if (rhsExpr != null) {
                emit(opcode, rhsExpr.getTempOffset(), lvIndex);
            }

            LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(variableDef);
            currentlLocalVarAttribInfo.addLocalVarInfo(localVarInfo);
        } else {
            // TODO
        }
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        if (assignStmt.isDeclaredWithVar()) {
            for (Expression expr : assignStmt.getLExprs()) {
                // non resolved VariableDef == '_' ignored variable ref underscore syntax
                if (((SimpleVarRefExpr) expr).getVariableDef() != null) {
                    assignVariableDefMemoryLocation(((SimpleVarRefExpr) expr).getVariableDef());
                }
            }
        }

        // Evaluate the rhs expression
        Expression rExpr = assignStmt.getRExpr();
        if (rExpr == null) {
            return;
        }

        rExpr.accept(this);

        int[] rhsExprRegIndexes;
        if (assignStmt.getRExpr() instanceof ExecutableMultiReturnExpr) {
            rhsExprRegIndexes = ((ExecutableMultiReturnExpr) assignStmt.getRExpr()).getOffsets();
        } else {
            rhsExprRegIndexes = new int[]{assignStmt.getRExpr().getTempOffset()};
        }

        Expression[] lhsExprs = assignStmt.getLExprs();
        for (int i = 0; i < lhsExprs.length; i++) {
            rhsExprRegIndex = rhsExprRegIndexes[i];
            Expression lExpr = lhsExprs[i];
            if (lExpr instanceof SimpleVarRefExpr) {
                if (((SimpleVarRefExpr) lExpr).getVarName().equals("_")) {
                    continue;
                }
                varAssignment = true;
                lExpr.accept(this);
                varAssignment = false;

            } else if (lExpr instanceof IndexBasedVarRefExpr) {
                arrayMapAssignment = true;
                lExpr.accept(this);
                arrayMapAssignment = false;
            } else if (lExpr instanceof FieldBasedVarRefExpr) {
                structAssignment = true;
                lExpr.accept(this);
                structAssignment = false;
            }
        }
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        for (Statement stmt : blockStmt.getStatements()) {
            if (stmt instanceof CommentStmt) {
                continue;
            }

            if (!(stmt instanceof TryCatchStmt)) {
                addLineNumberInfo(stmt.getNodeLocation());
            }
            stmt.accept(this);

            for (int i = 0; i < maxRegIndexes.length; i++) {
                if (maxRegIndexes[i] < regIndexes[i]) {
                    maxRegIndexes[i] = regIndexes[i];
                }
            }

            resetIndexes(regIndexes);
        }
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        Expression ifCondExpr = ifElseStmt.getCondition();
        ifCondExpr.accept(this);
        List<Instruction> gotoInstructionList = new ArrayList<>();

        // Operand2 should be the jump address  else-if or else or to the next instruction after then block
        Instruction ifInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE,
                ifCondExpr.getTempOffset(), -1);
        emit(ifInstruction);

        ifElseStmt.getThenBody().accept(this);

        // Check whether this then block is the last block of code
        Instruction gotoInstruction = InstructionFactory.get(InstructionCodes.GOTO, -1);
        emit(gotoInstruction);
        gotoInstructionList.add(gotoInstruction);

        ifInstruction.setOperand(1, nextIP());

        // Process else-if parts
        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            addLineNumberInfo(elseIfBlock.getNodeLocation());
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            elseIfCondition.accept(this);
            ifInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE,
                    elseIfCondition.getTempOffset(), -1);
            emit(ifInstruction);

            elseIfBlock.getElseIfBody().accept(this);
            gotoInstruction = new Instruction(InstructionCodes.GOTO, -1);
            emit(gotoInstruction);
            gotoInstructionList.add(gotoInstruction);
            ifInstruction.setOperand(1, nextIP());
        }

        Statement elseBody = ifElseStmt.getElseBody();
        if (elseBody != null) {
            elseBody.accept(this);
        }

        int nextIP = nextIP();
        for (Instruction instruction : gotoInstructionList) {
            instruction.setOperand(0, nextIP);
        }
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        if (replyStmt.getReplyExpr() != null) {
            replyStmt.getReplyExpr().accept(this);
            emit(InstructionCodes.REP, replyStmt.getReplyExpr().getTempOffset());
        } else {
            emit(InstructionCodes.REP, -1);
        }
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        int[] regIndexes;
        if (returnStmt.getExprs().length == 1 &&
                returnStmt.getExprs()[0] instanceof FunctionInvocationExpr) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) returnStmt.getExprs()[0];
            returnStmt.getExprs()[0].accept(this);
            regIndexes = funcIExpr.getOffsets();
            BType[] retTypes = funcIExpr.getTypes();
            for (int i = 0; i < regIndexes.length; i++) {
                // 1: return value position; 2:callee's value index;
                emit(new Instruction(getOpcode(retTypes[i].getTag(), InstructionCodes.IRET), i, regIndexes[i]));
            }
        } else {
            regIndexes = new int[returnStmt.getExprs().length];
            for (int i = 0; i < returnStmt.getExprs().length; i++) {
                Expression expr = returnStmt.getExprs()[i];
                expr.accept(this);
                regIndexes[i] = expr.getTempOffset();
                emit(new Instruction(getOpcode(expr.getType().getTag(), InstructionCodes.IRET), i, regIndexes[i]));
            }
        }
        generateFinallyInstructions(returnStmt, StatementKind.CALLABLE_UNIT_BLOCK);

        emit(InstructionCodes.RET);
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        Expression conditionExpr = whileStmt.getCondition();
        Instruction gotoInstruction = InstructionFactory.get(InstructionCodes.GOTO, nextIP());
        continueInstructions.push(gotoInstruction);
        conditionExpr.accept(this);
        Instruction ifInstruction = new Instruction(InstructionCodes.BR_FALSE,
                conditionExpr.getTempOffset(), -1);
        emit(ifInstruction);

        Instruction breakGotoInstruction = new Instruction(InstructionCodes.GOTO, 0);
        breakInstructions.push(breakGotoInstruction);
        whileStmt.getBody().accept(this);

        emit(gotoInstruction);
        int nextIP = nextIP();
        ifInstruction.setOperand(1, nextIP);
        breakGotoInstruction.setOperand(0, nextIP);
        breakInstructions.pop();
        continueInstructions.pop();
    }

    @Override
    public void visit(BreakStmt breakStmt) {
        generateFinallyInstructions(breakStmt, StatementKind.WHILE_BLOCK);
        emit(breakInstructions.peek());
    }

    @Override
    public void visit(ContinueStmt continueStmt) {
        generateFinallyInstructions(continueStmt, StatementKind.WHILE_BLOCK);
        emit(continueInstructions.peek());
    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        resetIndexes(regIndexes);
        Instruction gotoEndOfTryCatchBlock = new Instruction(InstructionCodes.GOTO, -1);
        List<int[]> unhandledErrorRangeList = new ArrayList<>();
        ErrorTableAttributeInfo errorTable = createErrorTableIfAbsent(currentPkgInfo);

        // Handle try block.
        int fromIP = nextIP();
        tryCatchStmt.getTryBlock().accept(this);
        int toIP = nextIP() - 1;

        // Append finally block instructions.
        if (tryCatchStmt.getFinallyBlock() != null) {
            tryCatchStmt.getFinallyBlock().getFinallyBlockStmt().accept(this);
        }

        emit(gotoEndOfTryCatchBlock);
        unhandledErrorRangeList.add(new int[]{fromIP, toIP});

        // Handle catch blocks.
        int order = 0;
        for (TryCatchStmt.CatchBlock catchBlock : tryCatchStmt.getCatchBlocks()) {
            addLineNumberInfo(catchBlock.getCatchBlockStmt().getNodeLocation());
            int targetIP = nextIP();

            // Define local variable index for Error.
            ParameterDef paramDef = catchBlock.getParameterDef();
            int lvIndex = ++lvIndexes[REF_OFFSET];
            paramDef.setMemoryLocation(new StackVarLocation(lvIndex));
            emit(new Instruction(InstructionCodes.ERRSTORE, lvIndex));

            // Visit Catch Block.
            catchBlock.getCatchBlockStmt().accept(this);
            unhandledErrorRangeList.add(new int[]{targetIP, nextIP() - 1});

            // Append finally block instructions.
            if (tryCatchStmt.getFinallyBlock() != null) {
                tryCatchStmt.getFinallyBlock().getFinallyBlockStmt().accept(this);
            }

            emit(gotoEndOfTryCatchBlock);

            // Create Error table entry for this catch block
            StructDef structDef = (StructDef) catchBlock.getParameterDef().getType();
            int pkgCPIndex = addPackageCPEntry(structDef.getPackagePath());
            UTF8CPEntry structNameCPEntry = new UTF8CPEntry(structDef.getName());
            int structNameCPIndex = currentPkgInfo.addCPEntry(structNameCPEntry);
            StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, structDef.getPackagePath(),
                    structNameCPIndex, structDef.getName());
            PackageRefCPEntry packageRefCPEntry = (PackageRefCPEntry) currentPkgInfo.getCPEntry(pkgCPIndex);
            StructInfo errorStructInfo = packageRefCPEntry.getPackageInfo().getStructInfo(structDef.getName());
            structureRefCPEntry.setStructureTypeInfo(
                    errorStructInfo);
            int structCPEntryIndex = currentPkgInfo.addCPEntry(structureRefCPEntry);
            ErrorTableEntry errorTableEntry = new ErrorTableEntry(fromIP, toIP, targetIP, order++, structCPEntryIndex);
            errorTableEntry.setError(errorStructInfo);
            errorTable.addErrorTableEntry(errorTableEntry);
        }

        if (tryCatchStmt.getFinallyBlock() != null) {
            // Create Error table entry for unhandled errors in try and catch(s) blocks
            for (int[] range : unhandledErrorRangeList) {
                ErrorTableEntry errorTableEntry = new ErrorTableEntry(range[0], range[1], nextIP(), order++, -1);
                errorTable.addErrorTableEntry(errorTableEntry);
            }

            // Append finally block instruction.
            tryCatchStmt.getFinallyBlock().getFinallyBlockStmt().accept(this);
            emit(new Instruction(InstructionCodes.THROW, -1));
        }

        gotoEndOfTryCatchBlock.setOperand(0, nextIP());
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        throwStmt.getExpr().accept(this);
        Instruction throwInstruction = new Instruction(InstructionCodes.THROW, throwStmt.getExpr().getTempOffset());
        emit(throwInstruction);
    }

    @Override
    public void visit(FunctionInvocationStmt functionIStmt) {
        visit(functionIStmt.getFunctionInvocationExpr());
    }

    @Override
    public void visit(ActionInvocationStmt actionIStmt) {
        visit(actionIStmt.getActionInvocationExpr());
    }

    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {
        WorkerDataChannel workerDataChannel = workerInvocationStmt.getWorkerDataChannel();

        WorkerDataChannelInfo workerDataChannelInfo = currentCallableUnitInfo
                .getWorkerDataChannelInfo(workerDataChannel.getChannelName());

        WorkerDataChannelRefCPEntry wrkrInvRefCPEntry = new WorkerDataChannelRefCPEntry(workerDataChannelInfo
                .getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());

        wrkrInvRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);

        int wrkrInvRefCPIndex = currentPkgInfo.addCPEntry(wrkrInvRefCPEntry);
        if (currentWorkerInfo != null) {
            currentWorkerInfo.setWrkrDtChnlRefCPIndex(wrkrInvRefCPIndex);
            currentWorkerInfo.setWorkerDataChannelInfoForForkJoin(workerDataChannelInfo);
        }
        workerDataChannelInfo.setDataChannelRefIndex(wrkrInvRefCPIndex);
        int workerInvocationIndex = getWorkerInvocationCPIndex(workerInvocationStmt);
        emit(InstructionCodes.WRKINVOKE, wrkrInvRefCPIndex, workerInvocationIndex);
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        WorkerDataChannel workerDataChannel = workerReplyStmt.getWorkerDataChannel();
        WorkerDataChannelInfo workerDataChannelInfo = currentCallableUnitInfo
                .getWorkerDataChannelInfo(workerDataChannel.getChannelName());

        WorkerDataChannelRefCPEntry wrkrChnlRefCPEntry = new WorkerDataChannelRefCPEntry(workerDataChannelInfo
                .getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());

        wrkrChnlRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        int wrkrRplyRefCPIndex = currentPkgInfo.addCPEntry(wrkrChnlRefCPEntry);
        workerDataChannelInfo.setDataChannelRefIndex(wrkrRplyRefCPIndex);

        int workerReplyIndex = getWorkerReplyCPIndex(workerReplyStmt);
        emit(InstructionCodes.WRKREPLY, wrkrRplyRefCPIndex, workerReplyIndex);
        // Generate store instructions to store the values.
        int[] rhsExprRegIndexes = workerReplyStmt.getOffsets();
        Expression[] lhsExprs = workerReplyStmt.getExpressionList();
        for (int i = 0; i < lhsExprs.length; i++) {
            rhsExprRegIndex = rhsExprRegIndexes[i];
            Expression lExpr = lhsExprs[i];

            if (lExpr instanceof SimpleVarRefExpr) {
                varAssignment = true;
                lExpr.accept(this);
                varAssignment = false;
            } else if (lExpr instanceof IndexBasedVarRefExpr) {
                arrayMapAssignment = true;
                lExpr.accept(this);
                arrayMapAssignment = false;
            } else if (lExpr instanceof FieldBasedVarRefExpr) {
                structAssignment = true;
                lExpr.accept(this);
                structAssignment = false;
            }
        }
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        getForkJoinCPIndex(forkJoinStmt);
    }

    @Override
    public void visit(TransformStmt transformStmt) {
        transformStmt.getBody().accept(this);
    }

    @Override
    public void visit(TransactionStmt transactionStmt) {
        ErrorTableAttributeInfo errorTable = createErrorTableIfAbsent(currentPkgInfo);
        Instruction gotoEndOfTransactionBlock = new Instruction(InstructionCodes.GOTO, -1);
        Instruction gotoStartOfAbortedBlock = new Instruction(InstructionCodes.GOTO, -1);
        abortInstructions.push(gotoStartOfAbortedBlock);

        //start transaction
        int startIP = nextIP();
        emit(new Instruction(InstructionCodes.TRBGN));

        //process transaction statements
        transactionStmt.getTransactionBlock().accept(this);

        //end the transaction
        int endIP = nextIP();
        emit(new Instruction(InstructionCodes.TREND, 0));

        //process committed block
        if (transactionStmt.getCommittedBlock() != null) {
            transactionStmt.getCommittedBlock().getCommittedBlockStmt().accept(this);
        }
        if (transactionStmt.getAbortedBlock() != null) {
            emit(gotoEndOfTransactionBlock);
        }
        abortInstructions.pop();
        gotoStartOfAbortedBlock.setOperand(0, nextIP());
        emit(new Instruction(InstructionCodes.TREND, -1));

        //process aborted block
        if (transactionStmt.getAbortedBlock() != null) {
            transactionStmt.getAbortedBlock().getAbortedBlockStmt().accept(this);
        }
        emit(gotoEndOfTransactionBlock);

        // CodeGen for error handling.
        int errorTargetIP = nextIP();
        emit(new Instruction(InstructionCodes.TREND, -1));
        if (transactionStmt.getAbortedBlock() != null) {
            transactionStmt.getAbortedBlock().getAbortedBlockStmt().accept(this);
        }

        emit(new Instruction(InstructionCodes.THROW, -1));
        gotoEndOfTransactionBlock.setOperand(0, nextIP());
        ErrorTableEntry errorTableEntry = new ErrorTableEntry(startIP, endIP, errorTargetIP, 0, -1);
        errorTable.addErrorTableEntry(errorTableEntry);
    }

    @Override
    public void visit(AbortStmt abortStmt) {
        generateFinallyInstructions(abortStmt, StatementKind.TRANSACTION_BLOCK);
        emit(abortInstructions.peek());
    }

    @Override
    public void visit(NamespaceDeclarationStmt namespaceDeclarationStmt) {
    }

    @Override
    public void visit(NamespaceDeclaration namespaceDeclaration) {
    }

    // Expressions

    @Override
    public void visit(BasicLiteral basicLiteral) {
        int opcode;
        int typeTag = basicLiteral.getType().getTag();

        switch (typeTag) {
            case TypeTags.INT_TAG:
                basicLiteral.setTempOffset(++regIndexes[INT_OFFSET]);
                long intVal = basicLiteral.getBValue().intValue();
                if (intVal >= 0 && intVal <= 5) {
                    opcode = InstructionCodes.ICONST_0 + (int) intVal;
                    emit(opcode, basicLiteral.getTempOffset());
                } else {
                    IntegerCPEntry intCPEntry = new IntegerCPEntry(basicLiteral.getBValue().intValue());
                    int intCPEntryIndex = currentPkgInfo.addCPEntry(intCPEntry);
                    emit(InstructionCodes.ICONST, intCPEntryIndex, basicLiteral.getTempOffset());
                }
                break;

            case TypeTags.FLOAT_TAG:
                basicLiteral.setTempOffset(++regIndexes[FLOAT_OFFSET]);
                double floatVal = basicLiteral.getBValue().floatValue();
                if (floatVal == 0 || floatVal == 1 || floatVal == 2 ||
                        floatVal == 3 || floatVal == 4 || floatVal == 5) {
                    opcode = InstructionCodes.FCONST_0 + (int) floatVal;
                    emit(opcode, basicLiteral.getTempOffset());
                } else {
                    FloatCPEntry floatCPEntry = new FloatCPEntry(basicLiteral.getBValue().floatValue());
                    int floatCPEntryIndex = currentPkgInfo.addCPEntry(floatCPEntry);
                    emit(InstructionCodes.FCONST, floatCPEntryIndex, basicLiteral.getTempOffset());
                }
                break;

            case TypeTags.STRING_TAG:
                basicLiteral.setTempOffset(++regIndexes[STRING_OFFSET]);
                String strValue = basicLiteral.getBValue().stringValue();
                UTF8CPEntry utf8CPEntry = new UTF8CPEntry(strValue);
                int stringValCPIndex = currentPkgInfo.addCPEntry(utf8CPEntry);

                StringCPEntry stringCPEntry = new StringCPEntry(stringValCPIndex, strValue);
                int strCPIndex = currentPkgInfo.addCPEntry(stringCPEntry);

                emit(InstructionCodes.SCONST, strCPIndex, basicLiteral.getTempOffset());
                break;

            case TypeTags.BOOLEAN_TAG:
                basicLiteral.setTempOffset(++regIndexes[BOOL_OFFSET]);
                boolean booleanVal = basicLiteral.getBValue().booleanValue();
                if (!booleanVal) {
                    opcode = InstructionCodes.BCONST_0;
                } else {
                    opcode = InstructionCodes.BCONST_1;
                }
                emit(opcode, basicLiteral.getTempOffset());
                break;
        }
    }

    @Override
    public void visit(NullLiteral nullLiteral) {
        int regIndex = ++regIndexes[REF_OFFSET];
        nullLiteral.setTempOffset(regIndex);
        emit(InstructionCodes.RCONST_NULL, regIndex);
    }

    @Override
    public void visit(LambdaExpression lambdaExpr) {
        Function function = lambdaExpr.getFunction();
        String pkgPath = function.getPackagePath();
        int pkgCPIndex = addPackageCPEntry(pkgPath);
        String funcName = function.getName();
        UTF8CPEntry funcNameCPEntry = new UTF8CPEntry(funcName);
        int funcNameCPIndex = currentPkgInfo.addCPEntry(funcNameCPEntry);

        // Find the package info entry of the function and from the package info entry find the function info entry
        PackageInfo funcPackageInfo = programFile.getPackageInfo(pkgPath);
        FunctionInfo functionInfo = funcPackageInfo.getFunctionInfo(funcName);

        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgCPIndex, pkgPath, funcNameCPIndex, funcName);
        funcRefCPEntry.setFunctionInfo(functionInfo);
        int funcRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);
        int nextIndex = getNextIndex(TypeTags.FUNCTION_POINTER_TAG, regIndexes);
        lambdaExpr.setTempOffset(nextIndex);
        emit(InstructionCodes.FPLOAD, funcRefCPIndex, nextIndex);
    }

    @Override
    public void visit(UnaryExpression unaryExpr) {
        Expression rExpr = unaryExpr.getRExpr();
        rExpr.accept(this);

        OpcodeAndIndex opcodeAndIndex;
        int opcode;
        int exprIndex;
        if (Operator.SUB.equals(unaryExpr.getOperator())) {
            opcodeAndIndex = getOpcodeAndIndex(unaryExpr.getType().getTag(),
                    InstructionCodes.INEG, regIndexes);
            opcode = opcodeAndIndex.opcode;
            exprIndex = opcodeAndIndex.index;
            emit(opcode, rExpr.getTempOffset(), exprIndex);

        } else if (Operator.LENGTHOF.equals(unaryExpr.getOperator())) {
            BType rType = unaryExpr.getRExpr().getType();
            if (rType == BTypes.typeJSON || getElementType(rType) == BTypes.typeJSON) {
                opcodeAndIndex = getOpcodeAndIndex(unaryExpr.getType().getTag(),
                        InstructionCodes.LENGTHOFJSON, regIndexes);
            } else {
                opcodeAndIndex = getOpcodeAndIndex(unaryExpr.getType().getTag(),
                        InstructionCodes.LENGTHOF, regIndexes);

            }
            opcode = opcodeAndIndex.opcode;
            exprIndex = opcodeAndIndex.index;
            emit(opcode, rExpr.getTempOffset(), exprIndex);

        } else if (Operator.TYPEOF.equals(unaryExpr.getOperator())) {

            if (rExpr.getType() == BTypes.typeAny) {
                exprIndex = ++regIndexes[REF_OFFSET];
                emit(InstructionCodes.TYPEOF, rExpr.getTempOffset(), exprIndex);
            } else {
                TypeSignature typeSig = rExpr.getType().getSig();
                UTF8CPEntry typeSigUTF8CPEntry = new UTF8CPEntry(typeSig.toString());
                int typeSigCPIndex = currentPkgInfo.addCPEntry(typeSigUTF8CPEntry);
                TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex, typeSig.toString());
                typeRefCPEntry.setType(getVMTypeFromSig(typeSig));
                int typeCPindex = currentPkgInfo.addCPEntry(typeRefCPEntry);
                exprIndex = ++regIndexes[REF_OFFSET];
                emit(InstructionCodes.TYPELOAD, typeCPindex, exprIndex);
            }

        } else if (Operator.NOT.equals(unaryExpr.getOperator())) {
            opcode = InstructionCodes.BNOT;
            exprIndex = ++regIndexes[BOOL_OFFSET];
            emit(opcode, rExpr.getTempOffset(), exprIndex);
        } else {
            // "+" operator
            // Nothing to do
            exprIndex = rExpr.getTempOffset();
        }

        unaryExpr.setTempOffset(exprIndex);
    }


    // Binary arithmetic expressions

    @Override
    public void visit(AddExpression addExpr) {
        emitBinaryArithmeticExpr(addExpr, InstructionCodes.IADD);
    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        emitBinaryArithmeticExpr(subtractExpr, InstructionCodes.ISUB);
    }

    @Override
    public void visit(MultExpression multExpr) {
        emitBinaryArithmeticExpr(multExpr, InstructionCodes.IMUL);
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        emitBinaryArithmeticExpr(divideExpr, InstructionCodes.IDIV);
    }

    @Override
    public void visit(ModExpression modExpr) {
        emitBinaryArithmeticExpr(modExpr, InstructionCodes.IMOD);
    }


    // Binary logical expressions

    @Override
    public void visit(AndExpression andExpr) {
        // Generate code for the left hand side
        Expression lExpr = andExpr.getLExpr();
        lExpr.accept(this);

        // Last operand will be filled later.
        Instruction lEvalInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE,
                lExpr.getTempOffset(), -1);
        emit(lEvalInstruction);

        // Generate code for the right hand side
        Expression rExpr = andExpr.getRExpr();
        rExpr.accept(this);

        // Last operand will be filled later.
        Instruction rEvalInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE,
                rExpr.getTempOffset(), -1);
        emit(rEvalInstruction);

        // If both l and r conditions are true, then load 'true'
        int exprRegIndex = ++regIndexes[BOOL_OFFSET];
        andExpr.setTempOffset(exprRegIndex);
        emit(InstructionCodes.BCONST_1, exprRegIndex);

        Instruction goToIns = InstructionFactory.get(InstructionCodes.GOTO, -1);
        emit(goToIns);

        int loadFalseIP = nextIP();
        lEvalInstruction.setOperand(1, loadFalseIP);
        rEvalInstruction.setOperand(1, loadFalseIP);

        // Load 'false' if the both conditions are false;
        emit(InstructionCodes.BCONST_0, exprRegIndex);
        goToIns.setOperand(0, nextIP());
    }

    @Override
    public void visit(OrExpression orExpr) {
        // Generate code for the left hand side
        Expression lExpr = orExpr.getLExpr();
        lExpr.accept(this);

        // Last operand will be filled later.
        Instruction lEvalInstruction = InstructionFactory.get(InstructionCodes.BR_TRUE,
                lExpr.getTempOffset(), -1);
        emit(lEvalInstruction);

        // Generate code for the right hand side
        Expression rExpr = orExpr.getRExpr();
        rExpr.accept(this);

        // Last operand will be filled later.
        Instruction rEvalInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE,
                rExpr.getTempOffset(), -1);
        emit(rEvalInstruction);

        // If either l and r conditions are true, then load 'true'
        lEvalInstruction.setOperand(1, nextIP());
        int exprRegIndex = ++regIndexes[BOOL_OFFSET];
        orExpr.setTempOffset(exprRegIndex);
        emit(InstructionCodes.BCONST_1, exprRegIndex);

        Instruction goToIns = InstructionFactory.get(InstructionCodes.GOTO, -1);
        emit(goToIns);
        rEvalInstruction.setOperand(1, nextIP());

        // Load 'false' if the both conditions are false;
        emit(InstructionCodes.BCONST_0, exprRegIndex);
        goToIns.setOperand(0, nextIP());
    }


    // Binary equality expressions

    @Override
    public void visit(EqualExpression equalExpr) {
        // Handle type equality as a special case.
        if ((equalExpr.getRExpr().getType() == equalExpr.getLExpr().getType())
                && equalExpr.getRExpr().getType() == BTypes.typeType) {
            emitBinaryTypeEqualityExpr(equalExpr, InstructionCodes.TEQ);
        } else {
            emitBinaryCompareAndEqualityExpr(equalExpr, InstructionCodes.IEQ);
        }
    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {
        // Handle type not equality as a special case.
        if ((notEqualExpr.getRExpr().getType() == notEqualExpr.getLExpr().getType())
                && notEqualExpr.getRExpr().getType() == BTypes.typeType) {
            emitBinaryTypeEqualityExpr(notEqualExpr, InstructionCodes.TNE);
        } else {
            emitBinaryCompareAndEqualityExpr(notEqualExpr, InstructionCodes.INE);
        }
    }
    // Binary comparison expressions

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {
        emitBinaryCompareAndEqualityExpr(greaterEqualExpr, InstructionCodes.IGE);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        emitBinaryCompareAndEqualityExpr(greaterThanExpr, InstructionCodes.IGT);
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        emitBinaryCompareAndEqualityExpr(lessEqualExpr, InstructionCodes.ILE);
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        emitBinaryCompareAndEqualityExpr(lessThanExpr, InstructionCodes.ILT);
    }


    // Callable unit invocation expressions

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        int funcCallIndex = getCallableUnitCallCPIndex(funcIExpr);
        // First check whether this is a function pointer invocation.
        if (funcIExpr.isFunctionPointerInvocation()
                && funcIExpr.getFunctionPointerVariableDef() instanceof SimpleVariableDef) {
            // Treat this as a SimpleVarRefExpr point to function pointer.
            // visiting this expression to load function pointer in to the refReg.
            SimpleVarRefExpr expr = new SimpleVarRefExpr(funcIExpr.getNodeLocation(), null, funcIExpr.getName());
            expr.setVariableDef(funcIExpr.getFunctionPointerVariableDef());
            expr.accept(this);
            // invoke loaded function.
            emit(InstructionCodes.FPCALL, regIndexes[REF_OFFSET], funcCallIndex);
            return;
        }
        // Else is normal function invocation.

        int pkgCPIndex = addPackageCPEntry(funcIExpr.getPackagePath());

        String funcName = funcIExpr.getName();
        UTF8CPEntry funcNameCPEntry = new UTF8CPEntry(funcName);
        int funcNameCPIndex = currentPkgInfo.addCPEntry(funcNameCPEntry);

        // Find the package info entry of the function and from the package info entry find the function info entry
        String pkgPath = funcIExpr.getPackagePath();
        PackageInfo funcPackageInfo = programFile.getPackageInfo(pkgPath);
        FunctionInfo functionInfo = funcPackageInfo.getFunctionInfo(funcName);

        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgCPIndex, funcIExpr.getPackagePath(),
                funcNameCPIndex, funcName);
        funcRefCPEntry.setFunctionInfo(functionInfo);
        int funcRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);

        if (functionInfo.isNative()) {
            // TODO Move this to the place where we create function info entry
            functionInfo.setNativeFunction((AbstractNativeFunction) funcIExpr.getCallableUnit());
            emit(InstructionCodes.NCALL, funcRefCPIndex, funcCallIndex);
        } else {
            emit(InstructionCodes.CALL, funcRefCPIndex, funcCallIndex);
        }
    }

    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        int pkgCPIndex = addPackageCPEntry(actionIExpr.getPackagePath());
        if (actionIExpr.isFunctionInvocation()) {
            // This is not an action invocation, but a filed based function invocation in a struct.
            int funcCallIndex = getCallableUnitCallCPIndex(actionIExpr);
            SimpleVarRefExpr expr = new SimpleVarRefExpr(actionIExpr.getNodeLocation(), null,
                    actionIExpr.getName());
            expr.setVariableDef(actionIExpr.getVariableDef());
            // Load function pointer.
            FieldBasedVarRefExpr fieldRefExpr = new FieldBasedVarRefExpr(actionIExpr.getNodeLocation(), null, expr, new
                    Identifier(actionIExpr.getName()));
            expr.setParentVarRefExpr(fieldRefExpr);
            fieldRefExpr.setFieldDef(actionIExpr.getFieldDef());
            fieldRefExpr.accept(this);
            // invoke loaded function.
            emit(InstructionCodes.FPCALL, regIndexes[REF_OFFSET], funcCallIndex);
            return;
        }
        BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) actionIExpr.getArgExprs()[0].getType();

        String pkgPath = actionIExpr.getPackagePath();
        PackageInfo actionPackageInfo = programFile.getPackageInfo(pkgPath);

        ConnectorInfo connectorInfo = actionPackageInfo.getConnectorInfo(connectorDef.getName());

        UTF8CPEntry connectorNameCPEntry = new UTF8CPEntry(connectorDef.getName());
        int connectorNameCPIndex = currentPkgInfo.addCPEntry(connectorNameCPEntry);
        StructureRefCPEntry connectorRefCPEntry = new StructureRefCPEntry(pkgCPIndex, actionIExpr.getPackagePath(),
                connectorNameCPIndex, connectorDef.getName());
        int connectorRefCPIndex = currentPkgInfo.addCPEntry(connectorRefCPEntry);

        String actionName = actionIExpr.getName();
        UTF8CPEntry actionNameCPEntry = new UTF8CPEntry(actionName);
        int actionNameCPIndex = currentPkgInfo.addCPEntry(actionNameCPEntry);

        ActionRefCPEntry actionRefCPEntry = new ActionRefCPEntry(pkgCPIndex, actionIExpr.getPackagePath(),
                connectorRefCPIndex, connectorRefCPEntry, actionNameCPIndex, actionName);
        ActionInfo actionInfo = connectorInfo.getActionInfo(actionName);
        actionRefCPEntry.setActionInfo(actionInfo);
        int actionRefCPIndex = currentPkgInfo.addCPEntry(actionRefCPEntry);
        int actionCallIndex = getCallableUnitCallCPIndex(actionIExpr);

        if (actionInfo.isNative()) {
            // TODO Move this to the place where we create action info entry
            actionInfo.setNativeAction((AbstractNativeAction) actionIExpr.getCallableUnit());
            emit(InstructionCodes.NACALL, actionRefCPIndex, actionCallIndex);
        } else {
            emit(InstructionCodes.ACALL, actionRefCPIndex, actionCallIndex);
        }

    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {

    }

    @Override
    public void visit(TypeCastExpression typeCastExpr) {
        Expression rExpr = typeCastExpr.getRExpr();
        rExpr.accept(this);

        // TODO Improve following logic
        int opCode = typeCastExpr.getOpcode();
        int errorRegIndex = ++regIndexes[REF_OFFSET];

        if (opCode == InstructionCodes.CHECKCAST) {
            TypeSignature typeSig = typeCastExpr.getType().getSig();
            UTF8CPEntry typeSigUTF8CPEntry = new UTF8CPEntry(typeSig.toString());
            int typeSigCPIndex = currentPkgInfo.addCPEntry(typeSigUTF8CPEntry);
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex, typeSig.toString());
            typeRefCPEntry.setType(getVMTypeFromSig(typeSig));
            int typeCPindex = currentPkgInfo.addCPEntry(typeRefCPEntry);
            int targetRegIndex = getNextIndex(typeCastExpr.getType().getTag(), regIndexes);

            typeCastExpr.setOffsets(new int[]{targetRegIndex, errorRegIndex});
            emit(opCode, rExpr.getTempOffset(), typeCPindex, targetRegIndex, errorRegIndex);

        } else if (opCode == InstructionCodes.ANY2T || opCode == InstructionCodes.ANY2C) {
            TypeSignature typeSig = typeCastExpr.getType().getSig();
            UTF8CPEntry typeSigUTF8CPEntry = new UTF8CPEntry(typeSig.toString());
            int typeSigCPIndex = currentPkgInfo.addCPEntry(typeSigUTF8CPEntry);
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex, typeSig.toString());
            typeRefCPEntry.setType(getVMTypeFromSig(typeSig));
            int typeCPindex = currentPkgInfo.addCPEntry(typeRefCPEntry);
            int targetRegIndex = getNextIndex(typeCastExpr.getType().getTag(), regIndexes);

            typeCastExpr.setOffsets(new int[]{targetRegIndex, errorRegIndex});
            emit(opCode, rExpr.getTempOffset(), typeCPindex, targetRegIndex, errorRegIndex);

        } else if (opCode != 0) {
            int targetRegIndex = getNextIndex(typeCastExpr.getType().getTag(), regIndexes);
            typeCastExpr.setOffsets(new int[]{targetRegIndex, errorRegIndex});
            emit(opCode, rExpr.getTempOffset(), targetRegIndex, errorRegIndex);

        } else {
            // Ignore NOP opcode
            typeCastExpr.setOffsets(new int[]{rExpr.getTempOffset(), errorRegIndex});
        }
    }

    @Override
    public void visit(TypeConversionExpr typeConversionExpr) {
        Expression rExpr = typeConversionExpr.getRExpr();
        rExpr.accept(this);

        int opCode = typeConversionExpr.getOpcode();
        int errorRegIndex = ++regIndexes[REF_OFFSET];

        if (opCode == InstructionCodes.MAP2T || opCode == InstructionCodes.JSON2T) {
            TypeSignature typeSig = typeConversionExpr.getType().getSig();
            UTF8CPEntry typeSigUTF8CPEntry = new UTF8CPEntry(typeSig.toString());
            int typeSigCPIndex = currentPkgInfo.addCPEntry(typeSigUTF8CPEntry);
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex, typeSig.toString());
            typeRefCPEntry.setType(getVMTypeFromSig(typeSig));
            int typeCPindex = currentPkgInfo.addCPEntry(typeRefCPEntry);
            int targetRegIndex = getNextIndex(typeConversionExpr.getType().getTag(), regIndexes);

            typeConversionExpr.setOffsets(new int[]{targetRegIndex, errorRegIndex});
            emit(opCode, rExpr.getTempOffset(), typeCPindex, targetRegIndex, errorRegIndex);

        } else if (opCode != 0) {
            int targetRegIndex = getNextIndex(typeConversionExpr.getType().getTag(), regIndexes);
            typeConversionExpr.setOffsets(new int[]{targetRegIndex, errorRegIndex});
            emit(opCode, rExpr.getTempOffset(), targetRegIndex, errorRegIndex);
        } else {
            // Ignore  NOP opcode
            typeConversionExpr.setOffsets(new int[]{rExpr.getTempOffset(), errorRegIndex});
        }
    }


    // Init expressions

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        BType elementType = ((BArrayType) arrayInitExpr.getType()).getElementType();

        TypeSignature typeSig = arrayInitExpr.getType().getSig();
        UTF8CPEntry typeSigUTF8CPEntry = new UTF8CPEntry(typeSig.toString());
        int typeSigCPIndex = currentPkgInfo.addCPEntry(typeSigUTF8CPEntry);
        TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex, typeSig.toString());
        typeRefCPEntry.setType(getVMTypeFromSig(typeSig));
        int typeCPindex = currentPkgInfo.addCPEntry(typeRefCPEntry);

        // Emit create array instruction
        int opcode = getOpcode(elementType.getTag(), InstructionCodes.INEWARRAY);
        int arrayVarRegIndex = ++regIndexes[REF_OFFSET];
        arrayInitExpr.setTempOffset(arrayVarRegIndex);
        emit(opcode, arrayVarRegIndex, typeCPindex);

        // Emit instructions populate initial array values;
        Expression[] argExprs = arrayInitExpr.getArgExprs();
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);

            BasicLiteral indexLiteral = new BasicLiteral(arrayInitExpr.getNodeLocation(),
                    null, new BInteger(i));
            indexLiteral.setType(BTypes.typeInt);
            indexLiteral.accept(this);

            opcode = getOpcode(argExpr.getType().getTag(), InstructionCodes.IASTORE);
            emit(opcode, arrayVarRegIndex, indexLiteral.getTempOffset(), argExpr.getTempOffset());
        }
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        int varRegIndex = ++regIndexes[REF_OFFSET];
        refTypeInitExpr.setTempOffset(varRegIndex);

        BType bType = refTypeInitExpr.getType();
        if (bType == BTypes.typeMessage) {
            emit(InstructionCodes.NEWMESSAGE, varRegIndex);
        } else if (bType == BTypes.typeDatatable) {
            emit(InstructionCodes.NEWDATATABLE, varRegIndex);
        }
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) connectorInitExpr.getType();
        PackageInfo connectorPkgInfo = programFile.getPackageInfo(connectorDef.getPackagePath());
        int pkgCPIndex = addPackageCPEntry(connectorDef.getPackagePath());

        UTF8CPEntry nameUTF8CPEntry = new UTF8CPEntry(connectorDef.getName());
        int nameIndex = currentPkgInfo.addCPEntry(nameUTF8CPEntry);

        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, connectorDef.getPackagePath(),
                nameIndex, connectorDef.getName());
        ConnectorInfo connectorInfo = connectorPkgInfo.getConnectorInfo(connectorDef.getName());
        connectorInfo.setFilterConnector(connectorDef.isFilterConnector());
        structureRefCPEntry.setStructureTypeInfo(connectorInfo);
        int structureRefCPIndex = currentPkgInfo.addCPEntry(structureRefCPEntry);

        //Emit an instruction to create a new connector.
        int connectorRegIndex = ++regIndexes[REF_OFFSET];
        ConnectorInitExpr filterConnectorInitExpr = connectorInitExpr.getParentConnectorInitExpr();
        emit(InstructionCodes.NEWCONNECTOR, structureRefCPIndex, connectorRegIndex);

        if (baseConnectorInfo == null) {
            if (filterConnectorInitExpr != null) {
                baseConnectorInfo = connectorInfo;
            }
        }

        if (baseConnectorInfo != null) {

            TypeSignature typeSig = connectorInitExpr.getInheritedType().getSig();
            UTF8CPEntry typeSigUTF8CPEntry = new UTF8CPEntry(typeSig.toString());
            int typeSigCPIndex = currentPkgInfo.addCPEntry(typeSigUTF8CPEntry);
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex, typeSig.toString());
            typeRefCPEntry.setType(getVMTypeFromSig(typeSig));
            int typeEntry = currentPkgInfo.addCPEntry(typeRefCPEntry);

            baseConnectorInfo.addMethodIndex(typeEntry, structureRefCPIndex);
            baseConnectorInfo.addMethodType((BConnectorType) getVMTypeFromSig(typeSig), connectorInfo);
        }
//        baseConnectorInfo.addMethodTypeStructure
//                ((BConnectorType) connectorInitExpr.getFilterSupportedType(), structureRefCPEntry);

        if (connectorInitExpr.getParentConnectorInitExpr() == null && !connectorDef.isFilterConnector()) {
            connectorInitExpr.setTempOffset(connectorRegIndex);
        }

        // Set all the connector arguments
        Expression[] argExprs = connectorInitExpr.getArgExprs();
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);
            int j = i;
            if (connectorDef.isFilterConnector()) {
                j += 1;
            }

            ParameterDef paramDef = connectorDef.getParameterDefs()[j];
            int fieldIndex = ((ConnectorVarLocation) paramDef.getMemoryLocation()).getConnectorMemAddrOffset();

            int opcode = getOpcode(paramDef.getType().getTag(), InstructionCodes.IFIELDSTORE);
            emit(opcode, connectorRegIndex, fieldIndex, argExpr.getTempOffset());
        }

        if (connectorDef.isFilterConnector()) {
            ParameterDef paramDef = connectorDef.getParameterDefs()[0];
            int fieldIndex = ((ConnectorVarLocation) paramDef.getMemoryLocation()).getConnectorMemAddrOffset();
            emit(InstructionCodes.RFIELDSTORE, connectorRegIndex, fieldIndex, baseConnectorIndex);
            lastFilterConnectorIndex = connectorRegIndex;
        }

        // Invoke Connector init function
        Function initFunction = connectorDef.getInitFunction();

        UTF8CPEntry nameCPEntry = new UTF8CPEntry(initFunction.getName());
        int initFuncNameIndex = currentPkgInfo.addCPEntry(nameCPEntry);

        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgCPIndex, connectorDef.getPackagePath(),
                initFuncNameIndex, initFunction.getName());
        funcRefCPEntry.setFunctionInfo(connectorPkgInfo.getFunctionInfo(initFunction.getName()));
        int initFuncRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);

        FunctionCallCPEntry initFuncCallCPEntry = new FunctionCallCPEntry(new int[]{connectorRegIndex}, new int[0]);
        int initFuncCallIndex = currentPkgInfo.addCPEntry(initFuncCallCPEntry);

        emit(InstructionCodes.CALL, initFuncRefCPIndex, initFuncCallIndex);

        baseConnectorIndex = connectorRegIndex;

        // Generate code for filterConnectors if there are any
        //ConnectorInitExpr filterConnectorInitExpr = connectorInitExpr.getParentConnectorInitExpr();
        if (filterConnectorInitExpr != null) {
            visit(filterConnectorInitExpr);
            connectorInitExpr.setTempOffset(lastFilterConnectorIndex);
        }

        baseConnectorInfo = null;
        // Invoke Connector init native action if any
        BallerinaAction action = connectorDef.getInitAction();
        if (action == null) {
            return;
        }

        String actionName = action.getName();
        UTF8CPEntry actionNameCPEntry = new UTF8CPEntry(actionName);
        int actionNameCPIndex = currentPkgInfo.addCPEntry(actionNameCPEntry);
        ActionRefCPEntry actionRefCPEntry = new ActionRefCPEntry(pkgCPIndex, connectorDef.getPackagePath(),
                structureRefCPIndex, structureRefCPEntry, actionNameCPIndex, actionName);

        ActionInfo actionInfo = connectorInfo.getActionInfo(actionName);
        actionRefCPEntry.setActionInfo(actionInfo);
        int actionRefCPIndex = currentPkgInfo.addCPEntry(actionRefCPEntry);

        actionInfo.setNativeAction((AbstractNativeAction) action.getNativeAction().load());
        actionInfo.setParamTypes(getParamTypes(connectorDef.getInitFunction().getParameterDefs()));
        emit(InstructionCodes.NACALL, actionRefCPIndex, initFuncCallIndex);
    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        StructDef structDef = (StructDef) structInitExpr.getType();
        int pkgCPIndex = addPackageCPEntry(structDef.getPackagePath());
        PackageInfo structDefPkgInfo = programFile.getPackageInfo(structDef.getPackagePath());

        UTF8CPEntry structNameCPEntry = new UTF8CPEntry(structDef.getName());
        int structNameCPIndex = currentPkgInfo.addCPEntry(structNameCPEntry);

        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, structDef.getPackagePath(),
                structNameCPIndex, structDef.getName());
        StructInfo structInfo = structDefPkgInfo.getStructInfo(structDef.getName());
        structureRefCPEntry.setStructureTypeInfo(structInfo);
        int structCPEntryIndex = currentPkgInfo.addCPEntry(structureRefCPEntry);

        //Emit an instruction to create a new struct.
        int structRegIndex = ++regIndexes[REF_OFFSET];
        emit(InstructionCodes.NEWSTRUCT, structCPEntryIndex, structRegIndex);
        structInitExpr.setTempOffset(structRegIndex);

        List<String> initializedFieldNameList = new ArrayList<>(structDef.getFieldDefStmts().length);

        for (Expression expr : structInitExpr.getArgExprs()) {
            KeyValueExpr keyValueExpr = (KeyValueExpr) expr;
            SimpleVarRefExpr varRefExpr = (SimpleVarRefExpr) keyValueExpr.getKeyExpr();
            int fieldIndex = ((StructVarLocation) varRefExpr.getMemoryLocation()).getStructMemAddrOffset();

            Expression valueExpr = keyValueExpr.getValueExpr();
            valueExpr.accept(this);

            int opcode = getOpcode(varRefExpr.getType().getTag(), InstructionCodes.IFIELDSTORE);
            emit(opcode, structRegIndex, fieldIndex, valueExpr.getTempOffset());
            initializedFieldNameList.add(varRefExpr.getVarName());
        }

        // Initialize default values in a struct definition
        for (VariableDefStmt fieldDefStmt : structDef.getFieldDefStmts()) {
            SimpleVarRefExpr varRefExpr = (SimpleVarRefExpr) fieldDefStmt.getLExpr();
            if (fieldDefStmt.getRExpr() == null || initializedFieldNameList.contains(varRefExpr.getVarName())) {
                continue;
            }

            int fieldIndex = ((StructVarLocation) varRefExpr.getMemoryLocation()).getStructMemAddrOffset();
            fieldDefStmt.getRExpr().accept(this);

            int opcode = getOpcode(varRefExpr.getType().getTag(), InstructionCodes.IFIELDSTORE);
            emit(opcode, structRegIndex, fieldIndex, fieldDefStmt.getRExpr().getTempOffset());
        }
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        int mapVarRegIndex = ++regIndexes[REF_OFFSET];
        mapInitExpr.setTempOffset(mapVarRegIndex);
        emit(InstructionCodes.NEWMAP, mapVarRegIndex);

        // Handle Map init stuff
        Expression[] argExprs = mapInitExpr.getArgExprs();
        for (Expression argExpr : argExprs) {
            KeyValueExpr keyValueExpr = (KeyValueExpr) argExpr;

            Expression keyExpr = keyValueExpr.getKeyExpr();
            keyExpr.accept(this);

            Expression valueExpr = keyValueExpr.getValueExpr();
            valueExpr.accept(this);

            emit(InstructionCodes.MAPSTORE, mapVarRegIndex, keyExpr.getTempOffset(), valueExpr.getTempOffset());
        }
    }

    @Override
    public void visit(JSONInitExpr jsonInitExpr) {
        int jsonVarRegIndex = ++regIndexes[REF_OFFSET];
        jsonInitExpr.setTempOffset(jsonVarRegIndex);
        emit(InstructionCodes.NEWJSON, jsonVarRegIndex);

        Expression[] argExprs = jsonInitExpr.getArgExprs();
        for (Expression argExpr : argExprs) {
            KeyValueExpr keyValueExpr = (KeyValueExpr) argExpr;

            Expression keyExpr = keyValueExpr.getKeyExpr();
            keyExpr.accept(this);

            Expression valueExpr = keyValueExpr.getValueExpr();
            valueExpr.accept(this);

            emit(InstructionCodes.JSONSTORE, jsonVarRegIndex, keyExpr.getTempOffset(), valueExpr.getTempOffset());
        }

    }

    @Override
    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {
        int jsonVarRegIndex = ++regIndexes[REF_OFFSET];
        jsonArrayInitExpr.setTempOffset(jsonVarRegIndex);
        Expression[] argExprs = jsonArrayInitExpr.getArgExprs();

        BasicLiteral arraySizeLiteral = new BasicLiteral(jsonArrayInitExpr.getNodeLocation(),
                null, new BInteger(argExprs.length));
        arraySizeLiteral.setType(BTypes.typeInt);
        arraySizeLiteral.accept(this);

        emit(InstructionCodes.JSONNEWARRAY, jsonVarRegIndex, arraySizeLiteral.getTempOffset());

        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);

            BasicLiteral indexLiteral = new BasicLiteral(jsonArrayInitExpr.getNodeLocation(),
                    null, new BInteger(i));
            indexLiteral.setType(BTypes.typeInt);
            indexLiteral.accept(this);

            emit(InstructionCodes.JSONASTORE, jsonVarRegIndex, indexLiteral.getTempOffset(), argExpr.getTempOffset());
        }
    }

    @Override
    public void visit(KeyValueExpr keyValueExpr) {

    }


    // Variable reference expressions

    @Override
    public void visit(SimpleVarRefExpr simpleVarRefExpr) {
        int opcode;
        int exprRegIndex;
        boolean variableStore = varAssignment && simpleVarRefExpr.getParentVarRefExpr() == null;

        MemoryLocation memoryLocation = simpleVarRefExpr.getVariableDef().getMemoryLocation();
        if (memoryLocation instanceof StackVarLocation) {
            int lvIndex = ((StackVarLocation) memoryLocation).getStackFrameOffset();
            if (variableStore) {
                opcode = getOpcode(simpleVarRefExpr.getType().getTag(),
                        InstructionCodes.ISTORE);

                emit(opcode, rhsExprRegIndex, lvIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(simpleVarRefExpr.getType().getTag(),
                        InstructionCodes.ILOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;
                emit(opcode, lvIndex, exprRegIndex);
                simpleVarRefExpr.setTempOffset(exprRegIndex);
            }

        } else if (memoryLocation instanceof StructVarLocation) {
            int fieldIndex = ((StructVarLocation) memoryLocation).getStructMemAddrOffset();

            // Since we are processing a struct field here, the struct reference must be stored in the current
            //  reference register index.
            int structRegIndex = regIndexes[REF_OFFSET];

            if (variableStore) {
                opcode = getOpcode(simpleVarRefExpr.getType().getTag(),
                        InstructionCodes.IFIELDSTORE);
                emit(opcode, structRegIndex, fieldIndex, rhsExprRegIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(simpleVarRefExpr.getType().getTag(),
                        InstructionCodes.IFIELDLOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;

                emit(opcode, structRegIndex, fieldIndex, exprRegIndex);
                simpleVarRefExpr.setTempOffset(exprRegIndex);
            }

        } else if (memoryLocation instanceof ConnectorVarLocation) {
            int fieldIndex = ((ConnectorVarLocation) memoryLocation).getConnectorMemAddrOffset();

            // Since we are processing a connector field here, the connector reference must be stored in the current
            //  reference register index.
            int connectorRegIndex = ++regIndexes[REF_OFFSET];

            // The connector is always the first parameter of the action
            emit(InstructionCodes.RLOAD, 0, connectorRegIndex);

            if (variableStore) {
                opcode = getOpcode(simpleVarRefExpr.getType().getTag(),
                        InstructionCodes.IFIELDSTORE);
                emit(opcode, connectorRegIndex, fieldIndex, rhsExprRegIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(simpleVarRefExpr.getType().getTag(),
                        InstructionCodes.IFIELDLOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;

                emit(opcode, connectorRegIndex, fieldIndex, exprRegIndex);
                simpleVarRefExpr.setTempOffset(exprRegIndex);
            }

        } else if (memoryLocation instanceof GlobalVarLocation) {
            int gvIndex = ((GlobalVarLocation) memoryLocation).getStaticMemAddrOffset();

            if (variableStore) {
                opcode = getOpcode(simpleVarRefExpr.getType().getTag(),
                        InstructionCodes.IGSTORE);

                emit(opcode, rhsExprRegIndex, gvIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(simpleVarRefExpr.getType().getTag(),
                        InstructionCodes.IGLOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;
                emit(opcode, gvIndex, exprRegIndex);
                simpleVarRefExpr.setTempOffset(exprRegIndex);
            }
        }

        // Check whether this is a function pointer pointing to ballerina/native function. Then load it to refReg.
        if (!variableStore && simpleVarRefExpr.getVariableDef() instanceof Function) {

            Function function = (Function) simpleVarRefExpr.getVariableDef();
            String pkgPath = function.getPackagePath();
            int pkgCPIndex = addPackageCPEntry(pkgPath);
            String funcName = function.getName();
            UTF8CPEntry funcNameCPEntry = new UTF8CPEntry(funcName);
            int funcNameCPIndex = currentPkgInfo.addCPEntry(funcNameCPEntry);

            // Find the package info entry of the function and from the package info entry find the function info entry
            PackageInfo funcPackageInfo = programFile.getPackageInfo(pkgPath);
            FunctionInfo functionInfo = funcPackageInfo.getFunctionInfo(funcName);

            FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgCPIndex, pkgPath, funcNameCPIndex, funcName);
            funcRefCPEntry.setFunctionInfo(functionInfo);
            int funcRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);
            int nextIndex = getNextIndex(TypeTags.FUNCTION_POINTER_TAG, regIndexes);
            simpleVarRefExpr.setTempOffset(nextIndex);
            emit(InstructionCodes.FPLOAD, funcRefCPIndex, nextIndex);
        }
    }

    @Override
    public void visit(FieldBasedVarRefExpr fieldBasedVarRefExpr) {
        boolean variableStore = structAssignment && fieldBasedVarRefExpr.getParentVarRefExpr() == null;
        String fieldName = fieldBasedVarRefExpr.getFieldName();
        VariableReferenceExpr varRefExpr = fieldBasedVarRefExpr.getVarRefExpr();
        varRefExpr.accept(this);
        int varRefRegIndex = varRefExpr.getTempOffset();

        // Type of the varRefExpr can be either Struct, Map, JSON, Array
        BType varRefType = varRefExpr.getType();
        if (varRefType instanceof StructDef) {
            int opcode;
            int fieldValRegIndex;
            VariableDef fieldDef = fieldBasedVarRefExpr.getFieldDef();
            int fieldIndex = ((StructVarLocation) fieldDef.getMemoryLocation()).getStructMemAddrOffset();
            if (variableStore) {
                opcode = getOpcode(fieldDef.getType().getTag(), InstructionCodes.IFIELDSTORE);
                emit(opcode, varRefRegIndex, fieldIndex, rhsExprRegIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(fieldDef.getType().getTag(),
                        InstructionCodes.IFIELDLOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                fieldValRegIndex = opcodeAndIndex.index;

                emit(opcode, varRefRegIndex, fieldIndex, fieldValRegIndex);
                fieldBasedVarRefExpr.setTempOffset(fieldValRegIndex);
            }

        } else if (varRefType == BTypes.typeMap) {
            BasicLiteral indexLiteral = new BasicLiteral(fieldBasedVarRefExpr.getNodeLocation(), null,
                    new BString(fieldName));
            indexLiteral.setType(BTypes.typeString);
            indexLiteral.accept(this);
            int indexValueRegIndex = indexLiteral.getTempOffset();

            if (variableStore) {
                emit(InstructionCodes.MAPSTORE, varRefRegIndex, indexValueRegIndex, rhsExprRegIndex);
            } else {
                int mapValueRegIndex = ++regIndexes[REF_OFFSET];
                emit(InstructionCodes.MAPLOAD, varRefRegIndex, indexValueRegIndex, mapValueRegIndex);
                fieldBasedVarRefExpr.setTempOffset(mapValueRegIndex);
            }

        } else if (varRefType == BTypes.typeJSON || varRefType instanceof BJSONConstraintType) {
            BasicLiteral indexLiteral = new BasicLiteral(fieldBasedVarRefExpr.getNodeLocation(), null,
                    new BString(fieldName));
            indexLiteral.setType(BTypes.typeString);
            indexLiteral.accept(this);
            int indexValueRegIndex = indexLiteral.getTempOffset();

            if (variableStore) {
                emit(InstructionCodes.JSONSTORE, varRefRegIndex, indexValueRegIndex, rhsExprRegIndex);
            } else {
                int jsonValueRegIndex = ++regIndexes[REF_OFFSET];
                emit(InstructionCodes.JSONLOAD, varRefRegIndex, indexValueRegIndex, jsonValueRegIndex);
                fieldBasedVarRefExpr.setTempOffset(jsonValueRegIndex);
            }
        } else if (varRefType instanceof BArrayType && fieldName.equals("length")) {
            int lengthValRegIndex = ++regIndexes[INT_OFFSET];
            emit(InstructionCodes.ARRAYLEN, varRefRegIndex, lengthValRegIndex);
            fieldBasedVarRefExpr.setTempOffset(lengthValRegIndex);
        }
    }

    @Override
    public void visit(IndexBasedVarRefExpr indexBasedVarRefExpr) {
        boolean variableStore = arrayMapAssignment && indexBasedVarRefExpr.getParentVarRefExpr() == null;
        VariableReferenceExpr varRefExpr = indexBasedVarRefExpr.getVarRefExpr();
        varRefExpr.accept(this);
        int varRefRegIndex = varRefExpr.getTempOffset();

        Expression indexExpr = indexBasedVarRefExpr.getIndexExpr();
        indexExpr.accept(this);
        int indexValueRegIndex = indexExpr.getTempOffset();

        // Type of the varRefExpr can be either Array, Map, JSON.
        BType varRefType = varRefExpr.getType();

        // We check for JSON first, because a JSON array is also a JSON, and is accessed different to
        // the other array types.
        if (getElementType(varRefType) == BTypes.typeJSON) {
            int jsonValueRegIndex;
            if (indexExpr.getType() == BTypes.typeString) {
                if (variableStore) {
                    emit(InstructionCodes.JSONSTORE, varRefRegIndex, indexValueRegIndex, rhsExprRegIndex);
                } else {
                    jsonValueRegIndex = ++regIndexes[REF_OFFSET];
                    emit(InstructionCodes.JSONLOAD, varRefRegIndex, indexValueRegIndex, jsonValueRegIndex);
                    indexBasedVarRefExpr.setTempOffset(jsonValueRegIndex);
                }
            } else if (indexExpr.getType() == BTypes.typeInt) {
                // JSON array access
                if (variableStore) {
                    emit(InstructionCodes.JSONASTORE, varRefRegIndex, indexValueRegIndex, rhsExprRegIndex);
                } else {
                    jsonValueRegIndex = ++regIndexes[REF_OFFSET];
                    emit(InstructionCodes.JSONALOAD, varRefRegIndex, indexValueRegIndex, jsonValueRegIndex);
                    indexBasedVarRefExpr.setTempOffset(jsonValueRegIndex);
                }
            }

        } else if (varRefType instanceof BArrayType) {
            BArrayType arrayType = (BArrayType) varRefType;
            if (variableStore) {
                int opcode = getOpcode(arrayType.getElementType().getTag(), InstructionCodes.IASTORE);
                emit(opcode, varRefRegIndex, indexValueRegIndex, rhsExprRegIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(arrayType.getElementType().getTag(),
                        InstructionCodes.IALOAD, regIndexes);
                emit(opcodeAndIndex.opcode, varRefRegIndex, indexValueRegIndex, opcodeAndIndex.index);
                indexBasedVarRefExpr.setTempOffset(opcodeAndIndex.index);
            }

        } else if (varRefType == BTypes.typeMap) {
            if (variableStore) {
                emit(InstructionCodes.MAPSTORE, varRefRegIndex, indexValueRegIndex, rhsExprRegIndex);
            } else {
                int mapValueRegIndex = ++regIndexes[REF_OFFSET];
                emit(InstructionCodes.MAPLOAD, varRefRegIndex, indexValueRegIndex, mapValueRegIndex);
                indexBasedVarRefExpr.setTempOffset(mapValueRegIndex);
            }

        } else if (varRefType instanceof StructDef) {
            int opcode;
            int fieldValRegIndex;
            VariableDef fieldDef = indexBasedVarRefExpr.getFieldDef();
            int fieldIndex = ((StructVarLocation) fieldDef.getMemoryLocation()).getStructMemAddrOffset();
            if (variableStore) {
                opcode = getOpcode(fieldDef.getType().getTag(), InstructionCodes.IFIELDSTORE);
                emit(opcode, varRefRegIndex, fieldIndex, rhsExprRegIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(fieldDef.getType().getTag(),
                        InstructionCodes.IFIELDLOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                fieldValRegIndex = opcodeAndIndex.index;

                emit(opcode, varRefRegIndex, fieldIndex, fieldValRegIndex);
                indexBasedVarRefExpr.setTempOffset(fieldValRegIndex);
            }
        }
    }

    @Override
    public void visit(XMLAttributesRefExpr xmlAttributesRefExpr) {
        boolean variableStore = arrayMapAssignment && xmlAttributesRefExpr.getParentVarRefExpr() == null;
        VariableReferenceExpr varRefExpr = xmlAttributesRefExpr.getVarRefExpr();
        varRefExpr.accept(this);
        int varRefRegIndex = varRefExpr.getTempOffset();

        Expression indexExpr = xmlAttributesRefExpr.getIndexExpr();
        if (indexExpr == null) {
            int xmlValueRegIndex = ++regIndexes[REF_OFFSET];
            emit(InstructionCodes.XML2XMLATTRS, varRefRegIndex, xmlValueRegIndex);
            xmlAttributesRefExpr.setTempOffset(xmlValueRegIndex);
            return;
        }

        indexExpr.accept(this);
        int qnameRegIndex = indexExpr.getTempOffset();

        // If this is a string representation of qname
        if (!(indexExpr instanceof XMLQNameExpr)) {
            int localNameRegIndex = ++regIndexes[STRING_OFFSET];
            int uriRegIndex = ++regIndexes[STRING_OFFSET];
            emit(InstructionCodes.S2QNAME, qnameRegIndex, localNameRegIndex, uriRegIndex);

            qnameRegIndex = ++regIndexes[REF_OFFSET];
            generateUriLookupInstructions(xmlAttributesRefExpr.getNamespaces(), localNameRegIndex, uriRegIndex,
                    qnameRegIndex, xmlAttributesRefExpr.getNodeLocation());
        }

        if (variableStore) {
            emit(InstructionCodes.XMLATTRSTORE, varRefRegIndex, qnameRegIndex, rhsExprRegIndex);
        } else {
            int xmlValueRegIndex = ++regIndexes[REF_OFFSET];
            emit(InstructionCodes.XMLATTRLOAD, varRefRegIndex, qnameRegIndex, xmlValueRegIndex);
            xmlAttributesRefExpr.setTempOffset(xmlValueRegIndex);
        }
    }

    @Override
    public void visit(XMLQNameExpr xmlQNameRefExpr) {
        // If the QName is use outside of XML, treat it as string.
        if (!xmlQNameRefExpr.isUsedInXML()) {
            String qName;
            if (xmlQNameRefExpr.getNamepsaceUri() != null) {
                qName = "{" + ((BasicLiteral) xmlQNameRefExpr.getNamepsaceUri()).getBValue().stringValue() + "}"
                        + xmlQNameRefExpr.getLocalname();
            } else {
                qName = xmlQNameRefExpr.getLocalname();
            }

            BasicLiteral qNameLiteral = new BasicLiteral(xmlQNameRefExpr.getNodeLocation(),
                    null, new BString(qName));
            qNameLiteral.setType(BTypes.typeString);
            qNameLiteral.accept(this);
            xmlQNameRefExpr.setTempOffset(qNameLiteral.getTempOffset());
            return;
        }

        // Else, treat it as QName

        Expression namespaceUriLiteral = xmlQNameRefExpr.getNamepsaceUri();
        namespaceUriLiteral.accept(this);

        BasicLiteral localNameLiteral =
                new BasicLiteral(xmlQNameRefExpr.getNodeLocation(), null, new BString(xmlQNameRefExpr.getLocalname()));
        localNameLiteral.setType(BTypes.typeString);
        localNameLiteral.accept(this);

        BasicLiteral prefixLiteral =
                new BasicLiteral(xmlQNameRefExpr.getNodeLocation(), null, new BString(xmlQNameRefExpr.getPrefix()));
        prefixLiteral.setType(BTypes.typeString);
        prefixLiteral.accept(this);

        int qnameLoadedRegIndex = ++regIndexes[REF_OFFSET];
        emit(InstructionCodes.NEWQNAME, localNameLiteral.getTempOffset(), namespaceUriLiteral.getTempOffset(),
                prefixLiteral.getTempOffset(), qnameLoadedRegIndex);
        xmlQNameRefExpr.setTempOffset(qnameLoadedRegIndex);
    }

    @Override
    public void visit(XMLLiteral xmlLiteral) {
    }

    @Override
    public void visit(XMLElementLiteral xmlElementLiteral) {
        int xmlVarRegIndex = ++regIndexes[REF_OFFSET];
        xmlElementLiteral.setTempOffset(xmlVarRegIndex);

        Expression startTagName = xmlElementLiteral.getStartTagName();
        startTagName.accept(this);
        int startTagNameRegIndex = startTagName.getTempOffset();
        
        // If this is a string representation of element name
        if (!(startTagName instanceof XMLQNameExpr)) {
            int localNameRegIndex = ++regIndexes[STRING_OFFSET];
            int uriRegIndex = ++regIndexes[STRING_OFFSET];
            emit(InstructionCodes.S2QNAME, startTagNameRegIndex, localNameRegIndex, uriRegIndex);

            startTagNameRegIndex = ++regIndexes[REF_OFFSET];
            generateUriLookupInstructions(xmlElementLiteral.getNamespaces(), localNameRegIndex, uriRegIndex,
                    startTagNameRegIndex, xmlElementLiteral.getNodeLocation());
        }

        Expression endTagName = xmlElementLiteral.getEndTagName();
        int endTagNameRegIndex;
        if (endTagName != null) {
            endTagName.accept(this);
            endTagNameRegIndex = endTagName.getTempOffset();
            
            // If this is a string representation of element name
            if (!(endTagName instanceof XMLQNameExpr)) {
                int localNameRegIndex = ++regIndexes[STRING_OFFSET];
                int uriRegIndex = ++regIndexes[STRING_OFFSET];
                emit(InstructionCodes.S2QNAME, endTagNameRegIndex, localNameRegIndex, uriRegIndex);
                
                endTagNameRegIndex = ++regIndexes[REF_OFFSET];
                generateUriLookupInstructions(xmlElementLiteral.getNamespaces(), localNameRegIndex, uriRegIndex,
                        endTagNameRegIndex, xmlElementLiteral.getNodeLocation());
            }
        } else {
            endTagNameRegIndex = startTagNameRegIndex;
        }

        Expression defaultNamespaceUri = xmlElementLiteral.getDefaultNamespaceUri();
        defaultNamespaceUri.accept(this);

        // Create an empty xml with the given QName
        emit(InstructionCodes.NEWXMLELEMENT, xmlVarRegIndex, startTagNameRegIndex, endTagNameRegIndex,
                defaultNamespaceUri.getTempOffset());

        // Add namespaces in the current scope, as attributes to the XML
        addNamespacesToXML(xmlElementLiteral.getNamespaces(), xmlVarRegIndex, defaultNamespaceUri.getTempOffset(),
                xmlElementLiteral.getNodeLocation());

        // Add attributes
        int attrQnameRegIndex;
        List<KeyValueExpr> attributes = xmlElementLiteral.getAttributes();
        for (KeyValueExpr attribute : attributes) {
            Expression attrNameExpr = attribute.getKeyExpr();
            attrNameExpr.accept(this);
            attrQnameRegIndex = attrNameExpr.getTempOffset();

            // If this is a string representation of qname
            if (!(attrNameExpr instanceof XMLQNameExpr)) {
                int localNameRegIndex = ++regIndexes[STRING_OFFSET];
                int uriRegIndex = ++regIndexes[STRING_OFFSET];
                emit(InstructionCodes.S2QNAME, attrQnameRegIndex, localNameRegIndex, uriRegIndex);

                attrQnameRegIndex = ++regIndexes[REF_OFFSET];
                generateUriLookupInstructions(new HashMap<>(), localNameRegIndex, uriRegIndex, attrQnameRegIndex,
                        xmlElementLiteral.getNodeLocation());
            }

            Expression attrValueExpr = attribute.getValueExpr();
            attrValueExpr.accept(this);

            emit(InstructionCodes.XMLATTRSTORE, xmlVarRegIndex, attrQnameRegIndex, attrValueExpr.getTempOffset());
        }

        // Add children
        XMLSequenceLiteral children = xmlElementLiteral.getContent();
        if (children != null && !children.isEmpty()) {
            children.accept(this);
            emit(InstructionCodes.XMLSTORE, xmlVarRegIndex, children.getTempOffset());
        }
    }

    @Override
    public void visit(XMLCommentLiteral xmlComment) {
        int xmlVarRegIndex = ++regIndexes[REF_OFFSET];
        xmlComment.setTempOffset(xmlVarRegIndex);

        Expression contentExpr = xmlComment.getContent();
        contentExpr.accept(this);

        // Create an XML comment item
        emit(InstructionCodes.NEWXMLCOMMENT, xmlVarRegIndex, contentExpr.getTempOffset());
    }

    @Override
    public void visit(XMLTextLiteral xmlText) {
        int xmlVarRegIndex = ++regIndexes[REF_OFFSET];
        xmlText.setTempOffset(xmlVarRegIndex);

        Expression contentExpr = xmlText.getContent();
        contentExpr.accept(this);

        // Create an XML text item
        emit(InstructionCodes.NEWXMLTEXT, xmlVarRegIndex, contentExpr.getTempOffset());
    }

    @Override
    public void visit(XMLPILiteral xmlPI) {
        int xmlVarRegIndex = ++regIndexes[REF_OFFSET];
        xmlPI.setTempOffset(xmlVarRegIndex);

        Expression target = xmlPI.getTarget();
        target.accept(this);

        Expression data = xmlPI.getData();
        data.accept(this);

        // Create an XML text item
        emit(InstructionCodes.NEWXMLPI, xmlVarRegIndex, target.getTempOffset(), data.getTempOffset());
    }

    @Override
    public void visit(XMLSequenceLiteral xmlSequence) {
        Expression concatExpr = xmlSequence.getConcatExpr();
        concatExpr.accept(this);
        xmlSequence.setTempOffset(concatExpr.getTempOffset());
    }

    // Private methods

    private void endWorkerInfoUnit(CodeAttributeInfo codeAttributeInfo) {
        codeAttributeInfo.setMaxLongLocalVars(lvIndexes[INT_OFFSET] + 1);
        codeAttributeInfo.setMaxDoubleLocalVars(lvIndexes[FLOAT_OFFSET] + 1);
        codeAttributeInfo.setMaxStringLocalVars(lvIndexes[STRING_OFFSET] + 1);
        codeAttributeInfo.setMaxIntLocalVars(lvIndexes[BOOL_OFFSET] + 1);
        codeAttributeInfo.setMaxByteLocalVars(lvIndexes[BLOB_OFFSET] + 1);
        codeAttributeInfo.setMaxRefLocalVars(lvIndexes[REF_OFFSET] + 1);

        codeAttributeInfo.setMaxLongRegs(maxRegIndexes[INT_OFFSET] + 1);
        codeAttributeInfo.setMaxDoubleRegs(maxRegIndexes[FLOAT_OFFSET] + 1);
        codeAttributeInfo.setMaxStringRegs(maxRegIndexes[STRING_OFFSET] + 1);
        codeAttributeInfo.setMaxIntRegs(maxRegIndexes[BOOL_OFFSET] + 1);
        codeAttributeInfo.setMaxByteRegs(maxRegIndexes[BLOB_OFFSET] + 1);
        codeAttributeInfo.setMaxRefRegs(maxRegIndexes[REF_OFFSET] + 1);

        resetIndexes(lvIndexes);
        resetIndexes(regIndexes);
    }

    private void resetIndexes(int[] indexes) {
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = -1;
        }
    }

    private int[] prepareIndexes(int[] indexes) {
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] += 1;
        }
        return indexes;
    }

    private OpcodeAndIndex getOpcodeAndIndex(int typeTag, int baseOpcode, int[] indexes) {
        int index;
        int opcode;
        switch (typeTag) {
            case TypeTags.INT_TAG:
                opcode = baseOpcode;
                index = ++indexes[INT_OFFSET];
                break;
            case TypeTags.FLOAT_TAG:
                opcode = baseOpcode + FLOAT_OFFSET;
                index = ++indexes[FLOAT_OFFSET];
                break;
            case TypeTags.STRING_TAG:
                opcode = baseOpcode + STRING_OFFSET;
                index = ++indexes[STRING_OFFSET];
                break;
            case TypeTags.BOOLEAN_TAG:
                opcode = baseOpcode + BOOL_OFFSET;
                index = ++indexes[BOOL_OFFSET];
                break;
            case TypeTags.BLOB_TAG:
                opcode = baseOpcode + BLOB_OFFSET;
                index = ++indexes[BLOB_OFFSET];
                break;
            default:
                opcode = baseOpcode + REF_OFFSET;
                index = ++indexes[REF_OFFSET];
                break;
        }

        return new OpcodeAndIndex(opcode, index);
    }

    private int getNextIndex(int typeTag, int[] indexes) {
        return getOpcodeAndIndex(typeTag, -1, indexes).index;
    }

    private int getOpcode(int typeTag, int baseOpcode) {
        int opcode;
        switch (typeTag) {
            case TypeTags.INT_TAG:
                opcode = baseOpcode;
                break;
            case TypeTags.FLOAT_TAG:
                opcode = baseOpcode + FLOAT_OFFSET;
                break;
            case TypeTags.STRING_TAG:
                opcode = baseOpcode + STRING_OFFSET;
                break;
            case TypeTags.BOOLEAN_TAG:
                opcode = baseOpcode + BOOL_OFFSET;
                break;
            case TypeTags.BLOB_TAG:
                opcode = baseOpcode + BLOB_OFFSET;
                break;
            default:
                opcode = baseOpcode + REF_OFFSET;
                break;
        }

        return opcode;
    }

    private BType[] getParamTypes(ParameterDef[] paramDefs) {
        if (paramDefs.length == 0) {
            return new BType[0];
        }

        BType[] types = new BType[paramDefs.length];
        for (int i = 0; i < paramDefs.length; i++) {
            types[i] = getVMTypeFromSig(paramDefs[i].getType().getSig());
        }

        return types;
    }

    private void setParameterNames(ParameterDef[] paramDefs, ResourceInfo resourceInfo) {
        if (paramDefs.length == 0) {
            resourceInfo.setParamNameCPIndexes(new int[0]);
            resourceInfo.setParamNames(new String[0]);
        }

        int[] paramNameCPIndexes = new int[paramDefs.length];
        String[] paramNames = new String[paramDefs.length];
        for (int i = 0; i < paramDefs.length; i++) {
            ParameterDef paramDef = paramDefs[i];

            // TODO Find a better way to solve the following problem.
            AnnotationAttachment[] attachments = paramDefs[i].getAnnotations();
            boolean isAnnotated = false;
            for (AnnotationAttachment annotationAttachment : attachments) {
                if ("PathParam".equalsIgnoreCase(annotationAttachment.getName())
                        || "QueryParam".equalsIgnoreCase(annotationAttachment.getName())) {
                    paramNames[i] = annotationAttachment.getAttributeNameValuePairs()
                            .get("value").getLiteralValue().stringValue();
                    isAnnotated = true;
                    break;
                }
            }

            if (!isAnnotated) {
                paramNames[i] = paramDef.getName();
            }

            UTF8CPEntry paramNameCPEntry = new UTF8CPEntry(paramNames[i]);
            int paramNameCPIndex = currentPkgInfo.addCPEntry(paramNameCPEntry);
            paramNameCPIndexes[i] = paramNameCPIndex;
        }

        resourceInfo.setParamNameCPIndexes(paramNameCPIndexes);
        resourceInfo.setParamNames(paramNames);
    }

    private int nextIP() {
        return currentPkgInfo.getInstructionCount();
    }

    private void emitBinaryArithmeticExpr(BinaryArithmeticExpression expr, int baseOpcode) {
        expr.getLExpr().accept(this);
        expr.getRExpr().accept(this);

        OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(expr.getType().getTag(), baseOpcode, regIndexes);
        int opcode = opcodeAndIndex.opcode;
        int exprIndex = opcodeAndIndex.index;

        expr.setTempOffset(exprIndex);
        emit(opcode, expr.getLExpr().getTempOffset(), expr.getRExpr().getTempOffset(), exprIndex);
    }

    private void emitBinaryCompareAndEqualityExpr(BinaryExpression binaryExpr, int baseOpcode) {
        Expression lExpr = binaryExpr.getLExpr();
        lExpr.accept(this);

        Expression rExpr = binaryExpr.getRExpr();
        rExpr.accept(this);

        // TODO Verify NPE Checks
//        if (isNullCheckAvailable(binaryExpr)) {
//            if (lExpr.getType() == BTypes.typeNull) {
//                binaryExpr.setTempOffset(rExpr.getTempOffset());
//            } else {
//                binaryExpr.setTempOffset(lExpr.getTempOffset());
//            }
//            return;
//        }

        int opcode = getOpcode(lExpr.getType().getTag(), baseOpcode);
        int exprIndex = ++regIndexes[BOOL_OFFSET];
        binaryExpr.setTempOffset(exprIndex);
        emit(opcode, lExpr.getTempOffset(), rExpr.getTempOffset(), exprIndex);
    }

    private void emitBinaryTypeEqualityExpr(BinaryExpression binaryExpr, int baseOpcode) {
        Expression lExpr = binaryExpr.getLExpr();
        lExpr.accept(this);

        Expression rExpr = binaryExpr.getRExpr();
        rExpr.accept(this);

        int exprIndex = ++regIndexes[BOOL_OFFSET];
        binaryExpr.setTempOffset(exprIndex);
        emit(baseOpcode, lExpr.getTempOffset(), rExpr.getTempOffset(), exprIndex);
    }

    private int emit(int opcode, int... operands) {
        return currentPkgInfo.addInstruction(InstructionFactory.get(opcode, operands));
    }

    private int emit(Instruction instruction) {
        return currentPkgInfo.addInstruction(instruction);
    }

    private BType getVMTypeFromSig(TypeSignature typeSig) {
        PackageInfo packageInfo;

        switch (typeSig.getSigChar()) {
            case TypeSignature.SIG_INT:
                return BTypes.typeInt;
            case TypeSignature.SIG_FLOAT:
                return BTypes.typeFloat;
            case TypeSignature.SIG_STRING:
                return BTypes.typeString;
            case TypeSignature.SIG_BOOLEAN:
                return BTypes.typeBoolean;
            case TypeSignature.SIG_BLOB:
                return BTypes.typeBlob;
            case TypeSignature.SIG_REFTYPE:
                return BTypes.getTypeFromName(typeSig.getName());
            case TypeSignature.SIG_CJSON:
                packageInfo = programFile.getPackageInfo(typeSig.getPkgPath());
                StructInfo structInf = packageInfo.getStructInfo(typeSig.getName());
                return new BJSONConstraintType(structInf.getType());
            case TypeSignature.SIG_ANY:
                return BTypes.typeAny;
            case TypeSignature.SIG_TYPE:
                return BTypes.typeType;
            case TypeSignature.SIG_STRUCT:
                packageInfo = programFile.getPackageInfo(typeSig.getPkgPath());
                StructInfo structInfo = packageInfo.getStructInfo(typeSig.getName());
                return structInfo.getType();
            case TypeSignature.SIG_CONNECTOR:
                packageInfo = programFile.getPackageInfo(typeSig.getPkgPath());
                ConnectorInfo connectorInfo = packageInfo.getConnectorInfo(typeSig.getName());
                return connectorInfo.getType();
            case TypeSignature.SIG_ARRAY:
                TypeSignature elementTypeSig = typeSig.getElementTypeSig();
                BType elementType = getVMTypeFromSig(elementTypeSig);
                return new BArrayType(elementType);
            case TypeSignature.SIG_FUNCTION:
                // TODO : Fix this for type casting.
                return new BFunctionType();
            default:
                throw new IllegalStateException("Unknown type signature");
        }
    }

    private int addPackageCPEntry(String pkgPath) {
        pkgPath = (pkgPath != null) ? pkgPath : ".";
        UTF8CPEntry pkgNameCPEntry = new UTF8CPEntry(pkgPath);
        int pkgNameIndex = currentPkgInfo.addCPEntry(pkgNameCPEntry);

        PackageRefCPEntry pkgCPEntry = new PackageRefCPEntry(pkgNameIndex, pkgPath);
        // Cache Value.
        pkgCPEntry.setPackageInfo(programFile.getPackageInfo(pkgPath));
        return currentPkgInfo.addCPEntry(pkgCPEntry);
    }

    private int getWorkerInvocationCPIndex(WorkerInvocationStmt workerInvocationStmt) {
        Expression[] argExprs = workerInvocationStmt.getExpressionList();
        int[] argRegs = new int[argExprs.length];
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);
            argRegs[i] = argExpr.getTempOffset();
        }

        BType[] bTypes = workerInvocationStmt.getTypes();
        WrkrInteractionArgsCPEntry workerInvokeCPEntry = new WrkrInteractionArgsCPEntry(argRegs, bTypes);

        StringBuilder strBuilder = new StringBuilder("");
        for (BType paramType : bTypes) {
            strBuilder.append(paramType.getSig());
        }

        String sig = strBuilder.toString();

        UTF8CPEntry sigCPEntry = new UTF8CPEntry(sig);
        int sigCPIndex = currentPkgInfo.addCPEntry(sigCPEntry);

        workerInvokeCPEntry.setTypesSignatureCPIndex(sigCPIndex);

        return currentPkgInfo.addCPEntry(workerInvokeCPEntry);
    }

    private int getWorkerReplyCPIndex(WorkerReplyStmt workerReplyStmt) {

        // Calculate registers to store return values
        BType[] retTypes = workerReplyStmt.getTypes();
        int[] argRegs = new int[retTypes.length];
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = retTypes[i];
            argRegs[i] = getNextIndex(retType.getTag(), regIndexes);
        }

        workerReplyStmt.setOffsets(argRegs);
        WrkrInteractionArgsCPEntry wrkrRplyCPEntry = new WrkrInteractionArgsCPEntry(argRegs,
                workerReplyStmt.getTypes());
        StringBuilder strBuilder = new StringBuilder("");
        for (BType paramType : workerReplyStmt.getTypes()) {
            strBuilder.append(paramType.getSig());
        }

        String sig = strBuilder.toString();

        UTF8CPEntry sigCPEntry = new UTF8CPEntry(sig);
        int sigCPIndex = currentPkgInfo.addCPEntry(sigCPEntry);

        wrkrRplyCPEntry.setTypesSignatureCPIndex(sigCPIndex);

        return currentPkgInfo.addCPEntry(wrkrRplyCPEntry);
    }

    private int getCallableUnitCallCPIndex(CallableUnitInvocationExpr invocationExpr) {
        Expression[] argExprs = invocationExpr.getArgExprs();
        int[] argRegs = new int[argExprs.length];
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);
            argRegs[i] = argExpr.getTempOffset();
        }

        // Calculate registers to store return values
        BType[] retTypes = invocationExpr.getTypes();
        int[] retRegs = new int[retTypes.length];
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = retTypes[i];
            retRegs[i] = getNextIndex(retType.getTag(), regIndexes);
        }

        invocationExpr.setOffsets(retRegs);
        if (retRegs.length > 0) {
            ((Expression) invocationExpr).setTempOffset(retRegs[0]);
        }

        FunctionCallCPEntry funcCallCPEntry = new FunctionCallCPEntry(argRegs, retRegs);
        return currentPkgInfo.addCPEntry(funcCallCPEntry);
    }

    private void getForkJoinCPIndex(ForkJoinStmt forkJoinStmt) {
        Expression argExpr = forkJoinStmt.getTimeout().getTimeoutExpression();
        int[] retRegs;
        if (argExpr != null) {
            retRegs = new int[1];
            argExpr.accept(this);
            retRegs[0] = argExpr.getTempOffset();
        } else {
            retRegs = new int[0];
        }

        int[] argRegs = lvIndexes;

        ForkjoinInfo forkjoinInfo = new ForkjoinInfo(argRegs, retRegs);
        if (argExpr != null) {
            forkjoinInfo.setTimeoutAvailable(true);
        }
        for (Worker worker : forkJoinStmt.getWorkers()) {
            UTF8CPEntry workerNameCPEntry = new UTF8CPEntry(worker.getName());
            int workerNameCPIndex = currentPkgInfo.addCPEntry(workerNameCPEntry);
            WorkerInfo workerInfo = new WorkerInfo(workerNameCPIndex, worker.getName());
            forkjoinInfo.addWorkerInfo(worker.getName(), workerInfo);
        }

        int forkJoinIndex;
        if (currentWorkerInfo != null) {
            forkJoinIndex = currentWorkerInfo.addForkJoinInfo(forkjoinInfo);
        } else {
            forkJoinIndex = currentCallableUnitInfo.getDefaultWorkerInfo().addForkJoinInfo(forkjoinInfo);
        }
        ForkJoinCPEntry forkJoinIndexCPEntry = new ForkJoinCPEntry(forkJoinIndex);
        forkJoinIndexCPEntry.setForkjoinInfo(forkjoinInfo);
        int forkJoinIndexCPEntryIndex = currentPkgInfo.addCPEntry(forkJoinIndexCPEntry);
        forkjoinInfo.setIndexCPIndex(forkJoinIndexCPEntryIndex);

        emit(InstructionCodes.FORKJOIN, forkJoinIndexCPEntryIndex);
        // visit the workers within fork-join block
        // Now visit each Worker
        UTF8CPEntry codeUTF8CPEntry = new UTF8CPEntry(AttributeInfo.Kind.CODE_ATTRIBUTE.toString());
        int codeAttribNameIndex = currentPkgInfo.addCPEntry(codeUTF8CPEntry);
        int[] lvIndexesCopy = lvIndexes.clone();
        int[] regIndexesCopy = regIndexes.clone();
        for (Worker worker : forkJoinStmt.getWorkers()) {
            WorkerInfo workerInfo = forkjoinInfo.getWorkerInfo(worker.getName());
            workerInfo.getCodeAttributeInfo().setAttributeNameIndex(codeAttribNameIndex);
            workerInfo.getCodeAttributeInfo().setCodeAddrs(nextIP());
            currentWorkerInfo = workerInfo;
            lvIndexes = lvIndexesCopy.clone();
            worker.getCallableUnitBody().accept(this);
            endWorkerInfoUnit(workerInfo.getCodeAttributeInfo());
            // emit HALT instruction to finish the worker activity
            emit(InstructionCodes.HALT);
        }

        lvIndexes = lvIndexesCopy;
        regIndexes = regIndexesCopy;

        int i = 0;
        int[] joinWrkrNameCPIndexes = new int[forkJoinStmt.getJoin().getJoinWorkers().length];
        String[] joinWrkrNames = new String[forkJoinStmt.getJoin().getJoinWorkers().length];
        for (String workerName : forkJoinStmt.getJoin().getJoinWorkers()) {
            UTF8CPEntry workerNameCPEntry = new UTF8CPEntry(workerName);
            int workerNameCPIndex = currentPkgInfo.addCPEntry(workerNameCPEntry);
            joinWrkrNameCPIndexes[i] = workerNameCPIndex;
            joinWrkrNames[i] = workerName;
            i++;
        }
        forkjoinInfo.setJoinWrkrNameIndexes(joinWrkrNameCPIndexes);
        forkjoinInfo.setJoinWorkerNames(joinWrkrNames);

        // Generate code for Join block
        ForkJoinStmt.Join join = forkJoinStmt.getJoin();
        UTF8CPEntry joinType = new UTF8CPEntry(join.getJoinType());
        int joinTypeCPIndex = currentPkgInfo.addCPEntry(joinType);
        forkjoinInfo.setJoinType(join.getJoinType());
        forkjoinInfo.setJoinTypeCPIndex(joinTypeCPIndex);
        forkjoinInfo.setJoinIp(nextIP());
        if (join.getJoinResult() != null) {
            visitForkJoinParameterDefs(join.getJoinResult());
        }

        int joinMemOffset = ((StackVarLocation) join.getJoinResult().getMemoryLocation()).getStackFrameOffset();
        forkjoinInfo.setJoinMemOffset(joinMemOffset);

        if (join.getJoinBlock() != null) {
            join.getJoinBlock().accept(this);
        }

        // Emit a GOTO instruction to jump out of the timeout block
        Instruction gotoInstruction = new Instruction(InstructionCodes.GOTO, -1);
        emit(gotoInstruction);

        // Generate code for timeout block
        ForkJoinStmt.Timeout timeout = forkJoinStmt.getTimeout();
        forkjoinInfo.setTimeoutIp(nextIP());
        if (timeout.getTimeoutExpression() != null) {
            timeout.getTimeoutExpression().accept(this);
        }

        if (timeout.getTimeoutResult() != null) {
            visitForkJoinParameterDefs(timeout.getTimeoutResult());
        }

        int timeoutMemOffset = ((StackVarLocation) join.getJoinResult().getMemoryLocation()).getStackFrameOffset();
        forkjoinInfo.setTimeoutMemOffset(timeoutMemOffset);

        if (timeout.getTimeoutBlock() != null) {
            timeout.getTimeoutBlock().accept(this);
        }

        gotoInstruction.setOperand(0, nextIP());
    }

    private AnnotationAttributeInfo getAnnotationAttributeInfo(AnnotationAttachment[] annotationAttachments) {
        UTF8CPEntry annotationAttribUTF8CPEntry = new UTF8CPEntry(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.toString());
        int annotationAttribNameIndex = currentPkgInfo.addCPEntry(annotationAttribUTF8CPEntry);
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);

        for (AnnotationAttachment attachment : annotationAttachments) {
            AnnAttachmentInfo attachmentInfo = getAnnotationAttachmentInfo(attachment);
            attributeInfo.addAttachmentInfo(attachmentInfo);
        }

        return attributeInfo;
    }

    private AnnAttachmentInfo getAnnotationAttachmentInfo(AnnotationAttachment attachment) {
        int pkgPathCPIndex = addPackageCPEntry(attachment.getPkgPath());
        UTF8CPEntry annotationNameCPEntry = new UTF8CPEntry(attachment.getName());
        int annotationNameCPIndex = currentPkgInfo.addCPEntry(annotationNameCPEntry);

        AnnAttachmentInfo attachmentInfo = new AnnAttachmentInfo(pkgPathCPIndex, attachment.getPkgPath(),
                annotationNameCPIndex, attachment.getName());

        attachment.getAttributeNameValuePairs()
                .forEach((attributeName, attributeValue) -> {
                    UTF8CPEntry attributeNameCPEntry = new UTF8CPEntry(attributeName);
                    int attributeNameCPIndex = currentPkgInfo.addCPEntry(attributeNameCPEntry);
                    AnnAttributeValue annotationAttribValue = getAnnotationAttributeValue(attributeValue);
                    attachmentInfo.addAttributeValue(attributeNameCPIndex, attributeName, annotationAttribValue);
                });

        return attachmentInfo;
    }

    private AnnAttributeValue getAnnotationAttributeValue(
            org.ballerinalang.model.AnnotationAttributeValue attributeValue) {

        AnnAttributeValue attribValue;
        if (attributeValue.getLiteralValue() != null) {
            // Annotation attribute value is a literal value
            BValue literalValue = attributeValue.getLiteralValue();
            String typeDesc = literalValue.getType().getSig().toString();
            UTF8CPEntry typeDescCPEntry = new UTF8CPEntry(typeDesc);
            int typeDescCPIndex = currentPkgInfo.addCPEntry(typeDescCPEntry);
            attribValue = new AnnAttributeValue(typeDescCPIndex, typeDesc);

            int valueCPIndex;
            int typeTag = literalValue.getType().getTag();
            switch (typeTag) {
                case TypeTags.INT_TAG:
                    long intValue = ((BInteger) literalValue).intValue();
                    attribValue.setIntValue(intValue);
                    valueCPIndex = currentPkgInfo.addCPEntry(new IntegerCPEntry(intValue));
                    attribValue.setValueCPIndex(valueCPIndex);

                    break;
                case TypeTags.FLOAT_TAG:
                    double floatValue = ((BFloat) literalValue).floatValue();
                    attribValue.setFloatValue(floatValue);
                    valueCPIndex = currentPkgInfo.addCPEntry(new FloatCPEntry(floatValue));
                    attribValue.setValueCPIndex(valueCPIndex);

                    break;
                case TypeTags.STRING_TAG:
                    String stringValue = literalValue.stringValue();
                    attribValue.setStringValue(stringValue);
                    valueCPIndex = currentPkgInfo.addCPEntry(new UTF8CPEntry(stringValue));
                    attribValue.setValueCPIndex(valueCPIndex);

                    break;
                case TypeTags.BOOLEAN_TAG:
                    boolean boolValue = ((BBoolean) literalValue).booleanValue();
                    attribValue.setBooleanValue(boolValue);
                    break;
            }

        } else if (attributeValue.getAnnotationValue() != null) {
            // Annotation attribute value is another annotation attachment
            AnnotationAttachment attachment = attributeValue.getAnnotationValue();
            AnnAttachmentInfo attachmentInfo = getAnnotationAttachmentInfo(attachment);

            String typeDesc = TypeSignature.SIG_ANNOTATION;
            UTF8CPEntry typeDescCPEntry = new UTF8CPEntry(typeDesc);
            int typeDescCPIndex = currentPkgInfo.addCPEntry(typeDescCPEntry);
            attribValue = new AnnAttributeValue(typeDescCPIndex, typeDesc, attachmentInfo);

        } else {
            org.ballerinalang.model.AnnotationAttributeValue[] attributeValues = attributeValue.getValueArray();
            AnnAttributeValue[] annotationAttribValues = new AnnAttributeValue[attributeValues.length];
            for (int i = 0; i < attributeValues.length; i++) {
                annotationAttribValues[i] = getAnnotationAttributeValue(attributeValues[i]);
            }

            String typeDesc = TypeSignature.SIG_ARRAY;
            UTF8CPEntry typeDescCPEntry = new UTF8CPEntry(typeDesc);
            int typeDescCPIndex = currentPkgInfo.addCPEntry(typeDescCPEntry);
            attribValue = new AnnAttributeValue(typeDescCPIndex, typeDesc, annotationAttribValues);
        }

        return attribValue;
    }

    private void visitForkJoinParameterDefs(ParameterDef parameterDef) {
        LocalVariableAttributeInfo localVariableAttributeInfo = new LocalVariableAttributeInfo(1);
        int lvIndex = getNextIndex(parameterDef.getType().getTag(), lvIndexes);
        parameterDef.setMemoryLocation(new StackVarLocation(lvIndex));
        parameterDef.accept(this);
        LocalVariableInfo localVariableDetails = getLocalVarAttributeInfo(parameterDef);
        localVariableAttributeInfo.addLocalVarInfo(localVariableDetails);
        //callableUnitInfo.addAttributeInfo(AttributeInfo.LOCALVARIABLES_ATTRIBUTE, localVariableAttributeInfo);

    }

    private void visitCallableUnitParameterDefs(ParameterDef[] parameterDefs, CallableUnitInfo callableUnitInfo,
                                                LocalVariableAttributeInfo localVarAttributeInfo) {
        boolean paramAnnotationFound = false;
        UTF8CPEntry paramAnnAttribUTF8CPEntry = new UTF8CPEntry(
                AttributeInfo.Kind.PARAMETER_ANNOTATIONS_ATTRIBUTE.toString());
        int paramAnnAttribNameIndex = currentPkgInfo.addCPEntry(paramAnnAttribUTF8CPEntry);
        ParamAnnotationAttributeInfo paramAttributeInfo = new ParamAnnotationAttributeInfo(
                paramAnnAttribNameIndex);

        for (int i = 0; i < parameterDefs.length; i++) {
            ParameterDef parameterDef = parameterDefs[i];
            int lvIndex = getNextIndex(parameterDef.getType().getTag(), lvIndexes);
            parameterDef.setMemoryLocation(new StackVarLocation(lvIndex));
            parameterDef.accept(this);
            LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(parameterDef);
            localVarAttributeInfo.addLocalVarInfo(localVarInfo);

            AnnotationAttachment[] paramAnnotationAttachments = parameterDef.getAnnotations();
            if (paramAnnotationAttachments.length == 0) {
                continue;
            }

            paramAnnotationFound = true;
            ParamAnnAttachmentInfo paramAttachmentInfo = new ParamAnnAttachmentInfo(i);
            int j = 0;
            int[] attachmentIndexes = new int[paramAnnotationAttachments.length];
            for (AnnotationAttachment annotationAttachment : paramAnnotationAttachments) {
                AnnAttachmentInfo attachmentInfo = getAnnotationAttachmentInfo(annotationAttachment);
                paramAttachmentInfo.addAnnotationAttachmentInfo(attachmentInfo);
                attachmentIndexes[j] = attachmentInfo.nameCPIndex;
            }
            localVarInfo.setAttachmentIndexes(attachmentIndexes);

            paramAttributeInfo.addParamAttachmentInfo(paramAttachmentInfo);
        }

        callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttributeInfo);
        if (paramAnnotationFound) {
            callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.PARAMETER_ANNOTATIONS_ATTRIBUTE, paramAttributeInfo);
        }
    }

    private void visitCallableUnit(CallableUnit callableUnit, CallableUnitInfo callableUnitInfo, Worker[] workers) {
        UTF8CPEntry codeAttribUTF8CPEntry = new UTF8CPEntry(AttributeInfo.Kind.CODE_ATTRIBUTE.toString());
        int codeAttribNameIndex = currentPkgInfo.addCPEntry(codeAttribUTF8CPEntry);

        UTF8CPEntry localVarAttribUTF8CPEntry = new UTF8CPEntry(
                AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.toString());
        int localVarAttribNameIndex = currentPkgInfo.addCPEntry(localVarAttribUTF8CPEntry);
        LocalVariableAttributeInfo localVarAttributeInfo = new LocalVariableAttributeInfo(localVarAttribNameIndex);

        // Read annotations attached to this callableUnit
        AnnotationAttachment[] annotationAttachments = callableUnit.getAnnotations();
        if (annotationAttachments.length > 0) {
            AnnotationAttributeInfo annotationsAttribute = getAnnotationAttributeInfo(annotationAttachments);
            callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, annotationsAttribute);
        }

        // Add local variable indexes to the parameters and return parameters
        visitCallableUnitParameterDefs(callableUnit.getParameterDefs(), callableUnitInfo, localVarAttributeInfo);

        // Visit return parameter defs
        for (ParameterDef parameterDef : callableUnit.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                int lvIndex = getNextIndex(parameterDef.getType().getTag(), lvIndexes);
                parameterDef.setMemoryLocation(new StackVarLocation(lvIndex));
            }

            parameterDef.accept(this);
        }

        if (!callableUnit.isNative()) {
            // Clone lvIndex array here. This array contain local variable indexes of the input and out parameters
            //  and they are common for all the workers.
            int[] lvIndexesCopy = lvIndexes.clone();

            WorkerInfo defaultWorker = callableUnitInfo.getDefaultWorkerInfo();
            defaultWorker.getCodeAttributeInfo().setAttributeNameIndex(codeAttribNameIndex);
            defaultWorker.getCodeAttributeInfo().setCodeAddrs(nextIP());

            currentlLocalVarAttribInfo = new LocalVariableAttributeInfo(localVarAttribNameIndex);
            currentlLocalVarAttribInfo.setLocalVariables(new ArrayList<>(localVarAttributeInfo.getLocalVariables()));
            defaultWorker.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, currentlLocalVarAttribInfo);

            // Visit the callableUnit body
            callableUnit.getCallableUnitBody().accept(this);

            // Set local variables and reg indexes and reset instance variables to defaults
            endWorkerInfoUnit(defaultWorker.getCodeAttributeInfo());
            resetIndexes(maxRegIndexes);

            // Now visit each Worker
            for (Worker worker : workers) {
                WorkerInfo workerInfo = callableUnitInfo.getWorkerInfo(worker.getName());
                workerInfo.getCodeAttributeInfo().setAttributeNameIndex(codeAttribNameIndex);
                workerInfo.getCodeAttributeInfo().setCodeAddrs(nextIP());

                currentlLocalVarAttribInfo = new LocalVariableAttributeInfo(localVarAttribNameIndex);
                currentlLocalVarAttribInfo.setLocalVariables(
                        new ArrayList<>(localVarAttributeInfo.getLocalVariables()));
                workerInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, currentlLocalVarAttribInfo);

                lvIndexes = lvIndexesCopy.clone();
                worker.getCallableUnitBody().accept(this);

                //workerInfo.setWorkerEndIP(nextIP());
                endWorkerInfoUnit(workerInfo.getCodeAttributeInfo());
                resetIndexes(maxRegIndexes);

                // emit HALT instruction to finish the worker activity
                emit(InstructionCodes.HALT);
            }

        } else {
            WorkerInfo defaultWorker = callableUnitInfo.getDefaultWorkerInfo();
            defaultWorker.getCodeAttributeInfo().setAttributeNameIndex(codeAttribNameIndex);
            defaultWorker.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttributeInfo);

            endWorkerInfoUnit(defaultWorker.getCodeAttributeInfo());
            resetIndexes(maxRegIndexes);
        }

        currentlLocalVarAttribInfo = null;
    }

    private void addLineNumberInfo(NodeLocation nodeLocation) {
        if (nodeLocation == null) {
            return;
        }
        LineNumberInfo lineNumberInfo = createLineNumberInfo(nodeLocation, currentPkgInfo,
                currentPkgInfo.getInstructionCount());
        lineNumberTableAttributeInfo.addLineNumberInfo(lineNumberInfo);
    }

    private LineNumberInfo createLineNumberInfo(NodeLocation nodeLocation, PackageInfo packageInfo, int ip) {
        if (nodeLocation == null) {
            return null;
        }
        UTF8CPEntry fileNameUTF8CPEntry = new UTF8CPEntry(nodeLocation.getFileName());
        int fileNameCPEntryIndex = packageInfo.addCPEntry(fileNameUTF8CPEntry);

        LineNumberInfo lineNumberInfo = new LineNumberInfo(nodeLocation.getLineNumber(),
                fileNameCPEntryIndex, nodeLocation.getFileName(), ip);
        lineNumberInfo.setPackageInfo(packageInfo);
        lineNumberInfo.setIp(ip);
        return lineNumberInfo;
    }

    private LocalVariableInfo getLocalVarAttributeInfo(VariableDef variableDef) {
        UTF8CPEntry annotationNameCPEntry = new UTF8CPEntry(variableDef.getName());
        int varNameCPIndex = currentPkgInfo.addCPEntry(annotationNameCPEntry);

        // TODO Support other variable memory locations
        MemoryLocation memLocation = variableDef.getMemoryLocation();
        int memLocationOffset;
        if (memLocation instanceof GlobalVarLocation) {
            memLocationOffset = ((GlobalVarLocation) variableDef.getMemoryLocation()).getStaticMemAddrOffset();
        } else if (memLocation instanceof ServiceVarLocation) {
            memLocationOffset = ((ServiceVarLocation) variableDef.getMemoryLocation()).getStaticMemAddrOffset();
        } else if (memLocation instanceof ConnectorVarLocation) {
            memLocationOffset = ((ConnectorVarLocation) variableDef.getMemoryLocation()).getConnectorMemAddrOffset();
        } else {
            memLocationOffset = ((StackVarLocation) variableDef.getMemoryLocation()).getStackFrameOffset();
        }

        BType varType = variableDef.getType();
        String sig = varType.getSig().toString();
        UTF8CPEntry sigCPEntry = new UTF8CPEntry(sig);
        int sigCPIndex = currentPkgInfo.addCPEntry(sigCPEntry);

        return new LocalVariableInfo(variableDef.getName(), varNameCPIndex, memLocationOffset, sigCPIndex, varType);
    }

    private void assignVariableDefMemoryLocation(VariableDef variableDef) {
        MemoryLocation memoryLocation = variableDef.getMemoryLocation();
        if (memoryLocation instanceof StackVarLocation || memoryLocation instanceof WorkerVarLocation) {
            int lvIndex = getNextIndex(variableDef.getType().getTag(), lvIndexes);
            MemoryLocation stackVarLocation = new StackVarLocation(lvIndex);
            variableDef.setMemoryLocation(stackVarLocation);
            LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(variableDef);
            currentlLocalVarAttribInfo.addLocalVarInfo(localVarInfo);
        }
    }

    private void generateFinallyInstructions(Statement statement, StatementKind scope) {
        int[] regIndexesOriginal = this.regIndexes.clone();
        Statement parent = statement;
        while (scope != parent.getKind()) {
            if (StatementKind.TRY_BLOCK == parent.getKind() || StatementKind.CATCH_BLOCK == parent.getKind()) {
                TryCatchStmt.FinallyBlock finallyBlock = ((TryCatchStmt) parent.getParent()).getFinallyBlock();
                if (finallyBlock != null) {
                    resetIndexes(this.regIndexes);
                    finallyBlock.getFinallyBlockStmt().accept(this);
                }
            }
            parent = parent.getParent();
        }
        this.regIndexes = regIndexesOriginal;
    }

    private BType getElementType(BType type) {
        if (type.getTag() != TypeTags.ARRAY_TAG) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    private void setCallableUnitSignature(CallableUnitInfo callableUnitInfo) {
        // Get signature and add it to the constant pool.
        String sig = callableUnitInfo.getSignature();
        UTF8CPEntry sigCPEntry = new UTF8CPEntry(sig);
        int sigCPIndex = currentPkgInfo.addCPEntry(sigCPEntry);
        callableUnitInfo.setSignatureCPIndex(sigCPIndex);
    }

    private void addVariableCountAttributeInfo(ConstantPool constantPool,
                                               AttributeInfoPool attributeInfoPool,
                                               int[] fieldCount) {
        UTF8CPEntry attribNameCPEntry = new UTF8CPEntry(AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE.toString());
        int attribNameCPIndex = constantPool.addCPEntry(attribNameCPEntry);
        VarTypeCountAttributeInfo varCountAttribInfo = new VarTypeCountAttributeInfo(attribNameCPIndex);
        varCountAttribInfo.setMaxLongVars(fieldCount[INT_OFFSET]);
        varCountAttribInfo.setMaxDoubleVars(fieldCount[FLOAT_OFFSET]);
        varCountAttribInfo.setMaxStringVars(fieldCount[STRING_OFFSET]);
        varCountAttribInfo.setMaxIntVars(fieldCount[BOOL_OFFSET]);
        varCountAttribInfo.setMaxByteVars(fieldCount[BLOB_OFFSET]);
        varCountAttribInfo.setMaxRefVars(fieldCount[REF_OFFSET]);
        attributeInfoPool.addAttributeInfo(AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE, varCountAttribInfo);
    }

    private ErrorTableAttributeInfo createErrorTableIfAbsent(PackageInfo packageInfo) {
        ErrorTableAttributeInfo errorTable =
                (ErrorTableAttributeInfo) packageInfo.getAttributeInfo(AttributeInfo.Kind.ERROR_TABLE);
        if (errorTable == null) {
            UTF8CPEntry attribNameCPEntry = new UTF8CPEntry(AttributeInfo.Kind.ERROR_TABLE.toString());
            int attribNameCPIndex = packageInfo.addCPEntry(attribNameCPEntry);
            errorTable = new ErrorTableAttributeInfo(attribNameCPIndex);
            packageInfo.addAttributeInfo(AttributeInfo.Kind.ERROR_TABLE, errorTable);
        }
        return errorTable;
    }

    /**
     * Create conditional statements to find the matching namespace URI. If an existing declaration id found,
     * get the prefix of it as the prefix to be used.
     * 
     * @param namespaces namespace map
     * @param localNameRegIndex Registry index of the local name
     * @param uriRegIndex Registry index of the uri
     * @param targetQnameRegIndex Registry index of the target qname
     * @param location Node location
     */
    private void generateUriLookupInstructions(Map<String, Expression> namespaces, int localNameRegIndex,
            int uriRegIndex, int targetQnameRegIndex, NodeLocation location) {
        if (namespaces.isEmpty()) {
            createQNameWithEmptyPrefix(localNameRegIndex, uriRegIndex, targetQnameRegIndex, location);
            return;
        }

        List<Instruction> gotoInstructionList = new ArrayList<>();
        Instruction ifInstruction;
        Instruction gotoInstruction;
        String prefix;
        for (Entry<String, Expression> keyValues : namespaces.entrySet()) {
            prefix = keyValues.getKey();

            // skip the default namespace
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                continue;
            }

            // Below section creates the condition to compare the namespace uri's

            // store the comparing uri as string
            Expression uriLiteral = keyValues.getValue();
            uriLiteral.accept(this);

            int opcode = getOpcode(BTypes.typeString.getTag(), InstructionCodes.IEQ);
            int exprIndex = ++regIndexes[BOOL_OFFSET];
            emit(opcode, uriRegIndex, uriLiteral.getTempOffset(), exprIndex);

            ifInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE, exprIndex, -1);
            emit(ifInstruction);

            // Below section creates instructions to be executed, if the above condition succeeds (then body)

            // create the prifix literal
            BasicLiteral prefixLiteral = new BasicLiteral(location, null, new BString(prefix));
            prefixLiteral.setType(BTypes.typeString);
            prefixLiteral.accept(this);

            // create a qname
            emit(InstructionCodes.NEWQNAME, localNameRegIndex, uriRegIndex, prefixLiteral.getTempOffset(),
                    targetQnameRegIndex);

            gotoInstruction = new Instruction(InstructionCodes.GOTO, -1);
            emit(gotoInstruction);
            gotoInstructionList.add(gotoInstruction);
            ifInstruction.setOperand(1, nextIP());
        }

        // else part. create a qname with empty prefix
        createQNameWithEmptyPrefix(localNameRegIndex, uriRegIndex, targetQnameRegIndex, location);

        int nextIP = nextIP();
        for (Instruction instruction : gotoInstructionList) {
            instruction.setOperand(0, nextIP);
        }
    }

    private void createQNameWithEmptyPrefix(int localNameRegIndex, int uriRegIndex, int targetQnameRegIndex,
                                            NodeLocation location) {
        BasicLiteral prefixLiteral = new BasicLiteral(location, null, BTypes.typeString.getEmptyValue());
        prefixLiteral.setType(BTypes.typeString);
        prefixLiteral.accept(this);

        emit(InstructionCodes.NEWQNAME, localNameRegIndex, uriRegIndex, prefixLiteral.getTempOffset(),
                targetQnameRegIndex);
    }

    /**
     * Generate instructions to add namespace declarations that are visible to the current scope.
     * 
     * @param namepsaces Namespaces that are visible to the current scope
     * @param xmlVarRegIndex Registry index of the XML variable
     * @param defaultNsUriOffset Registry offset of the default namespace URI
     * @param location Node location
     */
    private void addNamespacesToXML(Map<String, Expression> namepsaces, int xmlVarRegIndex, int defaultNsUriOffset,
            NodeLocation location) {
        int qnameRegIndex = ++regIndexes[REF_OFFSET];
        String localname;

        // Prefix for namespaces is always 'xmlns'
        BasicLiteral prefixLiteral = new BasicLiteral(location, null, new BString(XMLConstants.XMLNS_ATTRIBUTE));
        prefixLiteral.setType(BTypes.typeString);
        prefixLiteral.accept(this);

        // declare the remaining namespaces
        for (Entry<String, Expression> namespace : namepsaces.entrySet()) {
            localname = namespace.getKey();

            BasicLiteral localNameLiteral = new BasicLiteral(location, null, new BString(localname));
            localNameLiteral.setType(BTypes.typeString);
            localNameLiteral.accept(this);

            Expression valueLiteral = namespace.getValue();
            valueLiteral.accept(this);

            qnameRegIndex = ++regIndexes[REF_OFFSET];
            emit(InstructionCodes.NEWQNAME, localNameLiteral.getTempOffset(), defaultNsUriOffset,
                    prefixLiteral.getTempOffset(), qnameRegIndex);
            emit(InstructionCodes.XMLATTRSTORE, xmlVarRegIndex, qnameRegIndex, valueLiteral.getTempOffset());
        }
    }

    /**
     * @since 0.87
     */
    public static class OpcodeAndIndex {
        int opcode;
        int index;

        public OpcodeAndIndex(int opcode, int index) {
            this.opcode = opcode;
            this.index = index;
        }
    }
}

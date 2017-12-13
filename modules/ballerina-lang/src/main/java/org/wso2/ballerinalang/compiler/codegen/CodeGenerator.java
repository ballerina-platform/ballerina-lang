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
package org.wso2.ballerinalang.compiler.codegen;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BEnumType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangEnum.BLangEnumerator;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangEnumeratorAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangFunctionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangTransformerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangJSONLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFieldVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeofExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.MultiReturnExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn.BLangWorkerReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.ActionInfo;
import org.wso2.ballerinalang.programfile.AnnAttachmentInfo;
import org.wso2.ballerinalang.programfile.AnnAttributeValue;
import org.wso2.ballerinalang.programfile.CallableUnitInfo;
import org.wso2.ballerinalang.programfile.ConnectorInfo;
import org.wso2.ballerinalang.programfile.EnumInfo;
import org.wso2.ballerinalang.programfile.EnumeratorInfo;
import org.wso2.ballerinalang.programfile.ErrorTableEntry;
import org.wso2.ballerinalang.programfile.ForkjoinInfo;
import org.wso2.ballerinalang.programfile.FunctionInfo;
import org.wso2.ballerinalang.programfile.Instruction;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.programfile.InstructionFactory;
import org.wso2.ballerinalang.programfile.LineNumberInfo;
import org.wso2.ballerinalang.programfile.LocalVariableInfo;
import org.wso2.ballerinalang.programfile.PackageInfo;
import org.wso2.ballerinalang.programfile.PackageVarInfo;
import org.wso2.ballerinalang.programfile.ProgramFile;
import org.wso2.ballerinalang.programfile.ResourceInfo;
import org.wso2.ballerinalang.programfile.ServiceInfo;
import org.wso2.ballerinalang.programfile.StructFieldDefaultValue;
import org.wso2.ballerinalang.programfile.StructFieldInfo;
import org.wso2.ballerinalang.programfile.StructInfo;
import org.wso2.ballerinalang.programfile.TransformerInfo;
import org.wso2.ballerinalang.programfile.WorkerDataChannelInfo;
import org.wso2.ballerinalang.programfile.WorkerInfo;
import org.wso2.ballerinalang.programfile.attributes.AnnotationAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfoPool;
import org.wso2.ballerinalang.programfile.attributes.CodeAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ErrorTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LineNumberTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LocalVariableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.VarTypeCountAttributeInfo;
import org.wso2.ballerinalang.programfile.cpentries.ActionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPool;
import org.wso2.ballerinalang.programfile.cpentries.FloatCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ForkJoinCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionCallCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.IntegerCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.PackageRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StringCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StructureRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.TransformerRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.TypeRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.UTF8CPEntry;
import org.wso2.ballerinalang.programfile.cpentries.WorkerDataChannelRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.WrkrInteractionArgsCPEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BLOB_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BOOL_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.FLOAT_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.INT_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.REF_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.STRING_OFFSET;

/**
 * @since 0.94
 */
public class CodeGenerator extends BLangNodeVisitor {

    private static final CompilerContext.Key<CodeGenerator> CODE_GENERATOR_KEY =
            new CompilerContext.Key<>();
    /**
     * This structure holds current package-level variable indexes.
     */
    private VariableIndex pvIndexes = new VariableIndex();

    /**
     * This structure holds current local variable indexes.
     */
    private VariableIndex lvIndexes = new VariableIndex();

    /**
     * This structure holds current field indexes.
     */
    private VariableIndex fieldIndexes = new VariableIndex();

    /**
     * This structure holds current register indexes.
     */
    private VariableIndex regIndexes = new VariableIndex();

    /**
     * This structure holds the maximum register count per type.
     * This structure is updated for every statement.
     */
    private VariableIndex maxRegIndexes = new VariableIndex();

    private SymbolEnv env;
    // TODO Remove this dependency from the code generator
    private SymbolEnter symEnter;
    private SymbolTable symTable;

    private ProgramFile programFile;

    private PackageInfo currentPkgInfo;
    private PackageID currentPkgID;
    private int currentPackageRefCPIndex;

    private LineNumberTableAttributeInfo lineNoAttrInfo;
    private CallableUnitInfo currentCallableUnitInfo;
    private LocalVariableAttributeInfo localVarAttrInfo;
    private WorkerInfo currentWorkerInfo;
    private ServiceInfo currentServiceInfo;
    private ConnectorInfo currentConnectorInfo;

    // Required variables to generate code for assignment statements
    private int rhsExprRegIndex = -1;
    private boolean varAssignment = false;

    private int transactionIndex = 0;

    private Stack<Instruction> loopResetInstructionStack = new Stack<>();
    private Stack<Instruction> loopExitInstructionStack = new Stack<>();
    private Stack<Instruction> abortInstructions = new Stack<>();

    private int workerChannelCount = 0;
    private int forkJoinCount = 0;

    private static final String MAIN_FUNCTION_NAME = "main";

    public static CodeGenerator getInstance(CompilerContext context) {
        CodeGenerator codeGenerator = context.get(CODE_GENERATOR_KEY);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }

        return codeGenerator;
    }

    public CodeGenerator(CompilerContext context) {
        context.put(CODE_GENERATOR_KEY, this);

        this.symEnter = SymbolEnter.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
    }

    public ProgramFile generate(BLangPackage pkgNode) {
        programFile = new ProgramFile();
        // TODO: Fix this. Added temporally for codegen. Load this from VM side.
        if (this.symTable.builtInPackageSymbol != null) {
            genPackage(this.symTable.builtInPackageSymbol);
        }
        BPackageSymbol pkgSymbol = pkgNode.symbol;
        genPackage(pkgSymbol);

        programFile.entryPkgCPIndex = addPackageRefCPEntry(programFile, pkgSymbol.pkgID);

        setEntryPoints(programFile, pkgNode);

        // Add global variable indexes to the ProgramFile
        prepareIndexes(pvIndexes);

        // Create Global variable attribute info
        addVarCountAttrInfo(programFile, programFile, pvIndexes);

        return programFile;
    }

    private static void setEntryPoints(ProgramFile programFile, BLangPackage pkgNode) {
        BLangFunction mainFunc = getMainFunction(pkgNode);
        if (mainFunc != null) {
            programFile.setMainEPAvailable(true);
        }

        if (pkgNode.services.size() != 0) {
            programFile.setServiceEPAvailable(true);
        }
    }

    private static BLangFunction getMainFunction(BLangPackage pkgNode) {
        List<BLangFunction> functions = pkgNode.functions.stream().filter(f -> (f.name.value
                .equals(MAIN_FUNCTION_NAME) && f.symbol.params.size() == 1 && f.symbol.retParams.size() == 0))
                .collect(Collectors.toList());
        if (functions.isEmpty()) {
            return null;
        }
        for (BLangFunction f : functions) {
            BType paramType = f.symbol.params.get(0).type;
            if (paramType.tag != TypeTags.ARRAY) {
                continue;
            }
            BArrayType arrayType = (BArrayType) paramType;
            if (arrayType.eType.tag == TypeTags.STRING) {
                return f;
            }
        }
        return null;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.CODE_GEN)) {
            return;
        }
        // first visit all the imports
        pkgNode.imports.forEach(impPkgNode -> genNode(impPkgNode, this.env));

        // Add the current package to the program file
        BPackageSymbol pkgSymbol = pkgNode.symbol;
        currentPkgID = pkgSymbol.pkgID;
        int pkgNameCPIndex = addUTF8CPEntry(programFile, currentPkgID.name.value);
        int pkgVersionCPIndex = addUTF8CPEntry(programFile, currentPkgID.version.value);
        currentPkgInfo = new PackageInfo(pkgNameCPIndex, pkgVersionCPIndex);

        // TODO We need to create identifier for both name and the version
        programFile.packageInfoMap.put(currentPkgID.name.value, currentPkgInfo);

        // Insert the package reference to the constant pool of the Ballerina program
        addPackageRefCPEntry(programFile, currentPkgID);

        // Insert the package reference to the constant pool of the current package
        currentPackageRefCPIndex = addPackageRefCPEntry(currentPkgInfo, currentPkgID);

        // This attribute keep track of line numbers
        int lineNoAttrNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE.value());
        lineNoAttrInfo = new LineNumberTableAttributeInfo(lineNoAttrNameIndex);

        // This attribute keep package-level variable information
        int pkgVarAttrNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.value());
        currentPkgInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE,
                new LocalVariableAttributeInfo(pkgVarAttrNameIndex));

        pkgNode.globalVars.forEach(this::createPackageVarInfo);
        pkgNode.structs.forEach(this::createStructInfoEntry);
        pkgNode.enums.forEach(this::createEnumInfoEntry);
        pkgNode.connectors.forEach(this::createConnectorInfoEntry);
        pkgNode.functions.forEach(this::createFunctionInfoEntry);
        pkgNode.services.forEach(this::createServiceInfoEntry);
        pkgNode.functions.forEach(this::createFunctionInfoEntry);
        pkgNode.transformers.forEach(this::createTransformerInfoEntry);

        // Create function info for the package function
        BLangFunction pkgInitFunc = pkgNode.initFunction;
        createFunctionInfoEntry(pkgInitFunc);

        // Visit package init function
        genNode(pkgInitFunc, this.env);

        for (TopLevelNode pkgLevelNode : pkgNode.topLevelNodes) {
            if (pkgLevelNode.getKind() == NodeKind.VARIABLE || pkgLevelNode.getKind() == NodeKind.XMLNS) {
                continue;
            }
            genNode((BLangNode) pkgLevelNode, this.env);
        }

        currentPkgInfo.addAttributeInfo(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE, lineNoAttrInfo);
        currentPackageRefCPIndex = -1;
        currentPkgID = null;
        pkgNode.completedPhases.add(CompilerPhase.CODE_GEN);
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        genPackage(pkgSymbol);
    }

    public void visit(BLangService serviceNode) {
        BLangFunction initFunction = (BLangFunction) serviceNode.getInitFunction();
        visit(initFunction);

        currentServiceInfo = currentPkgInfo.getServiceInfo(serviceNode.getName().getValue());

        int annotationAttribNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.value());
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);
        serviceNode.annAttachments.forEach(annt -> visitAnnotationAttachment(annt, attributeInfo));
        currentServiceInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, attributeInfo);

        SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceNode.symbol.scope, this.env);
        serviceNode.resources.forEach(resource -> genNode(resource, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        ResourceInfo resourceInfo = currentServiceInfo.resourceInfoMap.get(resourceNode.name.getValue());
        currentCallableUnitInfo = resourceInfo;
        int annotationAttribNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.value());
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);
        resourceNode.annAttachments.forEach(annt -> visitAnnotationAttachment(annt, attributeInfo));
        currentCallableUnitInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, attributeInfo);

        SymbolEnv resourceEnv = SymbolEnv
                .createResourceActionSymbolEnv(resourceNode, resourceNode.symbol.scope, this.env);
        visitInvokableNode(resourceNode, currentCallableUnitInfo, resourceEnv);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.env);
        currentCallableUnitInfo = currentPkgInfo.functionInfoMap.get(funcNode.symbol.name.value);
        int annotationAttribNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.value());
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);
        funcNode.annAttachments.forEach(annt -> visitAnnotationAttachment(annt, attributeInfo));
        currentCallableUnitInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, attributeInfo);
        visitInvokableNode(funcNode, currentCallableUnitInfo, funcEnv);
    }

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, this.env);

        for (BLangStatement stmt : blockNode.stmts) {
            if (stmt.getKind() != NodeKind.TRY && stmt.getKind() != NodeKind.CATCH
                    && stmt.getKind() != NodeKind.IF && stmt.getKind() != NodeKind.COMMENT) {
                addLineNumberInfo(stmt.pos);
            }

            genNode(stmt, blockEnv);

            // Update the maxRegIndexes structure
            setMaxRegIndexes(regIndexes, maxRegIndexes);

            // Reset the regIndexes structure for every statement
            regIndexes = new VariableIndex();
        }
    }

    public void visit(BLangEnum enumNode) {
        EnumInfo enumInfo = currentPkgInfo.getEnumInfo(enumNode.getName().getValue());

        int annotationAttribNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.value());
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);
        enumNode.annAttachments.forEach(annt -> visitAnnotationAttachment(annt, attributeInfo));
        enumInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, attributeInfo);
    }

    public void visit(BLangVariable varNode) {
        int opcode;
        int lvIndex;
        BVarSymbol varSymbol = varNode.symbol;

        BLangExpression rhsExpr = varNode.expr;
        if (rhsExpr != null) {
            genNode(rhsExpr, this.env);
            rhsExprRegIndex = rhsExpr.regIndex;
        }

        int ownerSymTag = env.scope.owner.tag;
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(varSymbol.type.tag,
                    InstructionCodes.ISTORE, lvIndexes);
            opcode = opcodeAndIndex.opcode;
            lvIndex = opcodeAndIndex.index;
            varSymbol.varIndex = lvIndex;
            if (rhsExpr != null) {
                emit(opcode, rhsExprRegIndex, lvIndex);
            }

            LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(varSymbol);
            localVarAttrInfo.localVars.add(localVarInfo);
        } else {
            // TODO Support other variable nodes
        }
    }

    public void visit(BLangTransformer transformerNode) {
        SymbolEnv transformerEnv =
                SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, this.env);
        currentCallableUnitInfo = currentPkgInfo.transformerInfoMap.get(transformerNode.symbol.name.value);
        int annotationAttribNameIndex =
                addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.value());
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);
        transformerNode.annAttachments.forEach(annt -> visitAnnotationAttachment(annt, attributeInfo));
        currentCallableUnitInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, attributeInfo);
        visitInvokableNode(transformerNode, currentCallableUnitInfo, transformerEnv);
    }

    // Statements

    public void visit(BLangVariableDef varDefNode) {
        genNode(varDefNode.var, this.env);
    }

    public void visit(BLangReturn returnNode) {
        visitReturnStatementsExprs(returnNode);
        emit(InstructionCodes.RET);
    }

    public void visit(BLangWorkerReturn returnNode) {
        visitReturnStatementsExprs(returnNode);
        this.emit(InstructionFactory.get(InstructionCodes.WRKRETURN));
    }

    private int typeTagToInstr(int typeTag) {
        switch (typeTag) {
            case TypeTags.INT:
                return InstructionCodes.IRET;
            case TypeTags.FLOAT:
                return InstructionCodes.FRET;
            case TypeTags.STRING:
                return InstructionCodes.SRET;
            case TypeTags.BOOLEAN:
                return InstructionCodes.BRET;
            case TypeTags.BLOB:
                return InstructionCodes.LRET;
            default:
                return InstructionCodes.RRET;
        }
    }


    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
        int opcode;
        int regIndex = -1;
        int typeTag = literalExpr.type.tag;

        switch (typeTag) {
            case TypeTags.INT:
                regIndex = ++regIndexes.tInt;
                long longVal = (Long) literalExpr.value;
                if (longVal >= 0 && longVal <= 5) {
                    opcode = InstructionCodes.ICONST_0 + (int) longVal;
                    emit(opcode, regIndex);
                } else {
                    int intCPEntryIndex = currentPkgInfo.addCPEntry(new IntegerCPEntry(longVal));
                    emit(InstructionCodes.ICONST, intCPEntryIndex, regIndex);
                }
                break;

            case TypeTags.FLOAT:
                regIndex = ++regIndexes.tFloat;
                double doubleVal = (Double) literalExpr.value;
                if (doubleVal == 0 || doubleVal == 1 || doubleVal == 2 ||
                        doubleVal == 3 || doubleVal == 4 || doubleVal == 5) {
                    opcode = InstructionCodes.FCONST_0 + (int) doubleVal;
                    emit(opcode, regIndex);
                } else {
                    int floatCPEntryIndex = currentPkgInfo.addCPEntry(new FloatCPEntry(doubleVal));
                    emit(InstructionCodes.FCONST, floatCPEntryIndex, regIndex);
                }
                break;

            case TypeTags.STRING:
                regIndex = ++regIndexes.tString;
                String strValue = (String) literalExpr.value;
                StringCPEntry stringCPEntry = new StringCPEntry(addUTF8CPEntry(currentPkgInfo, strValue), strValue);
                int strCPIndex = currentPkgInfo.addCPEntry(stringCPEntry);
                emit(InstructionCodes.SCONST, strCPIndex, regIndex);
                break;

            case TypeTags.BOOLEAN:
                regIndex = ++regIndexes.tBoolean;
                boolean booleanVal = (Boolean) literalExpr.value;
                if (!booleanVal) {
                    opcode = InstructionCodes.BCONST_0;
                } else {
                    opcode = InstructionCodes.BCONST_1;
                }
                emit(opcode, regIndex);
                break;
            case TypeTags.NULL:
                regIndex = ++regIndexes.tRef;
                emit(InstructionCodes.RCONST_NULL, regIndex);
        }

        literalExpr.regIndex = regIndex;
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        BType etype;
        if (arrayLiteral.type.tag == TypeTags.ANY) {
            etype = arrayLiteral.type;
        } else {
            etype = ((BArrayType) arrayLiteral.type).eType;
        }

        int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, arrayLiteral.type.getDesc());
        TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
        int typeCPindex = currentPkgInfo.addCPEntry(typeRefCPEntry);

        // Emit create array instruction
        int opcode = getOpcode(etype.tag, InstructionCodes.INEWARRAY);
        int arrayVarRegIndex = ++regIndexes.tRef;
        arrayLiteral.regIndex = arrayVarRegIndex;
        emit(opcode, arrayVarRegIndex, typeCPindex);

        // Emit instructions populate initial array values;
        for (int i = 0; i < arrayLiteral.exprs.size(); i++) {
            BLangExpression argExpr = arrayLiteral.exprs.get(i);
            genNode(argExpr, this.env);

            BLangLiteral indexLiteral = new BLangLiteral();
            indexLiteral.pos = arrayLiteral.pos;
            indexLiteral.value = new Long(i);
            indexLiteral.type = symTable.intType;
            genNode(indexLiteral, this.env);

            opcode = getOpcode(argExpr.type.tag, InstructionCodes.IASTORE);
            emit(opcode, arrayVarRegIndex, indexLiteral.regIndex, argExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangJSONArrayLiteral arrayLiteral) {
        int jsonVarRegIndex = ++regIndexes.tRef;
        arrayLiteral.regIndex = jsonVarRegIndex;
        List<BLangExpression> argExprs = arrayLiteral.exprs;

        BLangLiteral arraySizeLiteral = new BLangLiteral();
        arraySizeLiteral.pos = arrayLiteral.pos;
        arraySizeLiteral.value = new Long(argExprs.size());
        arraySizeLiteral.type = symTable.intType;
        arraySizeLiteral.accept(this);

        emit(InstructionCodes.JSONNEWARRAY, jsonVarRegIndex, arraySizeLiteral.regIndex);

        for (int i = 0; i < argExprs.size(); i++) {
            BLangExpression argExpr = argExprs.get(i);
            genNode(argExpr, this.env);

            BLangLiteral indexLiteral = new BLangLiteral();
            indexLiteral.pos = arrayLiteral.pos;
            indexLiteral.value = new Long(i);
            indexLiteral.type = symTable.intType;
            genNode(indexLiteral, this.env);

            emit(InstructionCodes.JSONASTORE, jsonVarRegIndex, indexLiteral.regIndex, argExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangJSONLiteral jsonLiteral) {
        int jsonVarRegIndex = ++regIndexes.tRef;
        jsonLiteral.regIndex = jsonVarRegIndex;
        emit(InstructionCodes.NEWJSON, jsonVarRegIndex);

        for (BLangRecordKeyValue keyValue : jsonLiteral.keyValuePairs) {
            BLangExpression keyExpr = keyValue.key.expr;
            genNode(keyExpr, this.env);

            BLangExpression valueExpr = keyValue.valueExpr;
            genNode(valueExpr, this.env);

            emit(InstructionCodes.JSONSTORE, jsonVarRegIndex, keyExpr.regIndex, valueExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangMapLiteral mapLiteral) {
        int mapVarRegIndex = ++regIndexes.tRef;
        mapLiteral.regIndex = mapVarRegIndex;
        emit(InstructionCodes.NEWMAP, mapVarRegIndex);

        // Handle Map init stuff
        for (BLangRecordKeyValue keyValue : mapLiteral.keyValuePairs) {
            BLangExpression keyExpr = keyValue.key.expr;
            genNode(keyExpr, this.env);

            BLangExpression valueExpr = keyValue.valueExpr;
            genNode(valueExpr, this.env);

            emit(InstructionCodes.MAPSTORE, mapVarRegIndex, keyExpr.regIndex, valueExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangStructLiteral structLiteral) {
        BSymbol structSymbol = structLiteral.type.tsymbol;
        int pkgCPIndex = addPackageRefCPEntry(currentPkgInfo, structSymbol.pkgID);
        int structNameCPIndex = addUTF8CPEntry(currentPkgInfo, structSymbol.name.value);
        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, structNameCPIndex);
        int structCPIndex = currentPkgInfo.addCPEntry(structureRefCPEntry);

        //Emit an instruction to create a new struct.
        int structRegIndex = ++regIndexes.tRef;
        emit(InstructionCodes.NEWSTRUCT, structCPIndex, structRegIndex);
        structLiteral.regIndex = structRegIndex;

        for (BLangRecordKeyValue keyValue : structLiteral.keyValuePairs) {
            BLangRecordKey key = keyValue.key;
            int fieldIndex = key.fieldSymbol.varIndex;

            genNode(keyValue.valueExpr, this.env);

            int opcode = getOpcode(key.fieldSymbol.type.tag, InstructionCodes.IFIELDSTORE);
            emit(opcode, structRegIndex, fieldIndex, keyValue.valueExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangLocalVarRef localVarRef) {
        int lvIndex = localVarRef.symbol.varIndex;
        if (varAssignment) {
            int opcode = getOpcode(localVarRef.type.tag, InstructionCodes.ISTORE);
            emit(opcode, rhsExprRegIndex, lvIndex);
            return;
        }

        OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(localVarRef.type.tag,
                InstructionCodes.ILOAD, regIndexes);
        int opcode = opcodeAndIndex.opcode;
        int exprRegIndex = opcodeAndIndex.index;
        emit(opcode, lvIndex, exprRegIndex);
        localVarRef.regIndex = exprRegIndex;
    }

    @Override
    public void visit(BLangFieldVarRef fieldVarRef) {
        int varRegIndex;
        int fieldIndex = fieldVarRef.symbol.varIndex;

        // This is a connector field.
        // the connector reference must be stored in the current reference register index.
        varRegIndex = ++regIndexes.tRef;

        // The connector is always the first parameter of the action
        emit(InstructionCodes.RLOAD, 0, varRegIndex);

        if (varAssignment) {
            int opcode = getOpcode(fieldVarRef.type.tag,
                    InstructionCodes.IFIELDSTORE);
            emit(opcode, varRegIndex, fieldIndex, rhsExprRegIndex);
            return;
        }

        OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(fieldVarRef.type.tag,
                InstructionCodes.IFIELDLOAD, regIndexes);
        int opcode = opcodeAndIndex.opcode;
        int exprRegIndex = opcodeAndIndex.index;
        emit(opcode, varRegIndex, fieldIndex, exprRegIndex);
        fieldVarRef.regIndex = exprRegIndex;
    }

    @Override
    public void visit(BLangPackageVarRef packageVarRef) {
        int gvIndex = packageVarRef.symbol.varIndex;
        if (varAssignment) {
            int opcode = getOpcode(packageVarRef.type.tag,
                    InstructionCodes.IGSTORE);
            emit(opcode, rhsExprRegIndex, gvIndex);
            return;
        }

        OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(packageVarRef.type.tag,
                InstructionCodes.IGLOAD, regIndexes);
        int opcode = opcodeAndIndex.opcode;
        int exprRegIndex = opcodeAndIndex.index;
        emit(opcode, gvIndex, exprRegIndex);
        packageVarRef.regIndex = exprRegIndex;
    }

    @Override
    public void visit(BLangFunctionVarRef functionVarRef) {
        visitFunctionPointerLoad(functionVarRef, (BInvokableSymbol) functionVarRef.symbol);
    }

    @Override
    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(fieldAccessExpr.expr, this.env);
        int varRefRegIndex = fieldAccessExpr.expr.regIndex;

        int opcode;
        int fieldRegIndex;
        int fieldIndex = fieldAccessExpr.symbol.varIndex;
        if (variableStore) {
            opcode = getOpcode(fieldAccessExpr.symbol.type.tag, InstructionCodes.IFIELDSTORE);
            emit(opcode, varRefRegIndex, fieldIndex, rhsExprRegIndex);
        } else {
            OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(fieldAccessExpr.symbol.type.tag,
                    InstructionCodes.IFIELDLOAD, regIndexes);
            opcode = opcodeAndIndex.opcode;
            fieldRegIndex = opcodeAndIndex.index;

            emit(opcode, varRefRegIndex, fieldIndex, fieldRegIndex);
            fieldAccessExpr.regIndex = fieldRegIndex;
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangMapAccessExpr mapKeyAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(mapKeyAccessExpr.expr, this.env);
        int varRefRegIndex = mapKeyAccessExpr.expr.regIndex;

        genNode(mapKeyAccessExpr.indexExpr, this.env);
        int keyRegIndex = mapKeyAccessExpr.indexExpr.regIndex;

        if (variableStore) {
            emit(InstructionCodes.MAPSTORE, varRefRegIndex, keyRegIndex, rhsExprRegIndex);
        } else {
            int mapValueRegIndex = ++regIndexes.tRef;
            emit(InstructionCodes.MAPLOAD, varRefRegIndex, keyRegIndex, mapValueRegIndex);
            mapKeyAccessExpr.regIndex = mapValueRegIndex;
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangJSONAccessExpr jsonAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(jsonAccessExpr.expr, this.env);
        int varRefRegIndex = jsonAccessExpr.expr.regIndex;

        genNode(jsonAccessExpr.indexExpr, this.env);
        int keyRegIndex = jsonAccessExpr.indexExpr.regIndex;

        if (jsonAccessExpr.indexExpr.type.tag == TypeTags.INT) {
            if (variableStore) {
                emit(InstructionCodes.JSONASTORE, varRefRegIndex, keyRegIndex, rhsExprRegIndex);
            } else {
                int mapValueRegIndex = ++regIndexes.tRef;
                emit(InstructionCodes.JSONALOAD, varRefRegIndex, keyRegIndex, mapValueRegIndex);
                jsonAccessExpr.regIndex = mapValueRegIndex;
            }
        } else {
            if (variableStore) {
                emit(InstructionCodes.JSONSTORE, varRefRegIndex, keyRegIndex, rhsExprRegIndex);
            } else {
                int mapValueRegIndex = ++regIndexes.tRef;
                emit(InstructionCodes.JSONLOAD, varRefRegIndex, keyRegIndex, mapValueRegIndex);
                jsonAccessExpr.regIndex = mapValueRegIndex;
            }
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangXMLAccessExpr xmlIndexAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(xmlIndexAccessExpr.expr, this.env);
        int varRefRegIndex = xmlIndexAccessExpr.expr.regIndex;

        genNode(xmlIndexAccessExpr.indexExpr, this.env);
        int indexRegIndex = xmlIndexAccessExpr.indexExpr.regIndex;

        int elementRegIndex = ++regIndexes.tRef;
        emit(InstructionCodes.XMLLOAD, varRefRegIndex, indexRegIndex, elementRegIndex);
        xmlIndexAccessExpr.regIndex = elementRegIndex;
        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangArrayAccessExpr arrayIndexAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(arrayIndexAccessExpr.expr, this.env);
        int varRefRegIndex = arrayIndexAccessExpr.expr.regIndex;

        genNode(arrayIndexAccessExpr.indexExpr, this.env);
        int indexRegIndex = arrayIndexAccessExpr.indexExpr.regIndex;

        BArrayType arrayType = (BArrayType) arrayIndexAccessExpr.expr.type;
        if (variableStore) {
            int opcode = getOpcode(arrayType.eType.tag, InstructionCodes.IASTORE);
            emit(opcode, varRefRegIndex, indexRegIndex, rhsExprRegIndex);
        } else {
            OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(arrayType.eType.tag,
                    InstructionCodes.IALOAD, regIndexes);
            emit(opcodeAndIndex.opcode, varRefRegIndex, indexRegIndex, opcodeAndIndex.index);
            arrayIndexAccessExpr.regIndex = opcodeAndIndex.index;
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangEnumeratorAccessExpr enumeratorAccessExpr) {
        int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, enumeratorAccessExpr.type.getDesc());
        TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
        int typeCPIndex = currentPkgInfo.addCPEntry(typeRefCPEntry);
        int varIndex = enumeratorAccessExpr.symbol.varIndex;
        int regIndex = ++regIndexes.tRef;
        emit(InstructionCodes.ENUMERATORLOAD, typeCPIndex, varIndex, regIndex);
        enumeratorAccessExpr.regIndex = regIndex;
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        if (OperatorKind.AND.equals(binaryExpr.opKind)) {
            visitAndExpression(binaryExpr);
        } else if (OperatorKind.OR.equals(binaryExpr.opKind)) {
            visitOrExpression(binaryExpr);
        } else if (binaryExpr.opSymbol.opcode == InstructionCodes.REQ_NULL ||
                binaryExpr.opSymbol.opcode == InstructionCodes.RNE_NULL ||
                binaryExpr.opSymbol.opcode == InstructionCodes.SEQ_NULL ||
                binaryExpr.opSymbol.opcode == InstructionCodes.SNE_NULL) {
            BLangExpression expr = (binaryExpr.lhsExpr.type.tag == TypeTags.NULL) ?
                    binaryExpr.rhsExpr : binaryExpr.lhsExpr;
            genNode(expr, this.env);
            int opcode = binaryExpr.opSymbol.opcode;
            int exprIndex = getNextIndex(binaryExpr.type.tag, regIndexes);
            binaryExpr.regIndex = exprIndex;
            emit(opcode, expr.regIndex, exprIndex);
        } else {
            genNode(binaryExpr.lhsExpr, this.env);
            genNode(binaryExpr.rhsExpr, this.env);

            int opcode = binaryExpr.opSymbol.opcode;
            int exprIndex = getNextIndex(binaryExpr.type.tag, regIndexes);

            binaryExpr.regIndex = exprIndex;
            emit(opcode, binaryExpr.lhsExpr.regIndex, binaryExpr.rhsExpr.regIndex, exprIndex);
        }
    }

    private void visitAndExpression(BLangBinaryExpr binaryExpr) {
        // Generate code for the left hand side
        genNode(binaryExpr.lhsExpr, this.env);

        // Last operand will be filled later.
        Instruction lEvalInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE,
                binaryExpr.lhsExpr.regIndex, -1);
        emit(lEvalInstruction);

        // Generate code for the right hand side
        genNode(binaryExpr.rhsExpr, this.env);

        // Last operand will be filled later.
        Instruction rEvalInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE,
                binaryExpr.rhsExpr.regIndex, -1);
        emit(rEvalInstruction);

        // If both l and r conditions are true, then load 'true'
        int exprRegIndex = ++regIndexes.tBoolean;
        binaryExpr.regIndex = exprRegIndex;
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

    private void visitOrExpression (BLangBinaryExpr binaryExpr) {
        // Generate code for the left hand side
        genNode(binaryExpr.lhsExpr, this.env);

        // Last operand will be filled later.
        Instruction lEvalInstruction = InstructionFactory.get(InstructionCodes.BR_TRUE,
                binaryExpr.lhsExpr.regIndex, -1);
        emit(lEvalInstruction);

        // Generate code for the right hand side
        genNode(binaryExpr.rhsExpr, this.env);

        // Last operand will be filled later.
        Instruction rEvalInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE,
                binaryExpr.rhsExpr.regIndex, -1);
        emit(rEvalInstruction);

        // If either l and r conditions are true, then load 'true'
        lEvalInstruction.setOperand(1, nextIP());
        int exprRegIndex = ++regIndexes.tBoolean;
        binaryExpr.regIndex = exprRegIndex;
        emit(InstructionCodes.BCONST_1, exprRegIndex);

        Instruction goToIns = InstructionFactory.get(InstructionCodes.GOTO, -1);
        emit(goToIns);
        rEvalInstruction.setOperand(1, nextIP());

        // Load 'false' if the both conditions are false;
        emit(InstructionCodes.BCONST_0, exprRegIndex);
        goToIns.setOperand(0, nextIP());
    }

    public void visit(BLangInvocation iExpr) {
        if (iExpr.expr == null) {
            BInvokableSymbol funcSymbol = (BInvokableSymbol) iExpr.symbol;
            int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, funcSymbol.pkgID);
            int funcNameCPIndex = addUTF8CPEntry(currentPkgInfo, funcSymbol.name.value);
            FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgRefCPIndex, funcNameCPIndex);

            int funcCallCPIndex = getFunctionCallCPIndex(iExpr);
            int funcRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);

            if (Symbols.isNative(funcSymbol)) {
                emit(InstructionCodes.NCALL, funcRefCPIndex, funcCallCPIndex);
            } else {
                emit(InstructionCodes.CALL, funcRefCPIndex, funcCallCPIndex);
            }
        }
    }

    public void visit(BLangActionInvocation aIExpr) {
        BInvokableSymbol actionSymbol = (BInvokableSymbol) aIExpr.symbol;
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, actionSymbol.pkgID);
        int actionNameCPIndex = addUTF8CPEntry(currentPkgInfo, actionSymbol.name.value);

        ActionRefCPEntry actionRefCPEntry = new ActionRefCPEntry(pkgRefCPIndex, actionNameCPIndex);
        int actionRefCPIndex = currentPkgInfo.addCPEntry(actionRefCPEntry);
        int actionCallIndex = getFunctionCallCPIndex(aIExpr);

        emit(InstructionCodes.ACALL, actionRefCPIndex, actionCallIndex);
    }

    public void visit(BLangConnectorInit cIExpr) {
        BConnectorType connectorType = (BConnectorType) cIExpr.type;
        BTypeSymbol connectorSymbol = connectorType.tsymbol;

        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, connectorSymbol.pkgID);
        int connNameCPIndex = addUTF8CPEntry(currentPkgInfo, connectorType.tsymbol.name.value);

        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgRefCPIndex, connNameCPIndex);
        int structureRefCPIndex = currentPkgInfo.addCPEntry(structureRefCPEntry);
        //Emit an instruction to create a new connector.
        int connectorRegIndex = ++regIndexes.tRef;
        cIExpr.regIndex = connectorRegIndex;
        emit(InstructionCodes.NEWCONNECTOR, structureRefCPIndex, connectorRegIndex);

        List<BLangExpression> argExprs = cIExpr.argsExpr;
        for (int i = 0; i < argExprs.size(); i++) {
            BLangExpression argExpr = argExprs.get(i);
            genNode(argExpr, this.env);
            BVarSymbol paramSymbol = connectorType.tsymbol.params.get(i);

            int fieldIndex = paramSymbol.varIndex;
            int opcode = getOpcode(paramSymbol.type.tag, InstructionCodes.IFIELDSTORE);
            emit(opcode, connectorRegIndex, fieldIndex, argExpr.regIndex);
        }

        BInvokableSymbol initFunc = connectorSymbol.initFunctionSymbol;
        int initFuncNameIndex = addUTF8CPEntry(currentPkgInfo, initFunc.name.value);
        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgRefCPIndex, initFuncNameIndex);
        int initFuncRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);
        FunctionCallCPEntry initFuncCallCPEntry = new FunctionCallCPEntry(new int[]{connectorRegIndex}, new int[0]);
        int initFuncCallIndex = currentPkgInfo.addCPEntry(initFuncCallCPEntry);
        emit(InstructionCodes.CALL, initFuncRefCPIndex, initFuncCallIndex);

        int actionNameCPIndex = addUTF8CPEntry(currentPkgInfo, "<init>");
        ActionRefCPEntry actionRefCPEntry = new ActionRefCPEntry(pkgRefCPIndex, actionNameCPIndex);
        int actionRefCPIndex = currentPkgInfo.addCPEntry(actionRefCPEntry);
        emit(InstructionCodes.ACALL, actionRefCPIndex, initFuncCallIndex);
    }

    public void visit(BLangFunctionInvocation iExpr) {
        BInvokableSymbol funcSymbol = (BInvokableSymbol) iExpr.symbol;
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, funcSymbol.pkgID);
        int funcNameCPIndex = addUTF8CPEntry(currentPkgInfo, funcSymbol.name.value);
        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgRefCPIndex, funcNameCPIndex);

        int funcCallCPIndex = getFunctionCallCPIndex(iExpr);
        int funcRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);

        if (Symbols.isNative(funcSymbol)) {
            emit(InstructionCodes.NCALL, funcRefCPIndex, funcCallCPIndex);
        } else {
            emit(InstructionCodes.CALL, funcRefCPIndex, funcCallCPIndex);
        }
    }

    public void visit(BLangTransformerInvocation iExpr) {
        BInvokableSymbol transformerSymbol = (BInvokableSymbol) iExpr.symbol;
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, transformerSymbol.pkgID);
        int transformerNameCPIndex = addUTF8CPEntry(currentPkgInfo, transformerSymbol.name.value);
        TransformerRefCPEntry transformerRefCPEntry = new TransformerRefCPEntry(pkgRefCPIndex, transformerNameCPIndex);

        int transformerCallCPIndex = getFunctionCallCPIndex(iExpr);
        int transformerRefCPIndex = currentPkgInfo.addCPEntry(transformerRefCPEntry);

        emit(InstructionCodes.TCALL, transformerRefCPIndex, transformerCallCPIndex);
    }

    public void visit(BFunctionPointerInvocation iExpr) {
        int funcCallCPIndex = getFunctionCallCPIndex(iExpr);
        genNode(iExpr.expr, env);
        emit(InstructionCodes.FPCALL, regIndexes.tRef, funcCallCPIndex);
    }

    public void visit(BLangTypeCastExpr castExpr) {
        BLangExpression rExpr = castExpr.expr;
        genNode(rExpr, this.env);

        // TODO Improve following logic
        int opCode = castExpr.castSymbol.opcode;
        int errorRegIndex = ++regIndexes.tRef;

        if (opCode == InstructionCodes.CHECKCAST) {
            int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, castExpr.type.getDesc());
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
            int typeCPIndex = currentPkgInfo.addCPEntry(typeRefCPEntry);
            int targetRegIndex = getNextIndex(castExpr.type.tag, regIndexes);

            castExpr.regIndexes = new int[]{targetRegIndex, errorRegIndex};
            emit(opCode, rExpr.regIndex, typeCPIndex, targetRegIndex, errorRegIndex);

        } else if (opCode == InstructionCodes.ANY2T ||
                opCode == InstructionCodes.ANY2C ||
                opCode == InstructionCodes.ANY2E) {
            int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, castExpr.type.getDesc());
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
            int typeCPIndex = currentPkgInfo.addCPEntry(typeRefCPEntry);
            int targetRegIndex = getNextIndex(castExpr.type.tag, regIndexes);

            castExpr.regIndexes = new int[]{targetRegIndex, errorRegIndex};
            emit(opCode, rExpr.regIndex, typeCPIndex, targetRegIndex, errorRegIndex);

        } else if (opCode != 0) {
            int targetRegIndex = getNextIndex(castExpr.type.tag, regIndexes);
            castExpr.regIndexes = new int[]{targetRegIndex, errorRegIndex};
            emit(opCode, rExpr.regIndex, targetRegIndex, errorRegIndex);

        } else {
            // Ignore NOP opcode
            castExpr.regIndexes = new int[]{rExpr.regIndex, errorRegIndex};
        }

        castExpr.regIndex = castExpr.regIndexes[0];
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        BLangExpression rExpr = conversionExpr.expr;
        genNode(rExpr, this.env);

        int opCode = conversionExpr.conversionSymbol.opcode;
        int errorRegIndex = ++regIndexes.tRef;

        if (opCode == InstructionCodes.MAP2T || opCode == InstructionCodes.JSON2T) {
            int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, conversionExpr.type.getDesc());
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
            int typeCPIndex = currentPkgInfo.addCPEntry(typeRefCPEntry);
            int targetRegIndex = getNextIndex(conversionExpr.type.tag, regIndexes);

            conversionExpr.regIndexes = new int[]{targetRegIndex, errorRegIndex};
            emit(opCode, rExpr.regIndex, typeCPIndex, targetRegIndex, errorRegIndex);

        } else if (opCode != 0) {
            int targetRegIndex = getNextIndex(conversionExpr.type.tag, regIndexes);
            conversionExpr.regIndexes = new int[]{targetRegIndex, errorRegIndex};
            emit(opCode, rExpr.regIndex, targetRegIndex, errorRegIndex);

        } else {
            // Ignore  NOP opcode
            conversionExpr.regIndexes = new int[]{rExpr.regIndex, errorRegIndex};
        }

        conversionExpr.regIndex = conversionExpr.regIndexes[0];
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        /* ignore */
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        this.genNode(ternaryExpr.expr, this.env);
        Instruction ifCondJumpInstr = InstructionFactory.get(InstructionCodes.BR_FALSE, ternaryExpr.expr.regIndex, -1);
        this.emit(ifCondJumpInstr);
        this.genNode(ternaryExpr.thenExpr, this.env);
        ternaryExpr.regIndex = ternaryExpr.thenExpr.regIndex;
        Instruction endJumpInstr = InstructionFactory.get(InstructionCodes.GOTO, -1);
        this.emit(endJumpInstr);
        ifCondJumpInstr.setOperand(1, this.nextIP());
        this.genNode(ternaryExpr.elseExpr, this.env);
        Instruction instruction = InstructionFactory.get(InstructionCodes.REG_CP, ternaryExpr.type.tag,
                ternaryExpr.elseExpr.regIndex, ternaryExpr.regIndex);
        this.emit(instruction);
        endJumpInstr.setOperand(0, this.nextIP());
    }

    public void visit(BLangTypeofExpr accessExpr) {
        int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, accessExpr.resolvedType.getDesc());
        TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
        int typeCPIndex = currentPkgInfo.addCPEntry(typeRefCPEntry);

        int opcode;
        int exprIndex;

        exprIndex = ++regIndexes.tRef;
        opcode = InstructionCodes.TYPELOAD;
        emit(opcode, typeCPIndex, exprIndex);
        accessExpr.regIndex = exprIndex;
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        genNode(unaryExpr.expr, this.env);

        int opcode;
        int exprIndex;

        if (OperatorKind.TYPEOF.equals(unaryExpr.operator)) {
            if (unaryExpr.expr.type.tag == TypeTags.ANY) {
                exprIndex = ++regIndexes.tRef;
                opcode = unaryExpr.opSymbol.opcode;
                emit(opcode, unaryExpr.expr.regIndex, exprIndex);
            } else {
                int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, unaryExpr.expr.type.getDesc());
                TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
                int typeCPIndex = currentPkgInfo.addCPEntry(typeRefCPEntry);

                exprIndex = ++regIndexes.tRef;
                opcode = unaryExpr.opSymbol.opcode;
                emit(opcode, typeCPIndex, exprIndex);
            }
            unaryExpr.regIndex = exprIndex;
        } else if (OperatorKind.ADD.equals(unaryExpr.operator)) {
            unaryExpr.regIndex = unaryExpr.expr.regIndex;
        } else {
            opcode = unaryExpr.opSymbol.opcode;
            exprIndex = getNextIndex(unaryExpr.type.tag, regIndexes);

            unaryExpr.regIndex = exprIndex;
            emit(opcode, unaryExpr.expr.regIndex, exprIndex);
        }
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        visitFunctionPointerLoad(bLangLambdaFunction, ((BLangFunction) bLangLambdaFunction.getFunctionNode()).symbol);
    }


    // private methods

    private void genNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        node.accept(this);
        this.env = prevEnv;
    }

    private void genPackage(BPackageSymbol pkgSymbol) {
        // TODO First check whether this symbol is from a BALO file.
        SymbolEnv pkgEnv = symEnter.packageEnvs.get(pkgSymbol);
        genNode(pkgEnv.node, pkgEnv);
    }

    private String generateSignature(CallableUnitInfo callableUnitInfo) {
        StringBuilder strBuilder = new StringBuilder("(");
        for (BType paramType : callableUnitInfo.paramTypes) {
            strBuilder.append(paramType.getDesc());
        }
        strBuilder.append(")(");

        for (BType retType : callableUnitInfo.retParamTypes) {
            strBuilder.append(retType.getDesc());
        }
        strBuilder.append(")");

        return strBuilder.toString();
    }

    private String generateSignature(ConnectorInfo callableUnitInfo) {
        StringBuilder strBuilder = new StringBuilder("(");
        for (BType paramType : callableUnitInfo.paramTypes) {
            strBuilder.append(paramType.getDesc());
        }
        strBuilder.append(")");

        return strBuilder.toString();
    }

    private OpcodeAndIndex getOpcodeAndIndex(int typeTag, int baseOpcode, VariableIndex indexes) {
        int index;
        int opcode;
        switch (typeTag) {
            case TypeTags.INT:
                opcode = baseOpcode;
                index = ++indexes.tInt;
                break;
            case TypeTags.FLOAT:
                opcode = baseOpcode + FLOAT_OFFSET;
                index = ++indexes.tFloat;
                break;
            case TypeTags.STRING:
                opcode = baseOpcode + STRING_OFFSET;
                index = ++indexes.tString;
                break;
            case TypeTags.BOOLEAN:
                opcode = baseOpcode + BOOL_OFFSET;
                index = ++indexes.tBoolean;
                break;
            case TypeTags.BLOB:
                opcode = baseOpcode + BLOB_OFFSET;
                index = ++indexes.tBlob;
                break;
            default:
                opcode = baseOpcode + REF_OFFSET;
                index = ++indexes.tRef;
                break;
        }

        return new OpcodeAndIndex(opcode, index);
    }

    private int getNextIndex(int typeTag, VariableIndex indexes) {
        return getOpcodeAndIndex(typeTag, -1, indexes).index;
    }

    private int getOpcode(int typeTag, int baseOpcode) {
        int opcode;
        switch (typeTag) {
            case TypeTags.INT:
                opcode = baseOpcode;
                break;
            case TypeTags.FLOAT:
                opcode = baseOpcode + FLOAT_OFFSET;
                break;
            case TypeTags.STRING:
                opcode = baseOpcode + STRING_OFFSET;
                break;
            case TypeTags.BOOLEAN:
                opcode = baseOpcode + BOOL_OFFSET;
                break;
            case TypeTags.BLOB:
                opcode = baseOpcode + BLOB_OFFSET;
                break;
            default:
                opcode = baseOpcode + REF_OFFSET;
                break;
        }

        return opcode;
    }

    private LocalVariableInfo getLocalVarAttributeInfo(BVarSymbol varSymbol) {
        int varNameCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.name.value);
        int varIndex = varSymbol.varIndex;
        int sigCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.type.getDesc());
        return new LocalVariableInfo(varNameCPIndex, sigCPIndex, varIndex);
    }

    private AnnAttachmentInfo getAnnotationAttachmentInfo(BLangAnnotationAttachment attachment) {
        int attachmentNameCPIndex = addUTF8CPEntry(currentPkgInfo, attachment.getAnnotationName().getValue());
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, attachment.annotationSymbol.pkgID);
        AnnAttachmentInfo annAttachmentInfo = new AnnAttachmentInfo(pkgRefCPIndex, attachmentNameCPIndex);
        attachment.attributes.forEach(attr -> {
            AnnAttributeValue attribValue = getAnnotationAttributeValue(attr.value);
            int attributeNameCPIndex = addUTF8CPEntry(currentPkgInfo, attr.getName().getValue());
            annAttachmentInfo.addAttributeValue(attributeNameCPIndex, attr.getName().getValue(), attribValue);
        });
        return annAttachmentInfo;
    }

    private AnnAttributeValue getAnnotationAttributeValue(BLangAnnotAttachmentAttributeValue attributeNode) {
        AnnAttributeValue attribValue = null;
        if (attributeNode.value != null
                && attributeNode.value instanceof BLangLiteral) {
            // Annotation attribute value is a literal value
            BLangLiteral literalValue = (BLangLiteral) attributeNode.value;
            String typeDesc = literalValue.type.getDesc();
            UTF8CPEntry typeDescCPEntry = new UTF8CPEntry(typeDesc);
            int typeDescCPIndex = currentPkgInfo.addCPEntry(typeDescCPEntry);
            attribValue = new AnnAttributeValue(typeDescCPIndex, typeDesc);

            int valueCPIndex;
            int typeTag = literalValue.type.tag;
            switch (typeTag) {
                case TypeTags.INT:
                    long intValue = (long) literalValue.value;
                    attribValue.setIntValue(intValue);
                    valueCPIndex = currentPkgInfo.addCPEntry(new IntegerCPEntry(intValue));
                    attribValue.setValueCPIndex(valueCPIndex);

                    break;
                case TypeTags.FLOAT:
                    double floatValue = (double) literalValue.value;
                    attribValue.setFloatValue(floatValue);
                    valueCPIndex = currentPkgInfo.addCPEntry(new FloatCPEntry(floatValue));
                    attribValue.setValueCPIndex(valueCPIndex);

                    break;
                case TypeTags.STRING:
                    String stringValue = (String) literalValue.value;
                    attribValue.setStringValue(stringValue);
                    valueCPIndex = currentPkgInfo.addCPEntry(new UTF8CPEntry(stringValue));
                    attribValue.setValueCPIndex(valueCPIndex);

                    break;
                case TypeTags.BOOLEAN:
                    boolean boolValue = (boolean) literalValue.value;
                    attribValue.setBooleanValue(boolValue);
                    break;
            }

        } else if (attributeNode.value != null
                && attributeNode.value instanceof BLangSimpleVarRef) {
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) attributeNode.value;
            String typeDesc = simpleVarRef.type.getDesc();
            UTF8CPEntry typeDescCPEntry = new UTF8CPEntry(typeDesc);
            int typeDescCPIndex = currentPkgInfo.addCPEntry(typeDescCPEntry);


            String constPkg = simpleVarRef.symbol.pkgID.getName().getValue();
            UTF8CPEntry constPkgCPEntry = new UTF8CPEntry(constPkg);
            int constPkgCPIndex = currentPkgInfo.addCPEntry(constPkgCPEntry);

            String constName = simpleVarRef.symbol.name.getValue();
            UTF8CPEntry constNameCPEntry = new UTF8CPEntry(constName);
            int constNameCPIndex = currentPkgInfo.addCPEntry(constNameCPEntry);

            attribValue = new AnnAttributeValue(typeDescCPIndex, typeDesc, constPkgCPIndex,
                    constPkg, constNameCPIndex, constName);
            attribValue.setConstVarExpr(true);

            programFile.addUnresolvedAnnAttrValue(attribValue);
        } else if (attributeNode.value != null) {
            // Annotation attribute value is another annotation attachment
            BLangAnnotationAttachment attachment = (BLangAnnotationAttachment) attributeNode.value;
            AnnAttachmentInfo attachmentInfo = getAnnotationAttachmentInfo(attachment);

            String typeDesc = TypeDescriptor.SIG_ANNOTATION;
            UTF8CPEntry typeDescCPEntry = new UTF8CPEntry(typeDesc);
            int typeDescCPIndex = currentPkgInfo.addCPEntry(typeDescCPEntry);
            attribValue = new AnnAttributeValue(typeDescCPIndex, typeDesc, attachmentInfo);

        } else {
            List<BLangAnnotAttachmentAttributeValue> attributeValues =
                    attributeNode.arrayValues;
            AnnAttributeValue[] annotationAttribValues = new AnnAttributeValue[attributeValues.size()];
            for (int i = 0; i < attributeValues.size(); i++) {
                annotationAttribValues[i] = getAnnotationAttributeValue(attributeValues.get(i));
            }

            String typeDesc = TypeDescriptor.SIG_ARRAY;
            UTF8CPEntry typeDescCPEntry = new UTF8CPEntry(typeDesc);
            int typeDescCPIndex = currentPkgInfo.addCPEntry(typeDescCPEntry);
            attribValue = new AnnAttributeValue(typeDescCPIndex, typeDesc, annotationAttribValues);
        }

        //TODO:create AnnAttributeValue
        return attribValue;
    }

    private void visitInvokableNode(BLangInvokableNode invokableNode,
                                    CallableUnitInfo callableUnitInfo,
                                    SymbolEnv invokableSymbolEnv) {
        int localVarAttrNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.value());
        LocalVariableAttributeInfo localVarAttributeInfo = new LocalVariableAttributeInfo(localVarAttrNameIndex);

        // TODO Read annotations attached to this callableUnit

        // Add local variable indexes to the parameters and return parameters
        visitInvokableNodeParams(invokableNode.symbol, callableUnitInfo, localVarAttributeInfo);

        if (Symbols.isNative(invokableNode.symbol)) {
            this.processWorker(invokableNode, callableUnitInfo.defaultWorkerInfo, null,
                    localVarAttributeInfo, invokableSymbolEnv, true, null);
        } else {
            // Clone lvIndex structure here. This structure contain local variable indexes of the input and
            // out parameters and they are common for all the workers.
            VariableIndex lvIndexCopy = this.copyVarIndex(lvIndexes);
            this.processWorker(invokableNode, callableUnitInfo.defaultWorkerInfo, invokableNode.body,
                    localVarAttributeInfo, invokableSymbolEnv, true, lvIndexCopy);
            for (BLangWorker worker : invokableNode.getWorkers()) {
                this.processWorker(invokableNode, callableUnitInfo.getWorkerInfo(worker.name.value),
                        worker.body, localVarAttributeInfo, invokableSymbolEnv, false, this.copyVarIndex(lvIndexCopy));
            }
        }
    }

    private void processWorker(BLangInvokableNode invokableNode, WorkerInfo workerInfo, BLangBlockStmt body,
                               LocalVariableAttributeInfo localVarAttributeInfo, SymbolEnv invokableSymbolEnv,
                               boolean defaultWorker, VariableIndex lvIndexCopy) {
        int codeAttrNameCPIndex = this.addUTF8CPEntry(this.currentPkgInfo, AttributeInfo.Kind.CODE_ATTRIBUTE.value());
        workerInfo.codeAttributeInfo.attributeNameIndex = codeAttrNameCPIndex;
        workerInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttributeInfo);
        if (body != null) {
            localVarAttrInfo = new LocalVariableAttributeInfo(localVarAttributeInfo.attributeNameIndex);
            localVarAttrInfo.localVars = new ArrayList<>(localVarAttributeInfo.localVars);
            workerInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttrInfo);
            workerInfo.codeAttributeInfo.codeAddrs = nextIP();
            this.lvIndexes = lvIndexCopy;
            this.currentWorkerInfo = workerInfo;
            this.genNode(body, invokableSymbolEnv);
            if (defaultWorker && invokableNode.workers.size() > 0) {
                this.emit(InstructionCodes.WRKSTART);
                if (invokableNode.retParams.size() == 0) {
                    this.emit(InstructionCodes.RET);
                } else {
                    this.emit(InstructionCodes.HALT);
                }
            }
        }
        this.endWorkerInfoUnit(workerInfo.codeAttributeInfo);
        if (!defaultWorker) {
            this.emit(InstructionCodes.HALT);
        }
    }

    private void visitInvokableNodeParams(BInvokableSymbol invokableSymbol, CallableUnitInfo callableUnitInfo,
                                          LocalVariableAttributeInfo localVarAttrInfo) {

        // TODO Read param and return param annotations
        invokableSymbol.params.forEach(param -> visitInvokableNodeParam(param, localVarAttrInfo));
        invokableSymbol.retParams.forEach(param -> visitInvokableNodeParam(param, localVarAttrInfo));
        callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttrInfo);
    }

    private void visitInvokableNodeParam(BVarSymbol paramSymbol, LocalVariableAttributeInfo localVarAttrInfo) {
        paramSymbol.varIndex = getNextIndex(paramSymbol.type.tag, lvIndexes);
        LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(paramSymbol);
        localVarAttrInfo.localVars.add(localVarInfo);
        // TODO read parameter annotations
    }

    private void visitServiceNodeVariable(BVarSymbol variableSymbol, LocalVariableAttributeInfo localVarAttrInfo) {
        variableSymbol.varIndex = getNextIndex(variableSymbol.type.tag, pvIndexes);
        LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(variableSymbol);
        localVarAttrInfo.localVars.add(localVarInfo);
    }

    private void visitConnectorNodeVariable(BVarSymbol variableSymbol, LocalVariableAttributeInfo localVarAttrInfo) {
        variableSymbol.varIndex = getNextIndex(variableSymbol.type.tag, fieldIndexes);
        LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(variableSymbol);
        localVarAttrInfo.localVars.add(localVarInfo);
    }

    private void visitAnnotationAttachment(BLangAnnotationAttachment annotationAttachment,
                                                  AnnotationAttributeInfo annotationAttributeInfo) {
        AnnAttachmentInfo attachmentInfo = getAnnotationAttachmentInfo(annotationAttachment);
        annotationAttributeInfo.attachmentList.add(attachmentInfo);
    }

    private void visitReturnStatementsExprs(BLangReturn returnNode) {
        BLangExpression expr;
        int i = 0;
        while (i < returnNode.exprs.size()) {
            expr = returnNode.exprs.get(i);
            this.genNode(expr, this.env);
            if (expr.isMultiReturnExpr()) {
                MultiReturnExpr invExpr = (MultiReturnExpr) expr;
                for (int j = 0; j < invExpr.getRegIndexes().length; j++) {
                    emit(this.typeTagToInstr(invExpr.getTypes().get(j).tag), i, invExpr.getRegIndexes()[j]);
                    i++;
                }
            } else {
                emit(this.typeTagToInstr(expr.type.tag), i, expr.regIndex);
                i++;
            }
        }
        generateFinallyInstructions(returnNode);
    }

    private VariableIndex copyVarIndex(VariableIndex that) {
        VariableIndex vIndexes = new VariableIndex();
        vIndexes.tInt = that.tInt;
        vIndexes.tFloat = that.tFloat;
        vIndexes.tString = that.tString;
        vIndexes.tBoolean = that.tBoolean;
        vIndexes.tBlob = that.tBlob;
        vIndexes.tRef = that.tRef;
        return vIndexes;
    }

    private int nextIP() {
        return currentPkgInfo.instructionList.size();
    }

    private void endWorkerInfoUnit(CodeAttributeInfo codeAttributeInfo) {
        codeAttributeInfo.maxLongLocalVars = lvIndexes.tInt + 1;
        codeAttributeInfo.maxDoubleLocalVars = lvIndexes.tFloat + 1;
        codeAttributeInfo.maxStringLocalVars = lvIndexes.tString + 1;
        codeAttributeInfo.maxIntLocalVars = lvIndexes.tBoolean + 1;
        codeAttributeInfo.maxByteLocalVars = lvIndexes.tBlob + 1;
        codeAttributeInfo.maxRefLocalVars = lvIndexes.tRef + 1;

        codeAttributeInfo.maxLongRegs = maxRegIndexes.tInt + 1;
        codeAttributeInfo.maxDoubleRegs = maxRegIndexes.tFloat + 1;
        codeAttributeInfo.maxStringRegs = maxRegIndexes.tString + 1;
        codeAttributeInfo.maxIntRegs = maxRegIndexes.tBoolean + 1;
        codeAttributeInfo.maxByteRegs = maxRegIndexes.tBlob + 1;
        codeAttributeInfo.maxRefRegs = maxRegIndexes.tRef + 1;

        lvIndexes = new VariableIndex();
        regIndexes = new VariableIndex();
        maxRegIndexes = new VariableIndex();
    }

    private void setMaxRegIndexes(VariableIndex current, VariableIndex max) {
        max.tInt = (max.tInt > current.tInt) ? max.tInt : current.tInt;
        max.tFloat = (max.tFloat > current.tFloat) ? max.tFloat : current.tFloat;
        max.tString = (max.tString > current.tString) ? max.tString : current.tString;
        max.tBoolean = (max.tBoolean > current.tBoolean) ? max.tBoolean : current.tBoolean;
        max.tBlob = (max.tBlob > current.tBlob) ? max.tBlob : current.tBlob;
        max.tRef = (max.tRef > current.tRef) ? max.tRef : current.tRef;
    }

    private void prepareIndexes(VariableIndex indexes) {
        indexes.tInt++;
        indexes.tFloat++;
        indexes.tString++;
        indexes.tBoolean++;
        indexes.tBlob++;
        indexes.tRef++;
    }

    private int emit(int opcode, int... operands) {
        currentPkgInfo.instructionList.add(InstructionFactory.get(opcode, operands));
        return currentPkgInfo.instructionList.size();
    }

    private int emit(Instruction instr) {
        currentPkgInfo.instructionList.add(instr);
        return currentPkgInfo.instructionList.size();
    }

    private void addVarCountAttrInfo(ConstantPool constantPool,
                                     AttributeInfoPool attributeInfoPool,
                                     VariableIndex fieldCount) {
        int attrNameCPIndex = addUTF8CPEntry(constantPool,
                AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE.value());
        VarTypeCountAttributeInfo varCountAttribInfo = new VarTypeCountAttributeInfo(attrNameCPIndex);
        varCountAttribInfo.setMaxLongVars(fieldCount.tInt);
        varCountAttribInfo.setMaxDoubleVars(fieldCount.tFloat);
        varCountAttribInfo.setMaxStringVars(fieldCount.tString);
        varCountAttribInfo.setMaxIntVars(fieldCount.tBoolean);
        varCountAttribInfo.setMaxByteVars(fieldCount.tBlob);
        varCountAttribInfo.setMaxRefVars(fieldCount.tRef);
        attributeInfoPool.addAttributeInfo(AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE, varCountAttribInfo);
    }

    private int getFunctionCallCPIndex(BLangInvocation iExpr) {
        int[] argRegs = new int[iExpr.argExprs.size()];
        for (int i = 0; i < iExpr.argExprs.size(); i++) {
            BLangExpression argExpr = iExpr.argExprs.get(i);
            genNode(argExpr, this.env);
            argRegs[i] = argExpr.regIndex;
        }

        // Calculate registers to store return values
        int[] retRegs = new int[iExpr.types.size()];
        for (int i = 0; i < iExpr.types.size(); i++) {
            BType retType = iExpr.types.get(i);
            retRegs[i] = getNextIndex(retType.tag, regIndexes);
        }

        iExpr.regIndexes = retRegs;
        if (retRegs.length > 0) {
            iExpr.regIndex = retRegs[0];
        }

        FunctionCallCPEntry funcCallCPEntry = new FunctionCallCPEntry(argRegs, retRegs);
        return currentPkgInfo.addCPEntry(funcCallCPEntry);
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

    private DefaultValueAttributeInfo getStructFieldDefaultValue(BLangLiteral literalExpr) {
        String desc = literalExpr.type.getDesc();
        int typeDescCPIndex = addUTF8CPEntry(currentPkgInfo, desc);
        StructFieldDefaultValue defaultValue = new StructFieldDefaultValue(typeDescCPIndex, desc);

        int typeTag = literalExpr.type.tag;
        switch (typeTag) {
            case TypeTags.INT:
                defaultValue.intValue = (Long) literalExpr.value;
                defaultValue.valueCPIndex = currentPkgInfo.addCPEntry(new IntegerCPEntry(defaultValue.intValue));
                break;
            case TypeTags.FLOAT:
                defaultValue.floatValue = (Double) literalExpr.value;
                defaultValue.valueCPIndex = currentPkgInfo.addCPEntry(new FloatCPEntry(defaultValue.floatValue));
                break;
            case TypeTags.STRING:
                defaultValue.stringValue = (String) literalExpr.value;
                defaultValue.valueCPIndex = currentPkgInfo.addCPEntry(new UTF8CPEntry(defaultValue.stringValue));
                break;
            case TypeTags.BOOLEAN:
                defaultValue.booleanValue = (Boolean) literalExpr.value;
                break;
        }

        UTF8CPEntry defaultValueAttribUTF8CPEntry =
                new UTF8CPEntry(AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE.toString());
        int defaultValueAttribNameIndex = currentPkgInfo.addCPEntry(defaultValueAttribUTF8CPEntry);

        return new DefaultValueAttributeInfo(defaultValueAttribNameIndex, defaultValue);
    }


    // Create info entries

    private void createPackageVarInfo(BLangVariable varNode) {
        BVarSymbol varSymbol = varNode.symbol;
        BType varType = varSymbol.type;
        varSymbol.varIndex = getNextIndex(varType.tag, pvIndexes);

        int varNameCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.name.value);
        int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, varType.getDesc());
        PackageVarInfo pkgVarInfo = new PackageVarInfo(varNameCPIndex, typeSigCPIndex, varSymbol.flags);
        currentPkgInfo.pkgVarInfoMap.put(varSymbol.name.value, pkgVarInfo);

        LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(varSymbol);
        LocalVariableAttributeInfo pkgVarAttrInfo = (LocalVariableAttributeInfo)
                currentPkgInfo.getAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);
        pkgVarAttrInfo.localVars.add(localVarInfo);

        // TODO Populate annotation attribute
    }

    private void createStructInfoEntry(BLangStruct structNode) {
        BTypeSymbol structSymbol = (BTypeSymbol) structNode.symbol;
        // Add Struct name as an UTFCPEntry to the constant pool
        int structNameCPIndex = addUTF8CPEntry(currentPkgInfo, structSymbol.name.value);
        StructInfo structInfo = new StructInfo(currentPackageRefCPIndex, structNameCPIndex);
        currentPkgInfo.addStructInfo(structSymbol.name.value, structInfo);
        structInfo.structType = (BStructType) structSymbol.type;

        List<BLangVariable> structFields = structNode.fields;
        for (BLangVariable structField : structFields) {
            // Create StructFieldInfo Entry
            int fieldNameCPIndex = addUTF8CPEntry(currentPkgInfo, structField.name.value);
            int sigCPIndex = addUTF8CPEntry(currentPkgInfo, structField.type.getDesc());

            StructFieldInfo structFieldInfo = new StructFieldInfo(fieldNameCPIndex, sigCPIndex);
            structFieldInfo.fieldType = structField.type;

            // Populate default values
            if (structField.expr != null) {
                DefaultValueAttributeInfo defaultVal = getStructFieldDefaultValue((BLangLiteral) structField.expr);
                structFieldInfo.addAttributeInfo(AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE, defaultVal);
            }

            structInfo.fieldInfoEntries.add(structFieldInfo);
            structField.symbol.varIndex = getNextIndex(structFieldInfo.fieldType.tag, fieldIndexes);
        }

        // Create variable count attribute info
        prepareIndexes(fieldIndexes);
        int[] fieldCount = new int[]{fieldIndexes.tInt, fieldIndexes.tFloat,
                fieldIndexes.tString, fieldIndexes.tBoolean, fieldIndexes.tBlob, fieldIndexes.tRef};
        addVariableCountAttributeInfo(currentPkgInfo, structInfo, fieldCount);
        fieldIndexes = new VariableIndex();
    }

    private void createEnumInfoEntry(BLangEnum enumNode) {
        BTypeSymbol enumSymbol = (BTypeSymbol) enumNode.symbol;
        // Add Enum name as an UTFCPEntry to the constant pool
        int enumNameCPIndex = addUTF8CPEntry(currentPkgInfo, enumSymbol.name.value);
        EnumInfo enumInfo = new EnumInfo(currentPackageRefCPIndex, enumNameCPIndex);
        currentPkgInfo.addEnumInfo(enumSymbol.name.value, enumInfo);
        enumInfo.enumType = (BEnumType) enumSymbol.type;
        
        for (int i = 0; i < enumNode.enumerators.size(); i++) {
            BLangEnumerator enumeratorNode = enumNode.enumerators.get(i);

            int enumeratorNameCPIndex = addUTF8CPEntry(currentPkgInfo, enumeratorNode.symbol.name.toString());
            EnumeratorInfo enumeratorInfo = new EnumeratorInfo(enumeratorNameCPIndex, i, enumInfo.enumType);
            enumInfo.enumeratorInfoList.add(enumeratorInfo);
        }
    }

    private void createFunctionInfoEntry(BLangInvokableNode invokable) {
        BInvokableSymbol funcSymbol = invokable.symbol;
        BInvokableType funcType = (BInvokableType) funcSymbol.type;

        // Add function name as an UTFCPEntry to the constant pool
        int funcNameCPIndex = this.addUTF8CPEntry(currentPkgInfo, funcSymbol.name.value);

        FunctionInfo invInfo = new FunctionInfo(currentPackageRefCPIndex, funcNameCPIndex);
        invInfo.paramTypes = funcType.paramTypes.toArray(new BType[0]);
        invInfo.retParamTypes = funcType.retTypes.toArray(new BType[0]);
        invInfo.flags = funcSymbol.flags;

        this.addWorkerInfoEntries(invInfo, invokable.getWorkers());

        invInfo.signatureCPIndex = addUTF8CPEntry(this.currentPkgInfo, generateSignature(invInfo));
        this.currentPkgInfo.functionInfoMap.put(funcSymbol.name.value, invInfo);
    }

    private void createTransformerInfoEntry(BLangInvokableNode invokable) {
        BInvokableSymbol transformerSymbol = invokable.symbol;
        BInvokableType transformerType = (BInvokableType) transformerSymbol.type;

        // Add transformer name as an UTFCPEntry to the constant pool
        int transformerNameCPIndex = this.addUTF8CPEntry(currentPkgInfo, transformerSymbol.name.value);

        TransformerInfo transformerInfo = new TransformerInfo(currentPackageRefCPIndex, transformerNameCPIndex);
        transformerInfo.paramTypes = transformerType.paramTypes.toArray(new BType[0]);
        transformerInfo.retParamTypes = transformerType.retTypes.toArray(new BType[0]);
        transformerInfo.flags = transformerSymbol.flags;

        this.addWorkerInfoEntries(transformerInfo, invokable.getWorkers());

        transformerInfo.signatureCPIndex = addUTF8CPEntry(this.currentPkgInfo, generateSignature(transformerInfo));
        this.currentPkgInfo.transformerInfoMap.put(transformerSymbol.name.value, transformerInfo);
    }

    private void addWorkerInfoEntries(CallableUnitInfo callableUnitInfo, List<BLangWorker> workers) {
        UTF8CPEntry workerNameCPEntry = new UTF8CPEntry("default");
        int workerNameCPIndex = this.currentPkgInfo.addCPEntry(workerNameCPEntry);
        WorkerInfo defaultWorkerInfo = new WorkerInfo(workerNameCPIndex, "default");
        callableUnitInfo.defaultWorkerInfo = defaultWorkerInfo;
        for (BLangWorker worker : workers) {
            workerNameCPEntry = new UTF8CPEntry(worker.name.value);
            workerNameCPIndex = currentPkgInfo.addCPEntry(workerNameCPEntry);
            WorkerInfo workerInfo = new WorkerInfo(workerNameCPIndex, worker.getName().value);
            callableUnitInfo.addWorkerInfo(worker.getName().value, workerInfo);
        }
    }

    private void createConnectorInfoEntry(BLangConnector connectorNode) {
        BConnectorType connectorType = (BConnectorType) connectorNode.symbol.type;
        // Add connector name as an UTFCPEntry to the constant pool
        int connectorNameCPIndex = addUTF8CPEntry(currentPkgInfo, connectorNode.name.value);
        //Create connector info
        ConnectorInfo connectorInfo = new ConnectorInfo(currentPackageRefCPIndex, connectorNameCPIndex);
        connectorInfo.paramTypes = connectorType.paramTypes.toArray(new BType[0]);
        connectorInfo.signatureCPIndex = addUTF8CPEntry(this.currentPkgInfo, generateSignature(connectorInfo));
        // Add connector level variables
        int localVarAttNameIndex = addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.value());
        LocalVariableAttributeInfo localVarAttributeInfo = new LocalVariableAttributeInfo(localVarAttNameIndex);
        connectorNode.params.forEach(var -> visitConnectorNodeVariable(var.symbol, localVarAttributeInfo));
        connectorNode.varDefs.forEach(var -> visitConnectorNodeVariable(var.var.symbol, localVarAttributeInfo));
        connectorInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttributeInfo);

        // Create variable count attribute info
        prepareIndexes(fieldIndexes);
        int[] fieldCount = new int[]{fieldIndexes.tInt, fieldIndexes.tFloat,
                fieldIndexes.tString, fieldIndexes.tBoolean, fieldIndexes.tBlob, fieldIndexes.tRef};
        addVariableCountAttributeInfo(currentPkgInfo, connectorInfo, fieldCount);

        // Create the init function info
        BLangFunction connectorInitFunction = (BLangFunction) connectorNode.getInitFunction();
        createFunctionInfoEntry(connectorInitFunction);
        this.currentPkgInfo.connectorInfoMap.put(connectorNode.name.value, connectorInfo);
        // Create action info entries for all actions
        connectorNode.actions.forEach(res -> createActionInfoEntry(res, connectorInfo));
        createActionInfoEntry(connectorNode.initAction, connectorInfo);
        fieldIndexes = new VariableIndex();
    }

    private void createActionInfoEntry(BLangAction actionNode, ConnectorInfo connectorInfo) {
        BInvokableSymbol actionSymbol = actionNode.symbol;
        BInvokableType actionType = (BInvokableType) actionSymbol.type;
        // Add action name as an UTFCPEntry to the constant pool
        int actionNameCPIndex = addUTF8CPEntry(currentPkgInfo, actionNode.name.value);
        ActionInfo actionInfo = new ActionInfo(currentPackageRefCPIndex, actionNameCPIndex);

        actionInfo.paramTypes = actionType.paramTypes.toArray(new BType[0]);
        actionInfo.retParamTypes = actionType.retTypes.toArray(new BType[0]);
        actionInfo.flags = actionSymbol.flags;
//        setParameterNames(actionNode, actionInfo);
        actionInfo.signatureCPIndex = addUTF8CPEntry(currentPkgInfo, generateSignature(actionInfo));
        // Add worker info
        this.addWorkerInfoEntries(actionInfo, actionNode.getWorkers());

        // Add action info to the connector info
        connectorInfo.actionInfoMap.put(actionNode.name.getValue(), actionInfo);
    }

    private void createServiceInfoEntry(BLangService serviceNode) {
        // Add service name as an UTFCPEntry to the constant pool
        int serviceNameCPIndex = addUTF8CPEntry(currentPkgInfo, serviceNode.name.value);
        //Create service info
        PackageID protocolPkgId = ((BTypeSymbol) serviceNode.symbol).protocolPkgId;
        if (protocolPkgId != null) {
            String protocolPkg = protocolPkgId.getName().value;
            int protocolPkgCPIndex = addUTF8CPEntry(currentPkgInfo, protocolPkg);
            ServiceInfo serviceInfo = new ServiceInfo(currentPackageRefCPIndex, serviceNameCPIndex, protocolPkgCPIndex);
            // Add service level variables
            int localVarAttNameIndex = addUTF8CPEntry(currentPkgInfo,
                                                      AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.value());
            LocalVariableAttributeInfo localVarAttributeInfo = new LocalVariableAttributeInfo(localVarAttNameIndex);
            serviceNode.vars.forEach(var -> visitServiceNodeVariable(var.var.symbol, localVarAttributeInfo));
            serviceInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttributeInfo);
            // Create the init function info
            BLangFunction serviceInitFunction = (BLangFunction) serviceNode.getInitFunction();
            createFunctionInfoEntry(serviceInitFunction);
            serviceInfo.initFuncInfo = currentPkgInfo.functionInfoMap.get(serviceInitFunction.name.toString());
            currentPkgInfo.addServiceInfo(serviceNode.name.value, serviceInfo);
            // Create resource info entries for all resources
            serviceNode.resources.forEach(res -> createResourceInfoEntry(res, serviceInfo));
        }
    }

    private void createResourceInfoEntry(BLangResource resourceNode, ServiceInfo serviceInfo) {
        BInvokableType resourceType = (BInvokableType) resourceNode.symbol.type;
        // Add resource name as an UTFCPEntry to the constant pool
        int serviceNameCPIndex = addUTF8CPEntry(currentPkgInfo, resourceNode.name.value);
        ResourceInfo resourceInfo = new ResourceInfo(currentPackageRefCPIndex, serviceNameCPIndex);
        resourceInfo.paramTypes = resourceType.paramTypes.toArray(new BType[0]);
        setParameterNames(resourceNode, resourceInfo);
        resourceInfo.retParamTypes = new BType[0];
        resourceInfo.signatureCPIndex = addUTF8CPEntry(currentPkgInfo, generateSignature(resourceInfo));
        // Add worker info
        int workerNameCPIndex = addUTF8CPEntry(currentPkgInfo, "default");
        resourceInfo.defaultWorkerInfo = new WorkerInfo(workerNameCPIndex, "default");
        resourceNode.workers.forEach(worker -> addWorkerInfoEntry(worker, resourceInfo));
        // Add resource info to the service info
        serviceInfo.resourceInfoMap.put(resourceNode.name.getValue(), resourceInfo);
    }

    private void addWorkerInfoEntry(BLangWorker worker, CallableUnitInfo callableUnitInfo) {
        int workerNameCPIndex = addUTF8CPEntry(currentPkgInfo, worker.name.value);
        WorkerInfo workerInfo = new WorkerInfo(workerNameCPIndex, worker.name.value);
        callableUnitInfo.addWorkerInfo(worker.name.value, workerInfo);
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

    private void addLineNumberInfo(DiagnosticPos pos) {
        LineNumberInfo lineNumInfo = createLineNumberInfo(pos, currentPkgInfo, currentPkgInfo.instructionList.size());
        lineNoAttrInfo.addLineNumberInfo(lineNumInfo);
    }

    private LineNumberInfo createLineNumberInfo(DiagnosticPos pos, PackageInfo packageInfo, int ip) {
        UTF8CPEntry fileNameUTF8CPEntry = new UTF8CPEntry(pos.src.cUnitName);
        int fileNameCPEntryIndex = packageInfo.addCPEntry(fileNameUTF8CPEntry);
        LineNumberInfo lineNumberInfo = new LineNumberInfo(pos.sLine, fileNameCPEntryIndex, pos.src.cUnitName, ip);
        lineNumberInfo.setPackageInfo(packageInfo);
        lineNumberInfo.setIp(ip);
        return lineNumberInfo;
    }

    private void setParameterNames(BLangResource resourceNode, ResourceInfo resourceInfo) {
        int paramCount = resourceNode.params.size();
        resourceInfo.paramNameCPIndexes = new int[paramCount];
        for (int i = 0; i < paramCount; i++) {
            BLangVariable paramVar = resourceNode.params.get(i);
            String paramName = null;
            boolean isAnnotated = false;
            for (BLangAnnotationAttachment annotationAttachment : paramVar.annAttachments) {
                String attachmentName = annotationAttachment.getAnnotationName().getValue();
                if ("PathParam".equalsIgnoreCase(attachmentName) || "QueryParam".equalsIgnoreCase(attachmentName)) {
                    //TODO:
                    //paramName = annotationAttachment.getAttributeNameValuePairs().get("value")
                    // .getLiteralValue().stringValue();
                    isAnnotated = true;
                    break;
                }
            }
            if (!isAnnotated) {
                paramName = paramVar.name.getValue();
            }
            int paramNameCPIndex = addUTF8CPEntry(currentPkgInfo, paramName);
            resourceInfo.paramNameCPIndexes[i] = paramNameCPIndex;
        }
    }

    private WorkerDataChannelInfo getWorkerDataChannelInfo(CallableUnitInfo callableUnit,
                                                           String source, String target) {
        WorkerDataChannelInfo workerDataChannelInfo = callableUnit.getWorkerDataChannelInfo(
                WorkerDataChannelInfo.generateChannelName(source, target));
        if (workerDataChannelInfo == null) {
            UTF8CPEntry sourceCPEntry = new UTF8CPEntry(source);
            int sourceCPIndex = this.currentPkgInfo.addCPEntry(sourceCPEntry);
            UTF8CPEntry targetCPEntry = new UTF8CPEntry(target);
            int targetCPIndex = this.currentPkgInfo.addCPEntry(targetCPEntry);
            workerDataChannelInfo = new WorkerDataChannelInfo(sourceCPIndex, source, targetCPIndex, target);
            workerDataChannelInfo.setUniqueName(workerDataChannelInfo.getChannelName() + this.workerChannelCount);
            String uniqueName = workerDataChannelInfo.getUniqueName();
            UTF8CPEntry uniqueNameCPEntry = new UTF8CPEntry(uniqueName);
            int uniqueNameCPIndex = this.currentPkgInfo.addCPEntry(uniqueNameCPEntry);
            workerDataChannelInfo.setUniqueNameCPIndex(uniqueNameCPIndex);
            callableUnit.addWorkerDataChannelInfo(workerDataChannelInfo);
            this.workerChannelCount++;
        }
        return workerDataChannelInfo;
    }

    // Constant pool related utility classes

    private int addUTF8CPEntry(ConstantPool pool, String value) {
        UTF8CPEntry pkgPathCPEntry = new UTF8CPEntry(value);
        return pool.addCPEntry(pkgPathCPEntry);
    }

    private int addPackageRefCPEntry(ConstantPool pool, PackageID pkgID) {
        int nameCPIndex = addUTF8CPEntry(pool, pkgID.name.value);
        int versionCPIndex = addUTF8CPEntry(pool, pkgID.version.value);
        PackageRefCPEntry packageRefCPEntry = new PackageRefCPEntry(nameCPIndex, versionCPIndex);
        return pool.addCPEntry(packageRefCPEntry);
    }

    /**
     * Holds the variable index per type.
     *
     * @since 0.94
     */
    private static class VariableIndex {
        int tInt = -1;
        int tFloat = -1;
        int tString = -1;
        int tBoolean = -1;
        int tBlob = -1;
        int tRef = -1;

        public int[] toArray() {
            int[] result = new int[6];
            result[0] = this.tInt;
            result[1] = this.tFloat;
            result[2] = this.tString;
            result[3] = this.tBoolean;
            result[4] = this.tBlob;
            result[5] = this.tRef;
            return result;
        }

    }

    /**
     * Bean class which keep both opcode and the current variable index.
     *
     * @since 0.94
     */
    public static class OpcodeAndIndex {
        int opcode;
        int index;

        public OpcodeAndIndex(int opcode, int index) {
            this.opcode = opcode;
            this.index = index;
        }
    }

    public void visit(BLangWorker workerNode) {
        this.genNode(workerNode.body, this.env);
    }

    /* visit the workers within fork-join block */
    private void processJoinWorkers(BLangForkJoin forkJoin, ForkjoinInfo forkjoinInfo, VariableIndex lvIndexesCopy,
                                    SymbolEnv forkJoinEnv) {
        UTF8CPEntry codeUTF8CPEntry = new UTF8CPEntry(AttributeInfo.Kind.CODE_ATTRIBUTE.toString());
        int codeAttribNameIndex = this.currentPkgInfo.addCPEntry(codeUTF8CPEntry);
        for (BLangWorker worker : forkJoin.workers) {
            WorkerInfo workerInfo = forkjoinInfo.getWorkerInfo(worker.name.value);
            workerInfo.codeAttributeInfo.attributeNameIndex = codeAttribNameIndex;
            workerInfo.codeAttributeInfo.codeAddrs = this.nextIP();
            this.currentWorkerInfo = workerInfo;
            this.lvIndexes = this.copyVarIndex(lvIndexesCopy);
            this.genNode(worker.body, forkJoinEnv);
            this.endWorkerInfoUnit(workerInfo.codeAttributeInfo);
            this.emit(InstructionCodes.HALT);
        }
    }

    private ForkjoinInfo processForkJoinTimeout(BLangForkJoin forkJoin) {
        BLangExpression argExpr = forkJoin.timeoutExpression;
        int[] retRegs;
        if (argExpr != null) {
            retRegs = new int[1];
            this.genNode(argExpr, this.env);
            retRegs[0] = argExpr.regIndex;
        } else {
            retRegs = new int[0];
        }
        VariableIndex argRegs = this.lvIndexes;
        ForkjoinInfo forkjoinInfo = new ForkjoinInfo(argRegs.toArray(), retRegs);
        if (argExpr != null) {
            forkjoinInfo.setTimeoutAvailable(true);
        }
        return forkjoinInfo;
    }

    private void populatForkJoinWorkerInfo(BLangForkJoin forkJoin, ForkjoinInfo forkjoinInfo) {
        for (BLangWorker worker : forkJoin.workers) {
            UTF8CPEntry workerNameCPEntry = new UTF8CPEntry(worker.name.value);
            int workerNameCPIndex = this.currentPkgInfo.addCPEntry(workerNameCPEntry);
            WorkerInfo workerInfo = new WorkerInfo(workerNameCPIndex, worker.name.value);
            forkjoinInfo.addWorkerInfo(worker.name.value, workerInfo);
        }
    }

    /* generate code for Join block */
    private void processJoinBlock(BLangForkJoin forkJoin, ForkjoinInfo forkjoinInfo, SymbolEnv forkJoinEnv) {
        UTF8CPEntry joinType = new UTF8CPEntry(forkJoin.joinType.name());
        int joinTypeCPIndex = this.currentPkgInfo.addCPEntry(joinType);
        forkjoinInfo.setJoinType(forkJoin.joinType.name());
        forkjoinInfo.setJoinTypeCPIndex(joinTypeCPIndex);
        forkjoinInfo.setJoinIp(nextIP());
        if (forkJoin.joinResultVar != null) {
            visitForkJoinParameterDefs(forkJoin.joinResultVar, forkJoinEnv);
        }
        int joinMemOffset = forkJoin.joinResultVar.symbol.varIndex;
        forkjoinInfo.setJoinMemOffset(joinMemOffset);
        if (forkJoin.joinedBody != null) {
            this.genNode(forkJoin.joinedBody, forkJoinEnv);
        }
    }

    /* generate code for timeout block */
    private void processTimeoutBlock(BLangForkJoin forkJoin, ForkjoinInfo forkjoinInfo, SymbolEnv forkJoinEnv) {
        /* emit a GOTO instruction to jump out of the timeout block */
        Instruction gotoInstruction = InstructionFactory.get(InstructionCodes.GOTO, -1);
        this.emit(gotoInstruction);
        forkjoinInfo.setTimeoutIp(nextIP());
        if (forkJoin.timeoutExpression != null) {
            this.genNode(forkJoin.timeoutExpression, forkJoinEnv);
        }
        if (forkJoin.timeoutVariable != null) {
            visitForkJoinParameterDefs(forkJoin.timeoutVariable, forkJoinEnv);
        }
        int timeoutMemOffset = forkJoin.joinResultVar.symbol.varIndex;
        forkjoinInfo.setTimeoutMemOffset(timeoutMemOffset);
        if (forkJoin.timeoutBody != null) {
            this.genNode(forkJoin.timeoutBody, forkJoinEnv);
        }
        gotoInstruction.setOperand(0, nextIP());
    }

    public void visit(BLangForkJoin forkJoin) {
        SymbolEnv forkJoinEnv = SymbolEnv.createForkJoinSymbolEnv(forkJoin, this.env);
        ForkjoinInfo forkjoinInfo = this.processForkJoinTimeout(forkJoin);
        this.populatForkJoinWorkerInfo(forkJoin, forkjoinInfo);
        int forkJoinInfoIndex = this.forkJoinCount++;
        /* was I already inside a fork/join */
        if (this.env.forkJoin != null) {
            this.currentWorkerInfo.addForkJoinInfo(forkjoinInfo);
        } else {
            this.currentCallableUnitInfo.defaultWorkerInfo.addForkJoinInfo(forkjoinInfo);
        }
        ForkJoinCPEntry forkJoinIndexCPEntry = new ForkJoinCPEntry(forkJoinInfoIndex);
        int forkJoinInfoIndexCPEntryIndex = this.currentPkgInfo.addCPEntry(forkJoinIndexCPEntry);

        forkjoinInfo.setIndexCPIndex(forkJoinInfoIndexCPEntryIndex);
        this.emit(InstructionCodes.FORKJOIN, forkJoinInfoIndexCPEntryIndex);
        VariableIndex lvIndexesCopy = this.copyVarIndex(this.lvIndexes);
        VariableIndex regIndexesCopy = this.copyVarIndex(this.regIndexes);
        VariableIndex maxRegIndexesCopy = this.copyVarIndex(this.maxRegIndexes);
        this.processJoinWorkers(forkJoin, forkjoinInfo, lvIndexesCopy, forkJoinEnv);
        this.lvIndexes = lvIndexesCopy;
        this.regIndexes = regIndexesCopy;
        this.maxRegIndexes = maxRegIndexesCopy;
        int i = 0;
        int[] joinWrkrNameCPIndexes = new int[forkJoin.joinedWorkers.size()];
        String[] joinWrkrNames = new String[joinWrkrNameCPIndexes.length];
        for (BLangIdentifier workerName : forkJoin.joinedWorkers) {
            UTF8CPEntry workerNameCPEntry = new UTF8CPEntry(workerName.value);
            int workerNameCPIndex = this.currentPkgInfo.addCPEntry(workerNameCPEntry);
            joinWrkrNameCPIndexes[i] = workerNameCPIndex;
            joinWrkrNames[i] = workerName.value;
            i++;
        }
        forkjoinInfo.setJoinWrkrNameIndexes(joinWrkrNameCPIndexes);
        forkjoinInfo.setJoinWorkerNames(joinWrkrNames);
        forkjoinInfo.setWorkerCount(forkJoin.joinedWorkerCount);
        this.processJoinBlock(forkJoin, forkjoinInfo, forkJoinEnv);
        this.processTimeoutBlock(forkJoin, forkjoinInfo, forkJoinEnv);
    }

    private void visitForkJoinParameterDefs(BLangVariable parameterDef, SymbolEnv forkJoinEnv) {
        LocalVariableAttributeInfo localVariableAttributeInfo = new LocalVariableAttributeInfo(1);
        int lvIndex = this.getNextIndex(parameterDef.type.tag, this.lvIndexes);
        parameterDef.symbol.varIndex = lvIndex;
        this.genNode(parameterDef, forkJoinEnv);
        LocalVariableInfo localVariableDetails = this.getLocalVarAttributeInfo(parameterDef.symbol);
        localVariableAttributeInfo.localVars.add(localVariableDetails);
    }

    public void visit(BLangWorkerSend workerSendNode) {
        WorkerDataChannelInfo workerDataChannelInfo = this.getWorkerDataChannelInfo(this.currentCallableUnitInfo,
                this.currentWorkerInfo.getWorkerName(), workerSendNode.workerIdentifier.value);
        WorkerDataChannelRefCPEntry wrkrInvRefCPEntry = new WorkerDataChannelRefCPEntry(workerDataChannelInfo
                .getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());
        wrkrInvRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        int wrkrInvRefCPIndex = currentPkgInfo.addCPEntry(wrkrInvRefCPEntry);
        if (workerSendNode.isForkJoinSend) {
            this.currentWorkerInfo.setWrkrDtChnlRefCPIndex(wrkrInvRefCPIndex);
            this.currentWorkerInfo.setWorkerDataChannelInfoForForkJoin(workerDataChannelInfo);
        }
        workerDataChannelInfo.setDataChannelRefIndex(wrkrInvRefCPIndex);
        int workerInvocationIndex = this.getWorkerSendCPIndex(workerSendNode);
        this.emit(InstructionCodes.WRKINVOKE, wrkrInvRefCPIndex, workerInvocationIndex);
    }

    private void genNodeList(List<BLangExpression> exprs, SymbolEnv env) {
        exprs.forEach(e -> this.genNode(e, env));
    }

    private int[] extractsRegisters(List<BLangExpression> exprs) {
        int[] regs = new int[exprs.size()];
        for (int i = 0; i < regs.length; i++) {
            regs[i] = exprs.get(i).regIndex;
        }
        return regs;
    }

    private BType[] extractTypes(List<BLangExpression> exprs) {
        return exprs.stream().map(e -> e.type).collect(Collectors.toList()).toArray(new BType[0]);
    }

    private String generateSig(BType[] types) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(types).forEach(e -> builder.append(e.getDesc()));
        return builder.toString();
    }

    private int getWorkerSendCPIndex(BLangWorkerSend workerSendStmt) {
        List<BLangExpression> argExprs = workerSendStmt.exprs;
        this.genNodeList(argExprs, this.env);
        int[] argRegs = this.extractsRegisters(argExprs);
        BType[] bTypes = this.extractTypes(argExprs);
        WrkrInteractionArgsCPEntry workerInvokeCPEntry = new WrkrInteractionArgsCPEntry(argRegs, bTypes);
        UTF8CPEntry sigCPEntry = new UTF8CPEntry(this.generateSig(bTypes));
        int sigCPIndex = this.currentPkgInfo.addCPEntry(sigCPEntry);
        workerInvokeCPEntry.setTypesSignatureCPIndex(sigCPIndex);
        return this.currentPkgInfo.addCPEntry(workerInvokeCPEntry);
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        WorkerDataChannelInfo workerDataChannelInfo = this.getWorkerDataChannelInfo(this.currentCallableUnitInfo,
                workerReceiveNode.workerIdentifier.value, this.currentWorkerInfo.getWorkerName());
        WorkerDataChannelRefCPEntry wrkrChnlRefCPEntry = new WorkerDataChannelRefCPEntry(workerDataChannelInfo
                .getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());
        wrkrChnlRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        int wrkrRplyRefCPIndex = currentPkgInfo.addCPEntry(wrkrChnlRefCPEntry);
        workerDataChannelInfo.setDataChannelRefIndex(wrkrRplyRefCPIndex);
        int workerReplyIndex = getWorkerReplyCPIndex(workerReceiveNode);
        WrkrInteractionArgsCPEntry wrkrRplyCPEntry = (WrkrInteractionArgsCPEntry) this.currentPkgInfo.getCPEntry(
                workerReplyIndex);
        emit(InstructionCodes.WRKREPLY, wrkrRplyRefCPIndex, workerReplyIndex);
        /* generate store instructions to store the values */
        int[] rhsExprRegIndexes = wrkrRplyCPEntry.getArgRegs();
        List<BLangExpression> lhsExprs = workerReceiveNode.exprs;
        for (int i = 0; i < lhsExprs.size(); i++) {
            this.rhsExprRegIndex = rhsExprRegIndexes[i];
            this.varAssignment = true;
            this.genNode(lhsExprs.get(i), this.env);
            this.varAssignment = false;
        }
    }

    private int getWorkerReplyCPIndex(BLangWorkerReceive workerReplyStmt) {
        BType[] retTypes = this.extractTypes(workerReplyStmt.exprs);
        int[] argRegs = new int[retTypes.length];
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = retTypes[i];
            argRegs[i] = getNextIndex(retType.tag, this.regIndexes);
        }
        WrkrInteractionArgsCPEntry wrkrRplyCPEntry = new WrkrInteractionArgsCPEntry(argRegs, retTypes);
        UTF8CPEntry sigCPEntry = new UTF8CPEntry(this.generateSig(retTypes));
        int sigCPIndex = currentPkgInfo.addCPEntry(sigCPEntry);
        wrkrRplyCPEntry.setTypesSignatureCPIndex(sigCPIndex);
        return currentPkgInfo.addCPEntry(wrkrRplyCPEntry);
    }

    public void visit(BLangConnector connectorNode) {
        BLangFunction initFunction = (BLangFunction) connectorNode.getInitFunction();
        visit(initFunction);

        currentConnectorInfo = currentPkgInfo.getConnectorInfo(connectorNode.getName().getValue());

        int annotationAttribNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.value());
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);
        connectorNode.annAttachments.forEach(annt -> visitAnnotationAttachment(annt, attributeInfo));
        currentConnectorInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, attributeInfo);

        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorNode.symbol.scope, this.env);
        connectorNode.actions.forEach(action -> genNode(action, connectorEnv));
        genNode(connectorNode.initAction, connectorEnv);
    }

    public void visit(BLangAction actionNode) {
        ActionInfo actionInfo = currentConnectorInfo.actionInfoMap.get(actionNode.name.getValue());
        currentCallableUnitInfo = actionInfo;

        int annotationAttribNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.value());
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);
        actionNode.annAttachments.forEach(annt -> visitAnnotationAttachment(annt, attributeInfo));
        currentCallableUnitInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, attributeInfo);

        SymbolEnv actionEnv = SymbolEnv
                .createResourceActionSymbolEnv(actionNode, actionNode.symbol.scope, this.env);
        visitInvokableNode(actionNode, currentCallableUnitInfo, actionEnv);
    }

    public void visit(BLangStruct structNode) {
        StructInfo structInfo = currentPkgInfo.getStructInfo(structNode.getName().getValue());

        int annotationAttribNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.value());
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);
        structNode.annAttachments.forEach(annt -> visitAnnotationAttachment(annt, attributeInfo));
        structInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, attributeInfo);
    }

    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    public void visit(BLangAnnotation annotationNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        /* ignore */
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        /* ignore */
    }

    public void visit(BLangAssignment assignNode) {
        if (assignNode.declaredWithVar) {
            assignNode.varRefs.stream()
                    .filter(v -> v.type.tag != TypeTags.NONE)
                    .forEach(v -> {
                        BLangVariableReference varRef = (BLangVariableReference) v;
                        varRef.symbol.varIndex = getNextIndex(v.type.tag, lvIndexes);
                        LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(varRef.symbol);
                        localVarAttrInfo.localVars.add(localVarInfo);
                    });
        }
        genNode(assignNode.expr, this.env);
        int[] rhsExprRegIndexes;
        if (assignNode.expr.isMultiReturnExpr()) {
            rhsExprRegIndexes = ((MultiReturnExpr) assignNode.expr).getRegIndexes();
        } else {
            rhsExprRegIndexes = new int[]{assignNode.expr.regIndex};
        }
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression lExpr = assignNode.varRefs.get(i);
            if (lExpr.type.tag == TypeTags.NONE) {
                continue;
            }
            rhsExprRegIndex = rhsExprRegIndexes[i];
            varAssignment = true;
            genNode(lExpr, this.env);
            varAssignment = false;
        }
    }

    public void visit(BLangNext continueNode) {
        generateFinallyInstructions(continueNode, NodeKind.WHILE);
        this.emit(this.loopResetInstructionStack.peek());
    }

    public void visit(BLangBreak breakNode) {
        generateFinallyInstructions(breakNode, NodeKind.WHILE);
        this.emit(this.loopExitInstructionStack.peek());
    }

    public void visit(BLangThrow throwNode) {
        genNode(throwNode.expr, env);
        emit(InstructionFactory.get(InstructionCodes.THROW, throwNode.expr.regIndex));
    }

    public void visit(BLangComment commentNode) {
        /* ignore */
    }

    public void visit(BLangIf ifNode) {
        addLineNumberInfo(ifNode.pos);
        this.genNode(ifNode.expr, this.env);
        Instruction ifCondJumpInstr = InstructionFactory.get(InstructionCodes.BR_FALSE, ifNode.expr.regIndex, -1);
        this.emit(ifCondJumpInstr);
        this.genNode(ifNode.body, this.env);
        Instruction endJumpInstr = InstructionFactory.get(InstructionCodes.GOTO, -1);
        this.emit(endJumpInstr);
        ifCondJumpInstr.setOperand(1, this.nextIP());
        if (ifNode.elseStmt != null) {
            this.genNode(ifNode.elseStmt, this.env);
        }
        endJumpInstr.setOperand(0, this.nextIP());
    }

    public void visit(BLangWhile whileNode) {
        Instruction gotoTopJumpInstr = InstructionFactory.get(InstructionCodes.GOTO, this.nextIP());
        this.genNode(whileNode.expr, this.env);
        Instruction whileCondJumpInstr = InstructionFactory.get(InstructionCodes.BR_FALSE,
                whileNode.expr.regIndex, -1);
        Instruction exitLoopJumpInstr = InstructionFactory.get(InstructionCodes.GOTO, -1);
        this.emit(whileCondJumpInstr);
        this.loopResetInstructionStack.push(gotoTopJumpInstr);
        this.loopExitInstructionStack.push(exitLoopJumpInstr);
        this.genNode(whileNode.body, this.env);
        this.loopResetInstructionStack.pop();
        this.loopExitInstructionStack.pop();
        this.emit(gotoTopJumpInstr);
        int endIP = this.nextIP();
        whileCondJumpInstr.setOperand(1, endIP);
        exitLoopJumpInstr.setOperand(0, endIP);
    }

    public void visit(BLangTransaction transactionNode) {
        ++transactionIndex;
        int retryCountAvailable = 0;
        if (transactionNode.retryCount != null) {
            this.genNode(transactionNode.retryCount, this.env);
            retryCountAvailable = 1;
        }

        ErrorTableAttributeInfo errorTable = createErrorTableIfAbsent(currentPkgInfo);
        Instruction gotoEndOfTransactionBlock = InstructionFactory.get(InstructionCodes.GOTO, -1);
        Instruction gotoStartOfAbortedBlock = InstructionFactory.get(InstructionCodes.GOTO, -1);
        abortInstructions.push(gotoStartOfAbortedBlock);

        //start transaction
        this.emit(InstructionFactory.get(InstructionCodes.TR_BEGIN, transactionIndex, retryCountAvailable));
        int startIP = nextIP();
        Instruction gotoInstruction = InstructionFactory.get(InstructionCodes.GOTO, startIP);

        //retry transaction;
        Instruction retryInstruction = InstructionFactory.get(InstructionCodes.TR_RETRY, transactionIndex, -1);
        this.emit(retryInstruction);

        //process transaction statements
        this.genNode(transactionNode.transactionBody, this.env);

        //end the transaction
        int endIP = nextIP();
        this.emit(InstructionFactory.get(InstructionCodes.TR_END, 0));

        //process committed block
        if (transactionNode.committedBody != null) {
            this.genNode(transactionNode.committedBody, this.env);
        }
        if (transactionNode.abortedBody != null) {
            this.emit(gotoEndOfTransactionBlock);
        }
        abortInstructions.pop();
        int startOfAbortedIP = nextIP();
        gotoStartOfAbortedBlock.setOperand(0, startOfAbortedIP);
        emit(InstructionFactory.get(InstructionCodes.TR_END, -1));

        //process aborted block
        if (transactionNode.abortedBody != null) {
            this.genNode(transactionNode.abortedBody, this.env);
        }
        emit(gotoEndOfTransactionBlock);

        // CodeGen for error handling.
        int errorTargetIP = nextIP();
        emit(InstructionFactory.get(InstructionCodes.TR_END, -1));
        if (transactionNode.failedBody != null) {
            this.genNode(transactionNode.failedBody, this.env);

        }
        emit(gotoInstruction);
        int ifIP = nextIP();
        retryInstruction.setOperand(1, ifIP);
        if (transactionNode.abortedBody != null) {
            this.genNode(transactionNode.abortedBody, this.env);
        }


        emit(InstructionFactory.get(InstructionCodes.THROW, -1));
        gotoEndOfTransactionBlock.setOperand(0, nextIP());

        ErrorTableEntry errorTableEntry = new ErrorTableEntry(startIP, endIP, errorTargetIP, 0, -1);
        errorTable.addErrorTableEntry(errorTableEntry);
        emit(InstructionFactory.get(InstructionCodes.TR_END, 1));
    }

    public void visit(BLangAbort abortNode) {
        generateFinallyInstructions(abortNode, NodeKind.TRANSACTION);
        this.emit(abortInstructions.peek());
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangLocalXMLNS xmlnsNode) {
        BLangExpression nsURIExpr = xmlnsNode.namespaceURI;
        int lvIndex = ++lvIndexes.tString;
        BXMLNSSymbol nsSymbol = (BXMLNSSymbol) xmlnsNode.symbol;
        genNode(nsURIExpr, env);
        nsSymbol.nsURIIndex = lvIndex;
        emit(InstructionCodes.SSTORE, nsURIExpr.regIndex, lvIndex);
    }

    @Override
    public void visit(BLangPackageXMLNS xmlnsNode) {
        BLangExpression nsURIExpr = xmlnsNode.namespaceURI;
        int pvIndex = ++pvIndexes.tString;
        BXMLNSSymbol nsSymbol = (BXMLNSSymbol) xmlnsNode.symbol;
        genNode(nsURIExpr, env);
        nsSymbol.nsURIIndex = pvIndex;
        emit(InstructionCodes.SGSTORE, nsURIExpr.regIndex, pvIndex);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        // If the QName is use outside of XML, treat it as string.
        if (!xmlQName.isUsedInXML) {
            String qName = xmlQName.namespaceURI == null ? xmlQName.localname.value
                    : ("{" + xmlQName.namespaceURI + "}" + xmlQName.localname);
            xmlQName.regIndex = createStringLiteral(qName, env);
            return;
        }

        // Else, treat it as QName
        int nsURIIndex = getNamespaceURIIndex(xmlQName.nsSymbol, env);
        int localnameIndex = createStringLiteral(xmlQName.localname.value, env);
        int prefixIndex = createStringLiteral(xmlQName.prefix.value, env);
        xmlQName.regIndex = ++regIndexes.tRef;
        emit(InstructionCodes.NEWQNAME, localnameIndex, nsURIIndex, prefixIndex, xmlQName.regIndex);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(xmlAttribute, env);
        BLangExpression attrNameExpr = (BLangExpression) xmlAttribute.name;
        genNode(attrNameExpr, xmlAttributeEnv);
        int attrQNameRegIndex = attrNameExpr.regIndex;

        // If the attribute name is a string representation of qname
        if (attrNameExpr.getKind() != NodeKind.XML_QNAME) {
            int localNameRegIndex = ++regIndexes.tString;
            int uriRegIndex = ++regIndexes.tString;
            emit(InstructionCodes.S2QNAME, attrQNameRegIndex, localNameRegIndex, uriRegIndex);

            attrQNameRegIndex = ++regIndexes.tRef;
            generateURILookupInstructions(((BLangXMLElementLiteral) env.node).namespacesInScope, localNameRegIndex,
                    uriRegIndex, attrQNameRegIndex, xmlAttribute.pos, xmlAttributeEnv);
            attrNameExpr.regIndex = attrQNameRegIndex;
        }

        BLangExpression attrValueExpr = (BLangExpression) xmlAttribute.value;
        genNode(attrValueExpr, env);

        if (xmlAttribute.isNamespaceDeclr) {
            ((BXMLNSSymbol) xmlAttribute.symbol).nsURIIndex = attrValueExpr.regIndex;
        }
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(xmlElementLiteral, env);
        xmlElementLiteral.regIndex = ++regIndexes.tRef;

        // Visit in-line namespace declarations. These needs to be visited first before visiting the 
        // attributes, start and end tag names of the element.
        xmlElementLiteral.inlineNamespaces.forEach(xmlns -> {
            genNode(xmlns, xmlElementEnv);
        });

        BLangExpression startTagName = (BLangExpression) xmlElementLiteral.getStartTagName();
        genNode(startTagName, xmlElementEnv);
        int startTagNameRegIndex = startTagName.regIndex;

        // If this is a string representation of element name, generate the namespace lookup instructions
        if (startTagName.getKind() != NodeKind.XML_QNAME) {
            int localNameRegIndex = ++regIndexes.tString;
            int uriRegIndex = ++regIndexes.tString;
            emit(InstructionCodes.S2QNAME, startTagNameRegIndex, localNameRegIndex, uriRegIndex);

            startTagNameRegIndex = ++regIndexes.tRef;
            generateURILookupInstructions(xmlElementLiteral.namespacesInScope, localNameRegIndex, uriRegIndex,
                    startTagNameRegIndex, xmlElementLiteral.pos, xmlElementEnv);
            startTagName.regIndex = startTagNameRegIndex;
        }

        // TODO: do we need to generate end tag name?
        int endTagNameRegIndex = startTagNameRegIndex;

        // Create an XML with the given QName
        int defaultNsURIIndex = getNamespaceURIIndex(xmlElementLiteral.defaultNsSymbol, xmlElementEnv);
        emit(InstructionCodes.NEWXMLELEMENT, xmlElementLiteral.regIndex, startTagNameRegIndex, endTagNameRegIndex,
                defaultNsURIIndex);

        // Add namespaces decelerations visible to this element.
        xmlElementLiteral.namespacesInScope.forEach((name, symbol) -> {
            BLangXMLQName nsQName = new BLangXMLQName(name.getValue(), XMLConstants.XMLNS_ATTRIBUTE);
            genNode(nsQName, xmlElementEnv);
            int uriIndex = getNamespaceURIIndex(symbol, xmlElementEnv);
            emit(InstructionCodes.XMLATTRSTORE, xmlElementLiteral.regIndex, nsQName.regIndex, uriIndex);
        });

        // Add attributes
        xmlElementLiteral.attributes.forEach(attribute -> {
            genNode(attribute, xmlElementEnv);
            emit(InstructionCodes.XMLATTRSTORE, xmlElementLiteral.regIndex, attribute.name.regIndex,
                    attribute.value.regIndex);
        });

        // Add children
        xmlElementLiteral.modifiedChildren.forEach(child -> {
            genNode(child, xmlElementEnv);
            emit(InstructionCodes.XMLSTORE, xmlElementLiteral.regIndex, child.regIndex);
        });
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.regIndex = ++regIndexes.tRef;
        genNode(xmlTextLiteral.concatExpr, env);
        emit(InstructionCodes.NEWXMLTEXT, xmlTextLiteral.regIndex, xmlTextLiteral.concatExpr.regIndex);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.regIndex = ++regIndexes.tRef;
        genNode(xmlCommentLiteral.concatExpr, env);
        emit(InstructionCodes.NEWXMLCOMMENT, xmlCommentLiteral.regIndex, xmlCommentLiteral.concatExpr.regIndex);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.regIndex = ++regIndexes.tRef;
        genNode(xmlProcInsLiteral.dataConcatExpr, env);
        genNode(xmlProcInsLiteral.target, env);
        emit(InstructionCodes.NEWXMLPI, xmlProcInsLiteral.regIndex, xmlProcInsLiteral.target.regIndex,
                xmlProcInsLiteral.dataConcatExpr.regIndex);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        genNode(xmlQuotedString.concatExpr, env);
        xmlQuotedString.regIndex = xmlQuotedString.concatExpr.regIndex;
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        genNode(stringTemplateLiteral.concatExpr, env);
        stringTemplateLiteral.regIndex = stringTemplateLiteral.concatExpr.regIndex;
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(xmlAttributeAccessExpr.expr, this.env);
        int varRefRegIndex = xmlAttributeAccessExpr.expr.regIndex;

        if (xmlAttributeAccessExpr.indexExpr == null) {
            int xmlValueRegIndex = ++regIndexes.tRef;
            emit(InstructionCodes.XML2XMLATTRS, varRefRegIndex, xmlValueRegIndex);
            xmlAttributeAccessExpr.regIndex = xmlValueRegIndex;
            return;
        }

        BLangExpression indexExpr = xmlAttributeAccessExpr.indexExpr;
        genNode(xmlAttributeAccessExpr.indexExpr, this.env);
        int qnameRegIndex = xmlAttributeAccessExpr.indexExpr.regIndex;

        // If this is a string representation of qname
        if (indexExpr.getKind() != NodeKind.XML_QNAME) {
            int localNameRegIndex = ++regIndexes.tString;
            int uriRegIndex = ++regIndexes.tString;
            emit(InstructionCodes.S2QNAME, qnameRegIndex, localNameRegIndex, uriRegIndex);

            qnameRegIndex = ++regIndexes.tRef;
            generateURILookupInstructions(xmlAttributeAccessExpr.namespaces, localNameRegIndex,
                    uriRegIndex, qnameRegIndex, indexExpr.pos, env);
        }

        if (variableStore) {
            emit(InstructionCodes.XMLATTRSTORE, varRefRegIndex, qnameRegIndex, rhsExprRegIndex);
        } else {
            int xmlValueRegIndex = ++regIndexes.tRef;
            emit(InstructionCodes.XMLATTRLOAD, varRefRegIndex, qnameRegIndex, xmlValueRegIndex);
            xmlAttributeAccessExpr.regIndex = xmlValueRegIndex;
        }
    }

    public void visit(BLangRetry retryNode) {
        /* ignore */
    }

    public void visit(BLangTryCatchFinally tryNode) {
        Instruction instructGotoTryCatchEnd = InstructionFactory.get(InstructionCodes.GOTO, -1);
        List<int[]> unhandledErrorRangeList = new ArrayList<>();
        ErrorTableAttributeInfo errorTable = createErrorTableIfAbsent(currentPkgInfo);

        // Handle try block.
        int fromIP = nextIP();
        genNode(tryNode.tryBody, env);
        int toIP = nextIP() - 1;

        // Append finally block instructions.
        if (tryNode.finallyBody != null) {
            genNode(tryNode.finallyBody, env);
        }
        emit(instructGotoTryCatchEnd);
        unhandledErrorRangeList.add(new int[]{fromIP, toIP});
        // Handle catch blocks.
        int order = 0;
        for (BLangCatch bLangCatch : tryNode.getCatchBlocks()) {
            addLineNumberInfo(bLangCatch.pos);
            int targetIP = nextIP();
            genNode(bLangCatch, env);
            unhandledErrorRangeList.add(new int[]{targetIP, nextIP() - 1});
            // Append finally block instructions.
            if (tryNode.finallyBody != null) {
                genNode(tryNode.finallyBody, env);
            }
            emit(instructGotoTryCatchEnd);
            // Create Error table entry for this catch block
            BTypeSymbol structSymbol = bLangCatch.param.symbol.type.tsymbol;
            BPackageSymbol packageSymbol = (BPackageSymbol) bLangCatch.param.symbol.type.tsymbol.owner;
            int pkgCPIndex = addPackageRefCPEntry(currentPkgInfo, packageSymbol.pkgID);
            int structNameCPIndex = addUTF8CPEntry(currentPkgInfo, structSymbol.name.value);
            StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, structNameCPIndex);
            int structCPEntryIndex = currentPkgInfo.addCPEntry(structureRefCPEntry);
            StructInfo errorStructInfo = this.programFile.getPackageInfo(packageSymbol.name.value)
                    .getStructInfo(structSymbol.name.value);
            ErrorTableEntry errorTableEntry = new ErrorTableEntry(fromIP, toIP, targetIP, order++, structCPEntryIndex);
            errorTableEntry.setError(errorStructInfo);
            errorTable.addErrorTableEntry(errorTableEntry);
        }

        if (tryNode.finallyBody != null) {
            // Create Error table entry for unhandled errors in try and catch(s) blocks
            for (int[] range : unhandledErrorRangeList) {
                ErrorTableEntry errorTableEntry = new ErrorTableEntry(range[0], range[1], nextIP(), order++, -1);
                errorTable.addErrorTableEntry(errorTableEntry);
            }
            // Append finally block instruction.
            genNode(tryNode.finallyBody, env);
            emit(InstructionFactory.get(InstructionCodes.THROW, -1));
        }
        instructGotoTryCatchEnd.setOperand(0, nextIP());
    }

    public void visit(BLangCatch bLangCatch) {
        // Define local variable index for Error.
        BLangVariable variable = bLangCatch.param;
        int lvIndex = ++lvIndexes.tRef;
        variable.symbol.varIndex = lvIndex;
        emit(InstructionFactory.get(InstructionCodes.ERRSTORE, lvIndex));

        // Visit Catch Block.
        genNode(bLangCatch.body, env);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        genNode(exprStmtNode.expr, this.env);
    }


    // private helper methods of visitors.

    private void visitFunctionPointerLoad(BLangExpression fpExpr, BInvokableSymbol funcSymbol) {
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, funcSymbol.pkgID);
        int funcNameCPIndex = addUTF8CPEntry(currentPkgInfo, funcSymbol.name.value);
        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgRefCPIndex, funcNameCPIndex);

        int funcRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);
        int nextIndex = getNextIndex(TypeTags.INVOKABLE, regIndexes);
        fpExpr.regIndex = nextIndex;
        emit(InstructionCodes.FPLOAD, funcRefCPIndex, nextIndex);
    }

    private void generateFinallyInstructions(BLangStatement statement) {
        generateFinallyInstructions(statement, null);
    }

    private void generateFinallyInstructions(BLangStatement statement, NodeKind targetStatementKind) {
        BLangStatement current = statement;
        while (current != null && current.statementLink.parent != null) {
            BLangStatement parent = current.statementLink.parent.statement;
            if (targetStatementKind != null && targetStatementKind == parent.getKind()) {
                return;
            }
            if (NodeKind.TRY == parent.getKind()) {
                BLangTryCatchFinally tryCatchFinally = (BLangTryCatchFinally) parent;
                final BLangStatement body = current;
                if (tryCatchFinally.finallyBody != null && (current == tryCatchFinally.tryBody
                        || tryCatchFinally.catchBlocks.stream().anyMatch(c -> c.body == body))) {
                    genNode(tryCatchFinally.finallyBody, env);
                }
            }
            current = parent;
        }
    }

    private int getNamespaceURIIndex(BXMLNSSymbol namespaceSymbol, SymbolEnv env) {
        if (namespaceSymbol == null && env.node.getKind() == NodeKind.XML_ATTRIBUTE) {
            return createStringLiteral(XMLConstants.NULL_NS_URI, env);
        }

        if (namespaceSymbol == null) {
            return createStringLiteral(null, env);
        }

        // If the namespace is defined within a callable unit, get the URI index in the local var registry.
        // Otherwise get the URI index in the global var registry.
        int index = ++regIndexes.tString;
        if ((namespaceSymbol.owner.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            emit(InstructionCodes.SLOAD, namespaceSymbol.nsURIIndex, index);
        } else {
            emit(InstructionCodes.SGLOAD, namespaceSymbol.nsURIIndex, index);
        }

        return index;
    }

    private void generateURILookupInstructions(Map<Name, BXMLNSSymbol> namespaces, int localNameRegIndex,
                                               int uriRegIndex, int targetQNameRegIndex, DiagnosticPos pos,
                                               SymbolEnv symbolEnv) {
        if (namespaces.isEmpty()) {
            createQNameWithoutPrefix(localNameRegIndex, uriRegIndex, targetQNameRegIndex, pos);
            return;
        }

        Stack<Instruction> endJumpInstrStack = new Stack<>();
        String prefix;

        for (Entry<Name, BXMLNSSymbol> keyValues : namespaces.entrySet()) {
            prefix = keyValues.getKey().getValue();

            // skip the default namespace
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                continue;
            }

            // Below section creates the condition to compare the namespace URIs

            // store the comparing uri as string
            BXMLNSSymbol nsSymbol = keyValues.getValue();

            int opcode = getOpcode(TypeTags.STRING, InstructionCodes.IEQ);
            int conditionExprIndex = ++regIndexes.tBoolean;
            emit(opcode, uriRegIndex, getNamespaceURIIndex(nsSymbol, symbolEnv), conditionExprIndex);

            Instruction ifCondJumpInstr = InstructionFactory.get(InstructionCodes.BR_FALSE, conditionExprIndex, -1);
            emit(ifCondJumpInstr);

            // Below section creates instructions to be executed, if the above condition succeeds (then body)

            // create the prefix literal
            int prefixIndex = createStringLiteral(prefix, env);

            // create a qname
            emit(InstructionCodes.NEWQNAME, localNameRegIndex, uriRegIndex, prefixIndex, targetQNameRegIndex);

            Instruction endJumpInstr = InstructionFactory.get(InstructionCodes.GOTO, -1);
            emit(endJumpInstr);
            endJumpInstrStack.add(endJumpInstr);

            ifCondJumpInstr.setOperand(1, nextIP());
        }

        // else part. create a qname with empty prefix
        createQNameWithoutPrefix(localNameRegIndex, uriRegIndex, targetQNameRegIndex, pos);

        while (!endJumpInstrStack.isEmpty()) {
            endJumpInstrStack.pop().setOperand(0, this.nextIP());
        }
    }

    private void createQNameWithoutPrefix(int localNameRegIndex, int uriRegIndex, int targetQNameRegIndex,
                                            DiagnosticPos pos) {
        int prefixIndex = createStringLiteral(null, env);
        emit(InstructionCodes.NEWQNAME, localNameRegIndex, uriRegIndex, prefixIndex, targetQNameRegIndex);
    }

    /**
     * Creates a string literal expression, generate the code and returns the registry index.
     *
     * @param value String value to generate the string literal
     * @param env Environment
     * @return String registry index of the generated string
     */
    private int createStringLiteral(String value, SymbolEnv env) {
        BLangLiteral prefixLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        prefixLiteral.value = value;
        prefixLiteral.typeTag = TypeTags.STRING;
        prefixLiteral.type = symTable.stringType;
        genNode(prefixLiteral, env);
        return prefixLiteral.regIndex;
    }
}

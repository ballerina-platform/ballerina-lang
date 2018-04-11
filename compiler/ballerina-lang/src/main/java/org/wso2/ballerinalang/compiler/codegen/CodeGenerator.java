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
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.FunctionFlags;
import org.ballerinalang.util.TransactionStatus;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BSingletonType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSingleton;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangEnumeratorAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangAttachedFunctionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangTransformerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangJSONLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStreamLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFieldVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
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
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.AttachedFunctionInfo;
import org.wso2.ballerinalang.programfile.CallableUnitInfo;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.PackageFile;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile;
import org.wso2.ballerinalang.programfile.DefaultValue;
import org.wso2.ballerinalang.programfile.ErrorTableEntry;
import org.wso2.ballerinalang.programfile.ForkjoinInfo;
import org.wso2.ballerinalang.programfile.FunctionInfo;
import org.wso2.ballerinalang.programfile.ImportPackageInfo;
import org.wso2.ballerinalang.programfile.Instruction;
import org.wso2.ballerinalang.programfile.Instruction.Operand;
import org.wso2.ballerinalang.programfile.Instruction.RegIndex;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.programfile.InstructionFactory;
import org.wso2.ballerinalang.programfile.LineNumberInfo;
import org.wso2.ballerinalang.programfile.LocalVariableInfo;
import org.wso2.ballerinalang.programfile.PackageInfo;
import org.wso2.ballerinalang.programfile.PackageVarInfo;
import org.wso2.ballerinalang.programfile.ResourceInfo;
import org.wso2.ballerinalang.programfile.ServiceInfo;
import org.wso2.ballerinalang.programfile.SingletonInfo;
import org.wso2.ballerinalang.programfile.StructFieldInfo;
import org.wso2.ballerinalang.programfile.StructInfo;
import org.wso2.ballerinalang.programfile.TransformerInfo;
import org.wso2.ballerinalang.programfile.WorkerDataChannelInfo;
import org.wso2.ballerinalang.programfile.WorkerInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfoPool;
import org.wso2.ballerinalang.programfile.attributes.CodeAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ErrorTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LineNumberTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LocalVariableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ParamDefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.VarTypeCountAttributeInfo;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPool;
import org.wso2.ballerinalang.programfile.cpentries.FloatCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ForkJoinCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.IntegerCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.PackageRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StringCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StructureRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.TransformerRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.TypeRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.UTF8CPEntry;
import org.wso2.ballerinalang.programfile.cpentries.WorkerDataChannelRefCPEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;

import static org.wso2.ballerinalang.compiler.codegen.CodeGenerator.VariableIndex.Kind.FIELD;
import static org.wso2.ballerinalang.compiler.codegen.CodeGenerator.VariableIndex.Kind.LOCAL;
import static org.wso2.ballerinalang.compiler.codegen.CodeGenerator.VariableIndex.Kind.PACKAGE;
import static org.wso2.ballerinalang.compiler.codegen.CodeGenerator.VariableIndex.Kind.REG;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BLOB_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BOOL_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.FLOAT_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.INT_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.REF_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.STRING_OFFSET;

/**
 * Generates Ballerina bytecode by visiting the AST.
 *
 * @since 0.94
 */
public class CodeGenerator extends BLangNodeVisitor {

    private static final CompilerContext.Key<CodeGenerator> CODE_GENERATOR_KEY =
            new CompilerContext.Key<>();
    /**
     * This structure holds current package-level variable indexes.
     */
    private VariableIndex pvIndexes = new VariableIndex(PACKAGE);

    /**
     * This structure holds current local variable indexes.
     */
    private VariableIndex lvIndexes = new VariableIndex(LOCAL);

    /**
     * This structure holds current field indexes.
     */
    private VariableIndex fieldIndexes = new VariableIndex(FIELD);

    /**
     * This structure holds current register indexes.
     */
    private VariableIndex regIndexes = new VariableIndex(REG);

    /**
     * This structure holds the maximum register count per type.
     * This structure is updated for every statement.
     */
    private VariableIndex maxRegIndexes = new VariableIndex(REG);

    private List<RegIndex> regIndexList = new ArrayList<>();

    private SymbolEnv env;
    // TODO Remove this dependency from the code generator
    private SymbolTable symTable;

    private boolean buildCompiledPackage;
    private ProgramFile programFile;
    private PackageFile packageFile;

    private PackageInfo currentPkgInfo;
    private PackageID currentPkgID;
    private int currentPackageRefCPIndex;

    private LineNumberTableAttributeInfo lineNoAttrInfo;
    private CallableUnitInfo currentCallableUnitInfo;
    private LocalVariableAttributeInfo localVarAttrInfo;
    private WorkerInfo currentWorkerInfo;
    private ServiceInfo currentServiceInfo;

    // Required variables to generate code for assignment statements
    private boolean varAssignment = false;

    // Disable register index reset behaviour.
    // By default register indexes get reset for every statement.
    // We need to disable this behaviour for desugared expressions.
    private boolean regIndexResetDisabled = false;

    private int transactionIndex = 0;

    private Stack<Instruction> loopResetInstructionStack = new Stack<>();
    private Stack<Instruction> loopExitInstructionStack = new Stack<>();
    private Stack<Instruction> abortInstructions = new Stack<>();
    private Stack<Instruction> failInstructions = new Stack<>();

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
        this.symTable = SymbolTable.getInstance(context);
    }

    public ProgramFile generateBALX(BLangPackage pkgNode) {
        programFile = new ProgramFile();
        // TODO: Fix this. Added temporally for codegen. Load this from VM side.
        genPackage(this.symTable.builtInPackageSymbol);

        // Normal Flow.
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

    public PackageFile generateBALO(BLangPackage pkgNode) {
        this.buildCompiledPackage = true;
        this.packageFile = new PackageFile();
        genPackage(pkgNode.symbol);

        // Add global variable indexes to the ProgramFile
        prepareIndexes(pvIndexes);

        // Create Global variable attribute info
        addVarCountAttrInfo(this.packageFile, this.packageFile, pvIndexes);
        return this.packageFile;
    }

    private void setEntryPoints(ProgramFile programFile, BLangPackage pkgNode) {
        BLangFunction mainFunc = getMainFunction(pkgNode);
        if (mainFunc != null) {
            programFile.setMainEPAvailable(true);
        }

        if (pkgNode.services.size() != 0) {
            programFile.setServiceEPAvailable(true);
        }
    }

    private BLangFunction getMainFunction(BLangPackage pkgNode) {
        List<BLangFunction> functions = pkgNode.functions.stream()
                .filter(f -> (f.name.value.equals(MAIN_FUNCTION_NAME) &&
                        f.symbol.params.size() == 1 &&
                        f.symbol.retType == symTable.nilType))
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
            if (resolveToSuperType(arrayType.eType).tag == TypeTags.STRING) {
                return f;
            }
        }
        return null;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.CODE_GEN)) {
            if (!buildCompiledPackage) {
                programFile.packageInfoMap.put(pkgNode.symbol.pkgID.bvmAlias(), pkgNode.symbol.packageInfo);
            }
            return;
        }

        // TODO Improve this design without if/else
        PackageInfo packageInfo = new PackageInfo();
        pkgNode.symbol.packageInfo = packageInfo;
        if (buildCompiledPackage) {
            // Generating the BALO
            pkgNode.imports.forEach(impPkgNode -> {
                int impPkgNameCPIndex = addUTF8CPEntry(packageInfo, impPkgNode.symbol.name.value);
                // TODO Improve the import package version once it is available
                int impPkgVersionCPIndex = addUTF8CPEntry(packageInfo, PackageID.DEFAULT.version.value);
                ImportPackageInfo importPkgInfo = new ImportPackageInfo(impPkgNameCPIndex, impPkgVersionCPIndex);
                packageInfo.importPkgInfoSet.add(importPkgInfo);
                packageFile.packageInfo = packageInfo;
            });
        } else {
            // Generating a BALX
            // first visit all the imports
            pkgNode.imports.forEach(impPkgNode -> genNode(impPkgNode, this.env));
            // TODO We need to create identifier for both name and the version
            programFile.packageInfoMap.put(pkgNode.symbol.pkgID.bvmAlias(), packageInfo);
        }

        // Add the current package to the program file
        BPackageSymbol pkgSymbol = pkgNode.symbol;
        currentPkgID = pkgSymbol.pkgID;
        currentPkgInfo = packageInfo;
        currentPkgInfo.nameCPIndex = addUTF8CPEntry(currentPkgInfo,
                                                    currentPkgID.bvmAlias());
        currentPkgInfo.versionCPIndex = addUTF8CPEntry(currentPkgInfo, currentPkgID.version.value);

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
        pkgNode.functions.forEach(this::createFunctionInfoEntry);
        pkgNode.services.forEach(this::createServiceInfoEntry);
        pkgNode.functions.forEach(this::createFunctionInfoEntry);
        pkgNode.transformers.forEach(this::createTransformerInfoEntry);
        pkgNode.singletons.forEach(this::createSingletonInfoEntry);

        // Visit package builtin function
        visitBuiltinFunctions(pkgNode.initFunction);
        visitBuiltinFunctions(pkgNode.startFunction);
        visitBuiltinFunctions(pkgNode.stopFunction);

        pkgNode.topLevelNodes.stream()
                .filter(pkgLevelNode -> pkgLevelNode.getKind() != NodeKind.VARIABLE &&
                        pkgLevelNode.getKind() != NodeKind.XMLNS)
                .forEach(pkgLevelNode -> genNode((BLangNode) pkgLevelNode, this.env));

        currentPkgInfo.addAttributeInfo(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE, lineNoAttrInfo);
        currentPackageRefCPIndex = -1;
        currentPkgID = null;
        pkgNode.completedPhases.add(CompilerPhase.CODE_GEN);
    }

    private void visitBuiltinFunctions(BLangFunction function) {
        createFunctionInfoEntry(function);
        genNode(function, this.env);
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        genPackage(pkgSymbol);
    }

    public void visit(BLangService serviceNode) {
        BLangFunction initFunction = (BLangFunction) serviceNode.getInitFunction();
        visit(initFunction);

        currentServiceInfo = currentPkgInfo.getServiceInfo(serviceNode.getName().getValue());

        SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceNode.symbol.scope, this.env);
        serviceNode.resources.forEach(resource -> genNode(resource, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        ResourceInfo resourceInfo = currentServiceInfo.resourceInfoMap.get(resourceNode.name.getValue());
        currentCallableUnitInfo = resourceInfo;

        SymbolEnv resourceEnv = SymbolEnv
                .createResourceActionSymbolEnv(resourceNode, resourceNode.symbol.scope, this.env);
        visitInvokableNode(resourceNode, currentCallableUnitInfo, resourceEnv);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.env);
        currentCallableUnitInfo = currentPkgInfo.functionInfoMap.get(funcNode.symbol.name.value);
        visitInvokableNode(funcNode, currentCallableUnitInfo, funcEnv);
    }

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, this.env);

        for (BLangStatement stmt : blockNode.stmts) {
            if (stmt.getKind() != NodeKind.TRY && stmt.getKind() != NodeKind.CATCH
                    && stmt.getKind() != NodeKind.IF) {
                addLineNumberInfo(stmt.pos);
            }

            genNode(stmt, blockEnv);
            if (regIndexResetDisabled) {
                // This block node is possibly be part of a desugered expression
                continue;
            }

            // Update the maxRegIndexes structure
            setMaxRegIndexes(regIndexes, maxRegIndexes);

            // Reset the regIndexes structure for every statement
            regIndexes = new VariableIndex(REG);
        }
    }

    public void visit(BLangEnum enumNode) {
    }

    public void visit(BLangVariable varNode) {
        BVarSymbol varSymbol = varNode.symbol;
        int ownerSymTag = env.scope.owner.tag;
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            varSymbol.varIndex = getLVIndex(varSymbol.type);
            LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(varSymbol);
            localVarAttrInfo.localVars.add(localVarInfo);
        } else {
            // TODO Support other variable nodes
            throw new IllegalStateException("");
        }

        BLangExpression rhsExpr = varNode.expr;
        if (rhsExpr != null) {
            rhsExpr.regIndex = varSymbol.varIndex;
            genNode(rhsExpr, this.env);
        }
    }

    public void visit(BLangTransformer transformerNode) {
        SymbolEnv transformerEnv =
                SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, this.env);
        currentCallableUnitInfo = currentPkgInfo.transformerInfoMap.get(transformerNode.symbol.name.value);
        visitInvokableNode(transformerNode, currentCallableUnitInfo, transformerEnv);
    }

    // Statements

    public void visit(BLangVariableDef varDefNode) {
        genNode(varDefNode.var, this.env);
    }

    @Override
    public void visit(BLangMatch matchStmt) {
        // TODO
    }

    public void visit(BLangReturn returnNode) {
        if (resolveToSuperType(returnNode.expr.type) != symTable.nilType) {
            BLangExpression expr = returnNode.expr;
            this.genNode(expr, this.env);
            emit(this.typeTagToInstr(expr.type), getOperand(0), expr.regIndex);
        }
        generateFinallyInstructions(returnNode);
        emit(InstructionCodes.RET);
    }

    private int typeTagToInstr(BType bType) {
        return typeTagToInstr(resolveToSuperType(bType).tag);
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
        Operand regIndex = calcAndGetExprRegIndex(literalExpr);
        int typeTag = resolveToSuperType(literalExpr.type).tag;

        switch (typeTag) {
            case TypeTags.INT:
                long longVal = (Long) literalExpr.value;
                if (longVal >= 0 && longVal <= 5) {
                    opcode = InstructionCodes.ICONST_0 + (int) longVal;
                    emit(opcode, regIndex);
                } else {
                    int intCPEntryIndex = currentPkgInfo.addCPEntry(new IntegerCPEntry(longVal));
                    emit(InstructionCodes.ICONST, getOperand(intCPEntryIndex), regIndex);
                }
                break;

            case TypeTags.FLOAT:
                double doubleVal = (Double) literalExpr.value;
                if (doubleVal == 0 || doubleVal == 1 || doubleVal == 2 ||
                        doubleVal == 3 || doubleVal == 4 || doubleVal == 5) {
                    opcode = InstructionCodes.FCONST_0 + (int) doubleVal;
                    emit(opcode, regIndex);
                } else {
                    int floatCPEntryIndex = currentPkgInfo.addCPEntry(new FloatCPEntry(doubleVal));
                    emit(InstructionCodes.FCONST, getOperand(floatCPEntryIndex), regIndex);
                }
                break;

            case TypeTags.STRING:
                String strValue = (String) literalExpr.value;
                StringCPEntry stringCPEntry = new StringCPEntry(addUTF8CPEntry(currentPkgInfo, strValue), strValue);
                int strCPIndex = currentPkgInfo.addCPEntry(stringCPEntry);
                emit(InstructionCodes.SCONST, getOperand(strCPIndex), regIndex);
                break;

            case TypeTags.BOOLEAN:
                boolean booleanVal = (Boolean) literalExpr.value;
                if (!booleanVal) {
                    opcode = InstructionCodes.BCONST_0;
                } else {
                    opcode = InstructionCodes.BCONST_1;
                }
                emit(opcode, regIndex);
                break;
            case TypeTags.NIL:
                emit(InstructionCodes.RCONST_NULL, regIndex);
        }
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        BType etype;
        if (arrayLiteral.type.tag == TypeTags.ANY) {
            etype = arrayLiteral.type;
        } else {
            etype = ((BArrayType) arrayLiteral.type).eType;
        }

        // Emit create array instruction
        int opcode = getOpcode(etype, InstructionCodes.INEWARRAY);
        Operand arrayVarRegIndex = calcAndGetExprRegIndex(arrayLiteral);
        Operand typeCPIndex = getTypeCPIndex(arrayLiteral.type);
        emit(opcode, arrayVarRegIndex, typeCPIndex);

        // Emit instructions populate initial array values;
        for (int i = 0; i < arrayLiteral.exprs.size(); i++) {
            BLangExpression argExpr = arrayLiteral.exprs.get(i);
            genNode(argExpr, this.env);

            BLangLiteral indexLiteral = new BLangLiteral();
            indexLiteral.pos = arrayLiteral.pos;
            indexLiteral.value = (long) i;
            indexLiteral.type = symTable.intType;
            genNode(indexLiteral, this.env);

            opcode = getOpcode(argExpr.type, InstructionCodes.IASTORE);
            emit(opcode, arrayVarRegIndex, indexLiteral.regIndex, argExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangJSONArrayLiteral arrayLiteral) {
        arrayLiteral.regIndex = calcAndGetExprRegIndex(arrayLiteral);
        List<BLangExpression> argExprs = arrayLiteral.exprs;

        BLangLiteral arraySizeLiteral = new BLangLiteral();
        arraySizeLiteral.pos = arrayLiteral.pos;
        arraySizeLiteral.value = (long) argExprs.size();
        arraySizeLiteral.type = symTable.intType;
        genNode(arraySizeLiteral, this.env);
        emit(InstructionCodes.JSONNEWARRAY, arrayLiteral.regIndex, arraySizeLiteral.regIndex);

        for (int i = 0; i < argExprs.size(); i++) {
            BLangExpression argExpr = argExprs.get(i);
            genNode(argExpr, this.env);

            BLangLiteral indexLiteral = new BLangLiteral();
            indexLiteral.pos = arrayLiteral.pos;
            indexLiteral.value = (long) i;
            indexLiteral.type = symTable.intType;
            genNode(indexLiteral, this.env);
            emit(InstructionCodes.JSONASTORE, arrayLiteral.regIndex, indexLiteral.regIndex, argExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangJSONLiteral jsonLiteral) {
        jsonLiteral.regIndex = calcAndGetExprRegIndex(jsonLiteral);
        Operand typeCPIndex = getTypeCPIndex(jsonLiteral.type);
        emit(InstructionCodes.NEWJSON, jsonLiteral.regIndex, typeCPIndex);

        for (BLangRecordKeyValue keyValue : jsonLiteral.keyValuePairs) {
            BLangExpression keyExpr = keyValue.key.expr;
            genNode(keyExpr, this.env);

            BLangExpression valueExpr = keyValue.valueExpr;
            genNode(valueExpr, this.env);

            emit(InstructionCodes.JSONSTORE, jsonLiteral.regIndex, keyExpr.regIndex, valueExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangMapLiteral mapLiteral) {
        Operand mapVarRegIndex = calcAndGetExprRegIndex(mapLiteral);
        Operand typeCPIndex = getTypeCPIndex(mapLiteral.type);
        emit(InstructionCodes.NEWMAP, mapVarRegIndex, typeCPIndex);

        // Handle Map init stuff
        for (BLangRecordKeyValue keyValue : mapLiteral.keyValuePairs) {
            BLangExpression keyExpr = keyValue.key.expr;
            genNode(keyExpr, this.env);

            BLangExpression valueExpr = keyValue.valueExpr;
            genNode(valueExpr, this.env);

            BMapType mapType = (BMapType) mapLiteral.type;

            int opcode = getValueToRefTypeCastOpcode(mapType.constraint);
            if (opcode == InstructionCodes.NOP) {
                emit(InstructionCodes.MAPSTORE, mapVarRegIndex, keyExpr.regIndex, valueExpr.regIndex);
            } else {
                RegIndex refRegMapValue = getRegIndex(symTable.anyType);
                emit(opcode, valueExpr.regIndex, refRegMapValue);
                emit(InstructionCodes.MAPSTORE, mapVarRegIndex, keyExpr.regIndex, refRegMapValue);
            }
        }
    }

    @Override
    public void visit(BLangStructLiteral structLiteral) {
        BStructSymbol structSymbol = (BStructSymbol) structLiteral.type.tsymbol;
        int pkgCPIndex = addPackageRefCPEntry(currentPkgInfo, structSymbol.pkgID);
        int structNameCPIndex = addUTF8CPEntry(currentPkgInfo, structSymbol.name.value);
        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, structNameCPIndex);
        Operand structCPIndex = getOperand(currentPkgInfo.addCPEntry(structureRefCPEntry));

        //Emit an instruction to create a new struct.
        RegIndex structRegIndex = calcAndGetExprRegIndex(structLiteral);
        emit(InstructionCodes.NEWSTRUCT, structCPIndex, structRegIndex);

        // Invoke the struct default values init function here.
        if (structSymbol.defaultsValuesInitFunc != null) {
            int funcRefCPIndex = getFuncRefCPIndex(structSymbol.defaultsValuesInitFunc.symbol);
            // call funcRefCPIndex 1 structRegIndex 0
            Operand[] operands = new Operand[5];
            operands[0] = getOperand(funcRefCPIndex);
            operands[1] = getOperand(false);
            operands[2] = getOperand(1);
            operands[3] = structRegIndex;
            operands[4] = getOperand(0);
            emit(InstructionCodes.CALL, operands);
        }

        // Invoke the struct initializer here.
        if (structLiteral.initializer != null) {
            int funcRefCPIndex = getFuncRefCPIndex(structLiteral.initializer.symbol);
            // call funcRefCPIndex 1 structRegIndex 0
            Operand[] operands = new Operand[5];
            operands[0] = getOperand(funcRefCPIndex);
            operands[1] = getOperand(false);
            operands[2] = getOperand(1);
            operands[3] = structRegIndex;
            operands[4] = getOperand(0);
            emit(InstructionCodes.CALL, operands);
        }

        // Generate code the struct literal.
        for (BLangRecordKeyValue keyValue : structLiteral.keyValuePairs) {
            BLangRecordKey key = keyValue.key;
            Operand fieldIndex = key.fieldSymbol.varIndex;

            genNode(keyValue.valueExpr, this.env);

            int opcode = getOpcode(key.fieldSymbol.type, InstructionCodes.IFIELDSTORE);
            emit(opcode, structRegIndex, fieldIndex, keyValue.valueExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        genNode(tableLiteral.configurationExpr, this.env);
        Operand varRefRegIndex = tableLiteral.configurationExpr.regIndex;

        tableLiteral.regIndex = calcAndGetExprRegIndex(tableLiteral);
        Operand typeCPIndex = getTypeCPIndex(tableLiteral.type);
        emit(InstructionCodes.NEWTABLE, tableLiteral.regIndex, typeCPIndex, varRefRegIndex);
    }

    @Override
    public void visit(BLangStreamLiteral streamLiteral) {
        streamLiteral.regIndex = calcAndGetExprRegIndex(streamLiteral);
        Operand typeCPIndex = getTypeCPIndex(streamLiteral.type);
        StringCPEntry nameCPEntry = new StringCPEntry(addUTF8CPEntry(currentPkgInfo, streamLiteral.name.value),
                streamLiteral.name.value);
        Operand nameCPIndex = getOperand(currentPkgInfo.addCPEntry(nameCPEntry));
        emit(InstructionCodes.NEWSTREAM, streamLiteral.regIndex, typeCPIndex, nameCPIndex);
    }

    @Override
    public void visit(BLangLocalVarRef localVarRef) {
        if (localVarRef.regIndex != null && (localVarRef.regIndex.isLHSIndex || localVarRef.regIndex.isVarIndex)) {
            emit(getOpcode(localVarRef.type, InstructionCodes.IMOVE),
                    localVarRef.varSymbol.varIndex, localVarRef.regIndex);
            return;
        }

        localVarRef.regIndex = localVarRef.varSymbol.varIndex;
    }

    @Override
    public void visit(BLangFieldVarRef fieldVarRef) {
        RegIndex fieldIndex = fieldVarRef.varSymbol.varIndex;

        // This is a connector field.
        // the connector reference must be stored in the current reference register index.
        Operand varRegIndex = getOperand(0);
        if (varAssignment) {
            int opcode = getOpcode(fieldVarRef.type, InstructionCodes.IFIELDSTORE);
            emit(opcode, varRegIndex, fieldIndex, fieldVarRef.regIndex);
            return;
        }

        int opcode = getOpcode(fieldVarRef.type, InstructionCodes.IFIELDLOAD);
        RegIndex exprRegIndex = calcAndGetExprRegIndex(fieldVarRef);
        emit(opcode, varRegIndex, fieldIndex, exprRegIndex);
    }

    @Override
    public void visit(BLangPackageVarRef packageVarRef) {
        Operand gvIndex = packageVarRef.varSymbol.varIndex;
        if (varAssignment) {
            int opcode = getOpcode(packageVarRef.type, InstructionCodes.IGSTORE);
            emit(opcode, packageVarRef.regIndex, gvIndex);
            return;
        }

        int opcode = getOpcode(packageVarRef.type, InstructionCodes.IGLOAD);
        packageVarRef.regIndex = calcAndGetExprRegIndex(packageVarRef);
        emit(opcode, gvIndex, packageVarRef.regIndex);
    }

    @Override
    public void visit(BLangFunctionVarRef functionVarRef) {
        visitFunctionPointerLoad(functionVarRef, (BInvokableSymbol) functionVarRef.symbol);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        Operand typeCPIndex = getTypeCPIndex(typeLoad.symbol.type);
        emit(InstructionCodes.TYPELOAD, typeCPIndex, calcAndGetExprRegIndex(typeLoad));
    }

    @Override
    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(fieldAccessExpr.expr, this.env);
        Operand varRefRegIndex = fieldAccessExpr.expr.regIndex;

        int opcode;
        Operand fieldIndex = fieldAccessExpr.varSymbol.varIndex;
        if (variableStore) {
            opcode = getOpcode(fieldAccessExpr.symbol.type, InstructionCodes.IFIELDSTORE);
            emit(opcode, varRefRegIndex, fieldIndex, fieldAccessExpr.regIndex);
        } else {
            opcode = getOpcode(fieldAccessExpr.symbol.type, InstructionCodes.IFIELDLOAD);
            emit(opcode, varRefRegIndex, fieldIndex, calcAndGetExprRegIndex(fieldAccessExpr));
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangMapAccessExpr mapKeyAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(mapKeyAccessExpr.expr, this.env);
        Operand varRefRegIndex = mapKeyAccessExpr.expr.regIndex;

        genNode(mapKeyAccessExpr.indexExpr, this.env);
        Operand keyRegIndex = mapKeyAccessExpr.indexExpr.regIndex;

        BMapType mapType = (BMapType) mapKeyAccessExpr.expr.type;
        if (variableStore) {
            int opcode = getValueToRefTypeCastOpcode(mapType.constraint);
            if (opcode == InstructionCodes.NOP) {
                emit(InstructionCodes.MAPSTORE, varRefRegIndex, keyRegIndex, mapKeyAccessExpr.regIndex);
            } else {
                RegIndex refRegMapValue = getRegIndex(symTable.anyType);
                emit(opcode, mapKeyAccessExpr.regIndex, refRegMapValue);
                emit(InstructionCodes.MAPSTORE, varRefRegIndex, keyRegIndex, refRegMapValue);
            }
        } else {
            int opcode = getRefToValueTypeCastOpcode(mapType.constraint);
            if (opcode == InstructionCodes.NOP) {
                emit(InstructionCodes.MAPLOAD, varRefRegIndex, keyRegIndex, calcAndGetExprRegIndex(mapKeyAccessExpr));
            } else {
                RegIndex refRegMapValue = getRegIndex(symTable.anyType);
                emit(InstructionCodes.MAPLOAD, varRefRegIndex, keyRegIndex, refRegMapValue);
                emit(opcode, refRegMapValue, calcAndGetExprRegIndex(mapKeyAccessExpr));
            }
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangJSONAccessExpr jsonAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(jsonAccessExpr.expr, this.env);
        Operand varRefRegIndex = jsonAccessExpr.expr.regIndex;

        genNode(jsonAccessExpr.indexExpr, this.env);
        Operand keyRegIndex = jsonAccessExpr.indexExpr.regIndex;

        if (resolveToSuperType(jsonAccessExpr.indexExpr.type).tag == TypeTags.INT) {
            if (variableStore) {
                emit(InstructionCodes.JSONASTORE, varRefRegIndex, keyRegIndex, jsonAccessExpr.regIndex);
            } else {
                emit(InstructionCodes.JSONALOAD, varRefRegIndex, keyRegIndex, calcAndGetExprRegIndex(jsonAccessExpr));
            }
        } else {
            if (variableStore) {
                emit(InstructionCodes.JSONSTORE, varRefRegIndex, keyRegIndex, jsonAccessExpr.regIndex);
            } else {
                emit(InstructionCodes.JSONLOAD, varRefRegIndex, keyRegIndex, calcAndGetExprRegIndex(jsonAccessExpr));
            }
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangXMLAccessExpr xmlIndexAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(xmlIndexAccessExpr.expr, this.env);
        RegIndex varRefRegIndex = xmlIndexAccessExpr.expr.regIndex;

        genNode(xmlIndexAccessExpr.indexExpr, this.env);
        RegIndex indexRegIndex = xmlIndexAccessExpr.indexExpr.regIndex;

        RegIndex elementRegIndex = calcAndGetExprRegIndex(xmlIndexAccessExpr);
        if (xmlIndexAccessExpr.fieldType == FieldKind.ALL) {
            emit(InstructionCodes.XMLLOADALL, varRefRegIndex, elementRegIndex);
        } else if (resolveToSuperType(xmlIndexAccessExpr.indexExpr.type).tag == TypeTags.STRING) {
            emit(InstructionCodes.XMLLOAD, varRefRegIndex, indexRegIndex, elementRegIndex);
        } else {
            emit(InstructionCodes.XMLSEQLOAD, varRefRegIndex, indexRegIndex, elementRegIndex);
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangArrayAccessExpr arrayIndexAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(arrayIndexAccessExpr.expr, this.env);
        Operand varRefRegIndex = arrayIndexAccessExpr.expr.regIndex;

        genNode(arrayIndexAccessExpr.indexExpr, this.env);
        Operand indexRegIndex = arrayIndexAccessExpr.indexExpr.regIndex;

        BArrayType arrayType = (BArrayType) arrayIndexAccessExpr.expr.type;
        if (variableStore) {
            int opcode = getOpcode(arrayType.eType, InstructionCodes.IASTORE);
            emit(opcode, varRefRegIndex, indexRegIndex, arrayIndexAccessExpr.regIndex);
        } else {
            int opcode = getOpcode(arrayType.eType, InstructionCodes.IALOAD);
            emit(opcode, varRefRegIndex, indexRegIndex, calcAndGetExprRegIndex(arrayIndexAccessExpr));
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangEnumeratorAccessExpr enumeratorAccessExpr) {
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (OperatorKind.AND.equals(binaryExpr.opKind)) {
            visitAndExpression(binaryExpr);
        } else if (OperatorKind.OR.equals(binaryExpr.opKind)) {
            visitOrExpression(binaryExpr);
        } else if (binaryExpr.opSymbol.opcode == InstructionCodes.REQ_NULL ||
                binaryExpr.opSymbol.opcode == InstructionCodes.RNE_NULL ||
                binaryExpr.opSymbol.opcode == InstructionCodes.SEQ_NULL ||
                binaryExpr.opSymbol.opcode == InstructionCodes.SNE_NULL) {
            BLangExpression expr = (binaryExpr.lhsExpr.type.tag == TypeTags.NIL) ?
                    binaryExpr.rhsExpr : binaryExpr.lhsExpr;
            genNode(expr, this.env);
            emit(binaryExpr.opSymbol.opcode, expr.regIndex, calcAndGetExprRegIndex(binaryExpr));
        } else {
            genNode(binaryExpr.lhsExpr, this.env);
            genNode(binaryExpr.rhsExpr, this.env);
            RegIndex regIndex = calcAndGetExprRegIndex(binaryExpr);
            emit(binaryExpr.opSymbol.opcode, binaryExpr.lhsExpr.regIndex, binaryExpr.rhsExpr.regIndex, regIndex);
        }
    }

    public void visit(BLangElvisExpr elvisExpr) {
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        genNode(assignableExpr.lhsExpr, this.env);
        RegIndex regIndex = calcAndGetExprRegIndex(assignableExpr);
        Operand typeCPIndex = getTypeCPIndex(assignableExpr.targetType);
        emit(assignableExpr.opSymbol.opcode, assignableExpr.lhsExpr.regIndex, typeCPIndex, regIndex);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        // Emit create array instruction
        RegIndex exprRegIndex = calcAndGetExprRegIndex(bracedOrTupleExpr);
        Operand typeCPIndex = getTypeCPIndex(bracedOrTupleExpr.type);
        emit(InstructionCodes.RNEWARRAY, exprRegIndex, typeCPIndex);

        // Emit instructions populate initial array values;
        for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
            BLangExpression argExpr = bracedOrTupleExpr.expressions.get(i);
            genNode(argExpr, this.env);

            BLangLiteral indexLiteral = new BLangLiteral();
            indexLiteral.pos = argExpr.pos;
            indexLiteral.value = (long) i;
            indexLiteral.type = symTable.intType;
            genNode(indexLiteral, this.env);
            emit(InstructionCodes.RASTORE, exprRegIndex, indexLiteral.regIndex, argExpr.regIndex);
        }
    }

    private void visitAndExpression(BLangBinaryExpr binaryExpr) {
        // Code address to jump if at least one of the expressions get evaluated to false.
        // short-circuit evaluation
        Operand falseJumpAddr = getOperand(-1);

        // Generate code for the left hand side
        genNode(binaryExpr.lhsExpr, this.env);
        emit(InstructionCodes.BR_FALSE, binaryExpr.lhsExpr.regIndex, falseJumpAddr);

        // Generate code for the right hand side
        genNode(binaryExpr.rhsExpr, this.env);
        emit(InstructionCodes.BR_FALSE, binaryExpr.rhsExpr.regIndex, falseJumpAddr);

        // If both l and r conditions are true, then load 'true'
        calcAndGetExprRegIndex(binaryExpr);
        emit(InstructionCodes.BCONST_1, binaryExpr.regIndex);

        Operand gotoAddr = getOperand(-1);
        emit(InstructionCodes.GOTO, gotoAddr);

        falseJumpAddr.value = nextIP();

        // Load 'false' if the both conditions are false;
        emit(InstructionCodes.BCONST_0, binaryExpr.regIndex);
        gotoAddr.value = nextIP();
    }

    private void visitOrExpression(BLangBinaryExpr binaryExpr) {
        // short-circuit evaluation
        // Code address to jump if the lhs expression gets evaluated to 'true'.
        Operand lExprTrueJumpAddr = getOperand(-1);

        // Code address to jump if the rhs expression gets evaluated to 'false'.
        Operand rExprFalseJumpAddr = getOperand(-1);

        // Generate code for the left hand side
        genNode(binaryExpr.lhsExpr, this.env);
        emit(InstructionCodes.BR_TRUE, binaryExpr.lhsExpr.regIndex, lExprTrueJumpAddr);

        // Generate code for the right hand side
        genNode(binaryExpr.rhsExpr, this.env);
        emit(InstructionCodes.BR_FALSE, binaryExpr.rhsExpr.regIndex, rExprFalseJumpAddr);

        lExprTrueJumpAddr.value = nextIP();
        RegIndex exprRegIndex = calcAndGetExprRegIndex(binaryExpr);
        emit(InstructionCodes.BCONST_1, exprRegIndex);

        Operand gotoAddr = getOperand(-1);
        emit(InstructionCodes.GOTO, gotoAddr);
        rExprFalseJumpAddr.value = nextIP();

        // Load 'false' if the both conditions are false;
        emit(InstructionCodes.BCONST_0, exprRegIndex);
        gotoAddr.value = nextIP();
    }

    public void visit(BLangInvocation iExpr) {
        if (iExpr.expr != null) {
            return;
        }

        Operand[] operands = getFuncOperands(iExpr);
        emit(InstructionCodes.CALL, operands);
    }

    public void visit(BLangActionInvocation aIExpr) {
    }

    public void visit(BLangTypeInit cIExpr) {
        BSymbol structSymbol = cIExpr.type.tsymbol;
        int pkgCPIndex = addPackageRefCPEntry(currentPkgInfo, structSymbol.pkgID);
        int structNameCPIndex = addUTF8CPEntry(currentPkgInfo, structSymbol.name.value);
        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, structNameCPIndex);
        Operand structCPIndex = getOperand(currentPkgInfo.addCPEntry(structureRefCPEntry));

        //Emit an instruction to create a new struct.
        RegIndex structRegIndex = calcAndGetExprRegIndex(cIExpr);
        emit(InstructionCodes.NEWSTRUCT, structCPIndex, structRegIndex);

        // Invoke the struct initializer here.
        Operand[] operands = getFuncOperands(cIExpr.objectInitInvocation);

        Operand[] callOperands = new Operand[operands.length + 1];
        callOperands[0] = operands[0];
        callOperands[1] = operands[1];
        callOperands[2] = getOperand(operands[2].value + 1);
        callOperands[3] = structRegIndex;

        System.arraycopy(operands, 3, callOperands, 4, operands.length - 3);
        emit(InstructionCodes.CALL, callOperands);
    }

    public void visit(BLangAttachedFunctionInvocation iExpr) {
        Operand[] operands = getFuncOperands(iExpr);
        if (iExpr.expr.type.tag == TypeTags.STRUCT) {
            Operand[] vCallOperands = new Operand[operands.length + 1];
            vCallOperands[0] = iExpr.expr.regIndex;
            System.arraycopy(operands, 0, vCallOperands, 1, operands.length);
            emit(InstructionCodes.VCALL, vCallOperands);
        } else {
            emit(InstructionCodes.CALL, operands);
        }
    }

    public void visit(BLangTransformerInvocation iExpr) {
        BInvokableSymbol transformerSymbol = (BInvokableSymbol) iExpr.symbol;
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, transformerSymbol.pkgID);
        int transformerNameCPIndex = addUTF8CPEntry(currentPkgInfo, transformerSymbol.name.value);
        TransformerRefCPEntry transformerRefCPEntry = new TransformerRefCPEntry(pkgRefCPIndex, transformerNameCPIndex);

        int transformerRefCPIndex = currentPkgInfo.addCPEntry(transformerRefCPEntry);
        Operand[] operands = getFuncOperands(iExpr, transformerRefCPIndex);

        emit(InstructionCodes.TCALL, operands);
    }

    public void visit(BFunctionPointerInvocation iExpr) {
        Operand[] operands = getFuncOperands(iExpr, -1);
        genNode(iExpr.expr, env);
        operands[0] = iExpr.expr.regIndex;
        emit(InstructionCodes.FPCALL, operands);
    }

    public void visit(BLangTypeConversionExpr convExpr) {
        int opcode = convExpr.conversionSymbol.opcode;

        // Figure out the reg index of the result value
        BType castExprType = convExpr.type;
        RegIndex convExprRegIndex = calcAndGetExprRegIndex(convExpr.regIndex, castExprType);
        convExpr.regIndex = convExprRegIndex;
        if (opcode == InstructionCodes.NOP) {
            convExpr.expr.regIndex = createLHSRegIndex(convExprRegIndex);
            genNode(convExpr.expr, this.env);
            return;
        }

        genNode(convExpr.expr, this.env);
        if (opcode == InstructionCodes.MAP2T ||
                opcode == InstructionCodes.JSON2T ||
                opcode == InstructionCodes.ANY2T ||
                opcode == InstructionCodes.ANY2C ||
                opcode == InstructionCodes.ANY2E ||
                opcode == InstructionCodes.ANY2M ||
                opcode == InstructionCodes.T2JSON ||
                opcode == InstructionCodes.MAP2JSON ||
                opcode == InstructionCodes.JSON2MAP ||
                opcode == InstructionCodes.CHECKCAST) {
            Operand typeCPIndex = getTypeCPIndex(convExpr.targetType);
            emit(opcode, convExpr.expr.regIndex, typeCPIndex, convExprRegIndex);
        } else {
            emit(opcode, convExpr.expr.regIndex, convExprRegIndex);
        }
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        /* ignore */
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        // Determine the reg index of the ternary expression and this reg index will be used by both then and else
        // expressions to store their result
        RegIndex ternaryExprRegIndex = calcAndGetExprRegIndex(ternaryExpr);

        // Generate code for the condition
        this.genNode(ternaryExpr.expr, this.env);
        Operand ifFalseJumpAddr = getOperand(-1);
        this.emit(InstructionCodes.BR_FALSE, ternaryExpr.expr.regIndex, ifFalseJumpAddr);

        // Generate code for the then expression
        ternaryExpr.thenExpr.regIndex = createLHSRegIndex(ternaryExprRegIndex);
        this.genNode(ternaryExpr.thenExpr, this.env);
        Operand endJumpAddr = getOperand(-1);
        this.emit(InstructionCodes.GOTO, endJumpAddr);
        ifFalseJumpAddr.value = nextIP();

        // Generate code for the then expression
        ternaryExpr.elseExpr.regIndex = createLHSRegIndex(ternaryExprRegIndex);
        this.genNode(ternaryExpr.elseExpr, this.env);
        endJumpAddr.value = nextIP();
    }

    public void visit(BLangAwaitExpr awaitExpr) {
        Operand valueRegIndex;
        if (awaitExpr.type != null) {
            valueRegIndex = calcAndGetExprRegIndex(awaitExpr);
        } else {
            valueRegIndex = this.getOperand(-1);
        }
        genNode(awaitExpr.expr, this.env);
        Operand futureRegIndex = awaitExpr.expr.regIndex;
        this.emit(InstructionCodes.AWAIT, futureRegIndex, valueRegIndex);
    }

    public void visit(BLangTypedescExpr accessExpr) {
        Operand typeCPIndex = getTypeCPIndex(accessExpr.resolvedType);
        emit(InstructionCodes.TYPELOAD, typeCPIndex, calcAndGetExprRegIndex(accessExpr));
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        RegIndex exprIndex = calcAndGetExprRegIndex(unaryExpr);

        if (OperatorKind.ADD.equals(unaryExpr.operator) || OperatorKind.UNTAINT.equals(unaryExpr.operator)) {
            unaryExpr.expr.regIndex = createLHSRegIndex(unaryExpr.regIndex);
            genNode(unaryExpr.expr, this.env);
            return;
        }

        int opcode;
        genNode(unaryExpr.expr, this.env);
        if (OperatorKind.LENGTHOF.equals(unaryExpr.operator)) {
            Operand typeCPIndex = getTypeCPIndex(unaryExpr.expr.type);
            opcode = unaryExpr.opSymbol.opcode;
            emit(opcode, unaryExpr.expr.regIndex, typeCPIndex, exprIndex);
        } else {
            opcode = unaryExpr.opSymbol.opcode;
            emit(opcode, unaryExpr.expr.regIndex, exprIndex);
        }
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        visitFunctionPointerLoad(bLangLambdaFunction, ((BLangFunction) bLangLambdaFunction.getFunctionNode()).symbol);
    }

    public void visit(BLangStatementExpression bLangStatementExpression) {
        bLangStatementExpression.regIndex = calcAndGetExprRegIndex(bLangStatementExpression);

        boolean prevRegIndexResetDisabledState = this.regIndexResetDisabled;
        this.regIndexResetDisabled = true;
        genNode(bLangStatementExpression.stmt, this.env);
        this.regIndexResetDisabled = prevRegIndexResetDisabledState;

        genNode(bLangStatementExpression.expr, this.env);
        emit(getOpcode(bLangStatementExpression.expr.type, InstructionCodes.IMOVE),
                bLangStatementExpression.expr.regIndex, bLangStatementExpression.regIndex);
    }

    // private methods

    private <T extends BLangNode, U extends SymbolEnv> T genNode(T t, U u) {
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
        return t;
    }

    private void genPackage(BPackageSymbol pkgSymbol) {
        // TODO First check whether this symbol is from a BALO file.
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgSymbol);
        genNode(pkgEnv.node, pkgEnv);
    }

    private String generateSig(BType[] types) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(types).forEach(e -> builder.append(e.getDesc()));
        return builder.toString();
    }

    private String generateFunctionSig(BType[] paramTypes, BType retType) {
        return "(" + generateSig(paramTypes) + ")(" + retType.getDesc() + ")";
    }

    private String generateFunctionSig(BType[] paramTypes) {
        return "(" + generateSig(paramTypes) + ")()";
    }

    private int getNextIndex(int typeTag, VariableIndex indexes) {
        int index;
        switch (typeTag) {
            case TypeTags.INT:
                index = ++indexes.tInt;
                break;
            case TypeTags.FLOAT:
                index = ++indexes.tFloat;
                break;
            case TypeTags.STRING:
                index = ++indexes.tString;
                break;
            case TypeTags.BOOLEAN:
                index = ++indexes.tBoolean;
                break;
            case TypeTags.BLOB:
                index = ++indexes.tBlob;
                break;
            default:
                index = ++indexes.tRef;
                break;
        }

        return index;
    }

    private int getOpcode(BType type, int baseOpcode) {
        return getOpcode(resolveToSuperType(type).tag, baseOpcode);
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

    private Operand getOperand(int value) {
        return new Operand(value);
    }

    private Operand getOperand(boolean value) {
        return new Operand(value ? 1 : 0);
    }

    private RegIndex getLVIndex(int typeTag) {
        return getRegIndexInternal(typeTag, LOCAL);
    }

    private RegIndex getLVIndex(BType bType) {
        return getLVIndex(resolveToSuperType(bType).tag);
    }

    private RegIndex getPVIndex(int typeTag) {
        return getRegIndexInternal(typeTag, PACKAGE);
    }

    private RegIndex getPVIndex(BType bType) {
        return getPVIndex(resolveToSuperType(bType).tag);
    }

    private RegIndex getFieldIndex(int typeTag) {
        return getRegIndexInternal(typeTag, FIELD);
    }

    private RegIndex getFieldIndex(BType bType) {
        return getFieldIndex(resolveToSuperType(bType).tag);
    }

    private RegIndex getRegIndex(int typeTag) {
        RegIndex regIndex = getRegIndexInternal(typeTag, REG);
        addToRegIndexList(regIndex);
        return regIndex;
    }

    private RegIndex getRegIndex(BType bType) {
        return getRegIndex(resolveToSuperType(bType).tag);
    }

    private BType resolveToSuperType(BType bType) {
        if (bType.tag == TypeTags.SINGLETON) {
            return ((BSingletonType) bType).superSetType;
        }
        return bType;
    }

    private RegIndex getRegIndexInternal(int typeTag, VariableIndex.Kind varIndexKind) {
        int index;
        switch (varIndexKind) {
            case REG:
                return new RegIndex(getNextIndex(typeTag, regIndexes), typeTag);
            case PACKAGE:
                index = getNextIndex(typeTag, pvIndexes);
                break;
            case FIELD:
                index = getNextIndex(typeTag, fieldIndexes);
                break;
            default:
                index = getNextIndex(typeTag, lvIndexes);
                break;
        }

        RegIndex regIndex = new RegIndex(index, typeTag);
        regIndex.isVarIndex = true;
        return regIndex;
    }

    private RegIndex calcAndGetExprRegIndex(BLangExpression expr) {
        expr.regIndex = calcAndGetExprRegIndex(expr.regIndex, expr.type);
        return expr.regIndex;
    }

    private RegIndex calcAndGetExprRegIndex(RegIndex regIndex, BType type) {
        if (regIndex != null && (regIndex.isVarIndex || regIndex.isLHSIndex)) {
            return regIndex;
        }

        return getRegIndex(type);
    }

    private RegIndex createLHSRegIndex(RegIndex regIndex) {
        if (regIndex.isVarIndex || regIndex.isLHSIndex) {
            return regIndex;
        }

        RegIndex lhsRegIndex = new RegIndex(regIndex.value, regIndex.typeTag, true);
        addToRegIndexList(lhsRegIndex);
        return lhsRegIndex;
    }

    private void addToRegIndexList(RegIndex regIndex) {
        if (regIndex.isVarIndex) {
            throw new IllegalStateException("");
        }
        regIndexList.add(regIndex);
    }

    private LocalVariableInfo getLocalVarAttributeInfo(BVarSymbol varSymbol) {
        int varNameCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.name.value);
        int varIndex = varSymbol.varIndex.value;
        int sigCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.type.getDesc());
        return new LocalVariableInfo(varNameCPIndex, sigCPIndex, varIndex);
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
        }
        this.endWorkerInfoUnit(workerInfo.codeAttributeInfo);
        this.emit(InstructionCodes.HALT);
    }

    private void visitInvokableNodeParams(BInvokableSymbol invokableSymbol, CallableUnitInfo callableUnitInfo,
                                          LocalVariableAttributeInfo localVarAttrInfo) {

        // TODO Read param and return param annotations
        invokableSymbol.params.forEach(param -> visitVarSymbol(param, lvIndexes, localVarAttrInfo));
        invokableSymbol.defaultableParams.forEach(param -> visitVarSymbol(param, lvIndexes, localVarAttrInfo));
        if (invokableSymbol.restParam != null) {
            visitVarSymbol(invokableSymbol.restParam, lvIndexes, localVarAttrInfo);
        }

        callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttrInfo);
    }

    private void visitVarSymbol(BVarSymbol varSymbol, VariableIndex variableIndex,
                                LocalVariableAttributeInfo localVarAttrInfo) {
        varSymbol.varIndex = getRegIndexInternal(resolveToSuperType(varSymbol.type).tag, variableIndex.kind);
        LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(varSymbol);
        localVarAttrInfo.localVars.add(localVarInfo);
    }

    private VariableIndex copyVarIndex(VariableIndex that) {
        VariableIndex vIndexes = new VariableIndex(that.kind);
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

        codeAttributeInfo.maxLongRegs = codeAttributeInfo.maxLongLocalVars + maxRegIndexes.tInt + 1;
        codeAttributeInfo.maxDoubleRegs = codeAttributeInfo.maxDoubleLocalVars + maxRegIndexes.tFloat + 1;
        codeAttributeInfo.maxStringRegs = codeAttributeInfo.maxStringLocalVars + maxRegIndexes.tString + 1;
        codeAttributeInfo.maxIntRegs = codeAttributeInfo.maxIntLocalVars + maxRegIndexes.tBoolean + 1;
        codeAttributeInfo.maxByteRegs = codeAttributeInfo.maxByteLocalVars + maxRegIndexes.tBlob + 1;
        codeAttributeInfo.maxRefRegs = codeAttributeInfo.maxRefLocalVars + maxRegIndexes.tRef + 1;

        // Update register indexes.
        for (RegIndex regIndex : regIndexList) {
            switch (regIndex.typeTag) {
                case TypeTags.INT:
                    regIndex.value = regIndex.value + codeAttributeInfo.maxLongLocalVars;
                    break;
                case TypeTags.FLOAT:
                    regIndex.value = regIndex.value + codeAttributeInfo.maxDoubleLocalVars;
                    break;
                case TypeTags.STRING:
                    regIndex.value = regIndex.value + codeAttributeInfo.maxStringLocalVars;
                    break;
                case TypeTags.BOOLEAN:
                    regIndex.value = regIndex.value + codeAttributeInfo.maxIntLocalVars;
                    break;
                case TypeTags.BLOB:
                    regIndex.value = regIndex.value + codeAttributeInfo.maxByteLocalVars;
                    break;
                default:
                    regIndex.value = regIndex.value + codeAttributeInfo.maxRefLocalVars;
                    break;
            }
        }

        regIndexList = new ArrayList<>();
        lvIndexes = new VariableIndex(LOCAL);
        regIndexes = new VariableIndex(REG);
        maxRegIndexes = new VariableIndex(REG);
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

    private int emit(int opcode) {
        currentPkgInfo.instructionList.add(InstructionFactory.get(opcode));
        return currentPkgInfo.instructionList.size();
    }

    private int emit(int opcode, Operand... operands) {
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

    private Operand[] getFuncOperands(BLangInvocation iExpr) {
        int funcRefCPIndex = getFuncRefCPIndex((BInvokableSymbol) iExpr.symbol);
        return getFuncOperands(iExpr, funcRefCPIndex);
    }

    private int getFuncRefCPIndex(BInvokableSymbol invokableSymbol) {
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, invokableSymbol.pkgID);
        int funcNameCPIndex = addUTF8CPEntry(currentPkgInfo, invokableSymbol.name.value);
        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgRefCPIndex, funcNameCPIndex);
        return currentPkgInfo.addCPEntry(funcRefCPEntry);
    }

    private Operand[] getFuncOperands(BLangInvocation iExpr, int funcRefCPIndex) {
        // call funcRefCPIndex, nArgRegs, argRegs[nArgRegs], nRetRegs, retRegs[nRetRegs]
        int i = 0;
        int nArgRegs = iExpr.requiredArgs.size() + iExpr.namedArgs.size() + iExpr.restArgs.size();
        int nRetRegs = 1; // TODO Improve balx format and VM side
        int flags = FunctionFlags.NOTHING;
        Operand[] operands = new Operand[nArgRegs + nRetRegs + 4];
        operands[i++] = getOperand(funcRefCPIndex);
        if (iExpr.async) {
            flags = FunctionFlags.markAsync(flags);
        }
        if (iExpr.actionInvocation) {
            flags = FunctionFlags.markObserved(flags);
        }
        operands[i++] = getOperand(flags);
        operands[i++] = getOperand(nArgRegs);

        // Write required arguments
        for (BLangExpression argExpr : iExpr.requiredArgs) {
            operands[i++] = genNode(argExpr, this.env).regIndex;
        }

        // Write named arguments
        i = generateNamedArgs(iExpr, operands, i);

        // Write rest arguments
        for (BLangExpression argExpr : iExpr.restArgs) {
            operands[i++] = genNode(argExpr, this.env).regIndex;
        }

        // Calculate registers to store return values
        operands[i++] = getOperand(nRetRegs);

        iExpr.regIndex = calcAndGetExprRegIndex(iExpr.regIndex, iExpr.type);
        operands[i] = iExpr.regIndex;
        return operands;
    }

    private int generateNamedArgs(BLangInvocation iExpr, Operand[] operands, int currentIndex) {
        if (iExpr.namedArgs.isEmpty()) {
            return currentIndex;
        }

        PackageInfo pkgInfo = programFile.packageInfoMap.get(iExpr.symbol.pkgID.bvmAlias());

        CallableUnitInfo callableUnitInfo;
        if (iExpr.symbol.kind == SymbolKind.FUNCTION) {
            callableUnitInfo = pkgInfo.functionInfoMap.get(iExpr.symbol.name.value);
        } else {
            throw new IllegalStateException("Unsupported callable unit");
        }

        ParamDefaultValueAttributeInfo defaultValAttrInfo = (ParamDefaultValueAttributeInfo) callableUnitInfo
                .getAttributeInfo(AttributeInfo.Kind.PARAMETER_DEFAULTS_ATTRIBUTE);

        for (int i = 0; i < iExpr.namedArgs.size(); i++) {
            BLangExpression argExpr = iExpr.namedArgs.get(i);
            // If some named parameter is not passed when invoking the function, then it will be null
            // at this point. If so, get the default value for that parameter from the function info.
            if (argExpr == null) {
                DefaultValue defaultVal = defaultValAttrInfo.getDefaultValueInfo()[i];
                argExpr = getDefaultValExpr(defaultVal);
            }
            operands[currentIndex++] = genNode(argExpr, this.env).regIndex;
        }

        return currentIndex;
    }

    private BLangExpression getDefaultValExpr(DefaultValue defaultVal) {
        switch (defaultVal.desc) {
            case TypeDescriptor.SIG_INT:
                return getIntLiteral(defaultVal.intValue);
            case TypeDescriptor.SIG_FLOAT:
                return getFloatLiteral(defaultVal.floatValue);
            case TypeDescriptor.SIG_STRING:
                return getStringLiteral(defaultVal.stringValue);
            case TypeDescriptor.SIG_BOOLEAN:
                return getBooleanLiteral(defaultVal.booleanValue);
            default:
                throw new IllegalStateException("Unsupported default value type");
        }
    }

    private BLangLiteral getStringLiteral(String value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.typeTag = TypeTags.STRING;
        literal.type = symTable.stringType;
        return literal;
    }

    private BLangLiteral getIntLiteral(long value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.typeTag = TypeTags.INT;
        literal.type = symTable.intType;
        return literal;
    }

    private BLangLiteral getFloatLiteral(double value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.typeTag = TypeTags.FLOAT;
        literal.type = symTable.floatType;
        return literal;
    }

    private BLangLiteral getBooleanLiteral(boolean value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.typeTag = TypeTags.BOOLEAN;
        literal.type = symTable.booleanType;
        return literal;
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

    private DefaultValue getDefaultValue(BLangLiteral literalExpr) {
        BType effectiveType = resolveToSuperType(literalExpr.type);
        String desc = effectiveType.getDesc();
        int typeDescCPIndex = addUTF8CPEntry(currentPkgInfo, desc);
        DefaultValue defaultValue = new DefaultValue(typeDescCPIndex, desc);

        int typeTag = effectiveType.tag;
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
            default:
                defaultValue = null;
        }

        return defaultValue;
    }

    private DefaultValueAttributeInfo getDefaultValueAttributeInfo(BLangLiteral literalExpr) {
        DefaultValue defaultValue = getDefaultValue(literalExpr);
        UTF8CPEntry defaultValueAttribUTF8CPEntry =
                new UTF8CPEntry(AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE.toString());
        int defaultValueAttribNameIndex = currentPkgInfo.addCPEntry(defaultValueAttribUTF8CPEntry);

        return new DefaultValueAttributeInfo(defaultValueAttribNameIndex, defaultValue);
    }


    // Create info entries

    private void createPackageVarInfo(BLangVariable varNode) {
        BVarSymbol varSymbol = varNode.symbol;
        varSymbol.varIndex = getPVIndex(varSymbol.type);

        int varNameCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.name.value);
        int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.type.getDesc());
        PackageVarInfo pkgVarInfo = new PackageVarInfo(varNameCPIndex, typeSigCPIndex, varSymbol.flags,
                varSymbol.varIndex.value);
        currentPkgInfo.pkgVarInfoMap.put(varSymbol.name.value, pkgVarInfo);

        LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(varSymbol);
        LocalVariableAttributeInfo pkgVarAttrInfo = (LocalVariableAttributeInfo)
                currentPkgInfo.getAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);
        pkgVarAttrInfo.localVars.add(localVarInfo);

        // TODO Populate annotation attribute
    }

    private void createStructInfoEntry(BLangStruct structNode) {
        BStructSymbol structSymbol = (BStructSymbol) structNode.symbol;
        // Add Struct name as an UTFCPEntry to the constant pool
        int structNameCPIndex = addUTF8CPEntry(currentPkgInfo, structSymbol.name.value);
        StructInfo structInfo = new StructInfo(currentPackageRefCPIndex, structNameCPIndex, structSymbol.flags);
        currentPkgInfo.addStructInfo(structSymbol.name.value, structInfo);
        structInfo.structType = (BStructType) structSymbol.type;

        List<BLangVariable> structFields = structNode.fields;
        for (BLangVariable structField : structFields) {
            // Create StructFieldInfo Entry
            int fieldNameCPIndex = addUTF8CPEntry(currentPkgInfo, structField.name.value);
            int sigCPIndex = addUTF8CPEntry(currentPkgInfo, structField.type.getDesc());

            StructFieldInfo structFieldInfo = new StructFieldInfo(fieldNameCPIndex,
                    sigCPIndex, structField.symbol.flags);
            structFieldInfo.fieldType = structField.type;

            // Populate default values
            if (structField.expr != null && structField.expr.getKind() == NodeKind.LITERAL) {
                DefaultValueAttributeInfo defaultVal = getDefaultValueAttributeInfo((BLangLiteral) structField.expr);
                structFieldInfo.addAttributeInfo(AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE, defaultVal);
            }

            structInfo.fieldInfoEntries.add(structFieldInfo);
            structField.symbol.varIndex = getFieldIndex(structField.symbol.type);
        }

        // Create variable count attribute info
        prepareIndexes(fieldIndexes);
        int[] fieldCount = new int[]{fieldIndexes.tInt, fieldIndexes.tFloat,
                fieldIndexes.tString, fieldIndexes.tBoolean, fieldIndexes.tBlob, fieldIndexes.tRef};
        addVariableCountAttributeInfo(currentPkgInfo, structInfo, fieldCount);
        fieldIndexes = new VariableIndex(FIELD);

        // Create attached function info entries
        for (BAttachedFunction attachedFunc : structSymbol.attachedFuncs) {
            int funcNameCPIndex = addUTF8CPEntry(currentPkgInfo, attachedFunc.funcName.value);

            // Remove the first type. The first type is always the type to which the function is attached to
            BType[] paramTypes = attachedFunc.type.paramTypes.toArray(new BType[0]);
            if (paramTypes.length == 1) {
                paramTypes = new BType[0];
            } else {
                paramTypes = attachedFunc.type.paramTypes.toArray(new BType[0]);
                paramTypes = Arrays.copyOfRange(paramTypes, 1, paramTypes.length);
            }
            int sigCPIndex = addUTF8CPEntry(currentPkgInfo,
                    generateFunctionSig(paramTypes, attachedFunc.type.retType));
            int flags = attachedFunc.symbol.flags;
            structInfo.attachedFuncInfoEntries.add(new AttachedFunctionInfo(funcNameCPIndex, sigCPIndex, flags));
        }
    }

    public void visit(BLangTypeDefinition typeDefinition) {
        //TODO
    }

    public void visit(BLangSingleton singleton) {
        //TODO
    }

    /**
     * Creates a {@code FunctionInfo} from the given function node in AST.
     *
     * @param funcNode function node in AST
     */
    private void createFunctionInfoEntry(BLangFunction funcNode) {
        BInvokableSymbol funcSymbol = funcNode.symbol;
        BInvokableType funcType = (BInvokableType) funcSymbol.type;

        // Add function name as an UTFCPEntry to the constant pool
        int funcNameCPIndex = this.addUTF8CPEntry(currentPkgInfo, funcNode.name.value);

        FunctionInfo funcInfo = new FunctionInfo(currentPackageRefCPIndex, funcNameCPIndex);
        funcInfo.paramTypes = funcType.paramTypes.toArray(new BType[0]);
        populateInvokableSignature(funcType, funcInfo);

        funcInfo.flags = funcSymbol.flags;
        if (funcNode.receiver != null) {
            funcInfo.attachedToTypeCPIndex = getTypeCPIndex(funcNode.receiver.type).value;
        }

        this.addWorkerInfoEntries(funcInfo, funcNode.getWorkers());

        // Add parameter default value info
        addParameterDefaultValues(funcNode, funcInfo);

        this.currentPkgInfo.functionInfoMap.put(funcSymbol.name.value, funcInfo);
    }

    private void createSingletonInfoEntry(BLangSingleton singleton) {
        BTypeSymbol singletonDefSymbol = (BTypeSymbol) singleton.symbol;

        int singletonNameCPIndex = addUTF8CPEntry(currentPkgInfo, singletonDefSymbol.name.value);
        SingletonInfo singletonDefInfo = new SingletonInfo(currentPackageRefCPIndex,
                singletonNameCPIndex, singletonDefSymbol.flags);
        currentPkgInfo.addSingletonInfo(singletonDefSymbol.name.value, singletonDefInfo);
        singletonDefInfo.singletonType = singletonDefSymbol.type;
        singletonDefInfo.valueSpace = getDefaultValue((BLangLiteral) singleton.valueSpace);
    }

    private void createTransformerInfoEntry(BLangInvokableNode invokable) {
        BInvokableSymbol transformerSymbol = invokable.symbol;
        BInvokableType transformerType = (BInvokableType) transformerSymbol.type;

        // Add transformer name as an UTFCPEntry to the constant pool
        int transformerNameCPIndex = this.addUTF8CPEntry(currentPkgInfo, transformerSymbol.name.value);

        TransformerInfo transformerInfo = new TransformerInfo(currentPackageRefCPIndex, transformerNameCPIndex);
        transformerInfo.paramTypes = transformerType.paramTypes.toArray(new BType[0]);
        populateInvokableSignature(transformerType, transformerInfo);

        transformerInfo.retParamTypes = new BType[1];
        transformerInfo.retParamTypes[0] = transformerType.retType;
        transformerInfo.flags = transformerSymbol.flags;

        this.addWorkerInfoEntries(transformerInfo, invokable.getWorkers());

        // Add parameter default value info
        addParameterDefaultValues(invokable, transformerInfo);
        this.currentPkgInfo.transformerInfoMap.put(transformerSymbol.name.value, transformerInfo);
    }

    private void populateInvokableSignature(BInvokableType bInvokableType, CallableUnitInfo callableUnitInfo) {
        if (bInvokableType.retType == symTable.nilType) {
            callableUnitInfo.retParamTypes = new BType[0];
            callableUnitInfo.signatureCPIndex = addUTF8CPEntry(this.currentPkgInfo,
                    generateFunctionSig(callableUnitInfo.paramTypes));
        } else {
            callableUnitInfo.retParamTypes = new BType[1];
            callableUnitInfo.retParamTypes[0] = bInvokableType.retType;
            callableUnitInfo.signatureCPIndex = addUTF8CPEntry(this.currentPkgInfo,
                    generateFunctionSig(callableUnitInfo.paramTypes, bInvokableType.retType));
        }
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

    @Override
    public void visit(BLangEndpoint endpointNode) {
    }

    private void createServiceInfoEntry(BLangService serviceNode) {
        // Add service name as an UTFCPEntry to the constant pool
        int serviceNameCPIndex = addUTF8CPEntry(currentPkgInfo, serviceNode.name.value);
        //Create service info
        if (serviceNode.endpointType != null) {
            String endPointQName = serviceNode.endpointType.tsymbol.toString();
            //TODO: bvmAlias needed?
            int epNameCPIndex = addUTF8CPEntry(currentPkgInfo, endPointQName);
            ServiceInfo serviceInfo = new ServiceInfo(currentPackageRefCPIndex, serviceNameCPIndex,
                    serviceNode.symbol.flags, epNameCPIndex);
            // Add service level variables
            int localVarAttNameIndex = addUTF8CPEntry(currentPkgInfo,
                    AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.value());
            LocalVariableAttributeInfo localVarAttributeInfo = new LocalVariableAttributeInfo(localVarAttNameIndex);
            serviceNode.vars.forEach(var -> visitVarSymbol(var.var.symbol, pvIndexes, localVarAttributeInfo));
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
        resourceInfo.signatureCPIndex = addUTF8CPEntry(currentPkgInfo,
                generateFunctionSig(resourceInfo.paramTypes));
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
        int paramCount = resourceNode.requiredParams.size();
        resourceInfo.paramNameCPIndexes = new int[paramCount];
        for (int i = 0; i < paramCount; i++) {
            BLangVariable paramVar = resourceNode.requiredParams.get(i);
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
        int nameCPIndex = addUTF8CPEntry(pool, pkgID.bvmAlias());
        int versionCPIndex = addUTF8CPEntry(pool, pkgID.version.value);
        PackageRefCPEntry packageRefCPEntry = new PackageRefCPEntry(nameCPIndex, versionCPIndex);
        return pool.addCPEntry(packageRefCPEntry);
    }

    /**
     * Holds the variable index per type.
     *
     * @since 0.94
     */
    static class VariableIndex {
        public enum Kind {
            LOCAL,
            FIELD,
            PACKAGE,
            REG
        }

        int tInt = -1;
        int tFloat = -1;
        int tString = -1;
        int tBoolean = -1;
        int tBlob = -1;
        int tRef = -1;
        Kind kind;

        VariableIndex(Kind kind) {
            this.kind = kind;
        }

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

    public void visit(BLangWorker workerNode) {
        this.genNode(workerNode.body, this.env);
    }

    /* visit the workers within fork-join block */
    private void processJoinWorkers(BLangForkJoin forkJoin, ForkjoinInfo forkjoinInfo,
                                    SymbolEnv forkJoinEnv) {
        UTF8CPEntry codeUTF8CPEntry = new UTF8CPEntry(AttributeInfo.Kind.CODE_ATTRIBUTE.toString());
        int codeAttribNameIndex = this.currentPkgInfo.addCPEntry(codeUTF8CPEntry);
        for (BLangWorker worker : forkJoin.workers) {
            VariableIndex lvIndexesCopy = copyVarIndex(this.lvIndexes);
            this.regIndexes = new VariableIndex(REG);
            VariableIndex regIndexesCopy = this.regIndexes;
            this.regIndexes = new VariableIndex(REG);
            VariableIndex maxRegIndexesCopy = this.maxRegIndexes;
            this.maxRegIndexes = new VariableIndex(REG);
            List<RegIndex> regIndexListCopy = this.regIndexList;
            this.regIndexList = new ArrayList<>();

            WorkerInfo workerInfo = forkjoinInfo.getWorkerInfo(worker.name.value);
            workerInfo.codeAttributeInfo.attributeNameIndex = codeAttribNameIndex;
            workerInfo.codeAttributeInfo.codeAddrs = this.nextIP();
            this.currentWorkerInfo = workerInfo;
            this.genNode(worker.body, forkJoinEnv);
            this.endWorkerInfoUnit(workerInfo.codeAttributeInfo);
            this.emit(InstructionCodes.HALT);

            this.lvIndexes = lvIndexesCopy;
            this.regIndexes = regIndexesCopy;
            this.maxRegIndexes = maxRegIndexesCopy;
            this.regIndexList = regIndexListCopy;
        }
    }

    private void populateForkJoinWorkerInfo(BLangForkJoin forkJoin, ForkjoinInfo forkjoinInfo) {
        for (BLangWorker worker : forkJoin.workers) {
            UTF8CPEntry workerNameCPEntry = new UTF8CPEntry(worker.name.value);
            int workerNameCPIndex = this.currentPkgInfo.addCPEntry(workerNameCPEntry);
            WorkerInfo workerInfo = new WorkerInfo(workerNameCPIndex, worker.name.value);
            forkjoinInfo.addWorkerInfo(worker.name.value, workerInfo);
        }
    }

    /* generate code for Join block */
    private void processJoinBlock(BLangForkJoin forkJoin, ForkjoinInfo forkjoinInfo, SymbolEnv forkJoinEnv,
                                  RegIndex joinVarRegIndex, Operand joinBlockAddr) {
        UTF8CPEntry joinType = new UTF8CPEntry(forkJoin.joinType.name());
        int joinTypeCPIndex = this.currentPkgInfo.addCPEntry(joinType);
        forkjoinInfo.setJoinType(forkJoin.joinType.name());
        forkjoinInfo.setJoinTypeCPIndex(joinTypeCPIndex);
        joinBlockAddr.value = nextIP();

        if (forkJoin.joinResultVar != null) {
            visitForkJoinParameterDefs(forkJoin.joinResultVar, forkJoinEnv);
            joinVarRegIndex.value = forkJoin.joinResultVar.symbol.varIndex.value;
        }

        if (forkJoin.joinedBody != null) {
            this.genNode(forkJoin.joinedBody, forkJoinEnv);
        }
    }

    /* generate code for timeout block */
    private void processTimeoutBlock(BLangForkJoin forkJoin, SymbolEnv forkJoinEnv,
                                     RegIndex timeoutVarRegIndex, Operand timeoutBlockAddr) {
        /* emit a GOTO instruction to jump out of the timeout block */
        Operand gotoAddr = getOperand(-1);
        this.emit(InstructionCodes.GOTO, gotoAddr);
        timeoutBlockAddr.value = nextIP();

        if (forkJoin.timeoutVariable != null) {
            visitForkJoinParameterDefs(forkJoin.timeoutVariable, forkJoinEnv);
            timeoutVarRegIndex.value = forkJoin.timeoutVariable.symbol.varIndex.value;
        }

        if (forkJoin.timeoutBody != null) {
            this.genNode(forkJoin.timeoutBody, forkJoinEnv);
        }
        gotoAddr.value = nextIP();
    }

    public void visit(BLangForkJoin forkJoin) {
        SymbolEnv forkJoinEnv = SymbolEnv.createForkJoinSymbolEnv(forkJoin, this.env);
        ForkjoinInfo forkjoinInfo = new ForkjoinInfo(this.lvIndexes.toArray());
        this.populateForkJoinWorkerInfo(forkJoin, forkjoinInfo);
        int forkJoinInfoIndex = this.forkJoinCount++;
        /* was I already inside a fork/join */
        if (this.env.forkJoin != null) {
            this.currentWorkerInfo.addForkJoinInfo(forkjoinInfo);
        } else {
            this.currentCallableUnitInfo.defaultWorkerInfo.addForkJoinInfo(forkjoinInfo);
        }
        ForkJoinCPEntry forkJoinCPEntry = new ForkJoinCPEntry(forkJoinInfoIndex);
        Operand forkJoinCPIndex = getOperand(this.currentPkgInfo.addCPEntry(forkJoinCPEntry));
        forkjoinInfo.setIndexCPIndex(forkJoinCPIndex.value);

        RegIndex timeoutRegIndex = new RegIndex(-1, TypeTags.INT);
        addToRegIndexList(timeoutRegIndex);
        if (forkJoin.timeoutExpression != null) {
            forkjoinInfo.setTimeoutAvailable(true);
            this.genNode(forkJoin.timeoutExpression, forkJoinEnv);
            timeoutRegIndex.value = forkJoin.timeoutExpression.regIndex.value;
        }

        // FORKJOIN forkJoinCPIndex timeoutRegIndex joinVarRegIndex joinBlockAddr timeoutVarRegIndex timeoutBlockAddr
        RegIndex joinVarRegIndex = new RegIndex(-1, TypeTags.MAP);
        Operand joinBlockAddr = getOperand(-1);
        RegIndex timeoutVarRegIndex = new RegIndex(-1, TypeTags.MAP);
        Operand timeoutBlockAddr = getOperand(-1);
        this.emit(InstructionCodes.FORKJOIN, forkJoinCPIndex, timeoutRegIndex,
                joinVarRegIndex, joinBlockAddr, timeoutVarRegIndex, timeoutBlockAddr);

        this.processJoinWorkers(forkJoin, forkjoinInfo, forkJoinEnv);

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
        this.processJoinBlock(forkJoin, forkjoinInfo, forkJoinEnv, joinVarRegIndex, joinBlockAddr);
        this.processTimeoutBlock(forkJoin, forkJoinEnv, timeoutVarRegIndex, timeoutBlockAddr);
    }

    private void visitForkJoinParameterDefs(BLangVariable parameterDef, SymbolEnv forkJoinEnv) {
        LocalVariableAttributeInfo localVariableAttributeInfo = new LocalVariableAttributeInfo(1);
        parameterDef.symbol.varIndex = getLVIndex(parameterDef.type);
        this.genNode(parameterDef, forkJoinEnv);
        LocalVariableInfo localVariableDetails = this.getLocalVarAttributeInfo(parameterDef.symbol);
        localVariableAttributeInfo.localVars.add(localVariableDetails);
    }

    public void visit(BLangWorkerSend workerSendStmt) {
        WorkerDataChannelInfo workerDataChannelInfo = this.getWorkerDataChannelInfo(this.currentCallableUnitInfo,
                this.currentWorkerInfo.getWorkerName(), workerSendStmt.workerIdentifier.value);
        WorkerDataChannelRefCPEntry wrkrInvRefCPEntry = new WorkerDataChannelRefCPEntry(workerDataChannelInfo
                .getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());
        wrkrInvRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        Operand wrkrInvRefCPIndex = getOperand(currentPkgInfo.addCPEntry(wrkrInvRefCPEntry));
        if (workerSendStmt.isForkJoinSend) {
            this.currentWorkerInfo.setWrkrDtChnlRefCPIndex(wrkrInvRefCPIndex.value);
            this.currentWorkerInfo.setWorkerDataChannelInfoForForkJoin(workerDataChannelInfo);
        }
        workerDataChannelInfo.setDataChannelRefIndex(wrkrInvRefCPIndex.value);

        genNode(workerSendStmt.expr, this.env);
        RegIndex argReg = workerSendStmt.expr.regIndex;
        BType bType = workerSendStmt.expr.type;
        UTF8CPEntry sigCPEntry = new UTF8CPEntry(this.generateSig(new BType[] { bType }));
        Operand sigCPIndex = getOperand(this.currentPkgInfo.addCPEntry(sigCPEntry));

        // WRKSEND wrkrInvRefCPIndex typesCPIndex regIndex
        Operand[] wrkSendArgRegs = new Operand[3];
        wrkSendArgRegs[0] = wrkrInvRefCPIndex;
        wrkSendArgRegs[1] = sigCPIndex;
        wrkSendArgRegs[2] = argReg;
        this.emit(InstructionCodes.WRKSEND, wrkSendArgRegs);
    }

    public void visit(BLangWorkerReceive workerReceiveStmt) {
        WorkerDataChannelInfo workerDataChannelInfo = this.getWorkerDataChannelInfo(this.currentCallableUnitInfo,
                workerReceiveStmt.workerIdentifier.value, this.currentWorkerInfo.getWorkerName());
        WorkerDataChannelRefCPEntry wrkrChnlRefCPEntry = new WorkerDataChannelRefCPEntry(workerDataChannelInfo
                .getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());
        wrkrChnlRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        Operand wrkrRplyRefCPIndex = getOperand(currentPkgInfo.addCPEntry(wrkrChnlRefCPEntry));
        workerDataChannelInfo.setDataChannelRefIndex(wrkrRplyRefCPIndex.value);

        BLangExpression lExpr = workerReceiveStmt.expr;
        RegIndex regIndex;
        BType bType;
        if (lExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF && lExpr instanceof BLangLocalVarRef) {
            lExpr.regIndex = ((BLangLocalVarRef) lExpr).varSymbol.varIndex;
            regIndex = lExpr.regIndex;
        } else {
            lExpr.regIndex = getRegIndex(lExpr.type);
            lExpr.regIndex.isLHSIndex = true;
            regIndex = lExpr.regIndex;
        }
        bType = lExpr.type;

        UTF8CPEntry sigCPEntry = new UTF8CPEntry(this.generateSig(new BType[] { bType }));
        Operand sigCPIndex = getOperand(currentPkgInfo.addCPEntry(sigCPEntry));

        // WRKRECEIVE wrkrRplyRefCPIndex typesCPIndex regIndex
        Operand[] wrkReceiveArgRegs = new Operand[3];
        wrkReceiveArgRegs[0] = wrkrRplyRefCPIndex;
        wrkReceiveArgRegs[1] = sigCPIndex;
        wrkReceiveArgRegs[2] = regIndex;
        emit(InstructionCodes.WRKRECEIVE, wrkReceiveArgRegs);

        if (!(lExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                lExpr instanceof BLangLocalVarRef)) {
            this.varAssignment = true;
            this.genNode(lExpr, this.env);
            this.varAssignment = false;
        }
    }

    public void visit(BLangConnector connectorNode) {
    }

    public void visit(BLangAction actionNode) {
    }

    public void visit(BLangForever foreverStatement) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangStruct structNode) {
    }

    public void visit(BLangObject objectNode) {
        /* ignore */
    }

    public void visit(BLangRecord recordNode) {
        /* ignore */
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
        BLangExpression lhrExpr = assignNode.varRef;
        if (assignNode.declaredWithVar) {
            BLangVariableReference varRef = (BLangVariableReference) lhrExpr;
            visitVarSymbol((BVarSymbol) varRef.symbol, lvIndexes, localVarAttrInfo);
        }

        BLangExpression rhsExpr = assignNode.expr;
        if (lhrExpr.type.tag != TypeTags.NONE && lhrExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                lhrExpr instanceof BLangLocalVarRef) {
            lhrExpr.regIndex = ((BVarSymbol) ((BLangVariableReference) lhrExpr).symbol).varIndex;
            rhsExpr.regIndex = lhrExpr.regIndex;
        }

        genNode(rhsExpr, this.env);
        if (lhrExpr.type.tag == TypeTags.NONE ||
                (lhrExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                        lhrExpr instanceof BLangLocalVarRef)) {
            return;
        }

        varAssignment = true;
        lhrExpr.regIndex = rhsExpr.regIndex;
        genNode(lhrExpr, this.env);
        varAssignment = false;
    }

    public void visit(BLangNext nextNode) {
        generateFinallyInstructions(nextNode, NodeKind.WHILE, NodeKind.FOREACH);
        this.emit(this.loopResetInstructionStack.peek());
    }

    public void visit(BLangBreak breakNode) {
        generateFinallyInstructions(breakNode, NodeKind.WHILE, NodeKind.FOREACH);
        this.emit(this.loopExitInstructionStack.peek());
    }

    public void visit(BLangThrow throwNode) {
        genNode(throwNode.expr, env);
        emit(InstructionFactory.get(InstructionCodes.THROW, throwNode.expr.regIndex));
    }

    public void visit(BLangIf ifNode) {
        addLineNumberInfo(ifNode.pos);

        // Generate code for the if condition evaluation
        genNode(ifNode.expr, this.env);
        Operand ifCondJumpAddr = getOperand(-1);
        emit(InstructionCodes.BR_FALSE, ifNode.expr.regIndex, ifCondJumpAddr);

        // Generate code for the then body
        genNode(ifNode.body, this.env);
        Operand endJumpAddr = getOperand(-1);
        emit(InstructionCodes.GOTO, endJumpAddr);
        ifCondJumpAddr.value = nextIP();

        // Visit else statement if any
        if (ifNode.elseStmt != null) {
            genNode(ifNode.elseStmt, this.env);
        }
        endJumpAddr.value = nextIP();
    }

    public void visit(BLangForeach foreach) {
        // Calculate temporary scope variables for iteration.
        Operand iteratorVar = getLVIndex(TypeTags.ITERATOR);
        Operand conditionVar = getLVIndex(symTable.booleanType);

        // Create new Iterator for given collection.
        this.genNode(foreach.collection, env);
        this.emit(InstructionCodes.ITR_NEW, foreach.collection.regIndex, iteratorVar);

        Operand foreachStartAddress = new Operand(nextIP());
        Operand foreachEndAddress = new Operand(-1);
        Instruction gotoStartInstruction = InstructionFactory.get(InstructionCodes.GOTO, foreachStartAddress);
        Instruction gotoEndInstruction = InstructionFactory.get(InstructionCodes.GOTO, foreachEndAddress);

        // Checks given iterator has a next value.
        this.emit(InstructionCodes.ITR_HAS_NEXT, iteratorVar, conditionVar);
        this.emit(InstructionCodes.BR_FALSE, conditionVar, foreachEndAddress);

        // assign variables.
        generateForeachVarAssignment(foreach, iteratorVar);

        this.loopResetInstructionStack.push(gotoStartInstruction);
        this.loopExitInstructionStack.push(gotoEndInstruction);
        this.genNode(foreach.body, env);                        // generate foreach body.
        this.loopResetInstructionStack.pop();
        this.loopExitInstructionStack.pop();

        this.emit(gotoStartInstruction);  // move to next iteration.
        foreachEndAddress.value = this.nextIP();
    }

    public void visit(BLangWhile whileNode) {
        Instruction gotoTopJumpInstr = InstructionFactory.get(InstructionCodes.GOTO, getOperand(this.nextIP()));
        this.genNode(whileNode.expr, this.env);

        Operand exitLoopJumpAddr = getOperand(-1);
        Instruction exitLoopJumpInstr = InstructionFactory.get(InstructionCodes.GOTO, exitLoopJumpAddr);
        emit(InstructionCodes.BR_FALSE, whileNode.expr.regIndex, exitLoopJumpAddr);

        this.loopResetInstructionStack.push(gotoTopJumpInstr);
        this.loopExitInstructionStack.push(exitLoopJumpInstr);
        this.genNode(whileNode.body, this.env);
        this.loopResetInstructionStack.pop();
        this.loopExitInstructionStack.pop();
        this.emit(gotoTopJumpInstr);

        exitLoopJumpAddr.value = nextIP();
    }

    public void visit(BLangLock lockNode) {
        if (lockNode.lockVariables.isEmpty()) {
            this.genNode(lockNode.body, this.env);
            return;
        }
        Operand gotoLockEndAddr = getOperand(-1);
        Instruction instructGotoLockEnd = InstructionFactory.get(InstructionCodes.GOTO, gotoLockEndAddr);
        Operand[] operands = getOperands(lockNode);
        ErrorTableAttributeInfo errorTable = createErrorTableIfAbsent(currentPkgInfo);

        int fromIP = nextIP();
        emit((InstructionCodes.LOCK), operands);

        this.genNode(lockNode.body, this.env);
        int toIP = nextIP() - 1;

        emit((InstructionCodes.UNLOCK), operands);
        emit(instructGotoLockEnd);

        ErrorTableEntry errorTableEntry = new ErrorTableEntry(fromIP, toIP, nextIP(), 0, -1);
        errorTable.addErrorTableEntry(errorTableEntry);

        emit((InstructionCodes.UNLOCK), operands);
        emit(InstructionFactory.get(InstructionCodes.THROW, getOperand(-1)));
        gotoLockEndAddr.value = nextIP();
    }

    private Operand[] getOperands(BLangLock lockNode) {
        Operand[] operands = new Operand[(lockNode.lockVariables.size() * 2) + 1];
        int i = 0;
        operands[i++] = new Operand(lockNode.lockVariables.size());
        for (BVarSymbol varSymbol : lockNode.lockVariables) {
            int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.getType().getDesc());
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
            operands[i++] = getOperand(currentPkgInfo.addCPEntry(typeRefCPEntry));
            operands[i++] = varSymbol.varIndex;
        }
        return operands;
    }

    public void visit(BLangTransaction transactionNode) {
        ++transactionIndex;
        Operand transactionIndexOperand = getOperand(transactionIndex);
        Operand retryCountRegIndex = new RegIndex(-1, TypeTags.INT);
        if (transactionNode.retryCount != null) {
            this.genNode(transactionNode.retryCount, this.env);
            retryCountRegIndex = transactionNode.retryCount.regIndex;
        }

        Operand committedFuncRegIndex = new RegIndex(-1, TypeTags.INVOKABLE);
        if (transactionNode.onCommitFunction != null) {
            committedFuncRegIndex.value = getFuncRefCPIndex(
                    (BInvokableSymbol) ((BLangFunctionVarRef) transactionNode.onCommitFunction).symbol);
        }

        Operand abortedFuncRegIndex = new RegIndex(-1, TypeTags.INVOKABLE);
        if (transactionNode.onAbortFunction != null) {
            abortedFuncRegIndex.value = getFuncRefCPIndex(
                    (BInvokableSymbol) ((BLangFunctionVarRef) transactionNode.onAbortFunction).symbol);
        }

        ErrorTableAttributeInfo errorTable = createErrorTableIfAbsent(currentPkgInfo);
        Operand transStmtEndAddr = getOperand(-1);
        Operand transStmtAbortEndAddr = getOperand(-1);
        Operand transStmtFailEndAddr = getOperand(-1);
        Instruction gotoAbortTransBlockEnd = InstructionFactory.get(InstructionCodes.GOTO, transStmtAbortEndAddr);
        Instruction gotoFailTransBlockEnd = InstructionFactory.get(InstructionCodes.GOTO, transStmtFailEndAddr);

        abortInstructions.push(gotoAbortTransBlockEnd);
        failInstructions.push(gotoFailTransBlockEnd);

        //start transaction
        this.emit(InstructionCodes.TR_BEGIN, transactionIndexOperand, retryCountRegIndex, committedFuncRegIndex,
                abortedFuncRegIndex);
        Operand transBlockStartAddr = getOperand(nextIP());

        //retry transaction;
        Operand retryEndWithThrowAddr = getOperand(-1);
        Operand retryEndWithNoThrowAddr = getOperand(-1);
        this.emit(InstructionCodes.TR_RETRY, transactionIndexOperand, retryEndWithThrowAddr, retryEndWithNoThrowAddr);

        //process transaction statements
        this.genNode(transactionNode.transactionBody, this.env);

        //end the transaction
        int transBlockEndAddr = nextIP();
        this.emit(InstructionCodes.TR_END, transactionIndexOperand, getOperand(TransactionStatus.SUCCESS.value()));

        abortInstructions.pop();
        failInstructions.pop();

        emit(InstructionCodes.GOTO, transStmtEndAddr);

        // CodeGen for error handling.
        int errorTargetIP = nextIP();
        transStmtFailEndAddr.value = errorTargetIP;
        emit(InstructionCodes.TR_END, transactionIndexOperand, getOperand(TransactionStatus.FAILED.value()));
        if (transactionNode.onRetryBody != null) {
            this.genNode(transactionNode.onRetryBody, this.env);

        }
        emit(InstructionCodes.GOTO, transBlockStartAddr);
        retryEndWithThrowAddr.value = nextIP();
        emit(InstructionCodes.TR_END, transactionIndexOperand, getOperand(TransactionStatus.END.value()));

        emit(InstructionCodes.THROW, getOperand(-1));
        ErrorTableEntry errorTableEntry = new ErrorTableEntry(transBlockStartAddr.value,
                transBlockEndAddr, errorTargetIP, 0, -1);
        errorTable.addErrorTableEntry(errorTableEntry);

        transStmtAbortEndAddr.value = nextIP();
        emit(InstructionCodes.TR_END, transactionIndexOperand, getOperand(TransactionStatus.ABORTED.value()));

        int transactionEndIp = nextIP();
        transStmtEndAddr.value = transactionEndIp;
        retryEndWithNoThrowAddr.value = transactionEndIp;
        emit(InstructionCodes.TR_END, transactionIndexOperand, getOperand(TransactionStatus.END.value()));
    }

    public void visit(BLangAbort abortNode) {
        generateFinallyInstructions(abortNode, NodeKind.TRANSACTION);
        this.emit(abortInstructions.peek());
    }
    
    public void visit(BLangDone doneNode) {
        generateFinallyInstructions(doneNode, NodeKind.DONE);
        this.emit(InstructionCodes.HALT);
    }

    public void visit(BLangFail failNode) {
        generateFinallyInstructions(failNode, NodeKind.TRANSACTION);
        this.emit(failInstructions.peek());
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
        RegIndex lvIndex = getLVIndex(symTable.stringType);
        BLangExpression nsURIExpr = xmlnsNode.namespaceURI;
        nsURIExpr.regIndex = createLHSRegIndex(lvIndex);
        genNode(nsURIExpr, env);

        BXMLNSSymbol nsSymbol = (BXMLNSSymbol) xmlnsNode.symbol;
        nsSymbol.nsURIIndex = lvIndex;
    }

    @Override
    public void visit(BLangPackageXMLNS xmlnsNode) {
        BLangExpression nsURIExpr = xmlnsNode.namespaceURI;
        Operand pvIndex = getPVIndex(symTable.stringType);
        BXMLNSSymbol nsSymbol = (BXMLNSSymbol) xmlnsNode.symbol;
        genNode(nsURIExpr, env);
        nsSymbol.nsURIIndex = pvIndex;
        emit(InstructionCodes.SGSTORE, nsURIExpr.regIndex, pvIndex);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        // If the QName is use outside of XML, treat it as string.
        if (!xmlQName.isUsedInXML) {
            xmlQName.regIndex = calcAndGetExprRegIndex(xmlQName);
            String qName = xmlQName.namespaceURI == null ? xmlQName.localname.value
                    : ("{" + xmlQName.namespaceURI + "}" + xmlQName.localname);
            xmlQName.regIndex = createStringLiteral(qName, xmlQName.regIndex, env);
            return;
        }

        // Else, treat it as QName
        RegIndex nsURIIndex = getNamespaceURIIndex(xmlQName.nsSymbol, env);
        RegIndex localnameIndex = createStringLiteral(xmlQName.localname.value, null, env);
        RegIndex prefixIndex = createStringLiteral(xmlQName.prefix.value, null, env);
        xmlQName.regIndex = calcAndGetExprRegIndex(xmlQName.regIndex, symTable.xmlType);
        emit(InstructionCodes.NEWQNAME, localnameIndex, nsURIIndex, prefixIndex, xmlQName.regIndex);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(xmlAttribute, env);
        BLangExpression attrNameExpr = xmlAttribute.name;
        attrNameExpr.regIndex = calcAndGetExprRegIndex(attrNameExpr);
        genNode(attrNameExpr, xmlAttributeEnv);
        RegIndex attrQNameRegIndex = attrNameExpr.regIndex;

        // If the attribute name is a string representation of qname
        if (attrNameExpr.getKind() != NodeKind.XML_QNAME) {
            RegIndex localNameRegIndex = getRegIndex(symTable.stringType);
            RegIndex uriRegIndex = getRegIndex(symTable.stringType);
            emit(InstructionCodes.S2QNAME, attrQNameRegIndex, localNameRegIndex, uriRegIndex);

            attrQNameRegIndex = getRegIndex(symTable.xmlType);
            generateURILookupInstructions(((BLangXMLElementLiteral) env.node).namespacesInScope, localNameRegIndex,
                    uriRegIndex, attrQNameRegIndex, xmlAttribute.pos, xmlAttributeEnv);
            attrNameExpr.regIndex = attrQNameRegIndex;
        }

        BLangExpression attrValueExpr = xmlAttribute.value;
        genNode(attrValueExpr, env);

        if (xmlAttribute.isNamespaceDeclr) {
            ((BXMLNSSymbol) xmlAttribute.symbol).nsURIIndex = attrValueExpr.regIndex;
        }
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(xmlElementLiteral, env);
        xmlElementLiteral.regIndex = calcAndGetExprRegIndex(xmlElementLiteral);

        // Visit in-line namespace declarations. These needs to be visited first before visiting the 
        // attributes, start and end tag names of the element.
        xmlElementLiteral.inlineNamespaces.forEach(xmlns -> {
            genNode(xmlns, xmlElementEnv);
        });

        // Create start tag name
        BLangExpression startTagName = (BLangExpression) xmlElementLiteral.getStartTagName();
        RegIndex startTagNameRegIndex = visitXMLTagName(startTagName, xmlElementEnv, xmlElementLiteral);

        // Create end tag name. If there is no endtag name (empty XML tag), 
        // then consider start tag name as the end tag name too.
        BLangExpression endTagName = (BLangExpression) xmlElementLiteral.getEndTagName();
        RegIndex endTagNameRegIndex = endTagName == null ? startTagNameRegIndex
                : visitXMLTagName(endTagName, xmlElementEnv, xmlElementLiteral);

        // Create an XML with the given QName
        RegIndex defaultNsURIIndex = getNamespaceURIIndex(xmlElementLiteral.defaultNsSymbol, xmlElementEnv);
        emit(InstructionCodes.NEWXMLELEMENT, xmlElementLiteral.regIndex, startTagNameRegIndex, endTagNameRegIndex,
                defaultNsURIIndex);

        // Add namespaces decelerations visible to this element.
        xmlElementLiteral.namespacesInScope.forEach((name, symbol) -> {
            BLangXMLQName nsQName = new BLangXMLQName(name.getValue(), XMLConstants.XMLNS_ATTRIBUTE);
            genNode(nsQName, xmlElementEnv);
            RegIndex uriIndex = getNamespaceURIIndex(symbol, xmlElementEnv);
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
            emit(InstructionCodes.XMLSEQSTORE, xmlElementLiteral.regIndex, child.regIndex);
        });
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        if (xmlTextLiteral.type == null) {
            xmlTextLiteral.regIndex = calcAndGetExprRegIndex(xmlTextLiteral.regIndex, symTable.xmlType);
        } else {
            xmlTextLiteral.regIndex = calcAndGetExprRegIndex(xmlTextLiteral);
        }
        genNode(xmlTextLiteral.concatExpr, env);
        emit(InstructionCodes.NEWXMLTEXT, xmlTextLiteral.regIndex, xmlTextLiteral.concatExpr.regIndex);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.regIndex = calcAndGetExprRegIndex(xmlCommentLiteral);
        genNode(xmlCommentLiteral.concatExpr, env);
        emit(InstructionCodes.NEWXMLCOMMENT, xmlCommentLiteral.regIndex, xmlCommentLiteral.concatExpr.regIndex);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.regIndex = calcAndGetExprRegIndex(xmlProcInsLiteral);
        genNode(xmlProcInsLiteral.dataConcatExpr, env);
        genNode(xmlProcInsLiteral.target, env);
        emit(InstructionCodes.NEWXMLPI, xmlProcInsLiteral.regIndex, xmlProcInsLiteral.target.regIndex,
                xmlProcInsLiteral.dataConcatExpr.regIndex);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.concatExpr.regIndex = calcAndGetExprRegIndex(xmlQuotedString);
        genNode(xmlQuotedString.concatExpr, env);
        xmlQuotedString.regIndex = xmlQuotedString.concatExpr.regIndex;
    }

    @Override
    public void visit(BLangXMLSequenceLiteral xmlSeqLiteral) {
        xmlSeqLiteral.regIndex = calcAndGetExprRegIndex(xmlSeqLiteral);
        // It is assumed that the sequence is always empty.
        emit(InstructionCodes.NEWXMLSEQ, xmlSeqLiteral.regIndex);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.concatExpr.regIndex = calcAndGetExprRegIndex(stringTemplateLiteral);
        genNode(stringTemplateLiteral.concatExpr, env);
        stringTemplateLiteral.regIndex = stringTemplateLiteral.concatExpr.regIndex;
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(xmlAttributeAccessExpr.expr, this.env);
        RegIndex varRefRegIndex = xmlAttributeAccessExpr.expr.regIndex;

        if (xmlAttributeAccessExpr.indexExpr == null) {
            RegIndex xmlValueRegIndex = calcAndGetExprRegIndex(xmlAttributeAccessExpr);
            emit(InstructionCodes.XML2XMLATTRS, varRefRegIndex, xmlValueRegIndex);
            return;
        }

        BLangExpression indexExpr = xmlAttributeAccessExpr.indexExpr;
        genNode(xmlAttributeAccessExpr.indexExpr, this.env);
        RegIndex qnameRegIndex = xmlAttributeAccessExpr.indexExpr.regIndex;

        // If this is a string representation of qname
        if (indexExpr.getKind() != NodeKind.XML_QNAME) {
            RegIndex localNameRegIndex = getRegIndex(symTable.stringType);
            RegIndex uriRegIndex = getRegIndex(symTable.stringType);
            emit(InstructionCodes.S2QNAME, qnameRegIndex, localNameRegIndex, uriRegIndex);

            qnameRegIndex = getRegIndex(symTable.xmlType);
            generateURILookupInstructions(xmlAttributeAccessExpr.namespaces, localNameRegIndex,
                    uriRegIndex, qnameRegIndex, indexExpr.pos, env);
        }

        if (variableStore) {
            emit(InstructionCodes.XMLATTRSTORE, varRefRegIndex, qnameRegIndex, xmlAttributeAccessExpr.regIndex);
        } else {
            RegIndex xmlValueRegIndex = calcAndGetExprRegIndex(xmlAttributeAccessExpr);
            emit(InstructionCodes.XMLATTRLOAD, varRefRegIndex, qnameRegIndex, xmlValueRegIndex);
        }
    }

    public void visit(BLangTryCatchFinally tryNode) {
        Operand gotoTryCatchEndAddr = getOperand(-1);
        Instruction instructGotoTryCatchEnd = InstructionFactory.get(InstructionCodes.GOTO, gotoTryCatchEndAddr);
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
            StructInfo errorStructInfo = this.programFile.packageInfoMap.get(packageSymbol.pkgID.bvmAlias())
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
            emit(InstructionFactory.get(InstructionCodes.THROW, getOperand(-1)));
        }
        gotoTryCatchEndAddr.value = nextIP();
    }

    public void visit(BLangCatch bLangCatch) {
        // Define local variable index for Error.
        BLangVariable variable = bLangCatch.param;
        RegIndex lvIndex = getLVIndex(variable.symbol.type);
        variable.symbol.varIndex = lvIndex;
        emit(InstructionFactory.get(InstructionCodes.ERRSTORE, lvIndex));

        // Visit Catch Block.
        genNode(bLangCatch.body, env);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        genNode(exprStmtNode.expr, this.env);
    }

    @Override
    public void visit(BLangIntRangeExpression rangeExpr) {
        BLangExpression startExpr = rangeExpr.startExpr;
        BLangExpression endExpr = rangeExpr.endExpr;

        genNode(startExpr, env);
        genNode(endExpr, env);
        rangeExpr.regIndex = calcAndGetExprRegIndex(rangeExpr);

        if (!rangeExpr.includeStart || !rangeExpr.includeEnd) {
            RegIndex const1RegIndex = getRegIndex(symTable.intType);
            emit(InstructionCodes.ICONST_1, const1RegIndex);
            if (!rangeExpr.includeStart) {
                emit(InstructionCodes.IADD, startExpr.regIndex, const1RegIndex, startExpr.regIndex);
            }
            if (!rangeExpr.includeEnd) {
                emit(InstructionCodes.ISUB, endExpr.regIndex, const1RegIndex, endExpr.regIndex);
            }
        }
        emit(InstructionCodes.NEW_INT_RANGE, startExpr.regIndex, endExpr.regIndex, rangeExpr.regIndex);
    }

    // private helper methods of visitors.

    private void generateForeachVarAssignment(BLangForeach foreach, Operand iteratorIndex) {
        List<BLangVariableReference> variables = foreach.varRefs.stream()
                .map(expr -> (BLangVariableReference) expr)
                .collect(Collectors.toList());
        // create Local variable Info entries.
        variables.stream()
                .filter(v -> v.type.tag != TypeTags.NONE)   // Ignoring ignored ("_") variables.
                .forEach(varRef -> visitVarSymbol((BVarSymbol) varRef.symbol, lvIndexes, localVarAttrInfo));
        List<Operand> nextOperands = new ArrayList<>();
        nextOperands.add(iteratorIndex);
        nextOperands.add(new Operand(variables.size()));
        foreach.varTypes.forEach(v -> nextOperands.add(new Operand(v.tag)));
        nextOperands.add(new Operand(variables.size()));
        for (int i = 0; i < variables.size(); i++) {
            BLangVariableReference varRef = variables.get(i);
            nextOperands.add(Optional.ofNullable(((BVarSymbol) varRef.symbol).varIndex)
                    .orElse(getRegIndex(foreach.varTypes.get(i))));
        }
        this.emit(InstructionCodes.ITR_NEXT, nextOperands.toArray(new Operand[0]));
    }

    private void visitFunctionPointerLoad(BLangExpression fpExpr, BInvokableSymbol funcSymbol) {
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, funcSymbol.pkgID);
        int funcNameCPIndex = addUTF8CPEntry(currentPkgInfo, funcSymbol.name.value);
        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgRefCPIndex, funcNameCPIndex);

        int funcRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);
        RegIndex nextIndex = calcAndGetExprRegIndex(fpExpr);
        Operand[] operands;
        if (!(fpExpr instanceof BLangLambdaFunction)) {
            operands = new Operand[3];
            operands[0] = getOperand(funcRefCPIndex);
            operands[1] = nextIndex;
            operands[2] = new Operand(0);
        } else {
            Operand[] closureIndexes = calcAndGetClosureIndexes(((BLangLambdaFunction) fpExpr).function);
            operands = new Operand[2 + closureIndexes.length];
            operands[0] = getOperand(funcRefCPIndex);
            operands[1] = nextIndex;
            System.arraycopy(closureIndexes, 0, operands, 2, closureIndexes.length);
        }
        emit(InstructionCodes.FPLOAD, operands);
    }

    private Operand[] calcAndGetClosureIndexes(BLangFunction function) {
        List<Operand> operands = new ArrayList<>();

        int closureOperandPairs = 0;

        for (BVarSymbol symbol : function.symbol.params) {
            if (!symbol.closure || function.requiredParams.stream().anyMatch(var -> var.symbol.equals(symbol))) {
                continue;
            }
            Operand type = new Operand(symbol.type.tag);
            Operand index = new Operand(symbol.varIndex.value);
            operands.add(type);
            operands.add(index);
            closureOperandPairs++;
        }

        operands.add(0, new Operand(closureOperandPairs));
        return operands.toArray(new Operand[]{});
    }

    private void generateFinallyInstructions(BLangStatement statement) {
        generateFinallyInstructions(statement, new NodeKind[0]);
    }

    private void generateFinallyInstructions(BLangStatement statement, NodeKind... expectedParentKinds) {
        BLangStatement current = statement;
        while (current != null && current.statementLink.parent != null) {
            BLangStatement parent = current.statementLink.parent.statement;
            for (NodeKind expected : expectedParentKinds) {
                if (expected == parent.getKind()) {
                    return;
                }
            }
            if (NodeKind.TRY == parent.getKind()) {
                BLangTryCatchFinally tryCatchFinally = (BLangTryCatchFinally) parent;
                if (tryCatchFinally.finallyBody != null && current != tryCatchFinally.finallyBody) {
                    genNode(tryCatchFinally.finallyBody, env);
                }
            } else if (NodeKind.LOCK == parent.getKind()) {
                BLangLock lockNode = (BLangLock) parent;
                if (!lockNode.lockVariables.isEmpty()) {
                    Operand[] operands = getOperands(lockNode);
                    emit((InstructionCodes.UNLOCK), operands);
                }
            }
            current = parent;
        }
    }

    private RegIndex getNamespaceURIIndex(BXMLNSSymbol namespaceSymbol, SymbolEnv env) {
        if (namespaceSymbol == null && env.node.getKind() == NodeKind.XML_ATTRIBUTE) {
            return createStringLiteral(XMLConstants.NULL_NS_URI, null, env);
        }

        if (namespaceSymbol == null) {
            return createStringLiteral(null, null, env);
        }

        // If the namespace is defined within a callable unit, get the URI index in the local var registry.
        // Otherwise get the URI index in the global var registry.
        if ((namespaceSymbol.owner.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            return (RegIndex) namespaceSymbol.nsURIIndex;
        }

        RegIndex index = getRegIndex(symTable.stringType);
        emit(InstructionCodes.SGLOAD, namespaceSymbol.nsURIIndex, index);
        return index;
    }

    private void generateURILookupInstructions(Map<Name, BXMLNSSymbol> namespaces, RegIndex localNameRegIndex,
                                               RegIndex uriRegIndex, RegIndex targetQNameRegIndex, DiagnosticPos pos,
                                               SymbolEnv symbolEnv) {
        if (namespaces.isEmpty()) {
            createQNameWithoutPrefix(localNameRegIndex, uriRegIndex, targetQNameRegIndex);
            return;
        }

        Stack<Operand> endJumpInstrStack = new Stack<>();
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

            int opcode = getOpcode(symTable.stringType, InstructionCodes.IEQ);
            RegIndex conditionExprIndex = getRegIndex(symTable.booleanType);
            emit(opcode, uriRegIndex, getNamespaceURIIndex(nsSymbol, symbolEnv), conditionExprIndex);

            Operand ifCondJumpAddr = getOperand(-1);
            emit(InstructionCodes.BR_FALSE, conditionExprIndex, ifCondJumpAddr);

            // Below section creates instructions to be executed, if the above condition succeeds (then body)

            // create the prefix literal
            RegIndex prefixIndex = createStringLiteral(prefix, null, env);

            // create a qname
            emit(InstructionCodes.NEWQNAME, localNameRegIndex, uriRegIndex, prefixIndex, targetQNameRegIndex);

            Operand endJumpAddr = getOperand(-1);
            emit(InstructionCodes.GOTO, endJumpAddr);
            endJumpInstrStack.add(endJumpAddr);

            ifCondJumpAddr.value = nextIP();
        }

        // else part. create a qname with empty prefix
        createQNameWithoutPrefix(localNameRegIndex, uriRegIndex, targetQNameRegIndex);

        while (!endJumpInstrStack.isEmpty()) {
            endJumpInstrStack.pop().value = nextIP();
        }
    }

    private void createQNameWithoutPrefix(RegIndex localNameRegIndex, RegIndex uriRegIndex,
                                          RegIndex targetQNameRegIndex) {
        RegIndex prefixIndex = createStringLiteral(null, null, env);
        emit(InstructionCodes.NEWQNAME, localNameRegIndex, uriRegIndex, prefixIndex, targetQNameRegIndex);
    }

    /**
     * Creates a string literal expression, generate the code and returns the registry index.
     *
     * @param value    String value to generate the string literal
     * @param regIndex String literal expression's reg index
     * @param env      Environment
     * @return String registry index of the generated string
     */
    private RegIndex createStringLiteral(String value, RegIndex regIndex, SymbolEnv env) {
        BLangLiteral prefixLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        prefixLiteral.value = value;
        prefixLiteral.typeTag = TypeTags.STRING;
        prefixLiteral.type = symTable.stringType;
        prefixLiteral.regIndex = regIndex;
        genNode(prefixLiteral, env);
        return prefixLiteral.regIndex;
    }

    /**
     * Visit XML tag name and return the index of the tag name in the reference registry.
     *
     * @param tagName           Tag name expression
     * @param xmlElementEnv     Environment of the XML element of the tag
     * @param xmlElementLiteral XML element literal to which the tag name belongs to
     * @return Index of the tag name, in the reference registry
     */
    private RegIndex visitXMLTagName(BLangExpression tagName, SymbolEnv xmlElementEnv,
                                     BLangXMLElementLiteral xmlElementLiteral) {
        genNode(tagName, xmlElementEnv);
        RegIndex startTagNameRegIndex = tagName.regIndex;

        // If this is a string representation of element name, generate the namespace lookup instructions
        if (tagName.getKind() != NodeKind.XML_QNAME) {
            RegIndex localNameRegIndex = getRegIndex(symTable.stringType);
            RegIndex uriRegIndex = getRegIndex(symTable.stringType);
            emit(InstructionCodes.S2QNAME, startTagNameRegIndex, localNameRegIndex, uriRegIndex);

            startTagNameRegIndex = getRegIndex(symTable.xmlType);
            generateURILookupInstructions(xmlElementLiteral.namespacesInScope, localNameRegIndex, uriRegIndex,
                    startTagNameRegIndex, xmlElementLiteral.pos, xmlElementEnv);
            tagName.regIndex = startTagNameRegIndex;
        }

        return startTagNameRegIndex;
    }

    /**
     * Get the constant pool entry index of a given type.
     *
     * @param type Type to get the constant pool entry index
     * @return constant pool entry index of the type
     */
    private Operand getTypeCPIndex(BType type) {
        int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, type.getDesc());
        TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
        return getOperand(currentPkgInfo.addCPEntry(typeRefCPEntry));
    }

    private void addParameterDefaultValues(BLangInvokableNode invokableNode, CallableUnitInfo callableUnitInfo) {
        int paramDefaultsAttrNameIndex =
                addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.PARAMETER_DEFAULTS_ATTRIBUTE.value());
        ParamDefaultValueAttributeInfo paramDefaulValAttrInfo =
                new ParamDefaultValueAttributeInfo(paramDefaultsAttrNameIndex);

        // Only named parameters can have default values.
        for (BLangVariableDef param : invokableNode.defaultableParams) {
            DefaultValue defaultVal = getDefaultValue((BLangLiteral) param.var.expr);
            paramDefaulValAttrInfo.addParamDefaultValueInfo(defaultVal);
        }

        callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.PARAMETER_DEFAULTS_ATTRIBUTE, paramDefaulValAttrInfo);
    }

    private int getValueToRefTypeCastOpcode(BType type) {
        return getValueToRefTypeCastOpcode(resolveToSuperType(type).tag);
    }

    private int getValueToRefTypeCastOpcode(int typeTag) {
        int opcode;
        switch (typeTag) {
            case TypeTags.INT:
                opcode = InstructionCodes.I2ANY;
                break;
            case TypeTags.FLOAT:
                opcode = InstructionCodes.F2ANY;
                break;
            case TypeTags.STRING:
                opcode = InstructionCodes.S2ANY;
                break;
            case TypeTags.BOOLEAN:
                opcode = InstructionCodes.B2ANY;
                break;
            case TypeTags.BLOB:
                opcode = InstructionCodes.L2ANY;
                break;
            default:
                opcode = InstructionCodes.NOP;
                break;
        }
        return opcode;
    }

    private int getRefToValueTypeCastOpcode(BType type) {
        return getRefToValueTypeCastOpcode(resolveToSuperType(type).tag);
    }

    private int getRefToValueTypeCastOpcode(int typeTag) {
        int opcode;
        switch (typeTag) {
            case TypeTags.INT:
                opcode = InstructionCodes.ANY2I;
                break;
            case TypeTags.FLOAT:
                opcode = InstructionCodes.ANY2F;
                break;
            case TypeTags.STRING:
                opcode = InstructionCodes.ANY2S;
                break;
            case TypeTags.BOOLEAN:
                opcode = InstructionCodes.ANY2B;
                break;
            case TypeTags.BLOB:
                opcode = InstructionCodes.ANY2L;
                break;
            default:
                opcode = InstructionCodes.NOP;
                break;
        }
        return opcode;
    }

}

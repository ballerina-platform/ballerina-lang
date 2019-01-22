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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.FunctionFlags;
import org.ballerinalang.util.Transactions;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.TaintRecord;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangTupleAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangAttachedFunctionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangBuiltInMethodInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangChannelLiteral;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangTypeLoad;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.CompilerUtils;
import org.wso2.ballerinalang.compiler.util.Constants;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.AnnotationInfo;
import org.wso2.ballerinalang.programfile.CallableUnitInfo;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.PackageFile;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile;
import org.wso2.ballerinalang.programfile.ConstantInfo;
import org.wso2.ballerinalang.programfile.DefaultValue;
import org.wso2.ballerinalang.programfile.ErrorTableEntry;
import org.wso2.ballerinalang.programfile.FiniteTypeInfo;
import org.wso2.ballerinalang.programfile.FunctionInfo;
import org.wso2.ballerinalang.programfile.ImportPackageInfo;
import org.wso2.ballerinalang.programfile.Instruction;
import org.wso2.ballerinalang.programfile.Instruction.Operand;
import org.wso2.ballerinalang.programfile.Instruction.RegIndex;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.programfile.InstructionFactory;
import org.wso2.ballerinalang.programfile.LabelTypeInfo;
import org.wso2.ballerinalang.programfile.LineNumberInfo;
import org.wso2.ballerinalang.programfile.LocalVariableInfo;
import org.wso2.ballerinalang.programfile.ObjectTypeInfo;
import org.wso2.ballerinalang.programfile.PackageInfo;
import org.wso2.ballerinalang.programfile.PackageInfoWriter;
import org.wso2.ballerinalang.programfile.PackageVarInfo;
import org.wso2.ballerinalang.programfile.RecordTypeInfo;
import org.wso2.ballerinalang.programfile.ServiceInfo;
import org.wso2.ballerinalang.programfile.StructFieldInfo;
import org.wso2.ballerinalang.programfile.TypeDefInfo;
import org.wso2.ballerinalang.programfile.ValueSpaceItemInfo;
import org.wso2.ballerinalang.programfile.WorkerDataChannelInfo;
import org.wso2.ballerinalang.programfile.WorkerInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfoPool;
import org.wso2.ballerinalang.programfile.attributes.CodeAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DocumentationAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DocumentationAttributeInfo.ParameterDocumentInfo;
import org.wso2.ballerinalang.programfile.attributes.ErrorTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LineNumberTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LocalVariableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ParamDefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ParameterAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.TaintTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.VarTypeCountAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.WorkerSendInsAttributeInfo;
import org.wso2.ballerinalang.programfile.cpentries.BlobCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ByteCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPool;
import org.wso2.ballerinalang.programfile.cpentries.FloatCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.IntegerCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.PackageRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StringCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StructureRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.TypeRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.UTF8CPEntry;
import org.wso2.ballerinalang.programfile.cpentries.WorkerDataChannelRefCPEntry;
import org.wso2.ballerinalang.util.Flags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;

import static org.wso2.ballerinalang.compiler.codegen.CodeGenerator.VariableIndex.Kind.FIELD;
import static org.wso2.ballerinalang.compiler.codegen.CodeGenerator.VariableIndex.Kind.LOCAL;
import static org.wso2.ballerinalang.compiler.codegen.CodeGenerator.VariableIndex.Kind.PACKAGE;
import static org.wso2.ballerinalang.compiler.codegen.CodeGenerator.VariableIndex.Kind.REG;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BOOL_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BYTE_NEGATIVE_OFFSET;
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

    /**
     * This structure holds child scopes of a given scope.
     */
    private Map<String, Stack<String>> childScopesMap = new HashMap<>();

    private SymbolEnv env;
    // TODO Remove this dependency from the code generator
    private final SymbolTable symTable;
    private final PackageCache packageCache;

    private PackageInfo currentPkgInfo;
    private PackageID currentPkgID;
    private int currentPackageRefCPIndex;

    private LineNumberTableAttributeInfo lineNoAttrInfo;
    private CallableUnitInfo currentCallableUnitInfo;
    private LocalVariableAttributeInfo localVarAttrInfo;
    private WorkerInfo currentWorkerInfo;

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
    private Stack<Integer> tryCatchErrorRangeFromIPStack = new Stack<>();
    private Stack<Integer> tryCatchErrorRangeToIPStack = new Stack<>();
    private Stack<RegIndex> abortedFromStatus = new Stack<>();
    private static CompilerOptions options;
    private int workerChannelCount = 0;

    public static CodeGenerator getInstance(CompilerContext context) {
        CodeGenerator codeGenerator = context.get(CODE_GENERATOR_KEY);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }
        options = CompilerOptions.getInstance(context);
        return codeGenerator;
    }

    public CodeGenerator(CompilerContext context) {
        context.put(CODE_GENERATOR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);
    }

    public ProgramFile generateBALX(BLangPackage pkgNode) {
        ProgramFile programFile = new ProgramFile();

        // Add all the packages to the program file structure.
        addPackageInfo(pkgNode.symbol, programFile);
        programFile.entryPkgCPIndex = addPackageRefCPEntry(programFile, pkgNode.symbol.pkgID);
        // TODO Remove the following line..
        setEntryPoints(programFile, pkgNode);
        return programFile;
    }

    public BLangPackage generateBALO(BLangPackage pkgNode) {
        // Reset package level variable indexes.
        this.pvIndexes = new VariableIndex(VariableIndex.Kind.PACKAGE);
        // Generate code for the given package.
        this.currentPkgInfo = new PackageInfo();
        // Add current package info to currentPackageInfo object
        addPkgDetailsToPkgInfoObj(pkgNode);
        generatePkgNode(pkgNode);

        // Generate program file for the Testable package
        pkgNode.getTestablePkgs().forEach(this::generatePkgNode);
        this.currentPackageRefCPIndex = -1;
        this.currentPkgID = null;
        this.currentPkgInfo = null;
        return pkgNode;
    }

    private void generatePkgNode(BLangPackage pkgNode) {
        genNode(pkgNode, this.symTable.pkgEnvMap.get(pkgNode.symbol));
        // Add global variable indexes to the ProgramFile
        prepareIndexes(this.pvIndexes);
        // Create Global variable attribute info
        addVarCountAttrInfo(this.currentPkgInfo, this.currentPkgInfo, pvIndexes);
        pkgNode.symbol.packageFile = new PackageFile(getPackageBinaryContent(pkgNode));
        setEntryPoints(pkgNode.symbol.packageFile, pkgNode);
    }

    private void setEntryPoints(CompiledBinaryFile compiledBinaryFile, BLangPackage pkgNode) {
        BLangFunction mainFunc = getMainFunction(pkgNode);
        if (mainFunc != null) {
            compiledBinaryFile.setMainEPAvailable(true);
            pkgNode.symbol.entryPointExists = true;
        }

        if (pkgNode.services.size() != 0) {
            compiledBinaryFile.setServiceEPAvailable(true);
            pkgNode.symbol.entryPointExists = true;
        }
    }

    private BLangFunction getMainFunction(BLangPackage pkgNode) {
        for (BLangFunction funcNode : pkgNode.functions) {
            if (CompilerUtils.isMainFunction(funcNode)) {
                return funcNode;
            }
        }
        return null;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.CODE_GEN)) {
            return;
        }
        // Visit imports
        visitImports(pkgNode);
        // Visit top level constructs
        visitTopLevelNodes(pkgNode);
        // Visit the builtin functions
        visitBuiltinFunctions(pkgNode, pkgNode.initFunction);
        visitBuiltinFunctions(pkgNode, pkgNode.startFunction);
        visitBuiltinFunctions(pkgNode, pkgNode.stopFunction);

        // We don't need to visit constants since we don't do any code generation for constants.
        pkgNode.topLevelNodes.stream()
                .filter(pkgLevelNode -> pkgLevelNode.getKind() != NodeKind.CONSTANT)
                .filter(pkgLevelNode -> pkgLevelNode.getKind() != NodeKind.VARIABLE &&
                        pkgLevelNode.getKind() != NodeKind.XMLNS)
                .forEach(pkgLevelNode -> genNode((BLangNode) pkgLevelNode, this.env));
        // Add function symbol for all functions
        pkgNode.functions.forEach(funcNode -> {
            funcNode.symbol = funcNode.originalFuncSymbol;
        });
        currentPkgInfo.addAttributeInfo(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE, lineNoAttrInfo);
        pkgNode.completedPhases.add(CompilerPhase.CODE_GEN);
    }

    /**
     * Add current package info.
     *
     * @param pkgNode package nod
     */
    private void addPkgDetailsToPkgInfoObj(BLangPackage pkgNode) {
        // Add the current package to the program file
        BPackageSymbol pkgSymbol = pkgNode.symbol;
        currentPkgID = pkgSymbol.pkgID;
        currentPkgInfo.orgNameCPIndex = addUTF8CPEntry(currentPkgInfo, currentPkgID.orgName.value);
        currentPkgInfo.nameCPIndex = addUTF8CPEntry(currentPkgInfo, currentPkgID.name.value);
        currentPkgInfo.versionCPIndex = addUTF8CPEntry(currentPkgInfo, currentPkgID.version.value);

        // Insert the package reference to the constant pool of the current package
        currentPackageRefCPIndex = addPackageRefCPEntry(currentPkgInfo, currentPkgID);

        // This attribute keep track of line numbers
        int lineNoAttrNameIndex = addUTF8CPEntry(currentPkgInfo,
                                                 AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE.value());
        lineNoAttrInfo = new LineNumberTableAttributeInfo(lineNoAttrNameIndex);

        // This attribute keep package-level variable information
        int pkgVarAttrNameIndex = addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE
                .value());
        currentPkgInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE,
                                        new LocalVariableAttributeInfo(pkgVarAttrNameIndex));
    }

    /**
     * Visit imports.
     *
     * @param pkgNode package node
     */
    private void visitImports(BLangPackage pkgNode) {
        pkgNode.imports.forEach(impPkgNode -> {
            int impPkgOrgNameCPIndex = addUTF8CPEntry(this.currentPkgInfo, impPkgNode.symbol.pkgID.orgName.value);
            int impPkgNameCPIndex = addUTF8CPEntry(this.currentPkgInfo, impPkgNode.symbol.pkgID.name.value);
            int impPkgVersionCPIndex = addUTF8CPEntry(this.currentPkgInfo, impPkgNode.symbol.pkgID.version.value);
            ImportPackageInfo importPkgInfo =
                    new ImportPackageInfo(impPkgOrgNameCPIndex, impPkgNameCPIndex, impPkgVersionCPIndex);
            this.currentPkgInfo.importPkgInfoSet.add(importPkgInfo);
        });
    }

    /**
     * Visit top level constructs.
     *
     * @param pkgNode package node
     */
    private void visitTopLevelNodes(BLangPackage pkgNode) {
        pkgNode.constants.forEach(this::createConstantInfo);
        pkgNode.globalVars.forEach(this::createPackageVarInfo);
        pkgNode.typeDefinitions.forEach(this::createTypeDefinitionInfoEntry);
        pkgNode.annotations.forEach(this::createAnnotationInfoEntry);
        pkgNode.functions.forEach(this::createFunctionInfoEntry);
        pkgNode.services.forEach(this::createServiceInfoEntry);
    }

    private void visitBuiltinFunctions(BLangPackage pkgNode, BLangFunction function) {
        if (Symbols.isFlagOn(pkgNode.symbol.flags, Flags.TESTABLE)) {
            String funcName = function.getName().value;
            String builtinFuncName = funcName.substring(funcName.indexOf("<") + 1, funcName.indexOf(">"));
            String modifiedFuncName = funcName.replace(builtinFuncName, "test" + builtinFuncName);
            function.name.setValue(modifiedFuncName);
            function.originalFuncSymbol.name.value = modifiedFuncName;
            function.symbol.name.value = modifiedFuncName;
        }
        createFunctionInfoEntry(function);
        genNode(function, this.env);
    }

    public void visit(BLangService serviceNode) {
        /* Ignore */
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

    public void visit(BLangSimpleVariable varNode) {
        BVarSymbol varSymbol = varNode.symbol;
        int ownerSymTag = env.scope.owner.tag;
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            varSymbol.varIndex = getLVIndex(varSymbol.type.tag);
            LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(varSymbol);
            setVariableScopeStart(localVarInfo, varNode);
            setVariableScopeEnd(localVarInfo, varNode);
            localVarInfo.isIdentifierLiteral = varNode.name.isLiteral;
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

    // Statements

    public void visit(BLangSimpleVariableDef varDefNode) {
        genNode(varDefNode.var, this.env);
    }

    @Override
    public void visit(BLangMatch matchStmt) {
        // TODO
    }

    public void visit(BLangReturn returnNode) {
        BLangExpression expr = returnNode.expr;
        this.genNode(expr, this.env);
        emit(this.typeTagToInstr(expr.type.tag), expr.regIndex);

        generateFinallyInstructions(returnNode);
        emit(InstructionCodes.RET);
    }

    private int typeTagToInstr(int typeTag) {
        switch (typeTag) {
            case TypeTags.INT:
                return InstructionCodes.IRET;
            case TypeTags.BYTE:
                return InstructionCodes.BRET;
            case TypeTags.FLOAT:
                return InstructionCodes.FRET;
            case TypeTags.DECIMAL:
                return InstructionCodes.DRET;
            case TypeTags.STRING:
                return InstructionCodes.SRET;
            case TypeTags.BOOLEAN:
                return InstructionCodes.BRET;
            default:
                return InstructionCodes.RRET;
        }
    }


    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
        int opcode;
        Operand regIndex = calcAndGetExprRegIndex(literalExpr);
        int typeTag = literalExpr.type.tag;

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

            case TypeTags.BYTE:
                byte byteVal = (Byte) literalExpr.value;
                int byteCPEntryIndex = currentPkgInfo.addCPEntry(new ByteCPEntry(byteVal));
                emit(InstructionCodes.BICONST, getOperand(byteCPEntryIndex), regIndex);
                break;

            case TypeTags.FLOAT:
                // TODO:Remove the instanceof check by converting the float literal instance in Semantic analysis phase
                double doubleVal = literalExpr.value instanceof String ?
                        Double.parseDouble((String) literalExpr.value) :
                        (Double) literalExpr.value;
                if (doubleVal == 0 || doubleVal == 1 || doubleVal == 2 ||
                        doubleVal == 3 || doubleVal == 4 || doubleVal == 5) {
                    opcode = InstructionCodes.FCONST_0 + (int) doubleVal;
                    emit(opcode, regIndex);
                } else {
                    int floatCPEntryIndex = currentPkgInfo.addCPEntry(new FloatCPEntry(doubleVal));
                    emit(InstructionCodes.FCONST, getOperand(floatCPEntryIndex), regIndex);
                }
                break;

            case TypeTags.DECIMAL:
                String decimalVal = (String) literalExpr.value;
                int decimalEntryIndex = currentPkgInfo.addCPEntry(new UTF8CPEntry(decimalVal));
                emit(InstructionCodes.DCONST, getOperand(decimalEntryIndex), regIndex);
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

            case TypeTags.ARRAY:
                if (TypeTags.BYTE == ((BArrayType) literalExpr.type).eType.tag) {
                    BlobCPEntry blobCPEntry = new BlobCPEntry((byte[]) literalExpr.value);
                    int blobCPIndex = currentPkgInfo.addCPEntry(blobCPEntry);
                    emit(InstructionCodes.BACONST, getOperand(blobCPIndex), regIndex);
                }
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
        int opcode = getOpcodeForArrayOperations(etype.tag, InstructionCodes.INEWARRAY);
        Operand arrayVarRegIndex = calcAndGetExprRegIndex(arrayLiteral);
        Operand typeCPIndex = getTypeCPIndex(arrayLiteral.type);

        long size = arrayLiteral.type.tag == TypeTags.ARRAY &&
                ((BArrayType) arrayLiteral.type).state != BArrayState.UNSEALED ?
                (long) ((BArrayType) arrayLiteral.type).size : -1L;
        BLangLiteral arraySizeLiteral = generateIntegerLiteralNode(arrayLiteral, size);

        emit(opcode, arrayVarRegIndex, typeCPIndex, arraySizeLiteral.regIndex);

        // Emit instructions populate initial array values;
        for (int i = 0; i < arrayLiteral.exprs.size(); i++) {
            BLangExpression argExpr = arrayLiteral.exprs.get(i);
            genNode(argExpr, this.env);

            BLangLiteral indexLiteral = new BLangLiteral();
            indexLiteral.pos = arrayLiteral.pos;
            indexLiteral.value = (long) i;
            indexLiteral.type = symTable.intType;
            genNode(indexLiteral, this.env);

            opcode = getOpcodeForArrayOperations(argExpr.type.tag, InstructionCodes.IASTORE);
            emit(opcode, arrayVarRegIndex, indexLiteral.regIndex, argExpr.regIndex);
        }
    }

    @Override
    public void visit(BLangJSONArrayLiteral arrayLiteral) {
        // Emit create array instruction
        int opcode = getOpcodeForArrayOperations(arrayLiteral.type.tag, InstructionCodes.INEWARRAY);
        Operand arrayVarRegIndex = calcAndGetExprRegIndex(arrayLiteral);
        Operand typeCPIndex = getTypeCPIndex(arrayLiteral.type);

        long size = arrayLiteral.type.tag == TypeTags.ARRAY &&
                ((BArrayType) arrayLiteral.type).state != BArrayState.UNSEALED ?
                (long) ((BArrayType) arrayLiteral.type).size : -1L;
        BLangLiteral arraySizeLiteral = generateIntegerLiteralNode(arrayLiteral, size);

        emit(opcode, arrayVarRegIndex, typeCPIndex, arraySizeLiteral.regIndex);

        for (int i = 0; i < arrayLiteral.exprs.size(); i++) {
            BLangExpression argExpr = arrayLiteral.exprs.get(i);
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
        emit(InstructionCodes.NEWMAP, jsonLiteral.regIndex, typeCPIndex);

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

            int opcode = getValueToRefTypeCastOpcode(mapType.constraint.tag);
            if (opcode == InstructionCodes.NOP) {
                emit(InstructionCodes.MAPSTORE, mapVarRegIndex, keyExpr.regIndex, valueExpr.regIndex);
            } else {
                RegIndex refRegMapValue = getRegIndex(TypeTags.ANY);
                emit(opcode, valueExpr.regIndex, refRegMapValue);
                emit(InstructionCodes.MAPSTORE, mapVarRegIndex, keyExpr.regIndex, refRegMapValue);
            }
        }
    }

    @Override
    public void visit(BLangStructLiteral structLiteral) {
        BRecordTypeSymbol structSymbol = (BRecordTypeSymbol) structLiteral.type.tsymbol;
        int pkgCPIndex = addPackageRefCPEntry(currentPkgInfo, structSymbol.pkgID);
        int structNameCPIndex = addUTF8CPEntry(currentPkgInfo, structSymbol.name.value);
        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, structNameCPIndex);
        Operand structCPIndex = getOperand(currentPkgInfo.addCPEntry(structureRefCPEntry));

        // Emit an instruction to create a new record.
        RegIndex structRegIndex = calcAndGetExprRegIndex(structLiteral);
        emit(InstructionCodes.NEWSTRUCT, structCPIndex, structRegIndex);

        // Invoke the struct default values init function here.
        if (structSymbol.defaultsValuesInitFunc != null) {
            int funcRefCPIndex = getFuncRefCPIndex(structSymbol.defaultsValuesInitFunc.symbol);
            // call funcRefCPIndex 1 structRegIndex 0
            Operand[] operands = new Operand[6];
            operands[0] = getOperand(funcRefCPIndex);
            operands[1] = getOperand(false);
            operands[2] = getOperand(1);
            operands[3] = structRegIndex;
            // Earlier, init function did not return any value. But now all functions should return a value. So we add
            // new two operands to indicate the return value of the init function. The first one is the number of
            // return values and the second one is the type of the return value.
            operands[4] = getOperand(1);
            operands[5] = getRegIndex(TypeTags.NIL);
            emit(InstructionCodes.CALL, operands);
        }

        // Invoke the struct initializer here.
        if (structLiteral.initializer != null) {
            int funcRefCPIndex = getFuncRefCPIndex(structLiteral.initializer.symbol);
            // call funcRefCPIndex 1 structRegIndex 0
            Operand[] operands = new Operand[6];
            operands[0] = getOperand(funcRefCPIndex);
            operands[1] = getOperand(false);
            operands[2] = getOperand(1);
            operands[3] = structRegIndex;
            operands[4] = getOperand(1);
            operands[5] = getRegIndex(TypeTags.NIL);
            emit(InstructionCodes.CALL, operands);
        }

        // Generate code the struct literal.
        for (BLangRecordKeyValue keyValue : structLiteral.keyValuePairs) {
            BLangRecordKey key = keyValue.key;
            genNode(key.expr, this.env);

            genNode(keyValue.valueExpr, this.env);
            storeStructField(keyValue.valueExpr, structRegIndex, key.expr.regIndex);
        }
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        Operand mapVarRegIndex = calcAndGetExprRegIndex(waitLiteral);
        Operand typeCPIndex = getTypeCPIndex(waitLiteral.type);
        emit(InstructionCodes.NEWMAP, mapVarRegIndex, typeCPIndex);

        // Create a list to store the operands
        List<Operand> operands = new ArrayList<>();
        Operand length = this.getOperand(-1);
        operands.add(length);
        // Add the map index
        operands.add(typeCPIndex);
        operands.add(mapVarRegIndex);
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValue : waitLiteral.keyValuePairs) {
            // Get index of the identifier
            UTF8CPEntry waitKeyCPEntry = new UTF8CPEntry(keyValue.key.value);
            Operand waitKeyCPIndex = getOperand(currentPkgInfo.addCPEntry(waitKeyCPEntry));

            // Get index of the expression
            BLangExpression expr = keyValue.valueExpr != null ? keyValue.valueExpr : keyValue.keyExpr;
            genNode(expr, this.env);

            operands.add(waitKeyCPIndex);
            operands.add(expr.regIndex);
        }
        length.value = operands.size() - 3;
        // length of operands, type of wait, regIndex of map, exprs: [regIndex of key, regIndex of expr]
        this.emit(InstructionCodes.WAITALL, operands.toArray(new Operand[operands.size()]));
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        List<Operand> operands = new ArrayList<>();
        RegIndex exprIndex = calcAndGetExprRegIndex(workerFlushExpr);
        operands.add(exprIndex);
        operands.add(getOperand(workerFlushExpr.workerIdentifierList.size()));
        for (BLangIdentifier wrkrName : workerFlushExpr.workerIdentifierList) {
            WorkerDataChannelInfo workerDataChannelInfo = this.getWorkerDataChannelInfo
                    (this.currentCallableUnitInfo, this.currentWorkerInfo.getWorkerName(), wrkrName.value);
            WorkerDataChannelRefCPEntry wrkrInvRefCPEntry = new WorkerDataChannelRefCPEntry(
                    workerDataChannelInfo.getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());
            wrkrInvRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
            Operand wrkrInvRefCPIndex = getOperand(currentPkgInfo.addCPEntry(wrkrInvRefCPEntry));
            operands.add(wrkrInvRefCPIndex);
        }
        emit(InstructionCodes.FLUSH, operands.toArray(new Operand[operands.size()]));
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        tableLiteral.regIndex = calcAndGetExprRegIndex(tableLiteral);
        Operand typeCPIndex = getTypeCPIndex(tableLiteral.type);
        ArrayList<BLangExpression> dataRows = new ArrayList<>();
        for (int i = 0; i < tableLiteral.tableDataRows.size(); i++) {
            BLangExpression dataRowExpr = tableLiteral.tableDataRows.get(i);
            genNode(dataRowExpr, this.env);
            dataRows.add(dataRowExpr);
        }
        BLangArrayLiteral arrayLiteral = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        arrayLiteral.exprs = dataRows;
        arrayLiteral.type = new BArrayType(symTable.anyType);
        genNode(arrayLiteral, this.env);
        genNode(tableLiteral.indexColumnsArrayLiteral, this.env);
        genNode(tableLiteral.keyColumnsArrayLiteral, this.env);
        emit(InstructionCodes.NEWTABLE, tableLiteral.regIndex, typeCPIndex,
                tableLiteral.indexColumnsArrayLiteral.regIndex, tableLiteral.keyColumnsArrayLiteral.regIndex,
                arrayLiteral.regIndex);
    }

    @Override
    public void visit(BLangStreamLiteral streamLiteral) {
        streamLiteral.regIndex = calcAndGetExprRegIndex(streamLiteral);
        Operand typeCPIndex = getTypeCPIndex(streamLiteral.type);
        StringCPEntry nameCPEntry =
                new StringCPEntry(addUTF8CPEntry(currentPkgInfo, streamLiteral.streamName), streamLiteral.streamName);
        Operand nameCPIndex = getOperand(currentPkgInfo.addCPEntry(nameCPEntry));
        emit(InstructionCodes.NEWSTREAM, streamLiteral.regIndex, typeCPIndex, nameCPIndex);
    }

    @Override
    public void visit(BLangChannelLiteral channelLiteral) {
        channelLiteral.regIndex = calcAndGetExprRegIndex(channelLiteral);
    }

    @Override
    public void visit(BLangLocalVarRef localVarRef) {
        if (localVarRef.regIndex != null && (localVarRef.regIndex.isLHSIndex || localVarRef.regIndex.isVarIndex)) {
            emit(getOpcode(localVarRef.type.tag, InstructionCodes.IMOVE),
                    localVarRef.varSymbol.varIndex, localVarRef.regIndex);
            return;
        }

        localVarRef.regIndex = localVarRef.varSymbol.varIndex;
    }

    @Override
    public void visit(BLangFieldVarRef fieldVarRef) {
        final int except = 0; // signals not to throw an exception in case the field could not be found
        String fieldName = fieldVarRef.varSymbol.name.value;
        RegIndex fieldNameRegIndex = createStringLiteral(fieldName, null, env);

        // This is a connector field.
        // the connector reference must be stored in the current reference register index.
        Operand varRegIndex = getOperand(0);
        if (varAssignment) {
            storeStructField(fieldVarRef, varRegIndex, fieldNameRegIndex);
            return;
        }

        loadStructField(fieldVarRef, varRegIndex, fieldNameRegIndex, except);
    }

    @Override
    public void visit(BLangPackageVarRef packageVarRef) {
        BPackageSymbol pkgSymbol;
        BSymbol ownerSymbol = packageVarRef.symbol.owner;
        pkgSymbol = (BPackageSymbol) ownerSymbol;

        Operand gvIndex = packageVarRef.varSymbol.varIndex;
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, pkgSymbol.pkgID);
        if (varAssignment) {
            int opcode = getOpcode(packageVarRef.type.tag, InstructionCodes.IGSTORE);
            emit(opcode, getOperand(pkgRefCPIndex), packageVarRef.regIndex, gvIndex);
        } else {
            int opcode = getOpcode(packageVarRef.type.tag, InstructionCodes.IGLOAD);
            packageVarRef.regIndex = calcAndGetExprRegIndex(packageVarRef);
            emit(opcode, getOperand(pkgRefCPIndex), gvIndex, packageVarRef.regIndex);
        }
    }

    @Override
    public void visit(BLangFunctionVarRef functionVarRef) {
        visitFunctionPointerLoad(functionVarRef, (BInvokableSymbol) functionVarRef.symbol);
    }

    @Override
    public void visit(BLangTypeLoad typeLoad) {
        Operand typeCPIndex = getTypeCPIndex(typeLoad.symbol.type);
        emit(InstructionCodes.TYPELOAD, typeCPIndex, calcAndGetExprRegIndex(typeLoad));
    }

    @Override
    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(fieldAccessExpr.expr, this.env);
        Operand varRefRegIndex = fieldAccessExpr.expr.regIndex;

        genNode(fieldAccessExpr.indexExpr, this.env);
        Operand keyRegIndex = fieldAccessExpr.indexExpr.regIndex;

        if (variableStore) {
            storeStructField(fieldAccessExpr, varRefRegIndex, keyRegIndex);
        } else {
            loadStructField(fieldAccessExpr, varRefRegIndex, keyRegIndex, fieldAccessExpr.except ? 1 : 0);
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangStructFunctionVarRef functionVarRef) {
        visitFunctionPointerLoad(functionVarRef, (BInvokableSymbol) functionVarRef.symbol);
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
            int opcode = getValueToRefTypeCastOpcode(mapType.constraint.tag);
            if (opcode == InstructionCodes.NOP || !mapKeyAccessExpr.except) {
                emit(InstructionCodes.MAPSTORE, varRefRegIndex, keyRegIndex, mapKeyAccessExpr.regIndex);
            } else {
                RegIndex refRegMapValue = getRegIndex(TypeTags.ANY);
                emit(opcode, mapKeyAccessExpr.regIndex, refRegMapValue);
                emit(InstructionCodes.MAPSTORE, varRefRegIndex, keyRegIndex, refRegMapValue);
            }
        } else {
            IntegerCPEntry exceptCPEntry = new IntegerCPEntry(mapKeyAccessExpr.except ? 1 : 0);
            Operand except = getOperand(currentPkgInfo.addCPEntry(exceptCPEntry));
            int opcode = getRefToValueTypeCastOpcode(mapType.constraint.tag);
            if (opcode == InstructionCodes.NOP || !mapKeyAccessExpr.except) {
                emit(InstructionCodes.MAPLOAD, varRefRegIndex, keyRegIndex, calcAndGetExprRegIndex(mapKeyAccessExpr),
                        except);
            } else {
                RegIndex refRegMapValue = getRegIndex(TypeTags.ANY);
                emit(InstructionCodes.MAPLOAD, varRefRegIndex, keyRegIndex, refRegMapValue, except);
                emit(opcode, refRegMapValue, calcAndGetExprRegIndex(mapKeyAccessExpr));
            }
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangTupleAccessExpr tupleIndexAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        genNode(tupleIndexAccessExpr.expr, this.env);
        Operand varRefRegIndex = tupleIndexAccessExpr.expr.regIndex;

        genNode(tupleIndexAccessExpr.indexExpr, this.env);
        Operand indexRegIndex = tupleIndexAccessExpr.indexExpr.regIndex;

        if (variableStore) {
            int opcode = getValueToRefTypeCastOpcode(tupleIndexAccessExpr.type.tag);
            if (opcode == InstructionCodes.NOP) {
                emit(InstructionCodes.RASTORE, varRefRegIndex, indexRegIndex, tupleIndexAccessExpr.regIndex);
            } else {
                RegIndex refRegTupleValue = getRegIndex(TypeTags.ANY);
                emit(opcode, tupleIndexAccessExpr.regIndex, refRegTupleValue);
                emit(InstructionCodes.RASTORE, varRefRegIndex, indexRegIndex, refRegTupleValue);
            }
        } else {
            int opcode = getRefToValueTypeCastOpcode(tupleIndexAccessExpr.type.tag);
            if (opcode == InstructionCodes.NOP) {
                emit(InstructionCodes.RALOAD,
                        varRefRegIndex, indexRegIndex, calcAndGetExprRegIndex(tupleIndexAccessExpr));
            } else {
                RegIndex refRegTupleValue = getRegIndex(TypeTags.ANY);
                emit(InstructionCodes.RALOAD, varRefRegIndex, indexRegIndex, refRegTupleValue);
                emit(opcode, refRegTupleValue, calcAndGetExprRegIndex(tupleIndexAccessExpr));
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

        if (jsonAccessExpr.indexExpr.type.tag == TypeTags.INT) {
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
        } else if (xmlIndexAccessExpr.indexExpr.type.tag == TypeTags.STRING) {
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
            int opcode = getOpcodeForArrayOperations(arrayType.eType.tag, InstructionCodes.IASTORE);
            emit(opcode, varRefRegIndex, indexRegIndex, arrayIndexAccessExpr.regIndex);
        } else {
            int opcode = getOpcodeForArrayOperations(arrayType.eType.tag, InstructionCodes.IALOAD);
            emit(opcode, varRefRegIndex, indexRegIndex, calcAndGetExprRegIndex(arrayIndexAccessExpr));
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (OperatorKind.AND.equals(binaryExpr.opKind)) {
            visitAndExpression(binaryExpr);
        } else if (OperatorKind.OR.equals(binaryExpr.opKind)) {
            visitOrExpression(binaryExpr);
        } else if (binaryExpr.opSymbol.opcode == InstructionCodes.REQ_NULL ||
                binaryExpr.opSymbol.opcode == InstructionCodes.RNE_NULL) {
            BLangExpression expr = (binaryExpr.lhsExpr.type.tag == TypeTags.NIL) ?
                    binaryExpr.rhsExpr : binaryExpr.lhsExpr;
            genNode(expr, this.env);
            emit(binaryExpr.opSymbol.opcode, expr.regIndex, calcAndGetExprRegIndex(binaryExpr));
        } else {
            genNode(binaryExpr.lhsExpr, this.env);
            genNode(binaryExpr.rhsExpr, this.env);
            RegIndex regIndex = calcAndGetExprRegIndex(binaryExpr);
            int opCode = binaryExpr.opSymbol.opcode;
            emit(opCode, binaryExpr.lhsExpr.regIndex, binaryExpr.rhsExpr.regIndex, regIndex);
        }
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        genNode(assignableExpr.lhsExpr, this.env);
        RegIndex regIndex = calcAndGetExprRegIndex(assignableExpr);
        Operand typeCPIndex = getTypeCPIndex(assignableExpr.targetType);
        emit(assignableExpr.opSymbol.opcode, assignableExpr.lhsExpr.regIndex, typeCPIndex, regIndex);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errExpr) {
        genNode(errExpr.reasonExpr, env);
        genNode(errExpr.detailsExpr, env);
        RegIndex regIndex = calcAndGetExprRegIndex(errExpr);
        emit(InstructionCodes.ERROR, getTypeCPIndex(errExpr.type), errExpr.reasonExpr.regIndex,
                errExpr.detailsExpr.regIndex, regIndex);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        // Emit create array instruction
        RegIndex exprRegIndex = calcAndGetExprRegIndex(bracedOrTupleExpr);
        Operand typeCPIndex = getTypeCPIndex(bracedOrTupleExpr.type);
        BLangLiteral sizeLiteral = generateIntegerLiteralNode(bracedOrTupleExpr, bracedOrTupleExpr.expressions.size());

        emit(InstructionCodes.RNEWARRAY, exprRegIndex, typeCPIndex, sizeLiteral.regIndex);

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

    @Override
    public void visit(BLangBuiltInMethodInvocation iExpr) {
        genNode(iExpr.expr, this.env);
        RegIndex regIndex = calcAndGetExprRegIndex(iExpr);
        switch (iExpr.builtInMethod) {
            case REASON:
                emit(InstructionCodes.REASON, iExpr.expr.regIndex, regIndex);
                break;
            case DETAIL:
                emit(InstructionCodes.DETAIL, iExpr.expr.regIndex, regIndex);
                break;
            case LENGTH:
                Operand typeCPIndex = getTypeCPIndex(iExpr.expr.type);
                emit(InstructionCodes.LENGTH, iExpr.expr.regIndex, typeCPIndex, regIndex);
                break;
            case FREEZE:
                emit(InstructionCodes.FREEZE, iExpr.expr.regIndex, regIndex);
                break;
            case IS_FROZEN:
                emit(InstructionCodes.IS_FROZEN, iExpr.expr.regIndex, regIndex);
                break;
            case CLONE:
                emit(InstructionCodes.CLONE, iExpr.expr.regIndex, regIndex);
                break;
            case STAMP:
                genNode(iExpr.requiredArgs.get(0), this.env);
                emit(InstructionCodes.STAMP, iExpr.requiredArgs.get(0).regIndex, getTypeCPIndex(iExpr.type), regIndex);
                break;
            case CONVERT:
                int opcode = ((BOperatorSymbol) iExpr.symbol).opcode;
                BLangExpression expr = iExpr.requiredArgs.get(0);
                BType castExprType = iExpr.type;
                RegIndex convExprRegIndex = calcAndGetExprRegIndex(iExpr.regIndex, castExprType.tag);
                iExpr.regIndex = convExprRegIndex;
                genNode(expr, this.env);
                if (opcode == InstructionCodes.CONVERT) {
                    typeCPIndex = getTypeCPIndex(((BInvokableType) iExpr.symbol.type).paramTypes.get(1));
                    emit(opcode, expr.regIndex, typeCPIndex, convExprRegIndex);
                } else {
                    emit(opcode, expr.regIndex, convExprRegIndex);
                }
                break;
            case ITERATE:
                emit(InstructionCodes.ITR_NEW, iExpr.expr.regIndex, regIndex);
                break;
            case NEXT:
                List<Operand> list = new LinkedList<>();
                list.add(iExpr.expr.regIndex);
                list.add(new Operand(1)); // Todo - Cleanup counts
                list.add(new Operand(iExpr.type.tag));
                list.add(new Operand(1)); // Todo - Cleanup counts
                list.add(iExpr.regIndex);

                BType resultType = ((BUnionType) iExpr.type).memberTypes.iterator().next();
                list.add(getTypeCPIndex(resultType));


                emit(InstructionCodes.ITR_NEXT, list.toArray(new Operand[0]));
                break;
        }
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
        Operand[] operands = getFuncOperands(cIExpr.initInvocation);

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
        if (iExpr.expr.type.tag == TypeTags.OBJECT) {
            Operand[] vCallOperands = new Operand[operands.length + 1];
            vCallOperands[0] = iExpr.expr.regIndex;
            System.arraycopy(operands, 0, vCallOperands, 1, operands.length);
            emit(InstructionCodes.VCALL, vCallOperands);
        } else {
            emit(InstructionCodes.CALL, operands);
        }
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
        RegIndex convExprRegIndex = calcAndGetExprRegIndex(convExpr.regIndex, castExprType.tag);
        convExpr.regIndex = convExprRegIndex;
        if (opcode == InstructionCodes.NOP) {
            convExpr.expr.regIndex = createLHSRegIndex(convExprRegIndex);
            genNode(convExpr.expr, this.env);
            return;
        }

        genNode(convExpr.expr, this.env);
        switch (opcode) {
            case InstructionCodes.MAP2T:
            case InstructionCodes.JSON2T:
            case InstructionCodes.ANY2T:
            case InstructionCodes.ANY2C:
            case InstructionCodes.ANY2E:
            case InstructionCodes.T2JSON:
            case InstructionCodes.MAP2JSON:
            case InstructionCodes.JSON2MAP:
            case InstructionCodes.JSON2ARRAY:
            case InstructionCodes.O2JSON:
            case InstructionCodes.CHECKCAST:
            case InstructionCodes.TYPE_ASSERTION:
                Operand typeCPIndex = getTypeCPIndex(convExpr.targetType);
                emit(opcode, convExpr.expr.regIndex, typeCPIndex, convExprRegIndex);
                break;
            default:
                emit(opcode, convExpr.expr.regIndex, convExprRegIndex);
                break;
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

    public void visit(BLangWaitExpr waitExpr) {
        Operand valueRegIndex = calcAndGetExprRegIndex(waitExpr);

        Operand length = this.getOperand(-1);
        List<Operand> operands = new ArrayList<>();
        operands.add(length);

        Operand typeCPIndex = getTypeCPIndex(waitExpr.type);
        operands.add(typeCPIndex);
        operands.add(valueRegIndex);

        for (BLangExpression expr : waitExpr.exprList) {
            genNode(expr, this.env);
            operands.add(expr.regIndex);
        }
        length.value = operands.size() - 3;
        // length of operands, type of wait, regIndex of value, expressions: regIndex of expr
        this.emit(InstructionCodes.WAIT, operands.toArray(new Operand[operands.size()]));
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        ErrorTableAttributeInfo errorTable = getErrorTable(currentPkgInfo);

        int fromIP = nextIP();
        genNode(trapExpr.expr, env);
        RegIndex regIndex = calcAndGetExprRegIndex(trapExpr);
        emit(InstructionCodes.RMOVE, trapExpr.expr.regIndex, regIndex);
        int toIP = nextIP();
        errorTable.addErrorTableEntry(new ErrorTableEntry(fromIP, toIP - 1, toIP, regIndex));
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
        visitFunctionPointerLoad(bLangLambdaFunction,
                ((BLangFunction) bLangLambdaFunction.getFunctionNode()).originalFuncSymbol);
    }

    public void visit(BLangStatementExpression bLangStatementExpression) {
        bLangStatementExpression.regIndex = calcAndGetExprRegIndex(bLangStatementExpression);

        boolean prevRegIndexResetDisabledState = this.regIndexResetDisabled;
        this.regIndexResetDisabled = true;
        genNode(bLangStatementExpression.stmt, this.env);
        this.regIndexResetDisabled = prevRegIndexResetDisabledState;

        genNode(bLangStatementExpression.expr, this.env);
        emit(getOpcode(bLangStatementExpression.expr.type.tag, InstructionCodes.IMOVE),
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

    private BLangLiteral generateIntegerLiteralNode(BLangNode node, long integer) {
        BLangLiteral literal = new BLangLiteral();
        literal.pos = node.pos;
        literal.value = integer;
        literal.type = symTable.intType;
        genNode(literal, this.env);
        return literal;
    }

    private int getNextIndex(int typeTag, VariableIndex indexes) {
        int index;
        switch (typeTag) {
            case TypeTags.INT:
                index = ++indexes.tInt;
                break;
            case TypeTags.BYTE:
                index = ++indexes.tBoolean;
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
            default:
                index = ++indexes.tRef;
                break;
        }

        return index;
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
            case TypeTags.BYTE:
            case TypeTags.BOOLEAN:
                opcode = baseOpcode + BOOL_OFFSET;
                break;
            default:
                opcode = baseOpcode + REF_OFFSET;
                break;
        }

        return opcode;
    }

    /**
     * This is a separate method that calculates opcode values for array related operations such as INEWARRAY, IALOAD,
     * IALOAD. A separate methods is added to adhere to the same pattern of using INT as a base opcode and to support
     * byte array related operations.
     */
    private int getOpcodeForArrayOperations(int typeTag, int baseOpcode) {
        int opcode;
        switch (typeTag) {
            case TypeTags.BYTE:
                opcode = baseOpcode - BYTE_NEGATIVE_OFFSET;
                break;
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

    private RegIndex getPVIndex(int typeTag) {
        return getRegIndexInternal(typeTag, PACKAGE);
    }

    private RegIndex getFieldIndex(int typeTag) {
        return getRegIndexInternal(typeTag, FIELD);
    }

    private RegIndex getRegIndex(int typeTag) {
        RegIndex regIndex = getRegIndexInternal(typeTag, REG);
        addToRegIndexList(regIndex);
        return regIndex;
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
        expr.regIndex = calcAndGetExprRegIndex(expr.regIndex, expr.type.tag);
        return expr.regIndex;
    }

    private RegIndex calcAndGetExprRegIndex(RegIndex regIndex, int typeTag) {
        if (regIndex != null && (regIndex.isVarIndex || regIndex.isLHSIndex)) {
            return regIndex;
        }

        return getRegIndex(typeTag);
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

        if (invokableNode.pos != null) {
            localVarAttributeInfo.localVars.forEach(param -> {
                param.scopeStartLineNumber = invokableNode.pos.sLine;
                param.scopeEndLineNumber = invokableNode.pos.eLine;
            });
        }

        if (Symbols.isNative(invokableNode.symbol)) {
            this.processWorker(callableUnitInfo.defaultWorkerInfo, null,
                    localVarAttributeInfo, invokableSymbolEnv, null);
        } else {
            // Clone lvIndex structure here. This structure contain local variable indexes of the input and
            // out parameters and they are common for all the workers.
            VariableIndex lvIndexCopy = this.copyVarIndex(lvIndexes);
            this.processWorker(callableUnitInfo.defaultWorkerInfo, invokableNode.body,
                    localVarAttributeInfo, invokableSymbolEnv, lvIndexCopy);
            for (BLangWorker worker : invokableNode.getWorkers()) {
                this.processWorker(callableUnitInfo.getWorkerInfo(worker.name.value),
                        worker.body, localVarAttributeInfo, invokableSymbolEnv, this.copyVarIndex(lvIndexCopy));
            }
        }

        if (invokableNode.symbol.taintTable != null) {
            int taintTableAttributeNameIndex = addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.TAINT_TABLE.value());
            TaintTableAttributeInfo taintTableAttributeInfo = new TaintTableAttributeInfo(taintTableAttributeNameIndex);
            visitTaintTable(invokableNode.symbol.taintTable, taintTableAttributeInfo);
            callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.TAINT_TABLE, taintTableAttributeInfo);
        }
    }

    private void visitTaintTable(Map<Integer, TaintRecord> taintTable,
                                 TaintTableAttributeInfo taintTableAttributeInfo) {
        int rowCount = 0;
        for (Integer paramIndex : taintTable.keySet()) {
            TaintRecord taintRecord = taintTable.get(paramIndex);
            boolean added = addTaintTableEntry(taintTableAttributeInfo, paramIndex, taintRecord);
            if (added) {
                // Number of columns required is: One column per parameter and one column for return tainted status.
                taintTableAttributeInfo.columnCount = taintRecord.parameterTaintedStatusList.size() + 1;
                rowCount++;
            }
        }
        taintTableAttributeInfo.rowCount = rowCount;
    }

    private boolean addTaintTableEntry(TaintTableAttributeInfo taintTableAttributeInfo, int index,
                                       TaintRecord taintRecord) {
        // Add to attribute info only if the current record has tainted status of return, but not taint errors.
        // It is not useful to preserve the propagated taint errors, since user will not be able to correct the compiled
        // code and will not need to know internals of the already compiled code.
        if (taintRecord.taintError == null || taintRecord.taintError.isEmpty()) {
            List<Byte> storedTaintTableValue = new ArrayList<>();
            storedTaintTableValue.add(taintRecord.returnTaintedStatus.getByteValue());
            storedTaintTableValue.addAll(taintRecord.parameterTaintedStatusList.stream().map(taintedStatus ->
                    taintedStatus.getByteValue()).collect(Collectors.toList()));
            taintTableAttributeInfo.taintTable.put(index, storedTaintTableValue);
            return true;
        }
        return false;
    }

    private void processWorker(WorkerInfo workerInfo, BLangBlockStmt body,
                               LocalVariableAttributeInfo localVarAttributeInfo, SymbolEnv invokableSymbolEnv,
                               VariableIndex lvIndexCopy) {
        workerInfo.codeAttributeInfo.attributeNameIndex = this.addUTF8CPEntry(
                this.currentPkgInfo, AttributeInfo.Kind.CODE_ATTRIBUTE.value());
        workerInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttributeInfo);
        if (body != null) {
            localVarAttrInfo = new LocalVariableAttributeInfo(localVarAttributeInfo.attributeNameIndex);
            localVarAttrInfo.localVars = new ArrayList<>(localVarAttributeInfo.localVars);
            workerInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttrInfo);
            workerInfo.codeAttributeInfo.codeAddrs = nextIP();
            this.lvIndexes = lvIndexCopy;
            this.currentWorkerInfo = workerInfo;
            this.emitTransactionParticipantBeginIfApplicable(body);
            this.genNode(body, invokableSymbolEnv);
        }
        this.endWorkerInfoUnit(workerInfo.codeAttributeInfo);
        this.emit(InstructionCodes.HALT);
    }

    private void emitTransactionParticipantBeginIfApplicable(BLangBlockStmt body) {
        BLangNode parent = body.parent;
        if (parent == null || parent.getKind() != NodeKind.FUNCTION) {
            return;
        }
        BLangFunction function = (BLangFunction) parent;
        List<BLangAnnotationAttachment> participantAnnotation = function.annAttachments.stream()
                .filter(a -> Transactions.isTransactionsAnnotation(a.pkgAlias.value, a.annotationName.value))
                .collect(Collectors.toList());
        if (participantAnnotation.isEmpty()) {
            // function not annotated for transaction participation.
            return;
        }

        transactionIndex++;
        BLangAnnotationAttachment annotation = participantAnnotation.get(0);
        Operand abortedFuncRegIndex = new RegIndex(-1, TypeTags.INVOKABLE);
        Operand committedFuncRegIndex = new RegIndex(-1, TypeTags.INVOKABLE);

        for (BLangRecordKeyValue keyValuePair : ((BLangRecordLiteral) annotation.expr).getKeyValuePairs()) {
            if (((BLangLiteral) keyValuePair.getKey()).value.equals(Transactions.TRX_ONABORT_FUNC)) {
                abortedFuncRegIndex.value = getFuncRefCPIndex(
                        (BInvokableSymbol) ((BLangSimpleVarRef) keyValuePair.getValue()).symbol);
            }
            if (((BLangLiteral) keyValuePair.getKey()).value.equals(Transactions.TRX_ONCOMMIT_FUNC)) {
                committedFuncRegIndex.value = getFuncRefCPIndex(
                        (BInvokableSymbol) ((BLangSimpleVarRef) keyValuePair.getValue()).symbol);
            }
        }
        // Participate in transaction.
        int txIdCPIndex = getTransactionIdCpIndex(transactionIndex);
        Operand transactionIndexOperand = getOperand(txIdCPIndex);
        int participantType = getParticipantTypeTag(function);
        Operand transactionType = getOperand(participantType);
        RegIndex retryCountRegIndex = getRegIndex(TypeTags.INT);
        this.emit(InstructionCodes.TR_BEGIN, transactionType, transactionIndexOperand, retryCountRegIndex,
                committedFuncRegIndex, abortedFuncRegIndex);
    }

    private int getParticipantTypeTag(BLangFunction function) {
        if (Symbols.isFlagOn((function).symbol.flags, Flags.RESOURCE)) {
            return Transactions.TransactionType.REMOTE_PARTICIPANT.value;
        }
        return Transactions.TransactionType.PARTICIPANT.value;
    }


    private void visitInvokableNodeParams(BInvokableSymbol invokableSymbol, CallableUnitInfo callableUnitInfo,
                                          LocalVariableAttributeInfo localVarAttrInfo) {

        // Visit the the receiver if this is an attached function
        if (invokableSymbol.receiverSymbol != null) {
            visitVarSymbol(invokableSymbol.receiverSymbol, lvIndexes, localVarAttrInfo);
        }

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
        varSymbol.varIndex = getRegIndexInternal(varSymbol.type.tag, variableIndex.kind);
        LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(varSymbol);
        localVarAttrInfo.localVars.add(localVarInfo);
    }

    private VariableIndex copyVarIndex(VariableIndex that) {
        VariableIndex vIndexes = new VariableIndex(that.kind);
        vIndexes.tInt = that.tInt;
        vIndexes.tFloat = that.tFloat;
        vIndexes.tString = that.tString;
        vIndexes.tBoolean = that.tBoolean;
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
        codeAttributeInfo.maxRefLocalVars = lvIndexes.tRef + 1;

        codeAttributeInfo.maxLongRegs = codeAttributeInfo.maxLongLocalVars + maxRegIndexes.tInt + 1;
        codeAttributeInfo.maxDoubleRegs = codeAttributeInfo.maxDoubleLocalVars + maxRegIndexes.tFloat + 1;
        codeAttributeInfo.maxStringRegs = codeAttributeInfo.maxStringLocalVars + maxRegIndexes.tString + 1;
        codeAttributeInfo.maxIntRegs = codeAttributeInfo.maxIntLocalVars + maxRegIndexes.tBoolean + 1;
        codeAttributeInfo.maxRefRegs = codeAttributeInfo.maxRefLocalVars + maxRegIndexes.tRef + 1;

        // Update register indexes.
        for (RegIndex regIndex : regIndexList) {
            switch (regIndex.typeTag) {
                case TypeTags.INT:
                    regIndex.value = regIndex.value + codeAttributeInfo.maxLongLocalVars;
                    break;
                case TypeTags.BYTE:
                    regIndex.value = regIndex.value + codeAttributeInfo.maxIntLocalVars;
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
        max.tRef = (max.tRef > current.tRef) ? max.tRef : current.tRef;
    }

    private void prepareIndexes(VariableIndex indexes) {
        indexes.tInt++;
        indexes.tFloat++;
        indexes.tString++;
        indexes.tBoolean++;
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

        iExpr.regIndex = calcAndGetExprRegIndex(iExpr.regIndex, iExpr.type.tag);
        operands[i] = iExpr.regIndex;
        return operands;
    }

    private int generateNamedArgs(BLangInvocation iExpr, Operand[] operands, int currentIndex) {
        if (iExpr.namedArgs.isEmpty()) {
            return currentIndex;
        }

        if (iExpr.symbol.kind != SymbolKind.FUNCTION) {
            throw new IllegalStateException("Unsupported callable unit");
        }

        for (BLangExpression argExpr : iExpr.namedArgs) {
            operands[currentIndex++] = genNode(argExpr, this.env).regIndex;
        }

        return currentIndex;
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
        varCountAttribInfo.setMaxRefVars(fieldCount[REF_OFFSET]);
        attributeInfoPool.addAttributeInfo(AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE, varCountAttribInfo);
    }

    private DefaultValue getDefaultValue(Object value) {
        BType type;
        if (value == null) {
            type = symTable.nilType;
        } else if (value instanceof Long) {
            type = symTable.intType;
        } else if (value instanceof Double) {
            type = symTable.floatType;
        } else if (value instanceof String) {
            type = symTable.stringType;
        } else if (value instanceof Boolean) {
            type = symTable.booleanType;
        } else {
            throw new IllegalStateException();
        }

        return getDefaultValue(value, type);
    }

    private DefaultValue getDefaultValue(Object value, BType typeOfValue) {
        int typeDescCPIndex = addUTF8CPEntry(currentPkgInfo, typeOfValue.getDesc());
        DefaultValue defaultValue = new DefaultValue(typeDescCPIndex, typeOfValue.getDesc());
        switch (typeOfValue.tag) {
            case TypeTags.INT:
                defaultValue.intValue = (Long) value;
                defaultValue.valueCPIndex = currentPkgInfo.addCPEntry(new IntegerCPEntry(defaultValue.intValue));
                break;
            case TypeTags.BYTE:
                defaultValue.byteValue = (Byte) value;
                defaultValue.valueCPIndex = currentPkgInfo.addCPEntry(new ByteCPEntry(defaultValue.byteValue));
                break;
            case TypeTags.FLOAT:
                // TODO:Remove the instanceof check by converting the float literal instance in Semantic analysis phase
                defaultValue.floatValue = value instanceof String ? Double.parseDouble((String) value) : (Double) value;
                defaultValue.valueCPIndex = currentPkgInfo.addCPEntry(new FloatCPEntry(defaultValue.floatValue));
                break;
            case TypeTags.STRING:
                defaultValue.stringValue = (String) value;
                defaultValue.valueCPIndex = currentPkgInfo.addCPEntry(new UTF8CPEntry(defaultValue.stringValue));
                break;
            case TypeTags.BOOLEAN:
                defaultValue.booleanValue = (Boolean) value;
                break;
            case TypeTags.DECIMAL:
                defaultValue.decimalValue = (String) value;
                defaultValue.valueCPIndex = currentPkgInfo.addCPEntry(new UTF8CPEntry(defaultValue.decimalValue));
                break;
            case TypeTags.NIL:
                break;
            default:
                defaultValue = null;
        }

        return defaultValue;
    }

    private DefaultValue getDefaultValue(BLangLiteral literalExpr) {
        return getDefaultValue(literalExpr.value, literalExpr.type);
    }

    private DefaultValueAttributeInfo getDefaultValueAttributeInfo(BLangLiteral literalExpr) {
        DefaultValue defaultValue = getDefaultValue(literalExpr);
        UTF8CPEntry defaultValueAttribUTF8CPEntry =
                new UTF8CPEntry(AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE.toString());
        int defaultValueAttribNameIndex = currentPkgInfo.addCPEntry(defaultValueAttribUTF8CPEntry);

        return new DefaultValueAttributeInfo(defaultValueAttribNameIndex, defaultValue);
    }


    // Create info entries

    private void createConstantInfo(BLangConstant constant) {
        BConstantSymbol constantSymbol = constant.symbol;
        int constantNameCPIndex = addUTF8CPEntry(currentPkgInfo, constantSymbol.name.value);
        int finiteTypeSigCPIndex = addUTF8CPEntry(currentPkgInfo, constantSymbol.type.getDesc());
        int valueTypeSigCPIndex = addUTF8CPEntry(currentPkgInfo, constantSymbol.literalValueType.getDesc());

        ConstantInfo constantInfo = new ConstantInfo(constantNameCPIndex, finiteTypeSigCPIndex, valueTypeSigCPIndex,
                constantSymbol.flags);
        currentPkgInfo.constantInfoMap.put(constantSymbol.name.value, constantInfo);

        BLangLiteral literal = new BLangLiteral();
        literal.pos = constant.pos;
        literal.value = ((BLangLiteral) constant.value).value;
        literal.type = ((BLangLiteral) constant.value).type;
        literal.typeTag = ((BLangLiteral) constant.value).typeTag;

        DefaultValueAttributeInfo value = getDefaultValueAttributeInfo(literal);
        constantInfo.addAttributeInfo(AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE, value);

        // Add documentation attributes.
        addDocAttachmentAttrInfo(constant.symbol.markdownDocumentation, constantInfo);
    }

    private void createPackageVarInfo(BLangSimpleVariable varNode) {
        BVarSymbol varSymbol = varNode.symbol;
        varSymbol.varIndex = getPVIndex(varSymbol.type.tag);

        int varNameCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.name.value);
        int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.type.getDesc());
        PackageVarInfo pkgVarInfo = new PackageVarInfo(varNameCPIndex, typeSigCPIndex, varSymbol.flags,
                varSymbol.varIndex.value, varNode.name.isLiteral);
        currentPkgInfo.pkgVarInfoMap.put(varSymbol.name.value, pkgVarInfo);

        LocalVariableInfo localVarInfo = getLocalVarAttributeInfo(varSymbol);
        LocalVariableAttributeInfo pkgVarAttrInfo = (LocalVariableAttributeInfo)
                currentPkgInfo.getAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);
        pkgVarAttrInfo.localVars.add(localVarInfo);

        // TODO Populate annotation attribute

        // Add documentation attributes
        addDocAttachmentAttrInfo(varNode.symbol.markdownDocumentation, pkgVarInfo);
    }

    public void visit(BLangTypeDefinition typeDefinition) {
        //TODO
    }

    private void createAnnotationInfoEntry(BLangAnnotation annotation) {
        int nameCPIndex = addUTF8CPEntry(currentPkgInfo, annotation.name.value);
        int typeSigCPIndex = -1;
        if (annotation.typeNode != null) {
            typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, annotation.typeNode.type.getDesc());
        }

        AnnotationInfo annotationInfo = new AnnotationInfo(nameCPIndex, typeSigCPIndex,
                annotation.symbol.flags, ((BAnnotationSymbol) annotation.symbol).attachPoints);
        currentPkgInfo.annotationInfoMap.put(annotation.name.value, annotationInfo);
    }

    private void createTypeDefinitionInfoEntry(BLangTypeDefinition typeDefinition) {
        BTypeSymbol typeDefSymbol = typeDefinition.symbol;
        int typeDefNameCPIndex = addUTF8CPEntry(currentPkgInfo, typeDefSymbol.name.value);
        TypeDefInfo typeDefInfo = new TypeDefInfo(currentPackageRefCPIndex,
                typeDefNameCPIndex, typeDefSymbol.flags);
        typeDefInfo.isLabel = typeDefinition.symbol.isLabel;

        typeDefInfo.typeTag = typeDefSymbol.type.tag;

        if (typeDefinition.symbol.isLabel) {
            createLabelTypeTypeDef(typeDefinition, typeDefInfo);
            // Add documentation attributes
            addDocAttachmentAttrInfo(typeDefinition.symbol.markdownDocumentation, typeDefInfo);

            currentPkgInfo.addTypeDefInfo(typeDefSymbol.name.value, typeDefInfo);
            return;
        }

        switch (typeDefinition.symbol.tag) {
            case SymTag.OBJECT:
                createObjectTypeTypeDef(typeDefinition, typeDefInfo, typeDefSymbol);
                break;
            case SymTag.RECORD:
                createRecordTypeTypeDef(typeDefinition, typeDefInfo, typeDefSymbol);
                break;
            case SymTag.FINITE_TYPE:
                createFiniteTypeTypeDef(typeDefinition, typeDefInfo);
                break;
            default:
                createLabelTypeTypeDef(typeDefinition, typeDefInfo);
                break;
        }
        // Add documentation attributes
        addDocAttachmentAttrInfo(typeDefinition.symbol.markdownDocumentation, typeDefInfo);

        currentPkgInfo.addTypeDefInfo(typeDefSymbol.name.value, typeDefInfo);
    }

    private void createObjectTypeTypeDef(BLangTypeDefinition typeDefinition,
                                         TypeDefInfo typeDefInfo, BTypeSymbol typeDefSymbol) {
        ObjectTypeInfo objInfo = new ObjectTypeInfo();
        BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) typeDefSymbol;
        // Add Struct name as an UTFCPEntry to the constant pool
        objInfo.objectType = (BObjectType) objectSymbol.type;

        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeDefinition.typeNode;

        List<BLangSimpleVariable> objFields = objectTypeNode.fields;
        for (BLangSimpleVariable objField : objFields) {
            // Create StructFieldInfo Entry
            int fieldNameCPIndex = addUTF8CPEntry(currentPkgInfo, objField.name.value);
            int sigCPIndex = addUTF8CPEntry(currentPkgInfo, objField.type.getDesc());

            objField.symbol.varIndex = getFieldIndex(objField.symbol.type.tag);
            StructFieldInfo objFieldInfo = new StructFieldInfo(fieldNameCPIndex,
                    sigCPIndex, objField.symbol.flags, objField.symbol.varIndex.value);
            objFieldInfo.fieldType = objField.type;

            // Populate default values
            if (objField.expr != null && (objField.expr.getKind() == NodeKind.LITERAL &&
                    objField.expr.type.getKind() != TypeKind.ARRAY)) {
                DefaultValueAttributeInfo defaultVal = getDefaultValueAttributeInfo((BLangLiteral) objField.expr);
                objFieldInfo.addAttributeInfo(AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE, defaultVal);
            }

            objInfo.fieldInfoEntries.add(objFieldInfo);

            // Add documentation attributes
            addDocAttachmentAttrInfo(objField.symbol.markdownDocumentation, objFieldInfo);
        }

        // Create variable count attribute info
        prepareIndexes(fieldIndexes);
        int[] fieldCount = new int[]{fieldIndexes.tInt, fieldIndexes.tFloat, fieldIndexes.tString,
                fieldIndexes.tBoolean, fieldIndexes.tRef};
        addVariableCountAttributeInfo(currentPkgInfo, objInfo, fieldCount);
        fieldIndexes = new VariableIndex(FIELD);

        for (BAttachedFunction attachedFunc : objectSymbol.referencedFunctions) {
            createAttachedFunctionInfo(attachedFunc.funcName, attachedFunc.symbol, objectSymbol);
        }

        //TODO decide what happens to documentations for interface functions and their implementations.

        typeDefInfo.typeInfo = objInfo;
    }

    private void createAttachedFunctionInfo(Name funcName, BInvokableSymbol funcSymbol,
                                            BObjectTypeSymbol objectSymbol) {
        // Add function name as an UTFCPEntry to the constant pool
        int funcNameCPIndex = this.addUTF8CPEntry(currentPkgInfo, funcName.getValue());

        FunctionInfo funcInfo = new FunctionInfo(currentPackageRefCPIndex, funcNameCPIndex);
        BInvokableType funcType = (BInvokableType) funcSymbol.type;
        funcInfo.paramTypes = funcType.paramTypes.toArray(new BType[0]);
        populateInvokableSignature(funcType, funcInfo);

        funcInfo.flags = funcSymbol.flags;
        funcInfo.attachedToTypeCPIndex = getTypeCPIndex(objectSymbol.type).value;

        // Add local var attributes. This will not be added automatically since this attached
        // function doesn't have a body, and wont get visited in the normal flow.
        int localVarAttNameIndex = addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.value());
        LocalVariableAttributeInfo localVarAttributeInfo = new LocalVariableAttributeInfo(localVarAttNameIndex);
        visitInvokableNodeParams(funcSymbol, funcInfo, localVarAttributeInfo);

        // Add parameter default value info
        addParameterAttributeInfo(funcSymbol, funcInfo);

        // Add documentation attributes
        addDocAttachmentAttrInfo(funcSymbol.markdownDocumentation, funcInfo);

        // Add worker send ins
        addWorkerSendInsAttributeInfo(new LinkedHashSet<>(), funcInfo);

        this.currentPkgInfo.functionInfoMap.put(funcSymbol.name.value, funcInfo);
    }

    private void createRecordTypeTypeDef(BLangTypeDefinition typeDefinition,
                                         TypeDefInfo typeDefInfo, BTypeSymbol typeDefSymbol) {
        RecordTypeInfo recordInfo = new RecordTypeInfo();
        BRecordTypeSymbol recordSymbol = (BRecordTypeSymbol) typeDefSymbol;
        // Add Struct name as an UTFCPEntry to the constant pool
        recordInfo.recordType = (BRecordType) recordSymbol.type;

        if (!recordInfo.recordType.sealed) {
            recordInfo.restFieldTypeSigCPIndex = addUTF8CPEntry(currentPkgInfo,
                    recordInfo.recordType.restFieldType.getDesc());
        }

        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeDefinition.typeNode;

        List<BLangSimpleVariable> recordFields = recordTypeNode.fields;
        for (BLangSimpleVariable recordField : recordFields) {
            // Create StructFieldInfo Entry
            int fieldNameCPIndex = addUTF8CPEntry(currentPkgInfo, recordField.name.value);
            int sigCPIndex = addUTF8CPEntry(currentPkgInfo, recordField.type.getDesc());

            recordField.symbol.varIndex = getFieldIndex(recordField.symbol.type.tag);
            StructFieldInfo recordFieldInfo = new StructFieldInfo(fieldNameCPIndex,
                    sigCPIndex, recordField.symbol.flags, recordField.symbol.varIndex.value);
            recordFieldInfo.fieldType = recordField.type;

            // Populate default values
            if (recordField.expr != null && (recordField.expr.getKind() == NodeKind.LITERAL &&
                    recordField.expr.type.getKind() != TypeKind.ARRAY)) {
                DefaultValueAttributeInfo defaultVal
                        = getDefaultValueAttributeInfo((BLangLiteral) recordField.expr);
                recordFieldInfo.addAttributeInfo(AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE, defaultVal);
            }

            recordInfo.fieldInfoEntries.add(recordFieldInfo);

            // Add documentation attributes
            addDocAttachmentAttrInfo(recordField.symbol.markdownDocumentation, recordFieldInfo);
        }

        // Create variable count attribute info
        prepareIndexes(fieldIndexes);
        int[] fieldCount = new int[]{fieldIndexes.tInt, fieldIndexes.tFloat, fieldIndexes.tString,
                fieldIndexes.tBoolean, fieldIndexes.tRef};
        addVariableCountAttributeInfo(currentPkgInfo, recordInfo, fieldCount);
        fieldIndexes = new VariableIndex(FIELD);
        typeDefInfo.typeInfo = recordInfo;
    }

    private void createFiniteTypeTypeDef(BLangTypeDefinition typeDefinition,
                                         TypeDefInfo typeDefInfo) {
        BLangFiniteTypeNode typeNode = (BLangFiniteTypeNode) typeDefinition.typeNode;
        FiniteTypeInfo typeInfo = new FiniteTypeInfo();

        for (BLangExpression literal : typeNode.valueSpace) {
            typeInfo.valueSpaceItemInfos.add(new ValueSpaceItemInfo(getDefaultValue((BLangLiteral) literal)));
        }

        typeDefInfo.typeInfo = typeInfo;
    }

    private void createLabelTypeTypeDef(BLangTypeDefinition typeDefinition,
                                        TypeDefInfo typeDefInfo) {
        int sigCPIndex = addUTF8CPEntry(currentPkgInfo, typeDefinition.typeNode.type.getDesc());
        typeDefInfo.typeInfo = new LabelTypeInfo(sigCPIndex);
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

        this.addWorkerInfoEntries(funcInfo, funcNode);

        // Add parameter default value info
        addParameterAttributeInfo(funcSymbol, funcInfo);

        // Add worker send ins
        addWorkerSendInsAttributeInfo(funcNode.sendsToThis, funcInfo);

        // Add documentation attributes
        addDocAttachmentAttrInfo(funcNode.symbol.markdownDocumentation, funcInfo);

        this.currentPkgInfo.functionInfoMap.put(funcSymbol.name.value, funcInfo);
    }

    private void populateInvokableSignature(BInvokableType bInvokableType, CallableUnitInfo callableUnitInfo) {
        callableUnitInfo.retParamTypes = new BType[1];
        callableUnitInfo.retParamTypes[0] = bInvokableType.retType;
        callableUnitInfo.signatureCPIndex = addUTF8CPEntry(this.currentPkgInfo,
                generateFunctionSig(callableUnitInfo.paramTypes, bInvokableType.retType));
    }

    private void addWorkerInfoEntries(CallableUnitInfo callableUnitInfo, BLangFunction funcNode) {
        UTF8CPEntry workerNameCPEntry = new UTF8CPEntry(funcNode.defaultWorkerName.value);
        int workerNameCPIndex = this.currentPkgInfo.addCPEntry(workerNameCPEntry);
        callableUnitInfo.defaultWorkerInfo = new WorkerInfo(workerNameCPIndex, funcNode.defaultWorkerName.value);
        for (BLangWorker worker : funcNode.getWorkers()) {
            workerNameCPEntry = new UTF8CPEntry(worker.name.value);
            workerNameCPIndex = currentPkgInfo.addCPEntry(workerNameCPEntry);
            WorkerInfo workerInfo = new WorkerInfo(workerNameCPIndex, worker.getName().value);
            callableUnitInfo.addWorkerInfo(worker.getName().value, workerInfo);
        }
    }

    private void createServiceInfoEntry(BLangService serviceNode) {
        // Add service name as an UTFCPEntry to the constant pool
        int serviceNameCPIndex = addUTF8CPEntry(currentPkgInfo, serviceNode.name.value);
        //Create service info
        int serviceTypeCPIndex = getTypeCPIndex(serviceNode.symbol.type).getValue();
        ServiceInfo serviceInfo = new ServiceInfo(currentPackageRefCPIndex, serviceNameCPIndex,
                serviceNode.symbol.flags, serviceTypeCPIndex);

        currentPkgInfo.addServiceInfo(serviceNode.name.value, serviceInfo);
        // Create resource info entries for all resources
        ((BObjectTypeSymbol) serviceNode.symbol.type.tsymbol).attachedFuncs.stream()
                .filter(func -> Symbols.isFlagOn(func.symbol.flags, Flags.RESOURCE)).forEach(
                fuc -> serviceInfo.resourcesCPIndex.add(addUTF8CPEntry(currentPkgInfo, fuc.symbol.getName().value)));
        // Add documentation attributes

        addDocAttachmentAttrInfo(serviceNode.symbol.markdownDocumentation, serviceInfo);
    }

    private ErrorTableAttributeInfo getErrorTable(PackageInfo packageInfo) {
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
        int nameCPIndex = addUTF8CPEntry(pool, pkgID.toString());
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
        int tRef = -1;
        Kind kind;

        VariableIndex(Kind kind) {
            this.kind = kind;
        }

        public int[] toArray() {
            int[] result = new int[5];
            result[0] = this.tInt;
            result[1] = this.tFloat;
            result[2] = this.tString;
            result[3] = this.tBoolean;
            result[4] = this.tRef;
            return result;
        }

    }

    public void visit(BLangWorker workerNode) {
        this.genNode(workerNode.body, this.env);
    }

    public void visit(BLangForkJoin forkJoin) {
         /* ignore */
    }

    public void visit(BLangWorkerSend workerSendStmt) {
        if (workerSendStmt.isChannel) {
            visitChannelSend(workerSendStmt);
            return;
        }
        WorkerDataChannelInfo workerDataChannelInfo = this.getWorkerDataChannelInfo(this.currentCallableUnitInfo,
                this.currentWorkerInfo.getWorkerName(), workerSendStmt.workerIdentifier.value);
        WorkerDataChannelRefCPEntry wrkrInvRefCPEntry = new WorkerDataChannelRefCPEntry(workerDataChannelInfo
                .getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());
        wrkrInvRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        Operand wrkrInvRefCPIndex = getOperand(currentPkgInfo.addCPEntry(wrkrInvRefCPEntry));
        workerDataChannelInfo.setDataChannelRefIndex(wrkrInvRefCPIndex.value);

        genNode(workerSendStmt.expr, this.env);
        RegIndex argReg = workerSendStmt.expr.regIndex;
        BType bType = workerSendStmt.expr.type;
        UTF8CPEntry sigCPEntry = new UTF8CPEntry(this.generateSig(new BType[]{bType}));
        Operand sigCPIndex = getOperand(this.currentPkgInfo.addCPEntry(sigCPEntry));

        // WRKSEND wrkrInvRefCPIndex typesCPIndex regIndex
        Operand[] wrkSendArgRegs = new Operand[3];
        wrkSendArgRegs[0] = wrkrInvRefCPIndex;
        wrkSendArgRegs[1] = sigCPIndex;
        wrkSendArgRegs[2] = argReg;
        this.emit(InstructionCodes.WRKSEND, wrkSendArgRegs);
    }

    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        WorkerDataChannelInfo workerDataChannelInfo = this.getWorkerDataChannelInfo(this.currentCallableUnitInfo,
                    this.currentWorkerInfo.getWorkerName(), syncSendExpr.workerIdentifier.value);
        WorkerDataChannelRefCPEntry wrkrInvRefCPEntry = new WorkerDataChannelRefCPEntry(workerDataChannelInfo
                    .getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());
        wrkrInvRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        Operand wrkrInvRefCPIndex = getOperand(currentPkgInfo.addCPEntry(wrkrInvRefCPEntry));
        workerDataChannelInfo.setDataChannelRefIndex(wrkrInvRefCPIndex.value);

        // send expression
        genNode(syncSendExpr.expr, this.env);
        RegIndex exprRegIndex = syncSendExpr.expr.regIndex;
        BType exprType = syncSendExpr.expr.type;
        UTF8CPEntry sigCPEntry = new UTF8CPEntry(this.generateSig(new BType[]{exprType}));
        Operand sigCPIndex = getOperand(this.currentPkgInfo.addCPEntry(sigCPEntry));
        // Reg index of send expression
        RegIndex regIndex = calcAndGetExprRegIndex(syncSendExpr);

        // WRKSEND wrkrInvRefCPIndex typesCPIndex exprRegIndex regIndex
        Operand[] wrkSendArgRegs = new Operand[4];
        wrkSendArgRegs[0] = wrkrInvRefCPIndex;
        wrkSendArgRegs[1] = sigCPIndex;
        wrkSendArgRegs[2] = exprRegIndex;
        wrkSendArgRegs[3] = regIndex;
        this.emit(InstructionCodes.WORKERSYNCSEND, wrkSendArgRegs);
    }

    public void visit(BLangWorkerReceive workerReceiveStmt) {
        if (workerReceiveStmt.isChannel) {
            visitChannelReceive(workerReceiveStmt);
            return;
        }
        WorkerDataChannelInfo workerDataChannelInfo = this.getWorkerDataChannelInfo(this.currentCallableUnitInfo,
                workerReceiveStmt.workerIdentifier.value, this.currentWorkerInfo.getWorkerName());
        WorkerDataChannelRefCPEntry wrkrChnlRefCPEntry = new WorkerDataChannelRefCPEntry(workerDataChannelInfo
                .getUniqueNameCPIndex(), workerDataChannelInfo.getUniqueName());
        wrkrChnlRefCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        Operand wrkrRplyRefCPIndex = getOperand(currentPkgInfo.addCPEntry(wrkrChnlRefCPEntry));
        workerDataChannelInfo.setDataChannelRefIndex(wrkrRplyRefCPIndex.value);

        RegIndex regIndex = calcAndGetExprRegIndex(workerReceiveStmt);
        BType bType = workerReceiveStmt.type;
        UTF8CPEntry sigCPEntry = new UTF8CPEntry(this.generateSig(new BType[]{bType}));
        Operand sigCPIndex = getOperand(currentPkgInfo.addCPEntry(sigCPEntry));

        // WRKRECEIVE wrkrRplyRefCPIndex typesCPIndex regIndex
        Operand[] wrkReceiveArgRegs = new Operand[3];
        wrkReceiveArgRegs[0] = wrkrRplyRefCPIndex;
        wrkReceiveArgRegs[1] = sigCPIndex;
        wrkReceiveArgRegs[2] = regIndex;
        emit(InstructionCodes.WRKRECEIVE, wrkReceiveArgRegs);
    }

    public void visit(BLangForever foreverStatement) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    public void visit(BLangAnnotation annotationNode) {
        /* ignore */
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
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

    public void visit(BLangContinue continueNode) {
        generateFinallyInstructions(continueNode, NodeKind.WHILE, NodeKind.FOREACH);
        this.emit(this.loopResetInstructionStack.peek());
    }

    public void visit(BLangBreak breakNode) {
        generateFinallyInstructions(breakNode, NodeKind.WHILE, NodeKind.FOREACH);
        this.emit(this.loopExitInstructionStack.peek());
    }

    public void visit(BLangPanic panicNode) {
        genNode(panicNode.expr, env);
        emit(InstructionFactory.get(InstructionCodes.PANIC, panicNode.expr.regIndex));
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
        if (lockNode.lockVariables.isEmpty() && lockNode.fieldVariables.isEmpty()) {
            this.genNode(lockNode.body, this.env);
            return;
        }

        //remove objects initialized within lock
        lockNode.fieldVariables.keySet().removeIf(symbol -> symbol.varIndex == null);

        Operand gotoLockEndAddr = getOperand(-1);
        Instruction instructGotoLockEnd = InstructionFactory.get(InstructionCodes.GOTO, gotoLockEndAddr);
        Operand[] operands = getOperands(lockNode);
        Operand[] unlockOps = getUnlockOperands(lockNode);
        ErrorTableAttributeInfo errorTable = getErrorTable(currentPkgInfo);

        int fromIP = nextIP();
        emit((InstructionCodes.LOCK), operands);

        this.genNode(lockNode.body, this.env);
        int toIP = nextIP() - 1;

        emit((InstructionCodes.UNLOCK), unlockOps);
        emit(instructGotoLockEnd);

        RegIndex errorRegIndex = getRegIndex(TypeTags.ERROR);

        ErrorTableEntry errorTableEntry = new ErrorTableEntry(fromIP, toIP, nextIP(), errorRegIndex);
        errorTable.addErrorTableEntry(errorTableEntry);

        emit((InstructionCodes.UNLOCK), unlockOps);
        emit(InstructionFactory.get(InstructionCodes.PANIC, errorRegIndex));
        gotoLockEndAddr.value = nextIP();
    }

    private Operand[] getOperands(BLangLock lockNode) {
        //need to visit all the vars because parent node of a nested structure may not be loaded yet
        lockNode.fieldVariables.values().forEach(exprList -> exprList.stream().forEach(expr -> visit(expr)));

        lockNode.uuid = UUID.randomUUID().toString();
        //count field vars
        int fieldVarCount = 0;
        for (Set<BLangStructFieldAccessExpr> fields : lockNode.fieldVariables.values()) {
            fieldVarCount += fields.size();
        }

        //lockVarCount, fieldVarCount [typeRefCP, pkgRefCP, varIndex], uuid,  fieldVars[pkgRefCP, varIndex, fieldName]
        Operand[] operands = new Operand[(lockNode.lockVariables.size() * 3) + 3 + (fieldVarCount * 3)];
        int i = 0;
        operands[i++] = new Operand(lockNode.lockVariables.size());
        operands[i++] = getOperand(fieldVarCount);

        for (BVarSymbol varSymbol : lockNode.lockVariables) {
            BPackageSymbol pkgSymbol;
            BSymbol ownerSymbol = varSymbol.owner;
            if (ownerSymbol.tag == SymTag.SERVICE) {
                pkgSymbol = (BPackageSymbol) ownerSymbol.owner;
            } else {
                pkgSymbol = (BPackageSymbol) ownerSymbol;
            }
            int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, pkgSymbol.pkgID);

            int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.getType().getDesc());
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
            operands[i++] = getOperand(currentPkgInfo.addCPEntry(typeRefCPEntry));
            operands[i++] = getOperand(pkgRefCPIndex);
            operands[i++] = varSymbol.varIndex;
        }

        int uuidCPEntry = addUTF8CPEntry(currentPkgInfo, lockNode.uuid);
        operands[i++] = getOperand(uuidCPEntry);

        for (Entry<BVarSymbol, Set<BLangStructFieldAccessExpr>> entry : lockNode.fieldVariables.entrySet()) {
            BSymbol symbol = entry.getKey();
            Set<BLangStructFieldAccessExpr> expressions = entry.getValue();

            int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, symbol.pkgID);

            for (BLangStructFieldAccessExpr expr : expressions) {
                operands[i++] = getOperand(pkgRefCPIndex);
                operands[i++] = expr.expr.regIndex;

                operands[i++] = expr.indexExpr.regIndex;
            }
        }

        return operands;
    }

    private Operand[] getUnlockOperands(BLangLock lockNode) {
        //count field vars
        int fieldVarCount = 0;
        for (Set<BLangStructFieldAccessExpr> fields : lockNode.fieldVariables.values()) {
            fieldVarCount += fields.size();
        }

        //lockVarCount, fieldVarCount, uuid, [typeRefCP, pkgRefCP, varIndex],
        Operand[] operands = new Operand[(lockNode.lockVariables.size() * 3) + 3];
        int i = 0;
        operands[i++] = new Operand(lockNode.lockVariables.size());
        operands[i++] = getOperand(fieldVarCount);

        int uuidCPEntry = addUTF8CPEntry(currentPkgInfo, lockNode.uuid);
        operands[i++] = getOperand(uuidCPEntry);

        for (BVarSymbol varSymbol : lockNode.lockVariables) {
            BPackageSymbol pkgSymbol;
            BSymbol ownerSymbol = varSymbol.owner;
            pkgSymbol = (BPackageSymbol) ownerSymbol;
            int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, pkgSymbol.pkgID);

            int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, varSymbol.getType().getDesc());
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
            operands[i++] = getOperand(currentPkgInfo.addCPEntry(typeRefCPEntry));
            operands[i++] = getOperand(pkgRefCPIndex);
            operands[i++] = varSymbol.varIndex;
        }
        return operands;
    }

    public void visit(BLangTransaction transactionNode) {
        ++transactionIndex;

        int txIdCpIndex = getTransactionIdCpIndex(transactionIndex);
        Operand transactionIndexOperand = getOperand(txIdCpIndex);

        Operand retryCountRegIndex = new RegIndex(-1, TypeTags.INT);
        if (transactionNode.retryCount != null) {
            this.genNode(transactionNode.retryCount, this.env);
            retryCountRegIndex = transactionNode.retryCount.regIndex;
        }

        Operand transStmtAbortEndAddr = getOperand(-1);
        Operand transStmtFailEndAddr = getOperand(-1);
        Instruction gotoAbortTransBlockEnd = InstructionFactory.get(InstructionCodes.GOTO, transStmtAbortEndAddr);
        Instruction gotoFailTransBlockEnd = InstructionFactory.get(InstructionCodes.GOTO, transStmtFailEndAddr);

        RegIndex trEndStatusReg = getRegIndex(TypeTags.BOOLEAN);
        abortedFromStatus.push(trEndStatusReg);
        abortInstructions.push(gotoAbortTransBlockEnd);
        failInstructions.push(gotoFailTransBlockEnd);

        // Start transaction.
        Operand minusOne = getOperand(-1);
        Operand transactionType = getOperand(Transactions.TransactionType.INITIATOR.value);
        this.emit(InstructionCodes.TR_BEGIN, transactionType, transactionIndexOperand, retryCountRegIndex,
                minusOne, minusOne);
        Operand retryInstructionAddress = getOperand(nextIP());

        // Retry transaction.
        Operand txConclusionEndAddr = getOperand(-1);
        this.emit(InstructionCodes.TR_RETRY, transactionIndexOperand, transStmtAbortEndAddr, trEndStatusReg);

        // Process transaction statements.
        boolean prevRegResetState = this.regIndexResetDisabled;
        this.regIndexResetDisabled = true;
        this.genNode(transactionNode.transactionBody, this.env);
        this.regIndexResetDisabled = prevRegResetState;

        // Last instruction of the transaction block.
        int trErrorHandlerAddress = nextIP();
        int transBlockFinalAddr = trErrorHandlerAddress;
        RegIndex errorRegIndex = getRegIndex(TypeTags.ERROR);
        // End the transaction.
        this.emit(InstructionCodes.TR_END, transactionIndexOperand,
                getOperand(Transactions.TransactionStatus.BLOCK_END.value()), trEndStatusReg, errorRegIndex);
        // If transaction in failed state, goto retry.
        this.emit(InstructionCodes.BR_TRUE, trEndStatusReg, transStmtFailEndAddr);

        abortInstructions.pop();
        abortedFromStatus.pop();
        failInstructions.pop();

        // Committed body.
        if (transactionNode.committedBody != null) {
            boolean prevRegIndexResetDisabledState = this.regIndexResetDisabled;
            this.regIndexResetDisabled = true;
            this.genNode(transactionNode.committedBody, this.env);
            this.regIndexResetDisabled = prevRegIndexResetDisabledState;

        }

        emit(InstructionCodes.GOTO, txConclusionEndAddr);

        // CodeGen for error handling.
        transStmtFailEndAddr.value = nextIP();
        emit(InstructionCodes.TR_END, transactionIndexOperand,
                getOperand(Transactions.TransactionStatus.FAILED.value()), trEndStatusReg, errorRegIndex);
        // If retry possible run on-retry block, otherwise goto retry instruction.
        emit(InstructionCodes.BR_FALSE, trEndStatusReg, retryInstructionAddress);
        if (transactionNode.onRetryBody != null) {
            boolean prevRegIndexResetDisabledState = this.regIndexResetDisabled;
            this.regIndexResetDisabled = true;
            this.genNode(transactionNode.onRetryBody, this.env);
            this.regIndexResetDisabled = prevRegIndexResetDisabledState;

        }
        emit(InstructionCodes.GOTO, retryInstructionAddress);

        // Steal error handling within transaction block to tx error handling section.
        ErrorTableAttributeInfo errorTable = getErrorTable(currentPkgInfo);
        ErrorTableEntry errorTableEntry = new ErrorTableEntry(retryInstructionAddress.value, transBlockFinalAddr,
                trErrorHandlerAddress, errorRegIndex);
        errorTable.addErrorTableEntry(errorTableEntry);

        // Aborted block.
        transStmtAbortEndAddr.value = nextIP();
        emit(InstructionCodes.TR_END, transactionIndexOperand,
                getOperand(Transactions.TransactionStatus.ABORTED.value()), trEndStatusReg, errorRegIndex);
        if (transactionNode.abortedBody != null) {
            boolean prevRegIndexResetDisabledState = this.regIndexResetDisabled;
            this.regIndexResetDisabled = true;
            this.genNode(transactionNode.abortedBody, this.env);
            this.regIndexResetDisabled = prevRegIndexResetDisabledState;
        }

        // Conclude transaction handling.
        int transactionEndIp = nextIP();
        txConclusionEndAddr.value = transactionEndIp;
        emit(InstructionCodes.TR_END, transactionIndexOperand, getOperand(Transactions.TransactionStatus.END.value()),
                trEndStatusReg, errorRegIndex);

        // Rethrow captured exceptions, if available.
        Operand oneAfterRethrowInstructionAddress = getOperand(-1);
        emit(InstructionCodes.BR_FALSE, trEndStatusReg, oneAfterRethrowInstructionAddress);
        emit(InstructionCodes.PANIC, errorRegIndex);
        oneAfterRethrowInstructionAddress.value = nextIP();
    }

    private int getTransactionIdCpIndex(int transactionIndex) {
        StringBuilder txNameBuilder = new StringBuilder();
        String transactionIndexStr = txNameBuilder
                .append(currentPkgID.orgName)
                .append("$")
                .append(currentPkgID.name)
                .append("$")
                .append(transactionIndex)
                .toString();
        StringCPEntry stringCPEntry = new StringCPEntry(
                addUTF8CPEntry(currentPkgInfo, transactionIndexStr), transactionIndexStr);
        return currentPkgInfo.addCPEntry(stringCPEntry);
    }

    public void visit(BLangAbort abortNode) {
        generateFinallyInstructions(abortNode, NodeKind.TRANSACTION);
        RegIndex index = abortedFromStatus.peek();
        // Set aborted reason reg to false.
        this.emit(InstructionCodes.BNE, index, index, index);
        this.emit(abortInstructions.peek());
    }

    public void visit(BLangRetry retryNode) {
        generateFinallyInstructions(retryNode, NodeKind.TRANSACTION);
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
        RegIndex lvIndex = getLVIndex(TypeTags.STRING);
        BLangExpression nsURIExpr = xmlnsNode.namespaceURI;
        nsURIExpr.regIndex = createLHSRegIndex(lvIndex);
        genNode(nsURIExpr, env);

        BXMLNSSymbol nsSymbol = (BXMLNSSymbol) xmlnsNode.symbol;
        nsSymbol.nsURIIndex = lvIndex;
    }

    @Override
    public void visit(BLangPackageXMLNS xmlnsNode) {
        BLangExpression nsURIExpr = xmlnsNode.namespaceURI;
        Operand pvIndex = getPVIndex(TypeTags.STRING);
        BXMLNSSymbol nsSymbol = (BXMLNSSymbol) xmlnsNode.symbol;
        genNode(nsURIExpr, env);
        nsSymbol.nsURIIndex = pvIndex;

        int pkgIndex = addPackageRefCPEntry(this.currentPkgInfo, this.currentPkgID);
        emit(InstructionCodes.SGSTORE, getOperand(pkgIndex), nsURIExpr.regIndex, pvIndex);
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
        xmlQName.regIndex = calcAndGetExprRegIndex(xmlQName.regIndex, TypeTags.XML);
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
            RegIndex localNameRegIndex = getRegIndex(TypeTags.STRING);
            RegIndex uriRegIndex = getRegIndex(TypeTags.STRING);
            emit(InstructionCodes.S2QNAME, attrQNameRegIndex, localNameRegIndex, uriRegIndex);

            attrQNameRegIndex = getRegIndex(TypeTags.XML);
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
            xmlTextLiteral.regIndex = calcAndGetExprRegIndex(xmlTextLiteral.regIndex, TypeTags.XML);
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
            RegIndex localNameRegIndex = getRegIndex(TypeTags.STRING);
            RegIndex uriRegIndex = getRegIndex(TypeTags.STRING);
            emit(InstructionCodes.S2QNAME, qnameRegIndex, localNameRegIndex, uriRegIndex);

            qnameRegIndex = getRegIndex(TypeTags.XML);
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
        int toIPPlaceHolder = -1;
        Operand gotoTryCatchEndAddr = getOperand(-1);
        Instruction instructGotoTryCatchEnd = InstructionFactory.get(InstructionCodes.GOTO, gotoTryCatchEndAddr);
        List<int[]> unhandledErrorRangeList = new ArrayList<>();
        ErrorTableAttributeInfo errorTable = getErrorTable(currentPkgInfo);

        int fromIP = nextIP();
        tryCatchErrorRangeToIPStack.push(toIPPlaceHolder);
        // Handle try block.
        genNode(tryNode.tryBody, env);

        // Pop out the final additional IP pushed on to the stack, if pushed when generating finally instrructions due
        // to a return statement being present in the try block
        if (!tryCatchErrorRangeFromIPStack.empty()) {
            tryCatchErrorRangeFromIPStack.pop();
        }

        while (!tryCatchErrorRangeFromIPStack.empty() && !tryCatchErrorRangeToIPStack.empty()
                && tryCatchErrorRangeToIPStack.peek() != toIPPlaceHolder) {
            unhandledErrorRangeList.add(new int[]{tryCatchErrorRangeFromIPStack.pop(),
                    tryCatchErrorRangeToIPStack.pop()});
        }

        int toIP = tryCatchErrorRangeToIPStack.pop();
        toIP = (toIP == toIPPlaceHolder) ? (nextIP() - 1) : toIP;

        unhandledErrorRangeList.add(new int[]{fromIP, toIP});

        // Append finally block instructions.
        if (tryNode.finallyBody != null) {
            genNode(tryNode.finallyBody, env);
        }
        emit(instructGotoTryCatchEnd);

        // Handle catch blocks.
        // Temporary error range list for new error ranges identified in catch blocks
        List<int[]> unhandledCatchErrorRangeList = new ArrayList<>();
        for (BLangCatch bLangCatch : tryNode.getCatchBlocks()) {
            addLineNumberInfo(bLangCatch.pos);
            int targetIP = nextIP();
            tryCatchErrorRangeToIPStack.push(toIPPlaceHolder);
            genNode(bLangCatch, env);

            if (!tryCatchErrorRangeFromIPStack.empty()) {
                tryCatchErrorRangeFromIPStack.pop();
            }

            while (tryCatchErrorRangeFromIPStack.size() > 1 && !tryCatchErrorRangeToIPStack.empty()
                    && tryCatchErrorRangeToIPStack.peek() != toIPPlaceHolder) {
                unhandledCatchErrorRangeList.add(new int[]{tryCatchErrorRangeFromIPStack.pop(),
                        tryCatchErrorRangeToIPStack.pop()});
            }
            int catchToIP = tryCatchErrorRangeToIPStack.pop();
            catchToIP = (catchToIP == toIPPlaceHolder) ? (nextIP() - 1) : catchToIP;
            unhandledCatchErrorRangeList.add(new int[]{targetIP, catchToIP});

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

            for (int[] range : unhandledErrorRangeList) {
                ErrorTableEntry errorTableEntry = new ErrorTableEntry(range[0], range[1], targetIP, null);
                errorTable.addErrorTableEntry(errorTableEntry);
            }
        }

        if (tryNode.finallyBody != null) {
            unhandledErrorRangeList.addAll(unhandledCatchErrorRangeList);

            // Create Error table entry for unhandled errors in try and catch(s) blocks
            for (int[] range : unhandledErrorRangeList) {
                ErrorTableEntry errorTableEntry = new ErrorTableEntry(range[0], range[1], nextIP(), null);
                errorTable.addErrorTableEntry(errorTableEntry);
            }
            // Append finally block instruction.
            genNode(tryNode.finallyBody, env);
            emit(InstructionFactory.get(InstructionCodes.PANIC, getOperand(-1)));
        }
        gotoTryCatchEndAddr.value = nextIP();
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

        emit(InstructionCodes.INT_RANGE, startExpr.regIndex, endExpr.regIndex, rangeExpr.regIndex);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        genNode(typeTestExpr.expr, env);
        Operand typeCPIndex = getTypeCPIndex(typeTestExpr.typeNode.type);
        emit(InstructionCodes.TYPE_TEST, typeTestExpr.expr.regIndex, typeCPIndex,
                calcAndGetExprRegIndex(typeTestExpr));
    }

    @Override
    public void visit(BLangIsLikeExpr isLikeExpr) {
        genNode(isLikeExpr.expr, env);
        Operand typeCPIndex = getTypeCPIndex(isLikeExpr.typeNode.type);
        emit(InstructionCodes.IS_LIKE, isLikeExpr.expr.regIndex, typeCPIndex,
                calcAndGetExprRegIndex(isLikeExpr));
    }

    // private helper methods of visitors.

    private void visitChannelSend(BLangWorkerSend channelSend) {
        //CHNSEND hasKey, keyIndex, keyTypeCPIndex  channelName, dataIndex, dataTypeCPIndex
        int i = 0;
        Operand[] argRegs;
        if (channelSend.keyExpr != null) {
            genNode(channelSend.keyExpr, this.env);
            argRegs = new Operand[6];
            argRegs[i++] = getOperand(true);

            RegIndex keyReg = channelSend.keyExpr.regIndex;
            BType keyType = channelSend.keyExpr.type;
            UTF8CPEntry keyCPEntry = new UTF8CPEntry(this.generateSig(new BType[] { keyType }));
            Operand keyCPIndex = getOperand(this.currentPkgInfo.addCPEntry(keyCPEntry));
            argRegs[i++] = keyReg;
            argRegs[i++] = keyCPIndex;
        } else {
            argRegs = new Operand[4];
            argRegs[i++] = getOperand(false);
        }
        int channelName = addUTF8CPEntry(currentPkgInfo, channelSend.env.enclPkg.symbol.name + "."
                + channelSend.getWorkerName().getValue());
        argRegs[i++] = getOperand(channelName);

        genNode(channelSend.expr, this.env);
        RegIndex dataReg = channelSend.expr.regIndex;
        BType dataType = channelSend.expr.type;
        UTF8CPEntry dataCPEntry = new UTF8CPEntry(this.generateSig(new BType[] { dataType }));
        Operand dataCPIndex = getOperand(this.currentPkgInfo.addCPEntry(dataCPEntry));
        argRegs[i++] = dataReg;
        argRegs[i] = dataCPIndex;

        emit(InstructionCodes.CHNSEND, argRegs);
    }

    private void visitChannelReceive(BLangWorkerReceive channelReceive) {
        // CHNRECEIVE hasKey, (keyTypeCPIndex, keyRegIndex), channelName, receiverTypeCPIndex, receiverRegIndex
        Operand[] chnReceiveArgRegs;
        int i = 0;
        if (channelReceive.keyExpr != null) {
            genNode(channelReceive.keyExpr, this.env);
            chnReceiveArgRegs = new Operand[6];
            chnReceiveArgRegs[i++] = getOperand(true);
            RegIndex keyReg = channelReceive.keyExpr.regIndex;
            BType keyType = channelReceive.keyExpr.type;
            UTF8CPEntry keyCPEntry = new UTF8CPEntry(this.generateSig(new BType[] { keyType }));
            Operand keyCPIndex = getOperand(this.currentPkgInfo.addCPEntry(keyCPEntry));
            chnReceiveArgRegs[i++] = keyCPIndex;
            chnReceiveArgRegs[i++] = keyReg;
        } else {
            chnReceiveArgRegs = new Operand[4];
            chnReceiveArgRegs[i++] = getOperand(false);
        }

        int chnName = addUTF8CPEntry(currentPkgInfo, channelReceive.env.enclPkg.symbol.name + "."
                + channelReceive.getWorkerName().getValue());
        chnReceiveArgRegs[i++] = getOperand(chnName);

        BType bType = channelReceive.type;
        RegIndex regIndex = calcAndGetExprRegIndex(channelReceive);

        UTF8CPEntry sigCPEntry = new UTF8CPEntry(this.generateSig(new BType[] { bType }));
        Operand sigCPIndex = getOperand(currentPkgInfo.addCPEntry(sigCPEntry));
        chnReceiveArgRegs[i++] = sigCPIndex;
        chnReceiveArgRegs[i] = regIndex;

        emit(InstructionCodes.CHNRECEIVE, chnReceiveArgRegs);
    }

    private void visitFunctionPointerLoad(BLangExpression fpExpr, BInvokableSymbol funcSymbol) {
        int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, funcSymbol.pkgID);
        int funcNameCPIndex = addUTF8CPEntry(currentPkgInfo, funcSymbol.name.value);
        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgRefCPIndex, funcNameCPIndex);
        Operand typeCPIndex = getTypeCPIndex(funcSymbol.type);

        int funcRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);
        RegIndex nextIndex = calcAndGetExprRegIndex(fpExpr);
        Operand[] operands;
        if (NodeKind.FIELD_BASED_ACCESS_EXPR == fpExpr.getKind()) {
            operands = calcObjectAttachedFPOperands((BLangStructFunctionVarRef) fpExpr, typeCPIndex, funcRefCPIndex,
                    nextIndex);
            //Separating this with a instruction code, so that at runtime, actual function can be loaded
            emit(InstructionCodes.VFPLOAD, operands);
            return;
        }
        if (NodeKind.LAMBDA == fpExpr.getKind()) {
            operands = calcClosureOperands(((BLangLambdaFunction) fpExpr).function, funcRefCPIndex, nextIndex,
                    typeCPIndex);
        } else {
            operands = new Operand[4];
            operands[0] = getOperand(funcRefCPIndex);
            operands[1] = nextIndex;
            operands[2] = typeCPIndex;
            operands[3] = new Operand(0);
        }
        emit(InstructionCodes.FPLOAD, operands);
    }

    /**
     * This is a helper method which calculate the required additional indexes needed for object attached function
     * invoked as a function pointer scenario.
     */
    private Operand[] calcObjectAttachedFPOperands(BLangStructFunctionVarRef fpExpr, Operand typeCPIndex,
                                                   int funcRefCPIndex, RegIndex nextIndex) {
        Operand[] operands = new Operand[6];
        operands[0] = getOperand(funcRefCPIndex);
        operands[1] = nextIndex;
        operands[2] = typeCPIndex;
        operands[3] = getOperand(2);
        operands[4] = getOperand(((BVarSymbol) ((BLangVariableReference) fpExpr.expr).symbol).type.tag);
        operands[5] = getOperand(((BVarSymbol) ((BLangVariableReference) fpExpr.expr).symbol).varIndex.value);
        return operands;
    }

    /**
     * This is a helper method which calculate the required additional indexes needed for closure scenarios.
     * If there are no closure variables found, then this method will just add 0 as the termination index
     * which is used at runtime.
     */
    private Operand[] calcClosureOperands(BLangFunction function, int funcRefCPIndex, RegIndex nextIndex,
                                          Operand typeCPIndex) {
        List<Operand> closureOperandList = new ArrayList<>();


        for (BVarSymbol symbol : function.symbol.params) {
            if (!symbol.closure || function.requiredParams.stream().anyMatch(var -> var.symbol.equals(symbol))) {
                continue;
            }
            Operand type = new Operand(symbol.type.tag);
            Operand index = new Operand(symbol.varIndex.value);
            closureOperandList.add(type);
            closureOperandList.add(index);
        }
        Operand[] operands;
        if (!closureOperandList.isEmpty()) {
            Operand[] closureIndexes = closureOperandList.toArray(new Operand[]{});
            operands = new Operand[4 + closureIndexes.length];
            operands[0] = getOperand(funcRefCPIndex);
            operands[1] = nextIndex;
            operands[2] = typeCPIndex;
            operands[3] = getOperand(closureIndexes.length);
            System.arraycopy(closureIndexes, 0, operands, 4, closureIndexes.length);
        } else {
            operands = new Operand[4];
            operands[0] = getOperand(funcRefCPIndex);
            operands[1] = nextIndex;
            operands[2] = typeCPIndex;
            operands[3] = getOperand(0);
        }
        return operands;
    }

    private void generateFinallyInstructions(BLangStatement statement) {
        generateFinallyInstructions(statement, new NodeKind[0]);
    }

    private void generateFinallyInstructions(BLangStatement statement, NodeKind... expectedParentKinds) {
        int returnInFinallyToIPPlaceHolder = -2;
        BLangStatement current = statement;
        boolean hasReturn = false;
        while (current != null && current.statementLink.parent != null) {
            BLangStatement parent = current.statementLink.parent.statement;
            for (NodeKind expected : expectedParentKinds) {
                if (expected == parent.getKind()) {
                    return;
                }
            }

            if (current.getKind() == NodeKind.RETURN) {
                hasReturn = true;
            }

            if (NodeKind.TRY == parent.getKind()) {
                boolean returnInFinally = false;
                if (hasReturn) {
                    //if generateFinallyInstructions is called due to a return statement being present in a try,
                    // catch block, maintain the current IP (before code generation for the finally block)
                    // to use as the toIP of the error table entry
                    if (!tryCatchErrorRangeToIPStack.isEmpty()) {
                        if (tryCatchErrorRangeToIPStack.peek() != returnInFinallyToIPPlaceHolder) {
                            tryCatchErrorRangeToIPStack.push(nextIP() - 1);
                        } else {
                            returnInFinally = true;
                        }
                    } else {
                        tryCatchErrorRangeToIPStack.push(nextIP() - 1);
                    }
                }

                BLangTryCatchFinally tryCatchFinally = (BLangTryCatchFinally) parent;
                if (tryCatchFinally.finallyBody != null && current != tryCatchFinally.finallyBody) {
                    tryCatchErrorRangeToIPStack.push(returnInFinallyToIPPlaceHolder);
                    genNode(tryCatchFinally.finallyBody, env);
                    tryCatchErrorRangeToIPStack.pop();
                }

                if (!returnInFinally) {
                    tryCatchErrorRangeFromIPStack.push(nextIP() + 1);
                }
            } else if (NodeKind.LOCK == parent.getKind()) {
                BLangLock lockNode = (BLangLock) parent;
                if (!lockNode.lockVariables.isEmpty() || !lockNode.fieldVariables.isEmpty()) {
                    Operand[] operands = getUnlockOperands(lockNode);
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

        // If the namespace is defined within a callable unit, object or a record, get the URI index
        // in the local var registry. Otherwise get the URI index in the global var registry.
        int ownerTag = namespaceSymbol.owner.tag;
        if ((ownerTag & SymTag.INVOKABLE) == SymTag.INVOKABLE ||
                (ownerTag & SymTag.OBJECT) == SymTag.OBJECT ||
                (ownerTag & SymTag.RECORD) == SymTag.RECORD) {
            return (RegIndex) namespaceSymbol.nsURIIndex;
        }

        int pkgIndex = addPackageRefCPEntry(this.currentPkgInfo, namespaceSymbol.owner.pkgID);
        RegIndex index = getRegIndex(TypeTags.STRING);
        emit(InstructionCodes.SGLOAD, getOperand(pkgIndex), namespaceSymbol.nsURIIndex, index);
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

            int opcode = getOpcode(TypeTags.STRING, InstructionCodes.IEQ);
            RegIndex conditionExprIndex = getRegIndex(TypeTags.BOOLEAN);
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
        // TODO: remove RegIndex arg
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
            RegIndex localNameRegIndex = getRegIndex(TypeTags.STRING);
            RegIndex uriRegIndex = getRegIndex(TypeTags.STRING);
            emit(InstructionCodes.S2QNAME, startTagNameRegIndex, localNameRegIndex, uriRegIndex);

            startTagNameRegIndex = getRegIndex(TypeTags.XML);
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

    private void addDocAttachmentAttrInfo(MarkdownDocAttachment docAttachment, AttributeInfoPool attrInfoPool) {
        if (docAttachment == null) {
            return;
        }
        int docAttrIndex = addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.DOCUMENT_ATTACHMENT_ATTRIBUTE.value());
        int descCPIndex = addUTF8CPEntry(currentPkgInfo, docAttachment.description);
        DocumentationAttributeInfo docAttributeInfo = new DocumentationAttributeInfo(docAttrIndex, descCPIndex);

        for (MarkdownDocAttachment.Parameter docAttribute : docAttachment.parameters) {
            int nameCPIndex = addUTF8CPEntry(currentPkgInfo, docAttribute.name);
            int descriptionCPIndex = addUTF8CPEntry(currentPkgInfo, docAttribute.description);
            ParameterDocumentInfo paramDocInfo = new ParameterDocumentInfo(nameCPIndex, descriptionCPIndex);
            docAttributeInfo.paramDocInfoList.add(paramDocInfo);
        }

        if (docAttachment.returnValueDescription != null) {
            docAttributeInfo.returnParameterDescriptionCPIndex = addUTF8CPEntry(currentPkgInfo,
                    docAttachment.returnValueDescription);
        }

        attrInfoPool.addAttributeInfo(AttributeInfo.Kind.DOCUMENT_ATTACHMENT_ATTRIBUTE, docAttributeInfo);
    }

    private void addParameterAttributeInfo(BInvokableSymbol funcSymbol, CallableUnitInfo callableUnitInfo) {
        // Add required params, defaultable params and rest params counts
        int paramAttrIndex =
                addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.PARAMETERS_ATTRIBUTE.value());
        ParameterAttributeInfo paramAttrInfo =
                new ParameterAttributeInfo(paramAttrIndex);
        paramAttrInfo.requiredParamsCount = funcSymbol.params.size();
        paramAttrInfo.defaultableParamsCount = funcSymbol.defaultableParams.size();
        paramAttrInfo.restParamCount = funcSymbol.restParam != null ? 1 : 0;
        callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.PARAMETERS_ATTRIBUTE, paramAttrInfo);

        // Add parameter default values
        addParameterDefaultValues(funcSymbol, callableUnitInfo);
    }

    private void addWorkerSendInsAttributeInfo(LinkedHashSet<String> sendIns, CallableUnitInfo callableUnitInfo) {
        int workerSendInsAttNameIndex = addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.WORKER_SEND_INS.value());
        WorkerSendInsAttributeInfo workerSendInsAttributeInfo =
                new WorkerSendInsAttributeInfo(workerSendInsAttNameIndex);
        int[] sendInIndexes = new int[sendIns.size()];
        int i = 0;
        for (String worker : sendIns) {
            sendInIndexes[i++] = addUTF8CPEntry(currentPkgInfo, worker);
        }

        workerSendInsAttributeInfo.sendsIns = sendInIndexes;
        callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.WORKER_SEND_INS, workerSendInsAttributeInfo);
    }

    private void addParameterDefaultValues(BInvokableSymbol funcSymbol, CallableUnitInfo callableUnitInfo) {
        int paramDefaultsAttrNameIndex =
                addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.PARAMETER_DEFAULTS_ATTRIBUTE.value());
        ParamDefaultValueAttributeInfo paramDefaulValAttrInfo =
                new ParamDefaultValueAttributeInfo(paramDefaultsAttrNameIndex);

        // Only named parameters can have default values.
        for (BVarSymbol param : funcSymbol.defaultableParams) {
            DefaultValue defaultVal = getDefaultValue(param.defaultValue.getValue());
            paramDefaulValAttrInfo.addParamDefaultValueInfo(defaultVal);
        }

        callableUnitInfo.addAttributeInfo(AttributeInfo.Kind.PARAMETER_DEFAULTS_ATTRIBUTE, paramDefaulValAttrInfo);
    }

    private int getValueToRefTypeCastOpcode(int typeTag) {
        int opcode;
        switch (typeTag) {
            case TypeTags.INT:
                opcode = InstructionCodes.I2ANY;
                break;
            case TypeTags.BYTE:
                opcode = InstructionCodes.BI2ANY;
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
            default:
                opcode = InstructionCodes.NOP;
                break;
        }
        return opcode;
    }

    private int getRefToValueTypeCastOpcode(int typeTag) {
        int opcode;
        switch (typeTag) {
            case TypeTags.INT:
                opcode = InstructionCodes.ANY2I;
                break;
            case TypeTags.BYTE:
                opcode = InstructionCodes.ANY2BI;
                break;
            case TypeTags.FLOAT:
                opcode = InstructionCodes.ANY2F;
                break;
            case TypeTags.DECIMAL:
                opcode = InstructionCodes.ANY2D;
                break;
            case TypeTags.STRING:
                opcode = InstructionCodes.ANY2S;
                break;
            case TypeTags.BOOLEAN:
                opcode = InstructionCodes.ANY2B;
                break;
            default:
                opcode = InstructionCodes.NOP;
                break;
        }
        return opcode;
    }

    private void addPackageInfo(BPackageSymbol packageSymbol, ProgramFile programFile) {
        BLangPackage pkgNode = this.packageCache.get(packageSymbol.pkgID);
        if (pkgNode == null) {
            // This is a package loaded from a BALO
            packageSymbol.imports.forEach(importPkdSymbol -> addPackageInfo(importPkdSymbol, programFile));
            if (!programFile.packageFileMap.containsKey(packageSymbol.pkgID.toString())
                    && !packageSymbol.pkgID.orgName.equals(Names.BUILTIN_ORG)) {
                programFile.packageFileMap.put(packageSymbol.pkgID.toString(), packageSymbol.packageFile);
            }
            return;
        }

        HashSet<BLangImportPackage> importPkgList = new HashSet<>();
        importPkgList.addAll(pkgNode.imports);

        // If tests are enabled then get the imports of the testable package as well.
        String testsEnabled = options.get(CompilerOptionName.SKIP_TESTS);
        if (testsEnabled != null && testsEnabled.equals(Constants.SKIP_TESTS)) {
            pkgNode.getTestablePkgs().forEach(testablePackage -> importPkgList.addAll(testablePackage.imports));
        }
        importPkgList.forEach(importPkdNode -> addPackageInfo(importPkdNode.symbol, programFile));
        if (!programFile.packageFileMap.containsKey(packageSymbol.pkgID.toString())
                && !packageSymbol.pkgID.orgName.equals(Names.BUILTIN_ORG)) {
            programFile.packageFileMap.put(packageSymbol.pkgID.toString(), packageSymbol.packageFile);
        }
    }

    private byte[] getPackageBinaryContent(BLangPackage pkgNode) {
        try {
            return PackageInfoWriter.getPackageBinary(this.currentPkgInfo);
        } catch (IOException e) {
            // This code will not be executed under normal condition
            throw new BLangCompilerException("failed to generate bytecode for module '" +
                    pkgNode.packageID + "': " + e.getMessage(), e);
        }
    }

    private void storeStructField(BLangExpression fieldAccessExpr, Operand varRefRegIndex, Operand keyRegIndex) {
        int opcode;
        opcode = getValueToRefTypeCastOpcode(fieldAccessExpr.type.tag);
        if (opcode == InstructionCodes.NOP) {
            // If the field is ref type, then struct field store will pick the value from ref reg.
            emit(InstructionCodes.MAPSTORE, varRefRegIndex, keyRegIndex, fieldAccessExpr.regIndex);
        } else {
            // Cast the value to ref type and put it in ref reg.
            // Then struct field store will take the value from ref reg.
            RegIndex valueRegIndex = getRegIndex(TypeTags.ANY);
            emit(opcode, fieldAccessExpr.regIndex, valueRegIndex);
            emit(InstructionCodes.MAPSTORE, varRefRegIndex, keyRegIndex, valueRegIndex);
        }
    }

    private void loadStructField(BLangExpression fieldAccessExpr, Operand varRefRegIndex, Operand keyRegIndex,
                                 int except) {
        IntegerCPEntry exceptCPEntry = new IntegerCPEntry(except);
        Operand exceptOp = getOperand(currentPkgInfo.addCPEntry(exceptCPEntry));
        int opcode = getRefToValueTypeCastOpcode(fieldAccessExpr.type.tag);
        if (opcode == InstructionCodes.NOP) {
            emit(InstructionCodes.MAPLOAD, varRefRegIndex, keyRegIndex, calcAndGetExprRegIndex(fieldAccessExpr),
                    exceptOp);
        } else {
            // Get the value from struct, and put it in ref reg. Then cast the ref value to the value type
            RegIndex targetRegIndex = getRegIndex(TypeTags.ANY);
            emit(InstructionCodes.MAPLOAD, varRefRegIndex, keyRegIndex, targetRegIndex, exceptOp);
            emit(opcode, targetRegIndex, calcAndGetExprRegIndex(fieldAccessExpr));
        }
    }

    private void setVariableScopeStart(LocalVariableInfo localVarInfo, BLangVariable varNode) {
        if (varNode.pos != null) {
            localVarInfo.scopeStartLineNumber = varNode.pos.sLine;
        }
    }

    private void setVariableScopeEnd(LocalVariableInfo localVarInfo, BLangVariable varNode) {
        if ((varNode.parent == null) && (varNode.pos != null)) {
            localVarInfo.scopeEndLineNumber = varNode.pos.eLine;
            return;
        }
        BLangNode parentNode = varNode;
        while ((parentNode.parent != null)) {
            parentNode = parentNode.parent;
            if ((parentNode.getKind().equals(NodeKind.BLOCK)) && (parentNode.parent != null) &&
                    (parentNode.parent.pos != null)) {
                localVarInfo.scopeEndLineNumber = parentNode.parent.pos.eLine;
                break;
            }
        }
    }
}

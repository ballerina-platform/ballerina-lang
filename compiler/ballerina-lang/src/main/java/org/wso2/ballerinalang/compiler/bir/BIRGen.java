/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.BlockNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.bir.model.ArgumentState;
import org.wso2.ballerinalang.compiler.bir.model.BIRArgument;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotation;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationAttachment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRConstant;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRLockDetailsHolder;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.ConstValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.BinaryOp;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.UnaryOP;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.bir.optimizer.BIROptimizer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDynamicArgExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStringAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangTableAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangTupleLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock.BLangLockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock.BLangUnLockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerUtils;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;

import static org.ballerinalang.model.tree.NodeKind.CLASS_DEFN;
import static org.ballerinalang.model.tree.NodeKind.INVOCATION;
import static org.wso2.ballerinalang.compiler.desugar.AnnotationDesugar.ANNOTATION_DATA;
import static org.wso2.ballerinalang.compiler.util.Constants.DESUGARED_MAPPING_CONSTR_KEY;

/**
 * Lower the AST to BIR.
 *
 * @since 0.980.0
 */
public class BIRGen extends BLangNodeVisitor {

    private static final CompilerContext.Key<BIRGen> BIR_GEN =
            new CompilerContext.Key<>();

    public static final String DEFAULT_WORKER_NAME = "function";
    public static final String CLONE_READ_ONLY = "cloneReadOnly";
    private BIRGenEnv env;
    private Names names;
    private final SymbolTable symTable;
    private BIROptimizer birOptimizer;
    private final Types types;

    // Required variables to generate code for assignment statements
    private boolean varAssignment = false;
    private Map<BSymbol, BIRTypeDefinition> typeDefs = new LinkedHashMap<>();
    private BlockNode currentBlock;
    private Map<BlockNode, List<BIRVariableDcl>> varDclsByBlock = new HashMap<>();
    // This is a global variable cache
    public Map<BSymbol, BIRGlobalVariableDcl> globalVarMap = new HashMap<>();

    // This map is used to create dependencies for imported module global variables
    private Map<BSymbol, BIRGlobalVariableDcl> dummyGlobalVarMapForLocks = new HashMap<>();

    // This is to cache the lockstmt to BIR Lock
    private Map<BLangLockStmt, BIRTerminator.Lock> lockStmtMap = new HashMap<>();

    // Required variables for Mock function implementation
    private static final String MOCK_ANNOTATION_DELIMITER = "#";
    private static final String MOCK_FN_DELIMITER = "~";

    private Unifier unifier;

    private BirScope currentScope;

    public static BIRGen getInstance(CompilerContext context) {
        BIRGen birGen = context.get(BIR_GEN);
        if (birGen == null) {
            birGen = new BIRGen(context);
        }

        return birGen;
    }

    private BIRGen(CompilerContext context) {
        context.put(BIR_GEN, this);

        this.names = Names.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.birOptimizer = BIROptimizer.getInstance(context);
        this.unifier = new Unifier();
        this.types = Types.getInstance(context);
    }

    public BLangPackage genBIR(BLangPackage astPkg) {
        BIRPackage birPkg = new BIRPackage(astPkg.pos, astPkg.packageID.orgName, astPkg.packageID.pkgName,
                astPkg.packageID.name, astPkg.packageID.version, astPkg.packageID.sourceFileName);

        astPkg.symbol.bir = birPkg; //TODO try to remove this

        this.env = new BIRGenEnv(birPkg);
        astPkg.accept(this);

        this.birOptimizer.optimizePackage(birPkg);
        if (!astPkg.moduleContextDataHolder.skipTests() && astPkg.hasTestablePackage()) {
            BIRPackage testBirPkg = new BIRPackage(astPkg.pos, astPkg.packageID.orgName, astPkg.packageID.pkgName,
                    astPkg.packageID.name, astPkg.packageID.version, astPkg.packageID.sourceFileName);
            this.env = new BIRGenEnv(testBirPkg);
            astPkg.accept(this);
            astPkg.getTestablePkgs().forEach(testPkg -> {
                visitBuiltinFunctions(testPkg, testPkg.initFunction);
                visitBuiltinFunctions(testPkg, testPkg.startFunction);
                visitBuiltinFunctions(testPkg, testPkg.stopFunction);
                // remove imports of the main module from testable module
                for (BLangImportPackage mod : astPkg.imports) {
                    testPkg.imports.remove(mod);
                }
                testPkg.accept(this);
                this.birOptimizer.optimizePackage(testBirPkg);
                testPkg.symbol.bir = testBirPkg;
                Map<String, String> mockFunctionMap = astPkg.getTestablePkg().getMockFunctionNamesMap();
                if (!mockFunctionMap.isEmpty()) {
                    replaceMockedFunctions(testBirPkg, mockFunctionMap, astPkg.packageID);
                }
            });
        }

        setEntryPoints(astPkg);
        return astPkg;
    }

    private void setEntryPoints(BLangPackage pkgNode) {
        BLangFunction mainFunc = getMainFunction(pkgNode);
        if (mainFunc != null || listenerDeclarationFound(pkgNode.getGlobalVariables()) || !pkgNode.services.isEmpty()) {
            pkgNode.symbol.entryPointExists = true;
        }
    }

    private boolean listenerDeclarationFound(List<BLangVariable> globalVars) {
        for (BLangVariable globalVar : globalVars) {
            if (Symbols.isFlagOn(globalVar.symbol.flags, Flags.LISTENER)) {
                return true;
            }
        }
        return false;
    }

    private BLangFunction getMainFunction(BLangPackage pkgNode) {
        for (BLangFunction funcNode : pkgNode.functions) {
            if (CompilerUtils.isMainFunction(funcNode)) {
                return funcNode;
            }
        }
        return null;
    }

    private void visitBuiltinFunctions(BLangPackage pkgNode, BLangFunction function) {
        if (Symbols.isFlagOn(pkgNode.symbol.flags, Flags.TESTABLE)) {
            String funcName = function.getName().value;
            String builtinFuncName = funcName.substring(funcName.indexOf("<") + 1, funcName.indexOf(">"));
            String modifiedFuncName = funcName.replace(builtinFuncName, "test" + builtinFuncName);
            function.name.setValue(modifiedFuncName);
            Name functionName = names.fromString(modifiedFuncName);
            function.originalFuncSymbol.name = functionName;
            function.symbol.name = functionName;
        }
    }

    private void replaceMockedFunctions(BIRPackage birPkg, Map<String, String> mockFunctionMap, PackageID packageID) {
        // Replace Mocked function calls in every function
        replaceFunctions(birPkg.functions, mockFunctionMap, packageID);

        // Replace Mocked Function calls in every service
        if (birPkg.typeDefs.size() != 0) {
            for (BIRTypeDefinition typeDef : birPkg.typeDefs) {
                if (typeDef.type instanceof BObjectType) {
                    replaceFunctions(typeDef.attachedFuncs, mockFunctionMap, packageID);
                }
            }
        }
    }

    private void replaceFunctions(List<BIRFunction> functionList,
                                  Map<String, String> mockFunctionMap,
                                  PackageID packageID) {
        // Loop through all defined BIRFunctions in functionList
        for (BIRFunction function : functionList) {
            List<BIRBasicBlock> basicBlocks = function.basicBlocks;
            for (BIRBasicBlock basicBlock : basicBlocks) {
                BIRTerminator bbTerminator = basicBlock.terminator;
                if (bbTerminator.kind.equals(InstructionKind.CALL)) {
                    //We get the callee and the name and generate 'calleepackage#name'
                    BIRTerminator.Call callTerminator = (BIRTerminator.Call) bbTerminator;

                    String functionKey = callTerminator.calleePkg.toString() + MOCK_ANNOTATION_DELIMITER
                            + callTerminator.name.toString();

                    String legacyKey = callTerminator.calleePkg.toString() + MOCK_FN_DELIMITER
                            + callTerminator.name.toString();

                    // If function in basic block exists in the MockFunctionMap
                    if (mockFunctionMap.containsKey(functionKey)) {
                        // Replace the function call with the equivalent $MOCK_ substitiute
                        String desugarFunction = "$MOCK_" + callTerminator.name.getValue();
                        callTerminator.name = new Name(desugarFunction);
                        callTerminator.calleePkg = packageID;
                    } else if (mockFunctionMap.get(legacyKey) != null) {
                        // Just "get" the reference. If this doesnt work then it doesnt exist
                        String mockfunctionName = mockFunctionMap.get(legacyKey);
                        callTerminator.name = new Name(mockfunctionName);
                        callTerminator.calleePkg = packageID;
                    }
                }
            }
        }
    }

    // Nodes

    @Override
    public void visit(BLangPackage astPkg) {
        // Lower function nodes in AST to bir function nodes.
        // TODO handle init, start, stop functions
        astPkg.imports.forEach(impPkg -> impPkg.accept(this));
        astPkg.constants.forEach(astConst -> astConst.accept(this));
        astPkg.typeDefinitions.forEach(astTypeDef -> astTypeDef.accept(this));
        generateClassDefinitions(astPkg.topLevelNodes);
        astPkg.globalVars.forEach(astGlobalVar -> astGlobalVar.accept(this));
        astPkg.initFunction.accept(this);
        astPkg.startFunction.accept(this);
        astPkg.stopFunction.accept(this);
        astPkg.functions.forEach(astFunc -> astFunc.accept(this));
        astPkg.annotations.forEach(astAnn -> astAnn.accept(this));
        astPkg.services.forEach(service -> service.accept(this));
    }

    private void generateClassDefinitions(List<TopLevelNode> topLevelNodes) {
        for (TopLevelNode topLevelNode : topLevelNodes) {
            if (topLevelNode.getKind() == CLASS_DEFN) {
                ((BLangClassDefinition) topLevelNode).accept(this);
            }
        }
    }

    @Override
    public void visit(BLangTypeDefinition astTypeDefinition) {
        BType type = getDefinedType(astTypeDefinition);
        Name displayName = astTypeDefinition.symbol.name;
        if (type.tag == TypeTags.RECORD) {
            BRecordType recordType = (BRecordType) type;
            if (recordType.shouldPrintShape()) {
                displayName = new Name(recordType.toString());
            }
        }

        BIRTypeDefinition typeDef = new BIRTypeDefinition(astTypeDefinition.pos,
                                                          astTypeDefinition.symbol.name,
                                                          astTypeDefinition.symbol.flags,
                                                          astTypeDefinition.isBuiltinTypeDef,
                                                          type,
                                                          new ArrayList<>(),
                                                          astTypeDefinition.symbol.origin.toBIROrigin(),
                                                          displayName,
                                                          astTypeDefinition.symbol.originalName);
        if (astTypeDefinition.symbol.tag == SymTag.TYPE_DEF) {
            if (type.tsymbol.owner == astTypeDefinition.symbol.owner) {
                typeDefs.put(astTypeDefinition.symbol.type.tsymbol, typeDef);
                typeDef.referenceType = ((BTypeDefinitionSymbol) astTypeDefinition.symbol).referenceType;
            } else {
                BTypeReferenceType referenceType = ((BTypeDefinitionSymbol) astTypeDefinition.symbol).referenceType;
                typeDef.referenceType = referenceType;

                if (referenceType != null) {
                    typeDef.type = referenceType;
                }

                typeDefs.put(astTypeDefinition.symbol, typeDef);
            }
        } else {
            //enum symbols
            typeDefs.put(astTypeDefinition.symbol, typeDef);
        }
        this.env.enclPkg.typeDefs.add(typeDef);
        typeDef.index = this.env.enclPkg.typeDefs.size() - 1;

        typeDef.setMarkdownDocAttachment(astTypeDefinition.symbol.markdownDocumentation);
        populateBIRAnnotAttachments(astTypeDefinition.annAttachments, typeDef.annotAttachments, this.env);

        if (astTypeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE ||
                astTypeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            BLangStructureTypeNode typeNode = (BLangStructureTypeNode) astTypeDefinition.typeNode;
            for (BLangType typeRef : typeNode.typeRefs) {
                typeDef.referencedTypes.add(typeRef.getBType());
            }
        }

        BSymbol typeSymbol = astTypeDefinition.symbol.tag == SymTag.TYPE_DEF
                ? astTypeDefinition.symbol.type.tsymbol : astTypeDefinition.symbol;
        // Write referenced functions, if this is an abstract-object
        if (typeSymbol.tag != SymTag.OBJECT || !Symbols.isFlagOn(typeSymbol.flags, Flags.CLASS)) {
            return;
        }

        for (BAttachedFunction func : ((BObjectTypeSymbol) typeSymbol).referencedFunctions) {
            if (!Symbols.isFlagOn(func.symbol.flags, Flags.INTERFACE)) {
                return;
            }

            BInvokableSymbol funcSymbol = func.symbol;
            BIRFunction birFunc = new BIRFunction(astTypeDefinition.pos, func.funcName, funcSymbol.flags, func.type,
                                                  names.fromString(DEFAULT_WORKER_NAME), 0,
                                                  funcSymbol.origin.toBIROrigin());

            if (funcSymbol.receiverSymbol != null) {
                birFunc.receiver = getSelf(funcSymbol.receiverSymbol
                );
            }

            birFunc.setMarkdownDocAttachment(funcSymbol.markdownDocumentation);

            int defaultableParamsCount = 0;
            birFunc.argsCount = funcSymbol.params.size() + defaultableParamsCount +
                    (funcSymbol.restParam != null ? 1 : 0);
            funcSymbol.params.forEach(requiredParam -> addParam(birFunc, requiredParam, astTypeDefinition.pos));
            if (funcSymbol.restParam != null) {
                addRestParam(birFunc, funcSymbol.restParam, astTypeDefinition.pos);
            }

            birFunc.returnVariable = new BIRVariableDcl(astTypeDefinition.pos, funcSymbol.retType,
                    this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.RETURN, null);
            birFunc.localVars.add(0, birFunc.returnVariable);

            typeDef.attachedFuncs.add(birFunc);
        }
    }

    private BType getDefinedType(BLangTypeDefinition astTypeDefinition) {
        BType nodeType = astTypeDefinition.typeNode.getBType();
        // Consider: type DE distinct E;
        // For distinct types, the type defined by typeDefStmt (DE) is different from type used to define it (E).
        if (types.getReferredType(nodeType).tag == TypeTags.ERROR) {
            return astTypeDefinition.symbol.type;
        }
        return nodeType;
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        BIRTypeDefinition typeDef = new BIRTypeDefinition(classDefinition.pos,
                                                          classDefinition.symbol.name,
                                                          classDefinition.symbol.originalName,
                                                          classDefinition.symbol.flags,
                                                          false,
                                                          classDefinition.getBType(),
                                                          new ArrayList<>(),
                                                          classDefinition.symbol.origin.toBIROrigin());
        typeDefs.put(classDefinition.symbol, typeDef);
        this.env.enclPkg.typeDefs.add(typeDef);
        typeDef.index = this.env.enclPkg.typeDefs.size() - 1;

        typeDef.setMarkdownDocAttachment(classDefinition.symbol.markdownDocumentation);

        for (BLangType typeRef : classDefinition.typeRefs) {
            typeDef.referencedTypes.add(typeRef.getBType());
        }

        populateBIRAnnotAttachments(classDefinition.annAttachments, typeDef.annotAttachments, this.env);

        for (BAttachedFunction func : ((BObjectTypeSymbol) classDefinition.symbol).referencedFunctions) {
            BInvokableSymbol funcSymbol = func.symbol;

            BIRFunction birFunc = new BIRFunction(classDefinition.pos, func.funcName, funcSymbol.flags, func.type,
                    names.fromString(DEFAULT_WORKER_NAME), 0, funcSymbol.origin.toBIROrigin());

            if (funcSymbol.receiverSymbol != null) {
                birFunc.receiver = getSelf(funcSymbol.receiverSymbol);
            }

            birFunc.setMarkdownDocAttachment(funcSymbol.markdownDocumentation);

            int defaultableParamsCount = 0;
            birFunc.argsCount = funcSymbol.params.size() + defaultableParamsCount +
                    (funcSymbol.restParam != null ? 1 : 0);
            funcSymbol.params.forEach(requiredParam -> addParam(birFunc, requiredParam, classDefinition.pos));
            if (funcSymbol.restParam != null) {
                addRestParam(birFunc, funcSymbol.restParam, classDefinition.pos);
            }

            birFunc.returnVariable = new BIRVariableDcl(classDefinition.pos, funcSymbol.retType,
                                                        this.env.nextLocalVarId(names), VarScope.FUNCTION,
                                                        VarKind.RETURN, null);
            birFunc.localVars.add(0, birFunc.returnVariable);

            typeDef.attachedFuncs.add(birFunc);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        BServiceSymbol symbol = (BServiceSymbol) serviceNode.symbol;
        List<String> attachPoint = symbol.getAbsResourcePath().orElse(null);
        String attachPointLiteral = symbol.getAttachPointStringLiteral().orElse(null);
        BIRNode.BIRServiceDeclaration serviceDecl =
                new BIRNode.BIRServiceDeclaration(attachPoint, attachPointLiteral, symbol.getListenerTypes(),
                        symbol.name, symbol.getAssociatedClassSymbol().name, symbol.type,
                        symbol.origin, symbol.flags, symbol.pos);
        serviceDecl.setMarkdownDocAttachment(symbol.markdownDocumentation);
        this.env.enclPkg.serviceDecls.add(serviceDecl);
    }

    @Override
    public void visit(BLangConstant astConstant) {
        BConstantSymbol constantSymbol = astConstant.symbol;
        Name constName = constantSymbol.name;
        Name constOriginalName = constantSymbol.getOriginalName();
        BType type = constantSymbol.type;

        // Get the value of the constant.
        ConstValue constantValue = getBIRConstantVal(constantSymbol.value);

        // Create a new constant info object.
        BIRConstant birConstant = new BIRConstant(astConstant.pos, constName, constOriginalName, constantSymbol.flags,
                                                  type, constantValue, constantSymbol.origin.toBIROrigin());
        birConstant.constValue = constantValue;

        birConstant.setMarkdownDocAttachment(astConstant.symbol.markdownDocumentation);

        // Add the constant to the package.
        this.env.enclPkg.constants.add(birConstant);
    }

    private ConstValue getBIRConstantVal(BLangConstantValue constValue) {
        if (constValue.type.tag == TypeTags.MAP) {
            Map<String, ConstValue> mapConstVal = new HashMap<>();
            ((Map<String, BLangConstantValue>) constValue.value)
                    .forEach((key, value) -> mapConstVal.put(key, getBIRConstantVal(value)));
            return new ConstValue(mapConstVal, constValue.type);
        }

        return new ConstValue(constValue.value, constValue.type);
    }

    @Override
    public void visit(BLangImportPackage impPkg) {
        this.env.enclPkg.importModules.add(new BIRNode.BIRImportModule(impPkg.pos, impPkg.symbol.pkgID.orgName,
                impPkg.symbol.pkgID.name, impPkg.symbol.pkgID.version));
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangFunction astFunc) {
        BInvokableType type = astFunc.symbol.getType();

        boolean isTypeAttachedFunction = astFunc.flagSet.contains(Flag.ATTACHED) &&
                !typeDefs.containsKey(astFunc.receiver.getBType().tsymbol);

        Name workerName = names.fromIdNode(astFunc.defaultWorkerName);

        this.env.unlockVars.push(new BIRLockDetailsHolder());
        BIRFunction birFunc;

        if (isTypeAttachedFunction) {
            Name funcName = names.fromString(astFunc.symbol.name.value);
            birFunc = new BIRFunction(astFunc.pos, funcName,
                                      names.fromString(astFunc.symbol.getOriginalName().value),
                                      astFunc.symbol.flags, type, workerName, astFunc.sendsToThis.size(),
                                      astFunc.symbol.origin.toBIROrigin());
        } else {
            Name funcName = getFuncName(astFunc.symbol);
            birFunc = new BIRFunction(astFunc.pos, funcName, names.fromString(astFunc.symbol.getOriginalName().value),
                                      astFunc.symbol.flags, type, workerName,
                                      astFunc.sendsToThis.size(), astFunc.symbol.origin.toBIROrigin());
        }
        this.currentScope = new BirScope(0, null);
        if (astFunc.receiver != null) {
            BIRFunctionParameter birVarDcl = new BIRFunctionParameter(astFunc.pos, astFunc.receiver.getBType(),
                                                                      this.env.nextLocalVarId(names), VarScope.FUNCTION,
                                                                      VarKind.ARG, astFunc.receiver.name.value, false);
            this.env.symbolVarMap.put(astFunc.receiver.symbol, birVarDcl);
        }

        if (astFunc.receiver != null) {
            birFunc.receiver = getSelf(astFunc.receiver.symbol);
        }

        birFunc.setMarkdownDocAttachment(astFunc.symbol.markdownDocumentation);

        //create channelDetails array
        int i = 0;
        for (String channelName : astFunc.sendsToThis) {
            birFunc.workerChannels[i] = new BIRNode.ChannelDetails(channelName, astFunc.defaultWorkerName.value
                    .equals(DEFAULT_WORKER_NAME), isWorkerSend(channelName, astFunc.defaultWorkerName.value));
            i++;
        }

        // Populate annotation attachments on external in BIRFunction node
        if (astFunc.hasBody() && astFunc.body.getKind() == NodeKind.EXTERN_FUNCTION_BODY) {
            populateBIRAnnotAttachments(((BLangExternalFunctionBody) astFunc.body).annAttachments,
                                        birFunc.annotAttachments, this.env);
        }
        // Populate annotation attachments on function in BIRFunction node
        populateBIRAnnotAttachments(astFunc.annAttachments, birFunc.annotAttachments, this.env);

        // Populate annotation attachments on return type
        populateBIRAnnotAttachments(astFunc.returnTypeAnnAttachments, birFunc.returnTypeAnnots, this.env);

        birFunc.argsCount = astFunc.requiredParams.size()
                + (astFunc.restParam != null ? 1 : 0) + astFunc.paramClosureMap.size();
        if (astFunc.flagSet.contains(Flag.ATTACHED) && typeDefs.containsKey(astFunc.receiver.getBType().tsymbol)) {
            typeDefs.get(astFunc.receiver.getBType().tsymbol).attachedFuncs.add(birFunc);
        } else {
            this.env.enclPkg.functions.add(birFunc);
        }

        this.env.enclFunc = birFunc;

        // TODO: Return variable with NIL type should be written to BIR
        // Special %0 location for storing return values
        BType retType = unifier.build(astFunc.symbol.type.getReturnType());
        birFunc.returnVariable = new BIRVariableDcl(astFunc.pos, retType, this.env.nextLocalVarId(names),
                                                    VarScope.FUNCTION, VarKind.RETURN, null);
        birFunc.localVars.add(0, birFunc.returnVariable);

        //add closure vars
        astFunc.paramClosureMap.forEach((k, v) -> addRequiredParam(birFunc, v, astFunc.pos));

        // Create variable declaration for function params
        astFunc.requiredParams.forEach(requiredParam -> addParam(birFunc, requiredParam));
        if (astFunc.restParam != null) {
            addRestParam(birFunc, astFunc.restParam.symbol, astFunc.restParam.pos);
        }

        if (astFunc.interfaceFunction || Symbols.isNative(astFunc.symbol)) {
            this.env.clear();
            return;
        }

        // Create the entry basic block
        BIRBasicBlock entryBB = new BIRBasicBlock(this.env.nextBBId(names));
        this.env.enclBasicBlocks = birFunc.basicBlocks;
        birFunc.basicBlocks.add(entryBB);
        this.env.enclBB = entryBB;
        addToTrapStack(entryBB);

        astFunc.body.accept(this);
        birFunc.basicBlocks.add(this.env.returnBB);

        // Due to the current algorithm, some basic blocks will not contain any instructions or a terminator.
        // These basic blocks will be remove by the optimizer, but for now just add a return terminator
        BIRBasicBlock enclBB = this.env.enclBB;
        if (enclBB.instructions.size() == 0 && enclBB.terminator == null && this.env.returnBB != null) {
            enclBB.terminator = new BIRTerminator.GOTO(null, this.env.returnBB, this.currentScope);
        }

        this.env.clear();

        this.env.unlockVars.clear();

        // Rearrange basic block ids.
        birFunc.parameters.values().forEach(basicBlocks -> basicBlocks.forEach(bb -> bb.id = this.env.nextBBId(names)));
        birFunc.basicBlocks.forEach(bb -> bb.id = this.env.nextBBId(names));
        // Rearrange error entries.
        birFunc.errorTable.sort(Comparator.comparingInt(o -> Integer.parseInt(o.trapBB.id.value.replace("bb", ""))));
        birFunc.dependentGlobalVars = astFunc.symbol.dependentGlobalVars.stream()
                .map(varSymbol -> this.globalVarMap.get(varSymbol)).collect(Collectors.toSet());
        this.env.clear();
    }

    private BIRVariableDcl getSelf(BSymbol receiver) {
        BIRVariableDcl self = this.env.symbolVarMap.get(receiver);
        if (self == null) {
            return new BIRVariableDcl(null, receiver.type, receiver.name,
                                      VarScope.FUNCTION, VarKind.SELF, null);
        }
        self.kind = VarKind.SELF;
        self.name = new Name("%self");

        return self;
    }

    @Override
    public void visit(BLangBlockFunctionBody astBody) {
        BIRBasicBlock endLoopEndBB = this.env.enclLoopEndBB;
        BlockNode prevBlock = this.currentBlock;
        this.currentBlock = astBody;
        this.varDclsByBlock.computeIfAbsent(astBody, k -> new ArrayList<>());

        for (BLangStatement astStmt : astBody.stmts) {
            astStmt.accept(this);
        }

        List<BIRVariableDcl> varDecls = this.varDclsByBlock.get(astBody);
        for (BIRVariableDcl birVariableDcl : varDecls) {
            birVariableDcl.endBB = this.env.enclBasicBlocks.get(this.env.enclBasicBlocks.size() - 1);
        }
        this.env.enclLoopEndBB = endLoopEndBB;
        this.currentBlock = prevBlock;
    }

    private BIRBasicBlock beginBreakableBlock(Location pos, BLangBlockStmt.FailureBreakMode mode) {
        BIRBasicBlock blockBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(blockBB);
        this.env.enclBasicBlocks.add(blockBB);

        // Insert a GOTO instruction as the terminal instruction into current basic block.
        this.env.enclBB.terminator = new BIRTerminator.GOTO(pos, blockBB, this.currentScope);

        BIRBasicBlock blockEndBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(blockEndBB);

        blockBB.terminator = new BIRTerminator.GOTO(pos, blockEndBB, this.currentScope);

        this.env.enclBB = blockBB;
        if (mode == BLangBlockStmt.FailureBreakMode.BREAK_WITHIN_BLOCK) {
            this.env.enclInnerOnFailEndBB = blockEndBB;
        } else {
            this.env.enclOnFailEndBB = blockEndBB;
        }
        this.env.unlockVars.push(new BIRLockDetailsHolder());
        return blockEndBB;
    }

    private void endBreakableBlock(BIRBasicBlock blockEndBB) {
        this.env.unlockVars.pop();
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(null, blockEndBB, this.currentScope);
        }
        this.env.enclBasicBlocks.add(blockEndBB);
        this.env.enclBB = blockEndBB;
    }

    @Override
    public void visit(BLangAnnotationAttachment astAnnotAttach) {
        // ------------------------------------------------------
        // In the current implementation of the compiler, there two possible values for `astAnnotAttach.expr`
        //  1) null
        //  2) BLangRecordLiteral
        // In this implementation, we support only the BLangRecordLiteral expressions
        //   which have only key:BLangLiteral key/value pairs
        // ------------------------------------------------------
        BIRAnnotationValue annotationValue;
        if (astAnnotAttach.expr == null) {
            annotationValue = new BIRNode.BIRAnnotationLiteralValue(symTable.booleanType, true);
        } else {
            if (!isCompileTimeAnnotationValue(astAnnotAttach.expr)) {
                return;
            }
            annotationValue = createAnnotationValue(astAnnotAttach.expr);
        }

        Name annotTagRef = this.names.fromIdNode(astAnnotAttach.annotationName);
        BIRAnnotationAttachment annotAttachment = new BIRAnnotationAttachment(astAnnotAttach.pos, annotTagRef);
        annotAttachment.packageID = astAnnotAttach.annotationSymbol.pkgID;
        annotAttachment.annotValues.add(annotationValue);
        this.env.enclAnnotAttachments.add(annotAttachment);
    }

    private boolean isCompileTimeAnnotationValue(BLangExpression expression) {

        BLangExpression expr = unwrapAnnotationExpressionFromCloneReadOnly(expression);

        switch (expr.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
                return true;
            case RECORD_LITERAL_EXPR:
                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expr;
                for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
                    if (!isCompileTimeAnnotationValue(((BLangRecordKeyValueField) field).valueExpr)) {
                        return false;
                    }
                }
                return true;
            case ARRAY_LITERAL_EXPR:
                BLangArrayLiteral arrayLiteral = (BLangArrayLiteral) expr;
                for (BLangExpression bLangExpr : arrayLiteral.exprs) {
                    if (!isCompileTimeAnnotationValue(bLangExpr)) {
                        return false;
                    }
                }
                return true;
            case TYPE_CONVERSION_EXPR:
                return isCompileTimeAnnotationValue(((BLangTypeConversionExpr) expr).expr);
            case STATEMENT_EXPRESSION:
                BLangStatementExpression stmtExpr = (BLangStatementExpression) expr;
                List<BLangStatement> stmts = ((BLangBlockStmt) stmtExpr.stmt).stmts;

                if (!((BLangLocalVarRef) stmtExpr.expr).varSymbol.name.value.startsWith(DESUGARED_MAPPING_CONSTR_KEY)) {
                    return false;
                }

                for (int i = 1; i < stmts.size(); i++) {
                    BLangAssignment assignmentStmt = (BLangAssignment) stmts.get(i);

                    if (!isCompileTimeAnnotationValue(((BLangIndexBasedAccess) assignmentStmt.varRef).indexExpr) ||
                            !isCompileTimeAnnotationValue(assignmentStmt.expr)) {
                        return false;
                    }
                }

                return true;
            default:
                return false;
        }
    }

    private BLangExpression unwrapAnnotationExpressionFromCloneReadOnly(BLangExpression expr) {
        if (expr.getKind() == INVOCATION) {
            BLangInvocation invocation = (BLangInvocation) expr;
            if (invocation.name.getValue().equals(CLONE_READ_ONLY)) {
                return invocation.expr;
            }
        }
        return expr;
    }

    private BIRAnnotationValue createAnnotationValue(BLangExpression expression) {
        BLangExpression expr = unwrapAnnotationExpressionFromCloneReadOnly(expression);
        // TODO Compile time literal constants
        switch (expr.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
                return createAnnotationLiteralValue((BLangLiteral) expr);
            case RECORD_LITERAL_EXPR:
                return createAnnotationRecordValue((BLangRecordLiteral) expr);
            case ARRAY_LITERAL_EXPR:
                return createAnnotationArrayValue((BLangArrayLiteral) expr);
            case TYPE_CONVERSION_EXPR:
                return createAnnotationValue(((BLangTypeConversionExpr) expr).expr);
            case STATEMENT_EXPRESSION:
                return createAnnotationRecordValue((BLangStatementExpression) expr);
            default:
                // This following line will not be executed
                throw new IllegalStateException("Invalid annotation value expression kind: " + expr.getKind());
        }
    }

    private BIRNode.BIRAnnotationRecordValue createAnnotationRecordValue(BLangRecordLiteral recordLiteral) {
        Map<String, BIRAnnotationValue> annotValueEntryMap = new HashMap<>();
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            BLangRecordKeyValueField keyValuePair = (BLangRecordKeyValueField) field;

            BLangLiteral keyLiteral = (BLangLiteral) keyValuePair.key.expr;
            String entryKey = (String) keyLiteral.value;
            BIRAnnotationValue annotationValue = createAnnotationValue(keyValuePair.valueExpr);
            annotValueEntryMap.put(entryKey, annotationValue);
        }
        return new BIRNode.BIRAnnotationRecordValue(recordLiteral.getBType(), annotValueEntryMap);
    }

    private BIRNode.BIRAnnotationRecordValue createAnnotationRecordValue(BLangStatementExpression stmtExpr) {
        Map<String, BIRAnnotationValue> annotValueEntryMap = new HashMap<>();

        List<BLangStatement> stmts = ((BLangBlockStmt) stmtExpr.stmt).stmts;

        for (int i = 1; i < stmts.size(); i++) {
            BLangAssignment assignmentStmt = (BLangAssignment) stmts.get(i);
            annotValueEntryMap.put(
                    (String) ((BLangLiteral) ((BLangIndexBasedAccess) assignmentStmt.varRef).indexExpr).value,
                    createAnnotationValue(assignmentStmt.expr));
        }
        return new BIRNode.BIRAnnotationRecordValue(stmtExpr.getBType(), annotValueEntryMap);
    }


    private BIRNode.BIRAnnotationArrayValue createAnnotationArrayValue(BLangArrayLiteral arrayLiteral) {
        BIRAnnotationValue[] annotValues = new BIRAnnotationValue[arrayLiteral.exprs.size()];
        for (int exprIndex = 0; exprIndex < arrayLiteral.exprs.size(); exprIndex++) {
            annotValues[exprIndex] = createAnnotationValue(arrayLiteral.exprs.get(exprIndex));
        }
        return new BIRNode.BIRAnnotationArrayValue(arrayLiteral.getBType(), annotValues);
    }

    private BIRNode.BIRAnnotationLiteralValue createAnnotationLiteralValue(BLangLiteral literalValue) {
        return new BIRNode.BIRAnnotationLiteralValue(literalValue.getBType(), literalValue.value);
    }

    @Override
    public void visit(BLangAnnotation astAnnotation) {
        BAnnotationSymbol annSymbol = (BAnnotationSymbol) astAnnotation.symbol;

        BIRAnnotation birAnn = new BIRAnnotation(astAnnotation.pos, annSymbol.name, annSymbol.originalName,
                                                 annSymbol.flags, annSymbol.points,
                                                 annSymbol.attachedType == null ? symTable.trueType :
                                                         annSymbol.attachedType, annSymbol.origin.toBIROrigin());
        birAnn.setMarkdownDocAttachment(annSymbol.markdownDocumentation);
        this.env.enclPkg.annotations.add(birAnn);
    }


    private boolean isWorkerSend(String chnlName, String workerName) {
        return chnlName.split("->")[0].equals(workerName);
    }

    @Override
    public void visit(BLangLambdaFunction lambdaExpr) {
        //fpload instruction
        BIRVariableDcl tempVarLambda = new BIRVariableDcl(lambdaExpr.pos, lambdaExpr.getBType(),
                                                          this.env.nextLocalVarId(names), VarScope.FUNCTION,
                                                          VarKind.TEMP, null);
        this.env.enclFunc.localVars.add(tempVarLambda);
        BIROperand lhsOp = new BIROperand(tempVarLambda);
        Name funcName = getFuncName(lambdaExpr.function.symbol);

        List<BIRVariableDcl> params = new ArrayList<>();

        lambdaExpr.function.requiredParams.forEach(param -> {

            BIRVariableDcl birVarDcl = new BIRVariableDcl(param.pos, param.symbol.type,
                    this.env.nextLambdaVarId(names), VarScope.FUNCTION, VarKind.ARG, param.name.value);
            params.add(birVarDcl);
        });

        BLangSimpleVariable restParam = lambdaExpr.function.restParam;
        if (restParam != null) {
            BIRVariableDcl birVarDcl = new BIRVariableDcl(restParam.pos, restParam.symbol.type,
                    this.env.nextLambdaVarId(names), VarScope.FUNCTION, VarKind.ARG, null);
            params.add(birVarDcl);
        }
        setScopeAndEmit(
                new BIRNonTerminator.FPLoad(lambdaExpr.pos, lambdaExpr.function.symbol.pkgID, funcName, lhsOp, params,
                                            getClosureMapOperands(lambdaExpr), lambdaExpr.getBType(),
                                            lambdaExpr.function.symbol.strandName,
                                            lambdaExpr.function.symbol.schedulerPolicy));
        this.env.targetOperand = lhsOp;
    }

    private List<BIROperand> getClosureMapOperands(BLangLambdaFunction lambdaExpr) {
        List<BIROperand> closureMaps = new ArrayList<>();

        lambdaExpr.function.paramClosureMap.forEach((k, v) -> {
            BVarSymbol symbol = lambdaExpr.enclMapSymbols.get(k);
            if (symbol == null) {
                symbol = lambdaExpr.paramMapSymbolsOfEnclInvokable.get(k);
            }
            BIROperand varRef = new BIROperand(this.env.symbolVarMap.get(symbol));
            closureMaps.add(varRef);
        });

        return closureMaps;
    }

    private Name getFuncName(BInvokableSymbol symbol) {
        if (symbol.receiverSymbol == null) {
            return names.fromString(symbol.name.value);
        }

        int offset = symbol.receiverSymbol.type.tsymbol.name.value.length() + 1;
        String attachedFuncName = symbol.name.value;
        return names.fromString(attachedFuncName.substring(offset));
    }

    private void addParam(BIRFunction birFunc, BLangVariable functionParam) {
        addParam(birFunc, functionParam.symbol, functionParam.expr, functionParam.pos);
    }

    private void addParam(BIRFunction birFunc, BVarSymbol paramSymbol, Location pos) {
        addParam(birFunc, paramSymbol, null, pos);
    }

    private void addParam(BIRFunction birFunc, BVarSymbol paramSymbol, BLangExpression defaultValExpr,
                          Location pos) {
        BIRFunctionParameter birVarDcl = new BIRFunctionParameter(pos, paramSymbol.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.ARG,
                paramSymbol.name.value, defaultValExpr != null);

        birFunc.localVars.add(birVarDcl);

        List<BIRBasicBlock> bbsOfDefaultValueExpr = new ArrayList<>();
        if (defaultValExpr != null) {
            // Parameter has a default value expression.
            BIRBasicBlock defaultExprBB = new BIRBasicBlock(this.env.nextBBId(names));
            bbsOfDefaultValueExpr.add(defaultExprBB);
            this.env.enclBB = defaultExprBB;
            this.env.enclBasicBlocks = bbsOfDefaultValueExpr;
            defaultValExpr.accept(this);

            // Create a variable reference for the function param and setScopeAndEmit move instruction.
            BIROperand varRef = new BIROperand(birVarDcl);
            setScopeAndEmit(new Move(birFunc.pos, this.env.targetOperand, varRef));

            this.env.enclBB.terminator = new BIRTerminator.Return(birFunc.pos);
        }
        BIRParameter parameter = new BIRParameter(pos, paramSymbol.name, paramSymbol.flags);
        birFunc.requiredParams.add(parameter);
        birFunc.parameters.put(birVarDcl, bbsOfDefaultValueExpr);

        // We maintain a mapping from variable symbol to the bir_variable declaration.
        // This is required to pull the correct bir_variable declaration for variable references.
        this.env.symbolVarMap.put(paramSymbol, birVarDcl);
    }

    private void addRestParam(BIRFunction birFunc, BVarSymbol paramSymbol, Location pos) {
        BIRFunctionParameter birVarDcl = new BIRFunctionParameter(pos, paramSymbol.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.ARG, paramSymbol.name.value, false);
        birFunc.parameters.put(birVarDcl, new ArrayList<>());
        birFunc.localVars.add(birVarDcl);

        birFunc.restParam = new BIRParameter(pos, paramSymbol.name, paramSymbol.flags);

        // We maintain a mapping from variable symbol to the bir_variable declaration.
        // This is required to pull the correct bir_variable declaration for variable references.
        this.env.symbolVarMap.put(paramSymbol, birVarDcl);
    }

    private void addRequiredParam(BIRFunction birFunc, BVarSymbol paramSymbol, Location pos) {
        BIRFunctionParameter birVarDcl = new BIRFunctionParameter(pos, paramSymbol.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.ARG, paramSymbol.name.value, false);
        birFunc.parameters.put(birVarDcl, new ArrayList<>());
        birFunc.localVars.add(birVarDcl);

        BIRParameter parameter = new BIRParameter(pos, paramSymbol.name, paramSymbol.flags);
        birFunc.requiredParams.add(parameter);

        // We maintain a mapping from variable symbol to the bir_variable declaration.
        // This is required to pull the correct bir_variable declaration for variable references.
        this.env.symbolVarMap.put(paramSymbol, birVarDcl);
    }

    // Statements

    @Override
    public void visit(BLangBlockStmt astBlockStmt) {
        BIRBasicBlock blockEndBB = null;
        BIRBasicBlock currentOnFailEndBB = this.env.enclOnFailEndBB;
        BIRBasicBlock currentWithinOnFailEndBB = this.env.enclInnerOnFailEndBB;
        BlockNode prevBlock = this.currentBlock;
        this.currentBlock = astBlockStmt;
        this.varDclsByBlock.computeIfAbsent(astBlockStmt, k -> new ArrayList<>());
        if (astBlockStmt.failureBreakMode != BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE) {
            blockEndBB = beginBreakableBlock(astBlockStmt.pos, astBlockStmt.failureBreakMode);
        }
        for (BLangStatement astStmt : astBlockStmt.stmts) {
            astStmt.accept(this);
        }
        if (astBlockStmt.failureBreakMode != BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE) {
            endBreakableBlock(blockEndBB);
        }
        this.varDclsByBlock.get(astBlockStmt).forEach(birVariableDcl ->
                birVariableDcl.endBB = this.env.enclBasicBlocks.get(this.env.enclBasicBlocks.size() - 1)
        );
        this.env.enclInnerOnFailEndBB = currentWithinOnFailEndBB;
        this.env.enclOnFailEndBB = currentOnFailEndBB;
        this.currentBlock = prevBlock;
    }

    @Override
    public void visit(BLangFail failNode) {
        if (failNode.expr == null) {
            if (this.env.enclInnerOnFailEndBB != null) {
                this.env.enclBB.terminator = new BIRTerminator.GOTO(failNode.pos, this.env.enclInnerOnFailEndBB,
                        this.currentScope);
            }
            return;
        }

        BIRLockDetailsHolder toUnlock = this.env.unlockVars.peek();
        if (!toUnlock.isEmpty()) {
            BIRBasicBlock goToBB = new BIRBasicBlock(this.env.nextBBId(names));
            this.env.enclBasicBlocks.add(goToBB);
            this.env.enclBB.terminator = new BIRTerminator.GOTO(failNode.pos, goToBB, this.currentScope);
            this.env.enclBB = goToBB;
        }

        int numLocks = toUnlock.size();
        while (numLocks > 0) {
            BIRBasicBlock unlockBB = new BIRBasicBlock(this.env.nextBBId(names));
            this.env.enclBasicBlocks.add(unlockBB);
            BIRTerminator.Unlock unlock = new BIRTerminator.Unlock(null, unlockBB, this.currentScope);
            this.env.enclBB.terminator = unlock;
            unlock.relatedLock = toUnlock.getLock(numLocks - 1);
            this.env.enclBB = unlockBB;
            numLocks--;
        }

        // Create a basic block for the on fail clause.
        BIRBasicBlock onFailBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(onFailBB);
        this.env.enclBasicBlocks.add(onFailBB);

        // Insert a GOTO instruction as the terminal instruction into current basic block.
        this.env.enclBB.terminator = new BIRTerminator.GOTO(failNode.pos, onFailBB, this.currentScope);

        // Visit condition expression
        this.env.enclBB = onFailBB;
        failNode.exprStmt.accept(this);
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(failNode.pos, this.env.enclOnFailEndBB,
                    this.currentScope);
        }

        // Statements after fail expression are unreachable, hence ignored
        BIRBasicBlock ignoreBlock = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(ignoreBlock);
        ignoreBlock.terminator = new BIRTerminator.GOTO(failNode.pos, this.env.enclOnFailEndBB, this.currentScope);
        this.env.enclBasicBlocks.add(ignoreBlock);
        this.env.enclBB = ignoreBlock;
    }


    @Override
    public void visit(BLangSimpleVariableDef astVarDefStmt) {
        VarKind kind;
        if (astVarDefStmt.var.symbol.origin == SymbolOrigin.VIRTUAL) {
            kind = VarKind.SYNTHETIC;
        } else {
            kind = VarKind.LOCAL;
        }
        BIRVariableDcl birVarDcl = new BIRVariableDcl(astVarDefStmt.pos, astVarDefStmt.var.symbol.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, kind, astVarDefStmt.var.name.value);
        birVarDcl.startBB = this.env.enclBB;
        this.varDclsByBlock.get(this.currentBlock).add(birVarDcl);
        this.env.enclFunc.localVars.add(birVarDcl);
        // We maintain a mapping from variable symbol to the bir_variable declaration.
        // This is required to pull the correct bir_variable declaration for variable references.
        this.env.symbolVarMap.put(astVarDefStmt.var.symbol, birVarDcl);

        BirScope newScope = new BirScope(this.currentScope.id + 1, this.currentScope);
        birVarDcl.insScope = newScope;
        this.currentScope = newScope;

        if (astVarDefStmt.var.expr == null) {
            return;
        }

        // Visit the rhs expression.
        astVarDefStmt.var.expr.accept(this);

        // Create a variable reference and
        BIROperand varRef = new BIROperand(birVarDcl);
        setScopeAndEmit(new Move(astVarDefStmt.pos, this.env.targetOperand, varRef));
        birVarDcl.insOffset = this.env.enclBB.instructions.size() - 1;
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        String name = ANNOTATION_DATA.equals(varNode.symbol.name.value) ? ANNOTATION_DATA : varNode.name.value;
        String originalName = ANNOTATION_DATA.equals(varNode.symbol.getOriginalName().value) ?
                                                                    ANNOTATION_DATA : varNode.name.originalValue;
        BIRGlobalVariableDcl birVarDcl = new BIRGlobalVariableDcl(varNode.pos, varNode.symbol.flags,
                                                                  varNode.symbol.type, varNode.symbol.pkgID,
                                                                  names.fromString(name),
                                                                  names.fromString(originalName), VarScope.GLOBAL,
                                                                  VarKind.GLOBAL, varNode.name.value,
                                                                  varNode.symbol.origin.toBIROrigin());
        birVarDcl.setMarkdownDocAttachment(varNode.symbol.markdownDocumentation);

        this.env.enclPkg.globalVars.add(birVarDcl);

        this.globalVarMap.put(varNode.symbol, birVarDcl);
        env.enclPkg.isListenerAvailable |= Symbols.isFlagOn(varNode.symbol.flags, Flags.LISTENER);
    }

    @Override
    public void visit(BLangAssignment astAssignStmt) {
        astAssignStmt.expr.accept(this);

        this.varAssignment = true;
        astAssignStmt.varRef.accept(this);
        this.varAssignment = false;
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr.accept(this);
        if (this.env.returnBB == null && exprStmtNode.expr.getKind() == NodeKind.INVOCATION &&
                types.isNeverTypeOrStructureTypeWithARequiredNeverMember(exprStmtNode.expr.getBType())) {
            BIRBasicBlock returnBB = new BIRBasicBlock(this.env.nextBBId(names));
            returnBB.terminator = new BIRTerminator.Return(exprStmtNode.pos);
            this.env.returnBB = returnBB;
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        createCall(invocationExpr, false);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocation) {
        createCall(actionInvocation, false);
    }

    @Override
    public void visit(BLangStatementExpression statementExpression) {
        statementExpression.stmt.accept(this);
        statementExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation invocationExpr) {
        createCall(invocationExpr, true);
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation invocation) {
        invocation.functionPointerInvocation = true;
        createCall(invocation, false);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(worker -> worker.accept(this));
    }

    @Override
    public void visit(BLangWorkerReceive workerReceive) {
        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(thenBB);
        String channel = workerReceive.workerIdentifier.value + "->" + env.enclFunc.workerName.value;

        BIRVariableDcl tempVarDcl = new BIRVariableDcl(workerReceive.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        boolean isOnSameStrand = DEFAULT_WORKER_NAME.equals(this.env.enclFunc.workerName.value);

        this.env.enclBB.terminator = new BIRTerminator.WorkerReceive(workerReceive.pos, names.fromString(channel),
                                                                     lhsOp, isOnSameStrand, thenBB, this.currentScope);

        this.env.enclBasicBlocks.add(thenBB);
        this.env.enclBB = thenBB;
    }

    @Override
    public void visit(BLangWorkerSend workerSend) {
        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(thenBB);
        this.env.enclBasicBlocks.add(thenBB);
        workerSend.expr.accept(this);

        String channelName = this.env.enclFunc.workerName.value + "->" + workerSend.workerIdentifier.value;
        boolean isOnSameStrand = DEFAULT_WORKER_NAME.equals(this.env.enclFunc.workerName.value);

        this.env.enclBB.terminator = new BIRTerminator.WorkerSend(
                workerSend.pos, names.fromString(channelName), this.env.targetOperand, isOnSameStrand, false, null,
                thenBB, this.currentScope);

        this.env.enclBB = thenBB;
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSend) {
        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(thenBB);
        syncSend.expr.accept(this);
        BIROperand dataOp = this.env.targetOperand;

        BIRVariableDcl tempVarDcl = new BIRVariableDcl(syncSend.receive.matchingSendsError,
                                                       this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        String channelName = this.env.enclFunc.workerName.value + "->" + syncSend.workerIdentifier.value;
        boolean isOnSameStrand = DEFAULT_WORKER_NAME.equals(this.env.enclFunc.workerName.value);

        this.env.enclBB.terminator = new BIRTerminator.WorkerSend(
                syncSend.pos, names.fromString(channelName), dataOp, isOnSameStrand, true, lhsOp,
                thenBB, this.currentScope);

        this.env.enclBasicBlocks.add(thenBB);
        this.env.enclBB = thenBB;
    }

    @Override
    public void visit(BLangWorkerFlushExpr flushExpr) {
        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(thenBB);

        //create channelDetails array
        BIRNode.ChannelDetails[] channels = new BIRNode.ChannelDetails[flushExpr.workerIdentifierList.size()];
        int i = 0;
        for (BLangIdentifier workerIdentifier : flushExpr.workerIdentifierList) {
            String channelName = this.env.enclFunc.workerName.value + "->" + workerIdentifier.value;
            boolean isOnSameStrand = DEFAULT_WORKER_NAME.equals(this.env.enclFunc.workerName.value);
            channels[i] = new BIRNode.ChannelDetails(channelName, isOnSameStrand, true);
            i++;
        }

        BIRVariableDcl tempVarDcl = new BIRVariableDcl(flushExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        this.env.enclBB.terminator = new BIRTerminator.Flush(flushExpr.pos, channels, lhsOp, thenBB,
                this.currentScope);
        this.env.enclBasicBlocks.add(thenBB);
        this.env.enclBB = thenBB;
    }

    private void createWait(BLangWaitExpr waitExpr) {

        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(thenBB);
        // This only supports wait for single future and alternate wait
        List<BIROperand> exprList = new ArrayList<>();

        waitExpr.exprList.forEach(expr -> {
            expr.accept(this);
            exprList.add(this.env.targetOperand);
        });

        BIRVariableDcl tempVarDcl = new BIRVariableDcl(waitExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        this.env.enclBB.terminator = new BIRTerminator.Wait(waitExpr.pos, exprList, lhsOp, thenBB, this.currentScope);

        this.env.enclBasicBlocks.add(thenBB);
        this.env.enclBB = thenBB;
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        BIRVariableDcl tempVarError = new BIRVariableDcl(errorConstructorExpr.getBType(),
                                                         this.env.nextLocalVarId(names), VarScope.FUNCTION,
                                                         VarKind.TEMP);

        this.env.enclFunc.localVars.add(tempVarError);
        BIROperand lhsOp = new BIROperand(tempVarError);

        this.env.targetOperand = lhsOp;
        List<BLangExpression> positionalArgs = errorConstructorExpr.positionalArgs;
        positionalArgs.get(0).accept(this);
        BIROperand messageOp = this.env.targetOperand;

        positionalArgs.get(1).accept(this);
        BIROperand causeOp = this.env.targetOperand;

        errorConstructorExpr.errorDetail.accept(this);
        BIROperand detailsOp = this.env.targetOperand;

        BIRNonTerminator.NewError newError =
                new BIRNonTerminator.NewError(errorConstructorExpr.pos, errorConstructorExpr.getBType(), lhsOp,
                                              messageOp, causeOp, detailsOp);
        setScopeAndEmit(newError);
        this.env.targetOperand = lhsOp;
    }

    private void createCall(BLangInvocation invocationExpr, boolean isVirtual) {
        List<BLangExpression> requiredArgs = invocationExpr.requiredArgs;
        List<BLangExpression> restArgs = invocationExpr.restArgs;
        List<BIRArgument> args = new ArrayList<>();
        boolean transactional = Symbols.isFlagOn(invocationExpr.symbol.flags, Flags.TRANSACTIONAL);

        for (BLangExpression requiredArg : requiredArgs) {
            if (requiredArg.getKind() == NodeKind.DYNAMIC_PARAM_EXPR) {
                ((BLangDynamicArgExpr) requiredArg).conditionalArgument.accept(this);
                BIROperand conditionalArg = this.env.targetOperand;
                ((BLangDynamicArgExpr) requiredArg).condition.accept(this);
                BIROperand condition = this.env.targetOperand;
                args.add(new BIRArgument(ArgumentState.CONDITIONALLY_PROVIDED, conditionalArg.variableDcl, condition));
            } else if (requiredArg.getKind() != NodeKind.IGNORE_EXPR) {
                requiredArg.accept(this);
                args.add(new BIRArgument(ArgumentState.PROVIDED, this.env.targetOperand.variableDcl));
            } else {
                BIRVariableDcl birVariableDcl =
                        new BIRVariableDcl(requiredArg.getBType(), new Name("_"), VarScope.FUNCTION, VarKind.ARG);
                birVariableDcl.ignoreVariable = true;
                args.add(new BIRArgument(ArgumentState.NOT_PROVIDED, birVariableDcl));
            }
        }

        // seems like restArgs.size() is always 1 or 0, but lets iterate just in case
        for (BLangExpression arg : restArgs) {
            arg.accept(this);
            args.add(new BIRArgument(ArgumentState.PROVIDED, this.env.targetOperand.variableDcl));
        }

        BIROperand fp = null;
        if (invocationExpr.functionPointerInvocation) {
            invocationExpr.expr.accept(this);
            fp = this.env.targetOperand;
        }

        // Create a temporary variable to store the return operation result.
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(invocationExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        // Lets create a block the jump after successful function return
        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(thenBB);
        this.env.enclBasicBlocks.add(thenBB);


        // TODO: make vCall a new instruction to avoid package id in vCall
        if (invocationExpr.functionPointerInvocation) {
            this.env.enclBB.terminator = new BIRTerminator.FPCall(invocationExpr.pos, InstructionKind.FP_CALL,
                    fp, args, lhsOp, invocationExpr.async, transactional, thenBB, this.currentScope);
        } else if (invocationExpr.async) {
            BInvokableSymbol bInvokableSymbol = (BInvokableSymbol) invocationExpr.symbol;
            List<BIRAnnotationAttachment> calleeAnnots = getStatementAnnotations(bInvokableSymbol.annAttachments,
                    this.env);

            List<BIRAnnotationAttachment> annots = getStatementAnnotations(invocationExpr.annAttachments, this.env);
            this.env.enclBB.terminator = new BIRTerminator.AsyncCall(invocationExpr.pos, InstructionKind.ASYNC_CALL,
                    isVirtual, invocationExpr.symbol.pkgID, getFuncName((BInvokableSymbol) invocationExpr.symbol),
                    args, lhsOp, thenBB, annots, calleeAnnots, bInvokableSymbol.getFlags(), this.currentScope);
        } else {
            BInvokableSymbol bInvokableSymbol = (BInvokableSymbol) invocationExpr.symbol;
            List<BIRAnnotationAttachment> calleeAnnots = getStatementAnnotations(bInvokableSymbol.annAttachments,
                    this.env);

            this.env.enclBB.terminator = new BIRTerminator.Call(invocationExpr.pos, InstructionKind.CALL, isVirtual,
                    invocationExpr.symbol.pkgID, getFuncName((BInvokableSymbol) invocationExpr.symbol), args, lhsOp,
                    thenBB, calleeAnnots, bInvokableSymbol.getFlags(), this.currentScope);
        }
        this.env.enclBB = thenBB;
    }

    @Override
    public void visit(BLangReturn astReturnStmt) {
        astReturnStmt.expr.accept(this);
        BIROperand retVarRef = new BIROperand(this.env.enclFunc.returnVariable);
        setScopeAndEmit(new Move(astReturnStmt.pos, this.env.targetOperand, retVarRef));

        // Check whether this function already has a returnBB.
        // A given function can have only one BB that has a return instruction.
        if (this.env.returnBB == null) {
            // If not create one
            BIRBasicBlock returnBB = new BIRBasicBlock(this.env.nextBBId(names));
            addToTrapStack(returnBB);
            returnBB.terminator = new BIRTerminator.Return(astReturnStmt.pos);
            this.env.returnBB = returnBB;
        }
        if (this.env.enclBB.terminator == null) {
            this.env.unlockVars.forEach(s -> {
                int i = s.size();
                while (i > 0) {
                    BIRBasicBlock unlockBB = new BIRBasicBlock(this.env.nextBBId(names));
                    this.env.enclBasicBlocks.add(unlockBB);
                    BIRTerminator.Unlock unlock = new BIRTerminator.Unlock(null,  unlockBB, this.currentScope);
                    this.env.enclBB.terminator = unlock;
                    unlock.relatedLock = s.getLock(i - 1);
                    this.env.enclBB = unlockBB;
                    i--;
                }
            });

            this.env.enclBB.terminator = new BIRTerminator.GOTO(astReturnStmt.pos, this.env.returnBB,
                    this.currentScope);
            BIRBasicBlock nextBB = new BIRBasicBlock(this.env.nextBBId(names));
            this.env.enclBasicBlocks.add(nextBB);
            this.env.enclBB = nextBB;
            addToTrapStack(nextBB);
        }
    }

    @Override
    public void visit(BLangPanic panicNode) {
        panicNode.expr.accept(this);
        // Some functions will only have panic but we need to add return for them to make current algorithm work.
        if (this.env.returnBB == null) {
            BIRBasicBlock returnBB = new BIRBasicBlock(this.env.nextBBId(names));
            addToTrapStack(returnBB);
            returnBB.terminator = new BIRTerminator.Return(panicNode.pos);
            this.env.returnBB = returnBB;
        }
        this.env.enclBB.terminator = new BIRTerminator.Panic(panicNode.pos, this.env.targetOperand, this.currentScope);

        // This basic block will contain statement that comes right after this 'if' statement.
        BIRBasicBlock unlockBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(unlockBB);
        this.env.enclBasicBlocks.add(unlockBB);
        this.env.enclBB = unlockBB;
    }

    @Override
    public void visit(BLangIf astIfStmt) {
        astIfStmt.expr.accept(this);
        BIROperand ifExprResult = this.env.targetOperand;

        // Create the basic block for the if-then block.
        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(thenBB);
        this.env.enclBasicBlocks.add(thenBB);

        // This basic block will contain statement that comes right after this 'if' statement.
        BIRBasicBlock nextBB = new BIRBasicBlock(this.env.nextBBId(names));

        // Add the branch instruction to the current basic block.
        // This is the end of the current basic block.
        BIRTerminator.Branch branchIns = new BIRTerminator.Branch(astIfStmt.pos, ifExprResult, thenBB, null,
                this.currentScope);
        this.env.enclBB.terminator = branchIns;

        // Visit the then-block
        this.env.enclBB = thenBB;
        astIfStmt.body.accept(this);

        // If a terminator statement has not been set for the then-block then just add it.
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(null, nextBB, this.currentScope);
        }

        // Check whether there exists an else-if or an else block.
        if (astIfStmt.elseStmt != null) {
            // Create a basic block for the else block.
            BIRBasicBlock elseBB = new BIRBasicBlock(this.env.nextBBId(names));
            addToTrapStack(elseBB);
            this.env.enclBasicBlocks.add(elseBB);
            branchIns.falseBB = elseBB;

            // Visit the else block. This could be an else-if block or an else block.
            this.env.enclBB = elseBB;
            astIfStmt.elseStmt.accept(this);

            // If a terminator statement has not been set for the else-block then just add it.
            if (this.env.enclBB.terminator == null) {
                this.env.enclBB.terminator = new BIRTerminator.GOTO(null, nextBB, this.currentScope);
            }
        } else {
            branchIns.falseBB = nextBB;
        }

        // Set the elseBB as the basic block for the rest of statements followed by this if.
        addToTrapStack(nextBB);
        this.env.enclBasicBlocks.add(nextBB);
        this.env.enclBB = nextBB;
    }

    @Override
    public void visit(BLangWhile astWhileStmt) {
        BIRBasicBlock currentEnclLoopBB = this.env.enclLoopBB;
        BIRBasicBlock currentEnclLoopEndBB = this.env.enclLoopEndBB;

        // Create a basic block for the while expression.
        BIRBasicBlock whileExprBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(whileExprBB);
        this.env.enclBasicBlocks.add(whileExprBB);

        // Insert a GOTO instruction as the terminal instruction into current basic block.
        this.env.enclBB.terminator = new BIRTerminator.GOTO(astWhileStmt.pos, whileExprBB, this.currentScope);

        // Visit condition expression
        this.env.enclBB = whileExprBB;
        astWhileStmt.expr.accept(this);
        BIROperand whileExprResult = this.env.targetOperand;

        // Create the basic block for the while-body block.
        BIRBasicBlock whileBodyBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(whileBodyBB);
        this.env.enclBasicBlocks.add(whileBodyBB);

        // Create the basic block for the statements that comes after the while statement.
        BIRBasicBlock whileEndBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(whileEndBB);

        // Add the branch instruction to the while expression basic block.
        this.env.enclBB.terminator =
                new BIRTerminator.Branch(astWhileStmt.pos, whileExprResult, whileBodyBB,
                        whileEndBB, this.currentScope);

        // Visit while body
        this.env.enclBB = whileBodyBB;
        this.env.enclLoopBB = whileExprBB;
        this.env.enclLoopEndBB = whileEndBB;
        this.env.unlockVars.push(new BIRLockDetailsHolder());
        astWhileStmt.body.accept(this);
        this.env.unlockVars.pop();
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(null, whileExprBB, this.currentScope);
        }

        this.env.enclBasicBlocks.add(whileEndBB);
        this.env.enclBB = whileEndBB;

        this.env.enclLoopBB = currentEnclLoopBB;
        this.env.enclLoopEndBB = currentEnclLoopEndBB;
    }


    // Expressions

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(ignoreExpr.getBType(),
                                                       this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
    }

    @Override
    public void visit(BLangLiteral astLiteralExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astLiteralExpr.getBType(),
                                                       this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        setScopeAndEmit(new BIRNonTerminator.ConstantLoad(astLiteralExpr.pos,
                                                          astLiteralExpr.value, astLiteralExpr.getBType(), toVarRef));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangMapLiteral astMapLiteralExpr) {
        visitTypedesc(astMapLiteralExpr.pos, astMapLiteralExpr.getBType(), Collections.emptyList());
        BIRVariableDcl tempVarDcl =
                new BIRVariableDcl(astMapLiteralExpr.getBType(), this.env.nextLocalVarId(names),
                                   VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        setScopeAndEmit(new BIRNonTerminator.NewStructure(astMapLiteralExpr.pos, toVarRef, this.env.targetOperand,
                                               generateMappingConstructorEntries(astMapLiteralExpr.fields)));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangTypeConversionExpr astTypeConversionExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astTypeConversionExpr.getBType(),
                                                       this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        astTypeConversionExpr.expr.accept(this);
        BIROperand rhsOp = this.env.targetOperand;

        setScopeAndEmit(
                new BIRNonTerminator.TypeCast(astTypeConversionExpr.pos, toVarRef, rhsOp, toVarRef.variableDcl.type,
                        astTypeConversionExpr.checkTypes));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangStructLiteral astStructLiteralExpr) {
        List<BIROperand> varDcls = mapToVarDcls(astStructLiteralExpr.enclMapSymbols);
        visitTypedesc(astStructLiteralExpr.pos, astStructLiteralExpr.getBType(), varDcls);

        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astStructLiteralExpr.getBType(),
                                                       this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);


        BIRNonTerminator.NewStructure instruction =
                new BIRNonTerminator.NewStructure(astStructLiteralExpr.pos, toVarRef, this.env.targetOperand,
                                                  generateMappingConstructorEntries(astStructLiteralExpr.fields));
        setScopeAndEmit(instruction);

        this.env.targetOperand = toVarRef;
    }

    private List<BIROperand> mapToVarDcls(TreeMap<Integer, BVarSymbol> enclMapSymbols) {
        if (enclMapSymbols == null || enclMapSymbols.size() == 0) {
            return Collections.emptyList();
        }

        ArrayList<BIROperand> varDcls = new ArrayList<>(enclMapSymbols.size());
        for (BVarSymbol varSymbol : enclMapSymbols.values()) {
            BIRVariableDcl varDcl = this.env.symbolVarMap.get(varSymbol);
            varDcls.add(new BIROperand(varDcl));
        }
        return varDcls;
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(connectorInitExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        BTypeSymbol objectTypeSymbol = getObjectTypeSymbol(connectorInitExpr.getBType());
        BIRNonTerminator.NewInstance instruction;
        if (isInSamePackage(objectTypeSymbol, env.enclPkg.packageID)) {
            BIRTypeDefinition def = typeDefs.get(objectTypeSymbol);
            instruction = new BIRNonTerminator.NewInstance(connectorInitExpr.pos, def, toVarRef);
        } else {
            BType connectorInitExprType = types.getReferredType(connectorInitExpr.getBType());
            BType objectType = connectorInitExprType.tag != TypeTags.UNION ? connectorInitExprType :
                    ((BUnionType) connectorInitExprType).getMemberTypes().stream()
                            .filter(bType -> bType.tag != TypeTags.ERROR)
                            .findFirst()
                            .get();

            String objectName = objectType.tsymbol.name.value;
            instruction = new BIRNonTerminator.NewInstance(connectorInitExpr.pos, objectTypeSymbol.pkgID,
                                                           objectName, toVarRef);
        }
        setScopeAndEmit(instruction);
        this.env.targetOperand = toVarRef;
    }

    private boolean isInSamePackage(BSymbol objectTypeSymbol, PackageID packageID) {
        return objectTypeSymbol.pkgID.equals(packageID);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
    }

    @Override
    public void visit(BLangArrayLiteral astArrayLiteralExpr) {
        generateListConstructorExpr(astArrayLiteralExpr);
    }

    @Override
    public void visit(BLangTupleLiteral tupleLiteral) {
        generateListConstructorExpr(tupleLiteral);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        groupExpr.expression.accept(this);
    }

    @Override
    public void visit(BLangJSONArrayLiteral jsonArrayLiteralExpr) {
        generateListConstructorExpr(jsonArrayLiteralExpr);
    }

    @Override
    public void visit(BLangMapAccessExpr astMapAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;
        BIROperand rhsOp = this.env.targetOperand;

        astMapAccessExpr.expr.accept(this);
        BIROperand varRefRegIndex = this.env.targetOperand;

        astMapAccessExpr.indexExpr.accept(this);
        BIROperand keyRegIndex = this.env.targetOperand;
        if (variableStore) {
            setScopeAndEmit(
                    new BIRNonTerminator.FieldAccess(astMapAccessExpr.pos, InstructionKind.MAP_STORE, varRefRegIndex,
                            keyRegIndex, rhsOp, astMapAccessExpr.isStoreOnCreation));
            return;
        }
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astMapAccessExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand tempVarRef = new BIROperand(tempVarDcl);

        setScopeAndEmit(new BIRNonTerminator.FieldAccess(astMapAccessExpr.pos, InstructionKind.MAP_LOAD, tempVarRef,
                keyRegIndex, varRefRegIndex, astMapAccessExpr.optionalFieldAccess,
                                              astMapAccessExpr.isLValue && !astMapAccessExpr.leafNode));
        this.env.targetOperand = tempVarRef;
        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangTableAccessExpr astTableAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;
        BIROperand rhsOp = this.env.targetOperand;

        astTableAccessExpr.expr.accept(this);
        BIROperand varRefRegIndex = this.env.targetOperand;

        astTableAccessExpr.indexExpr.accept(this);
        BIROperand keyRegIndex = this.env.targetOperand;
        if (variableStore) {
            setScopeAndEmit(new BIRNonTerminator.FieldAccess(astTableAccessExpr.pos, InstructionKind.TABLE_STORE,
                    varRefRegIndex, keyRegIndex, rhsOp));
            return;
        }
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astTableAccessExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand tempVarRef = new BIROperand(tempVarDcl);

        setScopeAndEmit(new BIRNonTerminator.FieldAccess(astTableAccessExpr.pos, InstructionKind.TABLE_LOAD, tempVarRef,
                keyRegIndex, varRefRegIndex));
        this.env.targetOperand = tempVarRef;
        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangStructFieldAccessExpr astStructFieldAccessExpr) {
        generateMappingAccess(astStructFieldAccessExpr, astStructFieldAccessExpr.optionalFieldAccess);
    }

    @Override
    public void visit(BLangJSONAccessExpr astJSONFieldAccessExpr) {
        if (astJSONFieldAccessExpr.indexExpr.getBType().tag == TypeTags.INT) {
            generateArrayAccess(astJSONFieldAccessExpr);
            return;
        }

        generateMappingAccess(astJSONFieldAccessExpr, astJSONFieldAccessExpr.optionalFieldAccess);
    }

    @Override
    public void visit(BLangDynamicArgExpr dynamicParamExpr) {
        dynamicParamExpr.condition.accept(this);
        dynamicParamExpr.conditionalArgument.accept(this);
    }

    @Override
    public void visit(BLangStringAccessExpr stringAccessExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(stringAccessExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand tempVarRef = new BIROperand(tempVarDcl);

        stringAccessExpr.expr.accept(this);
        BIROperand varRefRegIndex = this.env.targetOperand;

        stringAccessExpr.indexExpr.accept(this);
        BIROperand keyRegIndex = this.env.targetOperand;

        setScopeAndEmit(new BIRNonTerminator.FieldAccess(stringAccessExpr.pos, InstructionKind.STRING_LOAD, tempVarRef,
                                              keyRegIndex, varRefRegIndex));
        this.env.targetOperand = tempVarRef;
    }

    @Override
    public void visit(BLangArrayAccessExpr astArrayAccessExpr) {
        generateArrayAccess(astArrayAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr tupleAccessExpr) {
        generateArrayAccess(tupleAccessExpr);
    }

    @Override
    public void visit(BLangIsLikeExpr isLikeExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(symTable.booleanType,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        isLikeExpr.expr.accept(this);
        BIROperand exprIndex = this.env.targetOperand;

        setScopeAndEmit(new BIRNonTerminator.IsLike(isLikeExpr.pos, isLikeExpr.typeNode.getBType(), toVarRef,
                                                    exprIndex));

        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(symTable.booleanType,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        typeTestExpr.expr.accept(this);
        BIROperand exprIndex = this.env.targetOperand;

        setScopeAndEmit(
                new BIRNonTerminator.TypeTest(typeTestExpr.pos, typeTestExpr.typeNode.getBType(), toVarRef, exprIndex));

        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangLocalVarRef astVarRefExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;
        BSymbol varSymbol = astVarRefExpr.symbol;

        if (variableStore) {
            if (astVarRefExpr.symbol.name != Names.IGNORE) {
                BIROperand varRef = new BIROperand(this.env.symbolVarMap.get(varSymbol));
                setScopeAndEmit(new Move(astVarRefExpr.pos, this.env.targetOperand, varRef));
            }
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(varSymbol.type,
                    this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);

            BIRVariableDcl varDecl = this.env.symbolVarMap.get(varSymbol);;

            BIROperand fromVarRef = new BIROperand(varDecl);

            setScopeAndEmit(new Move(astVarRefExpr.pos, fromVarRef, tempVarRef));
            this.env.targetOperand = tempVarRef;
        }
        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangPackageVarRef astPackageVarRefExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        if (variableStore) {
            if (astPackageVarRefExpr.symbol.name != Names.IGNORE) {
                BIROperand varRef = new BIROperand(getVarRef(astPackageVarRefExpr));
                setScopeAndEmit(new Move(astPackageVarRefExpr.pos, this.env.targetOperand, varRef));
            }
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(astPackageVarRefExpr.getBType(),
                                                           this.env.nextLocalVarId(names), VarScope.FUNCTION,
                                                           VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);
            BIROperand fromVarRef = new BIROperand(getVarRef(astPackageVarRefExpr));
            setScopeAndEmit(new Move(astPackageVarRefExpr.pos, fromVarRef, tempVarRef));
            this.env.targetOperand = tempVarRef;
        }
        this.varAssignment = variableStore;
    }

    private BIRGlobalVariableDcl getVarRef(BLangPackageVarRef astPackageVarRefExpr) {
        BSymbol symbol = astPackageVarRefExpr.symbol;
        if ((symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT ||
                !isInSamePackage(astPackageVarRefExpr.varSymbol, env.enclPkg.packageID)) {
            return new BIRGlobalVariableDcl(astPackageVarRefExpr.pos, symbol.flags, symbol.type, symbol.pkgID,
                                            symbol.name, symbol.getOriginalName(), VarScope.GLOBAL, VarKind.CONSTANT,
                                            symbol.name.value, symbol.origin.toBIROrigin());
        }

        return this.globalVarMap.get(symbol);
    }

    @Override
    public void visit(BLangBinaryExpr astBinaryExpr) {
        astBinaryExpr.lhsExpr.accept(this);
        BIROperand rhsOp1 = this.env.targetOperand;

        astBinaryExpr.rhsExpr.accept(this);
        BIROperand rhsOp2 = this.env.targetOperand;

        // Create a temporary variable to store the binary operation result.
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astBinaryExpr.getBType(),
                                                       this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        // Create binary instruction
        BinaryOp binaryIns = new BinaryOp(astBinaryExpr.pos, getBinaryInstructionKind(astBinaryExpr.opKind),
                                          astBinaryExpr.getBType(), lhsOp, rhsOp1, rhsOp2);
        setScopeAndEmit(binaryIns);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr.accept(this);
        BIROperand rhsOp = this.env.targetOperand;

        // Create a temporary variable to store the unary operation result.
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(unaryExpr.getBType(),
                                                       this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);

        if (OperatorKind.ADD.equals(unaryExpr.operator) || OperatorKind.UNTAINT.equals(unaryExpr.operator)) {
            setScopeAndEmit(new Move(unaryExpr.pos, rhsOp, lhsOp));
            this.env.targetOperand = lhsOp;
            return;
        }

        UnaryOP unaryIns = new UnaryOP(unaryExpr.pos, getUnaryInstructionKind(unaryExpr.operator), lhsOp, rhsOp);
        setScopeAndEmit(unaryIns);
        this.env.targetOperand = lhsOp;
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        BIRBasicBlock trapBB = new BIRBasicBlock(this.env.nextBBId(names));
        this.env.enclBasicBlocks.add(trapBB);
        this.env.enclBB.terminator = new BIRTerminator.GOTO(trapExpr.pos, trapBB, this.currentScope);
        this.env.enclBB = trapBB;
        this.env.trapBlocks.push(new ArrayList<>());
        addToTrapStack(trapBB);

        trapExpr.expr.accept(this);

        List<BIRBasicBlock> trappedBlocks = this.env.trapBlocks.pop();
        // Create new block for instructions after trap.
        BIRBasicBlock nextBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(nextBB);
        env.enclBasicBlocks.add(nextBB);
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(trapExpr.pos, nextBB, this.currentScope);
        }

        env.enclFunc.errorTable.add(new BIRNode.BIRErrorEntry(trappedBlocks.get(0),
                trappedBlocks.get(trappedBlocks.size() - 1), env.targetOperand, nextBB));

        this.env.enclBB = nextBB;
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        createWait(waitExpr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        visitTypedesc(waitLiteral.pos, waitLiteral.getBType(), Collections.emptyList());
        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(thenBB);
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(waitLiteral.getBType(),
                                                       this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        setScopeAndEmit(new BIRNonTerminator.NewStructure(waitLiteral.pos, toVarRef, this.env.targetOperand));
        this.env.targetOperand = toVarRef;

        List<String> keys = new ArrayList<>();
        List<BIROperand> valueExprs = new ArrayList<>();
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValue : waitLiteral.keyValuePairs) {
            keys.add(keyValue.key.value);
            BLangExpression expr = keyValue.valueExpr != null ? keyValue.valueExpr : keyValue.keyExpr;
            expr.accept(this);
            BIROperand valueRegIndex = this.env.targetOperand;
            valueExprs.add(valueRegIndex);
        }
        this.env.enclBB.terminator = new BIRTerminator.WaitAll(waitLiteral.pos, toVarRef, keys,
                valueExprs, thenBB, this.currentScope);
        this.env.targetOperand = toVarRef;
        this.env.enclFunc.basicBlocks.add(thenBB);
        this.env.enclBB = thenBB;
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(symTable.booleanType, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        assignableExpr.lhsExpr.accept(this);
        BIROperand exprIndex = this.env.targetOperand;

        setScopeAndEmit(
                new BIRNonTerminator.TypeTest(assignableExpr.pos, assignableExpr.targetType, toVarRef, exprIndex));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        BIRVariableDcl tempVarDcl =
                new BIRVariableDcl(symTable.anyType, this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        // If the QName is use outside of XML, treat it as string.
        if (!xmlQName.isUsedInXML) {
            String qName = xmlQName.namespaceURI == null ? xmlQName.localname.value
                    : ("{" + xmlQName.namespaceURI + "}" + xmlQName.localname);
            generateStringLiteral(qName);
            return;
        }

        // Else, treat it as QName
        BIROperand nsURIIndex = generateStringLiteral(xmlQName.namespaceURI);
        BIROperand localnameIndex = generateStringLiteral(xmlQName.localname.value);
        BIROperand prefixIndex = generateStringLiteral(xmlQName.prefix.value);
        BIRNonTerminator.NewXMLQName newXMLQName =
                new BIRNonTerminator.NewXMLQName(xmlQName.pos, toVarRef, localnameIndex, nsURIIndex, prefixIndex);
        setScopeAndEmit(newXMLQName);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(xmlElementLiteral.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        // Visit in-line namespace declarations. These needs to be visited first before visiting the
        // attributes, start and end tag names of the element.
        xmlElementLiteral.inlineNamespaces.forEach(xmlns -> xmlns.accept(this));

        // Create start tag name
        BLangExpression startTagName = (BLangExpression) xmlElementLiteral.getStartTagName();
        startTagName.accept(this);
        BIROperand startTagNameIndex = this.env.targetOperand;

        // Create default namespace uri
        BIROperand defaultNsURIVarRef = generateNamespaceRef(xmlElementLiteral.defaultNsSymbol, xmlElementLiteral.pos);

        // Create xml element
        BIRNonTerminator.NewXMLElement newXMLElement =
                new BIRNonTerminator.NewXMLElement(xmlElementLiteral.pos, toVarRef, startTagNameIndex,
                                                   defaultNsURIVarRef,
                                                   Symbols.isFlagOn(xmlElementLiteral.getBType().flags,
                                                                    Flags.READONLY));
        setScopeAndEmit(newXMLElement);

        // Populate the XML by adding namespace declarations, attributes and children
        populateXML(xmlElementLiteral, toVarRef);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLAttribute attribute) {
        BIROperand xmlVarRef = this.env.targetOperand;

        attribute.name.accept(this);
        BIROperand attrNameOp = this.env.targetOperand;

        attribute.value.accept(this);
        BIROperand attrValueOp = this.env.targetOperand;
        setScopeAndEmit(new BIRNonTerminator.FieldAccess(attribute.pos, InstructionKind.XML_ATTRIBUTE_STORE, xmlVarRef,
                attrNameOp, attrValueOp));
    }

    @Override
    public void visit(BLangXMLSequenceLiteral xmlSequenceLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(xmlSequenceLiteral.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);

        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        BIRNonTerminator.NewXMLSequence newXMLSequence =
                new BIRNonTerminator.NewXMLSequence(xmlSequenceLiteral.pos, toVarRef);

        setScopeAndEmit(newXMLSequence);
        populateXMLSequence(xmlSequenceLiteral, toVarRef);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(xmlTextLiteral.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        xmlTextLiteral.concatExpr.accept(this);
        BIROperand xmlTextIndex = this.env.targetOperand;

        BIRNonTerminator.NewXMLText newXMLElement =
                new BIRNonTerminator.NewXMLText(xmlTextLiteral.pos, toVarRef, xmlTextIndex);
        setScopeAndEmit(newXMLElement);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(xmlCommentLiteral.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        xmlCommentLiteral.concatExpr.accept(this);
        BIROperand xmlCommentIndex = this.env.targetOperand;

        BIRNonTerminator.NewXMLComment newXMLComment =
                new BIRNonTerminator.NewXMLComment(xmlCommentLiteral.pos, toVarRef, xmlCommentIndex,
                                                   Symbols.isFlagOn(xmlCommentLiteral.getBType().flags,
                                                                    Flags.READONLY));
        setScopeAndEmit(newXMLComment);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(xmlProcInsLiteral.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        xmlProcInsLiteral.dataConcatExpr.accept(this);
        BIROperand dataIndex = this.env.targetOperand;

        xmlProcInsLiteral.target.accept(this);
        BIROperand targetIndex = this.env.targetOperand;

        BIRNonTerminator.NewXMLProcIns newXMLProcIns =
                new BIRNonTerminator.NewXMLProcIns(xmlProcInsLiteral.pos, toVarRef, dataIndex, targetIndex,
                                                   Symbols.isFlagOn(xmlProcInsLiteral.getBType().flags,
                                                                    Flags.READONLY));
        setScopeAndEmit(newXMLProcIns);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.concatExpr.accept(this);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        // do nothing
    }

    @Override
    public void visit(BLangLocalXMLNS xmlnsNode) {
        generateXMLNamespace(xmlnsNode);
    }

    @Override
    public void visit(BLangPackageXMLNS xmlnsNode) {
        generateXMLNamespace(xmlnsNode);
    }

    @Override
    public void visit(BLangXMLAccessExpr xmlAccessExpr) {
        generateMappingAccess(xmlAccessExpr, false);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        BIRVariableDcl tempVarDcl =
                new BIRVariableDcl(accessExpr.getBType(), this.env.nextLocalVarId(names), VarScope.FUNCTION,
                                   VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        setScopeAndEmit(new BIRNonTerminator.NewTypeDesc(accessExpr.pos, toVarRef, accessExpr.resolvedType,
                                              Collections.emptyList()));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(tableConstructorExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);

        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        BLangArrayLiteral keySpecifierLiteral = new BLangArrayLiteral();
        keySpecifierLiteral.pos = tableConstructorExpr.pos;
        keySpecifierLiteral.setBType(symTable.stringArrayType);
        keySpecifierLiteral.exprs = new ArrayList<>();
        BTableType type = (BTableType) tableConstructorExpr.getBType();

        if (type.fieldNameList != null) {
            type.fieldNameList.forEach(col -> {
                BLangLiteral colLiteral = new BLangLiteral();
                colLiteral.pos = tableConstructorExpr.pos;
                colLiteral.setBType(symTable.stringType);
                colLiteral.value = col;
                keySpecifierLiteral.exprs.add(colLiteral);
            });
        }

        keySpecifierLiteral.accept(this);
        BIROperand keyColOp = this.env.targetOperand;

        BLangArrayLiteral dataLiteral = new BLangArrayLiteral();
        dataLiteral.pos = tableConstructorExpr.pos;
        dataLiteral.setBType(new BArrayType(((BTableType) tableConstructorExpr.getBType()).constraint));
        dataLiteral.exprs = new ArrayList<>(tableConstructorExpr.recordLiteralList);
        dataLiteral.accept(this);
        BIROperand dataOp = this.env.targetOperand;

        setScopeAndEmit(
                new BIRNonTerminator.NewTable(tableConstructorExpr.pos, tableConstructorExpr.getBType(), toVarRef,
                                              keyColOp, dataOp));

        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        visitTypedesc(typeLoad.pos, typeLoad.symbol.type, Collections.emptyList());
    }

    private void visitTypedesc(Location pos, BType type, List<BIROperand> varDcls) {
        BIRVariableDcl tempVarDcl =
                new BIRVariableDcl(symTable.typeDesc, this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind
                        .TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        setScopeAndEmit(new BIRNonTerminator.NewTypeDesc(pos, toVarRef, type, varDcls));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangBreak breakStmt) {
        BIRLockDetailsHolder toUnlock = this.env.unlockVars.peek();
        if (!toUnlock.isEmpty()) {
            BIRBasicBlock goToBB = new BIRBasicBlock(this.env.nextBBId(names));
            this.env.enclBasicBlocks.add(goToBB);
            this.env.enclBB.terminator = new BIRTerminator.GOTO(breakStmt.pos, goToBB, this.currentScope);
            this.env.enclBB = goToBB;
        }

        int numLocks = toUnlock.size();
        while (numLocks > 0) {
            BIRBasicBlock unlockBB = new BIRBasicBlock(this.env.nextBBId(names));
            this.env.enclBasicBlocks.add(unlockBB);
            BIRTerminator.Unlock unlock = new BIRTerminator.Unlock(null, unlockBB, this.currentScope);
            this.env.enclBB.terminator = unlock;
            unlock.relatedLock = toUnlock.getLock(numLocks - 1);
            this.env.enclBB = unlockBB;
            numLocks--;
        }
        this.env.enclBB.terminator = new BIRTerminator.GOTO(breakStmt.pos, this.env.enclLoopEndBB, this.currentScope);
    }

    @Override
    public void visit(BLangContinue continueStmt) {
        BIRLockDetailsHolder toUnlock = this.env.unlockVars.peek();
        if (!toUnlock.isEmpty()) {
            BIRBasicBlock goToBB = new BIRBasicBlock(this.env.nextBBId(names));
            this.env.enclBasicBlocks.add(goToBB);
            this.env.enclBB.terminator = new BIRTerminator.GOTO(continueStmt.pos, goToBB, this.currentScope);
            this.env.enclBB = goToBB;
        }
        int numLocks = toUnlock.size();
        while (numLocks > 0) {
            BIRBasicBlock unlockBB = new BIRBasicBlock(this.env.nextBBId(names));
            this.env.enclBasicBlocks.add(unlockBB);
            BIRTerminator.Unlock unlock = new BIRTerminator.Unlock(null,  unlockBB, this.currentScope);
            this.env.enclBB.terminator = unlock;
            BIRTerminator.Lock lock = toUnlock.getLock(numLocks - 1);
            unlock.relatedLock = lock;
            this.env.enclBB = unlockBB;
            numLocks--;
        }

        this.env.enclBB.terminator = new BIRTerminator.GOTO(continueStmt.pos, this.env.enclLoopBB, this.currentScope);
    }

    @Override
    public void visit(BLangFunctionVarRef fpVarRef) {
        generateFPVarRef(fpVarRef, (BInvokableSymbol) fpVarRef.symbol);
    }

    @Override
    public void visit(BLangStructFunctionVarRef structFpVarRef) {
        generateFPVarRef(structFpVarRef, (BInvokableSymbol) structFpVarRef.symbol);
    }

    @Override
    public void visit(BLangLockStmt lockStmt) {
        BIRBasicBlock lockedBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(lockedBB);
        this.env.enclBasicBlocks.add(lockedBB);
        BIRTerminator.Lock lock = new BIRTerminator.Lock(lockStmt.pos, lockedBB, this.currentScope);
        this.env.enclBB.terminator = lock;
        lockStmtMap.put(lockStmt, lock); // Populate the cache.
        this.env.unlockVars.peek().addLock(lock);
        populateBirLockWithGlobalVars(lockStmt);
        this.env.enclBB = lockedBB;

    }

    private void populateBirLockWithGlobalVars(BLangLockStmt lockStmt) {
        for (BVarSymbol globalVar : lockStmt.lockVariables) {
            BIRGlobalVariableDcl birGlobalVar = this.globalVarMap.get(globalVar);

            // If null query the dummy map for dummy variables.
            if (birGlobalVar == null) {
                birGlobalVar = dummyGlobalVarMapForLocks.computeIfAbsent(globalVar, k ->
                        new BIRGlobalVariableDcl(null, globalVar.flags, globalVar.type, globalVar.pkgID,
                                                 globalVar.name, globalVar.getOriginalName(), VarScope.GLOBAL,
                                                 VarKind.GLOBAL, globalVar.name.value,
                                                 globalVar.origin.toBIROrigin()));
            }

            ((BIRTerminator.Lock) this.env.enclBB.terminator).lockVariables.add(birGlobalVar);
        }
    }

    @Override
    public void visit(BLangUnLockStmt unLockStmt) {
        BIRLockDetailsHolder lockDetailsHolder = this.env.unlockVars.peek();
        if (lockDetailsHolder.isEmpty()) {
            return;
        }
        BIRBasicBlock unLockedBB = new BIRBasicBlock(this.env.nextBBId(names));
        addToTrapStack(unLockedBB);
        this.env.enclBasicBlocks.add(unLockedBB);
        this.env.enclBB.terminator = new BIRTerminator.Unlock(unLockStmt.pos, unLockedBB, this.currentScope);
        ((BIRTerminator.Unlock) this.env.enclBB.terminator).relatedLock = lockStmtMap.get(unLockStmt.relatedLock);
        this.env.enclBB = unLockedBB;

        lockDetailsHolder.removeLastLock();
    }

    private void setScopeAndEmit(BIRNonTerminator instruction) {
        instruction.scope = this.currentScope;
        this.env.enclBB.instructions.add(instruction);
    }

    private InstructionKind getBinaryInstructionKind(OperatorKind opKind) {
        switch (opKind) {
            case ADD:
                return InstructionKind.ADD;
            case SUB:
                return InstructionKind.SUB;
            case MUL:
                return InstructionKind.MUL;
            case DIV:
                return InstructionKind.DIV;
            case MOD:
                return InstructionKind.MOD;
            case EQUAL:
            case EQUALS:
                return InstructionKind.EQUAL;
            case NOT_EQUAL:
                return InstructionKind.NOT_EQUAL;
            case GREATER_THAN:
                return InstructionKind.GREATER_THAN;
            case GREATER_EQUAL:
                return InstructionKind.GREATER_EQUAL;
            case LESS_THAN:
                return InstructionKind.LESS_THAN;
            case LESS_EQUAL:
                return InstructionKind.LESS_EQUAL;
            case AND:
                return InstructionKind.AND;
            case OR:
                return InstructionKind.OR;
            case REF_EQUAL:
                return InstructionKind.REF_EQUAL;
            case REF_NOT_EQUAL:
                return InstructionKind.REF_NOT_EQUAL;
            case CLOSED_RANGE:
                return InstructionKind.CLOSED_RANGE;
            case HALF_OPEN_RANGE:
                return InstructionKind.HALF_OPEN_RANGE;
            case ANNOT_ACCESS:
                return InstructionKind.ANNOT_ACCESS;
            case BITWISE_AND:
                return InstructionKind.BITWISE_AND;
            case BITWISE_OR:
                return InstructionKind.BITWISE_OR;
            case BITWISE_XOR:
                return InstructionKind.BITWISE_XOR;
            case BITWISE_LEFT_SHIFT:
                return InstructionKind.BITWISE_LEFT_SHIFT;
            case BITWISE_RIGHT_SHIFT:
                return InstructionKind.BITWISE_RIGHT_SHIFT;
            case BITWISE_UNSIGNED_RIGHT_SHIFT:
                return InstructionKind.BITWISE_UNSIGNED_RIGHT_SHIFT;
            default:
                throw new IllegalStateException("unsupported binary operation: " + opKind.value());
        }
    }

    private InstructionKind getUnaryInstructionKind(OperatorKind opKind) {
        switch (opKind) {
            case TYPEOF:
                return InstructionKind.TYPEOF;
            case NOT:
                return InstructionKind.NOT;
            case SUB:
                return InstructionKind.NEGATE;
            case ADD:
                return InstructionKind.MOVE;
            default:
                throw new IllegalStateException("unsupported unary operator: " + opKind.value());
        }
    }

    private void generateListConstructorExpr(BLangListConstructorExpr listConstructorExpr) {
        // Emit create array instruction
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(listConstructorExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        long size = -1L;
        List<BLangExpression> exprs = listConstructorExpr.exprs;
        BType listConstructorExprType = types.getReferredType(listConstructorExpr.getBType());
        if (listConstructorExprType.tag == TypeTags.ARRAY &&
                ((BArrayType) listConstructorExprType).state != BArrayState.OPEN) {
            size = ((BArrayType) listConstructorExprType).size;
        } else if (listConstructorExprType.tag == TypeTags.TUPLE) {
            size = exprs.size();
        }

        BLangLiteral literal = new BLangLiteral();
        literal.pos = listConstructorExpr.pos;
        literal.value = size;
        literal.setBType(symTable.intType);
        literal.accept(this);
        BIROperand sizeOp = this.env.targetOperand;

        List<BIROperand> valueOperands = new ArrayList<>(exprs.size());

        for (BLangExpression expr : exprs) {
            expr.accept(this);
            valueOperands.add(this.env.targetOperand);
        }

        setScopeAndEmit(
                new BIRNonTerminator.NewArray(listConstructorExpr.pos, listConstructorExprType, toVarRef, sizeOp,
                        valueOperands));
        this.env.targetOperand = toVarRef;
    }

    private void generateArrayAccess(BLangIndexBasedAccess astArrayAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        BIROperand rhsOp = this.env.targetOperand;

        astArrayAccessExpr.expr.accept(this);
        BIROperand varRefRegIndex = this.env.targetOperand;

        astArrayAccessExpr.indexExpr.accept(this);
        BIROperand keyRegIndex = this.env.targetOperand;

        if (variableStore) {
            setScopeAndEmit(new BIRNonTerminator.FieldAccess(astArrayAccessExpr.pos, InstructionKind.ARRAY_STORE,
                    varRefRegIndex, keyRegIndex, rhsOp));
            return;
        }
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astArrayAccessExpr.getBType(), this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand tempVarRef = new BIROperand(tempVarDcl);

        setScopeAndEmit(new BIRNonTerminator.FieldAccess(astArrayAccessExpr.pos, InstructionKind.ARRAY_LOAD, tempVarRef,
                                              keyRegIndex, varRefRegIndex, false,
                                              astArrayAccessExpr.isLValue && !astArrayAccessExpr.leafNode));
        this.env.targetOperand = tempVarRef;

        this.varAssignment = variableStore;
    }

    private void generateMappingAccess(BLangIndexBasedAccess astIndexBasedAccessExpr, boolean except) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;
        InstructionKind insKind;
        BType astAccessExprExprType = types.getReferredType(astIndexBasedAccessExpr.expr.getBType());
        if (variableStore) {
            BIROperand rhsOp = this.env.targetOperand;

            astIndexBasedAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astIndexBasedAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            if (astIndexBasedAccessExpr.getKind() == NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
                insKind = InstructionKind.XML_ATTRIBUTE_STORE;
                keyRegIndex = getQNameOP(astIndexBasedAccessExpr.indexExpr, keyRegIndex);
            } else if (astAccessExprExprType.tag == TypeTags.OBJECT ||
                    (astAccessExprExprType.tag == TypeTags.UNION &&
                             ((BUnionType) astAccessExprExprType).getMemberTypes().iterator()
                                     .next().tag == TypeTags.OBJECT)) {
                insKind = InstructionKind.OBJECT_STORE;
            } else {
                insKind = InstructionKind.MAP_STORE;
            }
            setScopeAndEmit(
                    new BIRNonTerminator.FieldAccess(astIndexBasedAccessExpr.pos, insKind, varRefRegIndex, keyRegIndex,
                            rhsOp, astIndexBasedAccessExpr.isStoreOnCreation));
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(astIndexBasedAccessExpr.getBType(),
                                                           this.env.nextLocalVarId(names),
                                                           VarScope.FUNCTION, VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);

            astIndexBasedAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astIndexBasedAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            if (astIndexBasedAccessExpr.getKind() == NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
                insKind = InstructionKind.XML_ATTRIBUTE_LOAD;
                keyRegIndex = getQNameOP(astIndexBasedAccessExpr.indexExpr, keyRegIndex);
            } else if (TypeTags.isXMLTypeTag(astAccessExprExprType.tag)) {
                generateXMLAccess((BLangXMLAccessExpr) astIndexBasedAccessExpr, tempVarRef, varRefRegIndex,
                        keyRegIndex);
                this.varAssignment = variableStore;
                return;
            } else if (astAccessExprExprType.tag == TypeTags.OBJECT ||
                    (astAccessExprExprType.tag == TypeTags.UNION &&
                             ((BUnionType) astAccessExprExprType).getMemberTypes().iterator()
                                     .next().tag == TypeTags.OBJECT)) {
                insKind = InstructionKind.OBJECT_LOAD;
            } else {
                insKind = InstructionKind.MAP_LOAD;
            }
            setScopeAndEmit(
                    new BIRNonTerminator.FieldAccess(astIndexBasedAccessExpr.pos, insKind, tempVarRef, keyRegIndex,
                            varRefRegIndex, except,
                            astIndexBasedAccessExpr.isLValue && !astIndexBasedAccessExpr.leafNode));
            this.env.targetOperand = tempVarRef;
        }
        this.varAssignment = variableStore;
    }

    private BTypeSymbol getObjectTypeSymbol(BType objType) {
        BType type = types.getReferredType(objType);
        if (type.tag == TypeTags.UNION) {
            return ((BUnionType) type).getMemberTypes().stream()
                    .filter(t -> t.tag == TypeTags.OBJECT)
                    .findFirst()
                    .orElse(symTable.noType).tsymbol;
        }
        return type.tsymbol;
    }

    private BIROperand generateStringLiteral(String value) {
        BLangLiteral prefixLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        prefixLiteral.value = value;

        if (value == null) {
            prefixLiteral.setBType(symTable.nilType);
        } else {
            prefixLiteral.setBType(symTable.stringType);
        }

        prefixLiteral.accept(this);
        return this.env.targetOperand;
    }

    private void generateXMLNamespace(BLangXMLNS xmlnsNode) {
        BIRVariableDcl birVarDcl = new BIRVariableDcl(xmlnsNode.pos, symTable.stringType,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.LOCAL, null);
        this.env.enclFunc.localVars.add(birVarDcl);
        this.env.symbolVarMap.put(xmlnsNode.symbol, birVarDcl);

        // Visit the namespace uri expression.
        xmlnsNode.namespaceURI.accept(this);

        // Create a variable reference and
        BIROperand varRef = new BIROperand(birVarDcl);
        setScopeAndEmit(new Move(xmlnsNode.pos, this.env.targetOperand, varRef));
    }

    private BIROperand generateNamespaceRef(BXMLNSSymbol nsSymbol, Location pos) {
        if (nsSymbol == null) {
            return generateStringLiteral(null);
        }

        // global-level, object-level, record-level namespace declarations will not have
        // any interpolated content. hence the namespace URI is statically known.
        int ownerTag = nsSymbol.owner.tag;
        if ((ownerTag & SymTag.PACKAGE) == SymTag.PACKAGE ||
                (ownerTag & SymTag.OBJECT) == SymTag.OBJECT ||
                (ownerTag & SymTag.RECORD) == SymTag.RECORD) {
            return generateStringLiteral(nsSymbol.namespaceURI);
        }

        BIRVariableDcl nsURIVarDcl = new BIRVariableDcl(symTable.stringType, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(nsURIVarDcl);
        BIROperand nsURIVarRef = new BIROperand(nsURIVarDcl);

        BIRVariableDcl varDecl = this.env.symbolVarMap.get(nsSymbol);
        BIROperand fromVarRef = new BIROperand(varDecl);
        setScopeAndEmit(new Move(pos, fromVarRef, nsURIVarRef));
        return nsURIVarRef;
    }

    private void populateXMLSequence(BLangXMLSequenceLiteral xmlSequenceLiteral, BIROperand toVarRef) {
        for (BLangExpression xmlItem : xmlSequenceLiteral.xmlItems) {
            xmlItem.accept(this);
            BIROperand childOp = this.env.targetOperand;
            setScopeAndEmit(
                    new BIRNonTerminator.XMLAccess(xmlItem.pos, InstructionKind.XML_SEQ_STORE, toVarRef, childOp));
        }
    }

    private void populateXML(BLangXMLElementLiteral xmlElementLiteral, BIROperand toVarRef) {
        // Add namespaces decelerations visible to this element.
        xmlElementLiteral.namespacesInScope.forEach((name, symbol) -> {
            BLangXMLQName nsQName = new BLangXMLQName(name.getValue(), XMLConstants.XMLNS_ATTRIBUTE);
            nsQName.setBType(symTable.stringType);
            nsQName.accept(this);
            BIROperand nsQNameIndex = this.env.targetOperand;
            BIROperand nsURIIndex = generateNamespaceRef(symbol, xmlElementLiteral.pos);
            setScopeAndEmit(new BIRNonTerminator.FieldAccess(xmlElementLiteral.pos, InstructionKind.XML_ATTRIBUTE_STORE,
                    toVarRef, nsQNameIndex, nsURIIndex));
        });

        // Add attributes
        xmlElementLiteral.attributes.forEach(attribute -> {
            this.env.targetOperand = toVarRef;
            attribute.accept(this);
        });

        // Add children
        xmlElementLiteral.modifiedChildren.forEach(child -> {
            child.accept(this);
            BIROperand childOp = this.env.targetOperand;
            setScopeAndEmit(
                    new BIRNonTerminator.XMLAccess(child.pos, InstructionKind.XML_SEQ_STORE, toVarRef, childOp));
        });
    }

    private BIROperand getQNameOP(BLangExpression qnameExpr, BIROperand keyRegIndex) {
        if (qnameExpr.getKind() == NodeKind.XML_QNAME) {
            return keyRegIndex;
        }

        BIRVariableDcl tempQNameVarDcl = new BIRVariableDcl(symTable.anyType,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempQNameVarDcl);
        BIROperand qnameVarRef = new BIROperand(tempQNameVarDcl);
        setScopeAndEmit(new BIRNonTerminator.NewStringXMLQName(qnameExpr.pos, qnameVarRef, keyRegIndex));
        return qnameVarRef;
    }

    // todo: remove/move this, we no longer support xml access like this
    private void generateXMLAccess(BLangXMLAccessExpr xmlAccessExpr, BIROperand tempVarRef,
                                   BIROperand varRefRegIndex, BIROperand keyRegIndex) {
        this.env.targetOperand = tempVarRef;
        InstructionKind insKind;
        if (xmlAccessExpr.fieldType == FieldKind.ALL) {
            setScopeAndEmit(new BIRNonTerminator.XMLAccess(xmlAccessExpr.pos, InstructionKind.XML_LOAD_ALL, tempVarRef,
                    varRefRegIndex));
            return;
        } else if (xmlAccessExpr.indexExpr.getBType().tag == TypeTags.STRING) {
            insKind = InstructionKind.XML_LOAD;
        } else {
            insKind = InstructionKind.XML_SEQ_LOAD;
        }

        setScopeAndEmit(
                new BIRNonTerminator.FieldAccess(xmlAccessExpr.pos, insKind, tempVarRef, keyRegIndex, varRefRegIndex));
    }

    private void generateFPVarRef(BLangExpression fpVarRef, BInvokableSymbol funcSymbol) {
        // fpload instruction
        BIRVariableDcl tempVarLambda =
                new BIRVariableDcl(fpVarRef.getBType(), this.env.nextLocalVarId(names), VarScope.FUNCTION,
                                   VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarLambda);
        BIROperand lhsOp = new BIROperand(tempVarLambda);
        Name funcName = getFuncName(funcSymbol);

        List<BIRVariableDcl> params = new ArrayList<>();

        funcSymbol.params.forEach(param -> {
            BIRVariableDcl birVarDcl = new BIRVariableDcl(fpVarRef.pos, param.type, this.env.nextLambdaVarId(names),
                    VarScope.FUNCTION, VarKind.ARG, null);
            params.add(birVarDcl);
        });

        BVarSymbol restParam = funcSymbol.restParam;
        if (restParam != null) {
            BIRVariableDcl birVarDcl = new BIRVariableDcl(fpVarRef.pos, restParam.type, this.env.nextLambdaVarId(names),
                    VarScope.FUNCTION, VarKind.ARG, null);
            params.add(birVarDcl);
        }

        setScopeAndEmit(
                new BIRNonTerminator.FPLoad(fpVarRef.pos, funcSymbol.pkgID, funcName, lhsOp, params, new ArrayList<>(),
                        funcSymbol.type, funcSymbol.strandName, funcSymbol.schedulerPolicy));
        this.env.targetOperand = lhsOp;
    }

    private void populateBIRAnnotAttachments(List<BLangAnnotationAttachment> astAnnotAttachments,
                                             List<BIRAnnotationAttachment> birAnnotAttachments,
                                             BIRGenEnv currentEnv) {
        currentEnv.enclAnnotAttachments = birAnnotAttachments;
        astAnnotAttachments.forEach(annotAttach -> annotAttach.accept(this));
        currentEnv.enclAnnotAttachments = null;
    }

    private void addToTrapStack(BIRBasicBlock birBasicBlock) {
        if (this.env.trapBlocks.isEmpty()) {
            return;
        }
        this.env.trapBlocks.peek().add(birBasicBlock);
    }

    private List<BIRAnnotationAttachment> getStatementAnnotations(List<BLangAnnotationAttachment> astAnnotAttachments,
                                                                  BIRGenEnv currentEnv) {
        //preserve function annotations
        List<BIRAnnotationAttachment> functionAnnotAttachments = currentEnv.enclAnnotAttachments;
        currentEnv.enclAnnotAttachments = new ArrayList<>();
        astAnnotAttachments.forEach(annotAttach -> annotAttach.accept(this));
        List<BIRAnnotationAttachment> statementAnnots = currentEnv.enclAnnotAttachments;
        //reset function annotations
        currentEnv.enclAnnotAttachments = functionAnnotAttachments;
        return statementAnnots;
    }

    private List<BIRNode.BIRMappingConstructorEntry> generateMappingConstructorEntries(
            List<RecordLiteralNode.RecordField> fields) {

        List<BIRNode.BIRMappingConstructorEntry> initialValues = new ArrayList<>(fields.size());

        for (RecordLiteralNode.RecordField field : fields) {
            if (field.isKeyValueField()) {
                BLangRecordKeyValueField keyValueField = (BLangRecordKeyValueField) field;
                keyValueField.key.expr.accept(this);
                BIROperand keyOperand = this.env.targetOperand;

                keyValueField.valueExpr.accept(this);
                BIROperand valueOperand = this.env.targetOperand;
                initialValues.add(new BIRNode.BIRMappingConstructorKeyValueEntry(keyOperand, valueOperand));
                continue;
            }

            BLangRecordLiteral.BLangRecordSpreadOperatorField spreadField =
                    (BLangRecordLiteral.BLangRecordSpreadOperatorField) field;
            spreadField.expr.accept(this);
            initialValues.add(new BIRNode.BIRMappingConstructorSpreadFieldEntry(this.env.targetOperand));
        }
        return initialValues;
    }
}

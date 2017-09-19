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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.XMLAttributeNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
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
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReply;
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
import org.wso2.ballerinalang.compiler.tree.statements.BlangTransform;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.CallableUnitInfo;
import org.wso2.ballerinalang.programfile.FunctionInfo;
import org.wso2.ballerinalang.programfile.Instruction;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.programfile.InstructionFactory;
import org.wso2.ballerinalang.programfile.LocalVariableInfo;
import org.wso2.ballerinalang.programfile.PackageInfo;
import org.wso2.ballerinalang.programfile.PackageVarInfo;
import org.wso2.ballerinalang.programfile.ProgramFile;
import org.wso2.ballerinalang.programfile.WorkerInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfoPool;
import org.wso2.ballerinalang.programfile.attributes.CodeAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LineNumberTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LocalVariableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.VarTypeCountAttributeInfo;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPool;
import org.wso2.ballerinalang.programfile.cpentries.FloatCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionCallCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.IntegerCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.PackageRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StringCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.TypeRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.UTF8CPEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BLOB_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.BOOL_OFFSET;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.FLOAT_OFFSET;
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

    private ProgramFile programFile;

    private PackageInfo currentPkgInfo;
    private String currentPkgName;
    private int currentPackageRefCPIndex;

    private LineNumberTableAttributeInfo lineNoAttrInfo;
    private CallableUnitInfo currentCallableUnitInfo;
    private LocalVariableAttributeInfo localVarAttrInfo;

    // Required variables to generate code for assignment statements
    private int rhsExprRegIndex = -1;
    private boolean varAssignment = false;

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
    }

    public ProgramFile generate(BLangPackage pkgNode) {
        programFile = new ProgramFile();
        BPackageSymbol pkgSymbol = pkgNode.symbol;
        genPackage(pkgSymbol);

        programFile.entryPkgCPIndex = addPackageRefCPEntry(programFile, pkgSymbol.name.value,
                pkgSymbol.version.value);

        // TODO Setting this program as a main program. Remove this ASAP
        programFile.setMainEPAvailable(true);

        // Add global variable indexes to the ProgramFile
        prepareIndexes(pvIndexes);

        // Create Global variable attribute info
        addVarCountAttrInfo(programFile, programFile, pvIndexes);

        return programFile;
    }

    public void visit(BLangPackage pkgNode) {
        // first visit all the imports
        pkgNode.imports.forEach(impPkgNode -> genNode(impPkgNode, this.env));

        // Add the current package to the program file
        BPackageSymbol pkgSymbol = pkgNode.symbol;
        currentPkgName = pkgSymbol.name.getValue();
        int pkgNameCPIndex = addUTF8CPEntry(programFile, currentPkgName);
        int pkgVersionCPIndex = addUTF8CPEntry(programFile, pkgSymbol.version.getValue());
        currentPkgInfo = new PackageInfo(pkgNameCPIndex, pkgVersionCPIndex);

        // TODO We need to create identifier for both name and the version
        programFile.packageInfoMap.put(currentPkgName, currentPkgInfo);

        // Insert the package reference to the constant pool of the Ballerina program
        addPackageRefCPEntry(programFile, currentPkgName, pkgSymbol.version.value);

        // Insert the package reference to the constant pool of the current package
        currentPackageRefCPIndex = addPackageRefCPEntry(currentPkgInfo, currentPkgName, pkgSymbol.version.value);

        // This attribute keep track of line numbers
        int lineNoAttrNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE.value());
        lineNoAttrInfo = new LineNumberTableAttributeInfo(lineNoAttrNameIndex);

        // This attribute keep package-level variable information
        int pkgVarAttrNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.value());
        currentPkgInfo.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE,
                new LocalVariableAttributeInfo(pkgVarAttrNameIndex));

//        visitConstants(bLangPackage.getConsts());
//        visitGlobalVariables(bLangPackage.getGlobalVariables());
        pkgNode.globalVars.forEach(varNode -> createPackageVarInfo(varNode.symbol));
//        createStructInfoEntries(bLangPackage.getStructDefs());
//        createConnectorInfoEntries(bLangPackage.getConnectors());
//        createServiceInfoEntries(bLangPackage.getServices());
        pkgNode.functions.forEach(funcNode -> createFunctionInfoEntry(funcNode.symbol));

//        // Create function info for the package function
        BLangFunction pkgInitFunc = pkgNode.initFunction;
        createFunctionInfoEntry(pkgInitFunc.symbol);
//
        for (TopLevelNode pkgLevelNode : pkgNode.topLevelNodes) {
            genNode((BLangNode) pkgLevelNode, this.env);
        }

        // Visit package init function
        genNode(pkgInitFunc, this.env);

        currentPkgInfo.addAttributeInfo(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE, lineNoAttrInfo);
        currentPackageRefCPIndex = -1;
        currentPkgName = null;
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        genPackage(pkgSymbol);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.env);
        currentCallableUnitInfo = currentPkgInfo.functionInfoMap.get(funcNode.symbol.name.value);
        visitInvokableNode(funcNode, currentCallableUnitInfo, funcEnv);
    }

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, this.env);

        for (BLangStatement stmt : blockNode.statements) {
//            if (stmt instanceof CommentStmt) {
//                continue;
//            }
//
//            if (!(stmt instanceof TryCatchStmt)) {
//                addLineNumberInfo(stmt.getNodeLocation());
//            }

            genNode(stmt, blockEnv);

            // Update the maxRegIndexes structure
            setMaxRegIndexes();

            // Reset the regIndexes structure for every statement
            regIndexes = new VariableIndex();
        }
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


    // Statements

    public void visit(BLangVariableDef varDefNode) {
        genNode(varDefNode.var, this.env);
    }

    public void visit(BLangReturn returnNode) {
        emit(InstructionCodes.RET);
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        int opcode;
        int regIndex = -1;
        int typeTag = literalExpr.typeTag;

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
        }

        literalExpr.regIndex = regIndex;
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        genNode(binaryExpr.lhsExpr, this.env);
        genNode(binaryExpr.rhsExpr, this.env);

        int opcode = binaryExpr.opSymbol.opcode;
        int exprIndex = getNextIndex(binaryExpr.type.tag, regIndexes);

        binaryExpr.regIndex = exprIndex;
        emit(opcode, binaryExpr.lhsExpr.regIndex, binaryExpr.rhsExpr.regIndex, exprIndex);
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        /* ignore */
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        /* ignore */
    }

    public void visit(BLangInvocation iExpr) {
        if (iExpr.expr == null) {
            BInvokableSymbol funcSymbol = iExpr.symbol;
            BPackageSymbol pkgSymbol = (BPackageSymbol) funcSymbol.owner;
            int pkgRefCPIndex = addPackageRefCPEntry(currentPkgInfo, pkgSymbol.name.value, pkgSymbol.version.value);
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

        } else if (opCode == InstructionCodes.ANY2T || opCode == InstructionCodes.ANY2C) {
            int typeSigCPIndex = addUTF8CPEntry(currentPkgInfo, castExpr.type.getDesc());
            TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex);
            int typeCPIndex = currentPkgInfo.addCPEntry(typeRefCPEntry);
            int targetRegIndex = getNextIndex(castExpr.type.tag, regIndexes);

            castExpr.regIndexes = new int[]{targetRegIndex, errorRegIndex};
            emit(opCode, rExpr.regIndex, typeCPIndex, targetRegIndex, errorRegIndex);

        } else if (opCode != 0) {
            int targetRegIndex = getNextIndex(castExpr.type.tag, regIndexes);
            castExpr.regIndexes = new int[]{targetRegIndex, errorRegIndex};
            emit(opCode, castExpr.regIndex, targetRegIndex, errorRegIndex);

        } else {
            // Ignore NOP opcode
            castExpr.regIndexes = new int[]{rExpr.regIndex, errorRegIndex};
        }

        castExpr.regIndex = castExpr.regIndexes[0];
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        genNode(exprStmtNode.expr, this.env);
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

    private void visitInvokableNode(BLangInvokableNode invokableNode,
                                    CallableUnitInfo callableUnitInfo,
                                    SymbolEnv invokableSymbolEnv) {
        int codeAttrNameCPIndex = addUTF8CPEntry(currentPkgInfo, AttributeInfo.Kind.CODE_ATTRIBUTE.value());

        int localVarAttrNameIndex = addUTF8CPEntry(currentPkgInfo,
                AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE.value());
        LocalVariableAttributeInfo localVarAttributeInfo = new LocalVariableAttributeInfo(localVarAttrNameIndex);

        // TODO Read annotations attached to this callableUnit

        // Add local variable indexes to the parameters and return parameters
        visitInvokableNodeParams(invokableNode.symbol, callableUnitInfo, localVarAttributeInfo);

        if (Symbols.isNative(invokableNode.symbol)) {
            WorkerInfo defaultWorker = callableUnitInfo.defaultWorkerInfo;
            defaultWorker.codeAttributeInfo.attributeNameIndex = codeAttrNameCPIndex;
            defaultWorker.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttributeInfo);

            endWorkerInfoUnit(defaultWorker.codeAttributeInfo);

        } else {
            // Clone lvIndex structure here. This structure contain local variable indexes of the input and
            // out parameters and they are common for all the workers.
            VariableIndex lvIndexCopy = copyVarIndex(lvIndexes);

            WorkerInfo defaultWorker = callableUnitInfo.defaultWorkerInfo;
            defaultWorker.codeAttributeInfo.attributeNameIndex = codeAttrNameCPIndex;
            defaultWorker.codeAttributeInfo.codeAddrs = nextIP();

            localVarAttrInfo = new LocalVariableAttributeInfo(localVarAttrNameIndex);
            // Create a copy of the local variables
            localVarAttrInfo.localVars = new ArrayList<>(localVarAttributeInfo.localVars);
            defaultWorker.addAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE, localVarAttrInfo);

            // Visit the invokableNode body
            genNode(invokableNode.body, invokableSymbolEnv);

            // Clean up the var index data structures
            endWorkerInfoUnit(defaultWorker.codeAttributeInfo);
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

    private void setMaxRegIndexes() {
        maxRegIndexes.tInt = (maxRegIndexes.tInt > regIndexes.tInt) ?
                maxRegIndexes.tInt : regIndexes.tInt;
        maxRegIndexes.tFloat = (maxRegIndexes.tFloat > regIndexes.tFloat) ?
                maxRegIndexes.tFloat : regIndexes.tFloat;
        maxRegIndexes.tString = (maxRegIndexes.tString > regIndexes.tString) ?
                maxRegIndexes.tString : regIndexes.tString;
        maxRegIndexes.tBoolean = (maxRegIndexes.tBoolean > regIndexes.tBoolean) ?
                maxRegIndexes.tBoolean : regIndexes.tBoolean;
        maxRegIndexes.tBlob = (maxRegIndexes.tBlob > regIndexes.tBlob) ?
                maxRegIndexes.tBlob : regIndexes.tBlob;
        maxRegIndexes.tRef = (maxRegIndexes.tRef > regIndexes.tRef) ?
                maxRegIndexes.tRef : regIndexes.tRef;
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


    // Create info entries

    private void createPackageVarInfo(BVarSymbol varSymbol) {
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

    private void createFunctionInfoEntry(BInvokableSymbol funcSymbol) {
        BInvokableType funcType = (BInvokableType) funcSymbol.type;

        // Add function name as an UTFCPEntry to the constant pool
        int funcNameCPIndex = addUTF8CPEntry(currentPkgInfo, funcSymbol.name.value);

        FunctionInfo funcInfo = new FunctionInfo(currentPackageRefCPIndex, funcNameCPIndex);
        funcInfo.paramTypes = funcType.paramTypes.toArray(new BType[0]);
        funcInfo.retParamTypes = funcType.retTypes.toArray(new BType[0]);
        funcInfo.flags = funcSymbol.flags;

        // Create default worker
        int workerNameCPIndex = addUTF8CPEntry(currentPkgInfo, "default");
        funcInfo.defaultWorkerInfo = new WorkerInfo(workerNameCPIndex, "default");

        // TODO Add worker details
//        if (function instanceof BallerinaFunction) {
//            addWorkerInfoEntries(funcInfo, (function).getWorkers());
//        }
//        generateCallableUnitInfoDataChannelMap(function, funcInfo);

        funcInfo.signatureCPIndex = addUTF8CPEntry(currentPkgInfo, generateSignature(funcInfo));
        currentPkgInfo.functionInfoMap.put(funcSymbol.name.value, funcInfo);
    }


    // Constant pool related utility classes

    private int addUTF8CPEntry(ConstantPool pool, String value) {
        UTF8CPEntry pkgPathCPEntry = new UTF8CPEntry(value);
        return pool.addCPEntry(pkgPathCPEntry);
    }

    private int addPackageRefCPEntry(ConstantPool pool, String name, String version) {
        int nameCPIndex = addUTF8CPEntry(pool, name);
        int versionCPIndex = addUTF8CPEntry(pool, version);
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
        /* ignore */
    }

    public void visit(BLangForkJoin forkJoin) {
        /* ignore */
    }

    public void visit(BLangWorkerSend workerSendNode) {
        /* ignore */
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        /* ignore */
    }

    public void visit(BLangService serviceNode) {
        /* ignore */
    }

    public void visit(BLangResource resourceNode) {
        /* ignore */
    }

    public void visit(BLangConnector connectorNode) {
        /* ignore */
    }

    public void visit(BLangAction actionNode) {
        /* ignore */
    }

    public void visit(BLangStruct structNode) {
        /* ignore */
    }

    public void visit(BLangEnum enumNode) {
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
        if (assignNode.declaredWithVar) {
            assignNode.varRefs.stream()
                    .filter(v -> v.type.tag != TypeTags.NONE)
                    .forEach(v -> {
                        v.regIndex = getNextIndex(v.type.tag, lvIndexes);
                        BLangVariableReference varRef = (BLangVariableReference) v;
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

    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    public void visit(BLangContinue continueNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    public void visit(BLangReply replyNode) {
        /* ignore */
    }

    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    public void visit(BLangComment commentNode) {
        /* ignore */
    }

    public void visit(BLangIf ifNode) {
        /* ignore */
    }

    public void visit(BLangWhile whileNode) {
        /* ignore */
    }

    public void visit(BLangTransaction transactionNode) {
        /* ignore */
    }

    public void visit(BlangTransform transformNode) {
        /* ignore */
    }

    public void visit(BLangTryCatchFinally tryNode) {
        /* ignore */
    }

    public void visit(BLangCatch catchNode) {
        /* ignore */
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        /* ignore */
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        /* ignore */
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        /* ignore */
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        /* ignore */
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        int opcode;
        int lvIndex;
        BXMLNSSymbol nsSymbol = xmlnsNode.symbol;

        BLangExpression nsURIExpr = xmlnsNode.namespaceURI;
        if (nsURIExpr != null) {
            genNode(nsURIExpr, this.env);
            rhsExprRegIndex = nsURIExpr.regIndex;
        }

        int ownerSymTag = env.scope.owner.tag;
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(xmlnsNode.type.tag, InstructionCodes.ISTORE, lvIndexes);
            opcode = opcodeAndIndex.opcode;
            lvIndex = opcodeAndIndex.index;
            nsSymbol.nsURIIndex = lvIndex;
            if (nsURIExpr != null) {
                emit(opcode, rhsExprRegIndex, lvIndex);
            }
        } else {
            // TODO Support global xmlns declr
            throw new AssertionError();
        }
    }

    public void visit(BLangXMLQName xmlQName) {
        // If the QName is use outside of XML, treat it as string.
        if (!xmlQName.isUsedInXML) {
            String qName =
                    (xmlQName.namespaceURI == null ? "" : "{" + xmlQName.namespaceURI + "}") + xmlQName.localname;
            BLangLiteral qNameLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            qNameLiteral.value = qName;
            qNameLiteral.typeTag = TypeTags.STRING;
            genNode(qNameLiteral, env);
            xmlQName.regIndex = qNameLiteral.regIndex;
            return;
        }

        // Else, treat it as QName

        int nsURIIndex = getNamespaceURIIndex(xmlQName.nsSymbol);

        BLangLiteral localnameLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        localnameLiteral.value = xmlQName.localname.value;
        localnameLiteral.typeTag = TypeTags.STRING;
        genNode(localnameLiteral, env);

        BLangLiteral prefixLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        prefixLiteral.value = xmlQName.prefix.value;
        prefixLiteral.typeTag = TypeTags.STRING;
        genNode(prefixLiteral, env);

        xmlQName.regIndex = ++regIndexes.tRef;
        emit(InstructionCodes.NEWQNAME, localnameLiteral.regIndex, nsURIIndex, prefixLiteral.regIndex,
                xmlQName.regIndex);
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        BLangExpression attrNameExpr = (BLangExpression) xmlAttribute.name;
        genNode(attrNameExpr, this.env);
        int attrQNameRegIndex = attrNameExpr.regIndex;

        // If the attribute name is a string representation of qname
        if (attrNameExpr.getKind() != NodeKind.XML_QNAME) {
            int localNameRegIndex = ++regIndexes.tString;
            int uriRegIndex = ++regIndexes.tString;
            emit(InstructionCodes.S2QNAME, attrQNameRegIndex, localNameRegIndex, uriRegIndex);

            attrQNameRegIndex = ++regIndexes.tRef;
//            generateURILookupInstructions(xmlElementLiteral.namespaces, localNameRegIndex, uriRegIndex,
//                    attrQNameRegIndex, xmlElementLiteral.pos);
        }

        BLangExpression attrValueExpr = (BLangExpression) xmlAttribute.value;
        genNode(attrValueExpr, this.env);

        if (xmlAttribute.isNamespaceDeclr) {
            ((BXMLNSSymbol) xmlAttribute.symbol).nsURIIndex = attrValueExpr.regIndex;
        }
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.regIndex = ++regIndexes.tRef;

        BLangExpression startTagName = (BLangExpression) xmlElementLiteral.getStartTagName();
        genNode(startTagName, this.env);
        int startTagNameRegIndex = startTagName.regIndex;

        // If this is a string representation of element name, generate the namespace lookup instructions
        if (startTagName.getKind() != NodeKind.XML_QNAME) {
            int localNameRegIndex = ++regIndexes.tString;
            int uriRegIndex = ++regIndexes.tString;
            emit(InstructionCodes.S2QNAME, startTagNameRegIndex, localNameRegIndex, uriRegIndex);

            startTagNameRegIndex = ++regIndexes.tRef;
            generateURILookupInstructions(xmlElementLiteral.namespaces, localNameRegIndex, uriRegIndex,
                    startTagNameRegIndex, xmlElementLiteral.pos);
        }

        // TODO: do we need to generate end tag name?
        int endTagNameRegIndex = startTagNameRegIndex;

        // Create an XML with the given QName
        int defaultNsURIIndex = getNamespaceURIIndex(xmlElementLiteral.defaultNsSymbol);
        emit(InstructionCodes.NEWXMLELEMENT, xmlElementLiteral.regIndex, startTagNameRegIndex, endTagNameRegIndex,
                defaultNsURIIndex);

        // Add attributes
        List<XMLAttributeNode> attributes = xmlElementLiteral.attributes;
        for (XMLAttributeNode attribute : attributes) {
            BLangXMLAttribute attr = (BLangXMLAttribute) attribute;
            genNode(attr, this.env);
            emit(InstructionCodes.XMLATTRSTORE, xmlElementLiteral.regIndex, attr.name.regIndex, attr.value.regIndex);
        }

        // Add children
        for (ExpressionNode child : xmlElementLiteral.modifiedChildren) {
            BLangExpression childExpr = (BLangExpression) child;
            genNode(childExpr, this.env);
            emit(InstructionCodes.XMLSTORE, xmlElementLiteral.regIndex, childExpr.regIndex);
        }
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.regIndex = ++regIndexes.tRef;
        genNode(xmlTextLiteral.concatExpr, env);
        emit(InstructionCodes.NEWXMLTEXT, xmlTextLiteral.regIndex, xmlTextLiteral.concatExpr.regIndex);
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.regIndex = ++regIndexes.tRef;
        genNode(xmlCommentLiteral.concatExpr, env);
        emit(InstructionCodes.NEWXMLCOMMENT, xmlCommentLiteral.regIndex, xmlCommentLiteral.concatExpr.regIndex);
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.regIndex = ++regIndexes.tRef;
        genNode(xmlProcInsLiteral.dataConcatExpr, env);
        genNode(xmlProcInsLiteral.target, env);
        emit(InstructionCodes.NEWXMLPI, xmlProcInsLiteral.regIndex, xmlProcInsLiteral.target.regIndex,
                xmlProcInsLiteral.dataConcatExpr.regIndex);
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        genNode(xmlQuotedString.concatExpr, env);
        xmlQuotedString.regIndex = xmlQuotedString.concatExpr.regIndex;
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        /* ignore */
    }

    private int getNamespaceURIIndex(BXMLNSSymbol defaultNsSymbol) {
        // If the namespace is declared in-line within the XML, get the URI index in the registry.
        if (defaultNsSymbol != null && defaultNsSymbol.definedInline) {
            return defaultNsSymbol.nsURIIndex;
        }

        // If the namespace is defined at top, get the URI index in the local var registry.
        if (defaultNsSymbol != null && !defaultNsSymbol.definedInline) {
            OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(TypeTags.STRING, InstructionCodes.ILOAD, regIndexes);
            emit(opcodeAndIndex.opcode, defaultNsSymbol.nsURIIndex, opcodeAndIndex.index);
            return opcodeAndIndex.index;
        }

        // create the prefix literal
        BLangLiteral nsURI = (BLangLiteral) TreeBuilder.createLiteralExpression();
        nsURI.value = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        nsURI.typeTag = TypeTags.STRING;
        genNode(nsURI, env);

        return nsURI.regIndex;
    }

    private void generateURILookupInstructions(Map<String, BXMLNSSymbol> namespaces, int localNameRegIndex,
                                               int uriRegIndex, int targetQNameRegIndex, DiagnosticPos pos) {
        if (namespaces.isEmpty()) {
            createQNameWithEmptyPrefix(localNameRegIndex, uriRegIndex, targetQNameRegIndex, pos);
            return;
        }

        List<Instruction> gotoInstructionList = new ArrayList<>();
        Instruction ifInstruction;
        Instruction gotoInstruction;
        String prefix;
        for (Entry<String, BXMLNSSymbol> keyValues : namespaces.entrySet()) {
            prefix = keyValues.getKey();

            // skip the default namespace
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                continue;
            }

            // Below section creates the condition to compare the namespace URIs

            // store the comparing uri as string
            BXMLNSSymbol nsSymbol = keyValues.getValue();

            int opcode = getOpcode(TypeTags.STRING, InstructionCodes.IEQ);
            int exprIndex = ++regIndexes.tBoolean;
            emit(opcode, uriRegIndex, nsSymbol.nsURIIndex, exprIndex);

            ifInstruction = InstructionFactory.get(InstructionCodes.BR_FALSE, exprIndex, -1);
//            emit(ifInstruction);

            // Below section creates instructions to be executed, if the above condition succeeds (then body)

            // create the prefix literal
            BLangLiteral prefixLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            prefixLiteral.value = prefix;
            prefixLiteral.typeTag = TypeTags.STRING;
            genNode(prefixLiteral, env);

            // create a qname
            emit(InstructionCodes.NEWQNAME, localNameRegIndex, uriRegIndex, prefixLiteral.regIndex,
                    targetQNameRegIndex);

            gotoInstruction = InstructionFactory.get(InstructionCodes.GOTO, -1);
//            emit(gotoInstruction);
            gotoInstructionList.add(gotoInstruction);
            ifInstruction.setOperand(1, nextIP());
        }

        // else part. create a qname with empty prefix
        createQNameWithEmptyPrefix(localNameRegIndex, uriRegIndex, targetQNameRegIndex, pos);

        int nextIP = nextIP();
        for (Instruction instruction : gotoInstructionList) {
            instruction.setOperand(0, nextIP);
        }
    }

    private void createQNameWithEmptyPrefix(int localNameRegIndex, int uriRegIndex, int targetQnameRegIndex,
                                            DiagnosticPos pos) {
        // TODO
    }
}

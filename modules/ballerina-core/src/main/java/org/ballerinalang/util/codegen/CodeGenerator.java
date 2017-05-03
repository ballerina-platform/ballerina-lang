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
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.MemoryLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
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
import org.ballerinalang.model.CompilationUnit;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BacktickExpr;
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
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.MapStructInitKeyValueExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ReferenceExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructFieldAccessExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.invokers.MainInvoker;
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
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionReturnCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.PackageCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.StructCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generates Ballerina bytecode instructions.
 *
 * @since 0.87
 */
public class CodeGenerator implements NodeVisitor {
    private static final int INT_OFFSET = 0;
    private static final int FLOAT_OFFSET = 1;
    private static final int STRING_OFFSET = 2;
    private static final int BOOL_OFFSET = 3;
    private static final int REF_OFFSET = 4;

    private int[] maxRegIndexes = {-1, -1, -1, -1, -1};

    // This int array hold then current local variable index of following types
    // index 0 - int, 1 - float, 2 - string, 3 - boolean, 4 - reference(BValue)
    private int[] lvIndexes = {-1, -1, -1, -1, -1};

    // This int array hold then current register index of following types
    // index 0 - int, 1 - float, 2 - string, 3 - boolean, 4 - reference(BValue)
    private int[] regIndexes = {-1, -1, -1, -1, -1};

    // This int array hold then current register index of following types
    // index 0 - int, 1 - float, 2 - string, 3 - boolean, 4 - reference(BValue)
    private int[] fieldIndexes = {-1, -1, -1, -1, -1};

    private ProgramFile programFile = new ProgramFile();
    private FunctionInfo funcInfo;
    private int pkgCPEntryIndex = -1;

    private SymbolScope currentScope;


    // Required variables to generate code for assignment statements
    int rhsExprRegIndex = -1;
    boolean varAssignment;
    boolean arrayAssignment;
    boolean structAssignment;
    int structRegIndex = -1;

    public ProgramFile getProgramFile() {
        return programFile;
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
    }

    @Override
    public void visit(BLangPackage bLangPackage) {
        for (BLangPackage dependentPkg : bLangPackage.getDependentPackages()) {
            if (dependentPkg.isSymbolsDefined()) {
                continue;
            }

            dependentPkg.accept(this);
        }

        UTF8CPEntry pkgNameEntry = new UTF8CPEntry(bLangPackage.getPackagePath());
        int pkgNameEntryIndex = programFile.addCPEntry(pkgNameEntry);
        PackageCPEntry packageCPEntry = new PackageCPEntry(pkgNameEntryIndex);
        pkgCPEntryIndex = programFile.addCPEntry(packageCPEntry);

        // TODO Create FunctionInfo structures for all the functions defined in this package

        // 4) Complete function call / invocation work - Complete this tonight
        // 5) Complete type specific registers
        // 6) Figure out a way to return values from the functions..
        // 7) Link function to the Call instruction... May be using CallInstruction

        createFunctionInfoEntries(bLangPackage.getFunctions());
        createStructInfoEntries(bLangPackage.getStructDefs());

        for (CompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
            compilationUnit.accept(this);
        }

        pkgCPEntryIndex = -1;
    }

    private void createStructInfoEntries(StructDef[] structDefs) {
        for (StructDef structDef : structDefs) {
            // Add Struct name as an UTFCPEntry to the constant pool
            UTF8CPEntry nameUTF8CPEntry = new UTF8CPEntry(structDef.getName());
            int nameIndex = programFile.addCPEntry(nameUTF8CPEntry);

            // Add FunctionCPEntry to constant pool
            StructCPEntry structCPEntry = new StructCPEntry(pkgCPEntryIndex, nameIndex);
            int structCPEntryIndex = programFile.addCPEntry(structCPEntry);

            VariableDefStmt[] varDefStmts = structDef.getFieldDefStmts();
            for (int i = 0, varDefStmtsLength = varDefStmts.length; i < varDefStmtsLength; i++) {
                VariableDefStmt fieldDefStmt = varDefStmts[i];
                VariableDef fieldDef = fieldDefStmt.getVariableDef();
                BType fieldType = fieldDef.getType();

                int fieldIndex = getNextIndex(fieldType.getTag(), fieldIndexes);
                StructVarLocation structVarLocation = new StructVarLocation(fieldIndex);
                fieldDef.setMemoryLocation(structVarLocation);
            }

            structCPEntry.setFieldCount(Arrays.copyOf(prepareIndexes(fieldIndexes), fieldIndexes.length));
            resetIndexes(fieldIndexes);
        }
    }

    private void createFunctionInfoEntries(Function[] functions) {
        for (Function function : functions) {
            // Add function name as an UTFCPEntry to the constant pool
            UTF8CPEntry nameUTF8CPEntry = new UTF8CPEntry(function.getName());
            int nameIndex = programFile.addCPEntry(nameUTF8CPEntry);

            // Add FunctionCPEntry to constant pool
            FunctionCPEntry functionCPEntry = new FunctionCPEntry(pkgCPEntryIndex, nameIndex);
            int funcCPEntryIndex = programFile.addCPEntry(functionCPEntry);

            // Add function descriptor as an UTFCPEntry to the constant pool
            UTF8CPEntry descUTF8CPEntry = new UTF8CPEntry(getFunctionDescriptor(function));
            int descIndex = programFile.addCPEntry(descUTF8CPEntry);

            // Add FunctionInfo to the program file.
            FunctionInfo funcInfo = new FunctionInfo(pkgCPEntryIndex, funcCPEntryIndex, descIndex);
            funcInfo.setParamTypeSigs(getTypeSigsForParamDefs(function.getParameterDefs()));
            funcInfo.setRetParamTypeSigs(getTypeSigsForParamDefs(function.getReturnParameters()));

            // TODO Set other flags such as isNative..
            programFile.addFunctionInfo(funcInfo);
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
    public void visit(Service service) {

    }

    @Override
    public void visit(BallerinaConnectorDef connector) {

    }

    @Override
    public void visit(Resource resource) {

    }

    @Override
    public void visit(BallerinaFunction function) {
        openScope(function);

        UTF8CPEntry nameUTF8CPEntry = new UTF8CPEntry(function.getName());
        int nameIndex = programFile.getCPEntryIndex(nameUTF8CPEntry);

        UTF8CPEntry descUTF8CPEntry = new UTF8CPEntry(getFunctionDescriptor(function));
        int descIndex = programFile.getCPEntryIndex(descUTF8CPEntry);

        int funcCPEntryIndex = programFile.getCPEntryIndex(new FunctionCPEntry(pkgCPEntryIndex, nameIndex));

        // Get FunctionInfo object from program file
        funcInfo = programFile.getFunctionInfo(new FunctionInfo(pkgCPEntryIndex, funcCPEntryIndex, descIndex));

        UTF8CPEntry codeUTF8CPEntry = new UTF8CPEntry("Code");
        funcInfo.codeAttributeInfo.setAttributeNameIndex(programFile.addCPEntry(codeUTF8CPEntry));
        funcInfo.codeAttributeInfo.setCodeAddrs(nextIP());

        // Add local variable indexes to the parameters and return parameters
        for (ParameterDef parameterDef : function.getParameterDefs()) {
            int lvIndex = getNextIndex(parameterDef.getType().getTag(), lvIndexes);
            parameterDef.setMemoryLocation(new StackVarLocation(lvIndex));
            parameterDef.accept(this);
        }

        for (ParameterDef parameterDef : function.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                int lvIndex = getNextIndex(parameterDef.getType().getTag(), lvIndexes);
                parameterDef.setMemoryLocation(new StackVarLocation(lvIndex));
            }

            parameterDef.accept(this);
        }

        function.getCallableUnitBody().accept(this);

        funcInfo.codeAttributeInfo.setMaxLongLocalVars(lvIndexes[INT_OFFSET] + 1);
        funcInfo.codeAttributeInfo.setMaxDoubleLocalVars(lvIndexes[FLOAT_OFFSET] + 1);
        funcInfo.codeAttributeInfo.setMaxStringLocalVars(lvIndexes[STRING_OFFSET] + 1);
        funcInfo.codeAttributeInfo.setMaxIntLocalVars(lvIndexes[BOOL_OFFSET] + 1);
        funcInfo.codeAttributeInfo.setMaxBValueLocalVars(lvIndexes[REF_OFFSET] + 1);

        funcInfo.codeAttributeInfo.setMaxLongRegs(maxRegIndexes[INT_OFFSET] + 1);
        funcInfo.codeAttributeInfo.setMaxDoubleRegs(maxRegIndexes[FLOAT_OFFSET] + 1);
        funcInfo.codeAttributeInfo.setMaxStringRegs(maxRegIndexes[STRING_OFFSET] + 1);
        funcInfo.codeAttributeInfo.setMaxIntRegs(maxRegIndexes[BOOL_OFFSET] + 1);
        funcInfo.codeAttributeInfo.setMaxBValueRegs(maxRegIndexes[REF_OFFSET] + 1);
        funcInfo = null;

        endCallableUnit();
        closeScope();
    }

    @Override
    public void visit(BTypeMapper typeMapper) {

    }

    @Override
    public void visit(BallerinaAction action) {

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
    public void visit(VariableDef variableDef) {

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
        int opcode = -1;
        int lvIndex = -1;
        MemoryLocation stackVarLocation;
        VariableDef variableDef = varDefStmt.getVariableDef();

        if (currentScope.getScopeName() == SymbolScope.ScopeName.LOCAL) {
            OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(variableDef.getType().getTag(),
                    InstructionCodes.istore, lvIndexes);
            opcode = opcodeAndIndex.opcode;
            lvIndex = opcodeAndIndex.index;
            stackVarLocation = new StackVarLocation(lvIndex);

        } else if (currentScope.getScopeName() == SymbolScope.ScopeName.CONNECTOR) {
            // TODO
            stackVarLocation = null;
        } else {
            // TODO
            stackVarLocation = null;
        }

        variableDef.setMemoryLocation(stackVarLocation);
        Expression rhsExpr = varDefStmt.getRExpr();
        if (rhsExpr == null) {
            return;
        }

        rhsExpr.accept(this);
        emit(opcode, rhsExpr.getTempOffset(), lvIndex);
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        // Evaluate the rhs expression
        assignStmt.getRExpr().accept(this);

        int[] rhsExprRegIndexes;
        if (assignStmt.getRExpr() instanceof CallableUnitInvocationExpr) {
            rhsExprRegIndexes = ((CallableUnitInvocationExpr) assignStmt.getRExpr()).getOffsets();
        } else {
            rhsExprRegIndexes = new int[]{assignStmt.getRExpr().getTempOffset()};
        }

        Expression[] lhsExprs = assignStmt.getLExprs();
        for (int i = 0; i < lhsExprs.length; i++) {
            rhsExprRegIndex = rhsExprRegIndexes[i];
            Expression lExpr = lhsExprs[i];

            if (lExpr instanceof VariableRefExpr) {
                varAssignment = true;
                lExpr.accept(this);
                varAssignment = false;
            } else if (lExpr instanceof ArrayMapAccessExpr) {
                arrayAssignment = true;
                lExpr.accept(this);
                arrayAssignment = false;
            } else if (lExpr instanceof StructFieldAccessExpr) {
                structAssignment = true;
                lExpr.accept(this);
                structAssignment = false;
            }
        }
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        for (Statement stmt : blockStmt.getStatements()) {
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
        // TODO Support null checks
        Expression ifCondExpr = ifElseStmt.getCondition();
        ifCondExpr.accept(this);
        Instruction gotoInstruction;
        List<Instruction> gotoInstructionList = new ArrayList<>();

        int opcode = getIfOpcode(ifCondExpr);

        // TODO operand2 should be the jump address  else-if or else or to the next instruction after then block
        Instruction ifInstruction = new Instruction(opcode, regIndexes[BOOL_OFFSET], 0);
        emit(ifInstruction);

        ifElseStmt.getThenBody().accept(this);

        // Check whether this then block is the last block of code
        gotoInstruction = new Instruction(InstructionCodes.goto_, 0);
        emit(gotoInstruction);
        gotoInstructionList.add(gotoInstruction);

        ifInstruction.setOperand(1, nextIP());

        // Process else-if parts
        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            elseIfCondition.accept(this);
            opcode = getIfOpcode(elseIfCondition);
            ifInstruction = new Instruction(opcode, regIndexes[BOOL_OFFSET], 0);
            emit(ifInstruction);

            elseIfBlock.getElseIfBody().accept(this);
            gotoInstruction = new Instruction(InstructionCodes.goto_, 0);
            emit(gotoInstruction);
            gotoInstructionList.add(gotoInstruction);
            ifInstruction.setOperand(1, nextIP());
            // TODO check whether there exits 'next' instruction
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

    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        int[] regIndexes = new int[returnStmt.getExprs().length];
        for (int i = 0; i < returnStmt.getExprs().length; i++) {
            Expression expr = returnStmt.getExprs()[i];
            expr.accept(this);
            regIndexes[i] = expr.getTempOffset();
        }

        FunctionReturnCPEntry funcRetCPEntry = new FunctionReturnCPEntry(regIndexes);
        int funcRetCPEntryIndex = programFile.addCPEntry(funcRetCPEntry);
        emit(InstructionCodes.ret, funcRetCPEntryIndex);
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        Expression conditionExpr = whileStmt.getCondition();
        Instruction gotoInstruction = new Instruction(InstructionCodes.goto_, nextIP());

        conditionExpr.accept(this);
        int opcode = getIfOpcode(conditionExpr);
        Instruction ifInstruction = new Instruction(opcode, regIndexes[BOOL_OFFSET], 0);
        emit(ifInstruction);

        whileStmt.getBody().accept(this);

        emit(gotoInstruction);
        ifInstruction.setOperand(1, nextIP());
    }

    @Override
    public void visit(BreakStmt breakStmt) {

    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {

    }

    @Override
    public void visit(ThrowStmt throwStmt) {

    }

    @Override
    public void visit(FunctionInvocationStmt functionInvocationStmt) {

    }

    @Override
    public void visit(ActionInvocationStmt actionInvocationStmt) {

    }

    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {

    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {

    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {

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
                    opcode = InstructionCodes.iconst_0 + (int) intVal;
                    emit(opcode, basicLiteral.getTempOffset());
                } else {
                    IntegerCPEntry intCPEntry = new IntegerCPEntry(basicLiteral.getBValue().intValue());
                    int intCPEntryIndex = programFile.addCPEntry(intCPEntry);
                    emit(InstructionCodes.iconst, intCPEntryIndex, basicLiteral.getTempOffset());
                }
                break;

            case TypeTags.FLOAT_TAG:
                basicLiteral.setTempOffset(++regIndexes[FLOAT_OFFSET]);
                double floatVal = basicLiteral.getBValue().floatValue();
                if (floatVal == 0 || floatVal == 1 || floatVal == 2 ||
                        floatVal == 3 || floatVal == 4 || floatVal == 5) {
                    opcode = InstructionCodes.fconst_0 + (int) floatVal;
                    emit(opcode, basicLiteral.getTempOffset());
                } else {
                    FloatCPEntry floatCPEntry = new FloatCPEntry(basicLiteral.getBValue().floatValue());
                    int floatCPEntryIndex = programFile.addCPEntry(floatCPEntry);
                    emit(InstructionCodes.fconst, floatCPEntryIndex, basicLiteral.getTempOffset());
                }
                break;

            case TypeTags.STRING_TAG:
                basicLiteral.setTempOffset(++regIndexes[STRING_OFFSET]);
                String strValue = basicLiteral.getBValue().stringValue();
                UTF8CPEntry utf8CPEntry = new UTF8CPEntry(strValue);
                int stringValCPIndex = programFile.addCPEntry(utf8CPEntry);

                StringCPEntry stringCPEntry = new StringCPEntry(stringValCPIndex, strValue);
                int strCPEntryIndex = programFile.addCPEntry(stringCPEntry);

                emit(InstructionCodes.sconst, strCPEntryIndex, basicLiteral.getTempOffset());
                break;

            case TypeTags.BOOLEAN_TAG:
                basicLiteral.setTempOffset(++regIndexes[BOOL_OFFSET]);
                boolean booleanVal = basicLiteral.getBValue().booleanValue();
                if (!booleanVal) {
                    opcode = InstructionCodes.bconst_0;
                } else {
                    opcode = InstructionCodes.bconst_1;
                }
                emit(opcode, basicLiteral.getTempOffset());
                break;
        }
    }

    @Override
    public void visit(NullLiteral nullLiteral) {
    }


    // Binary arithmetic expressions

    @Override
    public void visit(AddExpression addExpr) {
        emitBinaryArithmeticExpr(addExpr, InstructionCodes.iadd);
    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        emitBinaryArithmeticExpr(subtractExpr, InstructionCodes.isub);
    }

    @Override
    public void visit(MultExpression multExpr) {
        emitBinaryArithmeticExpr(multExpr, InstructionCodes.imul);
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        emitBinaryArithmeticExpr(divideExpr, InstructionCodes.idiv);
    }

    @Override
    public void visit(ModExpression modExpr) {
        emitBinaryArithmeticExpr(modExpr, InstructionCodes.imod);
    }


    // Binary logical expressions

    @Override
    public void visit(AndExpression andExpression) {

    }

    @Override
    public void visit(OrExpression orExpression) {

    }


    // Binary equality expressions

    @Override
    public void visit(EqualExpression equalExpr) {
        emitBinaryCompareAndEqualityExpr(equalExpr, InstructionCodes.icmp);
    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {
        emitBinaryCompareAndEqualityExpr(notEqualExpr, InstructionCodes.icmp);
    }


    // Binary comparison expressions

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {
        emitBinaryCompareAndEqualityExpr(greaterEqualExpr, InstructionCodes.icmp);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        emitBinaryCompareAndEqualityExpr(greaterThanExpr, InstructionCodes.icmp);
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        emitBinaryCompareAndEqualityExpr(lessEqualExpr, InstructionCodes.icmp);
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        emitBinaryCompareAndEqualityExpr(lessThanExpr, InstructionCodes.icmp);
    }


    // Callable unit invocation expressions

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        String packagePath = (funcIExpr.getPackagePath() != null) ?
                funcIExpr.getPackagePath() : ".";

        // TODO Refactor this code. We need cache package indexes somewhere
        UTF8CPEntry pkgNameCPEntry = new UTF8CPEntry(packagePath);
        int pkgNameIndex = programFile.getCPEntryIndex(pkgNameCPEntry);

        PackageCPEntry pkgCPEntry = new PackageCPEntry(pkgNameIndex);
        int pkgCPEntryIndex = programFile.getCPEntryIndex(pkgCPEntry);

        UTF8CPEntry nameCPEntry = new UTF8CPEntry(funcIExpr.getName());
        int nameIndex = programFile.getCPEntryIndex(nameCPEntry);

        FunctionCPEntry funcRefCPEntry = new FunctionCPEntry(pkgCPEntryIndex, nameIndex);
        int funcRefCPIndex = programFile.getCPEntryIndex(funcRefCPEntry);

        Expression[] argExprs = funcIExpr.getArgExprs();
        int[] argRegs = new int[argExprs.length];
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);
            argRegs[i] = argExpr.getTempOffset();
        }

        // Calculate registers to store return values
        BType[] retTypes = funcIExpr.getTypes();
        int[] retRegs = new int[retTypes.length];
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = retTypes[i];
            retRegs[i] = getNextIndex(retType.getTag(), regIndexes);
        }

        FunctionCallCPEntry funcCallCPEntry = new FunctionCallCPEntry(argRegs, retRegs);
        int funcCallIndex = programFile.addCPEntry(funcCallCPEntry);

        funcIExpr.setOffsets(retRegs);
        funcIExpr.setTempOffset(retRegs[0]);
        emit(InstructionCodes.call, funcRefCPIndex, funcCallIndex);
    }

    @Override
    public void visit(ActionInvocationExpr actionInvocationExpr) {

    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {

    }

    @Override
    public void visit(UnaryExpression unaryExpression) {

    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {

    }

    @Override
    public void visit(BacktickExpr backtickExpr) {

    }


    // Init expressions

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
//        int dimentions = 0;
//        BType elementType = arrayInitExpr.getType();
//        while(elementType instanceof BArrayType) {
//            dimentions++;
//            elementType = ((BArrayType) elementType).getElementType();
//        }

        // TODO consider array of arrays
        BType elementType = ((BArrayType) arrayInitExpr.getType()).getElementType();

        // Emit create array instruction
        int opcode = getOpcode(elementType.getTag(), InstructionCodes.inewarray);
        int arrayVarRegIndex = ++regIndexes[REF_OFFSET];
        arrayInitExpr.setTempOffset(arrayVarRegIndex);
        emit(opcode, arrayVarRegIndex);

        // Emit instructions populate initial array values;
        Expression[] argExprs = arrayInitExpr.getArgExprs();
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);

            BasicLiteral indexLiteral = new BasicLiteral(arrayInitExpr.getNodeLocation(), new BInteger(i));
            indexLiteral.setType(BTypes.typeInt);
            indexLiteral.accept(this);

            opcode = getOpcode(argExpr.getType().getTag(), InstructionCodes.iastore);
            emit(opcode, arrayVarRegIndex, indexLiteral.getTempOffset(), argExpr.getTempOffset());
        }
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {

    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {

    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        StructDef structDef = (StructDef) structInitExpr.getType();

        String packagePath = (structDef.getPackagePath() != null) ? structDef.getPackagePath() : ".";
        UTF8CPEntry pkgUTF8CPEntry = new UTF8CPEntry(packagePath);
        int pkgIndex = programFile.getCPEntryIndex(pkgUTF8CPEntry);

        PackageCPEntry pkgCPEntry = new PackageCPEntry(pkgIndex);
        int pkgCPEntryIndex = programFile.getCPEntryIndex(pkgCPEntry);

        UTF8CPEntry nameUTF8CPEntry = new UTF8CPEntry(structDef.getName());
        int nameIndex = programFile.getCPEntryIndex(nameUTF8CPEntry);

        // Add FunctionCPEntry to constant pool
        StructCPEntry structCPEntry = new StructCPEntry(pkgCPEntryIndex, nameIndex);
        int structCPEntryIndex = programFile.getCPEntryIndex(structCPEntry);

        //Emit an instruction to create a new struct.
        int structRegIndex = ++regIndexes[REF_OFFSET];
        emit(InstructionCodes.newstruct, structCPEntryIndex, structRegIndex);
        structInitExpr.setTempOffset(structRegIndex);

        for (Expression expr : structInitExpr.getArgExprs()) {
            MapStructInitKeyValueExpr keyValueExpr = (MapStructInitKeyValueExpr) expr;
            VariableRefExpr varRefExpr = (VariableRefExpr) keyValueExpr.getKeyExpr();
            int fieldIndex = ((StructVarLocation) varRefExpr.getMemoryLocation()).getStructMemAddrOffset();

            Expression valueExpr = keyValueExpr.getValueExpr();
            valueExpr.accept(this);

            int opcode = getOpcode(varRefExpr.getType().getTag(), InstructionCodes.ifieldstore);
            emit(opcode, structRegIndex, fieldIndex, valueExpr.getTempOffset());
        }
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {

    }

    @Override
    public void visit(MapStructInitKeyValueExpr keyValueExpr) {

    }


    // Variable reference expressions

    @Override
    public void visit(StructFieldAccessExpr structFieldAccessExpr) {
        StructFieldAccessExpr childSFAccessExpr = structFieldAccessExpr;

        while (true) {
            ReferenceExpr referenceExpr = childSFAccessExpr.getVarRef();
            boolean isAssignment = childSFAccessExpr.getFieldExpr() == null && structAssignment;
            if (referenceExpr instanceof VariableRefExpr) {
                varAssignment = isAssignment;
                referenceExpr.accept(this);
                varAssignment = false;

            } else if (referenceExpr instanceof ArrayMapAccessExpr) {
                arrayAssignment = isAssignment;
                referenceExpr.accept(this);
                arrayAssignment = false;
            }

            if (isAssignment || childSFAccessExpr.getFieldExpr() == null) {
                structFieldAccessExpr.setTempOffset(referenceExpr.getTempOffset());
                break;
            }
            childSFAccessExpr = childSFAccessExpr.getFieldExpr();
        }
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        Expression arrayVarExpr = arrayMapAccessExpr.getRExpr();
        arrayVarExpr.accept(this);

        Expression[] indexExprs = arrayMapAccessExpr.getIndexExprs();
        for (int i = indexExprs.length - 1; i >= 0; i--) {
            Expression indexExpr = indexExprs[i];
            indexExpr.accept(this);

            if (i == 0) {
                // Here we assume that the array reference is stored in the current reference register;
                int arrayIndex = regIndexes[REF_OFFSET];
                if (arrayAssignment) {
                    int opcode = getOpcode(arrayMapAccessExpr.getType().getTag(), InstructionCodes.iastore);
                    emit(opcode, arrayIndex, indexExpr.getTempOffset(), rhsExprRegIndex);
                } else {
                    OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(arrayMapAccessExpr.getType().getTag(),
                            InstructionCodes.iaload, regIndexes);
                    arrayMapAccessExpr.setTempOffset(opcodeAndIndex.index);
                    emit(opcodeAndIndex.opcode, arrayIndex, indexExpr.getTempOffset(), opcodeAndIndex.index);
                }
            } else {
                // reg, index, reg
                emit(InstructionCodes.raload, regIndexes[REF_OFFSET],
                        indexExpr.getTempOffset(), ++regIndexes[REF_OFFSET]);
            }
        }
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        int opcode;
        int exprRegIndex;

        MemoryLocation memoryLocation = variableRefExpr.getVariableDef().getMemoryLocation();
        if (memoryLocation instanceof StackVarLocation) {
            int lvIndex = ((StackVarLocation) memoryLocation).getStackFrameOffset();
            if (varAssignment) {
                opcode = getOpcode(variableRefExpr.getType().getTag(),
                        InstructionCodes.istore);

                emit(opcode, rhsExprRegIndex, lvIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(variableRefExpr.getType().getTag(),
                        InstructionCodes.iload, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;
                emit(opcode, lvIndex, exprRegIndex);
                variableRefExpr.setTempOffset(exprRegIndex);
            }

        } else if (memoryLocation instanceof StructVarLocation) {
            int fieldIndex = ((StructVarLocation) memoryLocation).getStructMemAddrOffset();

            // Since we are processing a struct field here, the struct reference must be stored in the current
            //  reference register index.
            int structRegIndex = regIndexes[REF_OFFSET];

            if (varAssignment) {
                opcode = getOpcode(variableRefExpr.getType().getTag(),
                        InstructionCodes.ifieldstore);
                emit(opcode, structRegIndex, fieldIndex, rhsExprRegIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(variableRefExpr.getType().getTag(),
                        InstructionCodes.ifieldload, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;

                emit(opcode, structRegIndex, fieldIndex, exprRegIndex);
                variableRefExpr.setTempOffset(exprRegIndex);
            }
        }
    }

    @Override
    public void visit(StackVarLocation stackVarLocation) {

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

    @Override
    public void visit(ResourceInvocationExpr resourceIExpr) {

    }

    @Override
    public void visit(MainInvoker mainInvoker) {

    }

    @Override
    public void visit(WorkerVarLocation workerVarLocation) {

    }


    // Private methods

    private void openScope(SymbolScope symbolScope) {
        currentScope = symbolScope;
    }

    private void closeScope() {
        currentScope = currentScope.getEnclosingScope();
    }

    private void endCallableUnit() {
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
            default:
                opcode = baseOpcode + REF_OFFSET;
                break;
        }

        return opcode;
    }

    private String getFunctionDescriptor(Function function) {
        StringBuilder sb = new StringBuilder("(");
        ParameterDef[] paramDefs = function.getParameterDefs();
        sb.append(getParamDefSig(paramDefs));
        sb.append(")");

        ParameterDef[] retParamDefs = function.getReturnParameters();
        sb.append(getParamDefSig(retParamDefs));
        return sb.toString();
    }

    private String getParamDefSig(ParameterDef[] paramDefs) {
        StringBuilder sb = new StringBuilder();
        if (paramDefs.length == 0) {
            sb.append(TypeEnum.VOID.getSig());
        } else {
            for (int i = 0; i < paramDefs.length; i++) {
                sb.append(paramDefs[i].getType().getSig());
            }
        }
        return sb.toString();
    }

    private String[] getTypeSigsForParamDefs(ParameterDef[] paramDefs) {
        if (paramDefs.length == 0) {
            return new String[]{TypeEnum.VOID.getSig()};
        }

        String[] typeSigs = new String[paramDefs.length];
        for (int i = 0; i < paramDefs.length; i++) {
            typeSigs[i] = paramDefs[i].getType().getSig();
        }

        return typeSigs;
    }

    private int nextIP() {
        return programFile.getInstructionList().size();
    }

    private int getIfOpcode(Expression expr) {
        int opcode;
        if (expr instanceof EqualExpression) {
            opcode = InstructionCodes.ifne;
        } else if (expr instanceof NotEqualExpression) {
            opcode = InstructionCodes.ifeq;
        } else if (expr instanceof LessThanExpression) {
            opcode = InstructionCodes.ifge;
        } else if (expr instanceof LessEqualExpression) {
            opcode = InstructionCodes.ifgt;
        } else if (expr instanceof GreaterThanExpression) {
            opcode = InstructionCodes.ifle;
        } else {
            opcode = InstructionCodes.iflt;
        }

        return opcode;
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

    private void emitBinaryCompareAndEqualityExpr(BinaryExpression expr, int baseOpcode) {
        expr.getLExpr().accept(this);
        expr.getRExpr().accept(this);

        int opcode = -1;
        int exprOffset = -1;

        // Consider the type of the LHS expression. RHS expression type should be the same
        int typeTag = expr.getLExpr().getType().getTag();
        switch (typeTag) {
            case TypeTags.INT_TAG:
                opcode = baseOpcode;
                exprOffset = ++regIndexes[BOOL_OFFSET];
                break;
            case TypeTags.FLOAT_TAG:
                opcode = baseOpcode + FLOAT_OFFSET;
                exprOffset = ++regIndexes[BOOL_OFFSET];
                break;
            case TypeTags.STRING_TAG:
                opcode = baseOpcode + STRING_OFFSET;
                exprOffset = ++regIndexes[BOOL_OFFSET];
                break;
            case TypeTags.BOOLEAN_TAG:
                opcode = baseOpcode + BOOL_OFFSET;
                exprOffset = ++regIndexes[BOOL_OFFSET];
                break;
            // TODO Handle NULL type
        }

        expr.setTempOffset(exprOffset);
        emit(opcode, expr.getLExpr().getTempOffset(), expr.getRExpr().getTempOffset(), exprOffset);
    }

    private int emit(int opcode, int... operands) {
        return programFile.addInstruction(InstructionFactory.get(opcode, operands));
    }

    private int emit(Instruction instruction) {
        return programFile.addInstruction(instruction);
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

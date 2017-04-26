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
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionReturnCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.PackageCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates Ballerina bytecode instructions.
 *
 * @since 0.87
 */
public class CodeGenerator implements NodeVisitor {
    // Local variable indexes
    private int intLVIndex = -1;
    private int longLVIndex = -1;
    private int doubleLVIndex = -1;
    private int stringLVIndex = -1;
    private int bValueLVIndex = -1;

    // Register indexes
    private int intRegIndex = -1;
    private int longRegIndex = -1;
    private int doubleRegIndex = -1;
    private int stringRegIndex = -1;
    private int bValueRegIndex = -1;

    // Return indexes
    private int intRetIndex = -1;
    private int longRetIndex = -1;
    private int doubleRetIndex = -1;
    private int stringRetIndex = -1;
    private int bValueRetIndex = -1;

    private ProgramFile programFile = new ProgramFile();
    private FunctionInfo funcInfo;
    private int pkgCPEntryIndex = -1;

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

        System.out.println("#####done");
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

        for (CompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
            compilationUnit.accept(this);
        }

        pkgCPEntryIndex = -1;
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
            // TODO Consider types of parameters
            parameterDef.setMemoryLocation(new StackVarLocation(++longLVIndex));
            parameterDef.accept(this);
        }

        for (ParameterDef parameterDef : function.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                // TODO Consider other types of return parameters
                parameterDef.setMemoryLocation(new StackVarLocation(++longLVIndex));
            }

            parameterDef.accept(this);

            // TODO Consider other types of return parameters
            longRegIndex++;

        }

        function.getCallableUnitBody().accept(this);

        funcInfo.codeAttributeInfo.setMaxIntLocalVars(intLVIndex + 1);
        funcInfo.codeAttributeInfo.setMaxLongLocalVars(longLVIndex + 1);
        funcInfo.codeAttributeInfo.setMaxDoubleLocalVars(doubleLVIndex + 1);
        funcInfo.codeAttributeInfo.setMaxStringLocalVars(stringLVIndex + 1);
        funcInfo.codeAttributeInfo.setMaxBValueLocalVars(bValueLVIndex + 1);

        funcInfo.codeAttributeInfo.setMaxIntRegs(intRegIndex + 1);
        funcInfo.codeAttributeInfo.setMaxLongRegs(longRegIndex + 1);
        funcInfo.codeAttributeInfo.setMaxDoubleRegs(doubleLVIndex + 1);
        funcInfo.codeAttributeInfo.setMaxStringRegs(stringRegIndex + 1);
        funcInfo.codeAttributeInfo.setMaxBValueRegs(bValueRegIndex + 1);
        funcInfo = null;

        endCallableUnit();
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
        varDefStmt.getLExpr().accept(this);

        // TODO Figure out the correct memory location for given variable
        StackVarLocation stackVarLocation;
        if (varDefStmt.getVariableDef().getType() == BTypes.typeInt) {
            stackVarLocation = new StackVarLocation(++longLVIndex);
            // TODO Fix the following else condition
        } else {
            stackVarLocation = new StackVarLocation(++intLVIndex);
        }

        varDefStmt.getVariableDef().setMemoryLocation(stackVarLocation);

        Expression rhsExpr = varDefStmt.getRExpr();
        if (rhsExpr == null) {
            return;
        }

        rhsExpr.accept(this);
        emit(new Instruction(InstructionCodes.istore, rhsExpr.getTempOffset(),
                stackVarLocation.getStackFrameOffset()));
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        assignStmt.getRExpr().accept(this);
        if (assignStmt.getRExpr() instanceof CallableUnitInvocationExpr) {
            // Handle multiple return values
            Expression[] lhsExprs = assignStmt.getLExprs();
            int[] offsets = ((FunctionInvocationExpr) assignStmt.getRExpr()).getOffsets();

            for (int i = 0; i < lhsExprs.length; i++) {
                VariableRefExpr varRefExpr = (VariableRefExpr) lhsExprs[i];
                varRefExpr.accept(this);

                // TODO Assumption  need to consider other memory locations
                StackVarLocation stackVarLocation = (StackVarLocation) varRefExpr.getVariableDef().getMemoryLocation();
                emit(new Instruction(InstructionCodes.istore, offsets[i],
                        stackVarLocation.getStackFrameOffset()));
            }
            return;
        }

        assignStmt.getLExprs()[0].accept(this);
        VariableRefExpr varRefExpr = (VariableRefExpr) assignStmt.getLExprs()[0];

        // TODO Assumption  need to consider other memory locations
        StackVarLocation stackVarLocation = (StackVarLocation) varRefExpr.getVariableDef().getMemoryLocation();
        emit(new Instruction(InstructionCodes.istore, assignStmt.getRExpr().getTempOffset(),
                stackVarLocation.getStackFrameOffset()));
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        for (Statement stmt : blockStmt.getStatements()) {
            stmt.accept(this);
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
        Instruction ifInstruction = new Instruction(opcode, intRegIndex, 0);
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
            ifInstruction = new Instruction(opcode, intRegIndex, 0);
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
        emit(new Instruction(InstructionCodes.ret, funcRetCPEntryIndex));
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        Expression conditionExpr = whileStmt.getCondition();
        Instruction gotoInstruction = new Instruction(InstructionCodes.goto_, nextIP());

        conditionExpr.accept(this);
        int opcode = getIfOpcode(conditionExpr);
        Instruction ifInstruction = new Instruction(opcode, intRegIndex, 0);
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
                basicLiteral.setTempOffset(++longRegIndex);
                long intVal = basicLiteral.getBValue().intValue();
                if (intVal >= 0 && intVal <= 5) {
                    opcode = InstructionCodes.iconst_0 + (int) intVal;
                    emit(InstructionFactory.get(opcode, basicLiteral.getTempOffset()));
                } else {
                    IntegerCPEntry intCPEntry = new IntegerCPEntry(basicLiteral.getBValue().intValue());
                    int intCPEntryIndex = programFile.addCPEntry(intCPEntry);
                    emit(InstructionFactory.get(InstructionCodes.iconst, intCPEntryIndex,
                            basicLiteral.getTempOffset()));
                }
                break;

            case TypeTags.FLOAT_TAG:
                basicLiteral.setTempOffset(++doubleRegIndex);
                double floatVal = basicLiteral.getBValue().floatValue();
                if (floatVal == 0 || floatVal == 1 || floatVal == 2 ||
                        floatVal == 3 || floatVal == 4 || floatVal == 5) {
                    opcode = InstructionCodes.fconst_0 + (int) floatVal;
                    emit(InstructionFactory.get(opcode, basicLiteral.getTempOffset()));
                } else {
                    FloatCPEntry floatCPEntry = new FloatCPEntry(basicLiteral.getBValue().floatValue());
                    int floatCPEntryIndex = programFile.addCPEntry(floatCPEntry);
                    emit(InstructionFactory.get(InstructionCodes.fconst, floatCPEntryIndex,
                            basicLiteral.getTempOffset()));
                }
                break;

            case TypeTags.STRING_TAG:
                basicLiteral.setTempOffset(++stringRegIndex);
                String strValue = basicLiteral.getBValue().stringValue();
                UTF8CPEntry utf8CPEntry = new UTF8CPEntry(strValue);
                int stringValCPIndex = programFile.addCPEntry(utf8CPEntry);

                StringCPEntry stringCPEntry = new StringCPEntry(stringValCPIndex, strValue);
                int strCPEntryIndex = programFile.addCPEntry(stringCPEntry);

                emit(InstructionFactory.get(InstructionCodes.sconst, strCPEntryIndex, basicLiteral.getTempOffset()));
                break;

            case TypeTags.BOOLEAN_TAG:
                basicLiteral.setTempOffset(++intRegIndex);
                boolean booleanVal = basicLiteral.getBValue().booleanValue();
                if (!booleanVal) {
                    opcode = InstructionCodes.bconst_0;
                } else {
                    opcode = InstructionCodes.bconst_1;
                }
                emit(InstructionFactory.get(opcode, basicLiteral.getTempOffset()));
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
            switch (retType.getSig()) {
                case TypeConstants.INT_TSIG:
                    retRegs[i] = ++longRegIndex;
                    break;
                case TypeConstants.FLOAT_TSIG:
                    retRegs[i] = ++doubleRegIndex;
                    break;
                case TypeConstants.STRING_TSIG:
                    retRegs[i] = ++stringRegIndex;
                    break;
                case TypeConstants.BOOLEAN_TSIG:
                    retRegs[i] = ++intRegIndex;
                    break;
                default:
                    retRegs[i] = ++bValueRegIndex;
                    break;
            }
        }

        FunctionCallCPEntry funcCallCPEntry = new FunctionCallCPEntry(argRegs, retRegs);
        int funcCallIndex = programFile.addCPEntry(funcCallCPEntry);

        emit(new Instruction(InstructionCodes.call, funcRefCPIndex, funcCallIndex));
        funcIExpr.setOffsets(retRegs);
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

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {

    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {

    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {

    }

    @Override
    public void visit(StructInitExpr structInitExpr) {

    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {

    }

    @Override
    public void visit(MapStructInitKeyValueExpr keyValueExpr) {

    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {

    }

    @Override
    public void visit(StructFieldAccessExpr structAttributeAccessExpr) {

    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        if (variableRefExpr.isLHSExpr()) {
            return;
        }

        VariableDef variableDef = variableRefExpr.getVariableDef();

        // TODO Assumption  need to consider other memory locations
        StackVarLocation stackVarLocation = (StackVarLocation) variableDef.getMemoryLocation();

        if (variableRefExpr.getType() == BTypes.typeInt) {
            variableRefExpr.setTempOffset(++longRegIndex);
        }

        emit(new Instruction(InstructionCodes.iload, stackVarLocation.getStackFrameOffset(),
                variableRefExpr.getTempOffset()));
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

    private void endCallableUnit() {
        intLVIndex = -1;
        longLVIndex = -1;
        doubleLVIndex = -1;
        stringLVIndex = -1;
        bValueLVIndex = -1;

        intRegIndex = -1;
        longRegIndex = -1;
        doubleRegIndex = -1;
        stringRegIndex = -1;
        bValueRegIndex = -1;

        intRetIndex = -1;
        longRetIndex = -1;
        doubleRetIndex = -1;
        stringRetIndex = -1;
        bValueRetIndex = -1;
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

        int opcode = -1;
        int exprOffset = -1;
        int typeTag = expr.getType().getTag();
        switch (typeTag) {
            case TypeTags.INT_TAG:
                opcode = baseOpcode;
                exprOffset = ++longRegIndex;
                break;
            case TypeTags.FLOAT_TAG:
                opcode = baseOpcode + 1;
                exprOffset = ++doubleRegIndex;
                break;
            case TypeTags.STRING_TAG:
                opcode = baseOpcode + 2;
                exprOffset = ++stringRegIndex;
                break;
        }

        expr.setTempOffset(exprOffset);
        emit(InstructionFactory.get(opcode, expr.getLExpr().getTempOffset(),
                expr.getRExpr().getTempOffset(), exprOffset));
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
                exprOffset = ++intRegIndex;
                break;
            case TypeTags.FLOAT_TAG:
                opcode = baseOpcode + 1;
                exprOffset = ++intRegIndex;
                break;
            case TypeTags.STRING_TAG:
                opcode = baseOpcode + 2;
                exprOffset = ++intRegIndex;
                break;
            case TypeTags.BOOLEAN_TAG:
                opcode = baseOpcode + 3;
                exprOffset = ++intRegIndex;
                break;
            // TODO Handle NULL type
        }

        expr.setTempOffset(exprOffset);
        emit(InstructionFactory.get(opcode, expr.getLExpr().getTempOffset(),
                expr.getRExpr().getTempOffset(), exprOffset));
    }

    private int emit(Instruction instruction) {
        return programFile.addInstruction(instruction);
    }
}

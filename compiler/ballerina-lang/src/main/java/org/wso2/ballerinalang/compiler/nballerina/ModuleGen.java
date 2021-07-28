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
package org.wso2.ballerinalang.compiler.nballerina;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.FiniteType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.BmpStringValue;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;


/**
 * Transform the AST to nBallerina Module.
 *
 * @since 0.980.0
 */
public class ModuleGen {

    private static final CompilerContext.Key<ModuleGen> MOD_GEN =
            new CompilerContext.Key<>();

    static final Module MODFRONT = new Module("wso2", "nballerina.front", "0.1.0");
    static final Module MODBIR = new Module("wso2", "nballerina.bir", "0.1.0");
    static final Module MODTYPES = new Module("wso2", "nballerina.types", "0.1.0");
    static final Module MODERROR = new Module("wso2", "nballerina.err", "0.1.0");

    private static JNModule jnmod;

    PrintStream console = System.out;

    public static ModuleGen getInstance(CompilerContext context) {
        ModuleGen modGen = context.get(MOD_GEN);
        if (modGen == null) {
            modGen = new ModuleGen(context);
        }

        return modGen;
    }

    private ModuleGen(CompilerContext context) {
        context.put(MOD_GEN, this);
    }

    public JNModule genMod(BLangPackage astPkg) {
        jnmod = new JNModule();
        jnmod.moduleId.organization = "wso2";
        jnmod.moduleId.names.add("baltest");

        for (BLangFunction func : astPkg.functions) {
            boolean acc = func.getFlags().contains(Flag.PUBLIC);
            String name = func.getName().toString();
            TypeKind ret = func.returnTypeNode.getBType().getKind();
            Position position = new Position(func.getPosition().lineRange().startLine().line() + 1,
                    func.getPosition().textRange().startOffset() + 9);
            FunctionDefn funcDefn = new FunctionDefn(acc, name, ret, position);
            func.getParameters().forEach(param -> {
                funcDefn.signature.paramTypes.add(param.getBType().getKind());
            });
            jnmod.functionDefns.put(name, funcDefn);
        }
        for (BLangFunction func : astPkg.functions) {
            FunctionCode fcode = new FunctionCode();
            BasicBlock curBlock = fcode.createBasicBlock();
            func.getParameters().forEach(param -> {
                fcode.createRegister(param.getBType().getKind(), param.getName().toString());
            });
            for (BLangStatement stmt : ((BLangBlockFunctionBody) func.body).getStatements()) {
                curBlock = codeGenStmt(stmt, fcode, curBlock);
            }
            if (curBlock != null) {
                curBlock.insns.add(new NullRetInsn());
            }
            createPanicBlock(fcode);
            jnmod.code.add(fcode);
        }
        return jnmod;
        //return astPkg.accept(this);
    }

    BasicBlock codeGenStmt(BLangStatement stmt, FunctionCode code, BasicBlock startBlock) {
        if (stmt instanceof BLangReturn) {
            BLangReturn retStmt = (BLangReturn) stmt;
            OpBlockHolder opb = codeGenExpr(retStmt.expr, code, startBlock);
            opb.nextBlock.insns.add(new RetInsn(opb.operand));
            return null;

        } else if (stmt instanceof BLangSimpleVariableDef) {
            BLangSimpleVariableDef varDec = (BLangSimpleVariableDef) stmt;
            OpBlockHolder opb = codeGenExpr(varDec.var.getInitialExpression(), code, startBlock);
            Register result = code.createRegister(varDec.getVariable().typeNode.getBType().getKind(),
                    varDec.getVariable().getName().toString());
            opb.nextBlock.insns.add(new AssignInsn(result, opb.operand));
            return opb.nextBlock;

        } else if (stmt instanceof BLangExpressionStmt) {
            return codeGenExpr(((BLangExpressionStmt) stmt).getExpression(), code, startBlock).nextBlock;
        } else if (stmt instanceof BLangAssignment) {
            BLangAssignment assign = (BLangAssignment) stmt;
            if (assign.varRef instanceof BLangSimpleVarRef) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) assign.varRef;
                Register reg = code.registers.get(varRef.variableName.getValue());
                OpBlockHolder opb = codeGenExpr(assign.expr, code, startBlock);
                opb.nextBlock.insns.add(new AssignInsn(reg, opb.operand));
                return opb.nextBlock;
            }
        } else if (stmt instanceof BLangIf) {
            BLangIf ifStmt = (BLangIf) stmt;
            OpBlockHolder branch = codeGenExpr(ifStmt.getCondition(), code, startBlock);
            BasicBlock ifBlock = code.createBasicBlock();
            BasicBlock ifContBlock, contBlock;
            BasicBlock curBlock = ifBlock;
            for (BLangStatement st : ifStmt.getBody().getStatements()) {
                curBlock = codeGenStmt(st, code, curBlock);
            }
            ifContBlock = curBlock;
            if (ifStmt.elseStmt == null) {
                contBlock = code.createBasicBlock();
                branch.nextBlock.insns.add(new CondBranchInsn(branch.operand.register, ifBlock.label, contBlock.label));
                if (ifContBlock != null) {
                    ifContBlock.insns.add(new BranchInsn(contBlock.label));
                }
                return contBlock;
            }
        } else if (stmt instanceof BLangWhile) {
            BLangWhile whileStmt = (BLangWhile) stmt;
            BasicBlock loopHead = code.createBasicBlock();
            BasicBlock exit = code.createBasicBlock();
            BranchInsn branchLoopHead = new BranchInsn(loopHead.label);
            startBlock.insns.add(branchLoopHead);
            OpBlockHolder conditionOpb = codeGenExpr(whileStmt.expr, code, loopHead);
            LoopContext lc = new LoopContext();
            lc.onBreak = exit;
            lc.onContinue = loopHead;
            lc.enclosing = code.loopContext;
            code.loopContext = lc;
            boolean exitReachable;
            if (conditionOpb.operand.isReg) {
                BasicBlock afterCondition = code.createBasicBlock();
                conditionOpb.nextBlock.insns.add(new CondBranchInsn(conditionOpb.operand.register,
                        exit.label, afterCondition.label));
                conditionOpb.nextBlock = afterCondition;
                exitReachable = true;
            } else {
                exitReachable = false;
            }
            BasicBlock curBlock = conditionOpb.nextBlock;
            for (BLangStatement st : whileStmt.body.getStatements()) {
                curBlock = codeGenStmt(st, code, curBlock);
            }
            BasicBlock loopEnd = curBlock;
            if (code.loopContext.breakUsed) {
                exitReachable = true;
            }
            code.loopContext = code.loopContext.enclosing;
            if (loopEnd != null) {
                loopEnd.insns.add(branchLoopHead);
            }
            return exitReachable ? exit : null;
        }

        throw new BallerinaException("Statement not recognized");
    }

    OpBlockHolder codeGenExpr(BLangExpression expr, FunctionCode code, BasicBlock bb) {
        if (expr instanceof BLangLiteral) {
            Operand op = new Operand(false);
            op.value = ((BLangLiteral) expr).getValue();
            return new OpBlockHolder(op, bb);
        } else if (expr instanceof BLangSimpleVarRef) {
            String name = ((BLangSimpleVarRef) expr).variableName.getValue();
            Operand op = new Operand(true);
            if (!code.registers.containsKey(name)) {
                throw new BallerinaException("variable '" + name + "' not found");
            }
            op.register = code.registers.get(name);
            return new OpBlockHolder(op, bb);
        } else if (expr instanceof BLangUnaryExpr) {
            BLangUnaryExpr unexpr = (BLangUnaryExpr) expr;
            OpBlockHolder opb = codeGenExpr(unexpr.expr, code, bb);
            Operand result = new Operand(true);
            OperatorKind op = unexpr.operator;
            switch (op) {
                case NOT:
                    Register reg1 = code.createRegister(TypeKind.BOOLEAN, null);
                    bb.insns.add(new BoolNotInsn(opb.operand.register, reg1));
                    result.register = reg1;
                    return new OpBlockHolder(result, opb.nextBlock);
                case SUB:
                    Register reg2 = code.createRegister(TypeKind.INT, null);
                    IntArithmeticBinaryInsn ins = new IntArithmeticBinaryInsn();
                    bb.ppb = true;
                    ins.op = "-";
                    ins.result = reg2;
                    Operand zeroOp = new Operand(false);
                    zeroOp.value = 0;
                    ins.operands[0] = zeroOp;
                    ins.operands[1] = opb.operand;
                    ins.position = new Position(unexpr.opSymbol.getPosition().lineRange().startLine().line() + 1,
                            unexpr.opSymbol.getPosition().textRange().startOffset());
                    bb.insns.add(ins);
                    result.register = reg2;
                    return new OpBlockHolder(result, opb.nextBlock);
            }
        } else if (expr instanceof BLangBinaryExpr) {
            BLangBinaryExpr bexpr = (BLangBinaryExpr) expr;
            OpBlockHolder lhs = codeGenExpr(bexpr.lhsExpr, code, bb);
            OpBlockHolder rhs = codeGenExpr(bexpr.rhsExpr, code, lhs.nextBlock);
            Operand result = new Operand(true);
            OperatorKind op = bexpr.opKind;
            switch (op) {
                case ADD: case SUB: case MUL: case DIV: case MOD:
                    Register reg = code.createRegister(TypeKind.INT, null);
                    IntArithmeticBinaryInsn ins = new IntArithmeticBinaryInsn();
                    bb.ppb = true;
                    ins.op = op.toString();
                    ins.result = reg;
                    ins.operands[0] = lhs.operand;
                    ins.operands[1] = rhs.operand;
                    ins.position = new Position(bexpr.getPosition().lineRange().startLine().line() + 1,
                            bexpr.getPosition().textRange().startOffset());
                    bb.insns.add(ins);
                    result.register = reg;
                    return new OpBlockHolder(result, rhs.nextBlock);
            }
        } else if (expr instanceof BLangInvocation) {
            BLangInvocation funcCall = (BLangInvocation) expr;
            return codegenFunctionCall(funcCall, code, bb);
        } else if (expr instanceof BLangListConstructorExpr) {
            BLangListConstructorExpr listExpr = (BLangListConstructorExpr) expr;
            ArrayList<Operand> operands = new ArrayList<>();
            BasicBlock nextBlock = bb;
            for (BLangExpression ex : listExpr.exprs) {
                OpBlockHolder opb = codeGenExpr(ex, code, nextBlock);
                operands.add(opb.operand);
                nextBlock = opb.nextBlock;
            }
            Operand result = new Operand(true);
            Register reg = code.createRegister(TypeKind.ARRAY, null);
            result.register = reg;
            ListConstructInsn ins = new ListConstructInsn(reg, operands);
            nextBlock.insns.add(ins);
            return new OpBlockHolder(result, nextBlock);
        } else if (expr instanceof BLangGroupExpr) {
            return codeGenExpr(((BLangGroupExpr) expr).expression, code, bb);
        } else if (expr instanceof BLangTypeConversionExpr) {
            BLangTypeConversionExpr tcExpr = (BLangTypeConversionExpr) expr;
            OpBlockHolder opb = codeGenExpr(tcExpr.expr, code, bb);
            if (opb.operand.isReg) {
                Register reg = code.createRegister(null, null);
                Position pos = new Position(tcExpr.pos.lineRange().startLine().line() + 1,
                        tcExpr.pos.textRange().startOffset());
                bb.insns.add(new TypeCastInsn(reg, opb.operand.register, tcExpr.targetType.getKind(), pos));
                Operand regOp = new Operand(true);
                regOp.register = reg;
                return new OpBlockHolder(regOp, opb.nextBlock);
            } else {
                return opb;
            }
        }

        throw new BallerinaException("Expression not recognized");
    }

    OpBlockHolder codegenFunctionCall(BLangInvocation funcCall, FunctionCode code, BasicBlock bb) {
        FunctionRef ref;
        Operand result = new Operand(true);
        if (funcCall.pkgAlias.getValue().equals("")) {
            FunctionDefn def = jnmod.functionDefns.get(funcCall.getName().getValue());
            ref  = new FunctionRef(def.symbol, def.signature);
        } else {
            FunctionSignature signature = new FunctionSignature();
            signature.returnType = funcCall.getBType().getKind();
            for (BLangExpression arg: funcCall.requiredArgs) {
                signature.paramTypes.add(arg.getBType().getKind());
            }
            if (!funcCall.restArgs.isEmpty()) {
                //signature.restParamType = funcCall.restArgs.get(0).getBType().getKind();
                signature.paramTypes.add(TypeKind.OTHER);
            }
            ModuleId mod = new ModuleId();
            mod.organization = funcCall.symbol.pkgID.orgName.getValue();
            mod.names.add(funcCall.symbol.pkgID.name.getValue());
            ExternalSymbol symbol = new ExternalSymbol(mod, funcCall.getName().getValue());
            ref = new FunctionRef(symbol, signature);
        }
        BasicBlock curBlock = bb;
        ArrayList<Operand> args = new ArrayList<>();

        for (BLangExpression arg : funcCall.argExprs) {
            OpBlockHolder opb = codeGenExpr(arg, code, curBlock);
            curBlock = opb.nextBlock;
            args.add(opb.operand);
        }
        Register reg = code.createRegister(ref.signature.returnType , null);
        CallInsn callInsn = new CallInsn(reg, ref, args);
        curBlock.insns.add(callInsn);
        result.register = reg;
        return new OpBlockHolder(result, curBlock);
    }

    void createPanicBlock(FunctionCode code) {
        boolean panic = false;
        int onPanicLabel = code.blocks.size();
        for (BasicBlock block : code.blocks) {
            if (block.ppb) {
                panic = true;
                block.onPanic = onPanicLabel;
            }
        }
        if (panic) {
            BasicBlock onPanicBlock = code.createBasicBlock();
            Register reg = code.createRegister(TypeKind.ERROR, null);
            CatchInsn catchInsn = new CatchInsn(reg);
            AbnormalRetInsn abnRetInsn = new AbnormalRetInsn(reg);
            onPanicBlock.insns.add(catchInsn);
            onPanicBlock.insns.add(abnRetInsn);
        }
    }

    static long convertSimpleSemType(TypeKind typeKind) {
        switch (typeKind) {
            case INT:
                return 128L;
            case BOOLEAN:
                return 2L;
            case NIL:
                return 1L;
            case ARRAY:
                return 262144L;
            case ERROR:
                return 2048L;
            case OTHER:
                return 8388607L;
            case ANY:
                return 8386559L;
            default:
                throw new BallerinaException("Semtype not implemented for type");
        }

    }
}

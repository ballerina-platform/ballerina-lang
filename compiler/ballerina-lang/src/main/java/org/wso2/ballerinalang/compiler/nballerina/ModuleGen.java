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
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;


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

    private JNModule jnmod;

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
            FunctionDefn funcDefn = new FunctionDefn(acc, name, convertSimpleSemType(ret), position);
            func.getParameters().forEach(param -> {
                funcDefn.signature.paramTypes.add(convertSimpleSemType(param.getBType().getKind()));
            });
            jnmod.functionDefns.put(name, funcDefn);
        }
        for (BLangFunction func : astPkg.functions) {
            FunctionCode fcode = new FunctionCode();
            BasicBlock curBlock = fcode.createBasicBlock();
            func.getParameters().forEach(param -> {
                fcode.createRegister(convertSimpleSemType(param.getBType().getKind()), param.getName().toString());
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
            Register result = code.createRegister(convertSimpleSemType(
                    varDec.getVariable().typeNode.getBType().getKind()), varDec.getVariable().getName().toString());
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
            } else {
                BLangIndexBasedAccess varRef = (BLangIndexBasedAccess) assign.varRef;
                BLangSimpleVarRef rec = (BLangSimpleVarRef) varRef.getExpression();
                Register reg = code.registers.get(rec.variableName.getValue());
                OpBlockHolder opbIndex = codeGenExpr(varRef.getIndex(), code, startBlock);
                OpBlockHolder opb = codeGenExpr(assign.expr, code, opbIndex.nextBlock);
                Position pos = new Position(varRef.pos.lineRange().startLine().line(),
                        varRef.pos.textRange().startOffset());
                if (varRef.getIndex().expectedType.getKind() == TypeKind.INT) {
                    ListSetInsn lsinsn = new ListSetInsn(reg, opbIndex.operand, opb.operand, pos);
                    opb.nextBlock.insns.add(lsinsn);
                } else if (varRef.getIndex().expectedType.getKind() == TypeKind.INT) {
                    MapSetInsn msinsn = new MapSetInsn(pos);
                    Operand regop = new Operand(true);
                    regop.register = reg;
                    msinsn.operands[0] = opbIndex.operand;
                    msinsn.operands[1] = opb.operand;
                    opb.nextBlock.insns.add(msinsn);
                } else {
                   throw new BallerinaException("key in assignment to member must be int or string");
                }

                return opb.nextBlock;
            }
        } else if (stmt instanceof BLangForeach) {
            BLangForeach feStmt = (BLangForeach) stmt;
            String varName = ((BLangSimpleVariableDef) feStmt.variableDefinitionNode).var.name.toString();
            BLangBinaryExpr range = (BLangBinaryExpr) feStmt.collection;
            OpBlockHolder lowerOpb = codeGenExpr(range.lhsExpr, code, startBlock);
            OpBlockHolder upperOpb = codeGenExpr(range.rhsExpr, code, lowerOpb.nextBlock);
            Register loopVar = code.createRegister(convertSimpleSemType(TypeKind.INT), varName);
            upperOpb.nextBlock.insns.add(new AssignInsn(loopVar, lowerOpb.operand));
            BasicBlock loopHead =  code.createBasicBlock();
            BasicBlock exit = code.createBasicBlock();
            BranchInsn branchToLoopHead = new BranchInsn(loopHead.label);
            upperOpb.nextBlock.insns.add(branchToLoopHead);
            Register condition = code.createRegister(convertSimpleSemType(TypeKind.BOOLEAN), null);
            CompareInsn compare = new CompareInsn("<", "int", condition);
            Operand op = new Operand(true);
            op.register = loopVar;
            compare.operands[0] = op;
            compare.operands[1] = upperOpb.operand;
            loopHead.insns.add(compare);
            BasicBlock afterCondition = code.createBasicBlock();
            loopHead.insns.add(new CondBranchInsn(condition, afterCondition.label, exit.label));
            LoopContext lc = new LoopContext();
            lc.onBreak = exit;
            lc.onContinue = null;
            lc.enclosing = code.loopContext;
            code.loopContext = lc;
            BasicBlock curBlock = afterCondition;
            for (BLangStatement st : feStmt.body.getStatements()) {
                curBlock = codeGenStmt(st, code, curBlock);
            }
            BasicBlock loopBody = curBlock;
            BasicBlock loopStep = code.loopContext.onContinue;

            if (loopBody != null) {
                if (loopStep == null) {
                    loopStep = code.createBasicBlock();
                }
                loopBody.insns.add(new BranchInsn(loopStep.label));
            }
            if (loopStep != null) {
                IntNoPanicArithmeticBinaryInsn increment = new IntNoPanicArithmeticBinaryInsn();
                increment.op = "+";
                Operand op0 = new Operand(true);
                Operand op1 = new Operand(false);
                op0.register = loopVar;
                op1.value = 1;
                increment.operands[0] = op0;
                increment.operands[1] = op1;
                increment.result = loopVar;
                loopStep.insns.add(increment);
                loopStep.insns.add(branchToLoopHead);
            }
            code.loopContext = code.loopContext.enclosing;
            return exit;

        } else if (stmt instanceof BLangIf) {
            BLangIf ifStmt = (BLangIf) stmt;
            OpBlockHolder branchOpb = codeGenExpr(ifStmt.getCondition(), code, startBlock);
            // TODO Fix if boolean literal
            BasicBlock ifBlock = code.createBasicBlock();
            BasicBlock ifContBlock, contBlock;
            BasicBlock curBlock = ifBlock;
            for (BLangStatement st : ifStmt.getBody().getStatements()) {
                curBlock = codeGenStmt(st, code, curBlock);
            }
            ifContBlock = curBlock;
            if (ifStmt.elseStmt == null) {
                contBlock = code.createBasicBlock();
                branchOpb.nextBlock.insns.add(new CondBranchInsn(branchOpb.operand.register, ifBlock.label,
                        contBlock.label));
                if (ifContBlock != null) {
                    ifContBlock.insns.add(new BranchInsn(contBlock.label));
                }
            } else {
                BasicBlock elseBlock = code.createBasicBlock();
                BasicBlock elseContBlock;
                curBlock = elseBlock;
                if (ifStmt.getElseStatement() instanceof BLangBlockStmt) {
                    for (BLangStatement st : ((BLangBlockStmt) ifStmt.getElseStatement()).getStatements()) {
                        curBlock = codeGenStmt(st, code, curBlock);
                    }
                } else {
                    curBlock = codeGenStmt(ifStmt.getElseStatement(), code, curBlock);
                }
                elseContBlock = curBlock;
                branchOpb.nextBlock.insns.add(new CondBranchInsn(branchOpb.operand.register, ifBlock.label,
                        elseBlock.label));
                if (ifContBlock == null && elseContBlock == null) {
                    return null;
                }
                contBlock = code.createBasicBlock();
                BranchInsn branch = new BranchInsn(contBlock.label);
                if (ifContBlock != null) {
                    ifContBlock.insns.add(branch);
                }
                if (elseContBlock != null) {
                    elseContBlock.insns.add(branch);
                }
            }
            return contBlock;
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
                        afterCondition.label, exit.label));
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
        } else if (stmt instanceof BLangBreak) {
            code.loopContext.breakUsed = true;
            startBlock.insns.add(new BranchInsn(code.loopContext.onBreak.label));
            return null;
        } else if (stmt instanceof BLangContinue) {
            BasicBlock b = code.loopContext.onContinue;
            if (b == null) {
                b = code.createBasicBlock();
            }
            code.loopContext.onContinue = b;
            startBlock.insns.add(new BranchInsn(code.loopContext.onContinue.label));
            return null;
        }

        throw new BallerinaException("Statement not recognized");
    }

    OpBlockHolder codeGenExpr(BLangExpression expr, FunctionCode code, BasicBlock bb) {
        if (expr instanceof BLangLiteral) {
            Operand op = new Operand(false);
            op.value = ((BLangLiteral) expr).getValue();
            if (expr.expectedType instanceof BNilType) {
                op.value = null;
            }
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
                    Register reg1 = code.createRegister(convertSimpleSemType(TypeKind.BOOLEAN), null);
                    if (opb.operand.isReg) {
                        bb.insns.add(new BoolNotInsn(opb.operand.register, reg1));

                    } else {
                        opb.operand.value = !(boolean) opb.operand.value;
                        bb.insns.add(new AssignInsn(reg1, opb.operand));
                    }
                    result.register = reg1;
                    return new OpBlockHolder(result, opb.nextBlock);
                case SUB:
                    Register reg2 = code.createRegister(convertSimpleSemType(TypeKind.INT), null);
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
            TypeKind pair = typedOpPair(bexpr.lhsExpr.expectedType.getKind(), bexpr.rhsExpr.expectedType.getKind());
            OperatorKind op = bexpr.opKind;
            switch (op) {
                case ADD: case SUB: case MUL: case DIV: case MOD:
                    Register reg = code.createRegister(convertSimpleSemType(pair), null);
                    if (pair == TypeKind.INT) {
                        IntArithmeticBinaryInsn ins = new IntArithmeticBinaryInsn();
                        bb.ppb = true;
                        ins.op = op.toString();
                        ins.position = new Position(bexpr.getPosition().lineRange().startLine().line() + 1,
                                bexpr.getPosition().textRange().startOffset());
                        ins.result = reg;
                        ins.operands[0] = lhs.operand;
                        ins.operands[1] = rhs.operand;
                        bb.insns.add(ins);
                    } else if (pair == TypeKind.STRING) {
                        StringConcatInsn ins = new StringConcatInsn();
                        ins.result = reg;
                        ins.operands[0] = lhs.operand;
                        ins.operands[1] = rhs.operand;
                        bb.insns.add(ins);
                    } else {
                        throw new BallerinaException("+ not supported for operand types");
                    }

                    result.register = reg;
                    return new OpBlockHolder(result, rhs.nextBlock);
                case EQUAL: case NOT_EQUAL: case REF_EQUAL: case REF_NOT_EQUAL:
                    result.register = code.createRegister(convertSimpleSemType(TypeKind.BOOLEAN), null);
                    EqualityInsn eqi = new EqualityInsn(op.toString(), result.register);
                    eqi.operands[0] = lhs.operand;
                    eqi.operands[1] = rhs.operand;
                    bb.insns.add(eqi);
                    return new OpBlockHolder(result, rhs.nextBlock);
                case GREATER_THAN: case GREATER_EQUAL: case LESS_THAN: case LESS_EQUAL:
                    result.register = code.createRegister(convertSimpleSemType(TypeKind.BOOLEAN), null);
                    CompareInsn cmpi = new CompareInsn(op.toString(), pair.typeName(), result.register);
                    cmpi.operands[0] = lhs.operand;
                    cmpi.operands[1] = rhs.operand;
                    bb.insns.add(cmpi);
                    return new OpBlockHolder(result, rhs.nextBlock);
                case BITWISE_AND: case BITWISE_OR: case BITWISE_XOR: case BITWISE_LEFT_SHIFT:
                case BITWISE_RIGHT_SHIFT: case BITWISE_UNSIGNED_RIGHT_SHIFT:
                    if (pair != TypeKind.INT) {
                        throw new BallerinaException("expected integer operand");
                    }
                    result.register = code.createRegister(convertSimpleSemType(TypeKind.INT), null);
                    IntBitwiseInsn bwi = new IntBitwiseInsn(op.toString(), result.register);
                    bwi.operands[0] = lhs.operand;
                    bwi.operands[1] = rhs.operand;
                    bb.insns.add(bwi);
                    return new OpBlockHolder(result, rhs.nextBlock);
                default:
                    throw new BallerinaException("Operator not recognized");
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
            Register reg = code.createRegister(262144L, null);
            result.register = reg;
            ListConstructInsn ins = new ListConstructInsn(reg, operands);
            nextBlock.insns.add(ins);
            return new OpBlockHolder(result, nextBlock);
        } else if (expr instanceof BLangRecordLiteral) {
            BLangRecordLiteral mapExpr = (BLangRecordLiteral) expr;
            BasicBlock nextBlock = bb;
            ArrayList<Operand> operands = new ArrayList<>();
            ArrayList<String> fieldNames = new ArrayList<>();
            for (RecordLiteralNode.RecordField field: mapExpr.fields) {
                OpBlockHolder opb = codeGenExpr(((BLangRecordLiteral.BLangRecordKeyValueField) field).valueExpr, code,
                        nextBlock);
                nextBlock = opb.nextBlock;
                operands.add(opb.operand);
                fieldNames.add(((BLangRecordLiteral.BLangRecordKeyValueField) field).key.toString());
            }
            Register result = code.createRegister(524288L, null);
            nextBlock.insns.add(new MapConstructInsn(result, operands, fieldNames));
            Operand retOp = new Operand(true);
            retOp.register = result;
            return new OpBlockHolder(retOp, nextBlock);
        } else if (expr instanceof BLangGroupExpr) {
            return codeGenExpr(((BLangGroupExpr) expr).expression, code, bb);
        } else if (expr instanceof BLangTypeConversionExpr) {
            BLangTypeConversionExpr tcExpr = (BLangTypeConversionExpr) expr;
            OpBlockHolder opb = codeGenExpr(tcExpr.expr, code, bb);
            if (opb.operand.isReg) {
                Register reg = code.createRegister(convertSimpleSemType(tcExpr.targetType.getKind()), null);
                Position pos = new Position(tcExpr.pos.lineRange().startLine().line() + 1,
                        tcExpr.pos.textRange().startOffset());
                bb.ppb = true;
                bb.insns.add(new TypeCastInsn(reg, opb.operand.register,
                        convertSimpleSemType(tcExpr.targetType.getKind()), pos));
                Operand regOp = new Operand(true);
                regOp.register = reg;
                return new OpBlockHolder(regOp, opb.nextBlock);
            } else {
                return opb;
            }
        } else if (expr instanceof BLangIndexBasedAccess) {
            BLangIndexBasedAccess accExpr = (BLangIndexBasedAccess) expr;
            OpBlockHolder l = codeGenExpr(accExpr.getExpression(), code, bb);
            OpBlockHolder r = codeGenExpr(accExpr.getIndex(), code, l.nextBlock);
            if (l.operand.isReg) {
                Register result = code.createRegister(convertSimpleSemType(TypeKind.ANY), null);
                if (accExpr.getExpression().expectedType instanceof BArrayType) {
                    Position pos = new Position(accExpr.pos.lineRange().startLine().line(),
                            accExpr.pos.textRange().startOffset());
                    bb.ppb = true;
                    bb.insns.add(new ListGetInsn(result, l.operand.register, r.operand, pos));
                } else {
                    MapGetInsn mapInsn = new MapGetInsn(result);
                    mapInsn.operands[0] = l.operand;
                    mapInsn.operands[1] = r.operand;
                    bb.insns.add(mapInsn);
                }
                Operand resultOp = new Operand(true);
                resultOp.register = result;
                return new OpBlockHolder(resultOp , r.nextBlock);
            } else {
                throw new BallerinaException("cannot apply member access to constant of simple type");
            }
        }

        throw new BallerinaException("Expression not recognized");
    }

    OpBlockHolder codegenFunctionCall(BLangInvocation funcCall, FunctionCode code, BasicBlock bb) {
        FunctionRef ref;
        Operand result = new Operand(true);
        if (funcCall.langLibInvocation) {
            FunctionSignature signature = new FunctionSignature();
            signature.returnType = convertSimpleSemType(funcCall.getBType().getKind());
            for (BLangExpression arg: funcCall.requiredArgs) {
                signature.paramTypes.add(convertSimpleSemType(arg.getBType().getKind()));
            }
            if (!funcCall.restArgs.isEmpty()) {
                //signature.restParamType = funcCall.restArgs.get(0).getBType().getKind();
                signature.paramTypes.add(8388607L);
            }
            ModuleId mod = new ModuleId();
            mod.organization = funcCall.symbol.pkgID.orgName.getValue();
            String[] names = funcCall.symbol.pkgID.name.getValue().split("\\.");
            mod.names.addAll(Arrays.asList(names));
            ExternalSymbol symbol = new ExternalSymbol(mod, funcCall.getName().getValue());
            ref = new FunctionRef(symbol, signature);
        } else if (funcCall.pkgAlias.getValue().equals("")) {
            FunctionDefn def = jnmod.functionDefns.get(funcCall.getName().getValue());
            ref  = new FunctionRef(def.symbol, def.signature);
        } else {
            FunctionSignature signature = new FunctionSignature();
            signature.returnType = convertSimpleSemType(funcCall.getBType().getKind());
            for (BLangExpression arg: funcCall.requiredArgs) {
                signature.paramTypes.add(convertSimpleSemType(arg.getBType().getKind()));
            }
            if (!funcCall.restArgs.isEmpty()) {
                //signature.restParamType = funcCall.restArgs.get(0).getBType().getKind();
                signature.paramTypes.add(8388607L);
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
            Register reg = code.createRegister(convertSimpleSemType(TypeKind.ERROR), null);
            CatchInsn catchInsn = new CatchInsn(reg);
            AbnormalRetInsn abnRetInsn = new AbnormalRetInsn(reg);
            onPanicBlock.insns.add(catchInsn);
            onPanicBlock.insns.add(abnRetInsn);
        }
    }

    long convertSimpleSemType(TypeKind typeKind) {
        switch (typeKind) {
            case INT:
                return 128L;
            case BOOLEAN:
                return 2L;
            case NIL:
                return 1L;
            case ARRAY:
                return 262148L;
            case ERROR:
                return 2048L;
            case ANY:
                return 8386559L;
            case STRING:
                return 1024L;
            case MAP:
                return 524296L;
            default:
                throw new BallerinaException("Semtype not implemented for type");
        }

    }

    TypeKind typedOpPair(TypeKind t1, TypeKind t2) {  // temporarily get from expected type
        if (t1 == t2) {
            return t1;
        } else if (t1 == TypeKind.ANY) {
            return t2;
        } else if (t2 == TypeKind.ANY) {
            return t1;
        } else {
            throw new BallerinaException("Operands have incompatible types");
        }
    }

}

/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Optimize BIR.
 *
 * @since 1.0
 */
public class BIROptimizer {

    private static final CompilerContext.Key<BIROptimizer> BIR_OPTIMIZER = new CompilerContext.Key<>();
    private RHSTempVarOptimizer rhsTempVarOptimizer;
    private LHSTempVarOptimizer lhsTempVarOptimizer;

    public static BIROptimizer getInstance(CompilerContext context) {
        BIROptimizer birGen = context.get(BIR_OPTIMIZER);
        if (birGen == null) {
            birGen = new BIROptimizer(context);
        }

        return birGen;
    }

    private BIROptimizer(CompilerContext context) {
        context.put(BIR_OPTIMIZER, this);
        this.rhsTempVarOptimizer = new RHSTempVarOptimizer();
        this.lhsTempVarOptimizer = new LHSTempVarOptimizer();
    }

    public void optimizePackage(BIRPackage pkg) {
        // RHS temp var optimization
        pkg.accept(this.rhsTempVarOptimizer);

        // LHS temp var optimization
        this.lhsTempVarOptimizer.optimizeNode(pkg, null);
    }

    /**
     * This class is to optimize away unwanted temporary variables in right hand side of statements.
     */
    public static class RHSTempVarOptimizer extends BIRVisitor {
        private Map<BIROperand, List<BIRAbstractInstruction>> tempVarUpdateInstructions = new HashMap<>();
        private Map<BIROperand, List<BIRErrorEntry>> errorEntries = new HashMap<>();
        private List<BIRVariableDcl> removedTempVars = new ArrayList<>();

        @Override
        public void visit(BIRPackage birPackage) {
            birPackage.typeDefs.forEach(tDef -> tDef.accept(this));
            birPackage.functions.forEach(func -> func.accept(this));
        }

        @Override
        public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {
            birTypeDefinition.attachedFuncs.forEach(func -> func.accept(this));
        }

        @Override
        public void visit(BIRFunction birFunction) {
            for (BIRErrorEntry errorEntry : birFunction.errorTable) {
                addErrorTableDependency(errorEntry);
            }

            // First add all the instructions within the function to a list.
            // This is done since the order of bb's cannot be guaranteed.
            birFunction.parameters.values().forEach(paramBBs -> addDependency(paramBBs));
            addDependency(birFunction.basicBlocks);

            // Then visit and replace any temp moves
            for (List<BIRBasicBlock> paramBBs : birFunction.parameters.values()) {
                paramBBs.forEach(bb -> bb.accept(this));
            }
            birFunction.basicBlocks.forEach(bb -> bb.accept(this));

            // Remove unused temp vars
            List<BIRVariableDcl> newLocalVars = new ArrayList<>();
            for (BIRVariableDcl var : birFunction.localVars) {
                if (var.kind != VarKind.TEMP) {
                    newLocalVars.add(var);
                    continue;
                }

                if (this.removedTempVars.contains(var)) {
                    // do not add
                    continue;
                }

                newLocalVars.add(var);
            }

            this.removedTempVars.clear();
            this.tempVarUpdateInstructions.clear();
            this.errorEntries.clear();
            birFunction.localVars = newLocalVars;
        }

        @Override
        public void visit(BIRBasicBlock birBasicBlock) {
            List<BIRNonTerminator> instructions = birBasicBlock.instructions;
            List<BIRNonTerminator> newInstructions = new ArrayList<>();

            for (BIRNonTerminator ins : instructions) {
                // if the current instruction is not a MOVE, don't do anything.
                if (ins.getKind() != InstructionKind.MOVE) {
                    newInstructions.add(ins);
                    continue;
                }

                // if the current MOVE is not from a TEMP, again, don't do anything.
                Move moveIns = ((Move) ins);
                if (moveIns.rhsOp.variableDcl.kind != VarKind.TEMP) {
                    newInstructions.add(ins);
                    continue;
                }

                replaceTempVar(moveIns);
            }

            birBasicBlock.instructions = newInstructions;
        }

        private void replaceTempVar(Move moveIns) {
            List<BIRAbstractInstruction> tempUpdateInsList = this.tempVarUpdateInstructions.get(moveIns.rhsOp);
            if (tempUpdateInsList != null) {
                this.removedTempVars.add(moveIns.rhsOp.variableDcl);
                for (BIRAbstractInstruction tempUpdateIns : tempUpdateInsList) {
                    // Here it is assumed that we replace only LHS.
                    tempUpdateIns.lhsOp = moveIns.lhsOp;
                    addDependency(tempUpdateIns);
                }
            }

            // Update error entry
            List<BIRErrorEntry> errorEntriesWithTemp = this.errorEntries.get(moveIns.rhsOp);
            if (errorEntriesWithTemp != null) {
                for (BIRErrorEntry errorEntryWithTemp : errorEntriesWithTemp) {
                    errorEntryWithTemp.errorOp = moveIns.lhsOp;
                    addErrorTableDependency(errorEntryWithTemp);
                }
            }
        }

        private void addErrorTableDependency(BIRErrorEntry errorEntryWithTemp) {
            List<BIRErrorEntry> errorTableEntries = this.errorEntries.get(errorEntryWithTemp.errorOp);
            if (errorTableEntries != null) {
                errorTableEntries.add(errorEntryWithTemp);
            } else {
                this.errorEntries.put(errorEntryWithTemp.errorOp, Lists.of(errorEntryWithTemp));
            }
        }

        private void addDependency(List<BIRBasicBlock> basicBlocks) {
            for (BIRBasicBlock bb : basicBlocks) {
                for (BIRInstruction ins : bb.instructions) {
                    addDependency((BIRAbstractInstruction) ins);
                }
                addDependency(bb.terminator);
            }
        }

        private void addDependency(BIRAbstractInstruction ins) {
            if (ins.lhsOp == null || ins.lhsOp.variableDcl.kind != VarKind.TEMP) {
                return;
            }

            List<BIRAbstractInstruction> tempVarUpdates = this.tempVarUpdateInstructions.get(ins.lhsOp);
            if (tempVarUpdates != null) {
                tempVarUpdates.add(ins);
            } else {
                this.tempVarUpdateInstructions.put(ins.lhsOp, Lists.of(ins));
            }
        }
    }

    /**
     * This class is to optimize away unwanted temp wars in left hand side of statements.
     */
    public static class LHSTempVarOptimizer extends BIRVisitor {
        private OptimizerEnv env;

        @Override
        public void visit(BIRPackage birPackage) {
            birPackage.typeDefs.forEach(tDef ->  this.optimizeNode(tDef, this.env));
            birPackage.functions.forEach(func -> this.optimizeNode(func, this.env));
        }

        public void optimizeNode(BIRNode node, OptimizerEnv env) {
            if (node == null) {
                return;
            }
            OptimizerEnv oldEnv = this.env;
            this.env = env;
            node.accept(this);
            this.env = oldEnv;
        }

        public void optimizeTerm(BIRTerminator term, OptimizerEnv env) {
            this.optimizeNode(term, env);
        }

        public void optimizeNonTerm(BIRNonTerminator nonTerm, OptimizerEnv env) {
            // TODO improve this
            if (nonTerm.kind != InstructionKind.MOVE) {
                this.env.newInstructions.add(nonTerm);
            }
            this.optimizeNode(nonTerm, env);
        }

        @Override
        public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {
            birTypeDefinition.attachedFuncs.forEach(func -> this.optimizeNode(func, this.env));
        }

        @Override
        public void visit(BIRFunction birFunction) {
            OptimizerEnv funcOpEnv = new OptimizerEnv();
            birFunction.basicBlocks.forEach(bb -> this.optimizeNode(bb, funcOpEnv));

            birFunction.errorTable.forEach(ee -> this.optimizeNode(ee, funcOpEnv));

            // Remove unused temp vars
            birFunction.localVars = birFunction.localVars.stream()
                    .filter(l -> l.kind != VarKind.TEMP || !funcOpEnv.tempVars.keySet()
                            .contains(l)).collect(Collectors.toList());
        }

        @Override
        public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
            this.env.newInstructions = new ArrayList<>();
            birBasicBlock.instructions.forEach(i -> this.optimizeNonTerm(i, this.env));
            birBasicBlock.instructions = this.env.newInstructions;
            this.optimizeTerm(birBasicBlock.terminator, this.env);
        }

        //////////////////////////////////////////////

        @Override
        public void visit(BIRNode.BIRErrorEntry birErrorEntry) {
            this.optimizeNode(birErrorEntry.errorOp, this.env);
        }

        // Terminating instructions

        @Override
        public void visit(BIRTerminator.GOTO birGoto) {
            // DO nothing
        }

        @Override
        public void visit(BIRTerminator.Call birCall) {
            this.optimizeNode(birCall.lhsOp, this.env);
            birCall.args.forEach(a -> this.optimizeNode(a, this.env));
        }

        @Override
        public void visit(BIRTerminator.AsyncCall birCall) {
            this.optimizeNode(birCall.lhsOp, this.env);
            birCall.args.forEach(a -> this.optimizeNode(a, this.env));
        }

        @Override
        public void visit(BIRTerminator.Return birReturn) {
            // Do nothing
        }

        @Override
        public void visit(BIRTerminator.Branch birBranch) {
            this.optimizeNode(birBranch.op, this.env);
        }

        @Override
        public void visit(BIRTerminator.FPCall fpCall) {
            this.optimizeNode(fpCall.lhsOp, this.env);
            this.optimizeNode(fpCall.fp, this.env);
            fpCall.args.forEach(a -> this.optimizeNode(a, this.env));
        }

        @Override
        public void visit(BIRTerminator.Lock lock) {
            // Do nothing
        }

        @Override
        public void visit(BIRTerminator.FieldLock lock) {
            this.optimizeNode(lock.localVar, this.env);
        }

        @Override
        public void visit(BIRTerminator.Unlock unlock) {
            unlock.fieldLocks.keySet().forEach(k -> this.optimizeNode(k, this.env));
        }

        @Override
        public void visit(BIRTerminator.Panic birPanic) {
            this.optimizeNode(birPanic.errorOp, this.env);
        }

        @Override
        public void visit(BIRTerminator.Wait birWait) {
            this.optimizeNode(birWait.lhsOp, this.env);
            birWait.exprList.forEach(e -> this.optimizeNode(e, this.env));
        }

        @Override
        public void visit(BIRTerminator.WaitAll waitAll) {
            this.optimizeNode(waitAll.lhsOp, this.env);
            waitAll.valueExprs.forEach(v -> this.optimizeNode(v, this.env));
        }

        @Override
        public void visit(BIRTerminator.Flush birFlush) {
            this.optimizeNode(birFlush.lhsOp, this.env);
        }

        @Override
        public void visit(BIRTerminator.WorkerReceive workerReceive) {
            this.optimizeNode(workerReceive.lhsOp, this.env);
        }

        @Override
        public void visit(BIRTerminator.WorkerSend workerSend) {
            this.optimizeNode(workerSend.lhsOp, this.env);
            this.optimizeNode(workerSend.data, this.env);
        }

        // Non-terminating instructions

        @Override
        public void visit(BIRNonTerminator.Move birMove) {
            if (birMove.lhsOp.variableDcl.kind != VarKind.TEMP) {
                this.env.newInstructions.add(birMove);
                return;
            }
            if (birMove.rhsOp.variableDcl.kind != VarKind.TEMP) {
                this.env.tempVars.put(birMove.lhsOp.variableDcl, birMove.rhsOp.variableDcl);
            }
        }

        @Override
        public void visit(BIRNonTerminator.BinaryOp birBinaryOp) {
            this.optimizeNode(birBinaryOp.lhsOp, this.env);
            birBinaryOp.rhsOp1.accept(this);
            birBinaryOp.rhsOp2.accept(this);
        }

        @Override
        public void visit(BIRNonTerminator.UnaryOP birUnaryOp) {
            this.optimizeNode(birUnaryOp.lhsOp, this.env);
            this.optimizeNode(birUnaryOp.rhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.ConstantLoad birConstantLoad) {
            this.optimizeNode(birConstantLoad.lhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewStructure birNewStructure) {
            this.optimizeNode(birNewStructure.lhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewArray birNewArray) {
            this.optimizeNode(birNewArray.lhsOp, this.env);
            this.optimizeNode(birNewArray.sizeOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.FieldAccess birFieldAccess) {
            this.optimizeNode(birFieldAccess.lhsOp, this.env);
            this.optimizeNode(birFieldAccess.rhsOp, this.env);
            this.optimizeNode(birFieldAccess.keyOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewError birNewError) {
            this.optimizeNode(birNewError.lhsOp, this.env);
            this.optimizeNode(birNewError.reasonOp, this.env);
            this.optimizeNode(birNewError.detailOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.FPLoad fpLoad) {
            this.optimizeNode(fpLoad.lhsOp, this.env);
            //TODO check this
        }

        @Override
        public void visit(BIRNonTerminator.TypeCast birTypeCast) {
            this.optimizeNode(birTypeCast.lhsOp, this.env);
            this.optimizeNode(birTypeCast.rhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewInstance newInstance) {
            this.optimizeNode(newInstance.lhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.IsLike birIsLike) {
            this.optimizeNode(birIsLike.lhsOp, this.env);
            this.optimizeNode(birIsLike.rhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.TypeTest birTypeTest) {
            this.optimizeNode(birTypeTest.lhsOp, this.env);
            this.optimizeNode(birTypeTest.rhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewTable newTable) {
            this.optimizeNode(newTable.lhsOp, this.env);
            this.optimizeNode(newTable.dataOp, this.env);
            this.optimizeNode(newTable.keyColOp, this.env);
            this.optimizeNode(newTable.columnsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewStream newStream) {
            this.optimizeNode(newStream.lhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewTypeDesc newTypeDesc) {
            this.optimizeNode(newTypeDesc.lhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewStringXMLQName newStringXMLQName) {
            this.optimizeNode(newStringXMLQName.lhsOp, this.env);
            this.optimizeNode(newStringXMLQName.stringQNameOP, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewXMLProcIns newXMLProcIns) {
            this.optimizeNode(newXMLProcIns.lhsOp, this.env);
            this.optimizeNode(newXMLProcIns.dataOp, this.env);
            this.optimizeNode(newXMLProcIns.targetOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewXMLComment newXMLComment) {
            this.optimizeNode(newXMLComment.lhsOp, this.env);
            this.optimizeNode(newXMLComment.textOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.XMLAccess xmlAccess) {
            this.optimizeNode(xmlAccess.lhsOp, this.env);
            this.optimizeNode(xmlAccess.rhsOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewXMLText newXMLText) {
            this.optimizeNode(newXMLText.lhsOp, this.env);
            this.optimizeNode(newXMLText.textOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewXMLQName newXMLQName) {
            this.optimizeNode(newXMLQName.lhsOp, this.env);
            this.optimizeNode(newXMLQName.localnameOp, this.env);
            this.optimizeNode(newXMLQName.nsURIOp, this.env);
            this.optimizeNode(newXMLQName.prefixOp, this.env);
        }

        @Override
        public void visit(BIRNonTerminator.NewXMLElement newXMLElement) {
            this.optimizeNode(newXMLElement.lhsOp, this.env);
            this.optimizeNode(newXMLElement.startTagOp, this.env);
            this.optimizeNode(newXMLElement.endTagOp, this.env);
            this.optimizeNode(newXMLElement.defaultNsURIOp, this.env);
        }

        // Operands
        @Override
        public void visit(BIROperand birVarRef) {
            BIRVariableDcl realVar = this.env.tempVars.get(birVarRef.variableDcl);
            if (realVar != null) {
                birVarRef.variableDcl = realVar;
            }
        }

    }

    /**
     * Env class to hold optimizer level common variables.
     */
    public static class OptimizerEnv {
        // key - temp var, value - real var
        private Map<BIRVariableDcl, BIRVariableDcl> tempVars = new HashMap<>();

        private List<BIRNonTerminator> newInstructions;

        public OptimizerEnv() {
        }
    }

}

package org.wso2.ballerinalang.compiler.bir.optimizer;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.CONST_LOAD;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.FP_CALL;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.TYPE_CAST;

/**
 * Remove redundant default function calls for record value creation.
 *
 * @since 2201.8.0
 */
public class BIRRecordValueOptimizer extends BIRVisitor {

    private final List<BIROperand> recordOperandList = new ArrayList<>();
    private final Map<BIROperand, BRecordType> recordOperandTypeMap = new HashMap<>();

    private BIRNode.BIRBasicBlock lastBB = null;
    private List<BIRNode.BIRBasicBlock> newBBs = new ArrayList<>();
    private List<BIRNode.BIRFunction> moduleFunctions = new ArrayList<>();
    private boolean fpRemoved = false;
    private boolean valueCreated = false;

    public void optimizeNode(BIRNode node) {
        // Collect lock nodes
        node.accept(this);
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {
        moduleFunctions = birPackage.functions;
        birPackage.typeDefs.forEach(tDef -> tDef.accept(this));
        birPackage.functions.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {
        birTypeDefinition.attachedFuncs.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {
        birFunction.basicBlocks.forEach(bb -> bb.accept(this));
        birFunction.basicBlocks = newBBs;
        newBBs = new ArrayList<>();
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock basicBlock) {
        List<BIRNonTerminator> instructions = basicBlock.instructions;
        for (BIRNonTerminator inst: instructions) {
            switch (inst.kind) {
                case NEW_TYPEDESC:
                    BType referredType = Types.getReferredType(((BIRNonTerminator.NewTypeDesc) inst).type);
                    if (referredType.tag == TypeTags.RECORD) {
                        recordOperandList.add(inst.lhsOp);
                        recordOperandTypeMap.put(inst.lhsOp, (BRecordType) referredType);
                    }
                    break;
                case NEW_STRUCTURE:
                    BIRNonTerminator.NewStructure newStructure = (BIRNonTerminator.NewStructure) inst;
                    recordOperandList.remove(newStructure.rhsOp);
                    valueCreated = true;
                    break;
                default:
                    break;
            }
        }
        if (!fpRemoved) {
            newBBs.add(basicBlock);
        } else {
            lastBB.instructions.addAll(basicBlock.instructions);
        }

        if (basicBlock.terminator.kind == FP_CALL) {
            BIRTerminator.FPCall fpCall = (BIRTerminator.FPCall) basicBlock.terminator;
            BIROperand recOperand = recordOperandList.isEmpty() ? null :
                    recordOperandList.get(recordOperandList.size() - 1);
            BRecordType recordType = recordOperandTypeMap.get(recOperand);
            if (recordType != null && recordType.tsymbol != null) {
                if (fpCall.fp.variableDcl.name.value.contains(recordType.tsymbol.name.value)) {
                    BIRNode.BIRFunction defaultFunction = getDefaultBIRFunction(fpCall.fp.variableDcl.name.value);
                    if (defaultFunction == null) {
                        return;
                    }
                    BIRNode.BIRBasicBlock firstBB = defaultFunction.basicBlocks.get(0);
                    lastBB = lastBB != null ? lastBB : basicBlock;
                    if (defaultFunction.basicBlocks.size() == 2 && containsOnlyConstantLoad(firstBB)) {
                        BIRNonTerminator.ConstantLoad constantLoad = (BIRNonTerminator.ConstantLoad)
                                firstBB.instructions.get(0);
                        BIRNonTerminator.ConstantLoad newConstLoad =
                                new BIRNonTerminator.ConstantLoad(constantLoad.pos, constantLoad.value,
                                        constantLoad.type, fpCall.lhsOp);
                        newConstLoad.scope = fpCall.scope;
                        lastBB.instructions.add(newConstLoad);
                        lastBB.terminator = null;
                        fpRemoved = true;
                    } else {
                        resetBasicBlock(basicBlock);
                    }
                } else {
                    resetBasicBlock(basicBlock);
                }
            }
        } else if (fpRemoved  && valueCreated) {
            resetBasicBlock(basicBlock);
        }
    }

    private boolean containsOnlyConstantLoad(BIRNode.BIRBasicBlock firstBB) {
        switch (firstBB.instructions.size()) {
            case 1:
                return firstBB.instructions.get(0).kind == CONST_LOAD;
            case 2:
                return firstBB.instructions.get(0).kind == CONST_LOAD &&
                        firstBB.instructions.get(1).kind == TYPE_CAST;
            default:
                return false;
        }
    }

    private void resetBasicBlock(BIRNode.BIRBasicBlock basicBlock) {
        if (lastBB != null) {
            lastBB.terminator = basicBlock.terminator;
            lastBB = null;
        }
        fpRemoved = false;
    }

    private BIRNode.BIRFunction getDefaultBIRFunction(String funcName) {
        for (BIRNode.BIRFunction func : moduleFunctions) {
            if (func.name.value.equals(funcName)) {
                return func;
            }
        }
        return null;
    }

}

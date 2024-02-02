/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.UsedState;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeDescClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;

public class BIRDeadNodeAnalyzer_ASM_Approach extends BIRVisitor {

    private static final CompilerContext.Key<BIRDeadNodeAnalyzer_ASM_Approach> BIR_DEAD_NODE_ANALYZER_ASM_APPROACH_KEY =
            new CompilerContext.Key<>();
    private static final HashSet<String> USED_FUNCTION_NAMES =
            new HashSet<>(Arrays.asList("main", ".<init>", ".<start>", ".<stop>"));
    public final HashMap<PackageID, InvocationData> pkgWiseInvocationData = new HashMap<>();
    public final HashMap<String, InvocationGraphNode> invocationNodePool = new HashMap<>();
    public final HashSet<InvocationGraphNode.TypeDefNode> unresolvedTypeDefs = new HashSet<>();
    private final PackageCache pkgCache;
    public PackageID currentPkgId;
    private BIRNode.BIRFunction currentParentFunction;

    private BIRDeadNodeAnalyzer_ASM_Approach(CompilerContext context) {
        context.put(BIR_DEAD_NODE_ANALYZER_ASM_APPROACH_KEY, this);
        this.pkgCache = PackageCache.getInstance(context);
    }

    public static BIRDeadNodeAnalyzer_ASM_Approach getInstance(CompilerContext context) {
        BIRDeadNodeAnalyzer_ASM_Approach birDeadNodeAnalyzerASMApproach = context.get(
                BIR_DEAD_NODE_ANALYZER_ASM_APPROACH_KEY);
        if (birDeadNodeAnalyzerASMApproach == null) {
            birDeadNodeAnalyzerASMApproach = new BIRDeadNodeAnalyzer_ASM_Approach(context);
        }
        return birDeadNodeAnalyzerASMApproach;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        currentPkgId = pkgNode.packageID;
        pkgWiseInvocationData.put(pkgNode.packageID, pkgNode.symbol.invocationData_Deprecated2);
        visit(pkgNode.symbol.bir);
        return pkgNode;
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {
        birPackage.typeDefs.forEach(this::createGraphNode);
        birPackage.typeDefs.forEach(tDef -> tDef.accept(this));
        birPackage.functions.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {
        birTypeDefinition.attachedFuncs.forEach(func -> func.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {
        currentParentFunction = birFunction;
        InvocationGraphNode.FunctionNode graphNode = getGraphNode(birFunction);

        if (USED_FUNCTION_NAMES.contains(birFunction.originalName.value)) {
            graphNode.markSelfAndChildrenAsUsed(pkgWiseInvocationData);
        }
        birFunction.basicBlocks.forEach(bb -> {
            bb.accept(this);
        });
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        if (birBasicBlock.terminator.getKind() == InstructionKind.CALL) {
            BIRTerminator.Call terminatorCall = (BIRTerminator.Call) birBasicBlock.terminator;
            addInvocation(currentParentFunction, terminatorCall);
        }

        birBasicBlock.instructions.forEach(instruction -> {
            if (instruction.getKind() == InstructionKind.NEW_TYPEDESC ||
                    instruction.getKind() == InstructionKind.NEW_INSTANCE) {
                instruction.accept(this);
            }
        });
    }

    @Override
    public void visit(BIRNonTerminator.NewTypeDesc typeDesc) {
        addInvocation(currentParentFunction, typeDesc);
    }

    @Override
    public void visit(BIRNonTerminator.NewInstance newInstance) {
        addInvocation(currentParentFunction, newInstance);
    }

    private void addInvocation(BIRNode.BIRFunction parentFunction, BIRTerminator.Call childFunction) {
        InvocationGraphNode parentNode = getGraphNode(parentFunction);
        InvocationGraphNode childNode = getGraphNode(childFunction);
        parentNode.childrenInvocations.add(childNode);
        childNode.parentFunctions.add(parentNode);
        if (parentNode.usedState == UsedState.USED) {
            childNode.markSelfAndChildrenAsUsed(pkgWiseInvocationData);
        }
    }

    private void addInvocation(BIRNode.BIRFunction parentFunction, BIRNonTerminator.NewTypeDesc newTypeDesc) {
        InvocationGraphNode parentNode = getGraphNode(parentFunction);
        InvocationGraphNode.TypeDefNode childNode = getGraphNode(newTypeDesc);
        parentNode.childrenInvocations.add(childNode);
        if (parentNode.usedState == UsedState.USED) {
            childNode.markSelfAndChildrenAsUsed(pkgWiseInvocationData);
        }
    }

    private void addInvocation(BIRNode.BIRFunction parentFunction, BIRNonTerminator.NewInstance newInstance) {
        InvocationGraphNode parentNode = getGraphNode(parentFunction);
        InvocationGraphNode.TypeDefNode childNode = getGraphNode(newInstance);
        parentNode.childrenInvocations.add(childNode);
        if (parentNode.usedState == UsedState.USED) {
            childNode.markSelfAndChildrenAsUsed(pkgWiseInvocationData);
        }
    }

    private InvocationGraphNode.FunctionNode getGraphNode(BIRNode.BIRFunction parentFunction) {
        String nodeId = currentPkgId.toString() + "/" + parentFunction.originalName;
        if (invocationNodePool.containsKey(nodeId)) {
            return (InvocationGraphNode.FunctionNode) invocationNodePool.get(nodeId);
        }

        InvocationGraphNode.FunctionNode graphNode =
                new InvocationGraphNode.FunctionNode(parentFunction.originalName.value, currentPkgId,
                        getFileName(parentFunction));
        initializeNode(graphNode);
        return graphNode;
    }

    private InvocationGraphNode.FunctionNode getGraphNode(BIRTerminator.Call invocation) {
        String nodeId = invocation.calleePkg.toString() + "/" + invocation.originalName;
        if (invocationNodePool.containsKey(nodeId)) {
            return (InvocationGraphNode.FunctionNode) invocationNodePool.get(nodeId);
        }

        InvocationGraphNode.FunctionNode graphNode =
                new InvocationGraphNode.FunctionNode(invocation.originalName.value, invocation.calleePkg,
                        getFileName(invocation));
        initializeNode(graphNode);
        return graphNode;
    }

    private InvocationGraphNode.TypeDefNode getGraphNode(BIRNonTerminator.NewTypeDesc newTypeDesc) {
        String nodeId = newTypeDesc.type.toString();
        if (invocationNodePool.containsKey(nodeId)) {
            return (InvocationGraphNode.TypeDefNode) invocationNodePool.get(nodeId);
        }
        // TODO Complete the read back cycle and remove the following temp Nodes
        InvocationGraphNode.TypeDefNode tempGraphNode = new InvocationGraphNode.TypeDefNode(nodeId);
        unresolvedTypeDefs.add(tempGraphNode);
        return tempGraphNode;
    }

    private InvocationGraphNode.TypeDefNode getGraphNode(BIRNonTerminator.NewInstance newInstance) {
        String nodeId = newInstance.expectedType.toString();
        if (invocationNodePool.containsKey(nodeId)) {
            return (InvocationGraphNode.TypeDefNode) invocationNodePool.get(nodeId);
        }
        // TODO Complete the read back cycle and remove the following temp Nodes
        InvocationGraphNode.TypeDefNode tempGraphNode = new InvocationGraphNode.TypeDefNode(nodeId);
        unresolvedTypeDefs.add(tempGraphNode);
        return tempGraphNode;
    }

    private void createGraphNode(BIRNode.BIRTypeDefinition birTypeDef) {
        InvocationGraphNode.TypeDefNode graphNode =
                new InvocationGraphNode.TypeDefNode(birTypeDef.internalName.toString(), currentPkgId,
                        getFileName(birTypeDef), getTypeDefClassPath(birTypeDef), getTypeDescClassPath(birTypeDef), birTypeDef.type.toString());
        initializeNode(graphNode);
    }

    private void initializeNode(InvocationGraphNode.FunctionNode graphNode) {
        invocationNodePool.put(graphNode.getNodeID(), graphNode);

        InvocationData invocationData = getInvocationData(graphNode.pkgID);
        invocationData.allFunctions.add(graphNode);
        invocationData.deadFunctions.add(graphNode);

        invocationData.deadFunctionJarPathMap.putIfAbsent(graphNode.jarClassFilePath, new HashSet<>());
        invocationData.deadFunctionJarPathMap.get(graphNode.jarClassFilePath).add(graphNode.nodeName);
    }

    private void initializeNode(InvocationGraphNode.TypeDefNode graphNode) {
        invocationNodePool.put(graphNode.getNodeID(), graphNode);

        InvocationData invocationData = getInvocationData(graphNode.pkgID);
        invocationData.allTypedefs.add(graphNode);
        invocationData.deadTypeDefs.add(graphNode);

        invocationData.deadTypeDefJarPathMap.add(graphNode.jarClassFilePath);
        invocationData.deadTypeDefJarPathMap.add(graphNode.typeDescJarPath);
    }

    private InvocationData getInvocationData(PackageID pkgId) {
        return pkgCache.getSymbol(pkgId).invocationData_Deprecated2;
    }

    private String getFileName(BIRNode invocation) {
        if (invocation.pos == null) {
            return null;
        }
        return invocation.pos.lineRange().fileName();
    }

    private String getFileName(BIRNode.BIRTypeDefinition typeDef) {
        if (typeDef.pos == null) {
            return null;
        }
        return typeDef.pos.lineRange().fileName();
    }

    private String getTypeDefClassPath(BIRNode.BIRTypeDefinition birTypeDef) {
        return getTypeValueClassName(currentPkgId, birTypeDef.internalName.value) + ".class";
    }

    // TODO getTypeDescClassPath method adds type descs for all the type defs. But it is only needed for RECORDS and TypeDescs
    private String getTypeDescClassPath(BIRNode.BIRTypeDefinition birTypeDef) {
        String typeDesc = getTypeDescClassName(JvmCodeGenUtil.getPackageName(currentPkgId), birTypeDef.internalName.value) + ".class";
        return typeDesc;
    }

    public static class InvocationData {

        public final PackageID pkgID;
        public final HashSet<InvocationGraphNode> allFunctions = new HashSet<>();
        public final HashSet<InvocationGraphNode> usedFunctions = new HashSet<>();
        public final HashSet<InvocationGraphNode> deadFunctions = new HashSet<>();
        public final HashSet<BIRNode.BIRFunction> deadBirFunctions = new HashSet<>();
        public final HashMap<String, HashSet<String>> deadFunctionJarPathMap = new HashMap<>();
        public final HashSet<InvocationGraphNode> allTypedefs = new HashSet<>();
        public final HashSet<InvocationGraphNode> usedTypeDefs = new HashSet<>();
        public final HashSet<InvocationGraphNode> deadTypeDefs = new HashSet<>();
        public final HashSet<BIRNode.BIRTypeDefinition> deadBirTypeDefs = new HashSet<>();
        public final HashSet<String> deadTypeDefJarPathMap = new HashSet<>();

        public InvocationData(PackageID pkgID) {
            this.pkgID = pkgID;
        }
    }
}

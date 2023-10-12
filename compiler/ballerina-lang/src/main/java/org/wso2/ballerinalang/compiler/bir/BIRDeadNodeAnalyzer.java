package org.wso2.ballerinalang.compiler.bir;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.UsedState;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.HashSet;

public class BIRDeadNodeAnalyzer extends BIRVisitor {

    private static final CompilerContext.Key<BIRDeadNodeAnalyzer> BIR_DEAD_NODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private static final String MAIN_FUNCTION_NAME = "main";
    private static final String INIT_FUNCTION_NAME = "init";
    public final HashMap<PackageID, HashSet<BIRInvocationGraphNode>> pkgWiseInvocationGraphs = new HashMap<>();
    public final HashMap<PackageID, HashSet<BIRInvocationGraphNode>> usedFunctions = new HashMap<>();
    public final HashMap<PackageID, HashSet<BIRInvocationGraphNode>> deadFunctions = new HashMap<>();
    public final HashMap<String, BIRInvocationGraphNode> invocationNodePool = new HashMap<>();
    public HashSet<BIRInvocationGraphNode> currentInvocationGraph;
    private static BIRNode.BIRFunction currentParentFunction;
    public static PackageID currentPkgId;

    private BIRDeadNodeAnalyzer(CompilerContext context) {
        context.put(BIR_DEAD_NODE_ANALYZER_KEY, this);
    }

    public static BIRDeadNodeAnalyzer getInstance(CompilerContext context) {
        BIRDeadNodeAnalyzer birDeadNodeAnalyzer = context.get(BIR_DEAD_NODE_ANALYZER_KEY);
        if (birDeadNodeAnalyzer == null) {
            birDeadNodeAnalyzer = new BIRDeadNodeAnalyzer(context);
        }
        return birDeadNodeAnalyzer;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgWiseInvocationGraphs.putIfAbsent(pkgNode.packageID, new HashSet<>());
        currentPkgId = pkgNode.packageID;
        currentInvocationGraph = pkgWiseInvocationGraphs.get(currentPkgId);
        usedFunctions.putIfAbsent(currentPkgId, new HashSet<>());
        deadFunctions.putIfAbsent(currentPkgId, new HashSet<>());

        visit(pkgNode.symbol.bir);
        return pkgNode;
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {
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
        BIRInvocationGraphNode graphNode = getGraphNode(birFunction);

        if (birFunction.originalName.value.equals(MAIN_FUNCTION_NAME) ||
                birFunction.originalName.value.equals(INIT_FUNCTION_NAME)) {
            graphNode.markAsUsed();
        }

        birFunction.basicBlocks.forEach(bb -> {
            if (bb.terminator.getKind() == InstructionKind.CALL) {
                bb.accept(this);
            }
        });
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        BIRTerminator.Call terminatorCall = (BIRTerminator.Call) birBasicBlock.terminator;
        addInvocation(currentParentFunction, terminatorCall);
    }

    private void addInvocation(BIRNode.BIRFunction parentFunction, BIRTerminator.Call childFunction) {
        BIRInvocationGraphNode parentNode = getGraphNode(parentFunction);
        BIRInvocationGraphNode childNode = getGraphNode(childFunction);
        addInvocation(parentNode, childNode);
    }

    private void addInvocation(BIRInvocationGraphNode parentNode, BIRInvocationGraphNode childNode) {
        parentNode.childrenInvocations.add(childNode);
        if (parentNode.usedState == UsedState.USED) {
            markSelfAndChildrenAsUsed(childNode);
        }
    }

    private void markSelfAndChildrenAsUsed(BIRInvocationGraphNode birFunctionGraphNode) {
        if (birFunctionGraphNode.usedState == UsedState.UNUSED) {
            deadFunctions.get(birFunctionGraphNode.pkgID).remove(birFunctionGraphNode);
            usedFunctions.putIfAbsent(birFunctionGraphNode.pkgID, new HashSet<>());
            usedFunctions.get(birFunctionGraphNode.pkgID).add(birFunctionGraphNode);
            birFunctionGraphNode.markAsUsed();
            birFunctionGraphNode.childrenInvocations.forEach(this::markSelfAndChildrenAsUsed);
        }
    }

    private BIRInvocationGraphNode getGraphNode(BIRNode.BIRFunction parentFunction) {
        String nodeId = currentPkgId.toString() + "/" + parentFunction.originalName;
        if (invocationNodePool.containsKey(nodeId)) {
            return invocationNodePool.get(nodeId);
        }

        BIRInvocationGraphNode graphNode =
                new BIRInvocationGraphNode(parentFunction.originalName.value, currentPkgId);
        initializeNode(graphNode);
        return graphNode;
    }

    private BIRInvocationGraphNode getGraphNode(BIRTerminator.Call invocation) {
        String nodeId = invocation.calleePkg.toString() + "/" + invocation.originalName;
        if (invocationNodePool.containsKey(nodeId)) {
            return invocationNodePool.get(nodeId);
        }

        BIRInvocationGraphNode graphNode =
                new BIRInvocationGraphNode(invocation.originalName.value, invocation.calleePkg);
        initializeNode(graphNode);
        return graphNode;
    }

    private void initializeNode(BIRInvocationGraphNode graphNode) {
        invocationNodePool.putIfAbsent(graphNode.getNodeID(), graphNode);
        pkgWiseInvocationGraphs.putIfAbsent(graphNode.pkgID, new HashSet<>());
        pkgWiseInvocationGraphs.get(graphNode.pkgID).add(graphNode);
        deadFunctions.putIfAbsent(graphNode.pkgID, new HashSet<>());
        deadFunctions.get(graphNode.pkgID).add(graphNode);
    }

    private class BIRInvocationGraphNode {

        private String nodeName;
        private PackageID pkgID;
        private HashSet<BIRInvocationGraphNode> childrenInvocations = new HashSet<>();
        private UsedState usedState = UsedState.UNUSED;

        public BIRInvocationGraphNode(String nodeName, PackageID nodePkgID) {
            this.nodeName = nodeName;
            this.pkgID = nodePkgID;
        }

        private void markAsUsed() {
            this.usedState = UsedState.USED;
        }

        private String getNodeID() {
            return pkgID.toString() + "/" + nodeName;
        }

        public String toString() {
            return nodeName;
        }
    }
}

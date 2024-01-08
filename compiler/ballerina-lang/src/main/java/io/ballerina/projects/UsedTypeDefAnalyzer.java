package io.ballerina.projects;

import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SimpleBTypeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.UsedState;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.HashSet;

public class UsedTypeDefAnalyzer extends SimpleBTypeAnalyzer<UsedTypeDefAnalyzer.AnalyzerData> {

    private static final CompilerContext.Key<UsedTypeDefAnalyzer> BIR_TYPE_DEF_ANALYZER_KEY = new CompilerContext.Key<>();
    private final HashMap<BType, BIRNode.BIRTypeDefinition> typeDefPool = new HashMap<>();
    public final HashSet<BType> visitedTypes = new HashSet<>();
    public PackageCache pkgCache;
    public UsedBIRNodeAnalyzer usedBIRNodeAnalyzer;

    private UsedTypeDefAnalyzer(CompilerContext context) {
        context.put(BIR_TYPE_DEF_ANALYZER_KEY, this);
        this.pkgCache = PackageCache.getInstance(context);
        this.usedBIRNodeAnalyzer = UsedBIRNodeAnalyzer.getInstance(context);
    }

    public static UsedTypeDefAnalyzer getInstance(CompilerContext context) {
        UsedTypeDefAnalyzer typeDefAnalyzer3 = context.get(BIR_TYPE_DEF_ANALYZER_KEY);
        if (typeDefAnalyzer3 == null) {
            typeDefAnalyzer3 = new UsedTypeDefAnalyzer(context);
        }
        return typeDefAnalyzer3;
    }

    @Override
    public void visitType(BType bType, UsedTypeDefAnalyzer.AnalyzerData data) {
        if (bType == null) {
            return;
        }

        BIRNode.BIRDocumentableNode prevParentNode = data.currentParentNode;
        boolean prevShouldAnalyzeChildren = data.shouldAnalyzeChildren;

        addDependency(bType, data);
        if (data.shouldAnalyzeChildren) {
            bType.accept(this, data);
        }

        data.currentParentNode = prevParentNode;
        data.shouldAnalyzeChildren = prevShouldAnalyzeChildren;
    }

    @Override
    public void analyzeType(BType bType, UsedTypeDefAnalyzer.AnalyzerData data) {
        HashSet<UsedBIRNodeAnalyzer.FunctionPointerData> fpDataSet = usedBIRNodeAnalyzer.currentInvocationData.getFpData(bType);
        if (fpDataSet != null) {
            fpDataSet.forEach(fpData -> {
                fpData.lambdaPointerVar.usedState = UsedState.USED;
                fpData.lambdaFunction.accept(usedBIRNodeAnalyzer);
            });
        }
    }

    public void populateTypeDefPool(BIRNode.BIRPackage birPackage, HashSet<String> interopDependencies) {
        birPackage.typeDefs.forEach(typeDef -> {
            typeDefPool.putIfAbsent(typeDef.type, typeDef);
            if (interopDependencies != null && interopDependencies.contains(typeDef.internalName.toString())) {
                pkgCache.getInvocationData2(typeDef.getPackageID()).startPointNodes.add(typeDef);
            }
        });
    }

    public void analyzeTypeDef(BIRNode.BIRTypeDefinition typeDef) {
        final UsedTypeDefAnalyzer.AnalyzerData data = new UsedTypeDefAnalyzer.AnalyzerData();
        data.currentParentNode = typeDef;
        typeDef.referencedTypes.forEach(refType -> visitType(refType, data));
        visitType(typeDef.type, data);
    }

    public void analyzeTypeDefWithinScope(BType bType, BIRNode.BIRDocumentableNode parentNode) {
        final UsedTypeDefAnalyzer.AnalyzerData data = new UsedTypeDefAnalyzer.AnalyzerData();
        data.currentParentNode = parentNode;
        visitType(bType, data);
    }

    private BIRNode.BIRTypeDefinition getBIRTypeDef(BType bType) {
        // TODO find a more reliable way to do this
        if (bType.tsymbol == null) {
            return null;
        }

        UsedBIRNodeAnalyzer.InvocationData invocationData = pkgCache.getInvocationData2(bType.tsymbol.pkgID);
        if (!invocationData.moduleIsUsed) {
            invocationData.registerNodes(this, pkgCache.getBirPkg(bType.tsymbol.pkgID));
        }

        // TODO check whether we need the null check
        if (typeDefPool.containsKey(bType)) {
            return typeDefPool.get(bType);
        }
        return null;
    }


    private void addDependency(BType bType, UsedTypeDefAnalyzer.AnalyzerData data) {
        data.shouldAnalyzeChildren = visitedTypes.add(bType);
        bType.isUsed = true;
        BIRNode.BIRTypeDefinition childNode = getBIRTypeDef(bType);
        if (childNode == null) {
            return;
        }

        if (data.currentParentNode != childNode) {
            data.currentParentNode.childNodes.add(childNode);
            childNode.parentNodes.add(data.currentParentNode);
        }

        if (!childNode.isInSamePkg(usedBIRNodeAnalyzer.currentPkgID)) {
            pkgCache.getInvocationData2(childNode.getPackageID()).startPointNodes.add(childNode);
            visitedTypes.remove(bType);
            data.shouldAnalyzeChildren = false;
            return;
        }

        usedBIRNodeAnalyzer.currentInvocationData.addToUsedPool(childNode);
        data.currentParentNode = childNode;
        childNode.usedState = UsedState.USED;   // TODO Remove this and find a better way

        // Handling method overriding instances TODO Find a way to handle polymorphism
        childNode.attachedFuncs.forEach(attachedFunc -> {
            childNode.childNodes.add(attachedFunc);
            attachedFunc.parentNodes.add(childNode);
            attachedFunc.accept(usedBIRNodeAnalyzer);
        });
    }

    public static class AnalyzerData {
        BIRNode.BIRDocumentableNode currentParentNode;
        boolean shouldAnalyzeChildren = true;
    }
}

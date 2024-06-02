package io.ballerina.projects;

import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SimpleBTypeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashMap;
import java.util.HashSet;

public class UsedTypeDefAnalyzer extends SimpleBTypeAnalyzer<UsedTypeDefAnalyzer.AnalyzerData> {

    private static final CompilerContext.Key<UsedTypeDefAnalyzer> BIR_TYPE_DEF_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private final HashMap<BType, BIRNode.BIRTypeDefinition> globalTypeDefPool = new HashMap<>();
    private final HashSet<BType> visitedTypes = new HashSet<>();
    private PackageCache pkgCache;
    private UsedBIRNodeAnalyzer usedBIRNodeAnalyzer;

    private UsedTypeDefAnalyzer(CompilerContext context) {
        context.put(BIR_TYPE_DEF_ANALYZER_KEY, this);
        this.pkgCache = PackageCache.getInstance(context);
        this.usedBIRNodeAnalyzer = UsedBIRNodeAnalyzer.getInstance(context);
    }

    public static UsedTypeDefAnalyzer getInstance(CompilerContext context) {
        UsedTypeDefAnalyzer usedTypeDefAnalyzer = context.get(BIR_TYPE_DEF_ANALYZER_KEY);
        if (usedTypeDefAnalyzer == null) {
            usedTypeDefAnalyzer = new UsedTypeDefAnalyzer(context);
        }
        return usedTypeDefAnalyzer;
    }

    @Override
    public void visitType(BType bType, AnalyzerData data) {
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

    // Since bRecordTypes can have related anon functions for default values we have to mark them as used as well
    @Override
    public void analyzeType(BType bType, AnalyzerData data) {
        HashSet<UsedBIRNodeAnalyzer.FunctionPointerData> fpDataSet =
                usedBIRNodeAnalyzer.currentInvocationData.getFpData(bType);
        if (fpDataSet != null) {
            fpDataSet.forEach(fpData -> {
                fpData.lambdaPointerVar.markAsUsed();
                fpData.lambdaFunction.accept(usedBIRNodeAnalyzer);
            });
        }
    }

    protected void populateTypeDefPool(BIRNode.BIRPackage birPackage, HashSet<String> interopDependencies) {
        birPackage.typeDefs.forEach(typeDef -> {
            // In case there are more than one reference types referencing to the same type
            if (typeDef.referenceType != null && typeDef.type.tag == TypeTags.TYPEREFDESC) {
                globalTypeDefPool.putIfAbsent(typeDef.referenceType, typeDef);
            } else {
                globalTypeDefPool.putIfAbsent(typeDef.type, typeDef);
            }
            if (interopDependencies != null && interopDependencies.contains(typeDef.internalName.toString())) {
                usedBIRNodeAnalyzer.getInvocationData(typeDef.getPackageID()).startPointNodes.add(typeDef);
            }
        });
    }

    protected void analyzeTypeDef(BIRNode.BIRTypeDefinition typeDef) {
        final AnalyzerData data = new AnalyzerData();
        data.currentParentNode = typeDef;
        typeDef.referencedTypes.forEach(refType -> visitType(refType, data));
        visitType(typeDef.type, data);
    }

    protected void analyzeTypeDefWithinScope(BType bType, BIRNode.BIRDocumentableNode parentNode) {
        final AnalyzerData data = new AnalyzerData();
        data.currentParentNode = parentNode;
        visitType(bType, data);
    }

    private BIRNode.BIRTypeDefinition getBIRTypeDef(BType bType) {
        if (bType.tsymbol == null) {
            return null;
        }

        UsedBIRNodeAnalyzer.InvocationData invocationData = usedBIRNodeAnalyzer.getInvocationData(bType.tsymbol.pkgID);
        if (invocationData == null) {
            return null;
        }
        if (!invocationData.moduleIsUsed && !usedBIRNodeAnalyzer.isTestablePkgAnalysis) {
            invocationData.registerNodes(this, pkgCache.getBirPkg(bType.tsymbol.pkgID));
        }

        if (globalTypeDefPool.containsKey(bType)) {
            return globalTypeDefPool.get(bType);
        }
        return null;
    }

    private void addDependency(BType bType, AnalyzerData data) {
        data.shouldAnalyzeChildren = visitedTypes.add(bType);
        bType.isUsed = true;
        BIRNode.BIRTypeDefinition childTypeDefNode = getBIRTypeDef(bType);
        if (childTypeDefNode == null) {
            return;
        }

        if (data.currentParentNode != childTypeDefNode) {
            data.currentParentNode.childNodes.add(childTypeDefNode);
            childTypeDefNode.parentNodes.add(data.currentParentNode);
        }

        if (!childTypeDefNode.isInSamePkg(usedBIRNodeAnalyzer.currentPkgID)) {
            usedBIRNodeAnalyzer.getInvocationData(childTypeDefNode.getPackageID()).startPointNodes.add(
                    childTypeDefNode);
            visitedTypes.remove(bType);
            data.shouldAnalyzeChildren = false;
            return;
        }

        usedBIRNodeAnalyzer.currentInvocationData.addToUsedPool(childTypeDefNode);
        data.currentParentNode = childTypeDefNode;
        childTypeDefNode.markAsUsed();

        // Handling method overriding instances
        childTypeDefNode.attachedFuncs.forEach(attachedFunc -> {
            childTypeDefNode.childNodes.add(attachedFunc);
            attachedFunc.parentNodes.add(childTypeDefNode);
            attachedFunc.accept(usedBIRNodeAnalyzer);
        });
    }

    public static class AnalyzerData {
        BIRNode.BIRDocumentableNode currentParentNode;
        boolean shouldAnalyzeChildren = true;
    }
}

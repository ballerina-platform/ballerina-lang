/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
package io.ballerina.projects;

import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SimpleBTypeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Analyses a given type definition and marks its dependencies.
 *
 * @since 2201.10.0
 */
public class UsedTypeDefAnalyzer extends SimpleBTypeAnalyzer<UsedTypeDefAnalyzer.AnalyzerData> {

    private static final CompilerContext.Key<UsedTypeDefAnalyzer> BIR_TYPE_DEF_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private final Map<BType, BIRNode.BIRTypeDefinition> globalTypeDefPool = new HashMap<>();
    private final Set<BType> visitedTypes = new HashSet<>();
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

        if (isTestablePkgImportedModuleDependency(bType)) {
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
        Set<UsedBIRNodeAnalyzer.FunctionPointerData> fpDataSet =
                usedBIRNodeAnalyzer.currentInvocationData.getFpData(bType);
        if (fpDataSet != null) {
            fpDataSet.forEach(fpData -> {
                fpData.lambdaPointerVar.markAsUsed();
                fpData.lambdaFunction.accept(usedBIRNodeAnalyzer);
            });
        }
    }

    protected void populateTypeDefPool(BIRNode.BIRPackage birPackage, Set<String> interopDependencies) {
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

    public boolean isTestablePkgImportedModuleDependency(BType bType) {
        if (bType.tsymbol == null || bType.tsymbol.pkgID == null) {
            return false;
        }

        return usedBIRNodeAnalyzer.isTestablePkgAnalysis &&
                !bType.tsymbol.pkgID.equals(usedBIRNodeAnalyzer.currentPkgID);
    }

    public static class AnalyzerData {
        BIRNode.BIRDocumentableNode currentParentNode;
        boolean shouldAnalyzeChildren = true;
    }
}

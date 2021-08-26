/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import io.ballerina.projects.ProjectKind;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for injecting Observability imports.
 */
public class ObservabilityDesugar {
    private static final CompilerContext.Key<ObservabilityDesugar> OBSERVE_DESUGAR_KEY = new CompilerContext.Key<>();
    private final boolean observabilityIncluded;
    private final PackageCache packageCache;

    public static ObservabilityDesugar getInstance(CompilerContext context) {
        ObservabilityDesugar desugar = context.get(OBSERVE_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new ObservabilityDesugar(context);
        }
        return desugar;
    }

    private ObservabilityDesugar(CompilerContext context) {
        context.put(OBSERVE_DESUGAR_KEY, this);
        observabilityIncluded = Boolean.parseBoolean(CompilerOptions.getInstance(context)
                .get(CompilerOptionName.OBSERVABILITY_INCLUDED));
        // TODO: Merge both modules into one user facing module
        packageCache = PackageCache.getInstance(context);
    }

    void addObserveInternalModuleImport(BLangPackage pkgNode) {
        if (observabilityIncluded && (pkgNode.moduleContextDataHolder != null
                && !pkgNode.moduleContextDataHolder.projectKind().equals(ProjectKind.BALA_PROJECT))) {
            BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
            List<BLangIdentifier> pkgNameComps = new ArrayList<>();
            pkgNameComps.add(ASTBuilderUtil.createIdentifier(pkgNode.pos, Names.OBSERVE.value));
            importDcl.pkgNameComps = pkgNameComps;
            importDcl.pos = pkgNode.symbol.pos;
            importDcl.orgName = ASTBuilderUtil.createIdentifier(pkgNode.pos, Names.BALLERINA_INTERNAL_ORG.value);
            importDcl.alias = ASTBuilderUtil.createIdentifier(pkgNode.pos, "_");
            importDcl.version = ASTBuilderUtil.createIdentifier(pkgNode.pos, "");
            importDcl.symbol = packageCache.getSymbol(PackageID.OBSERVE_INTERNAL);
            pkgNode.imports.add(importDcl);
            pkgNode.symbol.imports.add(importDcl.symbol);
        }
    }
}

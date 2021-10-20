/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for injecting Code2Cloud import.
 *
 * @since 2.0.0
 */
public class Code2CloudDesugar {
    private static final CompilerContext.Key<Code2CloudDesugar> CODE2CLOUD_DESUGAR_KEY = new CompilerContext.Key<>();
    private final boolean c2cEnabled;
    private final PackageCache packageCache;
    private PackageID c2cPkgID;

    private Code2CloudDesugar(CompilerContext context) {
        context.put(CODE2CLOUD_DESUGAR_KEY, this);
        String cloudOption = CompilerOptions.getInstance(context).get(CompilerOptionName.CLOUD);
        c2cEnabled = "k8s".equals(cloudOption) || "docker".equals(cloudOption) || "choreo".equals(cloudOption);
        packageCache = PackageCache.getInstance(context);
        final BPackageSymbol symbol = PackageCache.getInstance(context).getSymbol(Names.BALLERINA_ORG.value
                + Names.ORG_NAME_SEPARATOR.value + Names.CLOUD.value);
        if (symbol != null) {
            c2cPkgID = symbol.pkgID;
        }
    }

    public static Code2CloudDesugar getInstance(CompilerContext context) {
        Code2CloudDesugar desugar = context.get(CODE2CLOUD_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new Code2CloudDesugar(context);
        }
        return desugar;
    }

    void addCode2CloudModuleImport(BLangPackage pkgNode) {
        if (c2cEnabled && (pkgNode.moduleContextDataHolder != null
                && !pkgNode.moduleContextDataHolder.projectKind().equals(ProjectKind.BALA_PROJECT))
                && c2cPkgID != null) {
            BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
            List<BLangIdentifier> pkgNameComps = new ArrayList<>();
            pkgNameComps.add(ASTBuilderUtil.createIdentifier(pkgNode.pos, Names.CLOUD.value));
            importDcl.pkgNameComps = pkgNameComps;
            importDcl.pos = pkgNode.symbol.pos;
            importDcl.orgName = ASTBuilderUtil.createIdentifier(pkgNode.pos, Names.BALLERINA_ORG.value);
            importDcl.alias = ASTBuilderUtil.createIdentifier(pkgNode.pos, "_");
            importDcl.version = ASTBuilderUtil.createIdentifier(pkgNode.pos, "");
            importDcl.symbol = packageCache.getSymbol(c2cPkgID);
            pkgNode.imports.add(importDcl);
            pkgNode.symbol.imports.add(importDcl.symbol);
        }
    }
}

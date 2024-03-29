/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * This class includes a set of methods to add the management-service module import to the Ballerina package.
 *
 * @since 2201.9.0
 */
public class RuntimeManagementDesugar {

    private static final CompilerContext.Key<RuntimeManagementDesugar> RUNTIME_MANAGEMENT_DESUGAR_KEY =
            new CompilerContext.Key<>();
    private final boolean runtimeManagementIncluded;
    private final boolean serviceCatalogPublish;
    private final String serviceCatalogVendor;
    private final PackageCache packageCache;
    private PackageID managementPkgID;
    private PackageID serviceCatalogPkgID;

    public static RuntimeManagementDesugar getInstance(CompilerContext context) {
        RuntimeManagementDesugar desugar = context.get(RUNTIME_MANAGEMENT_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new RuntimeManagementDesugar(context);
        }
        return desugar;
    }

    private RuntimeManagementDesugar(CompilerContext context) {
        context.put(RUNTIME_MANAGEMENT_DESUGAR_KEY, this);
        runtimeManagementIncluded = Boolean.parseBoolean(CompilerOptions.getInstance(context)
                .get(CompilerOptionName.RUNTIME_MANAGEMENT_INCLUDED));
        serviceCatalogPublish = Boolean.parseBoolean(CompilerOptions.getInstance(context)
                .get(CompilerOptionName.SERVICE_CATALOG_PUBLISH));
        serviceCatalogVendor = CompilerOptions.getInstance(context).get(CompilerOptionName.SERVICE_CATALOG_VENDOR);
        packageCache = PackageCache.getInstance(context);

        final BPackageSymbol serviceCatalogPackageSymbol;
        final BPackageSymbol symbol;

        if (runtimeManagementIncluded || serviceCatalogPublish) {
            symbol = PackageCache.getInstance(context).getSymbol(Names.BALLERINA_ORG.value
                    + Names.ORG_NAME_SEPARATOR.value + Names.RUNTIME_MANAGEMENT.value);
        } else {
            symbol = null;
        }

        if (serviceCatalogPublish) {
            if (serviceCatalogVendor != null && serviceCatalogVendor.equals(Names.WSO2_APIM_CATALOG.getValue())) {
                serviceCatalogPackageSymbol = PackageCache.getInstance(context)
                        .getSymbol(Names.BALLERINAX_ORG.value
                                + Names.ORG_NAME_SEPARATOR.value + Names.WSO2_APIM_CATALOG.value);
            } else {
                serviceCatalogPackageSymbol = null;
            }
        } else {
            serviceCatalogPackageSymbol = null;
        }

        if (symbol != null) {
            managementPkgID = symbol.pkgID;
        }
        if (serviceCatalogPackageSymbol != null) {
            serviceCatalogPkgID = serviceCatalogPackageSymbol.pkgID;
        }
    }

    void addManagementServiceModuleImport(BLangPackage pkgNode) {
        if ((runtimeManagementIncluded || serviceCatalogPublish)
                && isContextDataHolderAndProjectKindValid(pkgNode)
                && managementPkgID != null) {
            BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
            if (serviceCatalogVendor != null && serviceCatalogVendor.equals(Names.WSO2_APIM_CATALOG.getValue())) {
                importWso2APIManagerCatalogModule(pkgNode, importDcl);
            }
            pkgNode.symbol.imports.add(importDcl.symbol);
        }
    }

    void addWso2ApiManagerCatalogModuleImport(BLangPackage pkgNode) {
        if (serviceCatalogPublish
                && isContextDataHolderAndProjectKindValid(pkgNode)
                && serviceCatalogPkgID != null) {
            BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();

            pkgNode.symbol.imports.add(importDcl.symbol);
        }
    }

    private boolean isContextDataHolderAndProjectKindValid(BLangPackage pkgNode) {
        return (pkgNode.moduleContextDataHolder != null
                && !pkgNode.moduleContextDataHolder.projectKind().equals(ProjectKind.BALA_PROJECT));
    }

    private void importWso2APIManagerCatalogModule(BLangPackage pkgNode, BLangImportPackage importDcl) {
        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        pkgNameComps.add(ASTBuilderUtil.createIdentifier(pkgNode.pos, Names.WSO2_APIM_CATALOG.value));
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.pos = pkgNode.symbol.pos;
        importDcl.orgName = ASTBuilderUtil.createIdentifier(pkgNode.pos, Names.BALLERINAX_ORG.value);
        importDcl.alias = ASTBuilderUtil.createIdentifier(pkgNode.pos, "_");
        importDcl.version = ASTBuilderUtil.createIdentifier(pkgNode.pos, "");
        importDcl.symbol = packageCache.getSymbol(serviceCatalogPkgID);
        pkgNode.imports.add(importDcl);
    }
}

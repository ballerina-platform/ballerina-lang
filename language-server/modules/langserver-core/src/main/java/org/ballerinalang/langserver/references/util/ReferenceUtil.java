/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.references.util;

import org.ballerinalang.langserver.BLangPackageContext;
import org.ballerinalang.langserver.BallerinaPackageLoader;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.references.ReferencesTreeVisitor;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.Location;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.List;
import java.util.Set;

/**
 * Utility class for find all references functionality in language server.
 */
public class ReferenceUtil {
    /**
     * Get definition position for the given definition context.
     *
     * @param referencesContext   context of the references.
     * @param bLangPackageContext package context for language server.
     * @param currentBLangPackage current package that is visiting.
     * @return position
     */
    public static List<Location> getReferences(TextDocumentServiceContext referencesContext,
                                               BLangPackageContext bLangPackageContext,
                                               BLangPackage currentBLangPackage) {
        ReferencesTreeVisitor referencesTreeVisitor = new ReferencesTreeVisitor(referencesContext);
        Set<PackageID> packages = BallerinaPackageLoader.getPackageList(referencesContext
                .get(DocumentServiceKeys.COMPILER_CONTEXT_KEY), 15);

        for (Object o : packages) {
            PackageID packageID = (PackageID) o;
            // TODO: remove the following condition after fixing the issue when loading runtime package it imports
            //       itself cause a stackOverFlow in semantic analyzer.
            if (!packageID.getName().getValue().equals("ballerina.runtime")) {
                BLangPackage bLangPackage = getPackageOfTheOwner(packageID.name, referencesContext,
                        bLangPackageContext);
                bLangPackage.accept(referencesTreeVisitor);
            }
        }

        // If the current package is default package continue.
        if (currentBLangPackage.symbol.pkgID.name.getValue().equals(".")) {
            currentBLangPackage.accept(referencesTreeVisitor);
        }

        return referencesContext.get(NodeContextKeys.REFERENCE_NODES_KEY);
    }

    /**
     * Get the package of the owner of given node.
     *
     * @param packageName         name of the package
     * @param referencesContext   context for the references
     * @param bLangPackageContext package context of language server
     * @return ballerina language package
     */
    private static BLangPackage getPackageOfTheOwner(Name packageName, TextDocumentServiceContext referencesContext,
                                                     BLangPackageContext bLangPackageContext) {
        return bLangPackageContext
                .getPackageByName(referencesContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY), packageName);
    }
}

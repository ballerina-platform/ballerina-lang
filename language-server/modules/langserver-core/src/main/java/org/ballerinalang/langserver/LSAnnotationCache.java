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
package org.ballerinalang.langserver;

import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.compiler.LSPackageCache;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.model.AttachmentPoint;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.AttachPoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Annotation cache for Language server.
 * 
 * Note: Annotation cache should be synced with the LS Package Cache
 * 
 * @since 0.970.0
 */
public class LSAnnotationCache {

    private static final Logger logger = LoggerFactory.getLogger(LSPackageCache.class);

    private static HashMap<PackageID, List<BAnnotationSymbol>> serviceAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> resourceAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> functionAnnotations = new HashMap<>();
    private static LSAnnotationCache lsAnnotationCache = null;
    
    private LSAnnotationCache() {
    }
    
    public static LSAnnotationCache getInstance() {
        return lsAnnotationCache;
    }
    
    public static synchronized void initiate() {
        if (lsAnnotationCache == null) {
            lsAnnotationCache = new LSAnnotationCache();
            CompilerContext context = LSContextManager.getInstance().getBuiltInPackagesCompilerContext();
            new Thread(() -> {
                Map<String, BPackageSymbol> packages = loadPackagesMap(context);
                loadAnnotations(new ArrayList<>(packages.values()));
            }).start();
        }
    }

    private static Map<String, BPackageSymbol> loadPackagesMap(CompilerContext tempCompilerContext) {
        Map<String, BPackageSymbol> staticPackages = new HashMap<>();

        // Annotation cache will only load the sk packages initially and the others will load in the runtime
        for (BallerinaPackage sdkPackage : LSPackageLoader.getSdkPackages()) {
            PackageID packageID = new PackageID(new org.wso2.ballerinalang.compiler.util.Name(sdkPackage.getOrgName()),
                    new org.wso2.ballerinalang.compiler.util.Name(sdkPackage.getPackageName()), Names.DEFAULT_VERSION);
            try {
                // We will wrap this with a try catch to prevent LS crashing due to compiler errors.
                BPackageSymbol bPackageSymbol = LSPackageLoader.getPackageSymbolById(tempCompilerContext, packageID);
                staticPackages.put(bPackageSymbol.pkgID.toString(), bPackageSymbol);
            } catch (Exception e) {
                logger.warn("Error while loading package :" + sdkPackage.getPackageName());
            }
        }

        return staticPackages;
    }
    
    private static void loadAnnotations(List<BPackageSymbol> packageList) {
        packageList.forEach(LSAnnotationCache::loadAnnotationsFromPackage);
    }
    
    private static void addAttachment(BAnnotationSymbol bAnnotationSymbol,
                                      HashMap<PackageID, List<BAnnotationSymbol>> map, PackageID packageID) {
        if (map.containsKey(packageID)) {
            map.get(packageID).add(bAnnotationSymbol);
            return;
        }
        map.put(packageID, new ArrayList<>(Collections.singletonList(bAnnotationSymbol)));
    }

    public HashMap<PackageID, List<BAnnotationSymbol>> getServiceAnnotations() {
        return serviceAnnotations;
    }

    public HashMap<PackageID, List<BAnnotationSymbol>> getResourceAnnotations() {
        return resourceAnnotations;
    }

    public HashMap<PackageID, List<BAnnotationSymbol>> getFunctionAnnotations() {
        return functionAnnotations;
    }

    /**
     * Get the annotation map for the given type.
     * @param attachmentPoint   Attachment point
     * @return {@link HashMap}  Map of annotation lists
     */
    public HashMap<PackageID, List<BAnnotationSymbol>> getAnnotationMapForType(AttachmentPoint attachmentPoint) {
        HashMap<PackageID, List<BAnnotationSymbol>> annotationMap;
        if (attachmentPoint == null) {
            // TODO: Here return the common annotations
            annotationMap = new HashMap<>();
        } else {
            switch (attachmentPoint) {
                case SERVICE:
                    annotationMap = serviceAnnotations;
                    break;
                case RESOURCE:
                    annotationMap = resourceAnnotations;
                    break;
                case FUNCTION:
                    annotationMap = functionAnnotations;
                    break;
                default:
                    annotationMap = new HashMap<>();
                    break;
            }
        }

        return annotationMap;
    }

    /**
     * Check whether annotations have been loaded from a given package.
     * @param packageID         Package ID to check against
     * @return {@link Boolean}  whether annotations are loaded or not
     */
    public static boolean containsAnnotationsForPackage(PackageID packageID) {
        return containsPackageWithBvmAlias(packageID, serviceAnnotations)
                || containsPackageWithBvmAlias(packageID, resourceAnnotations)
                || containsPackageWithBvmAlias(packageID, functionAnnotations);
    }

    /**
     * Load annotations from the package.
     * @param bPackageSymbol      BLang Package Symbol to load annotations
     */
    public static void loadAnnotationsFromPackage(BPackageSymbol bPackageSymbol) {
        List<Scope.ScopeEntry> scopeEntries = extractAnnotationDefinitions(bPackageSymbol.scope.entries);

        scopeEntries.forEach(annotationEntry -> {
            if (annotationEntry.symbol instanceof BAnnotationSymbol) {
                BAnnotationSymbol annotationSymbol = ((BAnnotationSymbol) annotationEntry.symbol);
                int attachPoints = ((BAnnotationSymbol) annotationEntry.symbol).attachPoints;

                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.SERVICE)) {
                    addAttachment(annotationSymbol, serviceAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.RESOURCE)) {
                    addAttachment(annotationSymbol, resourceAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.FUNCTION)) {
                    addAttachment(annotationSymbol, functionAnnotations, bPackageSymbol.pkgID);
                }
            }
        });
    }
    
    private static boolean containsPackageWithBvmAlias(PackageID packageID, HashMap<PackageID,
            List<BAnnotationSymbol>> map) {
        return map.entrySet().stream()
                .filter(entry -> entry.getKey().toString().equals(packageID.toString()))
                .findFirst()
                .orElse(null) != null;
    }
    
    private static List<Scope.ScopeEntry> extractAnnotationDefinitions(Map<Name, Scope.ScopeEntry> scopeEntries) {
        return scopeEntries.entrySet().stream().map(entry -> {
            if (entry.getValue().symbol.kind == SymbolKind.ANNOTATION) {
                return entry.getValue();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}

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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.compiler.LSPackageCache;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
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
import org.wso2.ballerinalang.util.AttachPoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Annotation cache for Language server.
 * <p>
 * Note: Annotation cache should be synced with the LS Package Cache
 *
 * @since 0.970.0
 */
public class LSAnnotationCache {

    private static final Logger logger = LoggerFactory.getLogger(LSPackageCache.class);

    private static HashMap<PackageID, List<BAnnotationSymbol>> typeAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> classAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> objectAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> functionAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> objectMethodAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> resourceAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> parameterAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> returnAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> serviceAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> listenerAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> annotationAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> externalAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> varAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> constAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> workerAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> fieldAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> recordFieldAnnotations = new HashMap<>();
    private static HashMap<PackageID, List<BAnnotationSymbol>> objectFieldAnnotations = new HashMap<>();
    private static LSAnnotationCache lsAnnotationCache = null;
    private static List<PackageID> processedPackages = new ArrayList<>();

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

    private static Map<String, BPackageSymbol> loadPackagesMap(CompilerContext compilerCtx) {
        Map<String, BPackageSymbol> staticPackages = new HashMap<>();

        // Annotation cache will only load the sk packages initially and the others will load in the runtime
        for (BallerinaPackage sdkPackage : LSPackageLoader.getSdkPackages()) {
            PackageID packageID = new PackageID(new org.wso2.ballerinalang.compiler.util.Name(sdkPackage.getOrgName()),
                    new Name(sdkPackage.getPackageName()),
                    new Name(sdkPackage.getVersion()));
            try {
                // We will wrap this with a try catch to prevent LS crashing due to compiler errors.
                Optional<BPackageSymbol> bPackageSymbol = LSPackageLoader.getPackageSymbolById(compilerCtx, packageID);
                if (!bPackageSymbol.isPresent()) {
                    continue;
                }
                staticPackages.put(bPackageSymbol.get().pkgID.toString(), bPackageSymbol.get());
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

    /**
     * Get the annotation map for the given type.
     *
     * @param attachmentPoint Attachment point based on the syntax kind of the attached node
     * @param ctx             LSContext
     * @return {@link HashMap}  Map of annotation lists
     */
    public HashMap<PackageID, List<BAnnotationSymbol>> getAnnotationMapForType(SyntaxKind attachmentPoint,
                                                                               LSContext ctx) {
        // TODO: Add service method definition, handle individual and rest params
        CompilerContext compilerCtx = ctx.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);

        // Check whether the imported packages in the current bLang package has been already processed
        ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY).getImports()
                .forEach(importPackage -> {
                    if (importPackage.symbol != null && !isPackageProcessed(importPackage.symbol.pkgID)
                            && !importPackage.symbol.pkgID.getName().getValue().equals("runtime")) {
                        Optional<BPackageSymbol> pkgSymbol = LSPackageLoader.getPackageSymbolById(compilerCtx,
                                importPackage.symbol.pkgID);
                        pkgSymbol.ifPresent(LSAnnotationCache::loadAnnotationsFromPackage);
                    }
                });
        switch (attachmentPoint) {
            case SERVICE_DECLARATION:
            case SERVICE_CONSTRUCTOR_EXPRESSION:
                return serviceAnnotations;
            case RESOURCE_KEYWORD:
                return resourceAnnotations;
            case EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION:
            case IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION:
            case FUNCTION_DEFINITION:
                return functionAnnotations;
            case METHOD_DECLARATION:
            case OBJECT_METHOD_DEFINITION:
                HashMap<PackageID, List<BAnnotationSymbol>> methodDefAnnotations = new HashMap<>();
                methodDefAnnotations.putAll(functionAnnotations);
                methodDefAnnotations.putAll(objectMethodAnnotations);
                return methodDefAnnotations;
            case LISTENER_DECLARATION:
                return listenerAnnotations;
            case NAMED_WORKER_DECLARATION:
            case START_ACTION:
                return workerAnnotations;
            case CONST_DECLARATION:
            case ENUM_MEMBER:
                return constAnnotations;
            case ENUM_DECLARATION:
            case TYPE_CAST_PARAM:
            case TYPE_DEFINITION:
                return typeAnnotations;
            case CLASS_DEFINITION:
                return classAnnotations;
            case RETURN_TYPE_DESCRIPTOR:
                return returnAnnotations;
            case OBJECT_FIELD:
                HashMap<PackageID, List<BAnnotationSymbol>> objFieldAnnotations = new HashMap<>();
                objFieldAnnotations.putAll(fieldAnnotations);
                objFieldAnnotations.putAll(objectFieldAnnotations);
                return objFieldAnnotations;
            case RECORD_FIELD:
            case RECORD_FIELD_WITH_DEFAULT_VALUE:
                HashMap<PackageID, List<BAnnotationSymbol>> recFieldAnnotations = new HashMap<>();
                recFieldAnnotations.putAll(fieldAnnotations);
                recFieldAnnotations.putAll(recordFieldAnnotations);
                return recFieldAnnotations;
            case MODULE_VAR_DECL:
            case LOCAL_VAR_DECL:
            case LET_VAR_DECL:
                return varAnnotations;
            case EXTERNAL_FUNCTION_BODY:
                return externalAnnotations;
            case ANNOTATION_DECLARATION:
                return annotationAnnotations;
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
                return parameterAnnotations;
            default:
                return new HashMap<>();
        }
    }

    /**
     * Get the annotations in the module when given the alias.
     * 
     * @param context Language server context
     * @param alias module alias
     * @param attachmentPoint attachment point
     * @return {@link Map} of annotations
     */
    public Map<PackageID, List<BAnnotationSymbol>> getAnnotationsInModule(LSContext context, String alias,
                                                                          SyntaxKind attachmentPoint) {
        HashMap<PackageID, List<BAnnotationSymbol>> annotations = getAnnotationMapForType(attachmentPoint, context);
        return annotations.entrySet().stream()
                .filter(entry -> {
                    PackageID moduleId = entry.getKey();
                    List<Name> nameComps = moduleId.getNameComps();

                    return nameComps.get(nameComps.size() - 1).getValue().equals(alias);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get all annotations.
     *
     * @return {@link List} list of all annotations in the cache
     */
    public List<BAnnotationSymbol> getAnnotations() {
        List<BAnnotationSymbol> annotations = new ArrayList<>();
        typeAnnotations.values().forEach(annotations::addAll);
        objectAnnotations.values().forEach(annotations::addAll);
        functionAnnotations.values().forEach(annotations::addAll);
        objectMethodAnnotations.values().forEach(annotations::addAll);
        resourceAnnotations.values().forEach(annotations::addAll);
        parameterAnnotations.values().forEach(annotations::addAll);
        returnAnnotations.values().forEach(annotations::addAll);
        serviceAnnotations.values().forEach(annotations::addAll);
        listenerAnnotations.values().forEach(annotations::addAll);
        annotationAnnotations.values().forEach(annotations::addAll);
        externalAnnotations.values().forEach(annotations::addAll);
        varAnnotations.values().forEach(annotations::addAll);
        constAnnotations.values().forEach(annotations::addAll);

        return annotations;
    }

    /**
     * Load annotations from the package.
     *
     * @param bPackageSymbol BLang Package Symbol to load annotations
     */
    private static void loadAnnotationsFromPackage(BPackageSymbol bPackageSymbol) {
        List<Scope.ScopeEntry> scopeEntries = extractAnnotationDefinitions(bPackageSymbol.scope.entries);

        scopeEntries.forEach(annotationEntry -> {
            if (annotationEntry.symbol instanceof BAnnotationSymbol
                    && ((annotationEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC)) {
                BAnnotationSymbol annotationSymbol = ((BAnnotationSymbol) annotationEntry.symbol);
                int attachPoints = ((BAnnotationSymbol) annotationEntry.symbol).maskedPoints;

                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.TYPE)) {
                    addAttachment(annotationSymbol, typeAnnotations, bPackageSymbol.pkgID);
                    addAttachment(annotationSymbol, objectAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.OBJECT)) {
                    addAttachment(annotationSymbol, objectAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.FUNCTION)) {
                    addAttachment(annotationSymbol, functionAnnotations, bPackageSymbol.pkgID);
                    addAttachment(annotationSymbol, objectMethodAnnotations, bPackageSymbol.pkgID);
                    addAttachment(annotationSymbol, resourceAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.OBJECT_METHOD)) {
                    addAttachment(annotationSymbol, objectMethodAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.RESOURCE)) {
                    addAttachment(annotationSymbol, resourceAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.PARAMETER)) {
                    addAttachment(annotationSymbol, parameterAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.RETURN)) {
                    addAttachment(annotationSymbol, returnAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.SERVICE)) {
                    addAttachment(annotationSymbol, serviceAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.LISTENER)) {
                    addAttachment(annotationSymbol, listenerAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.ANNOTATION)) {
                    addAttachment(annotationSymbol, annotationAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.EXTERNAL)) {
                    addAttachment(annotationSymbol, externalAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.VAR)) {
                    addAttachment(annotationSymbol, varAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.CONST)) {
                    addAttachment(annotationSymbol, constAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.WORKER)) {
                    addAttachment(annotationSymbol, workerAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.FIELD)) {
                    addAttachment(annotationSymbol, fieldAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.OBJECT_FIELD)) {
                    addAttachment(annotationSymbol, objectFieldAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.RECORD_FIELD)) {
                    addAttachment(annotationSymbol, recordFieldAnnotations, bPackageSymbol.pkgID);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.CLASS)) {
                    addAttachment(annotationSymbol, classAnnotations, bPackageSymbol.pkgID);
                }
            }
        });

        processedPackages.add(bPackageSymbol.pkgID);
    }

    private static List<Scope.ScopeEntry> extractAnnotationDefinitions(Map<Name, Scope.ScopeEntry> scopeEntries) {
        return scopeEntries.entrySet().stream()
                .filter(entry -> entry.getValue().symbol.kind == SymbolKind.ANNOTATION)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private boolean isPackageProcessed(PackageID packageID) {
        return processedPackages
                .stream()
                .anyMatch(processedPkgId -> processedPkgId.toString().equals(packageID.toString()));
    }
}

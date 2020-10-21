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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.runtime.util.Flags;
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

    private static final Map<ModuleID, List<AnnotationSymbol>> typeAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> classAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> objectAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> functionAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> objectMethodAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> resourceAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> parameterAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> returnAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> serviceAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> listenerAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> annotationAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> externalAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> varAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> constAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> workerAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> fieldAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> recordFieldAnnotations = new HashMap<>();
    private static final Map<ModuleID, List<AnnotationSymbol>> objectFieldAnnotations = new HashMap<>();
    private static final List<PackageID> processedPackages = new ArrayList<>();

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

    /**
     * Get the annotation map for the given type.
     *
     * @param attachmentPoint Attachment point based on the syntax kind of the attached node
     * @param ctx             LSContext
     * @return {@link HashMap}  Map of annotation lists
     */
    public Map<ModuleID, List<AnnotationSymbol>> getAnnotationMapForSyntaxKind(SyntaxKind attachmentPoint,
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
                HashMap<ModuleID, List<AnnotationSymbol>> methodDefAnnotations = new HashMap<>();
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
                HashMap<ModuleID, List<AnnotationSymbol>> objFieldAnnotations = new HashMap<>();
                objFieldAnnotations.putAll(fieldAnnotations);
                objFieldAnnotations.putAll(objectFieldAnnotations);
                return objFieldAnnotations;
            case RECORD_FIELD:
            case RECORD_FIELD_WITH_DEFAULT_VALUE:
                HashMap<ModuleID, List<AnnotationSymbol>> recFieldAnnotations = new HashMap<>();
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
     * @param context         Language server context
     * @param alias           module alias
     * @param attachmentPoint attachment point
     * @return {@link Map} of annotations
     */
    public Map<ModuleID, List<AnnotationSymbol>> getAnnotationsInModule(LSContext context, String alias,
                                                                        SyntaxKind attachmentPoint) {
        Map<ModuleID, List<AnnotationSymbol>> annotations = getAnnotationMapForSyntaxKind(attachmentPoint, context);
        return annotations.entrySet().stream()
                .filter(entry -> entry.getKey().modulePrefix().equals(alias))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get all annotations.
     *
     * @return {@link List} list of all annotations in the cache
     */
    public List<AnnotationSymbol> getAnnotations() {
        List<AnnotationSymbol> annotations = new ArrayList<>();
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
                    addAttachment(annotationSymbol, typeAnnotations);
                    addAttachment(annotationSymbol, objectAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.OBJECT)) {
                    addAttachment(annotationSymbol, objectAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.FUNCTION)) {
                    addAttachment(annotationSymbol, functionAnnotations);
                    addAttachment(annotationSymbol, objectMethodAnnotations);
                    addAttachment(annotationSymbol, resourceAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.OBJECT_METHOD)) {
                    addAttachment(annotationSymbol, objectMethodAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.RESOURCE)) {
                    addAttachment(annotationSymbol, resourceAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.PARAMETER)) {
                    addAttachment(annotationSymbol, parameterAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.RETURN)) {
                    addAttachment(annotationSymbol, returnAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.SERVICE)) {
                    addAttachment(annotationSymbol, serviceAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.LISTENER)) {
                    addAttachment(annotationSymbol, listenerAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.ANNOTATION)) {
                    addAttachment(annotationSymbol, annotationAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.EXTERNAL)) {
                    addAttachment(annotationSymbol, externalAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.VAR)) {
                    addAttachment(annotationSymbol, varAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.CONST)) {
                    addAttachment(annotationSymbol, constAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.WORKER)) {
                    addAttachment(annotationSymbol, workerAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.FIELD)) {
                    addAttachment(annotationSymbol, fieldAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.OBJECT_FIELD)) {
                    addAttachment(annotationSymbol, objectFieldAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.RECORD_FIELD)) {
                    addAttachment(annotationSymbol, recordFieldAnnotations);
                }
                if (Symbols.isAttachPointPresent(attachPoints, AttachPoints.CLASS)) {
                    addAttachment(annotationSymbol, classAnnotations);
                }
            }
        });

        processedPackages.add(bPackageSymbol.pkgID);
    }

    private static List<Scope.ScopeEntry> extractAnnotationDefinitions(Map<Name, Scope.ScopeEntry> scopeEntries) {
        return scopeEntries.values().stream()
                .filter(scopeEntry -> scopeEntry.symbol.kind == SymbolKind.ANNOTATION)
                .collect(Collectors.toList());
    }

    private boolean isPackageProcessed(PackageID packageID) {
        return processedPackages
                .stream()
                .anyMatch(processedPkgId -> processedPkgId.toString().equals(packageID.toString()));
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
                if (bPackageSymbol.isEmpty()) {
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
                                      Map<ModuleID, List<AnnotationSymbol>> map) {
        AnnotationSymbol annotationSymbol = SymbolFactory.createAnnotationSymbol(bAnnotationSymbol);
        // TODO: Check the map contains is valid
        if (map.containsKey(annotationSymbol.moduleID())) {
            map.get(annotationSymbol.moduleID()).add(annotationSymbol);
            return;
        }
        map.put(annotationSymbol.moduleID(), new ArrayList<>(Collections.singletonList(annotationSymbol)));
    }
}

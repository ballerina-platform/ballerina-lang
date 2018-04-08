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

import org.ballerinalang.model.AttachmentPoint;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Annotation cache for Language server.
 * 
 * Note: Annotation cache should be synced with the LS Package Cache
 * 
 * @since 0.970.0
 */
public class LSAnnotationCache {
    
    private final HashMap<PackageID, List<BLangAnnotation>> serviceAnnotations = new HashMap<>();
    private HashMap<PackageID, List<BLangAnnotation>> resourceAnnotations = new HashMap<>();
    private HashMap<PackageID, List<BLangAnnotation>> functionAnnotations = new HashMap<>();
    private static LSAnnotationCache lsAnnotationCache = null;
    
    private LSAnnotationCache() {
    }
    
    public static LSAnnotationCache getInstance() {
        if (lsAnnotationCache == null) {
            initiate();
        }
        return lsAnnotationCache;
    }
    
    static synchronized void initiate() {
        if (lsAnnotationCache == null) {
            lsAnnotationCache = new LSAnnotationCache();
            lsAnnotationCache.loadAnnotations(LSPackageCache.getInstance().getPackageMap().values()
                    .stream().collect(Collectors.toList()));
        }
    }
    
    private void loadAnnotations(List<BLangPackage> packageList) {
        packageList.forEach(bLangPackage -> {
            bLangPackage.getAnnotations().forEach(bLangAnnotation -> {
                bLangAnnotation.getAttachmentPoints().forEach(attachmentPoint -> {
                    switch (attachmentPoint.attachmentPoint) {
                        case SERVICE:
                            this.addAttachment(bLangAnnotation, serviceAnnotations, bLangPackage.packageID);
                            break;
                        case RESOURCE:
                            this.addAttachment(bLangAnnotation, resourceAnnotations, bLangPackage.packageID);
                            break;
                        case FUNCTION:
                            this.addAttachment(bLangAnnotation, functionAnnotations, bLangPackage.packageID);
                            break;
                        default:
                            break;
                    }
                });
            });
        });
    }
    
    private void addAttachment(BLangAnnotation bLangAnnotation, HashMap<PackageID, List<BLangAnnotation>> map,
                               PackageID packageID) {
        if (map.containsKey(packageID)) {
            map.get(packageID).add(bLangAnnotation);
            return;
        }
        map.put(packageID, new ArrayList<>(Collections.singletonList(bLangAnnotation)));
    }

    public HashMap<PackageID, List<BLangAnnotation>> getServiceAnnotations() {
        return serviceAnnotations;
    }

    public HashMap<PackageID, List<BLangAnnotation>> getResourceAnnotations() {
        return resourceAnnotations;
    }

    public HashMap<PackageID, List<BLangAnnotation>> getFunctionAnnotations() {
        return functionAnnotations;
    }

    /**
     * Get the annotation map for the given type.
     * @param attachmentPoint   Attachment point
     * @return {@link HashMap}  Map of annotation lists
     */
    public HashMap<PackageID, List<BLangAnnotation>> getAnnotationMapForType(AttachmentPoint attachmentPoint) {
        HashMap<PackageID, List<BLangAnnotation>> annotationMap;
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
}

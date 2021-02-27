/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.core.model;

import org.wso2.ballerinalang.compiler.tree.BLangResource;

import java.util.List;

/**
 * {@code Parameter} represent a Parameter in various signatures.
 * <p>
 * This can be a part of {@link Function}, {@link BLangResource}
 * or {@link Action} signature
 *
 * @since 0.8.0
 */
public class ParameterDef extends SimpleVariableDef implements Node {
    private List<AnnotationAttachment> annotations;

    /**
     * Get all the Annotations attached to this parameter.
     *
     * @return List of annotation attachments
     */
    public AnnotationAttachment[] getAnnotations() {
        return this.annotations.toArray(new AnnotationAttachment[annotations.size()]);
    }

    /**
     * Add an {@code AnnotationAttachment} to the Argument.
     *
     * @param annotation Annotation to be added to the Argument
     */
    public void addAnnotation(AnnotationAttachment annotation) {
        annotations.add(annotation);
    }
}

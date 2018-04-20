/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.model;

import java.util.ArrayList;

/**
 * Documentable node for Annotations.
 */
public class AnnotationDoc extends Documentable {
    public final boolean isAnnotation;
    public final String attachments;
    public final String dataType;
    public final String href;

    /**
     * Constructor.
     *
     * @param name        annotation name.
     * @param description description.
     * @param dataType    data type if any.
     * @param href        link to data type.
     * @param attachments attachment points of this annotation.
     */
    public AnnotationDoc(String name, String description, String dataType, String href, String attachments) {
        super(name, "fw-annotation", description, new ArrayList<>());
        this.dataType = dataType;
        this.href = href;
        this.attachments = attachments;
        isAnnotation = true;
    }
}

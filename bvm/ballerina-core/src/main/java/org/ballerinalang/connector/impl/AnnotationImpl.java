/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Struct;

/**
 * {@code BAnnotation} This is the implementation for the {@code Annotation} API.
 *
 * @since 0.94
 */
public class AnnotationImpl implements Annotation {
    private String name;
    private String pkgPath;
    private StructImpl value;

    AnnotationImpl(String name, String pkgPath, StructImpl value) {
        this.name = name;
        this.pkgPath = pkgPath;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return pkgPath + ":" + name;
    }

    @Override
    public Struct getValue() {
        return value;
    }

    @Override
    @Deprecated
    public AnnAttrValue getAnnAttrValue(String attributeName) {
        return null;
    }

    @Deprecated
    public void addAnnotationValue(String attributeName, AnnAttrValue annotationValue) {
    }
}

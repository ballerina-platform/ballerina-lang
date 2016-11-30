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

package org.wso2.ballerina.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent an Argument which can be a part of Function, Resource and Action definition
 */
public class Argument {

    private String name, value;
    private List<Annotation> annotations;

    /**
     * @param name  Name of the Argument
     * @param value Value of the Argument
     */
    public Argument(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get name of the Argument
     *
     * @return name of the Argument
     */
    public String getName() {
        return name;
    }

    /**
     * Get the value of the Argument
     *
     * @return value of the Argument
     */
    public String getValue() {
        return value;
    }

    /**
     * Get list of Annotation of the Argument
     *
     * @return list of Annotations related to an Argument
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * Set Annotation to an Argument
     *
     * @param annotations list of Annotations to assigned to the Argument
     */
    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * Add an Annotation to the Argument
     *
     * @param annotation Annotation to be added to the Argument
     */
    public void addAnnotation(Annotation annotation) {
        if (annotations == null) {
            annotations = new ArrayList<Annotation>();
        }
        annotations.add(annotation);
    }

}

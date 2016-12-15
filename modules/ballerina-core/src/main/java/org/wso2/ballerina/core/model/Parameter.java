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

package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.types.TypeC;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Parameter} represent a Parameter in various signatures.
 *
 * This can be a part of {@link Function}, {@link Resource}
 * or {@link BallerinaAction} signature
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Parameter implements Node {

    private Type type;
    private TypeC typeC;
    private SymbolName name;
    private List<Annotation> annotations;

    /**
     * @param type Type of the Argument
     * @param name  Name of the Argument
     */
    public Parameter(Type type, SymbolName name) {
        this.type = type;
        this.name = name;
    }

    public Parameter(TypeC typeC, SymbolName name) {
        this.typeC = typeC;
        this.name = name;
    }

    /**
     * Get connectorName of the Argument
     *
     * @return connectorName of the Argument
     */
    public SymbolName getName() {
        return name;
    }

    /**
     * Get the {@code Type} of the Argument
     *
     * @return type of the Argument
     */
    public Type getType() {
        return type;
    }

    public TypeC getTypeC() {
        return typeC;
    }

    /**
     * Get the {@code Annotation} list of the Argument
     *
     * @return list of Annotations related to an Argument
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * Set {@code Annotation} list to an Argument
     *
     * @param annotations list of Annotations to assigned to the Argument
     */
    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * Add an {@code Annotation} to the Argument
     *
     * @param annotation Annotation to be added to the Argument
     */
    public void addAnnotation(Annotation annotation) {
        if (annotations == null) {
            annotations = new ArrayList<Annotation>();
        }
        annotations.add(annotation);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}

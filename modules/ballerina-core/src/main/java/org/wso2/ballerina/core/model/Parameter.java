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

import org.wso2.ballerina.core.model.types.BType;

import java.util.List;

/**
 * {@code Parameter} represent a Parameter in various signatures.
 * <p>
 * This can be a part of {@link Function}, {@link Resource}
 * or {@link Action} signature
 *
 * @since 0.8.0
 */
public class Parameter implements Node {

    private BType type;
    private SymbolName name;
    private List<Annotation> annotations;
    protected NodeLocation nodeLocation;

    public Parameter(BType type, SymbolName name) {
        this(null, type, name);
    }

    public Parameter(NodeLocation location, BType type, SymbolName name) {
        this.type = type;
        this.name = name;
        this.nodeLocation = location;
    }

    /**
     * Get CONNECTOR_NAME of the Argument.
     *
     * @return CONNECTOR_NAME of the Argument
     */
    public SymbolName getName() {
        return name;
    }

    /**
     * Get the {@code Type} of the Argument.
     *
     * @return type of the Argument
     */
    public BType getType() {
        return type;
    }

    /**
     * Get the {@code Annotation} list of the Argument.
     *
     * @return list of Annotations related to an Argument
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * Set {@code Annotation} list to an Argument.
     *
     * @param annotations list of Annotations to assigned to the Argument
     */
    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Get the source location of this parameter.
     * Return the source file and the line number of this parameter.
     *
     * @return Source location of this parameter
     */
    public NodeLocation getNodeLocation() {
        return nodeLocation;
    }

    /**
     * Set the source location of this parameter.
     *
     * @param location Source location of this parameter.
     */
    public void setNodeLocation(NodeLocation location) {
        this.nodeLocation = location;
    }
}

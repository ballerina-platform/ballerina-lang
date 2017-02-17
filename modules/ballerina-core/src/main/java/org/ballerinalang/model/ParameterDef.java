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

package org.ballerinalang.model;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Parameter} represent a Parameter in various signatures.
 * <p>
 * This can be a part of {@link Function}, {@link Resource}
 * or {@link Action} signature
 *
 * @since 0.8.0
 */
public class ParameterDef extends VariableDef implements Node {
    private List<Annotation> annotations;

    public ParameterDef(BType type, SymbolName symbolName) {
        super(null, null, null, null, null);
        this.type = type;
        this.symbolName = symbolName;
    }

    public ParameterDef(NodeLocation location,
                        String name,
                        SimpleTypeName typeName,
                        SymbolName symbolName,
                        SymbolScope symbolScope) {

        super(location, name, typeName, symbolName, symbolScope);
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * Add an {@code Annotation} to the Argument.
     *
     * @param annotation Annotation to be added to the Argument
     */
    public void addAnnotation(Annotation annotation) {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        annotations.add(annotation);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}

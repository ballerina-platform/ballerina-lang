/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model.types;

import org.wso2.ballerina.core.model.TypeConvertor;

import java.util.function.Function;

import static org.wso2.ballerina.core.model.types.TypeConstants.NATIVE_PACKAGE;

/**
 * One edge in the Type Lattice graph
 */
public class TypeEdge {
    private TypeVertex source, target;
    private Function typeConvertorFunction;
    private TypeConvertor typeConvertor;
    private String packageName;

    public TypeEdge(TypeVertex source, TypeVertex target, Function typeConvertorFunction) {
        this.source = source;
        this.target = target;
        this.typeConvertorFunction = typeConvertorFunction;
        this.packageName = NATIVE_PACKAGE;
    }

    public TypeEdge(TypeVertex source, TypeVertex target, TypeConvertor typeConvertor, String packageName) {
        this.source = source;
        this.target = target;
        this.typeConvertor = typeConvertor;
        this.packageName = packageName;
    }

    public TypeVertex getSource() {
        return source;
    }

    public void setSource(TypeVertex source) {
        this.source = source;
    }

    public TypeVertex getTarget() {
        return target;
    }

    public void setTarget(TypeVertex target) {
        this.target = target;
    }

    public Function getTypeConvertorFunction() {
        return typeConvertorFunction;
    }

    public TypeConvertor getTypeConvertor() {
        return typeConvertor;
    }

    public void setTypeConvertor(TypeConvertor typeConvertor) {
        this.typeConvertor = typeConvertor;
    }

    /**
     * @return String A String representation of this Edge
     */
    public String toString() {
        return "({" + source.toString() + ", " + target.toString() + "}" + ": " + packageName + ")";
    }

    /**
     * @return int The hash code for this Edge
     */
    public int hashCode() {
        return (source.toString() + target.toString() + packageName).hashCode();
    }

    /**
     * @param other The Object to compare against this
     * @return ture iff other is an Edge with the same Vertices as this
     */
    public boolean equals(Object other) {
        if (!(other instanceof TypeEdge)) {
            return false;
        }

        TypeEdge e = (TypeEdge) other;
        if (typeConvertorFunction != null) {
            return e.source.equals(this.source) && e.target.equals(this.target)
                    && e.packageName.equals(this.packageName)
                    && e.typeConvertorFunction.equals(this.typeConvertorFunction);
        } else {
            return e.source.equals(this.source) && e.target.equals(this.target)
                    && e.packageName.equals(this.packageName)
                    && e.typeConvertor.equals(this.typeConvertor);
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}

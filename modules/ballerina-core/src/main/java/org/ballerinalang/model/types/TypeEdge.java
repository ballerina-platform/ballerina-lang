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
package org.ballerinalang.model.types;

import org.ballerinalang.model.TypeMapper;

import java.util.function.Function;

/**
 * One edge in the Type Lattice graph
 */
public class TypeEdge {
    private TypeVertex source, target;
    private Function typeMapperFunction;
    private TypeMapper typeMapper;
    private String packageName;

    public TypeEdge(TypeVertex source, TypeVertex target, Function typeMapperFunction) {
        this.source = source;
        this.target = target;
        this.typeMapperFunction = typeMapperFunction;
        this.packageName = TypeConstants.NATIVE_PACKAGE;
    }

    public TypeEdge(TypeVertex source, TypeVertex target, TypeMapper typeMapper, String packageName) {
        this.source = source;
        this.target = target;
        this.typeMapper = typeMapper;
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

    public Function getTypeMapperFunction() {
        return typeMapperFunction;
    }

    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    public void setTypeMapper(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
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
        if (typeMapperFunction != null) {
            return e.source.equals(this.source) && e.target.equals(this.target)
                    && e.packageName.equals(this.packageName)
                    && e.typeMapperFunction.equals(this.typeMapperFunction);
        } else {
            return e.source.equals(this.source) && e.target.equals(this.target)
                    && e.packageName.equals(this.packageName)
                    && e.typeMapper.equals(this.typeMapper);
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}

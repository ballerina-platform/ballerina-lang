/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types.definition;

import io.ballerina.types.SemType;

import java.util.Objects;

/**
 * Represent a record field in a type-descriptor.
 *
 * @since 2201.10.0
 */
public final class Field {
    private final String name;
    private final SemType ty;
    private final boolean ro;
    private final boolean opt;

    /**
     *
     */
    public Field(String name, SemType ty, boolean ro, boolean opt) {
        this.name = name;
        this.ty = ty;
        this.ro = ro;
        this.opt = opt;
    }

    public String name() {
        return name;
    }

    public SemType ty() {
        return ty;
    }

    public boolean ro() {
        return ro;
    }

    public boolean opt() {
        return opt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (Field) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.ty, that.ty) &&
                this.ro == that.ro &&
                this.opt == that.opt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ty, ro, opt);
    }

    @Override
    public String toString() {
        return "Field[" +
                "name=" + name + ", " +
                "ty=" + ty + ", " +
                "ro=" + ro + ", " +
                "opt=" + opt + ']';
    }

}

/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.TypeId;
import io.ballerina.runtime.api.types.TypeIdSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent Ballerina distinct types type-ids in the runtime.
 *
 * @since 2.0
 */
public class BTypeIdSet implements TypeIdSet {
    List<TypeId> ids;

    public BTypeIdSet() {
        this.ids = new ArrayList<>();
    }

    public BTypeIdSet(List<TypeId> ids) {
        this.ids = ids;
    }

    public void add(Module pkg, String name, boolean isPrimary) {
        ids.add(new BTypeId(pkg, name, isPrimary));
    }

    public boolean containsAll(BTypeIdSet other) {
        if (other == null) {
            return true;
        }
        // All items in other set is present in current.
        for (TypeId id : other.ids) {
            boolean found = false;
            for (TypeId otherTypeId : ids) {
                if (id.getName().equals(otherTypeId.getName()) && id.getPkg().equals(otherTypeId.getPkg())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<TypeId> getIds() {
        return ids;
    }

    /**
     * Represent Ballerina distinct type id.
     *
     * @since 2.0
     */
    public static class BTypeId implements TypeId {
        final Module pkg;
        final String name;
        final boolean isPrimary;

        public BTypeId(Module pkg, String name, boolean isPrimary) {
            this.pkg = pkg;
            this.name = name;
            this.isPrimary = isPrimary;
        }

        @Override
        public int hashCode() {
            return pkg.hashCode() * 31 + name.hashCode() + (isPrimary ? 0 : 1);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof BTypeId that) {
                return this.name.equals(that.name) && this.pkg.equals(that.pkg);
            }
            return false;
        }

        @Override
        public Module getPkg() {
            return pkg;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isPrimary() {
            return isPrimary;
        }
    }
}

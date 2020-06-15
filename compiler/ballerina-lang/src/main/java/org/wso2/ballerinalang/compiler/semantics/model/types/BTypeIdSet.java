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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.elements.PackageID;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent type id set of distinct types.
 *
 * @since 2.0
 */
public class BTypeIdSet {
    public final Set<BTypeId> primary;
    public final Set<BTypeId> secondary;
    private Set<BTypeId> all = null;

    private static final Set<BTypeId> emptySet = Collections.unmodifiableSet(new HashSet<>());
    private static final BTypeIdSet empty = new BTypeIdSet(emptySet);

    public BTypeIdSet(Set<BTypeId> primary, Set<BTypeId> secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public BTypeIdSet(Set<BTypeId> primary) {
        this.primary = primary;
        this.secondary = emptySet;
    }

    public static BTypeIdSet from(PackageID packageID, String typeId, boolean publicId) {
        HashSet<BTypeId> set = new HashSet<>();
        set.add(new BTypeId(packageID, typeId, publicId));
        return new BTypeIdSet(set);
    }

    public static BTypeIdSet from(PackageID packageID, String typeId, boolean publicId, BTypeIdSet secondary) {
        HashSet<BTypeId> primarySet = new HashSet<>();
        primarySet.add(new BTypeId(packageID, typeId, publicId));

        HashSet<BTypeId> secondarySet = new HashSet<>(secondary.primary);
        secondarySet.addAll(secondary.secondary);

        return new BTypeIdSet(primarySet, secondarySet);
    }

    public static BTypeIdSet emptySet() {
        return empty;
    }

    public boolean isAssignableFrom(BTypeIdSet sourceTypeIdSet) {
        if (sourceTypeIdSet == null) {
            return false;
        }
        if (sourceTypeIdSet == this) {
            return true;
        }

        if (all == null) {
            HashSet<BTypeId> tAll = new HashSet<>(primary);
            tAll.addAll(secondary);
            all = tAll;
        }

        if (sourceTypeIdSet.all == null) {
            HashSet<BTypeId> tAll = new HashSet<>(sourceTypeIdSet.primary);
            tAll.addAll(sourceTypeIdSet.secondary);
            sourceTypeIdSet.all = tAll;
        }

        return sourceTypeIdSet.all.containsAll(all);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BTypeIdSet)) {
            return false;
        }

        BTypeIdSet that = (BTypeIdSet) obj;
        return this.primary.equals(that.primary) && this.secondary.equals(that.secondary);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (BTypeId bTypeId : primary) {
            hashCode = hashCode * 31 + bTypeId.hashCode();
        }
        for (BTypeId bTypeId : secondary) {
            hashCode = hashCode * 31 + bTypeId.hashCode();
        }
        return hashCode;
    }

    public boolean isEmpty() {
        if (primary == emptySet && secondary == emptySet) {
            return true;
        }
        return primary.isEmpty() && secondary.isEmpty();
    }

    /**
     * Represent Ballerina type id.
     *
     * @since 2.0
     */
    public static class BTypeId {
        public final PackageID packageID;
        public final String name;
        public final boolean publicId;

        public BTypeId(PackageID packageID, String name, boolean publicId) {
            this.packageID = packageID;
            this.name = name;
            this.publicId = publicId;
        }

        @Override
        public int hashCode() {
            return this.packageID.hashCode() * 31 + this.name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof BTypeId) {
                BTypeId that = (BTypeId) obj;
                return this.name.equals(that.name) && this.packageID.equals(that.packageID);
            }
            return false;
        }

        @Override
        public String toString() {
            return "BTypeId { " + packageID.toString() + "/" + name + "}";
        }
    }
}

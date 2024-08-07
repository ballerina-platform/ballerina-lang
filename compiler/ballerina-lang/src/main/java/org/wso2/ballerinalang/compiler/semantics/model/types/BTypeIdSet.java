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
import org.wso2.ballerinalang.compiler.util.CompilerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Represent type id set of distinct types.
 *
 * @since 2.0
 */
public class BTypeIdSet {
    private final List<Set<BTypeId>> primary;
    private final List<Set<BTypeId>> secondary;

    // cache
    private Set<BTypeId> primaryAll = null;
    private Set<BTypeId> secondaryAll = null;
    private Set<BTypeId> all = null;

    public BTypeIdSet(Set<BTypeId> primary, Set<BTypeId> secondary) {
        this();
        this.primary.add(primary);
        this.secondary.add(secondary);
    }

    public BTypeIdSet() {
        this.primary = new ArrayList<>();
        this.secondary = new ArrayList<>();
    }

    public static BTypeIdSet from(PackageID packageID, String typeId, boolean publicId) {
        HashSet<BTypeId> set = new HashSet<>();
        set.add(new BTypeId(packageID, typeId, publicId));

        BTypeIdSet typeIdSet = new BTypeIdSet();
        typeIdSet.addPrimarySet(set);
        return typeIdSet;
    }

    public static BTypeIdSet from(PackageID packageID, String typeId, boolean publicId, BTypeIdSet secondary) {
        BTypeIdSet newSet = from(packageID, typeId, publicId);
        newSet.secondary.addAll(secondary.primary);
        newSet.secondary.addAll(secondary.secondary);
        return newSet;
    }

    public BTypeIdSet addPrimarySet(Set<BTypeId> set) {
        this.primary.add(set);
        invalidateCache();
        return this;
    }

    public BTypeIdSet addSecondarySet(Set<BTypeId> set) {
        this.secondary.add(set);
        invalidateCache();
        return this;
    }

    public BTypeIdSet add(BTypeIdSet that) {
        this.primary.addAll(that.primary);
        this.secondary.addAll(that.secondary);
        invalidateCache();
        return this;
    }

    public static BTypeIdSet emptySet() {
        return new BTypeIdSet();
    }

    public Set<BTypeId> getPrimary() {
        if (this.primaryAll == null) {
            HashSet<BTypeId> set = new HashSet<>();
            for (Set<BTypeId> bTypeIds : primary) {
                set.addAll(bTypeIds);
            }
            this.primaryAll = Collections.unmodifiableSet(set);
        }
        return this.primaryAll;
    }

    public Set<BTypeId> getSecondary() {
        if (this.secondaryAll == null) {
            HashSet<BTypeId> set = new HashSet<>();
            for (Set<BTypeId> bTypeIds : secondary) {
                set.addAll(bTypeIds);
            }
            this.secondaryAll = Collections.unmodifiableSet(set);
        }
        return this.secondaryAll;
    }

    public Set<BTypeId> getAll() {
        if (this.all == null) {
            HashSet<BTypeId> all = new HashSet<>();
            all.addAll(getPrimary());
            all.addAll(getSecondary());
            this.all = all;
        }
        return this.all;
    }

    private void invalidateCache() {
        this.all = null;
        this.primaryAll = null;
        this.secondaryAll = null;
    }

    public static BTypeIdSet getIntersection(BTypeIdSet lhsTypeIds, BTypeIdSet rhsTypeIds) {
        if (lhsTypeIds.isEmpty() && rhsTypeIds.isEmpty()) {
            return emptySet();
        }

        BTypeIdSet typeIdSet = new BTypeIdSet();
        typeIdSet.primary.addAll(lhsTypeIds.primary);
        typeIdSet.primary.addAll(rhsTypeIds.primary);
        typeIdSet.secondary.addAll(lhsTypeIds.secondary);
        typeIdSet.secondary.addAll(rhsTypeIds.secondary);
        return typeIdSet;
    }

    public boolean isAssignableFrom(BTypeIdSet sourceTypeIdSet) {
        if (sourceTypeIdSet == null) {
            return false;
        }
        if (sourceTypeIdSet == this) {
            return true;
        }

        return sourceTypeIdSet.getAll().containsAll(getAll());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BTypeIdSet that)) {
            return false;
        }

        return this.primary.equals(that.primary) && this.secondary.equals(that.secondary);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (BTypeId bTypeId : getPrimary()) {
            hashCode = hashCode * 31 + bTypeId.hashCode();
        }
        for (BTypeId bTypeId : getSecondary()) {
            hashCode = hashCode * 31 + bTypeId.hashCode();
        }
        return hashCode;
    }

    public boolean isEmpty() {
        return getAll().isEmpty();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (BTypeId id : getAll()) {
            joiner.add(id.toString());
        }
        return joiner.toString();
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
            return CompilerUtils.getPackageIDStringWithMajorVersion(this.packageID).hashCode() * 31 +
                    this.name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof BTypeId that) {
                return this.name.equals(that.name) &&
                        CompilerUtils.getPackageIDStringWithMajorVersion(this.packageID).equals(
                                CompilerUtils.getPackageIDStringWithMajorVersion(that.packageID));
            }
            return false;
        }

        @Override
        public String toString() {
            return "BTypeId { " + packageID.toString() + "/" + name + "}";
        }
    }
}

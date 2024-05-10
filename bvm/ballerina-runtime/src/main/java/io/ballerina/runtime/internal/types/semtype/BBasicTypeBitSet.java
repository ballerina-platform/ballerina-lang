/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.BasicTypeBitSet;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.SemType.SemTypeHelper;
import io.ballerina.runtime.api.types.Type;

import java.util.HashMap;
import java.util.Map;

public final class BBasicTypeBitSet implements BasicTypeBitSet {

    private final int all;
    private final BTypeAdapter adapter;

    private final static Map<Integer, BBasicTypeBitSet> cache = new HashMap<>();

    private BBasicTypeBitSet(int all) {
        this.all = all;
        this.adapter = new BTypeAdapter(this);
    }

    public static BasicTypeBitSet from(int all) {
        return cache.computeIfAbsent(all, BBasicTypeBitSet::new);
    }

    @Override
    public <V> V getZeroValue() {
        return adapter.getZeroValue();
    }

    @Override
    public <V> V getEmptyValue() {
        return adapter.getEmptyValue();
    }

    @Override
    public int getTag() {
        return adapter.getTag();
    }

    @Override
    public boolean isNilable() {
        return adapter.isNilable();
    }

    @Override
    public String getName() {
        return adapter.getName();
    }

    @Override
    public String getQualifiedName() {
        return adapter.getQualifiedName();
    }

    @Override
    public Module getPackage() {
        return adapter.getPackage();
    }

    @Override
    public boolean isPublic() {
        return adapter.isPublic();
    }

    @Override
    public boolean isNative() {
        return adapter.isNative();
    }

    @Override
    public boolean isAnydata() {
        return adapter.isAnydata();
    }

    @Override
    public boolean isPureType() {
        return adapter.isPureType();
    }

    @Override
    public boolean isReadOnly() {
        return adapter.isReadOnly();
    }

    @Override
    public long getFlags() {
        return adapter.getFlags();
    }

    @Override
    public Type getImmutableType() {
        return adapter.getImmutableType();
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        adapter.setImmutableType(immutableType);
    }

    @Override
    public Module getPkg() {
        return adapter.getPkg();
    }

    @Override
    public int all() {
        return all;
    }

    @Override
    public String toString() {
        return SemTypeHelper.stringRepr(this);
    }
}

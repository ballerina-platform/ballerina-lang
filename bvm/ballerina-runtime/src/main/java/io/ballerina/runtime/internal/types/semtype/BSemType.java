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
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.SemType.SemType;
import io.ballerina.runtime.api.types.SemType.SubType;
import io.ballerina.runtime.api.types.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BSemType implements SemType {

    private final int all;
    private final int some;
    // TODO: subTypeData is a sparse list to make iteration simple, when we have an efficient implementation fix this
    private final List<SubType> subTypeData;
    private final BTypeAdapter adapter;

    private BSemType(int all, int some, List<SubType> subTypeData) {
        this.all = all;
        this.some = some;
        this.subTypeData = toSparseList(some, subTypeData);
        adapter = new BTypeAdapter(this);
    }

    private static List<SubType> toSparseList(int some, List<SubType> subTypeData) {
        if (some == 0) {
            return List.of();
        }
        List<SubType> sparse = new ArrayList<>(subTypeData.size());
        int index = 0;
        int code = 0;
        while (index < subTypeData.size()) {
            if ((some & (1 << code)) != 0) {
                sparse.add(subTypeData.get(index));
                index++;
            } else {
                sparse.add(null);
            }
            code++;
        }
        return sparse;
    }

    public static SemType from(int all, int some, List<SubType> subTypeData) {
        if (some == 0) {
            return BBasicTypeBitSet.from(all);
        }
        return new BSemType(all, some, subTypeData);
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
    public int some() {
        return some;
    }

    @Override
    public List<SubType> subTypeData() {
        return Collections.unmodifiableList(subTypeData);
    }
}

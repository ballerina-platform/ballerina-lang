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
package io.ballerina.types;

import java.util.Arrays;
import java.util.Objects;

/**
 * This is to represent a SemType belonging to cell basic type.
 *
 * @since 2201.10.0
 */
public final class CellSemType implements ComplexSemType {

    private static final int SOME = PredefinedType.CELL.bitset;
    private final ProperSubtypeData[] subtypeDataList;

    private CellSemType(ProperSubtypeData[] subtypeDataList) {
        assert subtypeDataList.length == 1;
        this.subtypeDataList = subtypeDataList;
    }

    public static CellSemType from(ProperSubtypeData[] subtypeDataList) {
        return new CellSemType(subtypeDataList);
    }

    @Override
    public String toString() {
        return "CellSemType{" + subtypeDataList()[0] + '}';
    }

    @Override
    public int all() {
        return 0;
    }

    @Override
    public int some() {
        return SOME;
    }

    @Override
    public ProperSubtypeData[] subtypeDataList() {
        return subtypeDataList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ComplexSemType other)) {
            return false;
        }
        return Objects.equals(all(), other.all()) &&
                Objects.equals(some(), other.some()) &&
                Arrays.equals(subtypeDataList(), other.subtypeDataList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(all(), some(), Arrays.hashCode(subtypeDataList()));
    }

    @Override
    public boolean isSingleType() {
        return false;
    }
}

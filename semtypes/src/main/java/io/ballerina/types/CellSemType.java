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

/**
 * This is to represent a SemType belonging to cell basic type.
 *
 * @since 2201.10.0
 */
public class CellSemType implements ComplexSemType {

    // Holding on to the single value instead of the array with a single value is more memory efficient. However, if
    // this start to cause problems in the future, we can change this to an array.
    private final ProperSubtypeData subtypeData;

    private CellSemType(ProperSubtypeData[] subtypeDataList) {
        assert subtypeDataList.length == 1;
        this.subtypeData = subtypeDataList[0];
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
        return PredefinedType.CELL.bitset;
    }

    @Override
    public ProperSubtypeData[] subtypeDataList() {
        return new ProperSubtypeData[]{subtypeData};
    }
}

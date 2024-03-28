/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.types.BasicTypeCode.BT_CELL;

/**
 * ComplexSemType node.
 *
 * @since 2201.8.0
 */
public class ComplexSemType implements SemType {
    // For a basic type with code c,
    // all & (1 << c) is non-zero iff this type contains all of the basic type
    // some & (1 << c) is non-zero iff this type contains some but not all of the basic type
    public final BasicTypeBitSet all;
    public final BasicTypeBitSet some;
    // There is one member of subtypes for each bit set in some.
    // Ordered in increasing order of BasicTypeCode
    public final ProperSubtypeData[] subtypeDataList;

    ComplexSemType(BasicTypeBitSet all, BasicTypeBitSet some, ProperSubtypeData[] subtypeDataList) {
        this.all = all;
        this.some = some;
        this.subtypeDataList = subtypeDataList;
    }

    public static ComplexSemType createComplexSemType(int allBitset, BasicSubtype... subtypeList) {
        return createComplexSemType(allBitset, Arrays.asList(subtypeList));
    }

    public static ComplexSemType createComplexSemType(int allBitset, int someBitset, ProperSubtypeData[] subtypeData) {
        if (allBitset == 0 && someBitset == (1 << BT_CELL.code)) {
            return CellSemType.from(subtypeData);
        }
        return new ComplexSemType(
                BasicTypeBitSet.from(allBitset),
                BasicTypeBitSet.from(someBitset),
                subtypeData);
    }

    public static ComplexSemType createComplexSemType(int allBitset, List<BasicSubtype> subtypeList) {
        int some = 0;
        ArrayList<ProperSubtypeData> dataList = new ArrayList<>();
        for (BasicSubtype basicSubtype : subtypeList) {
            dataList.add(basicSubtype.subtypeData);
            int c = basicSubtype.basicTypeCode.code;
            some |= 1 << c;
        }
        return createComplexSemType(allBitset, some, dataList.toArray(new ProperSubtypeData[0]));
    }

    @Override
    public String toString() {
        return "ComplexSemType{all=" + all + ", some=" + some + ", subtypeDataList=" +
                Arrays.toString(subtypeDataList) + '}';
    }
}

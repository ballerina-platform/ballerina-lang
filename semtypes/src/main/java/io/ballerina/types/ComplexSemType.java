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

package io.ballerina.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.types.BasicTypeCode.BT_CELL;

public interface ComplexSemType extends SemType {

    static ComplexSemType createComplexSemType(int allBitset, BasicSubtype... subtypeList) {
        return createComplexSemType(allBitset, Arrays.asList(subtypeList));
    }

    static ComplexSemType createComplexSemType(int allBitset, int someBitset, ProperSubtypeData[] subtypeData) {
        if (allBitset == 0 && someBitset == (1 << BT_CELL.code)) {
            return CellSemType.from(subtypeData);
        }
        return new ComplexSemTypeImpl(
                BasicTypeBitSet.from(allBitset),
                BasicTypeBitSet.from(someBitset),
                subtypeData);
    }

    static ComplexSemType createComplexSemType(int allBitset, List<BasicSubtype> subtypeList) {
        int some = 0;
        List<ProperSubtypeData> dataList = new ArrayList<>(subtypeList.size());
        for (BasicSubtype basicSubtype : subtypeList) {
            dataList.add(basicSubtype.subtypeData);
            some |= 1 << basicSubtype.basicTypeCode.code;
        }
        return createComplexSemType(allBitset, some, dataList.toArray(ProperSubtypeData[]::new));
    }

    BasicTypeBitSet all();

    BasicTypeBitSet some();

    ProperSubtypeData[] subtypeDataList();
}

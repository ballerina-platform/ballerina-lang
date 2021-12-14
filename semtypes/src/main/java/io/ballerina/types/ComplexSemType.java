/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ComplexSemType node.
 *
 * @since 3.0.0
 */
public class ComplexSemType implements SemType {
    // For a uniform type with code c,
    // all & (1 << c) is non-zero iff this type contains all of the uniform type
    // some & (1 << c) is non-zero iff this type contains some but not all of the uniform type
    public final UniformTypeBitSet all;
    public final UniformTypeBitSet some;
    // There is one member of subtypes for each bit set in some.
    // Ordered in increasing order of UniformTypeCode
    public final ProperSubtypeData[] subtypeDataList;

    public ComplexSemType(UniformTypeBitSet all, UniformTypeBitSet some, ProperSubtypeData[] subtypeDataList) {
        this.all = all;
        this.some = some;
        this.subtypeDataList = subtypeDataList;
    }

    public static ComplexSemType createComplexSemType(int allBitset, UniformSubtype... subtypeList) {
        return createComplexSemType(allBitset, Arrays.asList(subtypeList));
    }

    public static ComplexSemType createComplexSemType(int allBitset, List<UniformSubtype> subtypeList) {
        int some = 0;
        ArrayList<ProperSubtypeData> dataList = new ArrayList<>();
        for (UniformSubtype uniformSubtype : subtypeList) {
            dataList.add(uniformSubtype.subtypeData);
            int c = uniformSubtype.uniformTypeCode.code;
            some |= 1 << c;
        }
        return new ComplexSemType(
                UniformTypeBitSet.from(allBitset),
                UniformTypeBitSet.from(some),
                dataList.toArray(new ProperSubtypeData[]{}));
    }
}

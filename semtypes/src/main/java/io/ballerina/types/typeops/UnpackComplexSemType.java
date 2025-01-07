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
package io.ballerina.types.typeops;

import io.ballerina.types.BasicSubtype;
import io.ballerina.types.BasicTypeCode;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.ProperSubtypeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent `unpackComplexSemType` function.
 *
 * @since 2201.8.0
 */
public class UnpackComplexSemType {
    private UnpackComplexSemType() {
    }

    public static List<BasicSubtype> unpack(ComplexSemType t) {
        int some = t.some();
        List<BasicSubtype> subtypeList = new ArrayList<>();
        for (ProperSubtypeData data : t.subtypeDataList()) {
            int code = Integer.numberOfTrailingZeros(some);
            subtypeList.add(BasicSubtype.from(BasicTypeCode.from(code), data));
            some ^= (1 << code);
        }
        return subtypeList;
    }
}

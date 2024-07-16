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
 * @param all             all & (1 << c) is non-zero iff this type contains all the basic type with code c
 * @param some            some & (1 << c) is non-zero iff this type contains some but not all the basic type with code c
 * @param subtypeDataList There is one member of subtypes for each bit set in some. Ordered in increasing order of
 *                        BasicTypeCode
 */
record ComplexSemTypeImpl(int all, int some, ProperSubtypeData[] subtypeDataList) implements ComplexSemType {

    @Override
    public String toString() {
        return "ComplexSemType{all=" + all() + ", some=" + some() + ", subtypeDataList=" +
                Arrays.toString(subtypeDataList()) + '}';
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
}

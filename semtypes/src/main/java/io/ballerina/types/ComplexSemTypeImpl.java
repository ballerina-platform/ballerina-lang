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

import java.util.Arrays;
import java.util.Objects;

/**
 * ComplexSemType node.
 *
 * @since 2201.8.0
 */
public class ComplexSemTypeImpl implements ComplexSemType {

    // For a basic type with code c,
    // all & (1 << c) is non-zero iff this type contains all of the basic type
    // some & (1 << c) is non-zero iff this type contains some but not all of the basic type
    private final int all;
    private final int some;
    // There is one member of subtypes for each bit set in some.
    // Ordered in increasing order of BasicTypeCode
    private final ProperSubtypeData[] subtypeDataList;

    ComplexSemTypeImpl(int all, int some, ProperSubtypeData[] subtypeDataList) {
        this.all = all;
        this.some = some;
        this.subtypeDataList = subtypeDataList;
    }

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

    @Override
    public int all() {
        return all;
    }

    @Override
    public int some() {
        return some;
    }

    @Override
    public ProperSubtypeData[] subtypeDataList() {
        return subtypeDataList;
    }

    @Override
    public boolean isSingleType() {
        return false;
    }
}

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

/**
 * Enumerable type wrapper for float.
 *
 * @since 2201.8.0
 */
public class EnumerableFloat implements EnumerableType {
    public final double value;

    private EnumerableFloat(double value) {
        this.value = value;
    }

    public static EnumerableFloat from(double d) {
        return new EnumerableFloat(d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EnumerableFloat e)) {
            return false;
        }

        Double v1 = e.value;
        Double v2 = this.value;
        return (v1.compareTo(v2) == 0);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
}

/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

/**
 * Enumerable type wrapper for string.
 *
 * @since 2201.12.0
 */
public class EnumerableString implements EnumerableType {
    public final String value;

    private EnumerableString(String value) {
        this.value = value;
    }

    public static EnumerableString from(String v) {
        return new EnumerableString(v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EnumerableString e)) {
            return false;
        }
        return (e.value.equals(this.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

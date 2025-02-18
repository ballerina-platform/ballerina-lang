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

import io.ballerina.types.subtypedata.Range;

/**
 * Represents a combined range.
 *
 * @param range range
 * @param i1    i1
 * @param i2    i2
 * @since 2201.12.0
 */
public record CombinedRange(Range range, Long i1, Long i2) {

    public static CombinedRange from(Range range, Long i1, Long i2) {
        return new CombinedRange(range, i1, i2);
    }
}

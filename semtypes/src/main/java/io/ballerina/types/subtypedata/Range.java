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
package io.ballerina.types.subtypedata;

/**
 * Int Range node.
 *
 * @since 2201.8.0
 */
public class Range {
    public final long min;
    public final long max;

    public Range(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public static Range from(long min, long max) {
        return new Range(min, max);
    }

    @Override
    public String toString() {
        return "Range[" + min + ", " + max + "]";
    }
}

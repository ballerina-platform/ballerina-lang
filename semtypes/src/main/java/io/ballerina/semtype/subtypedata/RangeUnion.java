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
package io.ballerina.semtype.subtypedata;

/**
 * Holds a range if there is a single range representing the union/intersect of r1 and r1.
 * status -1 means union/intersect is empty because r1 is before r2, with no overlap
 * status 1 means union/intersect is empty because r2 is before r1 with no overlap
 * Precondition r1 and r2 are non-empty.
 *
 * @since 2.0.0
 */
public class RangeUnion {
    public final int status; // -1, 1, default to zero when there is a range
    public final Range range;

    private RangeUnion(int status, Range range) {
        this.status = status;
        this.range = range;
    }

    public static RangeUnion from(int status) {
        return new RangeUnion(status, null);
    }

    public static RangeUnion from(Range range) {
        return new RangeUnion(0, range);
    }
}

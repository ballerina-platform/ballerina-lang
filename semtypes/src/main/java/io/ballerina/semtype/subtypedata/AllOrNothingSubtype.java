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

import io.ballerina.semtype.SubtypeData;

/**
 * A subtype representing either all subtypes or nothing.
 * This is the Java representation of the `boolean` found in `SubtypeData` type in Ballerina impl.
 *
 * @since 2.0.0
 */
public class AllOrNothingSubtype implements SubtypeData {
    private final boolean isAll;

    private AllOrNothingSubtype(boolean isAll) {
        this.isAll = isAll;
    }

    public static AllOrNothingSubtype createAll() {
        return new AllOrNothingSubtype(true);
    }

    public static AllOrNothingSubtype createNothing() {
        return new AllOrNothingSubtype(false);
    }

    public boolean isAllSubtype() {
        return this.isAll;
    }

    public boolean isNothingSubtype() {
        return !this.isAll;
    }
}

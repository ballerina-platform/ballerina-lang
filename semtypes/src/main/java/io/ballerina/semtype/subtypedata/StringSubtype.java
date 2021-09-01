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

import io.ballerina.semtype.EnumerableString;
import io.ballerina.semtype.EnumerableSubtype;
import io.ballerina.semtype.ProperSubtypeData;
import io.ballerina.semtype.SubtypeData;

/**
 * Represent StringSubtype.
 *
 * @since 2.0.0
 */
public class StringSubtype extends EnumerableSubtype implements ProperSubtypeData {
    public final boolean allowed;
    public final EnumerableString[] values;

    private StringSubtype(boolean allowed, EnumerableString[] values) {
        this.allowed = allowed;
        this.values = values;
    }

    public static boolean stringSubtypeContains(SubtypeData d, String s) {
        if (d instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d).isAllSubtype();
        }
        StringSubtype v = (StringSubtype) d;

        boolean found = false;
        for (EnumerableString value : v.values) {
            if (value.value.equals(s)) {
                found = true;
                break;
            }
        }

        return found ? v.allowed : !v.allowed;
    }

    public static SubtypeData createStringSubtype(boolean allowed, EnumerableString[] values) {
        if (values.length == 0) {
            return new AllOrNothingSubtype(!allowed);
        }
        return new StringSubtype(allowed, values);
    }
}

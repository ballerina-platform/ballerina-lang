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

package org.ballerinalang.nativeimpl.jvm.tests;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BTypedescType;

public class ValidateFieldAnnotations {

    public static boolean testDerived1(BTypedesc value) {
        return assertAnnotatedFields(value, "x", "y");
    }

    public static boolean testDerived2(BTypedesc value) {
        return assertAnnotatedFields(value, "x", "y", "z");
    }

    public static boolean testDerived3(BTypedesc value) {
        return assertAnnotatedFields(value, "x", "y", "a");
    }

    public static boolean testOverride1(BTypedesc value) {
        return assertAnnotatedFields(value);
    }

    private static boolean assertAnnotatedFields(BTypedesc td, String... fields) {
        BAnnotatableType recordType = (BAnnotatableType) ((BTypedescType) td.getType()).getConstraint();
        BMap<BString, Object> annoations = recordType.getAnnotations();
        Set<String> annoatatedFields =
                Arrays.stream(annoations.getKeys()).map(BString::getValue).collect(Collectors.toSet());
        if (annoatatedFields.size() != fields.length) {
            return false;
        }
        for (String field : fields) {
            if (!annoatatedFields.contains("$field$." + field)) {
                return false;
            }
        }
        return true;
    }
}

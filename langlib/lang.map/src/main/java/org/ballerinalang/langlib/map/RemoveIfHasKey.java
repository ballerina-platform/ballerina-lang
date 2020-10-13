/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langlib.map;

import io.ballerina.jvm.api.BErrorCreator;
import io.ballerina.jvm.api.BStringUtils;
import io.ballerina.jvm.api.values.BMap;
import io.ballerina.jvm.api.values.BString;

import static io.ballerina.jvm.MapUtils.checkIsMapOnlyOperation;
import static org.ballerinalang.langlib.map.util.MapLibUtils.validateRequiredFieldForRecord;

/**
 * Extern function to remove element from the map if key exists.
 * ballerina.model.map:removeIfHasKey(string)
 *
 * @since 1.2.0
 */
public class RemoveIfHasKey {

    public static Object removeIfHasKey(BMap<?, ?> m, BString k) {
        String op = "removeIfHasKey()";

        checkIsMapOnlyOperation(m.getType(), op);
        validateRequiredFieldForRecord(m, k.getValue());
        try {
            return m.remove(k);
        } catch (io.ballerina.jvm.util.exceptions.BLangFreezeException e) {
            throw BErrorCreator.createError(BStringUtils.fromString(e.getMessage()),
                                            BStringUtils.fromString("Failed to remove element: " + e.getDetail()));
        }
    }
}

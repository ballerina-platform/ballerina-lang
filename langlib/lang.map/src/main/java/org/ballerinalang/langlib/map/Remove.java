/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.jvm.MapUtils;
import io.ballerina.jvm.api.BErrorCreator;
import io.ballerina.jvm.api.BStringUtils;
import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.api.values.BString;
import io.ballerina.jvm.values.MapValue;

import static io.ballerina.jvm.MapUtils.checkIsMapOnlyOperation;
import static io.ballerina.jvm.util.exceptions.BallerinaErrorReasons.MAP_KEY_NOT_FOUND_ERROR;
import static org.wso2.ballerinalang.compiler.util.Constants.REMOVE;

/**
 * Extern function to remove element from the map.
 * ballerina.model.map:remove(string)
 */
public class Remove {

    public static Object remove(MapValue<?, ?> m, BString k) {
        Type type = m.getType();

        checkIsMapOnlyOperation(type, REMOVE);
        MapUtils.validateRequiredFieldForRecord(m, k.getValue());
        if (m.containsKey(k)) {
            try {
                return m.remove(k);
            } catch (io.ballerina.jvm.util.exceptions.BLangFreezeException e) {
                throw BErrorCreator.createError(BStringUtils.fromString(e.getMessage()),
                                                BStringUtils.fromString(
                                                        "Failed to remove element from map: " + e.getDetail()));
            }
        }

        throw BErrorCreator.createError(MAP_KEY_NOT_FOUND_ERROR, BStringUtils
                .fromString("cannot find key '" + k + "'"));
    }
}

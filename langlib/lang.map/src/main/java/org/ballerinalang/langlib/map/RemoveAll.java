/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.map;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.errors.ErrorHelper;

import static io.ballerina.runtime.internal.MapUtils.checkIsMapOnlyOperation;
import static org.ballerinalang.langlib.map.util.MapLibUtils.validateRecord;

/**
 * ENative implementation of lang.map:removeAll(map&lt;Type&gt;).
 *
 * @since 1.0
 */
public class RemoveAll {

    public static void removeAll(BMap<?, ?> m) {
        checkIsMapOnlyOperation(TypeUtils.getReferredType(m.getType()), "removeAll()");
        validateRecord(m);
        try {
            m.clear();
        } catch (BError e) {
            String errorMsgDetail = "Failed to clear map";
            if (ErrorHelper.hasMessageDetail(e)) {
                errorMsgDetail += ": " +
                        ((BMap<BString, Object>) e.getDetails()).get(StringUtils.fromString("message")).toString();
            }
            throw ErrorCreator.createError(e.getErrorMessage(), StringUtils.fromString(errorMsgDetail));
        }
    }
}

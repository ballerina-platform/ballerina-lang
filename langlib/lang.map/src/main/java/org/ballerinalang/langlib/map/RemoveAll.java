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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import static org.ballerinalang.jvm.MapUtils.checkIsMapOnlyOperation;
import static org.ballerinalang.jvm.MapUtils.validateRecord;

/**
 * ENative implementation of lang.map:removeAll(map&lt;Type&gt;).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.map", functionName = "removeAll",
        args = {@Argument(name = "m", type = TypeKind.MAP)},
        isPublic = true
)
public class RemoveAll {

    public static void removeAll(Strand strand, MapValue<?, ?> m) {
        checkIsMapOnlyOperation(m.getType(), "removeAll()");
        validateRecord(m);
        try {
            m.clear();
        } catch (org.ballerinalang.jvm.util.exceptions.BLangFreezeException e) {
            throw BallerinaErrors.createError(e.getMessage(), "Failed to clear map: " + e.getDetail());
        }
    }
}

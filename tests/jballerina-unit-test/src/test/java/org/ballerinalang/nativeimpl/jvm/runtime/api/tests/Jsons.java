/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.jvm.runtime.api.tests;

import io.ballerina.runtime.api.types.StructureType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.values.ErrorValue;

import static io.ballerina.runtime.api.utils.JsonUtils.convertJSONToRecord;

/**
 * This class contains a set of utility methods required for @{@link io.ballerina.runtime.api.utils.JsonUtils} testing.
 *
 * @since 2201.0.0
 */
public class Jsons {

    public static BMap<BString, Object> testConvertJSONToRecord(Object record, BTypedesc t) throws BError {
        Type describingType = t.getDescribingType();
        if (describingType instanceof StructureType) {
            return convertJSONToRecord(record, (StructureType) describingType);
        } else {
            throw new ErrorValue(StringUtils.fromString("provided typedesc does not describe a record type."));
        }
    }

    public static BString convertJSONToString(Object jValue) {
        return StringUtils.fromString(StringUtils.getJsonString(jValue));
    }
}

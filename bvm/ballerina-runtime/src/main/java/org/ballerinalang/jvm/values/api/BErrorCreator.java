/*
*   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.jvm.values.api;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypeIdSet;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

/**
 * Util Class for handling Error in Ballerina VM.
 *
 * @since 0.995.0
 */
public class BErrorCreator {

    private static final BString ERROR_MESSAGE_FIELD = StringUtils.fromString("message");

    public static BError createError(BString message) {
        return new ErrorValue(message, new MapValueImpl<>(BTypes.typeErrorDetail));
    }

    public static BError createError(BString reason, BString detail) {
        MapValueImpl<BString, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
        if (detail != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, detail);
        }
        return new ErrorValue(reason, detailMap);
    }

    public static BError createError(BString message, Throwable throwable) {
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage()),
                              message, createError(StringUtils.fromString(throwable.getMessage())),
                              new MapValueImpl<>(BTypes.typeErrorDetail));
    }

    public static BError createError(BType type, BString message, BError cause, Object details) {
        return new ErrorValue(type, message, cause, details);
    }

    public static BError createError(BType type, BString message, BString detail) {
        MapValueImpl<BString, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
        if (detail != null) {
            detailMap.put(ERROR_MESSAGE_FIELD, detail);
        }
        return new ErrorValue(type, message, null, detailMap);
    }

    public static BError createError(BString message, BMap detailMap) {
        return new ErrorValue(message, detailMap);
    }

    public static BError createError(Throwable error) {
        if (error instanceof BError) {
            return (BError) error;
        }
        return createError(StringUtils.fromString(error.getMessage()));
    }

    public static BError createDistinctError(String typeIdName, BPackage typeIdPkg, BString message) {
        return createDistinctError(typeIdName, typeIdPkg, message, new MapValueImpl<>(BTypes.typeErrorDetail));
    }

    public static BError createDistinctError(String typeIdName, BPackage typeIdPkg, BString message,
                                             MapValue<BString, Object> detailRecord) {
        BError error = createError(message, detailRecord);
        setTypeId(typeIdName, typeIdPkg, error);
        return error;
    }

    public static BError createDistinctError(String typeIdName, BPackage typeIdPkg, BString message,
                                             BError cause) {
        MapValueImpl<Object, Object> details = new MapValueImpl<>(BTypes.typeErrorDetail);
        BError error = new ErrorValue(new BErrorType(TypeConstants.ERROR, BTypes.typeError.getPackage(),
                                                     TypeChecker.getType(details)), message, cause, details);
        setTypeId(typeIdName, typeIdPkg, error);
        return error;
    }

    public static void setTypeId(String typeIdName, BPackage typeIdPkg, BError error) {
        BErrorType type = (BErrorType) error.getType();
        BTypeIdSet typeIdSet = new BTypeIdSet();
        typeIdSet.add(typeIdPkg, typeIdName, true);
        type.setTypeIdSet(typeIdSet);
    }
}

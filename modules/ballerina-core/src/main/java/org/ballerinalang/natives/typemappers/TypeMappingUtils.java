/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.natives.typemappers;

import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Class with utility methods for casting and conversion.
 * 
 * @since 0.88
 */
public class TypeMappingUtils {
    
    // TODO: set this only once during semantic analyzer.
    private static StructDef castErrorStruct;
    
    /**
     * Check whether a given source type can be assigned to a destination type.
     * 
     * @param lType Destination type
     * @param rType Source Type
     * @return Flag indicating whether the given source type can be assigned to the destination type.
     */
    public static boolean isCompatible(BType lType, BType rType) {
        if (lType == rType) {
            return true;
        }

        if (lType == BTypes.typeAny) {
            return true;
        }

        if (!BTypes.isValueType(lType) && (rType == BTypes.typeNull)) {
            return true;
        }

        return false;
    }
    
    public static void setErrorStruct(StructDef error) {
        castErrorStruct = error;
    }

    public static BValue[] getError(boolean returnErrors, String errorMsg, BType sourceType, BType targetType) {
        if (returnErrors) {
            return new BValue[] { targetType.getZeroValue(), createError(errorMsg, sourceType, targetType) };
        }
        throw new BallerinaException(errorMsg);
    }
    
    private static BStruct createError(String message, BType sourceType, BType targetType) {
        BString msg = new BString(message);
        BString sourceTypeName = new BString(sourceType.getSymbolName().toString());
        BString targetTypeName = new BString(targetType.getSymbolName().toString());
        return new BStruct(castErrorStruct, new BValue[]{msg, null, sourceTypeName, targetTypeName});
    }
}

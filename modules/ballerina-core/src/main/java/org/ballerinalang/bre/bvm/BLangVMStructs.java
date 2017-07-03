/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.StructInfo;

/**
 * Util Class for handling structs in Ballerina VM.
 *
 * @since 0.89
 */
public class BLangVMStructs {

    /**
     * Create BStruct for given StructInfo and BValues.
     *
     * @param structInfo {@link StructInfo} of the BStruct
     * @param values     field values of the BStruct.
     * @return BStruct instance.
     */
    public static BStruct createBStruct(StructInfo structInfo, Object... values) {
        BStruct bStruct = new BStruct(structInfo.getType());
        bStruct.setFieldTypes(structInfo.getFieldTypes());
        bStruct.init(structInfo.getFieldCount());

        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int blobRegIndex = -1;
        int refRegIndex = -1;
        for (int i = 0; i < structInfo.getFieldTypes().length; i++) {
            BType paramType = structInfo.getFieldTypes()[i];
            if (values.length < i + 1) {
                break;
            }
            switch (paramType.getTag()) {
            case TypeTags.INT_TAG:
                ++longRegIndex;
                if (values[i] != null) {
                    if (values[i] instanceof Integer) {
                        bStruct.setIntField(longRegIndex, (Integer) values[i]);
                    } else if (values[i] instanceof Long) {
                        bStruct.setIntField(longRegIndex, (Long) values[i]);
                    }
                }
                break;
            case TypeTags.FLOAT_TAG:
                ++doubleRegIndex;
                if (values[i] != null) {
                    if (values[i] instanceof Float) {
                        bStruct.setFloatField(doubleRegIndex, (Float) values[i]);
                    } else if (values[i] instanceof Double) {
                        bStruct.setFloatField(doubleRegIndex, (Double) values[i]);
                    }
                }
                break;
            case TypeTags.STRING_TAG:
                ++stringRegIndex;
                if (values[i] != null && values[i] instanceof String) {
                    bStruct.setStringField(stringRegIndex, (String) values[i]);
                }
                break;
            case TypeTags.BOOLEAN_TAG:
                ++booleanRegIndex;
                if (values[i] != null && values[i] instanceof Boolean) {
                    bStruct.setBooleanField(booleanRegIndex, (Boolean) values[i] ? 1 : 0);
                }
                break;
            case TypeTags.BLOB_TAG:
                ++blobRegIndex;
                if (values[i] != null && values[i] instanceof BBlob) {
                    bStruct.setBlobField(blobRegIndex, ((BBlob) values[i]).blobValue());
                }
                break;
            default:
                ++refRegIndex;
                if (values[i] != null && (values[i] instanceof BRefType)) {
                    bStruct.setRefField(refRegIndex, (BRefType) values[i]);
                }
            }
        }
        return bStruct;
    }
}

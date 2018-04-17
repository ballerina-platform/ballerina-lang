/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.bre.bvm.persistency.reftypes;

import org.ballerinalang.bre.bvm.persistency.RefTypeCounter;
import org.ballerinalang.bre.bvm.persistency.SerializableState;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;

public class SerializableRefFields {

    private RefTypeIndex[] refTypeIndices;
    private SerializableBString[] bStrings;
    private String[] bStructKeys;

    public SerializableRefFields(BRefType[] refFields, SerializableState state) {
        refTypeIndices = new RefTypeIndex[refFields.length];
        RefTypeCounter refTypeCounter = new RefTypeCounter(refFields);
        bStructKeys = new String[refTypeCounter.getbStructCount()];
        bStrings = new SerializableBString[refTypeCounter.getbStringCount()];
        int refTypeIndex = 0;
        int bStructIndex = 0;
        int bStringIndex = 0;
        for (int i = 0; i < refFields.length; i++) {
            BRefType refType = refFields[i];
            if (refType instanceof BStruct) {
                bStructKeys[bStructIndex] = state.addBStruct((BStruct) refType);
                refTypeIndices[refTypeIndex] =
                        new RefTypeIndex(i, bStructIndex, RefTypeIndex.RefTypeName.BStruct);
                bStructIndex++;
                refTypeIndex++;
            } else if (refType instanceof BString) {
                bStrings[bStringIndex] = new SerializableBString((BString) refType);
                refTypeIndices[refTypeIndex] =
                        new RefTypeIndex(i, bStringIndex, RefTypeIndex.RefTypeName.BString);
                bStringIndex++;
                refTypeIndex++;
            }
        }
    }

    public BRefType[] getRefFeilds(ProgramFile programFile, SerializableState state) {
        BRefType[] refFields = new BRefType[refTypeIndices.length];
        for (int i = 0; i < refTypeIndices.length; i++) {
            RefTypeIndex refTypeIndex = refTypeIndices[i];
            if (refTypeIndex == null) {
                continue;
            }
            switch (refTypeIndex.getRefTypeName()) {
                case BStruct:
                    String bStructKey = bStructKeys[refTypeIndex.getSpecificTypeIndex()];
                    BStruct childBStruct = state.getBStruct(bStructKey, programFile);
                    refFields[refTypeIndex.getRefTypeIndex()] = childBStruct;
                    break;
                case BString:
                    BString bString = bStrings[refTypeIndex.getSpecificTypeIndex()].getBString();
                    refFields[refTypeIndex.getRefTypeIndex()] = bString;
                    break;
                default:
                    throw new BallerinaException("Unknown ref type encountered.");
            }
        }
        return refFields;
    }
}

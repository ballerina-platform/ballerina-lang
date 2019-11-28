/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.programfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a constant value.
 *
 * @since 0.990.4
 */
@Deprecated
public class ConstantValue {

    public int finiteTypeSigCPIndex = -1;
    public int valueTypeSigCPIndex = -1;

    public int recordLiteralSigCPIndex = -1;

    public boolean isSimpleLiteral;
    public boolean isConstRef;

    public int literalValueTypeTag = -1;

    public int valueCPEntryIndex = -1;

    public boolean booleanValue;

    public Map<KeyInfo, ConstantValue> constantValueMap = new HashMap<>();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConstantValue)) {
            return false;
        }

        ConstantValue constantValue = (ConstantValue) obj;

        return finiteTypeSigCPIndex == constantValue.finiteTypeSigCPIndex &&
                valueTypeSigCPIndex == constantValue.valueTypeSigCPIndex &&
                recordLiteralSigCPIndex == constantValue.recordLiteralSigCPIndex &&
                isSimpleLiteral == constantValue.isSimpleLiteral &&
                isConstRef == constantValue.isConstRef &&
                literalValueTypeTag == constantValue.literalValueTypeTag &&
                valueCPEntryIndex == constantValue.valueCPEntryIndex;
    }
}

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
package org.ballerinalang.bre.bvm.persistency;

import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;

public class RefTypeCounter {

    private int bStructCount;
    private int bStringCount;

    public RefTypeCounter(BRefType[] refTypes) {
        if (refTypes == null) {
            return;
        }

        for (BRefType refType : refTypes) {
            if (refType instanceof BStruct) {
                bStructCount++;
            } else if (refType instanceof BString) {
                bStringCount++;
            }
        }
    }

    public int getbStructCount() {
        return bStructCount;
    }

    public int getbStringCount() {
        return bStringCount;
    }
}

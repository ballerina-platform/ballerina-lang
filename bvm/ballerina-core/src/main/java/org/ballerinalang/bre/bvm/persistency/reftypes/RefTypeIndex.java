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

public class RefTypeIndex {

    public enum RefTypeName {BStruct, BString};

    /**
     * Index in the refType[] array of the BStruct object
     */
    private int refTypeIndex;

    /**
     * Index in the array specific to the particular (deserialized) type (e.g. SerializableBStruct)
     */
    private int specificTypeIndex;

    private RefTypeName refTypeName;

    public RefTypeIndex(int refTypeIndex, int specificTypeIndex, RefTypeName refTypeName) {
        this.refTypeIndex = refTypeIndex;
        this.specificTypeIndex = specificTypeIndex;
        this.refTypeName = refTypeName;
    }

    public int getRefTypeIndex() {
        return refTypeIndex;
    }

    public void setRefTypeIndex(int refTypeIndex) {
        this.refTypeIndex = refTypeIndex;
    }

    public int getSpecificTypeIndex() {
        return specificTypeIndex;
    }

    public void setSpecificTypeIndex(int specificTypeIndex) {
        this.specificTypeIndex = specificTypeIndex;
    }

    public RefTypeName getRefTypeName() {
        return refTypeName;
    }

    public void setRefTypeName(RefTypeName refTypeName) {
        this.refTypeName = refTypeName;
    }
}

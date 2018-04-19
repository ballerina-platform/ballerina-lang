/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.LockableStructureType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.VarTypeCountAttributeInfo;

/**
 * This class is used hold the global memory area of a running Ballerina program.
 *
 * @since 0.970.0
 */
public class GlobalMemoryArea {
    private final LockableStructureType[] globalMemBlock;

    public GlobalMemoryArea(PackageInfo[] packageInfoArray) {
        this.globalMemBlock = new BStruct[packageInfoArray.length];
        initGlobalMemBlock(packageInfoArray);
    }

    public long getIntField(int pkgIndex, int varIndex) {
        return globalMemBlock[pkgIndex].getIntField(varIndex);
    }


    public void setIntField(int pkgIndex, int varIndex, long value) {
        globalMemBlock[pkgIndex].setIntField(varIndex, value);
    }

    public double getFloatField(int pkgIndex, int varIndex) {
        return globalMemBlock[pkgIndex].getFloatField(varIndex);

    }

    public void setFloatField(int pkgIndex, int varIndex, double value) {
        globalMemBlock[pkgIndex].setFloatField(varIndex, value);
    }

    public String getStringField(int pkgIndex, int varIndex) {
        return globalMemBlock[pkgIndex].getStringField(varIndex);
    }

    public void setStringField(int pkgIndex, int varIndex, String value) {
        globalMemBlock[pkgIndex].setStringField(varIndex, value);
    }

    public int getBooleanField(int pkgIndex, int varIndex) {
        return globalMemBlock[pkgIndex].getBooleanField(varIndex);
    }

    public void setBooleanField(int pkgIndex, int varIndex, int value) {
        globalMemBlock[pkgIndex].setBooleanField(varIndex, value);
    }

    public byte[] getBlobField(int pkgIndex, int varIndex) {
        return globalMemBlock[pkgIndex].getBlobField(varIndex);
    }

    public void setBlobField(int pkgIndex, int varIndex, byte[] value) {
        globalMemBlock[pkgIndex].setBlobField(varIndex, value);
    }

    public BRefType getRefField(int pkgIndex, int varIndex) {
        return globalMemBlock[pkgIndex].getRefField(varIndex);
    }

    public void setRefField(int pkgIndex, int varIndex, BRefType value) {
        globalMemBlock[pkgIndex].setRefField(varIndex, value);
    }


    // private methods

    private void initGlobalMemBlock(PackageInfo[] packageInfoArray) {
        for (PackageInfo packageInfo : packageInfoArray) {
            // Get the package-level variable count from the attribute.
            AttributeInfo attributeInfo = packageInfo.getAttributeInfo(
                    AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE);
            VarTypeCountAttributeInfo varTypeCountAttribInfo = (VarTypeCountAttributeInfo) attributeInfo;
            int[] globalVarCount = varTypeCountAttribInfo.getVarTypeCount();
            // We are using the struct value to hold package-level variable values for the moment.
            BStructType dummyType = new BStructType(null, "", "", 0);
            dummyType.setFieldTypeCount(globalVarCount);
            globalMemBlock[packageInfo.pkgIndex] = new BStruct(dummyType);
        }
    }
}

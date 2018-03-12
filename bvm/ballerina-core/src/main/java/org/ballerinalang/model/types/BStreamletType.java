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
package org.ballerinalang.model.types;

import org.ballerinalang.model.values.BStreamlet;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.StreamletInfo;

/**
 * {@code BStreamletType} represents a {@code Streamlet} in Ballerina.
 *
 * @since 0.965.0
 */
public class BStreamletType extends BType {

    private int[] fieldTypeCount;
    private StreamletInfo streamletInfo;
    public int flags;

    /**
     * Create a {@code BStreamletType} which represents the Ballerina Streamlet type.
     */
    public BStreamletType(StreamletInfo streamletInfo, String typeName, String pkgPath, int flags) {
        super(typeName, pkgPath, BStreamlet.class);
        this.streamletInfo = streamletInfo;
        this.flags = flags;
    }

    public BStreamletType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BStreamlet.class);
    }

    public int[] getFieldTypeCount() {
        return fieldTypeCount;
    }

    public void setFieldTypeCount(int[] fieldTypeCount) {
        this.fieldTypeCount = fieldTypeCount;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return (V) new BStreamlet(this);
    }

    @Override
    public TypeSignature getSig() {
        String packagePath = (pkgPath == null) ? "." : pkgPath;
        return new TypeSignature(TypeSignature.SIG_STREAMLET, packagePath, typeName);
    }

    @Override
    public int getTag() {
        return TypeTags.STREAMLET_TAG;
    }
}


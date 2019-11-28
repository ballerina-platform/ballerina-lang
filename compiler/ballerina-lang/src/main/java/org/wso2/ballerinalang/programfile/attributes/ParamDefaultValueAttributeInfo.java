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
package org.wso2.ballerinalang.programfile.attributes;

import org.wso2.ballerinalang.programfile.DefaultValue;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code ParamDefaultValueAttributeInfo} contains metadata of default values for parameters of
 * a function/resource/action/connector nodes.
 *
 * @since 0.956
 */
@Deprecated
public class ParamDefaultValueAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    private int attributeNameIndex;

    private List<DefaultValue> paramDefaultValues = new ArrayList<>();

    public ParamDefaultValueAttributeInfo(int attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    public void addParamDefaultValueInfo(DefaultValue attachmentInfo) {
        paramDefaultValues.add(attachmentInfo);
    }

    public DefaultValue[] getDefaultValueInfo() {
        return paramDefaultValues.toArray(new DefaultValue[paramDefaultValues.size()]);
    }

    @Override
    public Kind getKind() {
        return Kind.PARAMETER_DEFAULTS_ATTRIBUTE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }
}

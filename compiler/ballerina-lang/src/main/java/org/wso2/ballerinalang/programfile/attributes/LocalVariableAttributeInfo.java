/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.wso2.ballerinalang.programfile.LocalVariableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code LocalVariableAttributeInfo} contains common metadata of parameters
 * of a Ballerina function/resource/action in the program file.
 *
 * @since 0.88
 */
@Deprecated
public class LocalVariableAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    public int attributeNameIndex;

    public List<LocalVariableInfo> localVars = new ArrayList<>();

    public LocalVariableAttributeInfo(int attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    @Override
    public Kind getKind() {
        return Kind.LOCAL_VARIABLES_ATTRIBUTE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }
}

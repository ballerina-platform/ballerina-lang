/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder.components;

import static org.ballerinalang.net.grpc.builder.BalGeneratorConstants.NEW_LINE_CHARACTER;

/**
 * Class that responsible of generating struct data type at .bal stub
 */
public class StructBuilder {
    private String[] attributesNameArr;
    private String[] attributesTypeArr;
    private String name;
    private String attributeList;
    
    public StructBuilder(String[] attributesNameArr, String[] attributesTypeArr, String name) {
        this.attributesNameArr = attributesNameArr;
        this.attributesTypeArr = attributesTypeArr;
        this.name = name;
        
    }
    
    private void buildAttributes() {
        
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < attributesNameArr.length; i++) {
            str.append(String.format(NEW_LINE_CHARACTER + "%s %s;", attributesTypeArr[i], attributesNameArr[i]));
        }
        attributeList = str.toString();
    }
    
    public String buildStructs() {
        buildAttributes();
        return String.format(NEW_LINE_CHARACTER +
                "struct %s {" + NEW_LINE_CHARACTER +
                "%s" + NEW_LINE_CHARACTER +
                "}" + NEW_LINE_CHARACTER, name, attributeList);
    }
}

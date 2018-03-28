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

import java.util.ArrayList;
import java.util.List;

/**
 * Bean class of type enum object.
 */
public class Enum {
    private String enumId;
    private List<EnumAttribute> enumAttribute = new ArrayList<>();
    
    public Enum(String enumId) {
        this.enumId = enumId;
    }
    
    public void addAttribute(String name) {
        enumAttribute.add(new EnumAttribute(name));
    }
    
    public String getStructId() {
        return enumId;
    }
    
    public void setStructId(String enumId) {
        this.enumId = enumId;
    }
    
    public List<EnumAttribute> getAttributesList() {
        return enumAttribute;
    }
    
    public void setAttributesList(List<EnumAttribute> attributesList) {
        this.enumAttribute = attributesList;
    }
}

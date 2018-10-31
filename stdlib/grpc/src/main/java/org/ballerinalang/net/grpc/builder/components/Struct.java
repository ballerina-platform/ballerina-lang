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
 * Bean class of type struct object.
 */
public class Struct {
    private String structId;
    private int size;
    private List<Attribute> attribute = new ArrayList<>();
    
    public Struct(String structId, int size) {
        this.structId = structId;
        this.size = size;
    }
    
    public void addAttribute(String name, String type , String label, String tag, String method) {
        attribute.add(new Attribute(name, type, label, tag, method));
    }
    
    public String getStructId() {
        return structId;
    }
    
    public void setStructId(String structId) {
        this.structId = structId;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
    
    public List<Attribute> getAttributesList() {
        return attribute;
    }
    
    public void setAttributesList(List<Attribute> attributesList) {
        this.attribute = attributesList;
    }
}

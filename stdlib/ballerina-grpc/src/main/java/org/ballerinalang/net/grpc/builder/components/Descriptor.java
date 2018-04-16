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

/**
 * Bean class of client descriptor object.
 */
public class Descriptor {
    private String descriptorKey;
    private String descriptorData;
    private String eoe;
    
    public Descriptor(String descriptorKey, String descriptorData, String eoe) {
        this.descriptorKey = descriptorKey;
        this.descriptorData = descriptorData;
        this.eoe = eoe;
    }
    
    public String getDescriptorKey() {
        return descriptorKey;
    }
    
    public void setDescriptorKey(String descriptorKey) {
        this.descriptorKey = descriptorKey;
    }
    
    public String getDescriptorData() {
        return descriptorData;
    }
    
    public void setDescriptorData(String descriptorData) {
        this.descriptorData = descriptorData;
    }
    
    public String getEoe() {
        return eoe;
    }
    
    public void setEoe(String eoe) {
        this.eoe = eoe;
    }
}

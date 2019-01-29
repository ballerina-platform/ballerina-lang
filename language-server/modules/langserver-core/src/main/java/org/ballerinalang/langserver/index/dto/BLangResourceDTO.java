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
package org.ballerinalang.langserver.index.dto;

/**
 * DTO for BLangResource.
 * 
 * @since 0.983.0
 */
public final class BLangResourceDTO {
    
    private int id;
    
    private int serviceId;
    
    private String name;

    private BLangResourceDTO(int id, int serviceId, String name) {
        this.id = id;
        this.serviceId = serviceId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getName() {
        return name;
    }

    /**
     * Builder for BLangResourceDTO.
     */
    public static class BLangResourceDTOBuilder {

        private int id;

        private int serviceId;

        private String name = "";

        public BLangResourceDTOBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public BLangResourceDTOBuilder setServiceId(int serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public BLangResourceDTOBuilder setName(String name) {
            this.name = name;
            return this;
        }
        
        public BLangResourceDTO build() {
            return new BLangResourceDTO(this.id, this.serviceId, this.name);
        }
    }
}

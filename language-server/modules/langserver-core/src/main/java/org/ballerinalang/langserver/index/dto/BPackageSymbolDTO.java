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
 * DTO for BPackageSymbol.
 * 
 * @since 0.983.0
 */
public final class BPackageSymbolDTO {
    
    private int id;
    
    private String name;
    
    private String orgName;
    
    private String version;

    private BPackageSymbolDTO(int id, String name, String orgName, String version) {
        this.id = id;
        this.name = name;
        this.orgName = orgName;
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Builder for BPackageSymbolDTO.
     */
    public static class BPackageSymbolDTOBuilder {

        private int id = -1;

        private String name = "";

        private String orgName = "";

        private String version = "";

        public BPackageSymbolDTOBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public BPackageSymbolDTOBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public BPackageSymbolDTOBuilder setOrgName(String orgName) {
            this.orgName = orgName;
            return this;
        }

        public BPackageSymbolDTOBuilder setVersion(String version) {
            this.version = version;
            return this;
        }
        
        public BPackageSymbolDTO build() {
            return new BPackageSymbolDTO(this.id, this.name, this.orgName, this.version);
        }
    }
}

/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil.connector.models.connector;

import com.google.gson.annotations.Expose;

/**
 * TypeInfo model.
 */
public class TypeInfo {
    @Expose
    public String name;
    @Expose
    public String orgName;
    @Expose
    public String moduleName;
    @Expose
    public String packageName;
    @Expose
    public String version;

    public TypeInfo(String name, String orgName, String moduleName, String packageName, String version) {
        this.name = name;
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.version = version;
    }
}

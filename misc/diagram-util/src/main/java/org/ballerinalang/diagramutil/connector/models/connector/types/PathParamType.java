/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;


/**
 * Path parameter type model.
 */
public class PathParamType {
    @Expose
    public String name;
    @Expose
    public String typeName;
    @Expose
    public boolean isRestType;

    public PathParamType() {
    }

    public PathParamType(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
        this.isRestType = false;
    }

    public PathParamType(String name, String typeName, boolean isRestType) {
        this.name = name;
        this.typeName = typeName;
        this.isRestType = isRestType;
    }
}

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

package org.ballerinalang.central.client.model;

import com.google.gson.annotations.SerializedName;

/**
 * {@code Tool} represents tool json from central.
 */
public class Tool {
    public static final String JSON_PROPERTY_ID = "id";

    @SerializedName(JSON_PROPERTY_ID) private String id;

    public static final String JSON_PROPERTY_PACKAGE = "package";
    @SerializedName(JSON_PROPERTY_PACKAGE) private Package pkg;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Package getPkg() {
        return pkg;
    }

    public void setPkg(Package pkg) {
        this.pkg = pkg;
    }
}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.model;

/**
 * Model for Balo TOML file.
 *
 * @since 1.0
 */
public class BaloToml {
    public String balo_version = "1.0.0";
    public String built_by = "WSO2";

    public String getBalo_version() {
        return balo_version;
    }

    public void setBalo_version(String balo_version) {
        this.balo_version = balo_version;
    }

    public String getBuilt_by() {
        return built_by;
    }

    public void setBuilt_by(String built_by) {
        this.built_by = built_by;
    }
}

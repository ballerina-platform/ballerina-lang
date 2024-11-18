/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects.internal.bala;

/**
 * {@code BalaJson} Model for Bala JSON file.
 *
 * @since 2.0.0
 */
public class BalaJson {
    private String bala_version = "3.0.0";
    private String built_by = "WSO2";

    public String getBala_version() {
        return bala_version;
    }

    public void setBala_version(String bala_version) {
        this.bala_version = bala_version;
    }

    public String getBuilt_by() {
        return built_by;
    }

    public void setBuilt_by(String built_by) {
        this.built_by = built_by;
    }
}

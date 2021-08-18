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

package io.ballerina.projects.internal.model;

import java.sql.Timestamp;

/**
 * {@code PackageJson} Model for build file.
 *
 * @since 2.0.0
 */
public class BuildJson {

    private Timestamp last_build_time;
    private Timestamp last_update_time;

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    public BuildJson(Timestamp last_build_time, Timestamp last_update_time) {
        this.last_build_time = last_build_time;
        this.last_update_time = last_update_time;
    }

    public Timestamp lastBuildTime() {
        return last_build_time;
    }

    public void setLastBuildTime(Timestamp last_build_time) {
        this.last_build_time = last_build_time;
    }

    public Timestamp lastUpdateTime() {
        return last_update_time;
    }

    public void setLastUpdateTime(Timestamp last_update_time) {
        this.last_update_time = last_update_time;
    }

    public boolean isExpiredLastUpdateTime() {
        long oneDayAgo = System.currentTimeMillis() - ONE_DAY;
        return lastBuildTime().getTime() < oneDayAgo;
    }
}

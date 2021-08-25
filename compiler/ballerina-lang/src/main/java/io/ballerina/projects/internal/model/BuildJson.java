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

import com.google.gson.annotations.SerializedName;

/**
 * {@code PackageJson} Model for build file.
 *
 * @since 2.0.0
 */
public class BuildJson {

    public static final String SERIALIZED_NAME_LAST_BUILD_TIME = "last_build_time";
    @SerializedName(SERIALIZED_NAME_LAST_BUILD_TIME)
    private long lastBuildTime;

    public static final String SERIALIZED_NAME_LAST_UPDATE_TIME = "last_update_time";
    @SerializedName(SERIALIZED_NAME_LAST_UPDATE_TIME)
    private long lastUpdateTime;

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    public BuildJson(long lastBuildTime, long lastUpdateTime) {
        this.lastBuildTime = lastBuildTime;
        this.lastUpdateTime = lastUpdateTime;
    }

    public long lastBuildTime() {
        return lastBuildTime;
    }

    public void setLastBuildTime(long lastBuildTime) {
        this.lastBuildTime = lastBuildTime;
    }

    public long lastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isExpiredLastUpdateTime() {
        long oneDayAgo = System.currentTimeMillis() - ONE_DAY;
        return lastUpdateTime() < oneDayAgo;
    }
}

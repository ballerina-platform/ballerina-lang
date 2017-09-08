/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.logging.util;

import java.util.List;

/**
 * A POJO for holding the log configurations read from the log.yml file
 *
 * @since 0.94
 */
public class LogConfiguration {

    private List<LoggerConfig> loggers;
    private List<PackageConfig> packages;

    public List<LoggerConfig> getLoggers() {
        return loggers;
    }

    public void setLoggers(List<LoggerConfig> loggers) {
        this.loggers = loggers;
    }

    public List<PackageConfig> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageConfig> packages) {
        this.packages = packages;
    }
}

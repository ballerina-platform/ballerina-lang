/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.util.tracer.config;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.tracer.TraceConstants;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * This is the config file loader for the global property provided by the ballerina global config.
 */
public class ConfigLoader {

    private ConfigLoader() {

    }

    public static OpenTracingConfig load() {
        String configLocation = ConfigRegistry.getInstance()
                .getConfiguration(TraceConstants.BALLERINA_TRACE_CONFIG_KEY);
        if (configLocation == null) {
            return null;
        } else {
            return load(configLocation);
        }
    }

    public static OpenTracingConfig load(String configLocation) {
        File initialFile = new File(configLocation);
        try (InputStream targetStream = new FileInputStream(initialFile)) {
            return new Yaml().loadAs(targetStream, OpenTracingConfig.class);
        } catch (Throwable ignored) {
            return null;
        }
    }
}

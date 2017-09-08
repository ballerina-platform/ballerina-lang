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

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for mapping between Ballerina log configurations and JDK log configurations
 *
 * @since 0.94
 */
public class ConfigMapper {
    private static Map<String, String> configMap;

    static {
        buildConfigMap();
    }

    public static String mapConfiguration(String balConfig) {
        String jdkConfig = configMap.get(balConfig);
        return jdkConfig != null ? jdkConfig : balConfig;
    }

    private static void buildConfigMap() {
        configMap = Collections.unmodifiableMap(Stream.of(
                new SimpleEntry<>("ballerina.runtime.level", Constants.BRELOG_FILE_HANDLER_LEVEL)
        ).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }
}

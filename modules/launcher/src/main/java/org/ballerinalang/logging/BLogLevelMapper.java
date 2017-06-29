/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.logging;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A mapper class to map log levels from JDK logging to our custom log levels
 *
 * @since 0.89
 */
public class BLogLevelMapper {

    private static final Map<Level, String> LEVEL_STRING_MAP = Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>(Level.SEVERE, "ERROR"),
            new AbstractMap.SimpleEntry<>(Level.WARNING, "WARN"),
            new AbstractMap.SimpleEntry<>(Level.INFO, "INFO"),
            new AbstractMap.SimpleEntry<>(Level.CONFIG, "INFO"),
            new AbstractMap.SimpleEntry<>(Level.FINE, "DEBUG"),
            new AbstractMap.SimpleEntry<>(Level.FINER, "DEBUG"),
            new AbstractMap.SimpleEntry<>(Level.FINEST, "TRACE"))
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

    public static String mapLevel(Level level) {
        //TODO: Rethink the undefined level value
        return LEVEL_STRING_MAP.containsKey(level) ? LEVEL_STRING_MAP.get(level) : "-";
    }
}

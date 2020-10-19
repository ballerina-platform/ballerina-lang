/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.utils;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channels;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The utility class used to execute read/write operations to a property file.
 */
public class PropertyUtils {
    private static final Logger log = LoggerFactory.getLogger(PropertyUtils.class);
    private static final MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_STRING);
    private static Map<String, Properties> propertiesMap = new HashMap<>();

    // Read a property related to a given key and return the BString value.
    public static BString readProperty(Reader reader, BString key, BString defaultValue,
                                       String propertiesId) throws IOException {
        Properties readableProperties;
        if (!propertiesMap.containsKey(propertiesId)) {
            readableProperties = new Properties();
            readableProperties.load(reader);
            propertiesMap.put(propertiesId, readableProperties);
        } else {
            readableProperties = propertiesMap.get(propertiesId);
        }
        String value = readableProperties.getProperty(key.getValue(), defaultValue.getValue());
        if (value != null) {
            return StringUtils.fromString(value);
        }

        return null;
    }

    // Read all properties and return as a ballerina map
    public static BMap<BString, Object> readAllProperties(Reader reader, String propertiesId) throws IOException {
        Properties readableProperties;
        if (!propertiesMap.containsKey(propertiesId)) {
            readableProperties = new Properties();
            readableProperties.load(reader);
            propertiesMap.put(propertiesId, readableProperties);
        } else {
            readableProperties = propertiesMap.get(propertiesId);
        }
        BMapInitialValueEntry[] keyValues = new BMapInitialValueEntry[readableProperties.stringPropertyNames().size()];
        int i = 0;
        for (Enumeration<?> e = readableProperties.propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = readableProperties.getProperty(key);
            BMapInitialValueEntry keyValue = ValueCreator.createKeyFieldEntry(StringUtils.fromString(key),
                                                                              StringUtils.fromString(value));
            keyValues[i] = keyValue;
            i++;
        }
        return ValueCreator.createMapValue(mapType, keyValues);
    }


    // Write properties map to a .properties file
    public static void writePropertyContent(CharacterChannel characterChannel,
                                                  BMap<BString, BString> propertiesMap,
                                                  BString comment) throws IOException {

        Properties writableProperties = new Properties();
        Set<Map.Entry<BString, BString>> propertiesSet = propertiesMap.entrySet();
        for (Map.Entry<BString, BString> entry : propertiesSet) {
            writableProperties.setProperty(entry.getKey().getValue(), entry.getValue().getValue());
        }
        writableProperties.store(
                Channels.newOutputStream(characterChannel.getChannel().getByteChannel()),
                comment.getValue()
        );
    }
}

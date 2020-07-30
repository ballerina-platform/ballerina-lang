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

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.MappingInitialValueEntry;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BString;
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
    private static final BMapType mapType = new BMapType(BTypes.typeString);
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
            return org.ballerinalang.jvm.StringUtils.fromString(value);
        }

        return null;
    }

    // Read all properties and return as a ballerina map
    public static MapValue<BString, BString> readAllProperties(Reader reader, String propertiesId) throws IOException {
        Properties readableProperties;
        if (!propertiesMap.containsKey(propertiesId)) {
            readableProperties = new Properties();
            readableProperties.load(reader);
            propertiesMap.put(propertiesId, readableProperties);
        } else {
            readableProperties = propertiesMap.get(propertiesId);
        }
        MappingInitialValueEntry.KeyValueEntry[] keyValues = new MappingInitialValueEntry
                .KeyValueEntry[readableProperties.stringPropertyNames().size()];
        int i = 0;
        for (Enumeration<?> e = readableProperties.propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = readableProperties.getProperty(key);
            MappingInitialValueEntry.KeyValueEntry keyValue = new MappingInitialValueEntry.KeyValueEntry(
                    StringUtils.fromString(key), StringUtils.fromString(value));
            keyValues[i] = keyValue;
            i++;
        }
        return new MapValueImpl<>(mapType, keyValues);
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

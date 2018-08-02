/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.model.fields;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Section headers valid in the manifest toml file.
 *
 * @since 0.964
 */
public enum ManifestHeader {
    PROJECT, DEPENDENCIES, PATCHES;

    private static final Map<String, ManifestHeader> LOOKUP = new HashMap<>();

    static {
        for (ManifestHeader header : ManifestHeader.values()) {
            LOOKUP.put(header.name().toLowerCase(Locale.ENGLISH), header);
        }
    }

    /**
     * Like as valueOf method, but input should be all lower case.
     *
     * @param fieldKey Lower case string value of filed to find.
     * @return Matching enum.
     */
    public static ManifestHeader valueOfLowerCase(String fieldKey) {
        return LOOKUP.get(fieldKey);
    }
}

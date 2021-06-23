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

import java.util.Locale;

/**
 * Section defined in the toml file.
 *
 * @since 0.964
 */
public enum Section {
    PROJECT, DEPENDENCIES, PATCHES, PROXY;

    /**
     * Check if the section header matches the toml header.
     *
     * @param match section header in the toml file
     * @return if it matches or not
     */
    public boolean stringEquals(String match) {
        return toString().toLowerCase(Locale.ENGLISH).equals(match);
    }
}

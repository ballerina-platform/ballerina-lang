/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.system.utils;

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

/**
 * @since 0.94.1
 */
public class SystemUtils {

    /**
     * Returns the system property which corresponds to the given key.
     *
     * @param key system property key
     * @return system property as a {@link BValue} or {@code BTypes.typeString.getZeroValue()} if the property does not
     * exist.
     */
    public static BValue getSystemProperty(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return BTypes.typeString.getZeroValue();
        }
        return new BString(value);
    }
}

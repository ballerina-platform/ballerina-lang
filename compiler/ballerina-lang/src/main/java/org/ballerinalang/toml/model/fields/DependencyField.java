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

import org.ballerinalang.toml.model.Dependency;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Dependency object fields.
 *
 * @since 0.964
 */
public enum DependencyField {
    NAME(Dependency::setPackageName),
    VERSION(Dependency::setVersion),
    LOCATION(Dependency::setLocation);

    private static final Map<String, DependencyField> LOOKUP = new HashMap<>();

    static {
        for (DependencyField dependencyField : DependencyField.values()) {
            LOOKUP.put(dependencyField.name().toLowerCase(Locale.ENGLISH), dependencyField);
        }
    }

    private final BiConsumer<Dependency, String> stringSetter;

    /**
     * Constructor which sets the string value.
     *
     * @param stringSetter string value to be set
     */
    DependencyField(BiConsumer<Dependency, String> stringSetter) {
        this.stringSetter = stringSetter;
    }

    /**
     * Like as valueOf method, but input should be all lower case.
     *
     * @param fieldKey Lower case string value of filed to find.
     * @return Matching enum.
     */
    public static DependencyField valueOfLowerCase(String fieldKey) {
        return LOOKUP.get(fieldKey);
    }

    /**
     * Set values to the dependency object.
     *
     * @param dependency dependency object
     * @param value      value to be set
     */
    public void setValueTo(Dependency dependency, String value) {
        stringSetter.accept(dependency, value);
    }
}

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


import org.ballerinalang.toml.model.Repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Fields defined in the "repositories" header.
 *
 * @since 0.970.1
 */
public enum RepositoriesField {
    DEFAULT(Repositories::setRepoOrder);

    private static final Map<String, RepositoriesField> LOOKUP = new HashMap<>();

    static {
        for (RepositoriesField packageFieldField : RepositoriesField.values()) {
            LOOKUP.put(packageFieldField.name().toLowerCase(Locale.ENGLISH).replace('_', '-'),
                       packageFieldField);
        }
    }

    private BiConsumer<Repositories, List<String>> listSetter;

    /**
     * Constructor which sets a list of strings.
     *
     * @param listSetter   list of strings
     */
    RepositoriesField(BiConsumer<Repositories, List<String>> listSetter) {
        this.listSetter = listSetter;
    }

    /**
     * Like as valueOf method, but input should be all lower case.
     *
     * @param fieldKey Lower case string value of filed to find.
     * @return Matching enum.
     */
    public static RepositoriesField valueOfLowerCase(String fieldKey) {
        return LOOKUP.get(fieldKey);
    }

    /**
     * Set the list of strings to the repositories object.
     *
     * @param repositories repositories object
     * @param list     list of strings
     */
    public void setListTo(Repositories repositories, List<String> list) {
        if (listSetter != null) {
            listSetter.accept(repositories, list);
        } else {
            throw new IllegalStateException(this + " field can't have list value " + list.toString());
        }
    }
}

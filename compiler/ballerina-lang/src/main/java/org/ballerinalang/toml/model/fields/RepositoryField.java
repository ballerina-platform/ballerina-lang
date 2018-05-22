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

import org.ballerinalang.toml.model.Repository;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Repository object fields.
 *
 * @since 0.971.1
 */
public enum RepositoryField {
    NAME(Repository::setName),
    URL(Repository::setUrl),
    ACCESSTOKEN(Repository::setAccessToken);

    private static final Map<String, RepositoryField> LOOKUP = new HashMap<>();

    static {
        for (RepositoryField repositoryField : RepositoryField.values()) {
            LOOKUP.put(repositoryField.name().toLowerCase(Locale.ENGLISH), repositoryField);
        }
    }

    private final BiConsumer<Repository, String> stringSetter;

    /**
     * Constructor which sets the string value.
     *
     * @param stringSetter string value to be set
     */
    RepositoryField(BiConsumer<Repository, String> stringSetter) {
        this.stringSetter = stringSetter;
    }

    /**
     * Like as valueOf method, but input should be all lower case.
     *
     * @param fieldKey Lower case string value of field to find.
     * @return Matching enum.
     */
    public static RepositoryField valueOfLowerCase(String fieldKey) {
        return LOOKUP.get(fieldKey);
    }

    /**
     * Set values to the repository object.
     *
     * @param repository repository object
     * @param value      value to be set
     */
    public void setValueTo(Repository repository, String value) {
        stringSetter.accept(repository, value);
    }
}

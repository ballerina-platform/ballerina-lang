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


import org.ballerinalang.toml.model.Manifest;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Fields defined in the manifest "package" header.
 *
 * @since 0.964
 */
public enum PackageField {
    ORG_NAME(Manifest::setName),
    VERSION(Manifest::setVersion),
    DESCRIPTION(Manifest::setDescription),
    DOCUMENTATION(Manifest::setDocumentationURL),
    HOMEPAGE(Manifest::setHomepageURL),
    REPOSITORY(Manifest::setRepositoryURL),
    README(Manifest::setReadmeFilePath),
    LICENSE(Manifest::setLicense),
    AUTHORS(null, Manifest::setAuthors),
    KEYWORDS(null, Manifest::setKeywords);

    private static final Map<String, PackageField> LOOKUP = new HashMap<>();

    static {
        for (PackageField packageFieldField : PackageField.values()) {
            LOOKUP.put(packageFieldField.name().toLowerCase(Locale.ENGLISH).replace('_', '-'),
                       packageFieldField);
        }
    }

    private BiConsumer<Manifest, String> stringSetter;
    private BiConsumer<Manifest, List<String>> listSetter;

    /**
     * Constructor which sets the string value.
     *
     * @param stringSetter value to be set
     */
    PackageField(BiConsumer<Manifest, String> stringSetter) {
        this(stringSetter, null);
    }

    /**
     * Constructor which sets a list of strings.
     *
     * @param stringSetter string to be set: will be always null
     * @param listSetter   list of strings
     */
    PackageField(BiConsumer<Manifest, String> stringSetter, BiConsumer<Manifest, List<String>> listSetter) {
        this.stringSetter = stringSetter;
        this.listSetter = listSetter;
    }

    /**
     * Like as valueOf method, but input should be all lower case.
     *
     * @param fieldKey Lower case string value of filed to find.
     * @return Matching enum.
     */
    public static PackageField valueOfLowerCase(String fieldKey) {
        return LOOKUP.get(fieldKey);
    }

    /**
     * Set the string value to the manifest object.
     *
     * @param manifest manifest object
     * @param value    string value
     */
    public void setStringTo(Manifest manifest, String value) {
        if (stringSetter != null) {
            stringSetter.accept(manifest, value);
        } else {
            throw new IllegalStateException(this + " field can't have string value " + value);
        }
    }

    /**
     * Set the list of strings to the manifest object.
     *
     * @param manifest manifest object
     * @param list     list of strings
     */
    public void setListTo(Manifest manifest, List<String> list) {
        if (listSetter != null) {
            listSetter.accept(manifest, list);
        } else {
            throw new IllegalStateException(this + " field can't have list value " + list.toString());
        }
    }
}

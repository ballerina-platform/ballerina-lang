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


import org.ballerinalang.toml.model.LockFile;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Fields defined in the LockFile "project" header.
 *
 * @since 0.973.1
 */
public enum LockFileField {
    NAME(LockFile::setName),
    VERSION(LockFile::setVersion),
    LOCKFILEVERSION(LockFile::setLockfileversion),
    BALLERINAVERSION(LockFile::setBallerinaVersion),
    MODULES(null, LockFile::setPackages);

    private static final Map<String, LockFileField> LOOKUP = new HashMap<>();

    static {
        for (LockFileField packageFieldField : LockFileField.values()) {
            LOOKUP.put(packageFieldField.name().toLowerCase(Locale.ENGLISH).replace('_', '-'),
                       packageFieldField);
        }
    }

    private BiConsumer<LockFile, String> stringSetter;
    private BiConsumer<LockFile, List<String>> listSetter;

    /**
     * Constructor which sets the string value.
     *
     * @param stringSetter value to be set
     */
    LockFileField(BiConsumer<LockFile, String> stringSetter) {
        this(stringSetter, null);
    }

    /**
     * Constructor which sets a list of strings.
     *
     * @param stringSetter string to be set: will be always null
     * @param listSetter   list of strings
     */
    LockFileField(BiConsumer<LockFile, String> stringSetter, BiConsumer<LockFile, List<String>> listSetter) {
        this.stringSetter = stringSetter;
        this.listSetter = listSetter;
    }

    /**
     * Like as valueOf method, but input should be all lower case.
     *
     * @param fieldKey Lower case string value of filed to find.
     * @return Matching enum.
     */
    public static LockFileField valueOfLowerCase(String fieldKey) {
        return LOOKUP.get(fieldKey);
    }

    /**
     * Set the string value to the LockFile object.
     *
     * @param lockFile  object
     * @param value    string value
     */
    public void setStringTo(LockFile lockFile, String value) {
        if (stringSetter != null) {
            stringSetter.accept(lockFile, value);
        } else {
            throw new IllegalStateException(this + " field can't have string value " + value);
        }
    }

    /**
     * Set the list of strings to the lockFile object.
     *
     * @param lockFile lockFile object
     * @param list     list of strings
     */
    public void setListTo(LockFile lockFile, List<String> list) {
        if (listSetter != null) {
            listSetter.accept(lockFile, list);
        } else {
            throw new IllegalStateException(this + " field can't have list value " + list.toString());
        }
    }
}

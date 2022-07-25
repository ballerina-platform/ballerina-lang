/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class responsible for loading foreign language keywords from properties file.
 */
public final class ParserConfigurations {

    private ResourceBundle properties = null;
    private static volatile ParserConfigurations instance = null;
    private static String language = "en";
    private static String country = "LK";

    private ParserConfigurations() {
        Locale currentLanguage;
        if (language == null || country == null) {
            currentLanguage = Locale.getDefault();
        } else {
            currentLanguage = new Locale(language, country);
        }
        this.properties = ResourceBundle.getBundle("token", currentLanguage);
    }

    public static ParserConfigurations getInstance() {
        if (instance == null) {
            synchronized (ParserConfigurations.class) {
                if (instance == null) {
                    instance = new ParserConfigurations();
                }
            }
        }
        return instance;
    }

    public String getProperty(String key) throws NullPointerException {
        if (this.properties.containsKey(key)) {
            return this.properties.getString(key);
        } else {
            return key;
        }
    }
}

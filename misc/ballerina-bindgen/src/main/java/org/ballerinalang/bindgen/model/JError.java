/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.model;

import java.util.Locale;

/**
 * Class for storing details pertaining to a specific Java Exception used for Ballerina bridge code generation.
 *
 * @since 1.2.3
 */
public class JError {

    private String exceptionName;
    private String exceptionConstName;
    private String shortExceptionName;

    JError(Class exception) {

        exceptionName = exception.getName();
        shortExceptionName = exception.getSimpleName();
        exceptionConstName = exception.getSimpleName().toUpperCase(Locale.ENGLISH);
    }

    public String getShortExceptionName() {
        return shortExceptionName;
    }

    public String getExceptionConstName() {
        return exceptionConstName;
    }
}

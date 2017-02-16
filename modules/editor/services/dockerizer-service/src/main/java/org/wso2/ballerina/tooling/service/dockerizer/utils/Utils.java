/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.wso2.ballerina.tooling.service.dockerizer.utils;

import java.util.Base64;

/**
 * Generic utilities need for the service.
 */
public class Utils {

    public static String getBase64DecodedString(String encodedString) {
        return Utils.getBase64DecodedString(encodedString, null);
    }

    public static String getBase64DecodedString(String encodedString, String defaultValue) {
        byte[] decodedArr = Base64.getDecoder().decode(encodedString);
        return decodedArr.length == 0 ? defaultValue : new String(decodedArr);
    }
}

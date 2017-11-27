/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.composer.service.workspace.ws.exception;

import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class BallerinaWebSocketException extends Exception {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BallerinaWebSocketException.class);

    public BallerinaWebSocketException(Exception e) {

        if (e instanceof NoSuchMethodException) {
            LOGGER.error("No such method in the provided class");
            e.printStackTrace();
        }

        else if (e instanceof IllegalAccessException) {
            LOGGER.error("Cannot access the definition of the specified class, field, method or constructor");
            e.printStackTrace();

        } else if (e instanceof InvocationTargetException) {
            LOGGER.error("Error when invoking the target method");
            e.printStackTrace();
        }
        else if (e instanceof InstantiationException) {
            LOGGER.error("Specified class object cannot be instantiated");
            e.printStackTrace();
        } else {
            LOGGER.error("Error");
            e.printStackTrace();
        }

    }
}
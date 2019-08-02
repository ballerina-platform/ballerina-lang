/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.internal.xmltojson;

import org.ballerinalang.util.exceptions.BLangFreezeException;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class containing error handling methods for native model classes.
 */
public class ErrorHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    /**
     * Handle any xpath related exception.
     * 
     * @param operation     Operation that executed
     * @param e             Throwable to handle
     */
    public static void handleXMLException(String operation, Throwable e) {
        // here local message of the cause is logged whenever possible, to avoid java class being logged 
        // along with the error message.
        if (e instanceof BallerinaException && ((BallerinaException) e).getDetail() != null) {
            throw new BallerinaException(BallerinaErrorReasons.XML_OPERATION_ERROR, "Failed to " + operation + ": " +
                    ((BallerinaException) e).getDetail());
        } else if (e instanceof BLangFreezeException) {
            throw new BallerinaException(BallerinaErrorReasons.XML_OPERATION_ERROR, "Failed to " + operation + ": " +
                    ((BLangFreezeException) e).getDetail());
        } else if (e.getCause() != null) {
            throw new BallerinaException(BallerinaErrorReasons.XML_OPERATION_ERROR,
                                         "Failed to " + operation + ": " + e.getCause().getMessage());
        } else {
            throw new BallerinaException(BallerinaErrorReasons.XML_OPERATION_ERROR,
                                         "Failed to " + operation + ": " + e.getMessage());
        }
    }
}

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
package org.ballerinalang.langlib.xml;

import io.ballerina.runtime.util.exceptions.BLangFreezeException;
import io.ballerina.runtime.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class containing error handling methods for native model classes.
 */
public class ErrorHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
    
    /*
     * JSON error handling methods.
     */
    
    /**
     * Handle invalid/malformed jsonpath exceptions.
     * 
     * @param operation     Operation that executed
     * @param e             Exception to handle
     */
    public static void handleInvalidJsonPath(String operation, Exception e) {
        throw new BallerinaException("Failed to " + operation + ". Invalid jsonpath: " + e.getMessage());
    }
    
    /**
     * Handle non-existing jsonpath exceptions.
     * 
     * @param operation Operation that executed
     * @param jsonPath  Jsonpath
     * @param e         Exception to handle
     */
    public static void handleNonExistingJsonpPath(String operation, String jsonPath, Exception e) {
        // TODO : Fix this properly when BNull comes. Until then we throw BallerinaException.
        throw new BallerinaException("Failed to " + operation + ". Jsonpath " + jsonPath +
                " does not match any element: " + e.getMessage());
    }
    
    /**
     * Handle any jsonpath related exception.
     * 
     * @param operation     Operation that executed
     * @param e             Throwable to handle
     */
    public static void handleJsonPathException(String operation, Throwable e) {
        throw new BallerinaException("Failed to " + operation + ". Error while executing jsonpath: " + e.getMessage());
    }
    
    /**
     * Handle any malformed json exception.
     * 
     * @param operation     Operation that executed
     * @param e             Exception to handle
     */
    public static void handleMalformedJson(String operation, Exception e) {
        // here local message of the cause is logged whenever possible, to avoid java class being logged
        // along with the error message.
        if (e.getCause() != null) {
            throw new BallerinaException("Failed to " + operation + " due to malformed json: " + 
                    e.getCause().getMessage());
        } else {
            throw new BallerinaException("Failed to " + operation + " due to malformed json: " + e.getMessage());
        }
    }
    
    /**
     * Handle any json related exception.
     * 
     * @param reason        The reason to set as error reason
     * @param operation     Operation that executed
     * @param e             Throwable to handle
     */
    public static void handleJsonException(String reason, String operation, Throwable e) {
        // here local message  of the cause is logged whenever possible, to avoid java class being logged 
        // along with the error message.
        if (e instanceof BallerinaException && ((BallerinaException) e).getDetail() != null) {
            throw new BallerinaException(reason, "Failed to " + operation + ": " +
                    ((BallerinaException) e).getDetail());
        } else if (e instanceof BLangFreezeException) {
            throw new BallerinaException(reason, "Failed to " + operation + ": " +
                    ((BLangFreezeException) e).getDetail());
        } else if (e.getCause() != null) {
            throw new BallerinaException(reason, "Failed to " + operation + ": " + e.getCause().getMessage());
        } else {
            throw new BallerinaException(reason, "Failed to " + operation + ": " + e.getMessage());
        }
    }
    
    
    /*
     * XML error handling methods.
     */

    /**
     * Handle invalid/malformed xpath exceptions.
     *
     * @param operation Operation that executed
     * @param e         Exception to handle
     */
    public static void handleInvalidXPath(String operation, Exception e) {
        throw new BallerinaException("Failed to " + operation + ". Invalid xpath: " + e.getMessage());
    }
    
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
            throw new BallerinaException(BallerinaErrorReasons.XML_OPERATION_ERROR.getValue(),
                                         "Failed to " + operation + ": " + ((BallerinaException) e).getDetail());
        } else if (e instanceof BLangFreezeException) {
            throw new BallerinaException(BallerinaErrorReasons.XML_OPERATION_ERROR.getValue(),
                                         "Failed to " + operation + ": " + ((BLangFreezeException) e).getDetail());
        } else if (e.getCause() != null) {
            throw new BallerinaException(BallerinaErrorReasons.XML_OPERATION_ERROR.getValue(),
                                         "Failed to " + operation + ": " + e.getCause().getMessage());
        } else {
            throw new BallerinaException(BallerinaErrorReasons.XML_OPERATION_ERROR.getValue(),
                                         "Failed to " + operation + ": " + e.getMessage());
        }
    }

    public static void handleUndefineHeader(String headerName) {
        throw new BallerinaException("Header '" + headerName + "' not present in the message");
    }
    
    /**
     * Log a warn.
     * 
     * @param operation     Operation that executed
     * @param msg           Warning message
     */
    public static void logWarn(String operation, String msg) {
        // TODO: Check this logic again.
        throw new BallerinaException("Failed to " + operation + ". " + msg);
    }
}

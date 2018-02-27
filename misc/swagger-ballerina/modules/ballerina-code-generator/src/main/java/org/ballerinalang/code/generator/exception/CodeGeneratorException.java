/*
 *
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.code.generator.exception;

/**
 * Top level exception class of Code Generator exceptions.
 * 
 */
public class CodeGeneratorException extends Exception {

    /**
     * @param message            Error message
     * @param cause              Error cause
     * @param enableSuppression  whether you need enable suppression
     * @param writableStackTrace Writable error stack trace.
     */
    protected CodeGeneratorException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    /**
     * This is a default constructure where you can pass error code to error DTO
     * @param message Error message
     * @param cause throwable object.
     */
    public CodeGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}

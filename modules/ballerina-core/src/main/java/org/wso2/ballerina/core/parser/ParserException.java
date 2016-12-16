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
package org.wso2.ballerina.core.parser;

/**
 * Exception thrown when a ballerina config is failed to parse.
 */
public class ParserException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new parser exception with the specified detail message.
     * 
     * @param message   Error Message
     */
    public ParserException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new parser exception with the specified detail message and cause.
     * 
     * @param message   Error message
     * @param cause     Cause
     */
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new parser exception with the cause.
     * 
     * @param cause
     */
    public ParserException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}

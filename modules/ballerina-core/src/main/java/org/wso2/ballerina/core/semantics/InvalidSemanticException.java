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
package org.wso2.ballerina.core.semantics;

/**
 * Exception thrown when a ballerina config is failed to parse.
 */
public class InvalidSemanticException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@link InvalidSemanticException} with the specified detail message.
     * 
     * @param message   Error Message
     */
    public InvalidSemanticException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new {@link InvalidSemanticException} with the specified detail message and cause.
     * 
     * @param message   Error message
     * @param cause     Cause
     */
    public InvalidSemanticException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link InvalidSemanticException} with the cause.
     * 
     * @param cause
     */
    public InvalidSemanticException(Throwable cause) {
        super(cause);
    }
}

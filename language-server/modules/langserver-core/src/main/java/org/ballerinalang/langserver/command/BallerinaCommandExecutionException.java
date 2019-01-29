/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
 */

package org.ballerinalang.langserver.command;

/**
 * This exception is thrown when Ballerina Command Execution encounters an error.
 */
public class BallerinaCommandExecutionException extends Exception {

    public BallerinaCommandExecutionException(String message) {
        super(message);
    }

    public BallerinaCommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BallerinaCommandExecutionException(Throwable cause) {
        super(cause);
    }

    public BallerinaCommandExecutionException(String message, Throwable cause, boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

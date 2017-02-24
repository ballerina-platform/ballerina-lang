/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.util.exceptions;

import org.ballerinalang.bre.Context;

/**
 * {@code ServiceNotFoundException} is thrown when service not found related errors occur with service dispatching
 * during ballerina runtime execution.
 *
 */
public class ServiceNotFoundException extends BallerinaException {

    /**
     * Constructs a new {@link ServiceNotFoundException} with the specified detail message.
     *
     * @param message Error Message
     */
    public ServiceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link ServiceNotFoundException} with ballerina context.
     *
     * @param message Error message
     * @param context Ballerina context
     */
    public ServiceNotFoundException(String message, Context context) {
        super(message, context);
    }

}

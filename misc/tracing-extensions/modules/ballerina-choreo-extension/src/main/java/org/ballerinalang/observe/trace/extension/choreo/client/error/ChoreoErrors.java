/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.observe.trace.extension.choreo.client.error;

/**
 * Collection of helper methods to create Choreo specific exceptions.
 *
 * @since 2.0.0
 */
public class ChoreoErrors {

    public static ChoreoClientException getUnavailableError() {
        return new ChoreoClientException(
                new ChoreoError(ChoreoError.Code.UNAVAILABLE, "Choreo services are not available.", null)
        );
    }

    public static ChoreoClientException getIncompatibleServiceError() {
        return new ChoreoClientException(
                new ChoreoError(ChoreoError.Code.UNAVAILABLE, "Choreo backend is not compatible.", null)
        );
    }

    public static ChoreoClientException createValidationError(String message) {
        return new ChoreoClientException(new ChoreoError(ChoreoError.Code.VALIDATION_ERROR, message, null));
    }
}

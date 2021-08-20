/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation;

/**
 * Error type definition for debug expression evaluation related exceptions.
 *
 * @since 2.0.0
 */
public class EvaluationException extends Exception {

    public EvaluationException(String message) {
        this(message, null);
    }

    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static EvaluationException createEvaluationException(String reason) {
        return createEvaluationException(EvaluationExceptionKind.CUSTOM_ERROR, reason);
    }

    public static EvaluationException createEvaluationException(EvaluationExceptionKind exceptionKind, String... details) {
        return new EvaluationException(String.format(exceptionKind.getString(), (Object[]) details));
    }
}

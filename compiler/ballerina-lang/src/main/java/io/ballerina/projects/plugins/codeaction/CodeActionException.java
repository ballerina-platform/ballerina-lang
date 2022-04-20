/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.projects.plugins.codeaction;

/**
 * A runtime exception thrown to capture exceptions captured while executing compiler plugins.
 *
 * @since 2.0.0
 */
public class CodeActionException extends RuntimeException {

    private final String codeActionName;

    public CodeActionException(String codeActionName, Throwable cause) {
        super(cause);
        this.codeActionName = codeActionName;
    }

    public String getCodeActionName() {
        return codeActionName;
    }
}

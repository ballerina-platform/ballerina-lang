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

import java.util.List;

/**
 * Context to hold information about the execution of a {@link CodeAction}.
 *
 * @since 2.0.0
 */
public interface CodeActionExecutionContext extends PositionedActionContext {

    /**
     * Arguments supplied for the code action execution.
     *
     * @return Arguments for the code action
     */
    List<CodeActionArgument> arguments();
}

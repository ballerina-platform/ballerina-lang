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
package org.ballerinalang.langserver.commons.command.spi;

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;

/**
 * Represents the SPI interface for the Language Server Command Executor.
 * 
 * @since 0.983.0
 */
public interface LSCommandExecutor {

    String ARG_KEY = "argumentK";

    String ARG_VALUE = "argumentV";

    /**
     * Execute the Command.
     * @param context           Language Server Context.
     * @return {@link Object}   Command Execution result
     * @throws LSCommandExecutorException exception while executing the code action
     */
    Object execute(LSContext context) throws LSCommandExecutorException;

    /**
     * Get the Command name as a String.
     * This command name is send from server to client in order to register the command.
     *
     * @return {@link String}   Name of the command
     */
    String getCommand();
}

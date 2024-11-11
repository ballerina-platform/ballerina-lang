/*
 * Copyright (c) 2023, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.JsonPrimitive;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;

import java.nio.file.Path;

/**
 * Command executor for stopping a Ballerina project.
 * See {@link RunExecutor} for running a Ballerina project.
 *
 * @since 2201.6.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class StopExecutor implements LSCommandExecutor {

    @Override
    public Boolean execute(ExecuteCommandContext context) {
        return context.workspace().stop(extractPath(context));
    }

    private static Path extractPath(ExecuteCommandContext context) {
        return Path.of(context.getArguments().get(0).<JsonPrimitive>value().getAsString());
    }

    @Override
    public String getCommand() {
        return "STOP";
    }
}

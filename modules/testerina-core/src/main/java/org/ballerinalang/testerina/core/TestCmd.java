/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.core;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Test command for ballerina launcher
 */
@Parameters(commandNames = "test", commandDescription = "test Ballerina program")
public class TestCmd implements BLauncherCmd {

    @Parameter(arity = 1, description = "arguments")
    private List<String> argList;

    @Parameter(names = "--debug", hidden = true)
    private String debugPort;

    public void execute() {
        if (argList == null || argList.size() == 0) {
            throw LauncherUtils.createUsageException("no ballerina program or folder given to run tests");
        }

        Path[] paths = argList.stream().map(Paths::get).toArray(Path[]::new);
        TestRunner.runTest(paths);
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder.append("ballerina test <filename>");
        stringBuilder.append("\n");
        stringBuilder.append("\n");
    }
}

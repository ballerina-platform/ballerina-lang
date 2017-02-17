/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.docgen.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.ballerinalang.launcher.BLauncherCmd;
import org.wso2.ballerina.docgen.docs.BallerinaDocGeneratorMain;

import java.io.PrintStream;
import java.util.List;

/**
 * doc command for ballerina which generates documentation for Ballerina packages
 */
@Parameters(commandNames = "doc", commandDescription = "generates documentation for Ballerina packages")
public class BallerinaDocCmd implements BLauncherCmd {
    private final PrintStream out = System.out;

    @Parameter(arity = 1, description = "a valid path which points either to a Ballerina file or a folder with "
            + "Ballerina files")
    private List<String> argList;

    @Override
    public void execute() {
        if (argList == null || argList.size() == 0) {
            // throw Utils.createUsageException("No valid Ballerina file given");
            out.println("No valid Ballerina file given");
            return;
        }

        BallerinaDocGeneratorMain.main(new String[] { argList.get(0) });
    }

    @Override
    public String getName() {
        return "doc";
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder.append("ballerina doc <path-to-ballerina-files>\n");
        stringBuilder
                .append("\npath-to-ballerina-files:\n A valid path which points either to a Ballerina file or a folder"
                        + " with Ballerina files");
        stringBuilder.append("\n");
    }

    @Override
    public void setParentCmdParser(JCommander arg0) {
    }

    @Override
    public void setSelfCmdParser(JCommander arg0) {
    }
}

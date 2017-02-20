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

package org.ballerinalang.docgen.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.launcher.BLauncherCmd;

import java.io.PrintStream;
import java.util.List;

/**
 * doc command for ballerina which generates documentation for Ballerina packages
 */
@Parameters(commandNames = "doc", commandDescription = "generates API documentation for Ballerina packages")
public class BallerinaDocCmd implements BLauncherCmd {
    private final PrintStream out = System.out;

    @Parameter(arity = 1, description = "either the path to the directories where Ballerina source files reside or a "
            + "path to a Ballerina file which does not belong to a package")
    private List<String> argList;
    
    @Parameter(names = { "--output", "-o" },
            description = "path to the output directory where the API documentation will be written to", hidden = false)
    private String outputDir;
    
    @Parameter(names = { "--exclude", "-e" },
            description = "comma separated list of package names to be filtered from the documentation", hidden = false)
    private String packageFilter;
    
    @Parameter(names = { "--native", "-n" },
            description = "treat the source as native ballerina code", hidden = false)
    private boolean nativeSource;
    
    @Parameter(names = { "--verbose", "-v" },
            description = "enable debug level logs", hidden = false)
    private boolean debugEnabled;

    @Override
    public void execute() {
        if (argList == null || argList.size() == 0) {
            StringBuilder sb = new StringBuilder("docerina: no valid ballerina source given.\n");
            printUsage(sb);
            out.println(sb);
            return;
        }

        if (debugEnabled) {
            System.setProperty(BallerinaDocConstants.ENABLE_DEBUG_LOGS, "true");
        }

        String[] sources = argList.toArray(new String[argList.size()]);
        BallerinaDocGenerator.generateApiDocs(outputDir, packageFilter, nativeSource, sources);
    }

    @Override
    public String getName() {
        return "doc";
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder.append("ballerina doc <sourcepath>... [-o outputdir -e excludedpackages -v -n]\n");
        stringBuilder
                .append("\n\tsourcepath:\n\tEither the paths to the directories where Ballerina source files reside or "
                        + "a path to a Ballerina file which does not belong to a package");
        stringBuilder.append("\n");
    }

    @Override
    public void setParentCmdParser(JCommander arg0) {
    }

    @Override
    public void setSelfCmdParser(JCommander arg0) {
    }
}

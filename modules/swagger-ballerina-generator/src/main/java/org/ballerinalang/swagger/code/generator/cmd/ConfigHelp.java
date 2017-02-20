/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.swagger.code.generator.cmd;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import org.ballerinalang.swagger.code.generator.BallerinaConnectorCodeGenerator;

@Command(name = "config-help", description = "Config help for chosen lang")
public class ConfigHelp implements Runnable {

    @Option(name = {"-l", "--lang"}, title = "language", required = true,
            description = "language to get config help for")
    private String lang;

    @Override
    public void run() {
        System.out.println();
        CodegenConfig config = new BallerinaConnectorCodeGenerator();

        System.out.println("CONFIG OPTIONS");
        for (CliOption langCliOption : config.cliOptions()) {
            System.out.println("\t" + langCliOption.getOpt());
            System.out.println("\t    " + langCliOption.getOptionHelp().replaceAll("\n", "\n\t    "));
            System.out.println();
        }
    }
}

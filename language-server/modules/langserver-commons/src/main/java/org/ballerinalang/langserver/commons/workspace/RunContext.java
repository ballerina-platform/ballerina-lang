/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.org).
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
package org.ballerinalang.langserver.commons.workspace;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the context required to run a Ballerina program using the LS workspace manager.
 *
 * @param balSourcePath Ballerina source file path to run
 * @param programArgs   program arguments to run the program
 * @param env           environment variables to be added to the program
 * @param debugPort     debug port to be used for debugging (if available)
 * @since 2201.11.0
 */
public record RunContext(Path balSourcePath, List<String> programArgs, Map<String, String> env, Integer debugPort) {

    public static class Builder {

        private final Path sourcePath;
        private List<String> programArgs = new ArrayList<>();
        private Map<String, String> env = Map.of();
        private Integer debugPort = -1;

        public Builder(Path sourcePath) {
            this.sourcePath = sourcePath;
        }

        public Builder withProgramArgs(List<String> programArgs) {
            this.programArgs = programArgs;
            return this;
        }

        public Builder withEnv(Map<String, String> env) {
            this.env = env;
            return this;
        }

        public Builder withDebugPort(Integer debugPort) {
            this.debugPort = debugPort;
            return this;
        }

        public RunContext build() {
            return new RunContext(sourcePath, programArgs, env, debugPort);
        }
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.spi;

import io.ballerina.projects.Project;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation of this interface analyze the AST of the Ballerina program
 * and generate symbol data to be used for observability purposes.
 *
 * @since 2.0.0
 */
public interface ObservabilitySymbolCollector {
    void process(Project project);

    void writeToExecutable(Path executableFile, Project project) throws IOException;
}

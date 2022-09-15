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

package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.projects.Project;
import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;

import java.nio.file.Path;

/**
 * Null Object for {@link ObservabilitySymbolCollector} interface.
 *
 * @since 2.0.0
 */
public class NoOpObservabilitySymbolCollector implements ObservabilitySymbolCollector {
    @Override
    public void process(Project project) {
        // Do nothing
    }

    @Override
    public void writeToExecutable(Path executableFile, Project project) {
        // Do nothing
    }
}

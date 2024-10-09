/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.plugins;

import io.ballerina.projects.BalCommand;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.nio.file.Path;
import java.util.Optional;

/**
 * the context for the compiler lifecycle event task.
 *
 * @see CompilerLifecycleContext
 * @since 2.0.0
 */
public interface CompilerLifecycleEventContext {

    /**
     * Returns the current {@code Package} instance on which the compilation is being performed.
     *
     * @return the current {@code Package} instance
     */
    public Package currentPackage();

    /**
     * Returns the compilation instance that captures the state of the package compilation.
     *
     * @return the package compilation instance
     */
    public PackageCompilation compilation();

    /**
     * Reports a diagnostic against the compilation.
     *
     * @param diagnostic the {@code Diagnostic} to be reported
     */
    public void reportDiagnostic(Diagnostic diagnostic);

    /**
     * Returns the path of the generated binary artifact.
     *
     * @return path to the generated artifact.
     */
    public Optional<Path> getGeneratedArtifactPath();
    public BalCommand balCommand();
}

/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.diagnostic;

import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code DiagnosticLog} provides a way for the Ballerina compiler
 * and it's components to report errors, warnings and other information.
 *
 * @since 0.961.0
 */
public interface DiagnosticLog {


    /**
     * Logs a message of the specified {@link DiagnosticSeverity} at the {@link Location}.
     *
     * @param kind    the kind of the diagnostic
     * @param location  the location of the source code element.
     * @param message the message
     */
    @Deprecated
    void logDiagnostic(DiagnosticSeverity kind, Location location, CharSequence message);

    /**
     * @param kind  the kind of the diagnostic
     * @param pkgId  the package ID of the package
     * @param location  the location of the source code element
     * @param message   the message
     */
    @Deprecated
    void logDiagnostic(DiagnosticSeverity kind, PackageID pkgId, Location location, CharSequence message);

    /**
     * @param severity  the severity of the diagnostic
     * @param moduleDescriptor  the module descriptor
     * @param location  the location of the source code element
     * @param message   the message
     */
    void logDiagnostic(DiagnosticSeverity severity,
                       @Nullable ModuleDescriptor moduleDescriptor,
                       Location location,
                       CharSequence message);
}

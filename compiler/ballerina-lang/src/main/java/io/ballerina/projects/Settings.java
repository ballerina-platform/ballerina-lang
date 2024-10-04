/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.model.Central;
import io.ballerina.projects.internal.model.Proxy;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * Represents a Settings.toml file.
 *
 * @since 2.0.0
 */
public class Settings {
    private final Proxy proxy;
    private final Central central;
    private final Repository[] repositories;
    private final DiagnosticResult diagnostics;

    private Settings(Proxy proxy, Central central, DiagnosticResult diagnostics, @Nullable Repository[] repositories) {
        this.proxy = proxy;
        this.central = central;
        this.diagnostics = diagnostics;
        this.repositories = repositories;
    }

    public static Settings from(Proxy proxy, Central central, DiagnosticResult diagnostics, Repository[] repositories) {
        return new Settings(proxy, central, diagnostics, repositories);
    }

    public static Settings from() {
        return new Settings(Proxy.from(), Central.from(), new DefaultDiagnosticResult(Collections.emptyList()), null);
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Central getCentral() {
        return central;
    }

    public DiagnosticResult diagnostics() {
        return diagnostics;
    }

    public Repository[] getRepositories() {
        if (repositories == null) {
            return new Repository[0];
        }
        return repositories;
    }

    public String getErrorMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Settings.toml contains errors\n");
        for (Diagnostic diagnostic : diagnostics().errors()) {
            message.append(convertDiagnosticToString(diagnostic));
            message.append("\n");
        }
        return message.toString();
    }

    private String convertDiagnosticToString(Diagnostic diagnostic) {
        LineRange lineRange = diagnostic.location().lineRange();

        LineRange oneBasedLineRange = LineRange.from(lineRange.fileName(), LinePosition
                .from(lineRange.startLine().line(), lineRange.startLine().offset() + 1), LinePosition
                                                             .from(lineRange.endLine().line(),
                                                                   lineRange.endLine().offset() + 1));

        return diagnostic.diagnosticInfo().severity().toString() + " [" + oneBasedLineRange.fileName() + ":"
                + oneBasedLineRange + "] " + diagnostic.message();
    }
}

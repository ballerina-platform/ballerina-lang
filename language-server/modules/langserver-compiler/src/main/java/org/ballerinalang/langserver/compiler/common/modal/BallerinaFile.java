/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.compiler.common.modal;

import io.ballerina.tools.diagnostics.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Class which contains Ballerina model and Diagnostic information.
 */
public class BallerinaFile {

    private BLangPackage bLangPackage;
    private final List<Diagnostic> diagnostics;
    private final boolean isBallerinaProject;
    private CompilerContext compilerContext;

    public BallerinaFile(BLangPackage bLangPackage,
                         List<Diagnostic> diagnostics, boolean isBallerinaProject, CompilerContext compilerContext) {
        this.bLangPackage = bLangPackage;
        this.diagnostics = diagnostics;
        this.isBallerinaProject = isBallerinaProject;
        this.compilerContext = compilerContext;
    }

    public Optional<List<Diagnostic>> getDiagnostics() {
        Optional<List<Diagnostic>> diagnostics = Optional.ofNullable(this.diagnostics);
        diagnostics.ifPresent(
                diag -> diag.sort(Comparator.comparingInt(a -> a.location().lineRange().startLine().line()))
        );
        return diagnostics;
    }

    /**
     * Returns optional BLangPackage.
     *
     * @return {@link BLangPackage}
     */
    public Optional<BLangPackage> getBLangPackage() {
        return Optional.ofNullable(bLangPackage);
    }

    /**
     * Returns True if a ballerina project, False otherwise.
     *
     * @return True if it is ballerina project. False otherwise.
     */
    public boolean isBallerinaProject() {
        return isBallerinaProject;
    }

    /**
     * Returns compiler context.
     *
     * @return compiler context.
     */
    public CompilerContext getCompilerContext() {
        return compilerContext;
    }

    public void setBLangPackage(BLangPackage bLangPackage) {
        this.bLangPackage = bLangPackage;
    }
}

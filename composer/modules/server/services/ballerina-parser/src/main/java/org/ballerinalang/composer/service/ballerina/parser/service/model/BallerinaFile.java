/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.ballerina.parser.service.model;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.Comparator;
import java.util.List;

/**
 * Class which contains Ballerina model and Diagnostic information.
 */
@Deprecated
public class BallerinaFile {

    private BLangPackage bLangPackage;
    private List<Diagnostic> diagnostics;

    public List<Diagnostic> getDiagnostics() {
        diagnostics.sort(Comparator.comparingInt(a -> a.getPosition().getStartLine()));
        return diagnostics;
    }

    public BLangPackage getBLangPackage() {
        return bLangPackage;
    }

    public void setBLangPackage(BLangPackage bLangPackage) {
        this.bLangPackage = bLangPackage;
    }

    public void setDiagnostics(List<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }
}

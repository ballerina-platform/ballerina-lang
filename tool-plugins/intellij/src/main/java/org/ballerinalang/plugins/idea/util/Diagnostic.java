/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.util;

/**
 * Represents a diagnostic. Used in external annotator.
 */
public interface Diagnostic {

    /**
     * Kind of the diagnostic.
     */
    enum Kind {
        ERROR,
        WARNING,
        NOTE,
    }

    /**
     * Contains diagnostic source details.
     */
    interface DiagnosticSource {

        String getPackageName();

        String getPackageVersion();

        String getCompilationUnitName();
    }

    /**
     * Contains details of diagnostic position.
     */
    interface DiagnosticPosition {

        DiagnosticSource getSource();

        int getStartLine();

        int getEndLine();

        int getStartColumn();

        int getEndColumn();
    }

    Kind getKind();

    DiagnosticSource getSource();

    DiagnosticPosition getPosition();

    String getMessage();

    DiagnosticCode getCode();
}

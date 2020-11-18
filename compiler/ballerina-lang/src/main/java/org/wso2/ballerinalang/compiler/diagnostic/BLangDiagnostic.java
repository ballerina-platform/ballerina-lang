/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.diagnostic;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.util.diagnostic.DiagnosticCode;

/**
 * Represent a diagnostic in the ballerina compiler front-end. A diagnostic can be a semantic
 * error, a warning or a info.
 *
 * @since 2.0.0
 */
public class BLangDiagnostic extends Diagnostic {

    private Location location;
    private String msg;
    private DiagnosticInfo diagnosticInfo;
    private DiagnosticCode diagnosticCode;

    public BLangDiagnostic(Location location, String msg, DiagnosticInfo diagnosticInfo) {
        this.location = location;
        this.msg = msg;
        this.diagnosticInfo = diagnosticInfo;
        this.diagnosticCode = null;
    }

    public BLangDiagnostic(Location location, String msg, DiagnosticInfo diagnosticInfo,
                           DiagnosticCode diagnosticCode) {
        this.location = location;
        this.msg = msg;
        this.diagnosticInfo = diagnosticInfo;
        this.diagnosticCode = diagnosticCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location location() {
        return location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiagnosticInfo diagnosticInfo() {
        return diagnosticInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String message() {
        return msg;
    }

    public DiagnosticCode getCode() {
        return diagnosticCode;
    }

    public String toString() {
        return diagnosticInfo.severity() + ": " + location.toString() + ": " + msg;
    }
}

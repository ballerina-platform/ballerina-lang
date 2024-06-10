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
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.Location;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private List<DiagnosticProperty<?>> properties;

    public BLangDiagnostic(Location location, String msg, DiagnosticInfo diagnosticInfo,
                           DiagnosticCode diagnosticCode) {
        this(location, msg, diagnosticInfo, diagnosticCode, Collections.emptyList());
    }

    public BLangDiagnostic(Location location, String msg, DiagnosticInfo diagnosticInfo,
                           DiagnosticCode diagnosticCode, List<DiagnosticProperty<?>> properties) {
        this.location = location;
        this.msg = msg;
        this.diagnosticInfo = diagnosticInfo;
        this.diagnosticCode = diagnosticCode;
        this.properties = properties;
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

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return properties;
    }

    public DiagnosticCode getCode() {
        return diagnosticCode;
    }

    @Override
    public int hashCode() {
        int propHash = Arrays.hashCode(properties.toArray());

        if (diagnosticCode != null) {
            return Arrays.hashCode(new int[]{
                    location.hashCode(), msg.hashCode(), diagnosticInfo.hashCode(), diagnosticCode.hashCode(),
                    propHash});
        }
        return Arrays.hashCode(new int[]{location.hashCode(), msg.hashCode(), diagnosticInfo.hashCode(), propHash});
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BLangDiagnostic that) {
            if (this.diagnosticCode != null) {
                return this.location.equals(that.location) && this.msg.equals(that.message())
                        && this.diagnosticInfo.equals(that.diagnosticInfo)
                        && this.properties.equals(that.properties)
                        && this.diagnosticCode.equals(that.diagnosticCode);
            } else {
                if (that.diagnosticCode != null) {
                    return false;
                }
                return this.location.equals(that.location)
                        && this.msg.equals(that.message())
                        && this.diagnosticInfo.equals(that.diagnosticInfo)
                        && this.properties.equals(that.properties);
            }
        }
        return false;
    }
}

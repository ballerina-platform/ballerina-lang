/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util.diagnotic;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticCode;

/**
 * @since 0.94
 */
public class BDiagnostic implements Diagnostic {

    public Kind kind;
    public DiagnosticPos pos;
    public String msg;
    public DiagnosticCode code;

    public BDiagnostic() {
    }

    public BDiagnostic(Kind kind,
                       DiagnosticPos pos,
                       DiagnosticCode code,
                       String msg) {
        this.kind = kind;
        this.pos = pos;
        this.code = code;
        this.msg = msg;
    }

    public BDiagnostic(Kind kind,
                       DiagnosticPos pos,
                       String msg) {
        this.kind = kind;
        this.pos = pos;
        this.msg = msg;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public DiagnosticSource getSource() {
        return pos.getSource();
    }

    @Override
    public DiagnosticPosition getPosition() {
        return pos;
    }

    @Override
    public String getMessage() {
        return msg;
    }

    @Override
    public DiagnosticCode getCode() {
        return code;
    }
    
    @Override
    public String toString() {
        return this.kind + ": " + this.pos + ": " + this.msg;
    }
    
}

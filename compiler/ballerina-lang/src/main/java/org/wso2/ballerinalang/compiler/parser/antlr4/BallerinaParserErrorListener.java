/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.parser.antlr4;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Error Listener for {@link BallerinaParser}
 *
 * @since 0.94
 */
public class BallerinaParserErrorListener extends BaseErrorListener {

    private BLangDiagnosticLog dlog;
    private BDiagnosticSource diagnosticSrc;

    public BallerinaParserErrorListener(CompilerContext context, BDiagnosticSource diagnosticSrc) {
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.diagnosticSrc = diagnosticSrc;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        dlog.error(getPosition(i, i1), DiagnosticCode.SYNTAX_ERROR, s);
    }

    private DiagnosticPos getPosition(int startLine, int startCol) {
        return new DiagnosticPos(diagnosticSrc, startLine, -1, startCol + 1, -1);
    }
}

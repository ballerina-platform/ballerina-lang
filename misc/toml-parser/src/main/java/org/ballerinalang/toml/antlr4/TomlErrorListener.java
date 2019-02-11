/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.toml.antlr4;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.io.PrintStream;

public class TomlErrorListener extends BaseErrorListener {
    private static TomlErrorListener errorListener = null;

    private static PrintStream errStream = System.err;
    private String filePath;

    private TomlErrorListener(String filePath) {
        this.filePath = filePath;
    }

    public static TomlErrorListener getErrorListener(String tomlFilePath) {
        if (errorListener == null) {
            errorListener = new TomlErrorListener(tomlFilePath);
        }

        return errorListener;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        if (filePath == null) {
            return;
        }
        String errorMsg = "invalid toml syntax at " + filePath + ":" + line;
        errStream.println("error: " + errorMsg);
        Runtime.getRuntime().exit(1);
    }

}

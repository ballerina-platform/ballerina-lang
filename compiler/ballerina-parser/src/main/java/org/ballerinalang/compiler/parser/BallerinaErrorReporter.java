/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.compiler.parser;

import java.io.PrintStream;

/**
 * Basic error reporter.
 * 
 * @since 1.2.0
 */
public class BallerinaErrorReporter {

    /*
     * -------------- Error reporting --------------
     * 
     * TODO: Duplicate methods. Try unifying
     */

    /**
     * Report invalid token.
     * 
     * @param token Invalid token
     */
    public void reportInvalidToken(Token token) {
        logError(token.line, token.startCol, "invalid token '" + token.text + "'");
    }

    /**
     * Report missing token.
     * 
     * @param token Token before the missing token
     * @param message Error message
     */
    public void reportMissingTokenError(Token token, String message) {
        logError(token.line, token.endCol, message);
    }

    private void logError(int line, int col, String message) {
        PrintStream out = System.out;
        out.println("xxx.bal:" + line + ":" + col + ":" + message);
    }
}

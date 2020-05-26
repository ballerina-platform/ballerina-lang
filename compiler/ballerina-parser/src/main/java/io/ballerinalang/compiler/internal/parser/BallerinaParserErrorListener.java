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
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STToken;

/**
 * Error listener that is responsible for reporting syntax errors. Custom
 * error reporting mechanisms can be implemented by extending this class.
 * Extending this error listener will have no impact to the default error
 * recovering.
 * 
 * @since 1.2.0
 */
public class BallerinaParserErrorListener {

    public BallerinaParserErrorListener() {
    }

    public void reportInvalidToken(STToken token) {
        logError(0, 0, "invalid token '" + token.toString().trim() + "'");
    }

    public void reportMissingTokenError(STToken token, String message) {
        logError(0, 0, message);
    }

    public void reportInvalidNodeError(STToken token, String message) {
        logError(0, 0, message);
    }
    
    private void logError(int line, int col, String message) {
        // TODO - Removing below lines as it breaks lang-server rpc
        // PrintStream out = System.out;
        // out.println("xxx.bal:" + line + ":" + col + ":" + message);
    }
}

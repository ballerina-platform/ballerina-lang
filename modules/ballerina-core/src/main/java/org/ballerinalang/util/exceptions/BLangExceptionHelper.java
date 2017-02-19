/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.util.exceptions;

import org.ballerinalang.model.Node;
import org.ballerinalang.model.NodeLocation;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for handler error messages.
 */
public class BLangExceptionHelper {
    private static ResourceBundle messageBundle = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault());

    public static void throwSemanticError(String errorMessage) {
        throw new SemanticException(errorMessage);
    }

    public static void throwSemanticError(Node node, SemanticErrors semanticError, Object... params) {
        NodeLocation nodeLocation = node.getNodeLocation();
        //todo NodeLocation toString with below
        String location = nodeLocation.getFileName() + ":" + nodeLocation.getLineNumber() + ": ";

        String errorMsg = MessageFormat.format(messageBundle.getString(semanticError.getErrorMsgKey()), params);
        throw new SemanticException(location + errorMsg);
    }

    public static String constructSemanticError(NodeLocation nodeLocation, SemanticErrors semanticError,
            Object... params) {
        String location = nodeLocation.getFileName() + ":" + nodeLocation.getLineNumber() + ": ";
        String errorMsg = MessageFormat.format(messageBundle.getString(semanticError.getErrorMsgKey()), params);
        return  location + errorMsg;

    }
}

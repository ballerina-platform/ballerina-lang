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
package org.ballerinalang.formatter.core;

import io.ballerina.compiler.syntax.tree.Node;

/**
 * Environment that holds a set of properties related to the currently formatting node.
 *
 * @since 2.0.0
 */
public class FormattingEnv {

    /**
     * Number of of whitespace characters to be used as the indentation for the current line.
     */
    int currentIndentation = 0;

    /**
     * Number of leading newlines to be added to the currently processing node.
     */
    int leadingNL = 0;

    /**
     * Number of trailing newlines to be added to the currently processing node.
     */
    int trailingNL = 0;

    /**
     * Number of trailing whitespace characters to be added to the currently processing node.
     */
    int trailingWS = 0;

    /**
     * Flag indicating whether the currently formatting token is the first token of the current line.
     */
    boolean hasNewline = true;

    /**
     * Length of the currently formatting line.
     */
    int lineLength = 0;

    /**
     * Flag indicating whether to preserve the user added newlines. Preserves up to two newlines per each line-of-code.
     */
    boolean preserveNewlines = false;

    /**
     * Reference to the next node that needs to be wrapped.
     */
    Node nodeToWrap = null;

    /**
     * Flag indicating whether the annotations should be inline.
     */
    boolean inLineAnnotation = false;
}

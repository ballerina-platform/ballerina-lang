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
package org.ballerinalang.langserver.commons;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelpCapabilities;

/**
 * Represents the Signature help operation context.
 *
 * @since 2.0.0
 */
public interface SignatureContext extends DocumentServiceContext {

    /**
     * Get the client's signature capabilities.
     *
     * @return {@link SignatureHelpCapabilities}
     */
    SignatureHelpCapabilities capabilities();

    /**
     * Get the cursor position, where the signature help request triggered.
     * 
     * @return {@link Position} of the cursor
     */
    Position getPosition();

    /**
     * Set the cursor position as an offset value according to the syntax tree.
     *
     * @param offset of the cursor
     */
    void setCursorPositionInTree(int offset);

    /**
     * Get the cursor position as an offset value according to the syntax tree.
     *
     * @return {@link Integer} offset of the cursor
     */
    int getCursorPositionInTree();
}

/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.tree.IdentifierNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.Set;

/**
 * Temporary  structure for storing name-reference;.
 *
 * @since 0.94
 */
public class BLangNameReference {
    public IdentifierNode pkgAlias;
    public IdentifierNode name;
    public DiagnosticPos pos;
    public Set<Whitespace> ws;

    public BLangNameReference(DiagnosticPos poc, Set<Whitespace> ws, IdentifierNode pkgAlias, IdentifierNode name) {
        this.pkgAlias = pkgAlias;
        this.name = name;
        this.pos = poc;
        this.ws = ws;
    }
}

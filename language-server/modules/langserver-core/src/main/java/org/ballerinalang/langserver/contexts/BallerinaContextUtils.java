/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.contexts;

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;

import java.util.Optional;

/**
 * Holds the utilities for ballerina context implementations.
 * 
 * @since 2.0.0
 */
public final class BallerinaContextUtils {

    private BallerinaContextUtils() {
    }
    
    public static Optional<ModuleMemberDeclarationNode> getEnclosingModuleMember(SyntaxTree syntaxTree, int cursor) {
        for (ModuleMemberDeclarationNode member : ((ModulePartNode) syntaxTree.rootNode()).members()) {
            int startOffset = member.textRange().startOffset();
            int endOffset = member.textRange().endOffset();
            
            if (cursor > startOffset && cursor < endOffset) {
                return Optional.of(member);
            }
        }
        
        return Optional.empty();
    }
}

/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.document;

import org.ballerinalang.langserver.commons.LSOperation;

/**
 * Represents a document context.
 *
 * @since 2.0.0
 */
public enum DocumentContext implements LSOperation {
    DC_SYNTAX_API_CALLS("ballerinaDocument/syntaxApiCalls"),
    DC_SYNTAX_TREE("ballerinaDocument/syntaxTree"),
    DC_SYNTAX_TREE_MODIFY("ballerinaDocument/syntaxTreeModify"),
    DC_SYNTAX_TREE_BY_RANGE("ballerinaDocument/syntaxTreeByRange"),
    DC_SYNTAX_TREE_BY_NAME("ballerinaDocument/syntaxTreeByName"),
    DC_SYNTAX_TREE_LOCATE("ballerinaDocument/syntaxTreeLocate"),
    DC_AST("ballerinaDocument/ast"),
    DC_PROJECT("ballerinaDocument/project"),
    DC_DIAGNOSTICS("ballerinaDocument/diagnostics"),
    DC_SYNTAX_TREE_NODE("ballerinaDocument/syntaxTreeNode"),
    DC_EXEC_POSITION("ballerinaDocument/executorPositions"),
    DC_RESOLVE_MISSING_DEPENDENCIES("ballerinaDocument/resolveMissingDependencies");

    private final String name;

    DocumentContext(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}

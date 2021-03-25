/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.shell.snippet;

import java.util.Set;

/**
 * Snippets are individual statements.
 * <p>
 * Every snippet must have a kind (which dictates where the snippet should go) and a
 * sub kind (depicting the statement type) Each snippet must refer to a single statement.
 * That means if the same input line contained several statements, it would be parsed
 * into several snippets. (This separation is done in preprocessor.)
 * <p>
 * In processing the snippets, if a snippet contained an error and failed to run,
 * the execution of the snippet would be stopped. If the snippet was contained in a
 * line with more snippets, (if the input contained multiple snippets)
 * all the snippets would be ditched.
 * This also means that an error snippet is taken as if it were never given.
 * <p>
 * Also, names given to the REPL may never be overridden.
 * (If {@code x} variable is defined, you cannot redefine variable {@code x} even
 * with the same type. Same goes for functions, classes etc..)
 * However, any valid redeclaration in a different scope may be possible.
 *
 * @since 2.0.0
 */
public interface Snippet {
    /**
     * Finds all the used imports in this snippet.
     *
     * @return Set of all the used import module prefixes.
     */
    Set<String> usedImports();

    /**
     * Kind is the category of the snippet.
     * This determines the position where the snippet will go.
     *
     * @return Snippet kind value.
     */
    SnippetKind getKind();
}

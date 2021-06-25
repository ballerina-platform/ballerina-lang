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

package io.ballerina.shell.invoker.classload.context;

import io.ballerina.shell.invoker.classload.ClassLoadInvoker;
import io.ballerina.shell.rt.InvokerMemory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Context that is used to populate the template
 * of {@link ClassLoadInvoker} objects.
 *
 * @since 2.0.0
 */
public class ClassLoadContext {
    private final String contextId;
    private final Collection<String> imports;
    private final Collection<String> moduleDclns;
    private final String lastVarDcln;
    private final Collection<VariableContext> varDclns;
    private final Collection<String> newVarNames;
    private final Collection<StatementContext> lastStmts;

    /**
     * Creates a context for class load invoker.
     * A simple data class which is bound to the template.
     * Of {@code varDclns} and {@code saveVarDclns}, the first value
     * should be the type descriptor. Second value should be the variable name.
     *
     * @param contextId   Id of the context to use in memory.
     * @param imports     Import declarations.
     * @param moduleDclns Module level declaration.
     * @param lastVarDcln Last variable declaration if the last snippet was a var dcln.
     *                    If not, this should be null.
     * @param varDclns    Variable declarations to initialize with values.
     * @param newVarNames New variables that were defined.
     *                    This should not have variables that are already in varDclns.
     * @param lastStmts   List of last expressions if last values were statements or expressions.
     */
    public ClassLoadContext(String contextId,
                            Collection<String> imports,
                            Collection<String> moduleDclns,
                            Collection<VariableContext> varDclns,
                            Collection<String> newVarNames,
                            String lastVarDcln,
                            Collection<StatementContext> lastStmts) {
        this.lastStmts = Objects.requireNonNullElse(lastStmts, List.of());
        this.lastVarDcln = Objects.requireNonNullElse(lastVarDcln, "");
        this.contextId = Objects.requireNonNull(contextId);
        this.imports = Objects.requireNonNull(imports);
        this.varDclns = Objects.requireNonNull(varDclns);
        this.newVarNames = Objects.requireNonNullElse(newVarNames, List.of());
        this.moduleDclns = Objects.requireNonNull(moduleDclns);
    }

    /**
     * Creates a context for class load invoker without new expression.
     *
     * @param contextId   Id of the context to use in memory.
     * @param imports     Import declarations.
     * @param moduleDclns Module level declaration.
     * @param newVarNames New variables that were defined.
     *                    This should not have variables that are already in varDclns.
     * @param lastVarDcln Last variable declaration if the last snippet was a var dcln.
     *                    If not, this should be null.
     * @param varDclns    VariableContext declarations to initialize with values.
     */
    public ClassLoadContext(String contextId,
                            Collection<String> imports,
                            Collection<String> moduleDclns,
                            Collection<VariableContext> varDclns,
                            Collection<String> newVarNames,
                            String lastVarDcln) {
        this(contextId, imports, moduleDclns, varDclns, newVarNames, lastVarDcln, null);
    }

    /**
     * Creates a context for class load invoker with only imports.
     *
     * @param contextId Id of the context to use in memory.
     * @param imports   Import declarations.
     */
    public ClassLoadContext(String contextId, Collection<String> imports) {
        this(contextId, imports, List.of(), List.of(), null, null);
    }

    public Collection<String> imports() {
        return imports;
    }

    public Collection<String> moduleDclns() {
        return moduleDclns;
    }

    public Collection<String> newVarNames() {
        return newVarNames;
    }

    public String lastVarDcln() {
        return lastVarDcln;
    }

    public Collection<StatementContext> lastStmts() {
        return lastStmts;
    }

    public Collection<VariableContext> varDclns() {
        return varDclns;
    }

    public String contextId() {
        return contextId;
    }

    public String exprVarName() {
        return ClassLoadInvoker.CONTEXT_EXPR_VAR_NAME;
    }

    public String memoryRef() {
        return InvokerMemory.class.getCanonicalName();
    }

    public boolean noExpressions() {
        return lastStmts.stream().allMatch(StatementContext::statement);
    }
}

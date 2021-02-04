/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.shell;

import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.NodeCloner;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ResolvedTypeBuilder;

import static org.ballerinalang.compiler.CompilerOptionName.SHELL_MODE;

/**
 * Performs processing required by the Ballerina Shell.
 *
 * @since 2.0.0
 */
public class ShellPlugin extends BLangNodeVisitor {
    private static final CompilerContext.Key<ShellPlugin> SHELL_PLUGIN_KEY =
            new CompilerContext.Key<>();
    private final CompilerOptions options;
    private final SymbolEnter symbolEnter;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Types types;
    private final Names names;
    private final NodeCloner nodeCloner;
    private final SemanticAnalyzer semanticAnalyzer;
    private final BLangAnonymousModelHelper anonModelHelper;
    private final ResolvedTypeBuilder typeBuilder;
    private final Boolean inShellMode;

    private ShellPlugin(CompilerContext context) {
        context.put(SHELL_PLUGIN_KEY, this);
        this.options = CompilerOptions.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.types = Types.getInstance(context);
        this.names = Names.getInstance(context);
        this.nodeCloner = NodeCloner.getInstance(context);
        this.semanticAnalyzer = SemanticAnalyzer.getInstance(context);
        this.anonModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.typeBuilder = new ResolvedTypeBuilder();
        this.inShellMode = options.isSet(SHELL_MODE)
                && Boolean.parseBoolean(this.options.get(SHELL_MODE));
    }

    public static ShellPlugin getInstance(CompilerContext context) {
        ShellPlugin shellPlugin = context.get(SHELL_PLUGIN_KEY);
        if (shellPlugin == null) {
            shellPlugin = new ShellPlugin(context);
        }

        return shellPlugin;
    }

    public BLangPackage perform(BLangPackage pkgNode) {
        if (!this.inShellMode) {
            return pkgNode;
        }

        return pkgNode;
    }
}

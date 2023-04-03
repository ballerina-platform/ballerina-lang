package io.ballerina.plugins.completions;

import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;

/**
 * Compiler plugin for testing completion providers.
 *
 * @since 2201.7.0
 */
public class CompilerPluginWithCompletionProviders extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCompletionProvider(new ServiceBodyContextProvider(ServiceDeclarationNode.class));
    }
}

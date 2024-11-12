package io.ballerina.projects;

import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.semantic.ast.TomlTableNode;

/**
 * Represents the 'Compiler-plugin.toml' file in a package.
 *
 * @since 2.0.0
 */
public class CompilerPluginToml {
    private final TomlDocumentContext compilerPluginTomlContext;
    private final Package packageInstance;

    private CompilerPluginToml(TomlDocumentContext compilerPluginTomlContext, Package packageInstance) {
        this.compilerPluginTomlContext = compilerPluginTomlContext;
        this.packageInstance = packageInstance;
    }

    public static CompilerPluginToml from(TomlDocumentContext compilerPluginTomlContext, Package pkg) {
        return new CompilerPluginToml(compilerPluginTomlContext, pkg);
    }

    TomlDocumentContext compilerPluginTomlContext() {
        return this.compilerPluginTomlContext;
    }

    public Package packageInstance() {
        return this.packageInstance;
    }

    public String name() {
        return ProjectConstants.COMPILER_PLUGIN_TOML;
    }

    public TomlTableNode tomlAstNode() {
        return tomlDocument().toml().rootNode();
    }

    public TomlDocument tomlDocument() {
        return this.compilerPluginTomlContext.tomlDocument();
    }

    /**
     * Returns an instance of the Document.Modifier.
     *
     * @return  module modifier
     */
    public CompilerPluginToml.Modifier modify() {
        return new CompilerPluginToml.Modifier(this);
    }

    /**
     * Inner class that handles Document modifications.
     */
    public static class Modifier {
        private TomlDocument tomlDocument;
        private final Package oldPackage;

        private Modifier(CompilerPluginToml oldDocument) {
            this.tomlDocument = oldDocument.tomlDocument();
            this.oldPackage = oldDocument.packageInstance();
        }

        /**
         * Sets the content to be changed.
         *
         * @param content content to change with
         * @return Document.Modifier that holds the content to be changed
         */
        public Modifier withContent(String content) {
            this.tomlDocument = TomlDocument.from(ProjectConstants.COMPILER_PLUGIN_TOML, content);
            return this;
        }

        /**
         * Returns a new document with updated content.
         *
         * @return document with updated content
         */
        public CompilerPluginToml apply() {
            CompilerPluginToml compilerPluginToml =
                    CompilerPluginToml.from(TomlDocumentContext.from(this.tomlDocument), oldPackage);
            Package newPackage = oldPackage.modify().updateCompilerPluginToml(compilerPluginToml).apply();
            return newPackage.compilerPluginToml().get();
        }
    }
}

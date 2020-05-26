module io.ballerina.language.server.commons {
    exports org.ballerinalang.langserver.commons;
    exports org.ballerinalang.langserver.commons.workspace;
    exports org.ballerinalang.langserver.commons.capability;
    exports org.ballerinalang.langserver.commons.codeaction;
    exports org.ballerinalang.langserver.commons.command;
    exports org.ballerinalang.langserver.commons.command.spi;
    exports org.ballerinalang.langserver.commons.completion;
    exports org.ballerinalang.langserver.commons.codeaction.spi;
    exports org.ballerinalang.langserver.commons.codelenses;
    exports org.ballerinalang.langserver.commons.codelenses.spi;
    requires org.eclipse.lsp4j;
    requires io.ballerina.lang;
    requires antlr4.runtime;
}
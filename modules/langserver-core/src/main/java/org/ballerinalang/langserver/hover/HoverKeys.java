package org.ballerinalang.langserver.hover;

import org.ballerinalang.langserver.LanguageServerContext;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

/**
 * Keys for the hover context.
 * */
public class HoverKeys {
    public static final LanguageServerContext.Key<BLangNode> HOVERING_OVER_NODE_KEY
            = new LanguageServerContext.Key<>();
    public static final LanguageServerContext.Key<PackageID> PACKAGE_OF_HOVER_NODE_KEY
            = new LanguageServerContext.Key<>();
    public static final LanguageServerContext.Key<BLangIdentifier> NAME_OF_HOVER_NODE_KEY
            = new LanguageServerContext.Key<>();
    public static final LanguageServerContext.Key<SymbolKind> SYMBOL_KIND_OF_HOVER_NODE_KEY
            = new LanguageServerContext.Key<>();
    public static final LanguageServerContext.Key<Object> PREVIOUSLY_VISITED_NODE_KEY
            = new LanguageServerContext.Key<>();
}

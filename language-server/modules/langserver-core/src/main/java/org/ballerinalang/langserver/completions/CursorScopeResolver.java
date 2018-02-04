package org.ballerinalang.langserver.completions;

import org.ballerinalang.langserver.completions.util.positioning.resolvers.BlockStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ConnectorScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.CursorPositionResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.PackageNodeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ResourceParamScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ServiceScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.TopLevelNodeScopeResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum for the cursor scope resolvers.
 */
enum CursorScopeResolver {

    BLOCK_STMT_SCOPE(BlockStatementScopeResolver.class, new BlockStatementScopeResolver()),
    CONNECTOR_DEF_SCOPE(ConnectorScopeResolver.class, new ConnectorScopeResolver()),
    PACKAGE_NODE_SCOPE(PackageNodeScopeResolver.class, new PackageNodeScopeResolver()),
    RESOURCE_PARAM_SCOPE(ResourceParamScopeResolver.class, new ResourceParamScopeResolver()),
    SERVICE_SCOPE(ServiceScopeResolver.class, new ServiceScopeResolver()),
    TOP_LEVEL_SCOPE(TopLevelNodeScopeResolver.class, new TopLevelNodeScopeResolver());

    private final Class context;
    private final CursorPositionResolver cursorPositionResolver;
    private static final Map<Class, CursorPositionResolver> resolverMap =
            Collections.unmodifiableMap(initializeMapping());

    CursorScopeResolver(Class context, CursorPositionResolver positionResolver) {
        this.context = context;
        this.cursorPositionResolver = positionResolver;
    }

    private Class getContext() {
        return context;
    }

    private CursorPositionResolver getCompletionItemResolver() {
        return cursorPositionResolver;
    }

    /**
     * Get the resolver by the class.
     * @param context - context class to extract the relevant resolver
     * @return {@link CursorPositionResolver} - Item resolver for the given context
     */
    public static CursorPositionResolver getResolverByClass(Class context) {
        return resolverMap.get(context);
    }

    private static Map<Class, CursorPositionResolver> initializeMapping() {
        Map<Class, CursorPositionResolver> map = new HashMap<>();
        for (CursorScopeResolver resolver : CursorScopeResolver.values()) {
            map.put(resolver.getContext(), resolver.getCompletionItemResolver());
        }
        return map;
    }
}

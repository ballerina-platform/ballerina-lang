/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import org.ballerinalang.langserver.completions.util.positioning.resolvers.BlockStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ConnectorScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.CursorPositionResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.InvocationParameterScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.MatchStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ObjectTypeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.PackageNodeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.RecordScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ResourceParamScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ServiceScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.TopLevelNodeScopeResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum for the cursor scope resolvers.
 */
public enum ScopeResolverConstants {
    BLOCK_STMT_SCOPE(BlockStatementScopeResolver.class, new BlockStatementScopeResolver()),
    INVOCATION_SCOPE(InvocationParameterScopeResolver.class, new InvocationParameterScopeResolver()),
    CONNECTOR_DEF_SCOPE(ConnectorScopeResolver.class, new ConnectorScopeResolver()),
    PACKAGE_NODE_SCOPE(PackageNodeScopeResolver.class, new PackageNodeScopeResolver()),
    RESOURCE_PARAM_SCOPE(ResourceParamScopeResolver.class, new ResourceParamScopeResolver()),
    SERVICE_SCOPE(ServiceScopeResolver.class, new ServiceScopeResolver()),
    TOP_LEVEL_SCOPE(TopLevelNodeScopeResolver.class, new TopLevelNodeScopeResolver()),
    MATCH_NODE_SCOPE(MatchStatementScopeResolver.class, new MatchStatementScopeResolver()),
    RECORD_NODE_SCOPE(RecordScopeResolver.class, new RecordScopeResolver()),
    OBJECT_TYPE_SCOPE(ObjectTypeScopeResolver.class, new ObjectTypeScopeResolver());

    private final Class context;
    private final CursorPositionResolver cursorPositionResolver;
    private static final Map<Class, CursorPositionResolver> resolverMap =
            Collections.unmodifiableMap(initializeMapping());

    ScopeResolverConstants(Class context, CursorPositionResolver positionResolver) {
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
        for (ScopeResolverConstants resolver : ScopeResolverConstants.values()) {
            map.put(resolver.getContext(), resolver.getCompletionItemResolver());
        }
        return map;
    }
}

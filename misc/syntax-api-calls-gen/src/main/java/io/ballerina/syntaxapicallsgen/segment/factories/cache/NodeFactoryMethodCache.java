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

package io.ballerina.syntaxapicallsgen.segment.factories.cache;

import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.syntaxapicallsgen.SyntaxApiCallsGenException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a cache of each method name of node factory to its reference to make later lookups faster.
 * This uses reflection to find the method.
 * If the method is not found throws an exception.
 *
 * @since 2.0.0
 */
public class NodeFactoryMethodCache {
    private final Map<String, NodeFactoryMethodReference> cache;

    private NodeFactoryMethodCache(Map<String, NodeFactoryMethodReference> cache) {
        this.cache = cache;
    }

    /**
     * Create {@link NodeFactoryMethodCache} for the given class.
     *
     * @return Created cache.
     */
    public static NodeFactoryMethodCache create() {
        Map<String, NodeFactoryMethodReference> methodCache = new HashMap<>();
        Method[] methodNames = NodeFactory.class.getMethods();
        for (Method method : methodNames) {
            methodCache.put(method.getName(), new NodeFactoryMethodReference(method));
        }
        return new NodeFactoryMethodCache(methodCache);
    }

    /**
     * Get the method reference of the given name.
     * Throws an error name is not found.
     *
     * @param name Name of the method.
     * @return Method reference of the method.
     */
    public NodeFactoryMethodReference getMethod(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        throw new SyntaxApiCallsGenException("Failed to find method " + name);
    }
}

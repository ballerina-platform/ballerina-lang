/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util.extension.holder;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.stream.input.source.SourceMapper;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Holder to store {@link SourceMapper} Extensions.
 */
public class SourceMapperExecutorExtensionHolder extends AbstractExtensionHolder {
    private static Class clazz = SourceMapper.class;

    private SourceMapperExecutorExtensionHolder(SiddhiAppContext siddhiAppContext) {
        super(clazz, siddhiAppContext);
    }

    public static SourceMapperExecutorExtensionHolder getInstance(SiddhiAppContext siddhiAppContext) {
        ConcurrentHashMap<Class, AbstractExtensionHolder> extensionHolderMap = siddhiAppContext.getSiddhiContext
                ().getExtensionHolderMap();
        AbstractExtensionHolder abstractExtensionHolder = extensionHolderMap.get(clazz);
        if (abstractExtensionHolder == null) {
            abstractExtensionHolder = new SourceMapperExecutorExtensionHolder(siddhiAppContext);
            extensionHolderMap.putIfAbsent(clazz, abstractExtensionHolder);
        }
        return (SourceMapperExecutorExtensionHolder) extensionHolderMap.get(clazz);
    }
}

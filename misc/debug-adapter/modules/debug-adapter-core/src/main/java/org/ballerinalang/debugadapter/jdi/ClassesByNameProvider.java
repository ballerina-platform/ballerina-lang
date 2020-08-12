/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.jdi;

import com.sun.jdi.ReferenceType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation for name based class provider.
 */
public interface ClassesByNameProvider {

    List<ReferenceType> get(String s);

    static ClassesByNameProvider createCache(List<ReferenceType> allTypes) {
        return new Cache(allTypes);
    }

    /**
     * Caching implementation for name based class provider.
     */
    final class Cache implements ClassesByNameProvider {

        private final ConcurrentHashMap<String, ReferenceType> myCache;

        public Cache(List<ReferenceType> classes) {
            myCache = new ConcurrentHashMap<>();
            classes.forEach(t -> myCache.put(t.signature(), t));
        }

        @Override
        public List<ReferenceType> get(String s) {
            String signature = VirtualMachineProxyImpl.JNITypeParserReflect.typeNameToSignature(s);
            if (signature != null) {
                return (List<ReferenceType>) myCache.get(signature);
            }
            return Collections.emptyList();
        }
    }
}

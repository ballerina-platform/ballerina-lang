/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.natives;

import org.ballerinalang.model.NativeScope;
import org.ballerinalang.util.repository.BuiltinPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * {@code BuiltInNativeConstructLoader} is responsible for loading built-in native constructs in the ballerina.
 * Constructs are loaded via java SPI. Each module implementing a native construct will register their
 * implementations as a service provider.
 * <p>
 *
 * @since 0.8.0
 */
public class BuiltInNativeConstructLoader {

    private static final Logger log = LoggerFactory.getLogger(BuiltInNativeConstructLoader.class);

    /**
     * Load the native constructs to the provided symbol scope.
     *
     * @param nativeScope Symbol scope to load native constructs
     */
    public static void loadConstructs(NativeScope nativeScope) {
        Iterator<NativeConstructLoader> nativeConstructLoaders =
                ServiceLoader.load(NativeConstructLoader.class).iterator();
        while (nativeConstructLoaders.hasNext()) {
            NativeConstructLoader constructLoader = nativeConstructLoaders.next();
            constructLoader.load(nativeScope);
        }
    }

    public static BuiltinPackageRepository[] loadPackageRepositories() {
        Iterator<BuiltinPackageRepository> ballerinaBuiltinPackageRepositories =
                ServiceLoader.load(BuiltinPackageRepository.class).iterator();
        List<BuiltinPackageRepository> pkgRepositories = new ArrayList<>();
        while (ballerinaBuiltinPackageRepositories.hasNext()) {
            BuiltinPackageRepository constructLoader = ballerinaBuiltinPackageRepositories.next();
            pkgRepositories.add(constructLoader);
        }
        if (!pkgRepositories.isEmpty()) {
            BuiltinPackageRepository internalRepo = null;
            for (BuiltinPackageRepository balRepo : pkgRepositories) {
                if (balRepo instanceof SystemPackageRepository) {
                    internalRepo = balRepo;
                    internalRepo.setSystemRepo(true);
                    break;
                }
            }
            for (BuiltinPackageRepository balRepo : pkgRepositories) {
                if (!(balRepo instanceof SystemPackageRepository)) {
                    balRepo.setSystemRepository(internalRepo);
                }
            }
        }
        return pkgRepositories.toArray(new BuiltinPackageRepository[0]);
    }

    public static void loadConstructs() {
    }
}

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
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.compiler.CompilerOptionName;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

/**
 * This class is responsible for finding packages with entry points
 * inside the project directory.
 *
 * @since 0.965.0
 */
public class EntryPointResolver {

    private static final CompilerContext.Key<EntryPointResolver> ENTRY_POINT_RESOLVER_KEY =
            new CompilerContext.Key<>();

    private CompilerOptions options;
    private PackageLoader packageLoader;
    private String projectDir;

    public static EntryPointResolver getInstance(CompilerContext context) {
        EntryPointResolver entryPointResolver = context.get(ENTRY_POINT_RESOLVER_KEY);
        if (entryPointResolver == null) {
            entryPointResolver = new EntryPointResolver(context);
        }
        return entryPointResolver;
    }

    private EntryPointResolver(CompilerContext context) {
        context.put(ENTRY_POINT_RESOLVER_KEY, this);

        this.options = CompilerOptions.getInstance(context);
        this.packageLoader = PackageLoader.getInstance(context);
        this.projectDir = options.get(CompilerOptionName.PROJECT_DIR);
    }

    public void getNextEntryPoint() {
        // Return the next entry point

        // Get the project root

        // Scan the file system
    }
}

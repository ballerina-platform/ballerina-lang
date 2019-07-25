/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.buildcontext;

/**
 * Key names for build context.
 */
public enum BuildContextField {
    SOURCE_ROOT("sourceRoot"),
    
    TARGET_DIR("targetDir"),
    
    OFFLINE_BUILD("offlineBuild"),
    
    LOCK_ENABLED("lockEnabled"),
    
    SKIP_TESTS("skipTests"),
    
    ENABLE_EXPERIMENTAL_FEATURES("enableExperimentalFeatures"),
    
    ENABLE_SIDDHI_RUNTIME("enableSiddhiRuntime"),
    
    COMPILED_MODULES("compiledModules"),
    
    COMPILER_CONTEXT("compilerContext"),
    
    BALO_CACHE_DIR("baloCacheDir"),
    
    BIR_CACHE_DIR("birCacheDir"),
    
    JAR_CACHE_DIR("jarCacheDir");
    
    public final String name;
    
    BuildContextField(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}

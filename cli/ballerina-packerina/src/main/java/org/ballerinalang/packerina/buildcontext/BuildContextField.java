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
    SOURCE_CONTEXT("sourceContext"),
    
    HOME_REPO("homeRepo"),
    
    HOME_BIR_CACHE_REPO("homeBirCacheRepo"),
    
    HOME_JAR_CACHE_REPO("homeJarCacheRepo"),
    
    SYSTEM_BIR_CACHE("systemBirCache"),
    
    SOURCE_ROOT("sourceRoot"),
    
    TARGET_DIR("targetDir"),
    
    COMPILER_CONTEXT("compilerContext"),

    JAR_RESOLVER("jarResolver"),
    
    BIR_CACHE_DIR("birCacheDir");
    
    public final String name;
    
    BuildContextField(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}

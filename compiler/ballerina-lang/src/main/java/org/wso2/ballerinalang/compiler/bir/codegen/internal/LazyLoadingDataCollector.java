/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.internal;

import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LazyLoadingDataCollector {

    // Map use to store global var name vs bb
    public final Map<String, LazyLoadBirBasicBlock> lazyLoadingBBMap;

    // Map use to store type name vs annotation bb
    public final Map<String, LazyLoadBirBasicBlock> lazyLoadingAnnotationsBBMap;

    public LazyLoadingDataCollector() {
        this.lazyLoadingBBMap = new HashMap<>();
        this.lazyLoadingAnnotationsBBMap = new HashMap<>();
    }
}

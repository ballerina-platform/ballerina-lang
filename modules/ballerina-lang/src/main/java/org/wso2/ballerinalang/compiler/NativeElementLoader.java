/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.natives.NativeElementRepository;
import org.ballerinalang.spi.NativeElementProvider;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ServiceLoader;

/**
 * @since 0.94
 */
public class NativeElementLoader {
    
    private NativeElementRepository nativeElementRepo;

    private static final CompilerContext.Key<NativeElementLoader> NATIVE_ELEMENT_LOADER_KEY =
            new CompilerContext.Key<>();

    public static NativeElementLoader getInstance(CompilerContext context) {
        NativeElementLoader codeGenerator = context.get(NATIVE_ELEMENT_LOADER_KEY);
        if (codeGenerator == null) {
            codeGenerator = new NativeElementLoader(context);
        }
        return codeGenerator;
    }

    public NativeElementLoader(CompilerContext context) {
        context.put(NATIVE_ELEMENT_LOADER_KEY, this);
        this.nativeElementRepo = new NativeElementRepository();
        ServiceLoader.load(NativeElementProvider.class).forEach(e -> e.populateNatives(this.nativeElementRepo));
    }

    public NativeElementRepository getNativeElementRepository() {
        return nativeElementRepo;
    }
    
}

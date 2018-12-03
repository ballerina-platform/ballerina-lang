/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.parser;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@link BLangAnonymousModelHelper} is a util for holding the number of anonymous constructs found so far in the
 * current package.
 *
 * @since 0.963.0
 */
public class BLangAnonymousModelHelper {

    private Map<PackageID, Integer> anonTypeCount;
    private Map<PackageID, Integer> anonFunctionCount;

    private static final String ANON_TYPE = "$anonType$";
    private static final String LAMBDA = "$lambda$";
    private static final String BUILTIN_ANON_TYPE = "$anonType$builtin$";
    private static final String BUILTIN_LAMBDA = "$lambda$builtin$";

    private static final CompilerContext.Key<BLangAnonymousModelHelper> ANONYMOUS_MODEL_HELPER_KEY =
            new CompilerContext.Key<>();

    private BLangAnonymousModelHelper(CompilerContext context) {
        context.put(ANONYMOUS_MODEL_HELPER_KEY, this);
        anonTypeCount = new HashMap<>();
        anonFunctionCount = new HashMap<>();
    }

    public static BLangAnonymousModelHelper getInstance(CompilerContext context) {
        BLangAnonymousModelHelper helper = context.get(ANONYMOUS_MODEL_HELPER_KEY);
        if (helper == null) {
            helper = new BLangAnonymousModelHelper(context);
        }
        return helper;
    }

    String getNextAnonymousTypeKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonTypeCount.get(packageID)).orElse(0);
        anonTypeCount.put(packageID, nextValue + 1);
        if (Names.BUILTIN_PACKAGE.equals(packageID.name)) {
            return BUILTIN_ANON_TYPE + nextValue;
        }
        return ANON_TYPE + nextValue;
    }

    public String getNextAnonymousFunctionKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonFunctionCount.get(packageID)).orElse(0);
        anonFunctionCount.put(packageID, nextValue + 1);
        if (Names.BUILTIN_PACKAGE.equals(packageID.name)) {
            return BUILTIN_LAMBDA + nextValue;
        }
        return LAMBDA + nextValue;
    }

    public boolean isAnonymousType(BSymbol symbol) {
        return symbol.name.value.startsWith(ANON_TYPE);
    }
}

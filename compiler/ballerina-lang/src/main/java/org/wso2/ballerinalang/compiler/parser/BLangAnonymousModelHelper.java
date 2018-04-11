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

    private Map<PackageID, Integer> anonStructCount;
    private Map<PackageID, Integer> anonRecordCount;
    private Map<PackageID, Integer> anonObjectCount;
    private Map<PackageID, Integer> anonFunctionCount;
    private Map<PackageID, Integer> anonSingletonCount;

    private static final String ANON_STRUCT = "$anonStruct$";
    private static final String ANON_RECORD = "$anonRecord$";
    private static final String ANON_OBJECT = "$anonObject$";
    private static final String ANON_SINGLETON = "$anonSingleton$";
    private static final String LAMBDA = "$lambda$";
    private static final String BUILTIN_ANON_STRUCT = "$anonStruct$builtin$";
    private static final String BUILTIN_ANON_RECORD = "$anonRecord$builtin$";
    private static final String BUILTIN_ANON_OBJECT = "$anonObject$builtin$";
    private static final String BUILTIN_ANON_SINGLETON = "$anonSingleton$builtin$";
    private static final String BUILTIN_LAMBDA = "$lambda$builtin$";

    private static final CompilerContext.Key<BLangAnonymousModelHelper> ANONYMOUS_MODEL_HELPER_KEY =
            new CompilerContext.Key<>();

    private BLangAnonymousModelHelper(CompilerContext context) {
        context.put(ANONYMOUS_MODEL_HELPER_KEY, this);
        anonStructCount = new HashMap<>();
        anonRecordCount = new HashMap<>();
        anonObjectCount = new HashMap<>();
        anonFunctionCount = new HashMap<>();
        anonSingletonCount = new HashMap<>();
    }

    public static BLangAnonymousModelHelper getInstance(CompilerContext context) {
        BLangAnonymousModelHelper helper = context.get(ANONYMOUS_MODEL_HELPER_KEY);
        if (helper == null) {
            helper = new BLangAnonymousModelHelper(context);
        }
        return helper;
    }

    public String getNextAnonymousStructKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonStructCount.get(packageID)).orElse(0);
        anonStructCount.put(packageID, nextValue + 1);
        if (Names.BUILTIN_PACKAGE.equals(packageID.name)) {
            return BUILTIN_ANON_STRUCT + nextValue;
        }
        return ANON_STRUCT + nextValue;
    }

    String getNextAnonymousRecordKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonRecordCount.get(packageID)).orElse(0);
        anonRecordCount.put(packageID, nextValue + 1);
        if (Names.BUILTIN_PACKAGE.equals(packageID.name)) {
            return BUILTIN_ANON_RECORD + nextValue;
        }
        return ANON_RECORD + nextValue;
    }

    String getNextAnonymousObjectKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonObjectCount.get(packageID)).orElse(0);
        anonObjectCount.put(packageID, nextValue + 1);
        if (Names.BUILTIN_PACKAGE.equals(packageID.name)) {
            return BUILTIN_ANON_OBJECT + nextValue;
        }
        return ANON_OBJECT + nextValue;
    }

    String getNextAnonymousSingletonKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonSingletonCount.get(packageID)).orElse(0);
        anonSingletonCount.put(packageID, nextValue + 1);
        if (Names.BUILTIN_PACKAGE.equals(packageID.name)) {
            return BUILTIN_ANON_SINGLETON + nextValue;
        }
        return ANON_SINGLETON + nextValue;
    }

    public String getNextAnonymousFunctionKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonFunctionCount.get(packageID)).orElse(0);
        anonFunctionCount.put(packageID, nextValue + 1);
        if (Names.BUILTIN_PACKAGE.equals(packageID.name)) {
            return BUILTIN_LAMBDA + nextValue;
        }
        return LAMBDA + nextValue;
    }

}

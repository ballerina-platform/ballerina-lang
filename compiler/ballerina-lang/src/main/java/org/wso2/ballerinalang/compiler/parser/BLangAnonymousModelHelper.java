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
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;

/**
 * {@link BLangAnonymousModelHelper} is a util for holding the number of anonymous constructs found so far in the
 * current package.
 *
 * @since 0.963.0
 */
public class BLangAnonymousModelHelper {

    private Map<PackageID, Integer> anonTypeCount;
    private Map<PackageID, Integer> anonServiceCount;
    private Map<PackageID, Integer> anonFunctionCount;
    private Map<PackageID, Integer> anonForkCount;
    private Map<PackageID, Integer> distinctTypeIdCount;
    private Map<PackageID, Integer> rawTemplateTypeCount;
    private Map<PackageID, Integer> tupleVarCount;
    private Map<PackageID, Integer> recordVarCount;
    private Map<PackageID, Integer> errorVarCount;
    private Map<PackageID, Integer> intersectionRecordCount;
    private Map<PackageID, Integer> intersectionErrorCount;

    public static final String ANON_PREFIX = "$anon";
    private static final String ANON_TYPE = ANON_PREFIX + "Type$";
    public static final String LAMBDA = "$lambda$";
    private static final String SERVICE = "$$service$";
    private static final String ANON_SERVICE = ANON_PREFIX + "Service$";
    private static final String BUILTIN_ANON_TYPE = ANON_PREFIX + "Type$builtin$";
    private static final String BUILTIN_LAMBDA = "$lambda$builtin$";
    private static final String FORK = "$fork$";
    private static final String RAW_TEMPLATE_TYPE = "$rawTemplate$";
    private static final String ANON_INTERSECTION_RECORD = ANON_PREFIX + "IntersectionRecordType$";
    private static final String ANON_INTERSECTION_ERROR_TYPE = ANON_PREFIX + "IntersectionErrorType$";
    private static final String TUPLE_VAR = "$tupleVar$";
    private static final String RECORD_VAR = "$recordVar$";
    private static final String ERROR_VAR = "$errorVar$";

    private static final CompilerContext.Key<BLangAnonymousModelHelper> ANONYMOUS_MODEL_HELPER_KEY =
            new CompilerContext.Key<>();

    private BLangAnonymousModelHelper(CompilerContext context) {
        context.put(ANONYMOUS_MODEL_HELPER_KEY, this);
        anonTypeCount = new HashMap<>();
        anonServiceCount = new HashMap<>();
        anonFunctionCount = new HashMap<>();
        anonForkCount = new HashMap<>();
        rawTemplateTypeCount = new HashMap<>();
        tupleVarCount = new HashMap<>();
        recordVarCount = new HashMap<>();
        errorVarCount = new HashMap<>();
        intersectionRecordCount = new HashMap<>();
        intersectionErrorCount = new HashMap<>();
        distinctTypeIdCount = new HashMap<>();
    }

    public static BLangAnonymousModelHelper getInstance(CompilerContext context) {
        BLangAnonymousModelHelper helper = context.get(ANONYMOUS_MODEL_HELPER_KEY);
        if (helper == null) {
            helper = new BLangAnonymousModelHelper(context);
        }
        return helper;
    }

    public String getNextAnonymousTypeKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonTypeCount.get(packageID)).orElse(0);
        anonTypeCount.put(packageID, nextValue + 1);
        if (PackageID.ANNOTATIONS.equals(packageID)) {
            return BUILTIN_ANON_TYPE + UNDERSCORE + nextValue;
        }
        return ANON_TYPE + UNDERSCORE + nextValue;
    }

    String getNextAnonymousServiceTypeKey(PackageID packageID, String serviceName) {
        Integer nextValue = Optional.ofNullable(anonServiceCount.get(packageID)).orElse(0);
        anonServiceCount.put(packageID, nextValue + 1);
        return serviceName + SERVICE + UNDERSCORE + nextValue;
    }

    String getNextAnonymousServiceVarKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonServiceCount.get(packageID)).orElse(0);
        anonServiceCount.put(packageID, nextValue + 1);
        return ANON_SERVICE + UNDERSCORE + nextValue;
    }

    public String getNextAnonymousFunctionKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonFunctionCount.get(packageID)).orElse(0);
        anonFunctionCount.put(packageID, nextValue + 1);
        return LAMBDA + UNDERSCORE + nextValue;
    }

    public String getNextAnonymousForkKey(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(anonFunctionCount.get(packageID)).orElse(0);
        anonFunctionCount.put(packageID, nextValue + 1);
        return FORK + UNDERSCORE + nextValue;
    }

    public String getNextAnonymousTypeId(PackageID packageID) {
        Integer nextValue = Optional.ofNullable(distinctTypeIdCount.get(packageID)).orElse(0);
        distinctTypeIdCount.put(packageID, nextValue + 1);
        return nextValue.toString();
    }

    public String getNextRawTemplateTypeKey(PackageID packageID, Name rawTemplateTypeName) {
        Integer nextValue = rawTemplateTypeCount.getOrDefault(packageID, 0);
        rawTemplateTypeCount.put(packageID, nextValue + 1);
        return RAW_TEMPLATE_TYPE + rawTemplateTypeName.value + "$" + UNDERSCORE + nextValue;
    }

    public String getNextTupleVarKey(PackageID packageID) {
        Integer nextValue = tupleVarCount.getOrDefault(packageID, 0);
        tupleVarCount.put(packageID, nextValue + 1);
        return TUPLE_VAR + UNDERSCORE + nextValue;
    }

    public String getNextRecordVarKey(PackageID packageID) {
        Integer nextValue = recordVarCount.getOrDefault(packageID, 0);
        recordVarCount.put(packageID, nextValue + 1);
        return RECORD_VAR + UNDERSCORE + nextValue;
    }

    public String getNextErrorVarKey(PackageID packageID) {
        Integer nextValue = errorVarCount.getOrDefault(packageID, 0);
        errorVarCount.put(packageID, nextValue + 1);
        return ERROR_VAR + UNDERSCORE + nextValue;
    }

    public boolean isAnonymousType(BSymbol symbol) {
        return symbol.name.value.startsWith(ANON_TYPE);
    }

    public String getNextAnonymousIntersectionErrorDetailTypeName(PackageID packageID) {
        Integer nextValue = intersectionRecordCount.getOrDefault(packageID, 0);
        intersectionRecordCount.put(packageID, nextValue + 1);
        return ANON_INTERSECTION_RECORD + UNDERSCORE + nextValue;
    }

    public String getNextAnonymousIntersectionErrorTypeName(PackageID packageID) {
        Integer nextValue = intersectionErrorCount.getOrDefault(packageID, 0);
        intersectionErrorCount.put(packageID, nextValue + 1);
        return ANON_INTERSECTION_ERROR_TYPE + UNDERSCORE + nextValue;
    }
}

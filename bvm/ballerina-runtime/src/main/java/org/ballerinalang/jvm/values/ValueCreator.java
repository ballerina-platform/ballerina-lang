/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.api.BString;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.jvm.util.BLangConstants.ANON_ORG;
import static org.ballerinalang.jvm.util.BLangConstants.DOT;
import static org.ballerinalang.jvm.util.BLangConstants.EMPTY;
import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;
import static org.ballerinalang.jvm.util.BLangConstants.VERSION_SEPARATOR;

/**
 * A {@code ValueCreator} is an API that will be implemented by all the module init classed from jvm codegen.
 * This provides facility for creating runtime record, object, error values.
 *
 * @since 0.995.0
 */
public abstract class ValueCreator {

    private static final Map<String, ValueCreator> runtimeValueCreators = new HashMap<>();

    public static void addValueCreator(String orgName, String moduleName, String moduleVersion,
                                       ValueCreator valueCreator) {
        String key = getLookupKey(orgName, moduleName, moduleVersion);

        if (!key.equals(DOT) && runtimeValueCreators.containsKey(key)) {
            // silently fail
            return;
        }

        runtimeValueCreators.put(key, valueCreator);
    }

    private static String getLookupKey(String orgName, String moduleName, String version) {
        if (DOT.equals(moduleName)) {
            return moduleName;
        }

        String pkgName = "";
        if (orgName != null && !orgName.equals(ANON_ORG)) {
            pkgName = orgName + ORG_NAME_SEPARATOR;
        }

        if (version == null || version.equals(EMPTY)) {
            return pkgName + moduleName;
        }

        return pkgName + moduleName + VERSION_SEPARATOR + version;
    }

    public static ValueCreator getValueCreator(String key) {
        if (!runtimeValueCreators.containsKey(key)) {
            throw new BallerinaException("Value creator object is not available");
        }

        return runtimeValueCreators.get(key);
    }

    public abstract MapValue<BString, Object> createRecordValue(String recordTypeName);

    public abstract ObjectValue createObjectValue(String objectTypeName, Scheduler scheduler, Strand parent,
                                                  Map<String, Object> properties, Object[] args);
}

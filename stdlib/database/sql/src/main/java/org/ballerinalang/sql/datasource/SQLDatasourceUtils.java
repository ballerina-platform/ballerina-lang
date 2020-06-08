/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.sql.datasource;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class contains utility methods for SQL datasource operations.
 *
 * @since 1.2.0
 */
public class SQLDatasourceUtils {

    private static final String POOL_MAP_KEY = UUID.randomUUID().toString();

    static Map<PoolKey, SQLDatasource> retrieveDatasourceContainer(
            MapValue<BString, Object> poolOptions) {
        return (ConcurrentHashMap<PoolKey, SQLDatasource>) poolOptions.getNativeData(POOL_MAP_KEY);
    }

    public static synchronized Map<PoolKey, SQLDatasource> putDatasourceContainer(
            MapValue<BString, Object> poolOptions,
            ConcurrentHashMap<PoolKey, SQLDatasource> datasourceMap) {
        Map<PoolKey, SQLDatasource> existingDataSourceMap =
                (Map<PoolKey, SQLDatasource>) poolOptions.getNativeData(POOL_MAP_KEY);
        if (existingDataSourceMap != null) {
            return existingDataSourceMap;
        }
        poolOptions.addNativeData(POOL_MAP_KEY, datasourceMap);
        return datasourceMap;
    }

    static boolean isSupportedDbOptionType(Object value) {
        boolean supported = false;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            int typeTag = type.getTag();
            supported = (typeTag == TypeTags.STRING_TAG || typeTag == TypeTags.INT_TAG || typeTag == TypeTags.FLOAT_TAG
                    || typeTag == TypeTags.BOOLEAN_TAG || typeTag == TypeTags.DECIMAL_TAG
                    || typeTag == TypeTags.BYTE_TAG);
        }
        return supported;
    }
}

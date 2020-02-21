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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinax.jdbc.datasource;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.MapValue;

import java.util.Map;

/**
 * The key that uniquely identifies a connection pool encapsulated by {@link SQLDatasource}.
 *
 * @since 1.0.0
 */
public class PoolKey {
    private String jdbcUrl;
    private MapValue<String, ?> dbOptions;

    public PoolKey(String jdbcUrl, MapValue<String, ?> dbOptions) {
        this.jdbcUrl = jdbcUrl;
        this.dbOptions = dbOptions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PoolKey)) {
            return false;
        }
        boolean jdbcUrlEqual = ((PoolKey) obj).jdbcUrl.equals(this.jdbcUrl);
        return jdbcUrlEqual && dbOptionsEqual((PoolKey) obj);
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = hashCode * 31 + jdbcUrl.hashCode();
        if (dbOptions != null) {
            hashCode = 31 * hashCode + calculateDbOptionsHashCode();
        }
        return hashCode;
    }

    private int calculateDbOptionsHashCode() {
        int hashCode = 17;
        for (Map.Entry<String, ?> entry : dbOptions.entrySet()) {
            int keyHashCode = entry.getKey().hashCode();
            Object value = entry.getValue();
            BType type = TypeChecker.getType(value);
            int typeTag = type.getTag();
            int valueHashCode;
            switch (typeTag) {
            case TypeTags.STRING_TAG:
            case TypeTags.DECIMAL_TAG:
                valueHashCode = value.hashCode();
                break;
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                long longValue = (Long) value;
                valueHashCode = (int) (longValue ^ (longValue >>> 32));
                break;
            case TypeTags.FLOAT_TAG:
                long longValueConvertedFromDouble = Double.doubleToLongBits((Double) value);
                valueHashCode = (int) (longValueConvertedFromDouble ^ (longValueConvertedFromDouble >>> 32));
                break;
            case TypeTags.BOOLEAN_TAG:
                valueHashCode = ((Boolean) value ? 1 : 0);
                break;
            default:
                throw new AssertionError("type " + type.getName() + " shouldn't have occurred");
            }
            hashCode = hashCode + keyHashCode + valueHashCode;
        }
        return hashCode;
    }

    private boolean dbOptionsEqual(PoolKey anotherPoolKey) {
        MapValue<String, ?> anotherDbOptions = anotherPoolKey.dbOptions;
        if (dbOptions == null && anotherDbOptions == null) {
            return true;
        }
        if (dbOptions == null || anotherDbOptions == null) {
            return false;
        }
        if (this.dbOptions.size() != anotherDbOptions.size()) {
            return false;
        }
        for (Map.Entry<String, ?> entry : dbOptions.entrySet()) {
            if (!entry.getValue().equals(anotherDbOptions.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }
}

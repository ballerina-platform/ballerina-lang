/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
 *
 */

package io.ballerina.runtime.internal.types.semtype;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DecimalSubType implements SubType {

    final SubTypeData data;

    private DecimalSubType(SubTypeData data) {
        this.data = data;
    }

    private static final DecimalSubType ALL = new DecimalSubType(AllOrNothing.ALL);
    private static final DecimalSubType NOTHING = new DecimalSubType(AllOrNothing.NOTHING);

    public static DecimalSubType createDecimalSubType(boolean allowed, BigDecimal[] values) {
        if (values.length == 0) {
            if (!allowed) {
                return ALL;
            } else {
                return NOTHING;
            }
        }
        Arrays.sort(values);
        return new DecimalSubType(new DecimalSubTypeData(allowed, values));
    }

    @Override
    public SubType union(SubType otherSubtype) {
        DecimalSubType other = (DecimalSubType) otherSubtype;
        if (data instanceof AllOrNothing) {
            if (data == AllOrNothing.ALL) {
                return this;
            } else {
                return other;
            }
        } else if (other.data instanceof AllOrNothing) {
            if (other.data == AllOrNothing.ALL) {
                return other;
            } else {
                return this;
            }
        }
        List<BigDecimal> values = new ArrayList<>();
        DecimalSubTypeData data = (DecimalSubTypeData) this.data;
        DecimalSubTypeData otherData = (DecimalSubTypeData) other.data;
        boolean allowed = data.union(otherData, values);
        return createDecimalSubType(allowed, values.toArray(new BigDecimal[0]));
    }

    @Override
    public SubType intersect(SubType otherSubtype) {
        DecimalSubType other = (DecimalSubType) otherSubtype;
        if (data instanceof AllOrNothing) {
            if (data == AllOrNothing.ALL) {
                return other;
            } else {
                return NOTHING;
            }
        } else if (other.data instanceof AllOrNothing) {
            if (other.data == AllOrNothing.ALL) {
                return this;
            } else {
                return NOTHING;
            }
        }
        List<BigDecimal> values = new ArrayList<>();
        DecimalSubTypeData data = (DecimalSubTypeData) this.data;
        DecimalSubTypeData otherData = (DecimalSubTypeData) other.data;
        boolean allowed = data.intersect(otherData, values);
        return createDecimalSubType(allowed, values.toArray(new BigDecimal[0]));
    }

    @Override
    public SubType complement() {
        if (data == AllOrNothing.ALL) {
            return NOTHING;
        } else if (data == AllOrNothing.NOTHING) {
            return ALL;
        }
        DecimalSubTypeData data = (DecimalSubTypeData) this.data;
        return createDecimalSubType(!data.allowed, data.values);
    }

    @Override
    public boolean isEmpty() {
        return data == AllOrNothing.NOTHING;
    }

    public BigDecimal defaultValue() {
        if (data instanceof DecimalSubTypeData subTypeData && subTypeData.allowed && subTypeData.values.length == 1) {
            return subTypeData.values[0];
        }
        return null;
    }

    @Override
    public String toString() {
        if (data instanceof DecimalSubTypeData subTypeData && subTypeData.allowed) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < subTypeData.values.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(subTypeData.values[i]);
            }
            return sb.toString();
        }
        return "decimal";
    }

    static class DecimalSubTypeData extends EnumerableSubtypeData<BigDecimal> implements SubTypeData {

        private final boolean allowed;
        private final BigDecimal[] values;

        DecimalSubTypeData(boolean allowed, BigDecimal[] values) {
            this.allowed = allowed;
            this.values = values;
        }

        @Override
        boolean allowed() {
            return allowed;
        }

        @Override
        BigDecimal[] values() {
            return values;
        }
    }
}

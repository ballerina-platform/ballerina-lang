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

import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runtime representation of DecimalSubType.
 *
 * @since 2201.10.0
 */
public final class BDecimalSubType extends SubType {

    final SubTypeData data;

    private BDecimalSubType(SubTypeData data) {
        super(data == AllOrNothing.ALL, data == AllOrNothing.NOTHING);
        this.data = data;
    }

    private static final BDecimalSubType ALL = new BDecimalSubType(AllOrNothing.ALL);
    private static final BDecimalSubType NOTHING = new BDecimalSubType(AllOrNothing.NOTHING);

    public static BDecimalSubType createDecimalSubType(boolean allowed, BigDecimal[] values) {
        if (values.length == 0) {
            if (!allowed) {
                return ALL;
            } else {
                return NOTHING;
            }
        }
        Arrays.sort(values);
        return new BDecimalSubType(new DecimalSubTypeData(allowed, values));
    }

    @Override
    public SubType union(SubType otherSubtype) {
        BDecimalSubType other = (BDecimalSubType) otherSubtype;
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
        return createDecimalSubType(allowed, values.toArray(BigDecimal[]::new));
    }

    @Override
    public SubType intersect(SubType otherSubtype) {
        BDecimalSubType other = (BDecimalSubType) otherSubtype;
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
        return createDecimalSubType(allowed, values.toArray(BigDecimal[]::new));
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
    public boolean isEmpty(Context cx) {
        return data == AllOrNothing.NOTHING;
    }

    @Override
    public SubTypeData data() {
        return data;
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

    private static final class DecimalSubTypeData extends EnumerableSubtypeData<BigDecimal> implements SubTypeData {

        private final boolean allowed;
        private final BigDecimal[] values;

        private DecimalSubTypeData(boolean allowed, BigDecimal[] values) {
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

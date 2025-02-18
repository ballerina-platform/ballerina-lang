/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runtime representation of FloatSubType.
 *
 * @since 2201.12.0
 */
public final class BFloatSubType extends SubType {

    final SubTypeData data;

    private BFloatSubType(SubTypeData data) {
        super(data == AllOrNothing.ALL, data == AllOrNothing.NOTHING);
        this.data = data;
    }

    private static final BFloatSubType ALL = new BFloatSubType(AllOrNothing.ALL);
    private static final BFloatSubType NOTHING = new BFloatSubType(AllOrNothing.NOTHING);

    public static BFloatSubType createFloatSubType(boolean allowed, Double[] values) {
        if (values.length == 0) {
            if (!allowed) {
                return ALL;
            } else {
                return NOTHING;
            }
        }
        return new BFloatSubType(new FloatSubTypeData(allowed, values));
    }

    @Override
    public SubType union(SubType otherSubtype) {
        BFloatSubType other = (BFloatSubType) otherSubtype;
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
        List<Double> values = new ArrayList<>();
        FloatSubTypeData data = (FloatSubTypeData) this.data;
        FloatSubTypeData otherData = (FloatSubTypeData) other.data;
        boolean allowed = data.union(otherData, values);
        return createFloatSubType(allowed, values.toArray(Double[]::new));
    }

    @Override
    public SubType intersect(SubType otherSubtype) {
        BFloatSubType other = (BFloatSubType) otherSubtype;
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
        List<Double> values = new ArrayList<>();
        FloatSubTypeData data = (FloatSubTypeData) this.data;
        FloatSubTypeData otherData = (FloatSubTypeData) other.data;
        boolean allowed = data.intersect(otherData, values);
        return createFloatSubType(allowed, values.toArray(Double[]::new));
    }

    @Override
    public SubType complement() {
        if (data == AllOrNothing.ALL) {
            return NOTHING;
        } else if (data == AllOrNothing.NOTHING) {
            return ALL;
        }
        FloatSubTypeData data = (FloatSubTypeData) this.data;
        return createFloatSubType(!data.allowed, data.values);
    }

    @Override
    public boolean isEmpty(Context cx) {
        return data == AllOrNothing.NOTHING;
    }

    @Override
    public SubTypeData data() {
        return data;
    }

    static final class FloatSubTypeData extends EnumerableSubtypeData<Double> implements SubTypeData {

        private final boolean allowed;
        private final Double[] values;

        private FloatSubTypeData(boolean allowed, Double[] values) {
            this.allowed = allowed;
            this.values = filteredValues(values);
        }

        private static Double[] filteredValues(Double[] values) {
            for (int i = 0; i < values.length; i++) {
                values[i] = canon(values[i]);
            }
            if (values.length < 2) {
                return values;
            }
            Arrays.sort(values);
            Double[] buffer = new Double[values.length];
            buffer[0] = values[0];
            int bufferLen = 1;
            for (int i = 1; i < values.length; i++) {
                Double value = values[i];
                Double prevValue = values[i - 1];
                if (isSame(value, prevValue)) {
                    continue;
                }
                buffer[bufferLen++] = value;
            }
            return Arrays.copyOf(buffer, bufferLen);
        }

        private static Double canon(Double d) {
            if (d.equals(0.0) || d.equals(-0.0)) {
                return 0.0;
            }
            return d;
        }

        private static boolean isSame(double f1, double f2) {
            if (Double.isNaN(f1)) {
                return Double.isNaN(f2);
            }
            return f1 == f2;
        }

        @Override
        public boolean allowed() {
            return allowed;
        }

        @Override
        public Double[] values() {
            return values;
        }
    }
}

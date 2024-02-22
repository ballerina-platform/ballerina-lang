/*
 *
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 * /
 *
 */

package io.ballerina.runtime.internal.types.semType;

import io.ballerina.runtime.internal.types.BType;

import java.util.BitSet;

import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.N_TYPES;

public final class SemTypeUtils {

    public static final class UniformTypeCodes {

        public final static int UT_BOOLEAN = 0x01;
        public final static int UT_BTYPE = 0x02;

        public final static int N_TYPES = 3;
    }

    public static final class SemTypeBuilder {

        public static BSemType from(int uniformTypeCode) {
            if (uniformTypeCode != SemTypeUtils.UniformTypeCodes.UT_BOOLEAN) {
                throw new IllegalStateException("This method can only be used to create a semType for a BType");
            }
            BitSet all = new BitSet();
            BitSet some = new BitSet();
            all.set(uniformTypeCode);
            ProperSubTypeData[] subTypeData = new ProperSubTypeData[N_TYPES];
            return new BSemType(all, some, subTypeData);
        }

        public static BSemType from(BType bType) {
            BitSet all = new BitSet();
            BitSet some = new BitSet();
            some.set(UniformTypeCodes.UT_BTYPE);
            // TODO: this means we always have an extra null at the beginning
            ProperSubTypeData[] subTypeData = new ProperSubTypeData[N_TYPES];
            subTypeData[UniformTypeCodes.UT_BTYPE] = bType;
            return new BSemType(all, some, subTypeData);
        }
    }

    public static final class TypeOperation {

        public static BSemType union(BSemType t1, BSemType t2) {
            if (t1.some.isEmpty()) {
                if (t2.some.isEmpty()) {
                    throw new RuntimeException("unimplemented");
                }
                throw new RuntimeException("unimplemented");
            }
            BSemType semtype = new BSemType();

            semtype.all.or(t1.all);
            semtype.all.or(t2.all);

            semtype.some.or(t1.some);
            semtype.some.or(t2.some);
            semtype.some.andNot(semtype.all);

            ProperSubTypeData[] subTypeData = semtype.subTypeData;
            for (int i = 0; i < N_TYPES; i++) {
                boolean t1Has = t1.some.get(i);
                boolean t2Has = t2.some.get(i);
                if (t1Has && t2Has) {
                    subTypeData[i] = t1.subTypeData[i].union(t2.subTypeData[i]);
                } else if (t1Has) {
                    subTypeData[i] = t1.subTypeData[i];
                } else if (t2Has) {
                    subTypeData[i] = t2.subTypeData[i];
                }
            }
            return semtype;
        }

        public static BSemType intersection(BSemType t1, BSemType t2) {
            // TODO: not sure how to do this with BSubTypes
            throw new RuntimeException("unimplemented");
        }
    }
}

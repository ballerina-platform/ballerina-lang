/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.ballerinalang.compiler.bir.writer;

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.Objects;

/**
 * An entry in the constant pool(cp).
 *
 * @since 0.980.0
 */
public class CPEntry {

    Type entryType;

    CPEntry(Type entryType) {
        this.entryType = entryType;
    }

    /**
     * An integer constant-pool entry.
     *
     * @since 0.980.0
     */
    public static class IntegerCPEntry extends CPEntry {
        public long value;

        public IntegerCPEntry(long value) {
            super(Type.CP_ENTRY_INTEGER);
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            IntegerCPEntry that = (IntegerCPEntry) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    /**
     * A float constant-pool entry.
     *
     * @since 0.980.0
     */
    public static class FloatCPEntry extends CPEntry {
        public double value;

        public FloatCPEntry(double value) {
            super(Type.CP_ENTRY_FLOAT);
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FloatCPEntry that = (FloatCPEntry) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    /**
     * A boolean constant-pool entry.
     *
     * @since 0.980.0
     */
    public static class BooleanCPEntry extends CPEntry {
        public boolean value;

        public BooleanCPEntry(boolean value) {
            super(Type.CP_ENTRY_BOOLEAN);
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BooleanCPEntry that = (BooleanCPEntry) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    /**
     * A string constant-pool entry.
     *
     * @since 0.980.0
     */
    public static class StringCPEntry extends CPEntry {
        public String value;

        public StringCPEntry(String value) {
            super(Type.CP_ENTRY_STRING);
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            StringCPEntry that = (StringCPEntry) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    /**
     * A byte constant-pool entry.
     *
     * @since 0.995.0
     */
    public static class ByteCPEntry extends CPEntry {
        public int value;

        public ByteCPEntry(int value) {
            super(Type.CP_ENTRY_BYTE);
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ByteCPEntry that = (ByteCPEntry) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    /**
     * A package constant-pool entry.
     *
     * @since 0.980.0
     */
    public static class PackageCPEntry extends CPEntry {
        public int orgNameCPIndex;
        public int pkgNameCPIndex;
        public int versionCPIndex;

        public PackageCPEntry(int orgNameCPIndex, int pkgNameCPIndex, int versionCPIndex) {
            super(Type.CP_ENTRY_PACKAGE);
            this.orgNameCPIndex = orgNameCPIndex;
            this.pkgNameCPIndex = pkgNameCPIndex;
            this.versionCPIndex = versionCPIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PackageCPEntry that = (PackageCPEntry) o;
            return orgNameCPIndex == that.orgNameCPIndex &&
                    pkgNameCPIndex == that.pkgNameCPIndex &&
                    versionCPIndex == that.versionCPIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(orgNameCPIndex, pkgNameCPIndex, versionCPIndex);
        }
    }

    /**
     * {@code Type} represents the type of a CP entry.
     *
     * @since 0.87
     */
    public enum Type {
        CP_ENTRY_INTEGER((byte) 1),
        CP_ENTRY_FLOAT((byte) 2),
        CP_ENTRY_BOOLEAN((byte) 3),
        CP_ENTRY_STRING((byte) 4),
        CP_ENTRY_PACKAGE((byte) 5),
        CP_ENTRY_BYTE((byte) 6),
        CP_ENTRY_SHAPE((byte) 7);

        byte value;

        Type(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * A type shape constant-pool entry.
     *
     * @since 0.995.0
     */
    public static class ShapeCPEntry extends CPEntry {

        public final BType shape;

        public ShapeCPEntry(BType shape) {
            super(Type.CP_ENTRY_SHAPE);
            this.shape = shape;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ShapeCPEntry that = (ShapeCPEntry) o;
            return Objects.equals(shape, that.shape);
        }

        @Override
        public int hashCode() {
            return Objects.hash(shape);
        }
    }
}

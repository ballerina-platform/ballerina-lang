/*
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
 */

package io.ballerina.runtime.internal.types.semtype;

/**
 * Runtime representation of BooleanSubType.
 *
 * @since 2201.10.0
 */
public class BooleanSubType implements SubType {

    final SubTypeData data;
    private static final BooleanSubType NOTHING = new BooleanSubType(AllOrNothing.NOTHING);
    private static final BooleanSubType ALL = new BooleanSubType(AllOrNothing.ALL);

    public BooleanSubType(boolean data) {
        this.data = new BooleanSubTypeData(data);
    }

    private BooleanSubType(AllOrNothing data) {
        this.data = data;
    }

    // TODO: refactor
    @Override
    public SubType union(SubType other) {
        if (other instanceof BooleanSubType otherBoolean) {
            if (this.data instanceof BooleanSubTypeData thisdata) {
                if (otherBoolean.data instanceof BooleanSubTypeData otherdata) {
                    if (thisdata.value == otherdata.value) {
                        return this;
                    }
                    return ALL;
                } else if (otherBoolean.data instanceof AllOrNothing allOrNothing) {
                    if (allOrNothing == AllOrNothing.ALL) {
                        return other;
                    }
                    return this;
                }
                throw new IllegalStateException("unreachable");
            } else if (this.data instanceof AllOrNothing allOrNothing) {
                if (allOrNothing == AllOrNothing.ALL) {
                    return this;
                }
                return other;
            }
            throw new IllegalStateException("unreachable");
        }
        throw new UnsupportedOperationException("union of different subtypes");
    }

    boolean defaultValue() {
        if (data instanceof AllOrNothing allOrNothing) {
            if (allOrNothing == AllOrNothing.ALL) {
                return false;
            } else {
                throw new IllegalStateException("default value of nothing");
            }
        }
        return ((BooleanSubTypeData) data).value;
    }

    // TODO: refactor
    @Override
    public SubType intersect(SubType other) {
        if (other instanceof BooleanSubType otherBoolean) {
            if (this.data instanceof BooleanSubTypeData thisdata) {
                if (otherBoolean.data instanceof BooleanSubTypeData otherdata) {
                    if (otherdata.value == thisdata.value) {
                        return this;
                    }
                    return NOTHING;
                } else if (otherBoolean.data instanceof AllOrNothing allOrNothing) {
                    if (allOrNothing == AllOrNothing.NOTHING) {
                        return other;
                    }
                    return this;
                }
            } else if (this.data instanceof AllOrNothing allOrNothing) {
                if (allOrNothing == AllOrNothing.NOTHING) {
                    return this;
                }
                return other;
            }
            throw new IllegalStateException("unreachable");
        } else {
            throw new UnsupportedOperationException("intersection of different subtypes");
        }
    }

    // TODO: refactor
    @Override
    public SubType diff(SubType other) {
        if (other instanceof BooleanSubType otherBoolean) {
            if (this.data instanceof BooleanSubTypeData thisdata) {
                if (otherBoolean.data instanceof BooleanSubTypeData otherdata) {
                    if (otherdata.value == thisdata.value) {
                        return NOTHING;
                    }
                    return this;
                } else if (otherBoolean.data instanceof AllOrNothing allOrNothing) {
                    if (allOrNothing == AllOrNothing.NOTHING) {
                        return this;
                    }
                    return NOTHING;
                }
            } else if (this.data instanceof AllOrNothing allOrNothing) {
                if (allOrNothing == AllOrNothing.NOTHING) {
                    return this;
                }
                return other.complement();
            }
            throw new IllegalStateException("unreachable");
        } else {
            throw new UnsupportedOperationException("intersection of different subtypes");
        }
    }

    @Override
    public SubType complement() {
        if (this.data instanceof BooleanSubTypeData booleanData) {
            return new BooleanSubType(!booleanData.value);
        } else if (this.data instanceof AllOrNothing allOrNothing) {
            if (allOrNothing == AllOrNothing.NOTHING) {
                return ALL;
            }
            return NOTHING;
        }
        throw new IllegalStateException("unreachable");
    }

    @Override
    public String toString() {
        if (data instanceof BooleanSubTypeData booleanSubTypeData) {
            return booleanSubTypeData.value ? "true" : "false";
        }
        return "boolean";
    }

    @Override
    public boolean isEmpty() {
        return data == AllOrNothing.NOTHING;
    }

    record BooleanSubTypeData(boolean value) implements SubTypeData {

    }
}

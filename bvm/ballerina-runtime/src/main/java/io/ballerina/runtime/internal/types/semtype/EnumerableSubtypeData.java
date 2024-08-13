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

package io.ballerina.runtime.internal.types.semtype;

import java.util.Arrays;
import java.util.List;

/**
 * All {@code SubTypeData} where we can enumerate individual values must extend this class. It will provide common
 * operations such as {@code union}, {@code intersect} and {@code diff} for all such data.
 *
 * @param <E> type individual value in the subset
 * @since 2201.10.0
 */
public abstract class EnumerableSubtypeData<E extends Comparable<E>> {

    public abstract boolean allowed();

    public abstract E[] values();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EnumerableSubtypeData other)) {
            return false;
        }
        return other.allowed() == this.allowed() && Arrays.equals(other.values(), this.values());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    boolean union(EnumerableSubtypeData<E> other, List<E> results) {
        boolean b1 = this.allowed();
        boolean b2 = other.allowed();
        if (b1 && b2) {
            enumerableListUnion(this.values(), other.values(), results);
            return true;
        } else if (!b1 && !b2) {
            enumerableListIntersection(this.values(), other.values(), results);
            return false;
        } else if (b1 && !b2) {
            enumerableListDiff(other.values(), this.values(), results);
            return false;
        } else {
            enumerableListDiff(this.values(), other.values(), results);
            return false;
        }
    }

    boolean intersect(EnumerableSubtypeData<E> other, List<E> results) {
        boolean b1 = this.allowed();
        boolean b2 = other.allowed();
        if (b1 && b2) {
            enumerableListIntersection(this.values(), other.values(), results);
            return true;
        } else if (!b1 && !b2) {
            enumerableListUnion(this.values(), other.values(), results);
            return false;
        } else if (b1 && !b2) {
            enumerableListDiff(this.values(), other.values(), results);
            return true;
        } else {
            enumerableListDiff(other.values(), this.values(), results);
            return true;
        }
    }

    private static <E extends Comparable<E>> void enumerableListUnion(E[] values1, E[] values2, List<E> results) {
        int i1, i2;
        i1 = i2 = 0;
        int len1 = values1.length;
        int len2 = values2.length;
        while (true) {
            if (i1 >= len1) {
                if (i2 >= len2) {
                    break;
                }
                results.add(values2[i2]);
                i2 += 1;
            } else if (i2 >= len2) {
                results.add(values1[i1]);
                i1 += 1;
            } else {
                E s1 = values1[i1];
                E s2 = values2[i2];
                int result = s1.compareTo(s2);
                if (result == 0) {
                    results.add(s1);
                    i1 += 1;
                    i2 += 1;
                } else if (result < 0) {
                    results.add(s1);
                    i1 += 1;
                } else {
                    results.add(s2);
                    i2 += 1;
                }
            }
        }
    }

    private static <E extends Comparable<E>> void enumerableListIntersection(E[] v1, E[] v2, List<E> results) {
        int i1, i2;
        i1 = i2 = 0;
        int len1 = v1.length;
        int len2 = v2.length;
        while (true) {
            // TODO: refactor condition
            if (i1 >= len1 || i2 >= len2) {
                break;
            } else {
                E s1 = v1[i1];
                E s2 = v2[i2];
                int result = s1.compareTo(s2);
                if (result == 0) {
                    results.add(s1);
                    i1 += 1;
                    i2 += 1;
                } else if (result < 0) {
                    i1 += 1;
                } else {
                    i2 += 1;
                }
            }
        }
    }

    private static <E extends Comparable<E>> void enumerableListDiff(E[] t1, E[] t2, List<E> results) {
        int i1, i2;
        i1 = i2 = 0;
        int len1 = t1.length;
        int len2 = t2.length;
        while (true) {
            if (i1 >= len1) {
                break;
            }
            if (i2 >= len2) {
                results.add(t1[i1]);
                i1 += 1;
            } else {
                E s1 = t1[i1];
                E s2 = t2[i2];
                int result = s1.compareTo(s2);
                if (result == 0) {
                    i1 += 1;
                    i2 += 1;
                } else if (result < 0) {
                    results.add(s1);
                    i1 += 1;
                } else {
                    i2 += 1;
                }
            }
        }
    }
}

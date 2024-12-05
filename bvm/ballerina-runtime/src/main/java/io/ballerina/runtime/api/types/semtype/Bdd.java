/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.internal.types.semtype.SubTypeData;

import java.util.HashMap;
import java.util.Map;

import static io.ballerina.runtime.api.types.semtype.Conjunction.and;

/**
 * Represent BDD node. Subtypes that uses BDDs to represent subtypes such as
 * list, mapping and cell should implement
 * their own {@code SubType} implementation that wraps an implementation of this
 * class.
 *
 * @since 2201.11.0
 */
public abstract sealed class Bdd extends SubType implements SubTypeData permits BddAllOrNothing, BddNode {

    Bdd(boolean all, boolean nothing) {
        super(all, nothing);
    }

    @Override
    public SubType union(SubType other) {
        return bddUnion(BddOpMemo.create(), (Bdd) other);
    }

    private Bdd bddUnion(BddOpMemo memoTable, Bdd other) {
        BddOpMemoKey key = new BddOpMemoKey(this, other);
        Bdd memoized = memoTable.unionMemo().get(key);
        if (memoized == null) {
            memoized = bddUnionInner(memoTable, other);
            memoTable.unionMemo().put(key, memoized);
        }
        return memoized;
    }

    private Bdd bddUnionInner(BddOpMemo memo, Bdd other) {
        if (other == this) {
            return this;
        } else if (this == BddAllOrNothing.ALL || other == BddAllOrNothing.ALL) {
            return BddAllOrNothing.ALL;
        } else if (other == BddAllOrNothing.NOTHING) {
            return this;
        } else if (this == BddAllOrNothing.NOTHING) {
            return other;
        }
        BddNode b1Bdd = (BddNode) this;
        BddNode b2Bdd = (BddNode) other;
        int cmp = atomCmp(b1Bdd.atom(), b2Bdd.atom());
        if (cmp < 0) {
            return bddCreate(b1Bdd.atom(),
                    b1Bdd.left(),
                    b1Bdd.middle().bddUnion(memo, other),
                    b1Bdd.right());
        } else if (cmp > 0) {
            return bddCreate(b2Bdd.atom(),
                    b2Bdd.left(),
                    this.bddUnion(memo, b2Bdd.middle()),
                    b2Bdd.right());
        } else {
            return bddCreate(b1Bdd.atom(),
                    b1Bdd.left().bddUnion(memo, b2Bdd.left()),
                    b1Bdd.middle().bddUnion(memo, b2Bdd.middle()),
                    b1Bdd.right().bddUnion(memo, b2Bdd.right()));
        }
    }

    private int atomCmp(Atom a1, Atom a2) {
        if (a1 instanceof RecAtom r1) {
            if (a2 instanceof RecAtom r2) {
                return r1.index() - r2.index();
            } else {
                return -1;
            }
        } else if (a2 instanceof RecAtom) {
            return 1;
        } else {
            return a1.index() - a2.index();
        }
    }

    @Override
    public SubType intersect(SubType other) {
        return bddIntersect(BddOpMemo.create(), (Bdd) other);
    }

    private Bdd bddIntersect(BddOpMemo memoTable, Bdd other) {
        BddOpMemoKey key = new BddOpMemoKey(this, other);
        Bdd memoized = memoTable.intersectionMemo().get(key);
        if (memoized == null) {
            memoized = bddIntersectInner(memoTable, other);
            memoTable.intersectionMemo().put(key, memoized);
        }
        return memoized;
    }

    private Bdd bddIntersectInner(BddOpMemo memo, Bdd other) {
        if (other == this) {
            return this;
        } else if (this == BddAllOrNothing.NOTHING || other == BddAllOrNothing.NOTHING) {
            return BddAllOrNothing.NOTHING;
        } else if (other == BddAllOrNothing.ALL) {
            return this;
        } else if (this == BddAllOrNothing.ALL) {
            return other;
        }
        BddNode b1Bdd = (BddNode) this;
        BddNode b2Bdd = (BddNode) other;
        int cmp = atomCmp(b1Bdd.atom(), b2Bdd.atom());
        if (cmp < 0) {
            return bddCreate(b1Bdd.atom(),
                    b1Bdd.left().bddIntersect(memo, other),
                    b1Bdd.middle().bddIntersect(memo, other),
                    b1Bdd.right().bddIntersect(memo, other));
        } else if (cmp > 0) {
            return bddCreate(b2Bdd.atom(),
                    this.bddIntersect(memo, b2Bdd.left()),
                    this.bddIntersect(memo, b2Bdd.middle()),
                    this.bddIntersect(memo, b2Bdd.right()));
        } else {
            return bddCreate(b1Bdd.atom(),
                    b1Bdd.left().bddUnion(memo, b1Bdd.middle())
                            .bddIntersect(memo, b2Bdd.left().bddUnion(memo, b2Bdd.middle())),
                    BddAllOrNothing.NOTHING,
                    b1Bdd.right().bddUnion(memo, b1Bdd.middle())
                            .bddIntersect(memo, b2Bdd.right().bddUnion(memo, b2Bdd.middle())));
        }
    }

    @Override
    public SubType diff(SubType other) {
        return bddDiff(BddOpMemo.create(), (Bdd) other);
    }

    private Bdd bddDiff(BddOpMemo memoTable, Bdd other) {
        var key = new BddOpMemoKey(this, other);
        var memoized = memoTable.diffMemo.get(key);
        if (memoized == null) {
            memoized = bddDiffInner(memoTable, other);
            memoTable.diffMemo.put(key, memoized);
        }
        return memoized;
    }

    private Bdd bddDiffInner(BddOpMemo memo, Bdd other) {
        if (this == other || other == BddAllOrNothing.ALL || this == BddAllOrNothing.NOTHING) {
            return BddAllOrNothing.NOTHING;
        } else if (other == BddAllOrNothing.NOTHING) {
            return this;
        } else if (this == BddAllOrNothing.ALL) {
            return other.bddComplement(memo);
        }
        BddNode b1Bdd = (BddNode) this;
        BddNode b2Bdd = (BddNode) other;
        int cmp = atomCmp(b1Bdd.atom(), b2Bdd.atom());
        if (cmp < 0L) {
            return bddCreate(b1Bdd.atom(),
                    b1Bdd.left().bddUnion(memo, b1Bdd.middle()).bddDiff(memo, other),
                    BddAllOrNothing.NOTHING,
                    b1Bdd.right().bddUnion(memo, b1Bdd.middle()).bddDiff(memo, other));
        } else if (cmp > 0L) {
            return bddCreate(b2Bdd.atom(),
                    this.bddDiff(memo, b2Bdd.left().bddUnion(memo, b2Bdd.middle())),
                    BddAllOrNothing.NOTHING,
                    this.bddDiff(memo, b2Bdd.right().bddUnion(memo, b2Bdd.middle())));
        } else {
            // There is an error in the Castagna paper for this formula.
            // The union needs to be materialized here.
            // The original formula does not work in a case like (a0|a1) - a0.
            // Castagna confirms that the following formula is the correct one.
            return bddCreate(b1Bdd.atom(),
                    b1Bdd.left().bddUnion(memo, b1Bdd.middle())
                            .bddDiff(memo, b2Bdd.left().bddUnion(memo, b2Bdd.middle())),
                    BddAllOrNothing.NOTHING,
                    b1Bdd.right().bddUnion(memo, b1Bdd.middle())
                            .bddDiff(memo, b2Bdd.right().bddUnion(memo, b2Bdd.middle())));
        }
    }

    @Override
    public SubType complement() {
        return bddComplement(BddOpMemo.create());
    }

    private Bdd bddComplement(BddOpMemo memo) {
        if (this == BddAllOrNothing.ALL) {
            return BddAllOrNothing.NOTHING;
        } else if (this == BddAllOrNothing.NOTHING) {
            return BddAllOrNothing.ALL;
        }
        Bdd nothing = BddAllOrNothing.NOTHING;
        BddNode b = (BddNode) this;
        if (b.right() == nothing) {
            return bddCreate(b.atom(),
                    nothing,
                    b.left().bddUnion(memo, b.middle()).bddComplement(memo),
                    b.middle().bddComplement(memo));
        } else if (b.left() == nothing) {
            return bddCreate(b.atom(),
                    b.middle().bddComplement(memo),
                    b.right().bddUnion(memo, b.middle()).bddComplement(memo),
                    nothing);
        } else if (b.middle() == nothing) {
            return bddCreate(b.atom(),
                    b.left().bddComplement(memo),
                    b.left().bddUnion(memo, b.right()).bddComplement(memo),
                    b.right().bddComplement(memo));
        } else {
            // There is a typo in the Frisch PhD thesis for this formula.
            // (It has left and right swapped.)
            // Castagna (the PhD supervisor) confirms that this is the correct formula.
            return bddCreate(b.atom(),
                    b.left().bddUnion(memo, b.middle()).bddComplement(memo),
                    nothing,
                    b.right().bddUnion(memo, b.middle()).bddComplement(memo));
        }
    }

    private Bdd bddCreate(Atom atom, Bdd left, Bdd middle, Bdd right) {
        if (middle == BddAllOrNothing.ALL) {
            return middle;
        }
        if (left.equals(right)) {
            return left.bddUnion(BddOpMemo.create(), right);
        }

        return new BddNodeImpl(atom, left, middle, right);
    }

    @Override
    public boolean isEmpty(Context cx) {
        // Basic types that uses Bdd as a delegate should implement isEmpty instead.
        throw new IllegalStateException("Bdd don't support isEmpty");
    }

    @Override
    public SubTypeData data() {
        // Basic types that uses Bdd (and has a meaningful data part) as a delegate should implement data instead.
        throw new IllegalStateException("Bdd don't support data");
    }

    public static boolean bddEvery(Context cx, Bdd b, BddPredicate predicate) {
        return bddEvery(cx, b, null, null, predicate);
    }

    private static boolean bddEvery(Context cx, Bdd b, Conjunction pos, Conjunction neg, BddPredicate predicate) {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return allOrNothing == BddAllOrNothing.NOTHING || predicate.apply(cx, pos, neg);
        }
        BddNode bn = (BddNode) b;
        return bddEvery(cx, bn.left(), and(bn.atom(), pos), neg, predicate)
                && bddEvery(cx, bn.middle(), pos, neg, predicate)
                && bddEvery(cx, bn.right(), pos, and(bn.atom(), neg), predicate);
    }

    public static boolean bddEveryPositive(Context cx, Bdd b, Conjunction pos, Conjunction neg,
                                           BddPredicate predicate) {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return allOrNothing == BddAllOrNothing.NOTHING || predicate.apply(cx, pos, neg);
        } else {
            BddNode bn = (BddNode) b;
            return bddEveryPositive(cx, bn.left(), andIfPositive(bn.atom(), pos), neg, predicate)
                    && bddEveryPositive(cx, bn.middle(), pos, neg, predicate)
                    && bddEveryPositive(cx, bn.right(), pos, andIfPositive(bn.atom(), neg), predicate);
        }
    }

    private static Conjunction andIfPositive(Atom atom, Conjunction next) {
        if (atom instanceof RecAtom recAtom && recAtom.index() < 0) {
            return next;
        }
        return and(atom, next);
    }

    public abstract boolean posMaybeEmpty();

    // This is for debugging purposes.
    // It uses the Frisch/Castagna notation.
    public static String bddToString(Bdd b, boolean inner) {
        if (b instanceof BddAllOrNothing) {
            return b.isAll() ? "1" : "0";
        } else {
            String str;
            BddNode bdd = (BddNode) b;
            Atom a = bdd.atom();

            if (a instanceof RecAtom) {
                str = "r";
            } else {
                str = "a";
            }
            str += a.index();
            str += "?" + bddToString(bdd.left(), true) + ":" + bddToString(bdd.middle(), true) +
                    ":" + bddToString(bdd.right(), true);
            if (inner) {
                str = "(" + str + ")";
            }
            return str;
        }
    }

    private record BddOpMemoKey(Bdd b1, Bdd b2) {

    }

    private record BddOpMemo(Map<BddOpMemoKey, Bdd> unionMemo, Map<BddOpMemoKey, Bdd> intersectionMemo,
                             Map<BddOpMemoKey, Bdd> diffMemo) {

        static BddOpMemo create() {
            return new BddOpMemo(new HashMap<>(), new HashMap<>(), new HashMap<>());
        }
    }
}

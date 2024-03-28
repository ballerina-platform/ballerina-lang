/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types.typeops;

import io.ballerina.types.Atom;
import io.ballerina.types.Bdd;
import io.ballerina.types.RecAtom;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;

/**
 * Contain common BDD operations found in bdd.bal file.
 *
 * @since 2201.8.0
 */
public abstract class BddCommonOps {

    public static BddNode bddAtom(Atom atom) {
        return BddNode.create(atom,
                BddAllOrNothing.bddAll(),
                BddAllOrNothing.bddNothing(),
                BddAllOrNothing.bddNothing());
    }

    public static Bdd bddUnion(Bdd b1, Bdd b2) {
        if (b1 == b2) {
            return b1;
        } else if (b1 instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b1).isAll() ? BddAllOrNothing.bddAll() : b2;
        } else if (b2 instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b2).isAll() ? BddAllOrNothing.bddAll() : b1;
        } else {
            BddNode b1Bdd = (BddNode) b1;
            BddNode b2Bdd = (BddNode) b2;
            long cmp = atomCmp(b1Bdd.atom(), b2Bdd.atom());
            if (cmp < 0L) {
                return bddCreate(b1Bdd.atom(),
                        b1Bdd.left(),
                        bddUnion(b1Bdd.middle(), b2),
                        b1Bdd.right());
            } else if (cmp > 0L) {
                return bddCreate(b2Bdd.atom(),
                        b2Bdd.left(),
                        bddUnion(b1, b2Bdd.middle()),
                        b2Bdd.right());
            } else {
                return bddCreate(b1Bdd.atom(),
                        bddUnion(b1Bdd.left(), b2Bdd.left()),
                        bddUnion(b1Bdd.middle(), b2Bdd.middle()),
                        bddUnion(b1Bdd.right(), b2Bdd.right()));
            }
        }
    }

    public static Bdd bddIntersect(Bdd b1, Bdd b2) {
        if (b1 == b2) {
            return b1;
        } else if (b1 instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b1).isAll() ? b2 : BddAllOrNothing.bddNothing();
        } else if (b2 instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b2).isAll() ? b1 : BddAllOrNothing.bddNothing();
        } else {
            BddNode b1Bdd = (BddNode) b1;
            BddNode b2Bdd = (BddNode) b2;
            long cmp = atomCmp(b1Bdd.atom(), b2Bdd.atom());
            if (cmp < 0L) {
                return bddCreate(b1Bdd.atom(),
                        bddIntersect(b1Bdd.left(), b2),
                        bddIntersect(b1Bdd.middle(), b2),
                        bddIntersect(b1Bdd.right(), b2));
            } else if (cmp > 0L) {
                return bddCreate(b2Bdd.atom(),
                        bddIntersect(b1, b2Bdd.left()),
                        bddIntersect(b1, b2Bdd.middle()),
                        bddIntersect(b1, b2Bdd.right()));
            } else {
                return bddCreate(b1Bdd.atom(),
                        bddIntersect(
                                bddUnion(b1Bdd.left(), b1Bdd.middle()),
                                bddUnion(b2Bdd.left(), b2Bdd.middle())),
                        BddAllOrNothing.bddNothing(),
                        bddIntersect(
                                bddUnion(b1Bdd.right(), b1Bdd.middle()),
                                bddUnion(b2Bdd.right(), b2Bdd.middle())));
            }
        }
    }

    public static Bdd bddDiff(Bdd b1, Bdd b2) {
        if (b1 == b2) {
            return BddAllOrNothing.bddNothing();
        } else if (b2 instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b2).isAll() ? BddAllOrNothing.bddNothing() : b1;
        } else if (b1 instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b1).isAll() ? bddComplement(b2) : BddAllOrNothing.bddNothing();
        } else {
            BddNode b1Bdd = (BddNode) b1;
            BddNode b2Bdd = (BddNode) b2;
            long cmp = atomCmp(b1Bdd.atom(), b2Bdd.atom());
            if (cmp < 0L) {
                return bddCreate(b1Bdd.atom(),
                        bddDiff(bddUnion(b1Bdd.left(), b1Bdd.middle()), b2),
                        BddAllOrNothing.bddNothing(),
                        bddDiff(bddUnion(b1Bdd.right(), b1Bdd.middle()), b2));
            } else if (cmp > 0L) {
                return bddCreate(b2Bdd.atom(),
                        bddDiff(b1, bddUnion(b2Bdd.left(), b2Bdd.middle())),
                        BddAllOrNothing.bddNothing(),
                        bddDiff(b1, bddUnion(b2Bdd.right(), b2Bdd.middle())));

            } else {
                // There is an error in the Castagna paper for this formula.
                // The union needs to be materialized here.
                // The original formula does not work in a case like (a0|a1) - a0.
                // Castagna confirms that the following formula is the correct one.
                return bddCreate(b1Bdd.atom(),
                        bddDiff(bddUnion(b1Bdd.left(), b1Bdd.middle()),
                                bddUnion(b2Bdd.left(), b2Bdd.middle())),
                        BddAllOrNothing.bddNothing(),
                        bddDiff(bddUnion(b1Bdd.right(), b1Bdd.middle()),
                                bddUnion(b2Bdd.right(), b2Bdd.middle())));
            }
        }
    }

    public static Bdd bddComplement(Bdd b) {
        if (b instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b).complement();
        } else {
            return bddNodeComplement((BddNode) b);
        }
    }

    public static Bdd bddNodeComplement(BddNode b) {
        BddAllOrNothing bddNothing = BddAllOrNothing.bddNothing();
        if (b.right().equals(bddNothing)) {
            return bddCreate(b.atom(),
                    bddNothing,
                    bddComplement(bddUnion(b.left(), b.middle())),
                    bddComplement(b.middle()));
        } else if (b.left().equals(bddNothing)) {
            return bddCreate(b.atom(),
                    bddComplement(b.middle()),
                    bddComplement(bddUnion(b.right(), b.middle())),
                    bddNothing);
        } else if (b.middle().equals(bddNothing)) {
            return bddCreate(b.atom(),
                    bddComplement(b.left()),
                    bddComplement(bddUnion(b.left(), b.right())),
                    bddComplement(b.right()));
        } else {
            // There is a typo in the Frisch PhD thesis for this formula.
            // (It has left and right swapped.)
            // Castagna (the PhD supervisor) confirms that this is the correct formula.
            return bddCreate(b.atom(),
                    bddComplement(bddUnion(b.left(), b.middle())),
                    bddNothing,
                    bddComplement(bddUnion(b.right(), b.middle())));
        }
    }

    public static Bdd bddCreate(Atom atom, Bdd left, Bdd middle, Bdd right) {
        if (middle instanceof BddAllOrNothing && ((BddAllOrNothing) middle).isAll()) {
            return middle;
        }
        if (left.equals(right)) {
            return bddUnion(left, right);
        }

        return BddNode.create(atom, left, middle, right);
    }

    // order RecAtom < TypeAtom
    public static long atomCmp(Atom a1, Atom a2) {
        if (a1 instanceof RecAtom r1) {
            if (a2 instanceof RecAtom r2) {
                return r1.index() - r2.index();
            } else {
                return -1L;
            }
        } else if (a2 instanceof RecAtom) {
            return 1L;
        } else {
            return a1.index() - a2.index();
        }
    }

    // This is for debugging purposes.
    // It uses the Frisch/Castagna notation.
    public static String bddToString(Bdd b, boolean inner) {
        if (b instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b).isAll() ? "1" : "0";
        } else {
            String str;
            BddNode bdd = (BddNode) b;
            Atom a = bdd.atom();

            if (a instanceof RecAtom) {
                str = "r" + a;
            } else {
                str = "a" + a.index();
            }
            str += "?" + bddToString(bdd.left(), true) + ":" + bddToString(bdd.middle(), true) +
                    ":" + bddToString(bdd.right(), true);
            if (inner) {
                str = "(" + str + ")";
            }
            return str;
        }
    }
}

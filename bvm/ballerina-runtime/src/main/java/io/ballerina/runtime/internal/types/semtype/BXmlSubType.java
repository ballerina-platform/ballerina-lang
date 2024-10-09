/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Conjunction;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.RecAtom;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.Objects;

public class BXmlSubType extends SubType implements DelegatedSubType {

    public final Bdd inner;
    private final int primitives;

    private BXmlSubType(Bdd inner, int primitives) {
        super(false, false);
        this.inner = inner;
        this.primitives = primitives;
    }

    public static BXmlSubType createDelegate(int primitives, SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BXmlSubType(bdd, primitives);
        } else if (inner instanceof BXmlSubType bXml) {
            return new BXmlSubType(bXml.inner, primitives);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    @Override
    public SubType union(SubType other) {
        BXmlSubType otherXml = (BXmlSubType) other;
        int primitives = this.primitives() | otherXml.primitives();
        return createDelegate(primitives, inner.union(otherXml.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        BXmlSubType otherXml = (BXmlSubType) other;
        int primitives = this.primitives() & otherXml.primitives();
        return createDelegate(primitives, inner.intersect(otherXml.inner));
    }

    @Override
    public SubType diff(SubType other) {
        BXmlSubType otherXml = (BXmlSubType) other;
        return diff(this, otherXml);
    }

    private static SubType diff(BXmlSubType st1, BXmlSubType st2) {
        int primitives = st1.primitives() & ~st2.primitives();
        return createDelegate(primitives, st1.inner.diff(st2.inner));
    }

    @Override
    public SubType complement() {
        return diff((BXmlSubType) XmlUtils.XML_SUBTYPE_TOP, this);
    }

    @Override
    public boolean isEmpty(Context cx) {
        if (primitives() != 0) {
            return false;
        }
        return xmlBddEmpty(cx);
    }

    private boolean xmlBddEmpty(Context cx) {
        return Bdd.bddEvery(cx, inner, BXmlSubType::xmlFormulaIsEmpty);
    }

    private static boolean xmlFormulaIsEmpty(Context cx, Conjunction pos, Conjunction neg) {
        int allPosBits = collectAllPrimitives(pos) & XmlUtils.XML_PRIMITIVE_ALL_MASK;
        return xmlHasTotalNegative(allPosBits, neg);
    }

    private static boolean xmlHasTotalNegative(int allPosBits, Conjunction conjunction) {
        if (allPosBits == 0) {
            return true;
        }
        Conjunction n = conjunction;
        while (n != null) {
            if ((allPosBits & ~getIndex(n)) == 0) {
                return true;
            }
            n = n.next();
        }
        return false;
    }

    private static int collectAllPrimitives(Conjunction conjunction) {
        int bits = 0;
        Conjunction current = conjunction;
        while (current != null) {
            bits &= getIndex(current);
            current = current.next();
        }
        return bits;
    }

    private static int getIndex(Conjunction conjunction) {
        var atom = conjunction.atom();
        assert atom instanceof RecAtom;
        return atom.index();
    }

    @Override
    public SubTypeData data() {
        return this;
    }

    @Override
    public SubType inner() {
        return this;
    }

    int primitives() {
        return primitives;
    }

    Bdd bdd() {
        return inner;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inner, primitives);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BXmlSubType other)) {
            return false;
        }
        return Objects.equals(bdd(), other.bdd()) && primitives() == other.primitives();
    }

}

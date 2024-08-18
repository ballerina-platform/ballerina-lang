package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Atom;
import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.BddNode;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;
import io.ballerina.runtime.api.types.semtype.TypeAtom;

public abstract sealed class BCellSubType extends SubType implements DelegatedSubType
        permits BCellSubTypeImpl, BCellSubTypeSimple {

    public BCellSubType(boolean all, boolean nothing) {
        super(all, nothing);
    }

    public static BCellSubType createDelegate(SubType inner) {
        Bdd bdd;
        if (inner instanceof Bdd b) {
            bdd = b;
        } else if (inner instanceof BCellSubTypeImpl bCellImpl) {
            bdd = bCellImpl.inner();
        } else if (inner instanceof BCellSubTypeSimple simple) {
            return simple;
        } else {
            throw new IllegalArgumentException("Unexpected inner type");
        }
        if (!(bdd instanceof BddNode bddNode && bddNode.isSimple())) {
            return new BCellSubTypeImpl(bdd);
        }
        Atom atom = bddNode.atom();
        if (!(atom instanceof TypeAtom typeAtom)) {
            return new BCellSubTypeImpl(bdd);
        }
        CellAtomicType atomicType = (CellAtomicType) typeAtom.atomicType();
        SemType ty = atomicType.ty();
        // We have special logic when it comes to handling undef that needs to be updated to deal with simple cell
        // TODO: probably we can also handle immutable cells as well
        if (Core.containsBasicType(ty, Builder.undef()) || ty.some() != 0 ||
                atomicType.mut() != CellAtomicType.CellMutability.CELL_MUT_LIMITED) {
            return new BCellSubTypeImpl(bdd);
        }
        return new BCellSubTypeSimple(ty, bddNode);
    }
}

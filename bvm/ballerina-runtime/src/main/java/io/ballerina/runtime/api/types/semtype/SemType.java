package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.semtype.ImmutableSemType;
import io.ballerina.runtime.internal.types.semtype.MutableSemType;
import io.ballerina.runtime.internal.types.semtype.SemTypeHelper;

/**
 * Represent a type in runtime.
 *
 * @since 2201.11.0
 */
public sealed class SemType extends BasicTypeBitSet
        permits io.ballerina.runtime.internal.types.BType, ImmutableSemType {

    private int some;
    private SubType[] subTypeData;

    protected SemType(int all, int some, SubType[] subTypeData) {
        super(all);
        this.some = some;
        this.subTypeData = subTypeData;
    }

    protected SemType() {
        this(-1, -1, null);
    }

    public static SemType from(int all) {
        return new SemType(all, 0, null);
    }

    public static SemType from(int all, int some, SubType[] subTypes) {
        return new SemType(all, some, subTypes);
    }

    public final int some() {
        assert some != -1 : "SemType created by no arg constructor must be initialized with setSome";
        return some;
    }

    public final SubType[] subTypeData() {
        return subTypeData;
    }

    public final SubType subTypeByCode(int code) {
        if ((some() & (1 << code)) == 0) {
            return null;
        }
        int someMask = (1 << code) - 1;
        int some = some() & someMask;
        return subTypeData()[Integer.bitCount(some)];
    }

    protected void setSome(int some, SubType[] subTypeData) {
        this.some = some;
        this.subTypeData = subTypeData;
    }

    public static SemType tryInto(Context cx, Type type) {
        if (type instanceof MutableSemType mutableSemType) {
            try {
                cx.enterTypeResolutionPhase(mutableSemType);
                mutableSemType.updateInnerSemTypeIfNeeded(cx);
            } catch (Exception ex) {
                cx.exitTypeResolutionPhaseAbruptly(ex);
                throw new RuntimeException("Error while resolving type: " + mutableSemType, ex);
            }
        }

        return (SemType) type;
    }

    @Override
    public String toString() {
        return SemTypeHelper.stringRepr(this);
    }
}

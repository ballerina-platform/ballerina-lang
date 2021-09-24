package io.ballerina.types.typeops;

import io.ballerina.types.Common;
import io.ballerina.types.EnumerableFloat;
import io.ballerina.types.EnumerableSubtype;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.Context;
import io.ballerina.types.UniformTypeOps;
import io.ballerina.types.subtypedata.FloatSubtype;

import java.util.ArrayList;

/**
 * Float specific methods operate on SubtypeData.
 *
 * @since 3.0.0
 */
public class FloatOps extends CommonOps implements UniformTypeOps {
    @Override
    public SubtypeData union(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableFloat> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeUnion((FloatSubtype) t1, (FloatSubtype) t2, values);
        EnumerableFloat[] valueArray = new EnumerableFloat[values.size()];
        return FloatSubtype.createFloatSubtype(allowed, values.toArray(valueArray));
    }

    @Override
    public SubtypeData intersect(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableFloat> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeIntersect((FloatSubtype) t1, (FloatSubtype) t2, values);
        return FloatSubtype.createFloatSubtype(allowed, values.toArray(new EnumerableFloat[]{}));
    }

    @Override
    public SubtypeData diff(SubtypeData t1, SubtypeData t2) {
        return intersect(t1, complement(t2));
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        FloatSubtype s = (FloatSubtype) t;
        return FloatSubtype.createFloatSubtype(!s.allowed, (EnumerableFloat[]) s.values);
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        return Common.notIsEmpty(cx, t);
    }
}

package io.ballerina.semtype.typeops;

import io.ballerina.semtype.Common;
import io.ballerina.semtype.EnumerableFloat;
import io.ballerina.semtype.EnumerableSubtype;
import io.ballerina.semtype.SubtypeData;
import io.ballerina.semtype.TypeCheckContext;
import io.ballerina.semtype.UniformTypeOps;
import io.ballerina.semtype.subtypedata.FloatSubtype;

import java.util.ArrayList;

/**
 * Float specific methods operate on SubtypeData.
 *
 * @since 2.0.0
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
        boolean allowed = EnumerableSubtype.enumerableSubtypeIntersect((FloatSubtype) t1, (FloatSubtype) t1, values);
        EnumerableFloat[] valueArray = new EnumerableFloat[values.size()];
        return FloatSubtype.createFloatSubtype(allowed, values.toArray(valueArray));
    }

    @Override
    public SubtypeData diff(SubtypeData t1, SubtypeData t2) {
        return intersect(t1, complement(t1));
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        FloatSubtype s = (FloatSubtype) t;
        return FloatSubtype.createFloatSubtype(!s.allowed, (EnumerableFloat[]) s.values);
    }

    @Override
    public boolean isEmpty(TypeCheckContext tc, SubtypeData t) {
        return Common.notIsEmpty(tc, t);
    }
}

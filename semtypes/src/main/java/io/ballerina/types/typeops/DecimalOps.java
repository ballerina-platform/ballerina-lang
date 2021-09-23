package io.ballerina.types.typeops;

import io.ballerina.types.Common;
import io.ballerina.types.EnumerableDecimal;
import io.ballerina.types.EnumerableFloat;
import io.ballerina.types.EnumerableSubtype;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.TypeCheckContext;
import io.ballerina.types.UniformTypeOps;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;

import java.util.ArrayList;

/**
 * Decimal specific methods operate on SubtypeData.
 *
 * @since 2.0.0
 */
public class DecimalOps extends CommonOps implements UniformTypeOps {
    @Override
    public SubtypeData union(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableDecimal> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeUnion((DecimalSubtype) t1, (DecimalSubtype) t2, values);
        EnumerableDecimal[] valueArray = new EnumerableDecimal[values.size()];
        return DecimalSubtype.createDecimalSubtype(allowed, values.toArray(valueArray));
    }

    @Override
    public SubtypeData intersect(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableDecimal> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeIntersect((DecimalSubtype) t1,
                (DecimalSubtype) t2, values);
        return DecimalSubtype.createDecimalSubtype(allowed, values.toArray(new EnumerableDecimal[]{}));
    }

    @Override
    public SubtypeData diff(SubtypeData t1, SubtypeData t2) {
        return intersect(t1, complement(t2));
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        DecimalSubtype s = (DecimalSubtype) t;
        return DecimalSubtype.createDecimalSubtype(!s.allowed, (EnumerableDecimal[]) s.values);
    }

    @Override
    public boolean isEmpty(TypeCheckContext tc, SubtypeData t) {
        return Common.notIsEmpty(tc, t);
    }
}

package io.ballerina.semtype.typeops;

import io.ballerina.semtype.EnumerableFloatType;
import io.ballerina.semtype.EnumerableSubtype;
import io.ballerina.semtype.SubtypeData;
import io.ballerina.semtype.subtypedata.FloatSubtype;

import java.util.ArrayList;

public class FloatOps extends CommonOps  {
    @Override
    public SubtypeData union(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableFloatType> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeUnion((FloatSubtype) t1, (FloatSubtype) t2, values);
        return FloatSubtype.createFloatSubtype(allowed, values);
    }

    @Override
    public SubtypeData intersect(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableFloatType> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeIntersect((FloatSubtype) t1, (FloatSubtype) t1, values);
        return FloatSubtype.createFloatSubtype(allowed, values);
    }

    @Override
    public SubtypeData diff(SubtypeData t1, SubtypeData t2) {
        return intersect(t1, complement(t1));
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        FloatSubtype s = (FloatSubtype) t;
        return FloatSubtype.createFloatSubtype(!s.allowed, s.values);
    }

}

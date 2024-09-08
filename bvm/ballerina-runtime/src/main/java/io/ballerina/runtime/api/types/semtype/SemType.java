package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.semtype.ImmutableSemType;
import io.ballerina.runtime.internal.types.semtype.SemTypeHelper;

import java.util.Map;
import java.util.WeakHashMap;

public sealed class SemType extends BasicTypeBitSet
        permits io.ballerina.runtime.internal.types.BType, ImmutableSemType {

    private int some;
    private SubType[] subTypeData;
    private volatile Map<SemType, CachedResult> cachedResults;

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

    public final CachedResult cachedSubTypeRelation(SemType other) {
        if (skipCache()) {
            return CachedResult.NOT_FOUND;
        }
        if (cachedResults == null) {
            synchronized (this) {
                if (cachedResults == null) {
                    cachedResults = new WeakHashMap<>();
                }
            }
            return CachedResult.NOT_FOUND;
        }
        return cachedResults.getOrDefault(other, CachedResult.NOT_FOUND);
    }

    private boolean skipCache() {
        return this.some() == 0;
    }

    public final void cacheSubTypeRelation(SemType other, boolean result) {
        if (skipCache() || other.skipCache()) {
            return;
        }
        // we always check of the result before caching so there will always be a map
        cachedResults.put(other, result ? CachedResult.TRUE : CachedResult.FALSE);
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

    public static SemType tryInto(Type type) {
        if (type instanceof MutableSemType mutableSemType) {
            mutableSemType.updateInnerSemTypeIfNeeded();
        }
        return (SemType) type;
    }

    public enum CachedResult {
        TRUE,
        FALSE,
        NOT_FOUND
    }

    @Override
    public String toString() {
        return SemTypeHelper.stringRepr(this);
    }
}

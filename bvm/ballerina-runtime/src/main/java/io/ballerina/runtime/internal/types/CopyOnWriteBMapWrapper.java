package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.Optional;

public final class CopyOnWriteBMapWrapper implements MapType, TypeWithShape {

    private BMapType inner;

    public CopyOnWriteBMapWrapper(BMapType inner) {
        this.inner = inner;
    }

    private void copyOnWrite() {
        inner = inner.clone();
    }

    @Override
    public Type getConstrainedType() {
        return inner.getConstrainedType();
    }

    @Override
    public <V> V getZeroValue() {
        return inner.getZeroValue();
    }

    @Override
    public <V> V getEmptyValue() {
        return inner.getEmptyValue();
    }

    @Override
    public int getTag() {
        return inner.getTag();
    }

    @Override
    public boolean isNilable() {
        return inner.isNilable();
    }

    @Override
    public String getName() {
        return inner.getName();
    }

    @Override
    public String getQualifiedName() {
        return inner.getQualifiedName();
    }

    @Override
    public Module getPackage() {
        return inner.getPackage();
    }

    @Override
    public boolean isPublic() {
        return inner.isPublic();
    }

    @Override
    public boolean isNative() {
        return inner.isNative();
    }

    @Override
    public boolean isAnydata() {
        return inner.isAnydata();
    }

    @Override
    public boolean isPureType() {
        return inner.isPureType();
    }

    @Override
    public boolean isReadOnly() {
        return inner.isReadOnly();
    }

    @Override
    public long getFlags() {
        return inner.getFlags();
    }

    @Override
    public IntersectionType getImmutableType() {
        return inner.getImmutableType();
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        copyOnWrite();
        inner.setImmutableType(immutableType);
    }

    @Override
    public Module getPkg() {
        return inner.getPkg();
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return inner.getIntersectionType();
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        copyOnWrite();
        inner.setImmutableType(intersectionType);
    }

    @Override
    public int all() {
        return inner.all();
    }

    @Override
    public int some() {
        return inner.some();
    }

    @Override
    public SubType[] subTypeData() {
        return inner.subTypeData();
    }

    @Override
    public CachedResult cachedSubTypeRelation(SemType other) {
        return inner.cachedSubTypeRelation(other);
    }

    @Override
    public void cacheSubTypeRelation(SemType other, boolean result) {
        inner.cacheSubTypeRelation(other, result);
    }

    @Override
    public SubType subTypeByCode(int code) {
        return inner.subTypeByCode(code);
    }

    @Override
    public Optional<SemType> shapeOf(Context cx, ShapeSupplier shapeSupplierFn, Object object) {
        return inner.shapeOf(cx, shapeSupplierFn, object);
    }

    @Override
    public Optional<SemType> readonlyShapeOf(Context cx, ShapeSupplier shapeSupplierFn, Object object) {
        return inner.readonlyShapeOf(cx, shapeSupplierFn, object);
    }

    @Override
    public boolean couldShapeBeDifferent() {
        return inner.couldShapeBeDifferent();
    }
}

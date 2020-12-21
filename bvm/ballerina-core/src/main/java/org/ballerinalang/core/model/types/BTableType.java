package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;

/**
 * {@code BTableType} represents a type of a table in Ballerina.
 * <p>
 * This is created to support BRunUtil class only
 * <p>
 *
 * @since slp8
 */
public class BTableType extends BType implements BIndexedType {

    private BType constraint;

    public BTableType(BType constraint) {
        super(TypeConstants.TABLE_TNAME, null, BMap.class);
        this.constraint = constraint;
    }

    @Override
    public BType getElementType() {
        return constraint;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return (V) new BMap(this);
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public String toString() {
        if (constraint == BTypes.typeAny) {
            return super.toString();
        } else {
            return "table" + "<" + constraint.getName() + ">";
        }
    }

    @Override
    public int getTag() {
        return TypeTags.TABLE_TAG;
    }
}

package org.ballerinalang.model.types;

import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;

/**
 * {@code BTableType} represents tabular data in Ballerina.
 *
 * @since 0.8.0
 */
public class BTableType extends BType {

    private BType constraint;

    /**
     * Create a {@code BTableType} which represents the SQL Result Set.
     *
     * @param typeName string name of the type
     */
    BTableType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BTable.class);
    }

    public BTableType(BType constraint) {
        super(TypeConstants.TABLE_TNAME, null, BTable.class);
        this.constraint = constraint;
    }

    public BType getConstrainedType() {
        return constraint;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.TABLE_TAG;
    }

    @Override
    public String toString() {
        if (constraint == null) {
            return super.toString();
        } else {
            return super.toString() + "<" + constraint.getName() + ">";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BTableType)) {
            return false;
        }

        BTableType other = (BTableType) obj;
        if (constraint == other.constraint) {
            return true;
        }

        if (constraint == null || other.constraint == null) {
            return false;
        }

        return constraint.equals(other.constraint);
    }
}
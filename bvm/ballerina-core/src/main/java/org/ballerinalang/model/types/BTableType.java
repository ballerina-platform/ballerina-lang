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
    private BType keyType;
    private String[] fieldNames;

    public BTableType(BType constraint, String[] fieldNames) {
        super(TypeConstants.TABLE_TNAME, null, BTable.class);
        this.constraint = constraint;
        this.fieldNames = fieldNames;
        this.keyType = null;
    }

    public BTableType(BType constraint, BType keyType, String pkgPath) {
        super(TypeConstants.TABLE_TNAME, pkgPath, BTable.class);
        this.constraint = constraint;
        this.keyType = keyType;
        this.fieldNames = new String[]{};
    }

    public BTableType(BType constraint) {
        super(TypeConstants.TABLE_TNAME, null, BTable.class);
        this.constraint = constraint;
    }

    public BType getConstrainedType() {
        return constraint;
    }

    public BType getKeyType() {
        return keyType;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return null;
    }

    public void setKeyType(BType keyType) {
        this.keyType = keyType;
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
        if (constraint == other.constraint && keyType == other.keyType) {
            return true;
        }

        if (constraint == null || other.constraint == null) {
            return false;
        }

        if (keyType == null || other.keyType == null) {
            return false;
        }

        return constraint.equals(other.constraint) && keyType.equals(other.keyType);
    }
}
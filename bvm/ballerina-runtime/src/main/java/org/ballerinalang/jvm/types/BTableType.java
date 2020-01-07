/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.values.TableValue;

/**
 * {@code BTableType} represents a type of a table in Ballerina.
 * <p>
 * Tables are defined using the table keyword as follows:
 * table tableName
 * <p>
 * All tables are unbounded in length and support column based indexing.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BTableType extends BType {

    private BType constraint;

    /**
     * Create a table type from the given name.
     *
     * @param typeName string name of the type.
     * @param constraint constraint type which particular table is bound to.
     * @param pkg package for the type.
     */
    public BTableType(String typeName, BType constraint, BPackage pkg) {
        super(typeName, pkg, TableValue.class);
        this.constraint = constraint;
    }

    public BTableType(BType constraint) {
        super(TypeConstants.TABLE_TNAME, null, TableValue.class);
        this.constraint = constraint;
    }

    /**
     * Returns element types which this table is constrained to.
     *
     * @return constraint type.
     */
    public BType getConstrainedType() {
        return constraint;
    }

    /**
     * Returns element type which this table contains.
     *
     * @return element type.
     * @deprecated use {@link #getConstrainedType()} instead.
     */
    @Deprecated
    public BType getElementType() {
        return constraint;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new TableValue(this, null, null);
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.TABLE_TAG;
    }

    @Override
    public String toString() {
        if (constraint == BTypes.typeAnydata) {
            return super.toString();
        }

        return "table" + "<" + constraint.getName() + ">";
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

        return TypeChecker.checkIsType(constraint, other.constraint);
    }
}

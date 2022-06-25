package org.ballerinalang.langlib.query;

import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.TableValueImpl;

/**
 * Implementation of lang.query:CreateImmutableType().
 *
 * @since 2201.2.0
 */

public class CreateImmutableType {

    public static void createImmutableValue(BRefValue value) {
        value.freezeDirect();
    }

    public static BTable createImmutableTable(BTable tbl, BArray arr) {
        TableType type = (TableType) tbl.getType();
        BTable immutableTable = new TableValueImpl(type,
                new ArrayValueImpl(arr.getValues(), (ArrayType) arr.getType()),
                new ArrayValueImpl(((TableType) tbl.getType()).getFieldNames(), true));
        immutableTable.freezeDirect();
        return immutableTable;
    }

    public static BTable createTableWithKeySpecifier(BTable immutableTable, BTypedesc tableType) {
        TableType type = (TableType) tableType.getDescribingType();
        BTable tbl = new TableValueImpl(type,
                new ArrayValueImpl(((TableType) immutableTable.getType()).getFieldNames(), false));
        return tbl;
    }
}

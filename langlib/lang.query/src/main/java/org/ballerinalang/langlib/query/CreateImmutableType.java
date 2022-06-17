package org.ballerinalang.langlib.query;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BXml;

/**
 * Implementation of lang.query:CreateImmutableType().
 *
 * @since 2201.2.0
 */

public class CreateImmutableType {

    public static BArray createImmutableArray(BArray arr) {
        arr.freezeDirect();
        return arr;
    }

    public static BMap createImmutableMap(BMap map) {
        map.freezeDirect();
        return map;
    }

    public static BTable createImmutableTable(BTable tbl) {
        tbl.freezeDirect();
        return tbl;
    }

    public static BXml createImmutableXml(BXml xml) {
        xml.freezeDirect();
        return xml;
    }
}

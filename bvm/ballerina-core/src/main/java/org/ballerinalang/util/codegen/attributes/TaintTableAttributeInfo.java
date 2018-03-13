package org.ballerinalang.util.codegen.attributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains metadata relevant to taint-analysis.
 */
public class TaintTableAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    private int attributeNameIndex;

    public int tableSize;
    public int retParamCount;
    public Map<Integer, List<Boolean>> taintTable = new LinkedHashMap<>();

    public TaintTableAttributeInfo(int attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    @Override
    public Kind getKind() {
        return Kind.TAINT_TABLE;
    }

    @Override
    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }
}

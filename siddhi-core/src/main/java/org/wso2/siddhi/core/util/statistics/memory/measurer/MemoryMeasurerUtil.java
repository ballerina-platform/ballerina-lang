package org.wso2.siddhi.core.util.statistics.memory.measurer;

import com.google.common.collect.Multiset;

/**
 * Created by sajith on 12/8/15.
 */
public class MemoryMeasurerUtil {
    public static final int WORD_LENGTH = Integer.parseInt(System.getProperty("sun.arch.data.model")) / 8;

    public static long footprintSizeEstimate(ObjectGraphMeasurer.Footprint footprint){
        long referencesMemory = WORD_LENGTH * footprint.getReferences();
        long primitivesMemory = 0;

        for (Multiset.Entry entry: footprint.getPrimitives().entrySet()){
            String dataType = entry.getElement().toString();
            if (dataType.equalsIgnoreCase("double") || dataType.equalsIgnoreCase("long")) {
                primitivesMemory += 8 * entry.getCount();
            } else if (dataType.equalsIgnoreCase("int") || dataType.equalsIgnoreCase("float")) {
                primitivesMemory += 4 * entry.getCount();
            } else if (dataType.equalsIgnoreCase("short") || dataType.equalsIgnoreCase("char")){
                primitivesMemory += 2 * entry.getCount();
            } else if (dataType.equalsIgnoreCase("byte") || dataType.equalsIgnoreCase("boolean")){
                primitivesMemory += entry.getCount();
            }
        }

        return (referencesMemory + primitivesMemory);
    }
}

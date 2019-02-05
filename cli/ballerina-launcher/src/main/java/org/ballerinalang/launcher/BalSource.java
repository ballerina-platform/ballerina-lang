package org.ballerinalang.launcher;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface to be implemented by all the a class files generated for bal files
 * containing a main function.
 * 
 * TODO: Need to move this to a common module
 */
public interface BalSource {

    public long __main(BValue[] args);

    public default Map<String, BType> __getMainFuncParams() {
        Map<String, BType> params = new HashMap<>();
        params.put("a", BTypes.typeInt);
        params.put("b", BTypes.typeInt);
        return params;
    }
}

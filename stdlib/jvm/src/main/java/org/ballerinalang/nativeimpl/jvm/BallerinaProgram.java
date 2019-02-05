package org.ballerinalang.nativeimpl.jvm;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BValue;

/**
 * Interface to be implemented by all the a class files generated for bal files
 * containing a main function.
 * 
 */
public interface BallerinaProgram {

    public void __main(BValue[] args);

    public default BType[] __getMainFuncParams() {
        return new BType[0];
    }
}

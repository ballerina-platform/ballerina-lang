package org.ballerinalang.net.jms;

import org.ballerinalang.model.NativeCallableUnit;

/**
 * A nonblocking action to be run on the BVM.
 */
public abstract class AbstractNonBlockinAction implements NativeCallableUnit {

    @Override
    public boolean isBlocking() {
        return false;
    }
}

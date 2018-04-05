package org.ballerinalang.net.jms;

import org.ballerinalang.model.NativeCallableUnit;

/**
 * A blocking action to be run on BVM.
 */
public abstract class AbstractBlockinAction implements NativeCallableUnit {

    @Override
    public boolean isBlocking() {
        return true;
    }
}

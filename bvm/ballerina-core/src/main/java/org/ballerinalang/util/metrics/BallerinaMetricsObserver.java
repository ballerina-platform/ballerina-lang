package org.ballerinalang.util.metrics;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.util.observability.BallerinaObserver;
import org.ballerinalang.util.observability.ObserverContext;

/**
 * Observe the runtime and collect measurements.
 */
public class BallerinaMetricsObserver implements BallerinaObserver {

    @Override
    public void startServerObservation(ObserverContext observerContext, WorkerExecutionContext executionContext) {

    }

    @Override
    public void startClientObservation(ObserverContext observerContext, WorkerExecutionContext executionContext) {

    }

    @Override
    public void stopObservation(ObserverContext observerContext, WorkerExecutionContext executionContext) {

    }
}

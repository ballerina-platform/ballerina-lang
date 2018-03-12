package org.ballerinalang.net.grpc;

import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.values.BStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daneshk on 3/12/18.
 */
public class GrpcCallableUnitCallBack implements CallableUnitCallback {
    private static final Logger log = LoggerFactory.getLogger(GrpcCallableUnitCallBack.class);
    private final StreamObserver<Message> requestSender;

    public GrpcCallableUnitCallBack(StreamObserver<Message> requestSender) {
        this.requestSender = requestSender;
    }

    public GrpcCallableUnitCallBack() {
        this.requestSender = null;
    }

    @Override
    public void notifySuccess() {
        //nothing to do, this will get invoked once resource finished execution
    }

    @Override
    public void notifyFailure(BStruct error) {
        MessageUtils.handleFailure(requestSender, error);
    }
}

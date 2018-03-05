package org.ballerinalang.test.util.grpc.server.helloworld;

import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.test.util.grpc.helloWorldGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldService extends helloWorldGrpc.helloWorldImplBase {
    private static final Logger log = LoggerFactory.getLogger(HelloWorldService.class);
    private static String name;
    
    @Override
    public void hello(StringValue request, StreamObserver<StringValue> responseObserver) {
        name = request.getValue();
        log.info("gRPC >> Server receive request '" + name + "'");
        StringValue response = StringValue.newBuilder().setValue("Get message from: " + name).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    public static String getName() {
        return name;
    }
}


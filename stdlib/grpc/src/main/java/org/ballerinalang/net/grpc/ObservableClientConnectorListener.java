package org.ballerinalang.net.grpc;

import org.ballerinalang.bre.Context;

public class ObservableClientConnectorListener extends ClientConnectorListener {
    private final Context context;
    ObservableClientConnectorListener(ClientCall.ClientStreamListener streamListener) {
        super(streamListener);
    }
}

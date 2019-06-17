package org.wso2.transport.http.netty.contractimpl.listener.http2;

import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Http2ServerChannel {
    private static final Logger LOG = LoggerFactory.getLogger(Http2ServerChannel.class);
    // streamIdRequestMap contains mapping of http carbon messages vs stream id to support multiplexing
    private Map<Integer, InboundMessageHolder> streamIdRequestMap = PlatformDependent.newConcurrentHashMap();
    private Map<String, Http2DataEventListener> dataEventListeners;

    public Http2ServerChannel() {
        dataEventListeners = new HashMap<>();
    }

    public void destroy() {
        streamIdRequestMap.clear();
    }

    public Map<Integer, InboundMessageHolder> getStreamIdRequestMap() {
        return streamIdRequestMap;
    }

    public InboundMessageHolder getInboundMessage(int streamId) {
        return streamIdRequestMap.get(streamId);
    }

    /**
     * Adds a listener which listen for HTTP/2 data events.
     *
     * @param name              name of the listener
     * @param dataEventListener the data event listener
     */
    public void addDataEventListener(String name, Http2DataEventListener dataEventListener) {
        dataEventListeners.put(name, dataEventListener);
    }

    /**
     * Gets the list of listeners which listen for HTTP/2 data events.
     *
     * @return list of data event listeners
     */
    public List<Http2DataEventListener> getDataEventListeners() {
        return new ArrayList<>(dataEventListeners.values());
    }
}

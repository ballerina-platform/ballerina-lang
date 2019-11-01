/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.message;

import io.netty.handler.codec.http2.Http2RemoteFlowController;
import io.netty.handler.codec.http2.Http2Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Server remote flow control listener.
 */
public class ServerRemoteFlowControlListener implements Http2RemoteFlowController.Listener {
    private static final Logger LOG = LoggerFactory.getLogger(ServerRemoteFlowControlListener.class);
    private Http2RemoteFlowController http2RemoteFlowController;
    private ConcurrentHashMap<Integer, Http2OutboundRespListener.ResponseWriter> responseWriters =
            new ConcurrentHashMap<>(); //Keep track of response writers to notify the relevant stream of its writability

    public ServerRemoteFlowControlListener(Http2RemoteFlowController http2RemoteFlowController) {
        this.http2RemoteFlowController = http2RemoteFlowController;
    }

    @Override
    public void writabilityChanged(Http2Stream stream) {
        Http2OutboundRespListener.ResponseWriter responseWriter = responseWriters.get(stream.id());
        //Writability of the push responses is not supported at the moment. Hence null check is needed.
        if (responseWriter == null) {
            return;
        }
        //Netty flow controller methods should only be called from an I/O thread.
        if (http2RemoteFlowController.isWritable(stream)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("In thread {}. Stream {} is writable. State {} ", Thread.currentThread().getName(),
                          stream.id(), stream.state());
            }
            responseWriter.setStreamWritable(true);
            responseWriter.getBackPressureObservable().notifyWritable();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("In thread {}. Stream {} is not writable. State {}. ", Thread.currentThread().getName(),
                          stream.id(), stream.state());
            }
            responseWriter.setStreamWritable(false);
        }
    }

    public void addResponseWriter(Http2OutboundRespListener.ResponseWriter responseWriter) {
        responseWriters.put(responseWriter.getStreamId(), responseWriter);
    }

    public void removeResponseWriter(Http2OutboundRespListener.ResponseWriter responseWriter) {
        if (responseWriter != null) {
            responseWriters.remove(responseWriter.getStreamId(), responseWriter);
        }
    }
}

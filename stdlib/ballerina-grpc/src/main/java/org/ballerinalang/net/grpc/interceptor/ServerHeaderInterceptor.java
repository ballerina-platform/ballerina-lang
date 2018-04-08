/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.ballerinalang.net.grpc.interceptor;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.ballerinalang.net.grpc.MessageContext;

/**
 * A interceptor to handle server header.
 *
 * @since 1.0.0
 */
public class ServerHeaderInterceptor extends AbstractHeaderInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        MessageContext ctx = readIncomingHeaders(headers);
        if (ctx != null) {
            return Contexts.interceptCall(Context.current().withValue(MessageContext.DATA_KEY, ctx), new
                    HeaderForwardingServerCall<>(call), headers, next);
        } else {
            // Don't attach a context if there is nothing to attach
            return next.startCall(new HeaderForwardingServerCall<>(call), headers);
        }
    }

    private class HeaderForwardingServerCall<ReqT, RespT> extends ForwardingServerCall
            .SimpleForwardingServerCall<ReqT, RespT> {

        HeaderForwardingServerCall(ServerCall<ReqT, RespT> delegate) {

            super(delegate);
        }

        @Override
        public void sendHeaders(Metadata headers) {
            Metadata responseHeaders = new Metadata();
            headers.merge(assignMessageHeaders(responseHeaders));
            super.sendHeaders(headers);
        }
    }
}

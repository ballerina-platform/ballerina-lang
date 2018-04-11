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

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.MessageContext;

/**
 * A interceptor to handle client headers.
 *
 * @since 1.0.0
 */
public class ClientHeaderInterceptor extends AbstractHeaderInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions, Channel channel) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor,
                callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                Metadata requestHeaders = new Metadata();
                headers.merge(assignMessageHeaders(requestHeaders));

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>
                        (responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        MessageContext ctx = readIncomingHeaders(headers);
                        if (ctx != null) {
                            Context ignore = Context.current().withValue(MessageContext.DATA_KEY, ctx).attach();
                        }
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }
}

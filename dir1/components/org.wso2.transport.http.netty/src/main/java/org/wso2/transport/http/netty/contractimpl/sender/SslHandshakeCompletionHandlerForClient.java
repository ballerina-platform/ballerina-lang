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
 *
 */

package org.wso2.transport.http.netty.contractimpl.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.wso2.transport.http.netty.contract.Constants;

import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * A handler to check whether TLS handshake has been completed and notify the listener.
 */
public class SslHandshakeCompletionHandlerForClient extends ChannelInboundHandlerAdapter {

    private ConnectionAvailabilityFuture connectionAvailabilityFuture;
    private HttpClientChannelInitializer httpClientChannelInitializer;
    private TargetHandler targetHandler;
    private SSLEngine sslEngine;

    SslHandshakeCompletionHandlerForClient(ConnectionAvailabilityFuture connectionAvailabilityFuture,
            HttpClientChannelInitializer httpClientChannelInitializer, TargetHandler targetHandler,
            SSLEngine sslEngine) {
        this.connectionAvailabilityFuture = connectionAvailabilityFuture;
        this.httpClientChannelInitializer = httpClientChannelInitializer;
        this.targetHandler = targetHandler;
        this.sslEngine = sslEngine;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SslHandshakeCompletionEvent) {
            ctx.pipeline().remove(this);

            SslHandshakeCompletionEvent event = (SslHandshakeCompletionEvent) evt;

            if (event.isSuccess()) {
                if (!httpClientChannelInitializer.getSslConfig().isDisableSsl()) {
                    try {
                        X509Certificate endUserCert = (X509Certificate) sslEngine.getSession().getPeerCertificates()[0];
                        endUserCert.checkValidity(new Date());
                    } catch (CertificateExpiredException e) {
                        connectionAvailabilityFuture
                                .notifyFailure(new SSLException("Certificate expired : " + e.getMessage()));
                        ctx.close();
                    }
                }
                this.httpClientChannelInitializer.configureHttpPipeline(ctx.pipeline(), targetHandler);
                connectionAvailabilityFuture.notifySuccess(Constants.HTTP_SCHEME);
            } else {
                Throwable cause = getTheRootCause(event);
                connectionAvailabilityFuture.notifyFailure(cause);
            }
        }
    }

    private Throwable getTheRootCause(SslHandshakeCompletionEvent event) {
        Throwable cause = event.cause();
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}


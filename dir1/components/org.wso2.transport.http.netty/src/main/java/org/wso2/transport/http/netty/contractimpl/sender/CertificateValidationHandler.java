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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.RevocationVerificationManager;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * A handler to validate certificates in certificate revocation process.
 */
public class CertificateValidationHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateValidationHandler.class);
    private SSLEngine sslEngine;
    private RevocationVerificationManager revocationVerifier;
    private int cacheSize;
    private int cacheDelay;

    public CertificateValidationHandler(SSLEngine sslEngine, int cacheDelay, int cacheSize) {
        this.sslEngine = sslEngine;
        this.cacheDelay = cacheDelay;
        this.cacheSize = cacheSize;
        this.revocationVerifier = null;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        revocationVerifier = new RevocationVerificationManager(cacheSize, cacheDelay);
        if (evt instanceof SslHandshakeCompletionEvent) {
            ctx.pipeline().remove(this);

            SslHandshakeCompletionEvent event = (SslHandshakeCompletionEvent) evt;

            if (event.isSuccess() && revocationVerifier
                    .verifyRevocationStatus(sslEngine.getSession().getPeerCertificateChain())) {
                ctx.fireChannelRead(evt);
            } else {
                ctx.close();
                throw new SSLException("Certificate Chain Validation failed. Hence closing the channel");
            }
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * When an exception occurs in channel pipeline, log error and notify the listener.
     *
     * @param ctx   Channel context
     * @param cause Exception occurred
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Exception occurred in CertificateValidationHandler.", cause);
        ctx.fireExceptionCaught(cause);
    }
}


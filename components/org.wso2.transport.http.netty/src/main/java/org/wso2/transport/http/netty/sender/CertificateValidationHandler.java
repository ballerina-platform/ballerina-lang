package org.wso2.transport.http.netty.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.certificatevalidation.RevocationVerificationManager;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * A handler to validate certificates in certificate revocation process.
 */
public class CertificateValidationHandler extends ChannelInboundHandlerAdapter {

    protected static final Logger LOG = LoggerFactory.getLogger(CertificateValidationHandler.class);
    private SSLEngine sslEngine;
    private RevocationVerificationManager revocationVerifier;
    private int cacheSize;
    private int cacheDelay;
    private HttpResponseFuture httpResponseFuture;

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
        }
       ctx.fireUserEventTriggered(evt);
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
        httpResponseFuture.notifyHttpListener(cause);
        ctx.fireExceptionCaught(cause);
    }
}


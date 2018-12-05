/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.ocsp.OcspClientHandler;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.RevocationVerificationManager;

import java.math.BigInteger;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

import static org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.Constants
        .CACHE_DEFAULT_ALLOCATED_SIZE;
import static org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.Constants
        .CACHE_DEFAULT_DELAY_MINS;

/**
 * A handler for OCSP stapling.
 */
public class OCSPStaplingHandler extends OcspClientHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OCSPStaplingHandler.class);

    public OCSPStaplingHandler(ReferenceCountedOpenSslEngine engine) {
        super(engine);
    }

    @Override
    protected boolean verify(ChannelHandlerContext ctx, ReferenceCountedOpenSslEngine engine)
            throws Exception {
        //Get the stapled ocsp response from the ssl engine.
        byte[] staple = engine.getOcspResponse();
        if (staple == null) {
            // If the response came from the server does not contain the OCSP staple, client attempts to validate
            // the certificate by directly calling OCSP access location and if that also fails, finally
            // do the CRL validation.
            RevocationVerificationManager revocationVerifier = new RevocationVerificationManager(
                    CACHE_DEFAULT_ALLOCATED_SIZE, CACHE_DEFAULT_DELAY_MINS);
            return revocationVerifier.verifyRevocationStatus(engine.getSession().getPeerCertificateChain());
        }

        OCSPResp response = new OCSPResp(staple);
        if (response.getStatus() != OCSPResponseStatus.SUCCESSFUL) {
            return false;
        }

        SSLSession session = engine.getSession();
        X509Certificate[] chain = session.getPeerCertificateChain();
        BigInteger certSerial = chain[0].getSerialNumber();

        BasicOCSPResp basicResponse = (BasicOCSPResp) response.getResponseObject();
        SingleResp singleResp = basicResponse.getResponses()[0];

        CertificateStatus status = singleResp.getCertStatus();
        BigInteger ocspSerial = singleResp.getCertID().getSerialNumber();
        if (LOG.isDebugEnabled()) {
            String message = new StringBuilder().append("OCSP status of ").append(ctx.channel().remoteAddress())
                    .append("\n  Status: ").append(status == CertificateStatus.GOOD ? "Good" : status)
                    .append("\n  This Update: ").append(singleResp.getThisUpdate()).append("\n  Next Update: ")
                    .append(singleResp.getNextUpdate()).append("\n  Cert Serial: ").append(certSerial)
                    .append("\n  OCSP Serial: ").append(ocspSerial).toString();
            LOG.debug(message);
        }
        //For an OCSP response to be valid, certificate serial number should be equal to the ocsp serial number.
        return status == CertificateStatus.GOOD && certSerial.equals(ocspSerial);
    }
}


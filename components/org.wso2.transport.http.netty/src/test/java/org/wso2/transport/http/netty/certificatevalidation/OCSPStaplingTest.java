package org.wso2.transport.http.netty.certificatevalidation;

import org.bouncycastle.cert.ocsp.OCSPResp;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.common.certificatevalidation.ocsp.OCSPVerifier;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.listener.OcspResponseBuilder;
import org.wso2.transport.http.netty.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by bhashinee on 2/27/18.
 */
public class OCSPStaplingTest {

    private String keyStoreFilePath = "/simple-test-config/localcrt.p12";
    private String keyStorePassword = "localpwd";
    private String tlsStoreType = "PKCS12";
    private X509Certificate userCertificate;
    private X509Certificate issuer;
    private List<String> ocspUrlList = new ArrayList<String>();

    @Test
    public void testRetrievingAIALocations()
            throws IOException, KeyStoreException, CertificateVerificationException {
        KeyStore keyStore = OcspResponseBuilder
                .getKeyStore(new File(TestUtil.getAbsolutePath(keyStoreFilePath)), keyStorePassword, tlsStoreType);
        Enumeration<String> aliases = keyStore.aliases();
        String alias = "";
        boolean isAliasWithPrivateKey = false;
        while (aliases.hasMoreElements()) {
            alias = (String) aliases.nextElement();
            if (isAliasWithPrivateKey = keyStore.isKeyEntry(alias)) {
                break;
            }
        }
        if (isAliasWithPrivateKey) {
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);
            userCertificate = (X509Certificate) certificateChain[0];
            issuer = (X509Certificate) certificateChain[certificateChain.length - 1];
            ocspUrlList = OCSPVerifier.getAIALocations(userCertificate);
        }
        assertEquals(ocspUrlList.get(0).toString(), "http://127.0.0.1:8080",
                "Failed to list the correct AIA locations");
    }

    @Test
    public void test()
            throws NoSuchAlgorithmException, CertificateVerificationException, UnrecoverableEntryException,
            KeyStoreException, IOException {
        SSLConfig sslConfig = new SSLConfig(new File(TestUtil.getAbsolutePath(keyStoreFilePath)), keyStorePassword);
        sslConfig.setTLSStoreType("PKCS12");
        Throwable throwable = null;
        try {
            OCSPResp ocspResp = OcspResponseBuilder.getOcspResponse(sslConfig, 51, 2);
        } catch (CertificateVerificationException e) {
            throwable = e;
        }
        boolean hasException = false;
        if (throwable.getMessage().contains("Unable to get OCSP responses from CA")) {
            hasException = true;
        }
        assertTrue(hasException, "Process has failed before getting the OCSP response");
    }
}


package org.wso2.transport.http.netty.util.server.http2;

import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.Parameter;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.util.TestUtil;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.transport.http.netty.contract.Constants.HTTPS_SCHEME;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_2_0;
import static org.wso2.transport.http.netty.contract.Constants.OPTIONAL;

public class Http2Util {

    public static ListenerConfiguration getH2ListenerConfigs() {
        Parameter paramServerCiphers = new Parameter("ciphers", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        List<Parameter> serverParams = new ArrayList<>(1);
        serverParams.add(paramServerCiphers);
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setParameters(serverParams);
        listenerConfiguration.setPort(TestUtil.SERVER_PORT1);
        listenerConfiguration.setScheme(HTTPS_SCHEME);
        listenerConfiguration.setVersion(String.valueOf(HTTP_2_0));
        listenerConfiguration.setVerifyClient(OPTIONAL);
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        listenerConfiguration.setKeyStorePass(TestUtil.KEY_STORE_PASSWORD);
        return listenerConfiguration;
    }

    public static SenderConfiguration getSenderConfigs(String httpVersion) {
        Parameter paramClientCiphers = new Parameter("ciphers", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        List<Parameter> clientParams = new ArrayList<>(1);
        clientParams.add(paramClientCiphers);
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setParameters(clientParams);
        senderConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        senderConfiguration.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        senderConfiguration.setHttpVersion(httpVersion);
        senderConfiguration.setScheme(HTTPS_SCHEME);
        return senderConfiguration;
    }
}

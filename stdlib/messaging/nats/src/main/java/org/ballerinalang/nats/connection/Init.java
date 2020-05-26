/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats.connection;

import io.nats.client.Connection;
import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;
import org.ballerinalang.nats.observability.NatsMetricsReporter;
import org.ballerinalang.nats.observability.NatsObservabilityConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Establish a connection with NATS server.
 *
 * @since 0.995
 */
public class Init {

    private static final BString RECONNECT_WAIT = StringUtils.fromString("reconnectWaitInSeconds");
    private static final String SERVER_URL_SEPARATOR = ",";
    private static final BString CONNECTION_NAME = StringUtils.fromString("connectionName");
    private static final BString MAX_RECONNECT = StringUtils.fromString("maxReconnect");
    private static final BString CONNECTION_TIMEOUT = StringUtils.fromString("connectionTimeoutInSeconds");
    private static final BString PING_INTERVAL = StringUtils.fromString("pingIntervalInMinutes");
    private static final BString MAX_PINGS_OUT = StringUtils.fromString("maxPingsOut");
    private static final BString INBOX_PREFIX = StringUtils.fromString("inboxPrefix");
    private static final BString NO_ECHO = StringUtils.fromString("noEcho");
    private static final BString ENABLE_ERROR_LISTENER = StringUtils.fromString("enableErrorListener");

    public static void externInit(ObjectValue connectionObject, ArrayValueImpl urlString, MapValue connectionConfig) {
        Options.Builder opts = new Options.Builder();
        try {
            String[] serverUrls = urlString.getStringArray();
            opts.servers(serverUrls);

            // Add connection name.
            opts.connectionName(connectionConfig.getStringValue(CONNECTION_NAME).getValue());

            // Add max reconnect.
            opts.maxReconnects(Math.toIntExact(connectionConfig.getIntValue(MAX_RECONNECT)));

            // Add reconnect wait.
            opts.reconnectWait(Duration.ofSeconds(connectionConfig.getIntValue(RECONNECT_WAIT)));

            // Add connection timeout.
            opts.connectionTimeout(Duration.ofSeconds(connectionConfig.getIntValue(CONNECTION_TIMEOUT)));

            // Add ping interval.
            opts.pingInterval(Duration.ofMinutes(connectionConfig.getIntValue(PING_INTERVAL)));

            // Add max ping out.
            opts.maxPingsOut(Math.toIntExact(connectionConfig.getIntValue(MAX_PINGS_OUT)));

            // Add inbox prefix.
            opts.inboxPrefix(connectionConfig.getStringValue(INBOX_PREFIX).getValue());

            List<ObjectValue> serviceList = Collections.synchronizedList(new ArrayList<>());

            // Add NATS connection listener.
            opts.connectionListener(new DefaultConnectionListener());

            // Add NATS error listener.
            if (connectionConfig.getBooleanValue(ENABLE_ERROR_LISTENER)) {
                ErrorListener errorListener = new DefaultErrorListener();
                opts.errorListener(errorListener);
            }

            // Add noEcho.
            if (connectionConfig.getBooleanValue(NO_ECHO)) {
                opts.noEcho();
            }

            MapValue secureSocket = connectionConfig.getMapValue(Constants.CONNECTION_CONFIG_SECURE_SOCKET);
            if (secureSocket != null) {
                SSLContext sslContext = getSSLContext(secureSocket);
                opts.sslContext(sslContext);
            }

            Connection natsConnection = Nats.connect(opts.build());
            connectionObject.addNativeData(Constants.NATS_METRIC_UTIL, new NatsMetricsReporter(natsConnection));
            connectionObject.addNativeData(Constants.NATS_CONNECTION, natsConnection);
            connectionObject.addNativeData(Constants.CONNECTED_CLIENTS, new AtomicInteger(0));
            connectionObject.addNativeData(Constants.SERVICE_LIST, serviceList);
        } catch (IOException | InterruptedException e) {
            NatsMetricsReporter.reportError(NatsObservabilityConstants.CONTEXT_CONNECTION,
                                            NatsObservabilityConstants.ERROR_TYPE_CONNECTION);
            String errorMsg = "Error while setting up a connection. " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            throw Utils.createNatsError(errorMsg);
        } catch (IllegalArgumentException e) {
            NatsMetricsReporter.reportError(NatsObservabilityConstants.CONTEXT_CONNECTION,
                                            NatsObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw Utils.createNatsError(e.getMessage());
        }
    }

    /**
     * Creates and retrieves the SSLContext from socket configuration.
     *
     * @param secureSocket secureSocket record.
     * @return Initialized SSLContext.
     */
    private static SSLContext getSSLContext(MapValue secureSocket) {
        try {
            MapValue cryptoKeyStore = secureSocket.getMapValue(Constants.CONNECTION_KEYSTORE);
            KeyManagerFactory keyManagerFactory = null;
            if (cryptoKeyStore != null) {
                char[] keyPassphrase = cryptoKeyStore.getStringValue(Constants.KEY_STORE_PASS).getValue().toCharArray();
                String keyFilePath = cryptoKeyStore.getStringValue(Constants.KEY_STORE_PATH).getValue();
                KeyStore keyStore = KeyStore.getInstance(Constants.KEY_STORE_TYPE);
                if (keyFilePath != null) {
                    try (FileInputStream keyFileInputStream = new FileInputStream(keyFilePath)) {
                        keyStore.load(keyFileInputStream, keyPassphrase);
                    }
                } else {
                    throw Utils.createNatsError(Constants.ERROR_SETTING_UP_SECURED_CONNECTION +
                                                        "Keystore path doesn't exist.");
                }
                keyManagerFactory =
                        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, keyPassphrase);
            }

            MapValue cryptoTrustStore = secureSocket.getMapValue(Constants.CONNECTION_TRUSTORE);
            TrustManagerFactory trustManagerFactory = null;
            if (cryptoTrustStore != null) {
                KeyStore trustStore = KeyStore.getInstance(Constants.KEY_STORE_TYPE);
                char[] trustPassphrase = cryptoTrustStore.getStringValue(Constants.KEY_STORE_PASS).getValue()
                        .toCharArray();
                String trustFilePath = cryptoTrustStore.getStringValue(Constants.KEY_STORE_PATH).getValue();
                if (trustFilePath != null) {
                    try (FileInputStream trustFileInputStream = new FileInputStream(trustFilePath)) {
                        trustStore.load(trustFileInputStream, trustPassphrase);
                    }
                } else {
                    throw Utils.createNatsError(Constants.ERROR_SETTING_UP_SECURED_CONNECTION
                                                        + "Truststore path doesn't exist.");
                }
                trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
            }

            String tlsVersion = secureSocket.getStringValue(Constants.CONNECTION_PROTOCOL).getValue();
            SSLContext sslContext = SSLContext.getInstance(tlsVersion);
            sslContext.init(keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                            trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null, null);
            return sslContext;
        } catch (FileNotFoundException e) {
            throw Utils
                    .createNatsError(Constants.ERROR_SETTING_UP_SECURED_CONNECTION + "File not found error, "
                                             + e.getMessage());
        } catch (CertificateException e) {
            throw Utils.createNatsError(Constants.ERROR_SETTING_UP_SECURED_CONNECTION + "Certificate error, "
                                                + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw Utils.createNatsError(Constants.ERROR_SETTING_UP_SECURED_CONNECTION + "Algorithm error, "
                                                + e.getMessage());
        } catch (IOException e) {
            throw Utils.createNatsError(Constants.ERROR_SETTING_UP_SECURED_CONNECTION + "IO error, "
                                                + e.getMessage());
        } catch (KeyStoreException e) {
            throw Utils.createNatsError(Constants.ERROR_SETTING_UP_SECURED_CONNECTION + "Keystore error, "
                                                + e.getMessage());
        } catch (UnrecoverableKeyException e) {
            throw Utils.createNatsError(
                    Constants.ERROR_SETTING_UP_SECURED_CONNECTION + "The key in the keystore cannot be recovered.");
        } catch (KeyManagementException e) {
            throw Utils
                    .createNatsError(Constants.ERROR_SETTING_UP_SECURED_CONNECTION + "Key management error, "
                                             + e.getMessage());
        }
    }
}

/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.socket;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.SocketIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Native function to open a secure client socket.
 *
 * @since 0.964.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io", functionName = "openSecureSocket",
        args = { @Argument(name = "host", type = TypeKind.STRING),
                 @Argument(name = "port", type = TypeKind.INT),
                 @Argument(name = "option", type = TypeKind.STRUCT, structType = "SocketProperties",
                           structPackage = "ballerina.io") },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "Socket", structPackage = "ballerina.io"),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io") },
        isPublic = true)
public class OpenSecureSocket extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(OpenSecureSocket.class);

    private static final String SEPARATOR = ",";
    private static final String SOCKET_PACKAGE = "ballerina.io";
    private static final String SOCKET_STRUCT_TYPE = "Socket";
    private static final String BYTE_CHANNEL_STRUCT_TYPE = "ByteChannel";

    @Override
    public void execute(Context context) {
        final String host = context.getStringArgument(0);
        final int port = (int) context.getIntArgument(0);
        if (log.isDebugEnabled()) {
            log.debug("Remote host: " + host);
            log.debug("Remote port: " + port);
        }
        final BStruct options = (BStruct) context.getRefArgument(0);
        Socket socket;
        BStruct socketStruct;
        try {
            SSLContext sslContext = getSslContext(options);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            socket = sslSocketFactory.createSocket(host, port);
            if (log.isDebugEnabled()) {
                log.debug("Socket connected: " + socket.isConnected());
                log.debug("Local port: " + socket.getLocalPort());
            }
            SSLSocket sslSocket;
            ByteChannel channel;
            if (socket instanceof SSLSocket) {
                sslSocket = (SSLSocket) socket;
                sslSocket.setUseClientMode(true);
            } else {
                log.error("Socket is not a SSLSocket instance.");
                throw new RuntimeException("Failed to get a SSLSocket instance.");
            }
            String ciphers = options.getStringField(SocketConstants.CIPHERS_OPTION_FIELD_INDEX);
            if (ciphers != null && !ciphers.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting ciphers: " + ciphers);
                }
                sslSocket.setEnabledCipherSuites(ciphers.replaceAll("\\s+", "").split(SEPARATOR));
            }
            String enabledProtocols = options.getStringField(SocketConstants.SSL_ENABLED_PROTOCOLS_OPTION_FIELD_INDEX);
            if (enabledProtocols != null && !enabledProtocols.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Enabled protocols: " + enabledProtocols);
                }
                sslSocket.setEnabledProtocols(enabledProtocols.replaceAll("\\s+", "").split(SEPARATOR));
            }
            log.debug("Start handshake!!!");
            sslSocket.startHandshake();
            log.debug("Secure handshake successful.");
            channel = new SocketByteChannel(sslSocket);
            socketStruct = createReturnStruct(context, sslSocket, channel);
            context.setReturnValues(socketStruct);
        } catch (Throwable e) {
            String msg = "Failed to open a connection to [" + host + ":" + port + "] : " + e.getMessage();
            log.error(msg, e);
            context.setReturnValues(IOUtils.createError(context, msg));
        }
    }

    private BStruct createReturnStruct(Context context, SSLSocket sslSocket, ByteChannel channel) throws IOException {
        PackageInfo ioPackageInfo = context.getProgramFile().getPackageInfo(SOCKET_PACKAGE);
        // Create ByteChannel Struct
        StructInfo channelStructInfo = ioPackageInfo.getStructInfo(BYTE_CHANNEL_STRUCT_TYPE);
        Channel ballerinaSocketChannel = new SocketIOChannel(channel, 0);
        BStruct channelStruct = BLangVMStructs.createBStruct(channelStructInfo, ballerinaSocketChannel);
        channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, ballerinaSocketChannel);

        // Create Socket Struct
        StructInfo socketStructInfo = ioPackageInfo.getStructInfo(SOCKET_STRUCT_TYPE);
        BStruct socketStruct = BLangVMStructs.createBStruct(socketStructInfo);
        socketStruct.setRefField(0, channelStruct);
        socketStruct.setIntField(0, sslSocket.getPort());
        socketStruct.setIntField(1, sslSocket.getLocalPort());
        socketStruct.setStringField(0, sslSocket.getInetAddress().getHostAddress());
        socketStruct.setStringField(1, sslSocket.getLocalAddress().getHostAddress());
        socketStruct.addNativeData(IOConstants.CLIENT_SOCKET_NAME, channel);
        return socketStruct;
    }

    private SSLContext getSslContext(BStruct options)
            throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
            IOException, KeyManagementException {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        if (log.isDebugEnabled()) {
            log.debug("SSL algorithm : " + algorithm);
        }
        String keyStorePath = options.getStringField(SocketConstants.KEY_STORE_OPTION_FIELD_INDEX);
        String keyStorePass = options.getStringField(SocketConstants.KEY_STORE_PASS_OPTION_FIELD_INDEX);
        String trustStorePath = options.getStringField(SocketConstants.TRUST_STORE_OPTION_FIELD_INDEX);
        String trustStorePass = options.getStringField(SocketConstants.TRUST_STORE_PASS_OPTION_FIELD_INDEX);
        String certPassword = options.getStringField(SocketConstants.CERT_PASS_OPTION_FIELD_INDEX);
        String protocol = options.getStringField(SocketConstants.SSL_PROTOCOL_OPTION_FIELD_INDEX);
        String sslProtocol = (protocol == null || protocol.isEmpty()) ?
                SocketConstants.DEFAULT_SSL_PROTOCOL :
                options.getStringField(SocketConstants.SSL_PROTOCOL_OPTION_FIELD_INDEX);
        if (log.isDebugEnabled()) {
            log.debug("KeyStore path: " + keyStorePath);
            log.debug("TrustStore path: " + trustStorePath);
            log.debug("Protocol: " + sslProtocol);
        }
        KeyManager[] keyManagers = getKeyManagers(certPassword, keyStorePath, keyStorePass, algorithm);
        TrustManager[] trustManagers = getTrustManagers(trustStorePath, trustStorePass, algorithm);
        SSLContext sslContext = SSLContext.getInstance(sslProtocol);
        sslContext.init(keyManagers, trustManagers, null);
        return sslContext;
    }

    private TrustManager[] getTrustManagers(String trustStore, String trustStorePass, String algorithm)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException {
        KeyStore tks = getKeyStore(trustStore, trustStorePass);
        TrustManager[] trustManagers = null;
        if (tks != null) {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(tks);
            trustManagers = tmf.getTrustManagers();
        }
        return trustManagers;
    }

    private KeyManager[] getKeyManagers(String certPassword, String keyStore, String keyStorePass, String algorithm)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException,
            CertificateException {
        KeyStore ks = getKeyStore(keyStore, keyStorePass);
        KeyManager[] keyManagers = null;
        if (ks != null) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, certPassword != null ? certPassword.toCharArray() : keyStorePass.toCharArray());
            keyManagers = kmf.getKeyManagers();
        }
        return keyManagers;
    }

    private KeyStore getKeyStore(String keyStorePath, String keyStorePassword)
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = null;
        if (keyStorePath != null && !keyStorePath.isEmpty() && keyStorePassword != null) {
            try (InputStream is = new FileInputStream(new File(keyStorePath))) {
                ks = KeyStore.getInstance(SocketConstants.PKCS_STORE_TYPE);
                ks.load(is, keyStorePassword.toCharArray());
            }
        }
        return ks;
    }
}

/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.debugger.test.utils.client.connection;

import org.ballerinalang.debugger.test.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.debugger.test.utils.DebugUtils.isInteger;

/**
 * Socket based stream connection provider.
 */
public class SocketStreamConnectionProvider extends ProcessStreamConnectionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SocketStreamConnectionProvider.class);
    private final String address;
    private final int port;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public SocketStreamConnectionProvider(List<String> commands, String workingDir, String address, int port,
                                          String balHome) {
        super(commands, workingDir, balHome);
        this.address = address;
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        CountDownLatch latch = new CountDownLatch(1);
        final Object[] finalResult = new Object[1];
        Callback callback = new Callback() {
            @Override
            public void notifySuccess(Object result) {
                latch.countDown();
                finalResult[0] = result;
            }

            @Override
            public void notifyFailure(Exception error) {
                latch.countDown();
                finalResult[0] = error;
            }
        };

        try {
            launchDebugServerAsync(callback);
            boolean await = latch.await(6000, TimeUnit.MILLISECONDS);
            if (await) {
                try {
                    socket = new Socket(address, port);
                } catch (Exception e) {
                    LOG.warn(e.getMessage());
                }

                if (socket == null) {
                    inputStream = null;
                    outputStream = null;
                    LOG.warn("Unable to make socket connection: " + this);
                } else {
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                }
            }
        } catch (InterruptedException interruptedException) {
            // Kills process stream connection as the only socket connection will be used for the communication.
            super.stop();
        }
    }

    private void launchDebugServerAsync(Callback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                super.start();
                InputStream stdIn = super.getInputStream();
                if (stdIn == null) {
                    throw new IOException("Debug adapter input stream is null.");
                }
                String line;
                try (BufferedReader buffReader = new BufferedReader(new InputStreamReader(stdIn,
                        StandardCharsets.UTF_8))) {
                    line = buffReader.readLine();
                    while (line != null && !line.contains("Debug server started on ")) {
                        // Just waits here for the debug adapter to print server init message to the std out.
                        line = buffReader.readLine();
                    }
                }
                if (line != null) {
                    String[] messageParts = line.split(" ");
                    String port = messageParts[messageParts.length - 1].trim();
                    if (isInteger(port)) {
                        callback.notifySuccess(Integer.parseInt(port));
                        return;
                    }
                }
                callback.notifyFailure(null);
            } catch (Exception e) {
                LOG.warn("Failed to launch debug server due to: " + e.getMessage(), e);
                callback.notifyFailure(e);
            }
        });
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void stop() {
        super.stop();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
        FileUtils.closeQuietly(inputStream);
        FileUtils.closeQuietly(outputStream);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SocketStreamConnectionProvider other) {
            return port == other.port && address.equals(other.address) && super.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return result ^ Objects.hashCode(this.port);
    }
}

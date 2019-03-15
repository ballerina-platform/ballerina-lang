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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.util.jvm;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * An in-memory jar class loader.
 *
 * @since 0.955.0
 */

public class JBallerinaInMemoryClassLoader {

    private byte[] jarBinaryContent = null;

    public JBallerinaInMemoryClassLoader(byte[] data) {
        jarBinaryContent = new byte[data.length];
        System.arraycopy(data, 0, jarBinaryContent, 0, data.length);
    }

    public Class<?> loadClass(String className) {

        final Map<String, byte[]> map = new HashMap<>();
        try (JarInputStream is = new JarInputStream(new ByteArrayInputStream(jarBinaryContent))) {
            JarEntry nextEntry;
            while ((nextEntry = is.getNextJarEntry()) != null) {
                final int est = (int) nextEntry.getSize();
                byte[] data = new byte[est > 0 ? est : 1024];
                int real = 0;

                for (int r = is.read(data); r > 0; r = is.read(data, real, data.length - real)) {
                    if (data.length == (real += r)) {
                        data = Arrays.copyOf(data, data.length * 2);
                    }
                }

                if (real != data.length) {
                    data = Arrays.copyOf(data, real);
                }

                map.put("/" + nextEntry.getName(), data);
            }

            URL jarFileUrl = new URL("x-buffer", null, -1, "/", new InMemoryURLStreamHandler(map));

            URLClassLoader cl = URLClassLoader.newInstance(new URL[]{
                    jarFileUrl
            });

            return cl.loadClass(className);
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Class '" + className + "' cannot be loaded in-memory", e);
        }
    }

    static class InMemoryURLStreamHandler extends URLStreamHandler {

        private final Map<String, byte[]> jarFiles;

        InMemoryURLStreamHandler(Map<String, byte[]> jarFiles) {

            this.jarFiles = jarFiles;
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {

            final byte[] data = jarFiles.get(url.getFile());

            if (data == null) {
                throw new FileNotFoundException(url.getFile());
            }

            return new URLConnection(url) {
                @Override
                public void connect() {

                }

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(data);
                }
            };
        }
    }
}

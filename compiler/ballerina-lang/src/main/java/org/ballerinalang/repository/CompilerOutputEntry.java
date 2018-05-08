/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.repository;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@code CompilerOutputEntry} represents compiled package entry.
 *
 * @since 0.970.0
 */
public interface CompilerOutputEntry {

    /**
     * Specifies the kinds of compiler output entries.
     *
     * @since 0.970.0
     */
    enum Kind {
        SRC("src"),
        OBJ("obj"),
        ROOT("/");

        private String value;

        Kind(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Returns the entry name, e.g. file name.
     *
     * @return the entry name
     */
    String getEntryName();

    /**
     * Returns the {@link Kind} of the output entry.
     *
     * @return the {@link Kind} of the output entry
     */
    Kind getEntryKind();

    /**
     * Returns the content as an input stream.
     *
     * @return the {@link InputStream} of the entry
     */
    InputStream getInputStream() throws IOException;
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.runtime.message;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Data source for binary payloads.
 *
 * @since 0.95.5
 */
public class BlobDataSource extends BallerinaMessageDataSource {

    private byte[] value;

    public BlobDataSource(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public void serializeData(OutputStream outputStream) {
        try {
            outputStream.write(value);
        } catch (IOException e) {
            throw new BallerinaException("Error occurred while writing the binary payload to the output stream", e);
        }
    }

    @Override
    public String getMessageAsString() {
        return new String(value, Charset.forName("UTF-8"));
    }

    @Override
    public BallerinaMessageDataSource clone() {
        return new BlobDataSource(value.clone());
    }

    @Override
    public Object getDataObject() {
        return this.value;
    }
}

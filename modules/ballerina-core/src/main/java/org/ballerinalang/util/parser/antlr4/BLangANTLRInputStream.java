/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.util.parser.antlr4;

import org.antlr.v4.runtime.ANTLRInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class BLangANTLRInputStream extends ANTLRInputStream {
    
    private boolean consume = true;
    
    /**
     * @param inputStream
     * @throws IOException 
     */
    public BLangANTLRInputStream(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    public void setConsume(boolean consume) {
        this.consume = consume;
    }

    @Override
    public void consume() {
        if (consume) {
            super.consume();
        }
    }
}

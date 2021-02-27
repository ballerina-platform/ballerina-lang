/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.transactions;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates XID for the distributed transactions.
 *
 * @since 1.0
 */
public class XIDGenerator {

    private static final Random rand = new Random();
    private static final AtomicInteger formatIdIdGenerator = new AtomicInteger();

    private static byte[] randomBytes() {
        final byte[] bytes = new byte[48];
        rand.nextBytes(bytes);
        return bytes;
    }

    static XATransactionID createXID() {
        final byte[] branchQualifier = randomBytes();
        final byte[] globalTransactionId = randomBytes();
        return new XATransactionID(formatIdIdGenerator.incrementAndGet(), branchQualifier, globalTransactionId);
    }
}

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.ballerinalang.util;

import static org.wso2.ballerinalang.compiler.util.Names.ORG_NAME_SEPARATOR;

/**
 * Hold transaction related entities.
 *
 * @since 0.95.5
 */
public class Transactions {
    public static final String TRANSACTION_ANNOTATION_NAME = "transactions" + ORG_NAME_SEPARATOR + "Participant";
    public static final String TRX_ONCOMMIT_FUNC = "oncommit";
    public static final String TRX_ONABORT_FUNC = "onabort";

    public static boolean isTransactionsAnnotation(String orgName, String pkgName) {
        StringBuilder pathBuilder = new StringBuilder();
        String createdPath = pathBuilder.append(orgName).append(ORG_NAME_SEPARATOR).append(pkgName).toString();
        return createdPath.equals(TRANSACTION_ANNOTATION_NAME);
    }

    /**
     * Type of transaction, initiator or participant.
     *
     * @since 0.990.0
     */
    public enum TransactionType {
        INITIATOR(0),
        PARTICIPANT(1),
        REMOTE_PARTICIPANT(2);

        public final int value;

        TransactionType(int val) {
            this.value = val;
        }
    }

    /**
     * The status codes for transaction.
     *
     * @since 0.95.5
     */
    public enum TransactionStatus {
        BLOCK_END(0),
        FAILED(-1),
        ABORTED(-2),
        END(1);

        private final int status;

        TransactionStatus(int value) {
            status = value;
        }

        public int value() {
            return status;
        }

        public static TransactionStatus getConst(int statusValue) {
            switch (statusValue) {
                case 0:
                    return BLOCK_END;
                case 1:
                    return END;
                case -1:
                    return FAILED;
                case -2:
                    return ABORTED;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}

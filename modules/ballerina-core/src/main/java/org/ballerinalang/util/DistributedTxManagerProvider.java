/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util;

import com.atomikos.icatch.jta.UserTransactionManager;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * {@code DistributedTxManagerProvider} creates UserTransactionManager for distributed transactions.
 *
 * @since 0.87
 */
public class DistributedTxManagerProvider {
    private static DistributedTxManagerProvider distributedTxManagerProvider = null;
    private TransactionManager transactionManager;

    protected DistributedTxManagerProvider() {
        try {
            UserTransactionManager utm = new UserTransactionManager();
            utm.init();
            this.transactionManager = utm;
        } catch (SystemException e) {
            throw new BallerinaException("distributed transaction manager initialization failed");
        }
    }

    public static DistributedTxManagerProvider getInstance() {
        if (distributedTxManagerProvider == null) {
            synchronized (DistributedTxManagerProvider.class) {
                if (distributedTxManagerProvider == null) {
                    distributedTxManagerProvider = new DistributedTxManagerProvider();
                }
            }
        }
        return distributedTxManagerProvider;
    }

    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }
}

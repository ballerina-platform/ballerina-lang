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
package org.ballerinalang.util.transactions;

/**
 * {@code TransactionConstants} Define transaction related constants.
 *
 * @since 0.964.0
 */
public class TransactionConstants {
    //Co-ordinator functions
    public static final String COORDINATOR_BEGIN_TRANSACTION = "beginTransaction";
    public static final String COORDINATOR_END_TRANSACTION = "endTransaction";
    public static final String COORDINATOR_ABORT_TRANSACTION = "abortTransaction";
    public static final String COORDINATOR_IS_INITIATOR = "isInitiator";

    public static final String COORDINATOR_PACKAGE = "ballerina.transactions";

    public static final int DEFAULT_RETRY_COUNT = 3;

    public static final String DEFAULT_COORDINATION_TYPE = "2pc";
}

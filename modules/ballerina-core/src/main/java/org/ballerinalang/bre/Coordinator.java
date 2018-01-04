/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.bre;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code Coordinator} registry to hold the service flow.
 */
public class Coordinator {

    Map transactions = new HashMap<Integer, List<Participant>>();

    public MicroTransactionContext createContext(String coordinationType) {
        return new MicroTransactionContext("3ef1", coordinationType, 1.0, "localhost:9090");
    }

    public void register(int transactionId, int participantId, String registerAtURL) {

        if (isRegisteredParticipant(participantId, (List) transactions.get(transactionId))) {

        } else {
            if (transactions.keySet().contains(transactionId)) {
                List<Participant> participants = (List) transactions.get(transactionId);
                participants.add(new Participant(participantId, "http", false));
            }
        }

    }

    private boolean isRegisteredParticipant(int participantId, List participants) {
        return participants.contains(participantId);
    }

    public void setInitiator(int transactionId) {

    }
}

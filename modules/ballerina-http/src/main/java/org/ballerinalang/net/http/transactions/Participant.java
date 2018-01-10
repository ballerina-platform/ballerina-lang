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

package org.ballerinalang.net.http.transactions;

/**
 * {@code Participant} represents a service in the flow.
 */
public class Participant {

    private final int participantId;
    private final String participantProtocols;
    private final boolean isInitiator;

    public Participant(int participantId, String participantProtocols, boolean isInitiator) {
        this.participantId = participantId;
        this.participantProtocols = participantProtocols;
        this.isInitiator = isInitiator;
    }

    public int getParticipantId() {
        return participantId;
    }

    public String getParticipantProtocols() {
        return participantProtocols;
    }

    public boolean isInitiator() {
        return isInitiator;
    }
}

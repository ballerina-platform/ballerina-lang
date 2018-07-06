/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.common;

/**
 * Inbound request and outbound response state.
 *
 * CONNECTED - State between connection creation and start of payload read
 * EXPECT_100_CONTINUE_HEADER_RECEIVED - Special state of receiving request with expect:100-continue header
 * RESPONSE_100_CONTINUE_SENT - Special state of sending 100-continue response
 * RECEIVING_ENTITY_BODY - State between start and end of payload read
 * ENTITY_BODY_RECEIVED - State between end of payload read and start of response write
 * SENDING_ENTITY_BODY - State between start and end of response write
 * ENTITY_BODY_SENT - State of successfully written response
 */
public enum SourceInteractiveState {
    CONNECTED,
    EXPECT_100_CONTINUE_HEADER_RECEIVED,
    RESPONSE_100_CONTINUE_SENT,
    RECEIVING_ENTITY_BODY,
    ENTITY_BODY_RECEIVED,
    SENDING_ENTITY_BODY,
    ENTITY_BODY_SENT
}

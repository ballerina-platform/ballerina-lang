/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.eventsync;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;

/**
 * Test Subscriber to test Event Sync publish subscriber.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber")
public class EventSyncTestSubscriber implements EventSubscriber {

    public static final String NAME = "Event sync test subscriber";
    public static boolean gotEvent = false;

    @Override
    public EventKind eventKind() {
        return EventKind.PROJECT_UPDATE;
    }

    @Override
    public void onEvent(ExtendedLanguageClient client,
                        DocumentServiceContext context,
                        LanguageServerContext languageServerContext) {
        gotEvent = true;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

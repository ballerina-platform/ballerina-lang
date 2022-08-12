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
package org.ballerinalang.langserver.eventsync.publishers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.eventsync.AbstractEventPublisher;

/**
 * Publishes the pull module event.
 *
 * @since 2201.2.1
 */
@JavaSPIService("org.ballerinalang.langserver.eventsync.EventPublisher")
public class PullModulePublisher extends AbstractEventPublisher {

    public static final String NAME = "Pull module event publisher";

    @Override
    public EventKind getKind() {
        return EventKind.PULL_MODULE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void publish(ExtendedLanguageClient client, LanguageServerContext serverContext,
                        DocumentServiceContext context) {
        subscribers.forEach(subscriber -> subscriber.onEvent(client, context, serverContext));
    }
}

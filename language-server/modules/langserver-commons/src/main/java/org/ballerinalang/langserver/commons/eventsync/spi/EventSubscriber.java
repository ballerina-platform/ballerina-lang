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
package org.ballerinalang.langserver.commons.eventsync.spi;

import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.EventKind;

/**
 * Represents the language server event subscriber.
 *
 * @since 2201.1.1
 */
public interface EventSubscriber {

    /**
     * EventKind of the publisher that subscriber has subscribed to.
     *
     * @return {@link EventKind} EventKind.
     */
    EventKind eventKind();

    /**
     * Execute for the subscriber when the subscribed publisher publishes.
     * 
     * @param client                  Extended Language Client
     * @param context                 Document Service Context
     * @param languageServerContext   Language Server Context
     */
    void onEvent(ExtendedLanguageClient client, DocumentServiceContext context, 
                 LanguageServerContext languageServerContext);

    /**
     * @return name of the Subscriber.
     */
    String getName();
}

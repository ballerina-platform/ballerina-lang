/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.observability;

/**
 * Observe Ballerina Server and Client execution code.
 */
public interface BallerinaObserver {

    /**
     * Start observing server execution code.
     *
     * @param observerContext  The {@link ObserverContext}
     */
    void startServerObservation(ObserverContext observerContext);

    /**
     * Start observing client execution code.
     *
     * @param observerContext  The {@link ObserverContext}
     */
    void startClientObservation(ObserverContext observerContext);

    /**
     * Stop observing server execution code.
     *
     * @param observerContext  The {@link ObserverContext}
     */
    void stopServerObservation(ObserverContext observerContext);

    /**
     * Stop observing client execution code.
     *
     * @param observerContext  The {@link ObserverContext}
     */
    void stopClientObservation(ObserverContext observerContext);

}

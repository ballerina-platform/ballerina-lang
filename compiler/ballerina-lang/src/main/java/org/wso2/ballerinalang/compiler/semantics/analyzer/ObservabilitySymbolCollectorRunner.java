/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Used to analyze ballerina code and store observability symbols in the Ballerina program artifact.
 *
 * @since 2.0.0
 */
public class ObservabilitySymbolCollectorRunner {
    private static final CompilerContext.Key<ObservabilitySymbolCollector> OBSERVABILITY_SYMBOL_COLLECTOR_KEY =
            new CompilerContext.Key<>();

    public static synchronized ObservabilitySymbolCollector getInstance(CompilerContext context) {
        ObservabilitySymbolCollector observabilitySymbolCollector = context.get(OBSERVABILITY_SYMBOL_COLLECTOR_KEY);
        if (observabilitySymbolCollector == null) {
            ServiceLoader<ObservabilitySymbolCollector> observabilitySymbolCollectors
                    = ServiceLoader.load(ObservabilitySymbolCollector.class);

            Iterator<ObservabilitySymbolCollector> collectorIterator = observabilitySymbolCollectors.iterator();
            if (collectorIterator.hasNext()) {
                observabilitySymbolCollector = collectorIterator.next();
            } else {
                observabilitySymbolCollector = new NoOpObservabilitySymbolCollector();
            }
            context.put(OBSERVABILITY_SYMBOL_COLLECTOR_KEY, observabilitySymbolCollector);
        }

        return observabilitySymbolCollector;
    }

}

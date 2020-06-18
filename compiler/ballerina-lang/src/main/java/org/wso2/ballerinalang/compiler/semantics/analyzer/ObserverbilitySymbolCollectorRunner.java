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

import java.io.PrintStream;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Used to analyze ballerina code and store observabilty symbols in the Ballerina program artifact.
 *
 * @since 2.0.0
 */
public class ObserverbilitySymbolCollectorRunner {
    private static final CompilerContext.Key<ObservabilitySymbolCollector> OBSERVARBILITY_SYMBOL_COLLECTOR_KEY =
            new CompilerContext.Key<>();

    private static final String NAME = "name";

    private static final PrintStream console = System.out;;

    private ObserverbilitySymbolCollectorRunner(CompilerContext context) {
        console.println("Choreo compiler plugin initialized");
    }

    public static synchronized ObservabilitySymbolCollector getInstance(CompilerContext context) {
        ObservabilitySymbolCollector observabilitySymbolCollector = context.get(OBSERVARBILITY_SYMBOL_COLLECTOR_KEY);
        if (observabilitySymbolCollector == null) {
            ServiceLoader<ObservabilitySymbolCollector> observerbilitySymbolCollectors
                    = ServiceLoader.load(ObservabilitySymbolCollector.class);

            Iterator<ObservabilitySymbolCollector> collectorIterator = observerbilitySymbolCollectors.iterator();
            if (collectorIterator.hasNext()) {
                observabilitySymbolCollector = collectorIterator.next();
                observabilitySymbolCollector.init(context);
            } else {
                observabilitySymbolCollector = new NullObservabiltySymbolCollector();
            }
            context.put(OBSERVARBILITY_SYMBOL_COLLECTOR_KEY, observabilitySymbolCollector);
        }

        return observabilitySymbolCollector;
    }

}

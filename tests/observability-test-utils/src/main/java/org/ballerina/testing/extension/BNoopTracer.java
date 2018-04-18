/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerina.testing.extension;

import io.opentracing.Tracer;
import org.ballerina.testing.extension.noop.NoopTracer;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.util.tracer.OpenTracer;
import org.ballerinalang.util.tracer.exception.InvalidConfigurationException;

/**
 * Tracer extension that returns an instance of Mock tracer.
 */
@JavaSPIService("org.ballerinalang.util.tracer.OpenTracer")
public class BNoopTracer implements OpenTracer {

    private static final String NAME = "noop";

    @Override
    public Tracer getTracer(String tracerName, String serviceName) {
        return NoopTracer.INSTANCE;
    }

    @Override
    public void init() throws InvalidConfigurationException {
        // Do Nothing
    }

    @Override
    public String getName() {
        return NAME;
    }
}

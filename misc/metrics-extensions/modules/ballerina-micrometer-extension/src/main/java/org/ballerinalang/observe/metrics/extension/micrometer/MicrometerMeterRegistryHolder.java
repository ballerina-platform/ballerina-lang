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
package org.ballerinalang.observe.metrics.extension.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.observe.metrics.extension.micrometer.spi.MeterRegistryProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TABLE_METRICS;

/**
 * Hold an instance of {@link MeterRegistry}.
 */
public class MicrometerMeterRegistryHolder {

    private MicrometerMeterRegistryHolder() {
    }

    private static class LazyHolder {
        private static final String METER_REGISTRY_NAME = CONFIG_TABLE_METRICS + ".micrometer_registry";
        private static final MeterRegistry INSTANCE;

        static {
            ConfigRegistry configRegistry = ConfigRegistry.getInstance();
            String registryName = configRegistry.getConfiguration(METER_REGISTRY_NAME);
            // Look for MeterRegistryProvider implementations
            Iterator<MeterRegistryProvider> meterRegistryProviders = ServiceLoader.load(MeterRegistryProvider.class)
                    .iterator();
            MeterRegistryProvider meterRegistryProvider = null;
            while (meterRegistryProviders.hasNext()) {
                MeterRegistryProvider temp = meterRegistryProviders.next();
                if (registryName != null && registryName.equalsIgnoreCase(temp.getName())) {
                    meterRegistryProvider = temp;
                    break;
                } else {
                    // Use a random provider
                    meterRegistryProvider = temp;
                }
            }
            MeterRegistry meterRegistry = null;
            if (meterRegistryProvider != null) {
                meterRegistry = meterRegistryProvider.get();
            }
            if (meterRegistry == null) {
                // This is a CompositeMeterRegistry and it is like a no-op registry when there are no other registries.
                meterRegistry = Metrics.globalRegistry;
            }
            INSTANCE = meterRegistry;
        }
    }

    public static MeterRegistry getInstance() {
        return LazyHolder.INSTANCE;
    }

}

/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/

package org.ballerinalang.util.tracer.config;


import java.util.Set;

/**
 * This is the configuration class which holds the list of tracers configured.
 */

public class OpenTracingConfig {

    private Set<TracerConfig> tracers;

    public Set<TracerConfig> getTracers() {
        return tracers;
    }

    public void setTracers(Set<TracerConfig> tracers) {
        this.tracers = tracers;
    }

    public TracerConfig getTracer(String tracerName) {
        if (tracerName != null) {
            for (TracerConfig tracer : this.tracers) {
                if (tracer.getName().equalsIgnoreCase(tracerName.trim())) {
                    return tracer;
                }
            }
        }
        return null;
    }

}

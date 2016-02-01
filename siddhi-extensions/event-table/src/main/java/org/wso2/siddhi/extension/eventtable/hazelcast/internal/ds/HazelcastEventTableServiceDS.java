/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.eventtable.hazelcast.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

@Component(
        name = "hazelcastEventTable.component",
        immediate = true
)
public class HazelcastEventTableServiceDS {
    private static final Logger logger = Logger.getLogger(HazelcastEventTableServiceDS.class);

    @Activate
    protected void activate(ComponentContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Successfully activated Hazelcast Event Table");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Successfully deactivated Hazelcast Event Table");
        }
    }

    @Reference(
            name = "hazelcast.instance.service",
            service = HazelcastInstance.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHazelcastInstance"
    )
    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        HazelcastEventTableServiceValueHolder.registerHazelcastInstance(hazelcastInstance);
    }

    protected void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        HazelcastEventTableServiceValueHolder.registerHazelcastInstance(null);
    }
}

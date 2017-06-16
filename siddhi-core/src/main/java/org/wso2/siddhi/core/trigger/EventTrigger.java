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

package org.wso2.siddhi.core.trigger;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;

/**
 * Interface class to represent event triggers. Event triggers are used to trigger events within Siddhi itself
 * according to a user given criteria.
 */
public interface EventTrigger extends EternalReferencedHolder {

    void init(TriggerDefinition triggerDefinition, SiddhiAppContext siddhiAppContext, StreamJunction
            streamJunction);

    TriggerDefinition getTriggerDefinition();

    String getId();

}

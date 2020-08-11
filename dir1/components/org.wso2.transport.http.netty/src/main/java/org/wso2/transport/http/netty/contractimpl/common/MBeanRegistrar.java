/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.contractimpl.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;

import java.lang.management.ManagementFactory;
import java.util.Set;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Mbean class for managing cache.
 */
public class MBeanRegistrar {

    private static final MBeanRegistrar ourInstance = new MBeanRegistrar();
    private static final Logger LOG = LoggerFactory.getLogger(MBeanRegistrar.class);

    public static MBeanRegistrar getInstance() {
        return ourInstance;
    }

    private MBeanRegistrar() {
    }

    public boolean registerMBean(Object mBeanInstance, String category, String id) {
        assertNull(mBeanInstance, "MBean instance is null");
        assertNull(category, "MBean instance category is null");
        assertNull(id, "MBean instance name is null");
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(getObjectName(category, id));
            Set set = mBeanServer.queryNames(objectName, null);
            if (set != null && set.isEmpty()) {
                mBeanServer.registerMBean(mBeanInstance, objectName);
            } else {
                mBeanServer.unregisterMBean(objectName);
                mBeanServer.registerMBean(mBeanInstance, objectName);
            }
            return true;
        } catch (MalformedObjectNameException | NotCompliantMBeanException | MBeanRegistrationException
                | InstanceNotFoundException | InstanceAlreadyExistsException e) {
            LOG.warn("Error registering a MBean with name ' " + id + " ' and category name ' " + category
                    + "' for JMX management", e);
            return false;
        }
    }

    private String getObjectName(String category, String id) {

        String jmxAgentName = System.getProperty(Constants.JMX_AGENT_NAME);
        if (jmxAgentName == null || jmxAgentName.isEmpty()) {
            jmxAgentName = "ballerina";
        }
        return jmxAgentName + ":Type=" + category + ",Name=" + id;
    }

    private void assertNull(Object object, String msg) {
        if (object == null) {
            handleException(msg);
        }
    }

    private static void handleException(String msg) {
        LOG.error(msg);
    }
}


/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.transport.http.netty.contract.config;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB representation of the Netty transport configuration.
 */
@SuppressWarnings("unused")
@XmlRootElement(name = "transports")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransportsConfiguration {
    /**
     * @deprecated
     * @return the default transport configuration.
     */
    @Deprecated
    public static TransportsConfiguration getDefault() {
        TransportsConfiguration defaultConfig = new TransportsConfiguration();
        ListenerConfiguration listenerConfiguration = ListenerConfiguration.getDefault();
        HashSet<ListenerConfiguration> listenerConfigurations = new HashSet<>();
        listenerConfigurations.add(listenerConfiguration);
        defaultConfig.setListenerConfigurations(listenerConfigurations);
        SenderConfiguration senderConfiguration = SenderConfiguration.getDefault();
        HashSet<SenderConfiguration> senderConfigurations = new HashSet<>();
        senderConfigurations.add(senderConfiguration);
        defaultConfig.setSenderConfigurations(senderConfigurations);
        HashSet<TransportProperty> transportProperties = new HashSet<>();
        defaultConfig.setTransportProperties(transportProperties);
        return defaultConfig;
    }

    public TransportsConfiguration () {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfigurations = new HashSet<>();
        listenerConfigurations.add(listenerConfiguration);

        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfigurations = new HashSet<>();
        senderConfigurations.add(senderConfiguration);
    }

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private Set<TransportProperty> transportProperties = new HashSet<>();


    @XmlElementWrapper(name = "listeners")
    @XmlElement(name = "listener")
    private Set<ListenerConfiguration> listenerConfigurations;

    @XmlElementWrapper(name = "senders")
    @XmlElement(name = "sender")
    private Set<SenderConfiguration> senderConfigurations;

    public Set<ListenerConfiguration> getListenerConfigurations() {
        if (listenerConfigurations == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(listenerConfigurations);
    }

    public Set<SenderConfiguration> getSenderConfigurations() {
        if (senderConfigurations == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(senderConfigurations);
    }

    public void setListenerConfigurations(Set<ListenerConfiguration> listenerConfigurations) {
        this.listenerConfigurations = Collections.unmodifiableSet(listenerConfigurations);
    }

    public void setSenderConfigurations(Set<SenderConfiguration> senderConfigurations) {
        this.senderConfigurations = Collections.unmodifiableSet(senderConfigurations);
    }

    public Set<TransportProperty> getTransportProperties() {
        return transportProperties;
    }

    public void setTransportProperties(Set<TransportProperty> transportProperties) {
        this.transportProperties = transportProperties;
    }
}

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.extension.input.transport;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.MessageType;
import org.wso2.carbon.event.input.adapter.core.Property;
import org.wso2.siddhi.extension.input.transport.util.JMSEventAdapterConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The jms event adapter factory class to create a jms input adapter
 */
public class JMSEventAdapterFactory extends InputEventAdapterFactory {

    private ResourceBundle resourceBundle = ResourceBundle.getBundle
            ("org.wso2.carbon.event.input.adapter.jms.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return JMSEventAdapterConstants.ADAPTER_TYPE_JMS;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportInputMessageTypes = new ArrayList<String>();

        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.MAP);
        supportInputMessageTypes.add(MessageType.TEXT);

        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();

        // Topic
        Property topicProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION);
        topicProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION_HINT));
        propertyList.add(topicProperty);

        // JNDI initial context factory class
        Property initialContextProperty = new Property(JMSEventAdapterConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS);
        initialContextProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS));
        initialContextProperty.setRequired(true);
        initialContextProperty.setHint(resourceBundle.getString(
                JMSEventAdapterConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS_HINT));
        propertyList.add(initialContextProperty);

        // JNDI Provider URL
        Property javaNamingProviderUrlProperty = new Property(JMSEventAdapterConstants.JAVA_NAMING_PROVIDER_URL);
        javaNamingProviderUrlProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.JAVA_NAMING_PROVIDER_URL));
        javaNamingProviderUrlProperty.setRequired(true);
        javaNamingProviderUrlProperty.setHint(resourceBundle.getString(
                JMSEventAdapterConstants.JAVA_NAMING_PROVIDER_URL_HINT));
        propertyList.add(javaNamingProviderUrlProperty);


        // JNDI Username
        Property userNameProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_USERNAME);
        userNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_USERNAME));
        propertyList.add(userNameProperty);

        // JNDI Password
        Property passwordProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_PASSWORD);
        passwordProperty.setSecured(true);
        passwordProperty.setEncrypted(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_PASSWORD));
        propertyList.add(passwordProperty);

        // Connection Factory JNDI Name
        Property connectionFactoryNameProperty = new Property(
                JMSEventAdapterConstants.ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME);
        connectionFactoryNameProperty.setRequired(true);
        connectionFactoryNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME));
        connectionFactoryNameProperty.setHint(resourceBundle.getString(
                JMSEventAdapterConstants.ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME_HINT));
        propertyList.add(connectionFactoryNameProperty);

        // Destination Type
        Property destinationTypeProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION_TYPE);
        destinationTypeProperty.setRequired(true);
        destinationTypeProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION_TYPE));
        destinationTypeProperty.setOptions(new String[]{JMSEventAdapterConstants.DESTINATION_TOPIC, JMSEventAdapterConstants.DESTINATION_QUEUE});
        destinationTypeProperty.setDefaultValue(JMSEventAdapterConstants.DESTINATION_TOPIC);
        destinationTypeProperty.setHint(resourceBundle.getString(
                JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION_TYPE_HINT));
        propertyList.add(destinationTypeProperty);


        Property isDurableSubscriptionProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_SUBSCRIPTION_DURABLE);
        isDurableSubscriptionProperty.setRequired(false);
        isDurableSubscriptionProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_SUBSCRIPTION_DURABLE));
        isDurableSubscriptionProperty.setHint(resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_SUBSCRIPTION_DURABLE_HINT));
        isDurableSubscriptionProperty.setOptions(new String[]{"true", "false"});
        isDurableSubscriptionProperty.setDefaultValue("false");
        propertyList.add(isDurableSubscriptionProperty);

        // Connection Factory JNDI Name
        Property subscriberNameProperty = new Property(JMSEventAdapterConstants.ADAPTER_JMS_DURABLE_SUBSCRIBER_CLIENT_ID);
        subscriberNameProperty.setRequired(false);
        subscriberNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_JMS_DURABLE_SUBSCRIBER_CLIENT_ID));
        subscriberNameProperty.setHint(resourceBundle.getString(
                JMSEventAdapterConstants.ADAPTER_JMS_DURABLE_SUBSCRIBER_CLIENT_ID_HINT));
        propertyList.add(subscriberNameProperty);

        Property jmsProperties = new Property(JMSEventAdapterConstants.ADAPTER_PROPERTIES);
        jmsProperties.setDisplayName(resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_PROPERTIES));
        jmsProperties.setHint(resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_PROPERTIES_HINT));
        jmsProperties.setRequired(false);
        propertyList.add(jmsProperties);

        Property jmsSecuredProperties = new Property(JMSEventAdapterConstants.ADAPTER_SECURED_PROPERTIES);
        jmsSecuredProperties.setDisplayName(resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_SECURED_PROPERTIES));
        jmsSecuredProperties.setHint(resourceBundle.getString(JMSEventAdapterConstants.ADAPTER_SECURED_PROPERTIES_HINT));
        jmsSecuredProperties.setRequired(false);
        jmsSecuredProperties.setEncrypted(true);
        propertyList.add(jmsSecuredProperties);

        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                                                Map<String, String> globalProperties) {
        return new JMSEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}

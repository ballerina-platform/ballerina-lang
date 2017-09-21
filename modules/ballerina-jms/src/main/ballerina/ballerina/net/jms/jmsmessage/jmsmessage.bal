package ballerina.net.jms.jmsmessage;

import ballerina.doc;
import ballerina.net.jms;

@doc:Description { value:"Sets a JMS transport string property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The string property name" }
@doc:Param { value:"value: The string property value" }
native function setStringProperty (jms:JMSMessage msg, string key, string value);

@doc:Description { value:"Gets a JMS transport string property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The string property name" }
@doc:Return { value:"string: The string property value" }
native function getStringProperty (jms:JMSMessage msg, string key) (string);

@doc:Description { value:"Sets a JMS transport integer property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The integer property name" }
@doc:Param { value:"value: The integer property value" }
native function setIntProperty (jms:JMSMessage msg, string key, int value);

@doc:Description { value:"Gets a JMS transport integer property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The integer property name" }
@doc:Return { value:"int: The integer property value" }
native function getIntProperty (jms:JMSMessage msg, string key) (int);

@doc:Description { value:"Sets a JMS transport boolean property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The boolean property name" }
@doc:Param { value:"value: The boolean property value" }
native function setBooleanProperty (jms:JMSMessage msg, string key, boolean value);

@doc:Description { value:"Gets a JMS transport boolean property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The boolean property name" }
@doc:Return { value:"boolean: The boolean property value" }
native function getBooleanProperty (jms:JMSMessage msg, string key) (boolean);

@doc:Description { value:"Sets a JMS transport float property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The float property name" }
@doc:Param { value:"value: The float property value" }
native function setFloatProperty (jms:JMSMessage msg, string key, float value);

@doc:Description { value:"Gets a JMS transport float property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The float property name" }
@doc:Return { value:"float: The float property value" }
native function getFloatProperty (jms:JMSMessage msg, string key) (float);

@doc:Description { value:"Sets a JMS transport header from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function setHeader (jms:JMSMessage msg, string key, string value);

@doc:Description { value:"Get a JMS transport header from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The header name" }
@doc:Return { value:"string: The header value" }
native function getHeader (jms:JMSMessage msg, string key) (string value);

@doc:Description { value:"Sets text content for the JMS message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"content: Text Message Content" }
native function setTextMessageContent (jms:JMSMessage msg, string content);

@doc:Description { value:"Gets text content of the JMS message"}
@doc:Param { value:"msg: A request message" }
@doc:Return { value:"string: Text Message Content" }
native function getTextMessageContent (jms:JMSMessage msg) (string value);
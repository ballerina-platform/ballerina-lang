package ballerina.net.jms.jmsmessage;

import ballerina.doc;
import ballerina.net.jms;

@doc:Description { value:"Sets a JMS transport string property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The string property name" }
@doc:Param { value:"value: The string property value" }
public native function setStringProperty (jms:JMSMessage msg, string key, string value);

@doc:Description { value:"Gets a JMS transport string property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The string property name" }
@doc:Return { value:"string: The string property value" }
public native function getStringProperty (jms:JMSMessage msg, string key) (string);

@doc:Description { value:"Sets a JMS transport integer property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The integer property name" }
@doc:Param { value:"value: The integer property value" }
public native function setIntProperty (jms:JMSMessage msg, string key, int value);

@doc:Description { value:"Gets a JMS transport integer property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The integer property name" }
@doc:Return { value:"int: The integer property value" }
public native function getIntProperty (jms:JMSMessage msg, string key) (int);

@doc:Description { value:"Sets a JMS transport boolean property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The boolean property name" }
@doc:Param { value:"value: The boolean property value" }
public native function setBooleanProperty (jms:JMSMessage msg, string key, boolean value);

@doc:Description { value:"Gets a JMS transport boolean property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The boolean property name" }
@doc:Return { value:"boolean: The boolean property value" }
public native function getBooleanProperty (jms:JMSMessage msg, string key) (boolean);

@doc:Description { value:"Sets a JMS transport float property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The float property name" }
@doc:Param { value:"value: The float property value" }
public native function setFloatProperty (jms:JMSMessage msg, string key, float value);

@doc:Description { value:"Gets a JMS transport float property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The float property name" }
@doc:Return { value:"float: The float property value" }
public native function getFloatProperty (jms:JMSMessage msg, string key) (float);

@doc:Description { value:"Sets text content for the JMS message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"content: Text Message Content" }
public native function setTextMessageContent (jms:JMSMessage msg, string content);

@doc:Description { value:"Gets text content of the JMS message"}
@doc:Param { value:"msg: A request message" }
@doc:Return { value:"string: Text Message Content" }
public native function getTextMessageContent (jms:JMSMessage msg) (string value);

@doc:Description { value:"Get JMS transport header MessageID from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Return { value:"string: The header value" }
public native function getMessageID(jms:JMSMessage m) (string);

@doc:Description { value:"Get JMS transport header Timestamp from the message"}
@doc:Param { value:"m: A request message" }
@doc:Return { value:"int: The header value" }
public native function getTimestamp(jms:JMSMessage m) (int);

@doc:Description { value:"Sets DeliveryMode JMS transport header to the message"}
@doc:Param { value:"m: A request message" }
@doc:Param { value:"i: The header value" }
public native function setDeliveryMode(jms:JMSMessage m, int i);

@doc:Description { value:"Get JMS transport header DeliveryMode from the message"}
@doc:Param { value:"m: A request message" }
@doc:Return { value:"int: The header value" }
public native function getDeliveryMode(jms:JMSMessage m) (int);

@doc:Description { value:"Sets Expiration JMS transport header to the message"}
@doc:Param { value:"m: A request message" }
@doc:Param { value:"i: The header value" }
public native function setExpiration(jms:JMSMessage m, int i);

@doc:Description { value:"Get JMS transport header Expiration from the message"}
@doc:Param { value:"m: A request message" }
@doc:Return { value:"int: The header value" }
public native function getExpiration(jms:JMSMessage m) (int);

@doc:Description { value:"Sets Priority JMS transport header to the message"}
@doc:Param { value:"m: A request message" }
@doc:Param { value:"i: The header value" }
public native function setPriority(jms:JMSMessage m, int i);

@doc:Description { value:"Get JMS transport header Priority from the message"}
@doc:Param { value:"m: A request message" }
@doc:Return { value:"int: The header value" }
public native function getPriority(jms:JMSMessage m) (int);

@doc:Description { value:"Get JMS transport header Redelivered from the message"}
@doc:Param { value:"m: A request message" }
@doc:Return { value:"boolean: The header value" }
public native function getRedelivered(jms:JMSMessage m) (boolean);

@doc:Description { value:"Sets CorrelationID JMS transport header to the message"}
@doc:Param { value:"m: A request message" }
@doc:Param { value:"s: The header value" }
public native function setCorrelationID(jms:JMSMessage m, string s);

@doc:Description { value:"Get JMS transport header CorrelationID from the message"}
@doc:Param { value:"m: A request message" }
@doc:Return { value:"string: The header value" }
public native function getCorrelationID(jms:JMSMessage m) (string);

@doc:Description { value:"Sets Type JMS transport header to the message"}
@doc:Param { value:"m: A request message" }
@doc:Param { value:"s: The header value" }
public native function setType(jms:JMSMessage m, string s);

@doc:Description { value:"Get JMS transport header Type from the message"}
@doc:Param { value:"m: A request message" }
@doc:Return { value:"string: The header value" }
public native function getType(jms:JMSMessage m) (string);

@doc:Description { value:"Clear JMS properties of the message"}
@doc:Param { value:"m: A request message" }
public native function clearProperties(jms:JMSMessage m);
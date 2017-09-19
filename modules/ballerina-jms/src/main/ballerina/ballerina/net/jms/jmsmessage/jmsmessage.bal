package ballerina.net.jms.jmsmessage;

import ballerina.doc;
import ballerina.net.jms;

@doc:Description { value:"Sets a JMS transport property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The property name" }
@doc:Param { value:"value: The property value" }
native function setProperty (jms:JMSMessage msg, string key, string value);

@doc:Description { value:"Gets a JMS transport property from the message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"key: The property name" }
@doc:Return { value:"string: The property value" }
native function getProperty (jms:JMSMessage msg, string key) (string);

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

@doc:Description { value:"Sets content for the JMS message"}
@doc:Param { value:"msg: A request message" }
@doc:Param { value:"content: Message Content" }
native function setContent (jms:JMSMessage msg, string content);

@doc:Description { value:"Gets content of the JMS message"}
@doc:Param { value:"msg: A request message" }
@doc:Return { value:"string: The Message Content" }
native function getContent (jms:JMSMessage msg) (string value);
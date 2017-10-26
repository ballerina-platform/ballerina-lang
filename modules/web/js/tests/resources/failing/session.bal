package ballerina.net.http;

import ballerina.doc;

public struct Session {
}

@doc:Description { value:"Gets the session struct for valid id, otherwise create new" }
@doc:Param { value:"req: The request message" }
@doc:Return { value:"Session: HTTP session struct" }
public native function createSessionIfAbsent (Request req) (Session);

@doc:Description { value:"Gets the session struct for valid id" }
@doc:Param { value:"req: The request message" }
@doc:Return { value:"Session: HTTP session struct" }
public native function getSession (Request req) (Session);

@doc:Description { value:"Gets the session attribute" }
@doc:Param { value:"session: A session struct" }
@doc:Param { value:"attributeKey: HTTPSession attribute key" }
@doc:Return { value:"any: HTTPSession attribute value" }
public native function getAttribute (Session session, string attributeKey) (any);

@doc:Description { value:"Sets session attributes to the message" }
@doc:Param { value:"session: A session struct" }
@doc:Param { value:"attributeKey: HTTPSession attribute key" }
@doc:Param { value:"attributeValue: HTTPSession attribute Value" }
public native function setAttribute (Session session, string attributeKey, any attributeValue);

@doc:Description { value:"Gets the session attribute names" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"string[]: HTTPSession attribute name array" }
public native function getAttributeNames (Session session) (string[]);

@doc:Description { value:"Gets the session attribute" }
@doc:Param { value:"session: A session struct" }
public native function invalidate (Session session);

@doc:Description { value:"Remove the session attribute" }
@doc:Param { value:"session: A session struct" }
@doc:Param { value:"attributeKey: HTTPSession attribute key" }
public native function removeAttribute (Session session, string attributeKey);

@doc:Description { value:"Gets the session id" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"string: HTTPSession id" }
public native function getId (Session session) (string);

@doc:Description { value:"Gets the session status" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"boolean: HTTPSession status" }
public native function isNew (Session session) (boolean);

@doc:Description { value:"Gets the session creation time" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"int: HTTPSession creation time" }
public native function getCreationTime (Session session) (int);

@doc:Description { value:"Gets the session last accessed time" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"int: HTTPSession last accessed time" }
public native function getLastAccessedTime (Session session) (int);

@doc:Description { value:"Gets the session max inactive interval" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"int: HTTPSession max inactive interval" }
public native function getMaxInactiveInterval (Session session) (int);

@doc:Description { value:"Sets session max inactive interval" }
@doc:Param { value:"session: A session struct" }
@doc:Param { value:"timeInterval: HTTPSession max inactive interval" }
public native function setMaxInactiveInterval (Session session, int timeInterval);




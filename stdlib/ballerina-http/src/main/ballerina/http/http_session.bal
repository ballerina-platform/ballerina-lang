package ballerina.http;

@Description { value:"Represents an HTTP Session"}
public type Session object {

@Description { value:"Gets the named session attribute" }
@Param { value:"attributeKey: HTTP session attribute key" }
@Return { value:"HTTP session attribute value" }
public native function getAttribute(string attributeKey) returns (any);

@Description { value:"Sets the specified key/value pair as a session attribute" }
@Param { value:"attributeKey: Session attribute key" }
@Param { value:"attributeValue: Session attribute Value" }
public native function setAttribute(string attributeKey, any attributeValue);

@Description { value:"Gets the session attribute names" }
@Return { value:"Session attribute names array" }
public native function getAttributeNames() returns (string[]);

@Description { value:"Gets the session attribute key value pairs as a map" }
@Return { value:"The map of session attributes key value pairs" }
public native function getAttributes() returns (map);

@Description { value:"Invalidates the session and it will no longer be accessible from the request" }
public native function invalidate();

@Description { value:"Remove the named session attribute" }
@Param { value:"attributeKey: Session attribute key" }
public native function removeAttribute(string attributeKey);

@Description { value:"Gets the session cookie ID" }
@Return { value:"Session ID" }
public native function getId() returns (string);

@Description { value:"Checks whether the given session is a newly created session or an existing session" }
@Return { value:"Indicates if the session is a newly created session or not" }
public native function isNew() returns (boolean);

@Description { value:"Gets the session creation time" }
@Return { value:"Session creation time" }
public native function getCreationTime() returns (int);

@Description { value:"Gets the last time the sessions was accessed" }
@Return { value:"Last accessed time of the session" }
public native function getLastAccessedTime() returns (int);

@Description { value:"Gets maximum inactive interval for the session. The session expires after this time period." }
@Return { value:"Session max inactive interval" }
public native function getMaxInactiveInterval() returns (int);

@Description { value:"Sets the maximum inactive interval for the session. The session expires after this time period." }
@Param { value:"timeInterval: Session max inactive interval" }
public native function setMaxInactiveInterval(int timeInterval);
};
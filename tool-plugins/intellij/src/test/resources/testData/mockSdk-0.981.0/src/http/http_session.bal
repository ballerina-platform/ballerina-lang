
//TODO Remove this class once service session LC is introduced
//@Description { value:"Represents an HTTP Session"}
//type Session object {
//
//@Description {value:"Gets the Session struct for a valid session cookie from the connection. Otherwise creates a new Session struct."}
//@Return {value:"HTTP Session record type"}
//extern function createSessionIfAbsent() returns (Session);
//
//@Description {value:"Gets the Session struct from the connection if it is present"}
//@Return {value:"The HTTP Session record type assoicated with the request"}
//extern function getSession() returns (Session);
//
//@Description { value:"Gets the named session attribute" }
//@Param { value:"attributeKey: HTTP session attribute key" }
//@Return { value:"HTTP session attribute value" }
//extern function getAttribute(string attributeKey) returns (any);
//
//@Description { value:"Sets the specified key/value pair as a session attribute" }
//@Param { value:"attributeKey: Session attribute key" }
//@Param { value:"attributeValue: Session attribute Value" }
//extern function setAttribute(string attributeKey, any attributeValue);
//
//@Description { value:"Gets the session attribute names" }
//@Return { value:"Session attribute names array" }
//extern function getAttributeNames() returns (string[]);
//
//@Description { value:"Gets the session attribute key value pairs as a map" }
//@Return { value:"The map of session attributes key value pairs" }
//extern function getAttributes() returns (map);
//
//@Description { value:"Invalidates the session and it will no longer be accessible from the request" }
//extern function invalidate();
//
//@Description { value:"Remove the named session attribute" }
//@Param { value:"attributeKey: Session attribute key" }
//extern function removeAttribute(string attributeKey);
//
//@Description { value:"Gets the session cookie ID" }
//@Return { value:"Session ID" }
//extern function getId() returns (string);
//
//@Description { value:"Checks whether the given session is a newly created session or an existing session" }
//@Return { value:"Indicates if the session is a newly created session or not" }
//extern function isNew() returns (boolean);
//
//@Description { value:"Gets the session creation time" }
//@Return { value:"Session creation time" }
//extern function getCreationTime() returns (int);
//
//@Description { value:"Gets the last time the sessions was accessed" }
//@Return { value:"Last accessed time of the session" }
//extern function getLastAccessedTime() returns (int);
//
//@Description { value:"Gets maximum inactive interval for the session. The session expires after this time period." }
//@Return { value:"Session max inactive interval" }
//extern function getMaxInactiveInterval() returns (int);
//
//@Description { value:"Sets the maximum inactive interval for the session. The session expires after this time period." }
//@Param { value:"timeInterval: Session max inactive interval" }
//extern function setMaxInactiveInterval(int timeInterval);
//};

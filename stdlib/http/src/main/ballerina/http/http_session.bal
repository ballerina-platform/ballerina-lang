
//TODO Remove this class once service session LC is introduced
//@Description { value:"Represents an HTTP Session"}
//type Session object {
//
//@Description {value:"Gets the Session struct for a valid session cookie from the connection. Otherwise creates a new Session struct."}
//@Return {value:"HTTP Session record type"}
//native function createSessionIfAbsent() returns (Session);
//
//@Description {value:"Gets the Session struct from the connection if it is present"}
//@Return {value:"The HTTP Session record type assoicated with the request"}
//native function getSession() returns (Session);
//
//@Description { value:"Gets the named session attribute" }
//@Param { value:"attributeKey: HTTP session attribute key" }
//@Return { value:"HTTP session attribute value" }
//native function getAttribute(string attributeKey) returns (any);
//
//@Description { value:"Sets the specified key/value pair as a session attribute" }
//@Param { value:"attributeKey: Session attribute key" }
//@Param { value:"attributeValue: Session attribute Value" }
//native function setAttribute(string attributeKey, any attributeValue);
//
//@Description { value:"Gets the session attribute names" }
//@Return { value:"Session attribute names array" }
//native function getAttributeNames() returns (string[]);
//
//@Description { value:"Gets the session attribute key value pairs as a map" }
//@Return { value:"The map of session attributes key value pairs" }
//native function getAttributes() returns (map);
//
//@Description { value:"Invalidates the session and it will no longer be accessible from the request" }
//native function invalidate();
//
//@Description { value:"Remove the named session attribute" }
//@Param { value:"attributeKey: Session attribute key" }
//native function removeAttribute(string attributeKey);
//
//@Description { value:"Gets the session cookie ID" }
//@Return { value:"Session ID" }
//native function getId() returns (string);
//
//@Description { value:"Checks whether the given session is a newly created session or an existing session" }
//@Return { value:"Indicates if the session is a newly created session or not" }
//native function isNew() returns (boolean);
//
//@Description { value:"Gets the session creation time" }
//@Return { value:"Session creation time" }
//native function getCreationTime() returns (int);
//
//@Description { value:"Gets the last time the sessions was accessed" }
//@Return { value:"Last accessed time of the session" }
//native function getLastAccessedTime() returns (int);
//
//@Description { value:"Gets maximum inactive interval for the session. The session expires after this time period." }
//@Return { value:"Session max inactive interval" }
//native function getMaxInactiveInterval() returns (int);
//
//@Description { value:"Sets the maximum inactive interval for the session. The session expires after this time period." }
//@Param { value:"timeInterval: Session max inactive interval" }
//native function setMaxInactiveInterval(int timeInterval);
//};

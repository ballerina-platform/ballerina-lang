package ballerina.net.ws;

import ballerina.doc;

@doc:Description { value:"This pushes text from server to the the same client who sent the message."}
@doc:Param { value:"text: Text which should be sent" }
public native function pushText (string text);

@doc:Description { value:"This pushes text from server to all the connected clients of the service."}
@doc:Param { value:"text: Text which should be sent" }
public native function broadcastText (string text);

@doc:Description { value:"Close the current client connection"}
public native function closeConnection ();

@doc:Description { value:"Store the connection globally for the use of other services."}
@doc:Param { value:"connectionName: Name of the connection" }
public native function storeConnection (string connectionName);

@doc:Description { value:"Removes connection from a connection store."}
@doc:Param { value:"connectionName: Name of the connection" }
public native function removeStoredConnection (string connectionName);

@doc:Description { value:"Push text to the connection chose by the user from the connection store."}
@doc:Param { value:"connectionName: Name of the connection" }
@doc:Param { value:"text: Text which should be sent" }
public native function pushTextToConnection (string connectionName, string text);

@doc:Description { value:"Close stored connection."}
@doc:Param { value:"connectionName: Name of the connection" }
public native function closeStoredConnection (string connectionName);

@doc:Description { value:"This pushes text from server to all the connected clients of the service."}
@doc:Param { value:"connectionGroupName: Name of the connection group" }
public native function addConnectionToGroup (string connectionGroupName);

@doc:Description { value:"Removes connection from a connection group."}
@doc:Param { value:"connectionGroupName: Name of the connection group" }
public native function removeConnectionFromGroup (string connectionGroupName);

@doc:Description { value:"Removes connection group."}
@doc:Param { value:"connectionGroupName: Name of the connection group" }
public native function removeConnectionGroup (string connectionGroupName);

@doc:Description { value:"Push text from server to all the connected clients of the service."}
@doc:Param { value:"connectionGroupName: Name of the connection group" }
@doc:Param { value:"text: Text which should be sent" }
public native function pushTextToGroup (string connectionGroupName, string text);

@doc:Description { value:"Close all the connections in connection group."}
@doc:Param { value:"connectionGroupName: Name of the connection group" }
public native function closeConnectionGroup(string connectionGroupName);

public connector ClientConnector (string url, string callbackService) {

    @doc:Description { value:"Push text to the server"}
    @doc:Param { value:"text: text which should be sent"}
    native action pushText(string text);

}




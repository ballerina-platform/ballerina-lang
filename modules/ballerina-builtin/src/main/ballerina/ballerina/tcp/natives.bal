package ballerina.tcp;

import ballerina.doc;
import ballerina.io;


@doc:Description {value:"Represents TCP socket"}
@doc:Field {value:"address: Destination address"}
@doc:Field {value:"port: Destination port"}
public struct Socket{
string address;
int port;
}

@doc:Description {value:"Retrieves the channel of the TCP"}
@doc:Param {value:"socket: TCP socket"}
@doc:Param {value:"permission: whether the socket will be opened for reading or writing"}
public native function openChannel (Socket socket, string permission) (io:ByteChannel);
package ballerina.tcp;

import ballerina.doc;
import ballerina.io;


@doc:Description {value:"Retrieves the channel of the TCP"}
@doc:Param {value:"address: Destination address"}
@doc:Param {value:"port: Destination port"}
native function openChannel (string address, int port) (io:ByteChannel);
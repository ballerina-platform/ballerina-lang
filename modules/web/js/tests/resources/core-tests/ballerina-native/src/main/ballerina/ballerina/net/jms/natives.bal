package ballerina.net.jms;

import ballerina.doc;

@doc:Description { value:"Message acknowledgement action implementation for jms connector when using jms client acknowledgement mode"}
@doc:Param { value:"message: message" }
@doc:Param { value:"deliveryStatus: Specify whether message delivery is SUCCESS or ERROR" }
native function acknowledge (message m, string deliveryStatus);

@doc:Description { value:"Session rollback action implementation for jms connector when using jms session transaction mode"}
@doc:Param { value:"message: message" }
native function rollback (message m);

@doc:Description { value:"Session commit action implementation for jms connector when using jms session transaction mode"}
@doc:Param { value:"message: message" }
native function commit (message m);

connector ClientConnector (map properties) {

	@doc:Description { value:"SEND action implementation of the JMS Connector"}
	@doc:Param { value:"connector: Connector" }
	@doc:Param { value:"destinationName: Destination Name" }
	@doc:Param { value:"msgType: Message Type" }
	@doc:Param { value:"message: Message" }
	native action send (ClientConnector jmsClientConnector, string destinationName, string msgType, message m) (boolean);

}
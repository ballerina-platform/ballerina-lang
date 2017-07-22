# To run the program, put the code in `websocket-connection-groups.bal`
# and use `ballerina run service websocket-connection-groups.bal`.
$ ballerina run service websocket-connection-groups.bal
# Now that we can run this program using any WebSocket client with "ws://host:port/groups/ws"
# Make sure to use multiple clients for this example (at least 2 clients)

# Sample json string: {"command":"send", "group":"even", "msg":"hi even"}

# To check the sample, you can use Chrome or Firefox javascript console and run the below commands <br>
$ var ws = new WebSocket("ws://localhost:9090/groups/ws");
$ ws.onmessage = function(frame) {console.log(frame.data)};
$ ws.onclose = function(frame) {console.log(frame.data)};

# To send messages to group even
$ ws.send('{"command":"send", "group":"even", "msg":"hi even"}');

# To remove this connection from odd group if exists
$ ws.send('{"command":"remove", "group":"odd", "msg":"rm connection"}');

# To remove even connection group
$ ws.send('{"command":"removeGroup", "group":"even", "msg":"rm even group"}');

# To close odd connection group
$ ws.send('{"command":"closeGroup", "group":"odd", "msg":"Close odd group"}');

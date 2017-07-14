# To run the program, put the code in `websocket-connection-groups.bal`
# and use `ballerina run service websocket-connection-groups.bal`.
$ ballerina run service websocket-connection-groups.bal
# Now that we can run this program using any WebSocket client with "ws://host:port/groups/ws"
# Make sure to use multiple clients for this example (at least 2 clients)

# Sample json string: {"command":"send", "group":"even", "msg":"hi even"}

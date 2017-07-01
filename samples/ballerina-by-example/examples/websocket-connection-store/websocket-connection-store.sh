# To run the program, put the code in `websocket-connection-store.bal`
# and use `$BALLERINA_HOME/bin/ballerina run service websocket-connection-store.bal`.

$ $BALLERINA_HOME/bin/ballerina run service websocket-connection-store.bal

# Now that we can run this program using any WebSocket client with "ws://host:port/store/ws"
# Make sure to use multiple clients for this example (at least 2 clients)

# sample json string: {"command":"send", "id":"0", "msg":"hi 0"}
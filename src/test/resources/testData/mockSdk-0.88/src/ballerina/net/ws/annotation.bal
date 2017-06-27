package ballerina.net.ws;

annotation WebSocketUpgradePath attach service {
    string value;
}

annotation OnOpen attach resource {
}

annotation OnTextMessage attach resource {
}

annotation OnClose attach resource {
}
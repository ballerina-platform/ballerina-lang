package ballerina.net.ws;

public annotation WebSocketUpgradePath attach service {
    string value;
}

public annotation ClientService attach service {
}

public annotation OnOpen attach resource {
}

public annotation OnTextMessage attach resource {
}

public annotation OnClose attach resource {
}

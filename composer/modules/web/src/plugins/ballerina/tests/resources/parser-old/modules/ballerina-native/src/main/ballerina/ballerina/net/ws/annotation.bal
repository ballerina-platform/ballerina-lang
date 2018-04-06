package ballerina.http;

annotation WebSocketUpgradePath attach service {
    string value;
}

annotation ClientService attach service {
}

annotation OnOpen attach resource {
}

annotation OnTextMessage attach resource {
}

annotation OnClose attach resource {
}

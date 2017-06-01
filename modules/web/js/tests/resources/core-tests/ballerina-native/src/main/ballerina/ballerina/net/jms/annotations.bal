package ballerina.net.jms;

annotation JMSSource attach service {
    string destination;
    string factoryInitial;
    string providerUrl;
    string connectionFactoryType;
}

annotation ConnectionProperty attach service {
    string key;
    string value;
}
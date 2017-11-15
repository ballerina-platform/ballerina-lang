package ballerina.net.jms;

public annotation configuration attach service<> {
    string initialContextFactory;
    string providerUrl;
    string connectionFactoryType;
    string connectionFactoryName;
    string destination;
    string acknowledgementMode;
    string subscriptionId;
    string clientId;
    string configFilePath;
    string connectionFactoryNature;
    int concurrentConsumers;
    string connectionUsername;
    string connectionPassword;
    string[] properties;
}

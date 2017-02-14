@Source (
protocol = "jms", destination = "queue", connectionFactoryJNDIName = "QueueConnectionFactory",
factoryInitial = "org.apache.activemq.jndi.ActiveMQInitialContextFactory", providerUrl = "vm://localhost?broker.persistent=false",
connectionFactoryType = "queue")
service jmsService {
    @OnMessage
    resource onTextMessage (message m) {
        message mes;
        mes = m;
    }
}

@Source (
protocol = "jms", destination = "queue", connectionFactoryJNDIName = "QueueConnectionFactory",
factoryInitial = "org.apache.activemq.jndi.ActiveMQInitialContextFactory", providerUrl = "vm://localhost?broker.persistent=false",
connectionFactoryType = "queue")
service jmsServiceWithoutResource {
}


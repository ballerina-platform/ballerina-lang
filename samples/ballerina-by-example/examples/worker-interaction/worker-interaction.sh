$ ballerina run main worker-interaction.bal
[default worker]
    Sending data to W1:
    Value of integer variable is [100]
    Value of float variable is [2.34]
[W1 worker]
    Data received from default worker:
    Value of integer variable is [100]
    Value of float variable is [2.34]
[W1 worker]
    Sending data to default worker:
    Value of json variable is {"name":"WSO2"}
[default worker]
    Data received from W1 worker:
    Value of json variable is {"name":"WSO2"}

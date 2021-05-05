public type ClientConfiguration record {|
    ClientAuth auth?;
|};

public type ClientAuth ClientCredentialsGrantConfig;

public type ClientCredentialsGrantConfig record {|
    ClientConfiguration clientConfig = {};
|};

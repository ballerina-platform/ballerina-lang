class A {
    *B;
    int i = 0;
}

class B {
    *A;
    *C;
    int i = 0;
}

type C object {
    *A;
};

type ServiceObject service object {
    resource function get name() returns string;
};

service class ServiceClass {
    *ServiceObject;
}

type ClientObject client object {
    resource function get name() returns string;
};

client class ClientClass {
    *ClientObject;
}

type ClientObject2 client object {
    *ClientObject;
};

client class ClientClass2 {
    *ClientObject2;
}

client class ClientClass3 {
    *ClientClass2;
}

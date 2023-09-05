type Ns1\:element1 record {
    @xmldata:Attribute
    string ns1\:attribute1;
    @xmldata:Attribute
    string ns2\:attribute2;
};

@xmldata:Namespace {
    prefix: "xsi",
    uri: "http://www.w3.org/2001/XMLSchema-instance"
}
type Ns2\:null record {
    @xmldata:Attribute
    string xsi\:nil;
};

type Ns2\:array record {
    string[] ns2\:item;
};

type Ns2\:element2 record {
    string ns2\:subelement;
    decimal ns2\:number;
    boolean ns2\:boolean;
    Ns2\:null ns2\:null;
    Ns2\:array ns2\:array;
};

@xmldata:Name {
    value: "root"
}
@xmldata:Namespace {
    prefix: "ns1",
    uri: "http://namespace1.com"
}
@xmldata:Namespace {
    prefix: "ns2",
    uri: "http://namespace2.com"
}
type Root record {
    Ns1\:element1 ns1\:element1;
    Ns2\:element2 ns2\:element2;
};

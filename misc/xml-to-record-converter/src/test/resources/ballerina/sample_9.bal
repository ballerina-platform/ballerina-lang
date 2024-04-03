type Ns1_Element1 record {
    @xmldata:Attribute
    string attribute1;
    @xmldata:Attribute
    string attribute2;
};

type Ns2_Null record {
    @xmldata:Attribute
    string nil;
};

type Ns2_Array record {
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "http://namespace2.com"
    }
    string[] item;
};

type Ns2_Element2 record {
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "http://namespace2.com"
    }
    string subelement;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "http://namespace2.com"
    }
    decimal number;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "http://namespace2.com"
    }
    boolean 'boolean;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "http://namespace2.com"
    }
    Ns2_Null 'null;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "http://namespace2.com"
    }
    Ns2_Array array;
};

@xmldata:Name {
    value: "root"
}
type Root record {
    @xmldata:Namespace {
        prefix: "ns1",
        uri: "http://namespace1.com"
    }
    Ns1_Element1 element1;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "http://namespace2.com"
    }
    Ns2_Element2 element2;
};

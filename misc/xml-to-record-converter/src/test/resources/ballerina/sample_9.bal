type Ns1_Element1 record {
    @xmldata:Attribute
    string ns1\:attribute1;
    @xmldata:Attribute
    string ns2\:attribute2;
};

type Ns2_Null record {
    @xmldata:Attribute
    string xmlns\:xsi = "http://www.w3.org/2001/XMLSchema-instance";
    @xmldata:Attribute
    string xsi\:nil;
};

type Ns2_Array record {
    string[] ns2\:item;
};

type Ns2_Element2 record {
    string ns2\:subelement;
    decimal ns2\:number;
    boolean ns2\:boolean;
    Ns2_Null ns2\:null;
    Ns2_Array ns2\:array;
};

@xmldata:Name {
    value: "root"
}
type Root record {
    Ns1_Element1 ns1\:element1;
    Ns2_Element2 ns2\:element2;
    @xmldata:Attribute
    string xmlns\:ns1 = "http://namespace1.com";
    @xmldata:Attribute
    string xmlns\:ns2 = "http://namespace2.com";
};

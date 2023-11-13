type Main_MainElement record {
    string[] child\:childElement;
};

@xmldata:Name {
    value: "root"
}
@xmldata:Namespace {
prefix: "root",
    uri: "http://example.com/root"
}
type Root_Root record {
    Main_MainElement main\:mainElement;
    @xmldata:Attribute
    string xmlns\:child = "http://example.com/child";
    @xmldata:Attribute
    string xmlns\:main = "http://example.com/main";
};

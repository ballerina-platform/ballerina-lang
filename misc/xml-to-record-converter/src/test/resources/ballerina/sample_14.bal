type Main_MainElement record {
    @xmldata:Namespace {
        prefix: "child",
        uri:"http://example.com/child"
    }
    string[] childElement;
};

@xmldata:Name {
    value: "root"
}
@xmldata:Namespace {
    prefix: "root",
    uri: "http://example.com/root"
}
type Root_Root record {
    @xmldata:Namespace {
        prefix: "main",
        uri: "http://example.com/main"
    }
    Main_MainElement mainElement;
};

type MainMainElement record {
    string[] child\:childElement;
};

@xmldata:Name {
    value: "root"
}
@xmldata:Namespace {
prefix: "root",
    uri: "http://example.com/root"
}
type RootRoot record {
    MainMainElement main\:mainElement;
    @xmldata:Attribute
    string xmlns\:child = "http://example.com/child";
    @xmldata:Attribute
    string xmlns\:main = "http://example.com/main";
};

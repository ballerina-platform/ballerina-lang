type Book record {
    @xmldata:Namespace {
        uri: "http://example.com/library"
    }
    (int|string) title;
    @xmldata:Namespace {
        uri: "http://example.com/library"
    }
    string author;
    @xmldata:Namespace {
        uri: "http://example.com/library"
    }
    string genre;
};

type Magazine record {
    @xmldata:Namespace {
        uri: "http://example.com/library"
    }
    string title;
    @xmldata:Namespace {
        uri: "http://example.com/library"
    }
    string issue;
};

@xmldata:Name {
    value: "library"
}
@xmldata:Namespace {
    uri: "http://example.com/library"
}
type Library record {
    @xmldata:Namespace {
        uri: "http://example.com/library"
    }
    Book[] book;
    @xmldata:Namespace {
        uri: "http://example.com/library"
    }
    Magazine magazine;
};

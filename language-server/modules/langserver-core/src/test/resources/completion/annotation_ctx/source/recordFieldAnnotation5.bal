type Info record {|
    int id;
    string name?;
    anydata description;
|};

type AnnotationConfig record {|
    Info info?;
    int|record {*Info; string description;} doc
|};

const annotation AnnotationConfig CustomAnnotation on source record field;

type RecordName record {|
    record {|
        int id;
        @CustomAnnotation {
            doc: {}
        }
        string value;
        anydata description;
    |} nestedRecord;
|};

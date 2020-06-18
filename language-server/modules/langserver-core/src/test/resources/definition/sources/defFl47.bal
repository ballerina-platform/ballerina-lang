type SampleErrorData record {
    string message?;
    error cause;
};

type Foo record {|
    string message?;
    error cause;
|};

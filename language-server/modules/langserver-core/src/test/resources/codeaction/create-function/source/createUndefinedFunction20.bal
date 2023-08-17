type MyType record {|
    string value;
|};

function func() {
    stream<MyType> streamName = stream from string letter in "JohnSnow"
            select foo(letter);
}
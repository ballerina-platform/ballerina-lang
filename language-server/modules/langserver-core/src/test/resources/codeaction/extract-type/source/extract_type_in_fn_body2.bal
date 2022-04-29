type Record record {|
    int id;
    string text;
|};

# Perform some processing
function process() {
    record {int id; string text;} message = {id: 1, text: "Hello!"};
}

type Input record {
    string fieldIn?;
};

type Output record {
    string fieldOut;
};

function transform(Input input) returns Output => {
    fieldOut: input.fieldIn
}

type T int?;

type Rec record {
    T id?;
};

function func() {
    Rec rec = {};
    rec?.id;
}

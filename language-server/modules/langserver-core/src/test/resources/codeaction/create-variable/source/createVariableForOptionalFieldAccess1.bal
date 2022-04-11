type Rec record {
    int? id?;
};

function func() {
    Rec rec = {};
    rec?.id;
}

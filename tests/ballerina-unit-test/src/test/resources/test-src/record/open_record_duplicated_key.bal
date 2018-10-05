type Family record {
    string spouse;
    int noOfChildren;
    string[] children;
};

function testDuplicatedKey() {
    Family family = {spouse: "Shereen", noOfChildren: 1, noOfChildren: 0, children: []};
}

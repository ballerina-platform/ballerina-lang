import ballerina/module1;

type Address record {
    string city;
    string? state;
};

type Person record {
    string name;
    Address addr;
};

function main(*) {
    
}

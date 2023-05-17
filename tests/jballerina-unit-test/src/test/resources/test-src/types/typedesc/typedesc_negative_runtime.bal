const AAA = "AAA";

type RecordType record {|
    string name;
|};

type JsonTypeDesc typedesc<json>;
type MapTypeDesc typedesc<map<string>>;
type TableTypeDesc typedesc<table<map<string>>>;
type MapJsonTypeDesc typedesc<map<JsonTypeDesc>>;
type StringTypeDesc typedesc<string>;
type IntTypeDesc typedesc<int>;
type BooleanTypeDesc typedesc<boolean>;
type FloatTypeDesc typedesc<float>;
type DecimalTypeDesc typedesc<decimal>;
type XMlTypeDesc typedesc<xml>;
type BytesTypeDesc typedesc<byte>;
type AnyTypeDesc typedesc<any>;
type NullTypeDesc typedesc<null>;
type AnydataTypeDesc typedesc<anydata>;
type ObjectTypeDesc typedesc<object{}>;
type ArrayTypeDesc typedesc<anydata[]>;
type UnionTypeDesc typedesc<json|map<string>>;
type IntersectionTypeDesc typedesc<int & readonly>;
type TupleTypeDesc typedesc<[int, string, decimal]>;
type SingletonTypeDesc typedesc<AAA>;

function jsonTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(JsonTypeDesc);
}

function mapTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(MapTypeDesc);
}

function tableTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(TableTypeDesc);
}

function mapJsonTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(MapJsonTypeDesc);
}

function stringTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(StringTypeDesc);
}

function intTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(IntTypeDesc);
}

function booleanTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(BooleanTypeDesc);
}

function floatTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(FloatTypeDesc);
}

function decimalTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(DecimalTypeDesc);
}

function xmlTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(XMlTypeDesc);
}

function bytesTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(BytesTypeDesc);
}

function anyTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(AnyTypeDesc);
}

function nullTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(NullTypeDesc);
}

function anydataTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(AnydataTypeDesc);
}

function objectTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(ObjectTypeDesc);
}

function arrayTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(ArrayTypeDesc);
}

function unionTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(UnionTypeDesc);
}

function intersectionTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(IntersectionTypeDesc);
}

function tupleTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(TupleTypeDesc);
}

function singletonTypeDesc() returns error? {
    RecordType value = {name: "test"};
    var _ = check value.ensureType(SingletonTypeDesc);
}

function jsonTypeDescCloneWithType() returns error? {
     RecordType value = {name: "test"};
     var _ = check value.cloneWithType(JsonTypeDesc);
}

function jsonTypeDescFromJsonWithType() returns error? {
     RecordType value = {name: "test"};
     var _ = check value.fromJsonWithType(JsonTypeDesc);
}

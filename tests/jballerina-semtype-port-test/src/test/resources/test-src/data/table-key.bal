type R record {|
   readonly [string, string] name;
   readonly string department;
   readonly string city;
   int salary;
|};

type KS1 table<R> key(name);

type KS2 table<R> key(city);

type KS3 table<R> key(department);

type KS4 table<R> key(city, department);

type KS5 table<R> key(department, city);

type KS6 table<R> key(name, department, city);

// KS2<:KC1
// KS3<:KC1
type KC1 table<R> key<string>;

// KS1<:KC2
// KS4<:KC2
// KS5<:KC2
type KC2 table<R> key<[string, string]>;

// KS6<:KC3
type KC3 table<R> key<[[string, string], string, string]>;

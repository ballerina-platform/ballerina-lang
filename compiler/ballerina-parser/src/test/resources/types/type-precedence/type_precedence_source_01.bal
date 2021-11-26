type custom1 Foo & Bar|Baz & Qux;

type custom2 int & Bar|string & ()|nill;

type custom3 Foo & readonly & int|Baz & Qux & boolean|float;

Foo & readonly|int|string & readonly custom4;

var output = from Foo & readonly|int & readonly person in personList
    join Foo|() & json dept in deptList on person.id equals dept.id
    select rec;

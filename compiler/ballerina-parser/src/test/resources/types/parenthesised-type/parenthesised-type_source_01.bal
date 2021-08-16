type Bar (T);

type Bar (((T)));

type Bar (+5);

type Bar ("ballerina");

type Bar (int);

type Bar (int?);

type Bar (int|float & readonly);

type Bar (int[][]);

type Bar (error<NO_MATCHING_OBJECT>);

type Bar (function ()?);

type Bar ([int, string]);

type Bar (int[1]|[int, int...]);

type Bar (());

type Bar (future<T?>);

type Bar (typedesc<object {}>);

type Bar (stream<string|float>);

type Bar (table<Myrecord> key<MyId>);

function foo() {
    (T) a;
    (((T))) a;
    (+5) a;
    ("ballerina") a;
    (int) a;
    (int?) a;
    (int|float & readonly) a;
    (int[][]) a;
    (error<NO_MATCHING_OBJECT>) a;
    (function ()?) a;
    ([int, string]) a;
    (int[1]|[int, int...]) a;
    (()) a;
    (future<T?>) a;
    (typedesc<object {}>) a;
    (stream<string|float>) a;
    (table<Myrecord> key<MyId>) a;
}

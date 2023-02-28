type SpanType object { 
    function myFunc() returns int; 
    function myFunc2() returns string;
};

type SpanAndReadOnlyTypeRef1 SpanType & readonly;

type SpanAndReadOnlyTypeRef2 SpanAndReadOnlyTypeRef1;

type SpanAndReadOnlyTypeRef3 SpanAndReadOnlyTypeRef2;

type SpanAndReadOnlyTypeRef4 SpanAndReadOnlyTypeRef3;

type SpanAndReadOnlyTypeRef5 SpanAndReadOnlyTypeRef4;

type SpanAndReadOnlyTypeRef6 SpanAndReadOnlyTypeRef5;

type SpanAndReadOnlyTypeRef7 SpanAndReadOnlyTypeRef6;

function testIntersection() {
    SpanAndReadOnlyTypeRef7 spanAndReadOnlyTypeRef4 = object {
        function myFunc() returns int {
            return 0;
        }

        function myFunc2() returns string {
            return "dsa";
        }
    };

    spanAndReadOnlyTypeRef4.
}

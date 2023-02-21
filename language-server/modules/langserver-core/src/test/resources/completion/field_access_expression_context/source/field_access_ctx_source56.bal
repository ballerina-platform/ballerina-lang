import ballerina/lang.regexp;

public function testSpanFieldAccess() {
    regexp:RegExp reg = re `World`;

    regexp:Span? matchAt = reg.matchAt("World");

    if matchAt is regexp:Span {
        matchAt.
    }
}

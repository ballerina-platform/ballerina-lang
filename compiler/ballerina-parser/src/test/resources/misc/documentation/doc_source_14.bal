# Test an identifier in backticks, when preceded by one of the special keywords
# Test type `typeDef`
# Test service `helloWorld`
# Test variable `testVar`
# Test var `testVar`
# Test annotation `annot`
# Test module `testModule`
# Test function `add`
# Test parameter `x`
# Test const `constant`
function foo() {
}

# if identifier is a qualified name M:X,
# then the source file must have imported M, and X must refer to a public name of an appropriate type in M
#
# Test type `m:typeDef`
# Test service `m:helloWorld`
# Test variable `m:testVar`
# Test var `m:testVar`
# Test annotation `m:annot`
# Test module `m:testModule`
# Test function `m:add`
# Test parameter `m:x`
# Test const `m:constant`
function bar() {
}

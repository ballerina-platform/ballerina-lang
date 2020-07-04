# test erroneous parameter documentation
#
# + no dash test
# + 123wrongIdentifier - test
#
function foo() { }

# test erroneous reference documentatoin
#
# test bakctick content `with no ending backtick
# This line will captured as a correct documentation line
#
# test parameter reference parameter `with no ending backtick
# This line will captured as a correct documentation line
#
function foo() { }

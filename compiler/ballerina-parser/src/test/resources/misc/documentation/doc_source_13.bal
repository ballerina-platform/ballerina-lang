# test erroneous parameter documentation
#
# + no dash test
# + 123wrongIdentifier - test
# `dummy` + test plus token after a backtick reference
# + x - + test plus token after a dash token
# + y + test plus token after a parameter name
#
function foo() { }

# test erroneous reference documentation
#
# test backtick content `with no ending backtick
# This line will be captured as a correct documentation line
#
# test parameter reference parameter `with no ending backtick
# This line will be captured as a correct documentation line
#
function foo() { }

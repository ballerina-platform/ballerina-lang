# BFM also recognizes `` `f()` `` as an alternative to `` function `f` ``
# In both cases, f can have any of the following forms: x(), m:x(), T.y(), m:T.y()
# 
# i.e. backtick content can have all together 6 formats such as x, m:x, x(), m:x(), T.y(), m:T.y()
# when preceded by function keyword
#
# function `foo`
# function `foo()`
# function `bar.baz()`
#
# function `m:foo`
# function `m:foo()`
# function `m:bar.baz()`
function foo() { }

class bar {
    function baz() { }
}

// Test resource path with missing slash tokens
service foo /bar baz/yay hey on listner {

}

// Test resource path with missing identifiers
service foo  / / / on listner {

}

// Test invalid tokens before and after resource path
service foo  from /bar in on listner {

}

# Adds parameter `a\-b` and parameter `\+b`
# + a\-b - one thing to be added
# + \+b - another thing to be added
# + return - the sum of them
function add(int a\-b, int \+b) returns int {
    return a\-b + \+b;
}

# Adds parameter `a\%b\#c\-\>d` and parameter `\+a\%b\#c\-\>d`
# + a\%b\#c\-\>d - one thing to be added
# + \+a\%b\#c\-\>d - another thing to be added
# + return - the sum of them
function add1(int a\%b\#c\-\>d, int \+a\%b\#c\-\>d) returns int {
    return a\%b\#c\-\>d + \+a\%b\#c\-\>d;
}

# Adds parameter `'foo_ƮέŞŢ` and parameter `'üňĩćőđę_param`
# + 'foo_ƮέŞŢ - one thing to be added
# + 'üňĩćőđę_param - another thing to be added
# + return - the sum of them
function add2(int 'foo_ƮέŞŢ, int 'üňĩćőđę_param) returns int {
    return 'foo_ƮέŞŢ + 'üňĩćőđę_param;
}

# Adds parameter `'foo_\u{2345}\u{0376}` and parameter `'\u{1324}\u{0387}_var`
# + 'foo_\u{2345}\u{0376} - one thing to be added
# + '\u{1324}\u{0387}_var - another thing to be added
# + return - the sum of them
function add3(int 'foo_\u{2345}\u{0376}, int '\u{1324}\u{0387}_var) returns int {
    return 'foo_\u{2345}\u{0376} + '\u{1324}\u{0387}_var;
}

# Adds parameter `'foo\ \/\:\@\[\`\{\~` and parameter `'var_\ \/\:\@\[\`\{\~`
# + 'foo\ \/\:\@\[\`\{\~ - one thing to be added
# + 'var_\ \/\:\@\[\`\{\~ - another thing to be added
# + return - the sum of them
function add4(int 'foo\ \/\:\@\[\`\{\~, int 'var_\ \/\:\@\[\`\{\~) returns int {
    return 'foo\ \/\:\@\[\`\{\~ + 'var_\ \/\:\@\[\`\{\~;
}

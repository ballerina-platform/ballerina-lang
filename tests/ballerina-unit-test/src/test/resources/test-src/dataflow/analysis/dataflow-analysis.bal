function testDataflow_1 () returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else if (true) {
            msg = "2";
        } else {
            msg = "3";
        }
    } else if (true) {
        msg = "4";
    } else {
        msg = "5";
    }

    return msg;
}

function testDataflow_2 () returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else if (true) {
            // do nothing
        } else {
            msg = "2";
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_3 () returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else if (true) {
            msg = "2";
        } else {
            // do nothing
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_4 () returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        }
    } else {
        msg = "2";
    }
    
    return msg;
}

function testDataflow_5 () returns string {
    string msg;
    if (true) {
        if (true) {
            //do nothing
        } else {
            msg = "1";
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_6 () returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            msg = "1";
        }
    } else {
        // do nothing
    }

    return msg;
}

function testDataflow_7 () returns string {
    string msg;
    if (true) {
        msg = "1";
        if (true) {
        } else {
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_8 () returns string {
    string msg;
    if (true) {
        if (true) {
            // do nothing
        } else {
            // do nothing
        }
        msg = "1";
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_9 () returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            if (true) {
                if (true) {
                    if (true) {
                        if (true) {
                            msg = "2.1.1.1.1";
                        } else if (true) {
                            msg = "2.1.1.1.2";
                        } else {
                            msg = "2.1.1.1.3";
                        }
                    } else {
                        msg = "2.1.1.2";
                    }
                } else {
                    msg = "2.1.2";
                }
            } else {
                msg = "2.2";
            }
        }
    } else {
        msg = "3";
    }

    return msg;
}

function testDataflow_10 () returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            if (true) {
                if (true) {
                    if (true) {
                        if (true) {
                            msg = "2.1.1.1.1";
                        } else if (true) {
                            // do nothing
                        } else {
                            msg = "2.1.1.1.3";
                        }
                    } else {
                        msg = "2.1.1.2";
                    }
                } else {
                    msg = "2.1.2";
                }
            } else {
                msg = "2.2";
            }
        }
    } else {
        msg = "3";
    }

    return msg;
}

function testUninitializedVarReferrencing() {
    map m;
    int a;
    error e;
    string s;

    // assigning uninitialized var
    int b = a;

    // increment uninitialized var
    a++;

    // throw uninitialized var
    if (false) {
        throw e;
    }

    // uninitialized var in:
    //    - 'if' condition
    //    - braced expr
    //    - binary expr
    if (a > 4) {
        // do nothing
    }

    // uninitialized var foreach
    foreach val in m {
        // m["msg"] = "hello";
    }

    // uninitialized var in while
    while (4 > a) {
        a++;
    }

    // uninitialized var in conversion
    string str = <string> a;

    // uninitialized var XML
    xml x1 = xml`<foo id="{{a}}" xmlns:ns0="{{a}}">
                    <bar> hello {{a}}</bar>
                    <!-- refering to uninitialized {{a}} -->
                    <?target content {{a}}?>
                </foo>`;
                
    // uninitialized var in string template
    string text = string `hello {{a}}`;

    // uninitialized var index/field based access
    _ = m.foo;
    _ = m[s];

    // uninitialized var in function invocation, expression statement
    _ = m.hasKey(s);
    foo(a, str = s, s);

    // uninitialized var in xml attribute access
    xml x;
    _ = x@[s];

    // uninitialized var in xml attribute access
    int[] range = [a...a+5];
    
}

function foo(int a, string str = "hello", string... args) {

}

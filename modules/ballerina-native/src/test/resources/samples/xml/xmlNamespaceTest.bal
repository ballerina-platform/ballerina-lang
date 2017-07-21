xmlns "http://sample.com/wso2/a1" as ns0;
xmlns "http://sample.com/wso2/b1" as ns1;
xmlns "http://sample.com/wso2/c1";

function testNamespaceDclr() (string, string, string) {
    xmlns "http://sample.com/wso2/a2" as ns0;
    xmlns "http://sample.com/wso2/c2";
    xmlns "http://sample.com/wso2/d2" as ns3;
    
    return ns0:foo, ns1:foo, ns3:foo;
}

function testInnerScopeNamespaceDclr() (string, string, string) {
    string s1;
    string s2;
    string s3;
    
    if (true) {
        s1 = ns0:foo;
        
        xmlns "http://sample.com/wso2/a3" as ns0;
        s2 = ns0:foo;
    }
    
    s3 = ns0:foo;
    
    return s1, s2, s3;
}
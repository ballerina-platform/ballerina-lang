type s1 record {
  float x;
};

function getGlobalVars() returns float {
    s1 v = {};
    float f = v.foo:x;
    return f;
}

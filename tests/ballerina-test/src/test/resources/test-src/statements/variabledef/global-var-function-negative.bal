struct s1 {
  float x;
}

function getGlobalVars() returns (float) {
    s1 v = {};
    float f = v.foo:x;
}

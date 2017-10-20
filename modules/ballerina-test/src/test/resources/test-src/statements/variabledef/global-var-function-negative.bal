struct s1 {
  float x;
}

function getGlobalVars() (float) {
    s1 v = {};
    float f = v.foo:x;
}

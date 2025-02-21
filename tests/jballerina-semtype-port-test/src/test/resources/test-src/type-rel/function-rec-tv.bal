// @type F < Fx
type F function() returns F;
type Fx function() returns function;

// @type Gx < G
type G function(G);
type Gx function(function);

@readonly string NAME;

public function getName () returns string | ()  {
      return NAME;
}

public function testRefFunction() returns int {
    int var1 = 1;
    int var2 = 22;
    int var3 = var1 + var2;
    
    return var3;
}

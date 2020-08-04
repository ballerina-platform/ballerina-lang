import ballerina/io;

public function main() {
    // Nil Simple Type Descriptor
      ()  m  = (   );
    string|  (  )  n = "hello";
    json j =   null  ;

    // Boolean Simple Type Descriptor
      boolean   b1   =   true  ;
    boolean   b2   =   false  ;
    boolean|  (  )  b3 = true;

    // Int Simple Type Descriptor
      int   i1   = 10 ;
        int  |error   i2     = 10;
      int   i3   =     0X123;
      int   i4   =     -9223372  ;

    // Float Simple Type Descriptor
      float    f1    =   20.0;
        float   f2   =   6.0  / 3.0;
      float    f3    =   20.0f;

    // Decimal Simple Type Descriptor
      decimal   d1   = 27.5;
      decimal   d2 =   27.5D;
}

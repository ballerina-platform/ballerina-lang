@Bar{
    value:@InnerAnnotation1{
      value:@InnerAnnotation2{
         value:5
       }
    }
}
function foo (string args) {
    // do nothing
}

annotation Bar {
    InnerAnnotation1 value;
}

annotation InnerAnnotation1 {
    InnerAnnotation2 value;
}

annotation InnerAnnotation2 {
    string value;
}
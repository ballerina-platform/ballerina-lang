import ballerina/module1;

type AnnotationData record {|
 
|};

annotation AnnotationData MyAnnotationService on service;

annotation AnnotationData MyAnnotationClass on class;

annotation AnnotationData MyAnnotationReturn on return;
 
function testFunction() returns @MyAnnotationReturn int {
   return 0;
}
 
public function main() {
   typedesc<any> td = typeof testFunction(); 
   AnnotationData? unionResult = td.@module1:
}

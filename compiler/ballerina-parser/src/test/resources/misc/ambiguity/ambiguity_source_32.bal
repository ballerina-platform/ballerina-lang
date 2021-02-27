service object {} obj;

isolated service object {} obj;

service isolated object {} obj;

final service object {} dummyService1 = service object {};

configurable service object {} dummyService2 = service object {};

configurable final service object {} dummyService23 = service object {};

final configurable service object {} dummyService23 = service object {};

service service object {} on listner1 {

}

service isolated service object {} on listner1 {

}

service service isolated object {} on listner1 {

}

isolated service service object {} on listner1 {

}

isolated service isolated service object {} on listner1 {

}

isolated service service isolated object {} on listner1 {

}

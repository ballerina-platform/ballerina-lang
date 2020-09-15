type foo object {
     remote invalidtok transactional function foo();

     remote transactional invalidtok function foo0();

     remote invalidtok 6 function foo2();

     resource invalidtok 6 function foo3();
}

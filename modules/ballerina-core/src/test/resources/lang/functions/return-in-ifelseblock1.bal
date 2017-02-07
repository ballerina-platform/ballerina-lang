function returnStatementCheck1(int i)(int) {
   if(i == 0) {
      i = 5;
   } else {
      return 0;
   }
   return 1;
}
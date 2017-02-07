function returnStatementCheck3(int i)(int) {
   if(i == 0) {
      i = 5;
      return 0;
   } else if (i == 1){
      i = 10;
   } else {
      return 0;
   }
   return 0;
}
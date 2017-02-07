function returnStatementCheck3(int i)(int) {
   if(i == 0) {
      i = 5;
      return 0;
   } else if (i == 1){
      return 1;
   } else if (i == 2){
       i = 3;
   } else {
      return 0;
   }
}
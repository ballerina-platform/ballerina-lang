function returnStatementCheck1(int i)(int) {
   if(i == 0) {
      while(i < 10) {
          i = i +1;
          return 0;
      }
   } else {
      return 0;
   }
}
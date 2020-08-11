type Counter object {
   int count = 0;

   public function update() {
       foreach var i in 1 ... 1000 {
           lock {
               self.count = self.count + 1;
           }
       }
   }
};

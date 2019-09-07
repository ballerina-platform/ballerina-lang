const annotation v1 on source worker;

public function main(string... args) {worker worker1 {int a = 0;}

worker worker2 {
             int b = 0;}@v1
                                                    worker w2 {

                                   }

         fork { @v1
                            worker w1 {

                         }
                }
}
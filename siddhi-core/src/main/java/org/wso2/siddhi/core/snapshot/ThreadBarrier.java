package org.wso2.siddhi.core.snapshot;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class ThreadBarrier {
    private boolean open = true;
    
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition threadSwitchCondition  = lock.newCondition();
    
    public void pass() {
        lock.lock();
        if(!open){
//            threadSwitchCondition.awaitUninterruptibly();
            try {
                threadSwitchCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lock.unlock();
     }
    
    public void open(){
        lock.lock();
        if(!open){
            open = true;
            threadSwitchCondition.signalAll(); 
        }
        lock.unlock();
    }

    public void close() {
        lock.lock();
        open = false;
        lock.unlock();

    }

}

package ait.mediation;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int maxSize;
    private T message;
    Lock mutex = new ReentrantLock();
    Condition authorsWaitingCondition = mutex.newCondition();
    Condition readersWaitingCondition = mutex.newCondition();


    public BlkQueueImpl(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void push(T message) {
        mutex.lock();
        try {
            while (queue.size() >= maxSize) {
                try {
                    authorsWaitingCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            this.message = message;
            readersWaitingCondition.signal();

        } finally {
            mutex.unlock();
        }
    }

    @Override
    public T pop() {
        mutex.lock();
        try {
            while (this.message == null) {
                try {
                    readersWaitingCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T res = message;
            message = null;
            authorsWaitingCondition.signal();
            return res;
        } finally {
            mutex.unlock();
        }
    }
}


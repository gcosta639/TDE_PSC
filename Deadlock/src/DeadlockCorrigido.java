import java.util.concurrent.locks.ReentrantLock;


public class DeadlockCorrigido {
    static final ReentrantLock LOCK_A = new ReentrantLock();
    static final ReentrantLock LOCK_B = new ReentrantLock();

    static synchronized void log(String msg) {
        System.out.printf("[%tT.%<tL] %s%n", System.currentTimeMillis(), msg);
    }

    static void dorme(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static Thread criaThread(String nome) {
        return new Thread(() -> {
            log(nome + " tentando adquirir LOCK_A...");
            LOCK_A.lock();
            try {
                log(nome + " adquiriu LOCK_A");
                dorme(50);
                log(nome + " tentando adquirir LOCK_B...");
                LOCK_B.lock();
                try {
                    log(nome + " adquiriu LOCK_B");
                    log(nome + " concluiu");
                } finally {
                    LOCK_B.unlock();
                }
            } finally {
                LOCK_A.unlock();
            }
        }, nome);
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = criaThread("T1");
        Thread t2 = criaThread("T2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log("Programa terminou normalmente: sem deadlock.");
    }
}
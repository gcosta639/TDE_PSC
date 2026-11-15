import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.locks.ReentrantLock;


public class DeadlockTrava {
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

    public static void main(String[] args) {
        boolean modoJstack = args.length > 0 && args[0].equals("jstack");

        Thread t1 = new Thread(() -> {
            log("T1 tentando adquirir LOCK_A...");
            LOCK_A.lock();
            try {
                log("T1 adquiriu LOCK_A");
                dorme(50);
                log("T1 tentando adquirir LOCK_B...");
                LOCK_B.lock();
                try {
                    log("T1 adquiriu LOCK_B");
                    log("T1 concluiu");
                } finally {
                    LOCK_B.unlock();
                }
            } finally {
                LOCK_A.unlock();
            }
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            log("T2 tentando adquirir LOCK_B...");
            LOCK_B.lock();
            try {
                log("T2 adquiriu LOCK_B");
                dorme(50);
                log("T2 tentando adquirir LOCK_A...");
                LOCK_A.lock();
                try {
                    log("T2 adquiriu LOCK_A");
                    log("T2 concluiu");
                } finally {
                    LOCK_A.unlock();
                }
            } finally {
                LOCK_B.unlock();
            }
        }, "Thread-2");

        t1.start();
        t2.start();

        if (modoJstack) {
            log("Modo jstack: o programa vai ficar travado. PID = " + ProcessHandle.current().pid());
            return;
        }


        dorme(2000);
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] ids = bean.findDeadlockedThreads();
        if (ids != null) {
            System.out.println("\n*** DEADLOCK DETECTADO ***");
            for (ThreadInfo info : bean.getThreadInfo(ids, true, true)) {
                System.out.printf("%s está %s esperando o lock %s, que pertence a %s%n",
                        info.getThreadName(), info.getThreadState(),
                        info.getLockName(), info.getLockOwnerName());
            }
            System.exit(1);
        } else {
            System.out.println("Nenhum deadlock detectado (as threads terminaram).");
        }
    }
}
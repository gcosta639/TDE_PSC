import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class FilosofosHierarquia {
    static final int N = 5;
    static final int DURACAO_MS = 10_000;
    static final Semaphore[] garfos = new Semaphore[N];
    static final AtomicIntegerArray refeicoes = new AtomicIntegerArray(N);
    static volatile boolean rodando = true;

    static synchronized void log(int id, String estado) {
        System.out.printf("[%tT.%<tL] Filósofo %d: %s%n", System.currentTimeMillis(), id, estado);
    }

    static void dorme(int maxMs) {
        try {
            Thread.sleep((long) (Math.random() * maxMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < N; i++) {
            garfos[i] = new Semaphore(1, true);
        }

        Thread[] threads = new Thread[N];
        for (int i = 0; i < N; i++) {
            final int id = i;
            final int menor = Math.min(id, (id + 1) % N);
            final int maior = Math.max(id, (id + 1) % N);
            threads[i] = new Thread(() -> {
                while (rodando) {
                    log(id, "pensando");
                    dorme(200);
                    log(id, "com fome");
                    garfos[menor].acquireUninterruptibly();
                    try {
                        log(id, "pegou garfo " + menor + " (menor)");
                        garfos[maior].acquireUninterruptibly();
                        try {
                            log(id, "pegou garfo " + maior + " (maior) -> comendo");
                            refeicoes.incrementAndGet(id);
                            dorme(200);
                        } finally {
                            garfos[maior].release();
                        }
                    } finally {
                        garfos[menor].release();
                    }
                }
            }, "Filosofo-" + id);
            threads[i].start();
        }

        Thread.sleep(DURACAO_MS);
        rodando = false;
        for (Thread t : threads) t.join();

        System.out.println("\n=== Refeições por filósofo (justiça) ===");
        for (int i = 0; i < N; i++) {
            System.out.printf("Filósofo %d comeu %d vezes%n", i, refeicoes.get(i));
        }
        System.out.println("Nenhum deadlock: todos terminaram.");
    }
}
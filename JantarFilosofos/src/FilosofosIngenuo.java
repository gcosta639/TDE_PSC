import java.util.concurrent.Semaphore;

public class FilosofosIngenuo {
    static final int N = 5;
    static final Semaphore[] garfos = new Semaphore[N];
    static final String[] estado = new String[N];
    static volatile long ultimoProgresso = System.currentTimeMillis();

    static synchronized void setEstado(int id, String novo) {
        estado[id] = novo;
        ultimoProgresso = System.currentTimeMillis();
        System.out.printf("[%tT.%<tL] Filósofo %d: %s%n", System.currentTimeMillis(), id, novo);
    }

    static void dorme(int maxMs) {
        try {
            Thread.sleep((long) (Math.random() * maxMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < N; i++) {
            garfos[i] = new Semaphore(1);
            estado[i] = "pensando";
        }

        for (int i = 0; i < N; i++) {
            final int id = i;
            final int esquerda = id;
            final int direita = (id + 1) % N;
            Thread t = new Thread(() -> {
                while (true) {
                    setEstado(id, "pensando");
                    dorme(200);
                    setEstado(id, "com fome");
                    garfos[esquerda].acquireUninterruptibly();
                    System.out.printf("   Filósofo %d pegou garfo %d (esquerda)%n", id, esquerda);
                    dorme(100);
                    garfos[direita].acquireUninterruptibly();
                    System.out.printf("   Filósofo %d pegou garfo %d (direita)%n", id, direita);
                    setEstado(id, "comendo");
                    dorme(200);
                    garfos[direita].release();
                    garfos[esquerda].release();
                }
            }, "Filosofo-" + id);
            t.setDaemon(true);
            t.start();
        }


        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
            if (System.currentTimeMillis() - ultimoProgresso > 3000) {
                synchronized (FilosofosIngenuo.class) {
                    System.out.println("\nDeadlock: nenhum filósofo muda de estado há 3s");
                    for (int i = 0; i < N; i++) {
                        System.out.printf("Filósofo %d: %s%n", i, estado[i]);
                    }
                    System.exit(1);
                }
            }
        }
    }
}
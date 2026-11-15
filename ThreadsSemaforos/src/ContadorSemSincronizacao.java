public class ContadorSemSincronizacao {
    static volatile long count = 0;

    public static void main(String[] args) throws InterruptedException {
        int t = args.length > 0 ? Integer.parseInt(args[0]) : 8;
        int m = args.length > 1 ? Integer.parseInt(args[1]) : 200_000;
        int execucoes = args.length > 2 ? Integer.parseInt(args[2]) : 3;

        System.out.printf("Versão sem sincronização — T=%d, M=%d%n", t, m);
        System.out.println("| Execução | Esperado | Obtido | Perdidos | Tempo (ms) |");
        System.out.println("|---|---|---|---|---|");

        for (int e = 1; e <= execucoes; e++) {
            count = 0;
            Thread[] threads = new Thread[t];
            for (int i = 0; i < t; i++) {
                threads[i] = new Thread(() -> {
                    for (int k = 0; k < m; k++) {
                        count = count + 1;
                    }
                });
            }

            long inicio = System.nanoTime();
            for (Thread th : threads) th.start();
            for (Thread th : threads) th.join();
            long ms = (System.nanoTime() - inicio) / 1_000_000;

            long esperado = (long) t * m;
            System.out.printf("| %d | %d | %d | %d | %d |%n",
                    e, esperado, count, esperado - count, ms);
        }
    }
}

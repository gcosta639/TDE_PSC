**Requisitos para excutar**: necessário JDK 17 ou superior.

**Parte 1** 

```powershell

cd ../JantarFilosos

javac FilosofosIngenuo.java FilosofosHierarquia.java

java FilosofosIngenuo

java FilosofosHierarquia

```

**Parte 2**

```powershell
cd ../ThreadsSemaforos

javac ContadorSemSincronizacao.java ContadorComSemaforo.java

java ContadorSemSincronizacao

java ContadorComSemaforo
```
**Parte 3**

```powershell
cd ../Deadlock

javac DeadlockTrava.java DeadlockCorrigido.java

java DeadlockTrava

java DeadlockTrava jstack

java DeadlockCorrigido
```

**Estrutura**
JantarFilosos/ -> Parte 1

ThreadsSemaforos/ -> Parte 2

Deadlock/ -> Parte 3

**Jantar dos Filosófos(Parte 1)**

**O problema**

São 5 filósofos sentados numa mesa redonda, com um garfo entre cada par de filósofos. Cada um fica pensando e depois fica com fome. Para comer ele precisa dos dois garfos, o da esquerda e o da direita, só que cada garfo é dividido com o vizinho. Depois de comer ele larga os garfos e volta a pensar.


**O Deadlock**

No código FilosofosIngenuo.java todo mundo pega primeiro o garfo da esquerda e depois o da direita. Se os 5 pegarem o da esquerda ao mesmo tempo, cada um fica segurando um garfo e esperando o da direita, que está na mão do vizinho. Ninguém solta, ninguém come e todo mundo espera para sempre, e assim ocorre o deadlock.

**Solução**

No arquivo FilosofosHierarquia.java cada garfo tem um número e todo filósofo pega primeiro o garfo de menor número e depois o de maior. Por exemplo, o filósofo 4 usa os garfos 4 e 0, então ele pega o 0 primeiro e depois o 4.

Condição de Coffman que foi quebrada: a espera circular. Com a ordem fixa não dá para formar um ciclo de filósofos esperando uns pelos outros. As outras três condições (exclusão mútua, manter-e-esperar e não preempção) continuam existindo.

**Pseudocódigo**
```java
N = 5
garfos 0..N-1 (garfo i fica entre os filósofos i e (i+1) mod N)

Para cada filósofo p:
  left  = min(garfo_esquerda(p), garfo_direita(p))
  right = max(garfo_esquerda(p), garfo_direita(p))

  Loop:
    pensar()
    estado[p] <- "com fome"
    adquirir(left)
    adquirir(right)
    estado[p] <- "comendo"
    comer()
    liberar(right)
    liberar(left)
    estado[p] <- "pensando"
```



**Resultados**

FilosofosIngenuo.java

<img width="515" height="470" alt="image" src="https://github.com/user-attachments/assets/9821cb9d-5efa-4b1c-8f23-042cc8c036ff" />

FilosofosHierarquia.java

<img width="628" height="337" alt="image" src="https://github.com/user-attachments/assets/41aa28c8-f5a3-4bb4-bc13-32cf21e3a61a" />

**Threads e Semáforos(Parte 2)**

**O que é feito**

Um contador compartilhado é incrementado por T = 8 threads, cada uma fazendo M = 200.000 incrementos. O valor esperado no final é T x M = 1.600.000.

ContadorSemSincronizacao.java: sem proteção nenhuma. Vai perder incrementos.

ContadorComSemaforo.java: usa um semáforo binário justo (Semaphore(1, true)) em volta do count = count + 1.

**Pseudocódigo**

```java
count = 0
sem = Semaforo(permissoes = 1, justo = true)

Funcao tarefa():
  para i de 1 ate M:
    // VERSAO 1 - sem sincronizacao
    count = count + 1

    // VERSAO 2 - com semaforo
    sem.adquirir()
    try:
      count = count + 1
    finally:
      sem.liberar()

Principal:
  iniciar T threads executando tarefa()
  esperar todas terminarem
  imprimir esperado = T*M, obtido = count, tempo
```

**Resultados**

ContadorSemSincronizacao.java

<img width="462" height="147" alt="image" src="https://github.com/user-attachments/assets/8bfdb6e4-45f6-4b85-bcd9-81099c1f4d11" />

ContadorComSemaforo.java

<img width="470" height="150" alt="image" src="https://github.com/user-attachments/assets/2ec6b732-c7f8-44a0-bec3-4ff7588e909b" />

**Deadlock(Parte 3)**

**O que foi feito**
São duas threads e dois locks (LOCK_A e LOCK_B):

Thread 1: pega A, dorme 50ms e tenta pegar B.

Thread 2: pega B, dorme 50ms e tenta pegar A.

Cada uma fica segurando um lock e esperando o outro, e o programa trava. O sleep de 50ms é para o deadlock acontecer sempre.

As condições de Coffmam que apareceram foi:

Exclusão mútua: cada lock só pode ser usado por uma thread por vez.

Manter-e-esperar: cada thread segura um lock e espera pelo outro.

Não preempção: ninguém consegue tirar o lock à força de quem está com ele.

Espera circular: a Thread 1 espera o B, que está com a Thread 2, e a Thread 2 espera o A, que está com a Thread 1.

**Solução**
No DeadlockCorrigido.java as duas threads sempre pegam o LOCK_A antes do LOCK_B. Foi mantido o sleep(50) para mostrar que o problema era a ordem e não o tempo.

A condição que foi quebrada foi a espera circular, que quem chega depois fica esperando para pegar o LOCK_A, sem segurar nada, fazendo com que não se forme o ciclo.

**Pseudocódigo**

Versão que trava:
```java

Thread 1:                      Thread 2:
  adquirir(LOCK_A)               adquirir(LOCK_B)
  dormir(50ms)                   dormir(50ms)
  adquirir(LOCK_B)               adquirir(LOCK_A)
  imprimir "T1 concluiu"         imprimir "T2 concluiu"
  liberar(LOCK_B)                liberar(LOCK_A)
  liberar(LOCK_A)                liberar(LOCK_B)
```

Versão corrigida
```java

Regra: sempre adquirir LOCK_A antes de LOCK_B, em todas as threads.

Thread 1 e Thread 2:
  adquirir(LOCK_A)
  adquirir(LOCK_B)
  // secao critica
  liberar(LOCK_B)
  liberar(LOCK_A)
```
**Resultados**

DeadlockTrava.java

<img width="1069" height="247" alt="image" src="https://github.com/user-attachments/assets/06de0217-4213-4492-9a56-526804693132" />

DeadlockCorrigido.java

<img width="526" height="271" alt="image" src="https://github.com/user-attachments/assets/fb9d4c63-d3c4-4201-adb7-51cf10465024" />



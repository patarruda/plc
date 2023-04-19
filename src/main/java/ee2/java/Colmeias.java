package ee2.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class Rainha {
    private List<Tarefa> tarefasPendentes;
    private List<Tarefa> tarefasFinlizadas;

    public Rainha(List<Tarefa> tarefasPendentes) {
        this.tarefasPendentes = tarefasPendentes;
        this.tarefasFinlizadas = new ArrayList<>();
    }

    public boolean hasTarefas() {
        synchronized (this) {
            return !tarefasPendentes.isEmpty();
        }
    }

    /*
     * Devolve a próxima tarefa discponível e retira ela da lista tarefasPendentes
     */
    public Tarefa takeProximaTarefa() {
        List<Integer> dependencias;
        Tarefa res = null;
        Tarefa tarefa = null;
        for (int i = 0; i < tarefasPendentes.size(); i++) {//(Tarefa tarefa : tarefasPendentes) {
            tarefa = tarefasPendentes.get(i);
            dependencias = tarefa.getDependencias();
            synchronized (this) {
                if (dependencias.isEmpty() || contains(tarefasFinlizadas, dependencias)) {
                    //tarefasPendentes.remove(tarefa);
                    tarefasPendentes.remove(i);
                    res = tarefa;
                    break;
                } else {
                    //tem dependência. joga para o final da fila
                    tarefasPendentes.remove(i);
                    tarefasPendentes.add(tarefa);
                    i--; // corrige índice do loop
                }
            }
        }
        return res;
    }

    /*
     * Adiciona a tarefa na lista tarefasFinlizadas
     *   Deve ser chamado após a tarefa ser finalizada
     */
    public void putTarefaFinalizada(Tarefa tarefa) {
        synchronized (this) {
            tarefasFinlizadas.add(tarefa);
        }
    }

    /*
     * Verifica se a tarefa com o idTarefa pertence à lista passada
     */
    public boolean contains(List<Tarefa> lista, int idTarefa) {
        boolean res = false;
        if (!lista.isEmpty()) {
            for (Tarefa tarefa : lista) {
                if (tarefa.getId() == idTarefa) {
                    res = true;
                }
            }
        }
        return res;
    }

    /*
     * Verifica se as tarefa com os ids recebidos pertencem à lista passada
     */
    public boolean contains(List<Tarefa> lista, List<Integer> ids) {
        boolean res = true;
        for (int id : ids) {
            if (!contains(lista, id)) {
                res = false;
                break;
            }
        }
        return res;
    }
}




class Operaria implements Runnable {
    // Objeto compartilhado entre as threads
    private Rainha rainha; 

    public Operaria(Rainha rainha) {
        this.rainha = rainha;
    }

    public void run() {
        Tarefa tarefa = null;
        
        //Verifica se a rainha tem tarefas pendentes (espera ocupada)
        while(rainha.hasTarefas()) {
            
            // pega a próxima tarefa disponível da rainha
            synchronized(rainha) {
                if (rainha.hasTarefas()) {
                    tarefa = rainha.takeProximaTarefa();
                }
            }

            if (tarefa != null) {
                try {
                    //Suspende a thread durante o tempoResolucao da tarefa
                    Thread.sleep(tarefa.getTempoResolucao());
                } catch (InterruptedException e) {}

                // Adiciona a tarefa na lista tarefasFinalizadas da rainha
                synchronized(rainha) {
                    rainha.putTarefaFinalizada(tarefa);
                    System.out.format("tarefa %d feita%n", tarefa.getId());
                }
                
            }
        }          
                    
    }
}




class Tarefa {
    private int id;

    //Tempo que a tarefa leva para ser resolvida
    private long tempoResolucao; // milisegundos
    
    //ids das tarefas que precisam estar finalizadas para que esta possa ser executada
    private List<Integer> dependencias; 


    public Tarefa(String dadosTarefa) {
        // quebrar dados tarefas em palavras (separadas por whitespaces)
        String[] palavras = dadosTarefa.split("\\s+");
        
        //setar id e tempoResolucao
        setId(palavras[0]);
        setTempoResolucao(palavras[1]);
        
        // inicializar e popular dependencias
        this.dependencias = new ArrayList<>();
        for (int i = 2; i < palavras.length; i++) {
            this.dependencias.add(Integer.parseInt(palavras[i]));
        }

    }

    // getters e setters
    public void setId(String id) {
        this.id = Integer.parseInt(id);
    }
    public void setTempoResolucao(String tempoResolucao) {
        this.tempoResolucao = Long.parseLong(tempoResolucao);
    }
    public int getId() {
        return id;
    }
    public long getTempoResolucao() {
        return tempoResolucao;
    }
    public List<Integer> getDependencias() {
        return dependencias;
    }
}



/*
 * Classe main
 */
public class Colmeias {

    private static ExecutorService pool; // ThreadPool
    private static int numOperarios; //quantidade de abelhas operárias
    private static int numTarefas; // quantidade de tarefas
    

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        System.out.println("\n\n###   COLMEIA   ###\n\nQuantas abelhas operárias?");
        numOperarios = scan.nextInt();

        System.out.println("Quantos tarefas a realizar?");
        numTarefas = scan.nextInt();

        List<Tarefa> tarefas = new ArrayList<>();
        String dadosTarefa;
        
        System.out.format("Linha a Linha, digite as propriedades das %d tarefas no formato:%nid tempoResolucao idsDependencias%n%n", numTarefas);
        scan.nextLine();
        for (int i = 0; i < numTarefas; i++) {
            //lê input, cria uma nova tarefa e adiciona na lista de tarefas
            dadosTarefa = scan.nextLine();
            tarefas.add(new Tarefa(dadosTarefa));
        }

        // Cria o ThreadPool com o número de threads Operarias
        pool = Executors.newFixedThreadPool(numOperarios);
        
        //Cria a rainha
        Rainha rainha = new Rainha(tarefas);

        // registra hora de início da operação
        String formatHora = "%1$tH:%1$tM:%1$tS.%1$tL";
        long inicio = System.currentTimeMillis();
        String ini = String.format(formatHora, inicio);
        System.out.format("\nOPERAÇÃO INICIADA!  %nHorário: %d - %s%n%n", inicio, ini);

        
        for (int j = 0; j < numOperarios; j++) {
            pool.execute(new Operaria(rainha));
        }
        pool.shutdown();

        while (!pool.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Todas as tarefas foram concluídas.");
        long fim = System.currentTimeMillis();
        String sFim = String.format(formatHora, fim);
        System.out.format("\nOPERAÇÃO FINALIZADA!  %nHorário: %d - %s%n%n", fim, sFim);

    }   
}

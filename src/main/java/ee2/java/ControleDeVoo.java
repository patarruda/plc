package ee2.java;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class Aviao implements Runnable, Comparable<Aviao>  {

    //Objeto compartilhado entre as threads Aviao
    private Aeroporto aeroporto;

    private int id;
    private long horaPrevista; //miliseconds  
    private long horaExecutada; //miliseconds  
    
    //true para Decolagem, false para Aterrissagem
    private boolean isPartida; 
    

    public Aviao(Aeroporto aeroporto, int id, long horaPrevista, boolean isPartida) {
        this.aeroporto = aeroporto;
        this.id = id;
        this.horaPrevista = horaPrevista;
        this.isPartida = isPartida;
    }

    public void run() {
        try {
            
            //Entrar na fila de aviões do aeroporto
            //synchronized (aeroporto) {
                aeroporto.programarVoo(this);
            //}

            //Suspender esta thread até que seja horário do vôo
            if (getHoraPrevista() > System.currentTimeMillis()) {
                //System.out.format("Aviao id %d dormiu%n", id);
                Thread.sleep(getHoraPrevista() - System.currentTimeMillis());
                //System.out.format("Aviao id %d acordou%n", id);
            }

            //Executar Decolagem ou Aterrisagem
            //synchronized (aeroporto) {
                aeroporto.acessarPista(this);
            //}
        
        } catch (InterruptedException e) {
            System.out.format("Avião id %d foi interrompido enquanto aguardava!%n", id);
        }
    }

    //SETA a horaExecutada e IMPRIME os dados do vôo realizado
    public void vooExecutado(long horaExecutada) {
        setHoraExecutada(horaExecutada);
        String horaFormat = "%1$tH:%1$tM:%1$tS.%1$tL";
        String sHoraExec = String.format(horaFormat, horaExecutada);
        String sHoraPrev = String.format(horaFormat, horaPrevista);
        
        System.out.format("%s do Avião id %d registrada!%n", tipoVoo(), id);
        System.out.format("  Previsto: %d - %s%n  Realizado: %d - %s%n", horaPrevista, sHoraPrev,
                                                                         horaExecutada, sHoraExec);
        System.out.format("  Atraso: %d - %s%n", horaExecutada - horaPrevista, 
                                String.format("%1$tM:%1$tS.%1$tL", horaExecutada - horaPrevista));
    }

    public String toString() {
        return String.format("Avião %d - %s >> %d - %s", id, tipoVoo(), horaPrevista, 
                                                    String.format("%1$tH:%1$tM:%1$tS.%1$tL", horaPrevista));
    }

    public String tipoVoo() {
        String res = "Aterrissagem";
        if (isPartida) res = "Decolagem";
        
        return res;
    }

    public int compareTo(Aviao aviao) {
        return Long.compare(this.horaPrevista, aviao.getHoraPrevista());
    }

    // getters e setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public long getHoraPrevista() {
        return horaPrevista;
    }
    public void setHoraPrevista(long horaPrevista) {
        this.horaPrevista = horaPrevista;
    }
    public long getHoraExecutada() {
        return horaExecutada;
    }
    public void setHoraExecutada(long horaExecutada) {
        this.horaExecutada = horaExecutada;
    }  
    public boolean isPartida() {
        return isPartida;
    }
    public void setPartida(boolean isPartida) {
        this.isPartida = isPartida;
    }

    
}

class Aeroporto {

    private int pistas; //pistas disponíveis
    private List<Aviao> fila; //fila de aviões para decolagem e aterrissagem
    private long tempoOcupado = 500; //tempo de ocupação da pista por vôo
    private Lock lock = new ReentrantLock();
    private Condition pistaLivre = lock.newCondition();

    

    public Aeroporto(int pistas) {
        this.pistas = pistas;
        this.fila = new ArrayList<>();
    }

    public void programarVoo(Aviao aviao) {
        lock.lock();

        //Colocar na fila em ordem de horário do vôo
        fila.add(aviao);
        Collections.sort(fila);
        System.out.println("PROGRAMADO: " + aviao);

        /*
        System.out.println(fila);
        for (Aviao a : fila) {
           System.out.println(a.getHoraPrevista() + " - " 
           + String.format("%1$tH:%1$tM:%1$tS.%1$tL", a.getHoraPrevista()));
           
        }  */

        lock.unlock();
    }

    public void acessarPista(Aviao aviao) {        
        lock.lock();

        //Checar se há pista disponível
        //se não houver, suspender a execução aguardando a Condition pistaLivre
        while (pistas == 0) {
            try {
                System.out.println("\nESPERANDO PISTA\n");
                pistaLivre.await();         
            } catch (InterruptedException e) {}
        }       
        
        //segue a execução depois de ser sinalizado que a Condition pistaLivre foi atendida
        try {
            pistas--; // ocupa uma pista
            Aviao next = fila.remove(0); // pega o 1º avião na espera
            next.vooExecutado(System.currentTimeMillis()); // vôo executado - dados são impressos
            
            //O processo de decolagem ou de aterrissagem de um avião ocupa a pista por 500 milissegundos
            Thread.sleep(tempoOcupado);

            pistas++; // libera uma pista
            pistaLivre.signalAll(); // sinaliza Threads que estão aguardando a Condition pistaLivre
        
        } catch (InterruptedException e) {
        } finally {
            lock.unlock(); // libera a trava
        }
    }
}

/**
 * Classe main
 */
public class ControleDeVoo {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int avioesPartida;
        int avioesChegada;
        int pistas;
        long timeSpan = 20000; //intervalo para distribuição dos voos em milisegundos
        long offset = 2000; // offset para início dos voos
        String horaFormat = "%1$tH:%1$tM:%1$tS.%1$tL";
    

        System.out.println("\n\n###   CONTROLE DE VÔOS   ###\n\nQuantos aviões programados para decolagem?");
        avioesPartida = scan.nextInt();

        System.out.println("Quantos aviões programados para aterrissagem?");
        avioesChegada = scan.nextInt();

        System.out.println("Quantas pistas disponíveis no aeroporto?");
        pistas = scan.nextInt();

        // Registra o horário de início da operação
        long inicio = System.currentTimeMillis();
        String ini = String.format(horaFormat, inicio);
        System.out.format("\nOPERAÇÃO INICIADA!  %nHorário: %d - %s%n%n", inicio, ini);
        

        // Cria aeroporto e lista de Threads para incluir os Avioes
        Aeroporto aeroporto = new Aeroporto(pistas);
        Thread[] threads = new Thread[avioesPartida + avioesChegada];

        int p;
        int c;
        long hora;
        Random random = new Random();

        // Cria aviões para decolagem e inicia threads
        for (p = 0; p < avioesPartida; p++) {
            //Horário do vôo criado randomicamente, num intervalo timeSpan, a partir da hora atual + offset
            hora = System.currentTimeMillis() + offset + (Math.abs(random.nextLong() % timeSpan)); 
            threads[p] = new Thread(new Aviao(aeroporto, p, hora, true));
            threads[p].start();
            //System.out.format("Avião id %d programado para partida%n  Horário: %d%n", p, hora);
        }

        // Cria aviões para aterrissagem e inicia threads
        for (c = 0; c < avioesChegada; c++) {
            //Horário do vôo criado randomicamente, num intervalo timeSpan, a partir da hora atual + offset
            hora = System.currentTimeMillis() + offset + (Math.abs(random.nextLong() % timeSpan)); // até timeSpan segundos no futuro
            threads[p+c] = new Thread(new Aviao(aeroporto, p+c, hora, false));
            threads[p+c].start();
            //System.out.format("Avião id %d programado para chegada%n  Horário: %d%n", p+c, hora);
        }

        // Main espera pelo fim de todas as threads (vôos)
        for (int t = 0; t < threads.length; t++) {
            try {
                threads[t].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Registra o horário de fim da operação
        long fim = System.currentTimeMillis();
        String sFim = String.format(horaFormat, fim);
        System.out.format("\nOPERAÇÃO FINALIZADA!  %nHorário: %d - %s%n", fim, sFim);
        System.out.format("TEMPO TOTAL: %d - %s%n", fim - inicio, 
                            String.format(horaFormat, fim - inicio));
        
    }
}




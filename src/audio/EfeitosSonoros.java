package audio;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class EfeitosSonoros {

    static final float TAXA_AMOSTRAGEM = 44100f;

    private static final int LIMITE_FALHAS = 3;

    private static final String CLIQUE = "clique";
    private static final String BANDEIRA = "bandeira";
    private static final String EXPLOSAO = "explosao";
    private static final String VITORIA = "vitoria";
    private static final String DICA = "dica";
    private static final String ERRO = "erro";
    private static final String CONQUISTA = "conquista";

    private final Map<String, byte[]> cacheSons = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private volatile boolean ativo = true;
    private volatile boolean audioIndisponivel = false;
    private int falhasConsecutivas = 0;

    public EfeitosSonoros() {
        executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable tarefa) {
                Thread thread = new Thread(tarefa, "efeitos-sonoros");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void tocarClique() {
        tocar(CLIQUE);
    }

    public void tocarBandeira() {
        tocar(BANDEIRA);
    }

    public void tocarExplosao() {
        tocar(EXPLOSAO);
    }

    public void tocarVitoria() {
        tocar(VITORIA);
    }

    public void tocarDica() {
        tocar(DICA);
    }

    public void tocarErro() {
        tocar(ERRO);
    }

    public void tocarConquista() {
        tocar(CONQUISTA);
    }

    private void tocar(final String nomeSom) {
        if (!ativo || audioIndisponivel) {
            return;
        }
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    reproduzir(nomeSom);
                }
            });
        } catch (RejectedExecutionException e) {
            audioIndisponivel = true;
        } catch (Exception e) {
            audioIndisponivel = true;
        }
    }

    private void reproduzir(String nomeSom) {
        if (!ativo || audioIndisponivel) {
            return;
        }
        SourceDataLine linha = null;
        try {
            byte[] dados = obterSom(nomeSom);
            AudioFormat formato = criarFormato();
            linha = AudioSystem.getSourceDataLine(formato);
            linha.open(formato, Math.max(4096, (int) (TAXA_AMOSTRAGEM * 0.1) * 2));
            linha.start();
            linha.write(dados, 0, dados.length);
            linha.drain();
            falhasConsecutivas = 0;
        } catch (Exception e) {
            registrarFalha();
        } catch (LinkageError e) {
            audioIndisponivel = true;
        } finally {
            if (linha != null) {
                try {
                    linha.stop();
                    linha.close();
                } catch (Exception e) {
                    registrarFalha();
                }
            }
        }
    }

    private void registrarFalha() {
        falhasConsecutivas++;
        if (falhasConsecutivas >= LIMITE_FALHAS) {
            audioIndisponivel = true;
        }
    }

    static AudioFormat criarFormato() {
        return new AudioFormat(TAXA_AMOSTRAGEM, 16, 1, true, false);
    }

    private byte[] obterSom(String nomeSom) {
        byte[] existente = cacheSons.get(nomeSom);
        if (existente != null) {
            return existente;
        }
        byte[] gerado = paraBytes(gerarAmostras(nomeSom));
        cacheSons.put(nomeSom, gerado);
        return gerado;
    }

    private double[] gerarAmostras(String nomeSom) {
        if (CLIQUE.equals(nomeSom)) {
            return gerarClique();
        }
        if (BANDEIRA.equals(nomeSom)) {
            return gerarBandeira();
        }
        if (EXPLOSAO.equals(nomeSom)) {
            return gerarExplosao();
        }
        if (VITORIA.equals(nomeSom)) {
            return gerarVitoria();
        }
        if (DICA.equals(nomeSom)) {
            return gerarDica();
        }
        if (ERRO.equals(nomeSom)) {
            return gerarErro();
        }
        return gerarConquista();
    }

    private double[] gerarClique() {
        double[] amostras = criarBuffer(0.04);
        for (int i = 0; i < amostras.length; i++) {
            double tempo = i / TAXA_AMOSTRAGEM;
            double envelope = Math.exp(-tempo * 90.0);
            amostras[i] = 0.5 * envelope * Math.sin(2 * Math.PI * 1200 * tempo);
        }
        return aplicarFades(amostras, 0.001, 0.008);
    }

    private double[] gerarBandeira() {
        double duracaoNota = 0.06;
        double[] amostras = criarBuffer(duracaoNota * 2);
        adicionarNota(amostras, 0.0, duracaoNota, 660, 0.35, 30.0);
        adicionarNota(amostras, duracaoNota, duracaoNota, 990, 0.35, 30.0);
        return aplicarFades(amostras, 0.002, 0.015);
    }

    private double[] gerarExplosao() {
        double[] amostras = criarBuffer(0.6);
        Random aleatorio = new Random(424242L);
        double ruidoFiltrado = 0.0;
        for (int i = 0; i < amostras.length; i++) {
            double tempo = i / TAXA_AMOSTRAGEM;
            double envelope = Math.exp(-tempo * 6.5);
            double ruido = aleatorio.nextDouble() * 2.0 - 1.0;
            ruidoFiltrado = ruidoFiltrado * 0.82 + ruido * 0.18;
            double frequenciaRonco = 55.0 - 20.0 * tempo;
            double ronco = Math.sin(2 * Math.PI * frequenciaRonco * tempo) * Math.exp(-tempo * 4.0);
            double estalo = ruido * Math.exp(-tempo * 40.0);
            amostras[i] = envelope * (0.55 * ruidoFiltrado * 2.2 + 0.45 * ronco) + 0.3 * estalo;
        }
        normalizar(amostras, 0.8);
        return aplicarFades(amostras, 0.001, 0.08);
    }

    private double[] gerarVitoria() {
        double[] frequencias = {523.25, 659.25, 783.99, 1046.50};
        double duracaoNota = 0.14;
        double[] amostras = criarBuffer(0.7);
        for (int indice = 0; indice < frequencias.length; indice++) {
            double inicio = indice * duracaoNota;
            double duracao = indice == frequencias.length - 1 ? 0.7 - inicio : duracaoNota * 1.4;
            adicionarNota(amostras, inicio, duracao, frequencias[indice], 0.25, 5.0);
        }
        normalizar(amostras, 0.6);
        return aplicarFades(amostras, 0.003, 0.08);
    }

    private double[] gerarDica() {
        double[] amostras = criarBuffer(0.45);
        adicionarNota(amostras, 0.0, 0.45, 880, 0.22, 7.0);
        adicionarNota(amostras, 0.12, 0.33, 1318.5, 0.18, 8.0);
        normalizar(amostras, 0.4);
        return aplicarFades(amostras, 0.01, 0.08);
    }

    private double[] gerarErro() {
        double[] amostras = criarBuffer(0.15);
        for (int i = 0; i < amostras.length; i++) {
            double tempo = i / TAXA_AMOSTRAGEM;
            double fase = (tempo * 150.0) % 1.0;
            double serra = 2.0 * fase - 1.0;
            double quadrada = Math.sin(2 * Math.PI * 150 * tempo) >= 0 ? 1.0 : -1.0;
            amostras[i] = 0.18 * serra + 0.12 * quadrada;
        }
        normalizar(amostras, 0.4);
        return aplicarFades(amostras, 0.004, 0.03);
    }

    private double[] gerarConquista() {
        double[] amostras = criarBuffer(0.6);
        adicionarNotaBrilhante(amostras, 0.0, 0.12, 783.99, 0.25);
        adicionarNotaBrilhante(amostras, 0.12, 0.12, 1046.50, 0.25);
        adicionarNotaBrilhante(amostras, 0.24, 0.36, 1567.98, 0.28);
        normalizar(amostras, 0.6);
        return aplicarFades(amostras, 0.002, 0.08);
    }

    private double[] criarBuffer(double segundos) {
        return new double[(int) (segundos * TAXA_AMOSTRAGEM)];
    }

    private void adicionarNota(double[] amostras, double inicio, double duracao, double frequencia,
                               double amplitude, double decaimento) {
        int primeira = (int) (inicio * TAXA_AMOSTRAGEM);
        int quantidade = (int) (duracao * TAXA_AMOSTRAGEM);
        int ataque = (int) (0.004 * TAXA_AMOSTRAGEM);
        for (int i = 0; i < quantidade && primeira + i < amostras.length; i++) {
            double tempo = i / TAXA_AMOSTRAGEM;
            double subida = i < ataque ? (double) i / ataque : 1.0;
            double descida = Math.min(1.0, (quantidade - i) / (0.01 * TAXA_AMOSTRAGEM));
            double envelope = subida * descida * Math.exp(-tempo * decaimento);
            double onda = Math.sin(2 * Math.PI * frequencia * tempo)
                    + 0.3 * Math.sin(2 * Math.PI * frequencia * 2 * tempo);
            amostras[primeira + i] += amplitude * envelope * onda;
        }
    }

    private void adicionarNotaBrilhante(double[] amostras, double inicio, double duracao, double frequencia,
                                        double amplitude) {
        adicionarNota(amostras, inicio, duracao, frequencia, amplitude, 4.0);
        adicionarNota(amostras, inicio, duracao, frequencia * 3, amplitude * 0.15, 9.0);
        adicionarNota(amostras, inicio, duracao, frequencia * 1.5, amplitude * 0.2, 6.0);
    }

    static void normalizar(double[] amostras, double pico) {
        double maximo = 0.0;
        for (double amostra : amostras) {
            maximo = Math.max(maximo, Math.abs(amostra));
        }
        if (maximo <= 0.0) {
            return;
        }
        double fator = pico / maximo;
        for (int i = 0; i < amostras.length; i++) {
            amostras[i] *= fator;
        }
    }

    static double[] aplicarFades(double[] amostras, double segundosEntrada, double segundosSaida) {
        int entrada = Math.min(amostras.length, (int) (segundosEntrada * TAXA_AMOSTRAGEM));
        int saida = Math.min(amostras.length, (int) (segundosSaida * TAXA_AMOSTRAGEM));
        for (int i = 0; i < entrada; i++) {
            amostras[i] *= (double) i / entrada;
        }
        for (int i = 0; i < saida; i++) {
            amostras[amostras.length - 1 - i] *= (double) i / saida;
        }
        return amostras;
    }

    static byte[] paraBytes(double[] amostras) {
        byte[] bytes = new byte[amostras.length * 2];
        for (int i = 0; i < amostras.length; i++) {
            double limitado = Math.max(-1.0, Math.min(1.0, amostras[i]));
            short valor = (short) Math.round(limitado * Short.MAX_VALUE);
            bytes[2 * i] = (byte) (valor & 0xFF);
            bytes[2 * i + 1] = (byte) ((valor >> 8) & 0xFF);
        }
        return bytes;
    }
}

package audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

public class MusicaAmbiente {

    private static final float TAXA_AMOSTRAGEM = EfeitosSonoros.TAXA_AMOSTRAGEM;
    private static final double DURACAO_ACORDE = 4.0;
    private static final double SOBREPOSICAO = 1.2;
    private static final int AMOSTRAS_POR_BLOCO = 2048;

    private static final double[][] ACORDES = {
        {110.00, 220.00, 261.63, 329.63, 440.00},
        {87.31, 174.61, 220.00, 261.63, 349.23},
        {65.41, 130.81, 196.00, 261.63, 329.63},
        {98.00, 196.00, 246.94, 293.66, 392.00}
    };

    private final Object trava = new Object();
    private volatile boolean tocando = false;
    private volatile int volume = 60;
    private volatile int geracao = 0;
    private volatile short[] amostrasLoop;
    private Clip clip;
    private FloatControl controleGanho;

    public MusicaAmbiente() {
    }

    public void iniciar() {
        final int minhaGeracao;
        synchronized (trava) {
            if (tocando) {
                return;
            }
            tocando = true;
            geracao++;
            minhaGeracao = geracao;
        }
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    executarReproducao(minhaGeracao);
                }
            }, "musica-ambiente");
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            synchronized (trava) {
                tocando = false;
            }
        }
    }

    public void parar() {
        synchronized (trava) {
            tocando = false;
            geracao++;
            fecharClip();
        }
    }

    public boolean isTocando() {
        return tocando;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        synchronized (trava) {
            aplicarGanho();
        }
    }

    public int getVolume() {
        return volume;
    }

    private boolean aindaAtual(int minhaGeracao) {
        return tocando && geracao == minhaGeracao;
    }

    private void executarReproducao(int minhaGeracao) {
        try {
            short[] amostras = obterAmostras();
            if (!aindaAtual(minhaGeracao)) {
                return;
            }
            if (tentarTocarComClip(amostras, minhaGeracao)) {
                return;
            }
            tocarPorStreaming(amostras, minhaGeracao);
        } catch (Exception e) {
            encerrarSeAtual(minhaGeracao);
        } catch (LinkageError e) {
            encerrarSeAtual(minhaGeracao);
        } catch (OutOfMemoryError e) {
            encerrarSeAtual(minhaGeracao);
        }
    }

    private void encerrarSeAtual(int minhaGeracao) {
        synchronized (trava) {
            if (geracao == minhaGeracao) {
                fecharClip();
            }
        }
    }

    private boolean tentarTocarComClip(short[] amostras, int minhaGeracao) {
        Clip novoClip = null;
        try {
            AudioFormat formato = EfeitosSonoros.criarFormato();
            byte[] dados = paraBytes(amostras, 1.0);
            novoClip = AudioSystem.getClip();
            novoClip.open(formato, dados, 0, dados.length);
            if (!novoClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                novoClip.close();
                return false;
            }
            synchronized (trava) {
                if (!aindaAtual(minhaGeracao)) {
                    novoClip.close();
                    return true;
                }
                clip = novoClip;
                controleGanho = (FloatControl) novoClip.getControl(FloatControl.Type.MASTER_GAIN);
                aplicarGanho();
                novoClip.setLoopPoints(0, -1);
                novoClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            return true;
        } catch (Exception e) {
            fecharSilenciosamente(novoClip);
            synchronized (trava) {
                if (clip == novoClip) {
                    clip = null;
                    controleGanho = null;
                }
            }
            return false;
        }
    }

    private void tocarPorStreaming(short[] amostras, int minhaGeracao) {
        SourceDataLine linha = null;
        try {
            AudioFormat formato = EfeitosSonoros.criarFormato();
            linha = AudioSystem.getSourceDataLine(formato);
            linha.open(formato, AMOSTRAS_POR_BLOCO * 8);
            linha.start();
            byte[] bloco = new byte[AMOSTRAS_POR_BLOCO * 2];
            int posicao = 0;
            while (aindaAtual(minhaGeracao)) {
                double fator = fatorLinear();
                for (int i = 0; i < AMOSTRAS_POR_BLOCO; i++) {
                    int valor = (int) Math.round(amostras[posicao] * fator);
                    bloco[2 * i] = (byte) (valor & 0xFF);
                    bloco[2 * i + 1] = (byte) ((valor >> 8) & 0xFF);
                    posicao++;
                    if (posicao >= amostras.length) {
                        posicao = 0;
                    }
                }
                linha.write(bloco, 0, bloco.length);
            }
            linha.flush();
        } catch (Exception e) {
            encerrarSeAtual(minhaGeracao);
        } finally {
            if (linha != null) {
                try {
                    linha.stop();
                    linha.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private double fatorLinear() {
        double proporcao = volume / 100.0;
        return proporcao * proporcao;
    }

    private void aplicarGanho() {
        FloatControl controle = controleGanho;
        if (controle == null) {
            return;
        }
        try {
            float minimo = controle.getMinimum();
            float maximo = controle.getMaximum();
            float decibeis;
            if (volume <= 0) {
                decibeis = minimo;
            } else {
                decibeis = (float) (40.0 * Math.log10(volume / 100.0));
            }
            controle.setValue(Math.max(minimo, Math.min(Math.min(maximo, 0f), decibeis)));
        } catch (Exception e) {
            controleGanho = null;
        }
    }

    private void fecharClip() {
        Clip atual = clip;
        clip = null;
        controleGanho = null;
        fecharSilenciosamente(atual);
    }

    private static void fecharSilenciosamente(Clip alvo) {
        if (alvo == null) {
            return;
        }
        try {
            alvo.stop();
            alvo.close();
        } catch (Exception e) {
        }
    }

    private short[] obterAmostras() {
        short[] existentes = amostrasLoop;
        if (existentes != null) {
            return existentes;
        }
        synchronized (MusicaAmbiente.class) {
            if (amostrasLoop == null) {
                amostrasLoop = gerarLoop();
            }
            return amostrasLoop;
        }
    }

    private static short[] gerarLoop() {
        int totalAmostras = (int) (DURACAO_ACORDE * ACORDES.length * TAXA_AMOSTRAGEM);
        double[] mistura = new double[totalAmostras];
        for (int indice = 0; indice < ACORDES.length; indice++) {
            adicionarPad(mistura, indice * DURACAO_ACORDE, ACORDES[indice]);
            adicionarArpejo(mistura, indice * DURACAO_ACORDE, ACORDES[indice]);
        }
        EfeitosSonoros.normalizar(mistura, 0.45);
        short[] resultado = new short[totalAmostras];
        for (int i = 0; i < totalAmostras; i++) {
            resultado[i] = (short) Math.round(mistura[i] * Short.MAX_VALUE);
        }
        return resultado;
    }

    private static void adicionarPad(double[] mistura, double inicioAcorde, double[] frequencias) {
        double duracaoTotal = DURACAO_ACORDE + 2 * SOBREPOSICAO;
        int quantidade = (int) (duracaoTotal * TAXA_AMOSTRAGEM);
        int deslocamento = (int) ((inicioAcorde - SOBREPOSICAO) * TAXA_AMOSTRAGEM);
        int amostrasTransicao = (int) (2 * SOBREPOSICAO * TAXA_AMOSTRAGEM);
        for (int i = 0; i < quantidade; i++) {
            double tempo = i / TAXA_AMOSTRAGEM;
            double envelope = envelopeSuave(i, quantidade, amostrasTransicao);
            double valor = 0.0;
            for (int voz = 0; voz < frequencias.length; voz++) {
                double frequencia = frequencias[voz];
                double peso = voz == 0 ? 0.55 : 0.32;
                double sinusoide = Math.sin(2 * Math.PI * frequencia * tempo);
                double desafinada = Math.sin(2 * Math.PI * frequencia * 1.003 * tempo + voz);
                double triangular = ondaTriangular(frequencia * 0.5, tempo);
                valor += peso * (0.55 * sinusoide + 0.3 * desafinada + 0.15 * triangular);
            }
            double respiracao = 0.85 + 0.15 * Math.sin(2 * Math.PI * 0.25 * tempo);
            int destino = Math.floorMod(deslocamento + i, mistura.length);
            mistura[destino] += valor * envelope * respiracao;
        }
    }

    private static void adicionarArpejo(double[] mistura, double inicioAcorde, double[] frequencias) {
        int[] ordem = {2, 3, 4, 3};
        double duracaoNota = DURACAO_ACORDE / ordem.length;
        for (int passo = 0; passo < ordem.length; passo++) {
            double frequencia = frequencias[ordem[passo]] * 2;
            int deslocamento = (int) ((inicioAcorde + passo * duracaoNota) * TAXA_AMOSTRAGEM);
            int quantidade = (int) (duracaoNota * 1.8 * TAXA_AMOSTRAGEM);
            int ataque = (int) (0.02 * TAXA_AMOSTRAGEM);
            for (int i = 0; i < quantidade; i++) {
                double tempo = i / TAXA_AMOSTRAGEM;
                double subida = i < ataque ? (double) i / ataque : 1.0;
                double descida = Math.min(1.0, (quantidade - i) / (0.3 * TAXA_AMOSTRAGEM));
                double envelope = subida * descida * Math.exp(-tempo * 1.8);
                double valor = 0.09 * envelope * Math.sin(2 * Math.PI * frequencia * tempo);
                int destino = Math.floorMod(deslocamento + i, mistura.length);
                mistura[destino] += valor;
            }
        }
    }

    private static double envelopeSuave(int indice, int quantidade, int transicao) {
        if (indice < transicao) {
            return 0.5 - 0.5 * Math.cos(Math.PI * indice / transicao);
        }
        int restante = quantidade - indice;
        if (restante < transicao) {
            return 0.5 - 0.5 * Math.cos(Math.PI * restante / transicao);
        }
        return 1.0;
    }

    private static double ondaTriangular(double frequencia, double tempo) {
        double fase = (tempo * frequencia) % 1.0;
        return 4.0 * Math.abs(fase - 0.5) - 1.0;
    }

    private static byte[] paraBytes(short[] amostras, double fator) {
        byte[] bytes = new byte[amostras.length * 2];
        for (int i = 0; i < amostras.length; i++) {
            int valor = (int) Math.round(amostras[i] * fator);
            bytes[2 * i] = (byte) (valor & 0xFF);
            bytes[2 * i + 1] = (byte) ((valor >> 8) & 0xFF);
        }
        return bytes;
    }
}

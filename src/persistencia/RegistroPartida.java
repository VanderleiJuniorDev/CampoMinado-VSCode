package persistencia;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RegistroPartida {

    private static final String SEPARADOR = ";";
    private static final String TEXTO_VITORIA = "VITORIA";
    private static final String TEXTO_DERROTA = "DERROTA";
    private static final int QUANTIDADE_CAMPOS = 10;
    private static final char MARCA_BOM = '﻿';

    private final LocalDateTime dataHora;
    private final String dificuldade;
    private final boolean vitoria;
    private final long segundos;
    private final int jogadas;
    private final int bandeirasUsadas;
    private final int dicasUsadas;
    private final int chordsUsados;
    private final int vidasPerdidas;
    private final String modos;

    public RegistroPartida(LocalDateTime dataHora, String dificuldade, boolean vitoria, long segundos, int jogadas,
            int bandeirasUsadas, int dicasUsadas, int chordsUsados, int vidasPerdidas, String modos) {
        this.dataHora = dataHora == null ? LocalDateTime.now() : dataHora;
        this.dificuldade = limparTexto(dificuldade);
        this.vitoria = vitoria;
        this.segundos = segundos;
        this.jogadas = jogadas;
        this.bandeirasUsadas = bandeirasUsadas;
        this.dicasUsadas = dicasUsadas;
        this.chordsUsados = chordsUsados;
        this.vidasPerdidas = vidasPerdidas;
        this.modos = limparTexto(modos);
    }

    private static String limparTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(';', ' ').replace('\r', ' ').replace('\n', ' ').trim();
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public boolean isVitoria() {
        return vitoria;
    }

    public long getSegundos() {
        return segundos;
    }

    public int getJogadas() {
        return jogadas;
    }

    public int getBandeirasUsadas() {
        return bandeirasUsadas;
    }

    public int getDicasUsadas() {
        return dicasUsadas;
    }

    public int getChordsUsados() {
        return chordsUsados;
    }

    public int getVidasPerdidas() {
        return vidasPerdidas;
    }

    public String getModos() {
        return modos;
    }

    public boolean temModo(String modo) {
        if (modo == null || modos.isEmpty()) {
            return false;
        }
        String procurado = modo.trim();
        for (String etiqueta : modos.split(",")) {
            if (etiqueta.trim().equalsIgnoreCase(procurado)) {
                return true;
            }
        }
        return false;
    }

    public String paraLinhaCsv() {
        StringBuilder linha = new StringBuilder();
        linha.append(dataHora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(SEPARADOR);
        linha.append(dificuldade).append(SEPARADOR);
        linha.append(vitoria ? TEXTO_VITORIA : TEXTO_DERROTA).append(SEPARADOR);
        linha.append(segundos).append(SEPARADOR);
        linha.append(jogadas).append(SEPARADOR);
        linha.append(bandeirasUsadas).append(SEPARADOR);
        linha.append(dicasUsadas).append(SEPARADOR);
        linha.append(chordsUsados).append(SEPARADOR);
        linha.append(vidasPerdidas).append(SEPARADOR);
        linha.append(modos);
        return linha.toString();
    }

    public static RegistroPartida deLinhaCsv(String linha) {
        if (linha == null) {
            return null;
        }
        String conteudo = linha;
        if (!conteudo.isEmpty() && conteudo.charAt(0) == MARCA_BOM) {
            conteudo = conteudo.substring(1);
        }
        conteudo = conteudo.replace("\r", "").replace("\n", "");
        if (conteudo.trim().isEmpty()) {
            return null;
        }
        String[] campos = conteudo.split(SEPARADOR, -1);
        if (campos.length != QUANTIDADE_CAMPOS && campos.length != QUANTIDADE_CAMPOS - 1) {
            return null;
        }
        try {
            LocalDateTime dataHora = LocalDateTime.parse(campos[0].trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String dificuldade = campos[1].trim();
            String resultado = campos[2].trim();
            boolean vitoria;
            if (TEXTO_VITORIA.equalsIgnoreCase(resultado)) {
                vitoria = true;
            } else if (TEXTO_DERROTA.equalsIgnoreCase(resultado)) {
                vitoria = false;
            } else {
                return null;
            }
            long segundos = Long.parseLong(campos[3].trim());
            int jogadas = Integer.parseInt(campos[4].trim());
            int bandeiras = Integer.parseInt(campos[5].trim());
            int dicas = Integer.parseInt(campos[6].trim());
            int chords = Integer.parseInt(campos[7].trim());
            int vidasPerdidas = Integer.parseInt(campos[8].trim());
            String modos = campos.length == QUANTIDADE_CAMPOS ? campos[9] : "";
            return new RegistroPartida(dataHora, dificuldade, vitoria, segundos, jogadas, bandeiras, dicas, chords,
                    vidasPerdidas, modos);
        } catch (DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }

    public static String cabecalhoCsv() {
        return "data_hora;dificuldade;resultado;segundos;jogadas;bandeiras;dicas;chords;vidas_perdidas;modos";
    }

    public String getTempoFormatado() {
        long total = Math.max(0L, segundos);
        long minutos = total / 60;
        long restoSegundos = total % 60;
        return String.format("%02d:%02d", minutos, restoSegundos);
    }

    @Override
    public String toString() {
        return paraLinhaCsv();
    }
}

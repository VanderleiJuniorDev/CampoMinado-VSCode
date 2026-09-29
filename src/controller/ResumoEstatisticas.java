package controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResumoEstatisticas {

    private String nomePerfil = "";
    private int totalPartidas;
    private int vitorias;
    private int derrotas;
    private double taxaVitoria;
    private double taxaUltimasDez;
    private int sequenciaAtual;
    private int melhorSequencia;
    private final List<Double> evolucaoTaxa = new ArrayList<>();
    private final List<String[]> recordes = new ArrayList<>();
    private final Map<String, List<String[]>> ranking = new LinkedHashMap<>();
    private final List<String[]> historico = new ArrayList<>();
    private final List<String[]> conquistas = new ArrayList<>();

    public String getNomePerfil() {
        return nomePerfil;
    }

    public void setNomePerfil(String nomePerfil) {
        this.nomePerfil = nomePerfil;
    }

    public int getTotalPartidas() {
        return totalPartidas;
    }

    public void setTotalPartidas(int totalPartidas) {
        this.totalPartidas = totalPartidas;
    }

    public int getVitorias() {
        return vitorias;
    }

    public void setVitorias(int vitorias) {
        this.vitorias = vitorias;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public void setDerrotas(int derrotas) {
        this.derrotas = derrotas;
    }

    public double getTaxaVitoria() {
        return taxaVitoria;
    }

    public void setTaxaVitoria(double taxaVitoria) {
        this.taxaVitoria = taxaVitoria;
    }

    public double getTaxaUltimasDez() {
        return taxaUltimasDez;
    }

    public void setTaxaUltimasDez(double taxaUltimasDez) {
        this.taxaUltimasDez = taxaUltimasDez;
    }

    public int getSequenciaAtual() {
        return sequenciaAtual;
    }

    public void setSequenciaAtual(int sequenciaAtual) {
        this.sequenciaAtual = sequenciaAtual;
    }

    public int getMelhorSequencia() {
        return melhorSequencia;
    }

    public void setMelhorSequencia(int melhorSequencia) {
        this.melhorSequencia = melhorSequencia;
    }

    public List<Double> getEvolucaoTaxa() {
        return evolucaoTaxa;
    }

    public List<String[]> getRecordes() {
        return recordes;
    }

    public Map<String, List<String[]>> getRanking() {
        return ranking;
    }

    public List<String[]> getHistorico() {
        return historico;
    }

    public List<String[]> getConquistas() {
        return conquistas;
    }
}

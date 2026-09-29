package controller;

import java.io.File;

public interface AcoesJogador {

    void aoEscolherDificuldade(String nomeDificuldade, int linhas, int colunas, int minas);

    void aoPedirDesafioDiario();

    void aoRevelarCelula(int linha, int coluna);

    void aoMarcarCelula(int linha, int coluna);

    void aoFazerChording(int linha, int coluna);

    void aoPedirDica();

    void aoPedirNovoJogo();

    void aoJogarNovamente();

    void aoIniciarSegundoJogadorDuelo();

    void aoPedirReplay();

    void aoPararReplay();

    void aoPedirCompartilhar();

    void aoPedirEstatisticas();

    void aoExportarCsv(File destino);

    void aoSelecionarPerfil(String nome);

    void aoCriarPerfil(String nome);

    void aoTrocarTema(String nomeTema);

    void aoAlternarSom();

    void aoAlterarZoom(double zoom);

    void aoSalvarConfiguracoes(DadosConfiguracao dados);

    void aoFecharAplicacao();
}

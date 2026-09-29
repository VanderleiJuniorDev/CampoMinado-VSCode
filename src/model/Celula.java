package model;

public class Celula {

    private boolean minada;
    private boolean revelada;
    private boolean explodida;
    private EstadoMarcacao estadoMarcacao;
    private int minasVizinhas;

    public Celula() {
        this.minada = false;
        this.revelada = false;
        this.explodida = false;
        this.estadoMarcacao = EstadoMarcacao.NENHUMA;
        this.minasVizinhas = 0;
    }

    public boolean isMinada() {
        return minada;
    }

    public void setMinada(boolean minada) {
        this.minada = minada;
    }

    public boolean isRevelada() {
        return revelada;
    }

    public void revelar() {
        if (estadoMarcacao == EstadoMarcacao.BANDEIRA) {
            return;
        }
        this.estadoMarcacao = EstadoMarcacao.NENHUMA;
        this.revelada = true;
    }

    public EstadoMarcacao getEstadoMarcacao() {
        return estadoMarcacao;
    }

    public boolean isMarcada() {
        return estadoMarcacao == EstadoMarcacao.BANDEIRA;
    }

    public boolean isInterrogacao() {
        return estadoMarcacao == EstadoMarcacao.INTERROGACAO;
    }

    public void alternarMarcacao(boolean permitirInterrogacao) {
        if (revelada) {
            return;
        }
        if (estadoMarcacao == EstadoMarcacao.NENHUMA) {
            estadoMarcacao = EstadoMarcacao.BANDEIRA;
        } else if (estadoMarcacao == EstadoMarcacao.BANDEIRA && permitirInterrogacao) {
            estadoMarcacao = EstadoMarcacao.INTERROGACAO;
        } else {
            estadoMarcacao = EstadoMarcacao.NENHUMA;
        }
    }

    public void alternarMarcacao() {
        alternarMarcacao(false);
    }

    public boolean isExplodida() {
        return explodida;
    }

    public void marcarExplodida() {
        this.explodida = true;
    }

    public int getMinasVizinhas() {
        return minasVizinhas;
    }

    public void setMinasVizinhas(int minasVizinhas) {
        this.minasVizinhas = minasVizinhas;
    }

    @Override
    public String toString() {
        if (estadoMarcacao == EstadoMarcacao.BANDEIRA) {
            return "F";
        }
        if (estadoMarcacao == EstadoMarcacao.INTERROGACAO) {
            return "?";
        }
        if (!revelada) {
            return ".";
        }
        if (minada) {
            return "*";
        }
        if (minasVizinhas == 0) {
            return " ";
        }
        return String.valueOf(minasVizinhas);
    }
}

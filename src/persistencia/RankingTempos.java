package persistencia;

import java.util.ArrayList;
import java.util.List;

public final class RankingTempos {

    private RankingTempos() {
    }

    public static void ordenarPorTempo(List<RegistroPartida> registros) {
        if (registros == null || registros.size() < 2) {
            return;
        }
        RegistroPartida[] elementos = new RegistroPartida[registros.size()];
        for (int indice = 0; indice < elementos.length; indice++) {
            elementos[indice] = registros.get(indice);
        }
        RegistroPartida[] auxiliar = new RegistroPartida[elementos.length];
        mergeSort(elementos, auxiliar, 0, elementos.length - 1);
        for (int indice = 0; indice < elementos.length; indice++) {
            registros.set(indice, elementos[indice]);
        }
    }

    private static void mergeSort(RegistroPartida[] elementos, RegistroPartida[] auxiliar, int inicio, int fim) {
        if (inicio >= fim) {
            return;
        }
        int meio = inicio + (fim - inicio) / 2;
        mergeSort(elementos, auxiliar, inicio, meio);
        mergeSort(elementos, auxiliar, meio + 1, fim);
        intercalar(elementos, auxiliar, inicio, meio, fim);
    }

    private static void intercalar(RegistroPartida[] elementos, RegistroPartida[] auxiliar, int inicio, int meio,
            int fim) {
        for (int indice = inicio; indice <= fim; indice++) {
            auxiliar[indice] = elementos[indice];
        }
        int esquerda = inicio;
        int direita = meio + 1;
        int destino = inicio;
        while (esquerda <= meio && direita <= fim) {
            if (vemDepois(auxiliar[esquerda], auxiliar[direita])) {
                elementos[destino++] = auxiliar[direita++];
            } else {
                elementos[destino++] = auxiliar[esquerda++];
            }
        }
        while (esquerda <= meio) {
            elementos[destino++] = auxiliar[esquerda++];
        }
        while (direita <= fim) {
            elementos[destino++] = auxiliar[direita++];
        }
    }

    private static boolean vemDepois(RegistroPartida primeiro, RegistroPartida segundo) {
        if (primeiro.getSegundos() != segundo.getSegundos()) {
            return primeiro.getSegundos() > segundo.getSegundos();
        }
        return primeiro.getJogadas() > segundo.getJogadas();
    }

    public static List<RegistroPartida> topTempos(List<RegistroPartida> registros, String dificuldade, int limite) {
        List<RegistroPartida> filtrados = new ArrayList<>();
        if (registros == null || dificuldade == null || limite <= 0) {
            return filtrados;
        }
        for (RegistroPartida registro : registros) {
            if (registro != null && registro.isVitoria() && registro.getDificuldade().equalsIgnoreCase(dificuldade)) {
                filtrados.add(registro);
            }
        }
        ordenarPorTempo(filtrados);
        List<RegistroPartida> melhores = new ArrayList<>();
        for (int indice = 0; indice < filtrados.size() && indice < limite; indice++) {
            melhores.add(filtrados.get(indice));
        }
        return melhores;
    }
}

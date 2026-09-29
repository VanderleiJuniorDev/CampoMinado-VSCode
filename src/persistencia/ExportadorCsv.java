package persistencia;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ExportadorCsv {

    private static final char MARCA_BOM = '﻿';
    private static final String QUEBRA_LINHA = "\r\n";

    private ExportadorCsv() {
    }

    public static void exportar(List<RegistroPartida> registros, Path destino) throws IOException {
        if (destino == null) {
            throw new IOException("Destino não informado");
        }
        Path pai = destino.toAbsolutePath().getParent();
        if (pai != null) {
            Files.createDirectories(pai);
        }
        try (BufferedWriter escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            escritor.write(MARCA_BOM);
            escritor.write(RegistroPartida.cabecalhoCsv());
            escritor.write(QUEBRA_LINHA);
            if (registros != null) {
                for (RegistroPartida registro : registros) {
                    if (registro != null) {
                        escritor.write(registro.paraLinhaCsv());
                        escritor.write(QUEBRA_LINHA);
                    }
                }
            }
        }
    }
}

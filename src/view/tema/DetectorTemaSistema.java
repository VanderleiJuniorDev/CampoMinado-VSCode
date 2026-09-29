package view.tema;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DetectorTemaSistema {

    private static final long TEMPO_LIMITE_SEGUNDOS = 2;

    private static volatile Boolean modoEscuroEmCache;

    private DetectorTemaSistema() {
    }

    public static boolean sistemaEmModoEscuro() {
        Boolean emCache = modoEscuroEmCache;
        if (emCache != null) {
            return emCache;
        }
        synchronized (DetectorTemaSistema.class) {
            if (modoEscuroEmCache == null) {
                modoEscuroEmCache = detectar();
            }
            return modoEscuroEmCache;
        }
    }

    public static String descreverSistema() {
        String modo = sistemaEmModoEscuro() ? "escuro" : "claro";
        return nomeSistema() + " (" + modo + ")";
    }

    private static String nomeSistemaOperacionalBruto() {
        try {
            String nome = System.getProperty("os.name");
            return nome == null ? "" : nome.toLowerCase(Locale.ROOT);
        } catch (SecurityException e) {
            return "";
        }
    }

    private static String nomeSistema() {
        String bruto = nomeSistemaOperacionalBruto();
        if (bruto.contains("win")) {
            return "Windows";
        }
        if (bruto.contains("mac") || bruto.contains("darwin")) {
            return "macOS";
        }
        if (bruto.contains("nux") || bruto.contains("nix") || bruto.contains("bsd")) {
            return "Linux";
        }
        return "Sistema desconhecido";
    }

    private static boolean detectar() {
        try {
            String sistema = nomeSistema();
            if ("Windows".equals(sistema)) {
                return detectarWindows();
            }
            if ("macOS".equals(sistema)) {
                return detectarMac();
            }
            if ("Linux".equals(sistema)) {
                return detectarLinux();
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean detectarWindows() {
        ResultadoComando resultado = executar(Arrays.asList("reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme"));
        if (resultado == null) {
            return true;
        }
        String saida = resultado.saida.toLowerCase(Locale.ROOT);
        if (saida.contains("0x1")) {
            return false;
        }
        return true;
    }

    private static boolean detectarMac() {
        ResultadoComando resultado = executar(Arrays.asList("defaults", "read", "-g", "AppleInterfaceStyle"));
        if (resultado == null) {
            return true;
        }
        if (resultado.codigoSaida != 0) {
            return false;
        }
        return resultado.saida.contains("Dark");
    }

    private static boolean detectarLinux() {
        ResultadoComando esquema = executar(Arrays.asList("gsettings", "get",
                "org.gnome.desktop.interface", "color-scheme"));
        if (esquema != null && esquema.codigoSaida == 0) {
            String saida = esquema.saida.toLowerCase(Locale.ROOT);
            if (saida.contains("dark")) {
                return true;
            }
            if (saida.contains("light")) {
                return false;
            }
        }
        ResultadoComando temaGtk = executar(Arrays.asList("gsettings", "get",
                "org.gnome.desktop.interface", "gtk-theme"));
        if (temaGtk != null && temaGtk.codigoSaida == 0) {
            return temaGtk.saida.toLowerCase(Locale.ROOT).contains("dark");
        }
        return true;
    }

    private static ResultadoComando executar(List<String> comando) {
        Process processo = null;
        try {
            ProcessBuilder construtor = new ProcessBuilder(comando);
            construtor.redirectErrorStream(true);
            processo = construtor.start();
            processo.getOutputStream().close();
            final Process processoAtual = processo;
            final ByteArrayOutputStream acumulador = new ByteArrayOutputStream();
            Thread leitor = new Thread(new Runnable() {
                @Override
                public void run() {
                    lerTudo(processoAtual.getInputStream(), acumulador);
                }
            }, "detector-tema-leitor");
            leitor.setDaemon(true);
            leitor.start();
            boolean terminou = processo.waitFor(TEMPO_LIMITE_SEGUNDOS, TimeUnit.SECONDS);
            if (!terminou) {
                processo.destroyForcibly();
                return null;
            }
            leitor.join(500);
            String saida;
            synchronized (acumulador) {
                saida = new String(acumulador.toByteArray(), StandardCharsets.UTF_8);
            }
            return new ResultadoComando(processo.exitValue(), saida);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            destruir(processo);
            return null;
        } catch (Exception e) {
            destruir(processo);
            return null;
        }
    }

    private static void lerTudo(InputStream entrada, ByteArrayOutputStream acumulador) {
        byte[] buffer = new byte[1024];
        try {
            int lidos;
            while ((lidos = entrada.read(buffer)) != -1) {
                synchronized (acumulador) {
                    acumulador.write(buffer, 0, lidos);
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                entrada.close();
            } catch (Exception e) {
            }
        }
    }

    private static void destruir(Process processo) {
        if (processo != null) {
            try {
                processo.destroyForcibly();
            } catch (Exception e) {
            }
        }
    }

    private static final class ResultadoComando {
        private final int codigoSaida;
        private final String saida;

        private ResultadoComando(int codigoSaida, String saida) {
            this.codigoSaida = codigoSaida;
            this.saida = saida;
        }
    }
}

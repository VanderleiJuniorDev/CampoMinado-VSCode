# 💣 Campo Minado — Versão 2.0 (MVC em Java Swing)

Projeto do Campo Minado em Java (Swing), organizado na arquitetura **MVC** e preparado para abrir direto no **Visual Studio Code**.

Esta versão implementa **todas as sugestões de dificuldade 🟢 fácil e 🟡 média** do documento *"50 sugestões de implementação — Campo Minado"*. Também implementa **uma sugestão 🔴 desafiadora: a nº 21, "Chording"**.

---

## Sumário

1. [Como abrir e rodar no VS Code](#1-como-abrir-e-rodar-no-vs-code)
2. [Estrutura do projeto](#2-estrutura-do-projeto)
3. [Resumo das alterações](#3-resumo-das-alterações)
4. [Alterações 🟢 fáceis (21 itens)](#4-alterações--fáceis-21-itens)
5. [Alterações 🟡 médias (21 itens)](#5-alterações--médias-21-itens)
6. [Alteração 🔴 desafiadora escolhida: nº 21 — Chording](#6-alteração--desafiadora-escolhida-nº-21--chording)
7. [Correções no código original](#7-correções-no-código-original)
8. [Testes unitários](#8-testes-unitários)
9. [Controles do jogo](#9-controles-do-jogo)
10. [Onde os dados ficam salvos](#10-onde-os-dados-ficam-salvos)

---

## 1. Como abrir e rodar no VS Code

**Pré-requisitos**

- JDK 11 ou superior (recomendado JDK 17 ou 21).
- VS Code com a extensão **Extension Pack for Java** (`vscjava.vscode-java-pack`). O próprio VS Code sugere instalá-la ao abrir a pasta.

**Passo a passo**

1. No VS Code, use **File → Open Folder…** e selecione a pasta `CampoMinado-VSCode`.
2. Aguarde a extensão Java carregar o projeto (canto inferior direito).
3. Abra a aba **Run and Debug** (Ctrl+Shift+D) e escolha uma opção:
   - **Campo Minado (Interface Gráfica)**: o jogo completo em Swing;
   - **Campo Minado (Console)**: a versão de terminal.
4. Pressione **F5**.

Outra forma: **Terminal → Run Task… → "Executar jogo"**. A tarefa compila tudo para `bin/` e abre o jogo.

**💡 Dica: rodar pelo terminal (sem o VS Code)**

Abra o terminal na pasta `CampoMinado-VSCode`. Primeiro compile (só é necessário na primeira vez e depois de alterar o código) e depois execute:

*Windows (PowerShell)*

```powershell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse src -Filter *.java | Where-Object { $_.FullName -notmatch '\\test\\' } | ForEach-Object { $_.FullName })
java "-Dfile.encoding=UTF-8" -cp bin main.JogoCampoMinadoGUI
```

> No PowerShell, mantenha as aspas em `"-Dfile.encoding=UTF-8"`. Sem elas, o PowerShell quebra o argumento no ponto e o Java mostra o erro *"Não foi possível localizar nem carregar a classe principal .encoding=UTF-8"*.

*Linux / macOS / WSL*

```bash
mkdir -p bin && javac -encoding UTF-8 -d bin $(find src -name '*.java' -not -path 'src/test/*')
java -Dfile.encoding=UTF-8 -cp bin main.JogoCampoMinadoGUI
```

Para jogar a versão de console, troque `main.JogoCampoMinadoGUI` por `main.JogoCampoMinado`.

**Arquivos de configuração do VS Code (`.vscode/`)**

| Arquivo | Função |
|---|---|
| `settings.json` | Define `src/` como pasta de código, `bin/` como saída, `lib/` para bibliotecas e UTF-8 como codificação |
| `launch.json` | As duas configurações de execução (GUI e Console) |
| `tasks.json` | Tarefas "Compilar jogo" (Ctrl+Shift+B) e "Executar jogo" (Windows, Linux e macOS) |
| `extensions.json` | Recomenda o Extension Pack for Java |

**Para rodar os testes (JUnit 5)**

Abra a aba **Testing** (ícone de béquer) e clique em **Enable Java Tests → JUnit Jupiter**. O VS Code baixa o JUnit para a pasta `lib/`. Depois é só clicar em ▶ ao lado de `CampoMinadoTest` ou de `PersistenciaTest`.

---

## 2. Estrutura do projeto

```
CampoMinado-VSCode/
├── .vscode/                     configuração do VS Code
├── lib/                         bibliotecas (JUnit, baixado pelo VS Code)
├── README.md
└── src/
    ├── model/                   MODEL: regras do jogo (não conhece Swing)
    │   ├── Celula.java
    │   ├── EstadoMarcacao.java          (NENHUMA, BANDEIRA, INTERROGACAO)
    │   ├── ConfiguracaoPartida.java     (modos: sem cascata, toroidal, vidas, ?, semente)
    │   ├── LeituraTabuleiro.java        (contrato somente-leitura usado pela View)
    │   └── Tabuleiro.java
    ├── controller/              CONTROLLER: liga Model e View
    │   ├── AcoesJogador.java            (eventos que a View dispara)
    │   ├── CampoMinadoController.java
    │   ├── OpcoesPartida.java / ModoJogadores.java
    │   ├── DadosConfiguracao.java / ResumoEstatisticas.java
    │   └── Jogada.java / TipoJogada.java (gravação para o replay)
    ├── view/                    VIEW: só desenha e captura interações
    │   ├── CampoMinadoView.java         (janela, telas, atalhos, notificações)
    │   ├── PainelTabuleiro.java         (grade responsiva, mouse, cursor de teclado)
    │   ├── BotaoCelula.java             (célula desenhada à mão com micro-animação)
    │   ├── PainelConfete.java           (partículas da vitória)
    │   ├── DialogoConfiguracoes.java / DialogoEstatisticas.java / DialogoPersonalizado.java
    │   ├── GraficoTaxa.java / EstiloUI.java / TipoMensagem.java
    │   └── tema/                        (TemaVisual, CatalogoTemas, DetectorTemaSistema,
    │                                     SkinBandeira, SkinCelula, SimbolosDaltonismo)
    ├── persistencia/            dados salvos em disco (usados pelo Controller)
    │   ├── Configuracoes.java, PerfilJogador.java, GerenciadorPerfis.java
    │   ├── RegistroPartida.java, HistoricoPartidas.java, Recordes.java
    │   ├── RankingTempos.java (merge sort manual), ExportadorCsv.java
    │   └── Conquista.java, GerenciadorConquistas.java
    ├── audio/                   sons gerados por código (javax.sound.sampled)
    │   ├── EfeitosSonoros.java
    │   └── MusicaAmbiente.java
    ├── main/
    │   ├── JogoCampoMinadoGUI.java      (ponto de entrada gráfico)
    │   └── JogoCampoMinado.java         (versão console)
    └── test/
        ├── CampoMinadoTest.java         (34 testes do Model)
        └── PersistenciaTest.java        (16 testes de persistência e ranking)
```

**Regras de MVC mantidas**

- O **Model** (`model/`) não importa nada de `java.awt`, `javax.swing` nem de outros pacotes do projeto.
- A **View** só enxerga o jogo pela interface `LeituraTabuleiro` (somente leitura). Ela nunca altera o Model; só avisa o Controller por meio de `AcoesJogador`.
- O **Controller** é o único que conhece Model, View, persistência e áudio ao mesmo tempo.
- O código **não tem nenhum comentário**; os nomes de classes, métodos e variáveis foram escolhidos para se explicarem sozinhos.

---

## 3. Resumo das alterações

| | Quantidade |
|---|---|
| Sugestões 🟢 implementadas | **21 de 21** |
| Sugestões 🟡 implementadas | **21 de 21** |
| Sugestão 🔴 implementada | **1** (nº 21 — Chording) |
| Arquivos `.java` | 45 (antes eram 9) |
| Testes unitários | 50 (antes eram 9), todos passando |
| Pacotes novos | `persistencia`, `audio`, `view.tema` |

---

## 4. Alterações 🟢 fáceis (21 itens)

| # | Sugestão | Como foi feito | Onde |
|---|---|---|---|
| 2 | Tema "terminal retrô" | Fundo preto, verde fósforo e fonte monoespaçada | `view/tema/CatalogoTemas` |
| 3 | Tema "cyberpunk neon" | Roxo escuro com rosa e ciano vibrantes | `CatalogoTemas` |
| 4 | Tema "Halloween" | Laranja, roxo e marrom | `CatalogoTemas` |
| 5 | Tema "oceano/tropical" | Azul profundo, coral e turquesa | `CatalogoTemas` |
| 6 | Tema "alto contraste" | Preto, branco e amarelo; o contraste de todos os temas foi medido pelo cálculo WCAG | `CatalogoTemas` |
| 8 | Skins de bandeira | Bandeira 🚩, Triângulo ▲, Estrela ★, Coração ♥ e Alfinete 📌 | `view/tema/SkinBandeira` |
| 9 | Skins de célula com emojis | Números, Frutas (🍒🍋🍇…) ou Animais (🐱🐶🐸…), com a legenda na tela | `view/tema/SkinCelula` |
| 12 | Modo "sem cascata" | Revela só a célula clicada, mesmo com zero minas vizinhas | `ConfiguracaoPartida`, `Tabuleiro.revelar` |
| 13 | Modo "minas visíveis" (treino) | As minas aparecem em transparência; a partida não conta para recordes nem conquistas | Controller + `PainelTabuleiro` |
| 14 | Dificuldade personalizada | Cartão "Personalizado" abre um diálogo de linhas, colunas e minas, que mostra a densidade | `DialogoPersonalizado` |
| 16 | Modo "relâmpago" | Revela algumas células seguras aleatórias (cerca de 8%) antes de começar, sem cascata e sem entregar a vitória | `Tabuleiro.revelarAleatorias` |
| 24 | Aviso de bandeiras em excesso | O contador de minas fica vermelho, ganha tooltip e aparece um alerta na barra de status | `CampoMinadoView.atualizarEstatisticas` |
| 25 | Símbolos para daltônicos | Cada número ganha uma forma no canto (● ▲ ■ ◆ ★ ✚ ✖ ⬢), além da cor | `view/tema/SimbolosDaltonismo` |
| 30 | Recorde por dificuldade | Melhor tempo salvo para cada dificuldade; "🏅 Novo recorde" aparece como notificação | `persistencia/Recordes` |
| 32 | Exportar estatísticas em CSV | Botão "📤 Exportar CSV" nas estatísticas; arquivo UTF-8 com BOM separado por `;`, que abre direto no Excel | `persistencia/ExportadorCsv` |
| 34 | Configurações persistentes | Tema, skins, som, música, volume, zoom, fonte, atalhos e perfil ficam salvos entre execuções | `persistencia/Configuracoes` |
| 35 | Efeitos sonoros | Clique, bandeira, explosão, vitória, dica, erro e conquista, todos sintetizados com `javax.sound.sampled` (sem arquivos de áudio) | `audio/EfeitosSonoros` |
| 37 | Micro-animação ao passar o mouse | A célula "levanta" com sombra e clareia suavemente no hover e "afunda" ao ser pressionada | `view/BotaoCelula` |
| 42 | Ranking com ordenação manual | Top 10 por dificuldade ordenado por um **merge sort escrito à mão** (estável, desempate por menos jogadas), sem `Collections.sort` | `persistencia/RankingTempos` |
| 44 | Compartilhar resultado | Gera um texto como "Terminei em 01:23 no modo Avançado! 🏆" e copia para a área de transferência | `Controller.aoPedirCompartilhar` |
| 47 | Modo tela cheia | Botão ⛶ ou tecla **F11**; **Esc** sai | `CampoMinadoView.alternarTelaCheia` |

---

## 5. Alterações 🟡 médias (21 itens)

| # | Sugestão | Como foi feito | Onde |
|---|---|---|---|
| 1 | Sistema de temas plugável | Classe imutável `TemaVisual` (Builder) com todas as cores e a fonte. O tema pode ser trocado **durante a partida**: a tela é reconstruída e o estado do jogo é mantido | `view/tema/TemaVisual`, `CatalogoTemas` |
| 7 | Detectar tema do sistema | Opção "Automático (sistema)": lê o registro no Windows, `defaults` no macOS e `gsettings` no Linux | `view/tema/DetectorTemaSistema` |
| 10 | Animação de "shake" | A tela treme (com amplitude diminuindo) e o fundo pisca ao clicar em uma mina | `CampoMinadoView.tremerTela` |
| 11 | Confete na vitória | Cerca de 180 partículas com gravidade, rotação e fade, nas cores do tema | `view/PainelConfete` |
| 15 | Cronômetro regressivo | 1, 2, 3, 5 ou 10 minutos; fica vermelho nos últimos 10 s; ao zerar, o jogador perde ("⏰ Tempo esgotado!") | Controller |
| 17 | Modo 3 vidas | Tolera 2 explosões (a mina vira 💥 e a tela treme); os corações ❤❤♡ aparecem no painel | `Tabuleiro.revelar`, Controller |
| 18 | Tabuleiro toroidal | As bordas se conectam; afeta a contagem de vizinhas e a cascata, sem duplicar vizinhos em tabuleiros pequenos | `Tabuleiro.getVizinhos` |
| 19 | Cooperativo local | Dois jogadores alternam as jogadas; o painel mostra de quem é a vez e, no fim, quanto cada um revelou | Controller |
| 20 | Marcação com "?" | Clique direito alterna bandeira → ? → vazio. Pode ser desligado nas configurações | `Celula`, `EstadoMarcacao` |
| 23 | Botão de dica | Revela uma célula segura, de preferência na fronteira do que já foi aberto. Limite de 3 por partida; a célula pisca | `Controller.aoPedirDica` |
| 26 | Navegação por teclado | Setas movem o cursor; Espaço/Enter revela (ou faz chording em número); F marca; C faz chording; H pede dica | `CampoMinadoView`, `PainelTabuleiro` |
| 28 | Zoom | Botões − / +, teclas − / +, ou **Ctrl + roda do mouse** (60% a 200%) | `PainelTabuleiro.setZoom` |
| 31 | Histórico de partidas | Toda partida é salva (data, dificuldade, resultado, tempo, jogadas, modos). As estatísticas mostram vitórias, derrotas, taxa de acerto, últimas 10, sequências e **um gráfico da taxa de acerto ao longo do tempo** | `HistoricoPartidas`, `DialogoEstatisticas`, `GraficoTaxa` |
| 33 | Perfis de jogador | Seletor de perfil e botão "+ Novo perfil" na tela inicial; cada perfil tem histórico, recordes e conquistas próprios | `GerenciadorPerfis`, `PerfilJogador` |
| 36 | Música ambiente com volume | Loop procedural de ~16 s (acordes Am–F–C–G) gerado em código, com liga/desliga e volume de 0 a 100 | `audio/MusicaAmbiente` |
| 41 | Modo replay | Cada jogada é gravada; "▶ Assistir replay" reproduz a partida no mesmo tabuleiro, jogada a jogada, com "⏹ Parar replay" | Controller (`Jogada`, `TipoJogada`) |
| 43 | Dois jogadores competindo | Modo Duelo: o Jogador 1 joga e depois o Jogador 2 recebe **o mesmo tabuleiro** (mesma semente). Vence quem ganhar; se os dois ganharem, vence o menor tempo; se os dois perderem, vence quem revelou mais | Controller |
| 46 | Painel de configurações | Tema, skins, **tamanho da fonte**, zoom, **densidade de minas padrão**, "?", daltônico, som, música, volume e **troca de atalhos de teclado** (clique e pressione a nova tecla) | `view/DialogoConfiguracoes` |
| 48 | Tabuleiro responsivo | Layout próprio (`LayoutManager`) que mantém as células quadradas e as redimensiona conforme o tamanho da janela | `PainelTabuleiro.LayoutGrade` |
| 49 | Modo diário | Cartão "Desafio diário": semente derivada da data (16×16, 40 minas), então todo mundo joga o mesmo tabuleiro no mesmo dia | `Controller.aoPedirDesafioDiario` |
| 50 | Conquistas / badges | 12 conquistas (Primeira vitória, Mãos limpas, Velocista <30 s, Veterano, Maratonista, Mestre do Avançado, Sem ajuda, Sobrevivente, Desafio diário, Maestro, Em chamas, Explorador), com notificação e aba própria nas estatísticas | `Conquista`, `GerenciadorConquistas` |

---

## 6. Alteração 🔴 desafiadora escolhida: nº 21 — Chording

**O que é:** quando o número de uma célula revelada já tem ao redor a mesma quantidade de bandeiras, um único comando revela **todos os outros vizinhos** de uma vez. É a técnica usada por jogadores avançados de Campo Minado.

**Por que foi escolhida:** mexe nas três camadas do MVC. O Model ganha a regra, o Controller recebe um novo evento e a View precisa detectar gestos de mouse mais complexos.

**Model (`Tabuleiro`)**

- `contarBandeirasVizinhas(l, c)` conta as bandeiras ao redor.
- `podeFazerChording(l, c)` confere cinco condições:
  - o jogo não acabou;
  - a célula está revelada;
  - a célula não é mina;
  - o número é maior que 0 e é igual à quantidade de bandeiras;
  - ainda existe algum vizinho fechado.
- `revelarVizinhos(l, c)` revela cada vizinho fechado e sem bandeira usando a regra normal de `revelar` (com cascata). Se o jogador marcou uma bandeira errada, uma mina explode normalmente, como no jogo original. No modo 3 vidas, uma mina que já explodiu conta como sinalizada.
- Funciona junto com o tabuleiro toroidal, porque usa `getVizinhos`.

**View (`PainelTabuleiro`)** — quatro formas de acionar:

1. clique com o **botão do meio**;
2. **duplo clique** esquerdo sobre um número;
3. **esquerdo + direito juntos** (dispara uma única vez, ao soltar o primeiro botão);
4. teclado: tecla **C**, ou **Espaço/Enter** sobre um número já revelado.

**Controller**

- `aoFazerChording` conta como jogada, entra no replay (`TipoJogada.CHORDING`) e toca o som.
- Se as bandeiras não batem, o jogador recebe um aviso ("Chording exige N bandeira(s) ao redor").
- A conquista **"Maestro"** é liberada ao usar chording 10 vezes em uma vitória.

**Testes:** chording com sucesso, chording recusado quando as bandeiras não batem e chording com bandeira errada que explode a mina.

---

## 7. Correções no código original

| Problema encontrado | Correção |
|---|---|
| `encerrarPorTempo()` criava **um tabuleiro novo** em vez de encerrar a partida, então o jogo continuava depois do tempo acabar | Novo método `Tabuleiro.encerrarPorTempo()`; a derrota por tempo agora bloqueia o jogo |
| O hover e a bandeira usavam cores constantes (`COR_CELULA_OCULTA`) e ignoravam o tema escolhido | Todas as cores vêm do `TemaVisual` ativo |
| O teste `testContagemDeMinasVizinhasComDuasMinasAdjacentes` esperava valores errados ((0,2)=2 e (2,2)=1; o correto é 1 e 0) | Valores corrigidos e novas verificações adicionadas |
| `CampoMinadoTest` não tinha `package` nem `import model.Tabuleiro` e não compilava no VS Code | Arquivo movido para o pacote `test` com os imports corretos |
| O tema só podia ser escolhido antes da partida | Troca de tema a qualquer momento (item 1) |
| O posicionamento de minas por sorteio repetido não permitia reproduzir um tabuleiro | Embaralhamento com `Random(semente)`, que é determinístico (base do duelo, do replay e do modo diário) |
| A janela tinha tamanho fixo (`setResizable(false)`) | Janela redimensionável e tabuleiro responsivo |
| Os construtores com minas fixas aceitavam posições repetidas ou fora do tabuleiro | Agora lançam `IllegalArgumentException` |

---

## 8. Testes unitários

- **`test/CampoMinadoTest.java`: 34 testes.** Cobrem:
  - os 9 testes originais, corrigidos;
  - sem cascata;
  - toroidal (contagem, cascata e vizinhos sem duplicação);
  - interrogação;
  - chording (3 cenários);
  - 3 vidas;
  - semente determinística;
  - relâmpago;
  - tempo esgotado;
  - validações;
  - replay;
  - contadores.
- **`test/PersistenciaTest.java`: 16 testes.** Cobrem:
  - merge sort manual (ordem, desempate e estabilidade);
  - top 10;
  - recordes;
  - histórico recarregado do disco;
  - CSV de ida e volta;
  - conquistas;
  - perfis;
  - configurações.

Resultado: **50 de 50 testes passando**. Além disso, o jogo foi testado de ponta a ponta em modo gráfico (todas as telas, temas, modos, replay, duelo e troca de tema no meio de animações) sem nenhuma exceção.

---

## 9. Controles do jogo

| Ação | Mouse | Teclado (padrão, configurável) |
|---|---|---|
| Revelar | Botão esquerdo | Espaço ou Enter |
| Bandeira / ? | Botão direito | F |
| Chording | Meio, duplo clique ou esquerdo+direito | C (ou Espaço sobre número) |
| Dica | Botão 💡 Dica | H |
| Mover cursor | — | Setas |
| Zoom | Ctrl + roda do mouse, botões − / + | − / + |
| Tela cheia | Botão ⛶ | F11 (Esc para sair) |

---

## 10. Onde os dados ficam salvos

Tudo fica em uma pasta `.campominado` dentro da pasta do usuário. No Windows, é `C:\Users\<usuário>\.campominado\`:

```
.campominado/
├── config.properties                 configurações (tema, som, atalhos, perfil atual…)
└── perfis/<perfil>/
    ├── nome.txt
    ├── historico.csv                 todas as partidas
    ├── recordes.properties           melhor tempo por dificuldade
    └── conquistas.txt                conquistas desbloqueadas
```

Se não for possível gravar no disco, o jogo continua funcionando normalmente, com os dados só na memória.

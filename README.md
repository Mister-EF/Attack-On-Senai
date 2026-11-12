# Attack on SENAI

Jogo **2D plataforma / boss rush** desenvolvido em **Java** com o framework **LibGDX** (backend desktop LWJGL3), criado como projeto do **SENAI Félix Guisard**.

O jogador atravessa uma fase vertical cheia de plataformas, espinhos e inimigos até enfrentar o chefe **O Ponteiro Perdido [C]**, inspirado na linguagem C. Pelo caminho, ele pode coletar power-ups temáticos de IA (Gemini, Claude e ChatGPT).

> **Status:** em desenvolvimento. Toda a arte é provisória (placeholders gerados por código) e o jogo não possui áudio.

---

## Sumário

- [Como executar](#como-executar)
- [Requisitos](#requisitos)
- [Controles](#controles)
- [Funcionalidades](#funcionalidades)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Arquitetura](#arquitetura)
- [Placeholders visuais](#placeholders-visuais)
- [Gerando um executável](#gerando-um-executável)
- [Roadmap](#roadmap)
- [Limitações conhecidas](#limitações-conhecidas)

---

## Como executar

Clone o repositório e, na pasta raiz do projeto, rode:

**Linux / macOS**

```bash
./gradlew lwjgl3:run
```

**Windows**

```bat
gradlew.bat lwjgl3:run
```

Na primeira execução, o Gradle baixa as dependências e, se necessário, o JDK 21 automaticamente. Depois disso, uma janela de **1280x720** abre no menu principal.

Outros comandos úteis:

| Comando | O que faz |
|---|---|
| `./gradlew build` | Compila todos os módulos |
| `./gradlew lwjgl3:jar` | Gera um JAR executável com todas as dependências |
| `./gradlew clean` | Remove os arquivos de build |

---

## Requisitos

- **JDK 21** (o projeto usa o toolchain do Gradle com resolução automática via Foojay)
- **Gradle 9.8.0** (já incluído pelo wrapper: não precisa instalar)
- Conexão com a internet na primeira build, para baixar as dependências
- **LibGDX 1.14.2** e **LWJGL 3.4.3** (configurados em `gradle.properties`)

---

## Controles

### Menu principal

| Ação | Como |
|---|---|
| Selecionar GO / PONTUAÇÃO / SAIR | Clique do mouse |
| Alternar tela cheia | Botão no canto superior direito |
| Exibir os cheats | Botão secreto invisível no canto superior esquerdo (fica verde ao passar o mouse) |

### Dentro do jogo

| Tecla | Ação |
|---|---|
| `A` / `←` | Andar para a esquerda |
| `D` / `→` | Andar para a direita |
| `Espaço` / `W` / `↑` | Pular |
| `J` | Ataque 1 (rápido) |
| `K` | Ataque 2 (médio) |
| `L` | Ataque 3 (pesado) |
| `R` | Reiniciar a fase |
| `ESC` | Abrir/fechar o menu de pausa (Recomeçar / Sair) |

### Ataques

| Ataque | Tecla | Dano | Alcance | Cooldown |
|---|---|---|---|---|
| 1 | `J` | 15 | 42 px | 0,35 s |
| 2 | `K` | 28 | 65 px | 0,80 s |
| 3 | `L` | 50 | 95 px | 2,20 s |

### Cheats (para demonstração)

Os cheats ficam ocultos por padrão. Para ativá-los, clique no **botão secreto** no canto superior esquerdo do menu principal. Depois disso, durante o jogo:

| Tecla | Efeito |
|---|---|
| `F1` | Alterna o Modo Deus (invencibilidade) |
| `F2` | Instakill no chefe |
| `F3` | Gera um power-up de IA na posição do jogador |
| `F4` | Força a vitória |

---

## Funcionalidades

- **Menu principal** com as opções GO, PONTUAÇÃO (leaderboard das últimas 5 partidas) e SAIR, além de botão de tela cheia/janela.
- **Fase de plataforma vertical** (mundo de 4200x900) com 6 plataformas em alturas diferentes e uma faixa de espinhos no chão que causa dano.
- **Inimigos básicos (minions)** que patrulham estritamente os limites da plataforma onde nasceram, sem aggro e sem cair do cenário.
- **Chefe O Ponteiro Perdido [C]** (200 HP) com máquina de estados:
  - `IDLE` (2,0 s) → `PREPARING` (0,4 s, fica vermelho como aviso) → `DASHING` (0,75 s, investida veloz) → `RECOVERING` (0,6 s)
  - Dispara projéteis amarelos mirados no jogador a cada 1,8 s.
- **Power-ups de IA** que flutuam na fase:

  | Power-up | Efeito | Duração |
  |---|---|---|
  | **Gemini** | Escudo de invulnerabilidade | 10 s |
  | **Claude** | Foco temporal (slow-motion) | 8 s |
  | **ChatGPT** | Dano dobrado e +45% de velocidade | 8 s |

- **Sistema de vida** com 100 HP para o jogador, invulnerabilidade temporária após dano e knockback.
- **Parallax background**, câmera ortográfica que segue o jogador e **HUD** com barras de vida, cooldowns e buffs ativos.
- **Menu de pausa** e **tela de vitória**.

---

## Estrutura de pastas

```
Attack-On-Senai/
├── Agents.md                  # Regras e diretrizes para o agente de IA
├── build.gradle               # Configuração raiz do Gradle
├── settings.gradle            # Módulos: core e lwjgl3
├── gradle.properties          # Versões (LibGDX, LWJGL) e opções do Gradle
├── gradlew / gradlew.bat      # Gradle wrapper (Linux/macOS e Windows)
├── assets/                    # Recursos do jogo (a arte atual é gerada por código)
│
├── core/                      # Lógica do jogo (independente de plataforma)
│   ├── build.gradle
│   └── src/main/java/io/github/AOS/
│       ├── Main.java                  # Classe principal (Game): inicia o menu
│       ├── combat/
│       │   └── CombatSystem.java      # Colisões de ataque, investida e projéteis
│       ├── entities/
│       │   ├── Player.java            # Jogador: física, ataques, vida e buffs
│       │   ├── Minion.java            # Inimigo básico de patrulha
│       │   ├── PowerUp.java           # Power-ups de IA (Gemini, Claude, ChatGPT)
│       │   ├── BossProjectile.java    # Projétil amarelo do chefe
│       │   └── bosses/
│       │       ├── Boss.java          # Classe base abstrata dos chefes
│       │       └── BossC.java         # Chefe "O Ponteiro Perdido [C]"
│       ├── input/
│       │   ├── MenuInputHandler.java  # Entrada (mouse) do menu principal
│       │   └── PlayerInputHandler.java# Entrada (teclado/mouse) do jogo e da pausa
│       ├── screens/
│       │   ├── MainMenuScreen.java    # Menu principal e leaderboard
│       │   └── BossArenaScreen.java   # Fase, HUD, pausa e vitória
│       ├── systems/
│       │   └── CheatManager.java      # Cheats de demonstração (F1-F4)
│       ├── utils/
│       │   └── PlaceholderFactory.java# Gera texturas via Pixmap
│       └── world/
│           ├── Platform.java          # Plataformas sólidas
│           ├── Hazard.java            # Espinhos / obstáculos letais
│           └── ParallaxBackground.java# Fundo com efeito de profundidade
│
└── lwjgl3/                    # Lançador desktop (LWJGL3)
    ├── build.gradle           # Tarefas run, jar e empacotamento (Construo)
    ├── nativeimage.gradle
    └── src/main/java/io/github/AOS/lwjgl3/
        ├── Lwjgl3Launcher.java    # Ponto de entrada: janela 1280x720, vsync
        └── StartupHelper.java     # Suporte a macOS/Windows na inicialização da JVM
```

---

## Arquitetura

- **Pacote raiz:** `io.github.AOS`
- **Separação de responsabilidades:** telas (`Screen`), entrada dedicada (`InputAdapter`), sistemas (combate e cheats) e entidades independentes.
- **Máquina de estados** para o comportamento dos chefes.
- **Sem Box2D:** colisões feitas com `Rectangle` e `Intersector` nativos do LibGDX.
- **Câmera:** `OrthographicCamera` + `FitViewport` com resolução virtual de **1280x720**, mantendo a proporção em qualquer tamanho de janela.
- **Sem áudio:** nenhum `Sound` ou `Music` é carregado.

---

## Placeholders visuais

Enquanto os sprites finais não ficam prontos, todas as texturas são geradas em tempo de execução com `Pixmap` (pela `PlaceholderFactory`):

| Elemento | Cor |
|---|---|
| Jogador | Verde |
| Inimigos comuns (minions) | Vermelho |
| Chefes | Roxo |
| Chão, plataformas e sólidos | Cinza claro |
| Fundo / cenário | Preto |
| Ataques do jogador e projéteis do chefe | Amarelo |

Para trocar por sprites, basta substituir a geração em `PlaceholderFactory` pelo carregamento de PNGs da pasta `assets/`.

---

## Gerando um executável

JAR único (multiplataforma), gerado em `lwjgl3/build/libs/`:

```bash
./gradlew lwjgl3:jar
java -jar "lwjgl3/build/libs/Attack on Senai-1.0.0.jar"
```

Variantes menores por sistema operacional: `lwjgl3:jarWin`, `lwjgl3:jarLinux` e `lwjgl3:jarMac`.

O módulo `lwjgl3` também está configurado com o plugin **Construo** para empacotar o jogo com JRE embutido (Linux x64, Windows x64 e macOS Intel/Apple Silicon).

---

## Roadmap

- [x] Menu principal (GO, Pontuação, Sair) e botão de tela cheia
- [x] Movimentação de plataforma e parallax
- [x] Ataques com cooldowns diferentes
- [x] Fase vertical com minions e espinhos
- [x] Chefe com investida e projéteis
- [x] Power-ups de IA e cheats de demonstração
- [x] Menu de pausa e tela de vitória
- [x] Seleção de dificuldade (Fácil, Médio, Difícil)
- [x] Sprites e animações finais
- [ ] Mais ataques para o chefe
- [ ] Chefes adicionais
- [x] Leaderboard com pontuações reais

---

## Limitações conhecidas

- O **leaderboard** exibe uma lista fixa de pontuações de exemplo; ainda não há registro das partidas reais.
- O **emoji ✌️** da tela de vitória não aparece, pois a fonte padrão do LibGDX (`BitmapFont`) não suporta emojis. Para exibi-lo, é preciso usar texto simples ou carregar uma fonte TTF com a extensão FreeType.
- A seleção de **dificuldade** ainda não foi implementada: o botão GO leva direto para a fase.

---

## Créditos

Projeto acadêmico desenvolvido para o **SENAI Félix Guisard**, com [LibGDX](https://libgdx.com/).

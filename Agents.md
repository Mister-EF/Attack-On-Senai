# AGENTS.md - Diretrizes do Projeto Attack on SENAI

## Papel (Role)
Você é um Arquiteto de Software e Desenvolvedor de Jogos 2D Especialista em Java e LibGDX. Sua missão é implementar o jogo "Attack on SENAI", um jogo 2D no estilo Boss Rush e Plataforma focado em Orientação a Objetos limpa em Java puro, excelente desempenho no Desktop e alta legibilidade de código.

## Diretrizes de Código e Arquitetura
1. **Linguagem & Framework:** Exclusivamente Linguagem Java no ecossistema LibGDX (backend Desktop via módulo `lwjgl3`). Não utilize Kotlin, Scala ou outras linguagens.
2. **Pacote Raiz:** O namespace oficial do projeto é **`io.github.AOS`** (localizado em `core/src/main/java/io/github/AOS/`).
3. **Design Pattern:** Separação clara de responsabilidades em Java (`Screen`, `InputProcessor` dedicado, Máquina de Estados para Chefes).
4. **Física & Colisão:** NÃO use Box2D. Utilize retângulos de colisão nativos do LibGDX (`Rectangle` / `Intersector`).
5. **Áudio:** Desativado. Não instancie nem carregue arquivos de `Sound` ou `Music`.
6. **Assets & Placeholders Visuais:** 
   - Gerar texturas em tempo de execução via classe `Pixmap` do LibGDX em Java.
   - Cores dos Placeholders:
     * Protagonista: Verde (`Color.GREEN`)
     * Inimigos Comuns/Minions: Vermelho (`Color.RED`)
     * Chefes (Bosses): Roxo (`Color.PURPLE`)
     * Chão e Plataforma Sólida: Cinza Claro (`Color.LIGHT_GRAY`)
     * Fundo da Tela/Cenário: Preto (`Color.BLACK`)
7. **Controle de Câmera:** `OrthographicCamera` ajustada com `FitViewport` (1280x720).
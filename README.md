# CLT: Caos, Luta e Treta

<br>

> Um jogo de luta 2D em Java desenvolvido como projeto final para a disciplina de Linguagem de Programação III no curso de Sistemas de Informação da UESB.

---

## 📖 Sobre o Projeto

**CLT: Caos, Luta e Treta** é um jogo de luta 2D _versus_ local, criado como avaliação final para a disciplina de Linguagem de Programação III, ministrada pelo professor **Murilo Santana**.

O principal objetivo do projeto foi aplicar e solidificar os conceitos de programação aprendidos em sala de aula em um ambiente prático e complexo. O desenvolvimento explorou:

- **Programação Orientada a Objetos (POO)**: Estruturação de classes para personagens, ataques e gerenciamento de jogo.
- **Interfaces Gráficas (GUI)**: Uso da biblioteca **Java Swing** para criar todas as janelas, menus e a arena de combate.
- **Tratamento de Eventos**: Captura e resposta a _inputs_ do teclado para movimentação e ações dos jogadores.
- **Estrutura Modular**: Organização do código para desacoplamento e manutenibilidade.
- **Mecânicas de Jogo**: Implementação de lógica de colisão, barras de vida, dano e animações baseadas em _spritesheets_.

---

## ✨ Principais Funcionalidades

- **Modo de Batalha 1v1 Local:** Jogue contra um amigo no mesmo teclado.
- **Elenco de 5 Personagens:** Escolha entre cinco lutadores únicos, cada um com seus próprios atributos.
- **Sistema de Combate Completo:** Inclui movimentação, pulo, agachamento, defesa, soco, chute e um ataque especial.
- **Animação 2D:** Personagens animados com _sprites_ para dar vida ao combate.

---

## 🛠️ Tecnologias Utilizadas

O projeto foi construído inteiramente com:

- **Java (JDK 8+):** Linguagem principal do projeto.
- **Java Swing:** Biblioteca nativa do Java utilizada para toda a interface gráfica.

---

## 👊 Conheça os Lutadores

Cada personagem possui atributos únicos de **Vida**, **Força** (que afeta o dano) e **Defesa** (que reduz o dano recebido). Escolha o arquétipo que mais combina com seu estilo!

| Personagem    | Categoria (Arquétipo)     | Vida | Força | Defesa |
| :------------ | :------------------------ | :--: | :---: | :----: |
| **Isagram**   | All-Rounder (Equilibrada) | 400  |  35   |   10   |
| **Lule**      | Agressivo / Tricky        | 400  |  38   |   8    |
| **Nita**      | Glass Cannon (Dano Alto)  | 360  |  45   |   5    |
| **Murissoca** | Tank (Tanque)             | 460  |  28   |   20   |
| **Teletony**  | Defensivo / Equilibrado   | 420  |  32   |   15   |

---

## 🕹️ Controles

Prepare-se para a luta! No final de cada round, pressione `Enter` para continuar.

| Ação               | Jogador 1 (Esquerda)  | Jogador 2 (Direita)                   |
| :----------------- | :-------------------- | :------------------------------------ |
| **Mover Esquerda** | `A`                   | `Seta Esquerda`                       |
| **Mover Direita**  | `D`                   | `Seta Direita`                        |
| **Pular**          | `W`                   | `Seta Cima`                           |
| **Agachar**        | `S`                   | `Seta Baixo`                          |
| **Soco**           | `F`                   | `Num Pad 1`                           |
| **Chute**          | `G`                   | `Num Pad 2`                           |
| **Especial**       | `H` + `G` (sequência) | `Num Pad 3` + `Num Pad 2` (sequência) |
| **Defender**       | `Espaço`              | `Num Pad 0`                           |

---

## 👥 Colaboradores

O desenvolvimento foi realizado de forma colaborativa pelos alunos:

- [Víctor Ramaciotti](https://github.com/victorramacciotti)
- [William Almeida](https://github.com/willalmeid)

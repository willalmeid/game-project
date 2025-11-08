package model;

import java.awt.Rectangle;
import java.util.Random;
import audio.SoundManager;
import model.CharacterFactory.CharacterType;

/**
 * A classe principal do modelo do jogo. Gerencia o estado da partida,
 * os jogadores, o timer e a lógica de colisão.
 * Inclui a lógica de "KO Freeze" (pausa dramática).
 */
public class Game {

  /**
   * Define os estados principais do loop do jogo.
   */
  public enum GameState { 
    /** O round está em andamento; jogadores podem se mover e atacar. */
    PLAYING, 
    /** Um round terminou (tela de "Próximo Round"). */
    ROUND_OVER, 
    /** A partida (melhor de 3) terminou (tela de "Novo Jogo"). */
    GAME_OVER,
    /** Pausa dramática após o golpe final (K.O.). */
    KO_FREEZE 
  }
  
  /** Define os fundos de cenário possíveis. */
  public enum BackgroundType { MANHA, TARDE, NOITE }

  // --- Constantes do Jogo ---
  /** A largura lógica da arena de jogo (em pixels). */
  private static final int LOGICAL_WIDTH = 800;
  /** A altura lógica da arena de jogo (em pixels). */
  private static final int LOGICAL_HEIGHT = 600;
  /** A coordenada Y do "chão" da arena. */
  private static final int GROUND_Y = 500;
  /** A margem inicial (em pixels) dos jogadores em relação à borda. */
  private static final int INITIAL_X_MARGIN = 50;
  /** O tempo inicial do timer de round (em segundos). */
  private static final int INITIAL_TIMER_SECONDS = 99;
  /** O número de rounds que um jogador precisa vencer para ganhar a partida. */
  private static final int WINS_TO_FINISH_MATCH = 2;
  
  /** Duração do "congelamento" (K.O.) em ticks do jogo (30 ticks = 0.5 seg). */
  private static final int KO_FREEZE_DURATION = 30;

  // --- Referências de Entidades ---
  /** Instância do Jogador 1. */
  private final Player player1;
  /** Instância do Jogador 2. */
  private final Player player2;
  
  // --- Estado do Jogo ---
  /** O estado atual da máquina de estados do jogo. */
  private GameState currentState;
  /** O fundo de cenário sorteado para esta partida. */
  private final BackgroundType background;
  /** O timer de round (em ticks/frames). */
  private int gameTimer;
  /** O nome do vencedor do round atual. */
  private String roundWinnerName;
  /** O nome do vencedor da partida inteira. */
  private String matchWinnerName;
  /** Contador de vitórias do Jogador 1. */
  private int p1Wins;
  /** Contador de vitórias do Jogador 2. */
  private int p2Wins;
  /** Instância de Random para sorteios (ex: fundo). */
  private final Random random = new Random();
  
  /** Timer para controlar o delay do KO (em ticks/frames). */
  private int koFreezeTimer = 0;

  /**
   * Constrói uma nova instância do Jogo.
   * @param p1Type O {@link CharacterType} escolhido pelo Jogador 1.
   * @param p2Type O {@link CharacterType} escolhido pelo Jogador 2.
   */
  public Game(CharacterType p1Type, CharacterType p2Type) {
    this.background = selectRandomBackground();
    
    Character char1 = CharacterFactory.createCharacter(p1Type);
    Character char2 = CharacterFactory.createCharacter(p2Type);
    String p1SpritePath = p1Type.getIdleSpritePath();
    String p2SpritePath = p2Type.getIdleSpritePath();
    
    this.player1 = new Player(0, 0, char1, java.awt.Color.BLUE, p1SpritePath);
    this.player2 = new Player(0, 0, char2, java.awt.Color.RED, p2SpritePath);
    
    this.p1Wins = 0;
    this.p2Wins = 0;
    startNextRound();
  }

  // --- MÉTODOS PÚBLICOS DE CONTROLE ---

  /**
   * Método principal do loop do jogo (Game Loop), chamado pelo Controller.
   * Gerencia a máquina de estados (PLAYING, KO_FREEZE, etc.).
   */
  public void update() {
    // Se a partida/round acabou (tela de botões), não faz NADA.
    if (currentState == GameState.ROUND_OVER || currentState == GameState.GAME_OVER) {
        return;
    }

    // Se estamos no "KO Freeze"...
    if (currentState == GameState.KO_FREEZE) {
        koFreezeTimer--;
        
        // Continua atualizando os players (para as animações rodarem)
        updatePlayers(); 
        
        // Quando o timer acabar, força a checagem de fim de round.
        if (koFreezeTimer <= 0) {
            checkRoundOver(); // Muda o estado para ROUND_OVER/GAME_OVER
        }
        return; // Pula o resto (input, colisões, etc.)
    }

    // --- Lógica normal de PLAYING ---
    updatePlayers();
    updateDirections();
    checkCollisions(); 
    updateTimerAndState();
  }

  /**
   * Configura o jogo para o início de um novo round.
   * Reseta os jogadores e o timer, e define o estado como PLAYING.
   */
  public void startNextRound() {
    resetPlayersToStart();
    this.gameTimer = INITIAL_TIMER_SECONDS * 60; // Converte segundos para ticks
    this.currentState = GameState.PLAYING;
  }

  /**
   * Injeta a dependência do SoundManager nos jogadores.
   * @param sm O SoundManager global.
   */
  public void setSoundManager(SoundManager sm) {
    player1.setSoundManager(sm);
    player2.setSoundManager(sm);
  }

  // --- GETTERS ---
  
  /** @return O estado atual da máquina de estados do jogo. */
  public GameState getCurrentState() { return currentState; }
  /** @return O tempo restante do round (em ticks/frames). */
  public int getGameTimer() { return gameTimer; }
  /** @return O nome do vencedor do último round. */
  public String getRoundWinnerName() { return roundWinnerName; }
  /** @return O nome do vencedor final da partida. */
  public String getMatchWinnerName() { return matchWinnerName; }
  /** @return O número de rounds vencidos pelo Jogador 1. */
  public int getP1Wins() { return p1Wins; }
  /** @return O número de rounds vencidos pelo Jogador 2. */
  public int getP2Wins() { return p2Wins; }
  /** @return O tipo de fundo de cenário sorteado. */
  public BackgroundType getBackground() { return this.background; }
  /** @return A instância do Jogador 1. */
  public Player getPlayer1() { return player1; }
  /** @return A instância do Jogador 2. */
  public Player getPlayer2() { return player2; }
  /** @return A largura lógica da arena. */
  public int getGameWidth() { return LOGICAL_WIDTH; }
  /** @return A altura lógica da arena. */
  public int getGameHeight() { return LOGICAL_HEIGHT; }

  // --- MÉTODOS PRIVADOS AUXILIARES ---

  /**
   * Sorteia um fundo de cenário aleatório da lista do enum.
   * @return Um {@link BackgroundType} aleatório.
   */
  private BackgroundType selectRandomBackground() {
    BackgroundType[] allBGs = BackgroundType.values();
    return allBGs[random.nextInt(allBGs.length)];
  }
  
  /**
   * Reseta a vida e a posição dos jogadores para o início do round.
   */
  private void resetPlayersToStart() {
    player1.getCharacter().resetLife();
    player2.getCharacter().resetLife();
    
    int initialY = GROUND_Y - player1.getCharacter().getHeight();
    int p1InitialX = INITIAL_X_MARGIN;
    int p2InitialX = LOGICAL_WIDTH - INITIAL_X_MARGIN - player2.getCharacter().getWidth();

    player1.resetPosition(p1InitialX, initialY);
    player2.resetPosition(p2InitialX, initialY);
  }

  /**
   * Chama o método update() de ambos os jogadores.
   */
  private void updatePlayers() {
    int ground = GROUND_Y - player1.getCharacter().getHeight();
    player1.update(ground, LOGICAL_WIDTH);
    player2.update(ground, LOGICAL_WIDTH);
  }
  
  /**
   * Atualiza o timer do jogo e verifica se o round terminou (por tempo).
   */
  private void updateTimerAndState() {
    // Não faz nada se o jogo já foi pausado por um KO
    if (currentState == GameState.KO_FREEZE) {
        return;
    }
    gameTimer--;
    checkRoundOver();
  }
  
  /**
   * Garante que os jogadores estejam sempre virados um para o outro.
   */
  private void updateDirections() {
    if (player1.getX() < player2.getX()) {
      player1.setDirection(1); // Direita
      player2.setDirection(-1); // Esquerda
    } else {
      player1.setDirection(-1); // Esquerda
      player2.setDirection(1); // Direita
    }
  }

  /**
   * Verifica colisões de ataque entre os dois jogadores.
   */
  private void checkCollisions() {
    checkAttackCollision(player1, player2);
    checkAttackCollision(player2, player1);
  }
  
  /**
   * Verifica se um atacante acertou um defensor.
   * Se o golpe for fatal, ativa o estado de KO_FREEZE.
   * @param attacker O jogador que está atacando.
   * @param defender O jogador que está defendendo.
   */
  private void checkAttackCollision(Player attacker, Player defender) {
    // Não registra novos golpes se o jogo já não estiver em PLAYING
    if (currentState != GameState.PLAYING) return;
      
    Rectangle attackHitbox = attacker.getCurrentAttackHitbox();
    
    if (attackHitbox != null && attacker.getCurrentAttackType() != AttackType.NONE && !defender.isHitStunned()) {
      
      if (attackHitbox.intersects(defender.getBodyHitbox())) {
        if (defender.isDefending()) {
          System.out.println("BLOQUEADO!");
        } else {
          Character attackerChar = attacker.getCharacter();
          AttackType type = attacker.getCurrentAttackType();
          float damage = 0;
          
          switch (type) {
            case PUNCH: damage = attackerChar.getPunchDamage(); break;
            case KICK: damage = attackerChar.getKickDamage(); break;
            case SPECIAL: damage = attackerChar.getSpecialDamage(); break;
            default: break; // Robustez: não faz nada se o AttackType não for conhecido
          }
          
          defender.takeHit(type, damage);
          attacker.addSpecial();
          
          // Se este golpe matou o defensor...
          if (defender.isDead()) {
            this.currentState = GameState.KO_FREEZE; // ATIVA O ESTADO DE FREEZE
            this.koFreezeTimer = KO_FREEZE_DURATION; // Inicia o timer
          }
          
          System.out.println(attackerChar.getName() + " acertou. Vida de " + defender.getCharacter().getName() + ": " + defender.getCharacter().getLife());
        }
      }
    }
  }

  /**
   * Verifica se o round terminou (por KO ou tempo) e atualiza o placar.
   * É chamado pelo updateTimerAndState() ou pelo update() (após o KO_FREEZE).
   */
  public void checkRoundOver() {
    // Se o timer de KO ainda está rodando, NÃO termine o round.
    if (currentState == GameState.KO_FREEZE && koFreezeTimer > 0) {
        return;
    }

    Character c1 = player1.getCharacter();
    Character c2 = player2.getCharacter();
    String winner = null;

    if (c1.getLife() <= 0) { // P2 Venceu
      winner = c2.getName(); 
      p2Wins++;
    } else if (c2.getLife() <= 0) { // P1 Venceu
      winner = c1.getName(); 
      p1Wins++;
    } else if (gameTimer <= 0) { // Time Out
      float p1LifePercent = c1.getLife() / c1.getMaxLife();
      float p2LifePercent = c2.getLife() / c2.getMaxLife();
      
      if (p1LifePercent > p2LifePercent) {
        winner = c1.getName(); p1Wins++;
      } else if (p2LifePercent > p1LifePercent) {
        winner = c2.getName(); p2Wins++;
      } else {
        winner = "Empate";
      }
    }
    
    // Se 'winner' for diferente de nulo, o round acabou
    if (winner != null) {
      endRound(winner);
    }
  }

  /**
   * Finaliza o round e define o estado como ROUND_OVER ou GAME_OVER.
   * @param winner O nome do vencedor do round (ou "Empate").
   */
  private void endRound(String winner) {
    this.roundWinnerName = winner;
    
    // Verifica se a partida (Melhor de 3) acabou
    if (p1Wins >= WINS_TO_FINISH_MATCH || p2Wins >= WINS_TO_FINISH_MATCH) {
      this.currentState = GameState.GAME_OVER;
      this.matchWinnerName = winner;
    } else {
      this.currentState = GameState.ROUND_OVER;
    }
  }
}
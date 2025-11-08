package model;

import java.awt.Rectangle;
import java.awt.Color;
import audio.SoundManager;

/**
 * Gerencia todas as lógicas de um jogador: estado, física, ataques e hitboxes.
 * Esta é a classe central do "Modelo" para um lutador individual.
 */
public class Player {

  // --- Constantes de Física e Estado ---
  /** A força da gravidade aplicada ao pulo (pixels/tick²). */
  private static final double GRAVITY = 0.9;
  /** A velocidade vertical inicial (negativa) do pulo. */
  private static final int JUMP_STRENGTH = -17;
  
  // --- Constantes de Balanceamento (Duração, Stun, Cooldown em frames/ticks) ---
  /** Duração da lógica de soco. */
  private static final int PUNCH_DURATION = 12, KICK_DURATION = 16, SPECIAL_DURATION = 25;
  /** Duração do "hitstun" que cada golpe causa. */
  private static final int PUNCH_STUN = 12, KICK_STUN = 16, SPECIAL_STUN = 25;
  /** Tempo de espera (cooldown) antes de poder usar o golpe novamente. */
  private static final int PUNCH_COOLDOWN = 15, KICK_COOLDOWN = 20, SPECIAL_COOLDOWN = 30;
  
  // --- Constantes de Ataque (Hitbox) ---
  private static final int PUNCH_HITBOX_W = 50, PUNCH_HITBOX_H = 20;
  private static final int KICK_HITBOX_W = 60, KICK_HITBOX_H = 20;
  private static final int SPECIAL_HITBOX_W = 80, SPECIAL_HITBOX_H = 40;
  
  // --- Constantes do Medidor de Especial ---
  /** Valor máximo do medidor de especial. */
  private static final float MAX_SPECIAL = 100f;
  /** Ganho de especial ao acertar um golpe. */
  private static final float HIT_GAIN = 15f;
  
  // --- Referências de Componentes ---
  /** A ficha de stats (vida, força) do personagem. */
  private final Character character; 
  /** O gerenciador de animações (aparência) do personagem. */
  private final PlayerAppearance appearance;
  /** A cor associada ao jogador (para UI ou debug). */
  private final Color color;
  /** A instância global do SoundManager (injetada). */
  private SoundManager soundManager;

  // --- Estado de Posição e Física ---
  /** Posição X atual no mundo. */
  private int x;
  /** Posição Y atual no mundo. */
  private int y;
  /** Direção para onde o jogador está virado (1 = Direita, -1 = Esquerda). */
  private int direction = 1;
  /** Velocidade vertical atual (para pulo/gravidade). */
  private double velocityY = 0;
  /** Flag de estado: o jogador está no ar? */
  private boolean isJumping = false;
  
  // --- Estado de Ação ---
  /** Flag de estado: o jogador está agachado? */
  private boolean isCrouching = false;
  /** Flag de estado: o jogador está defendendo? */
  private boolean isDefending = false;
  /** Flag de estado: o jogador está em "hitstun" (levou dano)? */
  private boolean isHitStunned = false;
  /** Timer (em ticks) de quanto tempo o hitstun dura. */
  private int hitStunTimer = 0;
  
  // --- Estado de Ataque ---
  /** O tipo de ataque atual (ou NONE). */
  private AttackType currentAttack = AttackType.NONE;
  /** Timer (em ticks) de espera para o próximo ataque. */
  private int attackCooldown = 0;
  /** Timer (em ticks) de duração do ataque atual. */
  private int attackDuration = 0;
  /** A hitbox do ataque atual (ou null). */
  private Rectangle currentAttackHitbox;
  
  // --- Estado de Recursos ---
  /** O valor atual do medidor de especial. */
  private float specialMeter = 0;
  
  /** (Otimização GC) Hitbox do corpo, reutilizado para evitar Garbage Collection. */
  private final Rectangle bodyHitbox;
  
  /**
   * Constrói uma nova instância de Jogador.
   * @param x Posição X inicial.
   * @param y Posição Y inicial.
   * @param character A ficha de stats (Modelo).
   * @param color A cor associada (para UI/debug).
   * @param idleSpritePath O caminho para o sprite 'idle' (base).
   */
  public Player(int x, int y, Character character, Color color, String idleSpritePath) {
    this.x = x; 
    this.y = y; 
    this.character = character; 
    this.color = color;
    this.appearance = new PlayerAppearance(idleSpritePath);
    this.bodyHitbox = new Rectangle(); // (Otimização GC) Criado uma única vez
  }

  /**
   * Método principal de atualização (Game Loop) para o jogador.
   * Chamado 60x por segundo pelo Game.
   * @param groundY A coordenada Y do "chão".
   * @param gameWidth A largura lógica da tela (para limites).
   */
  public void update(int groundY, int gameWidth) {
    appearance.update(); // Avança a animação
    
    // Se estiver em "hitstun", o jogador não pode fazer nada.
    if (isHitStunned) {
      handleHitStun();
      enforceBoundaries(gameWidth);
      return; // Pula toda a lógica de input e física
    }
    
    updateCooldowns();
    updateVisualState();
    applyPhysics(groundY);
    enforceBoundaries(gameWidth);
  }
  
  /**
   * Injeta a dependência do SoundManager (chamado pelo Game).
   * @param sm A instância global do SoundManager.
   */
  public void setSoundManager(SoundManager sm) { 
    this.soundManager = sm; 
  }

  /**
   * Move o jogador horizontalmente, se nenhuma outra ação impedir.
   * Define o estado visual como WALKING.
   * @param dx A quantidade de pixels a mover.
   */
  public void move(int dx) {
    if (isHitStunned || isCrouching || isDefending || currentAttack != AttackType.NONE) {
      return;
    }
    appearance.setState(PlayerState.WALKING);
    this.x += dx;
  }
  
  /**
   * Define o estado visual como IDLE (parado), mas apenas 
   * se o estado atual for WALKING.
   */
  public void stopWalking() {
    if (appearance.getCurrentState() == PlayerState.WALKING) {
      appearance.setState(PlayerState.IDLE);
    }
  }
  
  /**
   * Garante que o jogador permaneça dentro dos limites da tela.
   * @param gameWidth A largura total da área de jogo.
   */
  private void enforceBoundaries(int gameWidth) {
    if (this.x < 0) this.x = 0;
    if (this.x + this.character.getWidth() > gameWidth) {
        this.x = gameWidth - this.character.getWidth();
    }
  }

  /**
   * (Otimização GC) Retorna o hitbox do corpo (reutilizado), 
   * atualizado para a posição e estado (agachado) atuais.
   * @return Um Rectangle representando a hitbox do corpo.
   */
  public Rectangle getBodyHitbox() {
    // Calcula as dimensões proporcionais
    int hitboxWidth = (int) (character.getWidth() * 0.4f);
    int hitboxHeight = (int) (character.getHeight() * 0.77f);
    int offsetX = (character.getWidth() - hitboxWidth) / 2;
    int offsetY = character.getHeight() - hitboxHeight;

    // Atualiza o retângulo reutilizado
    if (isCrouching) {
      int crouchHeight = hitboxHeight / 2;
      bodyHitbox.setBounds(x + offsetX, y + offsetY + crouchHeight, hitboxWidth, crouchHeight);
    } else {
      bodyHitbox.setBounds(x + offsetX, y + offsetY, hitboxWidth, hitboxHeight);
    }
    return bodyHitbox; // Retorna o objeto reutilizado
  }

  // --- MÉTODOS DE AÇÃO (PULIC API) ---

  /** Inicia a ação de pulo, se permitido. */
  public void startJump() {
    if (canPerformAction() && !isJumping) {
      isJumping = true;
      velocityY = JUMP_STRENGTH;
      if (soundManager != null) soundManager.playSoundFX(SoundManager.SoundFiles.JUMP_SOUND);
      appearance.setState(PlayerState.JUMPING);
    }
  }

  /** Inicia um ataque de soco, se permitido. */
  public void startPunch() {
    if (!canAttack()) return;
    performAttack(AttackType.PUNCH, PUNCH_DURATION, PUNCH_COOLDOWN, 
                    PlayerState.PUNCHING, SoundManager.SoundFiles.PUNCH_SOUND);
    
    Rectangle body = getBodyHitbox();
    // TODO: A posição (y) da hitbox está com "número mágico" (body.y + 20).
    // Idealmente, seria proporcional (ex: body.y + (body.height * 0.1f)).
    int w = PUNCH_HITBOX_W, h = PUNCH_HITBOX_H, y = body.y + 20;
    int x = (direction == 1) ? (body.x + body.width) : (body.x - w);
    this.currentAttackHitbox = new Rectangle(x, y, w, h);
  }

  /** Inicia um ataque de chute, se permitido. */
  public void startKick() {
    if (!canAttack()) return;
    performAttack(AttackType.KICK, KICK_DURATION, KICK_COOLDOWN, 
                    PlayerState.KICKING, SoundManager.SoundFiles.KICK_SOUND);
                    
    Rectangle body = getBodyHitbox();
    // TODO: Posição (y) da hitbox com "número mágico".
    int w = KICK_HITBOX_W, h = KICK_HITBOX_H, y = body.y + body.height - h - 10;
    int x = (direction == 1) ? (body.x + body.width) : (body.x - w);
    this.currentAttackHitbox = new Rectangle(x, y, w, h);
  }

  /** Inicia um ataque especial, se permitido e com medidor cheio. */
  public void startSpecial() {
    if (!canAttack() || specialMeter < MAX_SPECIAL) return;
    performAttack(AttackType.SPECIAL, SPECIAL_DURATION, SPECIAL_COOLDOWN, 
                    PlayerState.SPECIAL_ATTACK, SoundManager.SoundFiles.SPECIAL_SOUND);
                    
    this.specialMeter = 0; // Consome o especial
    Rectangle body = getBodyHitbox();
    // TODO: Posição (y) da hitbox com "número mágico".
    int w = SPECIAL_HITBOX_W, h = SPECIAL_HITBOX_H, y = body.y + 30;
    int x = (direction == 1) ? (body.x + body.width) : (body.x - w);
    this.currentAttackHitbox = new Rectangle(x, y, w, h);
  }

  /** Define o estado do jogador como agachado, se permitido. */
  public void startCrouching() {
    if (canPerformAction() && !isJumping) this.isCrouching = true;
  }

  /** Remove o estado de agachado. */
  public void endCrouching() { this.isCrouching = false; }

  /** Define o estado do jogador como defendendo, se permitido. */
  public void startDefending() {
    if (canPerformAction() && !isJumping) this.isDefending = true;
  }

  /** Remove o estado de defesa. */
  public void endDefending() { this.isDefending = false; }

  /**
   * Aplica dano e "hitstun" ao jogador quando ele é atingido.
   * @param attackType O tipo de ataque recebido (para calcular o stun).
   * @param damage O dano (já calculado) a ser aplicado.
   */
  public void takeHit(AttackType attackType, float damage) {
    isHitStunned = true;
    character.takeDamage(damage);
    if (soundManager != null) soundManager.playSoundFX(SoundManager.SoundFiles.HIT_SOUND);
    endAttack(); // Interrompe qualquer ataque que o jogador estava fazendo
    
    // Define o tempo de "stun" baseado no ataque recebido
    switch (attackType) {
      case PUNCH: hitStunTimer = PUNCH_STUN; break;
      case KICK: hitStunTimer = KICK_STUN; break;
      case SPECIAL: hitStunTimer = SPECIAL_STUN; break;
      default: hitStunTimer = 10; break; // Fallback
    }
  }

  /** Adiciona uma quantia ao medidor de especial (ao acertar um golpe). */
  public void addSpecial() {
    this.specialMeter = Math.min(MAX_SPECIAL, this.specialMeter + HIT_GAIN);
  }

  /**
   * Reseta o jogador para sua posição inicial (início do round).
   * @param x Posição X inicial.
   * @param y Posição Y inicial (no chão).
   */
  public void resetPosition(int x, int y) {
    this.x = x;
    this.y = y;
    this.isHitStunned = false;
    this.velocityY = 0;
    this.isJumping = false;
    this.specialMeter = 0;
    endAttack();
    appearance.setState(PlayerState.IDLE);
  }

  // --- MÉTODOS PRIVADOS DE GERENCIAMENTO DE ESTADO ---

  /** Gerencia o timer de "hitstun" e sincroniza a animação de "take hit". */
  private void handleHitStun() {
    hitStunTimer--;
    // (Otimização) Sincroniza a animação de "hit" com o tempo de "stun"
    appearance.setSyncedState(PlayerState.TAKING_HIT, hitStunTimer);
    
    if (hitStunTimer <= 0) {
      isHitStunned = false;
    }
  }

  /** Atualiza os timers de cooldown de ataque e duração do ataque. */
  private void updateCooldowns() {
    if (attackCooldown > 0) attackCooldown--;
    
    if (attackDuration > 0) {
      attackDuration--;
      if (attackDuration == 0) {
        endAttack();
      }
    }
  }

  /**
   * Atualiza o estado visual (animação) do jogador baseado na ação atual.
   * Corrige o bug do "soco duplo" e do "andar".
   */
  private void updateVisualState() {
    // Se um ataque estiver em andamento, ele controla a animação.
    if (currentAttack != AttackType.NONE) {
      return;
    }
    
    PlayerState currentState = appearance.getCurrentState();

    // Estados de prioridade
    if (isJumping) {
      appearance.setState(PlayerState.JUMPING);
    } else if (isDefending) {
      appearance.setState(PlayerState.DEFENDING);
    } else if (isCrouching) {
      appearance.setState(PlayerState.CROUCHING);
    } 
    // (Correção de Bug)
    // Se não estiver em estado de prioridade E não estiver andando...
    else if (currentState != PlayerState.WALKING) {
       appearance.setState(PlayerState.IDLE); // ...volta para IDLE.
    }
  }

  /** Aplica gravidade e verifica colisão com o chão. */
  private void applyPhysics(int groundY) {
    if (isJumping) {
      velocityY += GRAVITY;
      y += velocityY;
    }
    
    // Colisão com o chão
    if (y >= groundY) {
      y = groundY;
      isJumping = false;
      velocityY = 0;
    }
  }

  /** Limpa o estado de ataque (fim da animação de ataque). */
  private void endAttack() {
    this.currentAttack = AttackType.NONE;
    this.currentAttackHitbox = null;
  }

  /**
   * Método auxiliar centralizado para iniciar qualquer ataque.
   * (Otimização) Chama setSyncedState para sincronizar a animação com a lógica.
   *
   * @param type O tipo de ataque (Enum).
   * @param duration Duração lógica (em ticks).
   * @param cooldown Cooldown (em ticks).
   * @param animState O estado de animação a ser definido.
   * @param sound O som a ser tocado.
   */
  private void performAttack(AttackType type, int duration, int cooldown, PlayerState animState, SoundManager.SoundFiles sound) {
    this.currentAttack = type;
    this.attackDuration = duration;
    this.attackCooldown = cooldown;
    
    // (Otimização) Sincroniza a animação com a duração da lógica
    appearance.setSyncedState(animState, duration); 
    
    if (soundManager != null && sound != null) soundManager.playSoundFX(sound);
  }

  /** * Verifica se o jogador pode realizar uma ação básica (pular, agachar, defender).
   * @return true se o jogador não estiver ocupado.
   */
  private boolean canPerformAction() {
    return !isHitStunned && !isDefending && !isCrouching && currentAttack == AttackType.NONE;
  }

  /**
   * Verifica se o jogador pode iniciar um ataque.
   * @return true se o jogador não estiver ocupado e o cooldown tiver acabado.
   */
  private boolean canAttack() {
    return canPerformAction() && attackCooldown == 0;
  }

  // --- GETTERS ---
  
  /** @return true se a vida do personagem chegou a 0. */
  public boolean isDead() {
    return this.character.getLife() <= 0;
  }
  
  /** @return O valor atual do medidor de especial. */
  public float getSpecialMeter() { return specialMeter; }
  
  /** @return O valor máximo do medidor de especial. */
  public float getMaxSpecial() { return MAX_SPECIAL; }
  
  /** @return A "ficha" (stats) do personagem. */
  public Character getCharacter() { return this.character; }
  
  /** @return O gerenciador de aparência (animações). */
  public PlayerAppearance getAppearance() { return appearance; }
  
  /** @return true se o jogador está no ar. */
  public boolean isJumping() { return isJumping; }
  
  /** @return true se o jogador está agachado. */
  public boolean isCrouching() { return isCrouching; }
  
  /** @return true se o jogador está defendendo. */
  public boolean isDefending() { return isDefending; }
  
  /** @return true se o jogador está em "hitstun". */
  public boolean isHitStunned() { return isHitStunned; }
  
  /** @return A hitbox do ataque atual, ou null se não houver ataque. */
  public Rectangle getCurrentAttackHitbox() { return currentAttackHitbox; }
  
  /** @return O {@link AttackType} do ataque atual, ou NONE. */
  public AttackType getCurrentAttackType() { return currentAttack; }
  
  /** @return A coordenada X atual do jogador. */
  public int getX() { return x; }
  
  /** @return A coordenada Y atual do jogador. */
  public int getY() { return y; }
  
  /** @return A cor de debug/UI associada ao jogador. */
  public Color getColor() { return color; }
  
  /** @return A direção (1 = Direita, -1 = Esquerda) do jogador. */
  public int getDirection() { return direction; }
  
  /**
   * Define a direção para onde o jogador está "virado".
   * @param direction 1 para Direita, -1 para Esquerda.
   */
  public void setDirection(int direction) {
    if (direction == 1 || direction == -1) this.direction = direction;
  }
}
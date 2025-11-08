package model;

import view.Animation;
import view.ImageCache;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * Gerencia a representação visual de um jogador.
 * Mapeia estados lógicos (PlayerState) para animações
 * e gerencia a sincronização da velocidade da animação.
 */
public class PlayerAppearance {
    
  // Velocidades PADRÃO (para animações em loop)
  // (60 FPS / 4 ticks = 15 FPS)
  private static final int IDLE_WALK_SPEED = 4;
  private static final int JUMP_SPEED = 4;
  
  // Velocidades PADRÃO (para animações de disparo único, caso a sincronia falhe)
  // (60 FPS / 3 ticks = 20 FPS)
  private static final int ATTACK_SPEED = 3;
  private static final int HIT_REACTION_SPEED = 3;

  /** Cache de todas as animações carregadas para este personagem. */
  private final Map<PlayerState, Animation> animations;
  /** A instância da animação que está ativa no momento. */
  private Animation currentAnimation;
  /** O estado lógico (PlayerState) atual. */
  private PlayerState currentState;

  /**
   * Constrói o gerenciador de aparência do jogador.
   * @param idleSpritePath O caminho base para o sprite 'idle', 
   * usado para encontrar as outras animações por convenção de nome.
   */
  public PlayerAppearance(String idleSpritePath) {
    this.animations = new HashMap<>();
    loadAnimations(idleSpritePath);
    setState(PlayerState.IDLE);
  }
  
  /**
   * Carrega todas as animações do personagem no cache, associando-as a um PlayerState.
   * @param basePath O caminho para o sprite 'idle'.
   */
  private void loadAnimations(String basePath) {
    // Carrega todas as animações com suas velocidades PADRÃO
    animations.put(PlayerState.IDLE, ImageCache.getAnimation(basePath, IDLE_WALK_SPEED, true));
    animations.put(PlayerState.WALKING, ImageCache.getAnimation(basePath.replace("idle", "walk"), IDLE_WALK_SPEED, true));
    animations.put(PlayerState.JUMPING, ImageCache.getAnimation(basePath.replace("idle", "jump"), JUMP_SPEED, false));
    animations.put(PlayerState.PUNCHING, ImageCache.getAnimation(basePath.replace("idle", "punch"), ATTACK_SPEED, false));
    animations.put(PlayerState.KICKING, ImageCache.getAnimation(basePath.replace("idle", "kick"), ATTACK_SPEED, false));
    animations.put(PlayerState.SPECIAL_ATTACK, ImageCache.getAnimation(basePath.replace("idle", "combo"), ATTACK_SPEED, false));
    animations.put(PlayerState.TAKING_HIT, ImageCache.getAnimation(basePath, HIT_REACTION_SPEED, false));
    
    // Otimização: Reúsa a animação 'IDLE' para estados que não têm animação própria
    Animation idleAnim = animations.get(PlayerState.IDLE);
    if (idleAnim != null) {
      animations.put(PlayerState.CROUCHING, idleAnim);
      animations.put(PlayerState.DEFENDING, idleAnim);
      animations.put(PlayerState.WALKING_BACKWARDS, idleAnim);
    }
  }
  
  /**
   * Altera a animação (para loops) usando a velocidade padrão.
   * Este método sempre reinicia a animação.
   * @param state O novo PlayerState.
   */
  public void setState(PlayerState state) {
    // O 'if (currentState == state)' foi removido intencionalmente
    // para permitir que as animações reiniciem (corrige o bug do "soco duplo").

    this.currentState = state;
    Animation newAnimation = animations.get(state);
    
    // Fallback: se a animação não for encontrada, usa IDLE
    this.currentAnimation = (newAnimation != null) ? newAnimation : animations.get(PlayerState.IDLE);
    
    if (this.currentAnimation != null) {
      // (Limpeza) Removida linha comentada desnecessária
      this.currentAnimation.reset();
    }
  }
  
  /**
   * (Otimização de Sincronia)
   * Altera a animação e SINCRONIZA sua velocidade (frameDelay)
   * para durar exatamente o tempo da lógica do jogo.
   *
   * @param state O novo PlayerState (ex: PUNCHING).
   * @param logicDurationTicks A duração da lógica em ticks (ex: PUNCH_DURATION = 12).
   */
  public void setSyncedState(PlayerState state, int logicDurationTicks) {
    this.currentState = state;
    Animation newAnimation = animations.get(state);

    this.currentAnimation = (newAnimation != null) ? newAnimation : animations.get(PlayerState.IDLE);
    
    if (this.currentAnimation == null) {
        return;
    }

    // --- A MÁGICA DA SINCRONIA ---
    int frameCount = this.currentAnimation.getFrameCount();
    
    // Calcula o delay necessário para a animação durar o mesmo que a lógica
    if (frameCount > 0 && logicDurationTicks > 0) {
      // Ex: 12 ticks de lógica / 6 quadros de animação = 2 ticks por quadro
      int newFrameDelay = logicDurationTicks / frameCount;
      this.currentAnimation.setFrameDelay(newFrameDelay);
    } else {
      // Fallback para a velocidade padrão se algo der errado (ex: GIF de 1 quadro)
      this.currentAnimation.setFrameDelay(ATTACK_SPEED);
    }
    // --- FIM DA MÁGICA ---

    this.currentAnimation.reset();
  }
  
  /**
   * Avança a animação atual em um quadro, se necessário.
   * Chamado 60x por segundo pelo Player.
   */
  public void update() {
    if (currentAnimation != null) {
      currentAnimation.update();
    }
  }

  /**
   * @return O quadro (imagem) atual da animação ativa, ou null.
   */
  public ImageIcon getCurrentFrame() {
    if (currentAnimation != null) {
      return currentAnimation.getCurrentFrame();
    }
    return null;
  }
  
  /**
   * @return O {@link PlayerState} lógico atual da aparência.
   */
  public PlayerState getCurrentState() {
    return this.currentState;
  }
}
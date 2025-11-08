package view;

import javax.swing.ImageIcon;

/**
 * Controla uma sequência de imagens (frames) para criar uma animação
 * sincronizada com o game loop.
 */
public class Animation {

  /** O array de imagens (quadros) da animação. */
  private final ImageIcon[] frames;
  
  /** Se a animação deve repetir (loop) ao terminar. */
  private boolean loops;
  
  /** O número de ticks do jogo a esperar antes de avançar um quadro. */
  private int frameDelay; 

  /** O índice do quadro (imagem) atual sendo exibido. */
  private int currentFrameIndex;
  
  /** O contador de ticks desde a última mudança de quadro. */
  private int frameTimer;

  /**
   * Cria um objeto de animação.
   * @param frames Um array de ImageIcon que compõe a animação.
   * @param frameDelay O número de ciclos do game loop a esperar antes de avançar um quadro.
   * @param loops Se true, a animação repetirá. Se false, travará no último quadro.
   */
  public Animation(ImageIcon[] frames, int frameDelay, boolean loops) {
    // (Otimização de Robustez) Garante que a animação seja válida.
    if (frames == null || frames.length == 0) {
      throw new IllegalArgumentException("O array de frames não pode ser nulo ou vazio.");
    }
    
    this.frames = frames.clone(); // Cópia defensiva
    this.frameDelay = (frameDelay < 1) ? 1 : frameDelay; // Garante delay mínimo
    this.loops = loops;
    this.currentFrameIndex = 0;
    this.frameTimer = 0;
  }

  /**
   * Avança a lógica da animação em um ciclo.
   * Deve ser chamado a cada quadro do game loop.
   */
  public void update() {
    // O delay é verificado aqui para permitir mudança em tempo real
    int activeDelay = (this.frameDelay < 1) ? 1 : this.frameDelay;

    frameTimer++;
    if (frameTimer < activeDelay) {
      return; // Ainda não é hora de mudar de quadro
    }

    // Reseta o timer e avança o quadro
    frameTimer = 0;
    currentFrameIndex++;

    // Se a animação chegou ao fim
    if (currentFrameIndex >= frames.length) {
      if (loops) {
        currentFrameIndex = 0; // Se repete, volta ao início
      } else {
        currentFrameIndex = frames.length - 1; // Senão, trava no último quadro
      }
    }
  }

  /**
   * Obtém a imagem do quadro atual da animação.
   * @return O ImageIcon do quadro atual.
   */
  public ImageIcon getCurrentFrame() {
    return frames[currentFrameIndex];
  }

  /**
   * Reinicia a animação para o primeiro quadro.
   */
  public void reset() {
    this.currentFrameIndex = 0;
    this.frameTimer = 0;
  }
  
  // --- MÉTODOS DE SINCRONIA ---
  
  /**
   * Define uma nova velocidade (frameDelay) para esta animação.
   * @param frameDelay O número de ticks do jogo por quadro de animação.
   */
  public void setFrameDelay(int frameDelay) {
    this.frameDelay = (frameDelay < 1) ? 1 : frameDelay;
  }
  
  /**
   * @return O número total de quadros (imagens) nesta animação.
   */
  public int getFrameCount() {
    return this.frames.length;
  }
}
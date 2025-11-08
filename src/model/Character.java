package model;

/**
 * Representa a "ficha de atributos" de um lutador (Modelo de Dados).
 * Contém informações imutáveis (stats base) e mutáveis (vida atual).
 * Não contém lógica de estado do jogo (ex: "pulando", "atacando").
 */
public class Character {
    
  // --- CONSTANTES DE BALANCEAMENTO ---
  /** Modificador de dano para soco. */
  private static final float PUNCH_DAMAGE_MODIFIER = 1.0f;
  /** Modificador de dano para chute (20% mais forte que soco). */
  private static final float KICK_DAMAGE_MODIFIER = 1.2f;
  /** Modificador de dano para especial (150% mais forte que soco). */
  private static final float SPECIAL_DAMAGE_MODIFIER = 2.5f;
  /** Dano mínimo que um personagem pode receber após a defesa. */
  private static final float MINIMUM_DAMAGE_TAKEN = 1.0f;

  // --- ATRIBUTOS (Estado Imutável - Stats Base) ---
  
  /** O nome de exibição do lutador. */
  private final String name;
  /** A quantidade máxima de vida. */
  private final float maxLife;
  /** O valor base de força, usado para calcular o dano. */
  private final float strength;
  /** O valor base de defesa, usado para reduzir o dano recebido. */
  private final float defense;
  /** A largura (em pixels) do sprite/hitbox do personagem. */
  private final int width;
  /** A altura (em pixels) do sprite/hitbox do personagem. */
  private final int height;
  
  // --- ATRIBUTOS (Estado Mutável - Durante a Partida) ---
  
  /** A vida atual do personagem. */
  private float life;

  /**
   * Constrói uma nova ficha de personagem.
   *
   * @param name O nome do lutador (ex: "Isagram").
   * @param maxLife A vida máxima do lutador.
   * @param strength A força base, usada para calcular o dano.
   * @param defense O valor de defesa, que reduz o dano recebido.
   * @param width A largura do personagem (em pixels), para hitbox.
   * @param height A altura do personagem (em pixels), para hitbox.
   */
  public Character(String name, float maxLife, float strength, float defense, int width, int height) {
    this.name = name;
    this.maxLife = maxLife;
    this.life = maxLife; // Começa com vida cheia
    this.strength = strength;
    this.defense = defense;
    this.width = width;
    this.height = height;
  }
  
  // --- LÓGICA DE COMBATE ---

  /**
   * Reduz a vida do personagem com base no dano recebido.
   * A defesa é subtraída do dano, e o dano final é no mínimo 1.
   * A vida nunca ficará abaixo de 0.
   *
   * @param incomingDamage O dano bruto do ataque.
   */
  public void takeDamage(float incomingDamage) {
    // Calcula o dano final após a redução da defesa (mínimo de 1)
    float finalDamage = Math.max(MINIMUM_DAMAGE_TAKEN, incomingDamage - this.defense);
    
    // Garante que a vida não fique negativa
    this.life = Math.max(0, this.life - finalDamage);
  }

  /**
   * Calcula o dano de um soco com base na força e no modificador.
   * @return O dano bruto do soco.
   */
  public float getPunchDamage() {
    return this.strength * PUNCH_DAMAGE_MODIFIER;
  }
  
  /**
   * Calcula o dano de um chute com base na força e no modificador.
   * @return O dano bruto do chute.
   */
  public float getKickDamage() {
    return this.strength * KICK_DAMAGE_MODIFIER;
  }
  
  /**
   * Calcula o dano de um ataque especial com base na força e no modificador.
   * @return O dano bruto do especial.
   */
  public float getSpecialDamage() {
    return this.strength * SPECIAL_DAMAGE_MODIFIER;
  }
  
  /**
   * Reseta a vida do personagem para o valor máximo (ex: novo round).
   */
  public void resetLife() {
    this.life = this.maxLife;
  }
  
  // --- GETTERS ---
  
  /** @return O nome de exibição do personagem. */
  public String getName() { return name; }
  
  /** @return A vida máxima que o personagem pode ter. */
  public float getMaxLife() { return maxLife; }
  
  /** @return A vida atual do personagem. */
  public float getLife() { return life; }
  
  /** @return A força base do personagem. */
  public float getStrength() { return strength; }
  
  /** @return A defesa base do personagem. */
  public float getDefense() { return defense; }
  
  /** @return A largura em pixels do personagem. */
  public int getWidth() { return width; }
  
  /** @return A altura em pixels do personagem. */
  public int getHeight() { return height; }
  
}
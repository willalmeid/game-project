package model;

/**
 * Uma "Fábrica" (Factory) utilitária para criar instâncias de objetos {@link Character}.
 * Esta classe também armazena o "banco de dados" de atributos de personagens
 * através do enum público aninhado {@link CharacterType}.
 *
 * Esta classe é 'final' e não pode ser instanciada.
 */
public final class CharacterFactory {

  /**
   * Construtor privado para prevenir a instanciação de uma classe utilitária.
   */
  private CharacterFactory() {}

  /**
   * Define o banco de dados de todos os personagens jogáveis.
   * Cada constante do enum armazena os atributos base, caminhos de sprite
   * e textos descritivos para um personagem específico.
   */
  public enum CharacterType {

    /** * ARQUÉTIPO: All-Rounder (Equilibrado).
     * Isa Gram, a Diva Digital do Instagram. Rainha dos filtros e dos flertes 
     * forçados, ela luta com egocentrismo e hashtags letais.
     */
    ISAGRAM("Isagram", 400f, 35f, 10f, 240, 248, 
            "/sprites/Isagram/idleIsagram.gif", 
            "Isa Gram, a Diva Digital do Instagram. Rainha dos filtros e dos flertes forçados, ela luta com egocentrismo e hashtags letais."),
    
    /** * ARQUÉTIPO: Tricky / Agressivo.
     * Metade carisma, metade caos, Lule é 100% polarização. Ele distribui 
     * promessas e golpes de oportunismo com a mesma fluidez.
     */
    LULE("Lule", 400f, 38f, 8f, 240, 248, 
         "/sprites/Lule/idleLule.gif", 
         "Metade carisma, metade caos, Lule é 100% polarização. Ele distribui promessas e golpes de oportunismo com a mesma fluidez."),
    
    /** * ARQUÉTIPO: Glass Cannon (Canhão de Vidro).
     * A N.I.T.A., Nova Influência do Tesão e da Audácia, chegou pra mudar o jogo.
     * No ringue ou no palco, ela domina com ritmo, charme e poder de nocaute.
     */
    NITA("Nita", 360f, 45f, 5f, 240, 248, 
         "/sprites/Nita/idleNita.gif", 
         "A N.I.T.A., Nova Influência do Tesão e da Audácia, chegou pra mudar o jogo. No ringue ou no palco, ela domina com ritmo, charme e poder de nocaute."),
    
    /** * ARQUÉTIPO: Tank (Tanque).
     * Murissoca, o Prof. P*tasso. Armado de giz, memes e polêmicas, ele leciona
     * soco com viés de direita. Sua didática? Uma voadora pedagógica!
     */
    MURISSOCA("Murissoca", 460f, 28f, 20f, 240, 248, 
                "/sprites/Murissoca/idleMurissoca.gif", 
                "Murissoca, o Prof. P*tasso. Armado de giz, memes e polêmicas, ele leciona soco com viés de direita. Sua didática? Uma voadora pedagógica!"),
    
    /** * ARQUÉTIPO: Balanced / Defensive.
     * TeleTony, o Guerreiro do SAC e do Sofrimento. Cansado da vida, do script
     * e da musiquinha de espera, ele luta sem vontade, mas com profundo rancor.
     */
    TELETONY("Teletony", 420f, 32f, 15f, 240, 248, 
               "/sprites/Teletony/idleTeletony.gif", 
               "TeleTony, o Guerreiro do SAC e do Sofrimento. Cansado da vida, do script e da musiquinha de espera, ele luta sem vontade, mas com profundo rancor.");

    // --- Atributos de dados de cada personagem ---
    /** O nome de exibição. */
    private final String name;
    /** Stats base: vida, força, defesa. */
    private final float maxLife, strength, defense;
    /** Dimensões (em pixels) para o sprite/hitbox. */
    private final int width, height;
    /** Caminho para o recurso do sprite "parado" (idle). */
    private final String idleSpritePath;
    /** Texto de biografia para a tela de seleção. */
    private final String description;

    /**
     * Construtor privado do Enum para armazenar os dados de cada personagem.
     */
    CharacterType(String name, float maxLife, float strength, float defense, 
                    int width, int height, String idleSpritePath, String description) {
      this.name = name;
      this.maxLife = maxLife;
      this.strength = strength;
      this.defense = defense;
      this.width = width;
      this.height = height;
      this.idleSpritePath = idleSpritePath;
      this.description = description;
    }

    // --- GETTERS PÚBLICOS DE DADOS ---

    /** @return O nome de exibição do personagem. */
    public String getName() { return name; }
    
    /** @return A vida máxima base do personagem. */
    public float getMaxLife() { return maxLife; }
    
    /** @return A força base do personagem. */
    public float getStrength() { return strength; }
    
    /** @return A defesa base do personagem. */
    public float getDefense() { return defense; }
    
    /** @return A largura (em pixels) da hitbox do personagem. */
    public int getWidth() { return width; }
    
    /** @return A altura (em pixels) da hitbox do personagem. */
    public int getHeight() { return height; }
    
    /** @return O caminho do recurso para o sprite "idle" (parado). */
    public String getIdleSpritePath() { return idleSpritePath; }
    
    /** @return O texto de descrição (biografia) do personagem. */
    public String getDescription() { return description; }
  }

  // --- MÉTODO DA FÁBRICA ---

  /**
   * Cria uma nova instância de {@link Character} com base no tipo selecionado.
   * Este é o método principal da fábrica, que desacopla os dados
   * (CharacterType) do modelo de jogo (Character).
   *
   * @param type O {@link CharacterType} que define os atributos do personagem.
   * @return Um objeto Character pronto para ser usado no jogo.
   */
  public static Character createCharacter(CharacterType type) {
    return new Character(
      type.getName(), 
      type.getMaxLife(), 
      type.getStrength(), 
      type.getDefense(), 
      type.getWidth(), 
      type.getHeight()
    );
  }
}
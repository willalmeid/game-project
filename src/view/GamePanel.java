package view;

import model.Game;
import model.Player;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * A View para a tela principal do jogo (a arena de luta).
 * É responsável por desenhar o cenário, os jogadores, a interface (HUD) e as
 * sobreposições de estado, aplicando uma escala para preencher a janela.
 */
public class GamePanel extends JPanel {

  // --- Constantes de Resolução e Layout ---
  /** A largura de exibição do painel (janela). */
  private static final int DISPLAY_WIDTH = 1024;
  /** A altura de exibição do painel (janela). */
  private static final int DISPLAY_HEIGHT = 768;
  /** A largura "lógica" do jogo (na qual a simulação roda). */
  private static final int LOGICAL_WIDTH = 800;
  /** A altura "lógica" do jogo (na qual a simulação roda). */
  private static final int LOGICAL_HEIGHT = 600;
  
  // --- Constantes de Design ---
  /** Flag para desenhar hitboxes de depuração. Mude para 'true' para depurar. */
  private static final boolean DRAW_DEBUG_HITBOXES = false;
  /** Cor para as sobreposições de "Round Over" e "Game Over". */
  private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 180);
  /** Cor principal dos botões e UI. */
  private static final Color THEME_COLOR = new Color(108, 60, 2);
  /** Cor dos botões ao passar o mouse (hover). */
  private static final Color HOVER_COLOR = new Color(80, 0, 0);

  // --- Constantes da HUD (baseadas na resolução lógica de 800x600) ---
  private static final int HUD_MARGIN_X = 20;
  private static final int HUD_MARGIN_Y = 50;
  private static final int HEALTH_BAR_WIDTH = 300;
  private static final int HEALTH_BAR_HEIGHT = 20;
  private static final int SPECIAL_BAR_HEIGHT = 8;
  private static final int WIN_INDICATOR_SIZE = 15;
  private static final int WIN_INDICATOR_SPACING = 20;

  // --- Fontes pré-criadas para otimização ---
  private static final Font NAME_FONT = new Font("Arial", Font.BOLD, 20);
  private static final Font TIMER_FONT = new Font("Arial", Font.BOLD, 40);
  private static final Font ROUND_OVER_FONT = new Font("Arial", Font.BOLD, 50);
  private static final Font GAME_OVER_FONT = new Font("Arial", Font.BOLD, 60);
  private static final Font RESTART_FONT = new Font("Arial", Font.PLAIN, 24);
  private static final Font ACTION_BUTTON_FONT = new Font("Tahoma", Font.BOLD, 18);

  // --- Referências e Componentes ---
  private final Game game;
  private final Image backgroundImage;
  private final JButton restartButton;
  private final JButton exitButton;

  /**
   * Constrói o painel principal do jogo.
   * @param game A instância do modelo 'Game' contendo o estado da partida.
   */
  public GamePanel(Game game) {
    this.game = game;
    setFocusable(true); // Essencial para capturar o KeyListener
    setLayout(null); // Usamos layout absoluto para os botões de fim de jogo
    setPreferredSize(new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT));
    
    this.backgroundImage = loadBackgroundImage();
    
    // Posições dos botões calculadas para o centro da tela (Display)
    this.restartButton = createActionButton("Novo Jogo", (DISPLAY_WIDTH - 150) / 2, 390);
    this.exitButton = createActionButton("Sair", (DISPLAY_WIDTH - 150) / 2, 460);

    add(restartButton);
    add(exitButton);
  }

  /**
   * O método de desenho principal, chamado pelo game loop (via repaint()).
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    
    // Salva o estado original dos gráficos (sem escala)
    AffineTransform originalTransform = g2d.getTransform();
    
    // --- Início do Desenho Escalonado (Mundo do Jogo) ---
    {
      // Calcula os fatores de escala
      double scaleX = (double) getWidth() / LOGICAL_WIDTH;
      double scaleY = (double) getHeight() / LOGICAL_HEIGHT;
      
      // Aplica a escala
      g2d.scale(scaleX, scaleY);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Desenha o mundo do jogo (que pensa em 800x600)
      drawBackground(g2d);
      drawPlayer(g2d, game.getPlayer1());
      drawPlayer(g2d, game.getPlayer2());
      drawHUD(g2d);

      // Desenha a sobreposição de "Round Over" (escalonada)
      if (game.getCurrentState() == Game.GameState.ROUND_OVER) {
          drawRoundOverOverlay(g2d);
      }
    }
    // --- Fim do Desenho Escalonado ---
    
    // Restaura a transformação original para desenhar a UI (botões, etc.)
    g2d.setTransform(originalTransform);
    
    // --- Início do Desenho Não Escalonado (UI de Tela Cheia) ---
    {
      // A sobreposição de "Game Over" e os botões são desenhados em 1024x768
      if (game.getCurrentState() == Game.GameState.GAME_OVER) {
          drawGameOverOverlay(g2d); // Desenha o overlay de fundo
          restartButton.setVisible(true); // Mostra o botão
          exitButton.setVisible(true);  // Mostra o botão
      } else {
          // Garante que os botões estejam escondidos
          restartButton.setVisible(false);
          exitButton.setVisible(false);
      }
    }
    // --- Fim do Desenho Não Escalonado ---
  }

  // --- GETTERS (API para o Controller) ---

  public JButton getRestartButton() { return restartButton; }
  public JButton getExitButton() { return exitButton; }

  // --- MÉTODOS PRIVADOS DE CRIAÇÃO (Helpers de Inicialização) ---

  /**
   * Carrega o plano de fundo correto baseado no estado do Jogo.
   * Usa o ImageCache para eficiência.
   */
  private Image loadBackgroundImage() {
    String bgPath;
    switch (game.getBackground()) {
        case MANHA: bgPath = "/backgrounds/manha.png"; break;
        case TARDE: bgPath = "/backgrounds/tarde.png"; break;
        case NOITE: default: bgPath = "/backgrounds/noite.png"; break;
    }
    return ImageCache.getStaticImage(bgPath);
  }

  /**
   * Cria e formata um botão de ação (Novo Jogo, Sair).
   */
  private JButton createActionButton(String text, int x, int y) {
      JButton button = new JButton(text);
      button.setBounds(x, y, 150, 50);
      button.setFont(ACTION_BUTTON_FONT);
      button.setForeground(Color.WHITE);
      button.setBackground(THEME_COLOR);
      button.setBorderPainted(false);
      button.setFocusPainted(false);
      button.setCursor(new Cursor(Cursor.HAND_CURSOR));
      button.setVisible(false); // Começa escondido
      
      // Efeito Hover
      button.addMouseListener(new java.awt.event.MouseAdapter() {
          public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(HOVER_COLOR); }
          public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(THEME_COLOR); }
      });
      return button;
  }

  // --- MÉTODOS PRIVADOS DE DESENHO (Chamados a cada frame) ---

  /**
   * Desenha o plano de fundo (escalonado).
   */
  private void drawBackground(Graphics2D g2d) {
    if (backgroundImage != null) {
      g2d.drawImage(backgroundImage, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT, null);
    } else {
      // Fallback se a imagem falhar
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.fillRect(0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT);
    }
  }

  /**
   * Desenha um único jogador (escalonado).
   */
  private void drawPlayer(Graphics2D g2d, Player player) {
    Rectangle body = player.getBodyHitbox();
    ImageIcon currentFrame = player.getAppearance().getCurrentFrame();
    
    // Desenha "auras" de estado
    if (player.isDefending() || player.isCrouching()) {
      Color auraColor = player.isDefending() ? new Color(100, 100, 255, 120) : new Color(200, 200, 100, 100);
      g2d.setColor(auraColor);
      g2d.fillOval(body.x - 5, body.y - 5, body.width + 10, body.height + 10);
    }

    // Desenha o sprite do personagem
    if (currentFrame != null) {
      int drawX = player.getX(), drawY = player.getY();
      int drawWidth = player.getCharacter().getWidth(), drawHeight = player.getCharacter().getHeight();
      
      // Lógica para inverter o sprite
      if (player.getDirection() == -1) {
        g2d.drawImage(currentFrame.getImage(), drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
      } else {
        g2d.drawImage(currentFrame.getImage(), drawX, drawY, drawWidth, drawHeight, null);
      }
    } else {
      // Fallback se a animação falhar
      Color baseColor = player.getColor();
      Color placeholderColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 80);
      g2d.setColor(placeholderColor);
      g2d.fillRect(body.x, body.y, body.width, body.height);
    }
    
    // --- HITBOX DE DEPURAÇÃO ---
    // (Otimização) Bloco descomentado, controlado pela flag.
    if (DRAW_DEBUG_HITBOXES) {
      if (player.isHitStunned()) { g2d.setColor(new Color(255, 255, 255, 150)); g2d.fillRect(body.x, body.y, body.width, body.height); }
      g2d.setColor(new Color(0, 255, 0, 150));
      g2d.drawRect(body.x, body.y, body.width, body.height);
      Rectangle attackHitbox = player.getCurrentAttackHitbox();
      if (attackHitbox != null) { g2d.setColor(new Color(255, 0, 0, 150)); g2d.fillRect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height); }
    }
  }

  /**
   * Desenha a Interface do Usuário (HUD): barras de vida, especial, timer, etc. (escalonado).
   */
  private void drawHUD(Graphics2D g2d) {
    drawHealthBar(g2d, game.getPlayer1(), HUD_MARGIN_X, HUD_MARGIN_Y, false, game.getP1Wins());
    drawHealthBar(g2d, game.getPlayer2(), LOGICAL_WIDTH - HEALTH_BAR_WIDTH - HUD_MARGIN_X, HUD_MARGIN_Y, true, game.getP2Wins());

    // Desenha o Timer
    g2d.setFont(TIMER_FONT);
    g2d.setColor(Color.WHITE);
    String timerText = String.valueOf(Math.max(0, game.getGameTimer() / 60));
    FontMetrics fm = g2d.getFontMetrics();
    g2d.drawString(timerText, (LOGICAL_WIDTH - fm.stringWidth(timerText)) / 2, 55);
  }

  /**
   * Método auxiliar para desenhar a barra de vida completa de um jogador.
   */
  private void drawHealthBar(Graphics2D g2d, Player p, int x, int y, boolean mirrored, int wins) {
    model.Character character = p.getCharacter();
    float lifePercent = character.getLife() / character.getMaxLife();

    // Nome
    g2d.setFont(NAME_FONT);
    g2d.setColor(Color.WHITE);
    g2d.drawString(character.getName(), x, y - 10);
    
    // Fundo da barra de vida
    g2d.setColor(Color.DARK_GRAY);
    g2d.fillRect(x, y, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
    
    // Vida atual (com cor dinâmica)
    Color lifeColor = (lifePercent > 0.5f) ? Color.GREEN : (lifePercent > 0.1f) ? Color.YELLOW : Color.RED;
    g2d.setColor(lifeColor);
    int currentLifeWidth = (int) (HEALTH_BAR_WIDTH * lifePercent);
    if (mirrored) g2d.fillRect(x + (HEALTH_BAR_WIDTH - currentLifeWidth), y, currentLifeWidth, HEALTH_BAR_HEIGHT);
    else g2d.fillRect(x, y, currentLifeWidth, HEALTH_BAR_HEIGHT);
    
    // Barra de Especial
    int specialY = y + HEALTH_BAR_HEIGHT + 5;
    float specialPercent = p.getSpecialMeter() / p.getMaxSpecial();
    g2d.setColor(Color.DARK_GRAY);
    g2d.fillRect(x, specialY, HEALTH_BAR_WIDTH, SPECIAL_BAR_HEIGHT);
    if (specialPercent > 0) {
      // Efeito de "piscar" quando está cheia
      if (specialPercent >= 1.0f && (System.currentTimeMillis() / 150) % 2 == 0) g2d.setColor(Color.CYAN);
      else g2d.setColor(Color.BLUE);
      
      int currentSpecialWidth = (int) (HEALTH_BAR_WIDTH * specialPercent);
      g2d.fillRect(x, specialY, currentSpecialWidth, SPECIAL_BAR_HEIGHT);
    }

    // Indicadores de Vitória (Wins)
    int winsY = specialY + SPECIAL_BAR_HEIGHT + 5; 
    g2d.setColor(Color.YELLOW);
    for (int i = 0; i < wins; i++) {
      int winX = mirrored ? (x + HEALTH_BAR_WIDTH - WIN_INDICATOR_SIZE) - (i * WIN_INDICATOR_SPACING) : x + (i * WIN_INDICATOR_SPACING);
      g2d.fillOval(winX, winsY, WIN_INDICATOR_SIZE, WIN_INDICATOR_SIZE);
    }
  }

  /**
   * Desenha a sobreposição de "Round Over" (escalonado).
   */
  private void drawRoundOverOverlay(Graphics2D g2d) {
    // (Otimização) Ativa o antialiasing de texto
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    
    g2d.setColor(OVERLAY_COLOR);
    g2d.fillRect(0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT);
    
    String winnerText = game.getRoundWinnerName().equalsIgnoreCase("Empate") ? "Empate!" : game.getRoundWinnerName() + " venceu o round!";
    drawCenteredText(g2d, ROUND_OVER_FONT, Color.WHITE, winnerText, -20);
    drawCenteredText(g2d, RESTART_FONT, Color.WHITE, "Pressione Enter para o próximo round", 30);
  }

  /**
   * Desenha a sobreposição de "Game Over" (NÃO escalonado).
   */
  private void drawGameOverOverlay(Graphics2D g2d) {
    // (Otimização) Ativa o antialiasing de texto
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    
    g2d.setColor(OVERLAY_COLOR);
    g2d.fillRect(0, 0, getWidth(), getHeight()); // Usa o tamanho real da janela
    
    String winnerText = game.getMatchWinnerName().equalsIgnoreCase("Empate") ? "Empate!" : game.getMatchWinnerName() + " Venceu!";
    
    g2d.setFont(GAME_OVER_FONT);
    g2d.setColor(Color.YELLOW);
    FontMetrics fm = g2d.getFontMetrics();
    g2d.drawString(winnerText, (getWidth() - fm.stringWidth(winnerText)) / 2, getHeight() / 2 - 50);
  }
  
  /**
   * Método auxiliar para desenhar texto centralizado no eixo X (lógico).
   */
  private void drawCenteredText(Graphics2D g2d, Font font, Color color, String text, int yOffset) {
    g2d.setFont(font);
    g2d.setColor(color);
    FontMetrics fm = g2d.getFontMetrics();
    g2d.drawString(text, (LOGICAL_WIDTH - fm.stringWidth(text)) / 2, LOGICAL_HEIGHT / 2 + yOffset);
  }
}
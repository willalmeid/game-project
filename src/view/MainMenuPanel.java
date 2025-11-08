package view;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

/**
 * A View (Painel) para o Menu Principal.
 * Exibe o título e as opções de "Iniciar" e "Sair".
 */
public class MainMenuPanel extends JPanel {
  
  private static final int PANEL_WIDTH = 1024;
  private static final int PANEL_HEIGHT = 768;
  private static final int TITLE_Y = 50;
  private static final int START_BUTTON_Y = 380;
  private static final int EXIT_BUTTON_Y = 490;
  
  private final JButton startButton;
  private final JButton exitButton;
  private final JLabel titleLabel;
  private final Image backgroundImage;

  /**
   * Constrói o painel do Menu Principal, carregando
   * recursos visuais e configurando os botões.
   */
  public MainMenuPanel() {
    setLayout(null);
    setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    
    // (Otimização) Usa o ImageCache
    this.backgroundImage = ImageCache.getStaticImage("/backgrounds/background.png");
    
    // Configura os componentes visuais
    this.titleLabel = setupTitle();
    
    // (Otimização) Usa um helper unificado para os botões
    this.startButton = createHoverButton(
      ImageCache.getStaticImage("/buttons/startButton.png"),
      ImageCache.getStaticImage("/buttons/startButtonHover.png"),
      START_BUTTON_Y
    );
    this.exitButton = createHoverButton(
      ImageCache.getStaticImage("/buttons/exitButton.png"),
      ImageCache.getStaticImage("/buttons/exitButtonHover.png"),
      EXIT_BUTTON_Y
    );

    add(titleLabel);
    add(startButton);
    add(exitButton);
  }

  /**
   * Desenha o plano de fundo do painel.
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (backgroundImage != null) {
      g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
  }
  
  // --- GETTERS (Para o Controller) ---
  
  public JButton getStartButton() { return startButton; }
  public JButton getExitButton() { return exitButton; }
  public JLabel getTitleLabel() { return titleLabel; }
  
  // --- MÉTODOS AUXILIARES DE CRIAÇÃO (Helpers) ---
  
  /**
   * Configura o JLabel do título.
   * (CORREÇÃO DE BUG) Define como não-opaco (transparente)
   * para evitar glitches na animação de pulso.
   */
  private JLabel setupTitle() {
    Image titleImg = ImageCache.getStaticImage("/buttons/title.png");
    ImageIcon titleIcon = new ImageIcon(titleImg);
    
    JLabel label = new JLabel(titleIcon);
    int titleX = (PANEL_WIDTH - titleIcon.getIconWidth()) / 2;
    label.setBounds(titleX, TITLE_Y, titleIcon.getIconWidth(), titleIcon.getIconHeight());
    
    // (A CORREÇÃO DO BUG ESTÁ AQUI)
    label.setOpaque(false); // Garante que o fundo do JLabel não seja desenhado
    
    return label;
  }

  /**
   * (Otimização) Método auxiliar unificado para criar botões
   * com efeito de "hover" (RolloverIcon).
   *
   * @param icon Imagem padrão do botão.
   * @param hoverIcon Imagem para quando o mouse estiver sobre o botão.
   * @param y A posição Y do botão.
   * @return Um JButton configurado.
   */
  private JButton createHoverButton(Image icon, Image hoverIcon, int y) {
    ImageIcon normalIcon = new ImageIcon(icon);
    ImageIcon rolloverIcon = new ImageIcon(hoverIcon);
    
    JButton button = new JButton(normalIcon);
    button.setRolloverIcon(rolloverIcon);
    
    int x = (PANEL_WIDTH - normalIcon.getIconWidth()) / 2;
    
    // Configurações visuais do botão
    button.setBounds(x, y, normalIcon.getIconWidth(), normalIcon.getIconHeight());
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setOpaque(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    return button;
  }
}
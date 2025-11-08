package view;

import javax.swing.*;
import java.awt.*;

/**
 * A View (Painel) para a tela de Seleção de Personagens.
 * Define o layout e os componentes visuais da tela.
 */
public class CharacterSelectionPanel extends JPanel {
    
  // --- Constantes de Layout para 1024x768 ---
  private static final int PANEL_WIDTH = 1024;
  private static final int PANEL_HEIGHT = 768;
  private static final Color THEME_COLOR = new Color(108, 60, 2);
  private static final Color HOVER_COLOR = new Color(80, 0, 0);
  private static final Font TITLE_FONT = new Font("Tahoma", Font.BOLD, 51);
  private static final Font SUBTITLE_FONT = new Font("Tahoma", Font.BOLD, 24);
  private static final Font VERSUS_FONT = new Font("Tahoma", Font.BOLD, 41);
  private static final Font ACTION_BUTTON_FONT = new Font("Tahoma", Font.BOLD, 18);
  private static final Font DESCRIPTION_FONT = new Font("Tahoma", Font.PLAIN, 20);
  
  // Constantes de posicionamento dos retratos
  private static final int CHAR_BUTTON_Y = 180;
  private static final int CHAR_BUTTON_SIZE = 140;
  private static final int CHAR_BUTTON_GAP = 30;

  private final JLabel labelTitle, labelSubtitle, labelVersus;
  private final Image backgroundImage;
  private final JButton buttonPerson1, buttonPerson2, buttonPerson3, buttonPerson4, buttonPerson5;
  private final JButton buttonStart, buttonConfirm;
  private final JLabel panelPlayer1, panelPlayer2;
  private final JTextArea description;
  
  /**
   * Constrói o painel de seleção de personagens,
   * inicializando e posicionando todos os componentes da UI.
   */
  public CharacterSelectionPanel() {
    setLayout(null);
    setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));

    // (Otimização) Usa o ImageCache para carregar o plano de fundo
    this.backgroundImage = ImageCache.getStaticImage("/backgrounds/backgroundSelection.png");
    
    // Inicialização dos componentes
    this.labelTitle = createLabel("Seleção", TITLE_FONT, THEME_COLOR, (PANEL_WIDTH - 210) / 2, 60, 210, 62);
    this.labelSubtitle = createLabel("Jogador 1, escolha seu personagem.", SUBTITLE_FONT, THEME_COLOR, 0, 130, PANEL_WIDTH, 29);
    this.labelSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
    this.labelVersus = createLabel("vs", VERSUS_FONT, THEME_COLOR, (PANEL_WIDTH - 58) / 2, 420, 58, 50);
    this.labelVersus.setHorizontalAlignment(SwingConstants.CENTER);

    // Lógica para centralizar o bloco de personagens
    int totalBlockWidth = (CHAR_BUTTON_SIZE * 5) + (CHAR_BUTTON_GAP * 4);
    int startX = (PANEL_WIDTH - totalBlockWidth) / 2;
    
    // (Otimização) Botões agora usam ImageCache (dentro do helper)
    this.buttonPerson3 = createCharButton("/buttons/murissoca.png", startX, CHAR_BUTTON_Y);
    this.buttonPerson1 = createCharButton("/buttons/nita.png", startX + (CHAR_BUTTON_SIZE + CHAR_BUTTON_GAP), CHAR_BUTTON_Y);
    this.buttonPerson2 = createCharButton("/buttons/isagram.png", startX + (CHAR_BUTTON_SIZE + CHAR_BUTTON_GAP) * 2, CHAR_BUTTON_Y);
    this.buttonPerson4 = createCharButton("/buttons/teletony.png", startX + (CHAR_BUTTON_SIZE + CHAR_BUTTON_GAP) * 3, CHAR_BUTTON_Y);
    this.buttonPerson5 = createCharButton("/buttons/lule.png", startX + (CHAR_BUTTON_SIZE + CHAR_BUTTON_GAP) * 4, CHAR_BUTTON_Y);

    this.buttonConfirm = createActionButton("Selecionar", (PANEL_WIDTH - 150) / 2, 500);
    this.buttonStart = createActionButton("Começar", (PANEL_WIDTH - 150) / 2, 560);
    
    this.panelPlayer1 = createPreviewPanel(160, 360);
    this.panelPlayer2 = createPreviewPanel(PANEL_WIDTH - 160 - 220, 360);
    
    this.description = createDescriptionArea();
    
    // Adiciona todos os componentes ao painel
    add(labelTitle); add(labelSubtitle); add(labelVersus);
    add(buttonPerson3); add(buttonPerson1); add(buttonPerson2); add(buttonPerson4); add(buttonPerson5);
    add(buttonConfirm); add(buttonStart);
    add(panelPlayer1); add(panelPlayer2);
    add(description);
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

  // --- MÉTODOS DE CONTROLE DA VIEW ---

  /** Define a imagem de pré-visualização do Jogador 1. */
  public void setProfileImageP1(ImageIcon icon) { panelPlayer1.setIcon(icon); }
  
  /** Define a imagem de pré-visualização do Jogador 2. */
  public void setProfileImageP2(ImageIcon icon) { panelPlayer2.setIcon(icon); }

  // --- GETTERS PARA O CONTROLLER ---

  public JLabel getLabelSubtitle() { return labelSubtitle; }
  public JButton getButtonPerson1() { return buttonPerson1; }
  public JButton getButtonPerson2() { return buttonPerson2; }
  public JButton getButtonPerson3() { return buttonPerson3; }
  public JButton getButtonPerson4() { return buttonPerson4; }
  public JButton getButtonPerson5() { return buttonPerson5; }
  public JButton getButtonStart() { return buttonStart; }
  public JButton getButtonConfirm() { return buttonConfirm; }
  public JTextArea getAreaDescription() { return description; }

  // --- MÉTODOS AUXILIARES DE CRIAÇÃO (Helpers) ---

  /**
   * Cria e formata um JLabel padrão para esta tela.
   */
  private JLabel createLabel(String text, Font font, Color color, int x, int y, int w, int h) {
    JLabel label = new JLabel(text);
    label.setBounds(x, y, w, h);
    label.setFont(font);
    label.setForeground(color);
    return label;
  }

  /**
   * Cria e formata um botão de seleção de personagem (retrato).
   * (Otimização) Agora usa o ImageCache.
   */
  private JButton createCharButton(String iconPath, int x, int y) {
    Image icon = ImageCache.getStaticImage(iconPath);
    JButton button = new JButton(new ImageIcon(icon));
    
    button.setBounds(x, y, CHAR_BUTTON_SIZE, CHAR_BUTTON_SIZE);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    return button;
  }

  /**
   * Cria e formata um botão de ação (Selecionar, Começar).
   */
  private JButton createActionButton(String text, int x, int y) {
    JButton button = new JButton(text);
    button.setBounds(x, y, 150, 50);
    button.setEnabled(false); // Começa desabilitado
    button.setFont(ACTION_BUTTON_FONT);
    button.setForeground(Color.WHITE);
    button.setBackground(THEME_COLOR);
    button.setBorderPainted(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    // Efeito Hover (Mouse over)
    button.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        button.setBackground(HOVER_COLOR);
      }
      public void mouseExited(java.awt.event.MouseEvent evt) {
        button.setBackground(THEME_COLOR);
      }
    });
    return button;
  }

  /**
   * Cria um painel (JLabel) para exibir a pré-visualização do personagem.
   */
  private JLabel createPreviewPanel(int x, int y) {
    JLabel panel = new JLabel();
    panel.setBounds(x, y, 220, 220);
    // (Opcional: adicionar uma borda para debug)
    // panel.setBorder(BorderFactory.createLineBorder(Color.RED));
    return panel;
  }

  /**
   * Cria e formata a área de texto para a descrição do personagem.
   */
  private JTextArea createDescriptionArea() {
    JTextArea area = new JTextArea();
    area.setBackground(new Color(238, 238, 238));
    area.setBounds(62, 630, PANEL_WIDTH - 124, 100);
    area.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
    area.setFont(DESCRIPTION_FONT);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setMargin(new Insets(10, 10, 10, 10));
    area.setFocusable(false); // Impede que o usuário digite nela
    return area;
  }
}
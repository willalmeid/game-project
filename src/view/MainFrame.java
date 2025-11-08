package view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Component; // Importação necessária
import java.awt.event.KeyListener;

/**
 * A janela principal (JFrame) da aplicação.
 * É responsável por conter e trocar os diferentes painéis (JPanels) 
 * do jogo, como o Menu Principal, a Tela de Seleção e o Painel do Jogo.
 */
public class MainFrame extends JFrame {

  /**
   * Constrói a janela principal do jogo.
   * @param title O texto a ser exibido na barra de título da janela.
   */
  public MainFrame(String title) {
    setTitle(title);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
  }

  /**
   * Define o painel atual a ser exibido na janela.
   * Este método gerencia a limpeza de listeners e a solicitação de foco.
   *
   * @param panel O JPanel que deve ser exibido.
   */
  public void showPanel(JPanel panel) {
    // Limpa listeners do painel *antigo* para evitar conflitos
    clearKeyListeners(); 
    
    // Define o novo painel
    setContentPane(panel);
    pack(); // Ajusta a janela ao setPreferredSize() do novo painel
    setLocationRelativeTo(null); // Centraliza a janela
    
    // Essencial para que o KeyListener do painel funcione
    panel.requestFocusInWindow(); 
    
    revalidate(); // Atualiza a hierarquia de componentes
    repaint();    // Redesenha a janela
  }

  /**
   * Limpa todos os KeyListeners do *painel de conteúdo atual*.
   * Isso é crucial para desativar o controle do jogo ao voltar para um menu.
   */
  public void clearKeyListeners() {
    // Pega o painel que está ATUALMENTE na janela (o antigo)
    Component currentPanel = getContentPane();
    
    if (currentPanel != null) {
      // Remove todos os KeyListeners desse painel
      for (KeyListener kl : currentPanel.getKeyListeners()) {
        currentPanel.removeKeyListener(kl);
      }
    }
  }
}
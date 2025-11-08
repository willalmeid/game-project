package controller;

import model.CharacterFactory;
import model.Game;
import view.*;
import audio.SoundManager;

/**
 * Ponto de entrada da aplicação (Maestro).
 * Gerencia a criação da janela principal (MainFrame) e a transição
 * entre os painéis (Menu, Seleção, Jogo).
 * Também gerencia a instância única e global do SoundManager.
 */
public class GameLauncher {

    /** A janela principal (JFrame) que contém todos os painéis. */
    private final MainFrame mainFrame;
    
    /** A instância única do SoundManager, compartilhada por toda a aplicação. */
    private final SoundManager soundManager;

    /**
     * Constrói o GameLauncher, inicializa o SoundManager e a janela principal,
     * e exibe o menu principal.
     */
    public GameLauncher() {
        this.mainFrame = new MainFrame("CLT: Caos, Luta e Treta");
        
        // O SoundManager é criado uma única vez e injetado nos controladores
        this.soundManager = new SoundManager(); 
        
        showMainMenu();
        mainFrame.setVisible(true);
    }

    /**
     * Cria e exibe o painel do Menu Principal, injetando as dependências
     * (incluindo o SoundManager) em seu controlador.
     */
    public void showMainMenu() {
        MainMenuPanel mainMenuPanel = new MainMenuPanel();
        
        // O callback 'this::showSelectionScreen' é passado para o controller
        new MainMenuController(mainMenuPanel, this::showSelectionScreen, soundManager);
        
        mainFrame.showPanel(mainMenuPanel);
    }

    /**
     * Cria e exibe o painel de Seleção de Personagens, injetando as dependências
     * (incluindo o SoundManager) em seu controlador.
     */
    public void showSelectionScreen() {
        CharacterSelectionPanel selectionPanel = new CharacterSelectionPanel();
        
        // O callback 'this::startGame' é passado para o controller
        new SelectionController(selectionPanel, this::startGame, soundManager);
        
        mainFrame.showPanel(selectionPanel);
    }

    /**
     * Cria e exibe o painel principal do Jogo, configurando o modelo, 
     * o controlador e os listeners de input.
     *
     * @param p1 O {@link CharacterFactory.CharacterType} escolhido pelo Jogador 1.
     * @param p2 O {@link CharacterFactory.CharacterType} escolhido pelo Jogador 2.
     */
    public void startGame(CharacterFactory.CharacterType p1, CharacterFactory.CharacterType p2) {
        // 1. Cria o Modelo e a View
        Game game = new Game(p1, p2);
        GamePanel gamePanel = new GamePanel(game);
        
        // 2. Cria o Controlador e injeta as dependências
        GameController gameController = new GameController(game, gamePanel, soundManager);

        // 3. Conecta os botões de "Game Over" (da View) a ações do Launcher
        gamePanel.getRestartButton().addActionListener(e -> {
            gameController.stopGameLoop(); // Para o loop do jogo antigo
            showSelectionScreen();         // Volta para a tela de seleção
        });
        gamePanel.getExitButton().addActionListener(e -> System.exit(0));

        // 4. Gerencia o foco do input (KeyListener)
        mainFrame.clearKeyListeners(); // Limpa listeners do painel anterior
        gamePanel.addKeyListener(gameController); // Adiciona o novo
        
        // 5. Exibe o painel do jogo
        mainFrame.showPanel(gamePanel);
        
        // Garante que o painel tenha foco para receber o input
        gamePanel.requestFocusInWindow(); 
        
        // 6. Inicia o loop do jogo
        gameController.startGameLoop();
    }

    /**
     * Ponto de entrada principal da aplicação.
     * Garante que a UI seja criada na Thread de Eventos do Swing (EDT).
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        // Garante que todo o código Swing rode na Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(GameLauncher::new);
    }
}
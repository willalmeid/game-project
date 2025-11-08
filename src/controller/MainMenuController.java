package controller;

import view.MainMenuPanel;
import audio.SoundManager;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Runnable;
import javax.swing.JLabel;

/**
 * Controla a lógica do Menu Principal.
 * Gerencia a navegação (Iniciar, Sair), a música de fundo
 * e a animação "pulsante" do título.
 */
public class MainMenuController implements ActionListener {

    // --- Constantes da Animação do Título ---
    /** O atraso em milissegundos para cada quadro da animação (aprox. 33 FPS). */
    private static final int PULSE_TIMER_DELAY = 60;
    /** A escala máxima que o título atinge. */
    private static final float PULSE_MAX_SCALE = 1.05f;
    /** A escala mínima (original) do título. */
    private static final float PULSE_MIN_SCALE = 1.0f;
    /** A velocidade (incremento) da animação por quadro. */
    private static final float PULSE_STEP = 0.002f;

    // --- Componentes ---
    /** A referência para o painel (View) que este controlador gerencia. */
    private final MainMenuPanel view;
    /** O callback (ação) a ser executado quando o jogo deve iniciar. */
    private final Runnable onStartGame;
    /** A instância global do SoundManager. */
    private final SoundManager soundManager;
    
    /** O Timer do Swing que controla a animação do título. */
    private Timer animationTimer; 

    // --- Estado da Animação ---
    /** A escala de tamanho atual do título. */
    private float scale = 1.0f;
    /** A direção da animação (true = aumentando, false = diminuindo). */
    private boolean scalingUp = true;
    /** A referência direta ao JLabel do título (para performance). */
    private final JLabel titleLabel;
    /** A posição X original do título (para centralização). */
    private final int originalTitleX;
    /** A largura original do título. */
    private final int originalTitleWidth;
    /** A altura original do título. */
    private final int originalTitleHeight;

    /**
     * Constrói o controlador do Menu Principal.
     * @param view O painel (View) do menu.
     * @param onStartGame A ação (callback) a ser executada ao clicar em "Iniciar".
     * @param soundManager A instância GLOBAL do SoundManager.
     */
    public MainMenuController(MainMenuPanel view, Runnable onStartGame, SoundManager soundManager) {
        this.view = view;
        this.onStartGame = onStartGame;
        this.titleLabel = view.getTitleLabel();
        
        // Recebe a instância global do SoundManager
        this.soundManager = soundManager; 
        
        // Armazena as dimensões originais para o cálculo da animação
        this.originalTitleX = titleLabel.getX();
        this.originalTitleWidth = titleLabel.getWidth();
        this.originalTitleHeight = titleLabel.getHeight();
        
        addListeners();
        startTitleAnimation();
        
        soundManager.setMusicVolume(0.6f);
        soundManager.playMusic(SoundManager.SoundFiles.MENU_MUSIC);
    }

    /** Adiciona os listeners de ação aos botões da view. */
    private void addListeners() {
        view.getStartButton().addActionListener(this);
        view.getExitButton().addActionListener(this);
    }

    /**
     * Lida com os eventos de clique nos botões "Iniciar" e "Sair".
     * @param e O evento de ação.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == view.getStartButton()) {
            // Limpa os recursos deste painel antes de transicionar
            animationTimer.stop();
            soundManager.stopMusic();
            
            // Chama o callback para o GameLauncher mudar de tela
            onStartGame.run();
            
        } else if (source == view.getExitButton()) {
            // Fecha a aplicação
            System.exit(0);
        }
    }

    /**
     * Configura e inicia o Timer do Swing para a animação "pulsante" do título.
     */
    private void startTitleAnimation() {
        animationTimer = new Timer(PULSE_TIMER_DELAY, ev -> {
            // 1. Calcula a nova escala (efeito de "pulso")
            if (scalingUp) {
                scale += PULSE_STEP;
                if (scale >= PULSE_MAX_SCALE) {
                    scale = PULSE_MAX_SCALE;
                    scalingUp = false;
                }
            } else {
                scale -= PULSE_STEP;
                if (scale <= PULSE_MIN_SCALE) {
                    scale = PULSE_MIN_SCALE;
                    scalingUp = true;
                }
            }
            
            // 2. Calcula as novas dimensões
            int newWidth = (int) (originalTitleWidth * scale);
            int newHeight = (int) (originalTitleHeight * scale);
            
            // 3. Calcula o novo X para manter o título centralizado
            int newX = originalTitleX - (newWidth - originalTitleWidth) / 2;
            
            // 4. Aplica as novas dimensões ao JLabel
            titleLabel.setBounds(newX, titleLabel.getY(), newWidth, newHeight);
        });
        animationTimer.start();
    }
}
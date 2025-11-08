package controller;

import model.Game;
import view.GamePanel;
import audio.SoundManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.Timer;

/**
 * Controla o loop principal do jogo e o input do usuário (teclado).
 * Implementa ActionListener para o Game Loop (Timer).
 * Estende KeyAdapter para o input do teclado.
 */
public class GameController extends KeyAdapter implements ActionListener {
    
    // --- Referências de Componentes ---
    /** O modelo (lógica) do jogo. */
    private final Game game;
    /** O painel (view) onde o jogo é desenhado. */
    private final GamePanel gamePanel;
    /** O Timer do Swing que atua como o game loop principal. */
    private Timer gameLoopTimer;
    /** A instância global do SoundManager. */
    private final SoundManager soundManager;
    /** Instância de Random para sorteios (ex: música). */
    private final Random random = new Random();

    // --- Constantes do Jogo ---
    /** Atraso do Game Loop em ms (aprox. 60 FPS). */
    private static final int DELAY = 16; 
    /** Velocidade de movimento do jogador no chão (pixels/frame). */
    private static final int PLAYER_GROUND_SPEED = 5;
    /** Velocidade de movimento do jogador no ar (pixels/frame). */
    private static final int PLAYER_AIR_SPEED = 4;
    
    /** Lista de músicas de fundo para a luta. */
    private static final SoundManager.SoundFiles[] FIGHT_MUSIC_TRACKS = {
        SoundManager.SoundFiles.FIGHT_MUSIC_1,
        SoundManager.SoundFiles.FIGHT_MUSIC_2,
        SoundManager.SoundFiles.FIGHT_MUSIC_3
    };

    // --- Mapeamento de Teclas (Key Bindings) ---
    private static final int P1_MOVE_LEFT = KeyEvent.VK_A;
    private static final int P1_MOVE_RIGHT = KeyEvent.VK_D;
    private static final int P1_JUMP = KeyEvent.VK_W;
    private static final int P1_CROUCH = KeyEvent.VK_S;
    private static final int P1_PUNCH = KeyEvent.VK_F;
    private static final int P1_KICK = KeyEvent.VK_G;
    private static final int P1_SPECIAL = KeyEvent.VK_H;
    private static final int P1_DEFEND = KeyEvent.VK_SPACE;

    private static final int P2_MOVE_LEFT = KeyEvent.VK_LEFT;
    private static final int P2_MOVE_RIGHT = KeyEvent.VK_RIGHT;
    private static final int P2_JUMP = KeyEvent.VK_UP;
    private static final int P2_CROUCH = KeyEvent.VK_DOWN;
    private static final int P2_PUNCH = KeyEvent.VK_NUMPAD1;
    private static final int P2_KICK = KeyEvent.VK_NUMPAD2;
    private static final int P2_SPECIAL = KeyEvent.VK_NUMPAD3;
    private static final int P2_DEFEND = KeyEvent.VK_NUMPAD0;
    
    /** Tecla para avançar para o próximo round. */
    private static final int GAME_NEXT_ROUND = KeyEvent.VK_ENTER;

    // --- Estado do Input ---
    /** Conjunto de teclas *atualmente* pressionadas (para ações contínuas). */
    private final Set<Integer> pressedKeys = new HashSet<>();
    /** Conjunto de teclas que *já executaram* uma ação (para ações de "pressão única"). */
    private final Set<Integer> singlePressActions = new HashSet<>();
    
    /**
     * Constrói o controlador do jogo.
     * @param game O modelo (lógica) do jogo.
     * @param gamePanel O painel (view) onde o jogo é desenhado.
     * @param soundManager A instância GLOBAL do SoundManager.
     */
    public GameController(Game game, GamePanel gamePanel, SoundManager soundManager) {
        this.game = game;
        this.gamePanel = gamePanel;
        this.soundManager = soundManager;
        
        // Injeta o som no modelo (para os Players poderem tocar sons)
        this.game.setSoundManager(this.soundManager);
        
        soundManager.setMusicVolume(0.4f);
        soundManager.setSfxVolume(0.9f);
    }

    /**
     * Inicia o Timer do game loop e a música de fundo.
     */
    public void startGameLoop() {
        gameLoopTimer = new Timer(DELAY, this);
        gameLoopTimer.start();
        
        // Sorteia uma música de luta do array
        SoundManager.SoundFiles songToPlay = 
            FIGHT_MUSIC_TRACKS[random.nextInt(FIGHT_MUSIC_TRACKS.length)];
            
        soundManager.playMusic(songToPlay);
        soundManager.playSoundFX(SoundManager.SoundFiles.FIGHT_ANNOUNCE);
    }
    
    /**
     * Para o Timer do game loop e a música.
     */
    public void stopGameLoop() {
        if (gameLoopTimer != null) {
            gameLoopTimer.stop();
        }
        if (soundManager != null) {
            soundManager.stopMusic();
        }
    }

    /**
     * Callback do Timer (Game Loop). Chamado a cada 'DELAY' ms.
     * Este é o "coração" do jogo, responsável por atualizar e redesenhar.
     * @param e Evento do Timer (ignorado).
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // 1. Processa o input APENAS se o estado for PLAYING
        if (game.getCurrentState() == Game.GameState.PLAYING) {
            processContinuousInput();
        }
        
        // 2. Atualiza o modelo (ele cuida de TODOS os estados, inclusive KO_FREEZE)
        game.update();
        
        // 3. Desenha o resultado na tela
        gamePanel.repaint();
        
        // 4. Se o game.update() MUDOU o estado para GAME_OVER, para o loop.
        if (game.getCurrentState() == Game.GameState.GAME_OVER) {
            stopGameLoop();
        }
    }
    
    /**
     * Chamado quando uma tecla é pressionada.
     * Gerencia ações de "pressão única" (pulo, ataques) para evitar
     * auto-repeat do teclado.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // Lógica de "Próximo Round" (só funciona na tela de ROUND_OVER)
        if (keyCode == GAME_NEXT_ROUND && game.getCurrentState() == Game.GameState.ROUND_OVER) {
            game.startNextRound();
            return;
        }

        pressedKeys.add(keyCode);
        
        // Só processa ataques se o estado for PLAYING
        // e se a tecla já não tiver disparado uma ação
        if (game.getCurrentState() != Game.GameState.PLAYING || singlePressActions.contains(keyCode)) {
            return;
        }
        
        boolean actionTaken = false;

        // --- Lógica do Jogador 1 (Ações Únicas) ---
        if (pressedKeys.contains(P1_SPECIAL) && pressedKeys.contains(P1_KICK)) {
            if (!singlePressActions.contains(P1_SPECIAL) && !singlePressActions.contains(P1_KICK)) {
                game.getPlayer1().startSpecial();
                actionTaken = true;
                singlePressActions.add(P1_SPECIAL); // Marca ambas como usadas
                singlePressActions.add(P1_KICK);
            }
        }
        
        if (!actionTaken) {
            if (keyCode == P1_JUMP) { game.getPlayer1().startJump(); actionTaken = true; }
            else if (keyCode == P1_PUNCH) { game.getPlayer1().startPunch(); actionTaken = true; }
            else if (keyCode == P1_KICK) { game.getPlayer1().startKick(); actionTaken = true; }
        }

        // --- Lógica do Jogador 2 (Ações Únicas) ---
        if (pressedKeys.contains(P2_SPECIAL) && pressedKeys.contains(P2_KICK)) {
             if (!singlePressActions.contains(P2_SPECIAL) && !singlePressActions.contains(P2_KICK)) {
                game.getPlayer2().startSpecial();
                actionTaken = true;
                singlePressActions.add(P2_SPECIAL);
                singlePressActions.add(P2_KICK);
             }
        }
        
        if (!actionTaken) {
            if (keyCode == P2_JUMP) { game.getPlayer2().startJump(); actionTaken = true; }
            else if (keyCode == P2_PUNCH) { game.getPlayer2().startPunch(); actionTaken = true; }
            else if (keyCode == P2_KICK) { game.getPlayer2().startKick(); actionTaken = true; }
        }

        // Se qualquer ação foi tomada, marca a tecla como "usada"
        if (actionTaken) {
            singlePressActions.add(keyCode);
        }
    }

    /**
     * Chamado quando uma tecla é solta.
     * Limpa os estados de "pressão única" e "pressionada".
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        pressedKeys.remove(keyCode);
        singlePressActions.remove(keyCode); // Limpa a ação principal
        
        // Lógica especial de "combo": soltar QUALQUER tecla
        // do especial limpa o estado "usado" de AMBAS.
        if (keyCode == P1_SPECIAL || keyCode == P1_KICK) {
            singlePressActions.remove(P1_SPECIAL);
            singlePressActions.remove(P1_KICK);
        }
        if (keyCode == P2_SPECIAL || keyCode == P2_KICK) {
            singlePressActions.remove(P2_SPECIAL);
            singlePressActions.remove(P2_KICK);
        }
    }

    /**
     * Processa inputs contínuos (ações de "segurar" como andar, agachar).
     * Chamado a cada quadro (frame) pelo actionPerformed.
     */
    private void processContinuousInput() {
        // --- Lógica do Jogador 1 (Contínua) ---
        int p1Speed = game.getPlayer1().isJumping() ? PLAYER_AIR_SPEED : PLAYER_GROUND_SPEED;
        
        boolean p1IsMoving = false;
        if (pressedKeys.contains(P1_MOVE_LEFT)) { game.getPlayer1().move(-p1Speed); p1IsMoving = true; }
        if (pressedKeys.contains(P1_MOVE_RIGHT)) { game.getPlayer1().move(p1Speed); p1IsMoving = true; }
        if (!p1IsMoving) { game.getPlayer1().stopWalking(); } // (Correção de Bug)
        
        if (pressedKeys.contains(P1_CROUCH)) game.getPlayer1().startCrouching(); 
        else game.getPlayer1().endCrouching();
        
        if (pressedKeys.contains(P1_DEFEND)) game.getPlayer1().startDefending(); 
        else game.getPlayer1().endDefending();
        
        // --- Lógica do Jogador 2 (Contínua) ---
        int p2Speed = game.getPlayer2().isJumping() ? PLAYER_AIR_SPEED : PLAYER_GROUND_SPEED;
        
        boolean p2IsMoving = false;
        if (pressedKeys.contains(P2_MOVE_LEFT)) { game.getPlayer2().move(-p2Speed); p2IsMoving = true; }
        if (pressedKeys.contains(P2_MOVE_RIGHT)) { game.getPlayer2().move(p2Speed); p2IsMoving = true; }
        if (!p2IsMoving) { game.getPlayer2().stopWalking(); } // (Correção de Bug)
        
        if (pressedKeys.contains(P2_CROUCH)) game.getPlayer2().startCrouching(); 
        else game.getPlayer2().endCrouching();
        
        if (pressedKeys.contains(P2_DEFEND)) game.getPlayer2().startDefending(); 
        else game.getPlayer2().endDefending();
    }
}
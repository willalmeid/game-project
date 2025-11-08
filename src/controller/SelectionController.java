package controller;

import model.CharacterFactory;
import view.CharacterSelectionPanel;
import view.ImageCache;
import audio.SoundManager;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Image; // Importação necessária
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Controla a lógica da tela de seleção de personagens.
 * Gerencia o estado de seleção (P1, P2), os "highlights",
 * a reprodução de sons e o início da partida.
 */
public class SelectionController implements ActionListener {

    /** A referência para o painel (View) de seleção. */
    private final CharacterSelectionPanel view;
    /** O callback (ação) a ser executado quando o jogo deve iniciar. */
    private final BiConsumer<CharacterFactory.CharacterType, CharacterFactory.CharacterType> onGameStart;
    /** A instância global do SoundManager. */
    private final SoundManager soundManager;
    
    /** Mapeia os botões da View (Object) para os dados (CharacterType). */
    private final Map<Object, CharacterFactory.CharacterType> buttonCharacterMap;

    /** Controla o estado de seleção (1 = Jogador 1, 2 = Jogador 2). */
    private int currentPlayer = 1;
    /** O personagem final escolhido pelo Jogador 1. */
    private CharacterFactory.CharacterType p1FinalSelection;
    /** O personagem final escolhido pelo Jogador 2. */
    private CharacterFactory.CharacterType p2FinalSelection;
    /** O personagem atualmente em destaque (antes da confirmação). */
    private CharacterFactory.CharacterType highlightedChar;

    /**
     * Constrói o controlador da tela de seleção.
     * @param view A instância do painel de seleção (View).
     * @param onGameStart A função (callback) a ser executada quando o jogo começar.
     * @param soundManager A instância GLOBAL do SoundManager.
     */
    public SelectionController(CharacterSelectionPanel view, 
                               BiConsumer<CharacterFactory.CharacterType, CharacterFactory.CharacterType> onGameStart,
                               SoundManager soundManager) {
        this.view = view;
        this.onGameStart = onGameStart;
        
        // Recebe a instância global
        this.soundManager = soundManager; 
        this.soundManager.setSfxVolume(0.8f);
        
        this.buttonCharacterMap = new HashMap<>(); 
        
        addListeners();
    }

    /**
     * Mapeia os botões (View) aos seus personagens (Model) e adiciona os listeners.
     * Esta é a forma robusta de ligar a view ao controle.
     */
    private void addListeners() {
        mapButtonToChar(view.getButtonPerson1(), CharacterFactory.CharacterType.NITA);
        mapButtonToChar(view.getButtonPerson2(), CharacterFactory.CharacterType.ISAGRAM);
        mapButtonToChar(view.getButtonPerson3(), CharacterFactory.CharacterType.MURISSOCA);
        mapButtonToChar(view.getButtonPerson4(), CharacterFactory.CharacterType.TELETONY);
        mapButtonToChar(view.getButtonPerson5(), CharacterFactory.CharacterType.LULE);

        view.getButtonConfirm().addActionListener(this);
        view.getButtonStart().addActionListener(this);
    }
    
    /**
     * Método auxiliar para popular o Map e adicionar o listener.
     * @param button O JButton da view.
     * @param charType O CharacterType que ele representa.
     */
    private void mapButtonToChar(JButton button, CharacterFactory.CharacterType charType) {
        if (button != null) {
            button.addActionListener(this);
            buttonCharacterMap.put(button, charType);
        }
    }

    /**
     * Lida com todos os eventos de clique da tela de seleção.
     * @param e O evento de ação.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // 1. Verifica se foi um botão de personagem (via Map)
        if (buttonCharacterMap.containsKey(source)) {
            highlightCharacter(buttonCharacterMap.get(source));
            return;
        }

        // 2. Verifica se foi o botão de confirmação
        if (source == view.getButtonConfirm()) {
            confirmSelection();
        }
        // 3. Verifica se foi o botão de iniciar o jogo
        else if (source == view.getButtonStart()) {
            if (p1FinalSelection != null && p2FinalSelection != null) {
                soundManager.stopAllSounds(); // Para as falas da seleção
                onGameStart.accept(p1FinalSelection, p2FinalSelection); // Inicia o jogo
            }
        }
    }

    /**
     * Atualiza a view para mostrar o personagem pré-selecionado (highlighted).
     * @param characterType O personagem a ser destacado.
     */
    private void highlightCharacter(CharacterFactory.CharacterType characterType) {
        this.highlightedChar = characterType;

        // (Otimização) Usa getStaticImage (carrega 1 quadro) 
        // em vez de getAnimation (carrega todos os quadros).
        Image img = ImageCache.getStaticImage(characterType.getIdleSpritePath());
        ImageIcon profileIcon = (img != null) ? new ImageIcon(img) : null;
        
        if (currentPlayer == 1) {
            view.setProfileImageP1(profileIcon);
        } else {
            view.setProfileImageP2(profileIcon);
        }

        // Atualiza a descrição (já usa o método correto do enum)
        view.getAreaDescription().setText(characterType.getDescription());
        view.getButtonConfirm().setEnabled(true);
    }

    /**
     * Confirma a seleção do personagem destacado para o jogador atual.
     * Avança o estado de seleção (P1 -> P2 -> Iniciar).
     */
    private void confirmSelection() {
        if (highlightedChar == null) return;

        playSoundForCharacter(highlightedChar);

        if (currentPlayer == 1) {
            p1FinalSelection = highlightedChar;
            view.getLabelSubtitle().setText("Jogador 2, escolha seu personagem.");
            currentPlayer = 2;
        } else {
            p2FinalSelection = highlightedChar;
            view.getLabelSubtitle().setText("Tudo pronto! Pressione Começar.");
            view.getButtonStart().setEnabled(true);
            disableCharacterButtons();
        }
        
        view.getButtonConfirm().setEnabled(false);
        highlightedChar = null;
    }

    /**
     * Toca a fala de seleção correspondente ao personagem.
     * @param type O personagem selecionado.
     */
    private void playSoundForCharacter(CharacterFactory.CharacterType type) {
        // Usa o método playVoiceLine que para a fala anterior
        switch (type) {
            case MURISSOCA: soundManager.playVoiceLine(SoundManager.SoundFiles.SELECT_MURISSOCA); break;
            case NITA:      soundManager.playVoiceLine(SoundManager.SoundFiles.SELECT_NITA);      break;
            case ISAGRAM:   soundManager.playVoiceLine(SoundManager.SoundFiles.SELECT_ISAGRAM);   break;
            case TELETONY:  soundManager.playVoiceLine(SoundManager.SoundFiles.SELECT_TELETONY);  break;
            case LULE:      soundManager.playVoiceLine(SoundManager.SoundFiles.SELECT_LULE);      break;
            default:        break; // Robustez
        }
    }

    /**
     * Desabilita todos os botões de seleção de personagem
     * (chamado após o P2 confirmar).
     */
    private void disableCharacterButtons() {
        view.getButtonPerson1().setEnabled(false);
        view.getButtonPerson2().setEnabled(false);
        view.getButtonPerson3().setEnabled(false);
        view.getButtonPerson4().setEnabled(false);
        view.getButtonPerson5().setEnabled(false);
    }
}
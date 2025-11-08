package audio;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

/**
 * Gerencia o carregamento e a reprodução de todos os sons do jogo.
 * Separa a lógica de música de fundo, falas de personagem e efeitos sonoros (SFX).
 */
public class SoundManager {

  /**
   * Enum para identificar todos os arquivos de som de forma segura e legível.
   */
  public enum SoundFiles {
    // Músicas
    MENU_MUSIC,    // Música do menu principal.
    FIGHT_MUSIC_1, // Música de luta 1.
    FIGHT_MUSIC_2, // Música de luta 2.
    FIGHT_MUSIC_3, // Música de luta 3.

    // Efeitos Sonoros Gerais
    PUNCH_SOUND,    // Som de soco.
    KICK_SOUND,     // Som de chute.
    SPECIAL_SOUND,  // Som de ataque especial.
    JUMP_SOUND,     // Som de pulo.
    FIGHT_ANNOUNCE, // Som de anúncio "Fight!".
    HIT_SOUND,      // Som de ser atingido.
    
    // Falas de Seleção de Personagem
    SELECT_ISAGRAM,   // Fala de seleção - Isagram.
    SELECT_LULE,      // Fala de seleção - Lule.
    SELECT_TELETONY,  // Fala de seleção - Teletony.
    SELECT_NITA,      // Fala de seleção - Nita.
    SELECT_MURISSOCA  // Fala de seleção - Murissoca.
  }

  private final Map<SoundFiles, URL> soundMap;
  private Clip musicClip;
  private Clip voiceClip;
  private float musicVolume = 1.0f;
  private float sfxVolume = 1.0f;

  public SoundManager() {
    soundMap = new HashMap<>();
    
    loadSound(SoundFiles.MENU_MUSIC, "/sounds/remixBR.wav");
    loadSound(SoundFiles.FIGHT_MUSIC_1, "/sounds/barbieGirl.wav");
    loadSound(SoundFiles.FIGHT_MUSIC_2, "/sounds/masterOfPuppets.wav");
    loadSound(SoundFiles.FIGHT_MUSIC_3, "/sounds/takeOnMe.wav");

    loadSound(SoundFiles.PUNCH_SOUND, "/sounds/punch.wav");
    loadSound(SoundFiles.KICK_SOUND, "/sounds/kick.wav");
    loadSound(SoundFiles.SPECIAL_SOUND, "/sounds/special.wav");
    loadSound(SoundFiles.HIT_SOUND, "/sounds/kick.wav");
    loadSound(SoundFiles.JUMP_SOUND, "/sounds/jump.wav");
    loadSound(SoundFiles.FIGHT_ANNOUNCE, "/sounds/fight.wav");
    
    loadSound(SoundFiles.SELECT_ISAGRAM, "/sounds/soundIsagram.wav");
    loadSound(SoundFiles.SELECT_LULE, "/sounds/soundLule.wav");
    loadSound(SoundFiles.SELECT_TELETONY, "/sounds/soundTeletony.wav");
    loadSound(SoundFiles.SELECT_NITA, "/sounds/soundNita.wav");
    loadSound(SoundFiles.SELECT_MURISSOCA, "/sounds/soundMurissoca.wav");
  }

  // --- MÉTODOS PÚBLICOS DE CONTROLE ---

  /**
   * Define o volume da música de fundo.
   * O valor é "clampado" (limitado) entre 0.0 e 1.0.
   * @param volume Um valor de 0.0 (mudo) a 1.0 (máximo).
   */
  public void setMusicVolume(float volume) {
    this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
    if (musicClip != null) {
      applyVolumeToClip(musicClip, this.musicVolume);
    }
  }

  /**
   * Define o volume para todos os efeitos sonoros e falas.
   * O valor é "clampado" (limitado) entre 0.0 e 1.0.
   * @param volume Um valor de 0.0 (mudo) a 1.0 (máximo).
   */
  public void setSfxVolume(float volume) {
    this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
  }

  /**
   * Toca uma música de fundo em loop contínuo.
   * Se uma música já estiver tocando, ela é parada e substituída.
   * @param sound O arquivo de música a ser tocado.
   */
  public void playMusic(SoundFiles sound) {
    stopMusic();
    musicClip = loadClip(sound, musicVolume);
    if (musicClip != null) {
      musicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }
  }

  /**
   * Toca um efeito sonoro (SFX) uma única vez.
   * O clip é fechado automaticamente ao terminar para evitar vazamento de recursos.
   * @param sound O efeito sonoro a ser tocado.
   */
  public void playSoundFX(SoundFiles sound) {
    Clip sfxClip = loadClip(sound, sfxVolume);
    if (sfxClip != null) {
      
      sfxClip.addLineListener(event -> {
        if (event.getType() == LineEvent.Type.STOP) {
          event.getLine().close();
        }
      });
      
      sfxClip.start();
    }
  }
  
  /**
   * Toca uma fala de personagem, interrompendo a anterior.
   * @param sound A fala a ser tocada.
   */
  public void playVoiceLine(SoundFiles sound) {
    stopVoiceLine();
    voiceClip = loadClip(sound, sfxVolume);
    if (voiceClip != null) {
      voiceClip.start();
    }
  }
  
  /**
   * Para a execução da música de fundo atual, fecha o clip e libera o recurso.
   */
  public void stopMusic() {
    if (musicClip != null) {
      musicClip.stop();
      musicClip.close();
      musicClip = null;
    }
  }

  /**
   * Para a fala atual, fecha o clip e libera o recurso.
   */
  public void stopVoiceLine() {
    if (voiceClip != null) {
      voiceClip.stop();
      voiceClip.close();
      voiceClip = null;
    }
  }

  /**
   * Para todos os sons "contínuos" (música e falas) gerenciados por esta classe.
   */
  public void stopAllSounds() {
    stopMusic();
    stopVoiceLine();
  }

  // --- MÉTODOS PRIVADOS AUXILIARES ---

  /**
   * Método auxiliar privado para carregar um único som no soundMap.
   * Verifica se o recurso existe (URL não nula) antes de adicioná-lo.
   *
   * @param key O Enum SoundFiles
   * @param path O caminho do arquivo .wav (ex: "/sounds/meu_som.wav")
   */
  private void loadSound(SoundFiles key, String path) {
    try {
      URL url = getClass().getResource(path);
      if (url == null) {
        System.err.println("Erro Crítico: Arquivo de som não encontrado: " + path);
      } else {
        soundMap.put(key, url);
      }
    } catch (Exception e) {
      System.err.println("Erro ao processar o som: " + path);
      e.printStackTrace();
    }
  }

  /**
   * Método centralizado para carregar qualquer arquivo de áudio em um Clip.
   * @param sound O som a ser carregado (via Enum).
   * @param volume O volume a ser aplicado (0.0 a 1.0).
   * @return Um objeto Clip pronto para ser tocado, ou null em caso de erro.
   */
  private Clip loadClip(SoundFiles sound, float volume) {
    URL url = soundMap.get(sound);
    if (url == null) {
      System.err.println("Arquivo de áudio não encontrado no map: " + sound);
      return null;
    }
    
    try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
      Clip clip = AudioSystem.getClip();
      clip.open(ais);
      applyVolumeToClip(clip, volume);
      return clip;
    } catch (Exception e) {
      System.err.println("Erro ao carregar o áudio: " + sound);
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Aplica um nível de volume a um Clip, convertendo a escala 0.0-1.0 para decibéis.
   * @param clip O Clip ao qual o volume será aplicado.
   * @param volume O volume em escala de 0.0 a 1.0.
   */
  private void applyVolumeToClip(Clip clip, float volume) {
    try {
      if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (range * volume) + gainControl.getMinimum();
        gainControl.setValue(gain);
      }
    } catch (Exception e) {
      // Ignora silenciosamente
      e.printStackTrace();
    }
  }
}
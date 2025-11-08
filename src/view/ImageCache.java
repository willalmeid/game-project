package view;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator; // Importação necessária
import java.util.Map;

/**
 * Uma classe utilitária estática para carregar e armazenar (em cache) os
 * recursos visuais do jogo (animações e imagens estáticas).
 * Isso evita o acesso repetido ao disco, melhorando significativamente o
 * desempenho.
 */
public final class ImageCache {

  /** Cache para animações (GIFs), indexado pelo caminho e estado de loop. */
  private static final Map<String, Animation> animationMap = new HashMap<>();

  /** Cache para imagens estáticas (PNGs, etc.), indexado pelo caminho. */
  private static final Map<String, Image> staticImageMap = new HashMap<>();

  /**
   * Construtor privado para prevenir a instanciação desta classe utilitária.
   */
  private ImageCache() {}

  /**
   * Recupera uma Animação (em loop) do cache ou a carrega se não estiver
   * presente.
   *
   * @param path       O caminho do recurso para o arquivo GIF.
   * @param frameDelay O número de ticks do jogo entre os quadros da animação.
   * @return Um objeto {@link Animation}.
   */
  public static Animation getAnimation(String path, int frameDelay) {
    return getAnimation(path, frameDelay, true);
  }

  /**
   * Recupera uma Animação do cache ou a carrega se não estiver presente.
   *
   * @param path       O caminho do recurso para o arquivo GIF.
   * @param frameDelay O número de ticks do jogo entre os quadros da animação.
   * @param loops      True se a animação deve repetir, false caso contrário.
   * @return Um objeto {@link Animation}, ou null se o recurso não puder ser
   * carregado.
   */
  public static Animation getAnimation(String path, int frameDelay, boolean loops) {
    String cacheKey = path + "_loops:" + loops;
    if (animationMap.containsKey(cacheKey)) {
      return animationMap.get(cacheKey);
    }

    URL url = ImageCache.class.getResource(path);
    if (url == null) {
      System.err.println("Recurso de animação não encontrado: " + path);
      return null;
    }

    // O try-with-resources garante que 'stream' será fechado.
    try (ImageInputStream stream = ImageIO.createImageInputStream(url.openStream())) {
      Iterator<ImageReader> readerIterator = ImageIO.getImageReaders(stream);

      // (Otimização 1) Verifica se o Java encontrou um leitor de imagem
      if (!readerIterator.hasNext()) {
        System.err.println("Nenhum leitor de imagem encontrado para: " + path);
        return null;
      }

      ImageReader reader = readerIterator.next();
      reader.setInput(stream);

      int numFrames = reader.getNumImages(true);

      // (Otimização 2) Verifica se o GIF não está vazio ou corrompido
      if (numFrames == 0) {
        System.err.println("Animação (GIF) está vazia ou corrompida: " + path);
        reader.dispose();
        return null;
      }

      ImageIcon[] frames = new ImageIcon[numFrames];
      for (int i = 0; i < numFrames; i++) {
        frames[i] = new ImageIcon(reader.read(i));
      }

      reader.dispose(); // Libera recursos nativos

      // A classe 'Animation' agora está protegida contra arrays vazios
      Animation animation = new Animation(frames, frameDelay, loops);
      animationMap.put(cacheKey, animation);
      return animation;

    } catch (Exception e) {
      System.err.println("Erro ao carregar animação: " + path);
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Recupera uma Imagem estática do cache ou a carrega se não estiver presente.
   *
   * @param path O caminho do recurso para o arquivo de imagem (ex: PNG, JPG).
   * @return Um objeto {@link Image}, ou null se o recurso não puder ser
   * carregado.
   */
  public static Image getStaticImage(String path) {
    if (staticImageMap.containsKey(path)) {
      return staticImageMap.get(path);
    }

    URL url = ImageCache.class.getResource(path);
    if (url != null) {
      // Usar ImageIcon é a forma mais direta em um app Swing
      Image image = new ImageIcon(url).getImage();
      staticImageMap.put(path, image);
      return image;
    } else {
      System.err.println("Recurso de imagem estática não encontrado: " + path);
      return null;
    }
  }
}
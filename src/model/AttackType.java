package model;

/**
 * Enum que define os tipos de ataque possíveis no jogo.
 * Funciona como um rótulo seguro para identificar as ações de combate,
 * evitando o uso de "magic strings" ou números.
 */
public enum AttackType {
  
  NONE,    // Representa a ausência de um ataque (estado padrão).
  PUNCH,   // Ataque de soco.
  KICK,    // Ataque de chute.
  SPECIAL  // Ataque especial.
}
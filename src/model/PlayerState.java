package model;

/**
 * Define todos os estados lógicos e visuais possíveis para um jogador.
 * Usar um enum previne erros de digitação e torna o código mais claro e seguro.
 */
public enum PlayerState {
  
  IDLE,               // O jogador está parado, em estado neutro.
  WALKING,            // O jogador está andando para frente.
  WALKING_BACKWARDS,  // O jogador está andando para trás.
  JUMPING,            // O jogador está no ar (pulando ou cindo).
  CROUCHING,          // O jogador está agachado.
  DEFENDING,          // O jogador está defendendo (bloqueando ataques).
  PUNCHING,           // O jogador está executando um soco.
  KICKING,            // O jogador está executando um chute.
  SPECIAL_ATTACK,     // O jogador está executando um ataque especial.
  TAKING_HIT          // O jogador está em "hitstun" (reagindo a um golpe).
}
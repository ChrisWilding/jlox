package dev.wilding.lox;

import lombok.Value;

@Value
class Token {
  TokenType type;
  String lexeme;
  Object literal;
  int line;
}

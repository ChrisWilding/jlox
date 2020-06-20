package dev.wilding.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
  private final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  void assign(Token name, Object value) {
    var lexme = name.getLexeme();
    if (values.containsKey(lexme)) {
      values.put(lexme, value);
      return;
    }

    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name, String.format("Undefined variable '%s'.", lexme));
  }

  void define(String name, Object value) {
    values.put(name, value);
  }

  Object get(Token name) {
    var lexeme = name.getLexeme();
    if (values.containsKey(lexeme)) {
      return values.get(lexeme);
    }

    if (enclosing != null) {
      return enclosing.get(name);
    }

    throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
  }
}
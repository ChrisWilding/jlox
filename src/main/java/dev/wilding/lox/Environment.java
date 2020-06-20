package dev.wilding.lox;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor(force = true)
class Environment {
  private final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  Environment ancestor(int distance) {
    var environment = this;
    for (var i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }

    return environment;
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

  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.getLexeme(), value);
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

  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }
}

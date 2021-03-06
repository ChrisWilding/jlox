package dev.wilding.lox;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure;

  @Override
  public int arity() {
    return declaration.getParams().size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    var environment = new Environment(closure);
    for (var i = 0; i < declaration.getParams().size(); i++) {
      environment.define(declaration.getParams().get(i).getLexeme(), arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.getBody(), environment);
    } catch (Return returnValue) {
      return returnValue.getValue();
    }
    return null;
  }

  @Override
  public String toString() {
    return String.format("<fn %s>", declaration.getName().getLexeme());
  }
}

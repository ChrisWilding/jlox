package dev.wilding.lox;

import java.util.ArrayList;
import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  private final Environment globals = new Environment();
  private Environment environment = globals;

  Interpreter() {
    globals.define(
        "clock",
        new LoxCallable() {
          @Override
          public int arity() {
            return 0;
          }

          @Override
          public Object call(Interpreter interpreter, List<Object> arguments) {
            return (double) System.currentTimeMillis() / 1000.0;
          }

          @Override
          public String toString() {
            return "<native fn>";
          }
        });
  }

  void executeBlock(List<Stmt> statements, Environment environment) {
    var previous = this.environment;
    try {
      this.environment = environment;

      for (var statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
    }
  }

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    var value = evaluate(expr.getValue());
    environment.assign(expr.getName(), value);
    return value;
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    var left = evaluate(expr.getLeft());
    var right = evaluate(expr.getRight());

    switch (expr.getOperator().getType()) {
      case BANG_EQUAL:
        return !isEqual(left, right);
      case EQUAL_EQUAL:
        return isEqual(left, right);
      case GREATER:
        checkNumberOperands(expr.getOperator(), left, right);
        return (double) left > (double) right;
      case GREATER_EQUAL:
        checkNumberOperands(expr.getOperator(), left, right);
        return (double) left >= (double) right;
      case LESS:
        checkNumberOperands(expr.getOperator(), left, right);
        return (double) left < (double) right;
      case LESS_EQUAL:
        checkNumberOperands(expr.getOperator(), left, right);
        return (double) left <= (double) right;
      case MINUS:
        checkNumberOperands(expr.getOperator(), left, right);
        return (double) left - (double) right;
      case PLUS:
        if (left instanceof Double && right instanceof Double) {
          return (double) left + (double) right;
        }

        if (left instanceof String && right instanceof String) {
          return (String) left + (String) right;
        }

        throw new RuntimeError(expr.getOperator(), "Operands must be two numbers or two strings.");
      case SLASH:
        checkNumberOperands(expr.getOperator(), left, right);
        return (double) left / (double) right;
      case STAR:
        checkNumberOperands(expr.getOperator(), left, right);
        return (double) left * (double) right;
    }

    return null;
  }

  @Override
  public Object visitCallExpr(Expr.Call expr) {
    var callee = evaluate(expr.getCallee());

    var arguments = new ArrayList<Object>();
    for (var argument : expr.getArguments()) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof LoxCallable)) {
      throw new RuntimeError(expr.getParen(), "Can only call functions and classes.");
    }

    LoxCallable function = (LoxCallable) callee;
    if (arguments.size() != function.arity()) {
      var message = String.format("Expected %s arguments but got %s.");
      throw new RuntimeError(expr.getParen(), message);
    }
    return function.call(this, arguments);
  }

  @Override
  public Object visitGetExpr(Expr.Get expr) {
    return null;
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.getExpression());
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.getValue();
  }

  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    var left = evaluate(expr.getLeft());

    if (expr.getOperator().getType() == TokenType.OR) {
      if (isTruthy(left)) return left;
    } else {
      if (!isTruthy(left)) return left;
    }
    return evaluate(expr.getRight());
  }

  @Override
  public Object visitSetExpr(Expr.Set expr) {
    return null;
  }

  @Override
  public Object visitSuperExpr(Expr.Super expr) {
    return null;
  }

  @Override
  public Object visitThisExpr(Expr.This expr) {
    return null;
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    var right = evaluate(expr.getRight());

    switch (expr.getOperator().getType()) {
      case BANG:
        return !isTruthy(right);
      case MINUS:
        checkNumberOperand(expr.getOperator(), right);
        return -(double) right;
    }

    return null;
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return environment.get(expr.getName());
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.getExpression());
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    var function = new LoxFunction(stmt, environment);
    environment.define(stmt.getName().getLexeme(), function);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.getCondition()))) {
      execute(stmt.getThenBranch());
    } else if (stmt.getElseBranch() != null) {
      execute(stmt.getElseBranch());
    }
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    var value = evaluate(stmt.getExpression());
    System.out.println(stringify(value));
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if (stmt.getValue() != null) {
      value = evaluate(stmt.getValue());
    }

    throw new Return(value);
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;

    var initializer = stmt.getInitializer();
    if (initializer != null) {
      value = evaluate(initializer);
    }

    environment.define(stmt.getName().getLexeme(), value);
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.getCondition()))) {
      execute(stmt.getBody());
    }
    return null;
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;

    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean) object;
    return true;
  }

  private String stringify(Object object) {
    if (object == null) return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.getStatements(), new Environment(environment));
    return null;
  }
}

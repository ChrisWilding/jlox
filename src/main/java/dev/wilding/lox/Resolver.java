package dev.wilding.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;
  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  void resolve(Expr expr) {
    expr.accept(this);
  }

  void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  void resolve(List<Stmt> statements) {
    for (var statement : statements) {
      resolve(statement);
    }
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.getValue());
    resolveLocal(expr, expr.getName());
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.getLeft());
    resolve(expr.getRight());
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.getCallee());

    for (var argument : expr.getArguments()) {
      resolve(argument);
    }

    return null;
  }

  @Override
  public Void visitGetExpr(Expr.Get expr) {
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.getExpression());
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.getLeft());
    resolve(expr.getRight());
    return null;
  }

  @Override
  public Void visitSetExpr(Expr.Set expr) {
    return null;
  }

  @Override
  public Void visitSuperExpr(Expr.Super expr) {
    return null;
  }

  @Override
  public Void visitThisExpr(Expr.This expr) {
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.getRight());
    return null;
  }

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty() && scopes.peek().get(expr.getName().getLexeme()) == Boolean.FALSE) {
      Lox.error(expr.getName(), "Cannot read local variable in its own initializer.");
    }
    resolveLocal(expr, expr.getName());
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.getStatements());
    endScope();
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.getExpression());
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    var name = stmt.getName();
    declare(name);
    define(name);

    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.getCondition());
    resolve(stmt.getThenBranch());
    var elseBranch = stmt.getElseBranch();
    if (elseBranch != null) resolve(elseBranch);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    resolve(stmt.getExpression());
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE) {
      Lox.error(stmt.getKeyword(), "Cannot return from top-level code.");
    }

    var value = stmt.getValue();
    if (value != null) {
      resolve(value);
    }
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    declare(stmt.getName());
    var initializer = stmt.getInitializer();
    if (initializer != null) {
      resolve(initializer);
    }
    define(stmt.getName());
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.getCondition());
    resolve(stmt.getBody());
    return null;
  }

  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) return;

    var scope = scopes.peek();
    var lexeme = name.getLexeme();
    if (scope.containsKey(lexeme)) {
      Lox.error(name, "Variable with this name already declared in this scope.");
    }
    scope.put(lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.getLexeme(), true);
  }

  private void endScope() {
    scopes.pop();
  }

  private void resolveFunction(Stmt.Function function, FunctionType type) {
    FunctionType enclosingFunction = currentFunction;
    currentFunction = type;

    beginScope();
    for (var param : function.getParams()) {
      declare(param);
      define(param);
    }
    resolve(function.getBody());
    endScope();

    currentFunction = enclosingFunction;
  }

  private void resolveLocal(Expr expr, Token name) {
    for (var i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.getLexeme())) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }

  private enum FunctionType {
    NONE,
    FUNCTION
  }
}

package dev.wilding.lox;

import lombok.Value;

import java.util.List;

abstract class Stmt {
  abstract <R> R accept(Visitor<R> visitor);

  interface Visitor<R> {
    R visitBlockStmt(Block stmt);

    R visitClassStmt(Class stmt);

    R visitExpressionStmt(Expression stmt);

    R visitFunctionStmt(Function stmt);

    R visitIfStmt(If stmt);

    R visitPrintStmt(Print stmt);

    R visitReturnStmt(Return stmt);

    R visitVarStmt(Var stmt);

    R visitWhileStmt(While stmt);
  }

  @Value
  static class Block extends Stmt {
    List<Stmt> statements;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }
  }

  @Value
  static class Class extends Stmt {
    Token name;
    Expr.Variable superclass;
    List<Stmt.Function> methods;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }
  }

  @Value
  static class Expression extends Stmt {
    Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }

  @Value
  static class Function extends Stmt {
    Token name;
    List<Token> params;
    List<Stmt> body;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }
  }

  @Value
  static class If extends Stmt {
    Expr condition;
    Stmt thenBranch;
    Stmt elseBranch;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }
  }

  @Value
  static class Print extends Stmt {
    Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
  }

  @Value
  static class Return extends Stmt {
    Token keyword;
    Expr value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }
  }

  @Value
  static class Var extends Stmt {
    Token name;
    Expr initializer;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
  }

  @Value
  static class While extends Stmt {
    Expr condition;
    Stmt body;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }
  }
}

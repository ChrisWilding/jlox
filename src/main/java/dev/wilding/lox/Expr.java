package dev.wilding.lox;

import lombok.Value;

import java.util.List;

abstract class Expr {
  abstract <R> R accept(Visitor<R> visitor);

  interface Visitor<R> {
    R visitAssignExpr(Assign expr);

    R visitBinaryExpr(Binary expr);

    R visitCallExpr(Call expr);

    R visitGetExpr(Get expr);

    R visitGroupingExpr(Grouping expr);

    R visitLiteralExpr(Literal expr);

    R visitLogicalExpr(Logical expr);

    R visitSetExpr(Set expr);

    R visitSuperExpr(Super expr);

    R visitThisExpr(This expr);

    R visitUnaryExpr(Unary expr);

    R visitVariableExpr(Variable expr);
  }

  @Value
  static class Assign extends Expr {
    Token name;
    Expr value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }
  }

  @Value
  static class Binary extends Expr {
    Expr left;
    Token operator;
    Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  @Value
  static class Call extends Expr {
    Expr callee;
    Token paren;
    List<Expr> arguments;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }
  }

  @Value
  static class Get extends Expr {
    Expr object;
    Token name;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }
  }

  @Value
  static class Grouping extends Expr {
    Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  @Value
  static class Literal extends Expr {
    Object value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  @Value
  static class Logical extends Expr {
    Expr left;
    Token operator;
    Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }
  }

  @Value
  static class Set extends Expr {
    Expr object;
    Token name;
    Expr value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }
  }

  @Value
  static class Super extends Expr {
    Token keyword;
    Token method;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSuperExpr(this);
    }
  }

  @Value
  static class This extends Expr {
    Token keyword;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitThisExpr(this);
    }
  }

  @Value
  static class Unary extends Expr {
    Token operator;
    Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }

  @Value
  static class Variable extends Expr {
    Token name;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
  }
}

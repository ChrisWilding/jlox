package dev.wilding.lox;

class AstPrinter implements Expr.Visitor<String> {
  String print(Expr expr) {
    return expr.accecpt(this);
  }

  @Override
  public String visitAssignExpr(Expr.Assign expr) {
    return null;
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.getOperator().getLexeme(), expr.getLeft(), expr.getRight());
  }

  @Override
  public String visitCallExpr(Expr.Call expr) {
    return null;
  }

  @Override
  public String visitGetExpr(Expr.Get expr) {
    return null;
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.getExpression());
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.getValue() == null) return "nil";
    return expr.getValue().toString();
  }

  @Override
  public String visitLogicalExpr(Expr.Logical expr) {
    return null;
  }

  @Override
  public String visitSetExpr(Expr.Set expr) {
    return null;
  }

  @Override
  public String visitSuperExpr(Expr.Super expr) {
    return null;
  }

  @Override
  public String visitThisExpr(Expr.This expr) {
    return null;
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.getOperator().getLexeme(), expr.getRight());
  }

  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    return null;
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accecpt(this));
    }
    builder.append(")");

    return builder.toString();
  }
}

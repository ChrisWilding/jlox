package dev.wilding.lox;

import java.util.ArrayList;
import java.util.List;

class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    var statements = new ArrayList<Stmt>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }

  private Expr assignment() {
    var expr = equality();

    if (match(TokenType.EQUAL)) {
      var equals = previous();
      var value = assignment();

      if (expr instanceof Expr.Variable) {
        var name = ((Expr.Variable) expr).getName();
        return new Expr.Assign(name, value);
      }

      error(equals, "Invalid assignment target.");
    }

    return expr;
  }

  private Expr addition() {
    var expr = multiplication();

    while (match(TokenType.MINUS, TokenType.PLUS)) {
      var operator = previous();
      var right = multiplication();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private List<Stmt> block() {
    var statements = new ArrayList<Stmt>();

    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().getType() == type;
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }

  private Expr comparison() {
    var expr = addition();

    while (match(
        TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      var operator = previous();
      var right = addition();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Stmt declaration() {
    try {
      if (match(TokenType.VAR)) return varDeclaration();
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Expr equality() {
    var expr = comparison();

    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      var operator = previous();
      var right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private Expr expression() {
    return assignment();
  }

  private Stmt expressionStatement() {
    var expr = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }

  private boolean isAtEnd() {
    return peek().getType() == TokenType.EOF;
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private Expr multiplication() {
    var expr = unary();

    while (match(TokenType.SLASH, TokenType.STAR)) {
      var operator = previous();
      var right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Expr primary() {
    if (match(TokenType.FALSE)) return new Expr.Literal(false);
    if (match(TokenType.TRUE)) return new Expr.Literal(true);
    if (match(TokenType.NIL)) return new Expr.Literal(null);

    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(previous().getLiteral());
    }

    if (match(TokenType.IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if (match(TokenType.LEFT_PAREN)) {
      Expr expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private Stmt printStatement() {
    var value = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  private Stmt statement() {
    if (match(TokenType.PRINT)) return printStatement();
    if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());

    return expressionStatement();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().getType() == TokenType.SEMICOLON) return;

      switch (peek().getType()) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }

  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      var operator = previous();
      var right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Stmt varDeclaration() {
    var name = consume(TokenType.IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(TokenType.EQUAL)) {
      initializer = expression();
    }

    consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private static class ParseError extends RuntimeException {}
}

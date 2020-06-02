package dev.wilding.lox;

import java.util.List;

class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
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

    while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      var operator = previous();
      var right = addition();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
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
    return equality();
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

  private static class ParseError extends RuntimeException {
  }
}

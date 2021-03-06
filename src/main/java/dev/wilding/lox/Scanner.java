package dev.wilding.lox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Scanner {
  private static final Map<String, TokenType> keywords =
      Map.ofEntries(
          Map.entry("and", TokenType.AND),
          Map.entry("class", TokenType.CLASS),
          Map.entry("else", TokenType.ELSE),
          Map.entry("false", TokenType.FALSE),
          Map.entry("for", TokenType.FOR),
          Map.entry("fun", TokenType.FUN),
          Map.entry("if", TokenType.IF),
          Map.entry("nil", TokenType.NIL),
          Map.entry("or", TokenType.OR),
          Map.entry("print", TokenType.PRINT),
          Map.entry("return", TokenType.RETURN),
          Map.entry("super", TokenType.SUPER),
          Map.entry("this", TokenType.THIS),
          Map.entry("true", TokenType.TRUE),
          Map.entry("var", TokenType.VAR),
          Map.entry("while", TokenType.WHILE));

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    var text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    var text = source.substring(start, current);
    var type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
    addToken(type);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  private void number() {
    while (isDigit(peek())) advance();

    if (peek() == '.' && isDigit(peekNext())) {
      advance();
      while (isDigit(peek())) advance();
    }

    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(TokenType.LEFT_PAREN);
        break;
      case ')':
        addToken(TokenType.RIGHT_PAREN);
        break;
      case '{':
        addToken(TokenType.LEFT_BRACE);
        break;
      case '}':
        addToken(TokenType.RIGHT_BRACE);
        break;
      case ',':
        addToken(TokenType.COMMA);
        break;
      case '.':
        addToken(TokenType.DOT);
        break;
      case '-':
        addToken(TokenType.MINUS);
        break;
      case '+':
        addToken(TokenType.PLUS);
        break;
      case ';':
        addToken(TokenType.SEMICOLON);
        break;
      case '*':
        addToken(TokenType.STAR);
        break;
      case '!':
        addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;
      case '=':
        addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;
      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;
      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;
      case '/':
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(TokenType.SLASH);
        }
        break;
      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n':
        line++;
        break;
      case '"':
        string();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated stirng.");
      return;
    }

    advance();

    var value = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, value);
  }
}

package dev.wilding.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {
  static boolean hadError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  static void error(Token token, String message) {
    if (token.getType() == TokenType.EOF) {
      report(token.getLine(), " at end", message);
    } else {
      report(token.getLine(), " at '" + token.getLexeme() + "'", message);
    }
  }

  private static void report(int line, String where, String message) {
    System.err.printf("[line %s] Error%s: %s", line, where, message);
    hadError = true;
  }

  private static void run(String source) {
    var scanner = new Scanner(source);
    var tokens = scanner.scanTokens();
    var parser = new Parser(tokens);
    var expression = parser.parse();

    if (hadError) return;

    System.out.println(new AstPrinter().print(expression));
  }

  private static void runFile(String path) throws IOException {
    var bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));

    if (hadError) {
      System.exit(65);
    }
  }

  private static void runPrompt() throws IOException {
    var input = new InputStreamReader(System.in);
    var reader = new BufferedReader(input);

    for (; ; ) {
      System.out.println("> ");
      run(reader.readLine());
      hadError = false;
    }
  }
}

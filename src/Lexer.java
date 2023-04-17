import java.util.Scanner;

public class Lexer {
    private enum TokenType {
        NIL,
        NUMBER,
        OPERATOR
    }

    private Scanner scanner;
    private TokenType tokenType;
    private double tokenNumberValue;
    private Operator tokenOperatorValue;

    public Lexer(String input) throws SemanticException {
        // remove all whitespaces and tabs
        input = input.replaceAll("\\s", "");
        scanner = new Scanner(input);

        // scanner should split the input into multiple tokens
        // e.g.: "2.5*2" -> { '2.5', '*', '2' }
        // for that the delimiter is used:

        // non-digit is [^\\d.] and not \\D because of the decimal numbers
        final String charBeforeDigit = "(?<=[^\\d.])(?=\\d)";
        final String digitBeforeChar = "(?<=\\d)(?=[^\\d.])";
        // break expressions like '...+(...' into '+', '('
        final String parenthesis = "(?<=[()])|(?=[()])";
        final String regex = charBeforeDigit+'|'+digitBeforeChar+'|'+parenthesis;
        scanner.useDelimiter(regex);

        advance();
    }

    public boolean match(Operator... operators) {
        if (tokenType == TokenType.OPERATOR) {
            for (var i : operators) {
                if (i == tokenOperatorValue) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean matchIfNumber() {
        return tokenType == TokenType.NUMBER;
    }

    public double popNumber() throws SemanticException {
        double number = tokenNumberValue;
        advance();
        return number;
    }

    public Operator popOperator() throws SemanticException {
        Operator operator = tokenOperatorValue;
        advance();
        return operator;
    }

    public static class SemanticException extends Exception {
        public SemanticException(String operator) {
            super("Semantic error: unknown operator \""+operator+"\".");
        }
    }

    private void advance() throws SemanticException {
        if (scanner != null) {
            if (scanner.hasNext()) {
                if (scanner.hasNextDouble()) {
                    tokenType = TokenType.NUMBER;
                    tokenNumberValue = scanner.nextDouble();
                }
                else {
                    tokenType = TokenType.OPERATOR;
                    tokenOperatorValue = getOperator(scanner.next());
                }
            }
            else {
                tokenType = TokenType.NIL;
                scanner.close();
                // prevent the use of a closed scanner
                scanner = null;
            }
        }
    }

    private Operator getOperator(String input) throws SemanticException {
        switch (input) {
            case "+" -> { return Operator.PLUS; }
            case "-" -> { return Operator.MINUS; }
            case "*" -> { return Operator.MULTIPLY; }
            case "/" -> { return Operator.DIVIDE; }
            case "(" -> { return Operator.PARENTHESES_OPENING; }
            case ")" -> { return Operator.PARENTHESES_CLOSING; }
            default -> throw new SemanticException(input);
        }
    }
}
/*  Using the recursive decent parser algorithm
    Grammar:
    E - expression, T - term F - factor, U - Unary expression, G - group, A - advanced group, e - nil

    E  -> TE'
    E' -> +TE' | -TE' | e
    T  -> UT'
    T' -> *UT' | /UT' | %T' | e
    U  -> -F | F
    F  -> double | G
    G -> (E) | sqrt(E) | lg(E) | fact(E) | A
    A -> pow(E,E) | log(E,E)
*/

public abstract class Parser {
    public static double parseAndEvaluate(Lexer lexer) throws Lexer.SemanticException, SyntaxException {
        return parseE(lexer);
    }

    public static class SyntaxException extends Exception {
        public SyntaxException(String error) {
            super("Syntax error: "+error+".");
        }
    }

    private static class NumberExpectedException extends SyntaxException {
        public NumberExpectedException() {
            super("number expected");
        }
    }

    private static class UnclosedParenthesisException extends SyntaxException {
        public UnclosedParenthesisException() {
            super("unclosed parenthesis");
        }
    }

        /* EXAMPLE:
        parse an expression "2+3"
        shortened parse tree (some nodes are numbered to avoid confusion):

        parseE(lexer):

          E
         / \
        T1  E'1
           /+\
          T2  E'2

        call parseT(lexer):

        T1 -> UT'; U -> F -> 2; T' -> e;
        parseT(lexer) returns 2
        set t to 2

           E
          / \
        (2)  E'1
         |  /+\
         | T2  E'2
         |
         |
         \_(siblingT is a sibling of E'1)

        call parseEPrime(lexer, siblingT):

        E'1 -> T2E'2;

        call parseT(lexer):

        T2 -> UT'; U -> F; F -> 3; T' -> e;
        parseT(lexer) returns 3;
        set t to 3

          E
         / \
        2   E'1
           /+\
          3   E'2

        set the new value to siblingT,
        so it would be a siblingT of E'2:
        siblingT = evaluate(+, siblingT(of E'1), t)
        siblingT = 2+3 = 5

        recursively call parseEPrime(lexer, siblingT):
        E'2 -> e
        return the unchanged value of siblingT(5)

        parseE(lexer) returns 5
     */

    private static double parseE(Lexer lexer) throws Lexer.SemanticException, SyntaxException {
        // E -> TE'
        double t = parseT(lexer);
        return parseEPrime(lexer, t);
    }

    private static double parseEPrime(Lexer lexer, double siblingT) throws Lexer.SemanticException, SyntaxException {
        if (lexer.match(Operator.PLUS,Operator.MINUS)) {
            // E' -> +TE' | -TE'
            Operator operator = lexer.popOperator();
            double t = parseT(lexer);
            siblingT = evaluate(operator, siblingT, t);
            return parseEPrime(lexer, siblingT);
        }
        else {
            // E' -> e
            // (keep the value unchanged)
            return siblingT;
        }
    }

    private static  double parseT(Lexer lexer) throws Lexer.SemanticException, SyntaxException {
        // T -> UT'
        double u = parseU(lexer);
        return parseTPrime(lexer, u);
    }

    private static double parseTPrime(Lexer lexer, double siblingU) throws Lexer.SemanticException, SyntaxException {
        if (lexer.match(Operator.MULTIPLY, Operator.DIVIDE, Operator.REMAINDER)) {
            // T' -> *UT' | /*UT' | %UT'
            Operator operator = lexer.popOperator();
            double u = parseU(lexer);
            siblingU = evaluate(operator, siblingU, u);
            return parseTPrime(lexer, siblingU);
        }
        else {
            // T' -> e
            // (keep the value unchanged)
            return siblingU;
        }
    }

    private static double parseU(Lexer lexer) throws Lexer.SemanticException, SyntaxException {
        if (lexer.match(Operator.PLUS,Operator.MINUS)) {
            // make the first number zero to evaluate unary expression, e.g.:
            // (-1) -> 0 - 1 = -1; (+1) -> 0 + 1 = 1
            return evaluate(lexer.popOperator(), 0, parseF(lexer));
        }
        else {
            return parseF(lexer);
        }
    }

    private static double parseF(Lexer lexer) throws Lexer.SemanticException, SyntaxException {
        if (lexer.matchIfNumber()) {
            return lexer.popNumber();
        }
        else if (lexer.match(Operator.PARENTHESES_OPENING)) {
            lexer.popOperator();
            double e = parseE(lexer);
            if (lexer.match(Operator.PARENTHESES_CLOSING)) {
                lexer.popOperator();
                return e;
            }
            else {
                throw new UnclosedParenthesisException();
            }
        }
        else {
            throw new NumberExpectedException();
        }
    }

    private static double evaluate(Operator operator, double first, double second) {
        switch (operator) {
            case PLUS -> { return first + second; }
            case MINUS -> { return first - second; }
            case MULTIPLY -> { return first * second; }
            case DIVIDE -> { return first / second; }
            case REMAINDER -> { return first % second; }
        }
        return -1; // impossible
    }
}
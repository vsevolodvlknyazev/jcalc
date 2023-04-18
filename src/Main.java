import java.util.Scanner;

public class Main {
    private static void printHelp() {
        System.out.println(
                "options: help, quit; available operators: + - * / ( )");
        System.out.println("functions: sqrt(x), lg(x), fact(int), pow(base,power), log(base, exponent)");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input;
        printHelp();
        while (true) {
            System.out.print(">> "); // prompt
            input = scanner.nextLine();
            // remove all whitespaces and tabs
            input = input.replaceAll("\\s", "");
            input = input.toLowerCase();

            if (input.isEmpty()) {
                continue;
            }
            if (input.equals("help")) {
                printHelp();
                continue;
            }
            if (input.equals("quit")) {
                break;
            }

            try {
                Lexer lexer = new Lexer(input);
                double result = Parser.parseAndEvaluate(lexer);

                System.out.print("== ");
                if (result % 1.0 != 0) { // decimal
                    System.out.println(result);
                }
                else { // not decimal, round up
                    System.out.printf("%.0f\n", result);
                }
            }
            catch (Lexer.SemanticException | Parser.SyntaxException e) {
                System.out.println("!! "+e.getMessage());
            }
        }
    }
}
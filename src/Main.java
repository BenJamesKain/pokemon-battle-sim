import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Main {
    
    // DEBUG VARIABLES
    private static final boolean TOGGLE_AST_PRINTER = false;
    private static final boolean TOGGLE_DEBUG = false;
    
    private static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();
    
    public static void main(String[] args) {
        // Prompt the user for the file path to the text file to scan
        Scanner consoleScanner = new Scanner(System.in);
        
        System.out.println("Please enter the file path of the file to parse: ");
        String path = consoleScanner.nextLine();
        
        // Empty String tests the default file, for ease of use
        if (path.isEmpty()) path = "src/TestFile.txt";
        
        File file = new File(path);
        
        // Scan the file
        try (Scanner scanner = new Scanner(file)) {
            
            // Create an input string that we will send to the tokenizer.
            StringBuilder input = new StringBuilder();
            
            // Start scanning the file
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                
                // Re-append a newline at the end of the line.
                // I am doing it this way because I want to read line by line for comments,
                // but I am unsure of how skip the rest of the line if my input file has no newline characters.
                //
                // Is there a better way to do this? Probably. Sure.
                // But it works!
                line += '\n';
                input.append(line);
            }
        
            // Create instances of our Tokenizer, and tokenize the input file
            Tokenizer tokenizer = new Tokenizer(input.toString());
            List<Token> tokens = tokenizer.tokenize();
            
            // Print out helpful info if desired
            ASTPrinter astPrinter = new ASTPrinter();
            
            // Parse program
            Parser parser = new Parser(tokens);
            ProgramNode prog = parser.parseProgram();
            
            // Symbol table for Scope and Type visitors
            SymbolTable symbols = new SymbolTable();
            ScopeVisitor scopeVisitor = new ScopeVisitor(symbols);
            scopeVisitor.visitProgramNode(prog);
            
            TypeVisitor typeVisitor = new TypeVisitor();
            typeVisitor.visitProgramNode(prog);
            
            // Interpreter
            interpreter.interpret(prog.statements);
            
            // Toggle the AST Printer for useful output
            if (TOGGLE_AST_PRINTER) System.out.println(prog.accept(astPrinter));
            if (hadRuntimeError) System.exit(70);
            
            // Prints the tokenizer output for debugging.
            if (TOGGLE_DEBUG) {
                int counter = 1;
                for (Token token : tokens) {
                    System.out.println(counter + " " + token.type + " -> " + token.value);
                    counter++;
                }
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("Error: The file at " + path + " was not found.");
        } finally {
            consoleScanner.close();
        }
    }
    
    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
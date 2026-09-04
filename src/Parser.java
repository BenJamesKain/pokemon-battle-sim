import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}
    
    
    // List of input tokens
    private final List<Token> tokens;
    // current is the next token waiting to be parsed
    private int current = 0;
    
    // Counters for the numbers of errors and successfully parsed tokens
    private int parsedSuccessfully = 0;
    private int parsedErrors = 0;
    
    // Toggle for printing out what the parser is doing, for debugging purposes.
    private final boolean TOGGLE_DEBUG = false;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public ProgramNode parseProgram() {
        if (TOGGLE_DEBUG) System.out.println("parseProgram();");
        
        
        ProgramNode program = new ProgramNode();
        while (!isAtEnd()) {
            try {
                program.statements.add(parseStmt());
                parsedSuccessfully++;
            } catch (ParseError e) {
                synchronize(); // Skips tokens until next statement
                if (TOGGLE_DEBUG) System.out.println("Sync");
            }
        }
        System.out.println("Successfully parsed Statements: " + parsedSuccessfully);
        System.out.println("Statements producing errors: " + parsedErrors);
        
        return program;
    }
    
    private StatementNode parseStmt() {
        
        if (check(TokenType.RBRACE)) {
            throw error(peek(), "Unexpected '}'");
        }
        
        if (match(TokenType.KW_INT, TokenType.KW_BOOLEAN, TokenType.KW_STRING))
            return parseVarDecStmt();
        
        if (match(TokenType.KW_POKEMON)) return parseLoadStmt();
        if (match(TokenType.KW_SPELLDATABASE)) return parseSpellDbStmt();
        if (match(TokenType.KW_SHOW)) return parseShowStmt();
        if (match(TokenType.KW_PRINT)) return parsePrintStmt();
        if (match(TokenType.KW_IF)) return parseIfStmt();
        if (match(TokenType.KW_WHILE)) return parseWhileStmt();
        if (match(TokenType.KW_FOR)) return parseForStmt();
        if (match(TokenType.KW_FUNCTION)) return parseFuncDecStmt();
        if (match(TokenType.KW_BREAK)) return parseBreakStmt();
        if (match(TokenType.KW_CONTINUE)) return parseContinueStmt();
        if (match(TokenType.KW_RETURN)) return parseReturnStmt();
        
        if (check(TokenType.IDENTIFIER)) {
            Token name = advance();
            return parseFallback(name);
        }
        
        throw error(peek(), "parseStmt: Expect statement");
    }
    
    // Parses the unmatched field access nodes, assignments, function calls, or move nodes.
    // Basically anything that starts with an expression.
    private StatementNode parseFallback(Token name) {
        
        ExpressionNode target = new VariableNode(name);
        
        // Field access: pokemon.hp
        if (match(TokenType.DOT)) {
            Token field = consumeField();
            target = new FieldAccessNode(target, field);
        }
        
        // Assignment: =, +=, -=
        if (match(TokenType.OP_ASSIGN, TokenType.OP_PLUS_ASSIGN, TokenType.OP_MINUS_ASSIGN)) {
            Token op = previous();
            ExpressionNode value = parseExpr();
            consume(TokenType.SEMICOLON, "Expect ';' after assignment.");
            return new AssnNode(target, op, value);
        }
        
        // Function call
        if (match(TokenType.LPAREN)) {
            List<ExpressionNode> args = new ArrayList<>();
            
            if (!check(TokenType.RPAREN)) {
                do {
                    args.add(parseExpr());
                } while (match(TokenType.COMMA));
            }
            
            consume(TokenType.RPAREN, "Expect ')'");
            
            consume(TokenType.SEMICOLON, "Expect ';' after function call.");
            
            return new ExpressionStmtNode(new FuncCallNode(name, args));
        }
        
        // Move statement: pokemon1 move1 pokemon2
        if (match(TokenType.KW_MOVE1, TokenType.KW_MOVE2,
                TokenType.KW_MOVE3, TokenType.KW_MOVE4)) {
            
            Token move = previous();
            Token targetId = consume(TokenType.IDENTIFIER, "Expect target");
            consume(TokenType.SEMICOLON, "Expect ';'");
            
            return new MoveNode(name, move, targetId);
        }
        
        throw error(name, "Invalid identifier statement");
    }
    
    /*
    Next tokens:
    '(' function call
    "move1" - "move4" move stmt
    '.', '=', '+=' assn statement
    */
    
    
    private StatementNode parseVarDecStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseVarDecStmt();");
        
        Token type = previous();
        Token name = consume(TokenType.IDENTIFIER, "Expect Identifier.");
        consume(TokenType.OP_ASSIGN, "Expect '=' after identifier.");
        
        ExpressionNode expr = parseExpr();
        
        consume(TokenType.SEMICOLON, "Expect ';' after Expression.");
        
        return new VarDecNode(typeFromToken(type), name, expr);
    }
    
    private StatementNode parseAssnStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseAssnStmt();");
        
        Token id = previous();
        ExpressionNode target = new VariableNode(id);
        
        // Field access operator
        if (match(TokenType.DOT)) {
            Token field = consumeField();
            target = new FieldAccessNode(target, field);
        }
        
        // Assignment Operator
        Token op = consumeAssnOperator();
        
        ExpressionNode value = parseExpr();
        
        consume(TokenType.SEMICOLON, "parseAssnStmt: Expect ';' after assignment.");
        return new AssnNode(target, op, value);
    }
    
    private StatementNode parseLoadStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseLoadStmt();");
        
        // "Pokemon" has already been matched
        Token name = consume(TokenType.IDENTIFIER, "parseLoadStmt: Expect identifier after keyword 'Pokemon'.");
        consume(TokenType.OP_ASSIGN, "parseLoadStmt: Expect '=' after identifier.");
        consume(TokenType.KW_POKEMON, "parseLoadStmt: Expect 'Pokemon' after assignment.");
        consume(TokenType.LPAREN, "parseLoadStmt: Expect '(' after keyword 'Pokemon'.");
        
        Token literal = consume(TokenType.STRING_LITERAL, "parseLoadStmt: Expect String after '('.");
        consume(TokenType.RPAREN, "parseLoadStmt: Expect ')' after String.");
        consume(TokenType.SEMICOLON, "parseLoadStmt: Expect ';' at end of Load statement.");
        
        
        return new PkmLoadNode(name, literal);
    }
    
    private StatementNode parseSpellDbStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseSpellDbStmt();");
       
       // "spelldatabase" already matched
        Token literal = consume(TokenType.STRING_LITERAL, "parseSpellDbStmt: Expect String after 'spelldatabase'.");
        consume(TokenType.SEMICOLON, "parseLoadStmt: Expect ';' at end of spelldatabase statement.");
        
        return new SpellDbNode(literal);
    }
    
    // Show
    private StatementNode parseShowStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseShowStmt();");
        
        
        Token target = consume(TokenType.IDENTIFIER, "parseShowStmt: Expect target Identifier.");
        
        consume(TokenType.SEMICOLON, "parseShowStmt: Expect ';' after expression.");
        return new ShowNode(target);
    }
    
    // Print statement
    private StatementNode parsePrintStmt() {
        if (TOGGLE_DEBUG) System.out.println("parsePrintStmt();");
        
        
        ExpressionNode expr = parseExpr();
        consume(TokenType.SEMICOLON, "parsePrintStmt: Expect ';' after expression.");
        return new PrintNode(expr);
    }
    
    // Move statement
    private StatementNode parseMoveStmt(Token user) {
        if (TOGGLE_DEBUG) System.out.println("parseMoveStmt();");
        
        
        Token move = previous();
        Token target = consume(TokenType.IDENTIFIER, "parseMoveStmt: Expect target Identifier.");
        consume(TokenType.SEMICOLON, "parseMoveStmt: Expect ';' after statement.");
        return new MoveNode(user, move, target);
    }
    
    // If Statement
    private StatementNode parseIfStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseIfStmt();");
        
        
        // Create Lists for elseif and elseBlock
        List<ExpressionNode> elseIfConditions = new ArrayList<>();
        List<List<StatementNode>> elseIfBlocks = new ArrayList<>();
        List<StatementNode> elseBlock = null;
        
        // If
        consume(TokenType.LPAREN, "parseIfStmt: Expect '(' after 'if'.");
        ExpressionNode condition = parseExpr();
        consume(TokenType.RPAREN, "parseIfStmt: Expect ')' after condition.");
        
        List<StatementNode> ifBlock = parseBlockNode();
        
        // Check if there is an elseif.
        while (match(TokenType.KW_ELSEIF)) {
            if (TOGGLE_DEBUG) System.out.println("parseElseIf();");
            
            consume(TokenType.LPAREN, "parseElseIf: Expect '(' after 'elseif'.");
            
            ExpressionNode elseIfCondition = parseExpr();
            consume(TokenType.RPAREN, "parseElseIf: Expect ')' after condition.");
            
            List<StatementNode> elseIfBlock = parseBlockNode();
            
            elseIfConditions.add(elseIfCondition);
            elseIfBlocks.add(elseIfBlock);
        }
        
        // Check if there is an else statement
        if (match(TokenType.KW_ELSE)) {
            if (TOGGLE_DEBUG) System.out.println("parseElse();");
            elseBlock = parseBlockNode();
        }
        return new IfNode(condition, ifBlock, elseIfConditions, elseIfBlocks, elseBlock);
    }
    
    // While Statement
    private StatementNode parseWhileStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseWhileStmt();");
        
        consume(TokenType.LPAREN, "parseWhileStmt: Expect '(' after 'while'.");
        ExpressionNode condition = parseExpr();
        
        consume(TokenType.RPAREN, "parseWhileStmt: Expect ')' after expression.");
        
        List<StatementNode> block = parseBlockNode();
        
        return new WhileNode(condition, block);
    }
    
    // For statement
    private StatementNode parseForStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseForStmt();");

        consume(TokenType.LPAREN, "parseForStmt: Expect '(' after 'for'.");

        // Init
        StatementNode initStmt;
        if (match(TokenType.KW_INT)) {
            initStmt = parseVarDecStmt();
        } else {
            throw error(peek(), "parseForStmt: Expect int variable declaration as for loop initializer.");
        }

        // Condition
        ExpressionNode condition = parseExpr();
        consume(TokenType.SEMICOLON, "parseForStmt: Expect ';' after condition.");

        // Update
        Token id = consume(TokenType.IDENTIFIER, "parseForStmt: Expect identifier as for loop update.");
        Token updateOp = consume(TokenType.OP_INCREMENT, "parseForStmt: Expect '++' after identifier.");
        IncrementNode update = new IncrementNode( new VariableNode(id), updateOp);

        consume(TokenType.RPAREN, "parseForStmt: Expect ')' after update.");

        List<StatementNode> block = parseBlockNode();
        return new ForNode((VarDecNode)initStmt, condition, update, block);
    }
    
    // Function Declaration
    private StatementNode parseFuncDecStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseFuncDecStmt();");
        
        
        Token returnType = consumeType();
        Token id = consume(TokenType.IDENTIFIER, "parseFuncDecStmt: Expect Identifier");
        
        consume(TokenType.LPAREN, "parseFuncDecStmt: Expect '(' after function identifier.");
        
        // Make a list of parameters for the function.
        List<FuncDecNode.ParameterNode> params = parseParameterList();
        
        consume(TokenType.RPAREN, "parseFuncDecStmt: Expect ')' after function parameters.");
        
        // Function body
        List<StatementNode> body = parseBlockNode();
        
        
        return new FuncDecNode(typeFromToken(returnType), id, params, body);
    }
    
    // Break;
    private StatementNode parseBreakStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseBreakStmt();");
        
        consume(TokenType.SEMICOLON, "parseBreakStmt: Expect ';' after break.");
        return new BreakNode();
    }
    
    // Continue;
    private StatementNode parseContinueStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseContinueStmt();");
        
        consume(TokenType.SEMICOLON, "parseBreakStmt: Expect ';' after continue.");
        return new ContinueNode();
    }
    
    // Return statement
    private StatementNode parseReturnStmt() {
        if (TOGGLE_DEBUG) System.out.println("parseReturnStmt();");
        
        
        ExpressionNode expr = parseExpr();
        consume(TokenType.SEMICOLON, "parseReturnStmt: Expect ';' after return value.");
        return new ReturnNode(expr);
    }
    
    
    // ===========
    // EXPRESSIONS
    // ===========
    
//    Logical expr (&&, ||) ->
//    Equality expr (== and != are not present) ->
//    Relational expr (<, >) ->
//    Additive expr (+, -) ->
//    Multiplicative expr (*, /) ->
//    Unary expr (!, ++, --) ->
//    Primary expr (literal, id, function call, member access)
    
    private ExpressionNode parseExpr() {
        if (TOGGLE_DEBUG) System.out.println("parseExpr();");;
        return parseLogicalExpr();
    }
    
    // &&, ||
    private ExpressionNode parseLogicalExpr() {
        if (TOGGLE_DEBUG) System.out.println("parseLogicalExpr();");
        
        
        ExpressionNode expr = parseEqualityExpr();
        while (match(TokenType.OP_OR, TokenType.OP_AND)) {
            Token operator = previous();
            ExpressionNode right = parseEqualityExpr();
            expr = new LogicalExprNode(expr, operator, right);
        }
        return expr;
    }
    
    // Equality expressions not in this language, skip
    private ExpressionNode parseEqualityExpr() {
        return parseRelationalExpr();
    }
    
    // <, >
    private ExpressionNode parseRelationalExpr() {
        if (TOGGLE_DEBUG) System.out.println("parseRelationalExpr();");
        
        
        ExpressionNode expr = parseAdditiveExpr();
        
        while (match(TokenType.OP_GREATER, TokenType.OP_GREATER_OR_EQUAL, TokenType.OP_LESS, TokenType.OP_LESS_OR_EQUAL)) {
            Token operator = previous();
            ExpressionNode right = parseAdditiveExpr();
            expr = new BinaryExprNode(expr, operator, right);
        }
        
        return expr;
    }
    
    // +, -
    private ExpressionNode parseAdditiveExpr() {
        if (TOGGLE_DEBUG) System.out.println("parseAdditiveExpr();");
        
        
        ExpressionNode expr = parseMultiplicativeExpr();
        
        while (match(TokenType.OP_MINUS, TokenType.OP_PLUS)) {
            Token operator = previous();
            ExpressionNode right = parseMultiplicativeExpr();
            expr = new BinaryExprNode(expr, operator, right);
        }
        return expr;
    }
    
    // *, /
    private ExpressionNode parseMultiplicativeExpr() {
        if (TOGGLE_DEBUG) System.out.println("parseMultiplicativeExpr();");
        
        
        ExpressionNode expr = parseUnaryExpr();
        
        while (match(TokenType.OP_DIVIDE, TokenType.OP_MULTIPLY)) {
            Token operator = previous();
            ExpressionNode right = parseUnaryExpr();
            expr = new BinaryExprNode(expr, operator, right);
        }
        
        return expr;
    }
    
    // !, ++, --
    private ExpressionNode parseUnaryExpr() {
        if (TOGGLE_DEBUG) System.out.println("parseUnaryExpr();");
        
        
        if (match(TokenType.OP_NOT, TokenType.OP_MINUS)) {
            Token operator = previous();
            ExpressionNode right = parseUnaryExpr();
            return new UnaryNode(operator, right);
        }
        
        return parsePrimaryExpr();
    }
    
    
    // Literals, Identifiers, Function calls, Field access
    private ExpressionNode parsePrimaryExpr() {
        if (TOGGLE_DEBUG) System.out.println("parsePrimaryExpr();");
        
        
        // Parse literals
        if (match(TokenType.KW_TRUE)) return new LiteralNode(true);
        if (match(TokenType.KW_FALSE)) return new LiteralNode(false);
        
        if (match(TokenType.INT_LITERAL, TokenType.STRING_LITERAL)) {
            return new LiteralNode(previous().literal);
        }
        
        // Parse Identifier
        if (match(TokenType.IDENTIFIER)) {
            Token name = previous();
            
            if (match(TokenType.LPAREN)) {
                List<ExpressionNode> args = new ArrayList<>();
                
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(parseExpr());
                    } while (match(TokenType.COMMA));
                }
                
                consume(TokenType.RPAREN, "Expect ')' after arguments.");
                
                return new FuncCallNode(name, args);
            }
            
            if (match(TokenType.DOT)) {
                Token field = consumeField();
                return new FieldAccessNode(new VariableNode(name), field);
            }
            
            return new VariableNode(name);
        }
        
        // Parenthesis
        if (match(TokenType.LPAREN)) {
            ExpressionNode expr = parseExpr();
            consume(TokenType.RPAREN, "parsePrimaryExpr: Expect ')' after expression.");
            return new Grouping(expr);
        }
        
        throw error(peek(), "parsePrimaryExpr: Expect expression");
    }
    
    // Parsing parameters for function declarations
    private FuncDecNode.ParameterNode parseParameterNode() {
        if (TOGGLE_DEBUG) System.out.println("parseParameterNode();");
        
        
        Token type = consumeType();
        Token id = consume(TokenType.IDENTIFIER, "parseParameterNode: Expect parameter name.");
        return new FuncDecNode.ParameterNode(typeFromToken(type), id);
    }
    
    private List<FuncDecNode.ParameterNode> parseParameterList() {
        if (TOGGLE_DEBUG) System.out.println("parseParameterList();");
        
        
        List<FuncDecNode.ParameterNode> params = new ArrayList<>();
        
        if (!check(TokenType.RPAREN)) {
            do {
                params.add(parseParameterNode());
            } while (match(TokenType.COMMA));
        }
        return params;
    }
    
    private List<StatementNode> parseBlockNode() {
        if (TOGGLE_DEBUG) System.out.println("parseBlockNode();");
        
        // Every block should have brackets before and after, so we can put them here
        consume(TokenType.LBRACE, "parseBlockNode: Expect '{' before block.");
        
        List<StatementNode> block = new ArrayList<>();
        
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            block.add(parseStmt());
        }
        
        consume(TokenType.RBRACE, "parseBlockNode: Expect '}' after block.");
        
        return block;
    }
    
    // Return true if the current token matches any of the TokenTypes.
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                if (TOGGLE_DEBUG) {
                    System.out.println("[MATCH] Matched: " + type);
                }
                advance();
                return true;
            }
        }
        return false;
    }
    
    // "Consume" the current token to make sure it doesn't get parsed again.
    // Reports an error if the token consumed does not match (syntax error)
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }
    
    private static final List<String> VALID_FIELDS = List.of(
            "hp", "attack", "defense", "type", "name"
    );
    
    // Check if the field is valid
    private Token consumeField() {
        if (match(TokenType.IDENTIFIER)) {
//        if (match(TokenType.KW_HP, TokenType.KW_ATTACK, TokenType.KW_DEFENSE, TokenType.KW_TYPE, TokenType.KW_NAME)) {
            Token field = previous();
            if (!VALID_FIELDS.contains(field.value)) {
                throw error(field, "Invalid Pokemon field: " + field.value);
            }
            return field;
        }
        throw error(peek(), "Expect Pokemon Field.");
    }
    
    // Check if the assignment operator is valid
    private Token consumeAssnOperator() {
        if (match(TokenType.OP_PLUS_ASSIGN, TokenType.OP_ASSIGN, TokenType.OP_MINUS_ASSIGN)) {
            return previous();
        }
        
        throw error(peek(), "Expect Assignment Operator.");
    }
    
    // Check if the type is valid
    private Token consumeType() {
        if (match(TokenType.KW_INT, TokenType.KW_STRING, TokenType.KW_BOOLEAN, TokenType.KW_VOID, TokenType.KW_POKEMON)) {
            return previous();
        }
        throw error(peek(), "Expect Type.");
    }
    
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    
    
    private Token advance() {
        if (!isAtEnd()) current++;
        Token token = previous();
        
        // For debugging, show what token is being parsed
        if (TOGGLE_DEBUG) {
            System.out.println(
                    "[ADVANCE] Consumed Token -> Type: "
                            + token.type +
                            ", Value: " + token.value +
                            ", Line: " + token.line
            );
        }
        return token;
    }
    
    // Returns true if the next token is the End of File.
    private boolean isAtEnd() {
        return peek().type == TokenType.KEYWORD_EOF;
    }
    
    // Returns the next token, but does not advance the list.
    private Token peek() {
        Token token = tokens.get(current);
        
        // Debugging
        if (TOGGLE_DEBUG) {
            System.out.println(
                    "[PEEK] Current Token -> Type: "
                            + token.type +
                            ", Value: " + token.value +
                            ", Line: " + token.line
            );
        }
        return token;
    }
    
    Type typeFromToken(Token token) {
        switch (token.type) {
            case KW_INT: return Type.INT;
            case KW_STRING: return Type.STRING;
            case KW_POKEMON: return Type.POKEMON;
            case KW_BOOLEAN: return Type.BOOLEAN;
            case KW_VOID: return Type.VOID;
            default:
                throw new RuntimeException("Unknown type keyword: " + token.type + " line " + token.line);
        }
    }
    
    // Returns the previous token.
    private Token previous() {
        return tokens.get(current - 1);
    }
    
    // Throws errors.
    private ParseError error(Token token, String message) {
        System.err.println(
                "[ERROR] Line " + token.line +
                        " | Found: (" + token.type + ": " + token.value + ")" +
                        " | Message: " + message
        );
        
        if (TOGGLE_DEBUG && current < tokens.size()) {
            System.err.println("Next token: " + peek().type);
        }
        
        parsedErrors++;
        return new ParseError();
    }
    
    // Skips tokens until next statement
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            
            switch (peek().type) {
                case KW_FOR, KW_ELSE, KW_FUNCTION, KW_PRINT, KW_RETURN, KW_WHILE, KW_ELSEIF, KW_INT, KW_IF, KW_STRING, KW_BOOLEAN, KW_SHOW -> {
                    return;
                }
            }
            
            advance();
        }
    }
}

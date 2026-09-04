import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Interpreter implements ASTVisitor<Object> {
    
    private final boolean TOGGLE_DEBUG = false;
    
    private Environment environment = new Environment();
    private SpellDatabase spellDb = new SpellDatabase();
    
    @Override
    public Void visitProgramNode(ProgramNode node) {
        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }
    
    @Override
    public Void visitVarDecNode(VarDecNode node) {
        Object value = null;
        
        if (node.expression != null) {
            value = evaluate(node.expression);
        }
        
        environment.define(node.name.value, value);
        return null;
    }
    
    @Override
    public Object visitPkmLoadNode(PkmLoadNode node) {
        
        String path = node.path.value;
        
        // Remove surrounding quotes
        path = path.substring(1, path.length() - 1);
        
//        Pokemon p = new Pokemon("Pikachu", "Electric", 100, 50, 40);
        Pokemon p = loadPokemon(path);
        environment.define(node.name.value, p);
        return null;
    }
    
    @Override
    public Object visitAssnNode(AssnNode node) {
        Object value = evaluate(node.expression);
        
        if (node.target instanceof VariableNode) {
            VariableNode var = (VariableNode) node.target;
            environment.assign(var.name, value);
            return value;
        }
        
        if (node.target instanceof FieldAccessNode) {
            FieldAccessNode field = (FieldAccessNode) node.target;
            Object obj = evaluate(field.object);
            
            if (!(obj instanceof Pokemon)) {
                throw new RuntimeError(field.field, "Only Pokemon Objects can have fields.");
            }
            
            Pokemon p = (Pokemon) obj;
            p.setField(field.field.value, value);
            return value;
         }
        
        return value;
    }
    
    @Override
    public Object visitSpellDbNode(SpellDbNode node) {
        String path = node.path.value;
        
        // Remove surrounding quotes
        path = path.substring(1, path.length() - 1);
        loadSpellDb(path);
        
        return null;
    }
    
    @Override
    public Object visitShowNode(ShowNode node) {
        Pokemon target = (Pokemon) environment.get(node.target);
        System.out.println("Showing " + target.getField("name"));
        return null;
    }
    
    @Override
    public Object visitPrintNode(PrintNode node) {
        Object value = evaluate(node.value);
        System.out.println(stringify(value));
        return null;
    }
    
    @Override
    public Object visitMoveNode(MoveNode node) {
        Pokemon user = (Pokemon) environment.get(node.user);
        Pokemon target = (Pokemon) environment.get(node.target);
        Spell spell = user.getSpell(node.move.value, spellDb);
        
        if (TOGGLE_DEBUG) {
            System.out.println("\nUser: " + user);
            System.out.println("Target: " + target.toString());
            System.out.println("Spell: " + spell + "\n");
        }
        
        String userName = user.getField("name").toString();
        String targetName = target.getField("name").toString();
        String spellName = spell.getName();
        
        System.out.println("\n" + userName + " used " + spellName + " on " + targetName + "!");
        if (spell.getType().equals("Attack")) {
            int damage = (int) user.getField("attack") - (int) target.getField("defense");
            
            // I want it to be faithful to Pokemon games, so no matter what,
            // an attack will always do at least 1 damage.
            if (damage <= 1) damage = 1;
            
            System.out.println("It dealt " + damage + " damage!");
            
            target.setField("hp", (int) target.getField("hp") - damage);
            System.out.println(targetName + "'s HP is now at " + target.getField("hp") + "!");
        }
        
        if (spell.getType().equals("Defend")) {
            int defense = (int) target.getField("defense") + spell.getValue();
            target.setField("defense", defense);
            
            String change = "raised";
            if (spell.getValue() < 0) change = "lowered";
            System.out.println(target.getField("name") + "'s defense was " + change + " to " + defense);
        }
        
        return null;
    }
    
    @Override
    public Object visitIfNode(IfNode node) {
        
        if (isTruthy(evaluate(node.condition))) {
            
            // Mirror entering the scopeStack like in ScopeVisitor
            // For scope management
            Environment previous = environment;
            environment = new Environment(environment);
            
            for (StatementNode stmt : node.block) {
                execute(stmt);
            }
            
            environment = previous;
        }
        return null;
    }
    
    @Override
    public Object visitWhileNode(WhileNode node) {
        
        while (isTruthy(evaluate(node.condition))) {
            
            try {
                Environment previous = environment;
                environment = new Environment(environment);
                
                for (StatementNode stmt : node.block) {
                    execute(stmt);
                }
                
                environment = previous;
            } catch (ContinueException e) {
            } catch (BreakException e) {
                break;
            }
        }
        return null;
    }
    
    @Override
    public Object visitForNode(ForNode node) {
        
        // Enter scope
        Environment previous = environment;
        environment = new Environment(environment);
        
        // Run init
        if (node.init != null) {
            execute(node.init);
        }
        
        // Loop
        while (true) {
            
            if (node.condition != null) {
                Object condition = evaluate(node.condition);
                
                if (!(condition instanceof Boolean)) {
                    throw new RuntimeException("For loop condition must be boolean.");
                }
                
                if (!((Boolean) condition)) break;
                
                try {
                    // Execute body
                    for (StatementNode stmt : node.block) {
                        execute(stmt);
                    }
                } catch (ContinueException e) {
                    // Skip to update
                } catch (BreakException e) {
                    break;
                }
                
                // Run update
                if (node.update != null) {
                    execute(node.update);
                }
            }
        }
        
        // Exit scope
        environment = previous;
        return null;
    }
    
    @Override
    public Object visitFuncDecNode(FuncDecNode node) {
        environment.define(node.id.value, node);
        return null;
    }
    
    @Override
    public Object visitFuncCallNode(FuncCallNode node) {
        if (TOGGLE_DEBUG) System.out.println("Calling function: " + node.name.value);
        
        Object value = environment.get(node.name);
        
        if (!(value instanceof FuncDecNode)) {
            throw new RuntimeException("Undefined function: " + node.name.value);
        }
        
        FuncDecNode func = (FuncDecNode) value;
        
        if (node.args.size() != func.parameters.size()) {
            throw new RuntimeException("Argument count mismatch in " + node.name.value);
        }
        
        // New Function scope
        Environment previous = environment;
        environment = new Environment(environment);
        
        
        // Parameters
        for (int i = 0; i < node.args.size(); i++) {
            Object argValue = evaluate(node.args.get(i));
            String paramName = func.parameters.get(i).id.value;
            
            environment.define(paramName, argValue);
        }
        
        Object returnValue = null;
        
        try {
            for (StatementNode stmt : func.block) {
                execute(stmt);
            }
        } catch (ReturnException e) {
            returnValue = e.value;
        }
        
        environment = previous;
        if (TOGGLE_DEBUG) System.out.println("Returning: " + returnValue);
        return returnValue;
    }
    
    @Override
    public Object visitBreakNode(BreakNode node) {
        throw new BreakException();
    }
    
    @Override
    public Object visitContinueNode(ContinueNode node) {
        throw new ContinueException();
    }
    
    @Override
    public Object visitReturnNode(ReturnNode node) {
        Object value = null;
        
        if (node.value != null) {
            value = evaluate(node.value);
        }
        
        throw new ReturnException(value);
    }
    
    @Override
    public Object visitParameterNode(FuncDecNode.ParameterNode node) {
        return null;
    }
    
    @Override
    public Object visitLogicalExprNode(LogicalExprNode node) {
        
        Object left = evaluate(node.left);
        
        if (!(left instanceof Boolean)) {
            throw new RuntimeError(node.operator, "Left operand must be boolean.");
        }
        
        // Immediate checks for situations where the result does not change regardless of right
        if (node.operator.type == TokenType.OP_AND) {
            if (!((Boolean) left)) {
                return false;
            }
        }
        
        if (node.operator.type == TokenType.OP_OR) {
            if ((Boolean) left) {
                return true;
            }
        }
        
        // Now we evaluate right
        Object right = evaluate(node.right);
        
        if (!(right instanceof  Boolean)) {
            throw new RuntimeError(node.operator, "Right operand must be boolean.");
        }
        
        if (node.operator.type == TokenType.OP_OR) {
            return (Boolean) left || (Boolean) right;
        }
        
        if (node.operator.type == TokenType.OP_AND) {
            return (Boolean) left && (Boolean) right;
        }
        return null;
    }
    
    @Override
    public Object visitVariableNode(VariableNode node) {
        return environment.get(node.name);
    }
    
    @Override
    public Object visitFieldAccessNode(FieldAccessNode node) {
        Object obj = evaluate(node.object);
        
        if (!(obj instanceof Pokemon)) {
            throw new RuntimeError(node.field, "Only Pokemon Objects can have fields.");
        }
        
        Pokemon p = (Pokemon) obj;
        return p.getField(node.field.value);
    }
    
    @Override
    public Object visitUnaryNode(UnaryNode node) {
        Object right = evaluate(node.right);
        
        if (node.operator.type == TokenType.OP_NOT) {
            if (!(right instanceof Boolean)) {
                throw new RuntimeError(node.operator, "Opperand must be of type Boolean.");
            }
            return !((Boolean) right);
        }
        
        if (node.operator.type == TokenType.OP_MINUS) {
            
            if (!(right instanceof Integer)) {
                throw new RuntimeError(node.operator, "Operand must be Integer.");
            }
            
            return -((Integer) right);
        }
        
        throw new RuntimeError(node.operator, "Unknown unary operator: " + node.operator.value);
    }
    
    @Override
    public Object visitLiteralNode(LiteralNode node) {
        return node.value;
    }
    
    @Override
    public Object visitGrouping(Grouping node) {
        return evaluate(node.expression);
    }
    
    @Override
    public Object visitBinaryExprNode(BinaryExprNode node) {
        Object left = evaluate(node.left);
        Object right = evaluate(node.right);
        
        switch(node.operator.type) {
            case OP_MINUS:
                checkNumberOperands(node.operator, left, right);
                return (Integer) left - (Integer) right;
            case OP_DIVIDE:
                if ((Integer) right == 0) throw new RuntimeError(node.operator, "Divide by zero error.");
                checkNumberOperands(node.operator, left, right);
                return Math.round((float) (Integer) left / (Integer) right);
            case OP_PLUS:
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left + (Integer) right;
                }
                
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(node.operator, "Operands must be two Integers or two Strings.");
            case OP_MULTIPLY:
                checkNumberOperands(node.operator, left, right);
                return (Integer) left * (Integer) right;
            case OP_GREATER:
                checkNumberOperands(node.operator, left, right);
                return (Integer) left > (Integer) right;
            case OP_LESS:
                checkNumberOperands(node.operator, left, right);
                return (Integer) left < (Integer) right;
            default:
                return null;
        }
    }
    
    @Override
    public Object visitIncrementNode(IncrementNode node) {
        if (node.target instanceof VariableNode) {
            VariableNode var = (VariableNode) node.target;
            Object value = environment.get(var.name);
            
            if (!(value instanceof Integer)) {
                throw new RuntimeError(var.name, "++ requires type Int.");
            }
            
            int updated = (Integer) value + 1;
            environment.assign(var.name, updated);
            
            return updated;
        }
        
        if (node.target instanceof FieldAccessNode) {
            FieldAccessNode field = (FieldAccessNode) node.target;
            
            Object obj = evaluate(field.object);
            
            if (!(obj instanceof Pokemon)) {
                throw new RuntimeError(field.field, "Only Pokemon fields support ++.");
            }
            
            Pokemon p = (Pokemon) obj;
            Object value = p.getField(field.field.value);
            
            if (!(value instanceof Integer)) {
                throw new RuntimeError(field.field, "Field must be type Int for ++ operation.");
            }
            
            int updated = (Integer) value + 1;
            p.setField(field.field.value, updated);
            
            return updated;
        }
        throw new RuntimeException("Invalid ++ target.");
    }
    
    @Override
    public Object visitExpressionStmtNode(ExpressionStmtNode node) {
        return evaluate(node.expression);
    }
    
    // Helper function for loading the spellDb
    private void loadSpellDb(String path) {
        try (Scanner scanner = new Scanner(new File(path))) {
            
            String currentName = null;
            String currentType = null;
            Integer currentValue = null;
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = cleanLine(line);
                
                if (line.isEmpty()) continue;
                
                // Extract name, in the form of:
                // Name {
                if (line.endsWith("{")) {
                    currentName = line.substring(0, line.length() - 1).trim();
                    continue;
                }
                
                // Extract Type, in the form of:
                // Type,
                if (line.endsWith(",")) {
                    currentType = line.substring(0, line.length() -1).trim();
                    continue;
                }
                
                // Extract Value, in the form of:
                // Value <int>
                if (line.startsWith("Value")) {
                    String[] parts = line.split("\\s+");
                    currentValue = Integer.parseInt(parts[1]);
                    continue;
                }
                
                // Ends with a '}'
                if (line.equals("}")) {
                    if (currentName == null || currentType == null || currentValue == null) {
                        throw new RuntimeException("Invalid Spell Format!\n" +
                                "Valid Format: \n" +
                                "Name {\n" +
                                "<Type>,\n" +
                                "Value <int>\n" +
                                "}");
                    }
                    
                    Spell spell = new Spell(currentName, currentType, currentValue);
                    if (TOGGLE_DEBUG) System.out.println("Spell created: " + spell);
                    spellDb.addSpell(spell);
                    
                    // Reset for next spell
                    currentName = null;
                    currentType = null;
                    currentValue = null;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to load spell database from file!", e);
        }
    }
    
    // Removes whitespace and checks for single line comments (//)
    private String cleanLine(String line) {
        int commentIndex = line.indexOf("//");
        if (commentIndex != -1) {
            line = line.substring(0, commentIndex);
        }
        return line.trim();
    }
    
    private Pokemon loadPokemon(String path) {
        try (Scanner scanner = new Scanner(new File(path))) {
            
            String name = null, type = null;
            Integer hp = null, attack = null, defend = null;
            
            String move1 = null, move2 = null, move3 = null, move4 = null;
            
            while (scanner.hasNextLine()) {
                String line = cleanLine(scanner.nextLine());
                
                if (line.isEmpty()) continue;
                
                if (line.startsWith("Name:")) {
                    String[] parts = line.split("\\s+");
                    name = parts[1];
                    continue;
                }
                
                if (line.startsWith("Type:")) {
                    String[] parts = line.split("\\s+");
                    type = parts[1];
                    continue;
                }
                
                if (line.startsWith("HP:")) {
                    hp = fetchInt(line);
                    continue;
                }
                
                if (line.startsWith("Attack:")) {
                    attack = fetchInt(line);
                    continue;
                }
                
                if (line.startsWith("Defend:")) {
                    defend = fetchInt(line);
                    continue;
                }
                
                if (line.startsWith("Move1")) {
                    move1 = fetchMoveName(line);
                    continue;
                }
                
                if (line.startsWith("Move2")) {
                    move2 = fetchMoveName(line);
                    continue;
                }
                
                if (line.startsWith("Move3")) {
                    move3 = fetchMoveName(line);
                    continue;
                }
                
                if (line.startsWith("Move4")) {
                    move4 = fetchMoveName(line);
                }
                // Implement Image if time permits
            }
            
            // Check if pokemon has all attributes assigned
            if (name == null || type == null || hp == null || attack == null || defend == null) {
                throw new RuntimeException("Pokemon is missing an attribute!");
            }
            
            // Same with moves
            if (move1 == null || move2 == null || move3 == null || move4 == null) {
                throw new RuntimeException("Pokemon is missing a move!");
            }
            
            Map<String, String> spellNames = new HashMap<>();
            spellNames.put("move1", move1);
            spellNames.put("move2", move2);
            spellNames.put("move3", move3);
            spellNames.put("move4", move4);
            
            Pokemon pokemon = new Pokemon(name, type, hp, attack, defend, spellNames);
            if (TOGGLE_DEBUG) System.out.println("Pokemon created: " + pokemon);
            return pokemon;
            
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Pokemon file not found!", e);
        }
    }
    
    private Integer fetchInt(String line) {
        String[] parts = line.split("\\s+");
        return Integer.parseInt(parts[1]);
    }
    
    private String fetchMoveName(String line) {
        String[] parts = line.split("\\s+");
        return parts[1];
    }
    
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Integer && right instanceof Integer) return;
        throw new RuntimeError(operator, "Operands must be of type Integer.");
    }
    
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }
    
    private Object evaluate(ExpressionNode node) {
        return node.accept(this);
    }
    

    void interpret(List<StatementNode> stmts) {
        try {
            for (StatementNode stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Main.runtimeError(error);
        }
    }
    
    private void execute(StatementNode stmt) {
        stmt.accept(this);
    }
    
    private String stringify(Object object) {
        if (object == null) return null;
        return object.toString();
    }
}

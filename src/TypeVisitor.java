public class TypeVisitor implements ASTVisitor<Type> {
    private final boolean TOGGLE_DEBUG = false;
    
    private int loopDepth = 0;
    
    // Store current return type for functions and such
    private Type currentType = null;
    
    @Override
    public Type visitProgramNode(ProgramNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitProgram");
        
        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
        }
        
        return Type.VOID;
    }
    
    @Override
    public Type visitVarDecNode(VarDecNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitVarDec");
        
        Type declaredType = node.type;
        Type valueType = node.expression.accept(this);
        
        if (declaredType != valueType) {
            error(node.name.line, "cannot assign " + valueType + " to " + declaredType);
        }
        
        return Type.VOID;
    }
    @Override
    public Type visitPkmLoadNode(PkmLoadNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitPkmLoad");
        
        return Type.POKEMON;
    }
    
    @Override
    public Type visitAssnNode(AssnNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitAssnNode");
        
        Type left = node.target.accept(this);
        Type right = node.expression.accept(this);
        
        if (left == Type.ERROR || right == Type.ERROR) {
            return Type.ERROR;
        }
        
        if (left != right) {
            error(node.operator.line, "Cannot assign: " + right + " to " + left);
        }
        return Type.VOID;
    }
    
    @Override
    public Type visitSpellDbNode(SpellDbNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitSpellDb");
        return Type.VOID;
    }
    
    @Override
    public Type visitShowNode(ShowNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitShow");
        
        return Type.VOID;
    }
    
    @Override
    public Type visitPrintNode(PrintNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitPrint");
        
        return Type.VOID;
    }
    
    @Override
    public Type visitMoveNode(MoveNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitMove");
        
        String move = node.move.value;
        
        if (!move.matches("move[1-4]")) error(node.move.line, "Invalid move: " + move);
        
        return Type.VOID;
    }
    
    @Override
    public Type visitIfNode(IfNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitIf");
        
        Type cond = node.condition.accept(this);
        
        if (cond != Type.BOOLEAN) {
            error("If condition must be boolean");
        }
        
        return Type.VOID;
    }
    
    @Override
    public Type visitWhileNode(WhileNode node) {
        loopDepth++;
        
        Type cond = node.condition.accept(this);
        if (cond != Type.BOOLEAN) {
            error("While condition must be boolean");
        }
        
        for (StatementNode stmt : node.block) {
            stmt.accept(this);
        }
        
        loopDepth--;
        return Type.VOID;
    }
    
    @Override
    public Type visitForNode(ForNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitFor");
        loopDepth++;
        
        node.condition.accept(this);
        for (StatementNode stmt : node.block) {
            stmt.accept(this);
        }
        
        loopDepth--;
        return Type.VOID;
    }
    
    @Override
    public Type visitFuncDecNode(FuncDecNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitFuncDec");
        
        
        Type previousType = currentType;
        currentType = node.returnType;
        
        // define parameters in scope
//        for (FuncDecNode.ParameterNode param : node.parameters) {
//            scopeStack.define(
//                    param.id.value,
//                    new Symbol(param.id.value, param.type)
//            );
//        }
        
        for (StatementNode stmt : node.block) {
            stmt.accept(this);
        }
        
        currentType = previousType;
        
        return Type.VOID;
    }
    
    @Override
    public Type visitFuncCallNode(FuncCallNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitFuncCall");
        
        if (node.symbol == null) {
            error(node.name.line, "Undefined function: " + node.name.value);
            return Type.ERROR;
        }
        
        FunctionSymbol func = node.symbol;
        
        if (node.args.size() != func.paramTypes.size()) {
            error(node.name.line, "Function '" + node.name.value + "' expects " +
                    func.paramTypes.size() + ", got " + node.args.size());
            return Type.ERROR;
        }
        
        for (int i = 0; i < node.args.size(); i++) {
            Type argType = node.args.get(i).accept(this);
            Type expected = func.paramTypes.get(i);
            
            if (argType != expected) {
                error(node.name.line, "Argument " + (i + 1) + " Type mismatch: expected " + expected + ", got " + argType);
            }
        }
        
        return func.returnType;
    }
    
    @Override
    public Type visitBreakNode(BreakNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitBreak");
        
        if (loopDepth == 0) {
            error("Break used outside of loop");
        }
        return Type.VOID;
    }
    
    @Override
    public Type visitContinueNode(ContinueNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitContinue");
        if (loopDepth == 0) {
            error("Continue used outside of loop");
        }
        return Type.VOID;
    }
    
    @Override
    public Type visitReturnNode(ReturnNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitReturn");
        
        // return outside of scope
        if (currentType == null) {
            error("Return statement outside of function");
            return Type.ERROR;
        }
        
        Type valueType = Type.VOID;
        
        if (node.value != null) {
            valueType = node.value.accept(this);
        }
        
        if (valueType != currentType) {
            error("Return type mismatch: expected " + currentType + ", got " + valueType);
        }
        
        return Type.VOID;
    }
    
    @Override
    public Type visitParameterNode(FuncDecNode.ParameterNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitParameter");
        
        return Type.VOID;
    }
    
    @Override
    public Type visitLogicalExprNode(LogicalExprNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitLogical");
        
        Type left = node.left.accept(this);
        Type right = node.right.accept(this);
        
        if (left == Type.ERROR || right == Type.ERROR) {
            return Type.ERROR;
        }
        
        if (left != Type.BOOLEAN || right != Type.BOOLEAN) {
            error(node.operator.line, "Logical expressions require boolean operands");
            return Type.ERROR;
        }
        return Type.BOOLEAN;
    }
    
    @Override
    public Type visitVariableNode(VariableNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitVariable");
        
        if (node.symbol == null) {
            error(node.name.line, "Undefined variable: " + node.name.value);
            return Type.ERROR;
        }
        
        return node.symbol.type;
    }
    
    @Override
    public Type visitFieldAccessNode(FieldAccessNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitFieldAccess");
        
        Type objType = node.object.accept(this);
        
        if (objType != Type.POKEMON) {
            error(node.field.line, "Only Pokemon objects may have fields.");
            return Type.ERROR;
        }
        
        // Checck field access operators
        switch(node.field.value) {
            case "name":
            case "type":
                return Type.STRING;
            case "hp":
            case "attack":
            case "defense":
                return Type.INT;
            default:
                error(node.field.line, "Invalid field: " + node.field.value);
                return Type.ERROR;
        }
    }
    
    @Override
    public Type visitUnaryNode(UnaryNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitUnary");
        
        Type exprType = node.right.accept(this);
        
        String op = node.operator.value;
        
        switch (op) {
            
            case "!":
                if (exprType != Type.BOOLEAN) {
                    error(node.operator.line, "! requires boolean");
                    return Type.ERROR;
                }
                return Type.BOOLEAN;
            
            case "-":
                if (exprType != Type.INT) {
                    error(node.operator.line, "- requires int");
                    return Type.ERROR;
                }
                return Type.INT;
            
            default:
                error(node.operator.line, "Unknown unary operator: " + op);
                return Type.ERROR;
        }
    }
    
    @Override
    public Type visitLiteralNode(LiteralNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitLiteral");
        
        if (node.value instanceof Integer) return Type.INT;
        if (node.value instanceof String) return Type.STRING;
        if (node.value instanceof Boolean) return Type.BOOLEAN;
        return Type.ERROR;
    }
    
    @Override
    public Type visitGrouping(Grouping node) {
        if (TOGGLE_DEBUG) System.out.println("visitGrouping");
        return node.expression.accept(this);
    }
    
    @Override
    public Type visitBinaryExprNode(BinaryExprNode node) {
        if (TOGGLE_DEBUG) System.out.println("visitBinary");
        
        Type left = node.left.accept(this);
        Type right = node.right.accept(this);
        
        if (left == Type.ERROR || right == Type.ERROR) return Type.ERROR;
        
        switch(node.operator.value) {
            case "+":
            case "-":
            case "*":
            case "/":
                if (left == Type.INT && right == Type.INT) {
                    return Type.INT;
                }
                break;
                
            case "==":
            case "!=":
                if (left == right) {
                    return Type.BOOLEAN;
                }
                break;
            case ">":
            case "<":
                if (left == Type.INT && right == Type.INT) {
                    return Type.BOOLEAN;
                }
                break;
        }
        
        error(node.operator.line, "Invalid operation: " + left + " " + node.operator.value + " " + right);
        return Type.ERROR;
    }
    
    @Override
    public Type visitIncrementNode(IncrementNode node) {
        Type t = node.target.accept(this);
        
        if (t != Type.INT) {
            error(node.operator.line, "++ operator requires type Int.");
            return Type.ERROR;
        }
        
        return Type.VOID;
    }
    
    @Override
    public Type visitExpressionStmtNode(ExpressionStmtNode node) {
        return Type.VOID;
    }
    
    private void error(int line, String message) {
        System.out.println("[Line " + line + "] Type error: " + message);
    }
    
    private void error(String message) {
        System.out.println("Type error: " + message);
    }
}
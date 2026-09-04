import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ScopeVisitor implements ASTVisitor<Void> {
    
    private SymbolTable scopeStack;
    
    public ScopeVisitor(SymbolTable scopeStack) {
        this.scopeStack = scopeStack;
    }
    
    @Override
    public Void visitProgramNode(ProgramNode node) {
        scopeStack.enterScope();
        
        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
        }
        
//        scopeStack.exitScope();
        return null;
    }
    
    @Override
    public Void visitVarDecNode(VarDecNode node) {
        String name = node.name.value;
        Type type = node.type;
        
        Symbol symbol = new Symbol(name, type);
        
        if (!scopeStack.define(name, symbol)) {
            System.err.println("Error: Variable '" + name + "' is already defined.");
        }
        
        node.symbol = symbol;
        
        if (node.expression != null) {
            node.expression.accept(this);
        }
        return null;
    }
    
    @Override
    public Void visitPkmLoadNode(PkmLoadNode node) {
        String name = node.name.value;
        
        Symbol symbol = new Symbol(name, Type.POKEMON);
        
        // Define pokemon variable in the scope
        if (!scopeStack.define(name, symbol)) {
            System.err.println("Error: Variable '" + name + "' is already defined.");
        }
        
        return null;
    }
    
    @Override
    public Void visitAssnNode(AssnNode node) {
        // Check if the variable exists
        node.target.accept(this);
        node.expression.accept(this);
        return null;
    }
    
    @Override
    public Void visitSpellDbNode(SpellDbNode node) {
        return null;
    }
    
    @Override
    public Void visitShowNode(ShowNode node) {
        String name = node.target.value;
        
        if (scopeStack.lookup(node.target.value) == null) {
            System.err.println("Error: Variable '" + name + "' is not defined.");
        }
        return null;
    }
    
    @Override
    public Void visitPrintNode(PrintNode node) {
        node.value.accept(this);
        return null;
    }
    
    @Override
    public Void visitMoveNode(MoveNode node) {
        String user = node.user.value;
        String target = node.target.value;
        
        if (scopeStack.lookup(user) == null) {
            System.err.println("Error: Variable '" + user + "' is not defined.");
        }
        
        if (scopeStack.lookup(target) == null) {
            System.err.println("Error: Variable '" + target + "' is not defined.");
        }
        return null;
    }
    
    @Override
    public Void visitIfNode(IfNode node) {
        
        node.condition.accept(this);
        
        scopeStack.enterScope();
        // Inside if block
        for (StatementNode stmt : node.block) {
            stmt.accept(this);
        }
        
        scopeStack.exitScope();
        
        for (int i = 0; i < node.elseIfBlocks.size(); i++) {
            node.elseIfConditions.get(i).accept(this);
            
            scopeStack.enterScope();
            // Enter else if
            
            for (StatementNode stmt : node.elseIfBlocks.get(i)) {
                stmt.accept(this);
            }
            
            scopeStack.exitScope();
        }
        
        if (node.elseBlock != null) {
            scopeStack.enterScope();
            // Enter else block
            for (StatementNode stmt : node.elseBlock) {
                stmt.accept(this);
            }
            
            scopeStack.exitScope();
        }
        
        return null;
    }
    
    @Override
    public Void visitWhileNode(WhileNode node) {
        
        node.condition.accept(this);
        
        scopeStack.enterScope();
        
        for (StatementNode stmt : node.block) {
            stmt.accept(this);
        }
        
        scopeStack.exitScope();
        
        return null;
    }
    
    @Override
    public Void visitForNode(ForNode node) {
        
        scopeStack.enterScope();
        
        node.init.accept(this);
        node.condition.accept(this);
        node.update.accept(this);
        
        for (StatementNode stmt : node.block) {
            stmt.accept(this);
        }
        
        scopeStack.exitScope();
        return null;
    }
    
    @Override
    public Void visitFuncDecNode(FuncDecNode node) {
        
        String funcName = node.id.value;
        
        List<Type> paramTypes = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();
        
        for (FuncDecNode.ParameterNode param : node.parameters) {
            paramTypes.add(param.type);
            paramNames.add(param.id.value);
        }
        
        FunctionSymbol funcSymbol =
                new FunctionSymbol(funcName, paramTypes, paramNames, node.block, node.returnType);
        
        if (!scopeStack.define(funcName, funcSymbol)) {
            System.err.println("Error: Function '" + funcName + "' is already defined.");
        }
        
        node.symbol = funcSymbol;
        
        scopeStack.enterScope();
        for (FuncDecNode.ParameterNode param : node.parameters) {
            Symbol paramSym = new Symbol(param.id.value, param.type);
            scopeStack.define(param.id.value, paramSym);

            param.symbol = paramSym;
        }

        for (StatementNode stmt : node.block) {
            stmt.accept(this);
        }

        scopeStack.exitScope();
        return null;
    }
    
    @Override
    public Void visitFuncCallNode(FuncCallNode node) {
        Symbol sym = scopeStack.lookup(node.name.value);
        
        if (sym == null || !(sym instanceof FunctionSymbol)) {
            System.err.println("Error: Function '" + node.name.value + "' is not defined.");
        } else {
            node.symbol = (FunctionSymbol) sym;
        }
        
        for (ExpressionNode arg : node.args) {
            arg.accept(this);
        }
        
        return null;
    }
    
    @Override
    public Void visitBreakNode(BreakNode node) {
        return null;
    }
    
    @Override
    public Void visitContinueNode(ContinueNode node) {
        return null;
    }
    
    @Override
    public Void visitReturnNode(ReturnNode node) {
        if (node.value != null) {
            node.value.accept(this);
        }
        return null;
    }
    
    @Override
    public Void visitParameterNode(FuncDecNode.ParameterNode node) {
        return null;
    }
    
    @Override
    public Void visitLogicalExprNode(LogicalExprNode node) {
        node.left.accept(this);
        node.right.accept(this);
        return null;
    }
    
    @Override
    public Void visitVariableNode(VariableNode node) {
        Symbol sym = scopeStack.lookup(node.name.value);
        
        if (sym == null) {
            System.err.println("Error: Variable '" + node.name.value + "' is not defined.");
        }
        
        node.symbol = sym;
        
        return null;
    }
    
    @Override
    public Void visitFieldAccessNode(FieldAccessNode node) {
        
        node.object.accept(this);
        return null;
    }
    
    @Override
    public Void visitUnaryNode(UnaryNode node) {
        node.right.accept(this);
        return null;
    }
    
    @Override
    public Void visitLiteralNode(LiteralNode node) {
        return null;
    }
    
    @Override
    public Void visitGrouping(Grouping node) {
        node.expression.accept(this);
        return null;
    }
    
    @Override
    public Void visitBinaryExprNode(BinaryExprNode node) {
        node.left.accept(this);
        node.right.accept(this);
        return null;
    }
    
    @Override
    public Void visitIncrementNode(IncrementNode node) {
        node.target.accept(this);
        return null;
    }
    
    @Override
    public Void visitExpressionStmtNode(ExpressionStmtNode node) {
        node.expression.accept(this);
        return null;
    }
}

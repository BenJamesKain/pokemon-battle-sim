import java.util.ArrayList;
import java.util.List;

abstract class StatementNode extends ASTNode {
    abstract <T> T accept(ASTVisitor<T> visitor);
}

class VarDecNode extends StatementNode {
        public Type type;
        public Token name;
        public ExpressionNode expression;
        public Symbol symbol;
        
        public VarDecNode(Type type, Token name, ExpressionNode expression) {
            this.type = type;
            this.name = name;
            this.expression = expression;
        }
        
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitVarDecNode(this);
    }
}

class AssnNode extends StatementNode {
        public ExpressionNode target;
        public Token operator;
        public ExpressionNode expression;
        
        public AssnNode(ExpressionNode target, Token operator, ExpressionNode expression) {
            this.target = target;
            this.operator = operator;
            this.expression = expression;
        }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitAssnNode(this);
    }
}

class PkmLoadNode extends StatementNode {
    public Token name;
    public Token path;
    
    public PkmLoadNode(Token name, Token path) {
        this.name = name;
        this.path = path;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitPkmLoadNode(this);
    }
}

class SpellDbNode extends StatementNode {
    public Token path;
    
    public SpellDbNode(Token path) {
        this.path = path;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSpellDbNode(this);
    }
}

class ShowNode extends StatementNode {
    public Token target;
    
    public ShowNode(Token target) {
        this.target = target;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitShowNode(this);
    }
}

class PrintNode extends StatementNode {
    public ExpressionNode value;
    
    public PrintNode(ExpressionNode value) {
        this.value = value;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitPrintNode(this);
    }
}

class MoveNode extends StatementNode {
    public Token user;
    public Token move;
    public Token target;
    
    public MoveNode(Token user, Token move, Token target) {
        this.user = user;
        this.move = move;
        this.target = target;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitMoveNode(this);
    }
}

class IfNode extends StatementNode {
    public final ExpressionNode condition;
    public final List<StatementNode> block;
    public List<ExpressionNode> elseIfConditions = new ArrayList<>();
    public List<List<StatementNode>> elseIfBlocks = new ArrayList<>();
    public List<StatementNode> elseBlock;
    
    public IfNode(ExpressionNode condition, List<StatementNode> block,
                  List<ExpressionNode> elseIfConditions, List<List<StatementNode>> elseIfBlocks, List<StatementNode> elseBlock) {
        this.condition = condition;
        this.block = block;
        this.elseIfConditions = elseIfConditions;
        this.elseIfBlocks = elseIfBlocks;
        this.elseBlock = elseBlock;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIfNode(this);
    }
}

class WhileNode extends StatementNode {
    public final ExpressionNode condition;
    public final List<StatementNode> block;
    
    public WhileNode(ExpressionNode condition, List<StatementNode> block) {
        this.condition = condition;
        this.block = block;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitWhileNode(this);
    }
}

class ForNode extends StatementNode {
    public final VarDecNode init;
    public final ExpressionNode condition;
    public final IncrementNode update;
    public List<StatementNode> block;
    
    public ForNode(VarDecNode init, ExpressionNode condition, IncrementNode update, List<StatementNode> block) {
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.block = block;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitForNode(this);
    }
}

class FuncDecNode extends StatementNode {
    public final Type returnType;
    public final Token id;
    public final List<ParameterNode> parameters;
    public final List<StatementNode> block;
    
    public FunctionSymbol symbol;
    
    // Parameter inner class, as this is only used here
    static class ParameterNode {
        public final Type type;
        public final Token id;
        public Symbol symbol;
        
        ParameterNode(Type type, Token id) {
            this.type = type;
            this.id = id;
        }
        
        public <T> T accept(ASTVisitor<T> visitor) {
            return visitor.visitParameterNode(this);
        }
    }
    
    public FuncDecNode(Type returnType, Token id, List<ParameterNode> parameters, List<StatementNode> block) {
        this.returnType = returnType;
        this.id = id;
        this.parameters = parameters;
        this.block = block;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFuncDecNode(this);
    }
}

class BreakNode extends StatementNode {
    public BreakNode() {}
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBreakNode(this);
    }
}

class ContinueNode extends StatementNode {
    public ContinueNode() {}
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitContinueNode(this);
    }
}

class ReturnNode extends StatementNode {
    public final ExpressionNode value;
    
    public ReturnNode(ExpressionNode value) {
        this.value = value;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitReturnNode(this);
    }
}

class IncrementNode extends StatementNode {
    public ExpressionNode target;
    public Token operator;
    
    public IncrementNode(ExpressionNode target, Token operator) {
        this.target = target;
        this.operator = operator;
    }
    
    @Override
    <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIncrementNode(this);
    }
}


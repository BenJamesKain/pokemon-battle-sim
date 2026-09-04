import java.util.List;

abstract class ExpressionNode extends ASTNode {
    abstract <T> T accept(ASTVisitor<T> visitor);
}

class LogicalExprNode extends ExpressionNode {
    final ExpressionNode left;
    final Token operator;
    final ExpressionNode right;
    
    public LogicalExprNode(ExpressionNode left, Token operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLogicalExprNode(this);
    }
}

class BinaryExprNode extends ExpressionNode {
    
    final ExpressionNode left;
    final Token operator;
    final ExpressionNode right;
    
    public BinaryExprNode(ExpressionNode left, Token operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBinaryExprNode(this);
    }
}

class Grouping extends ExpressionNode {
    final ExpressionNode expression;
    
    public Grouping(ExpressionNode expression) {
        this.expression = expression;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitGrouping(this);
    }
}

class LiteralNode extends ExpressionNode {
    
    final Object value;
    
    public LiteralNode(Object value) {
        this.value = value;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLiteralNode(this);
    }
}

class UnaryNode extends ExpressionNode {
    
    public final Token operator;
    public final ExpressionNode right;
    public UnaryNode(Token operator, ExpressionNode right) {
        this.operator = operator;
        this.right = right;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUnaryNode(this);
    }
}

class FieldAccessNode extends ExpressionNode {
    public final ExpressionNode object;
    public final Token field;
    
    public FieldAccessNode(ExpressionNode object, Token field) {
        this.object = object;
        this.field = field;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFieldAccessNode(this);
    }
}

class VariableNode extends ExpressionNode {
    public Token name;
    public Symbol symbol;
    
    public VariableNode(Token name) {
        this.name = name;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitVariableNode(this);
    }
}

class FuncCallNode extends ExpressionNode {
    public final Token name;
    public final List<ExpressionNode> args;
    public FunctionSymbol symbol;
    
    public FuncCallNode(Token name, List<ExpressionNode> args) {
        this.name = name;
        this.args = args;
    }
    
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFuncCallNode(this);
    }
}



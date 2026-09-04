public class ExpressionStmtNode extends StatementNode {
    public final ExpressionNode expression;
    
    public ExpressionStmtNode(ExpressionNode expression) {
        this.expression = expression;
    }
    
    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitExpressionStmtNode(this);
    }
}
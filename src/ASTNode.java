public abstract class ASTNode {
    abstract <T> T accept(ASTVisitor<T> visitor);
}

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends ASTNode {
    public final List<StatementNode> statements = new ArrayList<>();
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitProgramNode(this);
    }
}

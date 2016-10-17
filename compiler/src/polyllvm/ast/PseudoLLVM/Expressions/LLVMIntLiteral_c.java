package polyllvm.ast.PseudoLLVM.Expressions;

import polyglot.ast.Ext;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PseudoLLVM.LLVMTypes.LLVMTypeNode;

public class LLVMIntLiteral_c extends LLVMExpr_c implements LLVMIntLiteral {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected long value;
    protected LLVMTypeNode typeNode;

    public LLVMIntLiteral_c(Position pos, long value, LLVMTypeNode tn,
            Ext ext) {
        super(pos, ext);
        this.value = value;
        typeNode = tn;
    }

    protected <N extends LLVMIntLiteral_c> N value(N n, long value) {
        if (n.value == value) return n;
        n = copyIfNeeded(n);
        n.value = value;
        return n;
    }

    protected <N extends LLVMIntLiteral_c> N reconstruct(N n, long value) {
        return value(n, value);
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write(toString());
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public LLVMTypeNode typeNode() {
        return typeNode;
    }

}
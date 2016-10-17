package polyllvm.ast.PseudoLLVM;

import polyllvm.ast.PolyLLVMNodeFactory;
import polyllvm.ast.PseudoLLVM.Statements.LLVMInstruction;
import polyllvm.ast.PseudoLLVM.Statements.LLVMSeq;

import java.util.List;

/**
 * @author Daniel
 *
 */
public interface LLVMBlock extends LLVMInstruction {

    /**
     * Return a new LLVMBlock with the code replaced with {@code instructions}
     */
    LLVMBlock instructions(List<LLVMInstruction> instructions);

    /**
     * Return a new LLVMBlock with {@code i} appended to the end of
     * the current block's instructions
     */
    LLVMBlock appendInstruction(LLVMInstruction i);

    /**
     * Return the list of instructions in this block as an LLVMSeq
     */
    LLVMSeq instructions(PolyLLVMNodeFactory nf);

    /**
     * Return the list of instructions in this block
     */
    List<LLVMInstruction> instructions();

}
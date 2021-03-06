//Copyright (C) 2018 Cornell University

package jlang.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;
import polyglot.util.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jlang.ast.JLangNodeFactory;
import jlang.types.JLangTypeSystem;

/**
 * Builds class initializers at the top of each constructor.
 * Preserves typing.
 */
public class DesugarInstanceInitializers extends DesugarVisitor {
    private static final int OUTER_CLASS_FORMAL_IDX = 0;
	private static final String ENCLOSING_FORMAL_NAME = "enclosingFormal$";
	private static final String SELF_FORMAL_NAME = "selfFormal$";
	private static final String INSTANCE_INIT_FUNC = "init$instance";

    public DesugarInstanceInitializers(Job job, JLangTypeSystem ts, JLangNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public ClassBody leaveClassBody(ParsedClassType ct, ClassBody cb) {
        // TODO: Erase initializers here after they're moved, then remove LLVMInitializerExt.

        if (ct.flags().isInterface())
            return super.leaveClassBody(ct, cb);
        assert !ct.constructors().isEmpty();

        // Collect class initialization code.
        List<Stmt> initCode = new ArrayList<>();
        Formal enclosingClassFormal = DesugarInnerClasses.hasEnclosingParameter(ct) ?
        		tnf.Formal(ct.position(), ENCLOSING_FORMAL_NAME, ct.outer(), Flags.FINAL) :
        		null;
        Formal selfClassFormal = tnf.Formal(ct.position(), SELF_FORMAL_NAME, ct, Flags.NONE);

        for (ClassMember member : cb.members()) {

            // Build initialization assignments for each initialized non-static field.
            if (member instanceof FieldDecl) {
                FieldDecl fd = (FieldDecl) member;
                Position pos = fd.position();
                if (fd.flags().isStatic() || fd.init() == null)
                    continue;
                Receiver receiver = tnf.Local(pos, selfClassFormal);
                Field field = tnf.Field(pos, receiver, fd.name());
                Expr rhs = (fd.name().equals(DeclareEnclosingInstances.ENCLOSING_STR))
                		? tnf.Local(pos, enclosingClassFormal)
                		: fd.init() ;
                Stmt assign = tnf.EvalAssign(field, rhs);
                initCode.add(assign);
            }

            // Build initialization blocks.
            if (member instanceof Initializer) {
                Initializer init = (Initializer) member;
                if (init.flags().isStatic())
                    continue;
                initCode.add(init.body());
            }
        }

        if (initCode.isEmpty())
            return super.leaveClassBody(ct, cb); // Optimization.

        
        List<Formal> initFormals = new LinkedList<>();
        initFormals.add(selfClassFormal);
        if (DesugarInnerClasses.hasEnclosingParameter(ct)) {
        	initFormals.add(enclosingClassFormal);
        }

        // Declare init method.
		MethodDecl initMethod = tnf.MethodDecl(
                ct.position(), ct,
                Flags.NONE.Private().Final().Static(), ts.Void(), INSTANCE_INIT_FUNC,
                initFormals,
                nf.Block(ct.position(), initCode));
        cb = cb.addMember(initMethod);

        cb = mapConstructors(cb, (ctor) -> {
            List<Stmt> stmts = new LinkedList<>();
            LinkedList<Stmt> oldStmts = new LinkedList<>(ctor.body().statements());

            // Check for a call to another constructor.
            // The JLS ensures that a constructor call will be the first statement.
            if (oldStmts.peek() instanceof ConstructorCall) {
                ConstructorCall call = (ConstructorCall) oldStmts.pop();
                if (call.kind().equals(ConstructorCall.THIS)) {
                    // Avoid duplicating initializer side-effects; the other
                    // constructor will handle initialization.
                    return ctor;
                }
                // Keep the constructor call at the beginning.
                stmts.add(call);
            }

            // Call init function
            ;
            Expr selfObj = tnf.UnqualifiedThis(ct.position(), ct);
            Expr[] args = (DesugarInnerClasses.hasEnclosingParameter(ct)) ?
            		new Expr[] {
            				selfObj, 
            				tnf.Local(ct.position(), ctor.formals().get(OUTER_CLASS_FORMAL_IDX))
            				} :
            			new Expr[] { selfObj };
            Call callInitFunc = tnf.Call(
                    ct.position(),
                    tnf.UnqualifiedThis(ct.position(), ct),
                    INSTANCE_INIT_FUNC,
                    ct, ts.Void(), args);
            stmts.add(nf.Eval(ct.position(), callInitFunc));

            // Add back remaining constructor code.
            stmts.addAll(oldStmts);
            return (ConstructorDecl) ctor.body(ctor.body().statements(stmts));
        });

        return super.leaveClassBody(ct, cb);
    }
}

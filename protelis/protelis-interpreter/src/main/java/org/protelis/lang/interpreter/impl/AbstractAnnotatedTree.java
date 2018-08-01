/*******************************************************************************
 * Copyright (C) 2014, 2015, Danilo Pianini and contributors
 * listed in the project's build.gradle or pom.xml file.
 *
 * This file is part of Protelis, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE.txt in this project's top directory.
 *******************************************************************************/
package org.protelis.lang.interpreter.impl;

import static java8.util.stream.StreamSupport.parallelStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.protelis.lang.interpreter.AnnotatedTree;
import org.protelis.vm.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import java8.util.stream.IntStream;
import java8.util.stream.IntStreams;

/**
 * Basic implementation of an {@link AnnotatedTree}.
 *
 * @param <T>
 *            annotation type
 */
public abstract class AbstractAnnotatedTree<T> implements AnnotatedTree<T> {

    private static final long serialVersionUID = -8156985119843359212L;
    private static final Logger L = LoggerFactory.getLogger(AbstractAnnotatedTree.class);
    private final List<AnnotatedTree<?>> branches;
    private T annotation;
    private boolean erased = true;

    /**
     * @param branch
     *            the branches for this tree
     */
    protected AbstractAnnotatedTree(final AnnotatedTree<?>... branch) {
        this(Arrays.asList(branch));
    }

    /**
     * @param branch
     *            the branches for this tree
     */
    protected AbstractAnnotatedTree(final List<AnnotatedTree<?>> branch) {
        Objects.requireNonNull(branch);
        branches = branch;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    /**
     * @param sb
     *            {@link StringBuilder} to fill
     * @param i
     *            level of indentation
     */
    @Override
    public final void toString(final StringBuilder sb, final int i) {
        indent(sb, i);
        if (erased) {
            sb.append('|');
            asString(sb, i);
            sb.append('|');
        } else {
            asString(sb, i);
            sb.append('\n');
            indent(sb, i);
            sb.append(':');
            if (annotation instanceof AnnotatedTree<?>) {
                sb.append('\n');
                ((AnnotatedTree<?>) annotation).toString(sb, i + 1);
            } else {
                sb.append(annotation);
            }
        }
    }

    /**
     * @param sb
     *            {@link StringBuilder} to fill
     * @param indent
     *            level of indentation
     */
    protected abstract void asString(StringBuilder sb, int indent);

    @Override
    public final T getAnnotation() {
        return annotation;
    }

    /**
     * {@inheritDoc}
     * Subclasses overriding this method must call super.{@link #erase()}
     */
    @Override
    public void erase() {
        for (final AnnotatedTree<?> b : branches) {
            b.erase();
        }
        annotation = null;
        erased = true;
    }

    /**
     * @param obj
     *            the annotation to set
     */
    protected final void setAnnotation(final T obj) {
        annotation = obj;
        erased = false;
    }

    @Override
    public boolean isErased() {
        return erased;
    }

    /**
     * @return Directly accesses the {@link List} where branches are stored:
     *         modifications on branches will reflect in the internal branch
     *         representation. Be careful.
     */
    protected List<AnnotatedTree<?>> getBranches() {
        return branches;
    }

    /**
     * @return the current branches annotations
     */
    protected final Object[] getBranchesAnnotations() {
        final Object[] annotations = branches.toArray();
        for (int i = 0; i < annotations.length; i++) {
            annotations[i] = ((AnnotatedTree<?>) annotations[i]).getAnnotation();
        }
        return annotations;
    }

    /**
     * @return the number of branches
     */
    protected final int getBranchesNumber() {
        return branches.size();
    }

    /**
     * Facility to run lambdas across all the branches.
     * 
     * @param action
     *            the Consumer to execute
     */
    protected final void forEach(final Consumer<? super AnnotatedTree<?>> action) {
        for (final AnnotatedTree<?> subProgram: branches) {
            action.accept(subProgram);
        }
    }

    /**
     * Facility to run lambdas across all the branches.
     * 
     * @param action
     *            the Consumer to execute
     */
    protected final void forEachWithIndex(final BiConsumer<Integer, ? super AnnotatedTree<?>> action) {
        for (int i = 0; i < getBranchesNumber(); i++) {
            action.accept(i, getBranch(i));
        }
    }

    /**
     * Facility to run lambdas across all the branches in a PARALELL fashion. Be
     * EXTREMELY careful with this. If you are not sure whether or not you
     * should use this, you should not.
     * 
     * @param action
     *            the Consumer to execute
     */
    protected final void parallelForEach(final Consumer<? super AnnotatedTree<?>> action) {
        parallelStream(branches).forEach(action);
    }

    /**
     * Facility to run lambdas across all the branches in a PARALELL fashion. Be
     * EXTREMELY careful with this. If you are not sure whether or not you
     * should use this, you should not.
     * 
     * @param action
     *            the Consumer to execute
     */
    protected final void parallelForEachWithIndex(final BiConsumer<Integer, ? super AnnotatedTree<?>> action) {
        indexStream().parallel().forEach(i -> action.accept(i, getBranch(i)));
    }

    private IntStream indexStream() {
        return IntStreams.range(0, getBranchesNumber());
    }

    /**
     * Runs eval() sequentially on every branch, creating a new stack frame for
     * each one.
     * 
     * @param context
     *            the execution context
     */
    protected final void projectAndEval(final ExecutionContext context) {
        forEachWithIndex((i, branch) -> {
            context.newCallStackFrame(i.byteValue());
            branch.eval(context);
            context.returnFromCallFrame();
        });
    }

    /**
     * Subclasses must use this method.
     * 
     * @return a deep copy of the branches
     */
    protected final List<AnnotatedTree<?>> deepCopyBranches() {
        final List<AnnotatedTree<?>> res = new ArrayList<>(branches.size());
        for (final AnnotatedTree<?> br : branches) {
            res.add(br.copy());
        }
        return res;
    }

    @Override
    public final AnnotatedTree<?> getBranch(final int i) {
        return branches.get(i);
    }

    /**
     * Utility for indenting lines.
     * 
     * @param target
     *            the {@link StringBuilder} containing
     * @param i
     *            the level of indentation
     */
    protected static void indent(final Appendable target, final int i) {
        for (int j = 0; j < i; j++) {
            try {
                target.append('\t');
            } catch (IOException e) {
                L.error("There is a bug.", e);
            }
        }
    }

    /**
     * Print utility to be used by subclasses. Prints all branches with the
     * desired separator.
     * 
     * @param sb
     *            the {@link StringBuilder} to use
     * @param i
     *            indentation
     * @param separator
     *            separator
     */
    protected void fillBranches(final StringBuilder sb, final int i, final char separator) {
        forEach(b -> {
            sb.append('\n');
            b.toString(sb, i + 1);
            sb.append(separator);
        });
        if (getBranchesNumber() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    @Override
    public void evalInNewStackFrame(final ExecutionContext context, final byte frameId) {
        context.newCallStackFrame(frameId);
        eval(context);
        context.returnFromCallFrame();
    }
}

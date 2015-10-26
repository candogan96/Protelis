/*******************************************************************************
 * Copyright (C) 2010, 2015, Danilo Pianini and contributors
 * listed in the project's build.gradle or pom.xml file.
 *
 * This file is part of Protelis, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE.txt in this project's top directory.
 *******************************************************************************/
package org.protelis.lang.interpreter.impl;

import org.protelis.vm.ExecutionContext;

/**
 * "Delta time" - amount of time that has passed since last evaluation.
 */
public class Dt extends AbstractAnnotatedTree<Number> {

	private static final long serialVersionUID = -583345937082081400L;
	
	@Override
	public Dt copy() {
		return new Dt();
	}

	@Override
	public void eval(final ExecutionContext context) {
		setAnnotation(context.getDeltaTime());
	}

	@Override
	protected void asString(final StringBuilder sb, final int i) {
		sb.append("dt");
	}

}
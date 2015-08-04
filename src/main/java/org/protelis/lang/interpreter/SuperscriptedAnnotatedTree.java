/*******************************************************************************
 * Copyright (C) 2014, 2015, Danilo Pianini and contributors
 * listed in the project's build.gradle or pom.xml file.
 *
 * This file is part of Protelis, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE.txt in this project's top directory.
 *******************************************************************************/
package org.protelis.lang.interpreter;

/**
 * @author Danilo Pianini
 *
 * @param <S>
 *            Superscript type
 * @param <T>
 *            Annotation type
 */
public interface SuperscriptedAnnotatedTree<S, T> extends AnnotatedTree<T> {

	/**
	 * @return the superscript
	 */
	S getSuperscript();

}

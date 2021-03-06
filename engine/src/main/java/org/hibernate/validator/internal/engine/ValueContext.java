/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.lang.annotation.ElementType;
import java.lang.reflect.Type;

import javax.validation.groups.Default;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * An instance of this class is used to collect all the relevant information for validating a single class, property or
 * method invocation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ValueContext<T, V> {

	private final ExecutableParameterNameProvider parameterNameProvider;

	/**
	 * The current bean which gets validated. This is the bean hosting the constraints which get validated.
	 */
	private final T currentBean;

	/**
	 * The class of the current bean.
	 */
	private final Class<T> currentBeanType;

	/**
	 * The current property path we are validating.
	 */
	private PathImpl propertyPath;

	/**
	 * The current group we are validating.
	 */
	private Class<?> currentGroup;

	/**
	 * The value which gets currently evaluated.
	 */
	private V currentValue;

	private final Validatable currentValidatable;

	/**
	 * The {@code ElementType} the constraint was defined on
	 */
	private ElementType elementType;

	/**
	 * The declared type of validated value.
	 */
	private Type declaredTypeOfValidatedElement;

	/**
	 * An optional value unwrapper for the current value
	 */
	private ValidatedValueUnwrapper<V> validatedValueHandler;

	/**
	 * Enum specifying how to handle validated values
	 */
	private UnwrapMode unwrapMode = UnwrapMode.AUTOMATIC;

	public static <T, V> ValueContext<T, V> getLocalExecutionContext(ExecutableParameterNameProvider parameterNameProvider, T value, Validatable validatable, PathImpl propertyPath) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) value.getClass();
		return new ValueContext<>( parameterNameProvider, value, rootBeanClass, validatable, propertyPath );
	}

	public static <T, V> ValueContext<T, V> getLocalExecutionContext(ExecutableParameterNameProvider parameterNameProvider, Class<T> type, Validatable validatable, PathImpl propertyPath) {
		return new ValueContext<>( parameterNameProvider, null, type, validatable, propertyPath );
	}

	private ValueContext(ExecutableParameterNameProvider parameterNameProvider, T currentBean, Class<T> currentBeanType, Validatable validatable, PathImpl propertyPath) {
		this.parameterNameProvider = parameterNameProvider;
		this.currentBean = currentBean;
		this.currentBeanType = currentBeanType;
		this.currentValidatable = validatable;
		this.propertyPath = propertyPath;
	}

	public final PathImpl getPropertyPath() {
		return propertyPath;
	}

	public final Class<?> getCurrentGroup() {
		return currentGroup;
	}

	public final T getCurrentBean() {
		return currentBean;
	}

	public final Class<T> getCurrentBeanType() {
		return currentBeanType;
	}

	public Validatable getCurrentValidatable() {
		return currentValidatable;
	}

	/**
	 * Returns the current value to be validated. If a {@link ValidatedValueUnwrapper} has been set, the value will be
	 * retrieved via that handler.
	 *
	 * @return the current value to be validated
	 */
	public final Object getCurrentValidatedValue() {
		return validatedValueHandler != null ? validatedValueHandler.handleValidatedValue( currentValue ) : currentValue;
	}

	public final void setPropertyPath(PathImpl propertyPath) {
		this.propertyPath = propertyPath;
	}

	public final void appendNode(Cascadable node) {
		PathImpl newPath = PathImpl.createCopy( propertyPath );
		node.appendTo( newPath );
		propertyPath = newPath;
	}

	public final void appendNode(ConstraintLocation location) {
		PathImpl newPath = PathImpl.createCopy( propertyPath );
		location.appendTo( parameterNameProvider, newPath );
		propertyPath = newPath;
	}

	public final void markCurrentPropertyAsIterable() {
		propertyPath.makeLeafNodeIterable();
	}

	public final void setKey(Object key) {
		propertyPath.setLeafNodeMapKey( key );
	}

	public final void setIndex(Integer index) {
		propertyPath.setLeafNodeIndex( index );
	}

	public final void setCurrentGroup(Class<?> currentGroup) {
		this.currentGroup = currentGroup;
	}

	public final void setCurrentValidatedValue(V currentValue) {
		propertyPath.setLeafNodeValue( currentValue );
		this.currentValue = currentValue;
	}

	public final boolean validatingDefault() {
		return getCurrentGroup() != null && getCurrentGroup().getName().equals( Default.class.getName() );
	}

	public final ElementType getElementType() {
		return elementType;
	}

	public final void setElementType(ElementType elementType) {
		this.elementType = elementType;
	}

	/**
	 * Returns the declared (static) type of the currently validated element. If a {@link ValidatedValueUnwrapper} has
	 * been set, the type will be retrieved via that handler.
	 *
	 * @return the declared type of the currently validated element
	 */
	public final Type getDeclaredTypeOfValidatedElement() {
		return declaredTypeOfValidatedElement;
	}

	public final void setDeclaredTypeOfValidatedElement(Type declaredTypeOfValidatedElement) {
		this.declaredTypeOfValidatedElement = declaredTypeOfValidatedElement;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ValueContext" );
		sb.append( "{currentBean=" ).append( currentBean );
		sb.append( ", currentBeanType=" ).append( currentBeanType );
		sb.append( ", propertyPath=" ).append( propertyPath );
		sb.append( ", currentGroup=" ).append( currentGroup );
		sb.append( ", currentValue=" ).append( currentValue );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( ", typeOfValidatedValue=" ).append( declaredTypeOfValidatedElement );
		sb.append( '}' );
		return sb.toString();
	}

	public void setValidatedValueHandler(ValidatedValueUnwrapper<V> handler) {
		this.validatedValueHandler = handler;
	}

	public ValidatedValueUnwrapper<V> getValidatedValueHandler() {
		return validatedValueHandler;
	}

	public UnwrapMode getUnwrapMode() {
		return unwrapMode;
	}

	public void setUnwrapMode(UnwrapMode unwrapMode) {
		this.unwrapMode = unwrapMode;
	}

	public Object getValue(Object parent, ConstraintLocation location) {
		// TODO: For BVAL-214 we'd get the value from a map or another alternative structure instead
		return location.getValue( parent );
	}
}

/**
 * ObjectScriptable.java
 * (c) Radim Loskot and Radek Burget, 2013-2014
 *
 * ScriptBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ScriptBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with ScriptBox. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.jsen.javascript.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.jsen.core.reflect.ObjectField;
import com.jsen.core.reflect.ObjectFunction;

/**
 * Class that adds functionality of the defining class members into this scriptable object.
 * 
 * @author Radim Loskot
 * @version 0.9
 * @since 0.9 - 21.4.2014
 */
public class ObjectScriptable extends ScriptableObject  {
	private static final long serialVersionUID = 1531587729453175461L;
	
	protected Object object;
	protected Class<?> objectClass;
	
	/**
	 * @see ScriptableObject
	 */
	public ObjectScriptable(Object object) {
		this.object = object;
		this.objectClass = object.getClass();
	}
	
	/**
	 * @see ScriptableObject
	 */
	public ObjectScriptable(Object object, Scriptable scope, Scriptable prototype) {
		super(scope, prototype);
		
		this.object = object;
		this.objectClass = object.getClass();
	}
	
	/**
	 * Returns associated object.
	 * 
	 * @return Associated object.
	 */
	public Object getObject() {
		return object;
	}
	
	/**
	 * Returns associated object's class.
	 * 
	 * @return Associated object's class.
	 */
	public Class<?> getObjectClass() {
		return objectClass;
	}
	
	@Override
	public String getClassName() {
		return "ObjectScriptable";
	}

	@Override
	public String toString() {
		if (object == null) {
			return "[object]";
		} else {
			Class<?> clazz = object.getClass();
			String name = clazz.getSimpleName();
			return "[object " + name + "]";
		}
	}
	
	/**
	 * Defines given object field into this scope with the passed name.
	 * 
	 * @param fieldName Name of the new property.
	 * @param objectField Field to be defined into this scope.
	 */
	public void defineObjectField(String fieldName, ObjectField objectField) {
		defineObjectField(this, fieldName, objectField);
	}
	
	/**
	 * Defines given object function into this scope with the passed name.
	 * 
	 * @param fieldName Name of the new property.
	 * @param objectField Function to be defined into this scope.
	 */
	public void defineObjectFunction(String functionName, ObjectFunction objectFunction) {		
		defineObjectFunction(this, functionName, objectFunction);
	}
	
	/**
	 * Defines given object field into passed scope with the passed name.
	 * 
	 * @param fieldScopeObject Scope where to define the passed field.
	 * @param fieldName Name of the new property.
	 * @param objectField Field to be defined into passed scope.
	 */
	public static void defineObjectField(ScriptableObject fieldScopeObject, String fieldName, ObjectField objectField) {
		if (!(objectField instanceof HostedJavaField)) {
			objectField = new HostedJavaField(objectField);
		}
				
		Method fieldGetterMethod = objectField.getFieldGetterMethod();
		Method fieldSetterMethod = objectField.getFieldSetterMethod();
		Field field = objectField.getMember();
		Method wrappedFieldGetterMethod = (fieldGetterMethod == null && field == null)? null : HostedJavaField.GETTER_METHOD;
		Method wrappedFieldSetterMethod = (fieldSetterMethod == null && field == null)? null : HostedJavaField.SETTER_METHOD;
		
		int attributes = ScriptableObject.DONTENUM;
		attributes = (fieldSetterMethod == null && field == null)? attributes | ScriptableObject.READONLY : attributes;
		
		fieldScopeObject.defineProperty(fieldName, objectField, wrappedFieldGetterMethod, wrappedFieldSetterMethod, attributes);
	}
	
	/**
	 * Defines given object function into passed scope with the passed name.
	 * 
	 * @param fieldScopeObject Scope where to define the passed function.
	 * @param fieldName Name of the new property.
	 * @param objectField Function to be defined into passed scope.
	 */
	public static void defineObjectFunction(ScriptableObject functionScopeObject, String functionName, ObjectFunction objectFunction) {		
		Object function = ScriptableObject.getProperty(functionScopeObject, functionName);
		
		if (function != Scriptable.NOT_FOUND && !(function instanceof HostedJavaMethod)) {
			deleteProperty(functionScopeObject, functionName);
			function = Scriptable.NOT_FOUND;
			//throw new FunctionException("Function already exists and cannot be overloaded because function object does not extends OverloadableFunctionObject");
		}
		
		if (function == Scriptable.NOT_FOUND) {
			Object object = objectFunction.getObject();
			function = new HostedJavaMethod(functionScopeObject, object, objectFunction);
			functionScopeObject.defineProperty(functionName, function, ScriptableObject.DONTENUM | ScriptableObject.PERMANENT);
		} else {
			((HostedJavaMethod)function).attachObjectFunction(objectFunction);
		}
	}
	

	
	
}

/*
 * Copyright (c) 2012, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.engine.element;

/**
 * The interface {@code ConstructorElement} defines the behavior of elements representing a
 * constructor or a factory method defined within a type.
 * 
 * @coverage dart.engine.element
 */
public interface ConstructorElement extends ClassMemberElement, ExecutableElement {
  /**
   * Return the constructor to which this constructor is redirecting.
   * 
   * @return the constructor to which this constructor is redirecting
   */
  public ConstructorElement getRedirectedConstructor();

  /**
   * Return {@code true} if this constructor is a const constructor.
   * 
   * @return {@code true} if this constructor is a const constructor
   */
  public boolean isConst();

  /**
   * Return {@code true} if this constructor can be used as a default constructor - unnamed and has
   * no required parameters.
   * 
   * @return {@code true} if this constructor can be used as a default constructor.
   */
  public boolean isDefaultConstructor();

  /**
   * Return {@code true} if this constructor represents a factory constructor.
   * 
   * @return {@code true} if this constructor represents a factory constructor
   */
  public boolean isFactory();
}

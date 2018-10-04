package com.github.gtache.lsp.contributors.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.{PsiElement, PsiReference}
import com.intellij.util.IncorrectOperationException

/**
  * A simple PsiReference for LSP
  *
  * @param element The corresponding PsiElement
  */
case class LSPPsiReference(var element: PsiElement) extends PsiReference {
  /**
    * Returns the underlying (referencing) element of the reference.
    *
    * @return the underlying element of the reference.
    */
  def getElement: PsiElement = element

  /**
    * Returns the part of the underlying element which serves as a reference, or the complete
    * text range of the element if the entire element is a reference.
    *
    * @return Relative range in element
    */
  def getRangeInElement: TextRange = new TextRange(0, element.getTextLength)

  /**
    * Returns the element which is the target of the reference.
    *
    * @return the target element, or null if it was not possible to resolve the reference to a valid target.
    * @see PsiPolyVariantReference#multiResolve(boolean)
    */
  def resolve: PsiElement = element

  /**
    * Returns the name of the reference target element which does not depend on import statements
    * and other context (for example, the full-qualified name of the class if the reference targets
    * a Java class).
    *
    * @return the canonical text of the reference.
    */
  def getCanonicalText: String = element.getText

  /**
    * Called when the reference target element has been renamed, in order to change the reference
    * text according to the new name.
    *
    * @param newElementName the new name of the target element.
    * @return the new underlying element of the reference.
    * @throws IncorrectOperationException if the rename cannot be handled for some reason.
    */
  @throws[IncorrectOperationException]
  def handleElementRename(newElementName: String): PsiElement = element

  /**
    * Changes the reference so that it starts to point to the specified element. This is called,
    * for example, by the "Create Class from New" quickfix, to bind the (invalid) reference on
    * which the quickfix was called to the newly created class.
    *
    * @param element the element which should become the target of the reference.
    * @return the new underlying element of the reference.
    * @throws IncorrectOperationException if the rebind cannot be handled for some reason.
    */
  @throws[IncorrectOperationException]
  def bindToElement(element: PsiElement): PsiElement = {
    this.element = element
    element
  }

  /**
    * Checks if the reference targets the specified element.
    *
    * @param element the element to check target for.
    * @return true if the reference targets that element, false otherwise.
    */
  def isReferenceTo(element: PsiElement): Boolean = this.element == element

  /**
    * Returns the array of String, {@link PsiElement} and/or {@link LookupElement}
    * instances representing all identifiers that are visible at the location of the reference. The contents
    * of the returned array is used to build the lookup list for basic code completion. (The list
    * of visible identifiers may not be filtered by the completion prefix string - the
    * filtering is performed later by IDEA core.)
    *
    * @return the array of available identifiers.
    */
  @SuppressWarnings(Array("JavadocReference")) def getVariants: Array[AnyRef] = Array()

  /**
    * Returns false if the underlying element is guaranteed to be a reference, or true
    * if the underlying element is a possible reference which should not be reported as
    * an error if it fails to resolve. For example, a text in an XML file which looks
    * like a full-qualified Java class name is a soft reference.
    *
    * @return true if the reference is soft, false otherwise.
    */
  def isSoft: Boolean = false
}

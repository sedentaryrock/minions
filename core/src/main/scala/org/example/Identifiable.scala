package org.example

trait Identifiable {
  def name: String
  def kind: String = this.getClass.getCanonicalName
}

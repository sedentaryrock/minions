package org.example

trait Task[Result] extends Identifiable {
  def execute: Result
}

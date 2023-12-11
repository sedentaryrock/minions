package org.example

trait Task[Result] {
  def execute: Result
}

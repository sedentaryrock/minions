package org.example.minion.tasks

import org.example.Identifiable

trait Task[Result] extends Identifiable {
  def execute: Result
}

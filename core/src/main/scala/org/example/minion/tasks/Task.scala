package org.example.minion.tasks

import org.example.Identifiable

trait Task[RESULT] extends Identifiable {
  def execute: RESULT
}

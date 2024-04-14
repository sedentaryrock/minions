All the bellow points are for Message's status.
* [X] Introduce framework status other than the message status, why?
  * Ability to recurse on a topic over and over - Done.

* [X] Think about message status thoroughly
  * Question usefulness of the status

* [X] Introduce transitioning a message without any message-status changes, why?
  * Whether the message is picked or not is a framework worry not the consumer worry.

Do Tasks need a builder or elaborate factories as currently implemented? why do I need to answer this?
* [ ] Tasks always have task trait, can we make use of it?
* [ ] Do I really need task class?

Now about recruiting a Minion.
* [ ] Find a better term for the operation
  * Current options are
    * Recruit
    * Employ
    * AssignAssign
* What is expected?
  * [ ] Minion needs to know
    * What topic to address? (topic, status)
    * How to address it? (task-details)
    * What to do with the topic once addressed? (end-status)
  * [ ] Task configuration
    * There are various kinds of tasks each with varied construction
    * Task and it's construction needs to be frozen to db and thawed when minion is recruited

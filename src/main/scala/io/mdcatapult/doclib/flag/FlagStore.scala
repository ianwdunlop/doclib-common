package io.mdcatapult.doclib.flag

/**
  * Constructs FlagContext for messages paying attention and encapsulating the consumer defaults.
  */
trait FlagStore {

  /**
    * Get the FlagContext for a given key and providing a default if a key isn't provided.
    * @param key override flag key or None for the consumer default
    * @return context
    */
  def findFlagContext(key: Option[String] = None): FlagContext
}

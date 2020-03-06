package part1_recap

import akka.actor.{Actor, ActorSystem, Props, Stash}

object AkkaRecap extends App {
  class SimpleActor extends Actor with Stash{
    override def receive: Receive = {
      case "createChild" =>
        val childActor = context.actorOf(Props)
      case "stashThis" =>
        stash()
      case "change handler NOW" =>
        unstashAll()
      case "change" => context.become(anotherHandler)
      case message => println(s"I received: $message")
    }

    def anotherHandler: Receive = {
      case message => println(s"In another receive handler: $message")
    }
  }

  // actor incapsulation
  val system = ActorSystem("AkkaRecap")
  // actor instantiation
  val actor = system.actorOf(Props[SimpleActor], "simpleActor")

  // #2: sending a message
  actor ! "hello"

  /*
    - messages are sent asynchronously
    - many actors (in the millions) can share a few dozen threads
    - each message is processed atomically
    - no need for locks
   */

  // changing actor behaviour + stashing

  // actors can spawn other actors
}

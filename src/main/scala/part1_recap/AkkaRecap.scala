package part1_recap

import akka.actor.{Actor, ActorSystem, Props}

object AkkaRecap extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => println(s"I received: $message")
    }
  }

  // actor incapsulation
  val system = ActorSystem("AkkaRecap")
  // actor instantiation
  val actor = system.actorOf(Props[SimpleActor], "simpleActor")
}

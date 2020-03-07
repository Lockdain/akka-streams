package part1_recap

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, PoisonPill, Props, Stash, SupervisorStrategy}
import akka.stream.Supervision.Stop
import akka.util.Timeout

object AkkaRecap extends App {
  class SimpleActor extends Actor with Stash with ActorLogging {
    override def receive: Receive = {
      case "createChild" =>
//        val childActor = context.actorOf(Props)
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

    override def preStart(): Unit = {
      log.info("I'm starting")
    }

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: RuntimeException => Restart
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
  // guardians: /system, /user, /root guardian

  // actors have a defined lifecycle: started, stop, suspended, resume, restarted
  // stopping actors - context.stop
  actor ! PoisonPill

  // logging

  // supervision

  // configure Akka infrastructure: dispatchers, routers, mailboxes

  // schedulers
  import scala.concurrent.duration._
  import system.dispatcher

  system.scheduler.scheduleOnce(2 seconds) {
    actor ! "delayed happy birthday!"
  }

  // Akka patterns
  // FSM + ask pattern
  import akka.pattern.ask
  implicit val timeout = Timeout(3 seconds)

  val future = actor ? "Question"

  // the pipe pattern
  import akka.pattern.pipe
  val anotherActor = system.actorOf(Props[SimpleActor], "anotherSimpleActor")
  future.mapTo[String].pipeTo(anotherActor)
}

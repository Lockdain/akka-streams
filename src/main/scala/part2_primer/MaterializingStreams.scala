package part2_primer

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MaterializingStreams extends App {
  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = ActorMaterializer()

  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))
  private val simpleMaterializedValue: NotUsed = simpleGraph.run()

  val source = Source(1 to 10)
  val sink = Sink.reduce[Int]((a, b) => a + b)

  import system.dispatcher
  private val sumFuture: Future[Int] = source.runWith(sink) // materialized value
  sumFuture.onComplete {
    case Success(value) => println(s"The sum is: $value")
    case Failure(exception) => println(s"An error occured: $exception")
  }

  // Each component may return ONLY ONE materialized value!

}

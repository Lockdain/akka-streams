package part2_primer

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MaterializingStreams extends App {
  implicit val system: ActorSystem = ActorSystem("MaterializingStreams")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

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
  // By default the most left materialized value is kept

  // How to choose a materialized value
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simpleSink = Sink.foreach[Int](println)
  // viaMat is able to compose materialized values from  previous components to a new one
//  simpleSource.viaMat(simpleFlow) ((sourceMat, flowMat) => flowMat) // the long way
  private val value: RunnableGraph[Future[Done]] = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
  value // syntactic sugar with additional sink

}

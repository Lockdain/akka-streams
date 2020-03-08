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
//  private val sumFuture: Future[Int] = source.runWith(sink) // materialized value
//  sumFuture.onComplete {
//    case Success(value) => println(s"The sum is: $value")
//    case Failure(exception) => println(s"An error occured: $exception")
//  }

  // Each component may return ONLY ONE materialized value!
  // By default the most left materialized value is kept

  // How to choose a materialized value
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simpleSink = Sink.foreach[Int](println)
  // viaMat is able to compose materialized values from  previous components to a new one
//  simpleSource.viaMat(simpleFlow) ((sourceMat, flowMat) => flowMat) // the long way
//  private val graph: RunnableGraph[Future[Done]] = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right) // syntactic sugar with additional sink
//  private val futureDone: Future[Done] = graph.run()
//  futureDone.onComplete {
//    case Success(value) => println(s"The value is: $value")
//    case Failure(exception) => println(s"Error: $exception")
//  }

  // syntactic sugars
  private val eventualInt: Future[Int] = Source(1 to 10).runWith(Sink.reduce[Int](_ + _))
  eventualInt // the same as source.toSink(reduce)(Keep.right)
  // or
  Source(1 to 10).runReduce[Int](_ + _) // doing exactly the same thing

  // running components backwards
  Sink.foreach(println).runWith(Source.single(55)) // the same as source().to(sink()).run()

  // running in both ways
//  Flow[Int].map(x => x * 2).runWith(simpleSource, simpleSink)

  /**
   * Test task.
   * - return last element of a source (Sink.last)
   * - compute the total word count out of a stream of sentences
   * - use map, fold, reduce
   */

  // take the last element
  val testSource = Source[Int](1 to 5)
//  private val testGraph: RunnableGraph[Future[Int]] = testSource.toMat(Sink.last)(Keep.right)
//  private val futureInt: Future[Int] = testGraph.run()
//  futureInt.onComplete {
//    case Success(value) => println(s"The last element is: $value")
//    case Failure(exception) => println(s"Exception: $exception")
//  }

  private val sentence = List(
    "There are many many words words in in this sentence",
    "Hi there",
    "I like Akka Streams"
  )

  val wordCountSink = Sink.fold[Int, String](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)

  private val future: Future[Int] = Source(sentence).toMat(wordCountSink)(Keep.right).run()
  future.onComplete {
    case Success(event) => println(s"Total number of words is: $event")
    case Failure(exception) => println(s"Exception occured: $exception")
  }

}

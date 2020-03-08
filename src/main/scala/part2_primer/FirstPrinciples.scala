package part2_primer

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}

import scala.concurrent.Future

object FirstPrinciples extends App {
  implicit val system = ActorSystem("FirstPrinciples")
  implicit val materializer = ActorMaterializer()

  // source
  val source = Source(1 to 10)

  // sink
  val sink = Sink.foreach[Int](println)

  // a graph
  val graph: RunnableGraph[NotUsed] = source.to(sink)
//  graph.run() // prints 1 to 10

  // flows - transform elements
  val flow = Flow[Int].map(x => x + 1)
  val sourceWithFlow = source.via(flow)
//  val flowWithSink = flow.to(sink)

  // combine them
//  sourceWithFlow.to(sink).run() // prints 2 to 11
//  source.to(flowWithSink).run() // prints 2 to 11
//  source.via(flow).to(sink).run() // prints 2 to 11

  // NULLs are not allowed
//  val illegalSource = Source.single[String](null)
//  illegalSource.to(Sink.foreach(println)).run() // will not run: Element must not be null, rule 2.13
  // Use options instead

  // kinds of sources
  val finiteSource = Source.single(1)
  val anotherfiniteSource = Source(List(1, 2, 3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(Stream.from(1))

  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.fromFuture(Future(42))

  // sinks
  val doNothingSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int] // retrieves head and closes the stream
  val foldSink = Sink.fold[Int, Int](0)((a, b) => a + b)

  // fold sink
  val foldSource = Source[Int](1 to 10)
  val foldTestSink = Sink.fold[Int, Int](0)(_ + _)
//  foldSource.to(foldTestSink).run()

  // flows
  val mapFlow = Flow[Int].map(x => 2 * x)
  val takeFlow = Flow[Int].take(5) // only take the first five elements
  // drop, filter
  // doesn't have flatMap

  // source - flow - flow - sink
  val doubleFlowGraph = source.via(mapFlow).via(takeFlow).to(sink)
  doubleFlowGraph.run()

  // sugar
  val mapSource = Source(1 to 10).map(x => 2 * x)
  // run streams directly
//  mapSource.runForeach(println)

  // operators = components
  /**
   * Create a stream that takes the names of persons, then keep the first two names
   * with length > 5 chars
   */
  val names = List("Alex", "John", "Daniel", "Jeremy", "Jonathan", "Matt")
  val namesSource = Source[String](names)
  val filterFlow = Flow[String].filter(name => name.length > 5)
  val takeTwoFlow = Flow[String].take(2)
  // compose the graph
  namesSource.via(filterFlow).via(takeTwoFlow).runForeach(println)
}

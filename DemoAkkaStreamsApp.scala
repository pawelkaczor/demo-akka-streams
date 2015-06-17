/*
 * Copyright 2015 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import akka.stream.{ ActorFlowMaterializer, FlowMaterializer }
import scala.concurrent.Future

object DemoAkkaStreamsApp {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("demo-akka-streams")
    implicit val mat = ActorFlowMaterializer()
    import system.dispatcher

    // Sequencing the demos which would run in parallel otherwise!
    for {
      _ <- demo1()
      d2 <- demo2()
      d3 <- demo3()
    } {
      println(s"Demo 2: $d2")
      println(s"Demo 3: $d3")
      system.shutdown()
      system.awaitTermination()
    }
  }

  def demo1()(implicit mat: FlowMaterializer): Future[Unit] = {
    //val printNumbers = Source(1.to(7)).to(Sink.foreach(println))
    val printNumbers = Source(1.to(7)).toMat(Sink.foreach(println))(Keep.right)
    println("Demo 1:")
    printNumbers.run()
  }

  def demo2()(implicit mat: FlowMaterializer): Future[Int] =
    Source(1.to(7)).runWith(Sink.fold(0)(_ + _))

  def demo3()(implicit mat: FlowMaterializer): Future[Int] = {
    val doubler = Flow[Int].map(_ * 2)
    Source(() => Iterator.from(1))
      .via(doubler)
      .take(7)
      .map(_ + 1)
      .filter(_ % 3 == 0)
      .runWith(Sink.fold(0)(_ + _))
  }
}

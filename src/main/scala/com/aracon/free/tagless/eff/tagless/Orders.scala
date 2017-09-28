/*
 * Copyright 2017 Pere Villega
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

package com.aracon.free.tagless.eff.tagless

import com.aracon.free.tagless.eff._
import cats._
import cats.implicits._

import scala.language.higherKinds

object Orders {

  trait OrdersT[M[_]] {
    def buy(stock: Symbol, amount: Int): M[Response]
    def sell(stock: Symbol, amount: Int): M[Response]
    def listStocks(): M[List[Symbol]]
  }

  def orderInterpreter = new OrdersT[ErrorOr] {
    override def buy(stock: Symbol, amount: Int): ErrorOr[Response] = "ok".asRight[String]

    override def sell(stock: Symbol, amount: Int): ErrorOr[Response] = "ok".asRight[String]

    override def listStocks(): ErrorOr[List[Symbol]] = List("FB", "TWTR").asRight[String]
  }
}

object Logger {

  trait LoggerT[M[_]] {
    def info(msg: String): M[Unit]
    def error(msg: String): M[Unit]
  }

  def logInterpreter = new LoggerT[ErrorOr] {
    override def info(msg: String): ErrorOr[Unit] = println(s"[Info] - $msg").asRight[String]

    override def error(msg: String): ErrorOr[Unit] = println(s"[Error] - $msg").asRight[String]
  }
}

object Program {
  import Logger._
  import Orders._

  def program[M[_]: Monad](orders: OrdersT[M], logger: LoggerT[M]): M[Response] = {
    import orders._
    import logger._

    for {
      _         <- info("Trading on shares")
      allStocks <- listStocks()
      _         <- allStocks.traverse(buy(_, 100))
      rsp       <- sell("TWTR", 500)
      _         <- info(s"Trading ended with result: $rsp")
    } yield rsp
  }

  program(orderInterpreter, logInterpreter)
}

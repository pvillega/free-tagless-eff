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

package com.aracon.free.tagless.eff.free

import com.aracon.free.tagless.eff._
import cats._
import cats.data.EitherK
import cats.free._
import cats.free.Free._
import cats.implicits._

object Orders {

  sealed trait Orders[A]

  case class Buy(stock: Symbol, amount: Int) extends Orders[Response]

  case class Sell(stock: Symbol, amount: Int) extends Orders[Response]

  case class ListStocks() extends Orders[List[Symbol]]

  type OrdersF[A] = Free[Orders, A]

  def buy(stock: Symbol, amount: Int): OrdersF[Response] =
    liftF[Orders, Response](Buy(stock, amount))

  def sell(stock: Symbol, amount: Int): OrdersF[Response] =
    liftF[Orders, Response](Sell(stock, amount))

  def listStocks(): OrdersF[List[Symbol]] =
    liftF[Orders, List[Symbol]](ListStocks())

  class OrderI[F[_]](implicit I: InjectK[Orders, F]) {
    def buyI(stock: Symbol, amount: Int): Free[F, Response] =
      Free.inject[Orders, F](Buy(stock, amount))

    def sellI(stock: Symbol, amount: Int): Free[F, Response] =
      Free.inject[Orders, F](Sell(stock, amount))

    def listStocksI(): Free[F, List[Symbol]] = Free.inject[Orders, F](ListStocks())
  }

  implicit def orderI[F[_]](implicit I: InjectK[Orders, F]): OrderI[F] = new OrderI[F]

  def orderInterpreter: Orders ~> ErrorOr =
    new (Orders ~> ErrorOr) {
      def apply[A](fa: Orders[A]): ErrorOr[A] = fa match {
        case ListStocks() =>
          List("FB", "TWTR").asRight[String]
        case Buy(stock, amount) =>
          Right("ok")
        case Sell(stock, amount) =>
          Right("ok")
      }
    }
}

object Logger {
  sealed trait Log[A]

  case class Info(msg: String) extends Log[Unit]

  case class Error(msg: String) extends Log[Unit]

  type LogF[A] = Free[Log, A]

  def info(msg: String): LogF[Unit] =
    liftF[Log, Unit](Info(msg))

  def error(msg: String): LogF[Unit] =
    liftF[Log, Unit](Error(msg))

  class LogI[F[_]](implicit I: InjectK[Log, F]) {
    def infoI(msg: String): Free[F, Unit] = Free.inject[Log, F](Info(msg))

    def errorI(msg: String): Free[F, Unit] = Free.inject[Log, F](Error(msg))
  }

  implicit def logI[F[_]](implicit I: InjectK[Log, F]): LogI[F] = new LogI[F]

  def logInterpreter: Log ~> ErrorOr =
    new (Log ~> ErrorOr) {
      def apply[A](fa: Log[A]): ErrorOr[A] = fa match {
        case Info(msg)  => println(s"[Info] - $msg").asRight[String]
        case Error(msg) => println(s"[Error] - $msg").asRight[String]
      }
    }
}

object Program {
  import Logger._
  import Orders._

  type TradeApp[A] = EitherK[Orders, Log, A]

  def composedInterpreter: TradeApp ~> ErrorOr = orderInterpreter or logInterpreter

  def program(implicit O: OrderI[TradeApp], L: LogI[TradeApp]): Free[TradeApp, Response] = {
    import L._
    import O._

    for {
      _         <- infoI("Trading on shares")
      allStocks <- listStocksI()
      _         <- allStocks.traverse(buyI(_, 100))
      rsp       <- sellI("TWTR", 500)
      _         <- infoI(s"Trading ended")
    } yield rsp
  }

  program.foldMap(composedInterpreter)
}

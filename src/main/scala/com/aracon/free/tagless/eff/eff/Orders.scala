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

package com.aracon.free.tagless.eff.eff

import com.aracon.free.tagless.eff._
import cats._
import cats.implicits._
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.interpret._
import org.atnos.eff.syntax.all._

import scala.language.higherKinds

object Orders {

  sealed trait Orders[A]

  case class Buy(stock: Symbol, amount: Int) extends Orders[Response]

  case class Sell(stock: Symbol, amount: Int) extends Orders[Response]

  case class ListStocks() extends Orders[List[Symbol]]

  type _orders[R] = Orders |= R

  def buy[T, R: _orders](stock: Symbol, amount: Int): Eff[R, Response] =
    Eff.send[Orders, R, Response](Buy(stock, amount))

  def sell[T, R: _orders](stock: Symbol, amount: Int): Eff[R, Response] =
    Eff.send[Orders, R, Response](Sell(stock, amount))

  def listStocks[T, R: _orders](): Eff[R, List[Symbol]] =
    Eff.send[Orders, R, List[Symbol]](ListStocks())

  def runOrders[R, A](effect: Eff[R, A])(implicit m: Orders <= R): Eff[m.Out, A] =
    recurse(effect)(new Recurser[Orders, m.Out, A, A] {
      def onPure(a: A): A = a

      def onEffect[X](i: Orders[X]): X Either Eff[m.Out, A] = Left {
        i match {
          case ListStocks() =>
            List("FB", "TWTR")
          case Buy(stock, amount) =>
            "ok"
          case Sell(stock, amount) =>
            "ok"
        }
      }

      def onApplicative[X, T[_]: Traverse](ms: T[Orders[X]]): T[X] Either Orders[T[X]] =
        Left(ms.map {
          case ListStocks() =>
            List("FB", "TWTR")
          case Buy(stock, amount) =>
            "ok"
          case Sell(stock, amount) =>
            "ok"
        })

    })(m)
}

object Logger {

  sealed trait Log[A]

  case class Info(msg: String) extends Log[Unit]

  case class Error(msg: String) extends Log[Unit]

  type _logs[R] = Log |= R

  def info[T, R: _logs](msg: String): Eff[R, Unit] =
    Eff.send[Log, R, Unit](Info(msg))

  def error[T, R: _logs](msg: String): Eff[R, Unit] =
    Eff.send[Log, R, Unit](Error(msg))

  def runLogs[R, A](effect: Eff[R, A])(implicit m: Log <= R): Eff[m.Out, A] =
    recurse(effect)(new Recurser[Log, m.Out, A, A] {
      def onPure(a: A): A = a

      def onEffect[X](i: Log[X]): X Either Eff[m.Out, A] = Left {
        i match {
          case Info(msg)  => println(s"[Info] - $msg")
          case Error(msg) => println(s"[Error] - $msg")
        }
      }

      def onApplicative[X, T[_]: Traverse](ms: T[Log[X]]): T[X] Either Log[T[X]] =
        Left(ms.map {
          case Info(msg)  => println(s"[Info] - $msg")
          case Error(msg) => println(s"[Error] - $msg")
        })

    })(m)
}

object Program {
  import Logger._
  import Orders._

  def program[R: _orders: _logs]: Eff[R, Response] =
    for {
      _         <- info("Trading on shares")
      allStocks <- listStocks()
      _         <- allStocks.traverse(buy(_, 100))
      rsp       <- sell("TWTR", 500)
      _         <- info(s"Trading ended with result: $rsp")
    } yield rsp

  type Stack = Fx.fx2[Orders, Log]

  runOrders(runLogs(program[Stack]))
}

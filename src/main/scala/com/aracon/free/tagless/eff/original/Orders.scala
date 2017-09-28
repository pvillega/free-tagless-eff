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

package com.aracon.free.tagless.eff.original

import com.aracon.free.tagless.eff._
import cats._
import cats.implicits._

object Orders {
  def buy(stock: Symbol, amount: Int): ErrorOr[Response]  = Right(s"Bought $amount of $stock shares")
  def sell(stock: Symbol, amount: Int): ErrorOr[Response] = Right(s"Sold $amount of $stock shares")
  def listStocks(): ErrorOr[List[Symbol]]                 = Right(List("APPL", "GOOG", "FB"))
}

object Logger {
  def info(msg: String): Unit  = println(s"[INFO] $msg")
  def error(msg: String): Unit = println(s"[ERROR] $msg")
}

object Program {
  import Logger._
  import Orders._

  def program(): Unit = {
    info("Trading on shares")

    val result: Either[String, Response] = for {
      allStocks <- listStocks()
      _         <- allStocks.traverse(buy(_, 100))
      rsp       <- sell("TWTR", 500)
    } yield rsp

    info(s"Trading ended with result: $result")
  }

}

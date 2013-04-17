package com.stackmob.customcode.localrunner.sdk

import scala.util.Random
import com.twitter.concurrent._
import com.twitter.util.{Await, Future, Time, Duration}
import scalaz.concurrent.{Promise, Strategy}
import java.util.concurrent.Executors

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk
 *
 * User: aaron
 * Date: 4/15/13
 * Time: 5:16 PM
 */

class Frequency(val number: Int, val every: Duration)

class ErrorSimulator(freq: Frequency) {
  private var count = 0
  private var lastRollover = Time.fromMilliseconds(0)
  private val lock = new Object
  private val rand = new Random(System.currentTimeMillis())

  def simulate[T](err: Throwable)(op: => T): T = {
    lock.synchronized {
      if(lastRollover + freq.every <= Time.now) {
        //if a rollover happened, reset stuff
        lastRollover = Time.now
        count = 0
        op
      } else if(count >= freq.number) {
        //if the counter is at max, run the op normally
        count += 1
        op
      } else {
        //if the counter is not at max, randomly decide if there's an error
        val shouldErr = rand.nextBoolean()
        if(shouldErr) {
          count += 1
          throw err
        } else {
          op
        }
      }
    }
  }
}

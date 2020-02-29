package com.diegomfv.moodtrackerv2

import io.reactivex.Observable
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MulticastingTest {

    val rxRange = Observable.range(1, 5)
    val rxRangeToRandom = rxRange.map { randomInt() }

    @Test
    fun connectableObserver() {

        rxRangeToRandom.testSubscribeNoDelay()
        rxRangeToRandom.testSubscribeNoDelay()

        separator()

        //ERROR: Publish is called too early
        val c1 = rxRange.publish()
        c1.map { randomInt() }.testSubscribeNoDelay()
        c1.map { randomInt() }.testSubscribeNoDelay()
        sleep()
        c1.connect()

        separator()

        //Proper MULTICASTING
        val c2 = rxRangeToRandom.publish()
        c2.testSubscribeNoDelay()
        c2.testSubscribeNoDelay()
        sleep()
        c2.connect()

    }


    @Test
    fun autoconnect () {
        //0: fires immediately
        //default: 1

        val ac = Observable.interval(1, TimeUnit.SECONDS).publish().autoConnect(2)

        ac.testSubscribeNoDelay()
        println("No emissions yet")
        sleep()
        ac.testSubscribeNoDelay()
        sleep()
        ac.testSubscribeNoDelay() //lost the firts emissions due to autoconnect (2)
        sleep()


    }


    @Test
    fun refCount_share () {
        /** Disposes and autoSubscribes when new subscribers
         * But take care, it STARTS AGAIN!!
         * */
        //.refCount() almost equal to autoconnect(1)
        //.shared() == publish().refCount() [WITH DEFAULT VALUE]

        val rxRefCount = Observable.interval(500, TimeUnit.MILLISECONDS).publish().refCount(1).doOnDispose { println("Disposed") }
        val rxShare = Observable.interval(500, TimeUnit.MILLISECONDS).share().doOnDispose { println("Disposed") }

        val d1 = rxRefCount.testSubscribeNoDelay()
        sleep()
        d1.dispose()
        sleep()

        val d2 = rxRefCount.testSubscribeNoDelay()
        sleep()
        d2.dispose()
        sleep()

        val d3 = rxRefCount.testSubscribeNoDelay() //lost the firts emissions due to autoconnect (2)
        sleep()
        d3.dispose()
        sleep()

    }

    @Test
    fun replay () {

    }

    @Test
    fun caching () {

    }
}

fun randomInt () : Int {
    return Random.nextInt(1, 10000)
}
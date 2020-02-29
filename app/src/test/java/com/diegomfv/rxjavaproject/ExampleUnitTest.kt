package com.diegomfv.moodtrackerv2

import io.reactivex.Maybe
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import io.reactivex.functions.Function3
import io.reactivex.observables.ConnectableObservable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.ResourceObserver
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleUnitTest {

    val rxStrings = Observable.just(
        "Alpha",
        "Beta",
        "Charlie",
        "Delta",
        "Echo",
        "Foxtrot",
        "Golf",
        "Hotel",
        "India",
        "Juliet"
    )

    @Test
    fun grouping() {

//        rxStrings.groupBy { it.length }
//            .flatMapSingle { group -> group.toList() }
////            .flatMap { it.toList().toObservable() }
//            .testSubscribe()
//
//
//        rxStrings.groupBy { it.length }
//            .flatMapSingle { group ->
//                group.reduce("",
//                    { acc, n -> if (t1 == "") t2 else t1 }).map { s -> group.key }
//            }
//
//
//
//        Observable.fromIterable(0..99)
//            .reduce("") { acc, n -> "$acc${if (acc == "") "" else "-"}$n" }
//            .singleTestSubscribe()


        rxStrings.groupBy { it.length }
            .doOnNext { println(it.key) }
            .flatMapSingle { group ->
                group.reduce("") { acc, n ->
                    "$acc${if(acc == "")"" else "-"}$n"
                }.map { s -> "${group.key}: $s" }
            }
            .testSubscribe()

    }


    @Test
    fun withLatestFrom() {

        val int1 = Observable.interval(2, TimeUnit.SECONDS).map { "First: $it" }
        val int2 = Observable.interval(500, TimeUnit.MILLISECONDS).map { "Second: $it" }

        int1.withLatestFrom(int2, BiFunction { t1: String, t2: String -> t1.plus(" ").plus(t2) })
            .testSubscribe(10000)

//        onNext First: 0 Second: 3
//        onNext First: 1 Second: 6
//        onNext First: 2 Second: 10
//        onNext First: 3 Second: 14
//        onNext First: 4 Second: 18

    }

    //TODO Buffering
    @Test
    fun combineLatest() {
        //Useful for UI purposes
        //First emissions from an observable before the first emission from the other are discarded

        val int1 = Observable.interval(2, TimeUnit.SECONDS).map { "First: $it" }
        val int2 = Observable.interval(500, TimeUnit.MILLISECONDS).map { "Second: $it" }

        Observable.combineLatest(
            int1,
            int2,
            BiFunction { t1: String, t2: String -> t1.plus(" ").plus(t2) })
            .testSubscribe(10000)

//        onNext First: 0 Second: 2
//        onNext First: 0 Second: 3
//        onNext First: 0 Second: 4
//        onNext First: 0 Second: 5
//        onNext First: 0 Second: 6
//        onNext First: 0 Second: 7
//        onNext First: 1 Second: 7
//        onNext First: 1 Second: 8
//        ...

    }


    //TODO Delay error
    @Test
    fun zip() {
        //sources that emit less items might never complete

//        val rxString1 = Observable.just("One")
//        val rxInt1 = Observable.just(2)
//        val rxString2 = Observable.just("Three")
//
//        Observable.zip(listOf(rxString1, rxInt1, rxString2)) { a -> a }
//            .flatMapIterable { it.toList() }
//            .filter { it::class.java == String::class.java }
//            .testSubscribe()
//
//
//        Observable.zip(rxString1,rxInt1,rxString2, Function3 { a : String, b : Int, c : String  -> a.plus(b).plus(c) })
//            .testSubscribe()


        //Docs
        /** The operator subscribes to its sources in order they are specified and completes eagerly if
         * one of the sources is shorter than the rest while disposing the other sources. Therefore, it
         * is possible those other sources will never be able to run to completion (and thus not calling
         * {@code doOnComplete()}). This can also happen if the sources are exactly the same length; if
         * source A completes and B has been consumed and is about to complete, the operator detects A won't
         * be sending further values and it will dispose B immediately. For example:
         * <pre><code>zip(range(1, 5).doOnComplete(action1), range(6, 5).doOnComplete(action2), (a, b) -&gt; a + b)</code></pre>
         * {@code action1} will be called but {@code action2} won't.
         * <br>To work around this termination property,
         * use {@link #doOnDispose(Action)} as well or use {@code using()} to do cleanup in case of completion
         * or a dispose() call.
         */

        val rxIterable1 = Observable.fromIterable(0..10).doOnComplete { println("1 finished") }
        val rxIterable2 = Observable.fromIterable(0..20).doOnComplete { println("2 finished") }
            .doOnDispose { println("2 from onDispose()") } //never completes
//
//        Observable.zip(rxIterable1, rxIterable2, BiFunction<Int,Int,Int> { t1, t2 -> t1 + t2 })
//            .testSubscribe()


        /**
         * From Learning RxJava by Thomas Nield
         * Use Observable.zipIterable() to pass a Boolean delayError argument to delay errors until
         * all sources terminate and an int bufferSize to hint an expected number of elements from
         * each source for queue size optimization. You may specify the latter to increase performance
         * in certain scenarios by buffering emissions before they are zipped.
         * */

        //TODO
//        val rxIterable1WithError = rxIterable1.flatMap { if (it == 2) Observable.error(Throwable(" Error")) else Observable.just(it) }
//        Observable.zip(rxIterable1WithError, rxIterable2, BiFunction { t1 : Int, t2 : Int -> t1 + t2 }, true, 10)
//            .testSubscribe()


        /* Delaying with zipping. Take care, it queues the emissions!!!!!
        *  */

        val rxIter = Observable.fromIterable(0..100000)

        Observable.zip(
            rxIter,
            Observable.interval(500, TimeUnit.MILLISECONDS),
            BiFunction<Int, Long, Long> { t1, t2 -> t2 })
            .testSubscribe()

    }

    @Test
    fun amb() {
        //ambiguous operator

        val rx = Observable.just(0).delay { rxTimer(1000) }
        val rx2 = Observable.just(1).delay { rxTimer(2000) }

        Observable.amb(listOf(rx, rx2))
            .testSubscribe()

        separator()

        rx.ambWith(rx2)
            .testSubscribe()

        Observable.ambArray(rx, rx2)
            .testSubscribe()

    }


    //TODO
    @Test
    fun concat() {
        //concatMapEagerly()
        //take care with infinite emissions

        //Exercise
        //1. First observable should be interval.
        //2. Stop it and let the other observables carry on.

        var counter = 0
        fun getCondition() = counter > 5

        val rxInterval = Observable.interval(500, TimeUnit.MILLISECONDS).map { "interval: $it" }
        val rxSuccess = Observable.just("Success")

        rxInterval
            .flatMap { if (getCondition()) rxSuccess else Observable.just(it) }
            .takeWhile { !getCondition() }
            .doOnNext { counter++ }
            .testSubscribe(5000)

    }

    @Test
    fun flatMap_interval() {

        val rx = Observable.fromIterable(1L..5L)

        rx.flatMap { i ->
            Observable.interval(i, TimeUnit.SECONDS)
                .map { i2 ->
                    "observer: $i // interval: $i2"
                }
        }
            .testSubscribe(10000)

    }


    @Test
    fun flatMap() {
        //map one emission to many emissions

        val rxString = Observable.just("Alpha1/Alpha2/Alpha3")
        val rxString2 = Observable.fromIterable(
            listOf(
                "Beta1/Beta2/Beta3",
                "Charlie1/Charlie2/Charlie3",
                "Delta1/Delta2/Delta3"
            )
        )
        val rxString3 = Observable.merge(rxString2, rxString)

        rxString.flatMap { string -> Observable.fromIterable(string.split("/")) }
            .testSubscribe()

        rxString
            .flatMapIterable { it.split("/") }
            .testSubscribe()


        //3 emissions to 9 emissions
        rxString2
            .flatMapIterable { it.split("/") }
            .testSubscribe()


        rxString3
            .flatMapIterable { it.split("/") }
            .testSubscribe()


        //////

        val rxString4 = Observable.just("Alpha", "Beta", "Charlie", "Delta")

        rxString4.flatMap(
            { s -> Observable.fromIterable(s.split("")) },
            { original, new -> original.plus('-').plus(new) })
            .filter { !it.endsWith('-') }
            .testSubscribe()

    }

    @Test
    fun merge() {
        //merge is useful with potentially infinite emissions like when using Observable.interval(...)

        //TODO

        /*
        * The Observable.merge() factory and the mergeWith() operator will subscribe to all the specified
        * sources simultaneously, but will likely fire the emissions in order if they are cold and on the SAME THREAD.
        * */

        val rx1 = Observable.just(1)
        val rx2 = Observable.fromIterable(10..15)
        val rx3 = Observable.just(0)

        val rxMerge = Observable.merge(rx1, rx2, rx3)
        rxMerge.testSubscribe()


        separator()

        val rxMerge2 = Observable.merge(rx1, rx2.delay { rxTimer() }, rx3) //order not respected
        rxMerge2.testSubscribe(4000)


//        Observable.mergeArray(rx1,rx2,rx3)

    }

    //TODO
    @Test
    fun disposableObserver_resourceObserver() {
        //DisposableObserver AND ResourceObserver require to call dispose manually to free the resources.

        val rxObservable = Observable.fromIterable(listOf(1, 2, 0))

        val d = rxObservable
            .doOnSubscribe { println("doOnSubscribe() called") }
            .doOnDispose { println("doOnDisposed() called") } //not called if disposition is not called manually
            .doFinally { println("doFinally() called") }
            .subscribeWith(object : DisposableObserver<Int>() {
                override fun onComplete() {}
                override fun onNext(t: Int) {
                    dispose()
                } //if we do not call dispose, d3 will remain not disposed!!!!

                override fun onError(e: Throwable) {}
            })

        sleep()

        println("isDisposed: ${d.isDisposed}") //onError automatically disposes
        d.dispose() //no effect on .doOnDispose()


        val r = rxObservable
            .doOnSubscribe { println("doOnSubscribe() called") }
            .doOnDispose { println("doOnDisposed() called") } //not called if disposition is not called manually
            .doFinally { println("doFinally() called") }
            .subscribeWith(object : ResourceObserver<Int>() {
                override fun onComplete() {}
                override fun onNext(t: Int) {
                    dispose()
                } //if we do not call dispose, d3 will remain not disposed!!!!

                override fun onError(e: Throwable) {}
            })

        sleep()

        println("isDisposed: ${r.isDisposed}") //onError automatically disposes
        r.dispose() //no effect on .doOnDispose()

        /* Note that doOnDispose() can fire multiple times for redundant disposal requests or not
        * at all if it is not disposed of in some form or another. Another option is to use the doFinally()
        * operator, which will fire after
        * either onComplete() or onError() is called or disposed of by the downstream.*/

    }


    //TODO
    @Test
    fun doOn() {

        val rxObservable = Observable.fromIterable(listOf(1, 2, 0))

        val d = rxObservable
            .doOnSubscribe { println("doOnSubscribe() called") }
            .map { 100 / it }
            .retry(1) //the position is critical
            .doOnDispose { println("doOnDisposed() called") } //not called if disposition is not called manually
            .doFinally { println("doFinally() called") }
            .testSubscribe()

        println("isDisposed: ${d.isDisposed}") //onError automatically disposes
        d.dispose() //no effect on .doOnDispose()


        separator()

        val d2 = rxObservable
            .doOnSubscribe { println("doOnSubscribe() called") }
            .doOnDispose { println("doOnDisposed() called") } //not called if disposition is not called manually
            .doFinally { println("doFinally() called") }
            .testSubscribe()

        println("isDisposed: ${d2.isDisposed}") //onError automatically disposes
        d2.dispose() //no effect on .doOnDispose()


        separator()

        val d3 = rxObservable
            .doOnSubscribe { println("doOnSubscribe() called") }
            .doOnDispose { println("doOnDisposed() called") } //not called if disposition is not called manually
            .doFinally { println("doFinally() called") }
            .subscribeWith(object : ResourceObserver<Int>() {
                override fun onComplete() {}
                override fun onNext(t: Int) {
                    dispose()
                } //if we do not call dispose, d3 will remain not disposed!!!!

                override fun onError(e: Throwable) {}
            })

        println("isDisposed: ${d3.isDisposed}") //onError automatically disposes
        d3.dispose() //no effect on .doOnDispose()


        //TODO

        // From book
        /* Note that doOnDispose() can fire multiple times for redundant disposal requests or not
        * at all if it is not disposed of in some form or another. Another option is to use the doFinally()
        * operator, which will fire after
        * either onComplete() or onError() is called or disposed of by the downstream.*/

    }

    @Test
    fun blockingSubscribe() {
        //Alters the behavior of onNext() -> see Retry with it
    }


    //TODO
    @Test
    fun retry() {
        //do not use in an 'infinite' fashion
        val rxObservable = Observable.fromIterable(listOf(1, 2, 0))

        //doAfterNext(), performs the action after the emission is passed downstream rather than before (.doOnNext).

        rxObservable
            .map { 100 / it }
            .retry(1) //the position is critical
            .testSubscribe()

        separator()

        rxObservable
            .doOnNext { println("$it") }
            .map { 100 / it }
            .doOnNext { println("$it") }
            .retry(1) //the position is critical
            .doOnNext { println("$it") }
            .testSubscribe()

    }

    @Test
    fun errorRecoveryOperators() {

        val rxError = Observable.fromIterable(listOf(1, 2, 0, 3, 4, 5))

        //default if error
        rxError
            .map { 100 / it }
            .onErrorReturnItem(-99)
            .testSubscribe()

        rxError
            .map { 100 / it }
            .onErrorReturn { e -> -99 }
            .testSubscribe()

        //continue the stream !!
        rxError
            .map {
                try {
                    100 / it
                } catch (e: ArithmeticException) {
                    -99
                }
            }
            .onErrorReturn { e -> -99 }
            .testSubscribe()


        //.onErrorResumeNext()
        //kind of flatMap if there is an error (maps one to many)
        rxError
            .map { 100 / it }
            .onErrorResumeNext(Observable.fromIterable(listOf(1000, 2000, 3000)))
            .testSubscribe()

        //with error checking
        rxError
            .map { 100 / it }
            .onErrorResumeNext { e: Throwable ->
                if (e !is ArithmeticException) {
                    Observable.fromIterable(listOf(-1, -1, -1))
                } else {
                    Observable.fromIterable(listOf(-2, -2, -2))
                }
            }
            .testSubscribe()

    }

    @Test
    fun toSet_viaCollect() {
        //take into consideration sleep extra time

        println(
            measureTimeMillis {
                Observable.fromIterable(0..10000000)
                    .toSet()
                    .map { it.size }
                    .singleTestSubscribe()
            }
        )

        sleep(2000)

        println(
            measureTimeMillis {
                Observable.fromIterable(0..10000000)
                    .toSet2()
                    .map { it.size }
                    .singleTestSubscribe()
            }
        )
    }

    @Test
    fun map_multimap() {

        Observable.fromIterable(0..10)
            .toMap { it % 2 == 0 }
            .singleTestSubscribe()

        Observable.fromIterable(0..10)
            .toMultimap { it % 2 == 0 }
            .singleTestSubscribe()

        Observable.fromIterable(0..10)
            .toMultimap(
                { it % 2 == 0 }, //keys
                { it + 100 }, //mapping
                { mutableMapOf<Boolean, MutableList<Int>>() as Map<Boolean, MutableCollection<Int>>? } // map type
            )
            .singleTestSubscribe()

    }

    @Test
    fun reduce() {
        //TODO Copied from RxJava book

        Observable.fromIterable(0..99)
            .reduce(0) { acc, n -> acc + 1 }
            .singleTestSubscribe()

        //To a string with separated values by '-'
        Observable.fromIterable(0..99)
            .reduce("") { acc, n -> "$acc-$n" }
            .map { it.takeLast(it.length - 1) }
            .singleTestSubscribe()

        //or
        Observable.fromIterable(0..99)
            .reduce("") { acc, n -> "$acc${if (acc == "") "" else "-"}$n" }
            .singleTestSubscribe()

    }

    @Test
    fun count_scan_reduce() {
        //counting emissions (results might vary)
        //take into consideration sleep time

        println(measureTimeMillis {
            Observable.fromIterable(0..1000000000)
                .count()
                .singleTestSubscribe()
        })

        //        onNext 1000000001
        //        Completed
        //        12005


        //slower for larger amount of emissions
        println(measureTimeMillis {
            Observable.fromIterable(0..1000000000)
                .scan(0) { acc, n -> acc + 1 }
                .skip(1)
                .takeLast(1)
                .testSubscribe()
        })

//        onNext 1000000001
//        Completed
//        4128


        println(measureTimeMillis {
            Observable.fromIterable(0..1000000000)
                .reduce(0) { acc, n -> acc + 1 }
                .singleTestSubscribe()
        })

//        onNext 1000000001
//        Completed
//        8017

    }

    @Test
    fun scan_vs_reduce() {

        Observable.fromIterable(0..10)
            .scan { acc: Int, next: Int -> acc + next }
            .testSubscribe()

        separator()

        //With initial value (it gets emitted first!!!)
        Observable.fromIterable(0..10)
            .scan(100) { acc: Int, next: Int -> acc + next }
            .skip(1) //to skip emission of 100 avoiding two repeated emissions
//            .distinctUntilChanged() //another option
            .testSubscribe()

        separator()

        //Counting emitted items (.count() might get stuck if the data source is potentially infinite)
        Observable.fromIterable(0..10)
            .scan(0) { acc: Int, next: Int -> acc + 1 }
            .skip(1)
            .testSubscribe()

        separator()

        Observable.fromIterable(0..10)
            .reduce { acc: Int, next: Int -> acc + next }
            .maybeTestSubscribe()

    }

    @Test
    fun repeat_repeatUntil() {

    }

    @Test
    fun delayWithObservable() {
        /* .delay() delays downstream */

        Observable.just(1)
            .doOnNext { println("Emitted $it") }
            .delay { Observable.timer(2, TimeUnit.SECONDS) }
            .testSubscribe(4000)

        separator()

        Observable.just(1)
            .delay { Observable.timer(2, TimeUnit.SECONDS) }
            .doOnNext { println("Emitted") }
            .testSubscribe(4000)

        separator()

        Observable.just(1)
            .delay { Observable.timer(2, TimeUnit.SECONDS) }
            .doOnNext { println("Emitted") }
            .delay { Observable.timer(2, TimeUnit.SECONDS) }
            .testSubscribe(6000)

    }

    @Test
    fun switchIfEmpty() {
        Observable.fromIterable<Int>(0..5)
            .filter { it > 10 }
            .switchIfEmpty(Observable.just(10, 11, 12))
            .testSubscribe()
    }

    @Test
    fun startWithArray() {
        Observable.just(5)
            .startWithArray(1, 0)
            .testSubscribe()
    }

    @Test
    fun map_flatMap() {
        /* map: one to one emission
        * flatMap: one to many emissions */
    }


    @Test
    fun resourceObserver() {
        /* ResourceObserver, an observer that implements Disposable and therefore an be disposed
        * in the middle of the chain */
        /* Subscribing using the 3 lambdas RETURNS a disposable. Subscribing using an Observer DOES NOT !!!!!!! */

        /* The Disposable is sent from the source all the way up the chain to the Observer, so each
        * step in the Observable chain has access to the Disposable.
        * Note that passing an Observer to the subscribe() method will be void and not return a
        * Disposable since it is assumed that the Observer will handle it. If you do not want to
        * explicitly handle the Disposable and want RxJava to handle it for you (which is probably
        * a good idea until you have reason to take control), you can extend ResourceObserver
        * as your Observer, which uses a default Disposable handling.
        */

        //Disposition after all emissions
        val resourceObserver: ResourceObserver<Long> = object : ResourceObserver<Long>() {
            override fun onComplete() {
                println("Completed")
            }

            override fun onNext(t: Long) {
                println(t)
            }

            override fun onError(e: Throwable) {
                println(e.message)
            }
        }

        val disposable =
            Observable.interval(1, TimeUnit.SECONDS).subscribeWith(resourceObserver) //can dispose
        sleep()
        disposable.dispose()
        println("Disposed")
        sleep()

        //Disposition in onNext()
        val resourceObserver2: ResourceObserver<Long> = object : ResourceObserver<Long>() {
            override fun onComplete() {
                println("Completed")
            }

            override fun onNext(t: Long) {
                println(t); if (t == 3L) dispose()
            } //disposing inside the chain

            override fun onError(e: Throwable) {
                println(e.message)
            }
        }

        val disposable2 = Observable.interval(1, TimeUnit.SECONDS)
            .subscribeWith(resourceObserver2) //disposing inside the chain
        sleep()
        println("disposable is disposed = ${disposable2.isDisposed}")
    }


    //TODO
    @Test
    fun callableAndDefer() {
        //????
        fun getValue(): Int {
            val random = Random.nextInt(0, 1)
            val value = 1 / random
            return value
        }

        val rxDefer = Observable.defer { Observable.just(getValue()) }
        val rxFromCallable = Observable.fromCallable { getValue() }

        rxDefer
            .blockingSubscribe(
                { n -> println("onNext $n") },
                { e -> println("Error, Defer: ${e.message}") },
                { println("Completed") }
            )

        rxFromCallable
            .blockingSubscribe(
                { n -> println("onNext $n") },
                { e -> println("Error, fromCallable: ${e.message}") },
                { println("Completed") }
            )

    }

    @Test
    fun never() {
        //Leaves the observers hanging waiting for an "onComplete()" that will never be emitted.
        val rxNever = Observable.never<String>()
    }

    @Test
    fun multicasting_HOT_observable() {
        //HOT observable. Last subscription lost emissions
        val connectableObservable_hotObservable: ConnectableObservable<Long> =
            Observable.interval(2, TimeUnit.SECONDS).take(15, TimeUnit.SECONDS).publish()
        connectableObservable_hotObservable.subscribe { println(it) }
        connectableObservable_hotObservable.subscribe { println("ITEM_ID$it") }
        connectableObservable_hotObservable.connect()
        sleep()
        connectableObservable_hotObservable.blockingSubscribe { println("LATE$it") }
    }

    @Test
    fun multicasting_COLD_observable() {
        //Cold observable.
        val coldObservable = Observable.interval(2, TimeUnit.SECONDS).take(15, TimeUnit.SECONDS)
        coldObservable.subscribe { println(it) }
        sleep()
        coldObservable.blockingSubscribe { println("LATE$it") }
    }

    @Test
    fun javaOptional() {
        val a = Optional.empty<Boolean>()
        println(a.orElse(false))
    }

    @Test
    fun shareOperator() {

        val serverResponseTime = 5L
        val slowConnectionTimer = 2L
        val verySlowConnectionTimer = 8L

        val rxCall = Observable.just(UserSessionModel.createValid())
            .delay(serverResponseTime, TimeUnit.SECONDS)
        val messagePopUpSlow = Observable.timer(slowConnectionTimer, TimeUnit.SECONDS)
            .flatMap { Observable.empty<UserSessionModel>() }
            .doOnComplete { println("Poor Connection") }
        val messagePopUpVerySlow = Observable.timer(verySlowConnectionTimer, TimeUnit.SECONDS)
            .flatMap { Observable.empty<UserSessionModel>() }
            .doOnComplete { println("Very Poor Connection") }

        val callToServer = Observable.merge(listOf(rxCall, messagePopUpSlow, messagePopUpVerySlow))

        callToServer
            .materialize()
            .takeUntil { it.value != null }
            .dematerialize<UserSessionModel> { it.dematerializeSafely() }
            .testSubscribe()


//        callToServer.share()
//
//        callToServer
//            .blockingSubscribe {
//
//            }
//
//        callToServer
//            .blockingSubscribe {
//
//            }


    }


    @Test
    fun takeUntilTrial() {

        val serverResponseTime = 5L
        val slowConnectionTimer = 2L

        val rxCall = Observable.just("Data from server").delay(serverResponseTime, TimeUnit.SECONDS)
        val messagePopUp = Observable.timer(slowConnectionTimer, TimeUnit.SECONDS)

        Observable.merge(rxCall, messagePopUp)
            .takeUntil { it::class == String::class }
            .filter { it::class != String::class }
            .testSubscribe()

    }

    @Test
    fun rxJavaDetectDelayInResponse() {

        val slowConnection: Long = 2L
        val verySlowConnection: Long = 5L
        val serverResponseTime = 8L

        val serverResponse =
            Observable.just("Data from server").delay(serverResponseTime, TimeUnit.SECONDS)
        val messageSlowConnection =
            Observable.empty<String>().delay(slowConnection, TimeUnit.SECONDS)
                .doOnComplete { println("Slow connection") }
        val messageVerySlowConnection =
            Observable.empty<String>().delay(verySlowConnection, TimeUnit.SECONDS)
                .doOnComplete { println("Very slow connection") }

        Observable.merge(listOf(serverResponse, messageSlowConnection, messageVerySlowConnection))
            .first("Default item")
            .subscribe({ println(it) }, { e -> e.printStackTrace() })
//            .subscribeWith(object : ResourceObserver<String>() {
//                override fun onComplete() {
//                    println("Completed")
//                }
//                override fun onNext(s: String) {
//                    dispose()
//                    println(s)
//                }
//
//                override fun onError(e: Throwable) {
//                    e.printStackTrace()
//                }
//            })

        sleep(10000)

    }


    @Test
    fun rxJavaDetectDelay() {

        val leftDelayBetweenEmissions: Long = 5
        val rightDelayBetweenEmissions: Long = 2

        val leftWindowDuration: Long = 5
        val rightWindowDuration: Long = 2

        val left = Observable.timer(leftDelayBetweenEmissions, TimeUnit.SECONDS)
        val right = Observable.just("String").delay(4, TimeUnit.SECONDS)

        left.join(right,
            Function { l: Long -> Observable.timer(leftWindowDuration, TimeUnit.SECONDS) },
            Function { s: String -> Observable.timer(rightWindowDuration, TimeUnit.SECONDS) },
            BiFunction { t1: Long, t2: String ->
                println("BiFunction called")
                println(t1)
                println(t2)
                t2
            })
            .testSubscribe()

    }


    @Test
    fun rxJavaRetryWhenWithDelayTest() {

        var counter = 0

        fun callToServer(): Observable<String> {
            return Observable.defer {
                counter++
                println("Call to server done")
                if (counter == 3) {
                    println("RESPONSE OK")
                    Observable.just("Data Obtained")
                } else {
                    println("RESPONSE ERROR")
                    Observable.error<String>(TimeoutException("Error in call"))
                }
            }
        }

        callToServer()
            .retryWhen { errors ->
                println("Error happened")
                errors
                    .zipWith(Observable.range(1, 3), BiFunction { t1: Throwable, t2: Int ->
                        t2
                    })
                    .flatMap {
                        Observable.timer((it * 2).toLong(), TimeUnit.SECONDS).map { it }
                    }
            }
            .testSubscribe()
    }

    @Test
    fun rxJavaRetryWhenTest() {

        var counter = 0

        fun callToServer(): Observable<String> {
            return Observable.defer {
                if (counter == 2) {
                    Observable.just("Good Response")
                } else {
                    counter++
                    Observable.error<String>(TimeoutException("Error in call"))
                }
            }
        }

        callToServer()
            .retryWhen { errors ->
                errors
                    .flatMap { error ->
                        if (error is TimeoutException) {
                            println("Error is timeOutException")
                            Observable.just(true) //retries
                        } else {
                            Observable.error<Boolean>(Throwable("Error in retryWhen"))
                        }
                    }
            }
            .testSubscribe()
    }
}

//////

data class UserSessionModel(val validSession: Boolean, val error: Throwable? = null) {

    companion object {
        fun createValid() = UserSessionModel(true, null)
    }
}

fun <T> T?.getOrElse(valueWhenNull: T): T {
    return this ?: valueWhenNull
}

fun <T> List<T>.randomSearch(): T {
    return with(this) {
        this.first()
    }
}


fun <T> Notification<T>.dematerializeSafely(): Notification<T> {
    return kotlin.with(this.value) {
        if (this != null) {
            Notification.createOnNext<T>(this)
        } else {
            Notification.createOnError<T>(kotlin.Throwable("Error in dematerialize"))
        }
    }
}

fun sleep(millis: Long = 5000) {
    try {
        Thread.sleep(millis)
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

fun <T> Observable<T>.testSubscribeNoDelay(): Disposable {
    val d = this
        .subscribe(
            { n -> kotlin.io.println("onNext $n") },
            { e -> kotlin.io.println("Error: ${e.message}") },
            { kotlin.io.println("Completed") }
        )
    return d
}

fun <T> Observable<T>.testSubscribe(timeInMillis: Long = 2000): Disposable {
    val d = this
        .subscribe(
            { n -> kotlin.io.println("onNext $n") },
            { e -> kotlin.io.println("Error: ${e.message}") },
            { kotlin.io.println("Completed") }
        )
    sleep(timeInMillis)
    return d
}

fun <T> Maybe<T>.maybeTestSubscribe(timeInMillis: Long = 2000): Disposable {
    val d = this
        .subscribe(
            { n -> kotlin.io.println("onNext $n") },
            { e -> kotlin.io.println("Error: ${e.message}") },
            { kotlin.io.println("Completed") }
        )
    sleep(timeInMillis)
    return d
}

fun <T> Single<T>.singleTestSubscribe(timeInMillis: Long = 2000): Disposable {
    val d = this.subscribe(
        { n -> kotlin.io.println("onSuccess $n") },
        { e -> kotlin.io.println("Error: ${e.message}") }
    )
    sleep(timeInMillis)
    return d
}

//Faster with collections of more than 10_000_000, slower with less
fun <T> Observable<T>.toSet(): Single<Set<T>> {
    return this.collect({ kotlin.collections.mutableListOf<T>() }, { a, b -> a.add(b) }).map { it.toSet() }
}

//Slower with collections of more than 10_000_000, faster with less
fun <T> Observable<T>.toSet2(): Single<Set<T>> {
    return this.collect({ kotlin.collections.mutableSetOf<T>() }, { a, b -> a.add(b) }).map { it.toSet() }
}

fun <T> Observable<T>.toMutableSet(): Single<MutableSet<T>> {
    return this.collect({ kotlin.collections.mutableSetOf<T>() }, { a, b -> a.add(b) })
}

fun separator() {
    println("")
    println("///////////")
    println("")
}

fun rxTimer(timeInMillis: Long = 2000): Observable<Long> {
    return Observable.timer(timeInMillis, TimeUnit.MILLISECONDS)
}


package com.diegomfv.moodtrackerv2

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test

class ThreadsTest {


    @Test
    fun sorting_io_computation () {

        fun getRandomNumbers () = (0..100_000_000).shuffled().toList()

        Observable.fromIterable(getRandomNumbers())
            .doOnSubscribe { println("Started") }
            .subscribeOn(Schedulers.io())
            .sorted()
            .toList()
            .doOnSuccess { println("Finished") }
            .subscribe()

        sleep(10000)

    }



}
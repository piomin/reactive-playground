package pl.piomin.service

import org.junit.Assert
import org.junit.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import kotlin.test.Asserter
import kotlin.test.assertEquals

class HelloTest {

    @Test
    fun test1() {
        val persons : Flux<Person> = getPersonsBasic()
                .zipWith(getPersonsContact())
                .map { t -> Person(t.t1.id, t.t1.firstName!!, t.t1.lastName!!, t.t2.email!!) }
        persons.toStream()
                .forEach { p -> Assert.assertEquals("${p.firstName}-${p.lastName}@example.com", p.email) }
    }

    @Test
    fun test2() {
        val persons : Flux<Person> = getPersonsBasic()
                .mergeWith(getPersonsContact())
        println(persons.toStream().count())
    }

    @Test
    fun test3() {
        val publisher : Publisher<Person> = Flux.empty()
        val persons : Flux<Person> = getPersonsBasic()
                .flatMap { t -> publisher }
        println(persons.toStream().count())
    }

    private fun getPersonsBasic() : Flux<Person> {
        return Flux.just(Person(1, "AB", "BA"), Person(2, "AA", "BB"))
    }

    private fun getPersonsContact() : Flux<Person> {
        return Flux.just(Person(1, "AB-BA@example.com"), Person(2, "AA-BB@example.com"))
    }

}

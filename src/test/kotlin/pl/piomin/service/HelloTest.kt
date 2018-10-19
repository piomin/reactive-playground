package pl.piomin.service

import org.junit.Assert
import org.junit.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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
        val organization : Mono<Organization> = getOrganizationByName("test")
                .zipWhen { organization ->
                    getEmployeesByOrganization(organization.id!!).collectList()
                }
                .map { tuple -> Organization(tuple.t1.id, tuple.t1.name, tuple.t2) }

        val org = organization.block()
        Assert.assertEquals("test", org.name)
        Assert.assertEquals(1, org.id)
        Assert.assertEquals(2, org.employees.size)
        org.employees.stream()
                .forEach { e -> Assert.assertEquals("Employee${e.id}", e.name) }
    }

    @Test
    fun test3() {
        var employees: Flux<Employee> = getDepartmentsByOrganization(1)
                .flatMapIterable { department -> department.employees }
        employees.subscribe(System.out::println)

        val organization : Mono<Organization> = getOrganizationByName("test")
                .zipWhen { organization ->
                    employees.collectList()
                }
                .map { tuple -> Organization(tuple.t1.id, tuple.t1.name, tuple.t2) }

        val org = organization.block()
        Assert.assertEquals("test", org.name)
        Assert.assertEquals(1, org.id)
        Assert.assertEquals(4, org.employees.size)
        org.employees.stream()
                .forEach { e -> Assert.assertEquals("Employee${e.id}", e.name) }
    }

    private fun getPersonsBasic() : Flux<Person> {
        return Flux.just(Person(1, "AB", "BA"),
                         Person(2, "AA", "BB"))
    }

    private fun getPersonsContact() : Flux<Person> {
        return Flux.just(Person(1, "AB-BA@example.com"),
                         Person(2, "AA-BB@example.com"))
    }

    private fun getOrganizationByName(name: String) : Mono<Organization> {
        return Mono.just(Organization(1, name))
    }

    private fun getEmployeesByOrganization(id: Int) : Flux<Employee> {
        return Flux.just(Employee(1, "Employee1", 1000, id),
                         Employee(2, "Employee2", 2000, id))
    }

    private fun getDepartmentsByOrganization(id: Int) : Flux<Department> {
        val dep1 = Department(1, "A", id, mutableListOf(
                Employee(1, "Employee1", 1000, id, 1),
                Employee(2, "Employee2", 2000, id, 1)
                )
        )
        val dep2 = Department(2, "B", id, mutableListOf(
                Employee(3, "Employee3", 1000, id, 2),
                Employee(4, "Employee4", 2000, id, 2)
                )
        )
        return Flux.just(dep1, dep2)
    }
}

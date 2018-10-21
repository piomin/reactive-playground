package pl.piomin.service

import org.junit.Assert
import org.junit.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.GroupedFlux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
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
        var organization: Mono<Organization> = getDepartmentsByOrganization(1)
                .flatMapIterable { department -> department.employees }
                .collectList()
                .map { t -> Organization(1, "X", t) }
        Assert.assertEquals(4, organization.block().employees.size)

        val employees : Flux<Employee> = organization.
                flatMapIterable { organization -> organization.employees }
        Assert.assertEquals(4, employees.toStream().count())
    }

    @Test
    fun test4() {
        val persons: Flux<Person> = getPersonsFirstPart()
                .mergeOrderedWith(getPersonsSecondPart(), Comparator { o1, o2 -> o1.id.compareTo(o2.id) })
                .map { person ->
                    Person(person.id, person.firstName!!, person.lastName!!,
                            "${person.firstName}-${person.lastName}@example.com")
                }
        persons.toIterable().forEachIndexed { index, person ->
            run {
                Assert.assertEquals(index + 1, person.id)
                Assert.assertEquals("${person.firstName}-${person.lastName}@example.com", person.email)
            }
        }
    }

    @Test
    fun test5() {
        val departments: Flux<Department> = getDepartments()
                .flatMap { department ->
                    Mono.just(department)
                            .zipWith(getEmployees().filter { it.departmentId == department.id }.collectList())
                            .map { t -> department.addEmployees(t.t2) }
                }
        departments.toStream().forEach {
            Assert.assertEquals(2, it.employees.size)
        }
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

    private fun getDepartments() : Flux<Department> {
        return Flux.just(Department(1, "X", 1),
                         Department(2, "Y", 1))
    }

    private fun getEmployees() : Flux<Employee> {
        return Flux.just(Employee(1, "Employee1", 1000, 1, 1),
                Employee(2, "Employee2", 2000, 1, 1),
                Employee(3, "Employee3", 1000, 1, 2),
                Employee(4, "Employee4", 2000, 1, 2))
    }

    private fun getPersonsFirstPart() : Flux<Person> {
        return Flux.just(Person(1, "AA", "AA"),
                Person(3, "BB", "BB"))
    }

    private fun getPersonsSecondPart() : Flux<Person> {
        return Flux.just(Person(2, "CC", "CC"),
                Person(4, "DD", "DD"))
    }

}

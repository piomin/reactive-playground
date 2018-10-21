package pl.piomin.service

class Department(var id: Int, var name: String, var organizationId: Int) {
    var employees: MutableList<Employee> = ArrayList()

    constructor(id: Int, name: String, organizationId: Int, employees: MutableList<Employee>) : this(id, name, organizationId) {
        this.employees.addAll(employees)
    }

    fun addEmployees(employees: MutableList<Employee>) : Department {
        this.employees.addAll(employees)
        return this
    }

    fun addEmployee(employee: Employee) : Department {
        this.employees.add(employee)
        return this
    }

}
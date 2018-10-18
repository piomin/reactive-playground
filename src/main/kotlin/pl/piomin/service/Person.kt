package pl.piomin.service

data class Person (var id : Int) {
    var firstName : String? = null
    var lastName : String? = null
    var email : String? = null

    constructor(id: Int, firstName: String, lastName: String, email: String) : this(id) {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
    }

    constructor(id: Int, firstName: String, lastName: String) : this(id) {
        this.firstName = firstName
        this.lastName = lastName
    }

    constructor(id: Int, email: String) : this(id) {
        this.email = email
    }
}
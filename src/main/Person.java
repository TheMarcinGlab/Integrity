package main;

import main.enums.Type;

public record Person(
        String personId,
        Type type,
        String firstName,
        String lastName,
        String mobile,
        String email,
        String pesel
) {
    public Person {
    }

    public Person withType(Type v)       { return new Person(personId, v, firstName, lastName, mobile, email, pesel); }
    public Person withFirstName(String v){ return new Person(personId, type, v, lastName, mobile, email, pesel); }
    public Person withLastName(String v) { return new Person(personId, type, firstName, v, mobile, email, pesel); }
    public Person withMobile(String v)   { return new Person(personId, type, firstName, lastName, v, email, pesel); }
    public Person withEmail(String v)    { return new Person(personId, type, firstName, lastName, mobile, v, pesel); }
    public Person withPesel(String v)    { return new Person(personId, type, firstName, lastName, mobile, email, v); }
}

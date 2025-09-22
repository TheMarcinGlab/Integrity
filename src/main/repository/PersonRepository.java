package main.repository;


import main.dto.Person;
import main.enums.Type;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface PersonRepository {
    Optional<Person> findById(String personId) throws IOException;

    List<Person> findAll() throws IOException;

    List<Person> findBy(Predicate<Person> filter) throws IOException;

    void create(Person person) throws IOException;

    boolean remove(String personId) throws IOException;

    void update(Person person) throws IOException;

    static Predicate<Person> by(
            String personId, Type type, String firstName, String lastName,
            String mobile, String email, String pesel
    ) {
        return p ->
                (personId == null || personId.equals(p.personId())) &&
                        (type == null || type == p.type()) &&
                        (firstName == null || firstName.equalsIgnoreCase(p.firstName())) &&
                        (lastName  == null || lastName.equalsIgnoreCase(p.lastName())) &&
                        (mobile    == null || mobile.equals(p.mobile())) &&
                        (email     == null || email.equalsIgnoreCase(p.email())) &&
                        (pesel     == null || pesel.equals(p.pesel()));
    }
}


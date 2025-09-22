package main.service;


import main.exception.PersonAlreadyExistsException;
import main.exception.PersonNotFoundException;
import main.dto.Person;
import main.enums.Type;
import main.repository.PersonRepository;
import main.validation.Validators;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class PersonService {
    private final PersonRepository repo;

    public PersonService(PersonRepository repo) {
        this.repo = Objects.requireNonNull(repo, "repo");
    }


    public Optional<Person> find(String personId, Type type,
                                 String firstName, String lastName,
                                 String mobile, String pesel, String email) throws IOException {
        Predicate<Person> filter = PersonRepository.by(personId, type, firstName, lastName, mobile, email, pesel);
        var list = repo.findBy(filter);
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.getFirst());
    }

    public List<Person> findAllBy(String personId, Type type,
                                  String firstName, String lastName,
                                  String mobile, String pesel, String email) throws IOException {
        Predicate<Person> filter = PersonRepository.by(personId, type, firstName, lastName, mobile, email, pesel);
        return repo.findBy(filter);
    }

    public void create(Person person) throws IOException {
        Validators.validateNew(person);
        if (repo.findById(person.personId()).isPresent())
            throw new PersonAlreadyExistsException("Person with id %s already exists".formatted(person.personId()));
        repo.create(person);
    }

    public boolean remove(String personId) throws IOException {
        return repo.remove(personId);
    }

    public void modify(Person person) throws IOException {
        Validators.validateUpdate(person);
        if (repo.findById(person.personId()).isEmpty())
            throw new PersonNotFoundException("Person with id %s not found".formatted(person.personId()));
        repo.update(person);
    }

    public static String newId() { return UUID.randomUUID().toString(); }
}


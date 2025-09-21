package tests;

import main.Person;
import main.exception.PersonAlreadyExistsException;
import main.exception.PersonNotFoundException;
import main.enums.Type;
import main.repository.PersonRepository;
import main.repository.XmlPersonRepository;
import main.service.PersonService;
import main.validation.Validators;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class ServiceTests {

    public static void main(String[] args) throws Exception {
        Path tempRoot = Files.createTempDirectory("people-xml-tests");
        try {
            runAll(tempRoot);
            System.out.println("ServiceTests: ALL PASSED");
        } finally {
            cleanup(tempRoot);
        }
    }

    private static void runAll(Path root) throws Exception {
        PersonRepository repo = new XmlPersonRepository(root);
        PersonService service = new PersonService(repo);

        String id1 = "P-1001";
        Person p1 = new Person(id1, Type.INTERNAL, "Anna", "Nowak",
                "+48500500500", "anna.nowak@example.com", "02270803628");
        Validators.validateNew(p1);
        service.create(p1);
        assert service.find(id1, null, null, null, null, null, null).isPresent();

        boolean dupThrown = false;
        try { service.create(p1); } catch (PersonAlreadyExistsException e) { dupThrown = true; }
        assert dupThrown;

        List<Person> res = service.findAllBy(null, Type.INTERNAL, "Anna", "Nowak", null, null, null);
        assert res.size() == 1;

        Person p1b = p1.withMobile("+48600600600");
        Validators.validateUpdate(p1b);
        service.modify(p1b);
        assert service.find(id1, null, null, null, "+48600600600", null, null).isPresent();

        Person p1c = p1b.withType(Type.EXTERNAL);
        Validators.validateUpdate(p1c);
        service.modify(p1c);
        assert service.findAllBy(null, Type.EXTERNAL, "Anna", "Nowak", null, null, null).size() == 1;

        String id2 = "P-2002";
        Person p2 = new Person(id2, Type.EXTERNAL, "Piotr", "Zieliński",
                "+48700700700", "piotr.z@example.com", "99010112376");
        Validators.validateNew(p2);
        service.create(p2);

        boolean removed = service.remove(id1);
        assert removed;

        boolean notFoundThrown = false;
        try { service.modify(p1c); } catch (PersonNotFoundException e) { notFoundThrown = true; }
        assert notFoundThrown;
    }

    private static void cleanup(Path root) throws IOException {
        try (var walk = Files.walk(root)) {
            walk.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        }
    }
}

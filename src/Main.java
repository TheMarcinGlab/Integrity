import main.Person;
import main.enums.Type;
import main.repository.PersonRepository;
import main.repository.XmlPersonRepository;
import main.service.PersonService;
import main.validation.Validators;
import main.exception.ValidationException;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        Path root = Path.of("data");
        PersonRepository repo = new XmlPersonRepository(root);
        PersonService service = new PersonService(repo);

        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Dodaj pracownika");
            System.out.println("2. Znajdź pracownika");
            System.out.println("3. Usuń pracownika");
            System.out.println("4. Zmień dane pracownika");
            System.out.println("5. Wyświetl wszystkich");
            System.out.println("0. Wyjście");
            System.out.print("Wybierz opcję: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addPerson(sc, service);
                case "2" -> findPerson(sc, service);
                case "3" -> removePerson(sc, service);
                case "4" -> modifyPerson(sc, service);
                case "5" -> listAll(service);
                case "0" -> {
                    System.out.println("Koniec programu.");
                    return;
                }
                default -> System.out.println("Nieprawidłowa opcja.");
            }
        }
    }


    private static void addPerson(Scanner sc, PersonService service) {
        try {
            String id     = UUID.randomUUID().toString();
            System.out.print("Imię: ");        String first  = sc.nextLine();
            System.out.print("Nazwisko: ");    String last   = sc.nextLine();
            System.out.print("Telefon: ");     String mobile = sc.nextLine();
            System.out.print("Email: ");       String email  = sc.nextLine();
            System.out.print("PESEL: ");       String pesel  = sc.nextLine();
            System.out.print("Typ (INTERNAL/EXTERNAL): ");
            Type type = Type.valueOf(sc.nextLine().trim().toUpperCase());

            Person p = new Person(id, type, first, last, mobile, email, pesel);

            Validators.validateNew(p);

            service.create(p);
            System.out.println("Dodano: " + p);
        } catch (IllegalArgumentException e) {
            System.out.println("Błąd: nieprawidłowy typ (INTERNAL/EXTERNAL).");
        } catch (ValidationException e) {
            System.out.println("Błąd walidacji: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    private static void findPerson(Scanner sc, PersonService service) throws Exception {
        System.out.print("Id (Enter = pomiń): "); String id = emptyToNull(sc.nextLine());
        System.out.print("Typ (INTERNAL/EXTERNAL/Enter): ");
        String typeStr = sc.nextLine().trim();
        Type type = typeStr.isBlank() ? null : Type.valueOf(typeStr.toUpperCase());
        System.out.print("Imię (Enter = pomiń): "); String first = emptyToNull(sc.nextLine());
        System.out.print("Nazwisko (Enter = pomiń): "); String last = emptyToNull(sc.nextLine());
        System.out.print("Telefon (Enter = pomiń): "); String mobile = emptyToNull(sc.nextLine());
        System.out.print("PESEL (Enter = pomiń): "); String pesel = emptyToNull(sc.nextLine());
        System.out.print("Email (Enter = pomiń): "); String email = emptyToNull(sc.nextLine());

        List<Person> results = service.findAllBy(id, type, first, last, mobile, pesel, email);
        if (results.isEmpty()) {
            System.out.println("Brak wyników.");
        } else {
            printPersons(results);
        }
    }

    private static void removePerson(Scanner sc, PersonService service) throws Exception {
        System.out.print("Podaj ID do usunięcia: ");
        String id = sc.nextLine().trim();
        boolean removed = service.remove(id);
        System.out.println(removed ? "Usunięto." : "Nie znaleziono.");
    }

    private static void modifyPerson(Scanner sc, PersonService service) {
        try {
            System.out.print("Podaj ID do modyfikacji: ");
            String id = sc.nextLine().trim();
            Optional<Person> found = service.find(id, null, null, null, null, null, null);
            if (found.isEmpty()) {
                System.out.println("Nie znaleziono.");
                return;
            }
            Person p = found.get();
            System.out.println("Aktualne dane: " + p);
            System.out.println("Wpisz nową wartość lub Enter aby pominąć.");

            System.out.print("Nowe imię: ");      String first  = sc.nextLine();
            if (!first.isBlank())  p = p.withFirstName(first);

            System.out.print("Nowe nazwisko: ");  String last   = sc.nextLine();
            if (!last.isBlank())   p = p.withLastName(last);

            System.out.print("Nowy telefon: ");   String mobile = sc.nextLine();
            if (!mobile.isBlank()) p = p.withMobile(mobile);

            System.out.print("Nowy email: ");     String email  = sc.nextLine();
            if (!email.isBlank())  p = p.withEmail(email);

            System.out.print("Nowy PESEL: ");     String pesel  = sc.nextLine();
            if (!pesel.isBlank())  p = p.withPesel(pesel);

            System.out.print("Nowy typ (INTERNAL/EXTERNAL/Enter): ");
            String typeStr = sc.nextLine().trim();
            if (!typeStr.isBlank()) p = p.withType(Type.valueOf(typeStr.toUpperCase()));

            // Walidacja WYŁĄCZNIE tutaj:
            Validators.validateUpdate(p);

            service.modify(p);
            System.out.println("Zaktualizowano: " + p);
        } catch (IllegalArgumentException e) {
            System.out.println("Błąd: nieprawidłowy typ (INTERNAL/EXTERNAL).");
        } catch (ValidationException e) {
            System.out.println("Błąd walidacji: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    private static void listAll(PersonService service) throws Exception {
        List<Person> all = service.findAllBy(null, null, null, null, null, null, null);
        if (all.isEmpty()) {
            System.out.println("Brak pracowników.");
        } else {
            printPersons(all);
        }
    }

    private static void printPersons(List<Person> persons) {
        for (Person p : persons) {
            System.out.println("-------------------------------------------------");
            System.out.println("ID:        " + p.personId());
            System.out.println("Typ:       " + p.type());
            System.out.println("Imię:      " + p.firstName());
            System.out.println("Nazwisko:  " + p.lastName());
            System.out.println("Telefon:   " + p.mobile());
            System.out.println("Email:     " + p.email());
            System.out.println("PESEL:     " + p.pesel());
        }
        System.out.println("-------------------------------------------------");
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

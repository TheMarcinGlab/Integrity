import main.dto.Person;
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
            System.out.println("\n=========== MENU ===========");
            System.out.println("1. Add an employee");
            System.out.println("2. Find employee");
            System.out.println("3. Delete employee");
            System.out.println("4. Update employee");
            System.out.println("5. List all employees");
            System.out.println("0. Quit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addPerson(sc, service);
                case "2" -> findPerson(sc, service);
                case "3" -> removePerson(sc, service);
                case "4" -> modifyPerson(sc, service);
                case "5" -> listAll(service);
                case "0" -> {
                    System.out.println("End. See you later!");
                    return;
                }
                default -> System.out.println("⚠️ Invalid option.");
            }
        }
    }

    private static void addPerson(Scanner sc, PersonService service) {
        try {
            String id = UUID.randomUUID().toString();
            System.out.print("First name: ");             String first  = sc.nextLine();
            System.out.print("Last name: ");              String last   = sc.nextLine();
            System.out.print("Phone number: ");           String mobile = sc.nextLine();
            System.out.print("Email: ");                  String email  = sc.nextLine();
            System.out.print("PESEL: ");                  String pesel  = sc.nextLine();
            System.out.print("Type (INTERNAL/EXTERNAL): ");
            Type type = Type.valueOf(sc.nextLine().trim().toUpperCase());

            Person p = new Person(id, type, first, last, mobile, email, pesel);
            Validators.validateNew(p);

            service.create(p);
            System.out.println("\n✅ New employee added:");
            printPerson(p, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("⚠️ Error: invalid type (use INTERNAL or EXTERNAL).");
        } catch (ValidationException e) {
            System.out.println("⚠️ Validation error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
    }

    private static void findPerson(Scanner sc, PersonService service) throws Exception {
        System.out.print("ID (Enter = skip): ");                  String id = emptyToNull(sc.nextLine());
        System.out.print("Type (INTERNAL/EXTERNAL or Enter): ");  String typeStr = sc.nextLine().trim();
        Type type = typeStr.isBlank() ? null : Type.valueOf(typeStr.toUpperCase());
        System.out.print("First name (Enter = skip): ");          String first = emptyToNull(sc.nextLine());
        System.out.print("Last name (Enter = skip): ");           String last = emptyToNull(sc.nextLine());
        System.out.print("Phone number (Enter = skip): ");        String mobile = emptyToNull(sc.nextLine());
        System.out.print("PESEL (Enter = skip): ");               String pesel = emptyToNull(sc.nextLine());
        System.out.print("Email (Enter = skip): ");               String email = emptyToNull(sc.nextLine());

        List<Person> results = service.findAllBy(id, type, first, last, mobile, pesel, email);
        if (results.isEmpty()) {
            System.out.println("\nℹ️ No results.");
        } else {
            System.out.println("\n🔎 Search results:");
            printPersons(results);
        }
    }

    private static void removePerson(Scanner sc, PersonService service) throws Exception {
        System.out.print("Enter the ID to delete: ");
        String id = sc.nextLine().trim();
        boolean removed = service.remove(id);
        System.out.println(removed ? "Employee removed." : "Employee not found.");
    }

    private static void modifyPerson(Scanner sc, PersonService service) {
        try {
            System.out.print("Enter employee ID to update: ");
            String id = sc.nextLine().trim();
            Optional<Person> found = service.find(id, null, null, null, null, null, null);
            if (found.isEmpty()) {
                System.out.println("⚠️ Not found.");
                return;
            }
            Person p = found.get();
            System.out.println("\n✏️ Current employee data:");
            printPerson(p, 1);
            System.out.println("Type a new value or press Enter to skip.");

            System.out.print("New first name: ");      String first  = sc.nextLine();
            if (!first.isBlank())  p = p.withFirstName(first);

            System.out.print("New last name: ");       String last   = sc.nextLine();
            if (!last.isBlank())   p = p.withLastName(last);

            System.out.print("New phone number: ");    String mobile = sc.nextLine();
            if (!mobile.isBlank()) p = p.withMobile(mobile);

            System.out.print("New email: ");           String email  = sc.nextLine();
            if (!email.isBlank())  p = p.withEmail(email);

            System.out.print("New PESEL: ");           String pesel  = sc.nextLine();
            if (!pesel.isBlank())  p = p.withPesel(pesel);

            System.out.print("New type (INTERNAL/EXTERNAL or Enter): ");
            String typeStr = sc.nextLine().trim();
            if (!typeStr.isBlank()) p = p.withType(Type.valueOf(typeStr.toUpperCase()));

            Validators.validateUpdate(p);
            service.modify(p);

            System.out.println("\n✅ Employee data updated:");
            printPerson(p, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("⚠️ Error: invalid type (use INTERNAL or EXTERNAL).");
        } catch (ValidationException e) {
            System.out.println("⚠️ Validation error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
    }

    private static void listAll(PersonService service) throws Exception {
        List<Person> all = service.findAllBy(null, null, null, null, null, null, null);
        if (all.isEmpty()) {
            System.out.println("\nℹ️ No employees found.");
        } else {
            System.out.println("\n📋 All employees:");
            printPersons(all);
            System.out.println("Total: " + all.size() + " employees.");
        }
    }

    private static void printPersons(List<Person> persons) {
        int i = 1;
        for (Person p : persons) {
            printPerson(p, i++);
        }
    }

    private static void printPerson(Person p, int index) {
        System.out.println("=========================================");
        System.out.println("Employee #" + index);
        System.out.println("ID:        " + p.personId());
        System.out.println("Type:      " + p.type());
        System.out.println("First name:" + " " + p.firstName());
        System.out.println("Last name: " + p.lastName());
        System.out.println("Phone:     " + p.mobile());
        System.out.println("Email:     " + p.email());
        System.out.println("PESEL:     " + p.pesel());
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

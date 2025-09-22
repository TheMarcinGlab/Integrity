package tests;

import main.dto.Person;
import main.exception.ValidationException;
import main.enums.Type;
import main.validation.Validators;

public class ValidatorsTests {

    public static void main(String[] args) {
        testValidPersonPasses();

        testFirstNameFormat();
        testLastNameFormat();

        testMobileFormat();
        testEmailFormat();

        testPeselLengthAndDigits();
        testPeselChecksum();

        testRequiredFieldsBlankOrNull();
        testLeadingTrailingSpacesRejected();
        testHyphenOrMultiPartNamesAreRejectedWithCurrentRules();

        testTypeRequired();
        testPersonIdRequiredOnCreateAndUpdate();

        System.out.println("ValidatorsTests: ALL PASSED");
    }

    private static Person baseValidPerson() {
        return new Person(
                "ID-123",
                Type.INTERNAL,
                "Jan",
                "Kowalski",
                "+48123456789",
                "jan.kowalski@example.com",
                "72030663621"
        );
    }

    private static void testValidPersonPasses() {
        var p = baseValidPerson();
        Validators.validateNew(p);
        Validators.validateUpdate(p);
    }

    private static void testFirstNameFormat() {
        expectValidationError(baseValidPerson().withFirstName("jan"));
        expectValidationError(baseValidPerson().withFirstName("JAN"));
        expectValidationError(baseValidPerson().withFirstName("J"));

        Validators.validateUpdate(baseValidPerson().withFirstName("Łukasz"));
    }

    private static void testLastNameFormat() {
        expectValidationError(baseValidPerson().withLastName("kowalski"));
        expectValidationError(baseValidPerson().withLastName("KOWALSKI"));
        expectValidationError(baseValidPerson().withLastName("K"));

        Validators.validateUpdate(baseValidPerson().withLastName("Żurawski"));
    }

    private static void testMobileFormat() {
        expectValidationError(baseValidPerson().withMobile("+48 123456789"));
        expectValidationError(baseValidPerson().withMobile("123456789"));
        expectValidationError(baseValidPerson().withMobile("+4812345678"));

        Validators.validateUpdate(baseValidPerson().withMobile("+48987654321"));
    }

    private static void testEmailFormat() {
        expectValidationError(baseValidPerson().withEmail("jk@"));
        expectValidationError(baseValidPerson().withEmail("@example.com"));
        expectValidationError(baseValidPerson().withEmail("jk example.com"));

        Validators.validateUpdate(baseValidPerson().withEmail("jk@example.co.uk"));
    }
    
    private static void testPeselLengthAndDigits() {
        expectValidationError(baseValidPerson().withPesel("123"));
        expectValidationError(baseValidPerson().withPesel("abcdefghijk"));
        expectValidationError(baseValidPerson().withPesel("12345678901"));
    }

    private static void testPeselChecksum() {
        expectValidationError(baseValidPerson().withPesel("72030663622"));
        Validators.validateUpdate(baseValidPerson().withPesel("72030663621"));
    }


    private static void testRequiredFieldsBlankOrNull() {
        var base = baseValidPerson();

        expectValidationError(new Person(base.personId(), base.type(), null, base.lastName(), base.mobile(), base.email(), base.pesel()));
        expectValidationError(new Person(base.personId(), base.type(), " ",  base.lastName(), base.mobile(), base.email(), base.pesel()));

        expectValidationError(new Person(base.personId(), base.type(), base.firstName(), null, base.mobile(), base.email(), base.pesel()));
        expectValidationError(new Person(base.personId(), base.type(), base.firstName(), " ",  base.mobile(), base.email(), base.pesel()));

        expectValidationError(new Person(base.personId(), base.type(), base.firstName(), base.lastName(), null, base.email(), base.pesel()));
        expectValidationError(new Person(base.personId(), base.type(), base.firstName(), base.lastName(), " ",  base.email(), base.pesel()));

        expectValidationError(new Person(base.personId(), base.type(), base.firstName(), base.lastName(), base.mobile(), null, base.pesel()));
        expectValidationError(new Person(base.personId(), base.type(), base.firstName(), base.lastName(), base.mobile(), " ",  base.pesel()));

        expectValidationError(new Person(base.personId(), base.type(), base.firstName(), base.lastName(), base.mobile(), base.email(), null));
        expectValidationError(new Person(base.personId(), base.type(), base.firstName(), base.lastName(), base.mobile(), base.email(), " "));
    }

    private static void testLeadingTrailingSpacesRejected() {
        var b = baseValidPerson();

        expectValidationError(b.withFirstName(" Jan"));
        expectValidationError(b.withFirstName("Jan "));
        expectValidationError(b.withLastName(" Kowalski"));
        expectValidationError(b.withLastName("Kowalski "));

        expectValidationError(b.withMobile(" +48123456789"));
        expectValidationError(b.withEmail("jan.kowalski@example.com "));
        expectValidationError(b.withPesel(" 72030663621"));
    }

    private static void testHyphenOrMultiPartNamesAreRejectedWithCurrentRules() {
        var b = baseValidPerson();
        expectValidationError(b.withLastName("Nowak-Kowalski"));
        expectValidationError(b.withLastName("De la Cruz"));
        expectValidationError(b.withLastName("O'Connor"));
    }


    private static void testTypeRequired() {
        expectValidationError(new Person("X", null, "Jan", "Kowalski", "+48123456789", "a@b.com", "72030663621"));
    }

    private static void testPersonIdRequiredOnCreateAndUpdate() {
        var p = baseValidPerson();

        expectValidationError(new Person(null, p.type(), p.firstName(), p.lastName(), p.mobile(), p.email(), p.pesel()), true);
        expectValidationError(new Person("  ", p.type(), p.firstName(), p.lastName(), p.mobile(), p.email(), p.pesel()), true);

        expectValidationError(new Person("  ", p.type(), p.firstName(), p.lastName(), p.mobile(), p.email(), p.pesel()), false);
    }


    private static void expectValidationError(Person p) { expectValidationError(p, false); }

    private static void expectValidationError(Person p, boolean newFlow) {
        boolean thrown = false;
        try {
            if (newFlow) Validators.validateNew(p);
            else Validators.validateUpdate(p);
        } catch (ValidationException e) {
            thrown = true;
        }
        assert thrown : "Expected ValidationException for: " + p;
    }
}

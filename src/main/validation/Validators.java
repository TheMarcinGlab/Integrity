package main.validation;

import main.exception.ValidationException;
import main.Person;

import java.util.regex.Pattern;

public final class Validators {
    private Validators() {}

    private static final Pattern NAME   = Pattern.compile("^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+$");
    private static final Pattern EMAIL  = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern MOBILE = Pattern.compile("^\\+48\\d{9}$");
    private static final Pattern PESEL  = Pattern.compile("^\\d{11}$");

    public static void validateNew(Person p) {
        if (p.personId() == null || p.personId().isBlank())
            throw new ValidationException("personId is required");
        validateCommon(p);
    }

    public static void validateUpdate(Person p) {
        if (p.personId() == null || p.personId().isBlank())
            throw new ValidationException("personId is required for update");
        validateCommon(p);
    }

    private static void validateCommon(Person p) {
        if (p.type() == null)
            throw new ValidationException("type is required");

        if (p.firstName() == null || p.firstName().isBlank())
            throw new ValidationException("firstName is required");
        if (!NAME.matcher(p.firstName()).matches())
            throw new ValidationException("firstName must start with uppercase and then lowercase letters only");

        if (p.lastName() == null || p.lastName().isBlank())
            throw new ValidationException("lastName is required");
        if (!NAME.matcher(p.lastName()).matches())
            throw new ValidationException("lastName must start with uppercase and then lowercase letters only");

        if (p.mobile() == null || p.mobile().isBlank())
            throw new ValidationException("mobile is required");
        if (!MOBILE.matcher(p.mobile()).matches())
            throw new ValidationException("mobile must match format +48XXXXXXXXX");

        if (p.email() == null || p.email().isBlank())
            throw new ValidationException("email is required");
        if (!EMAIL.matcher(p.email()).matches())
            throw new ValidationException("email is invalid");

        if (p.pesel() == null || p.pesel().isBlank())
            throw new ValidationException("pesel is required");
        if (!PESEL.matcher(p.pesel()).matches())
            throw new ValidationException("pesel must be 11 digits");
        if (!isPeselChecksumValid(p.pesel()))
            throw new ValidationException("pesel checksum invalid");
    }

    private static boolean isPeselChecksumValid(String pesel) {
        int[] w = {1,3,7,9,1,3,7,9,1,3};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (pesel.charAt(i) - '0') * w[i];
        }
        int check = (10 - (sum % 10)) % 10;
        return check == (pesel.charAt(10) - '0');
    }
}

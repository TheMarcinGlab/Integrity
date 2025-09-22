# Employee Management System (XML-based)

This project implements a simple employee database using XML files as storage.  
Employees are stored in two separate directories:

- **Internal** – internal employees  
- **External** – external employees  

Each employee record includes:
- Identifier (ID)  
- Type (**Internal** / **External**)  
- First name  
- Last name  
- Phone number  
- Email address  
- PESEL (Polish national identification number)  

---

## ✨ Features

- **Add employee** – creates a new XML file with employee data.  
- **Search employees** – filter by any attribute (or a combination of them).  
- **Edit employee** – update existing employee data, including moving between directories when type changes.  
- **Delete employee** – remove an employee XML file from the system.  
- **List all employees** – retrieve and display all records.  

---

## ⚙️ Requirements

- **JDK 21+**  
  - Some features require Java 21+.  
  - To run on JDK 17, replace `getFirst()` with `get(0)`.  

- **No external dependencies** – works purely on **Java SE**.  

- **File system setup**  
  - A `data/` directory is required, containing two subdirectories:  
    - `Internal/`  
    - `External/`  
  - The application will create these folders automatically on the first run.  

---

## 📂 Data Structure

- Employee data is stored as XML files in either `Internal/` or `External/`.  

---

## 🔒 Data Validation

The system validates input fields before saving:  

- **First name & last name** – must start with an uppercase letter, Polish characters supported.  
- **Phone number** – format: `+48XXXXXXXXX`.  
- **Email address** – basic syntax validation.  
- **PESEL** – 11 digits, including checksum validation.  
- **Identifier** – required, only safe characters allowed (`A–Z, a–z, 0–9, . _ -`).  

---

## 🧪 Tests

The project includes standalone test programs (with `main` methods):  

- **ValidatorsTests** – verifies field validation rules.  
- **ServiceTests** – checks core operations (add, search, edit, delete).  


---

## 📝 Additional Notes

- XML files are saved using **atomic operations** to prevent corruption in case of crashes.  
- XML parser is configured with **secure processing mode** to mitigate XXE attacks.  
- The application provides a **simple text-based console menu** for managing employees.  

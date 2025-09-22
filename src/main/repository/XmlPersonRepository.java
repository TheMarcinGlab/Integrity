package main.repository;


import main.dto.Person;
import main.enums.Type;
import main.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class XmlPersonRepository implements PersonRepository {
    private final Path root;

    public XmlPersonRepository(Path root) throws IOException {
        this.root = Objects.requireNonNull(root, "root");
        initDirs();
    }

    private void initDirs() throws IOException {
        Files.createDirectories(root.resolve("Internal"));
        Files.createDirectories(root.resolve("External"));
    }

    private Path pathOf(Type type, String personId) {
        return root.resolve(type.toDirectoryName()).resolve(personId + ".xml");
    }

    private Optional<Path> locateById(String personId) {
        Path in = root.resolve("Internal").resolve(personId + ".xml");
        if (Files.exists(in)) return Optional.of(in);
        Path ex = root.resolve("External").resolve(personId + ".xml");
        if (Files.exists(ex)) return Optional.of(ex);
        return Optional.empty();
    }

    @Override
    public Optional<Person> findById(String personId) throws IOException {
        var p = locateById(personId);
        if (p.isEmpty()) return Optional.empty();
        return Optional.of(readPerson(p.get()));
    }

    @Override
    public List<Person> findAll() throws IOException {
        try (Stream<Path> s1 = Files.list(root.resolve("Internal"));
             Stream<Path> s2 = Files.list(root.resolve("External"))) {
            return Stream.concat(s1, s2)
                    .filter(p -> p.toString().endsWith(".xml"))
                    .map(path -> {
                        try { return readPerson(path); }
                        catch (IOException e) { throw new UncheckedIOException(e); }
                    })
                    .collect(Collectors.toList());
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public List<Person> findBy(Predicate<Person> filter) throws IOException {
        return findAll().stream().filter(filter).toList();
    }

    @Override
    public void create(Person person) throws IOException {
        Path target = pathOf(person.type(), person.personId());
        if (Files.exists(target)) throw new IOException("File already exists: " + target);
        writePersonAtomically(person, target);
    }

    @Override
    public boolean remove(String personId) throws IOException {
        var p = locateById(personId);
        if (p.isEmpty()) return false;
        return Files.deleteIfExists(p.get());
    }

    @Override
    public void update(Person person) throws IOException {
        var existing = locateById(person.personId());
        Path target = pathOf(person.type(), person.personId());
        if (existing.isPresent() && !existing.get().equals(target)) {
            Files.deleteIfExists(existing.get());
        }
        writePersonAtomically(person, target);
    }

    private Person readPerson(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            DocumentBuilder db = XmlUtils.secureDbf().newDocumentBuilder();
            Document doc = db.parse(in);

            String id     = text(doc, "personId");
            String first  = text(doc, "firstName");
            String last   = text(doc, "lastName");
            String mobile = text(doc, "mobile");
            String email  = text(doc, "email");
            String pesel  = text(doc, "pesel");

            Type type = Type.fromDirectoryName(file.getParent().getFileName().toString());

            return new Person(id, type, first, last, emptyToNull(mobile), emptyToNull(email), emptyToNull(pesel));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to parse XML: " + file, e);
        }
    }

    private static String text(Document doc, String tag) {
        var nodes = doc.getElementsByTagName(tag);
        if (nodes == null || nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent();
    }

    private static String emptyToNull(String v) { return (v == null || v.isBlank()) ? null : v; }

    private void writePersonAtomically(Person p, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");

        try (OutputStream out = Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            var db = XmlUtils.secureDbf().newDocumentBuilder();
            var doc = db.newDocument();

            Element rootEl = doc.createElement("person");
            doc.appendChild(rootEl);

            append(doc, rootEl, "personId", p.personId());
            append(doc, rootEl, "firstName", p.firstName());
            append(doc, rootEl, "lastName", p.lastName());
            append(doc, rootEl, "mobile", nvl(p.mobile()));
            append(doc, rootEl, "email",  nvl(p.email()));
            append(doc, rootEl, "pesel",  nvl(p.pesel()));

            var tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            tf.transform(new DOMSource(doc), new StreamResult(out));
        } catch (Exception e) {
            throw new IOException("Failed to write XML for: " + p.personId(), e);
        }

        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String nvl(String v) { return v == null ? "" : v; }

    private static void append(Document doc, Element parent, String tag, String text) {
        Element el = doc.createElement(tag);
        el.setTextContent(text == null ? "" : text);
        parent.appendChild(el);
    }
}

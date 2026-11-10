package nba.model;

/**
 * Abstract base class representing an individual person in the NBA ecosystem (e.g., PLayer or Coach).
 * Encapsulates shared attributes such as name and age with validation logic.
 */
public abstract class Person {

    private String name;
    private int age;

    /**
     * Constructs a new Person instance with name and age validations.
     * @param name the person's name
     * @param age the person's age (must be between 18 and 100)
     * @throws IllegalArgumentException if name is null/empty or age is out of bounds
     */
    public Person(String name, int age) {
        setName(name);
        setAge(age);
    }

    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }

    public final void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }
    public final void setAge(int age) {
        if (age < 18 || age > 100) {
            throw new IllegalArgumentException("Age must be between 18 and 100");
        }
        this.age = age;
    }

    @Override
    public String toString() {
        return name + " (" + age + " years old)";
    }

    /**
     * Returns a polymorphic summary of the person's role and performance metrics.
     * @return role summary string
     */
    public abstract String getRoleSummary();

    @Override
    public boolean equals(Object other) {
        // Identitaet
        if (this == other)
            return true;
        // null
        if (other == null)
            return false;
        // Typvergliech
        if (getClass() != other.getClass())
            return false;
        // Person-Cast
        Person otherPerson = (Person) other;
        // Attribute vergleichen
        if (!name.equals(otherPerson.getName()))
            return false;
        if (age != otherPerson.getAge())
            return false;
        // Objekte sind gleich
        return true;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, age);
    }
}
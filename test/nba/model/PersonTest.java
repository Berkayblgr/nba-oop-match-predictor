package nba.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PersonTest {

    private static class TestPerson extends Person {
        TestPerson(String name, int age) {
            super(name, age);
        }

        @Override
        public String getRoleSummary() {
            return "Test Role";
        }
    }

    @Test
    @DisplayName("Should create Person with valid name and age and return correct getter values")
    void shouldCreatePersonWithValidNameAndAge() {

        Person person = new TestPerson("Berkay Bulgur", 24);

        assertEquals("Berkay Bulgur", person.getName());
        assertEquals(24, person.getAge());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when name is null or blank")
    void shouldThrowExceptionWhenNameIsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> new TestPerson(null, 35));
        assertThrows(IllegalArgumentException.class, () -> new TestPerson("  ", 25));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when age is below 18 or above 100")
    void shouldThrowExceptionWhenAgeIsOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new TestPerson("Berkay Bulgur", 101));
        assertThrows(IllegalArgumentException.class, () -> new TestPerson("Berkay Bulgur", 17));
        assertThrows(IllegalArgumentException.class, () -> new TestPerson("Berkay Bulgur", -3));

    }

    @Test
    @DisplayName("Should format toString with name and age in years")
    void shouldReturnFormattedToString() {
        // Arrange
        Person person = new TestPerson("Berkay Bulgur", 24);

        // Act
        String actualString = person.toString();

        // Assert
        assertEquals("Berkay Bulgur (24 years old)",  actualString);
    }

    @Test
    @DisplayName("Should consider two Person objects equal when they have the same name and age")
    void shouldBeEqualWhenNameAndAgeAreSame() {
        // Arrange
        Person person1 = new TestPerson("Berkay Bulgur", 24);
        Person person2 = new TestPerson("Berkay Bulgur", 24);

        // Assert
        assertEquals(person1, person2);
        assertEquals(person1.hashCode(), person2.hashCode());
    }

    @Test
    @DisplayName("Should not consider two Person objects equal when name or age differs")
    void shouldNotBeEqualWhenNameOrAgeDiffers() {
        // Arrange
        Person person = new TestPerson("Berkay Bulgur", 24);
        Person personSameNameDiffAge = new TestPerson("Berkay Bulgur", 25);
        Person personSameAgeDiffName = new TestPerson("Alperen Sengün", 24);

        // Assert
        assertNotEquals(person, personSameNameDiffAge);
        assertNotEquals(person, personSameAgeDiffName);
    }
}
# Knowledge I gained in this project

One design pattern that was highlighted in this subject is the Builder Pattern.

## Let's learn

### Reasons to use this pattern:
We should be able to generate complex objects (many attributes) and immutable objects whenever we want!
So, there are two primary reasons to use this design pattern: **handling complexity** and **ensuring immutability**.

---

### 1. Complex object - has many attributes (Telescoping Constructor Anti-Pattern)
In a case where we need to pass a large number of attributes to a constructor, and many of them are optional, we might be tempted to create multiple overloaded constructors to fulfill every combination. For 5 optional attributes, we could theoretically end up with up to $2^5 = 32$ constructors! DAAAAAMN, that is a lot of constructors. This is known as the **Telescoping Constructor** anti-pattern.

Worse yet, in Java, you cannot even define all those constructors if their parameter types overlap (for example, two constructors accepting different single `int` attributes like `height` vs `weight` will cause a duplicate method signature compilation error).

Another common alternative is the **JavaBeans pattern** (a default no-argument constructor with setters). While this eliminates multiple constructors, it introduces a major flaw: the object can be left in an inconsistent, partially constructed state during initialization, and it completely sacrifices immutability.

That is where **The Builder Pattern** comes in handy!

### 2. Immutability
With the Builder pattern, the target class has no public setters—only getters. Its fields are marked `final` and initialized through a private constructor that takes the builder. Once the object is built, its state can never be modified. It also allows us to perform validation (e.g., checking required fields or valid ranges) before the immutable instance is even created.

---

## Builder Pattern in action

```java
public class Agent {
    // Immutable fields
    private final String name;
    private final int age;
    private final int height;
    private final int weight;
    private final String location;
    private final String countryOfOrigin;

    // Private constructor accepting the Builder
    private Agent(AgentBuilder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.height = builder.height;
        this.weight = builder.weight;
        this.location = builder.location;
        this.countryOfOrigin = builder.countryOfOrigin;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWeight() {
        return this.weight;
    }

    public String getLocation() {
        return this.location;
    }

    public String getCountryOfOrigin() {
        return this.countryOfOrigin;
    }

    public static class AgentBuilder {
        // Builder fields are mutable (not final)
        private String name;
        private int age;
        private int height;
        private int weight;
        private String location;
        private String countryOfOrigin;

        public AgentBuilder() {
        }

        public AgentBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public AgentBuilder setAge(int age) {
            this.age = age;
            return this;
        }

        public AgentBuilder setHeight(int height) {
            this.height = height;
            return this;
        }

        public AgentBuilder setWeight(int weight) {
            this.weight = weight;
            return this;
        }

        public AgentBuilder setLocation(String location) {
            this.location = location;
            return this;
        }

        public AgentBuilder setCountryOfOrigin(String countryOfOrigin) {
            this.countryOfOrigin = countryOfOrigin;
            return this;
        }

        public Agent build() {
            // Optional: validate mandatory fields or constraints here before creation
            return new Agent(this);
        }
    }
}

class Main {
    public static void main(String[] args) {
        Agent agent = new Agent.AgentBuilder()
                .setName("Smith")
                .setAge(42)
                .setHeight(180)
                .build();

        System.out.println(agent.getName()); // displays "Smith"
        System.out.println(agent.getAge());  // displays 42
    }
}
```

# Knowledge I gained in this project

One design pattern that was highlighted in this subject is the Builder Pattern.

## Let's learn

### Reasons to use this pattern:
We should be able to generate complex objects (many attributes) and Immutable objects whenever we want!
So, there are two reasons to use this desgin pattern: complexity and immutability!

---
1. Complex object - has many attributes
In a case where we would need to pass in a number of attributes to the constructor, and sometimes we don't need n attributes, so technically we could have N constructores to fulfull our constraints. So, for 5 attributes we can have up to 2^5 constructors. DAAAAAMN, that is a lot of constructors. That is when The Builder Pattern comes in handy! Let's see this in action.

2. Immutability
We could write a Java class that sets the attrbitues via the constructor without any setters, we'd only have getters. The result is an immutable Java object.


---

## Builder Pattern in action

```java
public class Agent {
    private final String name;
    private final int age;
    private final int height;
    private final int weight;
    private final String location;
    private final String countryOfOrigin;

    private Agent(String name, int age, int height, int weight,
                  String location, String countryOfOrigin) {
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.location = location;
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return age;
    }
    public int getHeight() {
        return height;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public String getLocation() {
        return location;
    }
    
    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }
    
    public static class AgentBuilder {
        private final String name;
        private final int age;
        private final int height;
        private final int weight;
        private final String location;
        private final String countryOfOrigin;
        
        public AgentBuilder() {
        }

        public Agent setName(String name, int age) {
            this.name = name;
            return this;
        }

        public AgentBuilder setAge(int age) {
            this.age = age;
            return this;
        }
        // need to add all needed settings of course!
        
        public Agent build() {
            return new Agent(this);
        }
    }
}

public class Main {
    void main() {
        Agent agent = new Agent.AgentBuilder()
                .setName("Smith")
                .setAge(42)
                .setHeight(180)
                .build();

        IO.println(agent.getName()); // displays "Smith"
        IO.println(agent.getAge()); // displays 42
    }
}
```

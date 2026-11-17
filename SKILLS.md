# Knowledge I gained in this project

One design pattern that was highlighted in this subject is the Builder Pattern.

---

## Let's learn

### Reasons to use this pattern:
1. Complex object (step by step)
2. Immutable data objects

We could write a Java class that sets the attrbitues via the constructor without any setters, we'd only have getters. The result is an immutable Java object, for example:
```java

public class Agent {
	private final String name;
	private final int age;

	public Agent(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return this.name;
	}
	
	public int getAge() {
		return this.age;
	}

	// and no setters!
}


// in Main class
public class Main {
	void main() {
		Agent agent = new Agent("Smith", 42);
		IO.println(agent.getName()); // displays "Smith"
		IO.println(agent.getAge()); // displays 42
	}
}
```

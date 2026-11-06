package com.example.cocktailbar.model;

/** Someone ordering at the bar. Only the data needed by business rules (the age check) is stored.
 * a name and an age (between 0 and 120). isAdult() returns ‘yes’ from the age of 18 exactly.
 * OrderService uses this for the ‘no alcohol for minors’ rule;
 * Order retains the customer.*/
public class Customer {

    public static final int LEGAL_DRINKING_AGE = 18;

    private static final int MAX_REALISTIC_AGE = 120;

    private final String name;
    private final int age;

    //Constructor
    public Customer(String name, int age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be blank");
        }
        if (age < 0 || age > MAX_REALISTIC_AGE) {
            throw new IllegalArgumentException("Customer age must be between 0 and " + MAX_REALISTIC_AGE + ": " + age);
        }
        this.name = name.trim();
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    /** Turning 18 today is enough: the limit is inclusive. */
    public boolean isAdult() {
        return age >= LEGAL_DRINKING_AGE;
    }
}

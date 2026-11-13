public abstract class Person {
    private String name;
    private int age;
    private String contactNum;

    public Person(String name, int age, String contactNum) {
        this.name = name;
        this.age = age;
        this.contactNum = contactNum;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getContactNum() { return contactNum; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setContactNum(String contactNum) { this.contactNum = contactNum; }

    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + ": " + name + " (" + contactNum + ")";
    }

}

package ui;

public class Employee {

	private String name;
	private String salary;
	private String age;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSalary() {
		return salary;
	}
	public void setSalary(String salary) {
		this.salary = salary;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public Employee(String name, String salary, String age) {
		super();
		this.name = name;
		this.salary = salary;
		this.age = age;
	}
	
	
	
}

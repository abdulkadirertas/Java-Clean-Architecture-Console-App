package infrastructure.user;

import entity.user.customer.Customer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerRepository implements IUserRepository<Customer> {
    private final String customersFilePath;
    private final Map<String, Customer> customerMap = new HashMap<>();

    public CustomerRepository(String customersFilePath){
        this.customersFilePath = customersFilePath;
    }

    /**
     * Adds a new customer to the repository.
     * @param id The unique identifier of the customer
     * @param customer The customer instance to be added
     */
    public void add(String id, Customer customer){
        customerMap.put(id, customer);
    }

    /**
     * Returns the customer corresponding to given id.
     * @param id The unique identifier of the customer to be retrieved.
     * @return The customer associated with the specified ID, or null if not found
     */
    public Customer get(String id){
        return customerMap.get(id);
    }

    /**
     * Fetches all entities from the repository and returns them as a list.
     * @return A list containing all customers
     */
    public List<Customer> getAll(){
        return new ArrayList<>(customerMap.values());
    }

    /**
     * Searches the repository for a customer with the specified phone number.
     * @param phone The phone number to search for
     * @return The customer if found, or null if no matching phone number exists
     */
    public Customer findByPhone(String phone){
        for(Customer customer: customerMap.values()){
            if(customer.getPhone().equals(phone)){
                return customer;
            }
        }
        return null;
    }

    /**
     * Loads customer data from the persistent storage and initializes the repository state.
     */
    public void load(){
        try(BufferedReader br = new BufferedReader(new FileReader(this.customersFilePath))) {

            String[] temp;

            String line = br.readLine(); //skip first line (titles)
            while ((line = br.readLine()) != null) {
                Customer.CustomerBuilder customerBuilder = new Customer.CustomerBuilder(); //create a customer builder
                //content of temp array: customerId;firstName;lastName;phone;password;isActive
                temp = line.split(";");

                //Set customer fields using builder
                customerBuilder.setUserId(temp[0]);
                customerBuilder.setFirstName(temp[1]);
                customerBuilder.setLastName(temp[2]);
                customerBuilder.setPhone(temp[3]);
                customerBuilder.setPassword(temp[4]);
                customerBuilder.setActive(Boolean.parseBoolean(temp[5]));
                //Build customer
                Customer customer = customerBuilder.build();
                customerMap.put(customer.getUserId(), customer);
            }
        }catch (FileNotFoundException e){
            System.out.println("File not found. "+ e.getMessage());
        }catch (IOException e){
            System.out.println("Error occurred while reading the file: " + this.customersFilePath +" " + e.getMessage());
        }
    }

    /**
     * Saves all currently existing customers to the data source
     */
    public void save(){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(this.customersFilePath))){
            //write the titles first
            bw.write("customerId;firstName;lastName;phone;password;isActive");
            bw.newLine();

            for(Customer customer: customerMap.values()){
                //csv line: customerId;firstName;lastName;phone;password;isActive
                String line = String.join(";",customer.getUserId(), customer.getFirstName(), customer.getLastName(), customer.getPhone(), customer.getPassword(), String.valueOf(customer.isActive()));
                bw.write(line);
                bw.newLine();
            }
        }catch (IOException e){
            System.out.println("Error occurred while writing to the file: " + this.customersFilePath + " " + e.getMessage());
        }
    }
}

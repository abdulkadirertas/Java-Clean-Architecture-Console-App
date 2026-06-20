package infrastructure.user;

import entity.user.barber.ApplicationState;
import entity.user.barber.Barber;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarberRepository implements IUserRepository<Barber> {
    private final String barbersFilePath;
    private final Map<String, Barber> barberMap = new HashMap<>();

    public BarberRepository(String barbersFilePath){
        this.barbersFilePath = barbersFilePath;
    }

    /**
     * Adds a new barber to the repository
     * @param id The unique identifier of the barber
     * @param barber The barber instance to be added
     */
    public void add(String id, Barber barber){
        barberMap.put(id, barber);
    }

    /**
     * Returns the barber corresponding to given id
     * @param id The unique identifier of the barber to be retrieved
     * @return The barber associated with the specified ID, or null if not found
     */
    public Barber get(String id){
        return barberMap.get(id);
    }

    /**
     * Fetches all entities from the repository and returns them as a list
     * @return A list containing all barbers
     */
    public List<Barber> getAll(){
        return new ArrayList<>(barberMap.values());
    }

    /**
     * Searches the repository for a barber with the specified phone number.
     * @param phone The phone number to search for
     * @return The barber if found, or null if no matching phone number exists
     */
    public Barber findByPhone(String phone){
        for(Barber barber: barberMap.values()){
            if(barber.getPhone().equals(phone)){
                return barber;
            }
        }
        return null;
    }

    /**
     * Loads barber data from the persistent storage and initializes the repository state
     */
    public void load(){
        try(BufferedReader br = new BufferedReader(new FileReader(this.barbersFilePath))) {

            String[] temp;

            String line = br.readLine(); //skip first line (titles)
            while ((line = br.readLine()) != null) {
                Barber.BarberBuilder barberBuilder = new Barber.BarberBuilder(); //create a barber builder
                //content of temp array: barberId;firstName;lastName;phone;password;isActive;shopName;approvalStatus
                temp = line.split(";");

                //Set barber fields using builder
                barberBuilder.setUserId(temp[0]);
                barberBuilder.setFirstName(temp[1]);
                barberBuilder.setLastName(temp[2]);
                barberBuilder.setPhone(temp[3]);
                barberBuilder.setPassword(temp[4]);
                barberBuilder.setActive(Boolean.parseBoolean(temp[5]));
                barberBuilder.setShopName(temp[6]);
                barberBuilder.setApprovalStatus(ApplicationState.valueOf(temp[7].toUpperCase()));
                //build the barber
                Barber barber = barberBuilder.build();

                barber.generateFreshSchedule(); //generate a schedule
                barberMap.put(barber.getUserId(), barber);
            }
        }catch (FileNotFoundException e){
            System.out.println("File not found. "+ e.getMessage());
        }catch (IOException e){
            System.out.println("Error occurred while reading the file: " + this.barbersFilePath +" " + e.getMessage());
        }
    }

    /**
     * Saves all currently existing barbers to the data source
     */
    public void save(){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(this.barbersFilePath))){
            //write the titles first
            bw.write("barberId;firstName;lastName;phone;password;isActive;shopName;approvalStatus");
            bw.newLine();

            for(Barber barber: barberMap.values()){
                //csv line: barberId;firstName;lastName;phone;password;isActive;shopName;approvalStatus
                String line = String.join(";",barber.getUserId(), barber.getFirstName(), barber.getLastName(), barber.getPhone(), barber.getPassword(), String.valueOf(barber.isActive()), barber.getShopName(), String.valueOf(barber.getApprovalStatus()));
                bw.write(line);
                bw.newLine();
            }
        }catch (IOException e){
            System.out.println("Error occurred while writing to the file: " + this.barbersFilePath + " " + e.getMessage());
        }
    }

}
